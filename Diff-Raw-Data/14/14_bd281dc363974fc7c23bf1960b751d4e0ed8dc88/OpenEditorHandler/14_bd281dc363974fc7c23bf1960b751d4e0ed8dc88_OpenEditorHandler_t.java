 package net.sf.jmoney.currencypage;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.handlers.SessionEditorInput;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 public class OpenEditorHandler extends AbstractHandler {
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		try {
 			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			IEditorInput editorInput = new SessionEditorInput();
			
			/**
			 * Note that this extended form of openEditor is used because we must match on the editor id,
			 * not the input id.  The form that takes just the first two parameters will match on the input
			 * which is no good because the same input may be used for multiple editors.
			 */
			window.getActivePage().openEditor(editorInput, CurrencyEditor.ID, true, IWorkbenchPage.MATCH_ID);
 		} catch (PartInitException e) {
 			JMoneyPlugin.log(e);
 		}
 		return null;
 	}
 }
