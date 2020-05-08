 /**
  * 
  */
 package net.sf.jmoney.stocks.navigator;
 
 import net.sf.jmoney.model2.AbstractDataOperation;
 import net.sf.jmoney.model2.ReferenceViolationException;
 import net.sf.jmoney.model2.Session;
import net.sf.jmoney.resources.Messages;
 import net.sf.jmoney.stocks.model.Security;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.BaseSelectionListenerAction;
 
 class DeleteSecurityAction extends BaseSelectionListenerAction {
 	private final Session session;
 
 	DeleteSecurityAction(Session session) {
		super(Messages.AccountsActionProvider_DeleteAccount);
		setToolTipText(Messages.AccountsActionProvider_DeleteAccount);
 		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
 		this.session = session;
 	}
 
 	@Override
 	protected boolean updateSelection(IStructuredSelection selection) {
 		if (selection.size() != 1) {
 			return false;
 		}
 		Object selectedObject = selection.getFirstElement();
 		return selectedObject instanceof Security;
 	}
 
 	@Override
 	public void run() {
 		// This action is enabled only when a single security is selected.
 		final Security security = (Security)getStructuredSelection().getFirstElement();
 
 		IOperationHistory history = PlatformUI.getWorkbench()
 		.getOperationSupport().getOperationHistory();
 
 		IUndoableOperation operation = new AbstractDataOperation(
 				session, "delete security") {
 			@Override
 			public IStatus execute() throws ExecutionException {
 				try {
 					session.getCommodityCollection().deleteElement(security);
 				} catch (ReferenceViolationException e) {
 					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Delete Failed", "The security is in use.  Unfortunately there is no easy way to find where the security was referenced.");
 					return Status.CANCEL_STATUS;
 				}
 				
 				return Status.OK_STATUS;
 			}
 		};
 
 		operation.addContext(session.getUndoContext());
 		try {
 			history.execute(operation, null, null);
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
