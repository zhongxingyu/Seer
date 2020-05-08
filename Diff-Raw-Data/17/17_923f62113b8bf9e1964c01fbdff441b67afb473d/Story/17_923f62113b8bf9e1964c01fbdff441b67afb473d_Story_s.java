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
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.UUID;
 
 import android.graphics.Bitmap;
 import ca.ualberta.cs.c301f13t13.backend.DBContract.StoryTable;
 
 /**
 * Role: A container to hold media information. The media can be a photo, an
 * illustration, audio, or video.
  * 
  * @author Stephanie Gil
  * @author Ashley Brown
  */
 public class Story implements Serializable {
 
 	private UUID id;
 	private String author;
 	private String title;
 	private String description;
 	private UUID firstChapterId;
 	private HashMap<UUID, Chapter> chapters;
 	private Boolean authorsOwn;
 	private Bitmap image;
 
 	/**
 	 * Initializes a new story object without need an id as an argument.
 	 * 
 	 * @param title
 	 *            Story title
 	 * @param author
 	 *            Story author
 	 * @param description
 	 *            Story description
 	 * @param authorsOwn
 	 *            Whether or not author created the story (true) or downloaded
 	 *            it from another user (false)
 	 */
 	public Story(String title, String author, String description,
 			Boolean authorsOwn) {
 		this.id = UUID.randomUUID();
 		this.author = author;
 		this.title = title;
 		this.description = description;
 		this.authorsOwn = authorsOwn;
 		this.firstChapterId = null;
 		chapters = new HashMap<UUID, Chapter>();
 	}
 
 	/**
 	 * Initializes a new story object with an id. Usually used when making a
 	 * story object that will be holding search criteria since you can
 	 * specify the id of the story.
 	 * 
 	 * @param id
 	 * 			  Story's unique id.
 	 * @param title
 	 *            Story title
 	 * @param author
 	 *            Story author
 	 * @param description
 	 *            Story description
 	 * @param authorsOwn
 	 *            Whether or not author created the story (true) or downloaded
 	 *            it from another user (false)
 	 */
 	public Story(UUID id, String title, String author, String description,
 			Boolean authorsOwn) {
 		this.id = id;
 		this.author = author;
 		this.title = title;
 		this.description = description;
 		this.authorsOwn = authorsOwn;
 		this.firstChapterId = null;
 		chapters = new HashMap<UUID, Chapter>();
 	}
 
 	/**
 	 * Initializes a new story object from data taken from the database.
 	 * 
 	 * @param id
 	 * 			  Story's unique id.
 	 * @param title
 	 *            Story title
 	 * @param author
 	 *            Story author
 	 * @param description
 	 *            Story description
 	 * @param authorsOwn
 	 *            Whether or not author created the story (true) or downloaded
 	 *            it from another user (false)
 	 */
 	protected Story(String id, String title, String author, String description,
 			String chapterId, Boolean authorsOwn) {
 		this.id = UUID.fromString(id);
 		this.author = author;
 		this.title = title;
 		this.description = description;
 		this.firstChapterId = UUID.fromString(chapterId);
 		this.chapters = new HashMap<UUID, Chapter>();
 		this.authorsOwn = authorsOwn;
 	}
 
 	// GETTERS
 
 	/**
 	 * Returns the Id of the story.
 	 * 
 	 * @return
 	 */
 	public UUID getId() {
 		return this.id;
 	}
 
 	/**
 	 * Returns the title of the story.
 	 * 
 	 * @return
 	 */
 	public String getTitle() {
 		return this.title;
 	}
 
 	/**
 	 * Returns the author of the story.
 	 * 
 	 * @return
 	 */
 	public String getAuthor() {
 		return this.author;
 	}
 
 	/**
 	 * Returns the description of the story.
 	 * 
 	 * @return
 	 */
 	public String getDescription() {
 		return this.description;
 	}
 
 	/**
 	 * Returns the story's cover image.
 	 * 
 	 * @return
 	 */
 	public Bitmap getImage() {
 		return this.image;
 	}
 
 	/**
 	 * Returns the chapters of the story.
 	 * 
 	 * @return chapters
 	 */
 	public HashMap<UUID, Chapter> getChapters() {
 		return this.chapters;
 	}
 
 	/**
 	 * Returns the first chapter of the story.
 	 * 
 	 * @return firstChapter
 	 */
 	public UUID getFirstChapterId() {
 		return this.firstChapterId;
 	}
 
 	/**
 	 * Returns the chapter matching the chapter id
 	 * 
 	 * @return chapter
 	 */
 	public Chapter getChapter(UUID id) {
 		Chapter chap = chapters.get(id);
 		return chap;
 	}
 
 	/**
 	 * Returns boolean indicating whether or not the story was created by the
 	 * phone's author or not.
 	 * 
 	 * @return
 	 */
 	public Boolean getAuthorsOwn() {
 		return this.authorsOwn;
 	}
 
 	// SETTERS
 
 	/**
 	 * Set the Id of the story.
 	 * 
 	 * @param id
 	 */
 	public void setId(UUID id) {
 		this.id = id;
 	}
 
 	/**
 	 * Set the title of the story.
 	 * 
 	 * @param title
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * Set the author of the story.
 	 * 
 	 * @param author
 	 */
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	/**
 	 * Set the description of the story.
 	 * 
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * Sets the first chapter of the story.
 	 * 
 	 * @param chapter
 	 */
 	public void setFirstChapterId(UUID chapterId) {
 		firstChapterId = chapterId;
 	}
 
 	/**
 	 * Set the chapters of the story.
 	 * 
 	 * @param chapters
 	 */
 	public void setChapters(HashMap<UUID, Chapter> chapters) {
 		this.chapters = chapters;
 	}
 
 	/**
 	 * Sets the Boolean indicating whether or not the story was created by the
 	 * phone's author.
 	 * 
 	 * @param authorsOwn
 	 */
 	public void setAuthorsOwn(Boolean authorsOwn) {
 		this.authorsOwn = authorsOwn;
 	}
 
 	// Other methods
 
 	/**
 	 * Adds a chapter onto the story object.
 	 */
 	public void addChapter(Chapter chapter) {
 		if (chapters.isEmpty()) {
 			// set first chapter id
 			firstChapterId = chapter.getId();
 		}
 		chapters.put(chapter.getId(), chapter);
 	}
 
 	/**
 	 * Returns the information of the story (id, title, author, authorsOwn) that
 	 * could be used in searching for a story in the database. This information
 	 * is returned in a HashMap where the keys are the corresponding Story Table
 	 * column names.
 	 * 
 	 * @return HashMap
 	 */
 	public HashMap<String, String> getSearchCriteria() {
 		HashMap<String, String> info = new HashMap<String, String>();
 
 		if (id != null) {
 			info.put(StoryTable.COLUMN_NAME_STORY_ID, id.toString());
 		}
 
 		if (title != null) {
 			info.put(StoryTable.COLUMN_NAME_TITLE, title);
 		}
 
 		if (author != null) {
 			info.put(StoryTable.COLUMN_NAME_AUTHOR, author);
 		}
 
 		if (authorsOwn != null) {
 			info.put(StoryTable.COLUMN_NAME_CREATED, authorsOwn.toString());
 		}
 
 		return info;
 	}
 
 	@Override
 	public String toString() {
 		return "Story [id=" + id + ", author=" + author + ", title=" + title
 				+ ", description=" + description + "]";
 	}
 }
