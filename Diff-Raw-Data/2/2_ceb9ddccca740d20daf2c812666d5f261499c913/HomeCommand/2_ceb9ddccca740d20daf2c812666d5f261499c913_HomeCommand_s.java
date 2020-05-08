 package me.furt.CraftEssence.commands;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 
 import me.furt.CraftEssence.CraftEssence;
 import me.furt.CraftEssence.sql.ceConnector;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class HomeCommand implements CommandExecutor {
 	CraftEssence plugin;
 
 	public HomeCommand(CraftEssence instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (!plugin.isPlayer(sender)) {
 			sender.sendMessage(CraftEssence.premessage
 					+ "Cannot use this command from console.");
 			return true;
 		}
 		if (!CraftEssence.Permissions.has((Player) sender, "craftessence.home")) {
 			sender.sendMessage(ChatColor.YELLOW
 					+ "You to dont have proper permissions for that command.");
 			return true;
 		}
 
 		Player player = (Player) sender;
 
 		if (args.length == 0) {
 			player.teleport(this.getHome(player));
 			player.sendMessage(CraftEssence.premessage + "Teleporting home...");
 			return true;
 		}
 
 		if (args.length == 1) {
 			if (args[0].equalsIgnoreCase("accept")) {
 				String[] homeArray = CraftEssence.homeInvite
 						.toArray(new String[] {});
 				for (String list : homeArray) {
 					String[] homeSplit = list.split(":");
 					if (homeSplit[1].equalsIgnoreCase(player.getName()
 							.toLowerCase())) {
 						Player p = plugin.getServer().getPlayer(homeSplit[0]);
 						player.teleport(this.getHome(p));
 						sender.sendMessage(CraftEssence.premessage
 								+ "Teleporting to " + p.getDisplayName()
 								+ "'s home...");
 						CraftEssence.homeInvite.remove(list);
 						return true;
 					}
 				}
 				sender.sendMessage(CraftEssence.premessage
 								+ "Must be invited to a home to use this command.");
 				return true;
 			} else {
 				if (plugin.getServer().getPlayer(args[0]) != null) {
 					if (!CraftEssence.Permissions.has((Player) sender,
 							"craftessence.home.admin")) {
 						sender.sendMessage(ChatColor.YELLOW
 								+ "You to dont have proper permissions for that command.");
 						return true;
 					}
 					Player p = plugin.getServer().getPlayer(args[0]);
 					player.teleport(this.getHome(p));
 					sender.sendMessage(CraftEssence.premessage
							+ "Teleporting to " + player.getDisplayName()
 							+ "'s home...");
 					return true;
 				} else {
 					sender.sendMessage(CraftEssence.premessage
 							+ "Player name could not be found.");
 					return true;
 				}
 			}
 		}
 
 		if (args.length == 2) {
 			if ((args[0].equalsIgnoreCase("invite"))
 					&& (plugin.getServer().getPlayer(args[1]) != null)) {
 				if (!CraftEssence.Permissions.has((Player) sender,
 						"craftessence.home.invite")) {
 					sender.sendMessage(ChatColor.YELLOW
 							+ "You to dont have proper permissions for that command.");
 					return true;
 				}
 				Player p = plugin.getServer().getPlayer(args[1]);
 				CraftEssence.homeInvite.add(player.getName().toLowerCase() + ":" + p.getName().toLowerCase());
 				p.sendMessage(CraftEssence.premessage
 						+ "You have been invited to " + player.getDisplayName()
 						+ "'s home type /home accept to teleport there.");
 				sender.sendMessage(CraftEssence.premessage
 						+ "Home invite sent to " + p.getDisplayName());
 				return true;
 			} else {
 				sender.sendMessage(CraftEssence.premessage
 						+ "Player name could not be found.");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Location getHome(Player player) {
 		String world = player.getWorld().getName();
 		String getname = player.getName();
 		String homeq = "Select * FROM home WHERE `name` = '" + getname
 				+ "' AND `world` = '" + world + "'";
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 
 		double x = 0;
 		double y = 0;
 		double z = 0;
 		float pitch = 0;
 		float yaw = 0;
 
 		try {
 			conn = ceConnector.getConnection();
 			ps = conn.prepareStatement(homeq);
 			rs = ps.executeQuery();
 			conn.commit();
 			while (rs.next()) {
 				x = rs.getDouble("x");
 				y = rs.getDouble("y");
 				z = rs.getDouble("z");
 				yaw = rs.getFloat("yaw");
 				pitch = rs.getFloat("pitch");
 
 			}
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null)
 					conn.close();
 			} catch (SQLException ex) {
 				CraftEssence.log.log(Level.SEVERE,
 						"[CraftEssence]: Find SQL Exception (on close)");
 			}
 		}
 		if (x != 0)
 			return new Location(player.getWorld(), x, y, z, yaw, pitch);
 
 		return player.getWorld().getSpawnLocation();
 	}
 
 }
