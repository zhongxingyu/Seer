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
 import ca.ualberta.cmput301f13t13.storyhoard.dataClasses.Chapter;
 import ca.ualberta.cmput301f13t13.storyhoard.dataClasses.Story;
 import ca.ualberta.cmput301f13t13.storyhoard.helpGuides.InfoActivity;
 import ca.ualberta.cmput301f13t13.storyhoard.local.Utilities;
 
 /**
  * Class meant for the testing of the Story class in the StoryHoard 
  * application.
  * 
  * @author Stephanie Gil
  * 
  * @see Story
  */
 public class TestStory extends
 		ActivityInstrumentationTestCase2<InfoActivity> {
 	
 	public TestStory() {
 		super(InfoActivity.class);
 	}
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 	
 	
 	/**
 	 * Tests creating a story without chapters.
 	 */
 	@SuppressWarnings("unused")
 	public void testCreateStory() {
 		try {
 			Story story = new Story("7 bugs", "Shamalan", "scary story",
 					Utilities.getPhoneId(this.getActivity()));
 		} catch (Exception e) {
 			fail("error creating a new story object");
 		}
 	}
 
 	/**
 	 * Tests adding a chapter to a story.
 	 */
 	public void testAddChapter() {
 		Story story = new Story("7 bugs", "Shamalan", "scary story",
 				Utilities.getPhoneId(this.getActivity()));
 		Chapter chapter = new Chapter(story.getId(), "On a cold, dark night.");
 		story.getChapters().add(chapter);
 		assertEquals(story.getChapters().size(), 1);
 	}
 
 	/**
 	 * Tests the setters and getters of a story object.
 	 */
 	@SuppressWarnings("unused")
 	public void testSettersGetters() {
 		Story mockStory = new Story("title1", "author1", "desc1",
 				Utilities.getPhoneId(this.getActivity()));
 
 		UUID storyId = mockStory.getId();
 		String title = mockStory.getTitle();
 		String author = mockStory.getAuthor();
 		String desc = mockStory.getDescription();
 		String phoneid = mockStory.getPhoneId();
 		ArrayList<Chapter> chapters = mockStory.getChapters();
 		UUID firstChapId = mockStory.getFirstChapterId();
 
 		mockStory.setId(UUID.randomUUID());
 		mockStory.setTitle("new title");
 		mockStory.setAuthor("pinkie");
 		mockStory.setDescription("new desc");
 		mockStory.setPhoneId(Utilities.getPhoneId(this.getActivity()));
 		mockStory.setChapters(null);
 		mockStory.setFirstChapterId(UUID.randomUUID());
 
 		assertNotSame(storyId, mockStory.getId());
 		assertNotSame("new title", title);
 		assertNotSame("pinkie", author);
 		assertNotSame("new desc", desc);
 		assertTrue(phoneid.equals(mockStory.getPhoneId()));
 		assertTrue(mockStory.getChapters() == null);
 		assertNotSame(mockStory.getFirstChapterId(), firstChapId);
 	}
 }
