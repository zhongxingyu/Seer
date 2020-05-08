 package de.is24.util.karmatestrunner;
 
 
 import org.json.simple.parser.ContainerFactory;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.junit.runner.Description;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import java.util.*;
 
 /**
  * The TestReporter is receives the test results and other infos from the
  * {@link de.is24.util.karmatestrunner.jetty.ResultReceiverServer} and transforms
  * them into JUnit results (via {@link RunNotifier}).
  */
 public class TestReporter {
 
     private HashMap<String, String> browsers = new HashMap<>();
     private JSONParser parser = new JSONParser();
     private RunNotifier notifier;
     private Class<?> testClass;
     private Description suiteDescription;
 
     /**
      * @param notifier JUnit notifier for transformed test results
      */
     TestReporter(RunNotifier notifier) {
         this.notifier = notifier;
     }
 
 
     /**
      * Receives the test results and transforms them into JUnit events.
      *
      * @param data test results as JSON string
      */
     public void handleMessage(String data) {
         try {
             Map message = (Map) parser.parse(data, containerFactory);
             String message_type = (String) message.get("type");
             if (message_type.equalsIgnoreCase("test")) {
                 reportTestResult(message);
             } else if (message_type.equalsIgnoreCase("browsers")) {
                 setBrowsers(message);
             } else if (message_type.equalsIgnoreCase("runComplete")) {
                 finishReportingForTestSuite();
            } else if (message_type.equalsIgnoreCase("browserError")) {
                reportBrowserError(message);
             } // other messages will be ignored
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public void setTestClass(Class<?> testClass) {
         this.testClass = testClass;
     }
 
     private void finishReportingForTestSuite() {
         notifier.fireTestFinished(suiteDescription);
     }
 
     private ContainerFactory containerFactory = new ContainerFactory() {
         public List creatArrayContainer() {
             return new LinkedList();
         }
 
         public Map createObjectContainer() {
             return new LinkedHashMap();
         }
 
     };
 
     private void setBrowsers(Map browsers) {
         for (int i = 0; i < ((List) browsers.get("list")).size(); i++) {
             Map browser = (Map) ((List) browsers.get("list")).get(i);
 
             this.browsers.put(
                     (String) browser.get("browserId"),
                     (String) browser.get("name")
             );
 
             suiteDescription = Description.createSuiteDescription(testClass);
            notifier.fireTestStarted(suiteDescription);
         }
     }
 
    private void reportBrowserError(Map message){
        String browserId = (String) message.get("browserId");

        Description description = describeChild(testClass,browsers.get(browserId)+ ": Browser Error" );
        String errorMsg = message.get("error").toString();
        JSTestFailure failure = new JSTestFailure(description, "Error",
                "Failures: " + errorMsg);

        notifier.fireTestStarted(description);
        notifier.fireTestFailure(failure);
        notifier.fireTestFinished(description);
    }

     private void reportTestResult(Map message) {
         String browserId = (String) message.get("browserId");
         Map result = (Map) message.get("result");
         Long time = (Long) result.get("time");
         Boolean skipped = (Boolean) result.get("skipped");
         String label = (String) result.get("description");
         Boolean success = (Boolean) result.get("success");
         String suite = result.get("suite").toString();
 
 
         Description description = describeChild(testClass,browsers.get(browserId) + ": " + suite + " " + label);
 
         try {
             if (skipped) {
                 notifier.fireTestIgnored(description);
             } else {
                 notifier.fireTestStarted(description);
             }
 
             if (!success) {
 
                 String log = result.get("log").toString();
                 JSTestFailure failure = new JSTestFailure(description, label,
                         "Failures: " + log);
                 notifier.fireTestFailure(failure);
             }
 
         } finally {
             notifier.fireTestFinished(description);
 
         }
 
     }
 
     private Description describeChild(Class browser, String description) {
 
         return Description
                 .createTestDescription(browser, description);
     }
 
     /**
      * A JavaScript execution failure object.
      */
     private static class JSTestFailure extends Failure {
         private final String name;
 
         public JSTestFailure(Description description, String name, String message) {
             super(description, new RuntimeException(message));
             this.name = name;
         }
 
         @Override
         public String getTestHeader() {
             return name;
         }
 
         @Override
         public String getTrace() {
             // The stack means nothing here.
             return getMessage();
         }
 
         @Override
         public String toString() {
             return getTestHeader();
         }
 
     }
 
 
 }
