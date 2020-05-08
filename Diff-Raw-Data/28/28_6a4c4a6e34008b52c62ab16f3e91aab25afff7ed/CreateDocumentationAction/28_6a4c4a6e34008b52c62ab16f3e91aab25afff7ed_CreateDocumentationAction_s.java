 package org.mule.tooling.devkit.popup.actions;
 
 import java.net.MalformedURLException;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.WorkspaceJob;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.help.ui.internal.DefaultHelpUI;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.mule.tooling.core.MuleCorePlugin;
 import org.mule.tooling.devkit.maven.BaseDevkitGoalRunner;
 import org.mule.tooling.devkit.maven.MavenDevkitProjectDecorator;
 import org.mule.tooling.ui.widgets.util.SilentRunner;
 
 @SuppressWarnings("restriction")
 public class CreateDocumentationAction implements IObjectActionDelegate {
 
     /** Current selection */
     private IStructuredSelection selection;
 
     public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 
     }
 
     public void run(IAction action) {
         Object selected = selection.getFirstElement();
         
         if (selected instanceof IJavaElement) {
             final IProject selectedProject = ((IJavaElement) selected).getJavaProject().getProject();
              
             if (selectedProject != null) {
             	int errorCount = 0;
             	IMarker[] errors;
      			try {
      				errors = selectedProject.findMarkers(null /*all markers*/, true,IResource.DEPTH_INFINITE);
      				for (IMarker error : errors) {
      					int severity = error.getAttribute(IMarker.SEVERITY, Integer.MAX_VALUE);
      				    if (severity == IMarker.SEVERITY_ERROR){
      				    	errorCount++;
      				    }
      	            }
      			} catch (CoreException e1) {
      				e1.printStackTrace();
      			}
      			
      			if(errorCount>0){
      				String errorText= "Your project has ("+errorCount+") " + ((errorCount>1) ? "errors":"error" + ".");
     				MessageDialog.openError(null, "Error", errorText+ "\nCannot generate documentation until all errors are fixed.");
     				return;
      			}
                 final String convertingMsg = "Generating Documentation...";
                 final WorkspaceJob createDocumentationJob = new WorkspaceJob(convertingMsg) {
 
                     @Override
                     public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                         monitor.beginTask(convertingMsg, 100);
                         MavenDevkitProjectDecorator mavenProject = MavenDevkitProjectDecorator.decorate(JavaCore.create(selectedProject));
                         final Integer result = new BaseDevkitGoalRunner(new String[] { "clean", "package", "-DskipTests", "javadoc:javadoc" }).run(mavenProject.getPomFile(), monitor);
 
                         Display.getDefault().syncExec(new Runnable() {
 
                             @Override
                             public void run() {
                                 SilentRunner.run(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         try {
                                         	if(result==BaseDevkitGoalRunner.CANCELED)
                                         		return;
                                         	DefaultHelpUI.showInWorkbenchBrowser(selectedProject.getFile("/target/apidocs/index.html").getLocationURI().toURL().toString(), true);
                                         } catch (MalformedURLException e) {
                                             throw new RuntimeException(e);
                                         }
 
                                     }
                                 });
                             }
                         });
 
                         return Status.OK_STATUS;
                     }
                 };
                 createDocumentationJob.setUser(true);
                 createDocumentationJob.setPriority(Job.SHORT);
                 createDocumentationJob.schedule();
 
             }
         }
 
     }
 
     public void selectionChanged(IAction action, ISelection selection) {
         if (selection instanceof IStructuredSelection) {
             this.selection = (IStructuredSelection) selection;
         }
     }
 }
