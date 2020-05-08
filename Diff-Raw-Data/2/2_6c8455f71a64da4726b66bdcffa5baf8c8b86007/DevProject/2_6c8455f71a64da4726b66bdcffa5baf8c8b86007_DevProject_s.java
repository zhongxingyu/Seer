 package ch.basler.importthem.dev;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.bsf.util.IOUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.internal.wizards.datatransfer.ImportPage;
 import org.eclipse.ui.internal.wizards.datatransfer.ImportPage.ProjectRecord;
 
 import ch.basler.importthem.io.UnZip;
 
 public class DevProject {
   public final static String HOME = System.getProperty("user.home") + "/.importThem";
   public final static String SCRIPT_NAME = "script.groovy";
  public static final String NAME = "ch.basler.importthem.dev_";
   public final static File SCRIPT = new File(HOME + "/" + NAME + "/src/" + SCRIPT_NAME);
 
   public static void createDevProject() throws IOException, CoreException {
     File dest = new File(HOME);
     File projDir = new File(dest, NAME);
     if (!projDir.exists()) {
       dest.mkdirs();
       InputStream resourceAsStream = DevProject.class.getResourceAsStream("dev.zip");
       UnZip.unzip(resourceAsStream, dest);
     }
     File dotProject = new File(projDir, ".project");
 
     ImportPage page = new ImportPage();
     ProjectRecord[] selected = new ProjectRecord[] {page.new ProjectRecord(dotProject)
 
     };
     page.createProjects(null, selected);
   }
 
   public static String getScriptContent() throws FileNotFoundException, IOException {
     return IOUtils.getStringFromReader(new FileReader(DevProject.SCRIPT));
   }
 
   public static void delete(IProgressMonitor monitor) throws CoreException {
     IProject project = getProject();
     project.delete(IResource.NEVER_DELETE_PROJECT_CONTENT | IResource.FORCE, monitor);
   }
 
   public static IProject getProject() {
     final IWorkspace workspace = ResourcesPlugin.getWorkspace();
     IProject project = workspace.getRoot().getProject(NAME);
     return project;
   }
 
   public static boolean exists() {
     IProject project = getProject();
     return project.exists();
   }
 
   public static void openEditor() throws PartInitException {
     IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
     IWorkbenchPage page = window.getActivePage();
     IFile scriptFile = getProject().getFolder("src").getFile(SCRIPT_NAME);
     int max = 2000;
     while (max > 0 && !scriptFile.exists()) {
       try {
         max -= 200;
         Thread.sleep(200);
       }
       catch (InterruptedException e) {}
     }
     if (scriptFile.exists()) {
       IDE.openEditor(page, scriptFile, true);
     }
   }
 
 }
