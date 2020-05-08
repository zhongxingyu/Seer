 package com.justinmaslin.SendCoords;
 
 import java.text.DecimalFormat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SendMain extends JavaPlugin
 {
 	DecimalFormat df = new DecimalFormat("0.0");
 	
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
 					sender.sendMessage("§b[SendCoords] §eThat player is offline or does not exist!");
 
 				else
 				{		
 					Player target = (Bukkit.getServer().getPlayer(args[0]));
 
 					Location senderPos = ((Player) sender).getLocation();
 					Location targetPos = ((Player) target).getLocation();
 					
 					double distance=Math.sqrt(Math.pow((senderPos.getX()-targetPos.getBlockX()), 2)+Math.pow((senderPos.getBlockZ()-targetPos.getBlockZ()), 2));

 					
					sender.sendMessage("§b[SendCoords] §eCoordinates sent to "+target.getName()+".");
					target.sendMessage("§b[SendCoords] §e"+sender.getName()+" is at: "+senderPos.getBlockX()+", "+senderPos.getBlockY()+", "+senderPos.getBlockZ()+
							" which is "+df.format(distance)+" blocks away.");
 				}
 			}
 
 			return true;
 		}
 		
 
 		return false;
 	}
 
 }
