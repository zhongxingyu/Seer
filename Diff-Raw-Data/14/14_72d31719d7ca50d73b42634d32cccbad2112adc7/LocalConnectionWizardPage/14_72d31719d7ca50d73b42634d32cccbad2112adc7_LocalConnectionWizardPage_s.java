 package org.exist.eclipse.internal.wizards;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.exist.eclipse.IConnection;
 import org.exist.eclipse.internal.BasePlugin;
 import org.exist.eclipse.internal.ConnectionBox;
 import org.exist.eclipse.internal.LocalConnection;
 
 /**
  * With this wizard you can add a new or change an existing local connection.
  * The user can define the configuration file, the username and the password.
  * Configuration file and username are mandatory fields.
  * 
  * @author Pascal Schmidiger
  */
 public class LocalConnectionWizardPage extends WizardPage {
 	private static final String DEFAULT_USER = "admin";
 	private static final String DEFAULT_PASSWORD = "";
 
 	private Text _name;
 	private Text _username;
 	private Text _password;
 	private Text _config;
 	private Button _browseBtn;
 	private IConnection _connection;
 	private boolean _copy;
 
 	/**
 	 * Constructor for SampleNewWizardPage.
 	 */
 	public LocalConnectionWizardPage() {
 		super("localconnectionwizardpage");
 		setImageDescriptor(BasePlugin
 				.getImageDescriptor("icons/hslu_exist_eclipse_logo.jpg"));
 		_copy = true;
 	}
 
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 3;
 
 		// username
 		Label nameLabel = new Label(container, SWT.NULL);
 		nameLabel.setText("&Name:");
 		_name = new Text(container, SWT.BORDER | SWT.SINGLE);
 		if (_connection != null) {
 			_name.setText(_connection.getName());
 		}
 
 		// username
 		Label userLabel = new Label(container, SWT.NULL);
 		userLabel.setText("&Username:");
 		_username = new Text(container, SWT.BORDER | SWT.SINGLE);
 		if (_connection != null) {
 			_username.setText(_connection.getUsername());
 		} else {
 			_username.setText(DEFAULT_USER);
 		}
 
 		// password
 		Label passwordLabel = new Label(container, SWT.NULL);
 		passwordLabel.setText("&Password:");
 		_password = new Text(container, SWT.BORDER | SWT.SINGLE);
 		_password.setEchoChar('*');
 		if (_connection != null) {
 			_password.setText(_connection.getPassword());
 		} else {
 			_password.setText(DEFAULT_PASSWORD);
 		}
 
 		// configuration
 		Label configLabel = new Label(container, SWT.NULL);
 		configLabel.setText("&Configuration:");
 		_config = new Text(container, SWT.BORDER | SWT.SINGLE);
 		if (_connection != null) {
 			_config.setText(_connection.getPath());
 		}
 		_browseBtn = new Button(container, SWT.PUSH);
 		_browseBtn.setText("Browse");
 
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		_name.setLayoutData(gd);
 		_name.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		_username.setLayoutData(gd);
 		_username.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		_password.setLayoutData(gd);
 		_password.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 1;
 		_config.setLayoutData(gd);
 		_config.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		gd = new GridData(GridData.END);
 		gd.horizontalSpan = 1;
 		_browseBtn.setLayoutData(gd);
 		_browseBtn.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				_config.setText(selectConfigFile());
 			}
 		});
 
 		setControl(container);
 		setPageComplete(false);
 		dialogChanged();
 	}
 
 	protected IConnection getConnection() {
 		return new LocalConnection(getConnectionName(), getUserName(),
 				getPassword(), getUri());
 	}
 
 	protected void setConnection(IConnection connection, boolean copy) {
 		_connection = connection;
 		_copy = copy;
 	}
 
 	// //////////////////////////////////////////////////////////////////////////
 	// private methods
 	// //////////////////////////////////////////////////////////////////////////
 	private String getConnectionName() {
 		return _name.getText();
 	}
 
 	private String getUri() {
 		return _config.getText();
 	}
 
 	private String getUserName() {
 		return _username.getText();
 	}
 
 	private String getPassword() {
 		return _password.getText();
 	}
 
 	/**
 	 * Opens a FileDialog so that the configuration file can be selected.
 	 * Returns the path of the selected file. The return-value is empty in case
 	 * the selection is not valid.
 	 * 
 	 * @return path of the selected file
 	 */
 	private String selectConfigFile() {
 		Shell shell = new Shell();
 		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 		dialog.setText("Select a Configuration File");
 		String[] filterExtensions = { "*.xml", "*" };
 		String[] filterNames = { "XML Files", "All Files" };
 		dialog.setFilterExtensions(filterExtensions);
 		dialog.setFilterNames(filterNames);
 		String filename = dialog.open();
 
 		if (filename != null) {
 			return filename;
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * Ensures that all text fields are set. Enable and disable fields according
 	 * to the selection of the database type.
 	 */
 	private void dialogChanged() {
 		if (getConnectionName().length() < 1) {
 			setErrorState("Name must be specified");
 		} else if (!ConnectionBox.getInstance().isUnique(getConnectionName())) {
 			if (!(!_copy && _connection != null && _connection.getName()
 					.equals(getConnectionName()))) {
 				setErrorState("There exists a connection with the same name.");
 			}
 		} else if (getUserName().length() < 1) {
 			setErrorState("Username must be specified");
 		} else if (getUri().length() < 1) {
 			setErrorState("Configuration File must be specified");
 		} else {
 			setErrorState(null);
 		}
 	}
 
 	private void setErrorState(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 }
