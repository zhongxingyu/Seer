 
 package vooga.scroller.level_editor.model;
 
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Map;
 import vooga.scroller.sprites.Sprite;
 import vooga.scroller.util.IBackgroundView;
 import vooga.scroller.level_editor.ILevelEditor;
 import vooga.scroller.level_editor.LevelEditing;
 import vooga.scroller.level_editor.commands.Command;
 import vooga.scroller.level_editor.commands.CommandLibrary;
 import vooga.scroller.level_editor.controllerSuite.LEController;
 
 
 
 /**
  * Level Editor creates and edits an Editable Level based on input from the
  * LEController.
  * 
  * @author Danny Goodman
  *
  */
 
 import vooga.scroller.util.Editable;
 import vooga.scroller.util.mvc.IController;
 /**
  * LevelEditor is the Model side of the Level Editor as a whole. It processes
  * commands received by the controller on an Editable Grid also handed from
  * the controller
  * 
  * @author Danny Goodman, Deo Fagnisse
  */
 public class LevelEditor implements ILevelEditor {
 
     private static final String SPACE = " ";
     private static final String NO_METHOD_COMMAND_ERROR = "Command does not exist";
     private static final String PARAM_COMMAND_ERROR = "Incorrect Parameters";
     private static final String DEFAULT_COMMAND_ERROR = "Incorrect Command";
     private static final String COPY_ERROR = "Cannot copy Sprite. Missing default constructor";
     private Editable myGrid;
     private Map<Integer, Sprite> mySpriteMap;
     private Map<Integer, IBackgroundView> myBackgrounds;
     private IController<LevelEditing> myController;
     
     public LevelEditor(IController<LevelEditing> con) {
         myController = con;
     }
 
     /**
      * Takes in the command name and parameters all as one String to be processed.
      * and processes it on the Editable given by the LEController.
      * 
      * @param command - the input from the LEView. 
      */
     /* (non-Javadoc)
      * @see vooga.scroller.level_editor.ILevelEditor#processCommand(vooga.scroller.util.Editable, java.lang.String)
      */
     @Override
     public void processCommand (Editable m, String cmd) {
         myGrid = m;
         processCommand(cmd);
     }
 
     /* (non-Javadoc)
      * @see vooga.scroller.level_editor.ILevelEditor#setSpriteMap(java.util.Map)
      */
     @Override
     public void setSpriteMap (Map<Integer, Sprite> spriteMap) {
         mySpriteMap = spriteMap;
     }
 
     /* (non-Javadoc)
      * @see vooga.scroller.level_editor.ILevelEditor#setBackgroundMap(java.util.Map)
      */
     @Override
     public void setBackgroundMap (Map<Integer, IBackgroundView> map) {
         myBackgrounds = map;
         
     }
 
     /**
      * Method to create a sprite uses the @command annotation to be called
      * through reflection by the processCommand method.
      * 
      * @param x - position in pixels
      * @param y - position in pixels
      * @param id - Sprite ID
      */
     @Command
     public void createSprite (int x, int y, int id) {
         if(id == START_ID){
             addStartPoint(x,y);
         }
         if(id < START_ID) {
            addDoor(x,y,-id);
         }
         else{
             Sprite sprite = getSpriteFromMap(id);
                 try{
                     myGrid.addSprite(sprite, x, y);
                 }
                 catch(NullPointerException e){
                     myController.showErrorMsg(COPY_ERROR);
                 }
         }
     }
 
     /**
      * Method to delete a sprite uses the @command annotation to be called
      * through reflection by the processCommand method.
      * 
      * @param x - position in pixels
      * @param y - position in pixels
      */
     @Command
     public void deleteSprite (int x, int y) {
         myGrid.deleteSprite(x,y);
     }
 
     /**
      * Method to change the background image uses the @command annotation
      * to be called through reflection by the processCommand method.
      * 
      * @param id - Background ID
      */
     @Command
     public void changeBackground (int id) {
         myGrid.changeBackground(myBackgrounds.get(id));
     }
 
     private Sprite getSpriteFromMap (int id) {
         Sprite sprite = mySpriteMap.get(id);
         sprite = sprite.copy();
         return sprite;
     }
     
     private void addStartPoint (int x, int y) {
         myGrid.addStartPoint(x,y);
     }
     
     private void addDoor (int x, int y, int id) {
         myGrid.addDoor(getSpriteFromMap(id),x,y);
     }
     
     /**Splits the string by White Space and obtains name and parameters from String.
     * Calls @Command method through reflection.
     * 
     * @param command as one String.
     */
     private void processCommand (String command) {
         String[] splitCommand = command.split(SPACE);
         String name = splitCommand[0];
         Object[] params = getParams(splitCommand);
         try {
             Method m = CommandLibrary.get(name);
             m.invoke(this, params);
         }
         catch (NullPointerException e) {
             myController.showErrorMsg(NO_METHOD_COMMAND_ERROR);
         }
         catch (IllegalAccessException e) {
             myController.showErrorMsg(DEFAULT_COMMAND_ERROR);
         }
         catch (IllegalArgumentException e) {
             myController.showErrorMsg(PARAM_COMMAND_ERROR);
         }
         catch (InvocationTargetException e) {
            myController.showErrorMsg(DEFAULT_COMMAND_ERROR);
         }
     }
 
     /**
      * Takes in the command as a Sting[] and returns the parameters as an Integer[].
      * The output is used by the processCommand method to invoke the command method.
      * @param splitCommand - command as a String[] with name followed by params.
      * @return Integer[] of parameters
      */
     private Integer[] getParams (String[] splitCommand) {
         Integer[] params = new Integer[splitCommand.length - 1];
         for (int i = 0; i < params.length; i++) {
             params[i] = Integer.parseInt(splitCommand[i + 1]);
         }
         return params;
     }
 
 }
