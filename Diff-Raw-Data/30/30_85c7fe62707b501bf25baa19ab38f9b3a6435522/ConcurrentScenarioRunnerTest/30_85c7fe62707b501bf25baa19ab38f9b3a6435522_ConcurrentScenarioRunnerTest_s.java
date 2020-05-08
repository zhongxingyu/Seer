 package uk.co.acuminous.julez.runner;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static uk.co.acuminous.julez.runner.ScenarioRunner.ConcurrencyUnit.THREADS;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import uk.co.acuminous.julez.event.Event;
 import uk.co.acuminous.julez.scenario.BaseScenario;
 import uk.co.acuminous.julez.scenario.Scenario;
 import uk.co.acuminous.julez.scenario.ScenarioEvent;
 import uk.co.acuminous.julez.scenario.ScenarioSource;
 import uk.co.acuminous.julez.scenario.source.SizedScenarioRepeater;
 import uk.co.acuminous.julez.test.NoOpScenario;
 import uk.co.acuminous.julez.test.SleepingScenario;
 import uk.co.acuminous.julez.test.TestEventRepository;
 import uk.co.acuminous.julez.test.ThreadCountingScenario;
 
 public class ConcurrentScenarioRunnerTest {
     
     @Test
     public void runsScenarios() {        
         TestEventRepository repository = new TestEventRepository();
         Scenario scenario = new NoOpScenario().register(repository);
         
         ScenarioSource scenarios = new SizedScenarioRepeater(scenario, 10); 
         
         new ConcurrentScenarioRunner().queue(scenarios).go();
         
        assertEquals(10, repository.count());        
     }
     
     @Test    
     public void stopsWhenScenariosTakeTooLong() {
         
         TestEventRepository repository = new TestEventRepository();
         
         Scenario scenario = new SleepingScenario(4, SECONDS).register(repository);       
         
         ScenarioSource scenarios = new SizedScenarioRepeater(scenario, 3);
         
         new ConcurrentScenarioRunner().queue(scenarios).runFor(5, SECONDS).go();
         
         assertEquals(2, repository.count(Event.TYPE, ScenarioEvent.BEGIN));
         assertEquals(1, repository.count(Event.TYPE, ScenarioEvent.END));
     }
     
     @Test    
     public void defersStartUntilAGivenTime() {
         DateTime now = new DateTime();
         long desiredStartTime = now.plusSeconds(5).getMillis();       
 
         StartTimeCapturingScenario scenario = new StartTimeCapturingScenario();        
         ScenarioSource scenarios = new SizedScenarioRepeater(scenario, 1);        
         
         new ConcurrentScenarioRunner().queue(scenarios).waitUntil(desiredStartTime).go();
         
         assertTrue("Runner did not defer start", !scenario.actualStartTime.isBefore(desiredStartTime));
     } 
     
     @Test    
     public void stopsWhenScenariosTakeTooLongFromTheSpecifiedStartTime() {
         
         DateTime now = new DateTime();
         long desiredStartTime = now.plusSeconds(5).getMillis();       
 
         TestEventRepository repository = new TestEventRepository();        
         Scenario scenario = new SleepingScenario(4, SECONDS).register(repository);
         
         ScenarioSource scenarios = new SizedScenarioRepeater(scenario, 3);        
         
         new ConcurrentScenarioRunner().queue(scenarios).waitUntil(desiredStartTime).runFor(5, SECONDS).go();
         
         assertEquals(2, repository.count(Event.TYPE, ScenarioEvent.BEGIN));
         assertEquals(1, repository.count(Event.TYPE, ScenarioEvent.END));
     }    
     
     @Test
     public void raisesBeginEvent() {
         ScenarioSource scenarios = new SizedScenarioRepeater(new NoOpScenario(), 10);        
 
         TestEventRepository repository = new TestEventRepository();        
         
         new ConcurrentScenarioRunner().register(repository).queue(scenarios).go();
         
         assertEquals(ScenarioRunnerEvent.BEGIN, repository.first().getType());
     }   
 
     @Test
     public void raisesBeginEventOnOrAfterDeferedStart() {
         DateTime now = new DateTime();
         DateTime desiredStartTime = now.plusSeconds(5);       
 
         TestEventRepository repository = new TestEventRepository();        
         
         StartTimeCapturingScenario scenario = new StartTimeCapturingScenario();        
         ScenarioSource scenarios = new SizedScenarioRepeater(scenario, 1);        
         
         ConcurrentScenarioRunner runner = new ConcurrentScenarioRunner();
         runner.register(repository);        
         runner.queue(scenarios).waitUntil(desiredStartTime.getMillis()).go();
         
         assertTrue(repository.first().getTimestamp() >= desiredStartTime.getMillis());
     }       
     
     @Test
     public void raisesEndEvent() {
         ScenarioSource scenarios = new SizedScenarioRepeater(new NoOpScenario(), 10);        
 
         TestEventRepository repository = new TestEventRepository();        
         
         ConcurrentScenarioRunner runner = new ConcurrentScenarioRunner();
         runner.register(repository);
         runner.queue(scenarios).go();
         
         assertEquals(ScenarioRunnerEvent.END, repository.last().getType());
     }
     
     @Test
     public void canBeConfiguredForMultipleThreads() {
         ThreadCountingScenario scenario = new ThreadCountingScenario();
         ScenarioSource scenarios = new SizedScenarioRepeater(scenario, 1000);        
         
         new ConcurrentScenarioRunner().queue(scenarios).allocate(10, THREADS).go();
         
         assertEquals(10, scenario.count());
     }    
     
     class StartTimeCapturingScenario extends BaseScenario { 
 
         DateTime actualStartTime;                
         
         @Override public void run() {
             actualStartTime = new DateTime();
         }
     }
 }
