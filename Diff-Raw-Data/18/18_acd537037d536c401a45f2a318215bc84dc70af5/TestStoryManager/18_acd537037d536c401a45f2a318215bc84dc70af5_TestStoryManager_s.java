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
 
 package ca.ualberta.cmput301f13t13.storyhoard.test;
 
 import java.util.ArrayList;
 
 import ca.ualberta.cmput301f13t13.storyhoard.backend.*;
 import ca.ualberta.cmput301f13t13.storyhoard.gui.*;
 import android.test.ActivityInstrumentationTestCase2;
 
 /**
  * Class meant for the testing of the OwnStoryManager class in the StoryHoard
  * application.
  * 
  * @author Stephanie Gil
  * 
  */
 public class TestStoryManager extends
 		ActivityInstrumentationTestCase2<ViewBrowseStories> {
 	private ArrayList<Object> mockStories;
 	private StoryManager sm = null;
 
 	public TestStoryManager() {
 		super(ViewBrowseStories.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// Clearing database
 		DBHelper helper = DBHelper.getInstance(this.getActivity());
 		helper.close();
 		this.getActivity().deleteDatabase(DBContract.DATABASE_NAME);
 		
 		sm = StoryManager.getInstance(getActivity());
 	}
 
 	/**
 	 * Create a new story.
 	 */
 	public Story newMockStory(String title, String author, String description,
 			String phoneId) {
 		// story object
 		Story mockStory = new Story(title, author, description, phoneId);
 
 		// first chapter of story
 		Chapter mockChapter = new Chapter(mockStory.getId(), "my first chapter");
 		mockStory.addChapter(mockChapter);
 		mockStory.setFirstChapterId(mockChapter.getId());
 
 		return mockStory;
 	}
 
 	/**
 	 * Tests caching a story locally on the device database, and then loading it
 	 * again to make sure it was properly saved.
 	 */
 	public void testCacheLoadStory() {
 		Story mockStory = newMockStory("My Frog", "blueberry", "my cute frog",
 				"32432423");
 
 		sm.insert(mockStory);
 
 		mockStories = sm.retrieve(mockStory);
 		assertEquals(mockStories.size(), 1);
 	}
 
 	/**
 	 * Tests adding and loading a story from the local storage
 	 */
 	public void testAddLoadStory() {
 		Story mockStory = newMockStory("My Cow", "Dr. Poe", "my chubby cow",
 				Utilities.getPhoneId(this.getActivity()));
 
 		sm.insert(mockStory);
 		
 		// retrieving story in db that matches mockStory
 		mockStories = sm.retrieve(mockStory);
 		assertEquals(mockStories.size(), 1);
 	}
 
 	/**
 	 * Tests loading all created stories, and makes sure the results don't
 	 * include any stories not created by author.
 	 */
 	public void testGetAllAuthorStories() {
 		Story mockStory1 = newMockStory("My Cow", "Dr. Poe", "my chubby cow",
 				Utilities.getPhoneId(this.getActivity()));
 		sm.insert(mockStory1);
 		Story mockStory2 = newMockStory("My Frog", "Dr. Phil",
 				"my chubby frog", Utilities.getPhoneId(this.getActivity()));
 		sm.insert(mockStory2);
 		Story mockStory3 = newMockStory("My Hen", "Dr. Farmer",
 				"my chubby hen", "blaksjdks");
 		sm.insert(mockStory3);
 
 		// setting search criteria
 		Story mockCriteria = new Story(null, null, null, null, Utilities.getPhoneId(getActivity()));
 		mockStories = sm.retrieve(mockCriteria);
 		assertEquals(mockStories.size(), 2);
 
 	}
 
 	/**
 	 * Tests loading all cached stories.
 	 */
 	public void testGetAllCachedStories() {
 		Story mockStory1 = newMockStory("My Cow", "Dr. Poe", "my chubby cow",
 				Utilities.getPhoneId(this.getActivity()));
 		sm.insert(mockStory1);
 		Story mockStory2 = newMockStory("My Frog", "Dr. Phil",
 				"my chubby frog", "43545454353");
 		sm.insert(mockStory2);
 		Story mockStory3 = newMockStory("My Hen", "Dr. Farmer",
 				"my chubby hen", "3432423432");
 		sm.insert(mockStory3);
 
 		// setting search criteria
		Story mockCriteria = new Story(null, null, null, null, "not");
 		mockStories = sm.retrieve(mockCriteria);
 		assertEquals(mockStories.size(), 2);

 	}
 
 	/**
 	 * Tests editing story
 	 */
 	public void testEditStory() {
 		Story mockStory = newMockStory("My Wizard Mouse", "JK ROlling",
 				"before the edit...", Utilities.getPhoneId(this.getActivity()));
 		sm.insert(mockStory);
 
 		mockStories = sm.retrieve(mockStory);
 		assertEquals(mockStories.size(), 1);
 		
 		Story newStory = (Story) mockStories.get(0);
 
 		newStory.setTitle("My Wizard newt");
 		newStory.setAuthor("not jk rolling");
 		newStory.setDescription("after the edit...");
 		sm.update(newStory);
 
 		// make sure you can find new story
 		mockStories = sm.retrieve(newStory);
 		assertTrue(mockStories.size() == 1);
 
 		// make sure old version no longer exists
 		mockStories = sm.retrieve(mockStory);
 		assertTrue(mockStories.size() == 0);
 	}
 }
