 package me.arno.blocklog.commands;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.database.Query;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 
 public class CommandLookup extends BlockLogCommand {
 	public CommandLookup(BlockLog plugin) {
 		super(plugin, "blocklog.lookup");
 	}
 
 	public boolean execute(Player player, Command cmd, String[] args) {
 		if(args.length < 2) {
			player.sendMessage(ChatColor.WHITE + "/bl lookup [player <value>] [since <value>] [until <value>] [area <value>]");
 			return true;
 		}
 		
 		Integer clauses = args.length/2;
 		
 		if(!clauses.toString().matches("[0-9]*")) {
 			player.sendMessage("Invalid amount of args");
 		}
 		
 		if(!hasPermission(player)) {
 			player.sendMessage("You don't have permission");
 			return true;
 		}
 		
 		try {
 			String target = null;
 			String entity = null;
 			Integer untilTime = 0;
 			Integer sinceTime = 0;
 			Integer area = 0;
 			
 			for(int i=0;i<args.length;i+=2) {
 				String type = args[i];
 				String value = args[i+1];
 				if(type.equalsIgnoreCase("area")) {
 					area = Integer.valueOf(value);
 				} else if(type.equalsIgnoreCase("player")) {
 					target = value;
 				} else if(type.equalsIgnoreCase("entity")) {
 					entity = value;
 				} else if(type.equalsIgnoreCase("since")) {
 					Character c = value.charAt(value.length() - 1);
 					sinceTime = convertToUnixtime(Integer.valueOf(value.replace(c, ' ').trim()), c.toString());
 				} else if(type.equalsIgnoreCase("until")) {
 					Character c = value.charAt(value.length() - 1);
 					untilTime = convertToUnixtime(Integer.valueOf(value.replace(c, ' ').trim()), c.toString());
 				}
 			}
 			
 			if(untilTime != 0 && sinceTime > untilTime) {
 				player.sendMessage(ChatColor.WHITE + "Until can't be bigger than since.");
 				return true;
 			}
 			
 			World world = player.getWorld();
 			
 			Query query = new Query("blocklog_blocks");
 			query.addSelect("*");
 			query.addSelectDate("date");
 			if(target != null) {
 				query.addWhere("entity", "player");
 				query.addWhere("trigered", target);
 			}
 			if(entity != null)
 				query.addWhere("entity", entity);
 			if(sinceTime != 0)
 				query.addWhere("date", sinceTime.toString(), "<");
 			if(untilTime != 0)
 				query.addWhere("date", untilTime.toString(), ">");
 			if(area != 0) {
 				Integer xMin = player.getLocation().getBlockX() - area;
 				Integer xMax = player.getLocation().getBlockX() + area;
 				Integer yMin = player.getLocation().getBlockY() - area;
 				Integer yMax = player.getLocation().getBlockY() + area;
 				Integer zMin = player.getLocation().getBlockZ() - area;
 				Integer zMax = player.getLocation().getBlockZ() + area;
 				
 				query.addWhere("x", xMin.toString(), ">=");
 				query.addWhere("x", xMax.toString(), "<=");
 				
 				query.addWhere("y", yMin.toString(), ">=");
 				query.addWhere("y", yMax.toString(), "<=");
 				
 				query.addWhere("z", zMin.toString(), ">=");
 				query.addWhere("z", zMax.toString(), "<=");
 			}
 			query.addWhere("world", world.getName());
 			query.addWhere("rollback_id", new Integer(0).toString());
 			query.addGroupBy("x");
 			query.addGroupBy("y");
 			query.addGroupBy("z");
 			query.addOrderBy("date", "DESC");
 			query.addLimit(getConfig().getInt("blocklog.results"));
 			
 			Statement stmt = conn.createStatement();
 			ResultSet actions = stmt.executeQuery(query.getQuery());
 			
 			while(actions.next()) {
 				String name = Material.getMaterial(actions.getInt("block_id")).toString();
 				int type = actions.getInt("type");
 				
 				player.sendMessage(ChatColor.BLUE + "[" + actions.getString("fdate") + "]" + ChatColor.DARK_RED + "[World:" + actions.getString("world") + ", X:" + actions.getString("x") + ", Y:" + actions.getString("y") + ", Z:" + actions.getString("z") + "]");
 				if(type == 0) {
 					player.sendMessage(ChatColor.GOLD + actions.getString("player") + ChatColor.DARK_GREEN + " broke a " + ChatColor.GOLD + name);
 				} else if(type == 1) {
 					player.sendMessage(ChatColor.GOLD + actions.getString("player") + ChatColor.DARK_GREEN + " placed a " + ChatColor.GOLD + name);
 				}
 			}
 		} catch(SQLException e) {
 			
 		}
 		return true;
 	}
 
 }
