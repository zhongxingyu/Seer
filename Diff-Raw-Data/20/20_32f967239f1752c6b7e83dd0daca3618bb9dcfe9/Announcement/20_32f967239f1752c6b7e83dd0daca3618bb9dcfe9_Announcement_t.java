 package entity;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQuery;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 @Entity
 @NamedQuery(name="Announcement.getLatestAnnouncements",
         query = "SELECT a from Announcement a JOIN a.course c WHERE ?1 MEMBER OF c.users ORDER BY a.date DESC")
 public class Announcement implements Serializable
 {
     @Id @GeneratedValue(strategy= GenerationType.TABLE)
     private long id;
     
     @Size(max=40)
     @NotNull
     private String title;
     
     @NotNull
    @Column(length=1000)  
     private String message;
     
     @Column(name="AnnouncementDate")
     @Temporal(TemporalType.TIMESTAMP)
     @NotNull
     private Date date;
     
     @ManyToOne
     @NotNull
     private User owner;
     
     @ManyToOne
     @NotNull
     private Course course;
     
     public long getId() {
         return id;
     }
     public String getTitle() {
         return title;
     }
     public String getMessage() {
         return message;
     }
     public Date getDate() {
         return date;
     }
     public User getOwner() {
         return owner;
     }
     public Course getCourse() {
         return course;
     }
     
     
     public void setId(long id) {
         this.id = id;
     }
     public void setTitle(String title) {
         this.title = title;
     }
     public void setMessage(String message) {
         this.message = message;
     }
     public void setDate(Date date) {
         this.date = date;
     }
     public void setOwner(User owner) {
         this.owner = owner;
     }
     public void setCourse(Course course) {
         this.course = course;
     }
 }
