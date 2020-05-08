 /*
  *        Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  *         Evan DeGraff
  *
  *         Permission is hereby granted, free of charge, to any person obtaining a copy of
  *         this software and associated documentation files (the "Software"), to deal in
  *         the Software without restriction, including without limitation the rights to
  *         use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *         the Software, and to permit persons to whom the Software is furnished to do so,
  *         subject to the following conditions:
  *
  *         The above copyright notice and this permission notice shall be included in all
  *         copies or substantial portions of the Software.
  *
  *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *         IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  *         FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  *         COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  *         IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  *         CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.model.Interfaces;
 
 import java.util.UUID;
 
 import ca.cmput301f13t03.adventure_datetime.model.Bookmark;
 import ca.cmput301f13t03.adventure_datetime.model.Comment;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 
 /**
  * Class provides an interface for controllers to interact with the model and to direct its behaviour
  *
  * @author Jesse Tucker, Evan DeGraff
  *         
  *         AKA : IModelSet
  */
 public interface IStoryModelDirector 
 {
 	/**
 	 * Select the story as the current story
 	 * @param storyId
 	 */
 	public void selectStory(UUID storyId);
 
 	/**
 	 * Select the fragment as the current fragment
 	 * @param fragmentId
 	 */
 	public void selectFragment(UUID fragmentId);
 
 	// void AddComment(Comment comment); 
 	//public void publish(long storyID);
 	/* Commenting out as I don't want to deal with this right now*/
 	/* TODO need to add in other functionality here! Pieces like authoring, saving, etc.*/
 
 	/**
 	 * Save a story in the database
 	 * @param story
 	 * @return Success value
 	 */
 	public boolean putStory(Story story);
 
 	/**
 	 * Creates a new story with a head fragment
 	 */
 	public Story CreateNewStory();
 
 	/**
 	 * Delete a story from the database
 	 * @param storyId
 	 */
 	public void deleteStory(UUID storyId);
 
 	/**
 	 * Save a fragment to the database
 	 * @param fragment
 	 * @return success value
 	 */
 	public boolean putFragment(StoryFragment fragment);
 
 	/**
 	 * Delete a fragment from the database
 	 * @param fragmentId
 	 */
 	public void deleteFragment(UUID fragmentId);
 
 	/**
 	 * Fetch a story by its Id
 	 * @param storyId
 	 * @return The matching story object
 	 */
 	public Story getStory(UUID storyId);
 
 	/**
 	 *	Save a bookmark to the database
 	 */
	public void setBookmark();
 
 	/**
 	 * Fetch a bookmark ID by its ID
 	 * 
 	 * Why is a fetch in the director?
 	 * 
 	 * @param bookmarkId
 	 * @return
 	 */
 	public Bookmark getBookmark(UUID bookmarkId);
 
 	/**
 	 * Uploads current story to the web storage
 	 */
 	public void uploadCurrentStory();
 
 	/* Deletes a bookmark by StoryID
 	 * 
 	 * @param storyId UUID of the story to delete
 	 */
 	public void deleteBookmark();
 
 	/**
 	 * Downloads current story to local database
 	 */
 	public void download();
 
 	/**
 	 *  Publishes a comment to the server
 	 * @param comment
 	 */
 	public void addComment(Comment comment);
 }
