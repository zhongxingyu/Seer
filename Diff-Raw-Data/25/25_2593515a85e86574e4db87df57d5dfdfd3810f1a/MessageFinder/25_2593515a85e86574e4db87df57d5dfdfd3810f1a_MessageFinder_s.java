 /**
  * SOEN 490
  * Capstone 2011
  * Table Data Gateway for the User Domain Object
  * Team members: 	
  * 			Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package foundation.finder;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.List;
 
 import javax.print.attribute.standard.Severity;
 
 import application.ServerParameters;
 
 import domain.serverparameter.ServerParameter;
 
 import foundation.Database;
 import foundation.tdg.MessageTDG;
 
 
 /**
  * Foundation class for executing finds on Message table
  * @author Moving Target
  */
 public class MessageFinder {
 	private final static String SELECT_ALL = 
 			"SELECT m.mid, " +
 			"m.uid, " +
 			"m.message, " +
 			"m.speed, " +
 			"m.latitude, " +
 			"m.longitude, " +
 			"m.created_at, " +
 			"m.user_rating " +
 			"FROM " + MessageTDG.TABLE + " AS m";
 
 	/**
 	 * Finds all messages in the Message table
 	 * @return Returns the ResultSet containing the message information as 
 	 * m.mid, m.uid, m.message, m.speed, m.latitude, m.longitude, m.created_at, m.user_rating, m.version
 	 * @throws SQLException
 	 */
 	public static ResultSet findAll() throws SQLException {
 		Connection connection = Database.getConnection();
 		PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
 		
 		ResultSet rs = ps.executeQuery();
 		return rs;
 	}
 
 	private static final String SELECT =
 		"SELECT m.mid, " +
 		"m.uid, " +
 		"m.message, " +
 		"m.speed, " +
 		"m.latitude, " +
 		"m.longitude, " +
 		"m.created_at, " +
 		"m.user_rating " +
 		"FROM " + MessageTDG.TABLE + " AS m " +
 		"WHERE m.mid = ?;";
 
 	/** 
 	 * Finds a message with the passed message id
 	 * @param mid Message id
 	 * @return Returns the ResultSet containing the message information as
 	 * m.mid, m.uid, m.message, m.speed, m.latitude, m.longitude, m.created_at, m.user_rating, m.version
 	 * @throws SQLException
 	 */
 	public static ResultSet find(BigInteger mid) throws SQLException {
 		Connection connection = Database.getConnection();
 		PreparedStatement ps = connection.prepareStatement(SELECT);
 		
 		ps.setObject(1, mid);
 		ResultSet rs = ps.executeQuery();
 		return rs;
 	}
 
 	private static final String SELECT_BY_EMAIL =
 			"SELECT * FROM " + MessageTDG.TABLE + " AS m WHERE m.uid = ?;";
 
 	public static ResultSet findByUser(BigInteger uid) throws SQLException {
 		Connection connection = Database.getConnection();
 		PreparedStatement ps = connection.prepareStatement(SELECT_BY_EMAIL);
 		
 		ps.setBigDecimal(1, new BigDecimal(uid));
 		ResultSet rs = ps.executeQuery();
 		return rs;
 	}
 
 	/**
 	 * 
 	 * @param longitude
 	 * @param latitude
 	 * @param radius
 	 * @return All points within a bounding rectangle that has a half cross-section of radius.
 	 * @throws SQLException
 	 */		
 	// Good explanation of how to create a fast query that is within particular bounds.
 	// http://www.scribd.com/doc/2569355/Geo-Distance-Search-with-MySQL
 	private static final String SELECT_BY_RADIUS =
 			"SELECT m.mid, m.uid, m.message, m.speed, m.latitude, m.longitude, m.created_at, m.user_rating, m.version " +
 			"FROM " + MessageTDG.TABLE + " AS m " + "WHERE m.longitude BETWEEN ? AND ? AND m.latitude BETWEEN ? AND ?;";
 
 	public static ResultSet findInProximity(double longitude, double latitude, double radius) throws SQLException {
 		Connection connection = Database.getConnection();
 		PreparedStatement ps = connection.prepareStatement(SELECT_BY_RADIUS);
 		
 		List<Coordinate> rectangle = GeoSpatialSearch.convertPointToRectangle(new Coordinate(longitude, latitude), radius);
 		ps.setDouble(1, rectangle.get(0).getLongitude());
 		ps.setDouble(2, rectangle.get(1).getLongitude());
 		ps.setDouble(3, rectangle.get(0).getLatitude());
 		ps.setDouble(4, rectangle.get(1).getLatitude());
 		ResultSet rs = ps.executeQuery();
 		return rs;
 	}
 
 	private static final String SELECT_ID_BY_RADIUS =
			"SET ROW COUNT ?" +
 			"SELECT m.mid " +
			"FROM " + MessageTDG.TABLE + " AS m " + "WHERE m.longitude BETWEEN ? AND ? AND m.latitude BETWEEN ? AND ?;";
 	
 	private static String GET_SIZE = 
 			"SELECT COUNT(m.mid) AS size " +
 			"FROM " + MessageTDG.TABLE + " AS m " + "WHERE m.longitude BETWEEN ? AND ? AND m.latitude BETWEEN ? AND ?;";
 
 	/**
 	 * 
 	 * @param longitude
 	 * @param latitude
 	 * @param speed
 	 * @return All ids of messages within a bounding rectangle that has a half cross-section of radius.
 	 * @throws SQLException
 	 */		
 	
 	public static ResultSet findIdsInProximity(double longitude, double latitude, double speed) throws SQLException, IOException {
 		
 		Connection connection = Database.getConnection();
 		ServerParameters params = ServerParameters.getUniqueInstance();
		int minMessages  = Integer.getInteger(params.get("minMessages").getValue());
		int maxMessages  = Integer.getInteger(params.get("maxMessages").getValue());
 
 		//////////////////////////////////////////////////////////////////////
 		
 		double radius = 0;
 		double multiplier = 0;
 		double radiusAdder = 500;
 		double maxRadius = 0;
 		
 		
 		//If the gps doesn't return a speed the default speed is 30
 		if(speed == 0)
 			speed = 30;
 		//The speed is in km 
 		//over 60km/h is highway driving 8 minutes to reach the farthest of the messages
 		//up to 16 minutes if you don't have at least 10 messages
 		if(speed > 60)
 		{	
 			multiplier = 134;
 			radius = multiplier*speed;
 			maxRadius = 2*multiplier*speed;
 		}//over 30km/h driving is city driving 10 minutes to reach the farthest of the messages Up to 20 minutes if you don't have at least 10 messages
 		else if(speed>30 && speed<=60)
 		{
 			multiplier = 167;
 			radius = multiplier*speed;
 			maxRadius = 2*multiplier*speed;
 		}//this is for biking speed 7 minutes to reach the farthest of the messages up to 14 minutes if you don't have at least 10 messages
 		else if(speed>9 && speed <=30)
 		{
 			multiplier = 11;
 			radius = multiplier * speed;
 			maxRadius = 2*multiplier*speed;
 		}//this is walking speed 5 minutes to reach the farthest of the messages up to 10 minutes if you don't have at least 10 messages
 		else if(speed<=9)
 		{
 			multiplier = 83.3;
 			radius = multiplier * speed;
 			maxRadius = 2*multiplier*speed;
 		}
 		//Next block of code is to enlarge the radius until you either find 10 messages or you reach the maximum radius allowed for that speed
 		ResultSet rsSize;
 		//flag for not incrementing the radius on the first run
 		boolean flag = false;
 		int count = 0; // Used to stop the loop if the algorithm can't find 10 messages after a certain amount of iterations. 
 		int size = 0;
 		do {		
 			PreparedStatement psSize = connection.prepareStatement(GET_SIZE);
 			
 			List<Coordinate> rectangle = GeoSpatialSearch.convertPointToRectangle(new Coordinate(longitude, latitude), radius);
			psSize.setInt(1, size);
			psSize.setDouble(2, rectangle.get(0).getLongitude());
			psSize.setDouble(3, rectangle.get(1).getLongitude());
			psSize.setDouble(4, rectangle.get(0).getLatitude());
			psSize.setDouble(5, rectangle.get(1).getLatitude());
 			rsSize = psSize.executeQuery();
 			rsSize.next();
 		
 			if(flag)
 				radius += radiusAdder;
 			else
 				flag = true;
 			
 			count++;		
 			
 			size = rsSize.getInt("size");
			
 			if(size > maxMessages) {
 				size = maxMessages;  
 			}
 		}
 		while(size <= minMessages && radius <= maxRadius && count <= 2);
 		
 		//Getting the actual ids at this point
 		ResultSet finaleRs;
 		
 		PreparedStatement finalPs = connection.prepareStatement(SELECT_ID_BY_RADIUS);
 		List<Coordinate> rectangle = GeoSpatialSearch.convertPointToRectangle(new Coordinate(longitude, latitude), radius);
		
 		finalPs.setDouble(1, rectangle.get(0).getLongitude());
 		finalPs.setDouble(2, rectangle.get(1).getLongitude());
 		finalPs.setDouble(3, rectangle.get(0).getLatitude());
 		finalPs.setDouble(4, rectangle.get(1).getLatitude());
 		finaleRs = finalPs.executeQuery();
 
 		return finaleRs;
 	}
 	
 	private static final String SELECT_BY_DATE = 
 			"SELECT m.mid" +  
 			"From" + MessageTDG.TABLE + "As m " +
 		    "Where DATE_ADD(created_at, INTERVAL ? DAY) >= ?";
 	
 	/**
 	 * Finds messages that are expired based on their date and time to live
 	 * @param timeToLive
 	 * @return Returns the resultset containing the message Ids to be deleted
 	 * @throws SQLException
 	 * @throws IOException
 	 */
 	public static ResultSet findExpired(int timeToLive) throws SQLException {
 		Connection connection = Database.getConnection();
 		PreparedStatement ps = connection.prepareStatement(SELECT_BY_DATE);	
 		
 		ps.setInt(1, timeToLive);
 		ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
 		ResultSet rs = ps.executeQuery();
 		return rs;
 	}
 		
 }
