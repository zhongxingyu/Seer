 package org.bloodtorrent.dto;
 
 import org.hibernate.validator.constraints.NotBlank;
 
 import javax.persistence.Entity;
 import javax.validation.constraints.NotNull;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 22
  * Time: 오전 11:26
  * To change this template use File | Settings | File Templates.
  */
 public class SuccessStory {
     @NotBlank
     private String title;
     @NotBlank
     private String story;
 
     public String getTitle() {
         return title;
     }
 
     public String getStory() {
         return story;
     }
 
     public void setStory(String story) {
         this.story = story;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 }
