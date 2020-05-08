 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.salaboy.rolo.the.robot.mind.listeners;
 
 import com.salaboy.rolo.events.ExternalNotificationEvent;
 import com.salaboy.rolo.events.MindNotificationEvent;
 import com.salaboy.rolo.events.ReadDistanceSensorEvent;
 import com.salaboy.rolo.events.RobotPartAddedEvent;
 import com.salaboy.rolo.model.reports.DistanceReport;
 import javax.annotation.PostConstruct;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import org.kie.api.cdi.KSession;
 import org.kie.api.runtime.KieSession;
 import org.kie.api.runtime.rule.EntryPoint;
 
 /**
  *
  * @author salaboy
  * This listener is intended to wrap the Knowledge Session used to assist the robot in making decisions
  * It consumes (Observes) already processed events, meaning that it doesn't care about low level hardware
  * notifications
  * 
  * The internal session generates Mind Notifications, which are some kind of suggestions to act on.
  * It also has direct access to body parts to control directly
  */
 @Singleton
 public class RobotKnowledgeListener {
     
     @Inject
     @KSession("rolo")
     private KieSession rolo;
 
     @Inject
     private Event<MindNotificationEvent> mindNotifications;
     
     @Inject
     private Event<ExternalNotificationEvent> externalNotifications;
     
     public RobotKnowledgeListener() {
     }
     
     @PostConstruct
     private void init(){
         rolo.setGlobal("notifications", mindNotifications);
         rolo.setGlobal("external", externalNotifications);
         new Thread(new Runnable() {
             public void run() {
                 rolo.fireUntilHalt();
             }
         }).start();
         
     }
     
     public void onRobotPartAddedEvent(@Observes RobotPartAddedEvent event){
         rolo.insert(event.getRobotPart());
         
     }
     
     public void onReadSensor(@Observes ReadDistanceSensorEvent event){
         EntryPoint entryPoint = rolo.getEntryPoint("distance-sensor");
         if(entryPoint != null){
             entryPoint.insert(new DistanceReport(event.getSensorName(), event.getDistance()));
             
         }else{
             System.out.println(">>> Rolo Mind: I've recieved a "+ event + "but I don't know what to do with it");
         }
     }
     
     
     
 }
