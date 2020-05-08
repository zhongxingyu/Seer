 package thesaurus.gui.window;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 import javafx.application.Application;
 import javafx.scene.image.Image;
 
 public class MainWindow extends Application {
 	
 	private Stage stageCurrent;
 	private Scene splashSceneCurrent;
 	
 	public void launchProgram(String[] args){
 		launch(args);
 	}
 
 	public void start(Stage stage) throws Exception {
 		
 		SplashRoot splashRootCurrent = new SplashRoot(this);
 		stageCurrent = stage;
 		splashSceneCurrent = new Scene(splashRootCurrent);
 		stageCurrent.setScene(splashSceneCurrent);
 		stageCurrent.setTitle("The Graphical Thesaurus by Team O");
 		stageCurrent.setWidth(800);
 		stageCurrent.setHeight(600);
 		stageCurrent.getIcons().add(new Image("file:o.png"));
 		stageCurrent.show();
 
 	}	
 	
 	public Stage getStage(){
 		return stageCurrent;
 	}
 	
 	public Scene getSplashScene(){
 		return splashSceneCurrent;
 	}
 
 }
