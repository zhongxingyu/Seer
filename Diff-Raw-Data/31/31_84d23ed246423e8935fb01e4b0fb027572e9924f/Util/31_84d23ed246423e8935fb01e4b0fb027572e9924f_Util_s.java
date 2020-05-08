 /*
  * Util.java
  *
  * Created on July 10, 2007, 12:27 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package org.netbeans.test.javafx.bestpractices.lib;
 
 import java.awt.Container;
 import org.netbeans.jellytools.MainWindowOperator;
 import org.netbeans.jellytools.NewProjectWizardOperator;
 import org.netbeans.jellytools.OutputOperator;
 import org.netbeans.jellytools.ProjectsTabOperator;
 import org.netbeans.jellytools.actions.SaveAllAction;
 import org.netbeans.jellytools.nodes.Node;
 import org.netbeans.jemmy.QueueTool;
 import org.netbeans.jemmy.TimeoutExpiredException;
 import org.netbeans.jemmy.operators.JPopupMenuOperator;
 import org.netbeans.jemmy.operators.JProgressBarOperator;
 import org.netbeans.jemmy.util.Dumper;
 import org.netbeans.jemmy.util.PNGEncoder;
 
 /**
  *
  * @author Lark Fitzgerald
  */
 public class Util {
 
     public static String WORKDIR = System.getProperty("xtest.workdir");
     public static String FILE_SEPARATOR = System.getProperty("file.separator");
     protected static final String _close = "Close";
     protected static final String _compile = "Compile";
     private static final int WAIT_TIME = 2000;
     public static final long MAX_WAIT_TIME = 300000;
 
     /** Performs New Project > Samples > JavaFX > Best Practices > ...*/
     public static Boolean createSampleProject(String path, String type, String projName) {
         NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
         projectWizard.selectCategory(path + type);
         projectWizard.selectProject(projName);
         projectWizard.next();
         projectWizard.finish();
         new QueueTool().waitEmpty();
         waitScanFinished();
 
         //Verifies that project exists
         try {
             ProjectsTabOperator pto = new ProjectsTabOperator();
             new Node(pto.invoke().tree(), projName);
             new QueueTool().waitEmpty();
         } catch (Exception e) {
             return false;
         }
         return true;
     }
 
     /** Compile single file using treePath */
     public static Boolean compileProjectFile(String path) {
         ProjectsTabOperator pto = new ProjectsTabOperator();
         Node projectNode = new Node(pto.invoke().tree(), path);
         new QueueTool().waitEmpty();
         sleep();
         JPopupMenuOperator item = projectNode.callPopup();
         item.pushMenuNoBlock(_compile);
         new QueueTool().waitEmpty();
         sleep();
 
         //Verify compilation
         try {
             OutputOperator oo = new OutputOperator();
             new QueueTool().waitEmpty();
             String output = oo.getText();
            CharSequence cs = new String("BUILD SUCCESS");
            if (!output.contains(cs)) {
                 return false;
             }
             return true;
         } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
            return false; //output window not found
         }
     }
 
     /** Performs Save and Close of project */
     public static Boolean closeProject(String name) {
         new SaveAllAction().performAPI();
         new QueueTool().waitEmpty();
         Node projectNode = new Node(ProjectsTabOperator.invoke().tree(), name);
         JPopupMenuOperator item = projectNode.callPopup();
         item.pushMenuNoBlock(_close);
         new QueueTool().waitEmpty();
         sleep();
 
         //Verify Project is not listed and clear Output window.
         Boolean status = false;
         try {
             ProjectsTabOperator pto = new ProjectsTabOperator();
             new Node(pto.invoke().tree(), name);
         } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
             status = true; //Should not find project
         }
         OutputOperator oo = new OutputOperator();
         new QueueTool().waitEmpty();
         oo.clear();
         return status;
     }
 
     /** Creates a screen capture of name in the workdir */
     public static void screenCapture(String name) { //screen.png
         String loc = WORKDIR + FILE_SEPARATOR + name;
         PNGEncoder.captureScreen(loc);
     }
 
     /** Creates a screen dump of name in the workdir */
     public static void screenDump(String name) { //screen.xml
         String loc = WORKDIR + FILE_SEPARATOR + name;
         try {
             Dumper.dumpAll(loc);
         } catch (Exception e) {
         }
     }
 
     public static void waitScanFinished() {
         try {
             Thread.sleep(3000);
         } catch (Exception e) {
         }
 
         long waitTime = 50;
         long waitCount = MAX_WAIT_TIME / waitTime;
 
         for (long time = 0; time < waitCount; time++) {
             try {
                 Thread.sleep(waitTime);
             } catch (Exception e) {
             }
 
             Object scanning = JProgressBarOperator.findJProgressBar((Container) MainWindowOperator.getDefault().getSource());
             if (scanning == null) {
                 return;
             }
         }
         throw new TimeoutExpiredException("Scaning isn't finished in " + MAX_WAIT_TIME + " ms");
     }
 // =================== Utility Operations  ===================
     public static void sleep() {
         sleep(WAIT_TIME);
     }
 
     public static void sleep(int ms) {
         try {
             Thread.sleep(ms);
         } catch (InterruptedException ex) {
         }
     }
 }
