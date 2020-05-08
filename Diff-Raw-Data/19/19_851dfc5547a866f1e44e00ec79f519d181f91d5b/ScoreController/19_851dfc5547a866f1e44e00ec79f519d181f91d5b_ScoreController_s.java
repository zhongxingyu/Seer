 package vooga.fighter.controller;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.Timer;
 import util.Location;
 import vooga.fighter.game.Mode;
 import vooga.fighter.input.AlertObject;
 import vooga.fighter.input.Input;
 import vooga.fighter.input.InputMethodTarget;
 import vooga.fighter.util.Paintable;
 import vooga.fighter.view.Canvas;
 
 public class ScoreController extends Controller {
     private static final String INPUT_PATHWAY = "vooga.fighter.input.Game1Mapping_en_US";
     
     private Mode myMode;
     private Canvas myCanvas;
 
     public ScoreController (String name, Canvas frame) {
         super(name, frame);
     }
 
     public ScoreController (String name, Canvas frame, ControllerDelegate manager,
                             GameInfo gameinfo) {
         super(name, frame, manager, gameinfo);
     }
     
     
     /**
      * Exits program.
      */
     public void exit () {
         System.exit(0);
     }
 
    
     @Override
     public Controller getController (ControllerDelegate delegate, GameInfo gameinfo) {
         return new LevelController(super.getName(), super.getView(),
                                    delegate, gameinfo);
     }
 
     @Override
     protected Input makeInput () {
         Input input = new Input(INPUT_PATHWAY, super.getView());
         input.addListenerTo(this);
         return input;
     }
     
     @InputMethodTarget(name = "Exit_Game")
     public void playerOneJumpInput (AlertObject alObj)  {
         System.exit(0);
     }
     
     @InputMethodTarget(name = "Restart")
     public void playerOneLeftInput (AlertObject alObj) {
         notifyEndCondition();
         
     }
 
     @Override
     public void notifyEndCondition () {
         myGameInfo.setCharacter(0, 1);
         myGameInfo.setCharacter(1, 2);
         myGameInfo.setMapName(1);
         myManager.notifyEndCondition("Test");
         
     }
    
 
 }
