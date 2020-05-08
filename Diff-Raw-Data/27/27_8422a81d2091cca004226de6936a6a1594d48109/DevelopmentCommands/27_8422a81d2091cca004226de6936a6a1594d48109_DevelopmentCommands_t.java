 package com.censoredsoftware.Demigods.Engine.Command;
 
 import org.bukkit.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Battle.Battle;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.SpigotUtility;
 
 public class DevelopmentCommands implements CommandExecutor
 {
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String labels, String[] args)
 	{
 		if(command.getName().equalsIgnoreCase("removechar")) return removeChar(sender, args);
 		else if(command.getName().equalsIgnoreCase("test1")) return test1(sender, args);
 		else if(command.getName().equalsIgnoreCase("test2")) return test2(sender, args);
 		else if(command.getName().equalsIgnoreCase("hspawn")) return hspawn(sender);
 		else if(command.getName().equalsIgnoreCase("soundtest")) return soundTest(sender, args);
 		return false;
 	}
 
 	private static boolean test1(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		player.sendMessage("Removing data...");
 
 		for(Battle battle : Battle.getAll())
 		{
 			battle.delete();
 		}
 
 		player.sendMessage("Data removed!");
 
 		return true;
 	}
 
 	private static boolean test2(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		if(!SpigotUtility.runningSpigot()) return true;
 
 		final Location center = player.getLocation();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Demigods.plugin, new BukkitRunnable()
 		{
			@Override
			public void run()
 			{
				SpigotUtility.drawCircle(center, Effect.MOBSPAWNER_FLAMES, 16, 120);
			}
		}, 20, 20);
 
 		return true;
 	}
 
 	private static boolean hspawn(CommandSender sender)
 	{
 		Player player = (Player) sender;
 
 		// This SHOULD happen automatically, but bukkit doesn't do this, so we need to.
 
 		if(player.isInsideVehicle() && player.getVehicle() instanceof Horse)
 		{
 			Horse horse = (Horse) player.getVehicle();
 			horse.eject();
 			horse.teleport(player.getLocation().getWorld().getSpawnLocation());
 			horse.setPassenger(player);
 			player.sendMessage(ChatColor.YELLOW + "Teleported to spawn...");
 		}
 
 		return true;
 	}
 
 	private static boolean soundTest(CommandSender sender, final String[] args)
 	{
 		if(sender instanceof ConsoleCommandSender) return false;
 		Player player = (Player) sender;
 		try
 		{
 			Sound sound = Sound.valueOf(args[0].toUpperCase());
 			if(!MiscUtility.isFloat(args[1].toUpperCase()))
 			{
 				player.sendMessage(ChatColor.RED + "Set a pitch, ie: 1F");
 				return false;
 			}
 			else
 			{
 				player.playSound(player.getLocation(), sound, 1F, Float.parseFloat(args[1].toUpperCase()));
 				player.sendMessage(ChatColor.YELLOW + "Sound played.");
 				return true;
 			}
 		}
 		catch(Exception ignored)
 		{}
 		player.sendMessage(ChatColor.RED + "Wrong arguments, please try again.");
 		return false;
 	}
 
 	private static boolean removeChar(CommandSender sender, String[] args)
 	{
 		if(args.length != 1) return false;
 
 		// Define args
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 		String charName = args[0];
 
 		if(PlayerWrapper.hasCharName(player, charName))
 		{
 			PlayerCharacter character = PlayerCharacter.getCharacterByName(charName);
 			character.remove();
 
 			sender.sendMessage(ChatColor.RED + "Character removed!");
 		}
 		else sender.sendMessage(ChatColor.RED + "There was an error while removing your character.");
 
 		return true;
 	}
 }
