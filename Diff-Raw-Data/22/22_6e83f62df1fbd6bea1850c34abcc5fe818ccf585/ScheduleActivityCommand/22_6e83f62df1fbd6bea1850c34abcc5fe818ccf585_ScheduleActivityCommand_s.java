 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Rhett Sutphin
  */
 public class ScheduleActivityCommand {
     private static final Logger log = LoggerFactory.getLogger(ScheduleActivityCommand.class.getName());
 
     private ScheduledActivity event;
     private ScheduledActivityMode newMode;
     private String newReason;
     private Date newDate;
     private String newNotes;
 
     private ScheduledCalendarDao scheduledCalendarDao;
 
     public ScheduleActivityCommand(ScheduledCalendarDao scheduledCalendarDao) {
         this.scheduledCalendarDao = scheduledCalendarDao;
     }
 
     ////// LOGIC
 
     public void apply() {
         if (hasStateChange()) {
             event.changeState(createState());
         }
         event.setNotes(getNewNotes());
         scheduledCalendarDao.save(event.getScheduledStudySegment().getScheduledCalendar());
     }
 
     private boolean hasStateChange() {
         return getNewMode() != null;
     }
 
     public ScheduledActivityState createState() {
         ScheduledActivityState instance = getNewMode().createStateInstance();
         instance.setReason(getNewReason());
         instance.setDate(getNewDate());
 
         return instance;
     }
 
     ////// BOUND PROPERTIES
 
     public ScheduledActivity getEvent() {
         return event;
     }
 
     public void setEvent(ScheduledActivity event) {
         this.event = event;
     }
 
     public ScheduledActivityMode getNewMode() {
         return newMode;
     }
 
     public void setNewMode(ScheduledActivityMode newMode) {
         this.newMode = newMode;
     }
 
     public Collection<ScheduledActivityMode> getEventSpecificMode(){
        List<ScheduledActivityMode> availableModes =  new ArrayList<ScheduledActivityMode>();
        if (event != null) {
            availableModes = ScheduledActivityMode.getAvailableModes(event.getCurrentState(), event.isConditionalEvent());
         }
         return availableModes;
     }
 
     public String getNewReason() {
         return newReason;
     }
 
     public void setNewReason(String newReason) {
         this.newReason = newReason;
     }
 
     public Date getNewDate() {
         if (newDate == null) {
             return getEvent() == null ? null : getEvent().getActualDate();
         } else {
             return newDate;
         }
     }
 
     public void setNewDate(Date newDate) {
         this.newDate = newDate;
     }
 
     public String getNewNotes() {
         if (newNotes  == null) {
             return getEvent() == null ? null : getEvent().getNotes();
         } else {
             return newNotes;
         }
     }
 
     public void setNewNotes(String newNotes) {
         this.newNotes = newNotes;
     }
 }
