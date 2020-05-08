 package vooga.fighter.controller;
 
 
 
import util.input.src.input.Input;
 import vooga.fighter.model.*;
 import vooga.fighter.view.Canvas;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.Timer;
 
 
 
 
 /**
  * 
  * @author Jerry Li and Jack Matteucci
  * 
  *
  *
  */
 
 public abstract class Controller implements ModelDelegate {
 
     protected ControllerDelegate myManager;
     private String myName;
     private String myPath;
     private Canvas myCanvas;
     protected GameInfo myGameInfo;
 
     protected Input myInput;
     public static final int FRAMES_PER_SECOND = 25;
     // better way to think about timed events (in milliseconds)
     public static final int ONE_SECOND = 1000;
     public static final int DEFAULT_DELAY = ONE_SECOND / FRAMES_PER_SECOND;
     private Timer myTimer;
     private Mode myMode;
     private LoopInfo myLoopInfo;
 
     public Controller(String name, Canvas frame){
         myName = name;
         myCanvas = frame;
         myInput = makeInput();
     }
 
     public Controller(String name, Canvas frame, ControllerDelegate manager, GameInfo gameinfo) {
         this(name, frame);
         myManager = manager;
         myGameInfo = gameinfo;
     }
 
     public String getName(){
         return myName;
     }
 
     protected Canvas getView(){
         return myCanvas;
     }
 
     protected String getPath(){
         return myPath;
     }
 
     protected ControllerDelegate getManager(){
         return myManager;
     }
 
     protected GameInfo getGameInfo(){
         return myGameInfo;
     }
     
     protected Mode getMode(){
     	return myMode;
     }
 
     protected void setMode(Mode mode){
         myMode = mode;
     }
     
     protected void setGameLoopInfo(LoopInfo gameinfo){
     	myLoopInfo = gameinfo;
     }
 
     public void displaySplash(){
         
     }
 
     public void start() {
         final int stepTime = DEFAULT_DELAY;
         // create a timer to animate the canvas
          myTimer = new Timer(stepTime, 
                                new ActionListener() {
             public void actionPerformed (ActionEvent e) {
                 myMode.update((double) stepTime / ONE_SECOND, myCanvas.getSize());
                 myLoopInfo.updateImages();
                 myCanvas.paint();
             }
         });
         // start animation
         myTimer.start();
     }
 
     /**
      * Exits program.
      */
     public void exit () {
         System.exit(0);
     }
 
     public void stop () {
         myTimer.stop();
 
     }
 
     public void loadMode() {
 
     }
 
     public abstract Controller getController(ControllerDelegate manager, GameInfo gameinfo);
 
     protected abstract Input makeInput();
 
 
 }
