 package org.vpac.grisu.client.view.swing.login;
 
 import java.awt.BorderLayout;
 import java.io.IOException;
 
 import javax.swing.JPanel;
 
 import org.globus.gsi.GlobusCredential;
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
 
 import au.org.arcs.commonInterfaces.HttpProxyInfoHolder;
 import au.org.mams.slcs.client.view.swing.SlcsLoginPanel;
 import au.org.mams.slcs.client.view.swing.SlcsPanelListener;
 
 public class GrisuSlcsLoginPanel extends JPanel implements SlcsPanelListener, HttpProxyInfoHolder {
 
 	private SlcsLoginPanel slcsLoginPanel;
 	
 	private GlobusCredential cred = null;
 	
 	private LoginPanelsHolder loginPanelHolder = null;
 	
 	/**
 	 * Create the panel
 	 */
 	public GrisuSlcsLoginPanel() {
 		super();
 		setLayout(new BorderLayout());
 		add(getSlcsLoginPanel());
 		//
 		getSlcsLoginPanel().addSlcsPanelListener(this);
 		
 		getSlcsLoginPanel().setUsername(ClientPropertiesManager.getSavedShibbolethUsername());
 		
 		getSlcsLoginPanel().setHttpProxyInfoHolder(this);
 	}
 	
 	/**
 	 * @return
 	 */
 	protected SlcsLoginPanel getSlcsLoginPanel() {
 		if (slcsLoginPanel == null) {
 			slcsLoginPanel = new SlcsLoginPanel();
 			slcsLoginPanel.setButtonText("Login");
 		}
 		return slcsLoginPanel;
 	}
 	
 	public void slcsPanelEventOccured(int eventType, GlobusCredential cred, Exception e) {
 
 		
 		if ( eventType == SlcsLoginPanel.LOGIN_SUCCESSFUL ) {
 			this.cred = cred;
 			login();
 		} else if ( eventType == SlcsLoginPanel.CANCEL_BUTTON_PRESSED ) {
 			loginPanelHolder.cancelled();
 		} else if ( eventType == SlcsLoginPanel.LOGIN_FAILED ) {
 			Utils.showErrorMessage(getUser(), GrisuSlcsLoginPanel.this, "slcsLoginError", e);
 		} else if ( eventType == SlcsLoginPanel.IDPS_RETRIEVED ) {
 			getSlcsLoginPanel().setSelectedIdp(ClientPropertiesManager.getSavedShibbolethIdp());
 		} else if ( eventType == SlcsLoginPanel.CANT_RETRIEVE_IDPS ) {
 			Utils.showErrorMessage(getUser(), GrisuSlcsLoginPanel.this, "slcsConnectionError", e);
 		}
 		
 	}
 	
 	public void setParamsHolder(LoginPanelsHolder paramsHolder) {
 		this.loginPanelHolder = paramsHolder;
 	}
 	
 	private void login() {
 		
 		ServiceInterface serviceInterface = null;
 		
 		try {
 //			serviceInterface = LoginHelpers.globusCredentialLogin(this.loginPanelHolder.getLoginParams(), cred);
 			serviceInterface = LoginManager.login(cred, null, null, null, this.loginPanelHolder.getLoginParams());
 		} catch (LoginException e) {
 			Utils.showErrorMessage(getUser(), GrisuSlcsLoginPanel.this, "loginError", e);
 			return;
		} catch (IOException e2) {
 			Utils.showErrorMessage(getUser(), GrisuSlcsLoginPanel.this, "pluginError", e2);
 			return;
 		}
 		
 		if ( serviceInterface != null ) {
 			loginPanelHolder.loggedIn(serviceInterface);
 		} else {
 			Utils.showErrorMessage(getUser(), GrisuSlcsLoginPanel.this, "unknownLoginError", null);
 		}
 
 		ClientPropertiesManager.saveShibbolethIdp(getSlcsLoginPanel().getSelectedIdp().getName());
 		ClientPropertiesManager.saveShibbolethUsername(getSlcsLoginPanel().getUsername());
 		
 	}
 	
 	private Person getUser() {
 		
 		String username = "anonymous";
 		Person user  = null;
 		try {
 			user = new Person(ClientPropertiesManager.getClientConfiguration(), username+" Grisu shibboleth login user");
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
 
 	public char[] getPassword() {
 		return loginPanelHolder.getLoginParams().getHttpProxyPassphrase();
 	}
 
 	public int getProxyPort() {
 		return loginPanelHolder.getLoginParams().getHttpProxyPort();
 	}
 
 	public String getProxyServer() {
 		return loginPanelHolder.getLoginParams().getHttpProxy();
 	}
 
 	public String getUsername() {
 		return loginPanelHolder.getLoginParams().getHttpProxyUsername();
 	}
 
 }
