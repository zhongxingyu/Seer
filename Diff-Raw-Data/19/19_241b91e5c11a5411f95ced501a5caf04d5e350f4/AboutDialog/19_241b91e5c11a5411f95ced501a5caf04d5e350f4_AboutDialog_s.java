 package com.hariko.awawa;
 
 
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.events.*;
 
 public final class AboutDialog extends Dialog {
     boolean result = false;
     Shell shell;
     
     public AboutDialog (Shell parent, int style) {
         super (parent, style);
 	}
     public AboutDialog (Shell parent) {
         this (parent, 0); // your default style bits go here (not the Shell's style bits)
     }
     
     public boolean open () {
         Shell parent = getParent();
         shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
         shell.setText("About this application");
         shell.setSize(240, 180);
             
         // Your code goes here (widget creation, set result, etc).
        // layout setting
		GridLayout gridBase = new GridLayout();
		gridBase.numColumns = 1;
		gridBase.marginWidth = 10;
		gridBase.marginHeight = 5;
		gridBase.horizontalSpacing = 6;
		gridBase.verticalSpacing = 4;
		shell.setLayout(gridBase);
		GridData gd;
		
 		// version information
 		Label info = new Label(shell, SWT.NONE);
 		info.setText("version information\ncopy right info\nauthor info.");
 		info.setAlignment(SWT.CENTER);
		gd = new GridData(GridData.CENTER, GridData.CENTER, true, true);
		info.setLayoutData(gd);
 		
 		// basic buttons
         Button btnOK = new Button(shell, SWT.PUSH);
         btnOK.setText("OK");
		gd = new GridData(80, SWT.DEFAULT);
		gd.horizontalAlignment = GridData.CENTER;
		btnOK.setLayoutData(gd);
 		btnOK.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				// pushed OK button
 				result = true;
 				shell.close();
 			}
 		});
         // my code ends
 		
         shell.open();
         Display display = parent.getDisplay();
         while (!shell.isDisposed()) {
                 if (!display.readAndDispatch()) display.sleep();
         }
         return result;
     }
 }
