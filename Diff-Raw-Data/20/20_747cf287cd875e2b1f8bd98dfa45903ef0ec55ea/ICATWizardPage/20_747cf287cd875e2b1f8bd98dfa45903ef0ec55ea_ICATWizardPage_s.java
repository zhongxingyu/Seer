 
 package uk.ac.diamond.scisoft.icatexplorer.rcp.wizards;
 
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 
 public class ICATWizardPage extends WizardPage implements KeyListener {
 
 	private Text txtDirectory;
 	private Text txtProject;
 	private Text txtFedid;
 	private Text txtPassword;
 	private Combo icatdbCombo;
 	private final String initProject;
 	private final String initDirectory;
 	//private final String initFolder;
 	//private final String initIcatdb;
 	private final String initFedid;
 	private final String initPassword;
 	
 
 	/**
 	 * Constructor for SampleNewWizardPage.
 	 * 
 	 * @param prevDirectory
 	 * @param prevFolder
 	 * @param prevProject
 	 */
 	public ICATWizardPage(@SuppressWarnings("unused") ISelection selection, String prevProject,
 			String prevFolder, String prevDirectory, String prevFedid, String prevPassword) {
 		super("ICATWizardPage"); 
 				
		this.initProject = prevProject != null ? prevProject : "ICAT"; 
 		//this.initFolder = prevFolder != null ? prevFolder : "myIcat"; 
 		this.initDirectory = prevDirectory != null ? prevDirectory : ""; 
 		this.initFedid = prevFedid != null ? prevFedid : ""; 
 		this.initPassword = prevPassword != null ? prevPassword : ""; 
 		//this.initIcatdb = prevIcatdb != null ? prevIcatdb : ""; 
 		setTitle("ICAT Project Wizard - creates a connection to an ICAT database"); 
 		setDescription("Wizard to create an ICAT connection to browse datafiles"); 
 	}
 
 	/**
 	 * @see IDialogPage#createControl(Composite)
 	 */
 	@SuppressWarnings("unused")
 	@Override
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		container.setLocation(-708, 1);
 		container.setLayout(null);
 		Label lblProjectName = new Label(container, SWT.NULL);
		lblProjectName.setBounds(5, 136, 38, 13);
 		lblProjectName.setText("&Project:"); 
 		txtProject = new Text(container, SWT.BORDER);
 		txtProject.setBounds(113, 133, 321, 19);
 		txtProject.setText(initProject);
 		txtProject.addKeyListener(this);
 		Composite composite = new Composite(container, SWT.NULL);
 		composite.setBounds(708, 37, 64, 64);
 		Composite composite_1 = new Composite(container, SWT.NULL);
 		composite_1.setBounds(708, 108, 64, 64);
 
 		Label lbldownloadDirectory = new Label(container, SWT.NULL);
		lbldownloadDirectory.setBounds(5, 171, 102, 13);
 		lbldownloadDirectory.setText("&Download directory:"); 
 		txtDirectory = new Text(container, SWT.BORDER);
		txtDirectory.setBounds(113, 168, 321, 19);
 		txtDirectory.setText(initDirectory);
 		txtDirectory.setEditable(true);
 		txtDirectory.setEnabled(true);
 
 		Button button = new Button(container, SWT.PUSH);
		button.setBounds(454, 166, 59, 23);
 		button.setText("Browse..."); 
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleBrowse();
 			}
 		});
 				
 		Label lblicatDatabase = new Label(container, SWT.NONE);
 		lblicatDatabase.setText("&ICAT Database:"); 
 		lblicatDatabase.setBounds(5, 23, 103, 13);
 		
 		icatdbCombo = new Combo(container, SWT.READ_ONLY);//non-editable
 		icatdbCombo.setItems(new String[] {"Diamond Light Source Ltd.", "ISIS"});  //$NON-NLS-2$
		icatdbCombo.setBounds(113, 20, 186, 21);
 		icatdbCombo.select(0);
 		
 		Label fedidLbl = new Label(container, SWT.NONE);
 		fedidLbl.setBounds(5, 68, 49, 13);
 		fedidLbl.setText("&FedId:"); 
 		
 		Label passwordLbl = new Label(container, SWT.NONE);
		passwordLbl.setBounds(5, 98, 49, 13);
 		passwordLbl.setText("&Password:"); 
 		
 		txtFedid = new Text(container, SWT.BORDER);
 		txtFedid.setText(initFedid);
 		txtFedid.setBounds(113, 65, 153, 19);
 		txtFedid.addKeyListener(this);
 
 		
 		txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
 		//txtPassword.setText(initPassword);
 		txtPassword.setBounds(113, 95, 153, 19);
 		txtPassword.addKeyListener(this);
 		
 		dialogChanged();
 		setControl(container);
 
 	}
 
 	/**
 	 * Uses the standard container selection dialog to choose the new value for the container field.
 	 */
 
 	private void handleBrowse() {
 		DirectoryDialog dirDialog = new DirectoryDialog(getShell(), SWT.OPEN);
 		dirDialog.setFilterPath(getDirectory());
 		final String filepath = dirDialog.open();
 		if (filepath != null) {
 			txtDirectory.setText(filepath);
 			dialogChanged();
 		}
 	}
 
 	/**
 	 * Ensures that both text fields are set.
 	 */
 
 	private void dialogChanged() {
 		if (getProject().length() == 0) {
 			updateStatus("Project name must be specified"); 
 			return;
 		}
 //
 //		if (getFolder().length() == 0) {
 //			updateStatus("Folder name must be specified. e.g. data"); 
 //			return;
 //		}
 
 		if (getDirectory().length() == 0) {
 			updateStatus("Directory where to store downloaded data files must be specified."); 
 			return;
 		}
 		
 		if (getFedid().length() == 0) {
 			updateStatus("a fedid must be specified."); 
 			return;
 		}
 		
 		if (getPassword().length() == 0) {
 			updateStatus("a password must be specified."); 
 			return;
 		}
 		
 		updateStatus(null);
 	}
 
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 	
 	public String getIcatdb(){
 		String site = icatdbCombo.getItem(icatdbCombo.getSelectionIndex()).toUpperCase();
 		
 		if(site.contains("diamond".toUpperCase())){
 			
 			return "dls";
 		}else if(site.contains("isis".toUpperCase())){
 			return "isis";
 		}
 		return site;
 	}
 	
 	public String getFedid(){
 		return txtFedid.getText();
 	}
 	
 	public String getPassword(){
 		return txtPassword.getText();
 	}
 	
 	public String getProject() {
 		return txtProject.getText();
 	}
 
 	public String getDirectory() {
 		return txtDirectory.getText();
 	}
 
 //	public String getFolder() {
 //		return txtFolder.getText();
 //	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if (e.getSource().equals(txtProject)) {
 			dialogChanged();
 		}
 //		if (e.getSource().equals(txtFolder)) {
 //			dialogChanged();
 //		}
 		if (e.getSource().equals(txtFedid)) {
 			dialogChanged();
 		}
 		if (e.getSource().equals(txtPassword)) {
 			dialogChanged();
 		}
 	}
 }
