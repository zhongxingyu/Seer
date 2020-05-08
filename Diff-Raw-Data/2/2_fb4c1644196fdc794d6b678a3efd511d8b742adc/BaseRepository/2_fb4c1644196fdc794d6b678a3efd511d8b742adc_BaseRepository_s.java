 package com.capsule.android.repositories;
 
 import com.capsule.common.SharePreferencesEditor;
 
 import android.content.Context;
 
 public abstract class BaseRepository {
 
 	protected Context myContext = null;
 	protected SharePreferencesEditor mySettingEditor = null; 
 	
 	public BaseRepository(Context context)
 	{
 		myContext = context;
		mySettingEditor = new SharePreferencesEditor(this,SharePreferencesEditor.SettingsName);
 	}
 }
