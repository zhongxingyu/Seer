 package com.mates120.myword;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.net.Uri;
 
 public class AvailableDictionaries
 {   // Implements Singleton pattern (no multithreading yet)
 	private List<Dictionary> knownDictionaries;
 	private KnownDictionariesDB dictsDB;
 	private PackageManager pacMan;
 	ContentResolver contentResolver;
 	
 	private static final String DICTIONARY_PACKAGE = "com.mates120.dictionary.";
 	private static AvailableDictionaries uniqueInstance;
 	
 	private AvailableDictionaries(Context context)
 	{
 		dictsDB = new KnownDictionariesDB(context);
 		pacMan = context.getPackageManager();
 		contentResolver = context.getContentResolver();
 	}
 	
 	public static AvailableDictionaries getInstance(Context context)
 	{
 		if (uniqueInstance == null)
 			uniqueInstance = new AvailableDictionaries(context);
 		return uniqueInstance;
 	}
 	
 	public List<Dictionary> getList()
 	{
 		return knownDictionaries;
 	}
 	
 	public void refreshList()
 	{
 		obtainKnownDictionariesList();
 		List<String> allDicts = obtainInstalledDictionariesList();
 		boolean thereAreNew = insertNewlyInstalledDictionaries(allDicts);
 		boolean thereAreDeleted = cleanupAlreadyDeletedDictionaries(allDicts);
 		if (thereAreNew || thereAreDeleted)
 			obtainKnownDictionariesList();
 	}
 	
 	private void obtainKnownDictionariesList()
 	{
		dictsDB.open();
 		knownDictionaries = dictsDB.getDicts();
		dictsDB.close();
 	}
 	
 	private List<String> obtainInstalledDictionariesList()
 	{
 		List<ApplicationInfo> packages = pacMan.getInstalledApplications(PackageManager.GET_META_DATA);
 		List<String> dictsInSystem = new ArrayList<String>();
 		for (ApplicationInfo packageInfo : packages)
 			if(packageInfo.packageName.startsWith(DICTIONARY_PACKAGE))
				dictsInSystem.add(packageInfo.packageName.substring(24));
 		return dictsInSystem;
 	}
 	
 	private boolean insertNewlyInstalledDictionaries(List<String> installedDicts)
 	{
 		boolean wereChanges = false;
 		for (String newDict : installedDicts)
 		{
 			if (isKnownDictionary(newDict))
 				continue;
 			String dictName = provideDictName(newDict);
 			dictsDB.insertDictionary(dictName, newDict);
 			wereChanges = true;
 		}
 		return wereChanges;
 	}
 	
 	private boolean isKnownDictionary(String dict)
 	{		
 		for (Dictionary knownDict : knownDictionaries)
 			if (dict.equals(knownDict.getApp()))
 				return true;
 		return false;
 	}
 			
 	private boolean cleanupAlreadyDeletedDictionaries(List<String> installedDicts)
 	{
 		boolean wereChanges = false;
 		for (Dictionary knownDict : knownDictionaries)
 		{
 			if (isKnownInTheSystem(knownDict.getApp(), installedDicts))
 				continue;
 			dictsDB.deleteDictByAppName(knownDict.getApp());
 			wereChanges = true;
 		}
 		return wereChanges;
 	}
 	
 	private boolean isKnownInTheSystem(String knownDict, List<String> allDicts)
 	{		
 		return allDicts.contains(knownDict);
 	}
 	
 	public void setDictionaryActive(String dictName, boolean isActive)
 	{
 		dictsDB.setActiveDict(dictName, isActive);
 		for (Dictionary knownDict : knownDictionaries)
 			if (knownDict.getName().equals(dictName))
 			{
 				knownDict.setActive(isActive);
 				break;
 			}
 	}
 	
 	public List<Word> getWord(String wordSource)
 	{
 		List<Word> foundWords = new ArrayList<Word>();
 		Word foundWord = null;
 		for (Dictionary d : knownDictionaries)
 		{
 			foundWord = d.getWord(wordSource, contentResolver);
 			if (foundWord != null)
 				foundWords.add(foundWord);
 		}
 		return foundWords;
 	}
 	
 	private String provideDictName(String app){
 		String dictName;
		Uri uri = getProviderUri(app);
 		Cursor cursor = contentResolver.query(uri, null, null, null, null);
 		cursor.moveToFirst();
 		dictName = cursor.getString(0);
 		cursor.close();
 		return dictName;
 	}
 	
 	private Uri getProviderUri(String app)
 	{
		Uri uri = Uri.withAppendedPath(Uri.parse("content://" + DICTIONARY_PACKAGE + app
				+ ".WordsProvider/words"), "create");
 		return uri;
 	}
 }
