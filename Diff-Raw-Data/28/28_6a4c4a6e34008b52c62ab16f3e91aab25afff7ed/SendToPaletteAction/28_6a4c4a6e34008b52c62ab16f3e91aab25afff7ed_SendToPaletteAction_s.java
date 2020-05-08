 package org.mule.tooling.devkit.popup.actions;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.jar.JarFile;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.WorkspaceJob;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
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
 import org.mule.tooling.core.utils.BundleJarFileInspector;
 import org.mule.tooling.core.utils.BundleManifestReader;
 import org.mule.tooling.devkit.DevkitUIPlugin;
 import org.mule.tooling.devkit.maven.BaseDevkitGoalRunner;
 import org.mule.tooling.devkit.maven.MavenDevkitProjectDecorator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 
 public class SendToPaletteAction implements IObjectActionDelegate {
 
 	private IStructuredSelection selection;
 
 	
 	private static Bundle findBundle(BundleContext bundleContext, String symbolicName ){
 		Bundle[] bundles = bundleContext.getBundles();
 		for (Bundle bundle : bundles) {
 			if ( bundle.getSymbolicName().equals(symbolicName) ) {
 				return bundle;
 			}
 		}
 		return null;
 	}
 	
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
 
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
     				MessageDialog.openError(null, "Error", errorText+ "\nCannot try it until all errors are fixed.");
     				return;
      			}
 
 				final String installingPalette = "Installing into palette...";
 				final WorkspaceJob sendToPalette = new WorkspaceJob(installingPalette) {
 
 					@Override
 					public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
 						monitor.beginTask(installingPalette, 100);
 						MavenDevkitProjectDecorator mavenProject = MavenDevkitProjectDecorator.decorate(JavaCore.create(selectedProject));
 
 						final Integer result=new BaseDevkitGoalRunner(new String[] { "clean", "package", "-DskipTests", "-Ddevkit.studio.package.skip=false" }).run(mavenProject.getPomFile(), monitor);
 						
 						if(result==BaseDevkitGoalRunner.CANCELED)
                     		return Status.CANCEL_STATUS;
 						
 						IFolder pluginsDirectory = selectedProject.getProject().getFolder("/target/update-site/plugins/");
 						File pluginsFolder = new File(pluginsDirectory.getLocationURI());
 						if ( pluginsFolder.exists() )
 						{
 							for ( File resource : pluginsFolder.listFiles()){
 								installBundle(resource);
 							}
 						}
 						return Status.OK_STATUS;
 					}
 				};
 				sendToPalette.setUser(true);
 				sendToPalette.setPriority(Job.SHORT);
 				sendToPalette.schedule();
 			}
 		}
 	}
 
 	private void installBundle(File resource) {
 		try {
 
 			BundleManifestReader manifestReader = new BundleJarFileInspector(new JarFile(resource)).getManifest();
 			BundleContext bundleContext = DevkitUIPlugin.getDefault().getBundle().getBundleContext();
 			String location = resource.toURI().toString();
 
 			Bundle bundle = findBundle(bundleContext, manifestReader.getSymbolicName());
 			
 			if ( bundle != null ){
 				bundle.update(new FileInputStream(resource));
 			}
 			else{
 				bundle = bundleContext.installBundle(location);
 			}
 				
 			bundle.start();
 
 			reloadPalette();
 		} catch (BundleException e) {
 			// TODO: Show error
 		} catch (FileNotFoundException e) {
 			// TODO: Show error
 		} catch (IOException e) {
 			// TODO: Show error
 		}
 	}
 
 	private void reloadPalette() {
 		Display display = Display.getCurrent();
 		if(display == null){
 			display = Display.getDefault();
 		}
 		display.asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				MuleCorePlugin.getModuleManager().reinit();
 			}
 		});
 	}
 
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			this.selection = (IStructuredSelection) selection;
 		}
 	}
 	
 }
