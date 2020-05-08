 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
 
 import play.data.validation.Email;
 import play.db.jpa.Model;
 import play.libs.Codec;
 
 
 @Entity
 public class JobApplication extends Model {
 
     public final static int PER_PAGE = 15;
 
     public static enum JobStatus {
         NEW, INPROGRESS, ARCHIVED
     }
 
     public String name;
     @Email public String email;
     public Date submitted = new Date();
     public String phone;
     @Lob public String message;
     @Enumerated(EnumType.STRING) public JobStatus status = JobStatus.NEW;
     public String uniqueID;
 
     public JobApplication(String name, String email, String message, List<Attachment> attachments) {
         this.name = name;
         this.email = email;
         this.message = message;
         this.uniqueID = Codec.UUID().substring(0,5);
     }
 
     public void addMessage(String from, String email, String content) {
         new Note(this, from, email, content, false).create();
     }
 
     public void addInternalNote(String from, String email, String content, Integer rating) {
         Note note = new Note(this, from, email, content, true);
         note.rating = rating;
         note.create();
     }
 
     public List<Note> getNotes() {
         return Note.find("jobApplication = ? order by date asc", this).fetch();
     }
     
     
     public Float getRating() {
         List<Note> notes = getNotes();
         int votes = 0;
         int total = 0;
         for( int i = 0; i < notes.size(); i++ ){
             int rating = notes.get( i ).rating == null ? 0 : notes.get( i ).rating;
             if(rating > 0){
                votes++;
                total+= rating;
             }
         }
         return votes == 0 ? 0 : (float)total / (float)votes;
     }
     
     public List<Attachment> getAttachments() {
         return Attachment.find("byJobApplication", this).fetch();
     }
 
     public static int pageCount(JobStatus status) {
         int count = find("status", status).fetch().size();
         int result = (int) Math.floor(count / PER_PAGE);
         return result + 1;
     }
 
     @Override
     public String toString() {
         return name + " <" + email + ">";
     }
 
 }
