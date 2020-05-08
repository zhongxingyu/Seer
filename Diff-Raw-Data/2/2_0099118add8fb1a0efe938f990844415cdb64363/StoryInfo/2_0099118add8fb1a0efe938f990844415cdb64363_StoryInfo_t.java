 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package story.book.model;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Class representing additional information about a <code>Story</code> object.
  * Contains fields such as author, title, genre, etc. of the story in addition
  * to information for identifying the story.
  * 
  * @author 	Alexander Cheung
  * @see		Story
  */
 public class StoryInfo {
 	
 	private String author;
 	private String title;
 	private String genre;
 	private String synopsis;
 	private int SID;
 	private Date publishDate;
 	private PublishState publishState;
 	private int startingFragmentID;
 	
 	/**
 	 * Defines the different publishing states of the story.
 	 * 
 	 * @author Alexander Cheung
 	 *
 	 */
 	public enum PublishState {
 		PUBLISHED,
 		UNPUBLISHED,
 		NEEDS_REPUBLISH
 	}
 	
 	/**
 	 * Initializes all of the fields to the empty <code>String</code>.
 	 */
 	public StoryInfo() {
 		setAuthor("");
 		setTitle("");
 		setGenre("");
 		setSynopsis("");
 		setPublishState(PublishState.UNPUBLISHED);
 		setStartingFragmentID(-1);
 	}
 	
 	/**
 	 * 
 	 * @return the author of the story
 	 */
 	public String getAuthor() {
 		return author;
 	}
 	
 	/**
 	 * 
 	 * @param author	the author of the story
 	 */
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	
 	/**
 	 * 
 	 * @return	the title of the story
 	 */
 	public String getTitle() {
 		return title;
 	}
 	
 	/**
 	 * 
 	 * @param title the desired title for the story
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	/**
 	 * 
 	 * @return the genre of the story
 	 */
 	public String getGenre() {
 		return genre;
 	}
 	
 	/**
 	 * 
 	 * @param genre the genre of the story
 	 */
 	public void setGenre(String genre) {
 		this.genre = genre;
 	}
 	
 	/**
 	 * 
 	 * @return the synopsis of the story
 	 */
 	public String getSynopsis() {
 		return synopsis;
 	}
 	
 	/**
 	 * 
 	 * @param synopsis the synopsis of the story
 	 */
 	public void setSynopsis(String synopsis) {
 		this.synopsis = synopsis;
 	}
 	
 	/**
 	 * 
 	 * @return the publish date of the story
 	 */
 	public Date getPublishDate() {
 		return publishDate;
 	}
 	
 	/**
 	 * Returns a <code>String</code> representation of the 
 	 * <code>publishDate</code> <code>Date</code> object.
 	 * 
 	 * @return the <code>String</code> representation of the publish date
 	 */
 	public String getPublishDateString() {
 		if (publishDate != null) {
			SimpleDateFormat stringForm = new SimpleDateFormat("MMMM dd, yyyy");
 			
 			return stringForm.format(publishDate);
 		} else
 			return "";
 	}
 	
 	/**
 	 * 
 	 * @param publishDate the publish date of the story
 	 */
 	public void setPublishDate(Date publishDate) {
 		this.publishDate = publishDate;
 	}
 	
 	/**
 	 * 
 	 * @return the fragment ID of the starting fragment,
 	 * -1 if none
 	 */
 	public int getStartingFragmentID() {
 		return startingFragmentID;
 	}
 	
 	/**
 	 * 
 	 * @param startingFragmentID the fragment ID of the starting fragment
 	 */
 	public void setStartingFragmentID(int startingFragmentID) {
 		this.startingFragmentID = startingFragmentID;
 	}
 
 	/**
 	 * 
 	 * @return the SID of the story
 	 */
 	public int getSID() {
 		return SID;
 	}
 
 	/**
 	 * 
 	 * @param SID the desired SID for the story
 	 */
 	public void setSID(int SID) {
 		this.SID = SID;
 	}
 
 	/**
 	 * 
 	 * @return the publish state of the story
 	 */
 	public PublishState getPublishState() {
 		return publishState;
 	}
 
 	/**
 	 * 
 	 * @param publishState the publish state of the story
 	 */
 	public void setPublishState(PublishState publishState) {
 		this.publishState = publishState;
 	}
 	
 	@Override
 	public String toString(){
 		return this.title + "\n" + this.author +"\n" + this.getPublishDateString();
 	}
 	
 }
