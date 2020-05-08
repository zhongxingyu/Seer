 package org.timadorus.webapp.client.verify_mail;
 
 import java.util.Date;
 
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.eventhandling.events.ShowVerifyMailEvent;
 import org.timadorus.webapp.client.eventhandling.handler.ShowHandler;
 import org.timadorus.webapp.client.rpc.service.UserService;
 import org.timadorus.webapp.client.rpc.service.UserServiceAsync;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.HistoryListener;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * This panel is essential while verifying the E-Mail to activate a user account.
  * 
  * @author Malte Kantak
  */
 @SuppressWarnings("deprecation")
 public final class VerifyMailPanel extends FormPanel implements HistoryListener, ShowHandler {
 
   private final int gridRows = 4;
 
   private final int gridColumns = 2;
 
   private Panel panel = new VerticalPanel();
 
   private Grid grid = new Grid(gridRows, gridColumns);
 
   private HTML headline = new HTML("<h1>E-Mail Addresse bestätigen</h1>");
 
   private TextBox userBox = new TextBox();
 
   private PasswordTextBox passBox = new PasswordTextBox();
 
   private Label userLabel = new Label("Benutzername");;
 
   private Label passLabel = new Label("Passwort");
 
   private HTML errorHTML = new HTML();
 
   private Button submit = new Button("Bestätigen");
 
   public User user;
 
   private DefaultTimadorusWebApp entry;
 
   private String activationCode;
 
   private static final long TWO_MIN = 1000 * 60 * 2;
 
   /**
    * Private constructor.
    * 
    * @param entryIn
    *          The timadorus web app object
    */
   private VerifyMailPanel(DefaultTimadorusWebApp entryIn) {
     super();
 
     this.entry = entryIn;
     this.user = new User();
 
     entry.addHandler(ShowVerifyMailEvent.SHOWDIALOG, this);
 
     setupHistory();
 
     activationCode = Window.Location.getParameter("activationCode");
 
     // CHECKSTYLE OFF
     grid.setWidget(0, 0, userLabel);
     grid.setWidget(0, 1, userBox);
     grid.setWidget(1, 0, passLabel);
     grid.setWidget(1, 1, passBox);
     grid.setWidget(2, 1, errorHTML);
     grid.setWidget(3, 1, submit);
     // CHECKSTYLE ON
 
     /**
      * Create a handler for the sendButton and nameField
      */
     class MyHandler implements ClickHandler, KeyUpHandler {
       /**
        * Will be triggered if button was clicked.
        * 
        * @param event
        *          The click event
        */
       public void onClick(ClickEvent event) {
         handleEvent();
       }
 
       /**
        * Will be triggered if the "Enter" button was hit while focused in an inputfield.
        * 
        * @param event
        *          The KeyUpEvent
        */
       public void onKeyUp(KeyUpEvent event) {
         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
           handleEvent();
         }
       }
 
       /**
        * Handles the actual event. Sends username and password to server if they wheren't empty.
        */
       private void handleEvent() {
         clearError();
         getUser().setUsername(userBox.getText());
         getUser().setPassword(passBox.getText());
 
         if (getUser().getUsername().equals("") || user.getPassword().equals("")) {
           verifyInvalid("Bitte Felder ausfüllen!");
           History.newItem("verfyMail");
         } else {
           sendToServer();
           userBox.setText("");
           passBox.setText("");
         }
       }
 
       /**
        * Send username, password and activation code to the server to verify the users e-mail address.
        */
       private void sendToServer() {
        
        //TODO Command-Pattern
        
         UserServiceAsync userServiceAsync = GWT.create(UserService.class);
 
         AsyncCallback<String> asyncCallback = new AsyncCallback<String>() {
           public void onSuccess(String result) {
             onSuccessCallback(result);
           }
 
           public void onFailure(Throwable caught) {
             onFailureCallback(caught);
           }
         };
 
         userServiceAsync.verifyMail(activationCode, user, asyncCallback);
       
       }
     }
 
     // Add a handler to send the name to the server
     MyHandler handler = new MyHandler();
     submit.addClickHandler(handler);
     userBox.addKeyUpHandler(handler);
     passBox.addKeyUpHandler(handler);
 
     panel.add(headline);
     panel.add(new Label("ActivationCode: " + activationCode));
     panel.add(grid);
 
    RootPanel.get("content").clear();
    RootPanel.get("content").add(panel);
     setStyleName("formPanel");
   }
 
   private void onSuccessCallback(String result) {
     if (result != null) {
       if (result.equals(User.USER_VERIFIED)) {
         getUser().setActive(true);
         entry.setLoggedin(true);
         Cookies.setCookie("session", result, new Date(System.currentTimeMillis() + TWO_MIN));
         System.out.println("Login session => " + result);
         History.newItem("login");
       } else {
         handleError(result);
       }
     }
   }
 
   private void onFailureCallback(Throwable caught) {
     getTimadorus().showDialogBox("Fehlermeldung", "Fehler bei der Anmeldung");
     verifyInvalid("Fehler bei der Anmeldung!");
     History.newItem("verifyMail");
   }
 
   /**
    * Handles error responses from the server and displays error messages.
    * 
    * @param result
    *          The result from the server
    */
   private void handleError(String result) {
     if (result.equals(User.USER_ALREADY_ACTIVATED)) {
       verifyInvalid("Das Benutzerkonto ist schon aktiviert worden!");
       submit.setEnabled(true);
       getTimadorus().setLoggedin(false);
     } else if (result.equals(User.USER_WRONG_CODE)) {
       verifyInvalid("Der Aktivierungscode ist ungültig!");
       submit.setEnabled(true);
       getTimadorus().setLoggedin(false);
     } else if (result.equals(User.USER_INVALID)) {
       verifyInvalid("Username und/oder Passwort falsch!");
       submit.setEnabled(true);
       getTimadorus().setLoggedin(false);
     }
   }
 
   /**
    * Returns an instance of this class.
    * 
    * @param entry
    *          The timadorus web app object
    * @return An instance of this class
    */
   public static final VerifyMailPanel getVerifyMailPanel(DefaultTimadorusWebApp entry) {
     return new VerifyMailPanel(entry);
   }
 
   /**
    * Adds this instance to the history listener.
    */
   private void setupHistory() {
     History.addHistoryListener(this);
   }
 
   /**
    * If the verification was invalid, the error message will be formatted and inserted here.
    * 
    * @param message
    *          The error message
    */
   private void verifyInvalid(String message) {
     errorHTML.setHTML("<span class=\"error\">" + message + "</span>");
   }
 
   /**
    * Clears all errors from the web-site.
    */
   private void clearError() {
     errorHTML.setHTML("");
     Element info = DOM.getElementById("info");
     if (info != null) {
       info.getParentElement().removeChild(info);
     }
   }
 
   /**
    * Unimplemented method.
    * 
    * @param historyToken
    *          The history token
    */
   @Override
   public void onHistoryChanged(String historyToken) {
   }
 
   /**
    * Returns the timadorus web app object.
    * 
    * @return The timadorus web app object
    */
   private DefaultTimadorusWebApp getTimadorus() {
     return entry;
   }
 
   /**
    * Returns the current User object.
    * 
    * @return The current User object.
    */
   private User getUser() {
     return user;
   }
 
   @Override
   public void show() {
     RootPanel.get("content").clear();
    RootPanel.get("content").add(new Label("E-Mail bestätigen"));
     RootPanel.get("content").add(this);
   }
 }
