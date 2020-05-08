 package org.mule.tooling.ui.contribution.munit.classpath;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.PlatformUI;
 import org.mule.tooling.core.MuleCorePlugin;
 import org.mule.tooling.core.MuleRuntime;
 import org.mule.tooling.core.model.IMuleProject;
 import org.mule.tooling.core.utils.CoreUtils;
 import org.mule.tooling.ui.contribution.munit.MunitPlugin;
 import org.mule.tooling.ui.contribution.munit.runtime.MunitLibrary;
 import org.mule.tooling.ui.contribution.munit.runtime.MunitRuntime;
 import org.mule.tooling.ui.contribution.munit.runtime.MunitRuntimeExtension;
 import org.osgi.framework.Bundle;
 
 /**
  * <p>
  * The class path container for all the Munit jars in case the project is not maven based
  * </p>
  */
 public class MunitClassPathContainer implements IClasspathContainer {
 
     public static Path CONTAINER_ID = new Path("MUNIT_RUNTIME");
 
     private List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
     private IMuleProject muleProject;
     private String munitVersion;
 
     public MunitClassPathContainer(IJavaProject javaProject) {
         try {
             this.muleProject = MuleRuntime.create(javaProject.getProject());
         } catch (CoreException e) {
             e.printStackTrace();
             MuleCorePlugin.getLog().log(e.getStatus());
         }
 
         addLibraries();
     }
 
     private void addLibraries() {
         try {
             MunitRuntime munitRuntime = MunitRuntimeExtension.getInstance().getMunitRuntimeFor(muleProject);
             if (munitRuntime != null) {
                munitVersion = munitRuntime.getMunitVersion();
                 Bundle bundle = Platform.getBundle(munitRuntime.getBundleId());
                 for (MunitLibrary munitLibrary : munitRuntime.getLibraries()) {
                     addClassPath(bundle, munitLibrary.getPath());
                 }
             } else {
                 MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "No Munit Runtime", "No Munit runtime found for the current mule runtime");
             }
         } catch (IOException e) {
             MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Unexpected Error", "Erro while creating Munit Runtime.");
             MunitPlugin.log(e);
         }
     }
 
     private void addClassPath(Bundle bundle, String string) throws IOException {
         final URL url = FileLocator.find(bundle, new Path(File.separator + string), null);
         if (url != null) {
             entries.add(CoreUtils.createClassPathEntryFromURL(url));
         }
     }
 
     @Override
     public IClasspathEntry[] getClasspathEntries() {
         return entries.toArray(new IClasspathEntry[] {});
     }
 
     @Override
     public String getDescription() {
         return "Munit Runtime (" + munitVersion + ")";
     }
 
     @Override
     public int getKind() {
         return IClasspathContainer.K_APPLICATION;
     }
 
     @Override
     public IPath getPath() {
         return CONTAINER_ID;
     }
 
 }
