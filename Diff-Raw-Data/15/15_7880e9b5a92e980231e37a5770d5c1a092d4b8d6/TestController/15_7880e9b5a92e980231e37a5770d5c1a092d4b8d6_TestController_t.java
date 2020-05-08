 package skrivfx;
 
 import classes.ClearThread;
 import classes.WordImage;
 import java.io.File;
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.animation.FadeTransition;
 import javafx.animation.ParallelTransition;
 import javafx.animation.RotateTransition;
 import javafx.animation.TranslateTransition;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Tab;
 import javafx.scene.control.TabPane;
 import javafx.scene.control.ToggleButton;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.Pane;
 import javafx.scene.shape.Rectangle;
 import javafx.stage.FileChooser;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 import model.Model;
 import view.View;
 
 
 public class TestController implements Initializable, EventHandler<MouseEvent>{
     
     // Other stuff
     private Model model;
     private View view;
     private model.ModelImage modelI;
     private view.ViewImage viewI;
     private Image image;
     private boolean hasReachedEnd = false;
     private static ClearThread t;
     
     //These are objects injected from the FXML file:
     @FXML
     private ToggleButton menuButton;
     @FXML
     private ToggleButton writeButton;
     @FXML
     private Button openButton;
     @FXML
     private Button newButton;
     @FXML
     private Button saveButton;
     @FXML
     private Button closeButton;
     @FXML
     private Rectangle thumb;
     @FXML
     private ImageView track;
     @FXML
     private Pane drawingPane;
     @FXML
     private Pane page;
     @FXML
     private TabPane tabPane;
     
     
     //attributes
     private ParallelTransition parallelTransition;
     
     //Button action methods:
     @FXML
     private void handleWriteButtonAction(ActionEvent e){
         if (writeButton.isSelected()){
             System.out.println("write button on");
         }
         else{
             System.out.println("write button off");
         } 
     }
     
 /*------------------------Animated Menu Button------------------------*/
     
     @FXML
     private void handleMenuButtonAction(){
         if (menuButton.isSelected()){
             newButton.setVisible(true);
             openButton.setVisible(true);
             saveButton.setVisible(true);
             closeButton.setVisible(true);
             this.menuButtonOpen();          
         }
         else{
             this.menuButtonClose();
             Thread t = new Thread(new Runnable(){
                 @Override
                 public void run(){
                     try{
                         Thread.sleep(250);
                     } catch(Exception e){ System.out.println("pause exception"); }
                     newButton.setVisible(false);
                     openButton.setVisible(false);
                     saveButton.setVisible(false);
                     closeButton.setVisible(false);
                 }
             });
             t.start(); 
         }
     }
     
     //code reuse goes out the window:
     
     private void menuButtonOpen(){
         FadeTransition newFade = 
         new FadeTransition(Duration.millis(50), newButton);
         newFade.setFromValue(0.0);
         newFade.setToValue(1.0);
         newFade.setCycleCount(1);
         newFade.setAutoReverse(true);
         
         TranslateTransition newTranslate =
         new TranslateTransition(Duration.millis(50), newButton);
         newTranslate.setFromY(-100);
         newTranslate.setToY(5);
         newTranslate.setCycleCount(1);
         newTranslate.setAutoReverse(true);
         
         FadeTransition openFade = 
         new FadeTransition(Duration.millis(100), openButton);
         openFade.setFromValue(0.0);
         openFade.setToValue(1.0);
         openFade.setCycleCount(1);
         openFade.setAutoReverse(true);
         
         TranslateTransition openTranslate =
         new TranslateTransition(Duration.millis(100), openButton);
         openTranslate.setFromY(-100);
         openTranslate.setToY(5);
         openTranslate.setCycleCount(1);
         openTranslate.setAutoReverse(true);
         
         FadeTransition saveFade = 
         new FadeTransition(Duration.millis(150), saveButton);
         saveFade.setFromValue(0.0);
         saveFade.setToValue(1.0);
         saveFade.setCycleCount(1);
         saveFade.setAutoReverse(true);
         
         TranslateTransition saveTranslate =
         new TranslateTransition(Duration.millis(150), saveButton);
         saveTranslate.setFromY(-100);
         saveTranslate.setToY(5);
         saveTranslate.setCycleCount(1);
         saveTranslate.setAutoReverse(true);
         
         FadeTransition closeFade = 
         new FadeTransition(Duration.millis(250), closeButton);
         closeFade.setFromValue(0.0);
         closeFade.setToValue(1.0);
         closeFade.setCycleCount(1);
         closeFade.setAutoReverse(true);
         
         TranslateTransition closeTranslate =
         new TranslateTransition(Duration.millis(250), closeButton);
         closeTranslate.setFromY(-100);
         closeTranslate.setToY(5);
         closeTranslate.setCycleCount(1);
         closeTranslate.setAutoReverse(true);
         
         RotateTransition rotateTransition = 
         new RotateTransition(Duration.millis(150), menuButton);
         rotateTransition.setByAngle(90);
         rotateTransition.setCycleCount(1);
         rotateTransition.setAutoReverse(true);
 
         parallelTransition = new ParallelTransition();
         parallelTransition.getChildren().addAll(
                 newFade,
                 newTranslate,
                 
                 openFade,
                 openTranslate,
                 
                 saveFade,
                 saveTranslate,
                 
                 closeFade,
                 closeTranslate,
                 
                 rotateTransition
         );
         parallelTransition.setCycleCount(1);
         parallelTransition.play();
 }
     private void menuButtonClose(){
         FadeTransition newFade = 
         new FadeTransition(Duration.millis(50), newButton);
         newFade.setFromValue(1.0);
         newFade.setToValue(0.0);
         newFade.setCycleCount(1);
         newFade.setAutoReverse(true);
         
         TranslateTransition newTranslate =
         new TranslateTransition(Duration.millis(50), newButton);
         newTranslate.setFromY(5);
         newTranslate.setToY(-100);
         newTranslate.setCycleCount(1);
         newTranslate.setAutoReverse(true);
         
         FadeTransition openFade = 
         new FadeTransition(Duration.millis(100), openButton);
         openFade.setFromValue(1.0);
         openFade.setToValue(0.0);
         openFade.setCycleCount(1);
         openFade.setAutoReverse(true);
         
         TranslateTransition openTranslate =
         new TranslateTransition(Duration.millis(100), openButton);
         openTranslate.setFromY(5);
         openTranslate.setToY(-100);
         openTranslate.setCycleCount(1);
         openTranslate.setAutoReverse(true);
         
         FadeTransition saveFade = 
         new FadeTransition(Duration.millis(150), saveButton);
         saveFade.setFromValue(1.0);
         saveFade.setToValue(0.0);
         saveFade.setCycleCount(1);
         saveFade.setAutoReverse(true);
         
         TranslateTransition saveTranslate =
         new TranslateTransition(Duration.millis(150), saveButton);
         saveTranslate.setFromY(5);
         saveTranslate.setToY(-100);
         saveTranslate.setCycleCount(1);
         saveTranslate.setAutoReverse(true);
         
         FadeTransition closeFade = 
         new FadeTransition(Duration.millis(75), closeButton);
         closeFade.setFromValue(1.0);
         closeFade.setToValue(0.0);
         closeFade.setCycleCount(1);
         closeFade.setAutoReverse(true);
         
         TranslateTransition closeTranslate =
         new TranslateTransition(Duration.millis(150), closeButton);
         closeTranslate.setFromY(5);
         closeTranslate.setToY(-100);
         closeTranslate.setCycleCount(1);
         closeTranslate.setAutoReverse(true);
         
         RotateTransition rotateTransition = 
         new RotateTransition(Duration.millis(150), menuButton);
         rotateTransition.setByAngle(-90);
         rotateTransition.setCycleCount(1);
         rotateTransition.setAutoReverse(true);
 
         parallelTransition = new ParallelTransition();
         parallelTransition.getChildren().addAll(
                 newFade,
                 newTranslate,
                 
                 openFade,
                 openTranslate,
                 
                 saveFade,
                 saveTranslate,
                 
                 closeFade,
                 closeTranslate,
                 
                 rotateTransition
         );
         parallelTransition.setCycleCount(1);
         parallelTransition.play();
 }
     
 /*------------------------    Menu Buttons    ------------------------*/
     
     @FXML
     private void handleNewButtonAction(){
         tabPane.getTabs().add(new Tab("Untitled"));
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             newButton.setVisible(false);
             openButton.setVisible(false);
             saveButton.setVisible(false);
             closeButton.setVisible(false);
 
         }
     }
     
     @FXML
     private void handleOpenButtonAction(){
         System.out.println("open button pressed");
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             newButton.setVisible(false);
             openButton.setVisible(false);
             saveButton.setVisible(false);
             closeButton.setVisible(false);
             FileChooser fc = new FileChooser();
             fc.setTitle("Open skriv file...");
             File defaultDirectory = new File(".");
             fc.setInitialDirectory(defaultDirectory);
             fc.showOpenDialog(new Stage());
         }
     }
     
     @FXML
     private void handleSaveButtonAction(){
         System.out.println("save button pressed");
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             newButton.setVisible(false);
             openButton.setVisible(false);
             saveButton.setVisible(false);
             closeButton.setVisible(false);
             
             FileChooser fc = new FileChooser();
             fc.setTitle("Save skriv file...");
             File defaultDirectory = new File(".");
             fc.setInitialDirectory(defaultDirectory);
             //File selectedDirectory = dc.showDialog(new Stage());
             fc.showSaveDialog(new Stage());
         }
     }
     @FXML
     private void handleCloseButtonAction(){
         tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem());
         //if(t != null){ t.kill(); }
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             newButton.setVisible(false);
             openButton.setVisible(false);
             saveButton.setVisible(false);
             closeButton.setVisible(false);
             
         }
     }
 
 /*------------------------  Mouse & Keyboard  ------------------------*/
     
     @FXML
     private void scrollDragAction(MouseEvent evt){
         //System.out.println("Druggeded");
        // if (thumb.getHeight() track.getFitHeight())
         thumb.setOpacity(0.5);
         if(evt.getY()-thumb.getHeight()/2>0&&evt.getY()+thumb.getHeight()/2<track.getFitHeight()) {
             thumb.setY(evt.getY()-thumb.getHeight()/2);
         }
         if (evt.getY()-thumb.getHeight()/2<=0) {
             thumb.setY(0);
         }
         if (evt.getY()+thumb.getHeight()>=track.getFitHeight()) {
             thumb.setY(track.getFitHeight()-thumb.getHeight());
         }
     }
     
     @FXML
     private void onMouseEntered(MouseEvent evt){
         thumb.setOpacity(0.5);
     }
     
     @FXML
     private void onMouseExited(MouseEvent evt){
         thumb.setOpacity(0.3);
     }
     
     @FXML
     private void keyboardShortcut(KeyEvent kp){ 
         System.out.println(kp.getText());
         
     }
     
     /*---------------- Overridden MouseEvent Handler -----------------*/
     @Override
     public void handle(MouseEvent e){
         if(e.getEventType() == MouseEvent.MOUSE_PRESSED){ mousePressedEvent(e); }
         else if(e.getEventType() == MouseEvent.MOUSE_DRAGGED){ mouseDraggedEvent(e); }
         else{ mouseReleasedEvent(e); }
     }
     
     private void mousePressedEvent(MouseEvent e){
         // Start drawing the Line in the writingBox (View)
         viewI.startLine(e.getX(), e.getY());
         // Reset Thread
         t.reset();
         // If point is not in current Word then take snapshot and make new Word then draw it to the page
         if(modelI.isNewWord(e.getX(), e.getY())){
//            image = viewI.getSnapshot(modelI.left(), modelI.top(), modelI.getWidth(), modelI.getHeight());
             WordImage w = new WordImage(image);
             modelI.addWord(w);
             viewI.drawWord(w);
         }
     }
     
     private void mouseDraggedEvent(MouseEvent e){       
         if(e.getX() > drawingPane.getWidth()*.9){ hasReachedEnd = true; }
         modelI.addPoint(e.getX(), e.getY());
         viewI.updateLine(e.getX(), e.getY());
     }
     
    private void mouseReleasedEvent(MouseEvent e){ 
        image = viewI.getSnapshot(modelI.left(), modelI.top(), modelI.getWidth(), modelI.getHeight());
         if(hasReachedEnd){
             t.restart();
             hasReachedEnd = false;
         }
     }
     
     public static ClearThread getThread(){
         return t;
     }
         
     @Override
     public void initialize(URL url, ResourceBundle rb){
         // Little more of an MVC structure
         //model = new Model();
         modelI = new model.ModelImage();
         // SPLIT UP VIEW
         //view = new View();
         viewI = new view.ViewImage();
 //        view.makePageCanvas(page.widthProperty(), page.heightProperty());
 //        view.makeWritingCanvas(drawingPane.widthProperty(), drawingPane.heightProperty());
         viewI.makePageCanvas(page.widthProperty(), page.heightProperty());
         viewI.makeWritingCanvas(drawingPane.widthProperty(), drawingPane.heightProperty());
        image = viewI.getSnapshot(modelI.left(), modelI.top(), modelI.getWidth(), modelI.getHeight());
         t = new ClearThread(modelI, viewI);
         t.start();
         
         // Add Handlers
 //        view.addHandlers(this);
         viewI.addHandlers(this);
         
         // Add Canvas to Drawing Pane
         drawingPane.getChildren().add(viewI.getWritingCanvas());
         
         // Add Canvas to Page Pane
         page.getChildren().add(viewI.getPageCanvas());
     }
 }
