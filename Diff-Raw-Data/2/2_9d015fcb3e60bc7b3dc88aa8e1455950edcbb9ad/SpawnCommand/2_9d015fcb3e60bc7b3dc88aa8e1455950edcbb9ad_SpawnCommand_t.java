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
 
 public class SpawnCommand implements CommandExecutor {
 
 	private CraftEssence plugin;
 
 	public SpawnCommand(CraftEssence plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (plugin.isPlayer(sender)) {
 			Player player = (Player) sender;
 			if (!CraftEssence.Permissions.has(player, "craftessence.spawn")) {
 				sender.sendMessage(ChatColor.YELLOW
 						+ "You to dont have proper permissions for that command.");
 				return true;
 			}
 		}
 		if (!plugin.isPlayer(sender))
 			return false;
 
 		Player player = (Player) sender;
 		Location loc = null;
 		loc = SpawnCommand.getSpawn(player);
		if (loc.getX() == 0)
 			loc = player.getWorld().getSpawnLocation();
 		
 		player.teleport(loc);
 		sender.sendMessage(CraftEssence.premessage + "Returned to spawn.");
 		return true;
 	}
 	
 	public static Location getSpawn(Player player) {
 		String world = player.getWorld().getName();
 		String spawnq = "Select * FROM warp WHERE `name` = 'spwn' AND `world` = '" + world + "'";
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
 			ps = conn.prepareStatement(spawnq);
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
 		return new Location(player.getWorld(), x, y, z, yaw, pitch);
 	}
 
 }
