 package org.vpac.grix.view.swing.slcs;
 
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
 import org.ietf.jgss.GSSCredential;
 import org.vpac.common.model.gridproxy.GridProxy;
 import org.vpac.common.model.gridproxy.GridProxyEvent;
 import org.vpac.common.model.gridproxy.GridProxyListener;
 import org.vpac.common.model.gridproxy.LocalProxy;
 import org.vpac.common.model.gridproxy.PlainProxy;
 import org.vpac.grix.control.utils.UserProperty;
 import org.vpac.grix.exceptions.shibboleth.ShibbolethException;
 import org.vpac.grix.view.swing.proxy.ProxyInfoPanel;
 
 import au.edu.archer.desktopshibboleth.idp.IDP;
 import au.org.mams.slcs.client.SLCSClient;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class SlcsPanel extends JPanel implements GridProxyListener {
 
 	private JButton button;
 	private ProxyInfoPanel proxyInfoPanel;
 	private JButton loginButton;
 	private JPasswordField passwordField;
 	private JLabel passwordLabel;
 	private JTextField textField;
 	private JLabel usernameLabel;
 	private JComboBox comboBox;
 	private JLabel pleaseSelectYourLabel;
 
 	// private static ResourceBundle messages = ResourceBundle.getBundle(
 	// "GridProxyPanelMessageBundle", java.util.Locale.getDefault());
 
 	SLCSClient client = null;
 	DefaultComboBoxModel idpModel = new DefaultComboBoxModel();
 
 
 	/**
 	 * Create the panel
 	 * 
 	 * @throws ShibbolethException
 	 */
 	public SlcsPanel() {
 		super();
 
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC, new ColumnSpec("53dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				new ColumnSpec("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				new RowSpec("top:13dlu"), new RowSpec("default"),
 				new RowSpec("top:11dlu") }));
 		add(getPleaseSelectYourLabel(), new CellConstraints(2, 2, 3, 1));
 		add(getComboBox(), new CellConstraints(2, 4, 3, 1));
 		add(getUsernameLabel(), new CellConstraints(2, 6));
 		add(getTextField(), new CellConstraints(4, 6));
 		add(getPasswordLabel(), new CellConstraints(2, 8));
 		add(getPasswordField(), new CellConstraints(4, 8));
 		add(getLoginButton(), new CellConstraints(4, 10, CellConstraints.RIGHT,
 				CellConstraints.DEFAULT));
 		add(getProxyInfoPanel(), new CellConstraints(2, 12, 3, 1,
 				CellConstraints.FILL, CellConstraints.FILL));
 		//
 
 		idpModel.addElement("Please wait... loading IDPs...");
 		enablePanel(false);
 
 		retrieveAllIDPs();
 
 		LocalProxy.addStatusListener(this);
 
 		if (LocalProxy.getDefaultProxy().getStatus() == GridProxy.INITIALIZED) {
 			getProxyInfoPanel().setCredential(
 					LocalProxy.getDefaultProxy().getGlobusCredential());
 		}
 		add(getButton(), new CellConstraints(2, 10, CellConstraints.LEFT, CellConstraints.DEFAULT));
 	}
 	
 
 
 	private void retrieveAllIDPs() {
 
 		new Thread() {
 			public void run() {
 				try {
 					client = new SLCSClient();
 				} catch (Exception e) {
 					// throw new ShibbolethException(
 					// "Can't create shibboleth panel: "
 					// + e.getLocalizedMessage(), e);
 				}
 				List<IDP> idps = null;
 				try {
 					idps = client.getAvailableIDPs();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				String lastIdpName = UserProperty
 						.getProperty("LAST_SELECTED_IDP");
 				IDP lastIdp = null;
 				idpModel.removeAllElements();
 				for (IDP idp : idps) {
 					idpModel.addElement(idp);
 					if (lastIdpName != null
 							&& idp.getName().equals(lastIdpName)) {
 						lastIdp = idp;
 					}
 				}
 				idpModel.setSelectedItem(lastIdp);
 				enablePanel(true);
 			}
 		}.start();
 
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		SLCSClient client = new SLCSClient();
 		List<IDP> ipds = client.getAvailableIDPs();
 		GSSCredential cred = client.slcsLogin(ipds.get(2), "test_user",
 				"test_password");
 
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getPleaseSelectYourLabel() {
 		if (pleaseSelectYourLabel == null) {
 			pleaseSelectYourLabel = new JLabel();
 			pleaseSelectYourLabel.setText("Please select your IDP:");
 		}
 		return pleaseSelectYourLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JComboBox getComboBox() {
 		if (comboBox == null) {
 			comboBox = new JComboBox(idpModel);
 		}
 		return comboBox;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getUsernameLabel() {
 		if (usernameLabel == null) {
 			usernameLabel = new JLabel();
 			usernameLabel.setText("Username:");
 		}
 		return usernameLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JTextField getTextField() {
 		if (textField == null) {
 			textField = new JTextField();
 			String lastUsername = UserProperty.getProperty("IDP_USERNAME");
 			if (lastUsername != null && !"".equals(lastUsername)) {
 				textField.setText(lastUsername);
 			}
 		}
 		return textField;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getPasswordLabel() {
 		if (passwordLabel == null) {
 			passwordLabel = new JLabel();
 			passwordLabel.setText("Password:");
 		}
 		return passwordLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JPasswordField getPasswordField() {
 		if (passwordField == null) {
 			passwordField = new JPasswordField();
 		}
 		return passwordField;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getLoginButton() {
 		if (loginButton == null) {
 			loginButton = new JButton();
 			loginButton.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					new Thread() {
 
 						public void run() {
 							try {
 								// client = new SLCSClient();
 								enablePanel(false);
 								setCursor( new Cursor(Cursor.WAIT_CURSOR) );
 								IDP selIdp = ((IDP) idpModel.getSelectedItem());
 								GSSCredential cred;
 								try {
 									cred = client.slcsLogin(
 											(IDP) (idpModel.getSelectedItem()),
 											getTextField().getText(), new String(
 													getPasswordField()
 															.getPassword()));
 								} catch (Throwable e) {
 									JOptionPane.showMessageDialog(SlcsPanel.this,
 										    e.getLocalizedMessage(),
 										    "Login error",
 										    JOptionPane.ERROR_MESSAGE);
 									e.printStackTrace();
 									return;
 								}
 
 								//TODO delete password
 								getPasswordField().setText("");
 								
 //								System.out.println("Remaining lifetime: "
 //										+ cred.getRemainingLifetime());
 								GlobusCredential globusCred = null;
 								if (cred instanceof GlobusGSSCredentialImpl) {
 									globusCred = ((GlobusGSSCredentialImpl) cred)
 											.getGlobusCredential();
 								}
 								try {
 									GridProxy proxy = new PlainProxy(LocalProxy
 											.getProxyFile(), globusCred);
 									LocalProxy.setDefaultProxy(proxy);
 								} catch (IOException e) {
 									JOptionPane.showMessageDialog(SlcsPanel.this,
 								    e.getLocalizedMessage(),
 								    "Error while saving proxy",
 								    JOptionPane.ERROR_MESSAGE);
 									e.printStackTrace();
 									return;
 								}
 
 								UserProperty.setProperty("IDP_USERNAME",
 										getTextField().getText());
 								UserProperty.setProperty("LAST_SELECTED_IDP",
 										selIdp.getName());
 
 //							} catch (Exception e1) {
 //								JOptionPane.showMessageDialog(SlcsPanel.this,
 //									    e1.getLocalizedMessage(),
 //									    "Login error",
 //									    JOptionPane.ERROR_MESSAGE);
 //								e1.printStackTrace();
 							} finally {
 								enablePanel(true);
 								setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
 							}
 						}
 					}.start();
 
 				}
 			});
 			loginButton.setText("Login");
 		}
 		return loginButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected ProxyInfoPanel getProxyInfoPanel() {
 		if (proxyInfoPanel == null) {
 			proxyInfoPanel = new ProxyInfoPanel();
 			proxyInfoPanel.setBorder(new TitledBorder(null,
 					"Proxy information:", TitledBorder.DEFAULT_JUSTIFICATION,
 					TitledBorder.DEFAULT_POSITION, null, null));
 		}
 		return proxyInfoPanel;
 	}
 
 	/**
 	 * @return
 	 */
 
 	private void enablePanel(boolean enable) {
 		getLoginButton().setEnabled(enable);
 		getButton().setEnabled(enable);
 		getComboBox().setEnabled(enable);
 		getPasswordField().setEnabled(enable);
 		getTextField().setEnabled(enable);
 	}
 
 	public void gridProxyStatusChanged(GridProxyEvent e) {
 
 		if (e.getStatus() == GridProxy.INITIALIZED) {
 			getProxyInfoPanel().setCredential(
 					LocalProxy.getDefaultProxy().getGlobusCredential());
 		} else {
 			getProxyInfoPanel().setCredential(null);
 		}
 
 	}
 	/**
 	 * @return
 	 */
 	protected JButton getButton() {
 		if (button == null) {
 			button = new JButton();
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					
 					LocalProxy.destroy();
 					
 				}
 			});
 			button.setText("Destroy");
 		}
 		return button;
 	}
 	/**
 	 * @return
 	 */
 
 }
