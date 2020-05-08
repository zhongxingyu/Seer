 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.*;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Josh
  * Date: 6/14/12
  * Time: 11:07 PM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name = "blog_post")
 public class Post extends Model {
     @Id
     public Long id;
 
     public String title;
     public Date postedAt;
 
 
     @Lob
     private String content;
 
     @ManyToOne
     public User author;
 
 
     public Post(User author, String title, String content) {
         this.author = author;
         this.title = title;
         this.content = content;
         this.postedAt = new Date();
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public static Finder<Long,Post> find = new Finder<Long, Post>(Long.class,Post.class);
 }
