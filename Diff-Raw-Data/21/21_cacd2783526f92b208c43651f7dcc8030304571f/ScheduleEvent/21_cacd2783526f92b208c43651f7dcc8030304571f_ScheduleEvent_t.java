 package jDistsim.core.simulation.simulator.event;
 
import jDistsim.core.simulation.distributed.DistributedModule;
import jDistsim.core.simulation.modules.lib.NullModule;

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
        Double timeA = new Double(getTime());
        Double timeB = new Double(scheduleModule.getTime());
        if (timeA.compareTo(timeB) == 0) {
            if (scheduleModule.getEventContainer().getModule() instanceof NullModule ||
                    scheduleModule.getEventContainer().getModule() instanceof DistributedModule) {
                return -1;
            }
        }
         return new Double(getTime()).compareTo(scheduleModule.getTime());
     }
 
     @Override
     public String toString() {
         return getEventContainer().getModule().getIdentifier();
     }
 }
