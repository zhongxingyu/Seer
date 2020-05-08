 package org.smartsnip.client;
 
 import org.smartsnip.shared.ISession;
 import org.smartsnip.shared.NoAccessException;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * This widget allows control over the login process.
  * 
  * @author Paul
  * @author Felix Niederwanger
  * 
  * 
  * 
  */
 public class Login extends Composite {
 
 	/** This {@link PopupPanel} acts as the parent for all components */
 	private final PopupPanel parent = new PopupPanel(false, true);
 
 	/* Components */
 	private final HorizontalPanel pnlHorToolbar = new HorizontalPanel();
 	private VerticalPanel vertPanel = new VerticalPanel();
 	private Label lblName = new Label("Username");
 	private TextBox txtName = new TextBox();
 	private Label lblPassword = new Label("Password");
 	private PasswordTextBox passPassword = new PasswordTextBox();
 	private final Button btnLogin;
 	private final Button btnClose;
 	private final Label lblStatus = new Label("");
 	private final Label lblTitle;
 
 	/* End of components */
 
 	/**
 	 * Creates a new Login {@link PopupPanel}
 	 */
 	private Login() {
 		vertPanel = new VerticalPanel();
 		lblTitle = new Label("Smartsnip Login");
 		lblTitle.setStyleName("h3");
 		lblName = new Label("Username");
 		txtName = new TextBox();
 		lblPassword = new Label("Password");
 		passPassword = new PasswordTextBox();
 
 		parent.setTitle("Login");
 
 		passPassword.addKeyDownHandler(new KeyDownHandler() {
 			@Override
 			public void onKeyDown(KeyDownEvent event) {
 				if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
 					login();
 				}
 			}
 		});
 
 		btnLogin = new Button("Login");
 		btnLogin.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				login();
 			}
 
 		});
 		btnClose = new Button("Close");
 		btnClose.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				close();
 			}
 		});
 
 		vertPanel.add(lblTitle);
 		vertPanel.add(lblName);
 		vertPanel.add(txtName);
 		vertPanel.add(lblPassword);
 		vertPanel.add(passPassword);
 		pnlHorToolbar.add(btnLogin);
 		pnlHorToolbar.add(btnClose);
 
 		vertPanel.add(pnlHorToolbar);
 		vertPanel.add(lblStatus);
 		initWidget(vertPanel);
 		parent.setWidget(this);
 
 		applyStyles();
 	}
 
 	private void applyStyles() {
 		Window.scrollTo(0, 0);
 		parent.setStyleName("Login");
 		parent.setGlassEnabled(true);
 		parent.setPopupPosition(90, 104);
 		parent.setWidth("250px");
 
 		// Give the overall composite a style name.
 		setStyleName("login");
 	}
 
 	/**
 	 * Called from the controller, if the login procedure fails.
 	 * 
 	 * @param reason
 	 *            Message, why the login failed
 	 */
 	void loginFailure(String reason) {
 		lblStatus.setText("Login failure: " + reason);
 		btnLogin.setEnabled(true);
 	}
 
 	/**
 	 * Called from the Control, if the login procedure succeeds
 	 */
 	void loginSuccess() {
 		parent.hide();
 		Control.myGUI.refresh();
 		Control.myGUI.refreshMeta();
 	}
 
 	/**
 	 * Does the login
 	 */
 	private void login() {
 		btnLogin.setEnabled(false);
 		final String username = txtName.getText();
 		final String password = passPassword.getText();
 
 		if (username.isEmpty() || password.isEmpty()) {
			btnLogin.setEnabled(true);
 			lblStatus.setText("Some arguments are missing ");
 			return;
 		}
 
 		ISession.Util.getInstance().login(username, password, new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onSuccess(Boolean result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Null returned"));
 					return;
 				}
 				if (result) {
 					// Login succeeded
 					Control.getInstance().changeLoginState(true);
 
 					close();
 
 					// This call MUST go to the controller, after a login
 					Control.getInstance().onLogin(username);
 				} else {
 					onFailure(new NoAccessException("Login failed"));
 					return;
 				}
 
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
				if (caught == null) lblStatus.setText("Login failed (unknown reason)");
				else if (caught instanceof NoAccessException) lblStatus
						.setText("Login failed - Wrong login credentials");
 				else
 					lblStatus.setText("Login failed: " + caught.getMessage());

				btnLogin.setEnabled(true);
 			}
 		});
 	}
 
 	/** Close the popup */
 	private void close() {
 		parent.hide();
 	}
 
 	/** Shows the popup */
 	private void show() {
 		parent.show();
 	}
 
 	/**
 	 * Creates a new login popup panel and shows it
 	 */
 	public static void showLoginPopup() {
 		Login login = new Login();
 		login.show();
 	}
 }
