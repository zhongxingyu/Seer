import java.util.HashMap;
import java.util.Map;

 /**
  * This class is part of the "World of Zuul" application. 
  * "World of Zuul" is a very simple, text based adventure game.  
  * 
  * This class holds an enumeration of all command words known to the game.
  * It is used to recognise commands as they are typed in.
  *
  * @author  Michael Kölling and David J. Barnes
  * @version 2011.07.31
  */
 
 public class CommandWords
 {
     // a constant array that holds all valid command words
     private static final String[] validCommands = {
         "go", "quit", "help", "look", "inventory"
     };
     
     private Map<CommandWord, String> commandMap;
 
     /**
      * Constructor - initialise the command words.
      */
     public CommandWords()
     {
    	commandMap = new HashMap<CommandWord, String>();
         commandMap.put(CommandWord.GO, "go");
         commandMap.put(CommandWord.QUIT, "quit");
         commandMap.put(CommandWord.HELP, "help");
         commandMap.put(CommandWord.LOOK, "look");
         commandMap.put(CommandWord.INVENTORY, "inventory");
         
     }
 
     /**
      * Check whether a given String is a valid command word. 
      * @return true if a given string is a valid command,
      * false if it isn't.
      */
     public boolean isCommand(String aString)
     {
         for(int i = 0; i < validCommands.length; i++) {
             if(validCommands[i].equals(aString))
                 return true;
         }
         // if we get here, the string was not found in the commands
         return false;
     }
     
     public static final String getValidCommands() {
         StringBuffer buff = new StringBuffer("\t");
         for(String command : validCommands) {
             buff.append(command);
             buff.append(" ");
         }
         return buff.toString();
     }
 }
