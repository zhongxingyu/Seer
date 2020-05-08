 package me.limebyte.battlenight.core.Listeners;
 
 import java.util.List;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.command.Command;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class CommandBlocker implements Listener {
 
     // Get Main Class
     public static BattleNight plugin;
     public CommandBlocker(BattleNight instance) {
         plugin = instance;
     }
     
    @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if (event.isCancelled()) return;
         if (!plugin.BattleUsersTeam.containsKey(event.getPlayer())) return;
         if (!plugin.config.getBoolean("Commands.Block")) return;
         
         List<String> whitelist = plugin.config.getStringList("Commands.Whitelist");
        
        String[] cmdArg = event.getMessage().split(" ");
        Command command = plugin.getServer().getPluginCommand(cmdArg[0].trim().replaceFirst("/", ""));
         
         // Don't block BattleNight commands
         if (command.getName().equals("bn")) return;
         
         // Check if the command is listed else cancel
         if (whitelist.contains(command.getName().toLowerCase())) {
             return;
         }
         // Command is not in the list
         else {
            if (command.getAliases().isEmpty()) {
                event.setCancelled(true);
                plugin.tellPlayer(event.getPlayer(), "You are not permitted to perform this command while in a Battle.");
                return;
            }
             // Check if an alias is listed else cancel
             for (String alias : command.getAliases()) {
                 if (whitelist.contains(alias.toLowerCase())) {
                     return;
                 }
                 else {
                     event.setCancelled(true);
                    plugin.tellPlayer(event.getPlayer(), "You are not permitted to perform this command while in a Battle.");
                 }
             }
         }
     }
     
 }
