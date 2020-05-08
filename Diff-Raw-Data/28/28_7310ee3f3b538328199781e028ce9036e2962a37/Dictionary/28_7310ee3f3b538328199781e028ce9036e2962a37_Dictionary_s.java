 package com.chalmers.speedtype.util;
 
 import java.util.Stack;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.chalmers.speedtype.model.Word;
 
 public class Dictionary {
 	
	private static Stack<Word> dictionary;
 	private static SQLiteDatabase database;
 	
 	public Dictionary(SQLiteDatabase database){
 		Dictionary.database = database;
 		
		dictionary = new Stack<Word>();
		
		String[] words = {"banana","apple","onion","orange","carrot"};
 		
		for(String word: words)
			Dictionary.addWord(new Word(word));
 		
 		loadWords();
 	}
 	public static Word getNextWord(){
		return dictionary.empty() ? null : dictionary.pop();
 	}
 	
 	public static void addWord(Word word) {
 		if(word != null) {
 			ContentValues values = new ContentValues();
 			values.put(DictionarySQLiteOpenHelper.WORD, word.toString());
 			
			database.insert(DictionarySQLiteOpenHelper.DICTIONARY_TABLE, null, values);
 		}
 	}
 	
 	private static void loadWords() {
 		Cursor cursor = database.query(
 				DictionarySQLiteOpenHelper.DICTIONARY_TABLE, 
 				new String[] {DictionarySQLiteOpenHelper.WORD}, 
 				null, null, null, null, null);
 		
 		cursor.moveToFirst();
 		Word word;
 		if (!cursor.isAfterLast()) {
 			do {
 				word = new Word(cursor.getString(0));
 				dictionary.add(word);
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 	}
 }
