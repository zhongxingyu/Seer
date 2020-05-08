 package uk.co.acuminous.julez.scenario.event;
 
 import org.junit.Test;
 
 import uk.co.acuminous.julez.scenario.event.ScenarioEvent;
 import uk.co.acuminous.julez.scenario.event.ThroughputMonitor;
 import uk.co.acuminous.julez.util.ConcurrencyUtils;
 import static java.util.concurrent.TimeUnit.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 public class ThroughputMonitorTest {
 
     @Test
     public void calculatesThroughputFromPasses() {
         assertThroughput(ScenarioEvent.pass());
     }
     
     @Test
     public void calculatesThroughputFromFailures() {
         assertThroughput(ScenarioEvent.fail());
     }
     
     @Test
     public void calculatesThroughputForVeryQuickScenarios() {
         ThroughputMonitor monitor = new ThroughputMonitor();
         
         monitor.onScenarioEvent(ScenarioEvent.start());
         monitor.onScenarioEvent(ScenarioEvent.pass());
         assertFalse("Throughput was not calculated for extremely quick scenario", 0 == monitor.getThroughput());
     }
     
    
    @Test
    public void workaroundForThroughPutUsingScenarioStartEventInsteadOfScenarioRunnerStartEvent() {
         ThroughputMonitor monitor = new ThroughputMonitor();
         
         monitor.onScenarioEvent(ScenarioEvent.start());
        monitor.onScenarioEvent(ScenarioEvent.pass());
        
        ConcurrencyUtils.sleep(1, SECONDS);
        
        monitor.onScenarioEvent(ScenarioEvent.start());
        monitor.onScenarioEvent(ScenarioEvent.pass());

        ConcurrencyUtils.sleep(1, SECONDS);
        
        monitor.onScenarioEvent(ScenarioEvent.start());
        monitor.onScenarioEvent(ScenarioEvent.pass());
        
        ConcurrencyUtils.sleep(1, SECONDS);
        
        assertEquals(1, monitor.getThroughput());
    }    
    
    private void assertThroughput(ScenarioEvent event) {
        ThroughputMonitor monitor = new ThroughputMonitor();
        
         for (int i = 0; i < 10; i++) {
            monitor.onScenarioEvent(ScenarioEvent.start());            
             monitor.onScenarioEvent(event);
         }
         ConcurrencyUtils.sleep(1, SECONDS);
         
         assertEquals(10, monitor.getThroughput());
         
         ConcurrencyUtils.sleep(1, SECONDS);
         
         assertEquals(5, monitor.getThroughput());        
     }
     
 }
