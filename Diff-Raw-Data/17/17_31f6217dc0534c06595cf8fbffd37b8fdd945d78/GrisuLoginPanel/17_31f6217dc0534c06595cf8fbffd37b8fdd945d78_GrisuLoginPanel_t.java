 package grisu.frontend.view.swing.login;
 
 
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.login.LoginManager;
 import grith.gridsession.SessionClient;
 import grith.gridsession.view.CredCreationPanel;
 import grith.jgrith.cred.Cred;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.logging.Level;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.LineBorder;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class GrisuLoginPanel extends JPanel implements PropertyChangeListener {
 
 	private class LoginAction extends AbstractAction {
 		public LoginAction() {
 			putValue(NAME, "Login");
 			putValue(SHORT_DESCRIPTION, "Login to backend");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 
 			if (siHolder == null) {
 				myLogger.error("No serviceInterfaceHolder attached to this panel.");
 				return;
 			}
 			loggingIn = true;
 			lockUI(true);
 			credCreationPanel.createCredential();
 
 
 		}
 	}
 
 	public static final Cursor WAIT_CURSOR = Cursor
 			.getPredefinedCursor(Cursor.WAIT_CURSOR);
 
 	public static final Cursor DEFAULT_CURSOR =
 			Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(GrisuLoginPanel.class.getName());
 	private CredCreationPanel credCreationPanel;
 	private AdvancedLoginPanelOptions advancedLoginPanelOptions;
 	private JButton loginButton;
 
 	private Action action;
 
 	private final ServiceInterfaceHolder siHolder;
 
 	private boolean loggingIn = false;
	private boolean failed = false;
 
 	/**
 	 * Create the panel.
 	 */
 	public GrisuLoginPanel(ServiceInterfaceHolder siHolder) {
 		this.siHolder = siHolder;
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("min:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(74dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getCredCreationPanel(), "2, 2, fill, fill");
 		add(getAdvancedLoginPanelOptions(), "2, 4, fill, fill");
 		add(getLoginButton(), "2, 6, right, default");
 		getCredCreationPanel().addPropertyChangeListener(this);
 
 	}
 
 	private Action getAction() {
 		if (action == null) {
 			action = new LoginAction();
 		}
 		return action;
 	}
 
 	private AdvancedLoginPanelOptions getAdvancedLoginPanelOptions() {
 		if (advancedLoginPanelOptions == null) {
 			advancedLoginPanelOptions = new AdvancedLoginPanelOptions();
 			advancedLoginPanelOptions
 			.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		}
 		return advancedLoginPanelOptions;
 	}
 	private CredCreationPanel getCredCreationPanel() {
 		if (credCreationPanel == null) {
 			credCreationPanel = new CredCreationPanel();
 		}
 		return credCreationPanel;
 	}
 
 	private JButton getLoginButton() {
 		if (loginButton == null) {
 			loginButton = new JButton("Login");
 			loginButton.setAction(getAction());
 		}
 		return loginButton;
 	}
 
 	private void lockUI(final boolean lock) {
 
 		getAdvancedLoginPanelOptions().lockUI(lock);
 
 		SwingUtilities.invokeLater(new Thread() {
 			@Override
 			public void run() {
 				if (lock) {
 					setCursor(WAIT_CURSOR);
 				} else {
 					setCursor(DEFAULT_CURSOR);
 				}
 				getLoginButton().setEnabled(!lock);
 			}
 		});
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 
 		if (evt.getSource() != credCreationPanel) {
 			return;
 		}
		
		if ( "credentialCreationFailed".equals(evt.getPropertyName()) ) {
			this.failed= (Boolean)evt.getNewValue();
			return;
		}
 
 		if ("creatingCredential".equals(evt.getPropertyName())) {
 			boolean creating = (Boolean) evt.getNewValue();
			
			if (failed) {
				lockUI(false);
				failed = false;
 			}
 			return;
 		}
 
 		if ("credential".equals(evt.getPropertyName())) {
 			String backend = getAdvancedLoginPanelOptions()
 					.getServiceInterfaceUrl();
			
 			Cred cred = getCredCreationPanel().getCredential();
 
 			try {
 				ServiceInterface si = LoginManager.login(backend, cred, false);
 				try {
 					siHolder.setServiceInterface(si);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 
 			} catch (Throwable ex) {
 				if (ex.getCause() != null) {
 					ex = ex.getCause();
 				}
 				final String msg = ex.getLocalizedMessage();
 				final ErrorInfo info = new ErrorInfo("Login error",
 						"Login to backend '" + backend + "' failed.", msg,
 						"Error", ex,
 						Level.SEVERE, null);
 
 				final JXErrorPane pane = new JXErrorPane();
 				pane.setErrorInfo(info);
 
 				JXErrorPane.showDialog(GrisuLoginPanel.this, pane);
 				return;
 			} finally {
 				lockUI(false);
 			}
 
 		}
 	}
 
 	public void setSessionClient(SessionClient sc) {
 		getCredCreationPanel().setSessionClient(sc);
 	}
 
 }
