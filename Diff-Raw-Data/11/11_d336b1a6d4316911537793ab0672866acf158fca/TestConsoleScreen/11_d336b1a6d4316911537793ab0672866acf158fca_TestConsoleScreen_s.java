 /*
  * TestConsoleScreen.java
  *
  * Created on February 2, 2007, 10:11 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package j2meunit.rimui;
 
 import j2meunit.framework.AssertionFailedError;
 import j2meunit.framework.Test;
 import j2meunit.framework.TestCase;
 import j2meunit.framework.TestFailure;
 import j2meunit.framework.TestListener;
 import j2meunit.framework.TestResult;
 import j2meunit.framework.TestSuite;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import net.rim.device.api.system.Characters;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.FontFamily;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.GaugeField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.TreeField;
 import net.rim.device.api.ui.component.TreeFieldCallback;
 import net.rim.device.api.ui.container.MainScreen;
 
 /**
  *
  * @author Derek Konigsberg
  */
 public class TestConsoleScreen extends MainScreen implements TreeFieldCallback, TestListener {
     private LabelField statusLabel;
     private LabelField failureLabel;
     private LabelField errorLabel;
     private LabelField timeLabel;
     private GaugeField progressGauge;
     private TreeField testTreeField;
 
     private TestSuite mainTestSuite;
     private Hashtable testTreeItems;
     private TestResult testResults;
     private Test selectedTest;
     private long elapsedTime;
     
     private static class TestTreeItem {
         public TestTreeItem(Test test) {
             this.test = test;
             if(test instanceof TestCase)
                 name = ((TestCase)test).getName();
             else if(test instanceof TestSuite)
                 name = ((TestSuite)test).toString();
             else
                 name = "Test";
             this.hasRun = false;
             this.hasPassed = true;
             this.id = 0;
         }
         public Test test;
         public String name;
         public boolean hasRun;
         public boolean hasPassed;
         public int id;
     }
     
     /**
      * Creates a new instance of TestConsoleScreen
      */
     public TestConsoleScreen(String[] testCaseClasses) {
         initializeFields();
         testTreeItems = new Hashtable();
         mainTestSuite = createTestSuite(testCaseClasses);
         elapsedTime = -1;
         populateTestTree(mainTestSuite, 0);
     }
     
     private void initializeFields() {
         try {
             FontFamily theFam = FontFamily.forName("SYSTEM");
             Font theFont = theFam.getFont(Font.PLAIN, 12);
             Font.setDefaultFont(theFont);
         } catch(Exception e) { }
         add(new LabelField("J2MEUnit " + j2meunit.util.Version.id()));
         statusLabel = new LabelField("Status: Idle");
         add(statusLabel);
         failureLabel = new LabelField("Failures: 0");
         add(failureLabel);
         errorLabel = new LabelField("Errors: 0");
         add(errorLabel);
         timeLabel = new LabelField("Time: n/a");
         add(timeLabel);
         progressGauge = new GaugeField(null, 0, 100, 0, GaugeField.NO_TEXT);
         add(progressGauge);
         add(new SeparatorField());
         
         testTreeField = new TreeField(this, Field.FOCUSABLE);
         testTreeField.setEmptyString("No tests", 0);
         testTreeField.setDefaultExpanded(true);
         testTreeField.setIndentWidth(20);
         add(testTreeField);
     }
     
     private void populateTestTree(TestSuite suite, int parentId) {
         for(Enumeration e = suite.tests(); e.hasMoreElements(); ) {
            Test test = (Test)e.nextElement();
             TestTreeItem item = new TestTreeItem(test);
             int id = testTreeField.addChildNode(parentId, item);
             item.id = id;
             testTreeItems.put(test, item);
             if(test instanceof TestSuite) {
                 populateTestTree((TestSuite)test, id);
             }
         }
     }
     
     /** Standard Escape-key handler */
     public boolean keyChar(char key, int status, int time) {
         boolean retval = false;
         switch (key) {
             case Characters.ESCAPE:
                 System.exit(1);
                 break;
             default:
                 retval = super.keyChar(key, status, time);
         }
         return retval;
     }
     
     public void drawTreeItem(TreeField treeField, Graphics graphics, int node, int y, int width, int indent) {
         Object cookie = testTreeField.getCookie(node);
         
         if(cookie instanceof TestTreeItem) {
             TestTreeItem item = (TestTreeItem)cookie;
             int height = testTreeField.getRowHeight();
             
             int oldColor = graphics.getColor();
             
             if(item.hasRun) {
                 if(item.hasPassed)
                     graphics.setColor(0x0000FF00); // green
                 else
                     graphics.setColor(0x00FF0000); // red
             } else
                 graphics.setColor(0x00C0C0C0); // grey
             
             graphics.fillArc(indent, y+1, height-2, height-2, 0, 360);
             graphics.setColor(oldColor);
             
             graphics.drawText(item.name, indent + height, y, Graphics.ELLIPSIS, width);
         }
     }
     
     private MenuItem resultsItem = new MenuItem("View results", 100000, 10) {
         public void run() {
             showTestResults();
         }
     };
     private MenuItem runAllItem = new MenuItem("Run all", 100010, 10) {
         public void run() {
             runAllTests();
         }
     };
     private MenuItem runSelectedItem = new MenuItem("Run selected", 100020, 10) {
         public void run() {
             runSelectedTests();
         }
     };
     private MenuItem exitItem = new MenuItem("Exit", 200001, 10) {
         public void run() {
             System.exit(0);
         }
     };
 
     protected void makeMenu(Menu menu, int instance) {
         int node = testTreeField.getCurrentNode();
         if(node > 0) {
             TestTreeItem item = (TestTreeItem)testTreeField.getCookie(node);
             if(item.hasRun && item.test instanceof TestCase) {
                 menu.addSeparator();
                 menu.add(resultsItem);
             }
         }
         menu.addSeparator();
         menu.add(runAllItem);
         menu.add(runSelectedItem);
         menu.addSeparator();
         menu.add(exitItem);
     }
 
     public synchronized void addError(Test test, Throwable throwable) {
         UiApplication.getUiApplication().invokeLater(new Runnable() {
             public void run() {
                 errorLabel.setText("Errors: " + testResults.errorCount());
             }
         });
     }
     
     public synchronized void addFailure(Test test, AssertionFailedError assertionFailedError) {
         UiApplication.getUiApplication().invokeLater(new Runnable() {
             public void run() {
                 failureLabel.setText("Failures: " + testResults.failureCount());
             }
         });
     }
     
     public void endTest(Test test) {
         UiApplication.getUiApplication().invokeLater(new Runnable() {
             public void run() {
                 progressGauge.setValue(progressGauge.getValue() + 1);
             }
         });
         updateTreeErrors();
         updateTestBranch(test);
         updateTestTree();
     }
     
     public void endTestStep(Test test) {
         UiApplication.getUiApplication().invokeLater(new Runnable() {
             public void run() {
                 progressGauge.setValue(progressGauge.getValue() + 1);
             }
         });
         updateTreeErrors();
         updateTestBranch(test);
         updateTestTree();
     }
     
     public synchronized void startTest(Test test) {
         TestTreeItem item = (TestTreeItem)testTreeItems.get(test);
         item.hasRun = true;
     }
 
     private void updateTestBranch(Test test) {
         TestTreeItem item = (TestTreeItem)testTreeItems.get(test);
         if(item == null) return;
         boolean hasAllRun = true;
         boolean hasAllPassed = true;
         int parentId = testTreeField.getParent(item.id);
         if(parentId <= 0) return;
         int node = testTreeField.getFirstChild(parentId);
         if(node == -1) return;
         while(node != -1) {
             item = (TestTreeItem)testTreeField.getCookie(node);
             if(!item.hasRun) hasAllRun = false;
             if(!item.hasPassed) hasAllPassed = false;
             node = testTreeField.getNextSibling(node);
         }
         item = (TestTreeItem)testTreeField.getCookie(parentId);
         item.hasRun = hasAllRun;
         item.hasPassed = hasAllPassed;
         
         if(testTreeField.getParent(item.id) >= 0) {
             updateTestBranch(item.test);
         }
     }
     
     private void updateTreeErrors() {
         // Update the test data structures
         TestTreeItem item;
         Enumeration e;
         TestFailure failure;
         for(e = testResults.failures(); e.hasMoreElements(); ) {
             failure = (TestFailure)e.nextElement();
             item = (TestTreeItem)testTreeItems.get(failure.failedTest());
             item.hasPassed = false;
         }
         for(e = testResults.errors(); e.hasMoreElements(); ) {
             failure = (TestFailure)e.nextElement();
             item = (TestTreeItem)testTreeItems.get(failure.failedTest());
             item.hasPassed = false;
         }
     }
     
     private synchronized void updateTestTree() {
         // Repaint the tree
         UiApplication.getUiApplication().invokeLater(new Runnable() {
             public void run() {
                 testTreeField.setDirty(true);
                 invalidate();
                 doPaint();
             }
         });        
     }
 
     private void showTestResults() {
         int node = testTreeField.getCurrentNode();
         if(node <= 0) return;
         TestTreeItem item = (TestTreeItem)testTreeField.getCookie(node);
         if(!(item.test instanceof TestCase)) return;
         TestCase testCase = (TestCase)item.test;
         
         Enumeration e;
         TestFailure testFailure = null;
         TestFailure testError = null;
         
         for(e = testResults.failures(); e.hasMoreElements(); ) {
             TestFailure failure = (TestFailure)e.nextElement();
             if(failure.failedTest() == testCase) {
                 testFailure = failure;
                 break;
             }
         }
         
         for(e = testResults.errors(); e.hasMoreElements(); ) {
             TestFailure error = (TestFailure)e.nextElement();
             if(error.failedTest() == testCase) {
                 testError = error;
                 break;
             }
         }
         
         UiApplication.getUiApplication().pushModalScreen(
                 new TestResultsScreen(testCase, testFailure, testError));
     }
     
     /**
      * Builds a test suite from all test case classes in a string array.
      *
      * @param  testCaseClasses A string array containing the test case class names
      * @return A test suite containing all tests
      */
     protected TestSuite createTestSuite(String[] testCaseClasses) {
         TestSuite testSuite = new TestSuite();
         if (testCaseClasses.length < 1) {
             return testSuite;
         }
         
         for (int i = 0; i < testCaseClasses.length; i++) {
             try {
                 String className = testCaseClasses[i];
                 TestCase testCase =
                     (TestCase)Class.forName(className).newInstance();
                 testSuite.addTest(testCase.suite());
             } catch (Exception e) {
                 System.out.println("Access to TestCase " + testCaseClasses[i] +
                     " failed: " + e.getMessage() + " - " +
                     e.getClass().getName());
             }
         }
         
         return testSuite;
     }
     
     /**
      * Run all tests in the given test suite.
      *
      * @param suite The test suite to run
      */
     protected void doRun(Test suite) {
         testResults = new TestResult();
         testResults.addListener(this);
         
         long startTime = System.currentTimeMillis();
 
         // Mark the suite's tree item
         TestTreeItem item = (TestTreeItem)testTreeItems.get(suite);
         if(item != null)
             item.hasRun = true;
 
         // Run the suite
         suite.run(testResults);
         
         long endTime = System.currentTimeMillis();
         elapsedTime = endTime - startTime;
     }
 
     private void runAllTests() {
         selectedTest = mainTestSuite;
         runTests();
     }
     
     private void runSelectedTests() {
         int nodeId = testTreeField.getCurrentNode();
         if(nodeId == -1) return;
         selectedTest = ((TestTreeItem)testTreeField.getCookie(nodeId)).test;
         runTests();
     }
 
     private void runTests() {
         statusLabel.setText("Status: Running...");
         progressGauge.reset(null, 0, selectedTest.countTestCases(), 0);
         new Thread() {
             public void run() {
                 try {
                     doRun(selectedTest);
                     updateResults();
                 } catch (Throwable e) {
                     System.out.println("Exception while running test: " + e);
                     e.printStackTrace();
                 }
             }
         }.start();
     }
     
     private synchronized void updateResults() {
         UiApplication.getUiApplication().invokeLater(new Runnable() {
             public void run() {
                 statusLabel.setText("Status: Idle");
                 if(elapsedTime >= 0) {
                     timeLabel.setText("Time: " + elapsedTime + "ms");
                 }
             }
         });
     }
 }
