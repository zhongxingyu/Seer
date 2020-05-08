 package org.xezz.timeregistration.model;
 
 import org.xezz.timeregistration.dao.TimeSpanDAO;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 26.04.13
  * Time: 17:53
  */
 @Entity
 public class TimeSpan implements Serializable {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long timeSpanId;
 
     // Relevant data
     @ManyToOne(optional = false)
     private Project project;
     @ManyToOne(optional = false)
     private Coworker coworker;
     @Temporal(TemporalType.TIMESTAMP)
     private Date startTime;
     private Long durationInMinutes;
 
 
     // "Auditing"
     @Temporal(TemporalType.TIMESTAMP)
     private Date creationDate;
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastUpdatedDate;
 
    public TimeSpan() {}
 
     public TimeSpan(TimeSpanDAO t) {
         this.timeSpanId = t.getTimeSpanId();
        this.project = t.getProject();
        this.coworker = t.getCoworker();
         this.startTime = t.getStartTime();
         this.durationInMinutes = t.getDurationInMinutes();
         this.creationDate = t.getCreationDate();
         this.lastUpdatedDate = t.getLastUpdatedDate();
     }
 
     @PrePersist
     private void setDateBeforePersisting() {
         lastUpdatedDate = creationDate = new Date();
     }
 
     @PreUpdate
     private void updateLastEditedDate() {
         lastUpdatedDate = new Date();
     }
 
     public Long getTimeSpanId() {
         return timeSpanId;
     }
 
     public void setTimeSpanId(Long timeSpanId) {
         this.timeSpanId = timeSpanId;
     }
 
     public Project getProject() {
         return project;
     }
 
     public void setProject(Project project) {
         this.project = project;
     }
 
     public Coworker getCoworker() {
         return coworker;
     }
 
     public void setCoworker(Coworker coworker) {
         this.coworker = coworker;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getLastUpdatedDate() {
         return lastUpdatedDate;
     }
 
     public void setLastUpdatedDate(Date lastUpdatedDate) {
         this.lastUpdatedDate = lastUpdatedDate;
     }
 
     public Date getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Date startTime) {
         this.startTime = startTime;
     }
 
     public Long getDurationInMinutes() {
         return durationInMinutes;
     }
 
     public void setDurationInMinutes(Long durationInMinutes) {
         this.durationInMinutes = durationInMinutes;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof TimeSpan)) return false;
 
         TimeSpan timeSpan = (TimeSpan) o;
 
         if (coworker != null ? !coworker.equals(timeSpan.coworker) : timeSpan.coworker != null) return false;
         if (creationDate != null ? !creationDate.equals(timeSpan.creationDate) : timeSpan.creationDate != null)
             return false;
         if (durationInMinutes != null ? !durationInMinutes.equals(timeSpan.durationInMinutes) : timeSpan.durationInMinutes != null)
             return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(timeSpan.lastUpdatedDate) : timeSpan.lastUpdatedDate != null)
             return false;
         if (project != null ? !project.equals(timeSpan.project) : timeSpan.project != null) return false;
         if (startTime != null ? !startTime.equals(timeSpan.startTime) : timeSpan.startTime != null) return false;
         if (timeSpanId != null ? !timeSpanId.equals(timeSpan.timeSpanId) : timeSpan.timeSpanId != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = timeSpanId != null ? timeSpanId.hashCode() : 0;
         result = 31 * result + (project != null ? project.hashCode() : 0);
         result = 31 * result + (coworker != null ? coworker.hashCode() : 0);
         result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
         result = 31 * result + (durationInMinutes != null ? durationInMinutes.hashCode() : 0);
         result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = 31 * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
         return result;
     }
 }
