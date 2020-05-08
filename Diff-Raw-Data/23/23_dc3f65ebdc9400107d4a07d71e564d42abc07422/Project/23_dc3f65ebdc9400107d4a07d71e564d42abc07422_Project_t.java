 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.redoddity.portfolios.model;
 
 import it.redoddity.model.Validatable;
 import java.util.Map;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.URL;
 
 /**
  *
  * @author madchicken
  */
 public class Project extends Validatable {
     
     private String name;
     
     @NotBlank
     private String description;
     
     @URL
     private String url;
     
     @URL
     private String thumbnailUrl;
     
     
     private Float rating;
     private String tags;
     
     public Project(Map<String, Object> properties) {
         super(properties);
     }
 
     public Project() {
         super();
     }
     
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setRating(Float rating) {
         this.rating = rating;
     }
 
     public Float getRating() {
         return rating;
     }
 
     public void setThumbnailUrl(String thumbnailUrl) {
         this.thumbnailUrl = thumbnailUrl;
     }
 
     public String getThumbnailUrl() {
         return thumbnailUrl;
     }
 
     public void setTags(String tags) {
         this.tags = tags;
     }
 
     public String getTags() {
         return tags;
    }    
 }
