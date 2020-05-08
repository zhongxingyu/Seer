 package uk.co.acuminous.julez;
 
 import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.jbehave.core.configuration.MostUsefulConfiguration;
 import org.jbehave.core.embedder.Embedder;
 import org.jbehave.core.embedder.SilentEmbedderMonitor;
 import org.jbehave.core.failures.FailingUponPendingStep;
 import org.jbehave.core.io.StoryFinder;
 import org.jbehave.core.steps.InstanceStepsFactory;
 import org.junit.Test;
 
 public class JBehavePerformanceTest {
 
     private static final int MAX_THROUGHPUT = 10;
     private static final int TEST_DURATION = 15;
 
     @Test
    public void testTheSystemSupportsTheRequiredNumberOfJBehaveScenariosPerSecond() {
 
         ResultRecorder recorder = new ResultRecorder();
         ConcurrentTestRunner concurrentTestRunner = new ConcurrentTestRunner(new JBehaveScenario("scenario1.txt", recorder), MAX_THROUGHPUT, TEST_DURATION);
         concurrentTestRunner.run();
 
         assertTrue(String.format("Recorded %d successes", recorder.successCount()), recorder.successCount() >= 1);
         assertTrue(String.format("Recorded %d failures", recorder.failureCount()), recorder.failureCount() <= 5);
         assertTrue(String.format("Actual throughput: %d scenarios per second", concurrentTestRunner.actualThroughput()), concurrentTestRunner.actualThroughput() >= 2);
     }
 
     class JBehaveScenario implements Scenario {
 
         ResultRecorder recorder;
         private final String scenario;
 
         public JBehaveScenario(String scenario, ResultRecorder recorder) {
             this.scenario = scenario;
             this.recorder = recorder;
         }
 
         public void execute() {
             try {
                 Embedder embedder = new Embedder();
                 embedder.useEmbedderMonitor(new SilentEmbedderMonitor(null));
                 embedder.useConfiguration(new MostUsefulConfiguration().usePendingStepStrategy(new FailingUponPendingStep()));
                 embedder.useCandidateSteps(new InstanceStepsFactory(embedder.configuration(), new JBehaveSteps(recorder)).createCandidateSteps());
 
                 List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(JBehavePerformanceTest.class), scenario, "");
                 embedder.runStoriesAsPaths(storyPaths);
             } catch (Throwable t) {
                 recorder.fail(t.getMessage());
             }
         }
     }
 }
