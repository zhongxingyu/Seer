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
 
 package ca.ualberta.cmput301f13t13.storyhoard.backend;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 
 import ca.ualberta.cmput301f13t13.storyhoard.backend.DBContract.CachedStoryTable;
 import ca.ualberta.cmput301f13t13.storyhoard.backend.DBContract.OwnStoryTable;
 
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
 public class StoryManager extends StoryManagerHelper implements StoringManager {
 	private static DBHelper helper = null;
 	private static StoryManager self = null;
 	private static String phoneId = null;
 
 	/**
 	 * Initializes a new OwnStoryManager object.
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
 	public void insert(Object object) {
 		Story story = (Story) object;
 		if (story.getPhoneId().equals(phoneId)) {
 			super.insert(object, OwnStoryTable.TABLE_NAME, helper);
 		} else {
 			super.insert(object, CachedStoryTable.TABLE_NAME, helper);
 		}
 	}
 
 	/**
 	 * Updates a story already in the database.
 	 * 
 	 * @param newObject
 	 *            Contains the changes to the object.
 	 */
 	@Override
 	public void update(Object newObject) {
 		Story story = (Story) newObject;
 		if (story.getPhoneId().equals(phoneId)) {
 			super.update(story, OwnStoryTable.TABLE_NAME, helper);
 		} else {
 			super.update(story, CachedStoryTable.TABLE_NAME, helper);
 		}		
 	}
 
 	/**
 	 * Retrieves a story /stories from the database.
 	 * 
 	 * @param criteria
 	 *            Holds the search criteria.
 	 */
 	@Override
 	public ArrayList<Object> retrieve(Object criteria) {
 		Story story = (Story) criteria;
		if (story.getPhoneId() != null) {
 			return super.retrieve(story, OwnStoryTable.TABLE_NAME, helper);
 		} else {
 			return super.retrieve(story, CachedStoryTable.TABLE_NAME, helper);
 		}		
 		
 	}
 }
