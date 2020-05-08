 package org.housered.jstestrunner.testrunners;
 
 import static org.housered.jstestrunner.tests.TestSuiteResultBuilder.*;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.io.File;
 
 import org.housered.jstestrunner.tests.SimpleHtmlTestPage;
 import org.housered.jstestrunner.tests.TestPage;
 import org.housered.jstestrunner.tests.TestResult;
 import org.housered.jstestrunner.tests.TestSuiteResult;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class QUnitHtmlTestRunnerTest
 {
 
     private TestPage testPage;
    @Mock WebClient browser;
    @InjectMocks QUnitHtmlTestRunner testRunner;
 
     @Before
     public void setup() throws Exception
     {
         MockitoAnnotations.initMocks(this);
         this.testPage = new SimpleHtmlTestPage(new File("test-page-path"));
     }
 
     @Test
     public void shouldParseTestResults() throws Exception
     {
         TestSuiteResult expectedResult = testSuiteResult("Test Title", 54)
                                                 .withTestSuite(testSuiteResult("Test module", 0)
                                                                 .withTestCase(new TestResult("test", 0, true))
                                                                 .withTestCase(new TestResult("test2", 0, true)))
                                                 .withTestSuite(testSuiteResult("Another module", 0)
                                                                 .withTestCase(new TestResult("final-test", 0, true)))
                                                 .build();
         
         HtmlPage resultsPage = asMockQUnitPage(expectedResult);
         when(browser.getPage(testPage.getFileURL())).thenReturn(resultsPage);
 
         TestResult actualResult = testRunner.runTest(testPage);
 
         assertEquals(expectedResult, actualResult);
     }
     
     @Test
     public void shouldParseTestResultsWithoutModules() throws Exception {
         TestSuiteResult expectedResult = testSuiteResult("Test Title", 28)
                                             .withTestCase(new TestResult("test", 0, true))
                                             .withTestSuite(testSuiteResult("Test Module", 0)
                                                     .withTestCase(new TestResult("test2", 0, false)))
                                             .build();
         
         HtmlPage resultsPage = asMockQUnitPage(expectedResult);
         when(browser.getPage(testPage.getFileURL())).thenReturn(resultsPage);
 
         TestResult actualResult = testRunner.runTest(testPage);
 
         assertEquals(expectedResult, actualResult);
     }
     
 }
