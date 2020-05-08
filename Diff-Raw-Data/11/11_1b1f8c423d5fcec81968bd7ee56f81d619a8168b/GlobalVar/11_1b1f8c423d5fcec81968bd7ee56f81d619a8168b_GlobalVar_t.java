 package com.ad.cow;
 
 import java.util.Date;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class GlobalVar extends Application {	
 	private int _level = 0;
 	private float _percent = 0.0f;
 	private long _time = 0;
 	private long _expTime = 0;
 	private float _exp = 0.0f;
 	
 	public int getLevel() {
 		return _level;
 	}
 	
 	public float getPercent() {
 		return _percent;
 	}
 	
 	public long getTime() {
 		return _time;
 	}
 	
 	public long getExpTime() {
 		return _expTime;
 	}
 	
 	
 	public float getExp() {
 		return _exp;
 	}
 	
 	
 	public void setLevel(int level) {
 		this._level = level;
 	}
 	
 	public void setPercent(float percent) {
 		this._percent = percent;
 	}
 	
 	public void setTime(long time) {
 		this._time = time;
 	}
 	
 	public void setExpTime(long expTime) {
 		this._expTime = expTime;
 	}
 	
 	public void setExp(float exp) {
 		this._exp = exp;
 	}
 	
 	/**
 	 **********************************************
 	 */
 	private SharedPreferences loader;
 	private static GlobalVar instance;
 
 	static {
 		instance = new GlobalVar();
 	}
 	
 	private GlobalVar() {
 	}
 	
 	public static GlobalVar getInstance(Context context) {
 		GlobalVar gv = GlobalVar.instance;
 		gv.LoadPreferences(context);
 		return gv;
 	}
 	
 	private void LoadPreferences(Context context) {
 		loader = PreferenceManager.getDefaultSharedPreferences(context);
 		this._level = loader.getInt("level", 0);
		this._percent = loader.getFloat("percent", 0.0f);
 		this._time = loader.getLong("time", new Date().getTime());
 		this._expTime = loader.getLong("exp_time", new Date().getTime());
 		this._exp = loader.getFloat("exp", 0.0f);		
 	}
 	
 	public static GlobalVar getInstance() {
 		return GlobalVar.instance;
 	}
 	
 	public void save() {
 		SharedPreferences.Editor saver = loader.edit();
 		saver.putInt("level", _level);
 		saver.putFloat("percent", _percent);
 		saver.putLong("time", _time);
 		saver.putLong("exp_time", _expTime);
 		saver.putFloat("exp", _exp);
 		saver.commit();
 	}
 }
