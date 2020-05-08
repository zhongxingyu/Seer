 package org.timadorus.webapp.client.register;
 
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.eventhandling.events.ShowRegisterEvent;
 import org.timadorus.webapp.client.eventhandling.handler.ShowHandler;
 import org.timadorus.webapp.client.service.Service;
 import org.timadorus.webapp.client.service.ServiceAsync;
 import org.timadorus.webapp.client.service.ServiceType;
 import org.timadorus.webapp.shared.Action;
 import org.timadorus.webapp.shared.Response;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.HistoryListener;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 //FormPanel for Registering
 @SuppressWarnings("deprecation")
 public class RegisterPanel extends FormPanel implements HistoryListener, ShowHandler {
 
   private final int rows = 9;
 
   private final int columns = 3;
 
   Grid grid = new Grid(rows, columns);
 
   Button submitButton = new Button("Registrieren");
 
   private TextBox vornameTextBox = new TextBox();
 
   private TextBox nachnameTextBox = new TextBox();
 
   private TextBox geburtstagTextBox = new TextBox();
 
   private TextBox emailTextBox = new TextBox();
 
   private TextBox emailRepeatTextBox = new TextBox();
 
   private TextBox usernameTextBox = new TextBox();
 
   private PasswordTextBox passwordTextBox = new PasswordTextBox();
 
   private PasswordTextBox passwordRepeatTextBox = new PasswordTextBox();
 
   private HTML vornameHTML = new HTML();
 
   private HTML nachnameHTML = new HTML();
 
   private HTML geburtstagHTML = new HTML();
 
   private HTML emailHTML = new HTML();
 
   private HTML emailRepeatHTML = new HTML();
 
   private HTML usernameHTML = new HTML();
 
   private HTML passwordHTML = new HTML();
 
   private HTML passwordRepeatHTML = new HTML();
 
   private DefaultTimadorusWebApp entry;
 
   /**
    * Create a remote service proxy to talk to the server-side Greeting service.
    */
   private final ServiceAsync<User, String> myService = GWT.create(Service.class);
 
   public DefaultTimadorusWebApp getEntry() {
     return entry;
   }
 
   public static final RegisterPanel getRegisterPanel(DefaultTimadorusWebApp entry) {
     return new RegisterPanel(entry);
   }
 
   private void setupHistory() {
     History.addHistoryListener(this);
   }
 
   public RegisterPanel(DefaultTimadorusWebApp entry) {
     super();
     this.entry = entry;
     entry.addHandler(ShowRegisterEvent.SHOWDIALOG, this);
     setupHistory();
 
     grid.setWidget(0, 0, new Label("Vorname"));
     grid.setWidget(1, 0, new Label("Nachname"));
     grid.setWidget(2, 0, new Label("Geburtstag (dd.mm.jjjj)"));
     grid.setWidget(3, 0, new Label("Email"));
     grid.setWidget(4, 0, new Label("Email (Wiederholung)"));
     grid.setWidget(5, 0, new Label("Benutzername"));
     grid.setWidget(6, 0, new Label("Passwort"));
     grid.setWidget(7, 0, new Label("Passwort (Wiederholung)"));
 
     grid.setWidget(0, 1, vornameTextBox);
     grid.setWidget(1, 1, nachnameTextBox);
     grid.setWidget(2, 1, geburtstagTextBox);
     grid.setWidget(3, 1, emailTextBox);
     grid.setWidget(4, 1, emailRepeatTextBox);
     grid.setWidget(5, 1, usernameTextBox);
     grid.setWidget(6, 1, passwordTextBox);
     grid.setWidget(7, 1, passwordRepeatTextBox);
 
     grid.setWidget(0, 2, vornameHTML);
     grid.setWidget(1, 2, nachnameHTML);
     grid.setWidget(2, 2, geburtstagHTML);
     grid.setWidget(3, 2, emailHTML);
     grid.setWidget(4, 2, emailRepeatHTML);
     grid.setWidget(5, 2, usernameHTML);
     grid.setWidget(6, 2, passwordHTML);
     grid.setWidget(7, 2, passwordRepeatHTML);
 
     grid.setWidget(8, 1, submitButton);
 
     vornameTextBox.setText("Vorname");
     nachnameTextBox.setText("Nachname");
     geburtstagTextBox.setText("01.01.1980");
     emailTextBox.setText("me@home.de");
     emailRepeatTextBox.setText("me@home.de");
     usernameTextBox.setText("Username");
     passwordTextBox.setText("passwort");
     passwordRepeatTextBox.setText("passwort");
 
     RegisterHandler handler = new RegisterHandler(this);
     submitButton.addClickHandler(handler);
     vornameTextBox.addKeyUpHandler(handler);
     nachnameTextBox.addKeyUpHandler(handler);
     geburtstagTextBox.addKeyUpHandler(handler);
     emailTextBox.addKeyUpHandler(handler);
     emailRepeatTextBox.addKeyUpHandler(handler);
     usernameTextBox.addKeyUpHandler(handler);
     passwordTextBox.addKeyUpHandler(handler);
     passwordRepeatTextBox.addKeyUpHandler(handler);
 
     setWidget(grid);
     setStyleName("formPanel");
   }
 
   /**
    * If the registration was invalid, the error message will be formatted and inserted here.
    * 
    * @param error
    *          An integer value representing the error message (Error code)
    */
   public void registerInvalid(int error) {
     switch (error) {
     case User.VORNAME_NACHNAME_EMPTY:
       setText(vornameHTML, "Bitte ausfüllen!");
       setText(nachnameHTML, "Bitte ausfüllen!");
       break;
     case User.GEBURTSTAG_EMPTY:
       setText(geburtstagHTML, "Bitte ausfüllen!");
       break;
     case User.GEBURTSTAG_AGE:
       setText(geburtstagHTML, "Du bist leider zu jung!");
       break;
     case User.GEBURTSTAG_FORMAT:
       setText(geburtstagHTML, "Das Format ist ungültig!");
       break;
     case User.GEBURTSTAG_FAULT:
       setText(geburtstagHTML, "Das Datum ist ungültig");
       break;
     case User.EMAIL_EMPTY:
       setText(emailHTML, "Bitte ausfüllen!");
       break;
     case User.EMAILREPEAT_EMPTY:
       setText(emailRepeatHTML, "Bitte ausfüllen!");
       break;
     case User.EMAIL_FAULT:
       setText(emailHTML, "Stimmen nicht überein!");
       setText(emailRepeatHTML, "Stimmen nicht überein!");
       break;
     case User.EMAIL_FORMAT:
       setText(emailHTML, "Das Format ist ungültig!");
       setText(emailRepeatHTML, "Das Format ist ungültig!");
       break;
     case User.USERNAME_EMPTY:
       setText(usernameHTML, "Bitte ausfüllen!");
       break;
     case User.USERNAME_FAULT:
       setText(usernameHTML, "Benutzername bereits vergeben!");
       break;
     case User.PASSWORD_EMPTY:
       setText(passwordHTML, "Bitte ausfüllen!");
       break;
     case User.PASSWORDREPEAT_EMPTY:
       setText(passwordRepeatHTML, "Bitte ausfüllen!");
       break;
     case User.PASSWORD_FAULT:
       setText(passwordHTML, "Stimmen nicht überein!");
       setText(passwordRepeatHTML, "Stimmen nicht überein!");
       break;
     default:
       break;
     }
   }
 
   public void tryRegisterUser() {
     submitButton.setEnabled(false);
     setText(vornameHTML, "");
     setText(nachnameHTML, "");
     setText(geburtstagHTML, "");
     setText(emailHTML, "");
     setText(emailRepeatHTML, "");
     setText(usernameHTML, "");
     setText(passwordHTML, "");
     setText(passwordRepeatHTML, "");
     User register = new User(vornameTextBox.getText(), nachnameTextBox.getText(), geburtstagTextBox.getText(),
                              emailTextBox.getText(), usernameTextBox.getText(), passwordTextBox.getText());
 
     if (!register.isValid()) {
       if (passwordRepeatTextBox.getText().length() == 0) {
         registerInvalid(User.PASSWORDREPEAT_EMPTY);
         passwordRepeatTextBox.setFocus(true);
       }
       if (register.getPassword().length() == 0) {
         registerInvalid(User.PASSWORD_EMPTY);
         passwordTextBox.setFocus(true);
       }
       if (register.getUsername().length() == 0) {
         registerInvalid(User.USERNAME_EMPTY);
         usernameTextBox.setFocus(true);
       }
       if (emailRepeatTextBox.getText().length() == 0) {
         registerInvalid(User.EMAILREPEAT_EMPTY);
         emailRepeatTextBox.setFocus(true);
       }
       if (register.getEmail().length() == 0) {
         registerInvalid(User.EMAIL_EMPTY);
         emailTextBox.setFocus(true);
       }
       if (register.getGeburtstag().length() == 0) {
         registerInvalid(User.GEBURTSTAG_EMPTY);
         geburtstagTextBox.setFocus(true);
       }
       if (register.getVorname().length() == 0 && register.getNachname().length() == 0) {
         registerInvalid(User.VORNAME_NACHNAME_EMPTY);
         vornameTextBox.setFocus(true);
       }
       History.newItem("register");
 
     } else if (!emailTextBox.getText().equals(emailRepeatTextBox.getText())) {
       registerInvalid(User.EMAIL_FAULT);
     } else if (!passwordTextBox.getText().equals(passwordRepeatTextBox.getText())) {
       registerInvalid(User.PASSWORD_FAULT);
     } else {
 
       sendToServer(register);
     }
     submitButton.setEnabled(true);
   }
 
   private void sendToServer(User register) {
     Action<User> action = new Action<User>(ServiceType.REGISTER, register);
     AsyncCallback<Response<String>> response = new AsyncCallback<Response<String>>() {
 
       @Override
       public void onFailure(Throwable caught) {
         handelFailureRegister(caught);
       }
 
       @Override
       public void onSuccess(Response<String> result) {
         handelSuccessRegister(result);
       }
     };
 
     myService.execute(action, response);
   }
 
   private void handelFailureRegister(Throwable caught) {
     registerInvalid(0);
    System.out.println(caught);
   }
 
   private void handelSuccessRegister(Response<String> response) {
     String result = response.getResult();
     if (result != null) {
       String[] tmp = result.split("_");
 
       int value = Integer.parseInt(tmp[0]);
       if (value == User.OK) {
         RootPanel.get("content").clear();
         String[] href = Window.Location.getHref().split("#");
         String linker = href[0].contains("?") ? "&" : "?";
         getEntry().showDialogBox("ActivationLink", href[0] + linker + "activationCode=" + tmp[1]);
         History.newItem("welcome");
       } else {
         if (value >= User.PASSWORD_FAULT) {
           value -= User.PASSWORD_FAULT;
         }
         if (value >= User.USERNAME_FAULT) {
           value -= User.USERNAME_FAULT;
           registerInvalid(User.USERNAME_FAULT);
         }
         if (value >= User.EMAIL_FORMAT) {
           value -= User.EMAIL_FORMAT;
           registerInvalid(User.EMAIL_FORMAT);
         }
         if (value >= User.GEBURTSTAG_AGE) {
           value -= User.GEBURTSTAG_AGE;
           registerInvalid(User.GEBURTSTAG_AGE);
         }
         if (value >= User.GEBURTSTAG_FORMAT) {
           value -= User.GEBURTSTAG_FORMAT;
           registerInvalid(User.GEBURTSTAG_FORMAT);
         }
         if (value >= User.GEBURTSTAG_FAULT) {
           value -= User.GEBURTSTAG_FAULT;
           registerInvalid(User.GEBURTSTAG_FAULT);
         }
         submitButton.setEnabled(true);
       }
     } else {
       submitButton.setEnabled(true);
     }
   }
 
   private void setText(HTML label, String message) {
     label.setHTML("<span class=\"error\">" + message + "</span>");
   }
 
   @Override
   public void onHistoryChanged(String historyToken) {
   }
 
   @Override
   public void show() {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(new Label("Benutzregistrierung"));
     RootPanel.get("content").add(this);
   }
 }
