 package org.housered.jstestrunner.tests;
 
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.DomElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class TestSuiteResultBuilder {
 
     private String name;
     private int failures;
     private int millisTaken;    
     private final List<TestResult> testResults;    
 
     private TestSuiteResultBuilder(String name, int millisTaken) {
         testResults = new ArrayList<TestResult>();
         this.name = name;
         this.millisTaken = millisTaken;
     }
     
     public TestSuiteResultBuilder withTestCase(TestResult testCase) {
         this.testResults.add(testCase);
         
         if (!testCase.wasSuccess()) {
             failures += 1;
         }
         
         return this;
     }
     
     public TestSuiteResultBuilder withTestSuite(TestSuiteResult testSuiteResult) {
         this.testResults.add(testSuiteResult);
         
         failures += testSuiteResult.getFailures();
         
         return this;
     }
     
     public TestSuiteResultBuilder withTestSuite(TestSuiteResultBuilder testSuiteBuilder) {
         return this.withTestSuite(testSuiteBuilder.build());
     }
     
     public TestSuiteResult build() {
         return new TestSuiteResult(name, millisTaken, testResults.size(), failures, 0, 0, testResults);
     }
     
     public static HtmlPage asMockQUnitPage(TestSuiteResult testResult) {
         HtmlPage resultsPage = mock(HtmlPage.class);
        when(resultsPage.getWebClient()).thenReturn(mock(WebClient.class));
         
         List<DomElement> testResultElements = new ArrayList<DomElement>();
         
         for (TestResult rootResult : testResult.getTestResults()) {
             if (rootResult instanceof TestSuiteResult) {
                 for (TestResult caseResult : ((TestSuiteResult) rootResult).getTestResults()) {
                     if (caseResult instanceof TestSuiteResult) {
                         throw new IllegalArgumentException("TestResult contains nested suites, which is not a form that can be expressed in QUnit");
                     } else {
                         testResultElements.add(mockQUnitTestCaseElement(rootResult.getName(), caseResult));
                     }
                 }
             } else {
                 testResultElements.add(mockQUnitTestCaseElement(null, rootResult));
             }
         }
         
         DomElement resultsElement = mock(DomElement.class);        
         when(resultsPage.getFirstByXPath(contains("testresult"))).thenReturn(resultsElement);
         doReturn(testResultElements).when(resultsElement).getByXPath(contains("qunit-test-output"));
         
         DomElement titleElement = mock(DomElement.class);
         when(resultsPage.getFirstByXPath(contains("qunit-header"))).thenReturn(titleElement);
         when(titleElement.getTextContent()).thenReturn(testResult.getName());       
         
         int totalTests = testResult.getTotalTestCount();
         int failedTests = testResult.getFailures();
         int passedTests = totalTests - failedTests;
         
         when(resultsElement.getTextContent()).thenReturn(
                "Tests completed in " + testResult.getTestDurationMillis() + " milliseconds.\n" +
                 passedTests + " tests of " + totalTests + " passed, " + failedTests + " failed.");
         
         DomElement totalNode = mock(DomElement.class);
         when(resultsElement.getFirstByXPath(contains("total"))).thenReturn(totalNode);
         when(totalNode.getTextContent()).thenReturn(String.valueOf(totalTests));
 
         DomElement failedNode = mock(DomElement.class);
         when(resultsElement.getFirstByXPath(contains("failed"))).thenReturn(failedNode);
        when(failedNode.getTextContent()).thenReturn(String.valueOf(failedTests));
         
         return resultsPage;
     }
     
     private static DomElement mockQUnitTestCaseElement(String moduleName, TestResult result) {
         DomElement testCase = mock(DomElement.class);
         
         if (result.wasSuccess()) when(testCase.getAttribute("class")).thenReturn("pass");
         else when(testCase.getAttribute("class")).thenReturn("fail");
 
         if (moduleName != null) {
             DomElement testClassElement = mock(DomElement.class);
             when(testClassElement.getTextContent()).thenReturn(moduleName);
             when(testCase.getFirstByXPath(contains("module-name"))).thenReturn(testClassElement);
         }
 
         DomElement testNameElement = mock(DomElement.class);
         when(testNameElement.getTextContent()).thenReturn(result.getName());
         when(testCase.getFirstByXPath(contains("test-name"))).thenReturn(testNameElement);
 
         return testCase;
     }
 
     public static TestSuiteResultBuilder testSuiteResult(String name, int millisTaken) {
         return new TestSuiteResultBuilder(name, millisTaken);
     }    
     
 }
