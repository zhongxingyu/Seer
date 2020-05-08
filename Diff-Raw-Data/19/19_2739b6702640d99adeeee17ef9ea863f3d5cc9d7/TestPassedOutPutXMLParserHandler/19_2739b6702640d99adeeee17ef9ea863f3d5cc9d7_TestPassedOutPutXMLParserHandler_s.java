 package hudson.plugins.sctmexecutor.publisher;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import hudson.plugins.sctmexecutor.publisher.handler.OutputXMLParserHandler;
 
 import java.io.File;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestPassedOutPutXMLParserHandler {
 
   private static final String TESTRESULTS_ROOTPATH = "src/test/resources/hudson/plugins/sctmexecutor/publisher/";
   private SCTMTestSuiteResult rootSuite;
 
   @Before
   public void setUp() throws Exception {
     SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
     rootSuite = new SCTMTestSuiteResult("root", null);
     OutputXMLParserHandler handler = new OutputXMLParserHandler(rootSuite , "config1");
     parser.parse(new File(TESTRESULTS_ROOTPATH+"passedTestResult/output.xml"), handler);
   }
   
   @Test
   public void testParsedStructure() throws Exception {
     assertEquals(3, this.rootSuite.getTotalCount());
     assertEquals(1, this.rootSuite.getChildren().size());
     
     SCTMTestSuiteResult suite = this.rootSuite.getChildSuiteByName("4Test.pln");
     assertNotNull(suite);
     assertEquals("4Test.pln", suite.getName());
     assertEquals(3, suite.getTotalCount());
     assertEquals(2, suite.getChildren().size());
     
     suite = suite.getChildSuiteByName("SubSuite.pln");
     assertNotNull(suite);
     assertEquals("SubSuite.pln", suite.getName());
     assertEquals(3, suite.getTotalCount());
     assertEquals(2, suite.getChildren().size());
     
     SCTMTestCaseResult test = suite.getChildTestByName("TestCase3"); 
     assertNotNull(test);
     assertEquals("TestCase3", test.getName());
     assertEquals(1, test.getTotalCount());
     
     suite = suite.getChildSuiteByName("TestSYSDirFunctions.t");
     assertNotNull(suite);
     assertEquals("TestSYSDirFunctions.t", suite.getName());
     assertEquals(2, suite.getTotalCount());
     assertEquals(2, suite.getChildren().size());
     
     test = suite.getChildTestByName("TestCase1"); 
     assertNotNull(test);
     assertEquals("TestCase1", test.getName());
     assertEquals(1, test.getTotalCount());
     
     test = suite.getChildTestByName("TestCase2"); 
     assertNotNull(test);
     assertEquals("TestCase2", test.getName());
     assertEquals(1, test.getTotalCount());
   }
   
   @Test
   public void testPassedTestRun() throws Exception {   
     assertEquals(0, rootSuite.getFailCount());
     assertEquals(0, rootSuite.getSkipCount());
     assertEquals(3, rootSuite.getPassCount());
     assertEquals(9000.0, rootSuite.getDuration(), 0.1);
     
     SCTMTestSuiteResult suite = rootSuite.getChildSuiteByName("4Test.pln").getChildSuiteByName("SubSuite.pln").getChildSuiteByName("TestSYSDirFunctions.t");
     assertEquals(0, suite.getFailCount());
     assertEquals(0, suite.getSkipCount());
     assertEquals(2, suite.getPassCount());
     assertEquals(6000.0, suite.getDuration(), 0.1);
     
     SCTMTestCaseResult test = suite.getChildTestByName("TestCase1");
     assertEquals(0, test.getFailCount());
     assertEquals(0, test.getSkipCount());
     assertEquals(1, test.getPassCount());
     assertEquals(3000.0, test.getDuration(), 0.1);
   }
   
   @Test
   public void testNormalizedNUnitSuiteName() throws Exception {
     SCTMTestSuiteResult suite = rootSuite.getChildSuiteByName("4Test.pln").getChildSuiteByName("SilkTest.Ntf.Test.Awt");
     assertNotNull(suite);
     assertEquals("SilkTest.Ntf.Test.Awt", suite.getName());
     assertEquals(0, suite.getTotalCount());
     assertEquals(0, suite.getChildren().size());
   }
 
 }
