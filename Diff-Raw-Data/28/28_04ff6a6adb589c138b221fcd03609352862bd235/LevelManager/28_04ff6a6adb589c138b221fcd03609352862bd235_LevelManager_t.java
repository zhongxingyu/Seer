 package vooga.scroller.level_management;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import util.Location;
 import util.input.Input;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.sprites.superclasses.Player;
 import vooga.scroller.view.GameView;
 import vooga.scroller.level_editor.Level;
 
 /**
  * Manages the flow and order of levels in gameplay.
  * 
  * @author Scott Valentine
  *
  */
 public class LevelManager {
 
     private static final String DEFAULT_INPUT_CONTROLS = "vooga/scroller/resources/controls/SplashMapping";
     
     private Input myInput;
     private Level myInitialLevel;
     private Level myCurrentLevel;
     private GameView myView;
     
         
     /**
      * Creates a new level manager based on the view used by individual levels.
      * @param gameView to be used in constructing individual levels.
      */
     public LevelManager(ScrollingManager sm, GameView gameView, String ...levelFileNames) {   
         myView = gameView;
         LevelFactory lf = new LevelFactory(this, sm, gameView);
        myInitialLevel = lf.linkLevels(lf.generateLevels(levelFileNames));        
         //myCurrentLevel = myLevels.get(DEFAULT_START_LEVEL_ID); 
         myInput = new Input(DEFAULT_INPUT_CONTROLS, gameView);
         setCurrentLevel(myInitialLevel);
     }
     
     /**
      * Creates a new level manager based on the view used by individual levels.
      * @param gameView to be used in constructing individual levels.
      */
     public LevelManager(ScrollingManager sm, GameView gameView, Level ...levels) {   
         myView = gameView;
         LevelFactory lf = new LevelFactory(this, sm, gameView);
         List<Level> theLevels = new ArrayList<Level>();
         for (int i=0; i<levels.length; i++) {
             theLevels.add(levels[i]);
         }
         myInitialLevel = lf.linkLevels(theLevels);        
         //myCurrentLevel = myLevels.get(DEFAULT_START_LEVEL_ID); 
         myInput = new Input(DEFAULT_INPUT_CONTROLS, gameView);
         setCurrentLevel(myInitialLevel);
     }
     
     
     /**
      * Gives the current level.
      * 
      * @return The current level
      */
     public Level getCurrentLevel() {
         return myCurrentLevel;
     }
     
     /**
      * Sets the current level to the specified ID.
      * 
      * @param id of the level to become the current level.
      */
     public void setCurrentLevel(Level level) {
         if(myCurrentLevel != null){
             myCurrentLevel.removeInputListeners(myInput);
             Player p = myCurrentLevel.getPlayer();
             myCurrentLevel = level;
             myCurrentLevel.addPlayer(p);
         }
         else{
             myCurrentLevel = level;
         }
         myCurrentLevel.addInputListeners(myInput);
     }
     
     /**
      * Map a door to a starting point. Bind door to this level manager.
      */
     public void put(IDoor door, Level nextLevel) {
         door.setNextLevel(nextLevel);
         door.setManager(this);
     }
     
     public void updateLevel(double elapsedTime, Dimension bounds) {        
         myCurrentLevel.update(elapsedTime, bounds, myView);
     }
 }
 
