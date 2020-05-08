 
 package ualberta.g12.adventurecreator.controllers;
 
 import ualberta.g12.adventurecreator.data.Story;
 import ualberta.g12.adventurecreator.data.StoryList;
 
 import java.util.List;
 
 // TODO implement interfaces
 
 /**
  * controller that interacts with the listview of stories
  */
 public class StoryListController {
     private StoryList sc = null;
 
     /**
      * Sole Constructor for the StoryListController. Initializes a
      * StoryListController Object with a StoryList.
      * 
      * @param sc The StoryList to set up the StoryListController with
      */
     public StoryListController(StoryList sc) {
         this.sc = sc;
     }
 
     /**
      * Add a story to the story list
      * 
      * @param s story to be added to the list
      */
     public void addStory(Story s) {
         sc.addStory(s);
     }
 
     /**
      * Delete a story from the list
      * 
      * @param s story to be deleted
      */
     public void deleteStory(Story s) {
         sc.deleteStory(s);
     }
 
     /**
      * gets story from story list with object
      * 
      * @param s story to be retrieved
      * @return null if blank other wise desired story
      */
     public Story getStory(Story s) {
         return sc.getStory(s);
     }
 
     /**
      * gets story from story list from title
      * 
      * @param t title of the selected story
      * @return null if blank other wise story
      */
     public Story getStory(String t) {
         return sc.getStory(t);
     }
 
     /**
      * Returns all of the stories in the StoryList.
      * 
      * @return a list of all of the stories in the StoryList
      */
     public List<Story> getAllStories() {
         return sc.getAllStories();
 
     }
 
     /**
      * Replaces the story currently at position {@link pos} with {@link s}
      * 
      * @param s the story to replace with
      * @param pos the position to replace the story at
      */
     public void setStory(Story s, int pos) {
         List<Story> stories = sc.getAllStories();
         stories.set(pos, s);
         sc.setAllStories(stories);
     }
 
     /**
      * Updates the story with id id. Essentially replaces the existing story
      * with id id with story s.
      * 
      * @param id the id of the story to update
      * @param s the new story to replace it with
      */
    @Deprecated
     public void updateStoryWithId(int id, Story s) {
         Story oldStory = sc.getStoryById(id);
         sc.deleteStory(oldStory);
         sc.addStory(s);
     }
 
 }
