 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.DatedScheduledActivityState;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
 
 import java.util.Date;
 import java.util.Calendar;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Rhett Sutphin
  */
 public class BatchRescheduleCommand {
     private ScheduledActivityMode<?> newMode;
     private Integer dateOffset;
     private Integer moveDateOffset;
     private String newReason;
     private Set<ScheduledActivity> events;
     private ScheduledCalendar scheduledCalendar;
 
     private ScheduledCalendarDao scheduledCalendarDao;
 
     private static final Logger log = LoggerFactory.getLogger(BatchRescheduleCommand.class.getName());
 
     public BatchRescheduleCommand(ScheduledCalendarDao scheduledCalendarDao) {
         this.scheduledCalendarDao = scheduledCalendarDao;
     }
 
     public void apply() {
         for (ScheduledActivity event : events) {
             if (getNewMode() == null || (event.isValidNewState(newMode.getClazz()))) {
                 changeState(event);
             }
         }
         scheduledCalendarDao.save(getScheduledCalendar());
     }
 
     private void changeState(ScheduledActivity event) {
         ScheduledActivityState newState;
         if (getNewMode() == null) {
            newState = event.getCurrentState();
         } else {
             newState = getNewMode().createStateInstance();
         }
         newState.setReason(createReason());
 
         if (newState instanceof DatedScheduledActivityState) {
             ((DatedScheduledActivityState) newState).setDate(createDate(event.getActualDate()));
         }
         event.changeState(newState);
     }
 
     private Date createDate(Date baseDate) {
         Calendar c = Calendar.getInstance();
         Integer shift = null;
         if (getNewMode() == null) {
             shift = getMoveDateOffset();
         } else {
             shift = getDateOffset();
         }
         c.setTime(baseDate);
         if(getNewMode() == null || (ScheduledActivityMode.OCCURRED != getNewMode())) {
             if (shift != null) {
                 c.add(Calendar.DATE, shift);
             } else {
                 c.add(Calendar.DATE, 0);
             }
         }
         return c.getTime();
     }
 
     private String createReason() {
         StringBuilder reason = new StringBuilder("Batch change");
         String message = getNewReason();
         if (message != null) {
             reason.append(": ").append(message);
         }
         return reason.toString();
     }
 
     ////// BOUND PROPERTIES
 
     public ScheduledCalendar getScheduledCalendar(){
         return scheduledCalendar;
     }
 
     public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
         this.scheduledCalendar = scheduledCalendar;
     }
 
     public String getNewReason() {
         return newReason;
     }
 
     public void setNewReason(String newReason) {
         this.newReason = newReason;
     }
 
     public ScheduledActivityMode<?> getNewMode() {
         return newMode;
     }
 
     public void setNewMode(ScheduledActivityMode<?> newMode) {
         this.newMode = newMode;
     }
 
     public Integer getDateOffset() {
         return dateOffset;
     }
 
     public void setDateOffset(Integer dateOffset) {
         this.dateOffset = dateOffset;
     }
 
     public Set<ScheduledActivity> getEvents() {
         return events;
     }
 
     public void setEvents(Set<ScheduledActivity> events) {
         this.events = events;
     }
 
 
     public Integer getMoveDateOffset() {
         return moveDateOffset;
     }
 
     public void setMoveDateOffset(Integer moveDateOffset) {
         this.moveDateOffset = moveDateOffset;
     }
 }
