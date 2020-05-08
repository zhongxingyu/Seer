 package net.bioclipse.brunn.ui.dialogs;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class CreatePatientCell extends TitleAreaDialog {
 
 	private Text lidText;
 	private Text nameText;
 	private String name;
 	private String lid;
 	
 	/**
 	 * Create the dialog
 	 * @param parentShell
 	 */
 	public CreatePatientCell(Shell parentShell) {
 		super(parentShell);
 	}
 
 	/**
 	 * Create contents of the dialog
 	 * @param parent
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite area = (Composite) super.createDialogArea(parent);
 		Composite container = new Composite(area, SWT.NONE);
 		container.setLayout(new FormLayout());
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		final Label label = new Label(container, SWT.NONE);
 		final FormData fd_label = new FormData();
 		fd_label.right = new FormAttachment(0, 109);
 		label.setLayoutData(fd_label);
 		label.setText("Name:");
 
 		nameText = new Text(container, SWT.BORDER);
 		fd_label.bottom = new FormAttachment(nameText, -2, SWT.BOTTOM);
 		final FormData fd_nameText = new FormData();
 		fd_nameText.right = new FormAttachment(100, -119);
 		fd_nameText.top = new FormAttachment(0, 36);
 		fd_nameText.left = new FormAttachment(0, 115);
 		nameText.setLayoutData(fd_nameText);
 
 		lidText = new Text(container, SWT.BORDER);
 		final FormData fd_lid = new FormData();
 		fd_lid.right = new FormAttachment(nameText, 0, SWT.RIGHT);
 		fd_lid.top = new FormAttachment(0, 76);
 		fd_lid.left = new FormAttachment(0, 115);
 		lidText.setLayoutData(fd_lid);
 
 		final Label lidLabel = new Label(container, SWT.NONE);
 		final FormData fd_lidLabel = new FormData();
 		fd_lidLabel.bottom = new FormAttachment(lidText, -2, SWT.BOTTOM);
 		fd_lidLabel.right = new FormAttachment(0, 109);
 		lidLabel.setLayoutData(fd_lidLabel);
 		lidLabel.setText("lid:");
 
 		final Label eg08045Label = new Label(container, SWT.NONE);
 		final FormData fd_eg08045Label = new FormData();
 		fd_eg08045Label.bottom = new FormAttachment(nameText, 0, SWT.BOTTOM);
 		fd_eg08045Label.left = new FormAttachment(nameText, 5, SWT.RIGHT);
 		eg08045Label.setLayoutData(fd_eg08045Label);
		eg08045Label.setText("(e.g. 08/045-B)");
 
 		final Label eg08045Label_1 = new Label(container, SWT.NONE);
 		final FormData fd_eg08045Label_1 = new FormData();
 		fd_eg08045Label_1.bottom = new FormAttachment(lidText, 0, SWT.BOTTOM);
 		fd_eg08045Label_1.left = new FormAttachment(lidText, 5, SWT.RIGHT);
 		eg08045Label_1.setLayoutData(fd_eg08045Label_1);
 		eg08045Label_1.setText("(e.g. 08/045)");
 		setTitle("Create Patient Cell");
 		//
 		return area;
 	}
 
 	/**
 	 * Create contents of the button bar
 	 * @param parent
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
 				true);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 				IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	/**
 	 * Return the initial size of the dialog
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(452, 349);
 	}
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			name = nameText.getText();
 			lid  = lidText.getText();
 		}
 		super.buttonPressed(buttonId);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getLid() {
 		return lid;
 	}
 }
