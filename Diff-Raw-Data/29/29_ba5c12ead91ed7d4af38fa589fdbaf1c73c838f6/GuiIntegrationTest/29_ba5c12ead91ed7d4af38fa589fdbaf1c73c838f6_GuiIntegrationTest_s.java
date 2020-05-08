 package org.javakontor.sherlog.test.ui;
 
 import org.javakontor.sherlog.test.ui.cases.GuiIntegrationTestCases;
 import org.javakontor.sherlog.test.ui.framework.GuiTestContext;
 
 public class GuiIntegrationTest extends AbstractGuiBasedIntegrationTest {
 
   public void test_LoadFilterResetLogFile() throws Exception {
     final GuiIntegrationTestCases test = new GuiIntegrationTestCases(new GuiTestContext(this.bundleContext,
         getWorkspaceLocation()));
     test.test_LoadFilterResetLogFile();
   }
 
   public void test_BundleView() throws Exception {
     final GuiIntegrationTestCases test = new GuiIntegrationTestCases(new GuiTestContext(this.bundleContext,
         getWorkspaceLocation()));
     test.test_BundleView();
   }
 
   @Override
   protected String getImportPackages() {
     return "org.javakontor.sherlog.application.view," //
         + "org.javakontor.sherlog.domain.reader";
 
   }
 
   @Override
   protected String[] getTestBundlesNames() {
     return concat(getBaseBundleNames(), new String[] { "null,org.javakontor.sherlog.util,1.0.0",
         "null,org.javakontor.sherlog.domain,1.0.0", "null,org.javakontor.sherlog.domain.impl,1.0.0",
         "null,org.javakontor.sherlog.application,1.0.0", "null,org.javakontor.sherlog.application.mvc,1.0.0",
         "null,org.javakontor.sherlog.ui.loadwizard,1.0.0", "null,org.javakontor.sherlog.ui.logview,1.0.0",
         "null,org.javakontor.sherlog.ui.filter,1.0.0", "null,org.javakontor.sherlog.util.ui,1.0.0",
        "null,org.lumberjack.ui.logview.colorfilter,1.0.0",
         "null,org.javakontor.sherlog.domain.logeventflavour.l4jbinary,1.0.0",
         "null,org.javakontor.sherlog.ui.managementagent,1.0.0", });
   }
 
 }
