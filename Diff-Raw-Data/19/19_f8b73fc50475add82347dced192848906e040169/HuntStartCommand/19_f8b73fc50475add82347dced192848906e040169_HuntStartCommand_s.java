 package com.matejdro.bukkit.monsterhunt.commands;
 
 import org.bukkit.command.CommandSender;
 
 import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
 import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Settings;
 import com.matejdro.bukkit.monsterhunt.Util;
 
 public class HuntStartCommand extends BaseCommand {
 	
 	public HuntStartCommand()
 	{
 		needPlayer = false;
 		permission = "monsterhunt.admincmd.huntstart";
 	}
 
 
 	public Boolean run(CommandSender sender, String[] args) {		
 		if (args.length < 1 && HuntWorldManager.getWorlds().size() == 1)
 		{
			args = new String[]{HuntWorldManager.getWorlds().get(0).name};
 		}
 		else if (args.length < 1)
 		{
 			Util.Message("Usage: /huntstart [World Name]",sender);
 			return true;
 		}
 		else if (HuntWorldManager.getWorld(args[0]) == null)
 		{
 			Util.Message("There is no such world!", sender);
 			return true;
 		}
 		MonsterHuntWorld world = HuntWorldManager.getWorld(args[0]);
 		world.start();
 		world.manual = true;
 		return true;
 	}
 
 }
