 package uk.co.acuminous.julez;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class WebPerformanceTest {
 
     private static final int MAX_THROUGHPUT = 10;
     private static final int TEST_DURATION = 15;
 
     @Test
    public void testTheSystemCanSupportTheRequiredNumberOfSimpleWebScenariosPerSecond() {
 
         ResultRecorder recorder = new ResultRecorder();
         ConcurrentTestRunner concurrentTestRunner = new ConcurrentTestRunner(new SimpleWebScenario(recorder), MAX_THROUGHPUT, TEST_DURATION);
         concurrentTestRunner.run();
 
         assertTrue(String.format("Recorded %d successes", recorder.successCount()), recorder.successCount() >= 1);
         assertTrue(String.format("Recorded %d failures", recorder.failureCount()), recorder.failureCount() <= 5);
         assertTrue(String.format("Actual throughput: %d scenarios per second", concurrentTestRunner.actualThroughput()), concurrentTestRunner.actualThroughput() >= 5);
     }
 
     class SimpleWebScenario implements Scenario {
 
         ResultRecorder recorder;
 
         SimpleWebScenario(ResultRecorder recorder) {
             this.recorder = recorder;
         }
 
         public void execute() {
 
             WebClient webClient = new WebClient();
             webClient.setCssEnabled(false);
             webClient.setJavaScriptEnabled(false);
 
             try {
                 HtmlPage page = webClient.getPage("http://www.bbc.co.uk/news");
                 if (page.getWebResponse().getStatusCode() != 200) {
                     recorder.fail(String.valueOf(page.getWebResponse().getStatusCode()));
                 } else {
                     recorder.pass();
                 }
             } catch (Exception e) {
                 recorder.fail(e.getMessage());
             } finally {
                 webClient.closeAllWindows();
             }
         }
     }
 }
