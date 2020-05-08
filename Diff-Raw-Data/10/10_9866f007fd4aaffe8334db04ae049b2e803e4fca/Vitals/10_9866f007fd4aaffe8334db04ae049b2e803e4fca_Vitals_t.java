 package me.Nelsin.Vitals ;
  
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
  
  
 public class Vitals extends JavaPlugin {
    
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        
         if (!(sender instanceof Player )) {
             sender.sendMessage("The console ran the needs command!"); //Broadcasts the message when console uses command
             return true;
         }
        
         Player player = (Player) sender;
        
         if (cmd.getName().equalsIgnoreCase("tips")) {
             player.sendMessage(ChatColor.AQUA + "This Servers tips are: 1./n 2./n 3./n 4./n 5./n"); //Shows the list of tips for current server
             return true;
         } else if (cmd.getName().equalsIgnoreCase("needs")) {
             player.sendMessage(ChatColor.GREEN + "Manager: Nelsin/n Project Lead: Igorvanloo/n Developers:"); //Shows list of Developers
             return true;
         } else if (cmd.getName().equalsIgnoreCase("hello")) {
         	player.sendMessage(ChatColor.AQUA + "Hello");
         	return true;
         } else if (cmd.getName().equalsIgnoreCase("Hi")) {
         	player.sendMessage(ChatColor.AQUA + "Hi");
         	return true;
        } else if (cmd.getName().equalsIgnoreCase("hallo")) {
         player.sendMessage(ChatColor.AQUA + "Hallo");
         return true;
        } else if (cmd.get().equalsIgnoreCase("Bonjour")) {
        player.sendMessage(ChatColor.AQUA + "Bonjour');
        return true;
         } else {
        return false
     }  
 }
