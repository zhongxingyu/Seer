 package org.exist.eclipse.internal.wizards;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.exist.eclipse.IConnection;
 import org.exist.eclipse.internal.BasePlugin;
 import org.exist.eclipse.internal.ConnectionBox;
 import org.exist.eclipse.internal.RemoteConnection;
 
 /**
  * With this wizard you can add a new or change an existing remote connection.
  * The user can define the hostname, the port, the username and the password.
  * Hostname, port and username are mandatory fields.
  * 
  * @author Pascal Schmidiger
  */
 public class RemoteConnectionWizardPage extends WizardPage {
 	private static final String DEFAULT_USER = "admin";
 	private static final String DEFAULT_PASSWORD = "";
 	private static final String DEFAULT_URL = "xmldb:exist://localhost:8080/exist/xmlrpc";
 	private static final String DEFAULT_NAME = "localhost";
 
 	private Text _name;
 	private Text _username;
 	private Text _password;
 	private Text _uri;
 	private IConnection _connection;
 	private boolean _copy;
 
 	/**
 	 * Constructor for SampleNewWizardPage.
 	 * 
 	 * @param selection
 	 */
 	public RemoteConnectionWizardPage() {
 		super("remoteconnectionwizardPage");
 		setImageDescriptor(BasePlugin
 				.getImageDescriptor("icons/hslu_exist_eclipse_logo.jpg"));
 		_copy = true;
 	}
 
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 3;
 
 		// name
 		Label nameLabel = new Label(container, SWT.NULL);
 		nameLabel.setText("&Name:");
 		_name = new Text(container, SWT.BORDER | SWT.SINGLE);
 		if (_connection != null) {
 			_name.setText(_connection.getName());
 		} else {
 			_name.setText(DEFAULT_NAME);
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
 
 		// url
 		Label urlLabel = new Label(container, SWT.NULL);
 		urlLabel.setText("&URL:");
 		_uri = new Text(container, SWT.BORDER | SWT.SINGLE);
 		if (_connection != null) {
 			_uri.setText(_connection.getPath());
 		} else {
 			_uri.setText(DEFAULT_URL);
 		}
 
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
 		gd.horizontalSpan = 2;
 		_uri.setLayoutData(gd);
 		_uri.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		setControl(container);
 		setPageComplete(false);
 		dialogChanged();
 	}
 
 	protected IConnection getConnection() {
 		return new RemoteConnection(getConnectionName(), getUserName(),
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
 		return _name.getText().trim();
 	}
 
 	private String getUri() {
 		return _uri.getText().trim();
 	}
 
 	private String getUserName() {
 		return _username.getText().trim();
 	}
 
 	private String getPassword() {
 		return _password.getText().trim();
 	}
 
 	/**
 	 * Ensures that all text fields are set. Enable and disable fields according
 	 * to the selection of the database type.
 	 */
 	private void dialogChanged() {
 		if (getConnectionName().length() == 0) {
 			setErrorState("Name must be specified");
 		} else if (!ConnectionBox.getInstance().isUnique(getConnectionName())) {
			if (!(!_copy && _connection != null && getConnectionName()
					.equals(_connection.getName()))) {
 				setErrorState("There exists a connection with the same name.");
			}else{
				setErrorState(null);
 			}
 		} else if (getUserName().length() == 0) {
 			setErrorState("Username must be specified");
 		} else if (getUri().length() == 0) {
 			setErrorState("URL must be specified");
 		} else {
 			setErrorState(null);
 		}
 	}
 
 	private void setErrorState(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 }
