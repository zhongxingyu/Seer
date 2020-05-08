 package org.mule.tooling.ui.contribution.munit.actions;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.mule.tooling.core.MuleCorePlugin;
 import org.mule.tooling.core.MuleRuntime;
 import org.mule.tooling.core.model.IMuleProject;
 import org.mule.tooling.core.utils.CoreUtils;
 import org.osgi.framework.Bundle;
 
 public class MunitClassPathContainer implements IClasspathContainer{
 
     /** Unique container id */
     public static Path CONTAINER_ID = new Path("MUNIT_RUNTIME");
     List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
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
         try{
             IConfigurationElement[] configurationElementsFor = Platform.getExtensionRegistry().getConfigurationElementsFor("org.mule.tooling.ui.contribution.munit.munitRuntime");
             for (IConfigurationElement configElement : configurationElementsFor) {
                 if ( Arrays.asList(configElement.getAttribute("muleVersion").split(",")).contains(muleProject.getRuntimeId()) ){
                     this.munitVersion = configElement.getAttribute("munitVersion");
                     Bundle bundle = Platform.getBundle(configElement.getContributor().getName());
                     for ( IConfigurationElement library : configElement.getChildren() ){
                         addClassPath(bundle, library.getAttribute("path"));
                     }
 
                     break;
                 }
             }
         }
         catch(IOException e){

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
         return entries.toArray(new IClasspathEntry[]{});
     }
 
     @Override
     public String getDescription() {
         return "Munit Runtime ("+munitVersion+")";
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
