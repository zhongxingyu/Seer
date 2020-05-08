 /*
 * Copyright (c) 2013, TeamCMPUT301F13T02
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 
 * Neither the name of the {organization} nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package ca.ualberta.CMPUT301F13T02.chooseyouradventure;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.app.Application;
 import android.provider.Settings.Secure;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.elasticsearch.ESHandler;
 import android.content.Intent;
 
 /**
  * This class represents the controller for our application. This class stores the state
  * of the application -- notably the story and page currently being viewed or edited.
  * 
  * This class is a controller and in typical MVC style is responsible for co-ordinating 
  * saving of the model and maintaining coherence between the model and the view.
  */
 
 public class ControllerApp extends Application{
 
 	private boolean isEditing = false;
 	
 	// These variables shouldn't be saved.
 	private ViewPageActivity pageActivity;
 	private boolean tilesChanged;
 	private boolean decisionsChanged;
 	private boolean commentsChanged;
 	private boolean endingChanged;
 	
 	private Story currentStory;
 	private Page currentPage;
 	private ArrayList<Story> stories;
 	private static ControllerApp instance = new ControllerApp();
 	
 	@Override
 	public void onCreate() {
 			super.onCreate();
 	}
 	
 	public ControllerApp() {
 		instance = this;
 	}
 	
 	/**
 	 * Allows for accessing the singleton ControllerApp class from outside
 	 * of an activity.
 	 * @return the singleton instance of ControllerApp
 	 */
 	public static ControllerApp getInstance() {
 		if (instance == null) {
 			instance = new ControllerApp();
 		}
 		return instance;
 	}
 	
 	/**
 	 * This sets the current story
 	 * @param A Story
 	 */
 	public void setStory(Story story) {
 		this.currentStory = story;
 	}
 
 	/**
 	 * This gets the current Story
 	 * @return The current Story
 	 */
 	public Story getStory() {
 		return currentStory;
 	}
 	
 	/**
 	 * This sets the current Page
 	 * @param A Page
 	 */
 	public void setPage(Page page) {
 		this.currentPage = page;
 		
 		// The page has now been initialized, so everything has changed
 		endingChanged = true;
 		tilesChanged = true;
 		decisionsChanged = true;
 		commentsChanged = true;
 		
 		reloadPage();
 	}
 
 	/**
 	 * Similar to the above function, this method creates a new page object
 	 * @param pageTitle
 	 * @return
 	 */
 	public Page initializeNewPage(String pageTitle){
 		final Page newPage = new Page();
 		newPage.setTitle(pageTitle);
 		return newPage;
 	}
 
 
 	/**
 	 * This gets the current page
 	 * @return The current Page
 	 */
 	public Page getPage() {
 		return currentPage;
 	}
 	
 	/**
 	 * Sets the list of stories.
 	 * @param stories
 	 */
 	public void setStories(ArrayList<Story> stories) {
 		this.stories = stories;
 	}
 	
 	/**
 	 * This function is a generalized class for creating a new story and placing it on the server
 	 * @param storyTitle
 	 * @throws HandlerException
 	 */
 	public void initializeNewStory(String storyTitle) throws HandlerException{
 	    	
 	    	final Story newStory = new Story(); 	
 	    	newStory.setTitle(storyTitle);	    	
 	    	Page newPage = initializeNewPage("First Page");
 	    	newStory.addPage(newPage);
 	    	newStory.setFirstpage(newPage.getId());
 	    	newStory.setAuthor(Secure.getString(
 					getBaseContext().getContentResolver(), Secure.ANDROID_ID));
 		    try
 			{			    	
 		    	ESHandler eshandler = new ESHandler();
 				eshandler.addStory(newStory);
 				
 	
 			} catch (Exception e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 		    jump(EditStoryActivity.class,newStory, newPage);
 	    }
 
 
 	/**
 	 * Returns the list of all stories.
 	 * @return the list of all stories.
 	 */
 	public ArrayList<Story> getStories() {
 		return this.stories;
 	}
 	
 	/**
 	 * Sets whether the user wants to be in viewing mode or editing mode.
 	 * @param editing
 	 */
 	public void setEditing(boolean editing) {
 		isEditing = editing;
 	}
 
 	/**
 	 * Get whether the page being viewed by the user is in editing mode or not.
 	 * @return If the page is in editing mode.
 	 */
 	public boolean getEditing() {
 		return isEditing;
 	}
 
 
 	/**
 	 * Sets the view associated with the currentPage to activity.
 	 * @param activity
 	 */
 	public void setActivity(ViewPageActivity activity) {
 		pageActivity = activity;
 	}
 
 	/**
 	 * Removes the view associated with this page.
 	 */
 	public void deleteActivity() {
 		pageActivity = null;
 	}
 
 	/**
 	 * Returns a list of strings for each page to be displayed in the Spinner
 	 * for editing a decision.
 	 * @param pages
 	 * @return A list of Strings, one representing each page in the story
 	 */
 	public ArrayList<String> getPageStrings(ArrayList<Page> pages) {
 		ArrayList<String> pageNames = new ArrayList<String>();
 		for (int i = 0; i < pages.size(); i++) {
 			pageNames.add("(" + pages.get(i).getRefNum() + ") " + pages.get(i).getTitle());
 		}
 		
 		return pageNames;
 	}
 
 
 	/**
 	 * This method is used for gathering new data from the model, it then 
 	 * returns it to update the listviews. It is generalized to work on both
 	 * listviews for Pages and Stories.
 	 * @param itemList
 	 * @param infoText
 	 * @return
 	 */
 	protected <T> ArrayList<String> updateView(ArrayList<T> itemList,
 	                                            ArrayList<String> infoText) {
 		infoText.clear();
 		if(itemList.size() != 0)
 		{
 			for (int i = 0; i < itemList.size(); i++) {
 				String outList = "";
 				if(itemList.get(i).getClass().equals(Page.class))
 				{
 					
 					if(itemList.get(i).equals(currentStory.getFirstpage())){
 						outList = "{Start} ";
 					}
 					if(((Page) itemList.get(i)).getDecisions().size() == 0){				
 						outList = outList + "{Endpoint} ";
 					}
 					outList = outList + "(" + 
 					          ((Page) itemList.get(i)).getRefNum() + ") " + 
 							  ((Page) itemList.get(i)).getTitle();
 				} else if(itemList.get(i).getClass().equals(Story.class)) {
 					//If the story has been saved locally, note it
 					if(((Story) itemList.get(i)).getLocal() == 1)
 						outList = "Cached: ";
					outList = outList + ((Story) itemList.get(i)).getTitle();
 				}
 				infoText.add(outList);
 			}
 		}
 		
 		return infoText;
 	}
 
 	/**
 	 * Updates a Pages title and pushes to the handler
 	 * @param pageTitle
 	 * @param currentPage
 	 */
 	protected void updateTitle(String pageTitle, Page currentPage){
 		currentPage.setTitle(pageTitle);		
 		currentStory.updateStory();
 	}
 
 	/**
 	 * Similar to the above method, but it creates a new page object with the 
 	 * title.
 	 * @param pageTitle
 	 */
 	protected void updateTitle(String pageTitle){
 		Page newPage = initializeNewPage(pageTitle);
 		currentStory.addPage(newPage);
 		currentStory.updateStory();
 	}
 
 	/**
 	 * Updates the first page of a story and pushes to handler
 	 * @param currentPage
 	 */
 	protected void updateFP(Page currentPage){
 		UUID newID = currentPage.getId();
 		currentStory.setFirstpage(newID);
 		currentStory.updateStory();
 	}
 
 	/**
 	 * Deletes a page.
 	 * @param currentPage
 	 */
 	protected void removePage(Page currentPage){
 		currentStory.deletePage(currentPage);
 		currentStory.updateStory();
 	}
 
 	/**
 	 * First sets the currentStory and currentPage to story and page 
 	 * respectively, and then moves to the activity in classItem.
 	 * @param classItem
 	 * @param story
 	 * @param page
 	 */
 	public <T> void jump(Class<T> classItem, Story story, Page page) {
 		setStory(story);
 		setPage(page);
 		Intent intent = new Intent(this, classItem);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(intent);
 	}
 
 	/**
 	 * Sets the currentPage to the page pointed to by the decision selected
 	 * and then the page is refreshed.
 	 * @param whichDecision
 	 */
 	public void followDecision(int whichDecision) {
 	    Decision decision = currentPage.getDecisions().get(whichDecision);
 		
 		UUID toPageId = decision.getPageID();
 		ArrayList<Page> pages = currentStory.getPages();
 		Page toPage = currentPage;
 		for (int i = 0; i < pages.size(); i++) {
 			if (toPageId.equals(pages.get(i).getId())) {
 				toPage = pages.get(i);
 				break;
 			}
 		}
 		setPage(toPage);
 	}
 
 	/**
 	 * Sets all the changed variables to true and then tells ViewPageActivity
 	 * to update. Basically it tells ViewPageActivity to refresh the whole
 	 * view.
 	 */
 	public void reloadPage() {
 		tilesChanged = true;
 		decisionsChanged = true;
 		commentsChanged = true;
 		endingChanged = true;
 		updateActivity();
 	}
 
 	/**
 	 * Adds a tile to the currentPage.
 	 * @param tile
 	 */
 	public void addTile(Tile tile) {
 		currentPage.addTile(tile);
 		setTilesChanged();
 	}
 
 	/**
 	 * Updates the tile at position whichTile to show the given string.
 	 * @param text
 	 * @param whichTile
 	 */
 	public void updateTile(String text, int whichTile) {
 		currentPage.updateTile(text, whichTile);
 		setTilesChanged();
 	}
 
 	/**
 	 * Deletes a tile from the page.
 	 * @param whichTile
 	 */
 	public void deleteTile(int whichTile) {
 		currentPage.removeTile(whichTile);
 		setTilesChanged();
 	}
 
 	/**
 	 * Adds a decision to the currentPage.
 	 * @param decision
 	 */
 	public void addDecision() {
 		Decision decision = new Decision(currentPage);
 		currentPage.addDecision(decision);
 		setDecisionsChanged();
 	}
 
 	/**
 	 * Updates the decision at position whichDecision to have the given text
 	 * and to point to the page at position whichPage in the story's list of
 	 * pages.
 	 * @param text
 	 * @param whichPage
 	 * @param whichDecision
 	 */
 	public void updateDecision(String text, int whichPage, int whichDecision) {
 		ArrayList<Page> pages = currentStory.getPages();
 		currentPage.updateDecision(text, pages.get(whichPage), whichDecision);
 		setDecisionsChanged();
 	}
 
 	/**
 	 * Deletes the decision at position whichDecision.
 	 * @param whichDecision
 	 */
 	public void deleteDecision(int whichDecision) {
 		currentPage.deleteDecision(whichDecision);
 		setDecisionsChanged();
 	}
 
 	/**
 	 * This adds a comment to the current page
 	 * @param A comment to add
 	 */
 	public void addComment(String text) {
 		String poster = Secure.getString( 
 				getBaseContext().getContentResolver(), Secure.ANDROID_ID);
 		Comment comment = new Comment(text, poster);
 		ESHandler eshandler = new ESHandler();
 		
 		currentPage.addComment(comment);
 		setCommentsChanged();
 		try
 		{
 			eshandler.addComment(getStory(), getPage(), comment);
 		} catch (HandlerException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * Sets the page ending the given string.
 	 * @param text
 	 */
 	public void setEnding(String text) {
 		currentPage.setPageEnding(text);
 		setEndingChanged();
 	}
 
 	/**
 	 * Tells the current story to save itself.
 	 */
 	public void saveStory() {
 		currentStory.updateStory();
 	}
 	
 	/**
 	 * Sets the tilesChanged mark to true so that the ViewPageActivity will 
 	 * know that the tiles need to be updated, and then tells the 
 	 * ViewPageActivity to update.
 	 */
 	public void setTilesChanged() {
 		tilesChanged = true;
 		updateActivity();
 	}
 	
 	/**
 	 * Get whether the tiles list has changed since the last update. 
 	 * @return Returns whether tiles have changed.
 	 */
 	public boolean haveTilesChanged() {
 		return tilesChanged;
 	}
 	
 	/**
 	 * Sets decisionsChanged to true so that the ViewPageActivity will know
 	 * that the decisions need to be updated, and then tells the 
 	 * ViewPageActivity to update.
 	 */
 	public void setDecisionsChanged() {
 		decisionsChanged = true;
 		updateActivity();
 	}
 	
 	/**
 	 * Get whether the decisions list has changed since the last update/
 	 * @return Returns whether the decisions have changed.
 	 */
 	public boolean haveDecisionsChanged() {
 		return decisionsChanged;
 	}
 	
 	/**
 	 * Sets commentsChanged to true so the ViewPageActivity will know to
 	 * update the comments, and then tells ViewPageActivity to update.
 	 */
 	public void setCommentsChanged() {
 		commentsChanged = true;
 		updateActivity();
 	}
 	
 	/**
 	 * Get whether the comments have changed since the last update.
 	 * @return Whether the comments have changed.
 	 */
 	public boolean haveCommentsChanged() {
 		return commentsChanged;
 	}
 	
 	/**
 	 * Sets endingChanged to true so the ViewPageActivity will know to 
 	 * update the ending, and then tells ViewPageActivity to update.
 	 */
 	public void setEndingChanged() {
 		endingChanged = true;
 		updateActivity();
 	}
 	
 	/**
 	 * Get whether the ending has changed since the last update.
 	 * @return Whether the ending has changed.
 	 */
 	public boolean hasEndingChanged() {
 		return endingChanged;
 	}
 	
 	/**
 	 * Lets the controller know that the view has finished updating by setting
 	 * the *Changed variables back to false.
 	 */
 	public void finishedUpdating() {
 		tilesChanged = false;
 		decisionsChanged = false;
 		commentsChanged = false;
 		endingChanged = false;
 	}
 
 	/**
 	 * Calls the update method of the current ViewPageActivity associated with
 	 * this page.
 	 */
 	private void updateActivity() {
 		if (pageActivity != null) {
 			pageActivity.update(currentPage);
 		}
 	}
 	
 }
