 package org.rubypeople.rdt.internal.core.builder;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 
 public class CleanRdtCompiler extends AbstractRdtCompiler  {
 
     private List<IFile> projectFiles;
 
     public CleanRdtCompiler(IProject project) {
         this(project, new MarkerManager());
     }
     
    private CleanRdtCompiler(IProject project,
            MarkerManager markerManager) {
         super(project, markerManager);
     }
 
     protected void removeMarkers(IMarkerManager markerManager) {
         markerManager.removeProblemsAndTasksFor(project);
     }
 
     protected List<IFile> getFilesToClear() throws CoreException {
     	return getFilesToCompile();
     }
 
     protected List<IFile> getFilesToCompile() throws CoreException {
     	if (projectFiles == null) {
     		analyzeFiles();
     	}
         return projectFiles;
     }
 
     private void analyzeFiles() throws CoreException {
         ProjectFileFinder finder = new ProjectFileFinder(project);
         projectFiles = finder.findFiles();
     }
 
 }
