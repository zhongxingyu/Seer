 package net.sf.jmoney.jdbcdatastore.handlers;
 
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.jdbcdatastore.JDBCDatastorePlugin;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.services.IEvaluationService;
 
 /**
  * Shows the given perspective. If no perspective is specified in the
  * parameters, then this opens the perspective selection dialog.
  * 
  * @since 3.1
  */
 public final class OpenSessionHandler extends AbstractHandler {
 
 	public final Object execute(final ExecutionEvent event)
 			throws ExecutionException {
 		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 
 		if (JMoneyPlugin.getDefault().saveOldSession(window)) {
 			IWorkbenchPage activePage = window.getActivePage();
 			activePage.close();
 
 			net.sf.jmoney.jdbcdatastore.SessionManager sessionManager = JDBCDatastorePlugin.getDefault().readSession(window);
 			if (sessionManager != null) {
 				// This call needs to be cleaned up, but is still needed
 				// to ensure a default currency is set.
				JMoneyPlugin.getDefault().initializeNewSession(sessionManager);
 
 			}
 
 			try {
 				window.openPage(sessionManager);
 			} catch (WorkbenchException e) {
 				ErrorDialog.openError(window.getShell(),
 						"Open Session failed", e
 						.getMessage(), e.getStatus());
 				throw new ExecutionException("Session could not be opened.", e); //$NON-NLS-1$
 			}
 
 			/*
 			 * The state of the 'isSessionOpen' property may have changed, so we
 			 * force a re-evaluation which will update any UI items whose
 			 * state depends on this property.
 			 */
 			IEvaluationService service = (IEvaluationService)PlatformUI.getWorkbench().getService(IEvaluationService.class);
 			service.requestEvaluation("net.sf.jmoney.core.isSessionOpen");
 		}
 		
 		return null;
 	}
 
 }
