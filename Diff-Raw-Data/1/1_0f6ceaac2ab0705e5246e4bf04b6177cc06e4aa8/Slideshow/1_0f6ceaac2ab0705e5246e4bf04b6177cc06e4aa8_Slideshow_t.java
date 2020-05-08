 package slideShow;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.animation.FadeTransition;
 import javafx.animation.KeyFrame;
 import javafx.animation.SequentialTransition;
 import javafx.animation.Timeline;
 import javafx.application.Application;
 import static javafx.application.Application.launch;
 import javafx.application.Platform;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.concurrent.Task;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Pos;
 import javafx.scene.Cursor;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.StackPane;
 import javafx.scene.paint.Color;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 import login.LoginWindow;
 
 /**
  *
  * @author Morten
  */
 public class Slideshow extends Application {
 
     private StackPane root;
     private SequentialTransition slideshow;
     private ImageTransition imageTrans;
     private ArrayList<ImageView> imageList;
     private ListOfImages imageViewSetter;
     private CheckNewDelay checkNewDelay;
     private Task retrieveImages, checkDelay;
     private Thread retrieveImagesThread, checkDelayThread;
     private boolean startup = true;
     private Button quit, menu;
     private HBox box;
     private double delayDiffFactor = 1.0;
     private int delay;
     private Timeline timeline = null;
     private FadeTransition fadeOut = null;
 
     @Override
     public void start(Stage stage) throws Exception {
         root = new StackPane();
         slideshow = new SequentialTransition();
         imageTrans = new ImageTransition();
         imageList = new ArrayList();
         checkNewDelay = new CheckNewDelay(imageTrans.getFadeTime() / 1000);
         checkDelay = checkNewDelay.checkNewDelay();
 
         delay = imageTrans.getDelay();
 
         initiateRetrieveImagesThread();
         initiateCheckDelayThread();
 
         menu = new Button();
         menu.setText("Admin Menu");
         menu.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent e) {
                 LoginWindow login = new LoginWindow(getSlideshowObject());
                 login.generateStage();
             }
         });
 
         quit = new Button();
         quit.setText("Quit Slideshow");
         quit.setLayoutX(500);
         quit.setLayoutY(500);
         quit.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent e) {
                 Platform.exit();
             }
         });
 
         box = new HBox(20);
         box.setAlignment(Pos.BOTTOM_RIGHT);
         box.setOpacity(0.0);
         box.getChildren().add(quit);
         box.getChildren().add(menu);
 
         box.setStyle("../stylesheets/Menu.css");
 
         /*
          * Listener on mouse movement for buttons
          */
         root.setOnMouseMoved(new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent mouseEvent) {
                 root.setCursor(Cursor.DEFAULT);
                 
                 FadeTransition fadeIn = new FadeTransition(Duration.millis(1), box);
                 fadeIn.setFromValue(0.0);
                 fadeIn.setToValue(1.0);
 
                 if (box.getOpacity() <= 0.1) {
                     fadeIn.play();
                 } else {
                     //Do nothing
                 }
 
                 if (timeline != null || fadeOut != null) {
                     timeline.stop();
                     fadeOut.stop();
                 }
                 timeline = new Timeline(
                         new KeyFrame(Duration.seconds(3),
                         new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent actionEvent) {
                         root.setCursor(Cursor.NONE);
                         fadeOut = new FadeTransition(Duration.millis(1000), box);
                         fadeOut.setFromValue(1.0);
                         fadeOut.setToValue(0.0);
                         fadeOut.play();
                     }
                 }));
                 timeline.play();
 
             }
         });
 
         root.getChildren().add(box);
 
         /*
          * Initiates stage and sets it visible
          */
         stage = SlideShowWindow.getSlideShowWindow();
         stage.setScene(new Scene(root, 800, 600, Color.BLACK));
         stage.getScene().getStylesheets().add(this.getClass().getResource("/stylesheets/Slideshow.css").toExternalForm());
         stage.show();
 
         startup = false;
     }
 
     /*
      * Creates a new slideshow with updated preferrences
      */
     public void initiateNewSlideshow() {
         Duration timestamp = slideshow.getCurrentTime();
         imageTrans.setNewDelay();
         slideshow.stop();
         root.getChildren().clear();
         slideshow.getChildren().clear();
 
         root.getChildren().add(box);
 
         for (int i = 0; i < imageList.size(); i++) {
             imageList.get(i).setOpacity(0);
             root.getChildren().add(imageList.get(i));
             slideshow.getChildren().add(imageTrans.getFullTransition(imageList.get(i)));
         }
 
         slideshow.setCycleCount(Timeline.INDEFINITE);
         double tempDuration = (timestamp.toMillis() * delayDiffFactor);
         slideshow.playFrom(new Duration(tempDuration));
         delay = imageTrans.getDelay();
         delayDiffFactor = 1.0;
         System.out.println("initated new slideshow with " + imageList.size() + " images");
     }
 
     public void initiateCheckDelayThread() {
         checkDelayThread = new Thread(checkDelay);
         checkDelayThread.setDaemon(true);
         checkDelayThread.start();
 
         /*
          * Listening on ready signal from Task: checkDelay
          */
         checkDelay.messageProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                 System.out.println(newValue);
                 delayDiffFactor = Double.parseDouble(checkDelay.messageProperty().getValue().split(" ")[4]);
                 initiateNewSlideshow();
             }
         });
     }
 
     public void initiateRetrieveImagesThread() {
         System.out.println("Initiating new retreiveImagesThread");
         if (!startup) {
             imageViewSetter.setIsRunning(false);
             try {
                 retrieveImagesThread.join();
             } catch (InterruptedException ex) {
                 Logger.getLogger(Slideshow.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         imageList.clear();
         imageViewSetter = new ListOfImages(imageList);
         retrieveImages = imageViewSetter.getImageViewList();
         retrieveImagesThread = new Thread(retrieveImages);
         retrieveImagesThread.setDaemon(true);
         retrieveImagesThread.start();
 
         /*
          * Listening on ready signal from Task: retrieveImages
          */
         retrieveImages.messageProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                 initiateNewSlideshow();
             }
         });
     }
 
     public Slideshow getSlideshowObject() {
         return this;
     }
 
     /*
      * Main function
      */
     public static void main(String[] args) {
         launch(args);
     }
 }
