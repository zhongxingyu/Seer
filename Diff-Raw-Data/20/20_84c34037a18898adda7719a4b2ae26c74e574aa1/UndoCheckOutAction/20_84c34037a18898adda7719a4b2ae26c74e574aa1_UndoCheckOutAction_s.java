 package net.sourceforge.eclipseccase.ui.actions;
 
 import net.sourceforge.eclipseccase.ClearDlgHelper;
 
 import java.util.*;
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;
 import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author conwaym To change this generated comment edit the template variable
  *         "typecomment": Workbench>Preferences>Java>Templates.
  */
 public class UndoCheckOutAction extends ClearCaseWorkspaceAction {
 
 	class UndoCheckOutQuestion implements Runnable {
 		private int returncode;
 
 		public int getReturncode() {
 			return returncode;
 		}
 
 		public void run() {
 			Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
 			MessageDialog checkoutQuestion = new MessageDialog(activeShell, "Checkout", null, "Do you really want to uncheckout?", MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
 
 			returncode = checkoutQuestion.open();
 		}
 	}
 
 	@Override
 	public void execute(IAction action) {
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
 			public void run(IProgressMonitor monitor) throws CoreException {
 				try {
 					IResource[] resources = getSelectedResources();
 					beginTask(monitor, "Undoing checkout...", resources.length);
 
 					if (ClearCasePlugin.isUseClearDlg()) {
 						monitor.subTask("Executing ClearCase user interface...");
 						ClearDlgHelper.uncheckout(resources);
 					} else {
 
 						UndoCheckOutQuestion question = new UndoCheckOutQuestion();
 
 						PlatformUI.getWorkbench().getDisplay().syncExec(question);
 
 						/* Yes=0 No=1 Cancel=2 */
 						if (question.getReturncode() == 0) {
 							// Sort resources with directories last so that the
 							// modification of a
 							// directory doesn't abort the modification of files
 							// within
 							// it.
							List resList = Arrays.asList(resources);
 							Collections.sort(resList, new DirectoryLastComparator());
 
 							Vector<IResource> parents = new Vector<IResource>();
 
 							ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
 							for (int i = 0; i < resources.length; i++) {
 								IResource resource = resources[i];
 								ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 								provider.setOperationListener(opListener);
 								provider.uncheckout(new IResource[] { resource }, IResource.DEPTH_ZERO, subMonitor(monitor));
 
 								// update parent status only once
 								if (!parents.contains(resource.getParent())) {
 									parents.add(resource.getParent());
 								}
 							}
 
 							for (IResource resource : parents) {
 								ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 								provider.setOperationListener(opListener);
 								provider.updateState(resource, IResource.DEPTH_ZERO, new SubProgressMonitor(monitor, 10));
 							}
 						}
 					}
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 
 		executeInBackground(runnable, "Uncheckout resources from ClearCase");
 	}
 
 	@Override
 	public boolean isEnabled() {
 		IResource[] resources = getSelectedResources();
 		if (resources.length == 0)
 			return false;
 		for (int i = 0; i < resources.length; i++) {
 			IResource resource = resources[i];
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
 				return false;
 			if (!provider.isCheckedOut(resource))
 				return false;
 		}
 		return true;
 	}
 
 }
