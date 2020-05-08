 package net.daboross.bukkitdev.commandexecutorbase;
 
 import java.io.Serializable;
 import org.bukkit.ChatColor;
 
 /**
  * This is a class that holds what colors PlayerData should user for different
  * purpose.
  *
  * @author daboross
  */
public final class ColorList {
 
     /**
      * This is the main color.
      */
     public static final String MAIN = ChatColor.AQUA.toString();
     /**
      * This is the color for Player's Usernames.
      */
     public static final String NAME = ChatColor.GRAY.toString();
     /**
      * This is the color for numbers, or other data.
      */
     public static final String NUMBER = ChatColor.RED.toString();
     /**
      * This is the color for commands, EG /asdf shold be this color.
      */
     public static final String CMD = ChatColor.GREEN.toString();
     /**
      * This is be the color for help text.
      */
     public static final String HELP = ChatColor.DARK_AQUA.toString();
     /**
      * This is the color for Sub Commands, EG in /asdf gl, GL should be this
      * color.
      */
     public static final String SUBCMD = ChatColor.AQUA.toString();
     /**
      * This is the color for messages saying that the user has no permission, or
      * that this is a player command when run from console.
      */
     public static final String NOPERM = ChatColor.RED.toString();
     /**
      * This is the color for Arguments of a command, EG in /asdf gl PLAYERNAME
      * NUMBER, PLAYERNAME and NUMBER would be this color.
      */
     public static final String ARGS = ChatColor.BLUE.toString();
     /**
      * This is the color for messages saying that the user has supplied an
      * Illegal Argument for a command.
      */
     public static final String ILLEGALARGUMENT = ChatColor.AQUA.toString();
     /**
      * This is the color for messages saying that there is an error.
      */
     public static final String ERROR = ChatColor.DARK_RED.toString();
     /**
      * This is the color for the arguments that have caused an error, or are
      * Illegal.
      */
     public static final String ERROR_ARGS = ChatColor.RED.toString();
     /**
      * This is the color that the server's name should be displayed in.
      */
     public static final String SERVERNAME = ChatColor.BLUE.toString();
     /**
      * This is the color that the Data Handler uses between a player's username
      * and their nickname in getPossibleUsernames.
      */
     public static final String DATA_HANDLE_SLASH = ChatColor.GRAY.toString();
     /**
      * This is the color for the divider slash in various places, EG if you were
      * separating two fields with a slash, that slash should be this color.
      */
     public static final String DIVIDER = ChatColor.GRAY.toString();
     /**
      * This is a darker version of the Main Color.
      */
     public static final String MAIN_DARK = ChatColor.DARK_GREEN.toString();
     /**
      * This is the color that broadcasts should be.
      */
     public static final String BROADCAST = MAIN;
 
     /**
      * Use This Function to get a broadcast name given a string username. This
      * will typically just be [NAME], just colored to different colors for name
      * and the []s. Use this to get the name of broadcasts given to players.
      *
      * @param name
      * @return
      */
     public static String getBroadcastName(String name) {
         return ChatColor.DARK_GRAY + "[" + ChatColor.RED + name + ChatColor.DARK_GRAY + "]";
     }
 }
