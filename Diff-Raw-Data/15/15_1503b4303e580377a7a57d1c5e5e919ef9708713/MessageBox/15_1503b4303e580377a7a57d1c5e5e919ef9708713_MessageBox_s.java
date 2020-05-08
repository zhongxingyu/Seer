 package com.uwusoft.timesheet.util;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolTip;
 import org.eclipse.ui.PlatformUI;
 
 import com.uwusoft.timesheet.Activator;
 
 public class MessageBox {
 	private static ToolTip toolTip;
 	private static Shell shell;
 	private static ILog logger = Activator.getDefault().getLog();
 
 	private static ToolTip getSystemTrayToolTip() {
 		if (toolTip != null) return toolTip;
 		try {
 			final Display display = PlatformUI.getWorkbench().getDisplay();
 			display.asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);
 					if (display.getSystemTray().getItemCount() > 0) {
 						toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
 						display.getSystemTray().getItem(0).setToolTip(toolTip);
 					}
 				}
 			});
 			return toolTip;
 		} catch (IllegalStateException e) {
 			shell = new Shell(Display.getDefault(), SWT.NO_TRIM | SWT.ON_TOP);
 			return null;
 		}
 	}
 
 	public static void setMessage(final String title, final String message) {
 		if ((toolTip = getSystemTrayToolTip()) == null) {
			MessageDialog.openInformation(shell, title, message);
 		}
 		else {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					toolTip.setText(title); 
 					toolTip.setMessage(message); 
 					toolTip.setVisible(true);
 				}
 			});
 		}
         logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, title + ": " + message));
 	}
 	
 	public static void setError(final String title, final String message) {
 		if ((toolTip = getSystemTrayToolTip()) == null) {
			MessageDialog.openError(shell, title, message);
 		}
 		else {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_ERROR);
 					toolTip.setText(title); 
 					toolTip.setMessage(message == null ? "" : message); 
 					toolTip.setVisible(true); 
 				}				
 			});
 		}
         logger.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, title + ": " + message));
 	}
 }
