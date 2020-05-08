 package controller;
 
 import javafx.animation.KeyFrame;
 import javafx.animation.KeyValue;
 import javafx.animation.Timeline;
 import javafx.application.Platform;
 import javafx.fxml.FXML;
 import javafx.scene.Parent;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.MenuButton;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.ToggleButton;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 import runner.Main;
 import utils.Utils;
 
 /**
  * Created with Intellij IDEA.
  * User: Erick
  * Date: 8/30/13
  * Time: 1:13 PM
  */
 
 public class ReadingController {
 
     public enum SpreadType {
         THREE_CARD_SPREAD, SWORD
     }
 
     public enum DeckType {
         WIATE, MARC, WIA_ART, NONE
     }
 
     @FXML
     private String enabled = "Reading Helper Enabled";
     @FXML
     private String disabled = "Reading Helper Disabled";
 
     public static SpreadType spread;
 
     private static Stage stage;
 
     @FXML
     private ToggleButton helpToggle;
 
     @FXML
     private Text selection;
 
     @FXML
     private MenuButton typeOfReading;
 
     @FXML
     private RadioButton wiaChoose;
 
     @FXML
     private RadioButton mercChoose;
 
     @FXML
     private RadioButton wiaArtChoose;
 
     @FXML
     private CheckBox invert;
 
     ///////////
     // boole's to keep track of what user wants
     private boolean wantsHelp = true;
     private boolean invertCards = false;
 
 
     public ReadingController() {
     }
 
     @FXML
     public void flash1910() {
         flashSelection("1910 Deck Selected");
         helpToggle.setSelected(false);
         helpToggle.setText(helpToggle.isSelected() ? disabled : enabled);
         setWantsHelp(helpToggle.isSelected() ? false : true);
         SpreadController.help = false;
     }
 
     @FXML
     public void flashMerc() {
         flashSelection("Marseilles Deck Selected");
         helpToggle.setSelected(true);
         helpToggle.setText("Help not available for selected deck");
         setWantsHelp(false);
     }
 
     @FXML
     public void flash1910Art() {
         flashSelection("1910 Art Variation Selected");
     }
 
     @FXML
     public void getReading() {
         System.out.println(spread);
         Timeline fadeAni = getStageAni();
 
         if (wiaChoose.isSelected()) {
             SpreadController.deck = DeckType.WIATE;
         }
 
         if (mercChoose.isSelected()) {
             SpreadController.deck = DeckType.MARC;
 
         }
 
         if (wiaArtChoose.isSelected()) {
             flashSelection("1910 Art Variation Deck Coming Soon");
         }
 
         try {
             switch (spread) {
                 case THREE_CARD_SPREAD: {
 
                     set3CardStage(fadeAni);
                 }
                 break;
 
                 case SWORD: {
                     getSwordSpread(fadeAni);
 
                     break;
                 }
                 default: {
                     set3CardStage(fadeAni);
                 }
 
 
             }
 
 //        if (spread == SpreadType.THREE_CARD_SPREAD) {
 //            set3CardStage(fadeAni);
 //        }
 //        if (spread == SpreadType.SWORD) {
 //            getSwordSpread(fadeAni);
 //        } else {
 //            System.out.println("Please tell me what to do");
 //        }
 
 
         } catch (NullPointerException e) {
             flashSelection("Please Select a Spread");
         }
     }
 
 
     /**
      * @return the fade animation for the stage fade effect
      */
     private Timeline getStageAni() {
         Timeline fadeAni = new Timeline();
         KeyFrame f1 = new KeyFrame(Duration.millis(225), new KeyValue(Main.getStage().opacityProperty(), 0.0f));
         KeyFrame f2 = new KeyFrame(Duration.millis(1450), new KeyValue(Main.getStage().opacityProperty(), 1.0f));
         fadeAni.getKeyFrames().addAll(f1, f2);
         return fadeAni;
     }
 
     /**
      * Sets the Parent of the current Scene to the SwordSpread.fxml
      * Also plays the Animation
      *
      * @param fadeAni The Timeline to set the animation to
      */
     private void getSwordSpread(Timeline fadeAni) {
         //todo make non help sword spread
         Parent p = Main.getSwordSpread();
         Main.getStage().setOpacity(0f);
         Main.getStage().getScene().setRoot(p);
         SpreadController.invert = invertCards;
         Main.getStage().getScene().setFill(Utils.READING_COLOR);
         Main.getStage().setWidth(778.0);
         Main.getStage().setHeight(727.0);
         Main.getStage().centerOnScreen();
         fadeAni.play();
     }
 
     /**
      * Sets the Parent of the current Scene to the TCS.fxml
      * Also plays the Animation
      *
      * @param fadeAni The Timeline to set the animation to
      */
     private void set3CardStage(Timeline fadeAni) {
         System.out.println(wantsHelp);
         if (wantsHelp && (SpreadController.deck == DeckType.WIATE || SpreadController.deck == DeckType.WIA_ART)) {
             Parent p = Main.getTCS();
             Main.getStage().setOpacity(0f);
             Main.getStage().getScene().setRoot(p);
             SpreadController.invert = invertCards;
             Main.getStage().getScene().setFill(Utils.READING_COLOR);
             Main.getStage().setWidth(778.0);
             Main.getStage().setHeight(727.0);
             Main.getStage().centerOnScreen();
             fadeAni.play();
         }
 
         if (!wantsHelp) {
             Parent p = Main.getTcsNoHelp();
             Main.getStage().setOpacity(0f);
             Main.getStage().getScene().setRoot(p);
             SpreadController.invert = invertCards;
             Main.getStage().getScene().setFill(Utils.READING_COLOR);
             Main.getStage().setWidth(775.0);
             Main.getStage().setHeight(500.0);
             Main.getStage().centerOnScreen();
             fadeAni.play();
         }
     }
 
 
     @FXML
     public void wants3Card() {
 //        wants3Card = true;
         spread = SpreadType.THREE_CARD_SPREAD;
         typeOfReading.setText(typeOfReading.getItems().get(0).getText());
     }
 
     @FXML
     public void wantsSword() {
         spread = SpreadType.SWORD;
         typeOfReading.setText(typeOfReading.getItems().get(1).getText());
     }
 
     public static void setStage(Stage stage) {
         ReadingController.stage = stage;
     }
 
     public void setWantsHelp(boolean wantsHelp) {
         this.wantsHelp = wantsHelp;
     }
 
     @FXML
     private void setHelp() {
 
         helpToggle.setText(helpToggle.isSelected() ? disabled : enabled);
 
         setWantsHelp(helpToggle.isSelected() ? false : true);
 
         SpreadController.help = wantsHelp;
 
         flashSelection(helpToggle.getText());
 
     }
 
     @FXML
     public void close() {
         Main.getStage().getScene().setRoot(Main.getMenu());
         Main.getStage().setWidth(540.0);
         Main.getStage().setHeight(160.0);
     }
 
     public void setInvert(boolean b) {
         invertCards = b;
     }
 
     @FXML
     public void invertSelected() {
         setInvert(invert.isSelected() ? true : false);
         flashSelection(invertCards ? "Inverted Cards On" : "Inverted Cards Off");
         SpreadController.invert = this.invertCards;
     }
 
     @FXML
     public void exit() {
         Platform.exit();
     }
 
     private void flashSelection(String text) {
         selection.setText(text);
         Timeline ani = new Timeline();
         KeyFrame frame = new KeyFrame(Duration.millis(0), new KeyValue(selection.opacityProperty(), 1.0f));
         KeyFrame fadeFrame = new KeyFrame(Duration.millis(2000), new KeyValue(selection.opacityProperty(), 0.0f));
         ani.getKeyFrames().addAll(frame, fadeFrame);
         ani.play();
     }
 }
