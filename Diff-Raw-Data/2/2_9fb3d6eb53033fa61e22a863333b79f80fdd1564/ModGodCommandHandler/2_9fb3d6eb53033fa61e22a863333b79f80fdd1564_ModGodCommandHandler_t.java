 package com.github.CubieX.ModGod;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class ModGodCommandHandler implements CommandExecutor
 {
    private ModGod plugin = null;
    private ModGodConfigHandler configHandler = null;
 
    public ModGodCommandHandler(ModGod plugin, ModGodConfigHandler configHandler) 
    {
       this.plugin = plugin;
       this.configHandler = configHandler;
    }
 
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
       Player player = null;
       if (sender instanceof Player) 
       {
          player = (Player) sender;
       }
 
       if(ModGod.debug){ModGod.log.info("onCommand");}
       if (cmd.getName().equalsIgnoreCase("mg"))
       {
          if (args.length == 0)
          { //no arguments, so help will be displayed
             return false;
          }
          if (args.length==1)
          {
             if (args[0].equalsIgnoreCase("version"))
             {            
                sender.sendMessage(ChatColor.YELLOW + "This server is running ModGod version " + plugin.getDescription().getVersion());
                return true;
             }    
             if (args[0].equalsIgnoreCase("reload"))
             {            
               if(sender.hasPermission("modgod.admin"))
                {                        
                   configHandler.reloadConfig(sender);                  
                   return true;
                }
                else
                {
                   sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
                }
             }
          }
          else
          {
             sender.sendMessage(ChatColor.YELLOW + "Ungueltige Anzahl Argumente.");
          }                
 
       }         
       return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }
 }
