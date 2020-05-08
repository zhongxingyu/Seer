 package ualberta.g12.adventurecreator;
 
 
 // TODO implement interfaces
 
 
 public class StoryListController {
 	// as we only have one story list for entire object (might be more later not sure)
 	// can be a singleton and also have sc = our main storylist~
 	StoryList sc = null;
 	
	public void storyListController(StoryList sc){
 		this.sc = sc;
 	}
 	
 	/**
 	 * Add a story to the story list
 	 * @param s
 	 */
 	public void addStory(Story s){
 		sc.addStory(s);
 	}
 	
 	/**
 	 * Delete a story list 
 	 * @param s
 	 */
 	public void deleteStory(Story s){
 		sc.deleteStory(s);
 	}
 	
 	/**
 	 * gets story from story list with object
 	 * @param t
 	 * @return null if blank other wise story
 	 */
 	public Story getStory(Story s){
 		return sc.getStory(s);
 	}
 	
 	/**
 	 * gets story from story list from title 
 	 * @param t
 	 * @return null if blank other wise story
 	 */
 	public Story getStory(String t){
 		return sc.getStory(t);
 	}
 
 }
