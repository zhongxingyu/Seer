 
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
 import java.util.UUID;
 
 import android.test.ActivityInstrumentationTestCase2;
 import ca.ualberta.cmput301f13t13.storyhoard.backend.*;
 import ca.ualberta.cmput301f13t13.storyhoard.gui.ViewBrowseStories;
 
 /**
  * Tests the methods of the ServerManager class.
  * 
  * @author Stephanie Gil
  *
  */
 public class TestServerManager 
 		extends ActivityInstrumentationTestCase2<ViewBrowseStories>{
 	private static ServerManager sm = null;
 	
 	public TestServerManager() {
 		super(ViewBrowseStories.class);
 	}
 
 	public void setUp() throws Exception {
 		super.setUp();
 		sm = ServerManager.getInstance();	
        // clean up server
        Story mockCriteria = new Story(null, null, null, null, null);
        sm.retrieve(mockCriteria);
        ArrayList<Story> mockStories = sm.retrieve(mockCriteria);
        for (Story story: mockStories) {
                sm.remove(story);
        }
 		sm.setTestServer();
 	}
 
 	public void tearDown() throws Exception {
 		super.tearDown();
 		sm = ServerManager.getInstance();
 		sm.setRealServer();
 	}
 	/**
 	 * Tests uploading and retrieving a story from the server.
 	 */
 	public void testAddLoadDeleteStory() {
 		Story story = new Story("Harry Potter", "oprah", "the emo boy", 
 				Utilities.getPhoneId(getActivity()));
 		Chapter chap = new Chapter(story.getId(), "on a dark cold night");
 		Chapter chap2 = new Chapter(story.getId(), "he lughe");
 		Choice c1 = new Choice(chap.getId(), chap2.getId(), "hit me!");
 
 		chap.addChoice(c1);
 		story.addChapter(chap);
 		story.addChapter(chap2);
 		
 		sm.update(story);
 		ArrayList<Story> stories = sm.retrieve(story);
 		assertEquals(stories.size(), 1);
 		assertNotNull((Story) stories.get(0));
 		
 		ArrayList<Chapter> chaps = story.getChapters();
 		assertEquals(chaps.size(), 2);
 		
 		// delete
 		sm.remove(story);
 		stories = sm.retrieve(story);
 		assertEquals(stories.size(), 0);
 	}
 	
 	/**
 	 * Tests updating a story on the server.
 	 */
 	public void testUpdateStory() {
 		Story story = new Story("Harry Potter", "oprah", "the emo boy", 
 				Utilities.getPhoneId(getActivity()));
 		Chapter chap = new Chapter(story.getId(), "on a dark cold night");
 		Choice c1 = new Choice(chap.getId(), UUID.randomUUID(), "hit me!");
 
 		chap.addChoice(c1);
 		story.setFirstChapterId(chap.getId());
 		story.addChapter(chap);
 		
 		sm.update(story);
 		ArrayList<Story> stories = sm.retrieve(story);
 		assertEquals(stories.size(), 1);
 		
 		Story newStory = (Story) stories.get(0);
 		newStory.setTitle("new title");
 		newStory.setAuthor("new author");
 		newStory.addChapter(new Chapter(newStory.getId(), "my text"));
 		
 		sm.update(newStory);
 		stories = sm.retrieve(newStory);
 		assertEquals(stories.size(), 1);
 		newStory = (Story) stories.get(0);
 		
 		ArrayList<Chapter> chaps = newStory.getChapters();
 		assertEquals(chaps.size(), 2);
 		assertFalse(newStory.getAuthor().equals(story.getAuthor()));
 		assertFalse(newStory.getTitle().equals(story.getTitle()));
 		
 		sm.remove(newStory);
 	}
 
 	/**
 	 * Tests loading all created stories, and makes sure the results don't
 	 * include any stories not created by author.
 	 */
 	public void testGetAllPublishedStories() {		
 		Story mockStory1 = new Story("My Cow", "Dr. Poe", "my chubby cow",
 				Utilities.getPhoneId(getActivity()));
 		sm.update(mockStory1);
 		Story mockStory2 = new Story("My Frog", "Dr. Phil",
 				"my chubby frog", Utilities.getPhoneId(getActivity()));
 		sm.update(mockStory2);
 
 		// setting search criteria
 		Story mockCriteria = new Story(null, null, null, null, null);
 		ArrayList<Story> stories = sm.retrieve(mockCriteria);
 		assertTrue(stories.size() > 0);
 
 		sm.remove(mockStory1);
 		sm.remove(mockStory2);
 	}
 }
