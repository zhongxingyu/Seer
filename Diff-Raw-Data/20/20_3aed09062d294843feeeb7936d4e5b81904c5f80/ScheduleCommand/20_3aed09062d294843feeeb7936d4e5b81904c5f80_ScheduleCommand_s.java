 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
 
 import java.util.*;
 
 import edu.northwestern.bioinformatics.studycalendar.service.ParticipantCoordinatorDashboardService;
 
 public class ScheduleCommand {
     private Integer toDate;
     private User user;
     private UserDao userDao;
     private ScheduledEventDao scheduledEventDao;
 
     private Map<ActivityType, Boolean> activityTypes = new HashMap<ActivityType, Boolean>();
     private ActivityType activityType;
     private Boolean activityValue;
 
     private static final Logger log = LoggerFactory.getLogger(ScheduleCommand.class.getName());
 
     public Map<String, Object> execute(ParticipantCoordinatorDashboardService participantCoordinatorDashboardService) {
         List<StudyParticipantAssignment> studyParticipantAssignments = getUserDao().getAssignments(getUser());
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("mapOfUserAndCalendar", participantCoordinatorDashboardService.getMapOfCurrentEventsForSpecificActivity(studyParticipantAssignments, getToDate(), getActivityTypes()));
         return model;
     }
 
 
     ////// BOUND PROPERTIES
 
 
     public Integer getToDate() {
         return toDate;
     }
 
     public void setToDate(Integer toDate) {
         this.toDate = toDate;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
 
     public UserDao getUserDao() {
         return userDao;
     }
 
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     public ScheduledEventDao getScheduledEventDao() {
         return scheduledEventDao;
     }
 
     public void setScheduledEventDao(ScheduledEventDao scheduledEventDao) {
         this.scheduledEventDao = scheduledEventDao;
     }
 
 
     public Map<ActivityType, Boolean> getActivityTypes() {
         return activityTypes;
     }
 
     public void setActivityTypes(Map<ActivityType, Boolean> activityTypes) {
         this.activityTypes = activityTypes;
     }
 
     public ActivityType getActivityType() {
         return activityType;
     }
 
     public void setActivityType(ActivityType activityType) {
         this.activityType = activityType;
     }
 
     public Boolean getActivityValue() {
         return activityValue;
     }
 
     public void setActivityValue(Boolean activityValue) {
         this.activityValue = activityValue;
     }
 }
