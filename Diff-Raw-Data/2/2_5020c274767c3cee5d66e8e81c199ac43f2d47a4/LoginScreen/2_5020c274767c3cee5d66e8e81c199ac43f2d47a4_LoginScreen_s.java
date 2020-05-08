 package org.jsc.client;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * A specialized extension of BaseScreen intended to handle input for logging
  * into the application.
  * 
  * @author Matt Jones
  */
 public class LoginScreen extends BaseScreen {
 
     private static final long DURATION = 1000 * 60 * 30;
 
     private HorizontalPanel screen;
     private VerticalPanel loginPanel;
     private VerticalPanel introPanel;
     private TextBox username;
     private TextBox password;
     private Button signinButton;
     private SkaterRegistrationServiceAsync regService;
     
     /**
      * Construct the login screen, recording the loginSession for later reference.
      * @param loginSession to be used for user information in later steps
      */
     public LoginScreen(LoginSession loginSession, HandlerManager eventBus) {
         super(loginSession, eventBus);
         layoutScreen();
         this.setContentPanel(screen);
     }
 
     /**
      * Lay out the user interface widgets on the screen.
      */
     private void layoutScreen() {
         this.setScreenTitle("Sign In");
         this.setStyleName("jsc-twopanel-screen");
         
         screen = new HorizontalPanel();
         
         createLoginPanel();
         Label spacer = new Label("");
         spacer.addStyleName("jsc-spacer");
         createIntroPanel();
         Hyperlink aboutLink = new Hyperlink("About Skate...", "about");
         aboutLink.addStyleName("jsc-about-link");
         screen.add(loginPanel);
         screen.add(spacer);
         screen.add(introPanel);
         screen.add(aboutLink);
     }
     
     /**
      * Fill in the GUI for the Login screen
      */
     private void createLoginPanel() {
         loginPanel = new VerticalPanel();
         loginPanel.addStyleName("jsc-leftpanel");
         Label signin = new Label("Sign In");
         signin.addStyleName("jsc-screentitle");
         Label usernameLabel = new Label("Username:");
         loginPanel.add(usernameLabel);
         usernameLabel.addStyleName("jsc-fieldlabel-left");
         username = new TextBox();
         username.addStyleName("jsc-field");
         loginPanel.add(username);
         Label pwLabel = new Label("Password:");
         loginPanel.add(pwLabel);
         pwLabel.addStyleName("jsc-fieldlabel-left");
         password = new PasswordTextBox();
         password.addStyleName("jsc-field");
         loginPanel.add(password);
         loginPanel.add(new Label(" "));
         signinButton = new Button("Sign In");
         signinButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 authenticate();
             }
         });
         signinButton.addStyleName("jsc-button-right");
         loginPanel.add(signinButton);
         
         Hyperlink newAccountLink = new Hyperlink("Need a New Account?", "settings");
         newAccountLink.addStyleName("jsc-link-right");
         loginPanel.add(newAccountLink);
         
         Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?", "resetpass");
         forgotPasswordLink.addStyleName("jsc-link-right");
         loginPanel.add(forgotPasswordLink);
     }
     
     /**
      * Check the user credentials and set up the loginSession if valid.
      */
     private void authenticate() {
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
 
         // Set up the callback object.
         AsyncCallback<LoginSession> callback = new AsyncCallback<LoginSession>() {
             public void onFailure(Throwable caught) {
                 // TODO: Do something with errors.
                 loginSession.setAuthenticated(false);
                 loginSession.setSessionId("invalid");
                 GWT.log("Authentication failed to complete.", null);
                 password.setText("");
             }
 
             public void onSuccess(LoginSession newLoginSession) {
                 // Clear the password box
                 password.setText("");
                 
                 if (newLoginSession != null) {
                     // Login succeeded
                     loginSession.setPerson(newLoginSession.getPerson());
                     loginSession.setSessionId(newLoginSession.getSessionId());
                     loginSession.setAuthenticated(newLoginSession.isAuthenticated());
                     Date expires = new Date(System.currentTimeMillis() + DURATION);
                     Cookies.setCookie("jscSession", loginSession.getSessionId(), expires, null, "/", false);
                     Cookies.setCookie("jscPid", Long.toString(loginSession.getPerson().getPid()), expires, null, "/", false);
                     
                     long pid = loginSession.getPerson().getPid();
                     GWT.log("Login succeeded: " + pid, null);
                     GWT.log(loginSession.getPerson().toString(), null);
 
                     // Change our application state to the classes screen
                     History.newItem("register");
                 } else {
                     loginSession.setAuthenticated(false);
                    setMessage("Incorrect username or password. Please try again.");
                 }
             }
         };
 
         // Make the call to the registration service.
         regService.authenticate(username.getText(), password.getText(), callback);
     }
     
     /**
      * Set up the Introductory dialog.
      */
     private void createIntroPanel() {
         introPanel = new VerticalPanel();
         introPanel.addStyleName("jsc-rightpanel");
         StringBuffer intro = new StringBuffer();
         intro.append("<p class=\"jsc-step\">Register for the <a href=\"http://juneauskatingclub.org\">Juneau Skating Club</a>!</p>");
         intro.append("<p class=\"jsc-text\">This is our new web-based site for class registration and management. Using this site, you can register for new classes after you have created an account and signed in. Some of the functions available from this site include:</p>");
         intro.append("<p class=\"jsc-text\"><ul><li>Register for classes</li><li>Pay registration fees</li><li>View registered classes</li></ul></p>");
         
         intro.append("<p class=\"jsc-text\">If you do not have an account, you can create a <a href=\"/SkaterData.html#settings\">New Account</a>.</p>");
         intro.append("<p class=\"jsc-text\">You can register for new classes after you have <a href=\"/SkaterData.html#signout\">Signed In</a>.</p>");
 
         HTMLPanel introHTML = new HTMLPanel(intro.toString());
         introPanel.add(introHTML);
     }
 }
