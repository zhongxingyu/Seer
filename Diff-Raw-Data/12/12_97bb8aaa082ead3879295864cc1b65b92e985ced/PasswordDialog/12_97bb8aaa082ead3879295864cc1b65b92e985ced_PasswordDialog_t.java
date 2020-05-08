 package org.certificatesmanager.guicomponents;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 

/**
 * This class represent a Dialog box with only a password field and an Ok Button.
 * @author Ivan Gualandri
 *
 */
 public class PasswordDialog extends Dialog {
 	
 	private Text passwordField;
     private String passwordString;
 
 
 	public PasswordDialog(Shell parent) {
 		super(parent);		
 	}
 	
 	@Override
     protected Control createDialogArea(Composite parent) {
         Composite comp = (Composite) super.createDialogArea(parent);
 
         GridLayout layout = (GridLayout) comp.getLayout();
         layout.numColumns = 2;
 
         Label passwordLabel = new Label(comp, SWT.RIGHT);
         passwordLabel.setText("Password: ");
         passwordField = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
 
         GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
         passwordField.setLayoutData(data);
 
         return comp;
     }
 	
 	@Override
 	protected void configureShell(Shell newShell)
 	{
 		super.configureShell(newShell);
 	    newShell.setText("Please enter password");
 	}
 	
     @Override
     protected void okPressed()
     {
         passwordString = passwordField.getText();
         super.okPressed();
     }
 
     @Override
     protected void cancelPressed()
     {
         passwordField.setText("");
         super.cancelPressed();
     }
 
     public String getPassword()
     {
         return passwordString;
     }
 
 }
