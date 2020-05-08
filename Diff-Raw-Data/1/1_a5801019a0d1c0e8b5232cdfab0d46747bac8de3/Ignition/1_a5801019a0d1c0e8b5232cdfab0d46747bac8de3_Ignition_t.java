 package com.github.Ninja3047.Ignition;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Ignition extends JavaPlugin
 {
 	public void onEnable() // When the plugin is enabled
 	{
 
 		getLogger().info("Ignition has been enabled. "); // display message
 
 	}
 
 	public void onDisable() // When the plugin is disabled
 	{
 
 		getLogger().info("Ignition has been disabled. "); // display message
 
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args)
 	{
 		if (cmd.getName().equalsIgnoreCase("ignite")) //if player typed /ignite
 		{
 			sender.sendMessage("You call on the fires of Hell...");
 			Player s = (Player) sender;
 			Player target = s.getServer().getPlayer(args[0]); // Gets the player who was typed in the command. 
 			target.setFireTicks(10000); //set the target on fire for 10000 milliseconds
 			sender.sendMessage("and you set " + target.getDisplayName()	+ " on fire!"); //displays a message
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("strike"))
 		{
 			sender.sendMessage("You call on the powers of Zeus...");
 			Player s = (Player) sender;
 			Player target = s.getServer().getPlayer(args[0]);
 			World world = target.getWorld();
 			Location location = target.getLocation();
 			world.strikeLightning(location);
 			sender.sendMessage("and you strike "+ target.getDisplayName() + " down from the heavens. ");
			return true;
 		}
 		
 		return false;
 	}
 }
