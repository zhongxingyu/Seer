 
package main.java.net.gamesketch.bukkit.SignToSign;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 /**
  * Creates and manages teleportation links between signs.
  *
  * @author Cogito
  */
 public class SignToSign extends JavaPlugin {
     //private final PlayerListener playerListener = new LoginListener();
 
     public void onDisable() {
         //PluginManager pm = getServer().getPluginManager();
     }
 
     public void onEnable() {
         //PluginManager pm = getServer().getPluginManager();
         PluginDescriptionFile pdfFile = this.getDescription();
 
         // Print a startup message to the console, so we know it was started.
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         String commandName = command.getName().toLowerCase();
 
         if (commandName.equals("")) {
         }
         return false;
     }
 
 }
