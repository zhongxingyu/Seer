 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
 
 import java.util.Date;
 import java.util.Calendar;
 import java.util.Set;
 
 /**
  * @author Rhett Sutphin
  */
 public class BatchRescheduleCommand {
     private ScheduledEventMode<?> newMode;
     private Integer dateOffset;
     private String newReason;
     private Set<ScheduledEvent> events;
     private ScheduledCalendar scheduledCalendar;
 
     private ScheduledCalendarDao scheduledCalendarDao;
 
     public BatchRescheduleCommand(ScheduledCalendarDao scheduledCalendarDao) {
         this.scheduledCalendarDao = scheduledCalendarDao;
     }
 
     public void apply() {
         if (getNewMode() == null) return;
 
         for (ScheduledEvent event : events) {
            if (isValidEventModeChange(event.getCurrentState().getMode(), getNewMode())) {
                 changeState(event);
             }
         }
         scheduledCalendarDao.save(getScheduledCalendar());
     }
 
     private void changeState(ScheduledEvent event) {
         ScheduledEventState newState = getNewMode().createStateInstance();
         newState.setReason(createReason());
         if (newState instanceof DatedScheduledEventState) {
             ((DatedScheduledEventState) newState).setDate(createDate(event.getActualDate()));
         }
         event.changeState(newState);
     }
 
     private Date createDate(Date baseDate) {
         Calendar c = Calendar.getInstance();
         c.setTime(baseDate);
         if(ScheduledEventMode.OCCURRED != getNewMode()) {
             c.add(Calendar.DATE, getDateOffset());
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
 
    private boolean isValidEventModeChange(ScheduledEventMode current, ScheduledEventMode future) {
        boolean result = false;
        if ((ScheduledEventMode.OCCURRED == current && ScheduledEventMode.SCHEDULED == future)
                || (ScheduledEventMode.CANCELED == current && ScheduledEventMode.SCHEDULED == future)
                ||  ScheduledEventMode.SCHEDULED == current) {
            result = true;
        }
        return result;
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
 
     public ScheduledEventMode<?> getNewMode() {
         return newMode;
     }
 
     public void setNewMode(ScheduledEventMode<?> newMode) {
         this.newMode = newMode;
     }
 
     public Integer getDateOffset() {
         return dateOffset;
     }
 
     public void setDateOffset(Integer dateOffset) {
         this.dateOffset = dateOffset;
     }
 
     public Set<ScheduledEvent> getEvents() {
         return events;
     }
 
     public void setEvents(Set<ScheduledEvent> events) {
         this.events = events;
     }
 }
