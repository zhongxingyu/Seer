 package org.caesarj.ui.util;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.internal.resources.Container;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 /**
  * Central Class for obtaining all relevant data from an IProject Object
  * 
  * @author Ivica Aracic <ivica.aracic@bytelords.de>
  */
 public class ProjectProperties {
     
     private String outputPath;
     private String projectLocation;
     private StringBuffer classPath = new StringBuffer();
     private List sourceFiles = new ArrayList();
     
     
     public ProjectProperties(IProject project) throws JavaModelException, CoreException {
         IJavaProject jProject = JavaCore.create(project);        
            
         String projectLocalPrefix = File.separator + project.getName();
             
         /*
          * get paths
          */
         this.projectLocation = project.getLocation().removeLastSegments(1).toOSString();
         this.outputPath = jProject.getOutputLocation().toOSString();
 
         /*
          * get source files
          */
         IClasspathEntry[] classPathEntries = jProject.getResolvedClasspath(false);
 
         for(int i=0; i<classPathEntries.length; i++) {
             if(classPathEntries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                 // 1st segment of the path has to be removed because it is added
                 // again by findMember ending in duplicated first segment in the path
                 getAllSourceFiles(
                     project.findMember(
                         classPathEntries[i].getPath().removeFirstSegments(1)
                     ),
                     this.sourceFiles
                 );
             }               
             else if(classPathEntries[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
         
                 if(this.classPath.length()>0) {
 					this.classPath.append(File.pathSeparator);
 				}
         
                 String cp = classPathEntries[i].getPath().toOSString(); 
                 if(cp.startsWith(projectLocalPrefix)) {
                     cp =
                         this.projectLocation + 
                         classPathEntries[i].getPath().toOSString();
                 }                
 
                 this.classPath.append(cp);
             }
         }
     }
 
     private void getAllSourceFiles(IResource resource, List sourceFilesArg) throws CoreException {
         if(resource!=null) {        
             if(resource.getName().endsWith(".java") || resource.getName().endsWith(".cj")) { //$NON-NLS-1$ //$NON-NLS-2$
                 sourceFilesArg.add(resource.getFullPath().toOSString());
             }
             else if(resource instanceof Container) {
                 Container container = (Container)resource;
                 IResource[] resources = container.members();
                 for(int i=0; i<resources.length; i++) {
                     getAllSourceFiles(resources[i], sourceFilesArg);       
                 }
             }
         }
     }
     
    public static IResource findResource(String fullPath, IProject p) {    
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();        
        IPath rootPath = root.getLocation();
        IPath path = new Path(fullPath);
        if(rootPath.isPrefixOf(path)) {
             // remove project location and project name segment
            path = path.removeFirstSegments( rootPath.segmentCount()+1 );
         }
         // find resource relative to project
         IResource ret = p.findMember(path);
         return ret;        
     }
 
 
 	public String getProjectLocation() {
 		return this.projectLocation;
 	}
         
     public String getOutputPath() {
         return this.outputPath;
     }
 
     public String getClassPath() {
         return this.classPath.toString();
     }
 
 	public Collection getSourceFiles() {		
 		return this.sourceFiles;
 	}
     
     public String toString() {
         StringBuffer res = new StringBuffer();
         res.append("projectLocation\n\t"+getProjectLocation()); //$NON-NLS-1$
         res.append("\noutputPath\n\t"+getOutputPath()); //$NON-NLS-1$
         res.append("\nclasspath\n\t"+getClassPath()); //$NON-NLS-1$
         res.append("\nsource files:\n"); //$NON-NLS-1$
         for(Iterator it=this.sourceFiles.iterator(); it.hasNext(); ) {
             res.append('\t');
             res.append(it.next());
             res.append('\n');
         }
 
         return res.toString();
     }
     
 }
