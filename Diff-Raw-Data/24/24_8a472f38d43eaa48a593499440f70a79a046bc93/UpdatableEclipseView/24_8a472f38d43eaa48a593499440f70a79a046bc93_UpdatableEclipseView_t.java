 package com.yoursway.autoupdater.eclipse;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import com.yoursway.autoupdater.auxiliary.AutoupdaterException;
 import com.yoursway.autoupdater.auxiliary.UpdatableApplicationView;
 
 public class UpdatableEclipseView implements UpdatableApplicationView {
     
    public void displayAutoupdaterErrorMessage(final AutoupdaterException e) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                //Shell parent = Workbench.getInstance().getActiveWorkbenchWindow().getShell();
                Shell parent = null;
                String dialogTitle = "Eclipse Autoupdater";
                String message = "Cannot update Eclipse";
                IStatus status = new Status(IStatus.ERROR, "com.yoursway.autoupdater.eclipse",
                        multimessage(e));
                
                ErrorDialog.openError(parent, dialogTitle, message, status);
            }
        });
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
