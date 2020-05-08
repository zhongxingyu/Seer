 package net.sourceforge.eclipseccase.ui.actions;
 
 import org.eclipse.core.resources.IProject;
 
 import net.sourceforge.clearcase.ClearCase;
 
 import java.io.File;
 
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.ui.IActionDelegate;
 
 /**
  * Updates the resources in a clearcase snapshot view.
  */
 public class ExternalUpdateAction extends ClearCaseWorkspaceAction {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isEnabled() {
 		IResource[] resources = getSelectedResources();
 		if (resources.length == 0)
 			return false;
 		for (int i = 0; i < resources.length; i++) {
 			IResource resource = resources[i];
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource))
 				return false;
 			if (!provider.isSnapShot(resource))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void execute(IAction action) {
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
 			public void run(IProgressMonitor monitor) throws CoreException {
 
 				try {
 
 					IResource[] resources = getSelectedResources();
 					if (resources != null && resources.length != 0) {
 						for (int i = 0; i < resources.length; i++) {
 
 							IResource resource = resources[i];
 							String element = null;
 
 							if (resource.getType() == IResource.FOLDER) {
 								element = resource.getLocation().toOSString();
 							} else if (resource.getType() == IResource.PROJECT) {
 								// Project folder.
 								element = resource.getLocation().toOSString();
 
 							} else {
 								// Folder to file.
 								element = resource.getParent().getLocation().toOSString();
 							}
 							ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
 							p.update(element, ClearCase.GRAPHICAL, true);
 
 						}
 					}
 				} finally {
 					monitor.done();
 				}
 
 			}
 		};
 		executeInBackground(runnable, "Updating resources in Snapshot View from ClearCase");
 	}
 
 }
