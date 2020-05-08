 package eu.icecraft.iceauth;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.alta189.sqlLibrary.MySQL.mysqlCore;
 import com.iConomy.iConomy;
 import com.iConomy.system.Holdings;
 
 public class Referrals {
 
 	public IceAuth plugin;
 	public mysqlCore db;
 
 	public Referrals(IceAuth parent) {
 		this.plugin = parent;
 		this.db = parent.manageMySQL;
 	}
 
 	public void onJoinNonReg(Player p) {
 		String referredBy = isReferred(p, false);
 		if(referredBy != null) p.sendMessage(ChatColor.GOLD + "You have been referred by " +  ChatColor.DARK_AQUA + referredBy);
 	}
 
 	public void onRegister(Player p) {
 		String referredBy = isReferred(p, true);
 		if(referredBy != null) {
 			p.sendMessage(ChatColor.YELLOW + "Play for 4 hours to get your referral rewards!");
 		}
 	}
 
 	public void refLinkCmd(Player p) {
 		String playerName = p.getName();
 		String refLink = getRefLink(playerName);
 		if(refLink == null) {
 			refLink = insertRefLink(playerName, p.getAddress().getAddress().getHostAddress());
 		}
 		p.sendMessage(ChatColor.DARK_AQUA + "Your referral link is: " + ChatColor.GOLD + "http://icecraft-mc.eu/?r=" + refLink);
 	}
 
 	public void rewardPlayer(Player player, String referredBy) {
 		try {
 			grantPlayer(player.getName(), 50);
 			plugin.giveKits(player, plugin.referralKit, true); // TODO: possibly give players who registered after 24+ hours only money
 
 			player.sendMessage(ChatColor.GOLD + "You've received $50 and items.");
 			player.sendMessage(ChatColor.GOLD + "For being referred by " + ChatColor.DARK_AQUA + referredBy);
 
 			grantPlayer(referredBy, 100);
 			Player referer = Bukkit.getServer().getPlayerExact(referredBy);
 			if(referer != null) {
 				referer.sendMessage(ChatColor.GOLD + "You have received $100 for referring " + ChatColor.DARK_AQUA + player.getName());
 			}
 		} catch(NoClassDefFoundError err) {
 			System.out.println("[IceAuth Referrals] iConomy not found.");
 		}
 	}
 
 	public String getRefLink(String playerName) {
 		Connection connection = null;
 		String refLink = null;
 		try {
 			connection = this.db.getConnection();
 
 			IceAuth.startTiming();
 			PreparedStatement regQ = connection.prepareStatement("SELECT link FROM reflinks WHERE ign = ?");
 			regQ.setString(1, playerName);
 			ResultSet result = regQ.executeQuery();
 			IceAuth.sqlQueryTime += IceAuth.stopTiming();
 			IceAuth.sqlQueries++;
 
 			while(result.next()) {
 				refLink = result.getString("link");
 			}
 
 			return refLink;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public String insertRefLink(String playerName, String ip) {
 		Connection connection = null;
 		String refLink = null;
 		try {
 			connection = this.db.getConnection();
 
 			IceAuth.startTiming();
 			PreparedStatement regQ = connection.prepareStatement("INSERT INTO reflinks (ign, date, ip) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 			regQ.setString(1, playerName);
 			regQ.setInt(2, (int) (System.currentTimeMillis() / 1000));
 			regQ.setString(3, ip);
 			regQ.executeUpdate();
 			IceAuth.sqlQueryTime += IceAuth.stopTiming();
 			IceAuth.sqlQueries++;
 
 			int insertId = 0;
 			ResultSet generatedKeys = regQ.getGeneratedKeys();
 			if (generatedKeys.next()) {
 				insertId = (int) generatedKeys.getLong(1);
 				refLink = generateUrlbyID(insertId);
 			}
 
 			IceAuth.startTiming();
 			PreparedStatement regQupd = connection.prepareStatement("UPDATE reflinks SET link = ? WHERE id = ?");
 			regQupd.setString(1, refLink);
 			regQupd.setInt(2, insertId);
 			regQupd.executeUpdate();
 			IceAuth.sqlQueryTime += IceAuth.stopTiming();
 			IceAuth.sqlQueries++;
 
 			return refLink;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public String isReferred(Player player, boolean markReferred) {
 		ResultSet result = null;
 		Connection connection = null;
 
 		try {
 			connection = this.db.getConnection();
 			IceAuth.startTiming();
 			PreparedStatement regQ = connection.prepareStatement("SELECT COUNT(*) AS c, referred_by, id FROM referrals WHERE ip = ? AND hasjoined = 0 GROUP BY id ORDER BY id DESC LIMIT 1");
 			regQ.setString(1, player.getAddress().getAddress().getHostAddress());
 			result = regQ.executeQuery();
 			IceAuth.sqlQueryTime += IceAuth.stopTiming();
 			IceAuth.sqlQueries++;
 
 			while(result.next()) {
 				if(result.getInt("c") > 0) {
 					if(markReferred) {
 						IceAuth.startTiming();
 						PreparedStatement regQupd1 = connection.prepareStatement("UPDATE referrals SET hasjoined = 1, ign = ? WHERE id = ?");
 						regQupd1.setString(1, player.getName());
 						regQupd1.setInt(2, result.getInt("id"));
 						regQupd1.executeUpdate();
 						IceAuth.sqlQueryTime += IceAuth.stopTiming();
 						IceAuth.sqlQueries++;
 					}
 					return result.getString("referred_by");
 				}
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		return null;
 	}
 
 	public int getReferredPlayers(Player player) {
 		Connection connection = null;
 		try {
 			connection = this.db.getConnection();
 
 			IceAuth.startTiming();
 			PreparedStatement regQ = connection.prepareStatement("SELECT COUNT(*) AS c FROM referrals WHERE ign = ? AND hasplayed = 1");
 			regQ.setString(1, player.getName());
 			ResultSet result = regQ.executeQuery();
 			IceAuth.sqlQueryTime += IceAuth.stopTiming();
 			IceAuth.sqlQueries++;
 
 			while(result.next()) {
 				return result.getInt("c");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	public static String generateUrlbyID(int in) {		
		String index = "abcdefghijklmnopqrstuvwxyz123456789";
 		int base = index.length();
 		StringBuilder out = new StringBuilder(base);
 
 		for (int t = (int) Math.floor(Math.log(in) / Math.log(base)); t >= 0; t--) {
 			int bcp = (int) Math.pow(base, t);
 			int a = (int) (Math.floor(in / bcp) % base);
 			out.append(index.substring(a, a + 1));
 			in -= (a * bcp);
 		}
 		return out.toString();
 	}
 
 	public static boolean grantPlayer(String player, double amount) {
 		Holdings balance = iConomy.getAccount(player).getHoldings();
 
 		balance.add(amount);
 		return true;
 	}
 }
