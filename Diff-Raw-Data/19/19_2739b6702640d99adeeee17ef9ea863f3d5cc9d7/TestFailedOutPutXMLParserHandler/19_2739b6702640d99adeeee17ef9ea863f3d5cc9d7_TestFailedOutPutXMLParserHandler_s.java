 package hudson.plugins.sctmexecutor.publisher;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import hudson.plugins.sctmexecutor.publisher.SCTMTestResult.TestState;
 import hudson.plugins.sctmexecutor.publisher.handler.OutputXMLParserHandler;
 
 import java.io.File;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class TestFailedOutPutXMLParserHandler {
 
   private static final String CONFIG_DUMMY = "config1";
   private static final String TESTRESULTS_ROOTPATH = "src/test/resources/hudson/plugins/sctmexecutor/publisher/";
   private SCTMTestSuiteResult rootSuite;
 
   @Before
   public void setUp() throws Exception {
     SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
     rootSuite = new SCTMTestSuiteResult("root", null);
     OutputXMLParserHandler handler = new OutputXMLParserHandler(rootSuite , CONFIG_DUMMY);
     parser.parse(new File(TESTRESULTS_ROOTPATH+"failedTestResult/output.xml"), handler);
   }
   
   @Test
   public void testParsedStructure() throws Exception {
     assertEquals(5, this.rootSuite.getTotalCount());
     assertEquals(1, this.rootSuite.getChildren().size());
     
     SCTMTestSuiteResult plan = this.rootSuite.getChildSuiteByName("plan.pln");
     assertNotNull(plan);
     assertEquals("plan.pln", plan.getName());
     assertEquals(5, plan.getTotalCount());
     assertEquals(5, plan.getTestResultForConfiguration(CONFIG_DUMMY).getTotalCount());
     assertEquals(4, plan.getChildren().size());
     
     SCTMTestSuiteResult suite = plan.getChildSuiteByName("codeField.t");
     assertNotNull(suite);
     assertEquals("codeField.t", suite.getName());
     assertEquals(1, suite.getTotalCount());
     assertEquals(1, suite.getChildren().size());
     
     SCTMTestCaseResult test = suite.getChildTestByName("codeField"); 
     assertNotNull(test);
     assertEquals("codeField", test.getName());
     assertEquals(1, test.getTotalCount());
     
     suite = plan.getChildSuiteByName("window.t");
     assertNotNull(suite);
     assertEquals("window.t", suite.getName());
     assertEquals(1, suite.getTotalCount());
     assertEquals(1, suite.getChildren().size());
     
     test = suite.getChildTestByName("WindowTest"); 
     assertNotNull(test);
     assertEquals("WindowTest", test.getName());
     assertEquals(1, test.getTotalCount());
     assertEquals(1, test.getTestResultForConfiguration(CONFIG_DUMMY).getTotalCount());
     
     suite = plan.getChildSuiteByName("table.t");
     assertNotNull(suite);
     assertEquals("table.t", suite.getName());
     assertEquals(2, suite.getTotalCount());
     assertEquals(2, suite.getChildren().size());
     
     test = suite.getChildTestByName("TableTest"); 
     assertNotNull(test);
     assertEquals("TableTest", test.getName());
     assertEquals(1, test.getTotalCount());
     
     test = suite.getChildTestByName("TableTest2"); 
     assertNotNull(test);
     assertEquals("TableTest2", test.getName());
     assertEquals(1, test.getTotalCount());
     
     suite = plan.getChildSuiteByName("calculator.test.ParameterizedCalcTest");
     assertNotNull(suite);
     assertEquals("calculator.test.ParameterizedCalcTest", suite.getName());
     assertEquals(1, suite.getTotalCount());
     assertEquals(1, suite.getChildren().size());
     
     test = suite.getChildTestByName("testAdd[0]"); 
     assertNotNull(test);
     assertEquals("testAdd[0]", test.getName());
     assertEquals(1, test.getTotalCount());
   }
   
   @Test
   public void testTestResult() throws Exception {
     SCTMTestSuiteResult suite = this.rootSuite.getChildSuiteByName("plan.pln").getChildSuiteByName("table.t");
     SCTMTestResult suiteResult = suite.getTestResultForConfiguration(CONFIG_DUMMY);
     assertEquals(TestState.FAILED, suiteResult.getState());
     
     SCTMTestCaseResult test = suite.getChildTestByName("TableTest");
     SCTMTestResult caseResult = test.getTestResultForConfiguration(CONFIG_DUMMY);
     assertEquals(TestState.FAILED, caseResult.getState());
     
     test = suite.getChildTestByName("TableTest2");
     caseResult = test.getTestResultForConfiguration(CONFIG_DUMMY);
     assertEquals(TestState.PASSED, caseResult.getState());
   }
   
   @Test
   public void testFailedTestRun() throws Exception {   
     assertEquals(3, rootSuite.getFailCount());
     assertEquals(0, rootSuite.getSkipCount());
     assertEquals(2, rootSuite.getPassCount());
     assertEquals(62160.0, rootSuite.getDuration(), 0.1);
     
     SCTMTestSuiteResult suite = rootSuite.getChildSuiteByName("plan.pln").getChildSuiteByName("codeField.t");
     assertEquals(1, suite.getFailCount());
     assertEquals(0, suite.getSkipCount());
     assertEquals(0, suite.getPassCount());
     assertEquals(21000.0, suite.getDuration(), 0.1);
     
     SCTMTestCaseResult test = suite.getChildTestByName("codeField");
     assertEquals(1, test.getFailCount());
     assertEquals(0, test.getSkipCount());
     assertEquals(0, test.getPassCount());
     assertEquals(21000.0, test.getDuration(), 0.1);
     
     suite = rootSuite.getChildSuiteByName("plan.pln").getChildSuiteByName("table.t");
     assertEquals(1, suite.getFailCount());
     assertEquals(0, suite.getSkipCount());
     assertEquals(1, suite.getPassCount());
     assertEquals(24000.0, suite.getDuration(), 0.1);
 
     test = suite.getChildTestByName("TableTest");
     assertEquals(1, test.getFailCount());
     assertEquals(0, test.getSkipCount());
     assertEquals(0, test.getPassCount());
     assertEquals(22000.0, test.getDuration(), 0.1);
     
     test = suite.getChildTestByName("TableTest2");
     assertEquals(0, test.getFailCount());
     assertEquals(0, test.getSkipCount());
     assertEquals(1, test.getPassCount());
     assertEquals(2000.0, test.getDuration(), 0.1);
     
     suite = rootSuite.getChildSuiteByName("plan.pln").getChildSuiteByName("calculator.test.ParameterizedCalcTest");
     assertEquals(1, suite.getFailCount());
     assertEquals(0, suite.getSkipCount());
     assertEquals(0, suite.getPassCount());
     assertEquals(160.0, suite.getDuration(), 0.1);
     
     test = suite.getChildTestByName("testAdd[0]");
     assertEquals(1, test.getFailCount());
     assertEquals(0, test.getSkipCount());
     assertEquals(0, test.getPassCount());
     assertEquals(160.0, test.getDuration(), 0.1);
   }
   
   @Test @Ignore
   public void testFailingTestWithMissingMessage() throws Exception {
     SCTMTestCaseResult test = rootSuite.getChildSuiteByName("plan.pln").getChildSuiteByName("calculator.test.ParameterizedCalcTest").getChildTestByName("testAdd[0]");
     assertNotNull(test);
     SCTMTestResult configurationResult = test.getTestResultForConfiguration(CONFIG_DUMMY);
     assertNotNull(configurationResult);
     String errormsg = configurationResult.getErrorMessage();
   }
   
   @Test @Ignore
   public void testErrorMessage() throws Exception {
     SCTMTestCaseResult test = this.rootSuite.getChildSuiteByName("plan.pln").getChildSuiteByName("codeField.t").getChildTestByName("codeField");
     assertNotNull(test);
     SCTMTestResult configurationResult = test.getTestResultForConfiguration(CONFIG_DUMMY);
     assertNotNull(configurationResult);
     String errormsg = configurationResult.getErrorMessage();
     assertEquals("<b>Error: </b><br/>"+
         "Log Error: Could not find object '//Tree[@automationId='shellcont/shell']'"+
         "<b>Stacktrace: </b><br/>"+
         "Print: Occurred in Find&#xD;"+
         "Print: Called from CodeField at CodeField.t(6)"+
         "<b>Error: </b><br/>"+
         "Log Error: &lt;&lt;&lt;&lt; Click to View Bitmap of Error cmd /C\" start C:\\DOCUME~1\\ADMINI~1\\LOCALS~1\\Temp\\SCC_ExecServer_19124_19125\\PerfProjects\\PerfPrj_45_1270803017013\\CodeField105147.bmp\""+
         "<b>Error: </b><br/>"+
         "Print: Occurred in CaptureDesktop at ..\\common.inc(150)&#xD;"+
         "Print: Called from TestCaseExit at frame.inc(16)&#xD;"+
         "Print: Called from CodeField at CodeField.t(6)", errormsg);
   }
 
 }
