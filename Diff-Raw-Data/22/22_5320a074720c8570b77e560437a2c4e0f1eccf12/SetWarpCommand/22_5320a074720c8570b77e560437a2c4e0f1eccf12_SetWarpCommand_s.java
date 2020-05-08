 package me.furt.CraftEssence.commands;
 
 import me.furt.CraftEssence.CraftEssence;
 import me.furt.CraftEssence.sql.WarpTable;
 
 import org.bukkit.ChatColor;
import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SetWarpCommand implements CommandExecutor {
 	CraftEssence plugin;
 
 	public SetWarpCommand(CraftEssence instance) {
 		this.plugin = instance;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (!plugin.hasPerm(sender, "setwarp", false)) {
 			sender.sendMessage(ChatColor.YELLOW
 					+ "You to dont have proper permissions for that command.");
 			return true;
 		}
 
 		Player player = (Player) sender;
 		String wname = player.getWorld().getName();
 		WarpTable wt = plugin.getDatabase().find(WarpTable.class).where()
 				.ieq("name", args[0]).ieq("world", wname).findUnique();
		if (wt != null) {
			player.sendMessage(CraftEssence.premessage
					+ "Your warp location is already set.");
			return true;
		}
 		if (wt == null) {
 			wt = new WarpTable();
 			wt.setName(args[0]);
 		}
		Location pl = player.getLocation();
		wt.setWorld(pl.getWorld().getName());
		wt.setX(pl.getX());
		wt.setY(pl.getY());
		wt.setZ(pl.getZ());
		wt.setYaw(pl.getYaw());
		wt.setPitch(pl.getPitch());
 		plugin.getDatabase().save(wt);
 		player.sendMessage(CraftEssence.premessage + args[0] + " warp set.");
 		return true;
 	}
 }
