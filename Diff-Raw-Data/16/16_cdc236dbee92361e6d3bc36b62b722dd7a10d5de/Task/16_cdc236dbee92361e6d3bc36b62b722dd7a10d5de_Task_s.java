 package ca.ualberta.backend;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 
 /** Task is the data class for Tasks.
  * handles storage and easy transferring of
  * tasks between methods
  * @author kerr2
  *
  */
 
 public class Task implements Serializable {
     private static final long serialVersionUID = 1L;
     private String description;
     private boolean isPhotoRequired;
     private String ownerEmail;
     private java.util.Date timestamp;
     private String id;
     private int likes;
     
     private String result_answer;
     private ArrayList<Bitmap> result_photo;
     
     public Task(String description, boolean isPhotoRequired, String ownerEmail, java.util.Date timestamp, String id, int likes) {
     	this.description = description;
         this.isPhotoRequired = isPhotoRequired;
         this.ownerEmail = ownerEmail;
         this.timestamp = timestamp;
         this.id = id;
         this.likes = likes;
         this.result_photo = new ArrayList<Bitmap>();
     }
     
     public Task(String newOwner, String description, boolean isPhotoRequired) {
     	this.description = description;
         this.isPhotoRequired = isPhotoRequired;
         this.ownerEmail = newOwner;
         this.timestamp = new java.util.Date();
         this.id = "local";
         this.likes = 0;
         this.result_photo = new ArrayList<Bitmap>();
     }
     /**getDescription returns the description value
      * @return String
      */
     public String getDescription() {
     	return this.description;
     }
     /**setDescription allows the program to change
      * the description of the task
      * @param description
      */
     public void setDescription(String description) {
     	this.description = description;
     }
     /**getReqPhoto returns the boolean required photo value
      * @return boolean
      */
     public boolean getReqPhoto() {
     	return this.isPhotoRequired;
     }
     /**setReqPhoto allows the program to require
      * that a photo be taken to complete the task
      * @param isPhotoRequired
      */
     public void setReqPhoto(Boolean isPhotoRequired) {
     	this.isPhotoRequired = isPhotoRequired;
     }
     /**getOwner returns the owner value
      * @return String
      */
     public String getOwner() {
     	return this.ownerEmail;
     }
     /**setOwner will attach an owner email to
      * the task
      * @param ownerEmail
      */
     public void setOwner(String ownerEmail) {
     	this.ownerEmail = ownerEmail;
     }
     /**getTimestamp returns the timestamp value
      * @return java.util.Date
      */
     public java.util.Date getTimestamp() {
     	return this.timestamp;
     }
     /**setTimestamp will be used to set the timestamp value
      * if the task is old (for sorting purposes)
      * @param timestamp
      */
     public void setTimestamp(java.util.Date timestamp) {
         this.timestamp = timestamp;
     }
     /**getLikes returns the likes value
      * @return int
      */
     public int getLikes() {
     	return this.likes;
     }
     /**getResAnswer returns the text result value
      * @return String
      */
     public String getResAnswer() {
         return this.result_answer;
     }
     /**setResAnswer attaches a result description
      * to the task upon completion
      * @param result_answer
      */
     public void setResAnswer(String result_answer) {
     	this.result_answer = result_answer;
     }
     /**getResPhoto returns the resulting photo value
      * @return String
      */
     public ArrayList<Bitmap> getResPhoto() {
         return this.result_photo;
     }
     /**setResPhoto attaches a photo to the
      * task upon completion
      * @param result_photo
      */
     public void setResPhoto(ArrayList<Bitmap> result_photo) {
     	this.result_photo = result_photo;
     }
     /**getID returns the ID value from the webservice
      * @return String
      */
     public String getID() {
         return this.id;
     }
     /**incrementLikes will increase the popularity of
      * the task.
      * @return
      */
     public int incrementLikes(){
         likes++;
         return likes;
     }
     /**addPhoto adds a photo to the task
      * 
      * @param image
      */
     public void addPhoto(Bitmap image) {
    	this.result_photo.add(image);
     }
     /**cloneTask creates an identical Task
      * to be used when the task needs to be modified
      * @return
      */
     public Task cloneTask() {
     	Task task = new Task(this.description, this.isPhotoRequired, this.ownerEmail, this.timestamp, this.id, this.likes);
     	task.setResult(this.result_answer, this.result_photo);
     	return task;
     }
     /**setResult attaches the description and resulting
      * photo file to the task
      * @param answer
      * @param result_photo
      */
     public void setResult(String answer, ArrayList<Bitmap> result_photo) {
     	this.setResAnswer(answer);
     	this.setResPhoto(result_photo);
     }
     /**isEqualTo determines if the timestamps of two tasks are
      * equal to each other
      * @param task
      * @return
      */
     public boolean isEqualTo(Task task) {
     	if ((this.timestamp.toString()).equals(task.timestamp.toString())) {
     		return true;
     	} else {
     		return false;
     	}
     }
     /**toString returns the description
      * @return String
      */
     @Override
     public String toString() {
         return this.description;
     }
 }
