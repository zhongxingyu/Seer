 package com.quizz.core.utils;
 
 import java.util.Locale;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.quizz.core.models.Level;
 
 public class PreferencesUtils {
 
 	public static final String PREF_VERSION_KEY = "PreferencesUtils.VERSION";
 	public static final String PREF_UNLOCKED_HINTS_COUNT_KEY = "PreferencesUtils.UNLOCKED_HINTS_COUNT";
 	public static final String PREF_DEFAULT_NB_HINTS_ONSUCCESS_KEY = "PreferencesUtils.DEFAULT_NB_HINTS_ONSUCCESS";
 	public static final String PREF_AUDIO_KEY = "PreferencesUtils.AUDIO";
 	public static final String PREF_VIBRATION_KEY = "PreferencesUtils.VIBRATION";
 	public static final String PREF_EXIT_POPUP_KEY = "PreferencesUtils.EXIT_POPUP";
 	public static final String PREF_LAST_PLAYED_LEVEL_KEY = "PreferencesUtils.PREF_LAST_LEVEL_PLAYED_KEY";
 	public static final String PREF_UNLOCKED_LETTERS_KEY = "PreferencesUtils.PREF_UNLOCKED_LETTERS_KEY";
 	public static final String PREF_USED_HINTS_COUNT_KEY = "PreferencesUtils.PREF_USED_HINTS_COUNT_KEY";
 	public static final String PREF_INFORMATION_UNLOCKED_KEY = "PreferencesUtils.PREF_INFORMATION_UNLOCKED_KEY";
 
 	public static final int PREF_VERSION_VALUE = 1;
 	public static final int PREF_DEFAULT_UNLOCKED_HINTS_COUNT_VALUE = 3;
 	public static final int PREF_DEFAULT_NB_HINTS_ONSUCCESS_VALUE = 1;
 	
 
 	public static SharedPreferences getSharedPreferences(Context context) {
 		String getSharedPreferencesKey = context.getPackageName();
 		return context.getSharedPreferences(getSharedPreferencesKey, Activity.MODE_PRIVATE);
 	}
 	
 	public static boolean containsVibrationPreference(Context context) {
 		return getSharedPreferences(context).contains(PREF_VIBRATION_KEY);		
 	}
 	
 	public static boolean containsAudioPreference(Context context) {
 		return getSharedPreferences(context).contains(PREF_AUDIO_KEY);
 	}
 	
 	public static boolean containsExitPopupPreference(Context context) {
 		return getSharedPreferences(context).contains(PREF_EXIT_POPUP_KEY);
 	}
 
 	public static int getHintsAvailable(Context context) {
 		return getSharedPreferences(context).getInt(PREF_UNLOCKED_HINTS_COUNT_KEY, 0);
 	}
 
 	public static boolean isVibrationEnabled(Context context) {
 		return getSharedPreferences(context).getBoolean(PREF_VIBRATION_KEY, false);		
 	}
 	
 	public static boolean isAudioEnabled(Context context) {
 		return getSharedPreferences(context).getBoolean(PREF_AUDIO_KEY, false);
 	}
 	
 	public static boolean isExitPopupEnabled(Context context) {
 		return getSharedPreferences(context).getBoolean(PREF_EXIT_POPUP_KEY, true);
 	}
 
 	public static int getLastPlayedLevel(Context context, int sectionId) {
 		return getSharedPreferences(context).getInt(PREF_LAST_PLAYED_LEVEL_KEY+"_s"+sectionId, -1);
 	}
 
 	public static Boolean isInformationUnlocked(Context context, Level level) {
 		String sectionId = "_s"+level.sectionId;
 		String levelId = "_l"+level.id;
 		return getSharedPreferences(context).getBoolean(
 				PREF_INFORMATION_UNLOCKED_KEY+sectionId+levelId, false);
 	}
 	
 	public static String getUnlockedLetters(Context context, Level level) {
 		String lang = Locale.getDefault().getLanguage();
 		String sectionId = "_s"+level.sectionId;
 		String levelId = "_l"+level.id;
 		return getSharedPreferences(context).getString(
 				PREF_UNLOCKED_LETTERS_KEY+sectionId+levelId+lang, "");
 	}
 
 	public static void setVibrationEnabled(Context context, boolean status) {		
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putBoolean(PREF_VIBRATION_KEY, status);		
 		editor.commit();
 	}
 	
 	public static void setAudioEnabled(Context context, boolean status) {
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putBoolean(PREF_AUDIO_KEY, status);
 		editor.commit();
 	}
 	
 	public static void setExitPopupEnabled(Context context, boolean status) {
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putBoolean(PREF_EXIT_POPUP_KEY, status);
 		editor.commit();
 	}
 	
 	public static void setHintsAvailable(Context context, int hintsNumber) {
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putInt(PREF_UNLOCKED_HINTS_COUNT_KEY, hintsNumber);
 		editor.commit();
 	}
 	
 	public static void removeUnlockedLetters(Context context, Level level) {
 		removeUnlockedLetters(context, level, true);
 	}
 
 	public static void resetGame(Context context) {
 		SharedPreferences sharedPrefs = getSharedPreferences(context);
 		Map<String, ?> allPrefs = sharedPrefs.getAll();
 
 		Editor editor = sharedPrefs.edit();
 		editor.putInt(PREF_UNLOCKED_HINTS_COUNT_KEY, PREF_DEFAULT_UNLOCKED_HINTS_COUNT_VALUE);
 		editor.putInt(PREF_USED_HINTS_COUNT_KEY, 0);
 		for (String pref : allPrefs.keySet()) {
 			if (pref.startsWith(PREF_UNLOCKED_LETTERS_KEY) 
 					|| pref.startsWith(PREF_LAST_PLAYED_LEVEL_KEY)
 					|| pref.startsWith(PREF_INFORMATION_UNLOCKED_KEY)) {
 				editor.remove(pref);
 			}
 		}
 		editor.commit();
 	}
 
 	public static void removeUnlockedLetters(Context context, Level level, boolean allLangs) {
 		String lang = Locale.getDefault().getLanguage();
 		String sectionId = "_s"+level.sectionId;
 		String levelId = "_l"+level.id;
 		
 		SharedPreferences sharedPrefs = getSharedPreferences(context);
 		Map<String, ?> allPrefs = sharedPrefs.getAll();
 		
 		Editor editor = sharedPrefs.edit();
 		for (String pref : allPrefs.keySet()) {
 			if (allLangs && pref.startsWith(PREF_UNLOCKED_LETTERS_KEY+sectionId+levelId)) {
 				editor.remove(pref);
 			} else if (pref.equals(PREF_UNLOCKED_LETTERS_KEY+sectionId+levelId+lang)) {
 				editor.remove(pref);
 			}
 		}
 		editor.commit();
 	}
 	
 	public static void setUnlockedLetters(Context context, Level level, String letters) {
 		String lang = Locale.getDefault().getLanguage();
 		String sectionId = "_s"+level.sectionId;
 		String levelId = "_l"+level.id;
 		
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putString(PREF_UNLOCKED_LETTERS_KEY+sectionId+levelId+lang, letters);
 		editor.commit();
 	}
 	
 	public static void setInformationUnlocked(Context context, Level level, boolean unlocked) {
 		String sectionId = "_s"+level.sectionId;
 		String levelId = "_l"+level.id;
 		
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putBoolean(PREF_INFORMATION_UNLOCKED_KEY+sectionId+levelId, unlocked);
 		editor.commit();
 	}
 	
 	public static void removeLastPlayedLevel(Context context, int sectionId) {
 		Editor editor = getSharedPreferences(context).edit();
 		editor.remove(PREF_LAST_PLAYED_LEVEL_KEY+"_s"+sectionId);
 		editor.commit();
 	}
 	
 	public static void setLastPlayedLevel(Context context, int sectionId, int levelId) {
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putInt(PREF_LAST_PLAYED_LEVEL_KEY+"_s"+sectionId, levelId);
 		editor.commit();
 	}
 	
 	public static void setUsedHintsCount(Context context, int usedHintCount) {
 		Editor editor = getSharedPreferences(context).edit();
 		editor.putInt(PREF_USED_HINTS_COUNT_KEY, usedHintCount);
 		editor.commit();
 	}
 
 	public static void incrementUsedHintsCount(Context context, int usedHintCount) {
 		setUsedHintsCount(context, getUsedHintsCount(context) + usedHintCount);
 	}
 	
 	public static int getUsedHintsCount(Context context) {
 		return getSharedPreferences(context).getInt(PREF_USED_HINTS_COUNT_KEY, 0);
 	}
 	
 }
