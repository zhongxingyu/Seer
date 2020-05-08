 package net.sf.jmoney.currencypage;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.handlers.SessionEditorInput;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 public class OpenEditorHandler extends AbstractHandler {
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		try {
 			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			IEditorInput editorInput = new SessionEditorInput(JMoneyPlugin.getDefault().getSession(), null);
			window.getActivePage().openEditor(editorInput, CurrencyEditor.ID);
 		} catch (PartInitException e) {
 			JMoneyPlugin.log(e);
 		}
 		return null;
 	}

 }
