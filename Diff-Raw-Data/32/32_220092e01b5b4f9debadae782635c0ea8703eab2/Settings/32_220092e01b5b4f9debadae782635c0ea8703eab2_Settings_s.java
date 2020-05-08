 package com.epam.AnotherSearch;
 
 import android.app.SearchableInfo;
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.epam.Suggestions.ISuggestion;
 import com.epam.Suggestions.ISuggestionsSettings;
 
 public class Settings implements ISuggestionsSettings {
 	
 	public static final String SETTINGS_PREF_FILE_NAME = "AnotherSearch.settings";
 	public boolean isSuggestionOn(ISuggestion suggestion) {
 		 SharedPreferences preferences = suggestion.getParent().
 				 getContext().getSharedPreferences(SETTINGS_PREF_FILE_NAME,
 				 Context.MODE_PRIVATE);
 		 return preferences.getBoolean(suggestionToKey(suggestion), true);
 	}
 
 	public void swithcSuggestrionOn(ISuggestion suggestion, boolean isOn) {
 		SharedPreferences preferences = suggestion.getParent().
 				 getContext().getSharedPreferences(SETTINGS_PREF_FILE_NAME,
 				 Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putBoolean(suggestionToKey(suggestion), isOn); 
 		editor.commit();
 	}
 	
 	private String suggestionToKey(ISuggestion suggestion)
 	{
 		SearchableInfo info = suggestion.getSearchable();
 		String suggestionPackage = info.getSuggestPackage();
 		String suggestionAuthority = info.getSuggestAuthority();
 		String suggestionPath= info.getSuggestPath();
		return suggestionPackage + ":" + suggestionAuthority  + ":" + suggestionPath; 
 	}
 
 }
