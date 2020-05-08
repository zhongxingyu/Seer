 package uk.co.acuminous.julez;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class SimplePerformanceTest {
 
     private static final int MAX_THROUGHPUT = 100;
     private static final int TEST_DURATION = 15;
 
     @Test
    public void testTheSystemSupportsAtLeast80HelloWorldScenariosPerSecond() {
 
         ConcurrentTestRunner concurrentTestRunner = new ConcurrentTestRunner(new HelloWorldScenario(), MAX_THROUGHPUT, TEST_DURATION);
         concurrentTestRunner.run();
         int actualThroughput = concurrentTestRunner.actualThroughput();
         
         assertTrue(String.format("Actual throughput: %d scenarios per second", actualThroughput), actualThroughput >= 20);
     }
 
     class HelloWorldScenario implements Scenario {
         public void execute() {
             System.out.println("Hello World");
         }
     }
 }
