 package me.KeybordPiano459.kEssentials.commands;
 
 import java.util.logging.Level;
 import me.KeybordPiano459.kEssentials.kEssentials;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 
 public class CommandPing extends kCommand implements CommandExecutor {
     public CommandPing(kEssentials plugin) {
         super(plugin);
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("ping")) {
             if (sender instanceof Player) {
                 Player player = (Player) sender;
                 if (args.length == 0) {
                     if (player.hasPermission("kessentials.ping")) {
                        try {
                            player.sendMessage(GREEN + "Pong " + ((CraftPlayer)player).getHandle().ping + "MS");
                        } catch (UnsupportedOperationException e) {
                            
                        }
                     } else {
                         noPermissionsMessage(player);
                     }
                 } else {
                     incorrectUsage(player, "/ping");
                 }
             } else {
                 if (args.length == 0) {
                     log(Level.INFO, "Pong");
                 } else {
                     incorrectUsageC("/ping");
                 }
             }
         }
         return false;
     }
 }
