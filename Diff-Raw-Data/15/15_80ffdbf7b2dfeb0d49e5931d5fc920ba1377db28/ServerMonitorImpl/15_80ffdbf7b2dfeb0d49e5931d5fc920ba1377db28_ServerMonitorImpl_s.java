 package lv.k2611a.jmx;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedResource;
 import org.springframework.stereotype.Service;
 
 @Service
 @ManagedResource
 @Scope("singleton")
 public class ServerMonitorImpl implements ServerMonitor {
 
     private LastNCounter last5Counter = new LastNCounter(5);
     private LastNCounter last40Counter = new LastNCounter(40);
     private LastNCounter last200Counter = new LastNCounter(200);
 
     @ManagedAttribute
    public Double averageTickTimeLast5Ticks() {
        return last5Counter.averageTickTime();
     }
 
     @ManagedAttribute
    public Double averageTickTimeLast40Ticks() {
         return last40Counter.averageTickTime();
     }
 
     @ManagedAttribute
    public Double averageTickTimeLast200Ticks() {
         return last200Counter.averageTickTime();
     }
 
 
     @Override
     public void reportTickTime(long tickTime) {
         last5Counter.reportTickTime(tickTime);
         last40Counter.reportTickTime(tickTime);
         last200Counter.reportTickTime(tickTime);
     }
 
 }
