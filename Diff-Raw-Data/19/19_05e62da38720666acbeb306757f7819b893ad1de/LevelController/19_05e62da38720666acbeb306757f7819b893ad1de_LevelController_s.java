 package vooga.fighter.controller;
 
 
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.Timer;
 
 import util.Location;
 import util.input.*;
 import vooga.fighter.model.LevelMode;
 import vooga.fighter.model.Mode;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.LevelMode;
 import vooga.fighter.model.Mode;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.util.Paintable;
 import vooga.fighter.view.Canvas;
 
 
 /**
  * 
  * @author Jerry Li
  * 
  * @Modified by Jack Matteucci
  * 
  */
 @InputClassTarget
 
 public abstract class LevelController extends Controller {
     private static final String INPUT_PATHWAY = "vooga.fighter.config.leveldefault";
     private List<CharacterObject> myInputObjects;
 
     public LevelController (String name, Canvas frame) {
         super(name, frame);
     }
 
     public LevelController(String name, Canvas frame, ControllerDelegate manager, 
                 GameInfo gameinfo) {
         super(name, frame, manager, gameinfo);

        Input input = new Input(INPUT_PATHWAY, this.getView());
        input.addListenerTo(this);
 
         DisplayLoopInfo gameLoopInfo = new GameLoopInfo((LevelMode) getMode());
         setLoopInfo(gameLoopInfo);
     }
 
     
     public void loadMode() {
         List<String> characterNames = getGameInfo().getCharacters();
         String mapID = getGameInfo().getMapName();
         LevelMode temp = new LevelMode(this, characterNames, mapID);
         setMode(temp);
         myInputObjects = temp.getMyCharacterObjects();
     }
    
     @InputMethodTarget(name = "player1_jump")
     public void playerOneJumpInput (AlertObject alObj)  {
         myInputObjects.get(0).move(270);
     }
     
     @InputMethodTarget(name = "player1_left")
     public void playerOneLeftInput (AlertObject alObj) {
         myInputObjects.get(0).move(180);
         
     }
     
     @InputMethodTarget(name = "player1_right")
     public void playerOneRightInput(AlertObject alObj) {
         myInputObjects.get(0).move(0);
         
     }
     
     @InputMethodTarget(name = "player1_down")
     public void playerOneDownInput(AlertObject alObj) {
         myInputObjects.get(0).move(90);
         
     }
     
     @InputMethodTarget(name = "player2_jump")
     public void playerTwoJumpInput (AlertObject alObj)  {
         myInputObjects.get(1).move(270);
     }
     
     @InputMethodTarget(name = "player2_left")
     public void playerTwoLeftInput (AlertObject alObj) {
         myInputObjects.get(1).move(180);
        
     }
     
     @InputMethodTarget(name = "player2_right")
     public void playerTwoRightInput(AlertObject alObj) {
         myInputObjects.get(1).move(0);
         
     }
     
     @InputMethodTarget(name = "player2_down")
     public void playerTwoDownInput(AlertObject alObj) {
         myInputObjects.get(1).move(90);
         
     }
 }
