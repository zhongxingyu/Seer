 package hudson.plugins.sctmexecutor.publisher;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class TestDomainObjectLogic {
   private SCTMTestSuiteResult rootSuite;
 
   @Before
   public void setup() {
     rootSuite = new SCTMTestSuiteResult("root", null);
     SCTMTestSuiteResult suite1 = new SCTMTestSuiteResult("suite1", null);
     SCTMTestCaseResult test1 = new SCTMTestCaseResult("case1", null);
     test1.addConfigurationResult("config1", new SCTMTestResult(SCTMTestResult.TestState.PASSED, 1, ""));
     test1.addConfigurationResult("config2", new SCTMTestResult(SCTMTestResult.TestState.SKIPPED, 1, ""));
     test1.addConfigurationResult("config3", new SCTMTestResult(SCTMTestResult.TestState.FAILED,1, ""));
     suite1.addChild(test1);
     rootSuite.addChild(suite1);
     SCTMTestSuiteResult suite2 = new SCTMTestSuiteResult("suite2", null);
     SCTMTestSuiteResult subsuite = new SCTMTestSuiteResult("suite2.1", null);
     SCTMTestCaseResult test2 = new SCTMTestCaseResult("case2", null);
     test2.addConfigurationResult("config1", new SCTMTestResult(SCTMTestResult.TestState.PASSED, 2, ""));
     test2.addConfigurationResult("config2", new SCTMTestResult(SCTMTestResult.TestState.SKIPPED, 2, ""));
     test2.addConfigurationResult("config3", new SCTMTestResult(SCTMTestResult.TestState.FAILED, 2, ""));
     subsuite.addChild(test2);
     SCTMTestCaseResult test3 = new SCTMTestCaseResult("case3", null);
     test3.addConfigurationResult("config1", new SCTMTestResult(SCTMTestResult.TestState.PASSED, 3, ""));
     test3.addConfigurationResult("config2", new SCTMTestResult(SCTMTestResult.TestState.SKIPPED, 3, ""));
     test3.addConfigurationResult("config3", new SCTMTestResult(SCTMTestResult.TestState.FAILED, 3, ""));
     subsuite.addChild(test3);
     suite2.addChild(subsuite);
     rootSuite.addChild(suite2);
     SCTMTestSuiteResult suite3 = new SCTMTestSuiteResult("suite3", null);
     rootSuite.addChild(suite3);
    this.rootSuite.calculateConfigurationResults();
     
   }
   
   @Test
   public void testTest1Result() throws Exception {
     SCTMTestCaseResult test1 = rootSuite.getChildSuiteByName("suite1").getChildTestByName("case1");
     assertEquals("case1", test1.getName());
     assertEquals(3, test1.getDuration(), 0.1);
     
     SCTMTestResult result = test1.getTestResultForConfiguration("config1");
     assertEquals(1, result.getPassCount());
     assertEquals(0, result.getSkipCount());
     assertEquals(0, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.PASSED, result.getState());
     assertEquals(1, result.getDuration(), 0.1);
     
     result = test1.getTestResultForConfiguration("config2");
     assertEquals(0, result.getPassCount());
     assertEquals(1, result.getSkipCount());
     assertEquals(0, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.SKIPPED, result.getState());
     assertEquals(1, result.getDuration(), 0.1);
     
     result = test1.getTestResultForConfiguration("config3");
     assertEquals(0, result.getPassCount());
     assertEquals(0, result.getSkipCount());
     assertEquals(1, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.FAILED, result.getState());
     assertEquals(1, result.getDuration(), 0.1);
   }
   
   @Test
   public void testSuite1Result() throws Exception {
     SCTMTestSuiteResult suite1 = rootSuite.getChildSuiteByName("suite1");
     assertEquals("suite1", suite1.getName());
     assertEquals(3, suite1.getDuration(), 0.1);
     
     SCTMTestResult result = suite1.getTestResultForConfiguration("config1");
     assertEquals(1, result.getPassCount());
     assertEquals(0, result.getSkipCount());
     assertEquals(0, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.PASSED, result.getState());
     assertEquals(1, result.getDuration(), 0.1);
     
     result = suite1.getTestResultForConfiguration("config2");
     assertEquals(0, result.getPassCount());
     assertEquals(1, result.getSkipCount());
     assertEquals(0, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.SKIPPED, result.getState());
     assertEquals(1, result.getDuration(), 0.1);
     
     result = suite1.getTestResultForConfiguration("config3");
     assertEquals(0, result.getPassCount());
     assertEquals(0, result.getSkipCount());
     assertEquals(1, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.FAILED, result.getState());
     assertEquals(1, result.getDuration(), 0.1);
   }
   
   @Test
   public void testRootResult() throws Exception {
     assertEquals(18, rootSuite.getDuration(), 0.1);
     
     SCTMTestResult result = this.rootSuite.getTestResultForConfiguration("config1");
     assertEquals(3, result.getPassCount());
     assertEquals(0, result.getSkipCount());
     assertEquals(0, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.PASSED, result.getState());
     assertEquals(6, result.getDuration(), 0.1);
     
     result = this.rootSuite.getTestResultForConfiguration("config2");
     assertEquals(0, result.getPassCount());
     assertEquals(3, result.getSkipCount());
     assertEquals(0, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.SKIPPED, result.getState());
     assertEquals(6, result.getDuration(), 0.1);
     
     result = this.rootSuite.getTestResultForConfiguration("config3");
     assertEquals(0, result.getPassCount());
     assertEquals(0, result.getSkipCount());
     assertEquals(3, result.getFailCount());
     assertEquals(SCTMTestResult.TestState.FAILED, result.getState());
     assertEquals(6, result.getDuration(), 0.1);
   }
   
   @Test
   public void testEmptySuiteResult() throws Exception {
     SCTMTestSuiteResult suite3 = this.rootSuite.getChildSuiteByName("suite3");
     assertEquals("suite3", suite3.getName());
     SCTMTestResult result = suite3.getTestResultForConfiguration("config1");
     assertNull(result);
     
     result = suite3.getTestResultForConfiguration("config2");
     assertNull(result);
     
     result = suite3.getTestResultForConfiguration("config3");
     assertNull(result);
   }
 }
