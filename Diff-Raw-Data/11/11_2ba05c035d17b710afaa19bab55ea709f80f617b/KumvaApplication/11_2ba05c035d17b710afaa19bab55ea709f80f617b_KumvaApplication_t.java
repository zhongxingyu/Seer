 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Kumva.
  *
  * Kumva is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Kumva is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Kumva. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.kumva;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ijuru.kumva.util.Dialogs;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 
 /**
  * The main Kumva application
  */
 public class KumvaApplication extends Application {
 	
 	private List<Dictionary> dictionaries = new ArrayList<Dictionary>();
 	private Dictionary activeDictionary = null;
 	private Definition definition;
 	
 	private final String PREF_FILE_DICTS = "dictionaries";
 	private final String PREF_KEY_ACTIVEDICT = "active_dict";
 
 	/**
 	 * @see android.app.Application#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		loadDictionaries();
 		
 		// Get active dictionary from preferences
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String activeDictURL = prefs.getString(PREF_KEY_ACTIVEDICT, null);
 		if (activeDictURL != null)
 			activeDictionary = getDictionaryByURL(activeDictURL);
 		
 		// Add Kinyarwanda.net if there are no dictionaries
 		if (this.dictionaries.size() == 0) {
 			Dictionary kinyaDict = new Dictionary("http://kinyarwanda.net", "Kinyarwanda.net", "?", "rw", "en");
 			this.dictionaries.add(kinyaDict);
 			this.activeDictionary = kinyaDict;
 		}
 	}
 		
 	/**
 	 * @see android.app.Application#onTerminate()
 	 */
 	@Override
 	public void onTerminate() {
 		super.onTerminate();
 		
 		saveDictionaries();
 	}
 
 	/**
 	 * Loads the dictionaries from the dictionary preferences file
 	 */
 	public void loadDictionaries() {
 		// Get the dictionaries shared pref file
 		SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREF_FILE_DICTS, MODE_PRIVATE);
 		
 		// Remove existing dictionaries
 		this.dictionaries.clear();
 		
 		for (Object dictCSV : prefs.getAll().values()) {
 			String[] fields = ((String)dictCSV).split(",");
 			if (fields.length == 5) {
 				String url = fields[0];
 				String name = fields[1];
 				String version = fields[2];
 				String defLang = fields[3];
 				String meanLang = fields[4];
 				addDictionary(new Dictionary(url, name, version, defLang, meanLang));
 			}
 			else {
 				Dialogs.toast(this, getString(R.string.err_dictloading));
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Saves the dictionaries to the dictionary preferences file
 	 */
 	public void saveDictionaries() {
 		// Get the dictionaries a special preference file
 		SharedPreferences prefs = getSharedPreferences(PREF_FILE_DICTS, MODE_PRIVATE);
 		Editor editor = prefs.edit();
 		editor.clear();
 		int dict = 1;
 		for (Dictionary dictionary : dictionaries)
 			editor.putString("site" + (dict++), dictionary.toString());
 
 		editor.commit();
 			
 		// Save active dictionary as a default preference
 		if (activeDictionary != null) {
 			prefs = PreferenceManager.getDefaultSharedPreferences(this);
 			editor = prefs.edit();
 			editor.putString(PREF_KEY_ACTIVEDICT, activeDictionary.getURL());
 			editor.commit();
 		}
 	}
 
 	/**
 	 * Gets the list of dictionaries available
 	 * @return the dictionaries
 	 */
 	public List<Dictionary> getDictionaries() {
 		return dictionaries;
 	}
 	
 	/**
 	 * Gets the dictionary with the given url
 	 * @return the dictionary or null
 	 */
 	public Dictionary getDictionaryByURL(String url) {
 		for (Dictionary dict : dictionaries)
 			if (url.equals(dict.getURL()))
 				return dict;
 		return null;
 	}
 
 	/**
 	 * Gets the active dictionary
 	 * @return the active dictionary
 	 */
 	public Dictionary getActiveDictionary() {
 		return this.activeDictionary;
 	}
 	
 	/**
 	 * Sets the active dictionary
 	 * @param dictionary the active dictionary
 	 */
 	public void setActiveDictionary(Dictionary dictionary) {
 		this.activeDictionary = dictionary;
 	}
 	
 	/**
 	 * Adds the specified dictionary
 	 * @param dictionary the dictionary to add
 	 */
 	public void addDictionary(Dictionary dictionary) {
 		// If its the only dictionary then make it active
 		if (this.dictionaries.size() == 0)
 			this.activeDictionary = dictionary;
 		
 		this.dictionaries.add(dictionary);
 	}
 	
 	/**
 	 * Removes the specified dictionary
 	 * @param dictionary the dictionary to delete
 	 */
 	public void removeDictionary(Dictionary dictionary) {
 		this.dictionaries.remove(dictionary);
 		
 		if (dictionary == activeDictionary)
 			activeDictionary = null;
 	}
 
 	/**
 	 * Gets the currently viewed definition
 	 * @return the definition
 	 */
 	public Definition getCurrentDefinition() {
 		return definition;
 	}
 
 	/**
 	 * Sets the currently viewed definition
 	 * @param definition the definition to set
 	 */
 	public void setCurrentDefinition(Definition definition) {
 		this.definition = definition;
 	}
 }
