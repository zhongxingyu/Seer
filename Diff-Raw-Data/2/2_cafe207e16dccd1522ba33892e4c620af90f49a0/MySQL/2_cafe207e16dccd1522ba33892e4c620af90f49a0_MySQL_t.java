 package org.monstercraft.support.plugin.util;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.monstercraft.support.plugin.Configuration.Variables;
 import org.monstercraft.support.plugin.wrappers.HelpTicket;
 
 public class MySQL {
 
 	Connection connection;
 
 	public MySQL() throws SQLException, ClassNotFoundException {
 		Class.forName("com.mysql.jdbc.Driver");
 		connection = DriverManager.getConnection("jdbc:mysql://"
 				+ Variables.db_host + ":3306/" + Variables.db_name
 				+ "?autoReconnect=true&user=" + Variables.db_username
 				+ "&password=" + Variables.db_password);
 		final ResultSet tableExists = connection.getMetaData().getTables(null,
 				null, "tickets", null);
 		if (!tableExists.first()) {
 			String tableCreation = "CREATE TABLE IF NOT EXISTS `tickets` ("
 					+ "  `id` int(11) NOT NULL,"
 					+ "  `player` varchar(16) NOT NULL,"
 					+ "  `description` text NOT NULL,"
 					+ "  `helper` varchar(16) DEFAULT NULL,"
 					+ "  `status` tinyint(1) NOT NULL,"
 					+ "  `x` int(11) NOT NULL," + "  `y` int(11) NOT NULL,"
 					+ "  `z` int(11) NOT NULL,"
 					+ "  `world` varchar(16) NOT NULL,"
 					+ "  PRIMARY KEY (`id`)"
 					+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
 			this.connection.createStatement().executeUpdate(tableCreation);
 		}
 	}
 
 	public ArrayList<HelpTicket> readTickets(int Status) throws SQLException {
 		PreparedStatement ps = connection
				.prepareStatement("SELECT * FROM `tickets` WHERE status=?");
 		ps.setInt(1, Status);
 		ResultSet rs = ps.executeQuery();
 		ArrayList<HelpTicket> tickets = new ArrayList<HelpTicket>();
 		while (rs.next()) {
 			String noob = rs.getString("player");
 			String description = rs.getString("description");
 			int id = rs.getInt("id");
 			int x = rs.getInt("x");
 			int y = rs.getInt("y");
 			int z = rs.getInt("z");
 			String world = rs.getString("world");
 			String mod = rs.getString("helper");
 			int status = rs.getInt("status");
 			HelpTicket h = new HelpTicket(id, description, noob, x, y, z, world);
 			if (status == 3 && mod != null) {
 				h.Claim(mod);
 				h.close();
 			}
 			tickets.add(h);
 		}
 		return tickets;
 	}
 
 	public int readLastRowID() throws SQLException {
 		PreparedStatement ps = connection
 				.prepareStatement("SELECT * FROM `tickets` ORDER BY `id` DESC LIMIT 1");
 		ResultSet rs = ps.executeQuery();
 		while (rs.next()) {
 			return rs.getInt("id");
 		}
 		return 0;
 
 	}
 
 	public void createTicket(HelpTicket ticket) throws SQLException {
 		PreparedStatement ps = this.connection
 				.prepareStatement("INSERT INTO `tickets` (id, player, description, status, x, y, z, world) VALUES (?,?,?,?,?,?,?,?)");
 		ps.setInt(1, ticket.getID());
 		ps.setString(2, ticket.getNoobName());
 		ps.setString(3, ticket.getDescription());
 		ps.setInt(4, ticket.getStatus().toInt());
 		ps.setInt(5, ticket.getX());
 		ps.setInt(6, ticket.getY());
 		ps.setInt(7, ticket.getZ());
 		ps.setString(8, ticket.getWorldName());
 		ps.executeUpdate();
 	}
 
 	public void claimTicket(HelpTicket ticket) throws SQLException {
 		PreparedStatement ps = this.connection
 				.prepareStatement("UPDATE `tickets` SET helper=?,status=? WHERE id=?");
 		ps.setString(1, ticket.getModName());
 		ps.setInt(2, ticket.getStatus().toInt());
 		ps.setInt(3, ticket.getID());
 		ps.executeUpdate();
 	}
 
 	public void closeTicket(HelpTicket ticket) throws SQLException {
 		PreparedStatement ps = this.connection
 				.prepareStatement("UPDATE `tickets` SET status=? WHERE id=?");
 		ps.setInt(1, ticket.getStatus().toInt());
 		ps.setInt(2, ticket.getID());
 		ps.executeUpdate();
 	}
 }
