 package pl.agh.enrollme.controller.preferencesmanagement;
 
 import org.primefaces.event.*;
 import org.primefaces.model.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.utils.DayOfWeek;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * This class is used as a schedule controller in preferences-management view
  * Author: Piotr Turek
  */
 public class ScheduleController implements Serializable {
 
     private static final long serialVersionUID = -740843017652008075L;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);
 
     //Progress Ring Controller (statistics controller)
     private ProgressRingController progressController;
 
     //Custom container model for the EnrollSchedule component
     private EnrollScheduleModel eventModel = new DefaultEnrollScheduleModel();
 
     //Custom event model for the EnrollSchedule component; currently selected event
     private DefaultEnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
     //Current reason (of impossibility)
     private String reason = "";
 
     //Maximum number of points to display in the choice edition dialog while editing current event
     private int eventPointsRange = 0;
 
 
     //Enroll data
     private EnrollConfiguration enrollConfiguration;
 
     private List<Subject> subjects;
 
     private List<Term> terms;
 
     private List<StudentPointsPerTerm> points;
     //Enroll data end
 
     //Mapping from Term to StudentPointsPerTerm
     private Map<Term, StudentPointsPerTerm> termToPointsMap = new HashMap<>();
 
     //Mapping from EventID to Term
     private Map<String, Term> eventToTermMap = new HashMap<>();
 
     //Schedule component attributes
     private boolean showWeekends = false;
     private boolean periodic = true;
     private int slotMinutes = 15;           //TODO: add necessary field to enroll so that slotMinutes can be adjusted
     private int firstHour = 8;
     private int minTime = 8;
     private int maxTime = 22;
     private Date initialDate = new Date();
     private String leftHeaderTemplate = "prev, next";
     private String centerHeaderTemplate = "title";
     private String rightHeaderTemplate = "month, agendaWeek, agendaDay";
     private int weekViewWidth = 1500;
     private String view = "agendaWeek";
     //Schedule theme: as of today, unused
     private String theme;
 
 
     public ScheduleController(ProgressRingController progressController, EnrollConfiguration enrollConfiguration,
                               List<Subject> subjects, List<Term> terms, List<StudentPointsPerTerm> points) {
         this.progressController = progressController;
         this.enrollConfiguration = enrollConfiguration;
         this.subjects = subjects;
         this.terms = terms;
         this.points = points;
 
         this.periodic = enrollConfiguration.getPeriodic();
         this.weekViewWidth = enrollConfiguration.getWeekViewWidth();
 
         preprocessTerms();
     }
 
 
     public ProgressRingController getProgressController() {
         return progressController;
     }
 
     public void setProgressController(ProgressRingController progressController) {
         this.progressController = progressController;
     }
 
     public EnrollScheduleModel getEventModel() {
         return eventModel;
     }
 
     public void setEventModel(EnrollScheduleModel eventModel) {
         this.eventModel = eventModel;
     }
 
     public DefaultEnrollScheduleEvent getEvent() {
         return event;
     }
 
     public void setEvent(DefaultEnrollScheduleEvent event) {
         this.event = event;
     }
 
     public String getReason() {
         return reason;
     }
 
     public void setReason(String reason) {
         this.reason = reason;
     }
 
     public int getEventPointsRange() {
         return eventPointsRange;
     }
 
     public void setEventPointsRange(int eventPointsRange) {
         this.eventPointsRange = eventPointsRange;
     }
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 
     /**
      * Event triggered when user clicks on a term
      * @param selectEvent
      */
     public void onEventSelect(SelectEvent selectEvent) {
         LOGGER.debug("Event Select triggered");
         event = (DefaultEnrollScheduleEvent) selectEvent.getObject();
 
         final Term term = eventToTermMap.get(event.getId());
         LOGGER.debug("Event: " + event + " clicked, corresponding term: " + term + " retrieved");
 
         final StudentPointsPerTerm termPoints = termToPointsMap.get(term);
         LOGGER.debug("Points: " + termPoints + " retrieved");
 
         final Subject subject = term.getSubject();
         LOGGER.debug("Subject: " + subject + " retrieved");
 
         final Map<Integer, Integer> pointsMap = progressController.getPointsMap();
 
         reason = termPoints.getReason();
 
         if (!event.isPossible()) {
             eventPointsRange = 0;
         } else {
             eventPointsRange = enrollConfiguration.getPointsPerSubject() - pointsMap.get(subject.getSubjectID())
                     + termPoints.getPoints();
             eventPointsRange = Math.min(eventPointsRange, enrollConfiguration.getPointsPerTerm());
         }
 
         LOGGER.debug("Points range computed to be: " + eventPointsRange);
     }
 
     /**
      * Updates current event (kept in event field)
      */
     public void updateEvent(ActionEvent actionEvent) {
         LOGGER.debug("Event Update triggered");
 
         final Term term = eventToTermMap.get(event.getId());
         LOGGER.debug("Event: " + event + " clicked, corresponding term: " + term + " retrieved");
 
         final StudentPointsPerTerm termPoints = termToPointsMap.get(term);
         LOGGER.debug("Points: " + termPoints + " retrieved");
 
         final int extraPointsLeft = enrollConfiguration.getAdditionalPoints() - progressController.getExtraPointsUsed();
         LOGGER.debug("ExtraPointsLeft: " + extraPointsLeft);
 
         final int pointsDelta = event.getPoints() - termPoints.getPoints();
         LOGGER.debug("Points delta: " + pointsDelta);
 
         if (!event.isPossible()) {
             final FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Choice Changed",
                     "You've just set an impossibility. It will be reviewed by a year representative." +
                             " Remember to save your changes frequently!!!");
             addMessage(message);
 
             termPoints.setReason(reason);
             termPoints.setPoints(-1);
             LOGGER.debug("Impossibility set, reason: " + reason);
 
             progressController.update();
             LOGGER.debug("Progress update called");
 
             event.setShowPoints(false);
 
             eventModel.updateEvent(event);
 
             return;
         }
 
         //Enforcing base boundaries on term points
         if (event.getPoints() > enrollConfiguration.getPointsPerTerm() || event.getPoints() < -1) {
             final FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Rule Broken!",
                     "You surpassed limit of points per term. Change rejected.");
             addMessage(message);
 
             LOGGER.debug("Base boundaries rules broken!");
 
             return;
         }
 
 
         //Enforcing (upper) boundaries on subject points
         if (termPoints.getPoints() + pointsDelta > enrollConfiguration.getPointsPerSubject() + extraPointsLeft) {
             final FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Rule Broken!",
                     "You surpassed limit of points per subject. Change rejected.");
             addMessage(message);
 
             LOGGER.debug("Subject points rules broken");
 
             return;
         }
 
         termPoints.setPoints(event.getPoints());
         LOGGER.debug("New points for term: " + term + " set to: " + termPoints.getPoints());
 
         event.setShowPoints(true);
         event.setPossible(true);
         setEventImportance(event);
 
         final FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Choice Changed",
                 "You've just altered your choice (points delta: " + pointsDelta + "). Currently there are " +
                         event.getPoints() + " points assigned to this term. Remember to save your changes frequently!");
         addMessage(message);
 
         eventModel.updateEvent(event);
 
         LOGGER.debug("Event updated");
 
         progressController.update();
         LOGGER.debug("Progress update called");
     }
 
 
     //Enroll data getters and setters begin
     public EnrollConfiguration getEnrollConfiguration() {
         return enrollConfiguration;
     }
 
     public void setEnrollConfiguration(EnrollConfiguration enrollConfiguration) {
         this.enrollConfiguration = enrollConfiguration;
     }
 
     public List<Subject> getSubjects() {
         return subjects;
     }
 
     public void setSubjects(List<Subject> subjects) {
         this.subjects = subjects;
     }
 
     public List<Term> getTerms() {
         return terms;
     }
 
     public void setTerms(List<Term> terms) {
         this.terms = terms;
     }
 
     public List<StudentPointsPerTerm> getPoints() {
         return points;
     }
 
     public void setPoints(List<StudentPointsPerTerm> points) {
         this.points = points;
     }
     //Enroll data getters and setters end
 
 
     //Getters for Schedule attributes begin
     public boolean isShowWeekends() {
         return showWeekends;
     }
 
     public boolean isPeriodic() {
         return periodic;
     }
 
     public int getSlotMinutes() {
         return slotMinutes;
     }
 
     public int getFirstHour() {
         return firstHour;
     }
 
     public int getMinTime() {
         return minTime;
     }
 
     public int getMaxTime() {
         return maxTime;
     }
 
     public Date getInitialDate() {
         return initialDate;
     }
 
     public String getLeftHeaderTemplate() {
         return leftHeaderTemplate;
     }
 
     public String getCenterHeaderTemplate() {
         return centerHeaderTemplate;
     }
 
     public String getRightHeaderTemplate() {
         return rightHeaderTemplate;
     }
 
     public int getWeekViewWidth() {
         return weekViewWidth;
     }
 
     public String getView() {
         return view;
     }
 
     public String getTheme() {
         return theme;
     }
     //Getters for Schedule attributes end
 
 
     private void preprocessTerms() {
         //create mapping: Term -> StudentPointsPerTerm to allow for efficient access to data
         for (StudentPointsPerTerm p : points) {
             termToPointsMap.put(p.getTerm(), p);
             LOGGER.debug("Creating map entry for term: " + p.getTerm() + " and points: " + p);
         }
         LOGGER.debug(termToPointsMap.size() + " entries created");
 
         int minHour = Integer.MAX_VALUE;
         int maxHour = Integer.MIN_VALUE;
         GregorianCalendar minDate = new GregorianCalendar();
         minDate.setTimeInMillis(Long.MAX_VALUE);
         GregorianCalendar maxDate = new GregorianCalendar();
         maxDate.setTimeInMillis(Long.MIN_VALUE);
 
         //preprocess terms, computing scope of enrollment, creating events etc.
         for (Term t : terms) {
             LOGGER.debug("Processing of term: " + t);
             GregorianCalendar startTime = new GregorianCalendar();
             startTime.setTime(t.getStartTime());
             GregorianCalendar endTime = new GregorianCalendar();
             endTime.setTime(t.getEndTime());
 
             //if term starts on a Saturday or Sunday, set showWeekends to true
             if(startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                 showWeekends = true;
                 LOGGER.debug("ShowWeekends set to true");
             }
 
             final int termStartHour = startTime.get(Calendar.HOUR_OF_DAY);
             final int termEndHour = endTime.get(Calendar.HOUR_OF_DAY);
 
             if (termStartHour < minHour) {
                 minHour = termStartHour;
             }
 
             if (termEndHour > maxHour) {
                 maxHour = termEndHour;
             }
 
             if (startTime.before(minDate)) {
                 minDate = startTime;
             }
 
             if (endTime.after(maxDate)) {
                 maxDate = endTime;
             }
 
             StudentPointsPerTerm p = termToPointsMap.get(t);
             LOGGER.debug("Points: " + p + " retrieved");
             DefaultEnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
             //setting event's points
             if (p.getPoints() == -1) {
                 event.setPoints(0);
                 event.setPossible(false);
             } else {
                 event.setPoints(p.getPoints());
                 event.setPossible(true);
             }
 
             //setting event's place
             event.setPlace(t.getRoom());
 
             Teacher teacher = t.getTeacher();
             String teacherString = teacher.getDegree() + " " + teacher.getFirstName().charAt(0) +
                     ". " + teacher.getSecondName();
             //setting event's teacher
             event.setTeacher(teacherString);
 
             final Subject subject = t.getSubject();
 
             //setting event's title to subjects' name
             event.setTitle(subject.getName());
 
             //setting event's start date
             event.setStartDate(t.getStartTime());
 
             //setting event's end date
             event.setEndDate(t.getEndTime());
 
             //setting event importance as percent of total points available to this event
             setEventImportance(event);
 
             //setting event's color to that of event's subject
             event.setColor("#" + subject.getColor());
 
             //setting event's type
             event.setActivityType(t.getType());
 
             //setting whether event is static or not
             event.setInteractive(!t.getCertain() && !p.getAssigned());
 
             //setting whether to display points or not
             event.setShowPoints(!t.getCertain() && event.isPossible() && !p.getAssigned());
 
             //event's shouldn't be editable
             event.setEditable(false);
 
             //adding event to the model
             eventModel.addEvent(event);
             LOGGER.debug("New event: " + event + " added to eventModel");
 
             eventToTermMap.put(event.getId(), t);
             LOGGER.debug("Term processing finished");
         }
         LOGGER.debug("All terms processed");
 
         //update time fields, but only if there were some events added, otherwise use defaults
         if (terms.size() > 0) {
             this.minTime = minHour;
             this.firstHour = minHour;
             this.maxTime = maxHour != 23 ? maxHour + 1 : maxHour;
             this.initialDate = minDate.getTime();
 
             //infering right header contents and default view
             if (minDate.get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)
                     && minDate.get(Calendar.DAY_OF_YEAR) == maxDate.get(Calendar.DAY_OF_YEAR)) {
                 this.rightHeaderTemplate = "agendaDay";
                 this.view = "agendaDay";
             } else if (minDate.get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)
                     && minDate.get(Calendar.WEEK_OF_YEAR) == maxDate.get(Calendar.WEEK_OF_YEAR)) {
                 this.rightHeaderTemplate = "agendaWeek, agendaDay";
                 this.view = "agendaWeek";
             }
         }
     }
 
     private void setEventImportance(DefaultEnrollScheduleEvent event) {
         if (event.isPossible()) {
             event.setImportance((int) ((double) event.getPoints() / (double) enrollConfiguration.getPointsPerTerm() * 100));
         } else {
             event.setImportance(100);
         }
     }
 
 }
