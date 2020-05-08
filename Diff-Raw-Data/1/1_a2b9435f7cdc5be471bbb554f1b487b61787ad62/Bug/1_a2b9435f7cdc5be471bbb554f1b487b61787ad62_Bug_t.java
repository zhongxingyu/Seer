 /************************************************************
 Filename: Bug.java
 Author: Christian Heckendorf
 Created date: 10/01/2013
 Purpose: Holds a bug
 Feature: None
 ************************************************************/
 package edu.cs673.plm.model;
 
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.OneToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.FetchType;
 import javax.persistence.JoinTable;
 import javax.persistence.JoinColumn;
 import org.hibernate.annotations.GenericGenerator;
 
 @Entity
 public class Bug{
 	@Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy = "increment")
 	private long id;
 	private String name;
 	private String description;
 
 	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="userStoryId")
 	private UserStory userStory;
 
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="createdId")
 	private User userCreated;
 
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="assignedId")
 	private User userAssigned;
 
 	/***************************************************************
 	Function name: getId
 	Author: Christian Heckendorf
 	Created Date: 10/01/13
 	Purpose: Gets an id
 	***************************************************************/
 	public long getId(){
 		return id;
 	}
 
 	/***************************************************************
 	Function name: setId
 	Author: Christian Heckendorf
 	Created Date: 10/01/13
 	Purpose: Sets an id
 	***************************************************************/
 	public void setId(long id){
 		this.id=id;
 	}
 
 	/***************************************************************
 	Function name: getName
 	Author: Christian Heckendorf
 	Created Date: 10/01/13
 	Purpose: Gets a name
 	***************************************************************/
 	public String getName(){
 		return name;
 	}
 
 	/***************************************************************
 	Function name: setName
 	Author: Christian Heckendorf
 	Created Date: 10/01/13
 	Purpose: Sets a name
 	***************************************************************/
 	public void setName(String name){
 		this.name=name;
 	}
 
 	/************************************************************
 	Function name: getDescription()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Returns the description
 	************************************************************/
 	public String getDescription(){
 		return description;
 	}
 
 	/************************************************************
 	Function name: setDescription()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Sets the description
 	************************************************************/
 	public void setDescription(String description){
 		this.description=description;
 	}
 
 	/************************************************************
 	Function name: getUserStory()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Returns the user story
 	************************************************************/
 	public UserStory getUserStory(){
 		return userStory;
 	}
 
 	/************************************************************
 	Function name: setUserStory()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Sets the user story
 	************************************************************/
 	public void setUserStory(UserStory userStory){
 		this.userStory=userStory;
 	}
 
 	/************************************************************
 	Function name: getUserAssigned()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Returns the assigned user
 	************************************************************/
 	public User getUserAssigned(){
 		return userAssigned;
 	}
 
 	/************************************************************
 	Function name: setUserAssigned()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Sets the assigned user
 	************************************************************/
 	public void setUserAssigned(User userAssigned){
 		this.userAssigned=userAssigned;
 	}
 
 	/************************************************************
 	Function name: getUserCreated()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Returns the user that created this
 	************************************************************/
 	public User getUserCreated(){
 		return userCreated;
 	}
 
 	/************************************************************
 	Function name: setUserCreated()
 	Author: Christian Heckendorf
 	Created date: 10/01/2013
 	Purpose: Sets the user that created this
 	************************************************************/
 	public void setUserCreated(User userCreated){
 		this.userCreated=userCreated;
 	}
 }
