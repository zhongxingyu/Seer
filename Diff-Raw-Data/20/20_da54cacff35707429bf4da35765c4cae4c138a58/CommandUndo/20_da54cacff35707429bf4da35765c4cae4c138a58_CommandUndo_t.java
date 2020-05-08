 package me.arno.blocklog.commands;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import me.arno.blocklog.Undo;
 import me.arno.blocklog.schedules.RollbackSchedule;
 import me.arno.blocklog.util.Query;
 import me.arno.blocklog.util.Syntax;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandUndo extends BlockLogCommand {
 	public CommandUndo() {
 		super("blocklog.rollback");
 		setCommandUsage("/bl undo [id] [delay <value>] [limit <amount>]");
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, Command cmd, String[] args) {
 		if(args.length > 5)
 			return false;
 		
 		if(!hasPermission(sender)) {
 			sender.sendMessage("You don't have permission");
 			return true;
 		}
 		
 		Player player = (Player) sender;
 		
 		int rollbackID = 0;
 		
 		try {
			Syntax syn = new Syntax(args, 1);
 
 			int limit = syn.getInt("limit", 200);
 			int delay = syn.getTime("delay", "3s");
 			
 			if(args.length == 1) {
 				rollbackID = Integer.valueOf(args[0]);
 			} else {
 				ResultSet rs = new Query("blocklog_rollbacks").select("id").orderBy("id", "DESC").getResult();
 				rs.next();
 				rollbackID = rs.getInt("id");
 			}
 			
 			if(rollbackID == 0) {
 				player.sendMessage(ChatColor.WHITE + "Rollback ID can't be 0");
 				return true;
 			}
 			
 			Query query = new Query("blocklog_undos");
 			
 			HashMap<String, Object> values = new HashMap<String, Object>();
 			values.put("rollback", rollbackID);
 			values.put("player", player.getName());
 			values.put("date", System.currentTimeMillis()/1000);
 			
 			query.insert(values);
 			
 			Undo undo = new Undo(player, delay, limit, rollbackID);
 			int blockCount = undo.getAffectedBlockCount();
 			
 			RollbackSchedule undoSchedule = new RollbackSchedule(undo);
 			int sid = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, undoSchedule, 20L, delay * 20L);
 			undoSchedule.setId(sid);
 			addSchedule(sid, rollbackID);
 			
 			player.sendMessage(ChatColor.BLUE + "This undo will affect " + ChatColor.GOLD + blockCount + " blocks");
 			player.sendMessage(ChatColor.BLUE + "At a speed of about " + ChatColor.GOLD + Math.round(limit/delay) + " blocks/second");
 			player.sendMessage(ChatColor.BLUE + "It will take about " + ChatColor.GOLD + Math.round(blockCount/(limit/delay)) + " seconds " + ChatColor.BLUE + "to complete the rollback");
 			player.sendMessage(ChatColor.BLUE + "To cancel the undo say " + ChatColor.GOLD + "/bl cancel " + sid);
 			return true;
 		} catch(NumberFormatException e) {
 			return false;
 		} catch(SQLException e) {
 			e.printStackTrace();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
