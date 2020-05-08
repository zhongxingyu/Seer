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
 import ca.ualberta.cs.c301f13t13.backend.DBContract.ChapterTable;
 
 /**
  * Role: Interacts with the database to store, update, and retrieve chapter
  * objects. It implements the StoringManager interface and inherits from the
  * Model class, meaning it can hold SHViews and notify them if they need to be
  * updated.
  * 
  * Design Pattern: Singleton
  * 
  * @author Stephanie Gil
  * 
  * @see Chapter
  * @see StoringManager
  */
 public class ChapterManager implements StoringManager {
 	private static DBHelper helper = null;
 	private static ChapterManager self = null;
 
 	/**
 	 * Initializes a new ChapterManager object.
 	 * 
 	 * @param context
 	 */
 	protected ChapterManager(Context context) {
 		helper = DBHelper.getInstance(context);
 	}
 
 	/**
 	 * Returns an instance of itself. Used to accomplish the singleton design
 	 * pattern.
 	 * 
 	 * @param context
 	 * @return ChapterManager
 	 */
 	public static ChapterManager getInstance(Context context) {
 		if (self == null) {
 			self = new ChapterManager(context);
 		}
 		return self;
 	}
 
 	/**
 	 * Inserts a new chapter into the database.
 	 * 
 	 * @param object
 	 *            Object to be stored in the database. In this case, it will be
 	 *            a chapter object.
 	 */
 	@Override
 	public void insert(Object object) {
 		Chapter chapter = (Chapter) object;
 		SQLiteDatabase db = helper.getWritableDatabase();
 
 		// Insert chapter
 		ContentValues values = new ContentValues();
 		values.put(ChapterTable.COLUMN_NAME_CHAPTER_ID,
 				(chapter.getId()).toString());
		values.put(ChapterTable.COLUMN_NAME_STORY_ID, chapter.getStoryId()
				.toString());
 		if (chapter.getText() != null) {
 			values.put(ChapterTable.COLUMN_NAME_TEXT, chapter.getText());
 		}
 
 		db.insert(ChapterTable.TABLE_NAME, null, values);
 	}
 
 	/**
 	 * Takes a chapter object that holds the desired search criteria (null
 	 * values for any parameter that you don't want included in the search, for
 	 * example, chapter text). It then retrieves a chapter or chapters from the
 	 * database that matched the criteria and returns them in an ArrayList.
 	 * 
 	 * @param criteria
 	 *            Criteria for the object(s) to be retrieved from the database.
 	 * @return chapters Contains the objects that matched the search criteria.
 	 */
 	@Override
 	public ArrayList<Object> retrieve(Object criteria) {
 		ArrayList<Object> results = new ArrayList<Object>();
 		String[] sArgs = null;
 		ArrayList<String> selectionArgs = new ArrayList<String>();
 
 		SQLiteDatabase db = helper.getReadableDatabase();
 
 		String[] projection = { ChapterTable.COLUMN_NAME_CHAPTER_ID,
 				ChapterTable.COLUMN_NAME_STORY_ID,
 				ChapterTable.COLUMN_NAME_TEXT };
 
 		// Setting search criteria
 		String selection = setSearchCriteria(criteria, selectionArgs);
 
 		if (selectionArgs.size() > 0) {
 			sArgs = selectionArgs.toArray(new String[selectionArgs.size()]);
 		} else {
 			sArgs = null;
 			selection = null;
 		}
 
 		// Querying the database
 		Cursor cursor = db.query(ChapterTable.TABLE_NAME, projection,
 				selection, sArgs, null, null, null);
 
 		// Retrieving all the entries
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			String storyId = cursor.getString(1);
 
 			Chapter newChapter = new Chapter(UUID.fromString(cursor
 					.getString(0)), // chapter id
 					UUID.fromString(storyId), // story id
 					cursor.getString(2) // text
 			);
 			results.add(newChapter);
 			cursor.moveToNext();
 		}
 		cursor.close();
 		return results;
 	}
 
 	/**
 	 * Updates a chapter's data in the database.
 	 * 
 	 * @param newObject
 	 *            Holds the new data that will be used to update.
 	 */
 	@Override
 	public void update(Object newObject) {
 		Chapter newC = (Chapter) newObject;
 		SQLiteDatabase db = helper.getReadableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(ChapterTable.COLUMN_NAME_CHAPTER_ID, newC.getId().toString());
 		values.put(ChapterTable.COLUMN_NAME_STORY_ID, newC.getStoryId()
 				.toString());
 		values.put(ChapterTable.COLUMN_NAME_TEXT, newC.getText());
 
 		String selection = ChapterTable.COLUMN_NAME_CHAPTER_ID + " LIKE ?";
 		String[] sArgs = { newC.getId().toString() };
 
 		db.update(ChapterTable.TABLE_NAME, values, selection, sArgs);
 	}
 
 	/**
 	 * Creates the selection string (the sql where clause) to be used in the
 	 * database query. Also creates an array holding the items the selection
 	 * arguments, since the selection string is a prepared statement.
 	 * 
 	 * Eg. selection: WHERE CHAPTER_ID = ? selectionArg = "11244543"
 	 * 
 	 * @param object
 	 *            Holds the data needed to build the selection string and the
 	 *            selection arguments array.
 	 * @param sArgs
 	 *            Holds the arguments to be passed into the selection string.
 	 * @return selection The selection string.
 	 */
 	@Override
 	public String setSearchCriteria(Object object, ArrayList<String> sArgs) {
 		Chapter chapter = (Chapter) object;
 		HashMap<String, String> chapCrit = chapter.getSearchCriteria();
 		String selection = "";
 
 		int maxSize = chapCrit.size();
 		int counter = 0;
 		for (String key : chapCrit.keySet()) {
 			String value = chapCrit.get(key);
 			selection += key + " LIKE ? ";
 			sArgs.add(value);
 
 			counter++;
 			if (counter < maxSize) {
 				selection += "AND ";
 			}
 		}
 		return selection;
 	}
 }
