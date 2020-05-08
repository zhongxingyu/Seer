 package org.efreak.warps.commands;
 
 import java.util.Arrays;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.efreak.warps.IOManager;
 import org.efreak.warps.Permissions;
 import org.efreak.warps.Warp;
 import org.efreak.warps.WarpsReloaded;
 import org.efreak.warps.help.HelpManager;
 
 public class SetwarpCommand implements CommandExecutor {
 
 	private static IOManager io;
 	
 	static {
 		io = WarpsReloaded.getIOManager();
 	}
 	
 	public SetwarpCommand() {
		HelpManager.registerCommand("Warp.Create", "/setwarp", Arrays.asList("(name)", "[p=permission]", "[c=cost]"), "warps.create");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player p = null;
 		ConsoleCommandSender c = null;
 		if (sender instanceof Player) p = (Player) sender;
 		if (sender instanceof ConsoleCommandSender) c = (ConsoleCommandSender) sender;
 		if (p == c) return false;
 		if (args.length == 0) return false;
 		else {
 			if (label.equalsIgnoreCase("setwarp")) {
 				if (Permissions.has(sender, "warps.create", true)) {
 					if (args.length > 3) io.sendError(sender, "Usage: /setwarp (name) [p=permission] [c=cost]");
 					else if (args.length == 1) {
 						Warp.create(args[0], ((Player) sender).getLocation(), null, 0); 
 						io.send(sender, "Warp " + args[0] + " was successfully created");
 					}else if (args.length == 2) {
 						if (args[1].startsWith("p=")) Warp.create(args[0], ((Player) sender).getLocation(), args[1].substring(2), 0);	
 						else if (args[1].startsWith("c=")) Warp.create(args[0], ((Player) sender).getLocation(), null, Double.valueOf(args[1].substring(2)));
 						else Warp.create(args[0], ((Player) sender).getLocation(), null, 0);
 						io.send(sender, "Warp " + args[0] + " was successfully created");
 					}else if (args.length == 3) {
 						String perm = null;
 						double cost = 0;
 						if (args[1].startsWith("p=")) perm = args[1].substring(2);
 						else if (args[1].startsWith("c=")) cost = Double.valueOf(args[1].substring(2));
 						if (args[2].startsWith("p=")) perm = args[2].substring(2);
 						else if (args[2].startsWith("c=")) cost = Double.valueOf(args[2].substring(2));
 						Warp.create(args[0], ((Player) sender).getLocation(), perm, cost);
 						io.send(sender, "Warp " + args[0] + " was successfully created");
 					}else io.sendError(sender, "Usage: /setwarp (name) [p=permission] [c=cost]");
 				}
 				return true;
 			}else return false;
 		}
 	}
 }
