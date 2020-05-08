 package models;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
/** Implements whole Event model, used to store Tasks and locate them in time.
  * @author n3v
  */
 public class Event implements Serializable {
 	
 	private static final long serialVersionUID = -4422979245490541800L;
 	private EventGroup parent;
 	private String comment;
     private Calendar startTime;
     private Calendar endTime;
     private UserProfile profile;
     private EventPriority priority;
     private String title;
     private String random;
 
     private List<Task> tasks;
 
     /** Constructs the event
      * @param parent
      * @param comment
      * @param startTime
      * @param endTime
      * @param profile
      * @param priority
      * @param tasks
      */
     public Event(EventGroup parent, String comment, Calendar startTime,
                  Calendar endTime, UserProfile profile, EventPriority priority, List<Task> tasks) {
         this.profile = profile;
         this.parent = parent;
         this.title = parent.getTitle();
         this.comment = comment;
         this.startTime = startTime;
         this.endTime = endTime;
         this.priority = priority;
         this.tasks = tasks;
         this.random = String.valueOf(new Random().nextInt(10000000));
     }
     
     /** Constructs the Event
      * @param event
      * @param sTime
      * @param eTime
      */
     public Event(final Task event, Calendar sTime, Calendar eTime){
     	this(event.getParent(), event.getComment(), sTime, eTime, 
     			event.getProfile(), event.getPriorityObject(), null);
         List<Task> tmp = new LinkedList<>();
         tmp.add(event);
         setTasks(tmp);
     }
 
     /** Returns start time of this Event
      * @return start time of this Event
      */
     public Calendar getStartTime() {
         return (Calendar) startTime.clone();
     }
 
     /** Sets start time of this Event
      * @param startTime
      */
     public void setStartTime(Calendar startTime) {
         this.startTime = (Calendar) startTime.clone();
     }
 
     /** Returns ending time of this Event
      * @return ending time of this Event
      */
     public Calendar getEndTime() {
         return (Calendar) endTime.clone();
     }
 
     /** Sets ending time of this Event
      * @param endTime
      */
     public void setEndTime(Calendar endTime) {
         this.endTime = (Calendar) endTime.clone();
     }
 
     /** Returns priority of this Event as int
      * @return priority of this Event as int
      */
     public int getPriority() {
     	return priority.getPriority();
     }
     
     /** Returns priority of this Event as EventPriority
      * @return priority of this Event as EventPriority
      */
     public EventPriority getPriorityObject() {
     	return priority;
     }
     
     /** Returns priority of this Event as Roman literal
      * @return priority of this Event as Roman literal
      */
     public String getRomanPriority() {
     	return priority.getRomanPriority();
     }
     
     /** Sets the priority of this event (influences Color)
      * @param priority
      */
     public void setPriority(EventPriority priority) {
     	this.priority = priority;
     }
     
     /** Returns Color of this Event 
      * @return Color of this Event 
      */
     public Color getColor() {
     	return priority.getColor(profile);
     }
     
     /** Returns the comment for this Event
      * @return the comment for this Event
      */
     public String getComment() {
         return comment;
     }
 
     /** Sets the comment for this Event
      * @param comment
      */
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     /** Returns the title for this Event
      * @return the title for this Event
      */
     public String getTitle() {
         return this.title;
     }
 
     /** Sets the title for the Event
      * @param title
      */
     public void setTitle(String title) {
         this.title = title;
     }
     
     /** Sets UserProfile of the owner of this Event
      * @param profile
      */
     public void setProfile(UserProfile profile) {
     	this.profile = profile;
     }
 
     /** Returns UserProfile of the owned of the Event
      * @return UserProfile of the owned of the Event
      */
     public UserProfile getProfile() {
         return profile;
     }
 
     /** Returns parent (EventGroup) of the current Event
      * @return parent (EventGroup) of the current Event
      */
     public EventGroup getParent() {
         return parent;
     }
 
     /** Returns the Task list
      * @return the Task list
      */
     public List<Task> getTasks() {
         return tasks;
     }
 
     /** Sets the Task list for the current Event
      * @param tasks
      */
     public void setTasks(List<Task> tasks) {
         this.tasks = tasks;
     }
 
     /** Saves this event into EventManager
      * @return <code>true</code> if save ended with success, <code>false</code> otherwise
      */
     public Event save() {
         if ( profile == null )
             throw new IllegalArgumentException("Profile violates not null constraint");
         profile.getEvents().add(this);
         return this;
     }
 
     /**
      *  Deletes Event and every Task connected with it
      */
     public void delete() {
     	for(Task t : tasks) {
             for(Resource r : t.getResources())
                 if(r instanceof ResourceFile) {
                     ((ResourceFile) r).removeFromResourcesDirectory();
                 }
     	}
     	
     	// Deletes resources folder
     	
     	String path = profile.getPath() + this.getRandom();
     	
     	new File(path).delete();
     	
         profile.getEvents().removeEvent(this);
     }
 
     /** Returns the duration of the event
      * @return the duration of the event
      */
     public long duration() {
         return (endTime.getTimeInMillis()-startTime.getTimeInMillis())/1000;
     }
 
     /** Returns whether or not this event exists between particular dates
      * @param startTime
      * @param endTime
      * @return whether or not this event exists between particular dates
      */
     public boolean isBetween(Calendar startTime, Calendar endTime) {
         Calendar eventStart = getStartTime();
         Calendar eventEnd = getEndTime();
         if ( eventStart == null || eventEnd == null )
             return false;
         return startTime.getTimeInMillis() <= eventStart.getTimeInMillis() &&
                eventEnd.getTimeInMillis() <= endTime.getTimeInMillis();
     }
     
     public String toString(){
     	String res = "Event[";
     	res += startTime.getTime();
     	res += endTime.getTime();
     	res += "]";
     	return res;
     }
 
     /** Checks whether or not this Event overlaps with given time range 
      * @param startTime
      * @param endTime
      * @return whether or not this Event overlaps with given time range 
      */
     public boolean overlaps(Calendar startTime, Calendar endTime) {
         Calendar eventStart = getStartTime();
         Calendar eventEnd = getEndTime();
         if ( eventStart == null || eventEnd == null )
             return false;
         if ( isBetween(startTime, endTime) )
             return true;
         if ( startTime.getTimeInMillis() <= eventEnd.getTimeInMillis() &&
              eventEnd.getTimeInMillis() <= endTime.getTimeInMillis() )
             return true;
         if ( eventStart.getTimeInMillis() <= startTime.getTimeInMillis() && 
         		eventEnd.getTimeInMillis() >= endTime.getTimeInMillis())
         	return true;
         return startTime.getTimeInMillis() <= eventStart.getTimeInMillis() &&
                eventStart.getTimeInMillis() <= endTime.getTimeInMillis();
     }
     
     
     /**	 Returns String that is used as folder name for this event
      * @return String that is used as folder name for this event
      */
     public String getRandom() {
     	return this.random;
     }
 
     public void addTask(Task instance) {
     }
 
     /** Returns list of Resources from all the tasks of this event
      * @return list of Resources from all the tasks of this event
      */
     public List<Resource> getResources() {
         List<Resource> resources = new LinkedList<>();
         for(Task t : tasks) {
             for(Resource r : t.getResources())
                 resources.add(r);
         }
         return resources;
     }
 }
