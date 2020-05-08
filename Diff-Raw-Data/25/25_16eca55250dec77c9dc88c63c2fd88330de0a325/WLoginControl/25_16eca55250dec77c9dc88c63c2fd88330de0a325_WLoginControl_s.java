 /*
  * Created on Mar 2, 2015
  */
 package com.delcyon.capo.webapp.widgets;
 
 import eu.webtoolkit.jwt.Signal;
 import eu.webtoolkit.jwt.Signal2;
 import eu.webtoolkit.jwt.WCompositeWidget;
 import eu.webtoolkit.jwt.WContainerWidget;
 import eu.webtoolkit.jwt.WLabel;
 import eu.webtoolkit.jwt.WLength;
 import eu.webtoolkit.jwt.WLength.Unit;
 import eu.webtoolkit.jwt.WLineEdit;
 import eu.webtoolkit.jwt.WLineEdit.EchoMode;
 import eu.webtoolkit.jwt.WPushButton;
 import eu.webtoolkit.jwt.WValidator;
 import eu.webtoolkit.jwt.WValidator.Result;
 
 /**
  * @author jeremiah
  * @version $Id: $
  */
 public class WLoginControl extends WCompositeWidget
 {
 
 	
 	
 	public enum LoginState
 	{
 		LOGGED_IN,
 		LOGGED_OUT,
 		INVALID
 	}
 	 
 	
     private WLineEdit userNameFieldTextEdit;
     private WLineEdit passwordNameFieldTextEdit;
     private WPushButton loginButton;
     private WContainerWidget implementationWidget = new WContainerWidget();
     private Signal2<String, String> loginSignal = new Signal2<>();
     private Signal logoutSignal = new Signal();
     private LoginState loginSate = null;
     
     /**
      * 
      */
     public WLoginControl()
     {
         
         setImplementation(implementationWidget);
         
         addStyleClass("form-inline");
         
         WLabel userNameLabel = new WLabel("Username");
         userNameLabel.addStyleClass("sr-only");
         userNameFieldTextEdit = new WLineEdit();
         userNameFieldTextEdit.setValidator(WValidatorFactory.validator(this::validate));
         userNameFieldTextEdit.setWidth(new WLength(100,Unit.Pixel));
         userNameFieldTextEdit.setPlaceholderText(userNameLabel.getText());
         userNameLabel.setBuddy(userNameFieldTextEdit);
         
         implementationWidget.addWidget(userNameLabel);
         implementationWidget.addWidget(userNameFieldTextEdit);
         
         WLabel passwordLabel = new WLabel("Password");
         passwordLabel.addStyleClass("sr-only");
         passwordNameFieldTextEdit = new WLineEdit();
         passwordNameFieldTextEdit.setEchoMode(EchoMode.Password);
         passwordNameFieldTextEdit.setValidator(WValidatorFactory.validator(this::validate));
         passwordNameFieldTextEdit.setWidth(new WLength(100,Unit.Pixel));
         passwordNameFieldTextEdit.setPlaceholderText(passwordLabel.getText());
         passwordLabel.setBuddy(passwordNameFieldTextEdit);
         implementationWidget.addWidget(passwordLabel);
         implementationWidget.addWidget(passwordNameFieldTextEdit);
         
         loginButton = new WPushButton("Login");
         implementationWidget.addWidget(loginButton);
         loginButton.clicked().addListener(this,this::fireLoginEvent);
         passwordNameFieldTextEdit.enterPressed().addListener(this, this::fireLoginEvent);
         
         setLoginSate(LoginState.LOGGED_OUT);
         
     }
     
     /**
      * don't care about input, only about LoginState
      * @param input
      * @return
      */
     public Result validate(String input)
     {
     	if(getLoginSate() == LoginState.LOGGED_OUT)
         {
             return new WValidator.Result(WValidator.State.InvalidEmpty,"Empty");
         }
         else if(getLoginSate() == LoginState.LOGGED_IN)
         {
             return new WValidator.Result(WValidator.State.Valid);
         }
         else
         {
             return new WValidator.Result(WValidator.State.Invalid,"Invalid Username or Password");
         } 
     }
     
     private void fireLoginEvent()
     {
         if(isLoggedIn())
         {
             logoutSignal.trigger();            
         }
         else
         {            
             loginSignal.trigger(userNameFieldTextEdit.getText(), passwordNameFieldTextEdit.getText());
         }
         userNameFieldTextEdit.validate();
         passwordNameFieldTextEdit.validate();
     }
     
     
     
     /**
      * This will result in to strings being passed to the listener, the first is username, the second is the password
      * @return
      */
     public Signal2<String, String> login()
     {
         return loginSignal;
     }
 
     public Signal logout()
     {
         return logoutSignal;
     }
     
     
     
     /**
      * @return the isLoggedIn    
      */
     public boolean isLoggedIn()
     {
         return (LoginState.LOGGED_IN == getLoginSate());
     }
     
     public LoginState getLoginSate()
 	{
 		return loginSate;
 	}
     
     /**
      * @param This will update the control according to it's correct state of logged in or not.
      */
     public void setLoginSate(LoginState loginSate)
 	{
     	if(loginSate != this.loginSate)
     	{
     		switch (loginSate)
 			{
 			case LOGGED_IN:
 				loginButton.setText("Logout");
                 passwordNameFieldTextEdit.hide();
                 userNameFieldTextEdit.disable();
                userNameFieldTextEdit.setStyleClass("valid");
                 passwordNameFieldTextEdit.setStyleClass("valid");
 				break;
 			case LOGGED_OUT:
 				loginButton.setText("Login");
 				passwordNameFieldTextEdit.show();
 				userNameFieldTextEdit.enable();
				userNameFieldTextEdit.setStyleClass("required");
				passwordNameFieldTextEdit.setStyleClass("required");
 				break;
 			case INVALID:
 				userNameFieldTextEdit.setStyleClass("invalid");
 				passwordNameFieldTextEdit.setStyleClass("invalid");
 				break;
 			}
     	}
     	if(loginSate != null)
     	{
     		this.loginSate = loginSate;
     	}
 	    
 	}
 }
