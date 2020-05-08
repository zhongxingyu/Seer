 package com.mates120.myword;
 
 import java.util.List;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 
 public class DictionaryManager {
 	
 	private DataSource dataSource;
 	private Context context;
 	
 	public DictionaryManager(Context context){
 		dataSource = new DataSource(context);
 		this.context = context;
 		
 	}
 	
 	public void addDictionary(Dictionary dictionary ){
 		/*
 		 * Now there should be function to install new app that provide 
 		 * own dictionary.
 		 */
 		dataSource.open();
 		if(!dataSource.dictionaryInDB(dictionary.getName()))
 			dataSource.insertDictionary(dictionary.getName());
 			for (int i = 0; i < dictionary.getWords().size(); i ++){
 				addWord(dictionary.getWord(i).getSource(), 
 						dictionary.getWord(i).getValues(), 
 						dictionary.getName());
 			}
 		dataSource.close();
 	}
 	
 	public void deleteDictionary(String name){
 		/*
 		 * Function to delete application with dictionary
 		 */
 		long valuesIds[];
 		dataSource.open();
 		valuesIds = dataSource.getDictionaryValuesIds(name);
 		deleteLinksOfValues(valuesIds);
 		dataSource.deleteDictionaryValues(name);
 		dataSource.deleteDictionaryByName(name);
 		dataSource.close();
 	}
 	
 	private void addWord(String wordSource, List<String> values, String dictName){
 		long wordId;
 		long valueIds[];
 		valueIds = dataSource.insertValues(values, dictName);
 		Word existWord = dataSource.getWord(wordSource);
 		if(existWord != null){
 			wordId = existWord.getId();
 		} else {
 			wordId = dataSource.insertWord(wordSource);
 		}
 		createWordLinks(wordId, valueIds);
 	}
 	
 	private void createWordLinks(long wordId, long[] valueIds){
 		for (int i = 0; i < valueIds.length; i ++)
 			dataSource.createLink(wordId, valueIds[i]);
 	}
 	
 	private void deleteLinksOfValues(long valuesIds[]){
 		long wordId;
 		for(int i = 0; i < valuesIds.length; i++){
 			wordId = dataSource.getWordIdByValueId(valuesIds[i]);
 			if(dataSource.wordHasOneValue(wordId))
 				dataSource.deleteWordById(wordId);
 			dataSource.deleteLinkByValue(valuesIds[i]);
 		}
 	}
 	
 	public Word getWord(String wordSource){
 		dataSource.open();
 		Word word = null;
 		word = dataSource.getWord(wordSource);
 		if(word != null)
 			word.setValues(dataSource.getWordValues(word.getId()));
 		dataSource.close();
 		return word;
 	}
 	
 	public SourceWord getSourceWord(String wordSource){
 		ContentResolver cr = context.getContentResolver();
 		Uri uri = Uri.parse(
 				"content://com.mates120.dictionarytemplate.WordsProvider/words");
		String[] projection = new String[]{"_id", "suorce", "value"};
 		String[] selectionArgs = new String[]{wordSource};
 		Cursor cursor = cr.query(uri, projection, null, selectionArgs, null);
 		return cursorToWord(cursor);
 	}
 	
 	private SourceWord cursorToWord(Cursor cursor){
 		SourceWord sWord = null;
 		if(cursor != null && cursor.getCount() > 0){
 			sWord = new SourceWord(cursor.getString(1),cursor.getString(2));
 		}
 		return sWord;
 	}
 	
 	public List<Dictionary> getDictionaries(){
 		dataSource.open();
 		List<Dictionary> dicts = dataSource.getAllDictionaries();
 		dataSource.close();
 		return dicts;
 	}
 	
 	public void setSearchInDict(String dictName, boolean searchIn){
 		dataSource.open();
 		dataSource.setSearchInDict(dictName, searchIn);
 		dataSource.close();
 	}
 }
