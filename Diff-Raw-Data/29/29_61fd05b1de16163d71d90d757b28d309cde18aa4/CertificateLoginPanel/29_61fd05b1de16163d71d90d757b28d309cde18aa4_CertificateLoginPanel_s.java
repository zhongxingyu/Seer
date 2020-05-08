 
 
 package org.vpac.grisu.client.view.swing.login;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 
 import org.vpac.grisu.client.model.login.LoginPanelsHolder;
 import org.vpac.grisu.client.view.swing.utils.Utils;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 import org.vpac.grisu.frontend.control.login.LoginException;
 import org.vpac.grisu.frontend.control.login.LoginHelpers;
 import org.vpac.grisu.frontend.control.login.LoginManager;
 import org.vpac.grisu.settings.ClientPropertiesManager;
 import org.vpac.helpDesk.model.Person;
 import org.vpac.helpDesk.model.PersonException;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class CertificateLoginPanel extends JPanel {
 	
 	private JButton button;
 	private JButton loginButton;
 	private JPasswordField passwordField;
 	private LoginPanelsHolder loginPanelHolder = null;
 
 	/**
 	 * Create the panel
 	 */
 	public CertificateLoginPanel() {
 		super();
 		setLayout(new FormLayout(
 			new ColumnSpec[] {
 				ColumnSpec.decode("left:8dlu"),
 				ColumnSpec.decode("left:9dlu"),
 				ColumnSpec.decode("109dlu:grow(1.0)"),
 				ColumnSpec.decode("left:10dlu"),
 				FormFactory.DEFAULT_COLSPEC,
 				ColumnSpec.decode("left:12dlu")},
 			new RowSpec[] {
 				RowSpec.decode("top:11dlu"),
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("top:9dlu"),
 				RowSpec.decode("top:20dlu"),
 				RowSpec.decode("top:11dlu:grow(1.0)")}));
 
 		final JLabel privateKeyPassphraseLabel = new JLabel();
 		privateKeyPassphraseLabel.setText("Private key passphrase:");
 		add(privateKeyPassphraseLabel, new CellConstraints(2, 2, 2, 1));
 		add(getPasswordField(), new CellConstraints(2, 4, 4, 1));
 		add(getLoginButton(), new CellConstraints(5, 6, CellConstraints.RIGHT, CellConstraints.BOTTOM));
 		add(cancelButton(), new CellConstraints(3, 6, CellConstraints.RIGHT, CellConstraints.BOTTOM));
 
 	}
 	protected JPasswordField getPasswordField() {
 		if (passwordField == null) {
 			passwordField = new JPasswordField();
 			passwordField.addKeyListener(new KeyAdapter() {
 				public void keyPressed(final KeyEvent e) {
 					
 					if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
 						login();
 					}
 					
 				}
 			});
 		}
 		return passwordField;
 	}
 	
 	private void login() {
 		
 		getLoginButton().setEnabled(false);
 		ServiceInterface serviceInterface = null;
 		try {
 //			serviceInterface = LoginHelpers.localProxyLogin(getPasswordField().getPassword(), loginPanelHolder.getLoginParams());
 			serviceInterface = LoginManager.login(null, getPasswordField().getPassword(), null, null, loginPanelHolder.getLoginParams());
 		} catch (LoginException e1) {
 			Utils.showErrorMessage(getUser(), CertificateLoginPanel.this, "loginError", e1);
 			getLoginButton().setEnabled(true);
 			return;
		} catch (IOException e) {
 			Utils.showErrorMessage(getUser(), CertificateLoginPanel.this, "pluginError", e);
 			getLoginButton().setEnabled(true);
 			return;
 		}
 		
 //		try {
 //			// username & password already in the header
 //			// calling this method only to check whether the backend can get a proxy
 //			// with them
 //			serviceInterface.login(loginPanelHolder.getLoginParams().getMyProxyUsername(), loginPanelHolder.getLoginParams().getMyProxyPassphrase());
 //		} catch (Exception e2) {
 //			Utils.showErrorMessage(getUser(), CertificateLoginPanel.this, "wrongMyProxyPassphrase", e2);
 //			getLoginButton().setEnabled(true);
 //			return;
 //		}
 		
 		if ( serviceInterface != null ) {
 				loginPanelHolder.loggedIn(serviceInterface);
 		} else {
 			Utils.showErrorMessage(getUser(), CertificateLoginPanel.this, "unknownLoginError", null);
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
 		
 		String username = "anonymous";
 		Person user  = null;
 		try {
 			user = new Person(ClientPropertiesManager.getClientConfiguration(), username+" Grisu certificateLogin user");
 		} catch (Exception e) {
 			try {
 				user = new Person("Anonymous");
 			} catch (PersonException e1) {
 				e1.printStackTrace();
 				// this should never happen
 			}
 		}
 		user.setRole(Person.USER_ROLE);
 		user.setNickname(username);
 		return user;
 	}
 	
 	public void setParamsHolder(LoginPanelsHolder paramsHolder) {
 		this.loginPanelHolder = paramsHolder;
 	}
 	protected JButton cancelButton() {
 		if (button == null) {
 			button = new JButton();
 			button.setVisible(false);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					loginPanelHolder.cancelled();
 				}
 			});
 			button.setText("Cancel");
 		}
 		return button;
 	}
 
 }
