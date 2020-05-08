 package com.felixware.gw2w.utilities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.Context;
 
 import com.felixware.gw2w.R;
 
 public class Constants {
 	public static final String BASE_URL_ENGLISH = "http://wiki.guildwars2.com";
 	public static final String BASE_URL_GERMAN = "http://wiki-de.guildwars2.com";
 	public static final String BASE_URL_SPANISH = "http://wiki-es.guildwars2.com";
 	public static final String BASE_URL_FRENCH = "http://wiki-fr.guildwars2.com";
 
 	public static final String DOMAIN_ENGLISH = "wiki.guildwars2.com";
 	public static final String DOMAIN_GERMAN = "wiki-de.guildwars2.com";
 	public static final String DOMAIN_SPANISH = "wiki-es.guildwars2.com";
 	public static final String DOMAIN_FRENCH = "wiki-fr.guildwars2.com";
 
 	public static final String MAIN_PAGE_ENGLISH = "Main Page";
 	public static final String MAIN_PAGE_GERMAN = "Hauptseite";
	public static final String MAIN_PAGE_SPANISH = "Pgina principal";
 	public static final String MAIN_PAGE_FRENCH = "Accueil";
 
 	// not currently used
 	public static String userAgentString = "GW2WApp/1.0 (https://github.com/Felixomni/gw2wiki-android; felixomni@gmail.com)";
 
 	public static final int ERROR_CONNECTION = 0;
 	public static final int ERROR_PAGE_DOES_NOT_EXIST = 1;
 	public static final int ERROR_SERVER = 2;
 
 	public static final int ENGLISH = 0;
 	public static final int GERMAN = 1;
 	public static final int SPANISH = 2;
 	public static final int FRENCH = 3;
 
 	public static String getLanguage(Context context, int language) {
 		switch (language) {
 		case Constants.ENGLISH:
 			return context.getResources().getString(R.string.language_english);
 		case Constants.GERMAN:
 			return context.getResources().getString(R.string.language_german);
 		case Constants.SPANISH:
 			return context.getResources().getString(R.string.language_spanish);
 		case Constants.FRENCH:
 			return context.getResources().getString(R.string.language_french);
 		default:
 			return null;
 		}
 	}
 
 	public static String getDomain(Context context) {
 		int language = PrefsManager.getInstance(context).getWikiLanguage();
 		switch (language) {
 		case Constants.ENGLISH:
 			return DOMAIN_ENGLISH;
 		case Constants.GERMAN:
 			return DOMAIN_GERMAN;
 		case Constants.SPANISH:
 			return DOMAIN_SPANISH;
 		case Constants.FRENCH:
 			return DOMAIN_FRENCH;
 		default:
 			return null;
 		}
 	}
 
 	public static String getBaseURL(Context context) {
 		int language = PrefsManager.getInstance(context).getWikiLanguage();
 		switch (language) {
 		case Constants.ENGLISH:
 			return BASE_URL_ENGLISH;
 		case Constants.GERMAN:
 			return BASE_URL_GERMAN;
 		case Constants.SPANISH:
 			return BASE_URL_SPANISH;
 		case Constants.FRENCH:
 			return BASE_URL_FRENCH;
 		default:
 			return null;
 		}
 	}
 
 	public static String getStartPage(Context context) {
 		int language = PrefsManager.getInstance(context).getWikiLanguage();
 		switch (language) {
 		case Constants.ENGLISH:
 			return MAIN_PAGE_ENGLISH;
 		case Constants.GERMAN:
 			return MAIN_PAGE_GERMAN;
 		case Constants.SPANISH:
 			return MAIN_PAGE_SPANISH;
 		case Constants.FRENCH:
 			return MAIN_PAGE_FRENCH;
 		default:
 			return null;
 		}
 	}
 
 	public static List<String> getFavoritesListFromJSON(Context context) {
 		List<String> favoritesList = new ArrayList<String>();
 		String favoritesJSONString = PrefsManager.getInstance(context).getFavorites();
 		if (favoritesJSONString == null) {
 			return favoritesList;
 		}
 		try {
 			JSONArray favoritesJSONArray = new JSONArray(favoritesJSONString);
 			for (int i = 0; i < favoritesJSONArray.length(); i++) {
 				favoritesList.add(favoritesJSONArray.getString(i));
 			}
 		} catch (JSONException e) {
 		}
 		return favoritesList;
 	}
 
 	public static String getJSONStringFromList(List<String> favorites) {
 		JSONArray favoritesJSONArray = new JSONArray();
 		for (String pageName : favorites) {
 			favoritesJSONArray.put(pageName);
 		}
 		return favoritesJSONArray.toString();
 	}
 }
