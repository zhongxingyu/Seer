 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.DatedScheduledActivityState;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 
 public class EventsRescheduleCommand {
     private Integer toDate;
     private Date currentDate;
     private String reason;
     private ScheduledCalendar scheduledCalendar;
 
     private ScheduledCalendarDao scheduledCalendarDao;
 
     private static final Logger log = LoggerFactory.getLogger(EventsRescheduleCommand.class.getName());
 
     public EventsRescheduleCommand(ScheduledCalendarDao scheduledCalendarDao) {
         this.scheduledCalendarDao = scheduledCalendarDao;
     }
 
     public void apply() {
         if (currentDate == null) return;
         if (toDate == null ) return;
         changeEvents();
         scheduledCalendarDao.save(getScheduledCalendar());
     }
 
 
     private void changeEvents() {
         List<ScheduledStudySegment> scheduledStudySegments = scheduledCalendar.getScheduledStudySegments();
         for (ScheduledStudySegment segment : scheduledStudySegments) {
             List<ScheduledActivity> events = segment.getActivities();
             List<ScheduledActivity> filteredEvents = filterEventsByDateAndState(events);
             for (ScheduledActivity event : filteredEvents) {
                 changeState(event);
             }
         }
     }
 
     private List<ScheduledActivity> filterEventsByDateAndState (List<ScheduledActivity> events) {
         List<ScheduledActivity> filteredEvents = new ArrayList<ScheduledActivity>();
 
            for (ScheduledActivity event : events) {
             if (event.getActualDate().getTime() >= currentDate.getTime() && ((event.getCurrentState().getMode() == ScheduledActivityMode.SCHEDULED) ||
                         (event.getCurrentState().getMode() == ScheduledActivityMode.CONDITIONAL))) {
                 filteredEvents.add(event);
             }
         }
 
         return filteredEvents;
     }
 
     private void changeState(ScheduledActivity event) {
        ScheduledActivityState newState = event.getCurrentState().getMode().createStateInstance();
         newState.setReason(createReason());
         if (newState instanceof DatedScheduledActivityState) {
             ((DatedScheduledActivityState) newState).setDate(createDate(event.getActualDate()));
         }
         event.changeState(newState);
     }
 
     private Date createDate(Date baseDate) {
         Calendar c = Calendar.getInstance();
         c.setTime(baseDate);
         c.add(Calendar.DATE, getToDate().intValue());
         return c.getTime();
     }
 
     private String createReason() {
         StringBuilder reason = new StringBuilder("Full schedule change");
         String message = getReason();
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
 
     public String getReason() {
         return reason;
     }
 
     public void setReason(String reason) {
         this.reason = reason;
     }
 
     public Integer getToDate() {
         return toDate;
     }
 
     public void setToDate(Integer toDate) {
         this.toDate = toDate;
     }
 
     public Date getCurrentDate() {
         return currentDate;
     }
 
     public void setCurrentDate(Date currentDate) {
         this.currentDate = currentDate;
     }
 }
