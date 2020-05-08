 package com.nmt.nmj.editor.action;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 
 import com.nmt.nmj.editor.Application;
 import com.nmt.nmj.editor.ICommandIds;
 import com.nmt.nmj.editor.exception.NmjEditorException;
 import com.nmt.nmj.editor.nls.NlsMessages;
 import com.nmt.nmj.editor.perspective.ListPerspective;
 import com.nmt.nmj.editor.view.ListView;
 
 public class RefreshDatabaseAction extends Action {
 
     private final IWorkbenchWindow window;
 
     public RefreshDatabaseAction(IWorkbenchWindow window, String label) {
         this.window = window;
         setText(label);
         setToolTipText(label);
         setId(com.nmt.nmj.editor.ICommandIds.CMD_REFRESH_DATABASE);
         setActionDefinitionId(ICommandIds.CMD_REFRESH_DATABASE);
     }
 
     public void run() {
         if (window != null) {
            if (Application.getDatabaseService().isOpen()) {
                 MessageDialog.openInformation(window.getShell(), "Information", NlsMessages.database_closed);
                 return;
             }
             try {
                 Application.getDatabaseService().flushConnection();
            } catch (NmjEditorException e1) {
                MessageDialog.openError(window.getShell(), NlsMessages.common_error, e1.getMessage());
                return;
            }
            try {
                 PlatformUI.getWorkbench().showPerspective(ListPerspective.ID, window);
                 ListView editorView = (ListView) window.getActivePage().showView(ListView.ID);
                 editorView.refresh();
             } catch (PartInitException e) {
                 MessageDialog.openError(window.getShell(), NlsMessages.common_error, "List view is missing");
             } catch (WorkbenchException e) {
                 MessageDialog.openError(window.getShell(), NlsMessages.common_error, "List perspective is missing");
             }
         }
     }
 }
