 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dev.xyzcraft.net.xyzshop.util;
 
import dev.xyzcraft.net.database.DatabaseHandler;
 import dev.xyzcraft.net.xyzshop.XYZShop;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Joey
  */
 public abstract class MacCommand implements CommandExecutor {
     public XYZShop plugin;
     public boolean requiresDB = true;
     public boolean requiresSurvival = true;
     public MacCommand(XYZShop pl) {
         this.plugin = pl;
     }
     @Override
     public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
         try {
         plugin.getLogger().log(Level.INFO, "MacCommand {0} executed", string);
         if (!(cs instanceof Player)) {
             cs.sendMessage("Console is not allowed to use a MacCommand.");
             return true;
         }
         if (requiresSurvival && ((Player)cs).getGameMode() != GameMode.SURVIVAL) {
             cs.sendMessage(ChatColor.RED + "You cannot perform this command in your current gamemode.");
             return true;
         }
         if (!this.plugin.db && requiresDB) {
             cs.sendMessage(ChatColor.DARK_RED + "No database connection, command cannot function.");
             return true;
         }
         MacCommandStatus status = this.onCommand((Player) cs, cmnd, string, strings);
         if (status == MacCommandStatus.SUCESSFUL) {
             return true;
         }
         else if (status == MacCommandStatus.UNSUPPORTED) {
             cs.sendMessage(ChatColor.RED + "This command has not been implemented by this plugin yet.");
             return true;
         }
         else if (status == MacCommandStatus.FAILED){
             cs.sendMessage(ChatColor.RED + "An Error has occured. The command was marked as Failed.");
             plugin.getLogger().log(Level.INFO, "MacCommand {0} FAILED", string);
            return true;
         }
         return false;
         }
         catch (Exception ex){
             plugin.getLogger().log(Level.INFO, "Mac Command FAILED and got an uncaught exception!" );
             cs.sendMessage(ChatColor.DARK_RED + "UNCAUGHT EXCEPTION! MacCommand has utterly failed." + ChatColor.GRAY + " - " + ChatColor.WHITE + ex.getMessage());
             Logger.getLogger(MacCommand.class.getName()).log(Level.SEVERE, null, ex);
             return true;
         }
     }
     public abstract MacCommandStatus onCommand(Player cs, Command cmnd, String label, String[] args);
 }
