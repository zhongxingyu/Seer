 package org.mule.tooling.devkit.wizards;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.mule.tooling.devkit.common.ConnectorMavenModel;
 import org.mule.tooling.devkit.common.DevkitUtils;
 import org.mule.tooling.ui.MuleUiConstants;
 import org.mule.tooling.ui.utils.UiUtils;
 
 public class NewDevkitProjectWizardPageAdvance extends WizardPage {
 
 	private static String DEFAULT_USER = "mulesoft";
 
 	private ConnectorMavenModel connectorModel;
 
 	protected NewDevkitProjectWizardPageAdvance(
 			ConnectorMavenModel connectorModel) {
 		super("Advanced Options");
 		setTitle("New Anypoint Connector Project");
 		setDescription("Advanced configuration");
 		this.connectorModel = connectorModel;
 	}
 
 	private Text owner;
 	private Text connection;
 	private Text devConnection;
 	private Text url;
 	private Button manuallyEditCheckBox;
 	private Button addGitHubInfo;
 
 	private Button checkBoxMetadata;
 	private Button checkBoxOAuth;
 	private Button checkBoxQuery;
 
 	private String devkitVersion;
 	
 	@Override
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 
 		Group connectorGroupBox = UiUtils.createGroupWithTitle(container,
 				"Advanced options", 2);
 		checkBoxMetadata = initializeCheckBox(connectorGroupBox,
 				"Enable DataSense", null);
 		checkBoxOAuth = initializeCheckBox(connectorGroupBox,
 				"OAuth authentication", null);
 		checkBoxQuery = initializeCheckBox(connectorGroupBox,
				"Add DataSense Query method", null);
 
 		Group gitHubGroupBox = UiUtils.createGroupWithTitle(container,
 				"GitHub", 2);
 		addGitHubInfo = initializeCheckBox(gitHubGroupBox,
 				"Add GitHub information", new SelectionListener() {
 
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						updateAllComponentsEnablement();
 					}
 
 					@Override
 					public void widgetDefaultSelected(SelectionEvent e) {
 						updateAllComponentsEnablement();
 					}
 				});
 
 		owner = initializeTextField(gitHubGroupBox, "GitHub Owner",
 				DEFAULT_USER, new ModifyListener() {
 
 					public void modifyText(ModifyEvent e) {
 						if (owner.getText().isEmpty()) {
 							owner.setText(DEFAULT_USER);
 						}
 						refresh();
 					}
 				});
 		manuallyEditCheckBox = initializeCheckBox(gitHubGroupBox,
				"Manually set values", new SelectionListener() {
 
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						updateComponentsEnablement();
 					}
 
 					@Override
 					public void widgetDefaultSelected(SelectionEvent e) {
 						updateComponentsEnablement();
 					}
 
 				});
 
 		connection = initializeTextField(gitHubGroupBox, "Connection",
 				"scm:git:git://github.com:" + DEFAULT_USER + "/"
 						+ connectorModel.getConnectorName().toLowerCase()
 						+ ".git", null);
 		devConnection = initializeTextField(gitHubGroupBox, "Dev. Connection",
 				"scm:git:git@github.com:" + DEFAULT_USER + "/"
 						+ connectorModel.getConnectorName().toLowerCase()
 						+ "-module" + ".git", null);
 		url = initializeTextField(gitHubGroupBox, "Url", "http://github.com/"
 				+ DEFAULT_USER + "/"
 				+ connectorModel.getConnectorName().toLowerCase(), null);
 
 		GridLayoutFactory.fillDefaults().numColumns(1)
 				.extendedMargins(2, 2, 10, 0).margins(0, 0).spacing(0, 0)
 				.applyTo(container);
 		GridDataFactory.fillDefaults().indent(0, 0).applyTo(container);
 
 		manuallyEditCheckBox.setSelection(false);
 		manuallyEditCheckBox.setEnabled(false);
 		setControl(container);
 	}
 
 	private Button initializeCheckBox(Composite parent, String label,
 			SelectionListener listener) {
 		final Button checkboxButton = new Button(parent, SWT.CHECK);
 		checkboxButton.setSelection(false);
 		checkboxButton.setText(" " + label);
 		checkboxButton.setLayoutData(GridDataFactory.swtDefaults().span(2, 1)
 				.create());
 		if (listener != null) {
 			checkboxButton.addSelectionListener(listener);
 		}
 
 		return checkboxButton;
 	}
 
 	private Text initializeTextField(Group groupBox, String labelText,
 			String defaultValue, ModifyListener modifyListener) {
 		Label label = new Label(groupBox, SWT.NULL);
 		label.setText(labelText);
 		label.setLayoutData(GridDataFactory.swtDefaults()
 				.align(SWT.BEGINNING, SWT.CENTER)
 				.hint(MuleUiConstants.LABEL_WIDTH, SWT.DEFAULT).create());
 		Text textField = new Text(groupBox, SWT.BORDER);
 		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		textField.setText(defaultValue);
 		textField.setEnabled(false);
 		textField.setToolTipText("Tool Tip");
 		if (modifyListener != null) {
 			textField.addModifyListener(modifyListener);
 		}
 		return textField;
 	}
 
 	private void updateAllComponentsEnablement() {
 		boolean createPom = addGitHubInfo.getSelection();
 		manuallyEditCheckBox.setEnabled(createPom);
 		owner.setEnabled(createPom);
 	}
 
 	private void updateComponentsEnablement() {
 		boolean createPom = manuallyEditCheckBox.getSelection();
 		connection.setEnabled(createPom);
 		devConnection.setEnabled(createPom);
 		url.setEnabled(createPom);
 	}
 
 	public void refresh() {
 		if(!manuallyEditCheckBox.getSelection()){
 		connection.setText("scm:git:git://github.com:" + owner.getText()
 				+ "/" + connectorModel.getConnectorName().toLowerCase()
 				+ ".git");
 		devConnection.setText("scm:git:git@github.com:" + owner.getText()
 				+ "/" + connectorModel.getConnectorName().toLowerCase()
 				+ "-module" + ".git");
 		url.setText("http://github.com/" + owner.getText() + "/"
 				+ connectorModel.getConnectorName().toLowerCase());
 		}
 	}
 
 	public boolean getAddGitHubInfo() {
 		return this.addGitHubInfo.getSelection();
 	}
 
 	public boolean isMetadaEnabled(){
 		return this.checkBoxMetadata.getSelection();
 	}
 	
 	public boolean isOAuth(){
 		return this.checkBoxOAuth.getSelection();
 	}
 	
 	public boolean hasQuery(){
 		return this.checkBoxQuery.isEnabled() && this.checkBoxQuery.getSelection();
 	}
 	
 	public boolean addGitHubInfo(){
 		return this.addGitHubInfo.getSelection();
 	}
 	
 	public String getConnection(){
 		return this.connection.getText();
 	}
 	
 	public String getDevConnection(){
 		return this.devConnection.getText();
 	}
 	
 	public String getUrl(){
 		return this.url.getText();
 	}
 
 	public String getDevkitVersion() {
 		return devkitVersion;
 	}
 
 	public void setDevkitVersion(String devkitVersion) {
 		this.devkitVersion = devkitVersion;
 		this.checkBoxQuery.setEnabled(devkitVersion.startsWith("3.5.0"));
 	}
 }
