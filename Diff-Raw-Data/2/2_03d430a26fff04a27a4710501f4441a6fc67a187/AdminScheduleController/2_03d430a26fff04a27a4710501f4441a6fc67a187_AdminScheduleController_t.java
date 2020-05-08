 package pl.agh.enrollme.controller.termmanagement;
 
 import org.primefaces.event.EnrollScheduleEntryMoveEvent;
 import org.primefaces.event.EnrollScheduleEntryResizeEvent;
 import org.primefaces.event.SelectEvent;
 import org.primefaces.model.DefaultEnrollScheduleEvent;
 import org.primefaces.model.DefaultEnrollScheduleModel;
 import org.primefaces.model.EnrollScheduleEvent;
 import org.primefaces.model.EnrollScheduleModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.utils.Week;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Author: Piotr Turek
  */
 public class AdminScheduleController implements Serializable {
 
     private static final long serialVersionUID = -740843017652008055L;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(AdminScheduleController.class);
 
     //Custom container model for the EnrollSchedule component
     private EnrollScheduleModel eventModel;
 
     //Custom event model for the EnrollSchedule component; currently selected event
     private DefaultEnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
     //Currently selected teacher from the teacher list
     private Teacher teacher = new Teacher();
 
     //Currently selected subject from the subject list
     private Subject subject = new Subject();
 
     //Whether currently selected event is certain or not
     private Boolean certain = false;
 
     //Capacity of the current event
     private Integer capacity = 0;
 
 
     //Enroll data
     private EnrollConfiguration enrollConfiguration;
 
     private List<Subject> subjects;
 
     private List<Term> terms;
 
     private List<Teacher> teachers;
     //Enroll data end
 
     //Mapping from EventID to Term
     private Map<String, Term> eventToTermMap = new HashMap<>();
 
     //Schedule component attributes
     private boolean periodic = true;
     private Date initialDate = new Date();
     private String leftHeaderTemplate = "prev, next";
     private String centerHeaderTemplate = "title";
     private String rightHeaderTemplate = "month, agendaWeek, agendaDay";
     private int weekViewWidth = 1500;
     private String view = "agendaWeek";
     //Schedule theme: as of today, unused
     private String theme;
 
 
     public AdminScheduleController(EnrollConfiguration enrollConfiguration, List<Subject> subjects,
                                    List<Term> terms, List<Teacher> teachers) {
         this.enrollConfiguration = enrollConfiguration;
         this.subjects = subjects;
         this.terms = terms;
         this.teachers = teachers;
 
         this.periodic = enrollConfiguration.getPeriodic();
         this.weekViewWidth = enrollConfiguration.getWeekViewWidth();
 
         if (!periodic) {
             leftHeaderTemplate += ", today";
         }
 
         this.eventModel = new DefaultEnrollScheduleModel();
 
         preprocessTerms();
 
 //        GregorianCalendar gc = new GregorianCalendar();
 //        gc.setTime(new Date());
 //
 //        Date begin = gc.getTime();
 //        gc.add(Calendar.MINUTE, 10);
 //        Date end = gc.getTime();
 //        DefaultEnrollScheduleEvent newEvent = new DefaultEnrollScheduleEvent("Analiza", begin, end);
 //        newEvent.setEditable(true);
 //        newEvent.setTeacher("dr W. Frydrych");
 //        newEvent.setPlace("s. 3.27");
 //        newEvent.setActivityType("Wykład");
 //        newEvent.setShowPoints(false);
 //        newEvent.setInteractive(true);
 //        eventModel.addEvent(newEvent);
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
 
     public Teacher getTeacher() {
         return teacher;
     }
 
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
 
     public Subject getSubject() {
         return subject;
     }
 
     public void setSubject(Subject subject) {
         this.subject = subject;
     }
 
     public Boolean getCertain() {
         return certain;
     }
 
     public void setCertain(Boolean certain) {
         this.certain = certain;
     }
 
     public Integer getCapacity() {
         return capacity;
     }
 
     public void setCapacity(Integer capacity) {
         this.capacity = capacity;
     }
 
 
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 
     /**
      * Event triggered when user clicks on a term
      * @param selectEvent
      */
     public void onEventSelect(SelectEvent selectEvent) {
         event = (DefaultEnrollScheduleEvent) selectEvent.getObject();
         LOGGER.debug("Selected event: " + event);
 
         final Term term = eventToTermMap.get(event.getId());
         subject = term.getSubject();
         teacher = term.getTeacher();
         certain = term.getCertain();
         capacity = term.getCapacity();
 
     }
 
     /**
      * Event triggered when user clicks on a date. It creates an empty event at a given time-point.
      * @param selectEvent
      */
     public void onDateSelect(SelectEvent selectEvent) {
         final Date begin = (Date) selectEvent.getObject();
         final GregorianCalendar date = new GregorianCalendar();
         date.setTime(begin);
         date.add(Calendar.MINUTE, 90);
         final Date end = date.getTime();
 
         event = new DefaultEnrollScheduleEvent("", begin, end);
 
         subject = new Subject();
         teacher = new Teacher();
         certain = false;
         capacity = 0;
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Date clicked", "Time: "
                 + begin + " event: " + event);
         addMessage(message);
 
         LOGGER.debug("Date clicked: " + event.getStartDate() + " - " + event.getEndDate() + " event:" + event);
     }
 
     /**
      * Event triggered when user moves an event. It updates a given event and corresponding term.
      * @param moveEvent
      */
     public void onEventMove(EnrollScheduleEntryMoveEvent moveEvent) {
         final EnrollScheduleEvent scheduleEvent = moveEvent.getScheduleEvent();
         LOGGER.debug("Event Moved: " + scheduleEvent);
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Term moved", "Day delta: "
                 + moveEvent.getDayDelta() + ", Minute delta: " + moveEvent.getMinuteDelta());
         addMessage(message);
 
         updateTermTime(scheduleEvent);
     }
 
     /**
      * Event triggered when user resizes an event
      * @param resizeEvent
      */
     public void onEventResize(EnrollScheduleEntryResizeEvent resizeEvent) {
         final EnrollScheduleEvent scheduleEvent = resizeEvent.getScheduleEvent();
         LOGGER.debug("Event Resized: " + scheduleEvent);
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Term resized", "Day delta: "
             + resizeEvent.getDayDelta() + ", Minute delta: " + resizeEvent.getMinuteDelta());
         addMessage(message);
 
         updateTermTime(scheduleEvent);
     }
 
     /**
      * Updates current event (kept in event field)
      */
     public void updateEvent(ActionEvent actionEvent) {
         LOGGER.debug("capacity=" + capacity + ", certain=" + certain + ", subject=" + subject.getName() + ", teacher=" + teacher.getSecondName());
 
         event.setShowPoints(false);
         setEventTeacher(teacher, event);
         event.setColor("#"+subject.getColor());
         event.setEditable(true);
         event.setInteractive(true);
         event.setTitle(subject.getName());
 
         Term term;
 
         if (event.getId() == null) {
             term = new Term();
             term.setTermId(new TermPK());
 
             eventModel.addEvent(event);
             eventToTermMap.put(event.getId(), term);
             LOGGER.debug("New Event: " + event);
         } else {
             term = eventToTermMap.get(event.getId());
 
             eventModel.updateEvent(event);
             LOGGER.debug("Old Event: " + event);
         }
 
         term.setTeacher(teacher);
         term.setCapacity(capacity);
         term.setCertain(certain);
         term.setSubject(subject);
         term.setWeek(Week.YEAR_ALL);    //TODO: implement choosing week type
         term.getTermId().setSubject(subject);
         updateTerm(term, event);
 
         LOGGER.debug("Term modified: " + term);
 
         event = new DefaultEnrollScheduleEvent();
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
 
     public List<Teacher> getTeachers() {
         return teachers;
     }
 
     public void setTeachers(List<Teacher> teachers) {
         this.teachers = teachers;
     }
     //Enroll data getters and setters end
 
 
     //Getters for Schedule attributes begin
     public boolean isPeriodic() {
         return periodic;
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
 
         GregorianCalendar minDate = new GregorianCalendar();
         GregorianCalendar startTime = new GregorianCalendar();
         minDate.setTime(new Date());
 
         //preprocess terms, computing scope of enrollment, creating events etc.
         for (Term t : terms) {
 
             startTime.setTime(t.getStartTime());
 
             if (startTime.before(minDate)) {
                 minDate = startTime;
             }
 
             DefaultEnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
             //setting event's place
             event.setPlace(t.getRoom());
 
             setEventTeacher(t.getTeacher(), event);
 
             final Subject subject = t.getSubject();
 
             //setting event's title to subjects' name
             event.setTitle(subject.getName());
 
             //setting event's color to that of event's subject
             event.setColor("#" + subject.getColor());
 
             //setting event's type
             event.setActivityType(t.getType());
 
 
             //setting whether to display points or not
             event.setShowPoints(false);
 
             event.setEditable(true);
 
             event.setInteractive(true);
 
             //adding event to the model
             eventModel.addEvent(event);
 
             eventToTermMap.put(event.getId(), t);
 
             LOGGER.debug("New event: " + event + " and corresponding term: " + t + " created");
         }
 
         //update time fields, but only if there were some events added, otherwise use defaults
         if (terms.size() > 0) {
             this.initialDate = minDate.getTime();
         }
     }
 
     private void setEventTeacher(Teacher teacher, DefaultEnrollScheduleEvent event) {
         String teacherString = teacher.getDegree() + " " + teacher.getFirstName().charAt(0) +
                 ". " + teacher.getSecondName();
         //setting event's teacher
         event.setTeacher(teacherString);
     }
 
     private void updateTermTime(EnrollScheduleEvent scheduleEvent) {
         eventModel.updateEvent(scheduleEvent);
         final Term term = eventToTermMap.get(scheduleEvent.getId());
         term.setStartTime(scheduleEvent.getStartDate());
         term.setEndTime(scheduleEvent.getEndDate());
     }
 
     private void updateTerm(Term term, DefaultEnrollScheduleEvent event) {
         term.setStartTime(event.getStartDate());
        term.setEndTime(event.getEndDate());
         term.setType(event.getActivityType());
         term.setRoom(event.getPlace());
     }
 
 }
