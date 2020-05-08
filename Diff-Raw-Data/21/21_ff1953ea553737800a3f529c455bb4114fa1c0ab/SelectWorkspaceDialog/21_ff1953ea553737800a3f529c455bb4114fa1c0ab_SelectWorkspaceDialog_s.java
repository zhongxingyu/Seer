 package at.photoselector.ui;
 
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import at.photoselector.Settings;
 import at.photoselector.Workspace;
 
 public class SelectWorkspaceDialog extends TitleAreaDialog implements Runnable {
 
 	private Combo workspaceCombo;
 
 	public SelectWorkspaceDialog(Shell parentShell) {
 		super(parentShell);
 	}
 
 	@Override
 	public void create() {
 		super.create();
 
 		setTitle("Select Workspace");
 		setMessage(
 				"PhotoSelector stores your workspace in a database. Choose a database location to use for this session.",
 				IMessageProvider.INFORMATION);
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		parent.setLayout(layout);
 
 		Label label = new Label(parent, SWT.NONE);
 		label.setText("Workspace");
 
 		workspaceCombo = new Combo(parent, SWT.DROP_DOWN | SWT.BORDER);
 
 		Button switchWorkspaceButton = new Button(parent, SWT.PUSH);
 		switchWorkspaceButton.setText("Browse...");
 		switchWorkspaceButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
 				try {
 					workspaceCombo.add(dialog.open(), 0);
 					workspaceCombo.select(0);
 				} catch (NullPointerException e) {
 					// dialog got cancelled
 				}
 			}
 		});
 
 		update();
 
 		return parent;
 	}
 
 	public void update() {
 		workspaceCombo.removeAll();
 		for (String current : Settings.getRecent())
 			workspaceCombo.add(current);
 		workspaceCombo.select(0);
 	}
 
 	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayout(new GridLayout());
		Button okButton = createButton(parent, OK, "OK", true);
		okButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				okPressed();
			}
		});

		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	@Override
 	protected void okPressed() {
 		String path = workspaceCombo.getText();
 		Workspace.open(path);
 		Settings.setRecent(path);
 
 		super.okPressed();
 	}
 
 	@Override
 	public void run() {
 		setBlockOnOpen(true);
 
 		open();
 	}
 }
