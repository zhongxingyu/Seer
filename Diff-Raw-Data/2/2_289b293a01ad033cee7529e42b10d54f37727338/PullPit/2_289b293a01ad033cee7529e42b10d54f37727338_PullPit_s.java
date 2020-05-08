 package kea.kme.pullpit.client;
 
 import kea.kme.pullpit.client.UI.PullPitConstants;
 import kea.kme.pullpit.client.UI.UIMain;
 import kea.kme.pullpit.client.services.LoginInfo;
 import kea.kme.pullpit.client.services.LoginService;
 import kea.kme.pullpit.client.services.LoginServiceAsync;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 /**
  * {@link com.google.gwt.core.client.EntryPoint} class, ie the GWT equivalent of a class containing the main method.
  * Verifies that a user is properly logged in before launching {@link UIMain}. 
  * @author Emil Thorenfeldt
  * 
  */
 public class PullPit implements EntryPoint {
 	private LoginInfo loginInfo;
 	private LoginServiceAsync loginService;
 	private PullPitConstants constants = GWT.create(PullPitConstants.class);
 	private PopupPanel myPop;
 	static String locale;
 	
 	/**
 	 * Entry point ("main") class which calls {@link kea.kme.pullpit.server.services.AdminServiceImpl} to get the client's
 	 * user credentials. If the user is logged on and is registered in the database,
 	 * the main GUI is loaded.
 	 */
 	public void onModuleLoad() {
 		// Checks if a locale has been specified. If not, locale is set to Danish
 		String getLocale = Window.Location.getParameter("locale");
 		if (getLocale==null) {
 			getLocale = "da";
 		}
 		locale = getLocale;
 		
 		// Loading animation
 		String userAgent = Window.Navigator.getUserAgent();
 		String styleName = "spinner";
		if (userAgent.contains("Firefox"))
 			styleName = "ffspinner";
 		myPop = new PopupPanel();
 		myPop.setStyleName(styleName);
 		myPop.show();
 		
 		// Handling login
 		String hostUrl = GWT.getHostPageBaseURL();
 		loginService = GWT.create(LoginService.class);
 		AsyncCallback<LoginInfo> callback = new AsyncCallback<LoginInfo>() {
 			public void onFailure(Throwable error) {
 				Window.alert(constants.unexpectedError());
 			}
 
 			public void onSuccess(LoginInfo result) {
 				loginInfo = result;
 				if (loginInfo.isLoggedIn()) {
 					myPop.hide();
 					if (loginInfo.isPullPitUser()) {
 						// If user is logged in & is a PullPit user, launch GUI
 						UIMain.getInstance().setUser(loginInfo);
 					} else {
 						// If user doesn't exist in PullPit DB, load user creation panel
 						loadCreateUser();
 					}
 				} else {
 					// Redirect to Google Login if user is not logged in
 					Window.Location.replace(loginInfo.getLoginUrl());
 				}
 			}
 		};
 		loginService.login(hostUrl, callback);
 
 	}
 
 	/**
 	 * Allows a user to register as a PullPit user by typing in the correct
 	 * authorisation token.  
 	 */
 	private void loadCreateUser() {
 		// Creating panel and creating textboxes, labels and a button
 		final HorizontalPanel contentPanel = new HorizontalPanel();
 		contentPanel
 				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		RootPanel.get("textArea").add(contentPanel);
 		Label tokenLabel = new Label(constants.enterToken());
 		Label userLabel = new Label(constants.inputUserName());
 		final TextBox tokenText = new TextBox();
 		final TextBox userText = new TextBox();
 		Button tokenButton = new Button("OK");
 		
 		// Calling async service when button is clicked
 		tokenButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				final String token = tokenText.getText();
 				final String userName = userText.getText();
 				loginInfo.setNickname(userName);
 				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert(constants.unexpectedError());
 					}
 
 					@Override
 					public void onSuccess(Boolean tokenAccepted) {
 						// If user is successfully created, reload page
 						if (tokenAccepted) {
 							RootPanel.get("textArea").remove(contentPanel);
 							Window.Location.reload();
 						// Otherwise, show error message
 						} else {
 							Window.alert(constants.invalidToken() + ": "
 									+ token);
 						}
 
 					}
 
 				};
 				loginService.createUser(loginInfo, token, callback);
 			}
 		});
 		
 		// Adding labels, textboxes and button to panel
 		contentPanel.add(tokenLabel);
 		contentPanel.add(tokenText);
 		contentPanel.add(userLabel);
 		contentPanel.add(userText);
 		contentPanel.add(tokenButton);
 
 	}
 		
 	public static String getLocale(){
 		return locale;
 	}
 
 }
