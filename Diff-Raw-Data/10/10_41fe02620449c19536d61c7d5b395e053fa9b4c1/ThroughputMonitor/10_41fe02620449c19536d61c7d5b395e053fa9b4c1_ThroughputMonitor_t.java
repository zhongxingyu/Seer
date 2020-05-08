 package uk.co.acuminous.julez.scenario.event;
 
 import java.util.Arrays;
 import java.util.concurrent.atomic.AtomicInteger;
 
 
 public class ThroughputMonitor implements ScenarioEventHandler {
 
     private long started;
     private AtomicInteger completed = new AtomicInteger();
     
     @Override
     public void onScenarioEvent(ScenarioEvent event) {        
        // Only setting started when it's zero is a dumb bug fix until I add Scenario runner events
        if (ScenarioEvent.START.equals(event.getType()) && started == 0) {
             started = event.getTimestamp();
         } else if (Arrays.asList(ScenarioEvent.PASS, ScenarioEvent.FAIL).contains(event.getType())) {
             completed.incrementAndGet();
         }
     }
     
     public int getThroughput() {
         double duration = Math.max(System.currentTimeMillis() - started, 1);        
         return (int) Math.round(completed.get() * 1000 / duration);
     }
 }
