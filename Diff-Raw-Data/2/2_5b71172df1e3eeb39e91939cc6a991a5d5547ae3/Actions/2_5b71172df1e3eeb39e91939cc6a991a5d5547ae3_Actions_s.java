 package com.cole2sworld.ColeBans.commands;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.cole2sworld.ColeBans.LogEntry;
 import com.cole2sworld.ColeBans.LogManager;
 import com.cole2sworld.ColeBans.LogManager.Type;
 import com.cole2sworld.ColeBans.framework.PermissionSet;
 /**
  * The actions command. Handles getting action logs.
  * @author cole2
  *
  */
 public final class Actions implements CBCommand {
 
 	public enum Unit {
 		MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS, YEARS, TIMESTAMP;
 		public long getMultiplier() {
 			if (this == MILLISECONDS) return 1;
 			if (this == SECONDS) return millisPerSecond;
 			if (this == MINUTES) return millisPerMinute;
 			if (this == HOURS) return millisPerHour;
 			if (this == DAYS) return millisPerDay;
 			if (this == YEARS) return millisPerYear;
 			return 1;
 		}
 	}
 	public static final long millisPerSecond = 1000L;
 	public static final long millisPerMinute = millisPerSecond*60;
 	public static final long millisPerHour = millisPerMinute*60;
 	public static final long millisPerDay = millisPerHour*24;
 	public static final long millisPerYear = millisPerDay*365;
 	
 	@Override
 	public String run(String[] args, CommandSender admin) throws Exception {
 		if (!(new PermissionSet(admin).canLog)) return ChatColor.RED+"You don't have permission to do that.";
 		if (args.length < 2) return ChatColor.RED+"Not enough arguments.";
 		String by = null;
 		String to = null;
 		long units = -1;
 		Unit unit = Unit.MINUTES;
 		long since = -1;
 		long curTime = System.currentTimeMillis();
 		for (int i = 0; i < args.length; i++) {
 			try {
 				if (args[i].equalsIgnoreCase("by")) by = args[i+1];
 			} catch (ArrayIndexOutOfBoundsException e) {
 				return ChatColor.RED+"Not enough arguments for 'by'";
 			}
 			try {
 				if (args[i].equalsIgnoreCase("on") || args[i].equalsIgnoreCase("to")) to = args[i+1];
 			} catch (ArrayIndexOutOfBoundsException e) {
 				return ChatColor.RED+"Not enough arguments for 'to' or 'on'";
 			}
 			try {
 				if (args[i].equalsIgnoreCase("since")) {
 					units = Long.parseLong(args[i+1]);
 					unit = Unit.valueOf(args[i+2]);
 				}
 			} catch (NumberFormatException e) {
 				return ChatColor.RED+"Expected number, got String (or number too large)";
 			} catch (IllegalArgumentException e) {
 				return ChatColor.RED+"Invalid unit for 'since'. Valid units are Milliseconds, Seconds, Minutes, Hours, Days, Years, Timestamp";
 			} catch (ArrayIndexOutOfBoundsException e) {
 				return ChatColor.RED+"Not enough arguments for 'since'";
 			}
 		}
 		if (units > -1) {
 			since = (curTime)-(units*unit.getMultiplier());
 		} else {
 			since = 0;
 		}
 		if (by == null && to == null) {
 			return ChatColor.RED+"You must specify by and/or to.";
 		}
 		if (by == null) {
 			by = "*";
 		}
 		if (to == null) {
 			to = "*";
 		}
		SimpleDateFormat formatter = new SimpleDateFormat("M d, y hh:mm");
 		admin.sendMessage(ChatColor.AQUA+"Retrieving all records for actions made on "+ChatColor.ITALIC+to+ChatColor.AQUA+" by "+ChatColor.ITALIC+by+ChatColor.AQUA+" since "+formatter.format(new Date(since))+"...");
 		List<LogEntry> entries = LogManager.getTo(args[1]);
 		for (LogEntry entry : entries) {
 			if (entry.getType() == Type.BAN) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.DARK_RED+entry.getAdmin()+" banned "+entry.getVictim());
 			} else if (entry.getType() == Type.KICK) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.YELLOW+entry.getAdmin()+" kicked "+entry.getVictim());
 			} else if (entry.getType() == Type.TEMPBAN) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.RED+entry.getAdmin()+" tempbanned "+entry.getVictim());
 			} else if (entry.getType() == Type.LOCAL_BAN) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.GRAY+entry.getAdmin()+" local banned "+entry.getVictim());
 			} else if (entry.getType() == Type.UNBAN) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.GREEN+entry.getAdmin()+" unbanned "+entry.getVictim());
 			} else if (entry.getType() == Type.SWITCH) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.BLUE+entry.getAdmin()+" switched ban handlers to "+entry.getVictim());
 			} else if (entry.getType() == Type.UNKNOWN) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.DARK_PURPLE+entry.getAdmin()+" did an unknown action on "+entry.getVictim());
 			}  else if (entry.getType() == Type.OTHER) {
 				admin.sendMessage("["+ChatColor.ITALIC+formatter.format(entry.getTime())+ChatColor.RESET+"] "+ChatColor.AQUA+entry.getAdmin()+" did something to "+entry.getVictim());
 			}
 		}
 		admin.sendMessage(ChatColor.GREEN+"Report complete.");
 		return null;
 	}
 
 }
