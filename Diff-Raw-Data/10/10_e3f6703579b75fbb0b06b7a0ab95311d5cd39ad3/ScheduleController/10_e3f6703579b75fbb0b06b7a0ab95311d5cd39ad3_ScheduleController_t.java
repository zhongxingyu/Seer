 package pl.agh.enrollme.controller;
 
import org.primefaces.event.*;
 import org.primefaces.model.*;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class ScheduleController implements Serializable {
 
     private static final long serialVersionUID = 1;
 
     private EnrollScheduleModel eventModel;
 
     private EnrollScheduleEvent event = new DefaultEnrollScheduleEvent();
 
     private String theme;
 
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
 
     public ScheduleController() {
         eventModel = new DefaultEnrollScheduleModel();
         GregorianCalendar gc = new GregorianCalendar(2013, 1, 11, 10, 15);
 
         Date begin = gc.getTime();
         gc.add(Calendar.HOUR_OF_DAY, 1);
         Date end = gc.getTime();
         //DefaultEnrollScheduleEvent newEvent = new DefaultEnrollScheduleEvent("Champions League Match", begin, end);
         //newEvent.setEditable(false);
         //eventModel.addEvent(newEvent);
         //DefaultEnrollScheduleEvent newEvent2 = new DefaultEnrollScheduleEvent("Polish League Match", begin, end);
         //newEvent2.setEditable(false);
         //eventModel.addEvent(newEvent2);
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
         event = new DefaultEnrollScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
         //EnrollScheduleEvent newEvent = eventModel.getEvents().get(0);
         //addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Event1 data:", "importance: " + newEvent.getImportance() +
                // " points: " + newEvent.getPoints() + " possible: " + newEvent.isPossible() + " teacher: " + newEvent.getTeacher() +
                 //" place: " + newEvent.getPlace()));
     }
 
    public void onEventMove(EnrollScheduleEntryMoveEvent event) {
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());
 
         addMessage(message);
     }
 
    public void onEventResize(EnrollScheduleEntryResizeEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());
 
        addMessage(message);
     }
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
 
         addMessage(message);
     }
 
 }
