 package net.sf.jmoney.handlers;
 
 import net.sf.jmoney.model2.DatastoreManager;
 import net.sf.jmoney.resources.Messages;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.services.IEvaluationService;
 
 public class CloseSessionHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		Shell shell = HandlerUtil.getActiveShellChecked(event);
 		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 		
 		DatastoreManager sessionManager = (DatastoreManager)window.getActivePage().getInput();
 		if (sessionManager == null) {
 			MessageDialog.openWarning(
 					shell,
 					Messages.CloseSessionAction_WarningTitle,
 					Messages.CloseSessionAction_WarningMessage);
 		} else {
 			if (sessionManager.canClose(window)) {
 				sessionManager.close();
 				
				// Replace page with one that has null input.
 				try {
					window.getActivePage().close();
 					window.openPage(null);
 					//Update title
 					String productName = Platform.getProduct().getName();
 					window.getShell().setText(productName);
 				} catch (WorkbenchException e) {
 					throw new ExecutionException("Workbench exception occured while closing window.", e); //$NON-NLS-1$
 				}
 
 				/*
 				 * The state of the 'isSessionOpen' property has changed, so we
 				 * force a re-evaluation which will update any UI items whose
 				 * state depends on this property.
 				 */
 				IEvaluationService service = (IEvaluationService)PlatformUI.getWorkbench().getService(IEvaluationService.class);
 				service.requestEvaluation("net.sf.jmoney.core.isSessionOpen"); //$NON-NLS-1$
 			}
 		}
 
 		return null;
 	}
 
 }
