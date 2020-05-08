 /************************************************************
 Filename: JSONUserStory.java
 Author: Christian Heckendorf
 Created date: 10/22/2013
 Purpose: Holds the public user story information
 Feature: User Story
 ************************************************************/
 package edu.cs673.plm.model;
 
 public class JSONUserStory{
 	long id;
 	String name;
 	String description;
 	int points;
 	JSONUser owner;
 	JSONStatus status;
 
 	/************************************************************
 	Function name: JSONUserStory()
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Default constructor
 	************************************************************/
 	public JSONUserStory(){
 	}
 
 	/************************************************************
 	Function name: toUserStory()
 	Author: Christian Heckendorf
 	Created date: 10/26/2013
 	Purpose: Converts to UserStory object
 	************************************************************/
 	public UserStory toUserStory(){
 		UserStory ret = new UserStory();
 		ret.setName(name);
 		ret.setDescription(description);
 		return ret;
 	}
 
 	/************************************************************
 	Function name: JSONUserStory()
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Constructs a JSONUserStory from a UserStory
 	************************************************************/
 	public JSONUserStory(UserStory userStory){
 		id = userStory.getId();
 		name = userStory.getName();
 		description = userStory.getDescription();
		points = userStory.getPoints();
		owner = new JSONUser(userStory.getOwner());
		status = new JSONStatus(userStory.getStatus());
 	}
 
 	/************************************************************
 	Function name: getId
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Returns the user ID
 	************************************************************/
 	public long getId(){
 		return id;
 	}
 
 	/************************************************************
 	Function name: setId
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Sets the user ID
 	************************************************************/
 	public void setId(long id){
 		this.id = id;
 	}
 
 	/************************************************************
 	Function name: getName
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Returns the name
 	************************************************************/
 	public String getName(){
 		return name;
 	}
 
 	/************************************************************
 	Function name: setName
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Sets the name
 	************************************************************/
 	public void setName(String name){
 		this.name = name;
 	}
 
 	/************************************************************
 	Function name: getDescription()
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Returns a description
 	************************************************************/
 	public String getDescription(){
 		return description;
 	}
 
 	/************************************************************
 	Function name: setDescription()
 	Author: Christian Heckendorf
 	Created date: 10/22/2013
 	Purpose: Sets a description
 	************************************************************/
 	public void setDescription(String description){
 		this.description = description;
 	}
 
 	/************************************************************
 	Function name: getOwner()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Returns a owner
 	************************************************************/
 	public JSONUser getOwner(){
 		return owner;
 	}
 
 	/************************************************************
 	Function name: setOwner()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Sets a owner
 	************************************************************/
 	public void setOwner(JSONUser owner){
 		this.owner = owner;
 	}
 
 	/************************************************************
 	Function name: getStatus()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Returns a status
 	************************************************************/
 	public JSONStatus getStatus(){
 		return status;
 	}
 
 	/************************************************************
 	Function name: setStatus()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Sets a status
 	************************************************************/
 	public void setStatus(JSONStatus status){
 		this.status = status;
 	}
 
 	/************************************************************
 	Function name: getPoints()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Returns a points
 	************************************************************/
 	public int getPoints(){
 		return points;
 	}
 
 	/************************************************************
 	Function name: setPoints()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Sets a points
 	************************************************************/
 	public void setPoints(int points){
 		this.points = points;
 	}
 }
