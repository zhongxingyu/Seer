 package com.morevaadin.vaadin7.jugdemo;
 
 import com.vaadin.navigator.Navigator.SimpleViewDisplay;
 import com.vaadin.terminal.WrappedRequest;
 import com.vaadin.ui.Root;
 
 @SuppressWarnings("serial")
 public class DemoRoot extends Root {
 
 	private SimpleViewDisplay display = new SimpleViewDisplay();
 
 	@Override
 	protected void init(WrappedRequest request) {
 
 		display.showView(new LoginView());
 
 		setContent(display);
 	}
 
 	void login(String login, String password) {
 
 		if ("juguser".equals(login)) {
 
 			display.showView(new MainView());
 		}
 	}
 }
