 package com.battleships.base;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 
 public class BSPreferences {
     private static final String PREFS_NAME = "config";
     private static final String PREFS_SERVER_IP = "server_ip";
     private static final String PREFS_USER_NAME = "user_name";
    private static final Preferences prefs = Gdx.app.getPreferences( PREFS_NAME );
     
     public void setUserName(String uName) {
    	prefs.putString(PREFS_USER_NAME, uName);
    	prefs.flush();
     }
     
     public String getUserName() {
    	return prefs.getString(PREFS_USER_NAME, "Battleship");
     }
     
     public void setServerIp(String sIP) {
    	prefs.putString(PREFS_SERVER_IP, sIP);
    	prefs.flush();
     }
     
     public String getServerIp() {
    	return prefs.getString(PREFS_SERVER_IP, "192.168.198.185");
     }
     
 }
