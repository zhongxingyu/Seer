 package faceclicker;
 import java.net.URL;
 
 import javafx.application.Application;
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.JavaFXBuilderFactory;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.input.MouseEvent;
 import javafx.stage.Stage;
 
 public class MainStage extends Application {
 	
     MainController ct = new MainController();
     
     public static void main(String[] args) {
         Application.launch(MainStage.class, args);
     }
     
     @Override
     public void start(Stage primaryStage) throws Exception {
     	FXMLLoader fxmlLoader = new FXMLLoader();    
         URL location = getClass().getResource("View.fxml");
         fxmlLoader.setLocation(location);
         fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
         Parent root = (Parent) fxmlLoader.load(location.openStream());
        
         final MainController controller = (MainController) fxmlLoader.getController();
         
        primaryStage.setTitle("trollop");
         primaryStage.setScene(new Scene(root, 600, 600));
         primaryStage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {      	
         	@Override
         	public void handle(MouseEvent me) {
                 	controller.grabCoords(me);
         	}
         });
         
         primaryStage.setX(50);
         primaryStage.setY(50);
         primaryStage.show();
     }
 }
