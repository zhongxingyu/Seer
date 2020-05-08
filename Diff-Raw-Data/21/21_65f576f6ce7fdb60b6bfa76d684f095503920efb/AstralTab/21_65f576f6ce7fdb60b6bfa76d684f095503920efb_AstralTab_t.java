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
 
 public class AstralTab extends net.hexid.hexbot.bot.gui.BotTab {
 	private Button returnToLoginButton, repeatProcessButton, loginButton;
 	private String usernameData, passwordData;
 	private TextField usernameField;
 	private PasswordField passwordField;
 
 	public AstralTab() {
 		super();
 	}
 
 	public String getShortName() {
 		return "Astral";
 	}
 
 	protected Node defaultContent() {
 		return createSetupContent(true);
 	}
 
 	public void processExitCode(int exitCode) {
 		returnToLoginButton.setDisable(false);
 		if(exitCode != 2) repeatProcessButton.setDisable(false);
 	}
 
 	public ArrayList<String> getBotExecuteData() {
 		ArrayList<String> data = new ArrayList<String>();
 		data.add("--username="+usernameData);
 		data.add("--password="+passwordData);
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
 		
 		return new Node[]{repeatProcessButton, returnToLoginButton};
 	}
 
 	private VBox createSetupContent(boolean setDefaultValues) {
 		// create a setup pane withT/withoutF default values
 		VBox tabContent = new VBox();
 		Insets inset = new Insets(17.5d, 15.0d, 0.0d, 15.0d);
 
 		HBox username = new HBox(3);
 		VBox.setMargin(username, inset);
 		Label usernameLabel = new Label("Username: ");
 		HBox.setHgrow(usernameLabel, Priority.NEVER);
 		usernameField = new TextField();
 		HBox.setHgrow(usernameField, Priority.ALWAYS);
 		username.getChildren().addAll(usernameLabel, usernameField);
 
 		HBox password = new HBox(3);
 		VBox.setMargin(password, inset);
 		Label passwordLabel = new Label("Password: ");
 		HBox.setHgrow(passwordLabel, Priority.NEVER);
 		passwordField = new PasswordField();
 		passwordField.setOnAction(new EventHandler<ActionEvent>() {
 			public void handle(ActionEvent e) {
 				loginButton.fire();
 			}
 		});
 		HBox.setHgrow(passwordField, Priority.ALWAYS);
 		password.getChildren().addAll(passwordLabel, passwordField);
 
 		HBox button = new HBox(7.5d);
 		VBox.setMargin(button, inset);
 		loginButton = ButtonBuilder.create()
 				.onAction(new EventHandler<ActionEvent>() {
 					public void handle(ActionEvent e) {
 						usernameData = usernameField.getText();
 						passwordData = passwordField.getText();
 						createProcess();
 					}
 				}).text("Login").maxWidth(Double.MAX_VALUE).build();
 		HBox.setHgrow(loginButton, Priority.ALWAYS);
 		button.getChildren().addAll(loginButton);
 
		if(setDefaultValues) { // set previous values
 			usernameField.setText(usernameData);
 			passwordField.setText(passwordData);
 		}
 
 		// add all the elements to the container that will be added to the tab
 		tabContent.getChildren().addAll(username, password, button);
 		return tabContent;
 	}
 }
