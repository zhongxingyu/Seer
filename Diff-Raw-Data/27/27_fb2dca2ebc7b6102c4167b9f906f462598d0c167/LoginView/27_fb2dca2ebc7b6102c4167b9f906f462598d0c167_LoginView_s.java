 package edu.ycp.cs320.chronos.client;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import edu.ycp.cs320.chronos.client.SignUpView;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.KeyboardListener;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.TextBoxBase;
 import com.google.gwt.user.client.ui.Label;
 
 public class LoginView extends Composite {
 	private Button loginButton;
 	private TextBox userName;
 	private PasswordTextBox password;
 	public LoginView() {
 		final LayoutPanel loginPanel = new LayoutPanel();
 		
 		loginPanel.setStyleName("chronos-loginPanel-bg", true);
 		
 		initWidget(loginPanel);
 		loginPanel.setSize("590px", "415px");
 		
 		// Location of Login Button
 		loginButton = new Button("Login!");
 		loginPanel.add(loginButton);
 		loginPanel.setWidgetLeftWidth(loginButton, 223.0, Unit.PX, 79.0, Unit.PX);
 		loginPanel.setWidgetTopHeight(loginButton, 316.0, Unit.PX, 33.0, Unit.PX);
 			
 			
 			// Handle if user clicks button
 			loginButton.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
					RPC.accountManagementService.verifyAccount(userName.getText(), password.getText(), new AsyncCallback<Boolean>() {
 						@Override
 						public void onSuccess(Boolean result) {
 							if (result) {
 								// successful login
 								//Update view
 								ChronosUI.setCurrentView(new mainView()); 
 								
 							} else {
 								// unsuccessful login
 								//Display an error message box under the login button 
 								Label error = new Label("The entered username and password did not match.");
 								loginPanel.add(error);
 								loginPanel.setWidgetLeftWidth(error, 177.0, Unit.PX, 170.0, Unit.PX);
 								loginPanel.setWidgetTopHeight(error, 358.0, Unit.PX, 57.0, Unit.PX);
 								
 							}
 						}
 						
 						@Override
 						public void onFailure(Throwable caught) {
 							GWT.log("RPC call to verify account failed: ", caught);
 						}
 					});
 				}
 			});
 				
 		
 		// Location of username textbox
 		userName = new TextBox();
 		userName.setTextAlignment(TextBoxBase.ALIGN_CENTER);
 		userName.setText("Enter User Name");
 		loginPanel.add(userName);
 		loginPanel.setWidgetLeftWidth(userName, 177.0, Unit.PX, 170.0, Unit.PX);
 		loginPanel.setWidgetTopHeight(userName, 238.0, Unit.PX, 33.0, Unit.PX);
 			
 		
 		
 		// Location of password textbox
 		password = new PasswordTextBox();
 		loginPanel.add(password);
 		loginPanel.setWidgetLeftWidth(password, 177.0, Unit.PX, 170.0, Unit.PX);
 		loginPanel.setWidgetTopHeight(password, 277.0, Unit.PX, 33.0, Unit.PX);
 		
 		Button signUp = new Button("Sign up!");
 		
 		//Click handler: Directs the user to the SignUpView
 		signUp.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				ChronosUI.setCurrentView(new SignUpView());
 			}
 		});
 		loginPanel.add(signUp);
 		loginPanel.setWidgetLeftWidth(signUp, 480.0, Unit.PX, 110.0, Unit.PX);
 		loginPanel.setWidgetTopHeight(signUp, 0.0, Unit.PX, 45.0, Unit.PX);
 		
 		
 			
 		
 	}
 }
