 package com.github.dreadslicer.tekkitrestrict;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Filter;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import org.bukkit.entity.Player;
 
 public class TRLogFilter implements Filter {
 	private static List<String> toFilter = new LinkedList<String>();
 	private static Boolean logConsole;
 
 	public static void reload() {
 		toFilter = tekkitrestrict.config.getStringList("LogFilter");
 		logConsole = tekkitrestrict.config.getBoolean("LogConsole");
 		/*
 		 * for(int i=0;i<toFilter.size();i++){
 		 * tekkitrestrict.log.info(toFilter.get(i)); }
 		 */
 	}
 
 	@Override
 	public boolean isLoggable(LogRecord record) {
 
 		if (logConsole) {
 			// okay, so here we will split up the server.log
 
 			// Login ---- logged in with
 			// Logout --- lost connection
 			// Warning -- WARNING
 			// Error ---- SEVERE
 			// Chat ----- PlayerName
 			// Command -- PLAYER_COMMAND
 			if (record.getMessage() != null) {
 				Player[] pl = tekkitrestrict.getInstance().getServer()
 						.getOnlinePlayers();
 				String a = record.getMessage();
 				String b = record.getMessage().toLowerCase();
 				boolean lc = false;
 				for (int i = 0; i < pl.length; i++) {
 					if (b.contains(pl[i].getName().toLowerCase()
 							+ " lost connection")) {
 						TRLogger.LogConsole("Login", a);
 						lc = true;
 					}
 				}
 
 				if (lc) {
 				} else if (b.contains("logged in with")) {
 					TRLogger.LogConsole("Login", a);
 				} else if (record.getLevel().equals(Level.WARNING)) {
 					TRLogger.LogConsole("Warning", a);
 				} else if (record.getLevel().equals(Level.SEVERE)) {
 					TRLogger.LogConsole("Error", a);
 				} else if (b.contains("player_command")) {
 					TRLogger.LogConsole("Command", a);
 				} else {
 					boolean cc = false;
 
 					for (int i = 0; i < pl.length; i++) {
 						String chatline = a;
 						if (b.contains("sending serverside check to")) {
 						} else if (b.contains(pl[i].getName().toLowerCase())
 								|| b.contains(pl[i].getDisplayName()
 										.toLowerCase())
 								|| b.contains(pl[i].getPlayerListName()
 										.toLowerCase())) {
							if (b.contains("[34;1mGiving")) {
								TRLogger.LogConsole("GiveItem", chatline);
 								cc = true;
 							} else {
 								TRLogger.LogConsole("Chat", chatline);
 								cc = true;
 							}
 						}
 					}
 					if (!cc) {
 						TRLogger.LogConsole("Info", a);
 					}
 				}
 			}
 		}
 
 		if (record.getMessage() != null) {
 			String a = record.getMessage();
 			boolean c = false;
 			for (int i = 0; i < toFilter.size(); i++) {
 				String filtera = toFilter.get(i).toLowerCase();
 				if (record.getLevel().getName().toLowerCase().equals(filtera)) {
 					c = true;
 				} else if (a.toLowerCase().contains(filtera)) {
 					c = true;
 				}
 			}
 			return !c;
 		}
 
 		return true;
 	}
 
 }
