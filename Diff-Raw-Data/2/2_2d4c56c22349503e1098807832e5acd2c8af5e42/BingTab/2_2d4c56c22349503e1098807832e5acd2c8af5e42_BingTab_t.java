 package net.hexid.hexbot.bots;
 
 import java.util.ArrayList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.scene.Node;
 import javafx.scene.control.Button;
 import javafx.scene.control.ButtonBuilder;
 import javafx.scene.control.Label;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.TextField;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Priority;
 import javafx.scene.layout.VBox;
 import jfxtras.labs.scene.control.ListSpinner;
 
 public class BingTab extends net.hexid.hexbot.bot.BotTab {
 	private Button returnToLoginButton, repeatProcessButton, stopProcessButton, loginButton;
 	private ListSpinner<Integer> queryCount, delayMin, delayMax;
 	private int queryCountData, delayMinData, delayMaxData;
 	private String emailData, passwordData;
 	private TextField emailField;
 	private PasswordField passwordField;
 
 	public BingTab() {
 		super();
 	}
 
 	public String getShortName() {
 		return "Bing";
 	}
 
 	protected Node defaultContent() {
 		return createSetupContent(true);
 	}
 
 	public void processExitCode(int exitCode) {
		stopProcessButton.setDisable(true);
 
 		switch(exitCode) {
 		case 2: // login error
 			returnToLoginButton.setDisable(false);
 			break;
 		case 0: // no error
 		case 1: // phantomjs/casperjs error
 		case 3: // internet connection error
 		default: // unknown error
 			returnToLoginButton.setDisable(false);
 			repeatProcessButton.setDisable(false);
 			break;
 		}
 	}
 
 	public ArrayList<String> getBotExecuteData() {
 		// returns the data that will be used
 		// to call the bot in the command line
 		ArrayList<String> data = new ArrayList<>();
 
 		// user-entered data
 		data.add("--email=" + emailData);
 		data.add("--password=" + passwordData);
 		data.add("--queryCount=" + queryCountData);
 		data.add("--delayMin=" + delayMinData);
 		data.add("--delayMax=" + delayMaxData);
 		return data;
 	}
 
 	protected Node[] createBottomOutputContent() {
 		repeatProcessButton = ButtonBuilder.create().text("Repeat")
 				.onAction(new EventHandler<ActionEvent>() {
 					public void handle(ActionEvent e) {
 						createProcess();
 					}
 				}).disable(true).maxWidth(Double.MAX_VALUE).build();
 		HBox.setHgrow(repeatProcessButton, Priority.ALWAYS);
 
 		returnToLoginButton = ButtonBuilder.create().text("Setup")
 				.onAction(new EventHandler<ActionEvent>() {
 					public void handle(ActionEvent e) {
 						setContent(createSetupContent(false));
 					}
 				}).disable(true).maxWidth(Double.MAX_VALUE).build();
 		HBox.setHgrow(returnToLoginButton, Priority.ALWAYS);
 
 		stopProcessButton = ButtonBuilder.create().text("Stop")
 				.onAction(new EventHandler<ActionEvent>() {
 					public void handle(ActionEvent e) {
 						process.destroy(); // exit code may vary (143 encountered during tests)
 					}
 				}).build();
 
 		return new Node[]{repeatProcessButton, returnToLoginButton, stopProcessButton};
 	}
 
 	public VBox createSetupContent(boolean setDefaultValues) {
 		// create a setup pane withT/withoutF default values
 		VBox tabContent = new VBox();
 		Insets inset = new Insets(17.5d, 15.0d, 0.0d, 15.0d);
 
 		HBox email = new HBox(3);
 		VBox.setMargin(email, inset);
 		Label emailLabel = new Label("Email: ");
 		HBox.setHgrow(emailLabel, Priority.NEVER);
 		emailField = new TextField();
 		HBox.setHgrow(emailField, Priority.ALWAYS);
 		email.getChildren().addAll(emailLabel, emailField);
 
 		HBox password = new HBox(3);
 		VBox.setMargin(password, inset);
 		Label passwordLabel = new Label("Password: ");
 		HBox.setHgrow(passwordLabel, Priority.NEVER);
 		passwordField = new PasswordField();
 		passwordField.setOnAction(new EventHandler<ActionEvent>() {
 			@Override public void handle(ActionEvent e) {
 				loginButton.fire();
 			}
 		});
 		HBox.setHgrow(passwordField, Priority.ALWAYS);
 		password.getChildren().addAll(passwordLabel, passwordField);
 
 		HBox query = new HBox(3);
 		VBox.setMargin(query, inset);
 		Label queryLabel = new Label("Queries: ");
 		HBox.setHgrow(queryLabel, Priority.NEVER);
 		queryCount = new ListSpinner<>(0, 200, 1);
 		HBox.setHgrow(queryCount, Priority.ALWAYS);
 		query.getChildren().addAll(queryLabel, queryCount);
 
 		HBox delay = new HBox(3);
 		VBox.setMargin(delay, inset);
 		Label delayLabel = new Label("Delay (Secs): ");
 		HBox.setHgrow(delayLabel, Priority.NEVER);
 		delayMin = new ListSpinner<>(5, 120, 1);
 		HBox.setHgrow(delayMin, Priority.ALWAYS);
 		Label delayToLabel = new Label(" to ");
 		HBox.setHgrow(delayToLabel, Priority.NEVER);
 		delayMax = new ListSpinner<>(5, 120, 1);
 		HBox.setHgrow(delayMax, Priority.ALWAYS);
 		delay.getChildren().addAll(delayLabel, delayMin, delayToLabel, delayMax);
 
 		HBox button = new HBox(7.5d);
 		VBox.setMargin(button, inset);
 		loginButton = ButtonBuilder.create()
 				.onAction(new EventHandler<ActionEvent>() {
 					public void handle(ActionEvent e) {
 						emailData = emailField.getText();
 						passwordData = passwordField.getText();
 						queryCountData = queryCount.getValue();
 						delayMinData = delayMin.getValue();
 						delayMaxData = delayMax.getValue();
 						createProcess();
 					}
 				}).text("Login").maxWidth(Double.MAX_VALUE).build();
 		HBox.setHgrow(loginButton, Priority.ALWAYS);
 		button.getChildren().addAll(loginButton);
 
 		if(setDefaultValues) { // set the default values
 			queryCount.setValue(0);
 			delayMin.setValue(20);
 			delayMax.setValue(40);
 		} else { // set previous values
 			emailField.setText(emailData);
 			passwordField.setText(passwordData);
 			queryCount.setValue(queryCountData);
 			delayMin.setValue(delayMinData);
 			delayMax.setValue(delayMaxData);
 		}
 
 		// add all the elements to the container that will be added to the tab
 		tabContent.getChildren().addAll(email, password, query, delay, button);
 		return tabContent;
 	}
 }
