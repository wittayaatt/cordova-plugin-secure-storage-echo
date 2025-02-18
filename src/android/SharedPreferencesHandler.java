package com.crypho.plugins;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import android.content.SharedPreferences;
import android.content.Context;

import android.os.Build;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SharedPreferencesHandler {
	private SharedPreferences prefs;

	public SharedPreferencesHandler (String prefsName, Context ctx){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                MasterKey key = new MasterKey.Builder(ctx)
                    .setKeyGenParameterSpec(
                            new KeyGenParameterSpec
                                    .Builder(MasterKey.DEFAULT_MASTER_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                    .setKeySize(256).build())
                    .build();
                prefs = EncryptedSharedPreferences.create(
                    ctx,
                    prefsName  + "_SS",
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (Exception ex) {
                // Cannot contruct Encrypted storage - fall back
                prefs = ctx.getSharedPreferences(prefsName  + "_SS", 0);
            }
        } else {
            prefs = ctx.getSharedPreferences(prefsName  + "_SS", 0);
        }
	}

	boolean isEmpty() {
	    return prefs.getAll().isEmpty();
    }

    public void store(String key, String value){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("_SS_" + key, value);
        editor.commit();
    }

    String fetch (String key){
        return prefs.getString("_SS_" + key, null);
    }

    void remove (String key){
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("_SS_" + key);
        editor.commit();
    }

    Set keys (){
        Set res = new HashSet<String>();
    	Iterator<String> iter = prefs.getAll().keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.startsWith("_SS_")  && !key.startsWith("_SS_MIGRATED_")) {
                res.add(key.replaceFirst("^_SS_", ""));
            }
        }
        return res;
    }

    void clear (){
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
