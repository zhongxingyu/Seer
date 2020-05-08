 package org.javakontor.sherlog.test.ui.cases;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.javakontor.sherlog.test.ui.framework.ApplicationWindowHandler;
 import org.javakontor.sherlog.test.ui.framework.BundleListViewHandler;
 import org.javakontor.sherlog.test.ui.framework.GuiTestContext;
 import org.javakontor.sherlog.test.ui.framework.LoadLogFileWizardHandler;
 
 public class LoadLogFileWizardGuiTest extends TestCase {
 
   private final GuiTestContext _guiTestContext;
 
   ApplicationWindowHandler     _applicationWindowHandler;
 
   public LoadLogFileWizardGuiTest(GuiTestContext guiTestContext) {
     super();
     _guiTestContext = guiTestContext;
     _applicationWindowHandler = new ApplicationWindowHandler(guiTestContext);
   }
 
   public void test_A() throws Exception {
 
     _applicationWindowHandler.pushFileMenuItem("Load log file...", false);
 
     LoadLogFileWizardHandler loadLogFileWizardHandler = new LoadLogFileWizardHandler(_guiTestContext,
         _applicationWindowHandler.getApplicationFrameOperator());
 
     File binaryLogFile = new File(_guiTestContext.getWorkspaceLocation(),
        "org.javakontor.sherlog.domain.impl.test/logs/log_small.bin");
     assertTrue("The binary test-logfile '" + binaryLogFile.getAbsolutePath() + "' must be an existing file",
         binaryLogFile.isFile());
     String testLogFile = binaryLogFile.getAbsolutePath();
 
     loadLogFileWizardHandler.getLogFileChooserViewHandler().enterFileName(testLogFile);
     loadLogFileWizardHandler.getLogFileChooserViewHandler().selectLogEventFlavour("log4j");
 
     loadLogFileWizardHandler.getOkButtonOperator().clickMouse();
     loadLogFileWizardHandler.assertClosed();
   }
 
   public void test_BundleView() throws Exception {
 
     // open Bundle-List via menu
     BundleListViewHandler bundleListViewHandler = BundleListViewHandler.openFromMenu(_applicationWindowHandler);
     assertNotNull(bundleListViewHandler);
 
     // make sure, rows displayed in the table are equal to the number of installed bundles
     int rows = bundleListViewHandler.getBundleListTableOperator().getModel().getRowCount();
     assertEquals(_guiTestContext.getBundleContext().getBundles().length, rows);
 
   }
 
 }
