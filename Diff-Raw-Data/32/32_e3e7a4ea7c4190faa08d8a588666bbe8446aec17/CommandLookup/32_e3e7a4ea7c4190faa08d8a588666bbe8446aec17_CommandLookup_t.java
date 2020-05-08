 package me.arno.blocklog.commands;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import me.arno.blocklog.logs.LogType;
 import me.arno.blocklog.util.Query;
 import me.arno.blocklog.util.Text;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 
 public class CommandLookup extends BlockLogCommand {
 	public CommandLookup() {
 		super("blocklog.lookup");
 		setCommandUsage("/bl lookup [player <value>] [entity <value>] [since <value>] [until <value>]");
 	}
 
 	@Override
 	public boolean execute(Player player, Command cmd, String[] args) {
 		if(args.length < 2)
 			return false;
 		
 		if(args.length % 2 != 0) {
 			player.sendMessage("Invalid amount of args");
 			return true;
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
 				player.sendMessage("Until can't be bigger than since.");
 				return true;
 			}
 			
 			World world = player.getWorld();
 			
 			Query query = new Query("blocklog_blocks");
 			query.select("*");
 			query.selectDate("date");
 			if(target != null) {
 				query.where("entity", "player");
 				query.where("triggered", target);
 			}
 			if(entity != null)
 				query.where("entity", entity);
 			if(sinceTime != 0)
 				query.where("date", sinceTime.toString(), "<");
 			if(untilTime != 0)
 				query.where("date", untilTime.toString(), ">");
 			if(area != 0) {
 				Integer xMin = player.getLocation().getBlockX() - area;
 				Integer xMax = player.getLocation().getBlockX() + area;
 				Integer yMin = player.getLocation().getBlockY() - area;
 				Integer yMax = player.getLocation().getBlockY() + area;
 				Integer zMin = player.getLocation().getBlockZ() - area;
 				Integer zMax = player.getLocation().getBlockZ() + area;
 				
 				query.where("x", xMin.toString(), ">=");
 				query.where("x", xMax.toString(), "<=");
 				
 				query.where("y", yMin.toString(), ">=");
 				query.where("y", yMax.toString(), "<=");
 				
 				query.where("z", zMin.toString(), ">=");
 				query.where("z", zMax.toString(), "<=");
 			}
 			query.where("world", world.getName());
 			query.where("rollback_id", 0);
 			query.groupBy("x");
 			query.groupBy("y");
 			query.groupBy("z");
 			query.orderBy("date", "DESC");
 			query.limit(getSettingsManager().getMaxResults());
 			
 			ResultSet actions = query.getResult();
			player.sendMessage(ChatColor.YELLOW + "Player History" + ChatColor.DARK_GRAY + " -------------------------------");
            player.sendMessage(ChatColor.GRAY + Text.addSpaces("Name", 90) + Text.addSpaces("Action", 75) + "Details");
            
            while(actions.next()) {
 				String name = Material.getMaterial(actions.getInt("block_id")).toString();
 				LogType type = LogType.values()[actions.getInt("type")];
 				
 				//player.sendMessage(ChatColor.DARK_RED + "[World:" + actions.getString("world") + ", X:" + actions.getString("x") + ", Y:" + actions.getString("y") + ", Z:" + actions.getString("z") + "]");
 				player.sendMessage(Text.addSpaces(ChatColor.GOLD + actions.getString("triggered"), 99) + Text.addSpaces(ChatColor.DARK_RED + type.name(), 81) + ChatColor.GREEN + name + ChatColor.AQUA + " [" + actions.getString("date") + "]");
 			}
 		} catch(SQLException e) {
 			
 		}
 		return true;
 	}
 
 }
