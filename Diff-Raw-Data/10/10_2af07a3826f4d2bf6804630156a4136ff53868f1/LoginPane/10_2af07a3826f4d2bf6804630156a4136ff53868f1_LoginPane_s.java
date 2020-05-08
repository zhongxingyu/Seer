 /*
  * Mohammad Juma
  * 
  * TCSS 360 - Winter 2013
  * TCSS 360 Project
  * November 11, 2013
  */
 
 package view.login;
 
 import javafx.concurrent.Task;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextField;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 import model.database.DatabaseException;
 import model.login.Login;
 import model.users.User;
 import view.main.MainPane;
 import view.util.GenericPane;
 import view.util.ProgressService;
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
 	private Text signInText;
 
 	/**
 	 * Constructs a new LoginPane pane that extends GridPane and displays a prompt for the user to login or register.
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
 		pane.add(emailTextField, 1, 1);
 
 		signInButton = new Button("Sign in");
 		signInButton.setOnAction(this);
 
 		registerButton = new Button("Register");
 		registerButton.setOnAction(this);
 
 		final HBox buttonHBox = new HBox(10);
 		buttonHBox.setAlignment(Pos.BOTTOM_RIGHT);
 		buttonHBox.getChildren().add(signInButton);
 		buttonHBox.getChildren().add(registerButton);
 		pane.add(buttonHBox, 1, 4);
 
 		signInText = new Text();
 		signInText.setFill(Color.FIREBRICK);
 		pane.add(signInText, 1, 6);
 	}
 
 	/**
 	 * 
 	 * @param event
 	 */
 	@Override
 	public void handle(Event event) {
 		if (event.getSource() == signInButton) {
 			String email = emailTextField.getText().trim();
 			if (email.isEmpty()) {
 				signInText.setText("Forgot to enter an email");
 			} else if (!Validator.isValidEmail(email)) {
 				signInText.setText("Not a valid email");
 			} else {
 				new LoginService(callbacks.getPrimaryStage()).start();
 			}
 		}
 
 		if (event.getSource() == registerButton) {
 		}
 	}
 
 	class LoginService extends ProgressService {
 
 		public LoginService(Stage primaryStage) {
 			super(primaryStage);
 		}
 
 		@Override
 		protected Task<String> createTask() {
 			return new Task<String>() {
 				@Override
 				protected String call() {
 					try {
 						User user = Login.loginUser(emailTextField.getText().trim());
 						LoggedUser.getInstance().setUser(user);
 					} catch (DatabaseException e) {
 						signInText.setText(e.getMessage());
 					}
 					return null;
 				}
 			};
 		}
 
 		@Override
 		protected void succeeded() {
 			super.succeeded();
			callbacks.changeScene(new MainPane());
 		}
 
 	}
 }
