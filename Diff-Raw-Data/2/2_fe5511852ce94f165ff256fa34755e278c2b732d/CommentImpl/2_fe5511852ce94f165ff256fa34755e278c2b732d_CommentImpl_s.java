 package org.test.bugtracker.impl.model;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.test.bugtracker.model.Bug;
 import org.test.bugtracker.model.Comment;
 import org.test.bugtracker.model.User;
 
 @Entity(name="Comment")
 @Table(name="BT_COMMENTS")
 public class CommentImpl implements Comment {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, targetEntity = UserImpl.class)
     private Bug bug;
     @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, targetEntity = UserImpl.class)
     private User author;
     @Column(nullable = false)
     private String message;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Bug getBug() {
         return bug;
     }
 
     public void setBug(Bug bug) {
         this.bug = bug;
     }
 
     public User getAuthor() {
         return author;
     }
 
     public void setAuthor(User author) {
         this.author = author;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     @Override
     public int hashCode() {
         return (int) (id ^ (id >>> 32));
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         CommentImpl other = (CommentImpl) obj;
         if (id != other.id) {
             return false;
         }
         return true;
     }
 
 }
