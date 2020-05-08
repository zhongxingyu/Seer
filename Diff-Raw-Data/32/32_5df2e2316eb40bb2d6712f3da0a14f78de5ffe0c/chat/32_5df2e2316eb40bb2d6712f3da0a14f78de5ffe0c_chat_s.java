 package nl.giantit.minecraft.GiantShop.Executors;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.Commands.*;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Misc;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Giant
  */
 public class chat {
 	
 	private GiantShop plugin;
 	
 	public chat(GiantShop plugin) {
 		this.plugin = plugin;
 	}
 
 	public boolean exec(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = (Player) sender;
 		
 		if(args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?")) {
			if(plugin.useLoc && plugin.cmds.contains("help")) {
 				if(plugin.getLocHandler().canUse(player))
 					help.showHelp(player, args);
 			}else
 				help.showHelp(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "sendhelp", "sh")) {
			if(plugin.useLoc && plugin.cmds.contains("sendhelp")) {
 				if(plugin.getLocHandler().canUse(player))
 					help.sendHelp(player, args);
 			}else
 				help.sendHelp(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "list", "l")) {
			if(plugin.useLoc && plugin.cmds.contains("list")) {
 				if(plugin.getLocHandler().canUse(player))
 					list.list(player, args);
 			}else
 				list.list(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "check", "c")) {
			if(plugin.useLoc && plugin.cmds.contains("check")) {
 				if(plugin.getLocHandler().canUse(player))
 					check.check(player, args);
 			}else
 				check.check(player, args);
 		}else if(args[0].equalsIgnoreCase("search")) {
 			//futuristic ideas! :D
 		}else if(Misc.isEitherIgnoreCase(args[0], "buy", "b")) {
			if(plugin.useLoc && plugin.cmds.contains("buy")) {
 				if(plugin.getLocHandler().canUse(player))
 					buy.buy(player, args);
 			}else
 				buy.buy(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "sell", "s")) {
			if(plugin.useLoc && plugin.cmds.contains("sell")) {
 				if(plugin.getLocHandler().canUse(player))
 					sell.sell(player, args);
 			}else
 				sell.sell(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "gift", "g")) {
			if(plugin.useLoc && plugin.cmds.contains("gift")) {
 				if(plugin.getLocHandler().canUse(player))
 					buy.gift(player, args);
 			}else
 				buy.gift(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "add", "a")) {
			if(plugin.useLoc && plugin.cmds.contains("add")) {
 				if(plugin.getLocHandler().canUse(player))
 					add.add(player, args);
 			}else
 				add.add(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "update", "u")) {
			if(plugin.useLoc && plugin.cmds.contains("update")) {
 				if(plugin.getLocHandler().canUse(player))
 					update.update(player, args);
 			}else
 				update.update(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "remove", "r")) {
			if(plugin.useLoc && plugin.cmds.contains("remove")) {
 				if(plugin.getLocHandler().canUse(player))
 					remove.remove(player, args);
 			}else
 				remove.remove(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "addstock", "as")) {
 			//stalled
 			//Probably no longer required
			if(plugin.useLoc && plugin.cmds.contains("addstock")) {
 				if(plugin.getLocHandler().canUse(player))
 					help.showHelp(player, args);
 			}else
 				help.showHelp(player, args);
 		}else{
 			Heraut.say(player, "Ok, we have no friggin clue what you are on about, so what about we just send you our help page?");
 			help.showHelp(player, args);
 		}
 
 		return true;
 	}
 }
