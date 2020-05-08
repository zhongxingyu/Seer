 /************************************************************
 Filename: UserStoryList.java
 Author: Christian Heckendorf
 Created date: 10/22/2013
 Purpose: Holds a list of JSONUserStories
 Feature: Dashboard
 ************************************************************/
 package edu.cs673.plm.model;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class UserStoryList{
 	private List<JSONUserStory> userStories;
 
 	/************************************************************
 	Function name: UserStoryList()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Default constructor
 	************************************************************/
 	public UserStoryList(){
 		userStories = new ArrayList<JSONUserStory>();
 	}
 
 	/************************************************************
 	Function name: addUserStory()
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Adds a release to the list
 	************************************************************/
	public void addUserStory(UserStory release){
		userStories.add(new JSONUserStory(release));
 	}
 
 	/************************************************************
 	Function name: addUserStories()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Adds a list of userStories
 	************************************************************/
 	public void addUserStories(List<UserStory> userStories){
 		for(UserStory r : userStories){
 			this.addUserStory(r);
 		}
 	}
 
 	/************************************************************
 	Function name: getUserStories()
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Returns the list of userStories
 	************************************************************/
 	public List<JSONUserStory> getUserStories(){
 		return userStories;
 	}
 
 	/************************************************************
 	Function name: setUserStories()
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Sets the list of userStories
 	************************************************************/
 	public void setUserStories(List<JSONUserStory> userStories){
 		this.userStories=userStories;
 	}
 }
