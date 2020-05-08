 package net.devmask.tuit.models;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author <a href="mailto:jonathan@devmask.net"> Jonathan Garay </a>
  *         07/02/12 Creado
  */
 @Entity
 @Table(name= "tuits")
 @NamedQueries({
         @NamedQuery(name = "user.timeline",
                query = "SELECT t from User u, Tuit t " +
                         "JOIN u.following following  " +
                        "WHERE (t.user = following OR t.user = :user) " +
                        "AND u = :user " +
                         "ORDER by t.stamp DESC " ),
 
         @NamedQuery(name = "user.timeline.since",
                 query = "SELECT t from User u, Tuit t " +
                         "JOIN u.following following  " +
                         "WHERE (t.user = following OR t.user = :user) " +
                         "AND u = :user " +
                         "AND t.stamp >= :date " +
                         "ORDER by t.stamp DESC " ),
 
         @NamedQuery(name = "all.tuits",
                 query = "SELECT t from User u, Tuit t ")
 
 
 })
 public class Tuit implements Serializable{
 
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
 
     @ManyToOne
     private User user;
 
     @NotNull(message = "Can't tuit blank  ")
     private String content;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date stamp = new Date();
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public Date getStamp() {
         return stamp;
     }
 
     public void setStamp(Date stamp) {
         this.stamp = stamp;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 }
