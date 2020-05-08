 package it.chalmers.fannysangles.friendzone.model;
 
 import java.util.ArrayList;
 import java.util.Date;
import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  * @author marcusisaksson
  * 
  * A class representing a post on the wall of the webpage.
  */
 @Entity
 public class EventPost extends AbstractPost {
     
     private String description;
     @OneToMany
    private List<Tag> tags;
     @OneToMany
    private List<FriendzoneUser> attending;
     @OneToMany
    private List<Comment> comments;
     @Temporal(TemporalType.DATE)
     private Date eventDate;
 
     public EventPost() {
     }
 
     
     /**
      * Constructor for the class Post
      * @param title - The title of the Post
      * @param description - The description of the Post
      * @param tags - A list of tags associated with the Post
      * @param creator - The user who created the Post
      * @param attending - The users who are attending
      * @param comments - FriendzoneUser comments
      * @param eventDate - The date of the event
      */
     public EventPost(String title, String description, ArrayList<Tag> tags,
             FriendzoneUser creator, ArrayList<FriendzoneUser> attending, ArrayList<Comment> comments,
             Date eventDate){
         super(title, creator);
         this.setDescription(description);
         this.setTags(tags);
         this.setAttending(attending);
         this.setComments(comments);
         this.setEventDate(eventDate);
     }
     
     
     
     /**
      * Returns the description of the Post
      * @return description
      */
     public String getDescription(){
         return this.description;
     }
     
     /**
      * Returns an ArrayList of the tags associated with the Post
      * @return tags
      */
    public List<Tag> getTags(){
         return this.tags;
     }
     
     
     /**
      * Returns an ArrayList of all attending users
      * @return attending
      */
    public List<FriendzoneUser> getAllAttending(){
         return this.attending;
     }
     
     
     /**
      * Return an ArrayList of all the Posts comments
      * @return comments
      */
    public List<Comment> getAllComments(){
         return this.comments;
     }
     
     /**
      * Returns the date which the event will take place.
      * @return eventDate
      */
     public Date getEventDate(){
         return this.eventDate;
     }
     
     
     
     
     /**
      * Sets the descritpion of the post to the given description
      * @param description
      */
     private void setDescription(String description){
         this.description = description;
     }
     
     /**
      * Updates the ArrayList of tags to the given one.
      * If the tags list is null then a new empty one is created
      * @param tags 
      */
     private void setTags(ArrayList<Tag> tags){
         if(tags != null){
             this.tags = tags;
         } else {
             this.tags = new ArrayList<Tag>();
         }
     }
     
     /**
      * Adds the given tag to the tags list.
      * @param tag 
      */
     private void addTag(Tag tag){
         if(this.tags != null){
             this.tags.add(tag);
         } else {
             this.tags = new ArrayList<Tag>();
             this.tags.add(tag);
         }
     }
     
 
     
     /**
      * Updates the ArrayList of attending to the given one.
      * If the attending list is null then a new empty one is created
      * @param attending 
      */
     private void setAttending(ArrayList<FriendzoneUser> attending){
         if(attending != null){
             this.attending = attending;
         } else {
             this.attending = new ArrayList<FriendzoneUser>();
         }
     }
     
     /**
      * Adds the given user to the attending list.
      * @param attendingUser 
      */
     private void addAttendingUser(FriendzoneUser attendingUser){
         if(this.attending != null){
             this.attending.add(attendingUser);
         } else {
             this.attending = new ArrayList<FriendzoneUser>();
             this.attending.add(attendingUser);
         }
     }
     
     
     /**
      * Updates the ArrayList of Comments to the given one.
      * If the comments list is null then a new empty one is created
      * @param comments 
      */
     private void setComments(ArrayList<Comment> comments){
         if(comments != null){
             this.comments = comments;
         } else {
             this.comments = new ArrayList<Comment>();
         }
     }
     
     /**
      * Adds the given comment to the comments list.
      * @param comment
      */
     private void addComment(Comment comment){
         if(this.comments != null){
             comments.add(comment);
         } else {
             this.comments = new ArrayList<Comment>();
             this.comments.add(comment);
         }
     }
     
     /**
      * Sets the date at which the event will take place.
      * @param date
      */
     private void setEventDate(Date eventDate){
         this.eventDate = eventDate;
     }
 }
