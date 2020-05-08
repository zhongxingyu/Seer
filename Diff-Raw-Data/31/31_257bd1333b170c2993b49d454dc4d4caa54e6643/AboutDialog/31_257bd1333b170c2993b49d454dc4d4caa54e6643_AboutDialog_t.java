 package nz.ac.auckland.netlogin.gui;
 
 import nz.ac.auckland.netlogin.NetLogin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 public class AboutDialog {
 
 	private Shell shell;
 	private String version;
 
 	public AboutDialog(Shell shell) {
 		this.shell = shell;
 		this.version = readVersion();
 	}
 
 	public void open() {
 		final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		dialog.setLayout(SWTHelper.createMinimalGridLayout());
 		dialog.setText("NetLogin - About");
 
		Composite iconMessage = new Composite(dialog, SWT.NONE);
 		GridLayout iconMessageLayout = new GridLayout(2, false);
 		iconMessageLayout.marginTop = 5;
 		iconMessageLayout.marginBottom = 5;
 		iconMessageLayout.marginLeft = 10;
 		iconMessageLayout.marginRight = 10;
 		iconMessageLayout.horizontalSpacing = 10;
 		iconMessage.setLayout(iconMessageLayout);
 
 		Label icon = new Label(iconMessage, SWT.CENTER);
 		icon.setImage(Icons.getInstance().getClosestIcon(32));
 		icon.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
 
 		Label label = new Label(iconMessage, SWT.LEFT);
 		label.setText(getMessage());
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 
 		Composite buttonPanel = SWTHelper.createButtonPanel(dialog);
 		Button closeButton = SWTHelper.createButton(buttonPanel, "Close");
 		dialog.setDefaultButton(closeButton);
 
 		closeButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				dialog.close();
 			}
 		});
 		
 		dialog.addShellListener(new ShellAdapter() {
 			public void shellClosed(ShellEvent e) {
 				dialog.dispose();
 			}
 		});
 
 		dialog.pack();
 		dialog.open();
 	}
 
 	public String getMessage() {
 		StringBuilder message = new StringBuilder();
 
 		message.append("NetLogin Client\n");
 		if (version != null) message.append("Version: ").append(version).append("\n");
         message.append("\n");
 		message.append("Copyright © 2001 The University of Auckland.\n");
         message.append("Released under terms of the GNU GPL.\n");
 
 		return message.toString();
 	}
 
 	public String readVersion() {
         Package pkg = NetLogin.class.getPackage();
         if (pkg == null) return null;
         return pkg.getImplementationVersion();
 	}
 
 }
