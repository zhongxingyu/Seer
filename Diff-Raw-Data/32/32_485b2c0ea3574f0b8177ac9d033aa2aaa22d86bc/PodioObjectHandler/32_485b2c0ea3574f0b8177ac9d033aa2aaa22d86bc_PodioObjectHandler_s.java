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
			pshow.setInt(1, pshow.getShowID());
			pshow.setInt(2, pshow.getBandID());
			pshow.setDate(3, pshow.getDate());
			pshow.setInt(4, pshow.getState());
			pshow.setDouble(5, pshow.getFee());
			pshow.setString(6, pshow.getFeeCurrency());
			pshow.setDouble(7, pshow.getProvision());
			pshow.setString(8, pshow.getProvisionCurrency());
			pshow.setInt(9, pshow.getProductionType());
			pshow.setInt(10, pshow.getProfitSplit());
			pshow.setInt(11, pshow.getTicketPrice());
			pshow.setDouble(12, pshow.getKodaPct());
			pshow.setDouble(13, pshow.getVAT());
			pshow.setInt(14, pshow.getTicketsSold());
			pshow.setString(15, pshow.getComment());
			pshow.setTimestamp(16, pshow.getLastEdit());
 			ps.executeUpdate();
 		}
 	}
 }
