 package pl.project.blog.domain;
 
 import java.util.Date;
 import org.svenson.converter.JSONConverter;
 import pl.project.blog.converter.DateConverter;
 
 /**
  *
  * @author Jarosław Bela
  */
 public class Comment extends AppDocument implements Comparable<Comment> {
 
     String post_id;
     //
     String content;
     String author;
     String email;
     Date created;
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public Date getCreated() {
         return created;
     }
 
    @JSONConverter(type=DateConverter.class)
     public void setCreated(Date created) {
         this.created = created;
     }
 
     public String getPost_id() {
         return post_id;
     }
 
     public void setPost_id(String post_id) {
         this.post_id = post_id;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public int compareTo(Comment o) {
         if (getCreated() != null && o.getCreated() != null) {
             return o.getCreated().compareTo(created);
         }
         return 0;
     }
 }
