 package com.battleships.base;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 
 public class BSPreferences {
     private static final String PREFS_NAME = "config";
     private static final String PREFS_SERVER_IP = "server_ip";
     private static final String PREFS_USER_NAME = "user_name";
    
    protected Preferences getPrefs() {
    	return Gdx.app.getPreferences( PREFS_NAME );
    }
     
     public void setUserName(String uName) {
    	getPrefs().putString(PREFS_USER_NAME, uName);
    	getPrefs().flush();
     }
     
     public String getUserName() {
    	return getPrefs().getString(PREFS_USER_NAME, "Battleship");
     }
     
     public void setServerIp(String sIP) {
    	getPrefs().putString(PREFS_SERVER_IP, sIP);
    	getPrefs().flush();
     }
     
     public String getServerIp() {
    	return getPrefs().getString(PREFS_SERVER_IP, "192.168.198.185");
     }
     
 }
