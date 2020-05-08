 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mycompany.ove.gui;
 
 // import com.mycompany.ove.model.OveScheduleEvent;
 import com.mycompany.ove.model.School;
 import com.mycompany.ove.model.SchoolRegistry;
 import com.mycompany.ove.model.Session;
 import com.mycompany.ove.model.Worker;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 
 import javax.ejb.EJB;
 
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Named;
 import org.primefaces.event.ScheduleEntryMoveEvent;
 import org.primefaces.event.ScheduleEntryResizeEvent;
 import org.primefaces.event.SelectEvent;
 import org.primefaces.model.DefaultScheduleEvent;
 import org.primefaces.model.DefaultScheduleModel;
 import org.primefaces.model.ScheduleEvent;
 
 import org.primefaces.model.ScheduleModel;
 
 /**
  *
  * @author Gustav
  */
 @Named("scheduleBean")
 @RequestScoped
 public class ScheduleBean implements Serializable
 {
     @EJB
     private SchoolRegistry reg;
     
     private ScheduleModel eventModel;  
     private List<School> schoolList;
 
     private List<Session> sessionList;
     private Date firstDate,secondDate;
     private String schoolName;
     private String notation;
     private int nbrOfStudents;
     private List<Worker> tutor;
       
     //Overide this to handle events,   comments and time 
     private ScheduleEvent event = new DefaultScheduleEvent();
    // private ScheduleEvent event = new OveScheduleEvent();
     
     public ScheduleBean() {  
         //Get a list of sessions from somewhere 
        eventModel = new DefaultScheduleModel();
       //  eventModel = (ScheduleModel) new OveScheduleEvent();
        schoolList =  reg.getRange(0, reg.getCount());
     //   createSessions(schoolList);
        
         //eventModel.addEvent(new DefaultScheduleEvent("Champions League Match", previousDay8Pm(), previousDay11Pm()));  
       //  eventModel.addEvent(new DefaultScheduleEvent("Birthday Party", today1Pm(), today6Pm()));  
      //   eventModel.addEvent(new DefaultScheduleEvent("Breakfast at Tiffanys", nextDay9Am(), nextDay11Am()));  
      //   eventModel.addEvent(new DefaultScheduleEvent("Plant the new garden stuff", theDayAfter3Pm(), fourDaysLater3pm())); 
         
     }  
     //Create all sessions for all schools in this list
   
     public Date getRandomDate(Date base) {  
         Calendar date = Calendar.getInstance();  
         date.setTime(base);  
         date.add(Calendar.DATE, ((int) (Math.random()*30)) + 1);    //set random day of month  
           
         return date.getTime();  
     }  
       
     public Date getInitialDate() {  
         Calendar calendar = Calendar.getInstance();  
         calendar.set(calendar.get(Calendar.YEAR), Calendar.FEBRUARY, calendar.get(Calendar.DATE), 0, 0, 0);  
           
         return calendar.getTime();  
     }  
     /**
      * Update the eventModel 
      */
     private void updateModel()
     {
         schoolList = reg.getRange(0, reg.getCount());
         for(School s: schoolList)
         {
             for(Session sc: s.getSchedule().getSessions())
             {
                firstDate = new Date(sc.getStartTime());
                secondDate = new Date(sc.getEndTime());
                eventModel.addEvent(new DefaultScheduleEvent(s.getName(),
                        firstDate,secondDate));
             }
         }
     }
     /**
      * 
      * @return the eventModel 
      */
     public ScheduleModel getEventModel() {  
         updateModel();
         return eventModel;  
     }  
 
     public ScheduleEvent getEvent() {  
         return event;  
     }  
   
     public void setEvent(ScheduleEvent event) {  
         this.event = event;  
     }  
       
     public void addEvent(ActionEvent actionEvent) {  
         if(event.getId() == null)  
             eventModel.addEvent(event);  
         else  
             eventModel.updateEvent(event);  
       
         //Använd info från dialogen
        // event = new OveScheduleEvent();
         //event = new OveScheduleEvent(null, null, null, 0, null, null);
         event = new DefaultScheduleEvent();  
     }  
       //Trycker event dialogen
     public void onEventSelect(SelectEvent selectEvent) {  
         event = (ScheduleEvent) selectEvent.getObject();  
     }  
    
     public void onDateSelect(SelectEvent selectEvent) {  
 
         event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());  
         String name = event.getTitle();
         Date startTime = event.getStartDate();
         //Hämta info från databasen
         School sch = reg.getByName(name).get(0);
         setSchoolName(sch.getName());
         for(Session s: reg.getByName(name).get(0).getSchedule().getSessions())
         {
             if(s.getStartTime() ==  event.getStartDate().getTime())
             {
                 setNotation(s.getNotation());
                 setNbrOfStudents(s.getNbrOfStudents());
                 setTutor(s.getTutors());
             }
         }
     }  
     
     public void onEventMove(ScheduleEntryMoveEvent event) {  
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());  
           
         addMessage(message);  
     }  
       
     public void onEventResize(ScheduleEntryResizeEvent event) {  
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());  
           
         addMessage(message);  
     }  
       
     private void addMessage(FacesMessage message) {  
         FacesContext.getCurrentInstance().addMessage(null, message);  
     }  
 
     /**
      * @return the schoolName
      */
     public String getSchoolName()
     {
         return schoolName;
     }
 
     /**
      * @param schoolName the schoolName to set
      */
     public void setSchoolName(String schoolName)
     {
         this.schoolName = schoolName;
     }
 
     /**
      * @return the notation
      */
     public String getNotation()
     {
         return notation;
     }
 
     /**
      * @param notation the notation to set
      */
     public void setNotation(String notation)
     {
         this.notation = notation;
     }
 
     /**
      * @return the nbrOfStudents
      */
     public int getNbrOfStudents()
     {
         return nbrOfStudents;
     }
 
     /**
      * @param nbrOfStudents the nbrOfStudents to set
      */
     public void setNbrOfStudents(int nbrOfStudents)
     {
         this.nbrOfStudents = nbrOfStudents;
     }
 
     /**
      * @return the tutor
      */
     public List<Worker> getTutor()
     {
         return tutor;
     }
 
     /**
      * @param tutor the tutor to set
      */
     public void setTutor(List<Worker> tutor)
     {
         this.tutor = tutor;
     }
 }  
 
