 /**
  * Copyright 2013 Alex Wong, Ashley Brown, Josh Tate, Kim Wu, Stephanie Gil
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package ca.ualberta.cs.c301f13t13.backend;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.UUID;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import ca.ualberta.cs.c301f13t13.backend.DBContract.StoryTable;
 
 /**
  * Role: Interacts with the database to store, update, and retrieve story
  * objects. It implements the StoringManager interface and inherits from the
  * Model class, meaning it can hold SHViews and notify them if they need to be
  * updated.
  * 
  * Design Pattern: Singleton
  * 
  * @author Stephanie Gil
  * 
  * @see Story
  * @see StoringManager
  * @see Model
  */
 public class StoryManager extends Model implements StoringManager {
 	private static DBHelper helper = null;
 	private static StoryManager self = null;
 
 	/**
 	 * Initializes a new StoryManager object.
 	 */
 	protected StoryManager(Context context) {
 		helper = DBHelper.getInstance(context);
 	}
 
 	/**
 	 * Returns an instance of a StoryManager. Used to implement the singleton
 	 * design pattern.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static StoryManager getInstance(Context context) {
 		if (self == null) {
 			self = new StoryManager(context);
 		}
 		return self;
 	}
 
 	/**
 	 * Saves a new story locally (in the database).
 	 */
 	@Override
 	public void insert(Object object) {
 		Story story = (Story) object;
 		SQLiteDatabase db = helper.getWritableDatabase();
 
 		UUID chapterId = story.getFirstChapterId();
 
 		// Insert story
 		ContentValues values = new ContentValues();
 		values.put(StoryTable.COLUMN_NAME_STORY_ID, (story.getId()).toString());
 		values.put(StoryTable.COLUMN_NAME_TITLE, story.getTitle());
 		if (story.getAuthor() != null) {
 			values.put(StoryTable.COLUMN_NAME_AUTHOR, story.getAuthor());
 		}
 		if (story.getDescription() != null) {
 			values.put(StoryTable.COLUMN_NAME_DESCRIPTION,
 					story.getDescription());
 		}
 		values.put(StoryTable.COLUMN_NAME_FIRST_CHAPTER, chapterId.toString());
 		values.put(StoryTable.COLUMN_NAME_CREATED, story.getAuthorsOwn()
 				.toString());
 
 		db.insert(StoryTable.TABLE_NAME, null, values);
 	}
 
 	/**
 	 * Updates a story already in the database.
 	 * 
 	 * @param newObject
	 *            Contains the changes to the object.
 	 */
 	@Override
 	public void update(Object newObject) {
 		Story newS = (Story) newObject;
 		SQLiteDatabase db = helper.getReadableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(StoryTable.COLUMN_NAME_STORY_ID, newS.getId().toString());
 		values.put(StoryTable.COLUMN_NAME_TITLE, newS.getTitle());
 		values.put(StoryTable.COLUMN_NAME_AUTHOR, newS.getAuthor());
 		values.put(StoryTable.COLUMN_NAME_DESCRIPTION, newS.getDescription());
 		values.put(StoryTable.COLUMN_NAME_FIRST_CHAPTER, newS
 				.getFirstChapterId().toString());
 		values.put(StoryTable.COLUMN_NAME_CREATED, newS.getAuthorsOwn()
 				.toString());
 
 		// Setting search criteria
 		String selection = StoryTable.COLUMN_NAME_STORY_ID + " LIKE ?";
 		String[] sArgs = { newS.getId().toString() };
 
 		db.update(StoryTable.TABLE_NAME, values, selection, sArgs);
 	}
 
 	/**
 	 * Retrieves a story /stories from the database.
 	 * 
 	 * @param criteria
 	 *            Holds the search criteria.
 	 */
 	@Override
 	public ArrayList<Object> retrieve(Object criteria) {
 		ArrayList<Object> results = new ArrayList<Object>();
 		SQLiteDatabase db = helper.getReadableDatabase();
 		String[] sArgs = null;
 		String[] projection = { StoryTable.COLUMN_NAME_STORY_ID,
 				StoryTable.COLUMN_NAME_TITLE, StoryTable.COLUMN_NAME_AUTHOR,
 				StoryTable.COLUMN_NAME_DESCRIPTION,
 				StoryTable.COLUMN_NAME_FIRST_CHAPTER,
 				StoryTable.COLUMN_NAME_CREATED };
 
 		// Setting search criteria
 		ArrayList<String> selectionArgs = new ArrayList<String>();
 		String selection = setSearchCriteria(criteria, selectionArgs);
 
 		if (selectionArgs.size() > 0) {
 			sArgs = selectionArgs.toArray(new String[selectionArgs.size()]);
 		} else {
 			selection = null;
 		}
 
 		// Querying the database
 		Cursor cursor = db.query(StoryTable.TABLE_NAME, projection, selection,
 				sArgs, null, null, null);
 
 		// Retrieving all the entries
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			String storyId = cursor.getString(0);
 
 			Story story = new Story(storyId, cursor.getString(1), // title
 					cursor.getString(2), // author
 					cursor.getString(3), // description
 					cursor.getString(4), // first chapter id
 					Boolean.valueOf(cursor.getString(5)));
 			results.add(story);
 			cursor.moveToNext();
 		}
 		cursor.close();
 
 		return results;
 	}
 
 	/**
 	 * Saves a story to the server for other users to see.
 	 * 
 	 * @param story
 	 */
 	public void publish(Story story) {
 		// TODO STUB
 
 	}
 
 	/**
 	 * Updates a published story, i.e. republishes a story after changes have
 	 * been made to it.
 	 * 
 	 * @param story
 	 */
 	public void updatePublished(Story story) {
 		// TODO STUBB
 	}
 
 	/**
 	 * Takes search criteria for a story, and returns and array list
 	 * of all stories on the server that matched the criteria.
 	 * 
 	 * @param criteria
 	 * 
 	 */
 	public ArrayList<Story> searchPublished(Story criteria) {
 		return null;
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Deletes a story from the server.
 	 * 
 	 * @param story
 	 */
 	public void deletePublished(Story story) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	/**
 	 * Creates the selection string (a prepared statement) to be used in the
 	 * database query. Also creates an array holding the items to be placed in
 	 * the ? of the selection.
 	 * 
 	 * @param object
 	 *            Holds the data needed to build the selection string and the
 	 *            selection arguments array.
 	 * @param sArgs
 	 *            Holds the arguments to be passed into the selection string.
 	 * @return String The selection string, i.e. the where clause that will be
 	 *         used in the sql query.
 	 */
 	@Override
 	public String setSearchCriteria(Object object, ArrayList<String> sArgs) {
 		Story story = (Story) object;
 		HashMap<String, String> storyCrit = story.getSearchCriteria();
 
 		// Setting search criteria
 		String selection = "";
 
 		int counter = 0;
 		int maxSize = storyCrit.size();
 
 		for (String key : storyCrit.keySet()) {
 			String value = storyCrit.get(key);
 			selection += key + " LIKE ?";
 			sArgs.add(value);
 
 			counter++;
 			if (counter < maxSize) {
 				selection += " AND ";
 			}
 		}
 		return selection;
 	}
 }
