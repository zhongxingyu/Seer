 package pl.agh.enrollme.controller;
 
 /**
  * Author: Piotr Turek
  */
 import org.primefaces.event.*;
 import org.primefaces.model.*;
 import pl.agh.enrollme.model.EnrollConfiguration;
 import pl.agh.enrollme.model.StudentPointsPerTerm;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.model.Term;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent    ;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 public class TestScheduleController implements Serializable {
 
     private static final long serialVersionUID = 1;
 
     private EnrollScheduleModel eventModel;
 
     private EnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
     private String theme;
 
     public TestScheduleController(EnrollConfiguration enrollConfiguration, List<Subject> subjects, List<Term> terms, List<StudentPointsPerTerm> points) {
     }
 
     public String getTheme() {
         return theme;
     }
 
     public void setTheme(String theme) {
         this.theme = theme;
     }
 
     public EnrollScheduleModel getEventModel() {
         return eventModel;
     }
 
     public void setEventModel(EnrollScheduleModel eventModel) {
         this.eventModel = eventModel;
     }
 
     public EnrollScheduleEvent getEvent() {
         return event;
     }
 
     public void setEvent(EnrollScheduleEvent event) {
         this.event = event;
     }
 
     public TestScheduleController() {
         eventModel = new DefaultEnrollScheduleModel();
         GregorianCalendar gc = new GregorianCalendar(2013, 1, 11, 10, 15);
 
         Date begin = gc.getTime();
         gc.add(Calendar.MINUTE, 90);
         Date end = gc.getTime();
         DefaultEnrollScheduleEvent newEvent = new DefaultEnrollScheduleEvent("Analiza", begin, end);
         newEvent.setEditable(false);
         newEvent.setTeacher("dr W. Frydrych");
         newEvent.setPlace("s. 3.27");
         newEvent.setActivityType("Wykład");
         newEvent.setShowPoints(false);
         newEvent.setInteractive(false);
         eventModel.addEvent(newEvent);
         DefaultEnrollScheduleEvent newEvent2 = new DefaultEnrollScheduleEvent("MOwNiT", begin, end);
         newEvent2.setEditable(false);
         newEvent2.setPossible(false);
         newEvent2.setTeacher("dr W. Czech");
         newEvent2.setPlace("s. 3.23");
         newEvent2.setActivityType("Dupa");
         newEvent2.setColor("#00ff00");
         eventModel.addEvent(newEvent2);
         DefaultEnrollScheduleEvent newEvent3 = new DefaultEnrollScheduleEvent("PSI", begin, end);
         newEvent3.setEditable(false);
         newEvent3.setTeacher("dr M. Żabińska");
         newEvent3.setPlace("2.31");
         newEvent3.setPoints(7);
         newEvent3.setImportance(70);
         newEvent3.setActivityType("Lab");
         eventModel.addEvent(newEvent3);
     }
 
     public void addEvent(ActionEvent actionEvent) {
         if(event.getId() == null)
             eventModel.addEvent(event);
         else
             eventModel.updateEvent(event);
 
         event = new DefaultEnrollScheduleEvent();
     }
 
     public void onEventSelect(SelectEvent selectEvent) {
         event = (EnrollScheduleEvent) selectEvent.getObject();
     }
 
     public void onDateSelect(SelectEvent selectEvent) {
         final Date begin = (Date) selectEvent.getObject();
         GregorianCalendar gc = new GregorianCalendar();
         gc.set(begin.getYear(), begin.getMonth(), begin.getDay(), begin.getHours(), begin.getMinutes());
         gc.add(Calendar.MINUTE, 90);
         final Date end = gc.getTime();
         event = new DefaultEnrollScheduleEvent("", begin, end);
     }
 
     public void onEventMove(EnrollScheduleEntryMoveEvent moveEvent) {
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + moveEvent.getDayDelta() + ", Minute delta:" + moveEvent.getMinuteDelta());
         addMessage(message);
         final EnrollScheduleEvent scheduleEvent = moveEvent.getScheduleEvent();
         eventModel.updateEvent(scheduleEvent);
     }
 
     public void onEventResize(EnrollScheduleEntryResizeEvent resizeEvent) {
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + resizeEvent.getDayDelta() + ", Minute delta:" + resizeEvent.getMinuteDelta());
         addMessage(message);
         final EnrollScheduleEvent scheduleEvent = resizeEvent.getScheduleEvent();
         eventModel.updateEvent(scheduleEvent);
     }
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 
 }
