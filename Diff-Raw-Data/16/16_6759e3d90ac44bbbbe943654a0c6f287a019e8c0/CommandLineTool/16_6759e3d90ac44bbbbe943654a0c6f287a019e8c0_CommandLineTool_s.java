 package org.housered.jstestrunner;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.housered.jstestrunner.output.TestResultOutputter;
 import org.housered.jstestrunner.output.TestResultOutputterFactory;
 import org.housered.jstestrunner.testrunners.TestRunner;
 import org.housered.jstestrunner.testrunners.TestRunnerFactory;
 import org.housered.jstestrunner.testrunners.UnableToRunTestException;
 import org.housered.jstestrunner.tests.TestPage;
 import org.housered.jstestrunner.tests.TestPageFactory;
 import org.housered.jstestrunner.tests.TestResult;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 
 @Component
 public class CommandLineTool
 {
 
     @Parameter(description = "List of test suite files to run")
     private List<String> tests = new ArrayList<String>();
 
     private final TestPageFactory testPageFactory;
     private final TestRunnerFactory testRunnerFactory;
     private final TestResultOutputterFactory testOutputterFactory;
     private final JCommander jCommander;
 
     @Autowired
     public CommandLineTool(JCommander jCommander, TestPageFactory testPageFactory, TestRunnerFactory testRunnerFactory, TestResultOutputterFactory testOutputterFactory)
     {
         jCommander.addObject(this);
         this.jCommander = jCommander;
         this.testPageFactory = testPageFactory;
         this.testRunnerFactory = testRunnerFactory;
         this.testOutputterFactory = testOutputterFactory;
     }
 
     public void run(String[] args)
     {
         jCommander.parse(args);
         
         if (tests.size() == 0) {
             System.out.println("No tests specified!");
             jCommander.usage();
             return;
         }
 
         for (String test : tests)
         {
            try 
             {
                 runTest(test);
             }
             catch (UnableToRunTestException e)
             {
                 System.err.println("Failed to run test " + test);
                 System.err.println(e.getMessage());
             }            
             catch (IOException e) {
                 System.err.println("Failed to record test results for test " + test);
                 System.err.println(e.getMessage());
             }
         }
 
         System.out.println("Done");
     }
 
     private void runTest(String testName) throws IOException, UnableToRunTestException
     {
        String outputFile = "output.xml";        
        
         TestPage test = testPageFactory.getTestPage(testName);
         
        TestRunner testRunner = testRunnerFactory.getRunnerForTestPage(test);
        
         TestResult result = testRunner.runTest(test);
         
         TestResultOutputter outputter = testOutputterFactory.getTestResultOutputter();
         
        FileOutputStream outputStream = new FileOutputStream(new File(outputFile));
         outputter.writeTestResultToFile(result, outputStream);
     }
 
 }
