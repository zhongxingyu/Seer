 
 package view.login;
 
 import javafx.concurrent.Service;
 import javafx.concurrent.Task;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Node;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.ProgressIndicator;
 import javafx.scene.control.TextField;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 import model.database.DatabaseException;
 import model.login.Login;
 import model.users.User;
 import view.main.MainPane;
 import view.util.CustomProgressIndicator;
 import view.util.GenericPane;
 import view.util.StatusText;
 import view.util.Validator;
 import controller.user.LoggedUser;
 
 /**
  * JavaFX pane for displaying the login window.
  * 
  * @author Mohammad Juma
  * @version 11-11-2013
  */
 public class LoginPane extends GenericPane<GridPane> implements EventHandler {
     
     /**
      * Text component for showing the welcome text.
      */
     private Text welcomeText;
     
     /**
      * Label for showing the email label text.
      */
     private Label emailLabel;
     
     /**
      * TextField for inputing the email.
      */
     private TextField emailTextField;
     
     /**
      * Button for signing into the application.
      */
     private Button signInButton;
     
     /**
      * Button for registering for an account.
      */
     private Button registerButton;
     
     /**
      * Text for displaying sign in errors.
      */
     private StatusText signInText;
     
     /**
      * Progress indicator to show during login process.
      */
     private CustomProgressIndicator progressIndicator;
     
     /**
      * Constructs a new LoginPane pane that extends GridPane and displays a prompt for the user
      * to login or register.
      */
     public LoginPane() {
         super(new GridPane());
         pane.setAlignment(Pos.CENTER);
         pane.setHgap(10);
         pane.setVgap(10);
         pane.setPadding(new Insets(25, 25, 25, 25));
         
         create();
     }
     
     /**
      * Creates the main components of the LoginPane pane.
      */
     private void create() {
         welcomeText = new Text("Welcome");
         welcomeText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
         pane.add(welcomeText, 0, 0, 2, 1);
         
         emailLabel = new Label("Email:");
         pane.add(emailLabel, 0, 1);
         
         emailTextField = new TextField();
         emailTextField.setOnKeyReleased(this);
         pane.add(emailTextField, 1, 1);
         
         signInButton = new Button("Sign in");
         signInButton.setOnAction(this);
         
         registerButton = new Button("Register");
         registerButton.setOnAction(this);
         
         final HBox buttonHBox = new HBox(10);
         buttonHBox.setAlignment(Pos.BOTTOM_RIGHT);
         buttonHBox.getChildren()
                   .add(signInButton);
         buttonHBox.getChildren()
                   .add(registerButton);
         pane.add(buttonHBox, 1, 4);
         
         signInText = new StatusText();
         pane.add(signInText, 1, 6);
         
         progressIndicator = new CustomProgressIndicator();
         pane.add(progressIndicator, 1, 1);
         
     }
     
     /**
      * Sets the progress indicator visible.
      * 
      * @param show the new progress indicator visible
      */
     private void setProgressIndicatorVisible(final boolean show) {
         for (Node n : pane.getChildren()) {
             if (n instanceof ProgressIndicator) {
                 n.setVisible(show);
             }
             else {
                 n.setVisible(!show);
             }
         }
     }
     
     /**
      * Handles action events from the user.
      * 
      * @param event the event
      */
     @Override
     public void handle(final Event event) {
        signInText.clear();
        
         if (event.getSource() == emailTextField) {
             KeyEvent keyEvent = (KeyEvent) event;
             if (keyEvent.getCode() == KeyCode.ENTER) {
                 login();
             }
         }
         
         if (event.getSource() == signInButton) {
             login();
         }
         
         if (event.getSource() == registerButton) {
             callbacks.changeScene(new RegisterPane());
         }
     }
     
     /**
      * Login.
      */
     private void login() {
         String email = emailTextField.getText()
                                      .trim();
         if (email.isEmpty()) {
             signInText.setErrorText("Forgot to enter an email");
         }
         else if (!Validator.isValidEmail(email)) {
             signInText.setErrorText("Not a valid email");
         }
         else {
             new LoginService().start();
         }
     }
     
     /**
      * Private inner class for login into the application.
      * 
      * @author Tim Mikeladze
      * @version 11-17-2013
      */
     private class LoginService extends Service<String> {
         
         private boolean success;
         
         /**
          * Starts a new task for logging in.
          * 
          * @return the task
          */
         @Override
         protected Task<String> createTask() {
             return new Task<String>() {
                 
                 /**
                  * Calls the new task.
                  */
                 @Override
                 protected String call() {
                     try {
                         User user = Login.loginUser(emailTextField.getText()
                                                                   .trim());
                         LoggedUser.getInstance()
                                   .setUser(user);
                         success = true;
                     }
                     catch (DatabaseException e) {
                         signInText.setErrorText(e.getMessage());
                     }
                     return null;
                 }
             };
         }
         
         @Override
         protected void cancelled() {
             setProgressIndicatorVisible(false);
             super.cancelled();
         }
         
         @Override
         protected void executeTask(final Task<String> task) {
             progressIndicator.progressProperty()
                              .bind(task.progressProperty());
             setProgressIndicatorVisible(true);
             super.executeTask(task);
         }
         
         @Override
         protected void failed() {
             setProgressIndicatorVisible(false);
             super.failed();
         }
         
         @Override
         protected void succeeded() {
             super.succeeded();
             setProgressIndicatorVisible(false);
             if (success) {
                 callbacks.changeScene(new MainPane(callbacks));
             }
             
         }
         
     }
 }
