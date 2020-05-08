 package com.cs1530.group4.addendum.client;
 
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
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
 import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
 
 public class NewUserDialog extends DialogBox
 {
 	MainView main;
 	Label errorLabel;
 	TextBox firstNameTextBox, lastNameTextBox, usernameTextBox;
 	PasswordTextBox passwordTextBox;
 	DialogBox dialog = this;
 
 	public NewUserDialog(MainView m)
 	{
 		main = m;
 		FlexTable flexTable = new FlexTable();
 		add(flexTable);
 		flexTable.setSize("528px", "432px");
 
 		Label lblFillInYour = new Label("Fill in your details below");
 		lblFillInYour.setStyleName("newUserDialogTitle");
 		flexTable.setWidget(0, 0, lblFillInYour);
 
 		firstNameTextBox = new TextBox();
 		firstNameTextBox.setAlignment(TextAlignment.CENTER);
 		firstNameTextBox.setStyleName("ADCTextbox");
 		firstNameTextBox.addKeyPressHandler(new KeyPressHandler()
 		{
 			public void onKeyPress(KeyPressEvent event)
 			{
 				if(event.getCharCode() == KeyCodes.KEY_ENTER)
 					createUser(firstNameTextBox.getText(), lastNameTextBox.getText(), usernameTextBox.getText(), passwordTextBox.getText());
 			}
 		});
 
 		Label lblFirstName = new Label("First Name:");
 		lblFirstName.setStyleName("whatever");
 		flexTable.setWidget(1, 1, lblFirstName);
 		flexTable.setWidget(1, 4, firstNameTextBox);
 		firstNameTextBox.setSize("200px", "30px");
 
 		lastNameTextBox = new TextBox();
		lastNameTextBox.setTextAlignment(TextBoxBase.ALIGN_CENTER);
 		lastNameTextBox.setStyleName("ADCTextbox");
 		lastNameTextBox.addKeyPressHandler(new KeyPressHandler()
 		{
 			public void onKeyPress(KeyPressEvent event)
 			{
 				if(event.getCharCode() == KeyCodes.KEY_ENTER)
 					createUser(firstNameTextBox.getText(), lastNameTextBox.getText(), usernameTextBox.getText(), passwordTextBox.getText());
 			}
 		});
 
 		Label lblLastName = new Label("Last Name:");
 		lblLastName.setStyleName("whatever");
 		flexTable.setWidget(2, 1, lblLastName);
 		flexTable.setWidget(2, 4, lastNameTextBox);
 		lastNameTextBox.setSize("200px", "30px");
 
 		usernameTextBox = new TextBox();
		usernameTextBox.setTextAlignment(TextBoxBase.ALIGN_CENTER);
 		usernameTextBox.setStyleName("ADCTextbox");
 		usernameTextBox.addKeyPressHandler(new KeyPressHandler()
 		{
 			public void onKeyPress(KeyPressEvent event)
 			{
 				if(event.getCharCode() == KeyCodes.KEY_ENTER)
 					createUser(firstNameTextBox.getText(), lastNameTextBox.getText(), usernameTextBox.getText(), passwordTextBox.getText());
 			}
 		});
 
 		Label lblUsername = new Label("Username:");
 		lblUsername.setStyleName("whatever");
 		flexTable.setWidget(3, 1, lblUsername);
 		flexTable.setWidget(3, 4, usernameTextBox);
 		usernameTextBox.setSize("200px", "30px");
 
 		passwordTextBox = new PasswordTextBox();
		passwordTextBox.setTextAlignment(TextBoxBase.ALIGN_CENTER);
 		passwordTextBox.setStyleName("ADCTextbox");
 		passwordTextBox.addKeyPressHandler(new KeyPressHandler()
 		{
 			public void onKeyPress(KeyPressEvent event)
 			{
 				if(event.getCharCode() == KeyCodes.KEY_ENTER)
 					createUser(firstNameTextBox.getText(), lastNameTextBox.getText(), usernameTextBox.getText(), passwordTextBox.getText());
 			}
 		});
 
 		Label lblPassword = new Label("Password:");
 		lblPassword.setStyleName("whatever");
 		flexTable.setWidget(4, 1, lblPassword);
 		flexTable.setWidget(4, 4, passwordTextBox);
 		passwordTextBox.setSize("200px", "30px");
 
 		errorLabel = new Label("username already in use");
 		errorLabel.setVisible(false);
 		errorLabel.setStyleName("gwt-Label-Error");
 		flexTable.setWidget(5, 0, errorLabel);
 		errorLabel.setWidth("500px");
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		horizontalPanel.setStyleName((String) null);
 		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		flexTable.setWidget(6, 0, horizontalPanel);
 
 		Button btnCancel = new Button("Cancel");
 		btnCancel.setStyleName("ADCButton");
 		btnCancel.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				dialog.hide();
 			}
 		});
 		horizontalPanel.add(btnCancel);
 		btnCancel.setSize("70px", "30px");
 
 		Button btnOk = new Button("OK");
 		btnOk.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				createUser(firstNameTextBox.getText(), lastNameTextBox.getText(), usernameTextBox.getText(), passwordTextBox.getText());
 			}
 		});
 		btnOk.setStyleName("ADCButton");
 		horizontalPanel.add(btnOk);
 		btnOk.setSize("70px", "30px");
 		flexTable.getFlexCellFormatter().setColSpan(0, 0, 5);
 		flexTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		flexTable.getFlexCellFormatter().setColSpan(6, 0, 5);
 		flexTable.getCellFormatter().setHorizontalAlignment(6, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		flexTable.getCellFormatter().setHorizontalAlignment(5, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		flexTable.getFlexCellFormatter().setColSpan(5, 0, 5);
 		setStyleName("ADCBasic");
 
 		setGlassEnabled(true);
 		center();
 	}
 
 	protected void createUser(String firstName, String lastName, final String username, String password)
 	{
 		if(firstName.length() == 0 || lastName.length() == 0 || username.length() == 0 || password.length() == 0)
 		{
 			Window.alert("All fields are required");
 			return;
 		}
 
 		UserServiceAsync userService = UserService.Util.getInstance();
 		// Set up the callback object.
 		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{
 			}
 
 			@Override
 			public void onSuccess(Boolean result)
 			{
 				if(result)
 				{
 					dialog.hide();
 					Cookies.setCookie("loggedIn", username);
 					User user = new User(username);
 
 					Storage localStorage = Storage.getLocalStorageIfSupported();
 					localStorage.setItem("loggedIn", user.serialize());
 
 					if(usernameTextBox.getText().equals("Administrator"))
 						main.setContent(new AdminPanel(main), "adminPanel");
 					else
 						main.setContent(new Stream(main, user), "profile-" + username);
 				}
 				else
 					errorLabel.setVisible(true);
 			}
 		};
 
 		userService.createUser(username, password, firstName, lastName, callback);
 	}
 }
