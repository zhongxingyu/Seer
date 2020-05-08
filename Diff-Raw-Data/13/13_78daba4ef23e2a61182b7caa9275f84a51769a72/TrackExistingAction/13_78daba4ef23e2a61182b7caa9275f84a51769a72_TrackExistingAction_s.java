 package org.fedoraproject.eclipse.packager.compatibility;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.dialogs.ListSelectionDialog;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
 
 /**
  * Action for tracking existing projects.
  *
  */
 public class TrackExistingAction implements IWorkbenchWindowActionDelegate {
 	ListSelectionDialog lsd;
 	Shell shell;
 	@Override
 	public void run(IAction action) {
 		String message = FedoraPackagerCompatibilityText.TrackExistingAction_ListHeader;
 		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 		IProject[] wsProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		Set<String> nameSet = new HashSet<String>();
 		for (IProject project : wsProjects){
 			nameSet.add(project.getName());
 		}
 		lsd = new ListSelectionDialog(
 				shell, wsProjects, new ArrayContentProvider(), 
 				new WorkbenchLabelProvider(), 
 				FedoraPackagerCompatibilityText.TrackExistingAction_Description);
		if (lsd.open() == Window.OK){
 			for (Object selected: lsd.getResult()){
 				try {
 					String selectedString = ((IProject) selected).getName();
 					ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(selectedString)
 					.setPersistentProperty(
 							PackagerPlugin.PROJECT_PROP, "true"); //$NON-NLS-1$
					message.concat(" " + selectedString);
 				} catch (CoreException e) {
 					logger.logError(e.getMessage(), e);
 					FedoraHandlerUtils.showErrorDialog(shell, 
 							FedoraPackagerCompatibilityText.TrackExistingAction_Error, 
 							NLS.bind(FedoraPackagerCompatibilityText.TrackExistingAction_TrackAddingFailure, 
 									selected.toString(), e.getMessage()));
 				}
 			}
 		}
 		lsd.close();
		FedoraHandlerUtils.showInformationDialog(
				shell, FedoraPackagerCompatibilityText.TrackExistingAction_NotificationTitle, message);
 	}
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		// action not selection dependent
 	}
 
 	@Override
 	public void dispose() {
 		//no disposal needed
 	}
 
 	@Override
 	public void init(IWorkbenchWindow window) {
 		shell = window.getShell();
 	}
 
 }
