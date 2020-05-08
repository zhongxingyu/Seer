 package org.mapsforge.mapmaker.gui;
 
 import java.io.File;
 
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 public class POIWizardPage extends WizardPage {
 	private static final String SETTINGS_SECTION_NAME = "poi";
 	private static final String TITLE = "POI Settings";
 	private String defaultCategoryConfigPath = "POICategoriesOsmosis.xml";
 	private IDialogSettings settings;
 
 	private Text tfCategoryConfigPath;
 	private Text tfOutputFilePath;
 
 	protected POIWizardPage(String pageName, IDialogSettings settings) {
 		super(pageName);
 		setPageComplete(false);
 		setTitle(pageName);
 		setImageDescriptor(ImageDescriptor.createFromFile(null, "logo.png"));
 		this.settings = settings;
 		this.settings.addNewSection(SETTINGS_SECTION_NAME);
 	}
 
 	public static String getSettingsSectionName() {
 		return SETTINGS_SECTION_NAME;
 	}
 
 	static String getStaticTitle() {
 		return TITLE;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		final Composite container = new Composite(parent, SWT.NULL);
 		setControl(container);
 		container.setLayout(new FormLayout());
 
 		// OUTPUT FILE PATH
 		Label lblOuptufFile = new Label(container, SWT.NONE);
 		FormData fd_lblOuptufFile = new FormData();
 		fd_lblOuptufFile.top = new FormAttachment(0);
 		fd_lblOuptufFile.left = new FormAttachment(0);
 		fd_lblOuptufFile.right = new FormAttachment(100);
 		lblOuptufFile.setLayoutData(fd_lblOuptufFile);
 		lblOuptufFile.setText("Output file: ");
 
 		tfOutputFilePath = new Text(container, SWT.BORDER);
 		FormData fd_tfOutputFilePath = new FormData();
 		fd_tfOutputFilePath.top = new FormAttachment(lblOuptufFile, 6);
 		fd_tfOutputFilePath.left = new FormAttachment(0);
 		tfOutputFilePath.setLayoutData(fd_tfOutputFilePath);
 		tfOutputFilePath.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				setOutputFilePath(((Text) e.widget).getText());
 			}
 		});
 
 		Button btnBrowsePOIFile = new Button(container, SWT.PUSH);
 		fd_tfOutputFilePath.right = new FormAttachment(btnBrowsePOIFile, -6);
 		FormData fd_btnBrowsePOIFile = new FormData();
 		fd_btnBrowsePOIFile.top = new FormAttachment(tfOutputFilePath, 0,
 				SWT.CENTER);
 		fd_btnBrowsePOIFile.right = new FormAttachment(100);
 		btnBrowsePOIFile.setLayoutData(fd_btnBrowsePOIFile);
 		btnBrowsePOIFile.setText("...");
 		btnBrowsePOIFile.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog d = new FileDialog(container.getShell());
 				d.setFilterPath(System.getProperty("user.dir"));
 				d.setFilterExtensions(new String[] { "*.poi" });
 				String selection = d.open();
 				setOutputFilePath(selection);
 			}
 
 		});
 
 		// CATEGORY CONFIG FILE
 		Label lblSelectFile = new Label(container, SWT.NONE);
 		FormData fd_lblSelectFile = new FormData();
 		fd_lblSelectFile.top = new FormAttachment(tfOutputFilePath, 6);
 		fd_lblSelectFile.left = new FormAttachment(0);
 		fd_lblSelectFile.right = new FormAttachment(100);
 		lblSelectFile.setLayoutData(fd_lblSelectFile);
 		lblSelectFile.setText("Category Mapping Configuration File:");
 
 		tfCategoryConfigPath = new Text(container, SWT.BORDER);
 		FormData fd_tfCategoryConfigPath = new FormData();
 		fd_tfCategoryConfigPath.top = new FormAttachment(lblSelectFile, 6);
 		fd_tfCategoryConfigPath.left = new FormAttachment(lblSelectFile, 0,
 				SWT.LEFT);
 		tfCategoryConfigPath.setLayoutData(fd_tfCategoryConfigPath);
 		tfCategoryConfigPath.setText(this.defaultCategoryConfigPath);
 		tfCategoryConfigPath.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				setCategoryConfigPath(((Text) e.widget).getText());
 			}
 		});
 
 		Button btnOpenFile = new Button(container, SWT.NONE);
 		fd_tfCategoryConfigPath.right = new FormAttachment(btnOpenFile, -6);
 		FormData fd_btnOpenFile = new FormData();
 		fd_btnOpenFile.top = new FormAttachment(tfCategoryConfigPath, 0,
 				SWT.CENTER);
 		fd_btnOpenFile.right = new FormAttachment(100);
 		btnOpenFile.setLayoutData(fd_btnOpenFile);
 		btnOpenFile.setText("...");
 		btnOpenFile.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog d = new FileDialog(container.getShell());
 				d.setFilterPath(System.getProperty("user.dir"));
 				d.setFilterExtensions(new String[] { "*.xml" });
 				String selection = d.open();
 				setCategoryConfigPath(selection);
 			}
 
 		});
 
 		// Validate default values
 		onInputChanged();
 	}
 
 	protected void setOutputFilePath(String path) {
 		if (path != null && !path.equals(this.tfOutputFilePath.getText())) {
 			this.tfOutputFilePath.setText(path);
 		}
 		onInputChanged();
 	}
 
 	protected void setCategoryConfigPath(String path) {
 		if (path != null && !path.equals(this.tfCategoryConfigPath.getText())) {
 			this.tfCategoryConfigPath.setText(path);
 		}
 		onInputChanged();
 
 	}
 
 	void onInputChanged() {
 		boolean isValid = isPageComplete();
 
 		// Show / hide error message
 		if (isValid) {
 			setErrorMessage(null);
 			updateSettings();
 		}
 
 		setPageComplete(isValid);
 	}
 
 	private void updateSettings() {
 		IDialogSettings section = this.settings
 				.getSection(SETTINGS_SECTION_NAME);
 		section.put("categoryConfigPath", this.tfCategoryConfigPath.getText());
 		// TODO create inputs for output file path
 		section.put("outputFilePath", this.tfOutputFilePath.getText());
 	}
 
 	@Override
 	public boolean isPageComplete() {
 		// Set page valid without any checks if this page is not used
 		if (this.settings.getSection(
				OptionSelectionWizardPage.getSettingsSectionName()).getBoolean(
 				"createPOIs") == false) {
 			return true;
 		}
 
 		boolean isValid = true;
 
 		// Output file may not be empty
 		if (this.tfOutputFilePath.getText().equals("")) {
 			super.setErrorMessage("No output file has been specified.");
 			isValid = false;
 		}
 
 		// Category config file must exist be a file and be writable
 		if (this.tfCategoryConfigPath.getText() != null) {
 			File f = new File(this.tfCategoryConfigPath.getText());
 
 			if (isValid && !f.exists()) {
 				super.setErrorMessage("The given input file does not exist. ("
 						+ f.getAbsolutePath() + ")");
 				isValid = false;
 			}
 
 			if (isValid && !f.isFile()) {
 				super.setErrorMessage("The given input file is not a file.");
 				isValid = false;
 			}
 
 			if (isValid && !f.canRead()) {
 				super.setErrorMessage("Cannot read the specified file.");
 				isValid = false;
 			}
 
 		}
 
 		return isValid;
 	}
 
 	@Override
 	public IWizardPage getNextPage() {
 		return WizardPageManager.getInstance().getNextWizardPage(this);
 
 	}
 
 }
