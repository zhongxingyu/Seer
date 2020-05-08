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
 
 package ca.ualberta.cmput301f13t13.storyhoard.local;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import ca.ualberta.cmput301f13t13.storyhoard.dataClasses.Story;
 import ca.ualberta.cmput301f13t13.storyhoard.local.DBContract.StoryTable;
 
 /**
  * Role: Interacts with the database to store, update, and retrieve story
  * objects. It implements the StoringManager interface.
  * 
  * </br>
  * Design Pattern: Singleton
  * 
  * @author Stephanie Gil
  * @author Ashley Brown
  * 
  * @see Story
  * @see StoringManager
  */
 public class StoryManager extends StoringManager<Story> {
 	private static DBHelper helper = null;
 	private static StoryManager self = null;
 	private static String phoneId = null;
 	protected ContentValues values;
 	protected String selection;
 	protected String[] sArgs;
 	protected String[] projection;
 
 	/**
 	 * Initializes a new OwnStoryManager story.
 	 */
 	protected StoryManager(Context context) {
 		helper = DBHelper.getInstance(context);
 		phoneId = Utilities.getPhoneId(context);
 	}
 
 	/**
 	 * Returns an instance of a OwnStoryManager. Used to implement the singleton
 	 * design pattern.
 	 * 
 	 * @param context
 	 * @return OwnStoryManager
 	 */
 	public static StoryManager getInstance(Context context) {
 		if (self == null) {
 			self = new StoryManager(context);
 		}
 		return self;
 	}
 
 	/**
 	 * Saves a new story locally (in the database). You can insert
 	 * a story that has any field empty except for its ID.
 	 * 
 	 * </br> Example Call.
 	 * </br> Story story = new Story("The boat", "Bob Week", 
 	 * 				"The boat that could", phoneId);
 	 * </br> OwnStoryManager sm = new OwnStoryManager.getInstance();
 	 * </br> sm.insert(story);
 	 */
 	@Override
 	public void insert(Story story) {
 		SQLiteDatabase db = helper.getWritableDatabase();
 		setContentValues(story);
 		db.insert(StoryTable.TABLE_NAME, null, values);
 	}
 
 	/**
 	 * Updates a story already in the database.
 	 * 
 	 * @param newStory
 	 *            Contains the changes to the story.
 	 */
 	@Override
 	public void update(Story newStory) {
 		setContentValues(newStory);
 		Story newS = (Story) newStory;
 		selection = StoryTable.COLUMN_NAME_STORY_ID + " LIKE ?";
 		sArgs = new String[]{ newS.getId().toString() };
 		SQLiteDatabase db = helper.getReadableDatabase();
 		db.update(StoryTable.TABLE_NAME, values, selection, 
 				sArgs);	
 	}
 
 	/**
 	 * Retrieves a story /stories from the database.
 	 * 
 	 * @param criteria
 	 *            Holds the search criteria.
 	 */
 	@Override
 	public ArrayList<Story> retrieve(Story criteria) {
 		ArrayList<Story> results = new ArrayList<Story>();
 		SQLiteDatabase db = helper.getReadableDatabase();
 		setUpSearch(criteria);
 
 		// Querying the database
 		Cursor cursor = db.query(StoryTable.TABLE_NAME, projection, selection,
 				sArgs, null, null, null);
 		
 		// Retrieving all the entries
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			String storyId = cursor.getString(0);
 			String firstchap = cursor.getString(4);
 			UUID firstchapUUID = null;
 			if (firstchap != null) {
 				firstchapUUID = UUID.fromString(firstchap);
 			}
 
 			Story story = new Story(
 					storyId, 
 					cursor.getString(1), // title
 					cursor.getString(2), // author
 					cursor.getString(3), // description
 					firstchapUUID, // first chapter id
 					cursor.getString(5) // phoneId
 					);
 			results.add(story);
 			cursor.moveToNext();
 		}
 		cursor.close();
 		return results;
 	}
 	
 	private void setUpSearch(Story criteria) {
 		sArgs = null;
 		projection = new String[]{ 
 				StoryTable.COLUMN_NAME_STORY_ID,
 				StoryTable.COLUMN_NAME_TITLE, 
 				StoryTable.COLUMN_NAME_AUTHOR,
 				StoryTable.COLUMN_NAME_DESCRIPTION,
 				StoryTable.COLUMN_NAME_FIRST_CHAPTER,
 				StoryTable.COLUMN_NAME_PHONE_ID };
 
 		// Setting search criteria
 		ArrayList<String> selectionArgs = new ArrayList<String>();
 		selection = setSearchCriteria(criteria, selectionArgs);
 
 		if (selectionArgs.size() > 0) {
 			sArgs = selectionArgs.toArray(new String[selectionArgs.size()]);
 		} else {
 			selection = null;
 		}
 	}	
 	
 	/**
 	 * Sets up the ContentValues for inserting or updating the database.
 	 * @param story
 	 */
 	private void setContentValues(Story story) {
 		UUID chapterId = story.getFirstChapterId();
 
 		// Insert story
 		values = new ContentValues();
 		values.put(StoryTable.COLUMN_NAME_STORY_ID, 
 				(story.getId()).toString());
 		values.put(StoryTable.COLUMN_NAME_TITLE, story.getTitle());
 		values.put(StoryTable.COLUMN_NAME_AUTHOR, story.getAuthor());
 		values.put(StoryTable.COLUMN_NAME_DESCRIPTION,
 				story.getDescription());
 		if (chapterId != null) {
 			values.put(StoryTable.COLUMN_NAME_FIRST_CHAPTER, 
 				chapterId.toString());
 		}
 		values.put(StoryTable.COLUMN_NAME_PHONE_ID, story.getPhoneId());
 	}
 	
 	/**
 	 * Creates the selection string (a prepared statement) to be used in the
 	 * database query. Also creates an array holding the items to be placed in
 	 * the ? of the selection.
 	 * 
 	 * @param story
 	 *            Holds the data needed to build the selection string and the
 	 *            selection arguments array.
 	 * @param sArgs
 	 *            Holds the arguments to be passed into the selection string.
 	 * @return String The selection string, i.e. the where clause that will be
 	 *         used in the sql query.
 	 */
 	@Override
 	public String setSearchCriteria(Story story, ArrayList<String> sArgs) {
 		HashMap<String, String> storyCrit = getSearchCriteria(story);
 		splitKeywords(storyCrit, story.getTitle());
 
 		// Setting search criteria
 		String selection = "1 LIKE ?";
 		sArgs.add("1");
 		
 		for (String key : storyCrit.keySet()) {
 			if (key.equals(StoryTable.COLUMN_NAME_PHONE_ID) 
 					&& story.getPhoneId().equals(Story.NOT_AUTHORS)) {
 				selection += " AND " + key	+ " NOT LIKE ?"; 
 				sArgs.add(phoneId);
 			} else {
 				String value = storyCrit.get(key);
 				selection += " AND " + key + " LIKE ?";
 				sArgs.add(value);
 			}
 		}
 		
 		return selection;
 	}
 	
 	/**
 	 * Returns the information of the story (id, title, author, PhoneId) that
 	 * could be used in searching for a story in the database. This information
 	 * is returned in a HashMap where the keys are the corresponding Story 
 	 * Table column names.
 	 * 
 	 * @return HashMap
 	 */
	private HashMap<String, String> getSearchCriteria(Story story) {
 		HashMap<String, String> info = new HashMap<String, String>();
 		if (story.getId() != null) {
 			info.put(StoryTable.COLUMN_NAME_STORY_ID, story.getId().toString());
 		}
 		if (story.getPhoneId() != null) {
 			info.put(StoryTable.COLUMN_NAME_PHONE_ID, phoneId);
 		}
 		return info;
 	}
 	
 	/**
 	 * Splits up the string for the title into keywords so a search
 	 * to find stories with titles containing the keywords will
 	 * be possible.
 	 * 
 	 * @param info
 	 */
 	private void splitKeywords(HashMap<String, String> info, String keywords) {
 		List<String> words;
 		
 		// No title specified in search criteria
 		if (keywords == null) {
 			return;
 		}
 		
 		words = Arrays.asList(keywords.split("\\s+"));
 		
 		for (String keyword : words) {
 			info.put(StoryTable.COLUMN_NAME_TITLE, "%" + keyword + "%");
 		}
 	}
 
 	/**
 	 * Gets all the stories that are either cached, created by the author, or
 	 * published.
 	 * 
 	 * @param type
 	 *            Will either be PUBLISHED_STORY, CACHED_STORY, or
 	 *            CREATED_STORY.
 	 * @param localStoryController TODO
 	 * @return Array list of all the stories the application asked for.
 	 */
 	public ArrayList<Story> getAllCachedStories() {
 		Story criteria = new Story(null, null, null, null, Story.NOT_AUTHORS);
 		return retrieve(criteria);
 	}
 
 	/**
 	 * Gets all the stories that are either cached, created by the author, or
 	 * published.
 	 * 
 	 * @param type
 	 *            Will either be PUBLISHED_STORY, CACHED_STORY, or
 	 *            CREATED_STORY.
 	 * @param localStoryController TODO
 	 * @return Array list of all the stories the application asked for.
 	 */
 	public ArrayList<Story> getAllAuthorStories() {
 		Story criteria = new Story(null, null, null, null, phoneId);
 		return retrieve(criteria);
 	}
 
 	/**
 	 * Used to search for stories matching the given search criteria. Users can
 	 * either search by specifying the title or author of the story. All stories
 	 * that match will be retrieved.
 	 * 
 	 * @param type
 	 *            Will either be PUBLISHED_STORY, CACHED_STORY
 	 * 
 	 * @param title
 	 *            Title of the story user is looking for.
 	 * @return ArrayList of stories that matched the search criteria.
 	 */
 	public ArrayList<Story> searchAuthorStories(String title) {
 		Story criteria = new Story(null, title, null, null, phoneId);
 		return retrieve(criteria);
 	}
 
 	/**
 	 * Used to search for stories matching the given search criteria. Users can
 	 * either search by specifying the title or author of the story. All stories
 	 * that match will be retrieved.
 	 * 
 	 * @param type
 	 *            Will either be PUBLISHED_STORY, CACHED_STORY
 	 * 
 	 * @param title
 	 *            Title of the story user is looking for.
 	 * @return ArrayList of stories that matched the search criteria.
 	 */
 	public ArrayList<Story> searchCachedStories(String title) {
 		Story criteria = new Story(null, title, null, null, Story.NOT_AUTHORS);
 		return retrieve(criteria);
 	}
 
 	@Override
 	public Story getById(UUID id) {
 		ArrayList<Story> result = retrieve(new Story(id, null, null, null, null));
 		if (result.size() != 1) {
 			return null;
 		}
 		return result.get(0);
 	}
 }
