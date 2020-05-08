 package net.new_liberty.commandtimer.set;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import net.new_liberty.commandtimer.CommandTimer;
 
 /**
  * Represents a set of commands.
  */
 public class CommandSet {
     private final String id;
 
     private final Map<String, String> messages;
 
     private final Set<String> cmds;
 
     public CommandSet(String id, Map<String, String> messages, Set<String> cmds) {
         this.id = id;
         this.messages = messages;
         this.cmds = cmds;
     }
 
     /**
      * Gets the id of this CommandSet.
      *
      * @return
      */
     public String getId() {
         return id;
     }
 
     /**
      * Gets a message from its key.
      *
      * @param key
      * @return
      */
     public String getMessage(String key) {
         String ret = messages.get(key);
         if (ret == null) {
             ret = CommandTimer.getInstance().getMessage(key);
         }
        return ret;
     }
 
     /**
      * Gets the command of this set corresponding to the given command if it
      * exists.
      *
      * @param command
      * @return
      */
     public String getCommand(String command) {
         for (String cmd : cmds) {
             if (cmd.startsWith(command.toLowerCase() + " ")) {
                 return cmd;
             }
         }
         return null;
     }
 
     /**
      * Gets a list of all commands in this CommandSet.
      *
      * @return
      */
     public List<String> getCommands() {
         return new ArrayList<String>(cmds);
     }
 
     /**
      * Returns true if this set has the given command.
      *
      * @param command
      * @return
      */
     public boolean hasCommand(String command) {
         return getCommand(command) != null;
     }
 }
