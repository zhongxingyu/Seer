 package kea.kme.pullpit.server.persistence;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import kea.kme.pullpit.server.podio.PodioBand;
 import kea.kme.pullpit.server.podio.PodioShow;
 
 public class PodioObjectHandler {
 	private static PodioObjectHandler podioObjectHandler;
 	// TODO indl�sning af hele podio
 	// TODO indl�sning af enkeltitems vha. webhooks
 	// TODO create sql query
 	
 	private PodioObjectHandler() {
 	}
 	
 	public static PodioObjectHandler getInstance() {
 		if (podioObjectHandler == null)
 			podioObjectHandler = new PodioObjectHandler();
 		return podioObjectHandler;
 	}
 
 	
 	public void writeBands(PodioBand... pb) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c.prepareStatement("INSERT INTO bands VALUES (?, ?, ?, ?, ?)");
 		for (PodioBand p : pb) {
 			ps.setInt(1, p.getBandID());
 			ps.setString(2, p.getBandName());
 			ps.setString(3, p.getBandCountry());
 			ps.setInt(4, p.getPromoterID());
 			ps.setTimestamp(5, p.getLastEdit());
 			ps.executeUpdate();
 		}
 	}
 	
 	public void writeShows(PodioShow... pshow) throws SQLException {
 		Connection c = DBConnector.getInstance().getConnection();
 		PreparedStatement ps = c.prepareStatement("INSERT INTO shows VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 		for (PodioShow p : pshow) {
			ps.setInt(1, p.getShowID());
			ps.setInt(2, p.getBandID());
			ps.setDate(3, p.getDate());
			ps.setInt(4, p.getState());
			ps.setDouble(5, p.getFee());
			ps.setString(6, p.getFeeCurrency());
			ps.setDouble(7, p.getProvision());
			ps.setString(8, p.getProvisionCurrency());
			ps.setInt(9, p.getProductionType());
			ps.setInt(10, p.getProfitSplit());
			ps.setInt(11, p.getTicketPrice());
			ps.setDouble(12, p.getKodaPct());
			ps.setDouble(13, p.getVAT());
			ps.setInt(14, p.getTicketsSold());
			ps.setString(15, p.getComments());
			ps.setTimestamp(16, p.getLastEdit());
 			ps.executeUpdate();
 		}
 	}
 }
