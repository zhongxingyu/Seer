 package infrastructure;
 
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Scanner;
 
 public class App extends Application {
 	public static GameWorld game;
 	public static Group root;
 	public static Scene scene;
 	public static Stage pS;
 
 	public static void main(String[] args) {
 		launch(args);
 	}
 
 	@Override
 	public void start(Stage primaryStage) throws IOException {
 		pS = primaryStage;
 
 		primaryStage.setTitle("Dreamscape");
 		primaryStage.setFullScreen(false);
 		primaryStage.setResizable(false);
 
 		root = new Group();
 		scene = new Scene(root, Utility.WIDTH, Utility.HEIGHT);
 		//game = GameWorld.parse(Parse.readFromFile("src/levels/index.txt"));
 		game = new GameWorld();
 		game.addMap(new GameMap("maps/castle.jpg"));
		//game.changeMap(game.maps.get(1));
 		KeyManager keyManager = new KeyManager();
 		Thread key = new Thread(keyManager.keyThread);
 		Thread cam = new Thread(game.camera);
 		key.start();
 		cam.start();
 		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
 			@Override
 			public void handle(WindowEvent arg0) {
 				Platform.exit();
 				System.exit(0);
 			}
 		});
 
 		scene.setOnKeyPressed(keyManager.keyPress);
 		scene.setOnKeyReleased(keyManager.keyRelease);
 		primaryStage.setScene(scene);
 		primaryStage.show();
 	}
 }
