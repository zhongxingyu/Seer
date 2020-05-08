 package info.vanderkooy.ucheck;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 
 public class Preferences {
 	private SharedPreferences pref;
 	private SafeSharedPrefs safePref;
 
 	public Preferences(Context ctx) {
 		pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		safePref = new SafeSharedPrefs(ctx, pref);
 	}
 
 	public boolean setUsername(String username) {
		pref.edit().putString("username", username);
		return pref.edit().commit();
 	}
 
 	public String getUsername() {
 		return pref.getString("username", "");
 	}
 
 	public boolean setPassword(String password) {	
		safePref.edit().putString("password", password);
		return pref.edit().commit();
 	}
 
 	public String getPassword() {
 		return safePref.getString("password", "");
 	}
 	
 	public Editor edit() {
		return pref.edit();		
 	}
 }
