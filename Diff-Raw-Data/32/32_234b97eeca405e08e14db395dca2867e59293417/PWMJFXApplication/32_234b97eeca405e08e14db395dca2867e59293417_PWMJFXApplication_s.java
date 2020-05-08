 package de.dzim.jfx.pwm.ui;
 
 import javafx.application.Application;
 import javafx.scene.Scene;
 import javafx.scene.layout.GridPane;
 import javafx.stage.Stage;
 import de.dzim.jfx.pwm.ui.component.MainWindow;
 import de.dzim.jfx.ui.dialog.MessageDialog;
 import de.dzim.jfx.ui.resource.ImageResource;
 import de.dzim.jfx.ui.resource.ImageResource.ImageResourceType;
 
 public class PWMJFXApplication extends Application {
 
 	public static final String CSS = "de/dzim/jfx/pwm/ui/pwm.css";
 	public static final String CSS_DIALOG = "de/dzim/jfx/pwm/ui/pwm-dialog.css";
 
 	public static final String APPLICATION_TITLE = "PWM - The Password Manager";
 
 	@Override
	public void start(Stage primaryStage) {
 
 		primaryStage.setTitle(APPLICATION_TITLE);
 		primaryStage.getIcons().add(
 				ImageResource.getImage(ImageResourceType.LOCK_32));
 
 		GridPane root = new GridPane();
 		root.setId("root-grid-pane");
 
		MainWindow window = new MainWindow(primaryStage, root);
 		window.setCreateMenu(true);
 		window.createContent();
 
 		Scene scene = new Scene(root, 1024, 800);
 		scene.getStylesheets().add(CSS);
 
 		MessageDialog.CSS_PATH = CSS_DIALOG;
 
 		primaryStage.setScene(scene);
 		primaryStage.show();
 	}
 
 	public static void main(String[] args) {
 		launch(args);
 	}
 }
