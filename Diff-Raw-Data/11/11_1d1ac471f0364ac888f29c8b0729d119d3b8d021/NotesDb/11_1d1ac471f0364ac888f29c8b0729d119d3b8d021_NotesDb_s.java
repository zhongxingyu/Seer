 /*
  *  This is a part of amfontai Notes
  *  Copyright (C) 2013 Andrew Fontaine
  *
  *  amfontai Notes program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  amfontai Notes program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along
  *  with this program; if not, write to the Free Software Foundation, Inc.,
  *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  *  SQLiteOpenHelper code adapted from
  *  http://developer.android.com/training/basics/data-storage/databases.html
  */
 
 package com.amfontai.cmput301asn1;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.mcavallo.opencloud.Cloud;
 import org.mcavallo.opencloud.formatters.HTMLFormatter;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.widget.SimpleCursorAdapter;
 
 /**
 * The Class NotesDb controls access to an {@link android.database.SQLiteDatabase}.
  * It handles all updating, reading, etc.
  */
 public class NotesDb implements BaseColumns {
 	
 	/**
 	 * Represents the read-only flag
 	 * for returning a read-only database
 	 */
 	public static char READ = 'r';
 	
 	/**
 	 * Represents the read-write flag
 	 * for returning a read-write database
 	 */
 	public static char WRITE = 'w';
 	
 	/** 
 	 * The Constant MAX_WORDS_CLOUD represents the total number of words
 	 * to be in the word cloud.
 	 */
 	private static final int MAX_WORDS_CLOUD = 100;
 	
 	/** 
 	 * The Constant MAX_TEXT_SIZE to set the maximum text size
 	 * for the word cloud. 
 	 */
 	private static final double MAX_TEXT_SIZE = 38.0;
 	
 	/** 
 	 * The Constant TABLE_NAME represents the name of
 	 * the table in the database.
 	 */
 	public static final String TABLE_NAME = "Notes";
 	
 	/** The Constant COLUMN_SUBJECT. */
 	public static final String COLUMN_SUBJECT = "Subject";
 	
 	/** The Constant COLUMN_DATE. */
 	public static final String COLUMN_DATE = "Date";
 	
 	/** The Constant COLUMN_CONTENT. */
 	public static final String COLUMN_CONTENT = "Content";
 	
 	/** 
 	 * mDbHelper is an instance of {@link NotesDbHelper}
	 * that is the layer above the {@link android.database.SQLiteDatabase}.
 	 * It handles memory, context, and access.
 	 */
 	private NotesDbHelper mDbHelper;
 	
 	/** 
 	 * A flag that sets whether we need
 	 * a read-only DB or a read-write DB.
 	 * 
 	 * We use this in the event where there's no room
 	 * to write to the database. This way, functions that
 	 * only read things from the DB will still function.
 	 */
 	private char RW;
 	
 	/**
 	 * Instantiates a new NotesDb.
 	 *
 	 * @param RW the read-write flag
 	 * @param context The context of the Activity
 	 */
 	public NotesDb (char RW, Context context) {
 			mDbHelper  = new NotesDbHelper(context);	
 			this.RW = RW;
 	}
 	
 	/**
	 * Gets either a readable or writeable database.
 	 *
	 * @return {@link android.database.SQLiteDatabase} The Database
 	 */
 	public SQLiteDatabase getDB() {
 		if(RW == READ)
 			return mDbHelper.getReadableDatabase();
 		else if(RW == WRITE)
 			return mDbHelper.getWritableDatabase();
 		else
 			return null;
 	}
 
 	/**
 	 * Gets the notes by date as an SQL Cursor.
 	 * Used by {@link listNotes} to get a {@link android.widget.SimpleCursorAdapter}
 	 *
 	 * @return the notes by date
 	 */
 	private Cursor getNotesByDate() {
 		return getDB().query(
 				NotesDb.TABLE_NAME,
 				new String[] {NotesDb._ID, NotesDb.COLUMN_SUBJECT, NotesDb.COLUMN_DATE},
 				null,
 				null,
 				null,
 				null,
 				NotesDb.COLUMN_DATE + " DESC");
 	}
 	
 	/**
 	 * Gets the note by row ID
 	 *
 	 * @param id the ID of the note
 	 * @return a Note with the row id or null if it doesn't exist
 	 */
 	public Note getNoteById(int id) {
 		Cursor cursor;
 		cursor = getDB().query(NotesDb.TABLE_NAME,
 				new String[] {NotesDb.COLUMN_SUBJECT, NotesDb.COLUMN_DATE, NotesDb.COLUMN_CONTENT},
 				NotesDb._ID + " = ?",
 				new String[] {String.valueOf(id)},
 				null,
 				null,
 				null,
 				"1");
 		if (cursor.moveToFirst())
 			return new Note(cursor);
 		else
 			return null;
 	}
 	
 	/**
 	 * Gets the all words.
 	 *
 	 * @return the all words
 	 */
 	public List<String> getAllWords() {
 		Cursor all = getDB().query(NotesDb.TABLE_NAME, 
 				new String[] {NotesDb.COLUMN_SUBJECT, NotesDb.COLUMN_CONTENT},
 				null,null, null, null, null);
 		String results = "";
 		if(all.moveToFirst()) {
 			do {
 				results += " " + all.getString(all.getColumnIndex(NotesDb.COLUMN_SUBJECT))
 						+ " " + all.getString(all.getColumnIndex(NotesDb.COLUMN_CONTENT));
 			} while(all.moveToNext());
 		
 			all.close();
 			
 			return (results.trim().equals(""))?new ArrayList<String>()
 					:Arrays.asList(results.trim().split("\\W+"));
 		}
 		else {
 			return new ArrayList<String>();
 		}
 	}
 	
 	/**
 	 * Gets the word cloud.
 	 *
 	 * @return the word cloud
 	 */
 	public String getWordCloud() {
 		Cloud wordCloud = new Cloud();
 		wordCloud.setMaxWeight(MAX_TEXT_SIZE);
 		wordCloud.setMinWeight(12);
 		for(String word : getAllWords())
 			wordCloud.addText(word);
 		wordCloud.setMaxTagsToDisplay(MAX_WORDS_CLOUD);
 		
 		HTMLFormatter html = new HTMLFormatter();
 		html.setHtmlTemplateTag("<span style=\"font-size: %tag-weight%px\">%tag-name% </span>\n");
 		return (!wordCloud.tags().isEmpty()) ? html.html(wordCloud) : "<span>Add some notes to make a word cloud!</span>";
 	}
 	
 	/**
 	 * Total entries.
 	 *
 	 * @return the long
 	 */
 	public long totalEntries() {
 		return DatabaseUtils.queryNumEntries(getDB(), NotesDb.TABLE_NAME);
 	}
 	
 	/**
 	 * Total characters.
 	 *
 	 * @return the long
 	 */
 	public long totalCharacters() {
 		Cursor all = getDB().query(NotesDb.TABLE_NAME, 
 				new String[] {NotesDb.COLUMN_SUBJECT, NotesDb.COLUMN_CONTENT},
 				null,null, null, null, null);
 		String results = "";
 		if(all.moveToFirst())
 			do {
 				results += all.getString(all.getColumnIndex(NotesDb.COLUMN_SUBJECT))
 						+ all.getString(all.getColumnIndex(NotesDb.COLUMN_CONTENT));
 			} while(all.moveToNext());
 		
 		all.close();
 		
 		return results.length();
 	}
 	
 	/**
 	 * Total words.
 	 *
 	 * @return the total number of words
 	 */
 	public long totalWords() {
 		return getAllWords().size();
 	}
 	
 	/**
 	 * Top one hundred most frequent words.
 	 *
 	 * @return the array list containing the top one hundred frequent words
 	 */
 	public ArrayList<String> topHundred() {
 		List<String> words = getAllWords();
 		
 		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
 		
 		for(String word : words) {
 			if(wordMap.containsKey(word))
 				wordMap.put(word, wordMap.get(word) + 1);
 			else
 				wordMap.put(word, 1);
 		}
 		
 		return recursiveSort(wordMap, new ArrayList<String>());
 	}
 	
 	/**
 	 * Recursive sort.
 	 *
 	 * @param wordMap the word map containing all words and their frequencies
 	 * @param results the array list so far
 	 * @return the array list containing all words, sorted by frequency descending
 	 */
 	private ArrayList<String> recursiveSort(HashMap<String, Integer> wordMap, ArrayList<String> results) {
 		if(results.size() > 99 || wordMap.isEmpty()) 
 			return results;
 		String top = "";
 		int highest = 0;
 		ArrayList<String> keys = new ArrayList<String>(wordMap.keySet());
 		
 		for(String word : keys) {
 			if(wordMap.get(word).intValue() > highest) {
 				highest = wordMap.get(word).intValue();
 				top = word;
 			}
 		}
 		results.add(top);
 		
 		wordMap.remove(top);
 		
 		return recursiveSort(wordMap, results);
 	}
 	
 	/**
 	 * List notes.
 	 *
 	 * @param context the context
 	 * @return the simple cursor adapter
 	 */
 	public SimpleCursorAdapter listNotes(Context context) {
 		return new SimpleCursorAdapter(
 				context,
 				android.R.layout.two_line_list_item,
 				getNotesByDate(),
 				new String[] {NotesDb.COLUMN_SUBJECT, NotesDb.COLUMN_DATE},
 				new int[] {android.R.id.text1, android.R.id.text2},
 				0);
 	}
 
 	/**
 	 * The Class NotesDbHelper.
 	 */
 	private class NotesDbHelper extends SQLiteOpenHelper {
 
 		/** The Constant DATABASE_VERSION. */
 		public static final int DATABASE_VERSION = 1;
 		
 		/** The Constant DATABASE_NAME. */
 		public static final String DATABASE_NAME = "Notes.db";
 		
 		/** The Constant TEXT_TYPE. */
 		private static final String TEXT_TYPE = " TEXT";
 		
 		/** The Constant COMMA_SEP. */
 		private static final String COMMA_SEP = ", ";
 		
 		/** The Constant SQL_CREATE_TABLE. */
 		private static final String SQL_CREATE_TABLE = 
 				"CREATE TABLE " + NotesDb.TABLE_NAME + " ("
 				+ NotesDb._ID + " INTEGER PRIMARY KEY" + COMMA_SEP
 				+ NotesDb.COLUMN_SUBJECT + TEXT_TYPE + COMMA_SEP
 				+ NotesDb.COLUMN_DATE + TEXT_TYPE + COMMA_SEP
 				+ NotesDb.COLUMN_CONTENT + TEXT_TYPE + " )";
 		
 		/** The Constant SQL_DELETE_TABLE. */
 		private static final String SQL_DELETE_TABLE = 
 				"DROP TABLE IF EXISTS " + NotesDb.TABLE_NAME;
 				
 		/**
 		 * Instantiates a new NotesDbHelper.
 		 *
 		 * @param context the context
 		 */
 		public NotesDbHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		/**
 		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 		 */
 		@Override
 		public void onCreate(SQLiteDatabase arg0) {
 			arg0.execSQL(SQL_CREATE_TABLE);
 			ContentValues c = new ContentValues();
 			c.put(COLUMN_SUBJECT, "test");
 			c.put(COLUMN_DATE, "2013-09-17");
 			c.put(COLUMN_CONTENT, "Hello!");
 			arg0.insert(TABLE_NAME, null, c);
 		}
 
 		/**
 		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
 		 */
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL(SQL_DELETE_TABLE);
 			db.execSQL(SQL_CREATE_TABLE);
 		}
 		
 		/**
 		 * @see android.database.sqlite.SQLiteOpenHelper#onDowngrade(android.database.sqlite.SQLiteDatabase, int, int)
 		 */
 		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			onUpgrade(db, oldVersion, newVersion);
 		}	
 	}
 }
