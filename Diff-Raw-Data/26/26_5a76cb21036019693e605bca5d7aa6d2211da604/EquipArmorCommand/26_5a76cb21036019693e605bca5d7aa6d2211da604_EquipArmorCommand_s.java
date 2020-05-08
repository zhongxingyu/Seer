 package me.Joeyy.Armor;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class EquipArmorCommand implements CommandExecutor {
 	Armor plugin;
 
 	public EquipArmorCommand(Armor instance) {
 		this.plugin = instance;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		System.out.println("ARMOR 1");
 		Player target = null;
 
 		// CommandSender could be server console.
 		if (args.length < 1)
 			return false;
 		System.out.println("ARMOR 2");
 		if (plugin.isPlayer(sender)) {
 			if (!Armor.Permissions.has((Player) sender, "Armor.equiparmor")) {
 				sender.sendMessage(ChatColor.YELLOW
 						+ "You to dont have proper permissions for that command.");
 				return true;
 			}
 			// default
 			target = (Player) sender;
 		}
 		System.out.println("ARMOR 3");
 
 		// check for both player and console
 		if (args.length >= 2) {
 			Player p = plugin.getServer().getPlayer(args[1]);
			if (p != null)
 				target = p;
 		}
 		System.out.println("ARMOR 4");
 		// send console an error message
 		if (target == null) {
 			sender.sendMessage("No player specified");
 			return true;
 		}
 		// find armor type
		ArmorType set = ArmorType.valueOf(args[0].toUpperCase());
 		System.out.println("ARMOR 5");
 		if (set == null) {
 			// maybe send list of valid armor types
 			sender.sendMessage("Invalid armor type. Types : "
 					+ ArmorType.getTypeNames());
 			return true;
 		}
 		System.out.println("ARMOR 6");
 		// now give target the armor
 		Armor.givePlayerArmor(target, set);
 		System.out.println("ARMOR 7");
 		return true;
 	}
 }
