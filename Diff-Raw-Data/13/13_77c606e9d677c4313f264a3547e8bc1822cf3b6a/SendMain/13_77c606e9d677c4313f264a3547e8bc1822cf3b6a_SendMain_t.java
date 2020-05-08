 package com.justinmaslin.SendCoords;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SendMain extends JavaPlugin
 {
 
 	public void onEnable()
 	{
 		getLogger().info("[SendCoords] Plugin Enabled!");
 		getLogger().info("Created for Merecraft! (www.merecraft.com)");
 	}
 
 	public void onDisable()
 	{
 		getLogger().info("[SendCoords] Plugin Disabled!");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 
 		if (cmd.getName().equalsIgnoreCase("sendcoords") && sender.hasPermission("sendcoords.use"))
 		{
 			if (!(sender instanceof Player))
 				sender.sendMessage("§b[SendCoords] §eYou must run this command in-game!");
 
			else if (args.length != 1)
				sender.sendMessage("§b[SendCoords] §eThe correct usage is: §c/sendcoords <name>");

 			else
 			{
 				if (!(Bukkit.getServer().getPlayer(args[0]) instanceof Player))
 					sender.sendMessage("§b[SendCoords] §eThis player is offline or does not exist!");
 
 				else
 				{		
 					Player target = (Bukkit.getServer().getPlayer(args[0]));
 
 					Location pos = ((Player) sender).getLocation();
 
 					int xPos = pos.getBlockX();
 					int yPos = pos.getBlockY();
 					int zPos = pos.getBlockZ();
 
					sender.sendMessage("§b[SendCoords] §eCoordinates sent to "+target.getName()+".");
					target.sendMessage("§b[SendCoords] §e"+sender.getName()+" is at: "+xPos+", "+yPos+", "+zPos+".");

 				}
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 
 }
