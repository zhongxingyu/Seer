 
 package vooga.scroller.level_editor;
 
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Map;
 import util.Location;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.sprites.superclasses.Player;
 import vooga.scroller.util.Sprite;
 import vooga.scroller.level_editor.commands.Command;
 import vooga.scroller.level_editor.commands.CommandLibrary;
 
 
 /**
  * Level Editor creates and edits an Editable Level based on input from the
  * iLEView myView.
  * 
  * @author Danny Goodman
  *
  */
 
 import vooga.scroller.util.Editable;
 
 public class LevelEditor implements ILevelEditor {
 
     private static final int DEFAULT_GRID_SIZE = 20;
     private static final String SPACE = " ";
     private static final String NO_METHOD_COMMAND_ERROR = "Command does not exist";
     private static final String PARAM_COMMAND_ERROR = "Incorrect Parameters";
     private static final String DEFAULT_COMMAND_ERROR = "Incorrect Command";
     private static final String COPY_ERROR = "Cannot copy Sprite. Missing default constructor";
     private ScrollingManager myScrollingManager;
     private Editable myGrid;
     private Map<Integer, Sprite> mySpriteMap;
 
     public LevelEditor (ISpriteLibrary lib) {
         myGrid = new LEGrid(DEFAULT_GRID_SIZE,DEFAULT_GRID_SIZE);
     }
 
     public LevelEditor (String language, ISpriteLibrary lib) {
         this(lib);
     }
 
     @Override
     public void processCommand (Editable m, String cmd) {
         // TODO Auto-generated method stub
         myGrid = m;
         processCommand(cmd);
     }
 
     @Override
     public void setSpriteMap (Map<Integer, Sprite> spriteMap) {
         mySpriteMap = spriteMap;
     }
 
     @Command
     public void createSprite (int x, int y, int id) {
         if(id == -1){
             addStartPoint(x,y);
         }
         else{
             Sprite sprite = mySpriteMap.get(id);
             sprite = sprite.copy();
                 try{
                     myGrid.addSprite(sprite, x, y);
                 }
                 catch(NullPointerException e){
                     //TODO COPY_ERROR = "Cannot copy Sprite. Missing default constructor";
                     System.out.println(COPY_ERROR);
                 }
         }
         
     }
     
     private void addStartPoint (int x, int y) {
         myGrid.addStartPoint(x,y);
     }
 
     @Command
     public void deleteSprite (int x, int y) {
         myGrid.deleteSprite(x,y);
     }
 
     @Command
     public void changeBackground (int id) {
         //TODO change Background based on id
     }
 
     /**
      * Takes in the command name and parameters all as one String to be processed.
      * Splits the string by White Space and obtains name and parameters from String.
      * Calls @Command method through reflection.
      * 
      * @param command - the input from the LEView. 
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
           //TODO NO_METHOD_COMMAND_ERROR = "Command does not exist";
             System.out.println(NO_METHOD_COMMAND_ERROR);
         }
         catch (IllegalAccessException e) {
           //TODO DEFAULT_COMMAND_ERROR = "Incorrect Command";
             System.out.println("Illegal Access Exception");
         }
         catch (IllegalArgumentException e) {
           //TODO PARAM_COMMAND_ERROR = "Incorrect Parameters";
             System.out.println(PARAM_COMMAND_ERROR);
         }
         catch (InvocationTargetException e) {
           //TODO DEFAULT_COMMAND_ERROR = "Incorrect Command";
             System.out.println("Invocation Target Exception");
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
