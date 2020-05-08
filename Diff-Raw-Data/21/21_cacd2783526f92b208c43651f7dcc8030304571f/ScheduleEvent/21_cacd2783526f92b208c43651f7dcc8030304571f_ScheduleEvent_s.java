 package jDistsim.core.simulation.simulator.event;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 18.2.13
  * Time: 22:04
  */
 public class ScheduleEvent implements Comparable<ScheduleEvent> {
 
     private final double time;
     private final EventContainer eventContainer;
 
     public ScheduleEvent(double time, EventContainer eventContainer) {
         this.time = time;
         this.eventContainer = eventContainer;
     }
 
     public double getTime() {
         return time;
     }
 
     public EventContainer getEventContainer() {
         return eventContainer;
     }
 
     @Override
     public int compareTo(ScheduleEvent scheduleModule) {
         return new Double(getTime()).compareTo(scheduleModule.getTime());
     }
 
     @Override
     public String toString() {
         return getEventContainer().getModule().getIdentifier();
     }
 }
