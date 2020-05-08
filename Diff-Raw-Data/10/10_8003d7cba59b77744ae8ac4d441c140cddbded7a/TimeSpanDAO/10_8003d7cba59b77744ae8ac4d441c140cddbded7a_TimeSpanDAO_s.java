 package org.xezz.timeregistration.dao;
 
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.xezz.timeregistration.model.Coworker;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.model.TimeSpan;
 import org.xezz.timeregistration.repositories.CoworkerRepository;
 import org.xezz.timeregistration.repositories.ProjectRepository;
 
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 30.05.13
  * Time: 22:50
  */
 @Configurable(autowire = Autowire.BY_TYPE)
 public class TimeSpanDAO {
     private Long timeSpanId;
     private Long projectId;
     private Long coworkerId;
     private Date startTime;
     private Long durationInMinutes;
     private Date creationDate;
     private Date lastUpdatedDate;
     @Autowired
     CoworkerRepository coworkerRepository;
     @Autowired
     ProjectRepository projectRepository;
 
     public TimeSpanDAO() {
     }
 
    // TODO: Make sure that project and coworker in TimeSpan can not be null!
     public TimeSpanDAO(TimeSpan timeSpan) {
         this(timeSpan.getTimeSpanId(),
                 timeSpan.getProject().getProjectId(),
                 timeSpan.getCoworker().getCoworkerId(),
                 timeSpan.getStartTime(),
                 timeSpan.getDurationInMinutes(),
                 timeSpan.getCreationDate(),
                 timeSpan.getLastUpdatedDate());
     }
 
     public TimeSpanDAO(Long timeSpanId, Long projectId, Long coworkerId, Date startTime, Long durationInMinutes, Date creationDate, Date lastUpdatedDate) {
         this.timeSpanId = timeSpanId;
         this.projectId = projectId;
         this.coworkerId = coworkerId;
         this.startTime = startTime;
         this.durationInMinutes = durationInMinutes;
         this.creationDate = creationDate;
         this.lastUpdatedDate = lastUpdatedDate;
     }
 
     public Long getTimeSpanId() {
         return timeSpanId;
     }
 
     public void setTimeSpanId(Long timeSpanId) {
         this.timeSpanId = timeSpanId;
     }
 
     public Long getProjectId() {
         return projectId;
     }
 
     public void setProjectId(Long projectId) {
         this.projectId = projectId;
     }
 
     public Long getCoworkerId() {
         return coworkerId;
     }
 
     public void setCoworkerId(Long coworkerId) {
         this.coworkerId = coworkerId;
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
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof TimeSpanDAO)) return false;
 
         TimeSpanDAO that = (TimeSpanDAO) o;
 
         if (coworkerId != null ? !coworkerId.equals(that.coworkerId) : that.coworkerId != null) return false;
        if (coworkerRepository != null ? !coworkerRepository.equals(that.coworkerRepository) : that.coworkerRepository != null)
            return false;
         if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
         if (durationInMinutes != null ? !durationInMinutes.equals(that.durationInMinutes) : that.durationInMinutes != null)
             return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(that.lastUpdatedDate) : that.lastUpdatedDate != null)
             return false;
         if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
        if (projectRepository != null ? !projectRepository.equals(that.projectRepository) : that.projectRepository != null)
            return false;
         if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
         if (timeSpanId != null ? !timeSpanId.equals(that.timeSpanId) : that.timeSpanId != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = timeSpanId != null ? timeSpanId.hashCode() : 0;
         result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
         result = 31 * result + (coworkerId != null ? coworkerId.hashCode() : 0);
         result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
         result = 31 * result + (durationInMinutes != null ? durationInMinutes.hashCode() : 0);
         result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = 31 * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
        result = 31 * result + (coworkerRepository != null ? coworkerRepository.hashCode() : 0);
        result = 31 * result + (projectRepository != null ? projectRepository.hashCode() : 0);
         return result;
     }
 
     public Project receiveProject() {
         return projectRepository.findOne(projectId);
     }
 
     public Coworker receiveCoworker() {
         return coworkerRepository.findOne(coworkerId);
     }
 }
