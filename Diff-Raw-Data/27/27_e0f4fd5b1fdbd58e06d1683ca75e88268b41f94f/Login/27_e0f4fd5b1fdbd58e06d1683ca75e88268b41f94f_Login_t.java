 package com.cs1530.group4.addendum.client;
 
 import java.util.Date;
 
 import com.cs1530.group4.addendum.shared.User;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
 import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Grid;
 
 public class Login extends Composite
 {
 	private TextBox usernameTextBox;
 	private PasswordTextBox passwordTextBox;
 	private Label errorLabel;
 	private MainView main;
 	private CheckBox rememberMeCheckBox;
 
 	public Login(MainView m)
 	{
 		main = m;
 		VerticalPanel verticalPanel = new VerticalPanel();
 		verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		initWidget(verticalPanel);
 		verticalPanel.setSize("843px", "657px");
 
 		if(Cookies.getCookie("loggedIn") != null)
 		{
 			Storage localStorage = Storage.getLocalStorageIfSupported();
 			User user = new User(Cookies.getCookie("loggedIn"));
 			if(localStorage.getItem("loggedIn") != null)
 				user = User.deserialize(localStorage.getItem("loggedIn"));
 			m.setContent(new Stream(m, user), "profile-" + Cookies.getCookie("loggedIn"));
 		}
 
 		Image image = new Image("image001.jpg");
 		image.setStyleName("LoginTitle");
 		verticalPanel.add(image);
 		verticalPanel.setCellVerticalAlignment(image, HasVerticalAlignment.ALIGN_MIDDLE);
 		verticalPanel.setCellHorizontalAlignment(image, HasHorizontalAlignment.ALIGN_CENTER);
 		image.setSize("600px", "100px");
		
		Grid grid = new Grid(2, 1);
		verticalPanel.add(grid);
		verticalPanel.setCellHorizontalAlignment(grid, HasHorizontalAlignment.ALIGN_CENTER);
		
				errorLabel = new Label("Could not login.  Invalid username or password.");
				grid.setWidget(0, 0, errorLabel);
				errorLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				errorLabel.setVisible(false);
 		errorLabel.setStyleName("gwt-Label-Error");
 		errorLabel.setWidth("315px");
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
		grid.setWidget(1, 0, horizontalPanel);
 		horizontalPanel.setStyleName("LoginBox");
 		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		horizontalPanel.setSize("543px", "154px");
 
 		FlexTable flexTable = new FlexTable();
 		horizontalPanel.add(flexTable);
 		flexTable.setSize("329px", "155px");
 
 		Label lblAlreadyAUser = new Label("Already a User?");
 		lblAlreadyAUser.setStyleName("gwt-Label-User");
 		flexTable.setWidget(0, 0, lblAlreadyAUser);
 
 		Label lblUsername = new Label("Username:");
 		lblUsername.setStyleName("gwt-Label-Login");
 		flexTable.setWidget(1, 0, lblUsername);
 
 		usernameTextBox = new TextBox();
 		usernameTextBox.setAlignment(TextAlignment.LEFT);
 		usernameTextBox.addKeyPressHandler(new KeyPressHandler()
 		{
 			public void onKeyPress(KeyPressEvent event)
 			{
 				if(event.getCharCode() == KeyCodes.KEY_ENTER)
 					login();
 			}
 		});
 		usernameTextBox.setFocus(true);
 		flexTable.setWidget(1, 1, usernameTextBox);
 
 		Label lblPassword = new Label("Password:");
 		lblPassword.setStyleName("gwt-Label-Login");
 		flexTable.setWidget(2, 0, lblPassword);
 
 		passwordTextBox = new PasswordTextBox();
 		passwordTextBox.setAlignment(TextAlignment.LEFT);
 		passwordTextBox.addKeyPressHandler(new KeyPressHandler()
 		{
 			public void onKeyPress(KeyPressEvent event)
 			{
 				if(event.getCharCode() == KeyCodes.KEY_ENTER)
 					login();
 			}
 		});
 		flexTable.setWidget(2, 1, passwordTextBox);
 
 		Anchor forgotPassword = new Anchor("Forgot Password?");
 		forgotPassword.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				//TODO: implement dialog prompting user's email
 				Window.alert("To Be Implemented");
 			}
 		});
 		flexTable.setWidget(3, 0, forgotPassword);
 
 		rememberMeCheckBox = new CheckBox("Remember me on this Computer");
 		flexTable.setWidget(3, 1, rememberMeCheckBox);
 
 		Button btnNewButton = new Button("Sign In");
 		btnNewButton.setStyleName("LoginButton");
 		btnNewButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				login();
 			}
 		});
 		flexTable.setWidget(4, 1, btnNewButton);
 		btnNewButton.setSize("110px", "25px");
 		flexTable.getFlexCellFormatter().setColSpan(0, 0, 2);
 
 		VerticalPanel verticalPanel_1 = new VerticalPanel();
 		horizontalPanel.add(verticalPanel_1);
 		verticalPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		verticalPanel_1.setSize("211px", "155px");
 
 		Label lblCreateANew = new Label("Don't Have an Account?");
 		verticalPanel_1.add(lblCreateANew);
 		lblCreateANew.setStyleName("gwt-Label-User");
 
 		Button button = new Button("Create Account");
 		button.setText("CREATE ACCOUNT");
 		button.setStyleName("LoginButton");
 		verticalPanel_1.add(button);
 		button.setSize("167px", "25px");
		grid.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		grid.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
		grid.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		grid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
 		button.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				new NewUserDialog(main);
 			}
 		});
 		setStyleName("Login");
 	}
 
 	protected void login()
 	{
 		String username = usernameTextBox.getText();
 		String password = passwordTextBox.getText();
 
 		if(username.length() == 0 || password.length() == 0)
 		{
 			Window.alert("Username or Password is empty.");
 			return;
 		}
 		else
 		{
 			UserServiceAsync loginService = UserService.Util.getInstance();
 			// Set up the callback object.
 			AsyncCallback<User> callback = new AsyncCallback<User>()
 			{
 				@Override
 				public void onFailure(Throwable caught)
 				{
 				}
 
 				@Override
 				public void onSuccess(User user)
 				{
 					if(user != null)
 						acceptLogin(user);
 					else
 						rejectLogin();
 				}
 			};
 
 			loginService.doLogin(username, password, callback);
 		}
 	}
 
 	private void acceptLogin(User user)
 	{
 		if(rememberMeCheckBox.getValue())
 		{
 			Date expires = new Date();
 			expires.setTime(expires.getTime() + (1000 * 60 * 60 * 24 * 14)); //14 days from now
 			Cookies.setCookie("loggedIn", usernameTextBox.getText(), expires);
 		}
 		else
 			Cookies.setCookie("loggedIn", usernameTextBox.getText());
 
 		Storage localStorage = Storage.getLocalStorageIfSupported();
 		localStorage.setItem("loggedIn", user.serialize());
 
 		if(usernameTextBox.getText().equals("Administrator"))
 			main.setContent(new AdminPanel(main), "adminPanel");
 		else
 			main.setContent(new Stream(main, user), "profile-" + usernameTextBox.getText());
 	}
 
 	private void rejectLogin()
 	{
 		errorLabel.setVisible(true);
 	}
 }
