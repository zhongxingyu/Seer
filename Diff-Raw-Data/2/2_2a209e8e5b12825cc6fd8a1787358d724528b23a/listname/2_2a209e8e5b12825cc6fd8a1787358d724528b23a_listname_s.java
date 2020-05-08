 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.timolia.core.cmds;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class listname extends TCommand {
 
 	public static String allowed = "abcdefghijklmnopqrstuvwxyz0123456789$_";
 
 	public listname(String name) {
 		super(name);
 		setMaxArgs(2);
 		setUsage("/listname [Player] <Name>");
 		setDesc("Change your or someone elses playerlistname");
 	}
 
 	public void perform(CommandSender sender, String[] args) {
 		if (args.length == 0) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage(_("ingame"));
 				return;
 			}
 
 			Player p = (Player) sender;
 			p.setPlayerListName(p.getName());
 			sender.sendMessage(_("listNameSet"));
 			return;
 		}
 
 		if (args[0].equalsIgnoreCase("reset")) {
 			if (!sender.hasPermission("tcore.listname.reset")) {
 				sender.sendMessage(_("noperm"));
 				return;
 			}
 
 			for (Player p : Bukkit.getOnlinePlayers())
 				p.setPlayerListName(p.getName());
 
 			sender.sendMessage(_("listReset"));
 			return;
 		}
 
 		Player target;
 		if (args.length == 1) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage(_("ingame"));
 				return;
 			}
 
 			target = (Player) sender;
 
 		} else {
 			if (!sender.hasPermission("tcore.listname.other")) {
 				sender.sendMessage(_("noperm"));
 				return;
 			}
 
 			target = Bukkit.getPlayer(args[0]);
 			if (target == null) {
 				sender.sendMessage(_("notonline"));
 				return;
 			}
 		}
 
		String out = ((args.length == 1) ? args[0] : args[1]) + "$r";
 		out = ChatColor.translateAlternateColorCodes('&', out);
 
 		if (out.length() > 16) {
 			sender.sendMessage(_("listnameTooLong"));
 			return;
 		}
 
 		for (int i = 0; i < out.length(); i++)
 			if (!allowed.contains(String.valueOf(out.toLowerCase().charAt(i)))) {
 				sender.sendMessage(_("illegalChar"));
 				return;
 			}
 
 		try {
 			target.setPlayerListName(out);
 			sender.sendMessage(_("listNameSet"));
 		} catch (Exception e) {
 			sender.sendMessage(_("nameAlreadyAssigned"));
 		}
 	}
 
 }
