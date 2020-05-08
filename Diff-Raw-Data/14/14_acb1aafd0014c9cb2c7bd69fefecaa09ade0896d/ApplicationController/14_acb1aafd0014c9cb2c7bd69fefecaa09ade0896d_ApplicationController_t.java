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
 import java.util.Random;
 import java.util.UUID;
 
 import android.app.Application;
 import android.content.Intent;
 import android.provider.Settings.Secure;
 import android.view.View;
 import android.widget.LinearLayout;
 import ca.ualberta.CMPUT301F13T02.chooseyouradventure.elasticsearch.ESHandler;
 
 /**
  * This class represents the main controller for our application. This class stores the state
  * of the application -- notably the functions pertaining to the story and page currently being viewed or edited.
  * This also contains instances of our two other Controllers. The reason being that StoryController and PageController each 
  * deal with either Storys or Pages, where ApplicationController contains functions regarding both.
  * 
  * This class is a controller and in typical MVC style is responsible for coordinating 
  * saving of the model and maintaining coherence between the model and the view.
  */
 
 public class ApplicationController extends Application {
 	
 	private ArrayList<Story> stories;
 	
 	//The instances of our 3 controllers are here
 	private static ApplicationController instance = new ApplicationController();
 	private StoryController storyController = new StoryController();
 	private PageController pageController = new PageController();
 	
 	@Override
 	public void onCreate() {
 			super.onCreate();
 	}
 	
 	public ApplicationController() {
 		instance = this;
 	}
 	
 	/**
 	 * Allows for accessing the singleton ControllerApp class from outside
 	 * of an activity.
 	 * @return the singleton instance of ControllerApp
 	 */
 	public static ApplicationController getInstance() {
 		if (instance == null) {
 			instance = new ApplicationController();
 		}
 		return instance;
 	}
 
 	/**
 	 * Sets the list of stories.
 	 * @param stories
 	 */
 	public void setStories(ArrayList<Story> stories) {
 		this.stories = stories;
 	}
 	
 	/**
 	 * Returns the list of all stories.
 	 * @return the list of all stories.
 	 */
 	public ArrayList<Story> getStories() {
 		return this.stories;
 	}
 
 	/**
 	 * This method is used for gathering new data from the model, it then 
 	 * returns it to update the listviews. It is generalized to work on both
 	 * listviews for Pages and Stories. It also adds indicator flags for things
 	 * such as which stories are cached and which pages contain fights
 	 * @param itemList
 	 * @param infoText
 	 * @return infoText
 	 */
 	protected <T> ArrayList<String> updateView(ArrayList<T> itemList,
 	                                            ArrayList<String> infoText) {
 		infoText.clear();
 		if(itemList.size() == 0)
 		{
 			return infoText;
 		}
 		for (int i = 0; i < itemList.size(); i++) {
 			String outList = "";
 			if(itemList.get(i).getClass().equals(Page.class))
 			{
 
 				if(itemList.get(i).equals(storyController.grabFirstPage())){
 					outList = getString(R.string.startDesignator);
 				}
 
 				if(((Page) itemList.get(i)).getFightingState() == true){
 					outList = outList + getString(R.string.fightDesignator);
 				}
 
 				boolean endDes = false;
 				if(((Page) itemList.get(i)).getDecisions().size() == 0){				
 					endDes = true;
 				}
 				else{
 					endDes = true;
 					ArrayList<Decision> pageIndex = ((Page) itemList.get(i)).getDecisions();
 					UUID pageID = ((Page) itemList.get(i)).getId();
 					
 					for(int j = 0; j < pageIndex.size(); j++){
 						if(!pageIndex.get(j).getPageID().equals(pageID)){
 							endDes = false;
 						}
 						
 					}
 				}
 				if(endDes == true){
 					outList = outList + getString(R.string.endDesignator);
 				}
 				outList = outList + "(" + 
 						((Page) itemList.get(i)).getRefNum() + ") " + 
 						((Page) itemList.get(i)).getTitle();
 			} else if(itemList.get(i).getClass().equals(Story.class)) {
 
 				//If the story has been saved locally, note it
 				if(((Story) itemList.get(i)).getHandler() instanceof DBHandler){
 					outList = getString(R.string.cachedDesignator);
 				}
 				outList = outList + ((Story) itemList.get(i)).getTitle();
 			}
 			infoText.add(outList);
 			
 		}
 		
 		return infoText;
 	}
 
 
 	/**
 	 * First sets the currentStory and currentPage to story and page 
 	 * respectively, and then moves to the activity in classItem.
 	 * @param classItem
 	 * @param story
 	 * @param page
 	 */
 	public <T> void jump(Class<T> classItem, Story story, Page page) {	
 		storyController.setStory(story);
 		pageController.setPage(page);
 		Intent intent = new Intent(this, classItem);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(intent);
 	}
 
 	
 	/**
 	 * Sets the currentPage to the page pointed to by the decision selected
 	 * and then the page is refreshed. If the destinationID of a page is null, 
 	 * this indicates that it will choose a random decision and follow it.
 	 * It finally calls calculate entry, which makes the change of page.
 	 * @param whichDecision
 	 */
 	public void followDecision(int whichDecision) {
 	    Decision decision = pageController.findDecisionByIndex(whichDecision);
 		
 		UUID toPageId = decision.getPageID();
 		ArrayList<Page> pages = storyController.getPages();
 		Page currentPage = pageController.getPage();
 		Page toPage = currentPage;
 		while(toPageId == null){
 			Random rn = new Random();
 			decision = currentPage.getDecisions().get(rn.nextInt(currentPage.getDecisions().size()));
 			toPageId = decision.getPageID();
 		}
 		for (int i = 0; i < pages.size(); i++) {
 			if (toPageId.equals(pages.get(i).getId())) {
 				toPage = pages.get(i);
 				break;
 			}
 		}
 		pageController.calculateEntry(toPage, toPageId);
 		
 		
 	}
 	
 	/**
 	 * This function is a generalized class for creating and initializing a new story and placing it on the server
 	 * @param storyTitle
 	 * @throws HandlerException
 	 */
 	
 	public void initializeNewStory(String storyTitle, Counters playerStats, boolean state) throws HandlerException{
     	
     	final Story newStory = new Story(); 
     	newStory.setUsesCombat(state);
     	newStory.setPlayerStats(playerStats);
     	newStory.setTitle(storyTitle);	    	
     	Page page = storyController.initializeNewPage("First Page");
     	newStory.addPage(page);
     	newStory.setFirstpage(page.getId());
     	newStory.setAuthor(getAndroidID());
     	newStory.setHandler(new ESHandler());
 	    try
 		{			    	
 	    	newStory.getHandler().addStory(newStory);
 			
 
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}	
 	    jump(EditStoryActivity.class,newStory, page);
     }
 
 	/**
 	 * @return the storyController
 	 */
 	public StoryController getStoryController() {
 		return storyController;
 	}
 
 
 	/**
 	 * @return the pageController
 	 */
 	public PageController getPageController() {
 		return pageController;
 	}
 	
 	
 /**
  * 
  * @return The UUID of your android device
  */
 	protected String getAndroidID(){
 		return Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
 	}
 	
 	
 	/**
 	 * This function deals with what happens when a decision is clicked.
 	 * If the story is set up to use counters, it first updates changes in these counter appropriately.
 	 * Then it calls followDecision to figure out where to go from here. 
 	 * @param view
 	 * @param decisionsLayout
 	 */
 	protected void onDecisionClick(View view, LinearLayout decisionsLayout){
 		Story story = storyController.getStory();
 		Page page = pageController.getPage();
 		int whichDecision = decisionsLayout.indexOfChild(view);
 		if(story.isUsesCombat()){
 			Decision decision = page.getDecisions().get(whichDecision);
 			if(page.getFightingState()){
 				story.getPlayerStats().invokeUpdateComplex(decision.getChoiceModifiers());
 			}
 			else{
 				story.getPlayerStats().invokeUpdateSimple(decision.getChoiceModifiers());
 			}
 			
 		}
 		followDecision(whichDecision);
 	}
 	
 	 /**
      * Picks a random story
      */
     protected void onRandomStory() {
 		try {
 			ESHandler eshandler = new ESHandler();
 			Story random = eshandler.getRandomStory();
 			jump(ViewPageActivity.class, random, random.getFirstpage());
 		} catch (HandlerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 }
