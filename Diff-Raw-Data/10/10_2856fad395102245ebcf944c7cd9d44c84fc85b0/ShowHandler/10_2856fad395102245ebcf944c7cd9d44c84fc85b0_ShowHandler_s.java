 package kea.kme.pullpit.server.persistence;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 
 import kea.kme.pullpit.client.objects.Band;
 import kea.kme.pullpit.client.objects.Show;
 import kea.kme.pullpit.client.objects.Venue;
 import java.util.logging.Logger;
 
 public class ShowHandler {
 	 private static final Logger log = Logger.getLogger(ShowHandler.class
 	 .getName());
 
 	public static Show[] getShows(int offset, int limit, String orderBy)
 			throws SQLException {
 		Connection con = DBConnector.getInstance().getConnection();
 		HashMap<Integer, Show> results = new HashMap<Integer, Show>();
 		Statement s = con.createStatement();
 		String sql = "SELECT shows.*, venues.*, bands.bandID, bands.bandName "
 				+ "FROM ((shows LEFT OUTER JOIN showvenues ON shows.showID = showvenues.showID) "
 				+ "LEFT OUTER JOIN venues ON showvenues.venueID = venues.venueID) "
 				+ "JOIN bands ON bands.bandID = shows.bandID "
 				+ "ORDER BY " + orderBy + " LIMIT " + offset + "," + limit;
 		ResultSet rs = s.executeQuery(sql);
 		while (rs.next()) {
 			// Uses column-name to avoid confusion when queries are joined
 			int showID = rs.getInt("showID");
 			int bandID = rs.getInt("bandID");
 			String bandName = rs.getString("bandName");
 			Date date = rs.getDate("date");
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			String dateString = dateFormat.format(date);
 			int state = rs.getInt("state");
 			String comments = rs.getString("comments");
 			Date lastEdit = rs.getDate("lastEdit");
 			SimpleDateFormat lastEditFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
 			String lastEditString =  lastEditFormat.format(lastEdit);
 			// Handles shows with no Venues
 			if (rs.getString("venueName") == null) {
 				results.put(showID, new Show(showID,
 						new Band(bandID, bandName), dateString, state, comments,
 						lastEditString));
 			// Handles shows with Venues
 			} else {
 				// Handles shows with one or more venues
 				if (results.containsKey(showID)) {
 					// Creates temp array to hold existing venues
 					Venue[] temp = results.get(showID).getVenues();
 					// Creates new array, 1 longer than temp
 					Venue[] newTemp = new Venue[temp.length + 1];
 					// Inserts new Venue on index 0 (always same place no matter how many venues in current show
 					newTemp[0] = new Venue(rs.getInt("venueID"),
 							rs.getString("venueName"));
 					// Moves the old venues one place up
					for (int i = 1; i <= newTemp.length; i++) {
 						newTemp[i] = temp[i - 1];
 					}
 					results.get(showID).setVenues(newTemp);
 				} else {
 					// Handles new shows 
 					Venue[] venues = new Venue[1];
 					venues[0] = new Venue(rs.getInt("venueID"), rs.getString("venueName"));
 					log.info("Adding new venue, " + venues[0].toString());
					results.put(
							showID,
							new Show(showID, new Band(bandID, bandName), dateString,
									state, comments, lastEditString, venues));
 				}
 			}
 		}
 		rs.close();
 		log.info("returning " + results.size() + "shows");
 		return results.values().toArray(new Show[results.size()]);
 	}
 }
