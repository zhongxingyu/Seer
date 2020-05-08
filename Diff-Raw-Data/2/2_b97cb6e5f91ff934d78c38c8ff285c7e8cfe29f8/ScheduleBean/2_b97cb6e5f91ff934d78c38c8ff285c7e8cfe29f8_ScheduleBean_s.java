 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.binout.soccer5.boundary;
 
 import com.binout.soccer5.controller.MatchEJB;
 import com.binout.soccer5.entity.Match;
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.enterprise.inject.Model;
 import javax.inject.Inject;
 import org.primefaces.event.ScheduleEntrySelectEvent;
 import org.primefaces.model.DefaultScheduleEvent;
 import org.primefaces.model.DefaultScheduleModel;
 import org.primefaces.model.ScheduleEvent;
 import org.primefaces.model.ScheduleModel;
 
 /**
  *
  * @author benoit
  */
 @Model
 public class ScheduleBean {
 
     @Inject
     private MatchEJB ejb;
     private ScheduleModel schedule;
 
     public ScheduleModel getSchedule() {
         return schedule;
     }
 
     public void setSchedule(ScheduleModel schedule) {
         this.schedule = schedule;
     }
 
     @PostConstruct
     public void initSchedule() {
         schedule = new DefaultScheduleModel();
         for (Match m : ejb.listMatches()) {
            schedule.addEvent(new DefaultScheduleEvent("Match " + m.getId(), m.getDate(), m.getEndDate(), m));
         }
     }
 }
