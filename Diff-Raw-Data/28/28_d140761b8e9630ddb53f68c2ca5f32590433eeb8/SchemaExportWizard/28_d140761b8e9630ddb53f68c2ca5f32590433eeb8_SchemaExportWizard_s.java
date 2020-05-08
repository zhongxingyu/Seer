 package com.runwaysdk.eclipse.plugin.wizards;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.maven.cli.MavenCli;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.console.MessageConsole;
 
 import com.runwaysdk.constants.ProfileManager;
 import com.runwaysdk.dataaccess.schemamanager.SchemaManager;
 import com.runwaysdk.eclipse.plugin.runway.diagram.part.RunwayCreationWizardPage;
 import com.runwaysdk.eclipse.plugin.runway.diagram.part.RunwayDiagramEditorPlugin;
 import com.runwaysdk.eclipse.plugin.schema.SchemaUtil;
 import com.runwaysdk.eclipse.plugin.schema.exporter.DOMExporter;
 
 public class SchemaExportWizard extends Wizard implements INewWizard
 {
   protected RunwayCreationWizardPage diagramModelFilePage;
   protected SchemaExportWizardPage1 page1;
   protected IStructuredSelection     selection;
   
   public SchemaExportWizard() {
     super();
     //setNeedsProgressMonitor(true);
   }
   
   @Override
   public void init(IWorkbench arg0, IStructuredSelection arg1)
   {
     this.selection = arg1;
   }
   
   @Override
   public void addPages() {
     page1 = new SchemaExportWizardPage1();
     page1.setSelection(selection);
     addPage(page1);
   }
   
   @Override
   public boolean performFinish()
   {
     final PrintStream out = SchemaUtil.openRunwayConsole();
     
     /*
      * Call Runway's merge tool using Maven.
      */
     URL url = Platform.getInstanceLocation().getURL();
     String workspace = new File(url.getPath()).getAbsolutePath();
     
     final IFile diagramFile = page1.getDiagramFile();
     final String projectName = diagramFile.getProject().getName();
     final String tempFolderStr = workspace + "/.metadata/.plugins/com.runwaysdk.eclipse.plugin/" + projectName + "/" + diagramFile.getName().replace(".runway_diagram", "");
     
    if (new File(workspace + diagramFile.getProject().getFullPath().toOSString() + "/target/classes/com/runwaysdk").exists() == false) {
       RuntimeException e = new RuntimeException("An error has occurred, unable to proceed with export. The project must be compiled first.");
       SchemaUtil.handleError(this.getShell(), e);
       throw e;
     }
    if (new File(workspace + diagramFile.getProject().getFullPath().toOSString() + "/lib").exists() == false) {
       RuntimeException e = new RuntimeException("An error has occurred, unable to proceed with export. This task requires a lib directory to exist first. Run the lib hack to fix this error.");
       SchemaUtil.handleError(this.getShell(), e);
       throw e;
     }
     
     String[] runwayArgs = new String[] { "-dir",
         tempFolderStr,
        "jar:file:" + workspace + "/" + projectName + "/lib/runwaysdk-server-0.0.2-SNAPSHOT.jar!/com/runwaysdk/resources/version.xsd",
        workspace + diagramFile.getProject().getFullPath().toOSString() + "/src/main/domain/application/" + page1.getSchemaName() };
     
 //    String[] mavenArgs = new String[] {
 //        "exec:java",
 //        "-Dexec.mainClass=com.runwaysdk.dataaccess.schemamanager.SchemaManager",
 //        "-Dexec.arguments=" + runwayArgs[0] + "," + runwayArgs[1] + "," + runwayArgs[2] + "," + runwayArgs[3] };
     final String[] mavenArgs = new String[] {
         "exec:exec",
         "-Dexec.executable=java",
         "-Dexec.args=-classpath %classpath com.runwaysdk.dataaccess.schemamanager.SchemaManager " +  runwayArgs[0] + " " + runwayArgs[1] + " " + runwayArgs[2] + " " + runwayArgs[3]};
     
     int retVal = new MavenCli().doMain(mavenArgs, workspace + diagramFile.getProject().getFullPath().toOSString(), out, out);
 
     if (retVal != 0) {
       MessageDialog dialog = new MessageDialog(this.getShell(), "An error has occurred.", null,
           "An exception has occurred while creating a new schema. (Maven exited with status code " + retVal + ")", MessageDialog.ERROR, new String[] { "Ok" }, 0);
       int result = dialog.open();
       return false;
     }
     
     final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
     final Runnable task = new Runnable() {
       public void run() {
         /*
          * Refresh Eclipse to display the new file Runway's merger created
          */
         try
         {
           diagramFile.getProject().getFolder("src/main/domain").refreshLocal(IResource.DEPTH_INFINITE, null);
         }
         catch (CoreException e)
         {
           SchemaUtil.handleError(getShell(), e);
         }
         
         /*
          * Delete the temp schema files used in the merge process.
          */
         File tempFolder = new File(tempFolderStr);
         
         LinkedList<File> files = new LinkedList<File>(Arrays.asList(tempFolder.listFiles()));
         
         Iterator<File> it = files.iterator();
         while (it.hasNext()) {
           File file = (File) it.next();
           
           file.delete();
         }
       }
     };
     worker.schedule(task, 4, TimeUnit.SECONDS);
     
     return true;
   }
   
 }
