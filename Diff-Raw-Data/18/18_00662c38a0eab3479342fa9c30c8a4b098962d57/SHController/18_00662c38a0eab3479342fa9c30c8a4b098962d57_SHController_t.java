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
 import java.util.Random;
 import java.util.UUID;
 
 import android.content.Context;
 
 /**
  * Role: Is called by the views of the application to then interact with the
  * manager classes to handle all types of data (story, chapter, media, choice).
  * This class does not directly interact with the database or server, only the
  * managers.
  * 
  * Design Pattern: Singleton
  * 
  * @author Stephanie Gil
  * @author Ashley Brown
  * 
  */
 public class SHController {
 	// CONSTANTS
 	private Context context = null;
 	private static SHController self = null; // SELF
 	private static ManagerFactory sf = null;
 
 	protected SHController(Context context) {
 		sf = new ManagerFactory(context);
 		this.context = context;
 	}
 
 	/**
 	 * Returns an instance of the general controller as a singleton.
 	 * 
 	 * @return SHController
 	 */
 	public static SHController getInstance(Context context) {
 		if (self == null) {
 			self = new SHController(context);
 		}
 		return self;
 	}
 
 	/**
 	 * Gets all the stories that are either cached, created by the author, or
 	 * published.
 	 * 
 	 * @param type
 	 *            Will either be PUBLISHED_STORY, CACHED_STORY, or
 	 *            CREATED_STORY.
 	 * @return Array list of all the stories the application asked for.
 	 */
 	public ArrayList<Story> getAllStories(ObjectType type) {
 		ArrayList<Story> stories = new ArrayList<Story>();
 		ArrayList<Object> objects = new ArrayList<Object>();
 		Story criteria = null;
 		StoringManager sm = sf.getStoringManager(type);
 
 		switch (type) {
 		case CACHED_STORY:
 			criteria = new Story(null, null, null, null, "NOT"
 					+ Utilities.getPhoneId(context));
 			break;
 		case CREATED_STORY:
 			criteria = new Story(null, null, null, null,
 					Utilities.getPhoneId(context));
 			break;
 		case PUBLISHED_STORY:
 			criteria = new Story(null, null, null, null, null);
 			break;
 		default:
 			break;
 		}
 
 		objects = sm.retrieve(criteria);
 		stories = Utilities.objectsToStories(objects);
 		return stories;
 	}
 
 	/**
 	 * Retrieves all the chapters that are in a given story.
 	 * 
 	 * @param storyId
 	 *            Id of the story the chapters are wanted from.
 	 * 
 	 * @return ArrayList of the chapters.
 	 */
 	public ArrayList<Chapter> getAllChapters(UUID storyId) {
 		ArrayList<Chapter> chapters = new ArrayList<Chapter>();
 		ArrayList<Object> objects = new ArrayList<Object>();
 
 		Chapter criteria = new Chapter(null, storyId, null);
 		StoringManager sm = sf.getStoringManager(ObjectType.CHAPTER);
 
 		objects = sm.retrieve(criteria);
 		chapters = Utilities.objectsToChapters(objects);
 
 		return chapters;
 	}
 	/**
 	 * Retrieves a random choice from the chapter.
 	 * 
 	 * @param chapterID
	 *            Id of  the chapters that the choice is for.
 	 * 
 	 * @return a choice
 	 */
 	public Choice getRandomChoice(UUID chapterId) {
 		ArrayList<Choice> choices = getAllChoices(chapterId);
 		int max=0;
 		max =choices.size();
 		Random rand = new Random(); 
 		int num;
 		num=rand.nextInt(max);
 		Choice choice=choices.get(num);
 
 		return choice;
 	}
 
 	/**
 	 * Retrieves all the choices that are in a chapter.
 	 * 
 	 * @param chapterId
 	 *            Id of the chapter the choices are wanted from.
 	 * 
 	 * @return ArrayList of the chapter's choices.
 	 */
 	public ArrayList<Choice> getAllChoices(UUID chapterId) {
 		ArrayList<Choice> choices = new ArrayList<Choice>();
 		ArrayList<Object> objects = new ArrayList<Object>();
 
 		Choice criteria = new Choice(null, chapterId);
 		StoringManager sm = sf.getStoringManager(ObjectType.CHOICE);
 
 		objects = sm.retrieve(criteria);
 		choices = Utilities.objectsToChoices(objects);
 		return choices;
 	}
 
 	/**
 	 * Retrieves all the illustrations that are in a chapter.
 	 * 
 	 * @param chapterId
 	 *            Id of the chapter the illustrations are wanted from.
 	 * 
 	 * @return ArrayList of the illustrations.
 	 */
 	public ArrayList<Media> getAllIllustrations(UUID chapterId) {
 		ArrayList<Media> illustrations = new ArrayList<Media>();
 		ArrayList<Object> objects = new ArrayList<Object>();
 		Media criteria = new Media(null, chapterId, null, Media.ILLUSTRATION);
 		StoringManager sm = sf.getStoringManager(ObjectType.MEDIA);
 
 		objects = sm.retrieve(criteria);
 		illustrations = Utilities.objectsToMedia(objects);
 		return illustrations;
 	}
 
 	/**
 	 * Retrieves the first illustration of a chapter. Returns null if the
 	 * chapter has no illustrations.
 	 * 
 	 * @param chapterId
 	 *            Id of the chapter the illustrations are wanted from.
 	 * 
 	 * @return ArrayList of the illustrations.
 	 */
 	public Media getFirstIllustration(UUID chapterId) {
 		ArrayList<Media> illustrations = new ArrayList<Media>();
 		ArrayList<Object> objects = new ArrayList<Object>();
 		Media ill = null;
 		Media criteria = new Media(null, chapterId, null, Media.ILLUSTRATION);
 		StoringManager sm = sf.getStoringManager(ObjectType.MEDIA);
 
 		objects = sm.retrieve(criteria);
 		illustrations = Utilities.objectsToMedia(objects);
 
 		if (illustrations.size() > 0) {
 			ill = illustrations.get(0);
 		}
 		return ill;
 	}
 
 	/**
 	 * Retrieves all the photos that are in a chapter.
 	 * 
 	 * @param chapterId
 	 *            Id of the chapter the photos are wanted from.
 	 * 
 	 * @return ArrayList of the photos.
 	 */
 	public ArrayList<Media> getAllPhotos(UUID chapterId) {
 		ArrayList<Media> photos = new ArrayList<Media>();
 		ArrayList<Object> objects = new ArrayList<Object>();
 		Media criteria = new Media(null, chapterId, null, Media.PHOTO);
 		StoringManager sm = sf.getStoringManager(ObjectType.MEDIA);
 
 		objects = sm.retrieve(criteria);
 		photos = Utilities.objectsToMedia(objects);
 		return photos;
 	}
 
 	/**
 	 * Adds either a story, chapter, or choice.
 	 * 
 	 * @param object
 	 *            Object to be inserted (must either be a Story, Chapter,
 	 *            Choice, or Media object).
 	 * @param type
 	 *            Will either be CHAPTER, CHOICE, MEDIA, PUBLISHED_STORY,
 	 *            CACHED_STORY, CREATED_STORY
 	 */
 	public void addObject(Object object, ObjectType type) {
 		StoringManager sm = sf.getStoringManager(type);
 		sm.insert(object);
 	}
 
 	/**
 	 * Used to search for stories matching the given search criteria. Users can
 	 * either search by specifying the title or author of the story. All stories
 	 * that match will be retrieved.
 	 * 
 	 * @param title
 	 *            Title of the story user is looking for.
 	 * 
 	 * @param type
 	 *            Will either be PUBLISHED_STORY, CACHED_STORY, CREATED_STORY
 	 * 
 	 * @return ArrayList of stories that matched the search criteria.
 	 */
 	public ArrayList<Story> searchStory(String title, ObjectType type) {
 		Story criteria = null;
 		ArrayList<Object> objects = new ArrayList<Object>();
 		ArrayList<Story> stories = new ArrayList<Story>();
 		StoringManager sm = sf.getStoringManager(type);
 
 		if (type.equals(ObjectType.CREATED_STORY)) {
 			criteria = new Story(null, title, null, null,
 					Utilities.getPhoneId(context));
 		} else if (type.equals(ObjectType.CACHED_STORY)) {
 			criteria = new Story(null, title, null, null, "none");
 		}
 
 		objects = sm.retrieve(criteria);
 		stories = Utilities.objectsToStories(objects);
 		return stories;
 	}
 
 	/**
 	 * Retrieves a complete chapter (including any photos, illustrations, and
 	 * choices).
 	 * 
 	 * @param id
 	 *            Id of the chapter wanted.
 	 * 
 	 * @return The complete chapter.
 	 */
 	public Chapter getCompleteChapter(UUID id) {
 		// Search criteria gets set
 		Chapter criteria = new Chapter(id, null, null, null);
 		StoringManager sm = sf.getStoringManager(ObjectType.CHAPTER);
 
 		// Get chapter
 		ArrayList<Object> objects = sm.retrieve(criteria);
 		Chapter chapter = (Chapter) objects.get(0);
 
 		// Get chapter choices
 		chapter.setChoices(getAllChoices(id));
 
 		// Get photos
 		chapter.setPhotos(getAllPhotos(id));
 
 		// Get illustrations
 		chapter.setIllustrations(getAllIllustrations(id));
 
 		return chapter;
 	}
 
 	/**
 	 * Retrieves a complete story (including chapters, and any photos,
 	 * illustrations, and choices belonging to the chapters).
 	 * 
 	 * @param id
 	 *            Story id of the story wanted.
 	 * 
 	 * @return The complete story.
 	 */
 	public Story getCompleteStory(UUID id) {
 		// Search criteria gets set
 		Story criteria = new Story(id, null, null, null, null);
 		StoringManager sm = sf.getStoringManager(ObjectType.CACHED_STORY);
 		ArrayList<Object> objects = sm.retrieve(criteria);
 		Story story = (Story) objects.get(0);
 
 		// Get all chapters
 		ArrayList<Chapter> chapters = getAllChapters(id);
 		HashMap<UUID, Chapter> chaptersHash = new HashMap<UUID, Chapter>();
 
 		// Get all choices
 		for (Chapter chap : chapters) {
 			Chapter fullChap = getCompleteChapter(chap.getId());
 			chaptersHash.put(chap.getId(), fullChap);
 		}
 
 		// add chapters to story
 		story.setChapters(chaptersHash);
 
 		return story;
 	}
 
 	/**
 	 * Updates either a story, chapter, or choice object. Must specify what type
 	 * of object it getting updated. Also, updates are happening to the database
 	 * of the phone, not the server.
 	 * 
 	 * @param object
 	 *            Object to be updated.
 	 * @param type
 	 *            Will either be CHAPTER, CHOICE, MEDIA, PUBLISHED_STORY,
 	 *            CACHED_STORY, CREATED_STORY
 	 */
 	public void updateObject(Object object, ObjectType type) {
 		StoringManager sm = sf.getStoringManager(type);
 		sm.update(object);
 	}
 }
