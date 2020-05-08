 package org.synyx.jmite.domain;
 
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.Duration;
 import org.synyx.jmite.Mite;
 import org.synyx.jmite.domain.support.AbstractEntity;
 import org.synyx.jmite.domain.support.QueryParameter;
 
 
 /**
  * @author Oliver Gierke - gierke@synyx.de
  */
 @XmlRootElement(name = "time-entry")
 public class TimeEntry extends AbstractEntity implements Comparable<TimeEntry> {
 
     @XmlElement(name = "date-at")
     private Date dateAt;
 
     @XmlElement
     private Integer minutes = 0;
 
     @XmlElement
     private String note;
 
     @XmlElement(name = "project-id")
     private Integer projectId;
 
    @XmlElement(name = "customer-id")
     private Integer customerId;
 
     @XmlElement(name = "service-id")
     private Integer serviceId;
 
 
     /**
      * 
      */
     public TimeEntry() {
 
     }
 
 
     /**
      * Creates a new {@link TimeEntry} for the given {@link Project} and
      * {@link Service} of the given duration.
      * 
      * @param project
      * @param service
      * @param duration
      */
     public TimeEntry(Project project, Service service, int duration) {
 
         this(project.getId(), service.getId(), duration);
     }
 
 
     /**
      * Creates a new {@link TimeEntry} for the given {@link Project} id,
      * {@link Service} id and duration.
      * 
      * @param projectId
      * @param serviceId
      * @param duration
      */
     public TimeEntry(int projectId, int serviceId, int duration) {
 
         this.projectId = projectId;
         this.serviceId = serviceId;
         this.minutes = duration;
     }
 
 
     /**
      * @return the note
      */
     public String getNote() {
 
         return note;
     }
 
 
     /**
      * @param note the note to set
      */
     public void setNote(String note) {
 
         this.note = note;
     }
 
 
     /**
      * Returns the day the {@link TimeEntry} was captured for.
      * 
      * @return
      */
     public DateMidnight getAt() {
 
         return null == dateAt ? null : new DateMidnight(dateAt);
     }
 
 
     public TimeEntry setAt(DateMidnight dateAt) {
 
         this.dateAt = dateAt.toDate();
         return this;
     }
 
 
     public TimeEntry setAt(int year, int month, int day) {
 
         this.dateAt = new DateMidnight(year, month, day).toDate();
         return this;
     }
 
 
     /**
      * Returns the duration of the {@link TimeEntry} (in minutes).
      * 
      * @return
      */
     public Duration getDuration() {
 
         return Duration.standardMinutes(minutes);
     }
 
 
     public TimeEntry setDuration(int minutes) {
 
         this.minutes = minutes;
         return this;
     }
 
 
     /**
      * Returns
      * 
      * @return
      */
     QueryParameter getProject() {
 
         return Project.getReference(projectId);
     }
 
 
     public Project getProject(Mite mite) {
 
         return mite.projects().get(projectId);
     }
 
 
     public Project getProject(Projects projects) {
 
         return projects.get(projectId);
     }
 
 
     public TimeEntry setProject(Project project) {
 
         this.projectId = project.getId();
         return this;
     }
 
 
     QueryParameter getCustomer() {
 
         return Customer.getReference(customerId);
     }
 
 
     public Customer getCustomer(Mite mite) {
 
         return mite.customers().get(customerId);
     }
 
 
     public Customer getCustomer(Customers customers) {
 
         return customers.get(customerId);
     }
 
 
     QueryParameter getService() {
 
         return Service.getReference(serviceId);
     }
 
 
     public Service getService(Mite mite) {
 
         return mite.services().get(serviceId);
     }
 
 
     public Service getService(Services services) {
 
         return services.get(serviceId);
     }
 
 
     public TimeEntry setService(Service service) {
 
         this.serviceId = service.getId();
         return this;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     public int compareTo(TimeEntry o) {
 
         return dateAt.compareTo(o.dateAt);
     }
 }
