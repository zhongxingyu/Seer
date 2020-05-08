 package com.yoursway.autoupdater.eclipse;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.Workbench;
 
 import com.yoursway.autoupdater.auxiliary.AutoupdaterException;
 import com.yoursway.autoupdater.auxiliary.UpdatableApplicationView;
 
 public class UpdatableEclipseView implements UpdatableApplicationView {
     
    public void displayAutoupdaterErrorMessage(AutoupdaterException e) {
        Shell parent = Workbench.getInstance().getActiveWorkbenchWindow().getShell();
        String dialogTitle = "Eclipse Autoupdater";
        String message = "Cannot update Eclipse";
        IStatus status = new Status(IStatus.ERROR, "com.yoursway.autoupdater.eclipse", multimessage(e));
        
        ErrorDialog.openError(parent, dialogTitle, message, status);
     }
     
     private String multimessage(AutoupdaterException e) {
         StringBuilder sb = new StringBuilder();
         Throwable _e = e;
         while (_e != null) {
             sb.append(_e.getMessage());
             sb.append("\n");
             _e = _e.getCause();
         }
         return sb.toString();
     }
 }
