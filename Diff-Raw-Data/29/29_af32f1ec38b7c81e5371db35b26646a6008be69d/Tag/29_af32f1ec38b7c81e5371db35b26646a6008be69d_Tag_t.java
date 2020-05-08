 package models;
  
 import java.util.*;
 import javax.persistence.*;
  
 import play.db.jpa.*;
  
 @Entity
 public class Tag extends Model {
  
     public String title;
 	
 	@ManyToOne
    public Articles article;
 	
 	//public Author author;
     
    public Tag(Articles article, String title) {
         this.article = article;
 		this.title = title;
     }
  
 }
