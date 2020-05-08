 package uk.co.acuminous.julez.scenario.source;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static uk.co.acuminous.julez.runner.ScenarioRunner.ConcurrencyUnit.THREADS;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.co.acuminous.julez.runner.ConcurrentScenarioRunner;
 import uk.co.acuminous.julez.scenario.Scenario;
 import uk.co.acuminous.julez.scenario.ScenarioSource;
 import uk.co.acuminous.julez.test.NoOpScenario;
 import uk.co.acuminous.julez.test.TestEventRepository;
 
 public class SizeLimiterTest {
 
     private ConcurrentScenarioRunner runner;
 
     @Before
     public void init() {        
         runner = new ConcurrentScenarioRunner();
     }
     
     @Test
     public void capsNumberOfScenariosToSpecifiedSize() {        
         ScenarioSource scenarios = new SizedScenarioRepeater(new NoOpScenario(), 100);
                 
         SizeLimiter limiter = new SizeLimiter(scenarios, 10);  
                 
         for (int i = 10; i > 0; i--) {
             assertNotNull(limiter.next());
         }
                 
         assertNull(limiter.next());                
     }   
     
     @Test
     public void tolleratesUnderlyingSourceSmallerThanSpecifiedSize() {        
         ScenarioSource scenarios = new SizedScenarioRepeater(new NoOpScenario(), 1);
         
         SizeLimiter limiter = new SizeLimiter(scenarios, 10);
         
         assertNotNull(limiter.next());
         assertNull(limiter.next());        
     } 
     
     @Test
     public void supportsMultiThreading() {
         
         TestEventRepository repository = new TestEventRepository();
         Scenario scenario = new NoOpScenario().register(repository);
         
         ScenarioSource scenarios = new SizedScenarioRepeater(scenario, 200);
         
         SizeLimiter limiter = new SizeLimiter(scenarios, 100);  
         
         runner.queue(limiter).allocate(10, THREADS).go();
         
        assertEquals(100, repository.count());
     }    
 }
