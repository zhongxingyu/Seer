 package me.Whatshiywl.heroesskilltree.commands;
 
 import com.herocraftonline.heroes.characters.Hero;
 import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Multitallented
  */
 public class SkillAdminCommand {
     public static void skillAdmin(HeroesSkillTree hst, CommandSender sender, String[] args) {
         if(args.length < 1) {
             sender.sendMessage(ChatColor.RED + "Not enough arguments: /skilladmin <command> (amount) [sender]");
             return;
         }
         if(args[0].equalsIgnoreCase("clear")) {
             if(!sender.hasPermission("skilladmin.clear")) {
                 sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                 return;
             }
             if(args.length == 2){
                 
                 if(Bukkit.getPlayer(args[1]) != null) {
                     Hero thero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[1]));
                     hst.setPlayerPoints(thero, 0);
                 } else {
                     sender.sendMessage(ChatColor.RED + "Sorry, " + args[1] + " is not online.");
                     return;
                 }
             } else {
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                     return;
                 }
                 Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
                 
                 hst.setPlayerPoints(hero, 0);
             }
             sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA +
                     "You have reset " + args[1] + "'s SkillPoints.");
             return;
         }
         
         if(args[0].equalsIgnoreCase("reset")){
             if(!sender.hasPermission("skilladmin.reset")) {
                 sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                 return;
             }
             if(args.length == 2){
                 if(Bukkit.getPlayer(args[1]) != null){
                     hst.resetPlayer(Bukkit.getPlayer(args[1]));
                     sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA +
                             "You have reset " + args[1]);
                 } else {
                     sender.sendMessage(ChatColor.RED + "Sorry, " + args[1] + " is not online.");
                 }
             } else{
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                     return;
                 }
                 hst.resetPlayer((Player) sender);
                 sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have reset yourself.");
             }
             return;
         } 
         
        if(args.length > 1) {
             sender.sendMessage(ChatColor.RED + "/skilladmin (set/give/remove/clear/reset)");
             return;
         }
         
         if(args[0].equalsIgnoreCase("set")){
             if(!sender.hasPermission("skilladmin.set")) {
                 sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                 return;
             }
             if(args.length > 2){
                 if(Bukkit.getPlayer(args[2]) != null){
                     Hero thero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
                     hst.setPlayerPoints(thero, Integer.parseInt(args[1]));
                     sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have set " +
                             args[2] + "'s SkillPoints to " + Integer.parseInt(args[1]) + ".");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Sorry, " + args[2] + " is not online.");
                 }
             } else{
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                     return;
                 }
                 Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
                 
                 hst.setPlayerPoints(hero, Integer.parseInt(args[1]));
                 sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA +
                         "You have set your SkillPoints to " + Integer.parseInt(args[1]) + ".");
             }
             return;
         }
         
         if(args[0].equalsIgnoreCase("give")){
             if(!sender.hasPermission("skilladmin.give")) {
                 sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                 return;
             }
             if(args.length > 2){
                 if(Bukkit.getPlayer(args[2]) != null){
                     Hero thero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
                     hst.setPlayerPoints(thero, hst.getPlayerPoints(thero) + Integer.parseInt(args[1]));
                     sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have given " +
                             Integer.parseInt(args[1]) + " SkillPoint(s) to " + args[2] + ".");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Sorry, " + args[2] + " is not online.");
                 }
             } else {
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                     return;
                 }
                 Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
                 hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) + Integer.parseInt(args[1]));
                 sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have removed " +
                         Integer.parseInt(args[1]) + " SkillPoint(s) to yourself.");
             }
             return;
         }
         
         if(args[0].equalsIgnoreCase("remove")){
             if(sender.hasPermission("skilladmin.remove")) {
                 sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                 return;
             }
             if(args.length > 2){
                 if(Bukkit.getPlayer(args[2]) != null){
                     Hero thero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
                     hst.setPlayerPoints(thero, hst.getPlayerPoints(thero) - Integer.parseInt(args[1]));
                     sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have removed " +
                         Integer.parseInt(args[1]) + " SkillPoint(s) from " + args[2] + ".");
                 } else {
                     sender.sendMessage(ChatColor.RED + "Sorry, " + args[2] + " is not online.");
                 }
             } else {
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                     return;
                 }
                 Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
                 hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) - Integer.parseInt(args[1]));
                 sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have removed " +
                         Integer.parseInt(args[1]) + " SkillPoint(s) from yourself.");
             }
         }
     }
 }
