 
 
 package org.vpac.grisu.client.view.swing.login;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import org.vpac.grisu.client.model.login.LoginPanelsHolder;
 import org.vpac.grisu.client.view.swing.utils.Utils;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 import org.vpac.grisu.frontend.control.login.LoginException;
 import org.vpac.grisu.frontend.control.login.LoginHelpers;
 import org.vpac.grisu.frontend.control.login.LoginManager;
 import org.vpac.grisu.frontend.control.login.LoginParams;
 import org.vpac.grisu.settings.ClientPropertiesManager;
 import org.vpac.grisu.settings.MyProxyServerParams;
 import org.vpac.helpDesk.model.Person;
 import org.vpac.helpDesk.model.PersonException;
 import org.vpac.security.light.utils.ProxyLightLibraryManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class MyProxyLoginPanel extends JPanel {
 	
 	private JButton myproxyInitButton;
 	private JButton loginButton;
 	private JButton cancelButton;
 	private JPasswordField passwordField;
 	private JTextField myProxyUsernameTextField;
 	private LoginPanelsHolder loginPanelHolder = null;
 
 	public void setParamsHolder(LoginPanelsHolder paramsHolder) {
 		this.loginPanelHolder = paramsHolder;
 	}
 
 	/**
 	 * Create the panel
 	 */
 	public MyProxyLoginPanel() {
 		super();
 		setLayout(new FormLayout(
 			new ColumnSpec[] {
 				ColumnSpec.decode("left:7dlu"),
 				ColumnSpec.decode("65dlu:grow(1.0)"),
 				ColumnSpec.decode("0dlu"),
 				ColumnSpec.decode("42dlu"),
 				ColumnSpec.decode("left:10dlu"),
 				FormFactory.DEFAULT_COLSPEC,
 				ColumnSpec.decode("left:6dlu")},
 			new RowSpec[] {
 				RowSpec.decode("top:9dlu"),
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("top:9dlu")}));
 
 		final JLabel pleaseProvideYourLabel = new JLabel();
 		pleaseProvideYourLabel.setText("MyProxy username:");
 		add(pleaseProvideYourLabel, new CellConstraints(2, 2, 6, 1));
 
 		myProxyUsernameTextField = new JTextField();
 		String defaultUserName = MyProxyServerParams.loadDefaultMyProxyUsername();
 		if ( defaultUserName != null && ! "".equals(defaultUserName) ) {
 			myProxyUsernameTextField.setText(defaultUserName);
 		}
 		
 		add(myProxyUsernameTextField, new CellConstraints(2, 4, 5, 1));
 
 		final JLabel myproxyPasswordLabel = new JLabel();
 		myproxyPasswordLabel.setText("MyProxy password:");
 		add(myproxyPasswordLabel, new CellConstraints(2, 6, 6, 1));
 
 		passwordField = new JPasswordField();
 		passwordField.addKeyListener(new KeyAdapter() {
 			public void keyPressed(final KeyEvent e) {
 				
 				if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
 					login();
 				}
 				
 			}
 		});
 		add(passwordField, new CellConstraints(2, 8, 5, 1));
 		add(getCancelButton(), new CellConstraints(4, 11, CellConstraints.RIGHT, CellConstraints.DEFAULT));
 		add(getLoginButton(), new CellConstraints(6, 11, CellConstraints.RIGHT, CellConstraints.DEFAULT));
 		if ( ProxyLightLibraryManager.prerequisitesForProxyCreationAvailable() ) { 
 			add(getMyproxyInitButton(), new CellConstraints(2, 11, CellConstraints.LEFT, CellConstraints.DEFAULT));
 		}
 		//
 	}
 	protected JButton getCancelButton() {
 		if (cancelButton == null) {
 			cancelButton = new JButton();
 			cancelButton.setVisible(false);
 			cancelButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					
 					loginPanelHolder.cancelled();
 					
 				}
 			});
 			cancelButton.setText("Cancel");
 		}
 		return cancelButton;
 	}
 	
 	private void login() {
 		
 		getLoginButton().setEnabled(false);
 		
 		ServiceInterface serviceInterface = null;
 		LoginParams params = loginPanelHolder.getLoginParams();
 		params.setMyProxyUsername(myProxyUsernameTextField.getText());
 		params.setMyProxyPassphrase(passwordField.getPassword());
 		
 		try {
 			serviceInterface = LoginManager.login(null, null, null, null, params);
 		} catch (LoginException e1) {
 			Utils.showErrorMessage(getUser(), MyProxyLoginPanel.this, "loginError", e1);
 			getLoginButton().setEnabled(true);
 			return;
		} catch (IOException e) {
 			Utils.showErrorMessage(getUser(), MyProxyLoginPanel.this, "pluginError", e);
 			getLoginButton().setEnabled(true);
 			return;
 		}
 		
 //		try {
 //			// username & password already in the header
 //			// calling this method only to check whether the backand can get a proxy
 //			// with them
 //			serviceInterface.login(myProxyUsernameTextField.getText(), passwordField.getPassword());
 //		} catch (Exception e2) {
 //			Utils.showErrorMessage(getUser(), MyProxyLoginPanel.this, "wrongMyProxyPassphrase", e2);
 //			getLoginButton().setEnabled(true);
 //			return;
 //		}
 		
 		if ( serviceInterface != null ) {
 				MyProxyServerParams.saveDefaultMyProxyUsername(params.getMyProxyUsername());
 				loginPanelHolder.loggedIn(serviceInterface);
 		} else {
 			Utils.showErrorMessage(getUser(), MyProxyLoginPanel.this, "unknownLoginError", null);
 		}
 		getLoginButton().setEnabled(true);
 
 	}
 	
 	protected JButton getLoginButton() {
 		if (loginButton == null) {
 			loginButton = new JButton();
 			loginButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					login();
 				}
 			});
 			loginButton.setText("Login");
 		}
 		return loginButton;
 	}
 	
 	private Person getUser() {
 		
 		String username = myProxyUsernameTextField.getText();
 		if ( username == null || "".equals(username) ) {
 			username = "anonymous";
 		}
 		Person user = null;
 		try {
 			user = new Person(ClientPropertiesManager.getClientConfiguration(), username+" Grisu myproxylogin user");
 		} catch (Exception e) {
 			try {
 				user = new Person("Anonymous");
 			} catch (PersonException e1) {
 				// this should never happen
 				e1.printStackTrace();
 			}
 		}
 		user.setRole(Person.USER_ROLE);
 		user.setNickname(username);
 		return user;
 	}
 	
 	protected JButton getMyproxyInitButton() {
 		if (myproxyInitButton == null) {
 			myproxyInitButton = new JButton();
 			myproxyInitButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					String myproxyServer = MyProxyServerParams.getMyProxyServer();
 					int myproxyPort = MyProxyServerParams.getMyProxyPort();
 					int lifetime_in_seconds = -1; // to allow the user to specify the time
 					String allowed_retrievers = null;
 					String allowed_renewers = null;
 					Map<String, char[]> loginInfo = ProxyLightLibraryManager.createMyProxyInitDialog(myproxyServer, myproxyPort, lifetime_in_seconds, allowed_retrievers, allowed_renewers);
 					if ( loginInfo != null && loginInfo.size() == 1 ) {
 						String key = loginInfo.keySet().iterator().next();
 						myProxyUsernameTextField.setText(key);
 						passwordField.setText(new String((loginInfo.get(key))));
 					}
 				}
 			});
 			myproxyInitButton.setText("MyProxy init");
 		}
 		return myproxyInitButton;
 	}
 
 }
