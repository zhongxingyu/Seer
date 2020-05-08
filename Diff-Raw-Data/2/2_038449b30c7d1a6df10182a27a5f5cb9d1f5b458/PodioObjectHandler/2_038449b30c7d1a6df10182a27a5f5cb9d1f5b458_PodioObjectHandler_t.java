 package kea.kme.pullpit.server.persistence;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import kea.kme.pullpit.server.podio.PodioAgent;
 import kea.kme.pullpit.server.podio.PodioBand;
 import kea.kme.pullpit.server.podio.PodioBooker;
 import kea.kme.pullpit.server.podio.PodioContact;
 import kea.kme.pullpit.server.podio.PodioShow;
 import kea.kme.pullpit.server.podio.PodioShowVenue;
 import kea.kme.pullpit.server.podio.PodioVenue;
 
 public class PodioObjectHandler {
 	private static PodioObjectHandler podioObjectHandler;
 	private static final Logger log = Logger.getLogger(PodioObjectHandler.class.getName());
 
 	private PodioObjectHandler() {
 	}
 
 	public static PodioObjectHandler getInstance() {
 		if (podioObjectHandler == null)
 			podioObjectHandler = new PodioObjectHandler();
 		return podioObjectHandler;
 	}
 
 	public void writeBands(PodioBand... pb) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c
 				.prepareStatement("INSERT INTO bands VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE bandName=?, bandCountry=?, promoterID=?, lastEdit=?");
 		for (PodioBand p : pb) {
 			ps.setInt(1, p.getBandID());
 			ps.setString(2, p.getBandName());
 			ps.setString(3, p.getBandCountry());
 			ps.setInt(4, p.getPromoterID());
 			ps.setString(5, p.getLastEdit());
 			
 			// ON DUPLICATE KEY UPDATE
 			ps.setString(6, p.getBandName());
 			ps.setString(7, p.getBandCountry());
 			ps.setInt(8, p.getPromoterID());
 			ps.setString(9, p.getLastEdit());
 			
 			ps.executeUpdate();
 		}
 		ps.close();
 	}
 
 	public void writeShows(PodioShow... pshow) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c
				.prepareStatement("INSERT INTO shows (showID, bandID, date, promoID, state, comments, lastEdit) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE bandID=?, date=?, promoID=?, state=?, comments=?, lastEdit=?");
 		for (PodioShow p : pshow) {
 			ps.setInt(1, p.getShowID());
 			ps.setInt(2, p.getBandID());
 			// Creates new java.sql.Date based on Long value of java.util.Date 
 			ps.setDate(3, new Date(p.getDate().getTime()));
 			ps.setInt(4, p.getPromoID());
 			ps.setInt(5, p.getState());
 			ps.setString(6, p.getComments());
 			ps.setString(7, p.getLastEdit());
 			
 			// ON DUPLICATE KEY UPDATE
 			ps.setInt(8, p.getBandID());
 			ps.setDate(9, new Date(p.getDate().getTime()));
 			ps.setInt(10, p.getPromoID());
 			ps.setInt(11, p.getState());
 			ps.setString(12, p.getComments());
 			ps.setString(13, p.getLastEdit());
 			
 			ps.executeUpdate();
 		}
 		ps.close();
 	}
 
 	public void writeVenues(PodioVenue... pv) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c
 				.prepareStatement("INSERT INTO venues VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE venueName=?, capacity=?, lastEdit=?");
 		for (PodioVenue p : pv) {
 			ps.setInt(1, p.getVenueID());
 			ps.setString(2, p.getVenueName());
 			ps.setInt(3, p.getCapacity());
 			ps.setString(4, p.getLastEdit());
 			
 			// ON DUPLICATE KEY UPDATE
 			ps.setString(5, p.getVenueName());
 			ps.setInt(6, p.getCapacity());
 			ps.setString(7, p.getLastEdit());
 			
 			ps.executeUpdate();
 		}
 		ps.close();
 	}
 	
 	public void writeAgents(PodioAgent... pa) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO agents VALUES (?, ?)");
 		for (PodioAgent p : pa) {
 			ps.setInt(1, p.getBandID());
 			ps.setInt(2, p.getContactID());
 			ps.executeUpdate();
 		}
 		ps.close();
 	}
 	
 	public void writeBookers(PodioBooker... pb) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO bookers VALUES (?, ?)");
 		for (PodioBooker p : pb) {
 			ps.setInt(1, p.getVenueID());
 			ps.setInt(2, p.getContactID());
 			ps.executeUpdate();
 		}
 		ps.close();
 	}
 	
 	public void writeContacts(PodioContact... pc) throws SQLException {
 		log.info("Attempting to write " + pc.length + " contacts.");
 		try {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c.prepareStatement("INSERT INTO contacts VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE email=?, telno=?, contactName=?");
 		for (PodioContact p : pc) {
 			ps.setInt(1, p.getContactID());
 			ps.setString(2, p.getEmail());
 			ps.setString(3, p.getTelno());
 			ps.setString(4, p.getContactName());
 			
 			//ON DUPLICATE KEY UPDATE
 			ps.setString(5, p.getEmail());
 			ps.setString(6, p.getTelno());
 			ps.setString(7, p.getContactName());
 			
 			ps.executeUpdate();
 		}
 		ps.close();
 		} catch (SQLException e) {
 			log.info("Error writing contacts! " + e.getMessage());
 		}
 	}
 	
 	public void writeShowVenues(PodioShowVenue... psv) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO showvenues VALUES (?, ?)");
 		for (PodioShowVenue p : psv) {
 			ps.setInt(1, p.getShowID());
 			ps.setInt(2, p.getVenueID());
 			ps.executeUpdate();
 		}
 		ps.close();
 	}
 	
 	
 	public void truncateTable(String tableName) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c.prepareStatement("TRUNCATE TABLE ?");
 		ps.setString(1, tableName);
 		ps.execute();
 		ps.close();
 	}
 	
 	
 	public void truncateAllPodioTables() throws SQLException {
 		String[] tables = {"agents", "bands", "bookers", "contacts", "showvenues", "venues" };
 		for (String s : tables) {
 		String sql = "TRUNCATE TABLE " + s;
 		Connection c = DBConnector.getInstance().getConnection();
 		Statement st = c.createStatement();
 		st.execute(sql);
 		st.close();
 		}
 		nullShows();
 	}
 	
 	private void nullShows() throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		Statement st = c.createStatement();
 		String sql = "UPDATE shows SET date=null";
 		st.execute(sql);
 		st.close();
 	}
 
 	public void deleteRow(String tableName, int itemID) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		log.info(tableName);
 		Statement s = c.createStatement();
 		String primaryKey = "Default";
 		switch(tableName) {
 		case "bands":
 			primaryKey = "bandID";
 			deleteRow("agents", itemID);
 			break;
 		case "shows":
 			primaryKey = "showID";
 			deleteRow("showvenues", itemID);
 			break;
 		case "venues":
 			primaryKey = "venueID";
 			deleteRow("bookers", itemID);
 			break;
 		case "agents":
 			primaryKey = "bandID";
 			break;
 		case "showvenues":
 			primaryKey = "showID";
 			break;
 		case "bookers":
 			primaryKey = "venueID";
 			break;
 		}
 		String sql = "DELETE FROM " + tableName + " WHERE " + primaryKey + "=" + itemID;
 		s.execute(sql);
 		s.close();
 	}
 
 	public void deleteNullShows() throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		Statement st = c.createStatement();
 		String sql = "DELETE FROM shows WHERE date is null";
 		st.execute(sql);
 		st.close();
 	}
 }
 
 
 
 
 
 
 
 
 
 
