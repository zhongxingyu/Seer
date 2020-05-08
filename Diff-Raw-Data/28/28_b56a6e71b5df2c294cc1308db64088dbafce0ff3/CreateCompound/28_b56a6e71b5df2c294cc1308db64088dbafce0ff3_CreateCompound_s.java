 package net.bioclipse.brunn.ui.dialogs;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 public class CreateCompound extends TitleAreaDialog {
 
 	private Button browseButton;
 	private Label molceularWeightLabel;
 	private Label structureLabel;
 	private Label nameLabel;
 	private Button keepCreatingButton;
 	private Text textStructure;
 	private Text textMW;
 	private Text textName;
 	
 	private String structureURL = "";
 	private double molecularWeight = -1;
 	private String name = "";
 	
 	private static boolean keepAddingCompounds = false;
 	
 	/**
 	 * Create the dialog
 	 * @param parentShell
 	 */
 	public CreateCompound(Shell parentShell) {
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
 
 		nameLabel = new Label(container, SWT.NONE);
 		final FormData fd_nameLabel = new FormData();
 		fd_nameLabel.bottom = new FormAttachment(0, 47);
 		fd_nameLabel.top = new FormAttachment(0, 27);
 		fd_nameLabel.right = new FormAttachment(0, 149);
 		fd_nameLabel.left = new FormAttachment(0, 104);
 		nameLabel.setLayoutData(fd_nameLabel);
 		nameLabel.setText("Name:");
 
 		structureLabel = new Label(container, SWT.NONE);
 		final FormData fd_structureLabel = new FormData();
 		fd_structureLabel.bottom = new FormAttachment(0, 94);
 		fd_structureLabel.top = new FormAttachment(0, 74);
 		fd_structureLabel.right = new FormAttachment(0, 149);
 		fd_structureLabel.left = new FormAttachment(0, 84);
 		structureLabel.setLayoutData(fd_structureLabel);
 		structureLabel.setText("Structure:");
 
 		molceularWeightLabel = new Label(container, SWT.NONE);
 		final FormData fd_molceularWeightLabel = new FormData();
 		fd_molceularWeightLabel.bottom = new FormAttachment(0, 141);
 		fd_molceularWeightLabel.top = new FormAttachment(0, 121);
 		fd_molceularWeightLabel.right = new FormAttachment(0, 149);
 		fd_molceularWeightLabel.left = new FormAttachment(0, 39);
 		molceularWeightLabel.setLayoutData(fd_molceularWeightLabel);
 		molceularWeightLabel.setText("Molecular weight:");
 
 		textName = new Text(container, SWT.BORDER);
 		final FormData fd_textName = new FormData();
 		fd_textName.bottom = new FormAttachment(0, 47);
 		fd_textName.top = new FormAttachment(0, 22);
 		fd_textName.right = new FormAttachment(0, 277);
 		fd_textName.left = new FormAttachment(0, 155);
 		textName.setLayoutData(fd_textName);
 
 		textMW = new Text(container, SWT.BORDER);
 		final FormData fd_textMW = new FormData();
 		fd_textMW.bottom = new FormAttachment(0, 141);
 		fd_textMW.top = new FormAttachment(0, 116);
 		fd_textMW.right = new FormAttachment(0, 277);
 		fd_textMW.left = new FormAttachment(0, 155);
 		textMW.setLayoutData(fd_textMW);
 
 		textStructure = new Text(container, SWT.BORDER);
 		final FormData fd_textStructure = new FormData();
 		fd_textStructure.bottom = new FormAttachment(0, 94);
 		fd_textStructure.top = new FormAttachment(0, 69);
 		fd_textStructure.right = new FormAttachment(0, 277);
 		fd_textStructure.left = new FormAttachment(0, 155);
 		textStructure.setLayoutData(fd_textStructure);
 
 		browseButton = new Button(container, SWT.NONE);
 		final FormData fd_browseButton = new FormData();
 		fd_browseButton.bottom = new FormAttachment(0, 94);
 		fd_browseButton.top = new FormAttachment(0, 64);
 		fd_browseButton.right = new FormAttachment(0, 353);
 		fd_browseButton.left = new FormAttachment(0, 283);
 		browseButton.setLayoutData(fd_browseButton);
 		browseButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog fd = 
 					new FileDialog( PlatformUI.getWorkbench().getDisplay().getActiveShell() );
 				String s = fd.open();
 				if(s != null) {
 					textStructure.setText(s);
 				}
 			}
 		});
 		browseButton.setText("Browse...");
 
 		keepCreatingButton = new Button(container, SWT.CHECK);
 		final FormData fd_keepCreatingButton = new FormData();
 		fd_keepCreatingButton.top = new FormAttachment(0, 155);
 		fd_keepCreatingButton.left = new FormAttachment(structureLabel, 0, SWT.LEFT);
 		keepCreatingButton.setLayoutData(fd_keepCreatingButton);
 		keepCreatingButton.setText("Keep creating more compounds");
 		keepCreatingButton.setSelection(keepAddingCompounds);
 		container.setTabList(new Control[] {textName, textStructure, textMW, keepCreatingButton, nameLabel, structureLabel, molceularWeightLabel, browseButton});
 		setTitle("Create compound");
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
 		return new Point(383, 353);
 	}
 
 	public double getMolecularWeight() {
 		return molecularWeight;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getStructureURL() {
 		return structureURL;
 	}
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			if(!textMW.getText().equals("")) {
 				try {
 					molecularWeight = Double.parseDouble( textMW.getText() );
 				}
 				catch(NumberFormatException e) {
 					MessageDialog.openInformation(
 							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 							"Info",
 							"Molecular weight must be a double");
 					return;
 				}
 			}
 			name                = textName.getText();
 			structureURL        = textStructure.getText();
 			keepAddingCompounds = keepCreatingButton.getSelection();
 		}
 		super.buttonPressed(buttonId);
 	}
 
 	public static boolean isKeepAddingCompounds() {
 		return keepAddingCompounds;
 	}
 }
