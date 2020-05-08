 package examples;
 
 import static uk.co.acuminous.julez.util.PerformanceAssert.assertMinimumThroughput;
 
 import org.junit.Test;
 
 import uk.co.acuminous.julez.runner.ConcurrentScenarioRunner;
 import uk.co.acuminous.julez.scenario.BaseScenario;
 import uk.co.acuminous.julez.scenario.Scenarios;
 import uk.co.acuminous.julez.scenario.event.ThroughputMonitor;
 import uk.co.acuminous.julez.test.TestUtils;
 
 public class SimplePerformanceTest {
 
     @Test
     public void demonstrateASimplePerformanceTest() {
 
         HelloWorldScenario scenario = new HelloWorldScenario();
 
         ThroughputMonitor throughputMonitor = new ThroughputMonitor();
         scenario.registerListeners(throughputMonitor);                        
 
         Scenarios scenarios = TestUtils.getScenarios(scenario, 100);        
         
         new ConcurrentScenarioRunner().queue(scenarios).run();
 
        assertMinimumThroughput(2000, throughputMonitor.getThroughput());
     }
 
     class HelloWorldScenario extends BaseScenario {        
         public void run() {
             start();
             System.out.print("Hello World ");
             pass();
         }
     }
 }
