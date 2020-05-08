 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import org.hibernate.annotations.*;
 import org.hibernate.annotations.CascadeType;
 
 import javax.persistence.Entity;
 import javax.persistence.*;
 import javax.persistence.Table;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 @Entity
 @Table
 @GenericGenerator(name="id-generator", strategy="native",
     parameters = {
         @Parameter(name="sequence", value="seq_scheduled_calendars_id")
     }
 )
 public class ScheduledCalendar extends AbstractMutableDomainObject {
     private StudyParticipantAssignment assignment;
     private List<ScheduledArm> scheduledArms = new LinkedList<ScheduledArm>();
 
     ////// LOGIC
 
     public void addArm(ScheduledArm arm) {
         scheduledArms.add(arm);
         arm.setScheduledCalendar(this);
     }
 
     @Transient
     public ScheduledArm getCurrentArm() {
         for (ScheduledArm arm : getScheduledArms()) {
             if (!arm.isComplete()) return arm;
         }
         return getScheduledArms().get(getScheduledArms().size() - 1);
     }
 
     @Transient
     public ScheduledEvent getNextScheduledEvent(Date currentDate) {
         for (ScheduledArm arm : getScheduledArms()) {
             if (!arm.isComplete()) {
                return arm.getNextScheduledEvent(currentDate);
             }
         }
         return null;
     }
 
 
 
     ////// BEAN PROPERTIES
 
     @ManyToOne
     @JoinColumn(name = "assignment_id")
     public StudyParticipantAssignment getAssignment() {
         return assignment;
     }
 
     public void setAssignment(StudyParticipantAssignment assignment) {
         this.assignment = assignment;
     }
 
     // This is annotated this way so that the IndexColumn will work with
     // the bidirectional mapping.  See section 2.4.6.2.3 of the hibernate annotations docs.
     @OneToMany
     @JoinColumn(name="scheduled_calendar_id", nullable=false)
     @IndexColumn(name="list_index")
     @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN})
     public List<ScheduledArm> getScheduledArms() {
         return scheduledArms;
     }
 
     public void setScheduledArms(List<ScheduledArm> arms) {
         this.scheduledArms = arms;
     }
 }
