 package com.veilingsite.client.widgets;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.veilingsite.client.controllers.UC;
 import com.veilingsite.shared.ServerService;
 import com.veilingsite.shared.ServerServiceAsync;
 import com.veilingsite.shared.domain.User;
 
 public class UserLoginWidget extends VerticalPanel {
 
 	private Label systemStatus = new Label();
 	private Button login = new Button("Login");
 	private Button logout = new Button("Logout");
 	private TextBox username = new TextBox();
 	private PasswordTextBox password = new PasswordTextBox();
 	private FlexTable table = new FlexTable();
 	private Timer systemStatusTimer;
 		
 	public UserLoginWidget() {
 		username.setText("test");
 		password.setText("testtest");
 		systemStatusTimer = new Timer() {
 		      public void run() {
 					systemStatus.setVisible(false);
 		      }
 		 };
 		
 		//add class for styling
 		this.addStyleName("widget");
 		
 		if(UC.getLoggedIn() != null) {
 			table.setVisible(false);
 		}
 		
 		systemStatus.setVisible(false);
 		add(table);	
 		table.setWidget(0, 0, new Label("Username:"));		table.setWidget(0, 1, new Label("Password:"));
 		table.setWidget(1, 0, username);					table.setWidget(1, 1, password);		table.setWidget(1, 2, login);
 		table.setWidget(2, 0, systemStatus);				table.setWidget(2, 3, logout);
 		table.getFlexCellFormatter().setColSpan(2, 0, 3);
 		table.getCellFormatter().setVisible(2, 3, false);
 		
 		login.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				systemStatusTimer.cancel();
 				if(UC.getLoggedIn() == null) {
 					loginUser(new User(username.getText(), password.getText()));
 					systemStatus.setText("Login request processing...");
 					systemStatus.setStyleName("status");
 					systemStatus.setVisible(true);
 				} else {
 					table.getRowFormatter().setVisible(0, true);
 					table.getRowFormatter().setVisible(1, true);
 					//table.getCellFormatter().setVisible(1, 0, true);
 					//table.getCellFormatter().setVisible(1, 1, true);
 					table.getCellFormatter().setVisible(2, 3, false);
 					systemStatus.setText("User logged out");			
 					systemStatus.setStyleName("succesfull");
 					systemStatus.setVisible(true);
 					systemStatusTimer.schedule(3000);
 					login.setText("Login");
 					UC.setLoggedIn(null);
 					return;
 				}
 			}
 		});
 		
 		logout.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				systemStatusTimer.cancel();
 				if(UC.getLoggedIn() == null) {
 					systemStatus.setText("Login request processing...");
 					loginUser(new User(username.getText(), password.getText()));
 					systemStatus.setStyleName("status");
 					systemStatus.setVisible(true);
 				} else {
 					table.getRowFormatter().setVisible(0, true);
 					table.getRowFormatter().setVisible(1, true);
 					//table.getCellFormatter().setVisible(1, 0, true);
 					//table.getCellFormatter().setVisible(1, 1, true);
 					table.getCellFormatter().setVisible(2, 3, false);
 					systemStatus.setText("User logged out");			
 					systemStatus.setStyleName("succesfull");
 					systemStatus.setVisible(true);
 					systemStatusTimer.schedule(3000);
 					login.setText("Login");
 					UC.setLoggedIn(null);
 					return;
 				}
 			}
 		});
 	}
 	
 	private void setLogin(User u) {
 		systemStatusTimer.cancel();
 		if(u != null) {
 			table.getCellFormatter().setVisible(2, 3, true);
 			table.getRowFormatter().setVisible(0, false);
 			table.getRowFormatter().setVisible(1, false);
 			//table.getCellFormatter().setVisible(1, 0, false);
 			//table.getCellFormatter().setVisible(1, 1, false);
 			systemStatus.setText("Welcome "+u.getUserName());
 			systemStatus.setStyleName("succesfull");
 			systemStatus.setVisible(true);
 			systemStatusTimer.schedule(3000);
 			login.setText("Logout");
 			UC.setLoggedIn(u);
 		} else {
 			systemStatus.setText("User was not found or submitted data was incorrect.");
 			systemStatus.setStyleName("error");
 			systemStatus.setVisible(true);
 			systemStatusTimer.schedule(3000);
 		}
 	}
 	
 	private void loginUser(User u) {
 		systemStatusTimer.cancel();
 		ServerServiceAsync myService = (ServerServiceAsync) GWT.create(ServerService.class);
 		AsyncCallback<User> callback = new AsyncCallback<User>() {		
 			@Override
 			public void onFailure(Throwable caught) {
 				systemStatus.setText("User login failed, reason: " + caught.getMessage());
 				systemStatus.setStyleName("error");
 				systemStatus.setVisible(true);
 				systemStatusTimer.schedule(3000);
 			}	
 			
 			@Override
 			public void onSuccess(User result) {
				if(result.getPermission() == 0){
 					systemStatus.setText("User Access Denied, reason: User is BLOCKED");
 					systemStatus.setStyleName("error");
 					systemStatus.setVisible(true);
 					systemStatusTimer.schedule(3000);
 				}else{
 					setLogin(result);
 				}
 			}
 		};
 		myService.loginUser(u, callback);
 	}
 }
