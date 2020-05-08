 package ca.ualberta.backend;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /** Task is the data class for Tasks.
  * handles storage and easy transferring of
  * tasks between methods
  * @author kerr2
  *
  */
 
 public class Task implements Serializable
 {
     private static final long serialVersionUID = 1L;
     private String description;
     private boolean isPhotoRequired;
     private boolean isAudioRequired;
     private boolean isComplete;
     private String ownerEmail;
     private Date timestamp;
     
     private String result_answer = "no answer";
     private String result_photofile = "none";
     private String result_audiofile = "none";
     private String id = "local";
     
     /** Task constructor makes a Task 
      * object.
      * @param String description
      * @param boolean isPhotoRequired
      * @param boolean isAudioRequired
      * @param String newOwner
      * @param id
      */
     public Task(String newOwner, String description, boolean isPhotoRequired, boolean isAudioRequired) {
     	this.description = description;
         this.isPhotoRequired = isPhotoRequired;
         this.isAudioRequired = isAudioRequired;
         this.isComplete = false;
         this.ownerEmail = newOwner;
         this.timestamp = new java.util.Date();
     }
     /** getOwner returns the owner of the task
      * 
      * @return String Owner of Task
      */
     public String getOwner(){
     	return this.ownerEmail;
     }
     /** getDescription returns the description
      * 
      * @return String
      */
     public String getDescription() {
     	return this.description;
     }
     /** getDescription returns the requirement for photo
      * 
      * @return boolean
      */
     public boolean getReqPhoto() {
     	return this.isPhotoRequired;
     }
     /** getReqAudio returns the requirement for audio
      * 
      * @return boolean
      */
     public boolean getReqAudio() {
     	return this.isAudioRequired;
     }
     /** getComplete returns the completion status
      * 
      * @return boolean
      */
     public boolean getComplete() {
     	return this.isComplete;
     }
     /** setComplete will set completion status
      * 
      * @param boolean isDone
      */
    public void setComplete(boolean isDone){
    	this.isComplete = isDone;
     }
    
     /** getTimeStamp returns the time stamp
      * 
      * @return Date
      */
    public Date getTimestamp() {
    	return this.timestamp;
    }
    
    public void setTimestamp(java.util.Date timestamp) {
        this.timestamp = timestamp;
    }
    
     public String getResAnswer() {
         return this.result_answer;
     }
     /** getResPhotoName returns the photo file from the result
      * 
      * @return String
      */
     public String getResPhotoName() {
         return this.result_photofile;
     }
     /** getResAudioName returns the audio file from the result
      * 
      * @return String
      */
     public String getResAudioName() {
         return this.result_audiofile;
     }
     /** setResult defines the fulfillment parameters
      * 
      * @param String answer
      * @param String photofile
      * @param String audiofile
      */
     public void setResult(String answer, String photofile, String audiofile) {
     	this.result_answer = answer;
     	this.result_photofile = photofile;
     	this.result_audiofile = audiofile;
     }
     /** isEqualto compares separate tasks
      * 
      * @param Task task
      * @return Task
      */
     public boolean isEqualTo(Task task) {
     	if ((this.timestamp.toString()).equals(task.timestamp.toString())) {
     		return true;
     	} else {
     		return false;
     	}
     }
     /** cloneTask makes a new task to be edited without
      * modifying the old task
      * @return Task
      */
     public Task cloneTask(){
     	Task newTask = new Task(this.ownerEmail, this.description, this.isPhotoRequired, this.isAudioRequired);
     	newTask.ownerEmail = this.ownerEmail;
     	newTask.isComplete = this.isComplete;
     	newTask.timestamp = this.timestamp;
     	newTask.result_answer = this.result_answer;
     	newTask.result_photofile = this.result_photofile;
     	newTask.result_audiofile = this.result_audiofile;
     	return newTask;
     }
     
     /** toString returns the description of the Task
      * @return String
      */
     @Override
     public String toString() {
         return this.description;
     }
     /** getID returns the ID of the task if it's a community
      * task, otherwise returns "local"
      * @return String
      */
     public String getID(){
         return this.id;
     }
     public void setID(String id){
         this.id = id;
     }
 }
