 package com.flexive.izpack.install;
 
 import com.izforge.izpack.event.InstallerListener;
 import com.izforge.izpack.installer.AutomatedInstallData;
 import com.izforge.izpack.util.AbstractUIProgressHandler;
 import com.izforge.izpack.Pack;
 import com.izforge.izpack.PackFile;
 
 import java.io.File;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 
 /**
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FlexiveInstallListener implements InstallerListener {
     private static final String JETTY_DIR = "jetty-6.1.17";
 
     public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler) throws Exception {
         handler.startAction("Flexive setup", 3);
 
 
         final Project project = createProject(new File(idata.getInstallPath() + "/flexive-dist"));
 
         createEAR(handler, project, 1);
         initSchemas(handler, project, 2);
         prepareJetty(idata, handler, project, 3);
 
         
 //        for (int i = 0; i < 3; i++) {
 //            handler.nextStep("Performing step " + (i + 1), i + 1, 5);
 //            for (int j = 0; j < 5; j++) {
 //                handler.progress(j + 1, "Substep " + j);
 //                Thread.sleep(500);
 //            }
 //        }
         handler.stopAction();
     }
 
     private void createEAR(AbstractUIProgressHandler handler, Project project, int step) {
         handler.nextStep("Creating flexive.ear", step, 2);
         handler.progress(0, "Creating flexive.ear");
         project.executeTarget("ear");
     }
 
     private void initSchemas(AbstractUIProgressHandler handler, Project project, int step) {
         handler.nextStep("Initializing database schemas", step, 3);
 
         handler.progress(0, "Creating configuration schema");
         project.executeTarget("db.config.create");
 
         handler.progress(1, "Creating main [fleXive] schema");
        project.setProperty("schema.name", "flexive");
         project.executeTarget("db.create");
     }
 
     private void prepareJetty(AutomatedInstallData idata, AbstractUIProgressHandler handler, Project project, int step) {
         handler.nextStep("Preparing Jetty WebServer", step, 3);
 
         handler.progress(0, "Installing shared libraries");
         project.setProperty("glassfishlibs.dir", "../" + JETTY_DIR + "/lib/ext/flexive-environment");
         project.executeTarget("glassfish.libs");
 
         handler.progress(1, "Installing flexive.ear");
         project.executeTarget("deploy.jetty");
 
         // Workaround for http://jira.codehaus.org/browse/IZPACK-179 (empty logs/ directory is not created)
         final File logsDir = new File(idata.getInstallPath() + "/" + JETTY_DIR + "/logs");
         if (!logsDir.exists() && !logsDir.mkdir()) {
             System.err.println("Failed to create /logs directory for Jetty.");
         }
 
     }
 
     private Project createProject(File basedir) {
         final File buildFile = new File(basedir.getPath() + File.separator + "build.xml");
         final Project project = new Project();
         project.setBaseDir(basedir);
         project.init();
         project.setUserProperty("ant.file", buildFile.getAbsolutePath());
         ProjectHelper.configureProject(project, buildFile);
         return project;
     }
 
 
     public void beforePacks(AutomatedInstallData idata, Integer npacks, AbstractUIProgressHandler handler) throws Exception {
 
     }
 
     public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception {
 
     }
 
     public boolean isFileListener() {
         return false;
     }
 
     public void beforeDir(File dir, PackFile pf) throws Exception {
 
     }
 
     public void afterDir(File dir, PackFile pf) throws Exception {
 
     }
 
     public void beforeFile(File file, PackFile pf) throws Exception {
 
     }
 
     public void afterFile(File file, PackFile pf) throws Exception {
 
     }
 
     public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception {
 
     }
 
     public void afterInstallerInitialization(AutomatedInstallData data) throws Exception {
 
     }
 }
