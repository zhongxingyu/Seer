 package edu.uci.lighthouse.core.preferences;
 
 import java.sql.SQLException;
 import java.util.Properties;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import edu.uci.lighthouse.core.Activator;
 import edu.uci.lighthouse.core.util.SSHTunnel;
 import edu.uci.lighthouse.model.jpa.JPAUtility;
 
 public class DatabasePreferences extends PreferencePage implements
 IWorkbenchPreferencePage{
 
 	private Text dbHost;
 	private Text dbUsername;
 	private Text dbPassword;
 	private Text dbDatabase;
 	private Text dbPort;
 	
 	private Button chkTunnel;
 	
 	private Text sshHost;
 	private Text sshUser;
 	private Text sshPassword;
 	private Text sshPort;
 	
 	private static final String ROOT = "edu.uci.lighthouse.core.preferences";
 	
 	public static final String DB_HOST = ROOT+ ".dbHost";
 	public static final String DB_USERNAME = ROOT+ ".dbUsername";
 	public static final String DB_PASSWD = ROOT+ ".dbPassword";
 	public static final String DB_DATABASE = ROOT+ ".dbDatabase";
 	public static final String DB_PORT = ROOT+ ".dbPort";
 	
 	public static final String USES_TUNNEL = ROOT+ ".sshTunnel";
 	
 	public static final String SSH_HOST = ROOT+ ".sshHost";
 	public static final String SSH_USERNAME = ROOT+ ".sshUser";
 	public static final String SSH_PASSWD = ROOT+ ".sshPassword";
 	public static final String SSH_PORT = ROOT+ ".sshPort";
 	
 	@Override
 	public void init(IWorkbench workbench) {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		setPreferenceStore(store);
 		setDescription("Enter MySQL connection details below:");
 	}
 	
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout());
 		
 		Composite dbComposite = getDBComposite(composite);
 		dbComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		chkTunnel = new Button(composite, SWT.CHECK);
 		chkTunnel.setText("Connect using SSH tunnel");
 //		chkTunnel.setEnabled(false);
 		
 		Composite sshComposite = getSSHComposite(composite);
 		sshComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		performLoad();
 		
 		return composite;
 	}
 	
 	@Override
 	protected void performApply() {
 		// Need to store the preferences before testing connect.
 		super.performApply();
 		
 		try {
 			SSHTunnel tunnel = null;
 			if (isConnectingUsingSSH()) {
 				tunnel = new SSHTunnel(getAllSettings());
 				tunnel.setLocalPort(12346);
 				tunnel.start(null);
 			}
 			JPAUtility.canConnect(getDatabaseSettings());
 			if (tunnel != null) {
 				tunnel.stop(null);
 			}
 			MessageDialog.openInformation(getShell(), "Database Connection", "The connection is working properly!");
 		} catch (Exception e) {
 			MessageDialog.openError(getShell(), "Database Connection", e.getMessage());
 		}
 	}
 
 	@Override
 	public boolean performOk() {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		
 		store.setValue(DB_HOST, dbHost.getText());
 		store.setValue(DB_USERNAME, dbUsername.getText());
 		store.setValue(DB_PASSWD, dbPassword.getText());
 		store.setValue(DB_DATABASE, dbDatabase.getText());
 		store.setValue(DB_PORT, dbPort.getText());
 		
 		store.setValue(USES_TUNNEL, chkTunnel.getSelection());
 
 		store.setValue(SSH_HOST, sshHost.getText());
 		store.setValue(SSH_USERNAME, sshUser.getText());
 		store.setValue(SSH_PASSWD, sshPassword.getText());
 		store.setValue(SSH_PORT, sshPort.getText());
 		
 		
 		return super.performOk();
 	}
 	
 	protected void performLoad() {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		
 		dbHost.setText(store.getString(DB_HOST));
 		dbUsername.setText(store.getString(DB_USERNAME));
 		dbPassword.setText(store.getString(DB_PASSWD));
 		dbDatabase.setText(store.getString(DB_DATABASE));
 		dbPort.setText(store.getString(DB_PORT));
 				
 		chkTunnel.setSelection(store.getBoolean(USES_TUNNEL));
 
 		sshHost.setText(store.getString(SSH_HOST));
 		sshUser.setText(store.getString(SSH_USERNAME));
 		sshPassword.setText(store.getString(SSH_PASSWD));
 		sshPort.setText(store.getString(SSH_PORT));
 	}
 	
 	@Override
 	protected void performDefaults() {
 
 		dbHost.setText("127.0.0.1");
 		dbUsername.setText("lighthouse");
 		dbPassword.setText("light99");
 		dbDatabase.setText("lighthouse");
 		dbPort.setText("3306");
 		
 		chkTunnel.setSelection(true);
 
 		sshHost.setText("calico.ics.uci.edu");
 		sshUser.setText("lighthouse");
 		sshPassword.setText("light99");
 		sshPort.setText("22");
 		
 		super.performDefaults();
 	}
 
 	private Composite getDBComposite(Composite parent){
 		Group group = new Group(parent,SWT.NONE);
 		group.setLayout(new GridLayout(2,false));
 		group.setText("Database Settings");
 		
 		Label label;
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Host:");
 		dbHost = new Text(group, SWT.SINGLE | SWT.BORDER);
 		dbHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Username:");
 		dbUsername = new Text(group, SWT.SINGLE | SWT.BORDER);
 		dbUsername.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Password:");
 		dbPassword = new Text(group, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
 		dbPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		label = new Label(group, SWT.NONE);
		label.setText("Dabase:");
 		dbDatabase = new Text(group, SWT.SINGLE | SWT.BORDER);
 		dbDatabase.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Port:");
 		dbPort = new Text(group, SWT.SINGLE | SWT.BORDER);
 		dbPort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		return group;
 	}
 	
 	private Composite getSSHComposite(Composite parent){
 		Group group = new Group(parent,SWT.NONE);
 		group.setLayout(new GridLayout(2,false));
 		group.setText("SSH Tunnel Settings");
 		
 		Label label;
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Host:");
 		label.setEnabled(false);
 		sshHost = new Text(group, SWT.SINGLE | SWT.BORDER);
 		sshHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 //		sshHost.setEnabled(false);
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Username:");
 		label.setEnabled(false);
 		sshUser = new Text(group, SWT.SINGLE | SWT.BORDER);
 		sshUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 //		sshUser.setEnabled(false);
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Password:");
 		label.setEnabled(false);
 		sshPassword = new Text(group, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
 		sshPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 //		sshPassword.setEnabled(false);
 		
 		label = new Label(group, SWT.NONE);
 		label.setText("Port:");
 		label.setEnabled(false);
 		sshPort =new Text(group, SWT.SINGLE | SWT.BORDER);
 		sshPort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 //		sshPort.setEnabled(false);
 		
 		return group;
 	}
 	
 	public static Properties getDatabaseSettings(){
 		Properties dbSettings = new Properties();
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		
 		String connUrl = "jdbc:mysql://"+store.getString(DB_HOST)+":"+store.getString(DB_PORT)+"/"+store.getString(DB_DATABASE);
 		dbSettings.setProperty("hibernate.connection.url", connUrl);
 		dbSettings.setProperty("hibernate.connection.username", store.getString(DB_USERNAME));
 		dbSettings.setProperty("hibernate.connection.password", store.getString(DB_PASSWD));
 		dbSettings.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
 		
 		return dbSettings;
 	}
 	
 	public static Properties getAllSettings(){
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		Properties settings = new Properties();
 		settings.setProperty(DB_HOST, store.getString(DB_HOST));
 		settings.setProperty(DB_PORT, store.getString(DB_PORT));
 		settings.setProperty(DB_DATABASE, store.getString(DB_DATABASE));
 		settings.setProperty(DB_USERNAME, store.getString(DB_USERNAME));
 		settings.setProperty(DB_PASSWD, store.getString(DB_PASSWD));
 		settings.putAll(getSSHTunnelSettings());
 		return settings;
 	}
 	
 	public static Properties getSSHTunnelSettings(){
 		Properties sshTunnelSettings = new Properties();
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		
 		sshTunnelSettings.setProperty(SSH_HOST, store.getString(SSH_HOST));
 		sshTunnelSettings.setProperty(SSH_USERNAME, store.getString(SSH_USERNAME));
 		sshTunnelSettings.setProperty(SSH_PASSWD, store.getString(SSH_PASSWD));
 		sshTunnelSettings.setProperty(SSH_PORT, store.getString(SSH_PORT));
 		
 		return sshTunnelSettings;
 	}
 	
 	public static boolean isConnectingUsingSSH(){
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		return store.getBoolean(USES_TUNNEL);
 	}
 	
 	public static void clear() {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 
 		store.setValue(DB_HOST, "");
 		store.setValue(DB_USERNAME, "");
 		store.setValue(DB_PASSWD, "");
 		store.setValue(DB_DATABASE, "");
 		store.setValue(DB_PORT, "");
 
 		store.setValue(USES_TUNNEL, "");
 
 		store.setValue(SSH_HOST, "");
 		store.setValue(SSH_USERNAME, "");
 		store.setValue(SSH_PASSWD, "");
 		store.setValue(SSH_PORT, "");
 	}
 }
