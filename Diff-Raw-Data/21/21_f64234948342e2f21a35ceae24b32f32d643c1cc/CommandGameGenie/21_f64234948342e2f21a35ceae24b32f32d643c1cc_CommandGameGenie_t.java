 package com.minecarts.gamegenie.command;
 
 import com.minecarts.gamegenie.CommandHandler;
 import com.minecarts.gamegenie.GameGenie;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import java.util.List;
 
 public class CommandGameGenie extends CommandHandler {
     public CommandGameGenie(GameGenie plugin){
         super(plugin);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(!sender.hasPermission("gamegenie.toggle")) return true;
         if(args.length == 0){
             if(sender instanceof Player){
                 Player p = (Player) sender;
                 if(p.getGameMode() == GameMode.SURVIVAL){
                     p.setGameMode(GameMode.CREATIVE);
                     p.sendMessage(ChatColor.GRAY + "You toggled your game mode to " + ChatColor.YELLOW + "CREATIVE" + ChatColor.GRAY + ".");
                 } else {
                     p.setGameMode(GameMode.SURVIVAL);
                     p.sendMessage(ChatColor.GRAY + "You toggled your game mode to " + ChatColor.GREEN + "SURVIVAL" + ChatColor.GRAY + ".");
                 }
             }
             return true;
         }
         
         //Handle /gm list
         if(args[0].equalsIgnoreCase("list")){
             sender.sendMessage(ChatColor.GRAY + "----Players in Creative----");
             for(Player p : Bukkit.getOnlinePlayers()){
                 if(p.getGameMode() == GameMode.CREATIVE){
                     sender.sendMessage(ChatColor.GRAY + " - " + p.getDisplayName() + ChatColor.GRAY + " (" + p.getWorld().getName() + ")");
                 }
             }
             return true;
         }
         
         for(String playerName : args){
             List<Player> playerList  = Bukkit.matchPlayer(playerName);
             if(playerList.size() > 1) {
                 sender.sendMessage("Too many players matched " + playerName + ". (" + playerList.size() + ")");
                 return true;
             } else if(playerList.size() == 0){
                 sender.sendMessage("No online players matched " + playerName + ".");
                 return true;
             }
 
             Player p = playerList.get(0);
             if(p.getGameMode() == GameMode.CREATIVE){
                 p.setGameMode(GameMode.SURVIVAL);
                 sender.sendMessage(ChatColor.GRAY + "Toggled game mode to " + ChatColor.GREEN + "SURVIVAL" + ChatColor.GRAY + " for " + ChatColor.WHITE + p.getDisplayName());
                 p.sendMessage(ChatColor.GRAY + sender.getName() + " set your game mode to " + ChatColor.GREEN + "SURVIVAL" + ChatColor.GRAY + ".");
                 //And notify any users who are set to be notified
                 for(Player np : Bukkit.getOnlinePlayers()){
                     if(np.equals(sender) || np.equals(p)) continue; //np == p, HOORAY!
                     if(np.hasPermission("gamegenie.notify")){
                         np.sendMessage(ChatColor.GRAY + sender.getName() + " toggled " + ChatColor.GREEN + "SURVIVAL" + ChatColor.GRAY + " on " + p.getDisplayName());
                     }
                 }
             } else {
                 p.setGameMode(GameMode.CREATIVE);
                 sender.sendMessage(ChatColor.GRAY +"Toggled game mode to " + ChatColor.YELLOW + "CREATIVE" + ChatColor.GRAY +" for "+ ChatColor.WHITE + p.getDisplayName());
                 p.sendMessage(ChatColor.GRAY + sender.getName() + " set your game mode to " + ChatColor.YELLOW + "CREATIVE" + ChatColor.GRAY + ".");
                 for(Player np : Bukkit.getOnlinePlayers()){
                     if(np.equals(sender) || np.equals(p)) continue;
                     if(np.hasPermission("gamegenie.notify")){
                        np.sendMessage(ChatColor.GRAY + sender.getName() + " toggled " + ChatColor.YELLOW + "CREATIVE" + ChatColor.GRAY + " on " + p.getDisplayName());
                     }
                 }
             }
         }
         return true;
     }
 }
