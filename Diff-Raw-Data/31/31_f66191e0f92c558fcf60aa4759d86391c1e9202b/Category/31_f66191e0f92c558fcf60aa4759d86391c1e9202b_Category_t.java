 /*
  * Copyright (C) 2013 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.morphoss.jumble.models;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 
 import com.morphoss.jumble.Util;
 import com.morphoss.jumble.database.JumbleCategoryTable;
 import com.morphoss.jumble.database.JumbleProvider;
 import com.morphoss.jumble.frontend.CategoryGridAdapter;
 import com.morphoss.jumble.frontend.CategoryScreenActivity;
 import com.morphoss.jumble.frontend.SettingsActivity;
 
 public class Category {
 
 	/**
 	 * This class sets all the details about the a Category type
 	 */
 	public static String TAG = "Category";
	public static ArrayList<String> unlockedCategories = new ArrayList<String>();
 	private int id;
 	private HashMap<String, String> names;
 	private static final String defaultCC = "en";
 	private String imagePath;
 	private boolean unlocked = false;
 	private ArrayList<Word> words = new ArrayList<Word>();
 	private ArrayList<String> solved = new ArrayList<String>();
 	private ArrayList<Word> wordsEasy = new ArrayList<Word>();
 	private ArrayList<Word> wordsMedium = new ArrayList<Word>();
 	private ArrayList<Word> wordsAdvanced = new ArrayList<Word>();
 
 	/**
 	 * 
 	 * @param context
 	 *            required to work
 	 * @return the drawable of the category
 	 */
 	public BitmapDrawable getImage(Context context) {
 
 		return Util.getImage(context, imagePath);
 	}
 
 	private Category(int id, JSONObject data) throws JSONException {
 		this.id = id;
 		;
 		this.imagePath = data.getString("image");
 
 		JSONObject translations = data.getJSONObject("translations");
 		names = new HashMap<String, String>();
 		JSONArray keys = translations.names();
 		for (int i = 0; i < keys.length(); i++) {
 			String key = (String) keys.get(i);
 			String name = translations.getString(key);
 			names.put(key, name);
 			Log.d(TAG, "mapping key to name: " + key + ":" + name);
 		}
 
 		Log.d(TAG, "Created category : " + this);
 	}
 
 	/**
 	 * This method gets all the categories informations from the JSON file
 	 * 
 	 * @param json
 	 * @return
 	 * @throws JSONException
 	 */
 	public static HashMap<Integer, Category> getCategoriesFromJson(
 			JSONObject json) throws JSONException {
 		Log.d(TAG, "Creating Category List from json: " + json);
 		HashMap<Integer, Category> categories = new HashMap<Integer, Category>();
 		JSONArray keys = json.names();
 		for (int i = 0; i < keys.length(); i++) {
 			String key = (String) keys.get(i);
 			JSONObject data = json.getJSONObject(key);
 			int id = Integer.parseInt(key);
 			categories.put(id, new Category(id, data));
 
 		}
 		return categories;
 
 	}
 
 	@Override
 	public String toString() {
 		String value = "Id: " + id + " ImagePath: " + imagePath
 				+ "\nTranslations:\n";
 		for (String key : names.keySet())
 			value += "\t" + key + ":" + names.get(key) + "\n";
 		value += "Words:\n";
 		for (Word w : words)
 			value += "\t" + w.toString() + "\n";
 		return value;
 	}
 
 	public void addWord(Word word) {
 		words.add(word);
 	}
 
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * This method get the localised name of a word if useful if the country
 	 * code is not en
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public String getLocalisedName() {
 		String cc = SettingsActivity.getLanguageToLoad();
 
 		// Log.d(TAG, "Current CC is " + cc);
 		if (names.containsKey(cc)) {
 			return names.get(cc);
 		}
 		return names.get(defaultCC);
 	}
 
 	public String getName() {
 		return names.get(defaultCC);
 	}
 
 	public int size() {
 		return words.size();
 	}
 
 	/**
 	 * 
 	 * @param context
 	 *            required to work
 	 * @return the next word of the available ones
 	 */
 	public Word getNextWord(Context context) {
 
 		ArrayList<String> tempsolved = CategoryWords.getSolvedWordsFromCategory(context, this);
 		double ratioSolved = (double) tempsolved.size() / (double) words.size();
 		Log.d(TAG, "size of solved list : " + tempsolved.size());
 		Log.d(TAG, "size of words list : " + words.size());
 		Log.d(TAG, "ratio solved : " + ratioSolved);
 		Category nextCategory = CategoryGridAdapter.getCategory(getId());
 		Log.d(TAG, "next category name : " + nextCategory.getLocalisedName());
 		if (!nextCategory.unlocked() && ratioSolved >= 0.2) {
 			Log.d(TAG, "unlocking a new category");
			unlockedCategories.add(nextCategory.getLocalisedName());
 			ContentValues cv = new ContentValues();
 			cv.put(JumbleCategoryTable.UNLOCK, "1");
 			cv.put(JumbleCategoryTable.CATEGORY,
 					nextCategory.getLocalisedName());
 			context.getContentResolver().insert(
 					JumbleProvider.CONTENT_URI_CATEGORIES, cv);
 			nextCategory.setUnlocked(true);
 		}
 
 		int countEasyWords = wordsEasy.size();
 		Log.d(TAG, "count of easy words :" + countEasyWords);
 		int countMediumWords = wordsMedium.size();
 		Log.d(TAG, "count of medium words :" + countMediumWords);
 		int countAdvancedWords = wordsAdvanced.size();
 		Log.d(TAG, "count of advanced words :" + countAdvancedWords);
 
 		ArrayList<Word> filteredwords = new ArrayList<Word>();
 		filteredwords.addAll(wordsEasy);
 		if (filteredwords.size() < 3) filteredwords.addAll(wordsMedium);
 		if (filteredwords.size() < 3) filteredwords.addAll(wordsAdvanced);
 		if (filteredwords.size() == 0 ) return null;
 
 		Word word = CategoryWords.getRandomItem(filteredwords);
 		ArrayList<Word> wordList;
 		Log.d(TAG, "the random word is : "+word.getLocalisedWord()+" with level :"+word.getLevel());
 		switch(word.getLevel()){
 		case EASY:
 			wordList = wordsEasy;
 			break;
 		case MEDIUM:
 			wordList = wordsMedium;
 			break;
 		case ADVANCED:
 			wordList = wordsAdvanced;
 			break;
 		default:
 			wordList = wordsAdvanced;
 			break;
 		}
 		for(int i=0; i<wordList.size(); i++){
 			if(wordList.get(i).equals(word)){
 				wordList.remove(i);
 				break;
 			}
 		}
 		return word;
 	}
 
 	public void getNewWords(Context context) {
 
 		ArrayList<Word> tempWords = this.words;
 		tempWords = CategoryWords.getLocalisedWordsFromCategory(tempWords);
 		solved = CategoryWords.getSolvedWordsFromCategory(context, this);
 		tempWords = CategoryWords.removeKnownWordsFromList(context, tempWords,
 				solved);
 		wordsEasy = CategoryWords.getWordsByDifficulty(tempWords,
 				Difficulty.EASY);
 		printWordList("All Easy Available Words", wordsEasy);
 		wordsMedium = CategoryWords.getWordsByDifficulty(tempWords,
 				Difficulty.MEDIUM);
 		printWordList("All Medium Available Words", wordsMedium);
 		wordsAdvanced = CategoryWords.getWordsByDifficulty(tempWords,
 				Difficulty.ADVANCED);
 		printWordList("All Advanced Available Words", wordsAdvanced);
 
 
 	}
 
 	/**
 	 * This method creates a log to show on logcat what are the available words
 	 * 
 	 * @param message
 	 * @param list
 	 */
 	public static void printWordList(String message, ArrayList<?> list) {
 		// log message displaying all the elements of list
 		Log.d(TAG, message);
 		String listString = "";
 		for (Object o : list)
 			listString += o + ",";
 		Log.d(TAG, listString);
 	}
 
 	public boolean unlocked() {
 		return unlocked;
 	}
 
 	public void setUnlocked(boolean unlocked) {
 		this.unlocked = unlocked;
 	}
 }
