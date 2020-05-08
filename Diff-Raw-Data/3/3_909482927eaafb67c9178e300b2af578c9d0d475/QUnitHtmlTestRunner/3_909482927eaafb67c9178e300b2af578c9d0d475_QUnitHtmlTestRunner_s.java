 package org.housered.jstestrunner.testrunners;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.housered.jstestrunner.tests.TestPage;
 import org.housered.jstestrunner.tests.TestResult;
 import org.housered.jstestrunner.tests.TestSuiteResult;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.gargoylesoftware.htmlunit.IncorrectnessListener;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.DomChangeEvent;
 import com.gargoylesoftware.htmlunit.html.DomChangeListener;
 import com.gargoylesoftware.htmlunit.html.DomElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 @Component
 public class QUnitHtmlTestRunner implements TestRunner {
 
     private WebClient browser;
 
     private static final String RESULTS_SUMMARY_XPATH = "//*[@id=\"qunit-testresult\"]";
     private static final String TEST_CASE_XPATH = "//*[starts-with(@id, 'qunit-test-output')]";
     
     private static final String TEST_SUITE_NAME = "//*[@id=\"qunit-header\"]"; 
     
     private static final String TOTAL_TEST_COUNT_XPATH = "//*[@class=\"total\"]";
     private static final String FAILED_TEST_COUNT_XPATH = "//*[@class=\"failed\"]";
     
     private static final String TEST_CASE_CLASS_XPATH = ".//*[@class=\"module-name\"]";
     private static final String TEST_CASE_NAME_XPATH = ".//*[@class=\"test-name\"]";
     private static final String TEST_CASE_FAILURE_MESSAGE_XPATH = ".//*[@class=\"test-message\"]";
     
     private static final Pattern TOTAL_TEST_TIME_REGEX = Pattern.compile("Tests completed in (\\d+) milliseconds");
 
     @Autowired
     public QUnitHtmlTestRunner(WebClient browser) {
         this.browser = browser;
         browser.setIncorrectnessListener(new IncorrectnessListener() {
             public void notify(String message, Object origin) {
             }
         });
     }
 
     public TestSuiteResult runTest(TestPage test) throws UnableToRunTestException {
         HtmlPage page;
 
         try {
             page = browser.getPage(test.getFileURL());
         } catch (Exception e) {
             throw new UnableToRunTestException(e);
         }
 
         waitUntilResultsAreReadyOn(page);
         return getTestResultFrom(page);
     }
 
     @SuppressWarnings("unchecked")
     private TestSuiteResult getTestResultFrom(HtmlPage resultsPage) {
         String testName = getTestNameFromTestPage(resultsPage);    	
     	
         DomElement results = resultsPage.getFirstByXPath(RESULTS_SUMMARY_XPATH);
 
         int totalTests = getTotalTestsFromResultsNode(results);
         int failedTests = getFailedTestsFromResultsNode(results);
         int totalTime = getTotalTimeTakenFromResultsNode(results);
 
         List<TestResult> modules = getModulesFromTestCaseElements((List<DomElement>) results.getByXPath(TEST_CASE_XPATH));
 
         return new TestSuiteResult(testName, totalTime, totalTests, failedTests, 0, 0, modules);
     }
     
     private List<TestResult> getModulesFromTestCaseElements(List<DomElement> testCaseElements) {        
         Map<String, List<TestResult>> resultsBySuiteName = new LinkedHashMap<String, List<TestResult>>();        
 
         for (DomElement testCaseElement : testCaseElements)
         {
             String moduleName = getTestModuleFromTestCaseNode(testCaseElement);
             if (!resultsBySuiteName.containsKey(moduleName)) {
                 resultsBySuiteName.put(moduleName, new ArrayList<TestResult>());
             }
             resultsBySuiteName.get(moduleName).add(getTestCaseResultsFromTestCaseNode(testCaseElement));
         }
         
         List<TestResult> modules = new ArrayList<TestResult>();
         
         for (String moduleName : resultsBySuiteName.keySet()) {
             if (moduleName == null) {
                 modules.addAll(resultsBySuiteName.get(moduleName));
             } else {
                 modules.add(TestSuiteResult.newSuiteFromResultsList(moduleName, resultsBySuiteName.get(moduleName)));
             }
         }
         
         return modules;
     }
 
     private String getTestNameFromTestPage(HtmlPage resultsPage) {
 		DomElement titleElement = resultsPage.getFirstByXPath(TEST_SUITE_NAME);		
 		return titleElement.getTextContent();
 	}
 
 	private int getTotalTestsFromResultsNode(DomElement resultsNode) {
         DomElement node = resultsNode.getFirstByXPath(TOTAL_TEST_COUNT_XPATH);
         return Integer.parseInt(node.getTextContent());
     }
 
     private int getFailedTestsFromResultsNode(DomElement resultsNode) {
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
     
     private String getTestModuleFromTestCaseNode(DomElement testCase) {
         DomElement testClassElement = testCase.getFirstByXPath(TEST_CASE_CLASS_XPATH);
         String testClass = null;
         if (testClassElement != null) {
             testClass = testClassElement.getTextContent();
         }
         return testClass;
     }
 
     private TestResult getTestCaseResultsFromTestCaseNode(DomElement testCase) {
         boolean success = testCase.getAttribute("class").contains("pass");
         
         DomElement testNameElement = testCase.getFirstByXPath(TEST_CASE_NAME_XPATH);
         String testName = testNameElement.getTextContent();
         
         int testDurationMillis = 0; // Not available from QUnit
         
         return new TestResult(testName, testDurationMillis, success);
     }
 
     private void waitUntilResultsAreReadyOn(HtmlPage resultsPage) {
         DomElement resultsNode = resultsPage.getFirstByXPath(RESULTS_SUMMARY_XPATH);
         ElementListenerAndNotifier resultsListener = new ElementListenerAndNotifier();
 
         synchronized (resultsListener) {
             resultsNode.addDomChangeListener(resultsListener);
 
             while (!resultsReady(resultsPage)) {
                 try {
                     resultsListener.wait(1000);
                 } catch (InterruptedException e) {
                 }
             }
         }
     }
 
     private boolean resultsReady(HtmlPage resultsPage) {
         DomElement resultsNode = resultsPage.getFirstByXPath(RESULTS_SUMMARY_XPATH);
         
         boolean qunitThinksItsDone = resultsNode.getTextContent().contains("Tests completed");
         boolean noBackgroundTasks = resultsPage.getWebClient().waitForBackgroundJavaScript(0) == 0;
         
         return qunitThinksItsDone && noBackgroundTasks;
     }
 
     private class ElementListenerAndNotifier implements DomChangeListener {
         private static final long serialVersionUID = 5876321513155410935L;
 
         public synchronized void nodeAdded(DomChangeEvent event) {
             this.notifyAll();
         }
 
         public void nodeDeleted(DomChangeEvent event) {
         }
     }
 
 }
