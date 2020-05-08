 package net.mms_projects.copy_it.ui.swt.forms;
 
 import net.mms_projects.copy_it.AndroidResourceLoader;
 import net.mms_projects.copy_it.Messages;
 import net.mms_projects.copy_it.app.CopyItDesktop;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 public class AboutDialog extends Dialog {
 
 	protected Object result;
 	protected Shell shell;
 
 	/**
 	 * Create the dialog.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public AboutDialog(Shell parent, int style) {
 		super(parent, style);
 		setText(Messages.getString("title_activity_about"));
 	}
 
 	/**
 	 * Open the dialog.
 	 * 
 	 * @return the result
 	 */
 	public Object open() {
 		createContents();
 		shell.open();
 		shell.layout();
 		Display display = getParent().getDisplay();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 */
 	private void createContents() {
 		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
 		shell.setSize(300, 221);
 		shell.setText(getText());
 		shell.setLayout(new FormLayout());
 
 		Label appIcon = new Label(shell, SWT.NONE);
 		FormData fd_appIcon = new FormData();
 		fd_appIcon.right = new FormAttachment(0, 199);
 		fd_appIcon.top = new FormAttachment(0, 10);
 		fd_appIcon.left = new FormAttachment(0, 99);
 		appIcon.setLayoutData(fd_appIcon);
 		appIcon.setImage(AndroidResourceLoader
 				.getImage("drawable-xhdpi/ic_launcher.png"));
 		Label appName = new Label(shell, SWT.NONE);
 		FormData fd_appName = new FormData();
 		fd_appName.right = new FormAttachment(0, 283);
 		fd_appName.top = new FormAttachment(0, 116);
 		fd_appName.left = new FormAttachment(0, 10);
 		appName.setLayoutData(fd_appName);
 		appName.setAlignment(SWT.CENTER);
 		Label appVersion = new Label(shell, SWT.NONE);
 		FormData fd_appVersion = new FormData();
 		fd_appVersion.right = new FormAttachment(0, 283);
 		fd_appVersion.top = new FormAttachment(0, 139);
 		fd_appVersion.left = new FormAttachment(0, 10);
 		appVersion.setLayoutData(fd_appVersion);
 		appVersion.setAlignment(SWT.CENTER);
 		Label appCopyright = new Label(shell, SWT.NONE);
 		FormData fd_appCopyright = new FormData();
 		fd_appCopyright.right = new FormAttachment(0, 283);
 		fd_appCopyright.top = new FormAttachment(0, 162);
 		fd_appCopyright.left = new FormAttachment(0, 10);
 		appCopyright.setLayoutData(fd_appCopyright);
 		appCopyright.setAlignment(SWT.CENTER);
 		Button btnClose = new Button(shell, SWT.NONE);
 		FormData fd_btnClose = new FormData();
 		fd_btnClose.right = new FormAttachment(appIcon, 0, SWT.RIGHT);
 		fd_btnClose.top = new FormAttachment(appCopyright, 3);
 		fd_btnClose.left = new FormAttachment(appIcon, 0, SWT.LEFT);
 		btnClose.setLayoutData(fd_btnClose);
 		appName.setText(Messages.getString("app_name"));
 		appVersion.setText(Messages.getString("about_version", CopyItDesktop.getVersion()));
 		appCopyright.setText(Messages.getString("about_copyright"));
		btnClose.setText(Messages.getString("button.close"));
 		btnClose.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				AboutDialog.this.shell.close();
 			}
 		});
 	}
 }
