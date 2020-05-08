 package sample;
 
 import java.io.File;
 
 import javafx.application.Application;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 
 public class Main extends Application {
 		
 	File Authfile = new File("AccessToken.txt");
 		
 	@Override
 	public void start(Stage stage) throws Exception {
 		if (!Authfile.exists()){
 			Authfile.createNewFile();
 		}
 		
 		stage.setTitle("TwitterTest");
 		Parent root = FXMLLoader.load(getClass().getResource("MainStage.fxml"));
 		Scene scene = new Scene(root);
 		stage.setScene(scene);
 		stage.show();
 	}
 	
 	@Override
 	public void stop(){
 	}
 
 	public static void main(String[] args) {		
 		launch(args);
 	}
 	
 }
 
