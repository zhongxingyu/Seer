 /************************************************************
 Filename: JSONTask.java
 Author: Christian Heckendorf
 Created date: 10/07/2013
 Purpose: Holds the public fields of a Task
 Feature: None
 ************************************************************/
 package edu.cs673.plm.model;
 
 public class JSONTask{
 	long id;
 	String name;
 	String description;
 	String category;
 	String priority;
 	String risk;
 	JSONStatus status;
 	JSONUser assigned;
 
 	/************************************************************
 	Function name: JSONTask()
 	Author: Christian Heckendorf
 	Created date: 10/07/2013
 	Purpose: Default constructor
 	************************************************************/
 	public JSONTask(){
 	}
 
 	/************************************************************
 	Function name: JSONTask()
 	Author: Christian Heckendorf
 	Created date: 10/07/2013
 	Purpose: Constructs a JSONTask from a Task
 	************************************************************/
 	public JSONTask(Task task){
 		id = task.getId();
 		name = task.getName();
 
 		status = new JSONStatus(task.getStatus());
		assigned = new JSONUser(task.getUserAssigned());
 
 		/* TODO: When the database is filled in, link these up */
 		category = new String("Development");
 		priority = new String("High");
 		risk = new String("Medium");
 	}
 
 	/************************************************************
 	Function name: getId
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Returns the task ID
 	************************************************************/
 	public long getId(){
 		return id;
 	}
 
 	/************************************************************
 	Function name: setId
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Sets the task ID
 	************************************************************/
 	public void setId(long id){
 		this.id = id;
 	}
 
 	/************************************************************
 	Function name: getName
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Returns the name
 	************************************************************/
 	public String getName(){
 		return name;
 	}
 
 	/************************************************************
 	Function name: setName
 	Author: Christian Heckendorf
 	Created date: 10/5/2013
 	Purpose: Sets the name
 	************************************************************/
 	public void setName(String name){
 		this.name = name;
 	}
 
 	/************************************************************
 	Function name: getDescription()
 	Author: Christian Heckendorf
 	Created date: 10/27/2013
 	Purpose: Returns the description
 	************************************************************/
 	public String getDescription(){
 		return description;
 	}
 
 	/************************************************************
 	Function name: setDescription()
 	Author: Christian Heckendorf
 	Created date: 10/27/2013
 	Purpose: Sets the description
 	************************************************************/
 	public void setDescription(String description){
 		this.description=description;
 	}
 
 	/************************************************************
 	Function name: getCategory()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Returns the category
 	************************************************************/
 	public String getCategory(){
 		return category;
 	}
 
 	/************************************************************
 	Function name: setCategory()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Sets the category
 	************************************************************/
 	public void setCategory(String category){
 		this.category = category;
 	}
 
 	/************************************************************
 	Function name: getPriority()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Returns the priority
 	************************************************************/
 	public String getPriority(){
 		return priority;
 	}
 
 	/************************************************************
 	Function name: setPriority()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Sets the priority
 	************************************************************/
 	public void setPriority(String priority){
 		this.priority = priority;
 	}
 
 	/************************************************************
 	Function name: getRisk()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Returns the risk
 	************************************************************/
 	public String getRisk(){
 		return risk;
 	}
 
 	/************************************************************
 	Function name: setRisk()
 	Author: Christian Heckendorf
 	Created date: 10/13/2013
 	Purpose: Sets the risk
 	************************************************************/
 	public void setRisk(String risk){
 		this.risk = risk;
 	}
 
 	/************************************************************
 	Function name: getAssigned
 	Author: Christian Heckendorf
 	Created date: 10/27/2013
 	Purpose: Returns the user assigned
 	************************************************************/
 	public JSONUser getAssigned(){
 		return assigned;
 	}
 
 	/************************************************************
 	Function name: setAssigned
 	Author: Christian Heckendorf
 	Created date: 10/27/2013
 	Purpose: Sets the user assigned
 	************************************************************/
 	public void setAssigned(JSONUser assigned){
 		this.assigned = assigned;
 	}
 
 	/************************************************************
 	Function name: getStatus()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Returns the status
 	************************************************************/
 	public JSONStatus getStatus(){
 		return status;
 	}
 
 	/************************************************************
 	Function name: setStatus()
 	Author: Christian Heckendorf
 	Created date: 11/02/2013
 	Purpose: Sets the status
 	************************************************************/
 	public void setStatus(JSONStatus status){
 		this.status = status;
 	}
 
 }
