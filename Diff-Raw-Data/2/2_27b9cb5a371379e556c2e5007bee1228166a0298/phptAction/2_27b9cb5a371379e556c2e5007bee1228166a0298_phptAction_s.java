 package de.schlueters.phpttestrunner;
 
 import de.schlueters.phpttestrunner.gui.TestResultsTopComponent;
 import de.schlueters.phpttestrunner.gui.startWizard.wzrdVisualPanel1;
 import de.schlueters.phpttestrunner.gui.startWizard.wzrdWizardPanel1;
 import de.schlueters.phpttestrunner.results.Result;
 import de.schlueters.phpttestrunner.results.fromhtmloutput.HTMLResult;
 import de.schlueters.phpttestrunner.results.Test;
 
 import de.schlueters.phpttestrunner.util.ExternalProcessRunner;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 import org.openide.util.actions.CallableSystemAction;
 
 import org.openide.windows.Mode;
 import org.openide.windows.WindowManager;
 
 import org.openide.util.Task;
 import org.openide.util.RequestProcessor;
 import java.awt.Dialog;
 import org.openide.NotifyDescriptor;
 import org.openide.DialogDisplayer;
 
 import org.openide.WizardDescriptor;
 
 import java.io.File;
 import java.util.List;
 
 public final class phptAction extends CallableSystemAction {
 
     public void performAction() {
         File resultfile = null;
         try {
             resultfile = File.createTempFile("phptresult.", ".html");
         } catch (Exception e) {
             NotifyDescriptor ex_dlg = new NotifyDescriptor.Message(e.toString()+" Couldn't create temp file!", NotifyDescriptor.ERROR_MESSAGE);
             DialogDisplayer.getDefault().notify(ex_dlg);
         }
 
 		wzrdWizardPanel1 wpanel = new wzrdWizardPanel1();
         WizardDescriptor.Panel[] panel = { wpanel };
         WizardDescriptor descr = new WizardDescriptor(panel);
         
         descr.setTitleFormat(new java.text.MessageFormat("Run phpt tests"));
         
         Dialog d = DialogDisplayer.getDefault().createDialog(descr);
         d.setVisible(true);
         d.toFront();
 
         if (descr.getValue() != WizardDescriptor.FINISH_OPTION) {
             return;
         }
 
 		wzrdVisualPanel1 vpanel = (wzrdVisualPanel1)wpanel.getComponent();
 
         phptTestRunner runner = new phptTestRunner(
             resultfile,
 			vpanel.getTestingBinaryFileName(),
 			vpanel.getTestsDirName(),
 			vpanel.getruntestsFileName(),
             vpanel.getArguements(),
             vpanel.getTestedBinaryFileName());
         
         Task task = new Task(runner);
 //        task.addTaskListener(new RunnerListener());
         RequestProcessor.getDefault().post(task);
         
         
         task.waitFinished();
         
         try {
             //Result res = new FailedResults("/tmp/phptresult.html");
 			//Result res = new HTMLResult(new File("/tmp/phptresult.html"));
             Result res = new HTMLResult(resultfile);
             final List<Test> executedTests = res.getExecutedTests();
 
            Mode myMode = WindowManager.getDefault().findMode("bottomSlidingSide");
             TestResultsTopComponent comp = (TestResultsTopComponent)WindowManager.getDefault().findTopComponent("TestResultsTopComponent");
             myMode.dockInto(WindowManager.getDefault().findTopComponent("TestResultsTopComponent"));
             comp.open();
             comp.setVisible(true);
             comp.setTests(executedTests);                
          } catch (Exception e) {
             e.printStackTrace();
             NotifyDescriptor ex_dlg = new NotifyDescriptor.Message(e, NotifyDescriptor.ERROR_MESSAGE);
             DialogDisplayer.getDefault().notify(ex_dlg);
          } finally {
             resultfile.delete();
          }
     }
     
     public String getName() {
         return NbBundle.getMessage(phptAction.class, "CTL_StartWizard");
     }
 
     @Override
     protected String iconResource() {
         return "de/schlueters/phpttestrunner/php.gif";
     }
 
     public HelpCtx getHelpCtx() {
         return HelpCtx.DEFAULT_HELP;
     }
 
     @Override
     protected boolean asynchronous() {
         return false;
     }
     /*
     class RunnerListener implements TaskListener{
         public void taskFinished(Task task) {
             try {
                 FailedResults res = new FailedResults("/tmp/result.txt");
                 res.getExecutedTests();
                 
                 Mode myMode = WindowManager.getDefault().findMode("bottomSlidingSide");
                 myMode.dockInto(WindowManager.getDefault().findTopComponent("TestResultsTopComponent"));
                 WindowManager.getDefault().findTopComponent("TestResultsTopComponent").open();
                 WindowManager.getDefault().findTopComponent("TestResultsTopComponent").setVisible(true);
             } catch (Exception e) {
             }
         }
     }
     */
 
 	private class phptTestRunner implements Runnable {
         ProcessBuilder command;
 
         public phptTestRunner(File resultfile, String testBinary, String tests, String runtests, String args, String testingBinary) {
 			File testdir = new File(tests);
 			if (!testdir.isDirectory()) testdir = testdir.getParentFile();
 
             command = new ProcessBuilder(testingBinary, runtests, "--html", resultfile.getAbsolutePath(), /*args,*/ tests);
             command.environment().put("TEST_PHP_EXECUTABLE", testingBinary);
             //command.directory(testdir);
         }
 
         public void run() {
             try {
                 ExternalProcessRunner.launchProcess("run-tests.php", command);
             } catch (java.io.IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
