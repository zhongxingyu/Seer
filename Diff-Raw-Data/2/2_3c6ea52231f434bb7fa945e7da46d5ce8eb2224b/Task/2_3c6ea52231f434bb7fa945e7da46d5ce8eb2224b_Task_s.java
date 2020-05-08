 package com.designs_1393.asana.task;
 
 // Asana classes
 import com.designs_1393.asana.*;
 
 public class Task
 {
 	public static final int STATUS_INBOX = 0;
 	public static final int STATUS_LATER = 1;
 	public static final int STATUS_TODAY = 2;
 	public static final int STATUS_UPCOMING = 3;
 
 	private long    ID;
 	private long    assignee;
 	private String  createdAt; // TODO: this should be parsed to a real time
 	private boolean completed;
 	private String  modifiedAt; // TODO: this should be parsed to a real time
 	private String  name;
 	private String  notes;
 	private long[]  projectIDs;
 	private long    workspaceID;
 
 	/**
 	 * Default constructor.
 	 * Creates a new Task object with the following properties: <br>
 	 * <blockquote>
 	 *   assignee = 0<br>
 	 *   createdAt = ""<br>
 	 *   completed = false<br>
 	 *   modifiedAt = ""<br>
 	 *   name = ""<br>
 	 *   notes = ""<br>
 	 *   projectIDs = [empty]<br>
 	 *   workspacesID = 0<br>
 	 * </blockquote>
 	 */
 	public Task()
 	{
 		assignee    = 0;
 		createdAt   = "";
 		completed   = false;
 		modifiedAt  = "";
 		name        = "";
 		notes       = "";
 		projectIDs  = new long[] {};
 		workspaceID = 0;
 	}
 
 	/**
 	 * Returns the task's ID.
 	 * @return the task's ID as assigned by Asana.
 	 */
 	public long getID()
 	{
 		return ID;
 	}
 
 	/**
 	 * Sets the task's unique identifier, as provided by Asana.
 	 * This exists mainly to facilitate the use of the Jackson JSON API.
 	 * @param taskID  Unique long int identifier for the task, as provided by
 	 *                Asana.
 	 */
 	public void setID( long taskID )
 	{
 		ID = taskID;
 	}
 
 	/**
	 * Returns the task's assignees.
 	 * @return the user this task was assigned to.
 	 */
 	public long getAssignee()
 	{
 		return assignee;
 	}
 
 	/**
 	 * Sets the task's assignee.
 	 * @param assigneeID  the user ID for the assignee.
 	 */
 	public void setAssignee( long assigneeID )
 	{
 		assignee = assigneeID;
 	}
 
 	/**
 	 * Returns the time at which the task was created on Asana's servers.
 	 * @return a String representation of a time, e.g. 2012-02-22T02:06:58.147Z.
 	 */
 	public String getCreatedAt()
 	{
 		return createdAt;
 	}
 
 	/**
 	 * Sets the time at which the task was created on Asana's servers.
 	 * Because this is a read-only property within Asana's API, this method
 	 * exists only to facilitate the use of the Jackson JSON API.
 	 * @param time  a String representation of a time, e.g.
 	 *              2012-02-22T02:06:58.147Z.
 	 */
 	public void setCreatedAt( String time )
 	{
 		createdAt = time;
 	}
 
 	/**
 	 * Returns whether or not this task has been mark as complete.
 	 * @return true if the task is complete, false otherwise.
 	 */
 	public boolean isCompleted()
 	{
 		return completed;
 	}
 
 	/**
 	 * Sets this task's completion state.
 	 * @param isComplete  whether or not the task has been completed.
 	 */
 	public void setCompleted( boolean isComplete )
 	{
 		completed = isComplete;
 	}
 
 	/**
 	 * Returns the time at which this task was last modified on Asana's servers.
 	 * @return a String representation of a time, e.g. 2012-02-22T02:06:58.147Z.
 	 */
 	public String getModifiedAt()
 	{
 		return modifiedAt;
 	}
 
 	/**
 	 * Sets the time at which this task was last modified on Asana's servers.
 	 * Because this is a read-only property within Asana's API, this method
 	 * exists only to facilitate the use of the Jackson JSON API.
 	 * @param time  a String representation of a time, e.g.
 	 *              2012-02-22T02:06:58.147Z.
 	 */
 	public void setModifiedAt( String time )
 	{
 		modifiedAt = time;
 	}
 
 	/**
 	 * Returns the name of the task.
 	 * @return a String containing the name of the task.
 	 */
 	public String getName()
 	{
 		return name;
 	}
 
 	/**
 	 * Sets the name of this task.
 	 * @param text  a String containing the name of the task.
 	 */
 	public void setName( String text )
 	{
 		name = text;
 	}
 
 	/**
 	 * Returns the notes stored with the task.
 	 * @return a String containing the notes stored with the task.
 	 */
 	public String getNotes()
 	{
 		return notes;
 	}
 
 	/**
 	 * Sets the notes stored with the task.
 	 * @param text  a String containing the notes stored with the task.
 	 */
 	public void setNotes( String text )
 	{
 		notes = text;
 	}
 
 	/**
 	 * Returns an array of projects (as IDs) that this task is associated with.
 	 * @return long[] of project IDs that this task is associated with.
 	 */
 	public long[] getProjects()
 	{
 		return projectIDs;
 	}
 
 	/**
 	 * Sets the array of projects (as IDs) that this task is associated with.
 	 * @param projects  long[] of project IDs that this task should be
 	 *                  associated with.
 	 */
 	public void setProjects( long[] projects )
 	{
 		projectIDs = projects;
 	}
 
 	/**
 	 * Returns the workspace ID this project is associated with.
 	 * @return the workspace ID this project is associated with
 	 */
 	public long getWorkspaceID()
 	{
 		return workspaceID;
 	}
 
 	/**
 	 * Sets the workspace ID this project is associated with.
 	 * Note that once created, projects cannot be moved to a different
 	 * workspace.
 	 * @param wsID the ID of the workspace this project is associated with
 	 */
 	public void setWorkspaceID( long wsID )
 	{
 		workspaceID = wsID;
 	}
 }
