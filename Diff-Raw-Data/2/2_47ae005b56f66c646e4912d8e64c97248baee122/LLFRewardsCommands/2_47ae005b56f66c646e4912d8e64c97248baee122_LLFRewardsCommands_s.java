 package com.llfrealms.LLFRewards.util;
 
 import org.bukkit.command.*;
 
 import com.llfrealms.LLFRewards.LLFRewards;
 import com.llfrealms.LLFRewards.util.Utilities;
 
 public class LLFRewardsCommands implements CommandExecutor 
 {
 	private LLFRewards plugin;
 	public LLFRewardsCommands(LLFRewards plugin) {
 		this.plugin = plugin;
 	}
  
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
 	{
 		if(cmd.getName().equalsIgnoreCase("llfsave") && sender.hasPermission("llf.save"))
 	    {
 			plugin.saveConfig();
         	sender.sendMessage("Config saved");
         	return true;
 	    }
 		if(cmd.getName().equalsIgnoreCase("llfload")  && sender.hasPermission("llf.load"))
 	    {
 			plugin.reloadConfig();
         	sender.sendMessage("Config reloaded");
         	return true;
 	    }
 		if(cmd.getName().equalsIgnoreCase("llfadd") && sender.hasPermission("llf.add"))
 	    {
 			// /<command> {rewardName} {powerlevel} r:{requirements yes/no} r1:{req1} r2:{req2} r3:{req3} r4:{req4} {commands}
			if(args.length > 0)
 			{
 				String reward = args[0], powerlvl = args[1], 
 						req = args[2], none = "none";
 				Integer plvl = Integer.parseInt(powerlvl);
 				String req1 = none, req2 = none, req3 = none, req4 = none;
 				String[]  temp, requirement1, requirement2, requirement3, requirement4;
 				boolean rewardExists = false, requirements = false;
 				for(int i = 0; i < plugin.name.size(); i++)
 				{
 					if(plugin.name.get(i).equalsIgnoreCase(reward))
 					{
 						rewardExists = true;
 					}
 				}
 				if(rewardExists)
 				{
 					Utilities.sendMessage(sender, reward + " already exists.");
 					return true;
 				}
 				else
 				{
 					if(req.contains(":"))
 					{
 						temp = req.split(":");
 						req = temp[1];
 					}
 					else
 					{
 						return false;
 					}
 					if(req.equalsIgnoreCase("yes"))
 					{
 						requirements = true;
 					}
 					if(requirements)
 					{
 						if(args.length >= 8)
 						{
 							String commands = Utilities.getFinalArg(args, 7);
 							requirement1 = args[3].split(":");
 							requirement2 = args[4].split(":");
 							requirement3 = args[5].split(":");
 							requirement4 = args[6].split(":");
 							req1 = requirement1[1];
 							req2 = requirement2[1];
 							req3 = requirement3[1];
 							req4 = requirement4[1];
 							plugin.plvl.add(plvl);
 							plugin.name.add(reward);
 							plugin.getConfig().set("rSetup.plvl", plugin.plvl);
 							plugin.getConfig().set("rSetup.name", plugin.name);
 							plugin.getConfig().createSection("rewards."+reward);
 							plugin.getConfig().createSection("rewards."+reward+".requirements");
 							plugin.getConfig().createSection("rewards."+reward+".oneStatAt");
 							plugin.getConfig().createSection("rewards."+reward+".twoStatsAt");
 							plugin.getConfig().createSection("rewards."+reward+".threeStatsAt");
 							plugin.getConfig().createSection("rewards."+reward+".allStatsAt");
 							plugin.getConfig().createSection("rewards."+reward+".commands");
 							plugin.getConfig().set("rewards."+reward+".requirements", requirements);
 							if(req1.equalsIgnoreCase(none) || req1.equalsIgnoreCase("0"))
 							{
 								req1 = none;
 								plugin.getConfig().set("rewards."+reward+".oneStatAt", req1);
 							}
 							else
 							{
 								plugin.getConfig().set("rewards."+reward+".oneStatAt", Integer.parseInt(req1));
 							}
 							if(req2.equalsIgnoreCase(none) || req2.equalsIgnoreCase("0"))
 							{
 								req2 = none;
 								plugin.getConfig().set("rewards."+reward+".twoStatsAt", req2);
 							}
 							else
 							{
 								plugin.getConfig().set("rewards."+reward+".twoStatsAt", Integer.parseInt(req2));
 							}
 							if(req3.equalsIgnoreCase(none) || req3.equalsIgnoreCase("0"))
 							{
 								req3 = none;
 								plugin.getConfig().set("rewards."+reward+".threeStatsAt", req3);
 							}
 							else
 							{
 								plugin.getConfig().set("rewards."+reward+".threeStatsAt", Integer.parseInt(req3));
 							}
 							if(req4.equalsIgnoreCase(none) || req4.equalsIgnoreCase("0"))
 							{
 								req4 = none;
 								plugin.getConfig().set("rewards."+reward+".allStatsAt", req4);
 							}
 							else
 							{
 								plugin.getConfig().set("rewards."+reward+".allStatsAt", Integer.parseInt(req4));
 							}
 							plugin.getConfig().set("rewards."+reward+".commands", commands);
 							Utilities.sendMessage(plugin.consoleMessage, reward + " sucessfully added!");
 							Utilities.sendMessage(sender, reward + " sucessfully added");
 							return true;
 						}
 						else if(args.length < 8)
 						{
 							Utilities.sendMessage(sender, "&4Not enough arguments");
 							return false;
 						}
 					}
 					else if(!requirements)
 					{
 						String commands = Utilities.getFinalArg(args, 3);
 						plugin.plvl.add(plvl);
 						plugin.name.add(reward);
 						plugin.getConfig().set("rSetup.plvl", plugin.plvl);
 						plugin.getConfig().set("rSetup.name", plugin.name);
 						plugin.getConfig().createSection("rewards."+reward);
 						plugin.getConfig().createSection("rewards."+reward+".requirements");
 						plugin.getConfig().createSection("rewards."+reward+".oneStatAt");
 						plugin.getConfig().createSection("rewards."+reward+".twoStatsAt");
 						plugin.getConfig().createSection("rewards."+reward+".threeStatsAt");
 						plugin.getConfig().createSection("rewards."+reward+".allStatsAt");
 						plugin.getConfig().createSection("rewards."+reward+".commands");
 						plugin.getConfig().set("rewards."+reward+".requirements", requirements);
 						plugin.getConfig().set("rewards."+reward+".oneStatAt", req1);
 						plugin.getConfig().set("rewards."+reward+".twoStatsAt", req2);
 						plugin.getConfig().set("rewards."+reward+".threeStatsAt", req3);
 						plugin.getConfig().set("rewards."+reward+".allStatsAt", req4);
 						plugin.getConfig().set("rewards."+reward+".commands", commands);
 						Utilities.sendMessage(plugin.consoleMessage, reward + " sucessfully added!");
 						Utilities.sendMessage(sender, reward + " sucessfully added");
 						return true;
 					}
 		        	return true;
 				}
 			}
 				
 	    }
         return false;
     }
 
 }
