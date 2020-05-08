 package com.nearme;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 
 /**
  * A PoiFinder class which talks to an SQL database and pulls out lists of
  * Points of Interest
  * 
  * @author twhume
  * 
  */
 
 public class DatabasePoiFinder implements PoiFinder {
 	
 	private static final Logger logger = Logger.getLogger(DatabasePoiFinder.class);
 	
 	private DataSource dataSource = null;
 	static Connection conn = null;
 
 	static String bd = "nearme";
 	static String login = "root";
 	static String password = "123";
 	static String url = "jdbc:mysql://localhost/" + bd;
 
 	public static void main(String args[]) throws Exception {
 
 		Class.forName("com.mysql.jdbc.Driver").newInstance(); // load the driver
 																// of mysql
 		conn = DriverManager.getConnection(url, login, password); // connect
 																	// with data
 																	// base
 
 		if (conn != null) {
 			logger.debug("Database connection " + url);
 			// conn.close();
 		}
 
 	}
 
 	/* Talk to this DataSource to deal with the database */
 
 	public DatabasePoiFinder(DataSource d) {
 		this.dataSource = d;
 	}
 
 	/**
 	 * Pull out a list of Points of Interest matching the contents of the
 	 * PoiQuery passed in, and return them in a list.
 	 */
 
 	public List<Poi> find(PoiQuery pq) throws SQLException {
 
 		ArrayList<Poi> ret = new ArrayList<Poi>();
 
 		Connection conn = dataSource.getConnection();
 
 		PoiType type = null;
 
 		/**
 		 * Algorithm taken from
 		 * 
 		 * http://stackoverflow.com/questions/574691/mysql-great-circle-distance
 		 * -haversine-formula
 		 * 
 		 */
 
 		String sql = "SELECT *, ( 6371 * acos( cos( radians(?) ) * cos( radians( latitude ) ) * cos( radians( longitude ) - radians(?) ) + sin( radians(?) ) * sin( radians( latitude ) ) ) ) AS distance  FROM poi WHERE type IN (";
 
 		for (int i: pq.getTypes()) {
 			sql = sql + i + ",";
 		}
 		sql = sql.substring(0, sql.length()-1);
 
 		sql = sql + ") HAVING distance < ? ORDER BY distance";
 		
 		logger.debug("find() sql="+sql);
 
 		PreparedStatement locationSearch = conn.prepareStatement(sql);
 		locationSearch.setDouble(1, pq.getLatitude());
 		locationSearch.setDouble(2, pq.getLongitude());
 		locationSearch.setDouble(3, pq.getLatitude());
 		float radiusInKm = ((float) pq.getRadius() / 1000);
 		locationSearch.setDouble(4, radiusInKm);
 		ResultSet rs = locationSearch.executeQuery();
 
 		while (rs.next()) // Return false when there is not more data in the
 							// table
 		{
			type = new PoiType("",rs.getInt("type")); //TODO ought to populate these with names too, by joining on types in the SQL query above
 			Poi newPoi = new Poi(rs.getString("name"),
 					rs.getDouble("latitude"), rs.getDouble("longitude"), type,
 					rs.getInt("Id"));
 			ret.add(newPoi);
 
 			logger.debug("Found " + rs.getObject("name") + ", Longitude: "
 					+ rs.getObject("longitude") + ", Latitude: "
 					+ rs.getObject("latitude") +", distance="+ rs.getString("distance"));
 		}
 		rs.close();
 
 		return ret;
 	}
 
 }
