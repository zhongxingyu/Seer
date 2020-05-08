 package darkknightcz.InviteEm;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 public class MySQL {
 	private String host;
 	private String username;
 	private String password;
 	private String database;
 
 	MySQL() throws SQLException {
 		this.host = Settings.host;
 		this.username = Settings.username;
 		this.password = Settings.password;
 		this.database = Settings.database;
 		this.createTable();
 	}
 
 	public synchronized Connection connect() throws SQLException {
 		return DriverManager.getConnection("jdbc:mysql://" + this.host
 				+ ":3306/" + this.database + "?autoReconnect=true&user="
 				+ this.username + "&password=" + this.password);
 	}
 	public synchronized Connection AuthMeConnect() throws SQLException {
 		return DriverManager.getConnection("jdbc:mysql://"
 				+ uk.org.whoami.authme.settings.Settings.getMySQLHost + ":"
 				+ uk.org.whoami.authme.settings.Settings.getMySQLPort + "/"
 				+ uk.org.whoami.authme.settings.Settings.getMySQLDatabase
 				+ "?autoReconnect=true&user="
 				+ uk.org.whoami.authme.settings.Settings.getMySQLUsername
 				+ "&password="
 				+ uk.org.whoami.authme.settings.Settings.getMySQLPassword);
 	}
 
 	public synchronized void disconnect(Connection con) throws SQLException {
 		con.close();
 	}
 
 	public void loadIps() {
 		try {
 			Connection con = this.connect();
 			PreparedStatement pst = con
 					.prepareStatement("SELECT ip FROM inviteem_deniedIps ORDER BY id ASC");
 			ResultSet rs = pst.executeQuery();
 			while (rs.next()) {
 				Settings.deniedIps.add(rs.getString("ip"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized boolean isRegistered(String user) {
 		Connection con = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		try {
 			con = this.AuthMeConnect();
 			pst = con.prepareStatement("SELECT id FROM "
 					+ uk.org.whoami.authme.settings.Settings.getMySQLTablename
 					+ " WHERE "
 					+ uk.org.whoami.authme.settings.Settings.getMySQLColumnName
 					+ "=?;");
 
 			pst.setString(1, user.toLowerCase());
 			rs = pst.executeQuery();
 			boolean bool = rs.next();
 			rs.close();
 			pst.close();
 			disconnect(con);
 
 			return bool;
 		} catch (Exception ex) {
 			return false;
 		}
 	}
 
 	private void createTable() throws SQLException {
 		Connection con = this.connect();
 		/* invitation */
 		PreparedStatement pst = con
 				.prepareStatement("CREATE TABLE IF NOT EXISTS `inviteem` (`id` INT NOT NULL AUTO_INCREMENT, `nick` VARCHAR(30) NOT NULL, `ref` VARCHAR(30) NOT NULL, `rewarded` INT, `url` TEXT NULL, `ip` VARCHAR(40) NOT NULL, PRIMARY KEY (`id`), UNIQUE (`nick`)) ENGINE = InnoDB;");
 		pst.execute();
 		pst.close();
 
 		/* users */
 		pst = con
 				.prepareStatement("CREATE TABLE IF NOT EXISTS `inviteem_users` ( `id` int(11) NOT NULL AUTO_INCREMENT,  `nick` VARCHAR(30) NOT NULL,  `invitations` int(11) NOT NULL,  `invitations_offset` int(11) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `nick` (`nick`)) ENGINE=InnoDB;");
 		pst.execute();
 		pst.close();
 
 		/* warnings */
 		pst = con
 				.prepareStatement("CREATE TABLE IF NOT EXISTS `inviteem_warnings` (`id` INT NOT NULL AUTO_INCREMENT, `nick` VARCHAR(30) NOT NULL, `banned_nick` VARCHAR(30) NULL, `message` VARCHAR(255) NULL, `for_ops` SMALLINT(1) NOT NULL DEFAULT '0', `received` SMALLINT(1) NOT NULL DEFAULT '0', PRIMARY KEY (`id`), UNIQUE (`banned_nick`)) ENGINE = InnoDB;");
 		pst.execute();
 		pst.close();
 
 		/* denied Ips */
 		pst = con
 				.prepareStatement("CREATE TABLE IF NOT EXISTS `inviteem_deniedIps` ( `id` int(11) NOT NULL AUTO_INCREMENT,  `ip` VARCHAR(40) NOT NULL, PRIMARY KEY (`id`),  UNIQUE KEY `ip` (`ip`)) ENGINE=InnoDB;");
 		pst.execute();
 		pst.close();
 
 		disconnect(con);
 	}
 
 	public synchronized boolean isInvited(String user) {
 		Connection con = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		try {
 			con = this.connect();
 			pst = con
 					.prepareStatement("SELECT id FROM `inviteem` WHERE nick =?;");
 			pst.setString(1, user.toLowerCase());
 			rs = pst.executeQuery();
 			boolean bool = rs.next();
 			rs.close();
 			pst.close();
 			disconnect(con);
 			return bool;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public synchronized boolean invite(String user, String sender) {
 		if (this.canInvite(sender)) {
 			if (this.isInvited(user)) {
 				Bukkit.getServer()
 						.getPlayer(sender)
 						.sendMessage(
 								ChatColor.YELLOW
 										+ Settings.alreadyInvited.replaceAll(
 												"USER", user));
 				return false;
 			}
 			if (this.isRegistered(user)) {
 				Bukkit.getServer()
 						.getPlayer(sender)
 						.sendMessage(
 								ChatColor.YELLOW
 										+ Settings.alreadyRegistered
 												.replaceAll("USER", user));
 				return false;
 			}
 
 			try {
 				Connection con = this.connect();
 
 				/* INSERT INVITATION */
 				PreparedStatement pst = con
 						.prepareStatement("INSERT INTO `inviteem` (`id`, `nick`, `ref`, `rewarded`, `url`, `ip`) VALUES (NULL, ?, ?,0, NULL, ?);");
 				pst.setString(1, user);
 				pst.setString(2, sender);
 				pst.setString(3,
 						Bukkit.getServer().getPlayer(sender.toLowerCase())
 								.getAddress().getAddress().getHostAddress());
 				pst.execute();
 				pst.close();
 
 				/* UPDATE PLAYER STATUS */
 				pst = con
 						.prepareStatement("UPDATE  `inviteem_users` SET  `invitations` =  `invitations`+1 WHERE  `inviteem_users`.`nick` =?;");
 				pst.setString(1, sender);
 				pst.execute();
 				pst.close();
 
 				/* CLEANING */
 				disconnect(con);
 				Bukkit.getServer()
 						.getPlayer(sender)
 						.sendMessage(
 								ChatColor.GREEN
 										+ Settings.successfullyInvited
 												.replaceAll("USER", user));
 				return true;
 			} catch (Exception e) {
 				e.printStackTrace();
 				Bukkit.getServer()
 						.getPlayer(sender)
 						.sendMessage(
 								ChatColor.RED + Settings.somethingWentWrong);
 				return false;
 			}
 
 		} else {
 			Bukkit.getServer().getPlayer(sender)
 					.sendMessage(ChatColor.RED + Settings.cannotInvite);
 			return false;
 		}
 
 	}
 
 	public synchronized void createPlayerStructure(String sender) {
 		try {
 			Connection con = this.connect();
 
 			PreparedStatement pst = con
 					.prepareStatement("INSERT INTO `inviteem_users` (`id`, `nick`, `invitations`, `invitations_offset`) VALUES (NULL, ?, '0', '0');");
 			pst.setString(1, sender.toLowerCase());
 			pst.execute();
 
 		} catch (Exception e) {
 			return;
 		}
 	}
 
 	private synchronized boolean canInvite(String sender) {
 		Connection con = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 
 		try {
 			con = this.connect();
 			pst = con
 					.prepareStatement("SELECT invitations FROM `inviteem_users` WHERE nick =?");
 			pst.setString(1, sender.toLowerCase());
 			rs = pst.executeQuery();
 			rs.next();
 			Integer number = rs.getInt("invitations");
 			rs.close();
 			pst.close();
 			disconnect(con);
 			if (number < Settings.MaxInvitations) {
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 
 	public int getOffset(String nick) throws Exception {
 		Connection con = this.connect();
 		PreparedStatement pst = con
 				.prepareStatement("SELECT invitations_offset FROM `inviteem_users` WHERE nick=?");
 		ResultSet rs = pst.executeQuery();
 		rs.next();
 		return rs.getInt("invitations_offset");
 	}
 
 	public void setOffset(String nick, int offset) throws SQLException {
 		Connection con = this.connect();
 		PreparedStatement pst = con
 				.prepareStatement("UPDATE  `inviteem_users` SET  `invitations_offset` = ? WHERE  `inviteem_users`.`nick` =?;");
 		pst.setInt(1, offset);
 		pst.setString(2, nick);
 		pst.execute();
 	}
 
 	public List<String> loadRewards(Player player) {
 		try {
 			List<String> list = new ArrayList<String>();
 			Connection con = this.connect();
 			PreparedStatement pst = con
 					.prepareStatement("SELECT nick,ip FROM `inviteem` WHERE rewarded = 0 AND ref = ?");
 			pst.setString(1, player.getName().toLowerCase());
 			ResultSet rs = pst.executeQuery();
 			while (rs.next()) {
 				if (rs.getString("ip").equals(
 						player.getAddress().getAddress().getHostAddress())) {
 					this.setRewarded(rs.getString("nick"));
 					player.sendMessage(ChatColor.RED
 							+ Settings.rewardCanceled.replaceAll("PLAYER",
 									rs.getString("nick")).replaceAll("REASON",
 									Settings.ipConflict));
 				} else {
 					list.add(rs.getString("nick"));
 				}
 			}
 			return list;
 		} catch (SQLException e) {
 			return null;
 		}
 	}
 
 	public void setRewarded(String nick) {
 		try {
 			Connection con = this.connect();
 			PreparedStatement pst = con
 					.prepareStatement("UPDATE `inviteem` SET `rewarded` = 1 WHERE `nick`=?;");
 			pst.setString(1, nick);
 			pst.execute();
 		} catch (SQLException e) {
 
 		}
 
 	}
 
 	public String getInviter(String nick) {
 		try {
 			Connection con = this.connect();
 			PreparedStatement pst = con
 					.prepareStatement("SELECT ref FROM `inviteem` WHERE `nick` =?");
 			pst.setString(1, nick);
 			ResultSet rs = pst.executeQuery();
 			rs.next();
 			return rs.getString("ref");
 		} catch (SQLException e) {
 			return "";
 		}
 	}
 
 	public String warnBan(String nick) {
 		String ref = getInviter(nick);
 		if (ref != null) {
 			try {
 				Connection con = this.connect();
 				PreparedStatement pst = con
 						.prepareStatement("INSERT INTO `inviteem_warnings` (`id`, `nick`, `banned_nick`, `message`, `for_ops`, `received`) VALUES (NULL, ?, ?, NULL, ?, 0);");
 				pst.setString(1, ref);
 				pst.setString(2, nick);
 				pst.setInt(3, (Settings.banOverride.contains(ref) ? 1 : 0)); // ban
 																				// override
 				pst.executeQuery();
 				
 				return ref;
 
 			} catch (SQLException e) {
 				for (OfflinePlayer op : Bukkit.getServer().getOperators()) {
 					if (op.isOnline()) {
 						op.getPlayer()
 								.sendMessage(
 										ChatColor.RED
 												+ "[InviteEm] Something happened, player is already banned or there is error in the database!");
 					}
 				}
 			}
 		}
 
 		return "";
 	}
 	
 	public int warnAdmin(String nick,String msg) {		
 			try {
 				Connection con = this.connect();
 				PreparedStatement pst = con
 						.prepareStatement("INSERT INTO `inviteem_warnings` (`id`, `nick`, `banned_nick`, `message`, `for_ops`, `received`) VALUES (NULL, ?, NULL, ?, 0, 0);");
 				pst.setString(1, nick);
 				pst.setString(2, msg);
 				return pst.executeUpdate();
 
 			} catch (SQLException e) {
 				for (OfflinePlayer op : Bukkit.getServer().getOperators()) {
 					if (op.isOnline()) {
 						op.getPlayer()
 								.sendMessage(
 										ChatColor.RED
 												+ "[InviteEm] Something happened, there is probably error in the database!");
 					}
 				}
 			}		
 
 			return 0;
 	}
 
 	public void setWarned(int id) {
 		try{
 			Connection con = this.connect();
 			PreparedStatement pst = con.prepareStatement("UPDATE  `inviteem_warnings` SET  `received` = 1 WHERE  `inviteem_warnings`.`id` =?;");
 			pst.setInt(1, id);
 			pst.executeUpdate();
 			pst.close();
 			
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void setWarned(String banned_player) {
 		try{
 			Connection con = this.connect();
			PreparedStatement pst = con.prepareStatement("UPDATE  `inviteem_warnings` SET  `received` = 1 WHERE  `inviteem_warnings`.`banned_nick` =?;");
 			pst.setString(1, banned_player);
 			pst.executeUpdate();
 			pst.close();
 			
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 		
 	}
 
 }
