 package com.slauson.asteroid_dasher.status;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.slauson.asteroid_dasher.game.Game;
 
 public class Debugging {
 
 	public static int dropType = Game.POWERUP_NONE;
 	public static int level = 0;
	public static boolean levelProgression = true;
 	public static boolean godMode = false;
 	public static boolean runtimeAnalysis = false;
 	
 	private static boolean initialized = false;
 	
 	public static void load(SharedPreferences sharedPreferences) {
 		
		dropType = sharedPreferences.getInt("debugging_drop_type", dropType);
		level = sharedPreferences.getInt("debugging_level", level);
		levelProgression = sharedPreferences.getBoolean("debugging_level_progression", levelProgression);
		godMode = sharedPreferences.getBoolean("debugging_god_mode", godMode);
		runtimeAnalysis = sharedPreferences.getBoolean("debugging_runtime_analysis", runtimeAnalysis);
 		
		Options.freeVersion = sharedPreferences.getBoolean("debugging_free_version", Options.freeVersion);
 		
 		initialized = true;
 	}
 
 	public static void save(Editor sharedPreferencesEditor) {
 		
 		sharedPreferencesEditor.putInt("debugging_drop_type", dropType);
 		sharedPreferencesEditor.putInt("debugging_level", level);
 		sharedPreferencesEditor.putBoolean("debugging_level_progression", levelProgression);
 		sharedPreferencesEditor.putBoolean("debugging_god_mode", godMode);
 		sharedPreferencesEditor.putBoolean("debugging_runtime_analysis", runtimeAnalysis);
 		
 		sharedPreferencesEditor.putBoolean("debugging_free_version", Options.freeVersion);
 		
 		sharedPreferencesEditor.commit();
 	}
 	
 	/**
 	 * Returns true if the debugging info was initialized from application preferences
 	 * @return true if the debugging info was initialized from application preferences
 	 */
 	public static boolean initialized() {
 		return initialized;
 	}
 
 }
