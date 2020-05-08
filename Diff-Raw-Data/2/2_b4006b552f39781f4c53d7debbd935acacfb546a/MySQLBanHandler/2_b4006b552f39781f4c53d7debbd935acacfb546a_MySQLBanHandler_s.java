 package com.cole2sworld.ColeBans.handlers;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.cole2sworld.ColeBans.Main;
 import com.cole2sworld.ColeBans.framework.MethodNotSupportedException;
 import com.cole2sworld.ColeBans.framework.PlayerAlreadyBannedException;
 import com.cole2sworld.ColeBans.framework.PlayerNotBannedException;
 
import me.PatPeter.SQLibrary.*;
 
 public class MySQLBanHandler extends BanHandler {
 	MySQL sqlHandler;
 	/**
 	 * Creates a new MySQLBanHandler using a database with the given settings
 	 * @param username - The username to log into the server
 	 * @param password - The password to log into the server
 	 * @param host - The hostname to log into the server
 	 * @param port - The port to log into the server
 	 * @param prefix - The table prefix
 	 * @param db - The database to use
 	 */
 	public MySQLBanHandler(String username, String password, String host, String port, String prefix, String db) {
 		System.out.println(Main.logPrefix+"[MySQLBanHandler] Opening connection");
 		long oldtime = System.currentTimeMillis();
 		sqlHandler = new MySQL(Logger.getLogger("Minecraft"), prefix, host, port, db, username, password);
 		sqlHandler.open();
 		long newtime = System.currentTimeMillis();
 		System.out.println(Main.logPrefix+"[MySQLBanHandler] Done. Took "+(newtime-oldtime)+" ms.");
 	}
 	public static String addSlashes(String workset) {
 		StringBuilder sanitizer = new StringBuilder();
 		for (int i = 0; i<workset.length(); i++) {
 			if (!isSQLSpecialCharacter(workset.charAt(i))) {
 				sanitizer.append("\\");
 				sanitizer.append(workset.charAt(i));
 			}
 			else {
 				sanitizer.append(workset.charAt(i));
 			}
 		}
 		return sanitizer.toString();
 	}
 	public static boolean isSQLSpecialCharacter(Character charAt) {
 		String workset = charAt.toString();
 		if ("0".equalsIgnoreCase(workset)) return true;
 		else if ("b".equalsIgnoreCase(workset)) return true;
 		else if ("n".equalsIgnoreCase(workset)) return true;
 		else if ("r".equalsIgnoreCase(workset)) return true;
 		else if ("t".equalsIgnoreCase(workset)) return true;
 		else if ("z".equalsIgnoreCase(workset)) return true;
 		else return false;
 	}
 
 	public void banPlayer(String player, String reason) throws PlayerAlreadyBannedException {
 		if (isPlayerBanned(player)) throw new PlayerAlreadyBannedException(player+" is already banned!");
 		String tbl = Main.sql.prefix+"perm";
 		if (sqlHandler.checkConnection()) {
 			if (sqlHandler.checkTable(tbl)) {
 				sqlHandler.query("INSERT INTO "+tbl+" (" +
 						"username, " +
 						"reason" +
 						") VALUES (" +
 						"'"+addSlashes(player)+"', " +
 						"'"+addSlashes(reason)+"'"+
 						");");
 				Player playerObj = Main.server.getPlayer(player);
 				if (playerObj != null) {
 					playerObj.kickPlayer(ChatColor.valueOf(Main.banColor)+"BANNED: "+reason);
 					if (Main.fancyEffects) {
 						World world = playerObj.getWorld();
 						world.createExplosion(playerObj.getLocation(), 0);
 					}
 				}
 			}
 			else {
 				sqlHandler.query("CREATE  TABLE `"+Main.sql.db+"`.`"+tbl+"` (" +
 						"`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ," +
 						"`username` VARCHAR(255) NULL ," +
 						"`reason` VARCHAR(255) NULL ," +
 						"PRIMARY KEY (`id`) );");
 				sqlHandler.query("ALTER TABLE `"+Main.sql.db+"`.`"+tbl+"`"+
 						"ADD INDEX `NAMEINDEX` (`username` ASC);");
 				banPlayer(player, reason);
 			}
 		}
 	}
 
 	public void tempBanPlayer(String player, long primTime) throws PlayerAlreadyBannedException, MethodNotSupportedException {
 		if (!Main.allowTempBans) throw new MethodNotSupportedException("Temp bans are disabled!");
 		if (isPlayerBanned(player)) throw new PlayerAlreadyBannedException(player+" is already banned!");
 		Long time = System.currentTimeMillis()+((primTime*60)*1000);
 		String tbl = Main.sql.prefix+"temp";
 		if (sqlHandler.checkConnection()) {
 			if (sqlHandler.checkTable(tbl)) {
 				sqlHandler.query("INSERT INTO `"+Main.sql.db+"`.`"+tbl+"` (" +
 						"username, " +
 						"time" +
 						") VALUES (" +
 						"'"+addSlashes(player)+"', " +
 						"'"+time+"'"+
 						");");
 				Player playerObj = Main.server.getPlayer(player);
 				if (playerObj != null) {
 					playerObj.kickPlayer(ChatColor.valueOf(Main.tempBanColor)+"Temporarily banned for "+primTime+" minute"+Main.getPlural(primTime)+".");
 					if (Main.fancyEffects) {
 						World world = playerObj.getWorld();
 						world.createExplosion(playerObj.getLocation(), 0);
 					}
 				}
 			}
 			else {
 				sqlHandler.query("CREATE  TABLE `"+Main.sql.db+"`.`"+tbl+"` (" +
 						"`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ," +
 						"`username` VARCHAR(255) NULL ," +
 						"`time` VARCHAR(255) NULL ," +
 						"PRIMARY KEY (`id`) );");
 				sqlHandler.query("ALTER TABLE `"+Main.sql.db+"`.`"+tbl+"`"+
 						"ADD INDEX `NAMEINDEX` (`username` ASC);");
 				tempBanPlayer(player, primTime);
 			}
 		}
 	}
 
 	public void unbanPlayer(String player) throws PlayerNotBannedException {
 		BanData bd = getBanData(player);
 		if (bd.getType() == Type.PERMANENT)  {
 			String tbl = Main.sql.prefix+"perm";
 			if (sqlHandler.checkConnection()) {
 				if (sqlHandler.checkTable(tbl)) {
 					sqlHandler.query("DELETE FROM `"+Main.sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 					return;
 				}
 			}
 		}
 		else if (bd.getType() == Type.TEMPORARY) {
 			String tbl = Main.sql.prefix+"temp";
 			if (sqlHandler.checkConnection()) {
 				if (sqlHandler.checkTable(tbl)) {
 					sqlHandler.query("DELETE FROM `"+Main.sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 					return;
 				}
 			}
 		}
 		throw new PlayerNotBannedException(player+" is not banned!");
 	}
 
 	public boolean isPlayerBanned(String player) {
 		return (getBanData(player).getType()) != Type.NOT_BANNED;
 	}
 
 
 	@Override
 	public BanData getBanData(String player) {
 		String tbl = Main.sql.prefix+"perm";
 		if (sqlHandler.checkConnection()) {
 			if (sqlHandler.checkTable(tbl)) {
 				ResultSet reasonResult = sqlHandler.query("SELECT reason FROM `"+Main.sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 				boolean results = false;
 				try {
 					results = reasonResult.first();
 				} catch (SQLException e) {}
 				if (results) {
 					String reason = "";
 					try {
 						reason = reasonResult.getString("reason");
 					} catch (SQLException e) {}
 					if (!reason.isEmpty()) {
 						return new BanData(player, reason);
 					}
 				}
 			}
 			String tblB = Main.sql.prefix+"temp";
 			if (sqlHandler.checkTable(tblB)) {
 				ResultSet reasonResultB = sqlHandler.query("SELECT time FROM `"+Main.sql.db+"`.`"+tblB+"` WHERE username='"+addSlashes(player)+"';");
 				boolean resultsB = false;
 				try {
 					resultsB = reasonResultB.first();
 				} catch (SQLException e) {}
 				if (resultsB) {
 					long time = -1L;
 					try {
 						time = reasonResultB.getLong("time");
 						if (time <= System.currentTimeMillis()) {
 							if (sqlHandler.checkConnection()) {
 								if (sqlHandler.checkTable(tblB)) {
 									sqlHandler.query("DELETE FROM `"+Main.sql.db+"`.`"+tbl+"` WHERE username='"+addSlashes(player)+"';");
 								}
 							}
 							return new BanData(player);
 						}
 					}
 					catch (SQLException e) {}
 					if (time > -1) {
 						return new BanData(player, time);
 					}
 				}	
 			}
 		}
 		return new BanData(player);
 	}
 
 	@Override
 	public void onDisable() {
 		System.out.println(Main.logPrefix+"[MySQLBanHandler] Closing connection");
 		long oldtime = System.currentTimeMillis();
 		sqlHandler.close();
 		long newtime = System.currentTimeMillis();
 		System.out.println(Main.logPrefix+"[MySQLBanHandler] Done. Took "+(newtime-oldtime)+" ms.");
 	}
 	@Override
 	public BanHandler onEnable(String username, String password, String host,
 			String port, String prefix, String db, File yaml, File json,
 			String api) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public void convert(BanHandler handler) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 }
