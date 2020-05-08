 package net.jeebiss.questmanager.quests;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents one chapter in a quest.
  * 
  * @author dbixler
  */
 public class QuestChapter {
 	public enum Status {
 		STARTED, FINISHED, FAILED
 	}
 	
 	private	String	name;
 	private	Status	status;
 	
 	List<Goal> goals = new ArrayList<Goal> ();
 	
 
 	/**
 	 * Create a new quest chapter that has not yet been completed.
 	 * 
 	 * @param name	The name of the chapter to create.
 	 */
 	public QuestChapter (String name) {
 		this.name = name;
 		this.status = Status.STARTED;
 	}
 	
 	/**
 	 * Create a new quest chapter with a specified completion status.
 	 * 
 	 * @param name	The name of the quest chapter.
 	 * @param status	The quest status.
 	 */
 	public QuestChapter (String name, Status status) {
 		this (name);
 		this.status = status;
 	}
 	
 	/**
 	 * Returns the name of the quest chapter.
 	 * 
 	 * @return	The name of the quest chapter.
 	 */
 	public String getName () {
 		return this.name;
 	}
 	
 	/**
 	 * Sets the status of the quest chapter.
 	 * 
 	 * @param status	The status of the quest chapter.
 	 */
 	public void setStatus (Status status) {
 		this.status = status;
 	}
 	
 	/**
 	 * Returns the completion status of this quest chapter.
 	 * 
 	 * @return	True if the chapter is complete, false otherwise.
 	 */
 	public Status getStatus () {
 		return this.status;
 	}
	
	Goal.addPropertyChangeListener(new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
	    }
	});
 }
