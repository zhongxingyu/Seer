 package com.mycompany.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.validation.constraints.Size;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 20/10/12
  * Time: 16:54
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 public class Post {
 
     /* Fields */
 
     @Id
     @GeneratedValue
     private Long id;
 
    @Size(min= 1, max = 100)
     private String title;
 
     private String content;
 
     @ManyToOne
     private User author;
 
     @ManyToOne
     private Feed feed;
 
     /* Setter and Getter methods */
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public User getAuthor() {
         return author;
     }
 
     public void setAuthor(User author) {
         this.author = author;
     }
 
     public Feed getFeed() {
         return feed;
     }
 
     public void setFeed(Feed feed) {
         this.feed = feed;
     }
 }
