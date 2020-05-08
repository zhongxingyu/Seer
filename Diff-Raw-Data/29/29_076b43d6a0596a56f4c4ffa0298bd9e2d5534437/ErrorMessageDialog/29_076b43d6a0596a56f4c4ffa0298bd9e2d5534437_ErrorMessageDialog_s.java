 package com.versionone.taskview.views;
 
 import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 public class ErrorMessageDialog extends Dialog {
 
     static int WINDOW_HEIGHT = 400;
     static int WINDOW_WIDTH = 400;
     private StyledText textArea;
     private final String message;
     
     public ErrorMessageDialog(Shell parentShell, String message) {
         super(parentShell);
         setShellStyle(this.getShellStyle() | SWT.RESIZE);
         this.message = message; 
     }
     
     /**
      * {@link #createDialogArea(Composite)}
      */
     @Override
     protected Control createDialogArea(Composite parent) {
         Composite container = (Composite) super.createDialogArea(parent);
         container.setLayout(new FillLayout(SWT.VERTICAL));
 
         textArea = new StyledText(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
         textArea.setText(message);
         textArea.setEditable(false);
         
         return container;
     }
     
     /**
      * {@link #configureShell(Shell)}
      */
     @Override
     protected void configureShell(Shell newShell) {
         super.configureShell(newShell);
         newShell.setText("Error message");
         Display display = PlatformUI.getWorkbench().getDisplay();
         Point size = newShell.computeSize(WINDOW_WIDTH, WINDOW_HEIGHT);
         Rectangle screen = display.getMonitors()[0].getBounds();
         newShell.setBounds((screen.width - size.x) / 2, (screen.height - size.y) / 2, size.x, size.y);
         newShell.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
     }
 }
