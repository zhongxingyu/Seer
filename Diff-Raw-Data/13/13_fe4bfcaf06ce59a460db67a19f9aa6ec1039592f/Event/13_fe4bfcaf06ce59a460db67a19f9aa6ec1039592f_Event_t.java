 package models;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 
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
 
     private List<Resource> resources;
 
     public Event(EventGroup parent, String comment, Calendar startTime,
                  Calendar endTime, UserProfile profile, EventPriority priority, List<Resource> resources) {
         this.profile = profile;
         this.parent = parent;
         this.title = parent.getTitle();
         this.comment = comment;
         this.startTime = startTime;
         this.endTime = endTime;
         this.priority = priority;
         this.resources = resources;
         this.random = String.valueOf(new Random().nextInt(10000000));
     }
     
     public Event(UnboundEvent event, Calendar sTime, Calendar eTime){
     	this(event.getParent(), event.getComment(), sTime, eTime, 
     			event.getProfile(), event.getPriorityObject(), event.getResources() );
     }
 
     public Calendar getStartTime() {
         return (Calendar) startTime.clone();
     }
 
     public void setStartTime(Calendar startTime) {
         this.startTime = (Calendar) startTime.clone();
     }
 
     public Calendar getEndTime() {
         return (Calendar) endTime.clone();
     }
 
     public void setEndTime(Calendar endTime) {
         this.endTime = (Calendar) endTime.clone();
     }
 
     public int getPriority() {
     	return priority.getPriority();
     }
     
     public EventPriority getPriorityObject() {
     	return priority;
     }
     
     public String getRomanPriority() {
     	return priority.getRomanPriority();
     }
     
     public void setPriority(EventPriority priority) {
     	this.priority = priority;
     }
     
     public Color getColor() {
     	return priority.getColor(profile);
     }
     
     public String getComment() {
         return comment;
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public String getTitle() {
         return this.title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
     
     public void setProfile(UserProfile profile) {
     	this.profile = profile;
     }
 
     public UserProfile getProfile() {
         return profile;
     }
 
     public EventGroup getParent() {
         return parent;
     }
 
     public List<Resource> getResources() {
         return resources;
     }
 
     public void setResources(List<Resource> resources) {
         this.resources = resources;
     }
 
     public Event save() {
         if ( profile == null )
             throw new IllegalArgumentException("Profile violates not null constraint");
         profile.getEvents().add(this);
         return this;
     }
 
     public void delete() {
     	for(Resource r : resources) {
     		if(r instanceof ResourceFile) {
     			((ResourceFile) r).removeFromResourcesDirectory();
     		}
     	}
    	
    	// Deletes resources folder
    	
    	String path = profile.getPath() + this.getRandom();
    	
    	new File(path).delete();
    	
         profile.getEvents().removeEvent(this);
     }
 
     public long duration() {
         return (endTime.getTimeInMillis()-startTime.getTimeInMillis())/1000;
     }
 
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
         return startTime.getTimeInMillis() <= eventStart.getTimeInMillis() &&
                eventStart.getTimeInMillis() <= endTime.getTimeInMillis();
     }
     
     
     /**	
      * @return String that is used as folder name for this event
      */
     public String getRandom() {
     	return this.random;
     }
 }
