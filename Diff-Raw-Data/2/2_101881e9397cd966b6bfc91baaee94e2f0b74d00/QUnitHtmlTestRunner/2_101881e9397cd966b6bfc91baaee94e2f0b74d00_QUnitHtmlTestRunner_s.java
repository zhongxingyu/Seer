 package org.housered.jstestrunner.testrunners;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.housered.jstestrunner.tests.TestPage;
 import org.housered.jstestrunner.tests.TestResult;
 import org.housered.jstestrunner.tests.TestResult.TestCaseResult;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.DomChangeEvent;
 import com.gargoylesoftware.htmlunit.html.DomChangeListener;
 import com.gargoylesoftware.htmlunit.html.DomElement;
 import com.gargoylesoftware.htmlunit.html.DomNode;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 @Component
 public class QUnitHtmlTestRunner implements TestRunner
 {
 
     private WebClient browser;
 
     private static final String RESULTS_SUMMARY_XPATH = "//*[@id=\"qunit-testresult\"]";
     private static final String TEST_CASE_XPATH = "//*[starts-with(@id, 'qunit-test-output')]";
     
     private static final String TOTAL_TEST_COUNT_XPATH = "//*[@class=\"total\"]";
     private static final String FAILED_TEST_COUNT_XPATH = "//*[@class=\"failed\"]";
     
     private static final String TEST_CASE_CLASS_XPATH = ".//*[@class=\"module-name\"]";
     private static final String TEST_CASE_NAME_XPATH = ".//*[@class=\"test-name\"]";
     private static final String TEST_CASE_FAILURE_MESSAGE_XPATH = ".//*[@class=\"test-message\"]";
     
     private static final Pattern TOTAL_TEST_TIME_REGEX = Pattern.compile("Tests completed in (\\d+) milliseconds");
 
     @Autowired
     public QUnitHtmlTestRunner(WebClient browser)
     {
         this.browser = browser;
     }
 
     public TestResult runTest(TestPage test) throws UnableToRunTestException
     {
         HtmlPage page;
 
         try
         {
             page = browser.getPage(test.getFilePath());
         }
         catch (Exception e)
         {
             throw new UnableToRunTestException(e);
         }
 
         waitUntilResultsAreReadyOn(page);
 
         return getTestResultFrom(page);
     }
 
     @SuppressWarnings("unchecked")
     private TestResult getTestResultFrom(HtmlPage resultsPage)
     {
         DomElement results = resultsPage.getFirstByXPath(RESULTS_SUMMARY_XPATH);
 
         int totalTests = getTotalTestsFromResultsNode(results);
         int failedTests = getFailedTestsFromResultsNode(results);
         int totalTime = getTotalTimeTakenFromResultsNode(results);
 
         List<TestCaseResult> testCaseResults = new ArrayList<TestCaseResult>();
 
         for (DomElement testCaseElement : (List<DomElement>) results.getByXPath(TEST_CASE_XPATH))
         {
             testCaseResults.add(getTestCaseResultsFromNode(testCaseElement));
         }
 
         return new TestResult(totalTests, failedTests, 0, 0, totalTime, "QUnit Test Suite", testCaseResults);
     }
 
     private int getTotalTestsFromResultsNode(DomElement resultsNode)
     {
         DomElement node = resultsNode.getFirstByXPath(TOTAL_TEST_COUNT_XPATH);
         return Integer.parseInt(node.getTextContent());
     }
 
     private int getFailedTestsFromResultsNode(DomElement resultsNode)
     {
         DomElement node = resultsNode.getFirstByXPath(FAILED_TEST_COUNT_XPATH);
         return Integer.parseInt(node.getTextContent());
     }
     
     private int getTotalTimeTakenFromResultsNode(DomElement resultsNode) {
         Matcher matcher = TOTAL_TEST_TIME_REGEX.matcher(resultsNode.getTextContent());
         
         if (matcher.find()) {
             return Integer.parseInt(matcher.group(1));
         } else {
             return 0;
         }
     }
 
     private TestCaseResult getTestCaseResultsFromNode(DomElement testCase)
     {
         boolean success = testCase.getAttribute("class").contains("pass");
         
         DomElement testClassElement = testCase.getFirstByXPath(TEST_CASE_CLASS_XPATH);
         String testClass = testClassElement.getTextContent();
         
         DomElement testNameElement = testCase.getFirstByXPath(TEST_CASE_NAME_XPATH);
         String testName = testNameElement.getTextContent();
         
         int testDurationMillis = 0; // Not available from QUnit
         
         return new TestCaseResult(testClass, testName, success, testDurationMillis);
     }
 
     private void waitUntilResultsAreReadyOn(HtmlPage resultsPage)
     {
         DomElement resultsNode = resultsPage.getFirstByXPath(RESULTS_SUMMARY_XPATH);
         ElementListenerAndNotifier resultsListener = new ElementListenerAndNotifier();
 
         synchronized (resultsListener)
         {
             resultsNode.addDomChangeListener(resultsListener);
 
             while (!resultsReady(resultsNode))
             {
                 try
                 {
                     resultsListener.wait();
                 }
                 catch (InterruptedException e)
                 {
                 }
             }
         }
     }
 
     private boolean resultsReady(DomNode resultsNode)
     {
         return resultsNode.getTextContent().contains("Tests completed");
     }
 
     private class ElementListenerAndNotifier implements DomChangeListener
     {
         private static final long serialVersionUID = 5876321513155410935L;
 
        public void nodeAdded(DomChangeEvent event)
         {
             this.notifyAll();
         }
 
         public void nodeDeleted(DomChangeEvent event)
         {
         }
 
     }
 
 }
