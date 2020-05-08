 package com.morevaadin.vaadin7.jugdemo;
 
 import com.vaadin.navigator.View;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.PasswordField;
 import com.vaadin.ui.Root;
 import com.vaadin.ui.TextField;
 
 @SuppressWarnings("serial")
 public class LoginView extends CustomComponent implements View {
 
 	private TextField loginField = new TextField("Login", "juguser");
 
 	private PasswordField passwordField = new PasswordField("Password");
 
 	public LoginView() {
 
 		FormLayout layout = new FormLayout();
 
 		layout.setMargin(true);
 		layout.setSpacing(true);
 
 		setCompositionRoot(layout);
 
 		layout.addComponent(loginField);
 		layout.addComponent(passwordField);
 
 		Button button = new Button("Enter");
 
 		button.addListener(new ClickListener() {
 
 			public void buttonClick(ClickEvent event) {
 
 				((DemoRoot) Root.getCurrent()).login(loginField.getValue(),
 						passwordField.getValue());
 			}
 		});
 
 		layout.addComponent(button);

		Root.getCurrent().getPage().setTitle("Login...");
 	}
 
 	public void navigateTo(String fragmentParameters) {
 	}
 }
