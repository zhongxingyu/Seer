 package entities;
 
 import java.io.FileNotFoundException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Set;
 
 import data.ReadAndWrite;
 import data.Time;
 
 /**
  * Represents a person or building or collection of events, etc.
  * stores its name, its schedule, a list of tags, and a unique ID
  * 
  * @author ganc
  *
  */
 public class Entity implements Serializable {
 
 	private static final long serialVersionUID = -6326844684848713032L;
 	//The entity's schedule
 	private Schedule entitySchedule;
 	//A list of tags characterizing the entity
 	private ArrayList<String> tags;
 	//Name of the entity
 	private String name;
 	//Entity's unique ID
 	private String ID;
 
 	/**
 	 * Creates an entity
 	 */
 	public Entity() {
 		entitySchedule = new Schedule();
 		tags = new ArrayList<String>();
 	}
 	
 
 /**
  * If entity exists, read from file, if not, create new Entity with name ID
  * @param ID name of new Entity
  */
 	public Entity(String ID) {
 		try {
 			ReadAndWrite.readEntityFromFile(ID);
 		} catch (FileNotFoundException e) {
			new Entity();
 			setName(ID);
 		}
 	}
 
 	/**
 	 * set name and ID of entity to parameter name 
 	 * @param name name to set entity name
 	 */
 	private void setName(String name) {
 		this.name = name;		
 		this.ID = name;
 	}
 
 
 	/**
 	 * Adds the parameter "tag" to the entity's list of tags
 	 * 
 	 * @param tag
 	 */
 	public void addTag(String tag) {
 		if (!tags.contains(tag.toUpperCase()))
 			tags.add(tag.toUpperCase());
 	}
 
 	/**
 	 * returns an ArrayList of all the entity's tags
 	 * 
 	 * @return an ArrayList of all the entity's tags
 	 */
 	public ArrayList<String> getTags() {
 		return tags;
 	}
 
 	/**
 	 * deletes the tag from the entity's tag list, returns true if the deletion
 	 * actually modified the list.
 	 * 
 	 * @param tag
 	 *            tag to be deleted from tag list
 	 * @return if the list was modified by the deletion
 	 */
 	public boolean deleteTag(String tag) {
 		return tags.remove(tag);
 	}
 
 	/**
 	 * Returns the schedule of the Entity.
 	 * 
 	 * @return schedule of the Entity
 	 */
 	public Schedule getSchedule() {
 		return entitySchedule;
 	}
 
 	/**
 	 * returns the name of the entity
 	 * 
 	 * @return name of this entity
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns what event the entity has at time t.
 	 * 
 	 * @return what event, or null if none, the entity has at time t.
 	 */
 	public TimeEvent getEventAtTime(Time t) {
 		return entitySchedule.getEventAtTime(t);
 	}
 
 	/**
 	 * Returns the schedule, in array form, of the Entity in question ONLY
 	 * including the available times.
 	 */
 	public Set<Time> timesFree() {
 		return entitySchedule.timesFree();
 	}
 
 	/**
 	 * Returns the next free Time for this entity after the Time t given
 	 * 
 	 * @param t
 	 *            starting point of the search for free time
 	 * @return next free Time after the Time t given
 	 */
 	public Time nextFree(Time t) {
 		return entitySchedule.nextFree(t);
 	}
 
 	/**
 	 * Returns the Identifier of the Entity Object, to be used for saving
 	 * purposes.
 	 */
 	public String getID() {
 		return ID;
 	}
 	
 	/**
 	 * Replaces the current schedule with one stored in an array of strings, [weekday][interval]
 	 * with each cell representing a Time block
 	 * @param strSched string representation of the schedule to add to Entity
 	 */
 	public void scheduleFromGUI(String[][] strSched) {
 		entitySchedule = new Schedule(strSched); 
 	}
 	
 	/**
 	 * returns a version of the current schedule into an array of strings of [weekday][interval]
 	 * with each cell presenting a Time block with event name and color for the GUI
 	 * @return an array of strings for the GUI
 	 */
 	public String[][] scheduleToGUI() {
 		return entitySchedule.scheduleToGUI();
 	}
 
 
 }
