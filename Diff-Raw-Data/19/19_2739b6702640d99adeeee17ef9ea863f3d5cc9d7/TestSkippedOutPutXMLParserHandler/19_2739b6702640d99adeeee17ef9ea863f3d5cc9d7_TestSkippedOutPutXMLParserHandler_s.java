 package hudson.plugins.sctmexecutor.publisher;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import hudson.plugins.sctmexecutor.publisher.SCTMTestResult.TestState;
 import hudson.plugins.sctmexecutor.publisher.handler.OutputXMLParserHandler;
 
 import java.io.File;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestSkippedOutPutXMLParserHandler {
 
   private static final String CONFIG_DUMMY = "config1";
   private static final String TESTRESULTS_ROOTPATH = "src/test/resources/hudson/plugins/sctmexecutor/publisher/";
   private SCTMTestSuiteResult rootSuite;
 
   @Before
   public void setUp() throws Exception {
     SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
     rootSuite = new SCTMTestSuiteResult("root", null);
     OutputXMLParserHandler handler = new OutputXMLParserHandler(rootSuite , CONFIG_DUMMY);
     parser.parse(new File(TESTRESULTS_ROOTPATH+"skippedTestResult/output.xml"), handler);
   }
   
   @Test
   public void testParsedStructure() throws Exception {
     assertEquals(2, this.rootSuite.getTotalCount());
     assertEquals(1, this.rootSuite.getChildren().size());
     
     SCTMTestSuiteResult testDef = this.rootSuite.getChildSuiteByName("Test");
     assertNotNull(testDef);
     assertEquals("Test", testDef.getName());
     assertEquals(2, testDef.getTotalCount());
     assertEquals(2, testDef.getTestResultForConfiguration(CONFIG_DUMMY).getTotalCount());
     assertEquals(1, testDef.getChildren().size());
     
     SCTMTestSuiteResult suite = testDef.getChildSuiteByName("RepeatButtonTest");
     assertNotNull(suite);
     assertEquals("RepeatButtonTest", suite.getName());
     assertEquals(2, suite.getTotalCount());
     assertEquals(2, suite.getTestResultForConfiguration(CONFIG_DUMMY).getTotalCount());
     assertEquals(2, suite.getChildren().size());
     
     SCTMTestCaseResult test = suite.getChildTestByName("RepeatButtonTest.CustomAttributesFindTest"); 
     assertNotNull(test);
     assertEquals("RepeatButtonTest.CustomAttributesFindTest", test.getName());
     assertEquals(1, test.getTotalCount());
     assertEquals(1, test.getTestResultForConfiguration(CONFIG_DUMMY).getTotalCount());
     
     test = suite.getChildTestByName("RepeatButtonTest.RepeatButtonTest"); 
     assertNotNull(test);
     assertEquals("RepeatButtonTest.RepeatButtonTest", test.getName());
     assertEquals(1, test.getTotalCount());
     assertEquals(1, test.getTestResultForConfiguration(CONFIG_DUMMY).getTotalCount());
   }
   
   @Test
   public void testSkippedTestRun() throws Exception {
     SCTMTestSuiteResult suite = this.rootSuite.getChildSuiteByName("Test").getChildSuiteByName("RepeatButtonTest");
     assertEquals(0, suite.getFailCount());
     assertEquals(2, suite.getSkipCount());
     assertEquals(0, suite.getPassCount());
     assertEquals(0.0, suite.getDuration(), 0.1);
     
     SCTMTestCaseResult test = suite.getChildTestByName("RepeatButtonTest.CustomAttributesFindTest"); 
     assertEquals(0, test.getFailCount());
     assertEquals(1, test.getSkipCount());
     assertEquals(0, test.getPassCount());
     assertEquals(0.0, test.getDuration(), 0.1);
     
     test = suite.getChildTestByName("RepeatButtonTest.RepeatButtonTest"); 
     assertEquals(0, test.getFailCount());
     assertEquals(1, test.getSkipCount());
     assertEquals(0, test.getPassCount());
     assertEquals(0.0, test.getDuration(), 0.1);
   }
   
   @Test
   public void testTestResult() throws Exception {
     SCTMTestSuiteResult suite = this.rootSuite.getChildSuiteByName("Test").getChildSuiteByName("RepeatButtonTest");
     SCTMTestResult suiteResult = suite.getTestResultForConfiguration(CONFIG_DUMMY);
     assertEquals(TestState.SKIPPED, suiteResult.getState());
     
     SCTMTestCaseResult test = suite.getChildTestByName("RepeatButtonTest.CustomAttributesFindTest"); 
     SCTMTestResult caseResult = test.getTestResultForConfiguration(CONFIG_DUMMY);
     assertEquals(TestState.SKIPPED, caseResult.getState());
     
     test = suite.getChildTestByName("RepeatButtonTest.RepeatButtonTest"); 
     caseResult = test.getTestResultForConfiguration(CONFIG_DUMMY);
     assertEquals(TestState.SKIPPED, caseResult.getState());
   }
 }
