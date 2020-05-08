 package gov.usgs.cida.coastalhazards.model;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 
 /**
  * We shouldn't need to deserialize this ever, but if we do we need to rethink it
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @Entity
 @Table(name = "activity")
 public class Activity implements Serializable {
     
     public enum ActivityType {
         USE,
         TWEET,
         WEATHER,
         PUBLISH,
         UPDATE,
         INSERT;
     }
 
     private long id;
     private String itemId;
     private ActivityType type;
     private Date activityTimestamp;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     @Column(length = 10)
     public String getItemId() {
         return itemId;
     }
 
     public void setItemId(String itemId) {
         this.itemId = itemId;
     }
 
     @Enumerated
     public ActivityType getType() {
         return type;
     }
 
     public void setType(ActivityType type) {
         this.type = type;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "activity_timestamp")
     public Date getActivityTimestamp() {
         return activityTimestamp;
     }
 
     public void setActivityTimestamp(Date activityTimestamp) {
         this.activityTimestamp = activityTimestamp;
     }
 }
 
