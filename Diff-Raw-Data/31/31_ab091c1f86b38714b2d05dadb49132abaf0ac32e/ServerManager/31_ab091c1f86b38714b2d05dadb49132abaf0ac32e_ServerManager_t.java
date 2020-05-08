 package net.kiwz.ThePlugin.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Calendar;
 
 import net.kiwz.ThePlugin.ThePlugin;
 import net.kiwz.ThePlugin.mysql.SqlConnection;
 import net.kiwz.ThePlugin.mysql.SqlQuery;
 
 import org.apache.logging.log4j.Level;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Marker;
 import org.apache.logging.log4j.core.Filter;
 import org.apache.logging.log4j.core.LogEvent;
 import org.apache.logging.log4j.core.Logger;
 import org.apache.logging.log4j.message.Message;
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.fusesource.jansi.Ansi;
 
 public class ServerManager {
 	private static boolean warning = false;
 	
 	public static void start() {
 		ServerManager sm = new ServerManager();
 		sm.unban();
 	    sm.save();
 	    sm.autoStop();
 	    sm.setFilter();
 	}
 	
 	private void unban() {
 		Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(ThePlugin.getPlugin(), new Runnable() {
 			public void run() {
 				for (MyPlayer myPlayer : MyPlayer.getPlayers()) {
 					if (myPlayer.isBanned() && myPlayer.getBanExpire() < System.currentTimeMillis() / 1000) {
 						myPlayer.setBanned(false, 0, "", "");
 					}
 				}
 			}
 		}, 20, 200);
 	}
 	
 	private void save() {
 		Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(ThePlugin.getPlugin(), new Runnable() {
 			@Override
             public void run() {
 				/**
 				 * TODO fjerne logging av tidsbruk
 				 */
				long time = System.currentTimeMillis();
 				// Behold linjer herfra og ned til neste //
 				Connection conn = new SqlConnection().getConnection();
 				if (conn == null) {
 					Bukkit.getServer().getScheduler().runTaskLater(ThePlugin.getPlugin(), new Runnable() {
 						public void run() {
 							ThePlugin.getPlugin().getLogger().severe(Ansi.ansi().fg(Ansi.Color.RED) + "Ingen database funnet, restarter serveren!"
 									+ Ansi.ansi().fg(Ansi.Color.DEFAULT));
 							ThePlugin.error = true;
 							for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 								player.kickPlayer(Color.WARNING + "Det har desverre skjedd noe feil med serveren og den vil restarte!");
 							}
 							Bukkit.getServer().shutdown();
 						}
 					}, 20);
 					return;
 				} else {
 					SqlQuery query = new SqlQuery(conn);
 					for (MyWorld myWorld : MyWorld.getWorlds()) query.updateWorld(myWorld);
 					for (MyPlayer myPlayer : MyPlayer.getPlayers()) query.updatePlayer(myPlayer);
 					for (Place place : Place.getPlaces()) query.updatePlace(place);
 					for (Home home : Home.getHomes()) query.updateHome(home);
 					for (WoolChest woolChest : WoolChest.getWoolChests()) query.updateWoolChest(woolChest);
 					try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
 				}
 				// Det er linjer over her som skal beholdes!
				time = (System.currentTimeMillis() - time);
				int players = Bukkit.getServer().getOnlinePlayers().length;
				if (time > 30) {
					try {
						File file = new File("db_save.log");
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
						bw.write(Util.getTimeLogDate(System.currentTimeMillis() / 1000) + " " + time + "ms (" + players + ")");
						bw.write("\n");
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
 				}
 			}
 		}, 200, 200);
 		
 		Bukkit.getServer().getScheduler().runTaskTimer(ThePlugin.getPlugin(), new Runnable() {
 			@Override
             public void run() {
 				long time = System.currentTimeMillis();
 				Bukkit.getServer().savePlayers();
 				for (World world : Bukkit.getWorlds()) {
 					world.save();
 				}
 				time = System.currentTimeMillis() - time;
 				broadcastMsg(Color.SERVER + "Lagring fullfrt (" + time + "ms)");
 			}
 		}, 12000, 12000);
 	}
 	
 	private void autoStop() {
 		Bukkit.getServer().getScheduler().runTaskTimer(ThePlugin.getPlugin(), new Runnable() {
             public void run() {
 				int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 				int min = Calendar.getInstance().get(Calendar.MINUTE);
 				int sec = Calendar.getInstance().get(Calendar.SECOND);
 				if ((hour == 01 || hour == 13 || hour == 17) && (min == 55 && sec == 00 && !ServerManager.warning)) {
 					ServerManager.warning = true;
 					broadcastMsg(Color.SERVER + "*** Server restarter om 5 minutter ***");
 				}
 				if ((hour == 01 || hour == 13 || hour == 17) && (min == 59 && sec == 57 && ServerManager.warning)) {
 					broadcastMsg(Color.SERVER + "*** Server restarter ***");
 				}
 				if ((hour == 02 || hour == 14 || hour == 18) && (min == 00 && sec == 00 && ServerManager.warning)) {
 					for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 						player.kickPlayer("Serveren restarter, kom tilbake om 1 minutt");
 					}
 					Bukkit.shutdown();
 				}
 			}
 		}, 28800, 10);
 	}
 	
 	public static void logString(String string) {
 		int unix = (int) (System.currentTimeMillis() / 1000);
 		try {
 			File file = new File("player.log");
 			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
 			bw.write(Util.getTimeLogDate(unix) + " " + string);
 			bw.write("\n");
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void setFilter() {
 		org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
 		coreLogger.addFilter(new Filter() {
 			@Override
 			public Result filter(LogEvent event) {
 				for (String filter : Config.getConfig().getFilters()) {
 					if (event.getMessage().toString().contains(filter)) {
 						return Result.DENY;
 					}
 				}
 				return null;
 			}
 
 			@Override
 			public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object... arg4) {
 				return null;
 			}
 
 			@Override
 			public Result filter(Logger arg0, Level arg1, Marker arg2, Object arg3, Throwable arg4) {
 				return null;
 			}
 
 			@Override
 			public Result filter(Logger arg0, Level arg1, Marker arg2, Message arg3, Throwable arg4) {
 				return null;
 			}
 
 			@Override
 			public Result getOnMatch() {
 				return null;
 			}
 
 			@Override
 			public Result getOnMismatch() {
 				return null;
 			}
 		});
 	}
     
     private void broadcastMsg(String string) {
     	for (Player player : Bukkit.getServer().getOnlinePlayers()) {
     		player.sendMessage(string);
     	}
     }
 }
