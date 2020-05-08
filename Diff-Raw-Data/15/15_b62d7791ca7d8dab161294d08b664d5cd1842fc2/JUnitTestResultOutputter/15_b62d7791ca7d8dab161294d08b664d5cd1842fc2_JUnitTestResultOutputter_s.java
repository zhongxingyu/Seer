 package org.housered.jstestrunner.output;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.housered.jstestrunner.tests.TestResult;
 import org.housered.jstestrunner.tests.TestResult.TestCaseResult;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 public class JUnitTestResultOutputter implements TestResultOutputter
 {
 
     public void writeTestResultToFile(TestResult result, OutputStream outputStream) throws IOException
     {
         Element testSuiteRoot = new Element("testsuite");
         Document testResultDoc = new Document(testSuiteRoot);
         
         testSuiteRoot.setAttribute("name", result.getName());
         testSuiteRoot.setAttribute("tests", String.valueOf(result.getTotalTestCount()));
         testSuiteRoot.setAttribute("errors", String.valueOf(result.getErrors()));
         testSuiteRoot.setAttribute("failures", String.valueOf(result.getFailures()));
         testSuiteRoot.setAttribute("skip", String.valueOf(result.getSkipped()));
         
         double totalTime = result.getTotalTime() / 1000d;
        if (totalTime != -1) {
             testSuiteRoot.setAttribute("time", String.valueOf(totalTime));
         }
         
         for (TestCaseResult testCaseResult : result.getTestResults()) {
             Element testCase = new Element("testcase");
             testCase.setAttribute("classname", testCaseResult.getTestClass());
             testCase.setAttribute("name", testCaseResult.getTestName());
             
             double testTime = testCaseResult.getTestDurationMillis() / 1000d;
             
            if (testTime != -1) {
                 testCase.setAttribute("time", String.valueOf(testCaseResult.getTestDurationMillis() / 1000f));
             }
             
             if (!testCaseResult.wasSuccess()) {
                 // TODO make status an enum
                 testCase.addContent(new Element("failure"));
             }
             
             // TODO support individual test case results + messages
             testSuiteRoot.addContent(testCase);
         }
 
         XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
 
         xmlOutputter.output(testResultDoc, outputStream);
     }
 
 }
