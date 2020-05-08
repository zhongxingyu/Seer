 package edgruberman.bukkit.simpletemplate;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 final class CommandManager implements CommandExecutor {
     
     private final JavaPlugin plugin;
     
     CommandManager(final JavaPlugin plugin) {
         this.plugin = plugin;
         
         this.setExecutorOf("command");
     }
     
     @Override
     public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
         Main.getMessageManager().log(MessageLevel.FINE
                 , ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                + " issued command: " + label + " " + CommandManager.join(split)
         );
         
         if (!sender.isOp()) {
             Main.getMessageManager().respond(sender, MessageLevel.RIGHTS, "You must be a server operator to use this command.");
             return false;
         }
 
         // TODO Add command processing logic here.
         
         return false;
     }
     
     /**
      * Registers this class as executor for a chat/console command.
      * 
      * @param label Command label to register.
      */
     private void setExecutorOf(final String label) {
         PluginCommand command = this.plugin.getCommand(label);
         if (command == null) {
             Main.getMessageManager().log(MessageLevel.WARNING, "Unable to register \"" + label + "\" command.");
             return;
         }
         
         command.setExecutor(this);
     }
     
     /**
      * Concatenate all string elements of an array together with a space.
      * 
      * @param s string array
      * @return concatenated elements
      */
     private static String join(final String[] s) {
         return join(Arrays.asList(s), " ");
     }
     
     /**
      * Combine all the elements of a list together with a delimiter between each.
      * 
      * @param list list of elements to join
      * @param delim delimiter to place between each element
      * @return string combined with all elements and delimiters
      */
     private static String join(final List<String> list, final String delim) {
         if (list == null || list.isEmpty()) return "";
      
         StringBuilder sb = new StringBuilder();
         for (String s : list) sb.append(s + delim);
         sb.delete(sb.length() - delim.length(), sb.length());
         
         return sb.toString();
     }
 }
