 /**
  * 
  */
 package edu.usc.cs587.examples.dbhandlers;
 
 import java.lang.reflect.Array;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import oracle.jdbc.OracleConnection;
 import edu.usc.cs587.examples.Constants;
 import edu.usc.cs587.examples.objects.UserMessage;
 
 /**
  * @author Ling
  *
  */
 public class DatabaseHandler {
 	private static final String HOST = "128.125.163.168";
 	private static final String PORT = "1521";
 	private static final String USERNAME = "team17";   
 	private static final String PASSWORD = "palhunter";
 	private static final String DBNAME = "csci585";
 	private static final String URL = "jdbc:oracle:thin:@";
 
 	protected OracleConnection connection;
 
 	/**
 	 * 
 	 */
 	public DatabaseHandler() {
 		String url = URL + HOST + ":" + PORT + ":" + DBNAME;;
 		try {
 			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
 			connection = (OracleConnection) DriverManager.getConnection(url,
 					USERNAME, PASSWORD);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void closeConnection() {
 		if (this.connection != null) {
 			try {
 				this.connection.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean isConnected() {
 		return this.connection == null;
 	}
 	
 	public boolean addFriends(String pid1, String pid2) {
 		String[] pid2_arr = pid2.split(",");
 		for (String friend : pid2_arr){
 			boolean result = addFriend(pid1,friend);
 			if(!result) return false;
 		}
 		return true;
 	}
 	
 	public boolean removeFriends(String pid1, String pid2) {
 		String[] pid2_arr = pid2.split(",");
 		for (String friend : pid2_arr){
 			boolean result = removeFriend(pid1,friend);
 			if(!result) return false;
 		}
 		return true;
 	}
 	
 	public boolean addFriend(String pid1, String pid2) {
 		if (this.connection == null) {
 			return false;
 		}
 		String sqlStmt = "INSERT INTO RELATIONSHIP(pid1,pid2) VALUES (?,?)";
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			PreparedStatement pstmt2 = this.connection.prepareStatement(sqlStmt);
 			pstmt.setString(1, pid1);
 			pstmt.setString(2, pid2);
 			pstmt.execute();
 			pstmt.close();
 			
 			pstmt2.setString(1, pid2);
 			pstmt2.setString(2, pid1);
 			pstmt2.execute();
 			pstmt2.close();
 			return true;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean removeFriend(String pid1, String pid2) {
 		if (this.connection == null) {
 			return false;
 		}
 		String sqlStmt = "DELETE FROM RELATIONSHIP WHERE pid1=? AND pid2=?";
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			PreparedStatement pstmt2 = this.connection.prepareStatement(sqlStmt);
 			pstmt.setString(1, pid1);
 			pstmt.setString(2, pid2);
 			pstmt.execute();
 			pstmt.close();
 			
 			pstmt2.setString(1, pid2);
 			pstmt2.setString(2, pid1);
 			pstmt2.execute();
 			pstmt2.close();
 			return true;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean insertNewPeopleToDB(int pid, String first_name, String last_name, long created_date, String username, String password) {
 		if (this.connection == null) {
 			return false;
 		}
 		String sqlStmt = "INSERT INTO PEOPLE(PID,FIRST_NAME, LAST_NAME,CREATED_TIME,USERNAME, PASSWORD) VALUES (?,?,?,?,?,?)";
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			
 			pstmt.setInt(1, pid);
 			pstmt.setString(2, first_name);
 			pstmt.setString(3, last_name);
 			pstmt.setLong(4, created_date);
 			pstmt.setString(5, first_name);
 			pstmt.setString(6, last_name);
 			pstmt.execute();
 			pstmt.close();
 			return true;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean insertPeopleToDB(int pid, String first_name, String last_name, long created_date) {
 		if (this.connection == null) {
 			return false;
 		}
 		String sqlStmt = "INSERT INTO PEOPLE(PID,FIRST_NAME, LAST_NAME,CREATED_TIME) VALUES (?,?,?,?)";
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			
 			pstmt.setInt(1, pid);
 			pstmt.setString(2, first_name);
 			pstmt.setString(3, last_name);
 			pstmt.setLong(4, created_date);
 			pstmt.execute();
 			pstmt.close();
 			return true;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean insertLocation(int pid, long lat_int, long long_int, long created_date) {
 		if (this.connection == null) {
 			return false;
 		}
 		String sqlStmt = "INSERT INTO LOCATION VALUES (?,?,?,?)";
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			
 			pstmt.setInt(1, pid);
 			pstmt.setLong(2, long_int);
 			pstmt.setLong(3, lat_int);
 			pstmt.setLong(4, created_date);
 			pstmt.execute();
 			pstmt.close();
 			return true;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 	}
 	
 	public String queryPeopleName(String first_name, String last_name){
 		String table = "PEOPLE";
 		String sqlStmt =  "SELECT * FROM "+table+" where first_name ='"+first_name+"' and last_name ='"+last_name+"'";
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String queryPeopleUsername(String username, String password){
 		String table = "PEOPLE";
 		String sqlStmt =  "SELECT * FROM "+table+" where USERNAME ='"+username+"' and PASSWORD ='"+password+"'";
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String findAllPeople(){
 		String table = "PEOPLE";
 		String sqlStmt =  "SELECT * FROM "+table;
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String getTotalPeople(){
 		String table = "PEOPLE";
 		String action = "AGGREGATE";
 		String sqlStmt =  "SELECT COUNT(*) FROM "+table;
 		String rs = runQuery (sqlStmt, action);
 		return rs;
 	}
 	
 	public String findAllFriends(String id){
 		String table = "PEOPLE";
 		String sqlStmt =  "SELECT * FROM PEOPLE WHERE PID IN (select PID2 from RELATIONSHIP WHERE PID1="+id+")";
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String findAllNonFriends(String id){
 		String table = "PEOPLE";
 		String sqlStmt =  "SELECT * FROM PEOPLE WHERE PID NOT IN (select PID2 from RELATIONSHIP WHERE PID1="+id+") and PID !="+id;
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String queryPeople(String id){
 		String table = "PEOPLE";
 		String sqlStmt =  "SELECT * FROM "+table+" where pid ="+id;
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String queryPastLocations(String id){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT * FROM "+table+" where pid ="+id;
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String queryCurrentLocations(String id){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT * FROM(SELECT * FROM "+table+" where pid ="+id +" ORDER BY UPDATED_TIME DESC) where rownum < 2";
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String queryFriendsLocations(String id){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT l.PID, l.long_int, l.lat_int, l.updated_time FROM LOCATION l, (SELECT MAX(updated_time) as updated_time,PID FROM location WHERE PID IN (select PID2 from RELATIONSHIP WHERE PID1="+id+") GROUP by PID) loc where l.updated_time = loc.updated_time and l.pid = loc.pid";
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String queryFriendsPastLocations(String id){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT * FROM "+table+" WHERE PID IN (select PID2 from RELATIONSHIP WHERE PID1="+id+")";
 		String rs = runQuery (sqlStmt, table);
 		return rs;
 	}
 	
 	public String queryFriendsLocationsWithinMiles(String id, String radius, String lat, String lon){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT l.PID, l.long_int, l.lat_int, l.updated_time FROM LOCATION l, (SELECT MAX(updated_time) as updated_time,PID FROM location WHERE PID IN (select PID2 from RELATIONSHIP WHERE PID1="+id+") GROUP by PID) loc where l.updated_time = loc.updated_time and l.pid = loc.pid";
 		double radius_d = Double.parseDouble(radius);
 		double lat_d = Integer.parseInt(lat)*0.000001;
 		double lon_d = Integer.parseInt(lon)*0.000001;
 		String rs = runQuerySpatialFilter (sqlStmt, radius_d, lat_d, lon_d);
 		return rs;
 	}
 	
 	public String queryKNN(String id, String kfriends, String lat, String lon){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT l.PID, l.long_int, l.lat_int, l.updated_time, p.FIRST_NAME, p.LAST_NAME, p.USERNAME FROM LOCATION l,PEOPLE p, (SELECT MAX(updated_time) as updated_time,PID FROM location WHERE PID IN (select PID2 from RELATIONSHIP WHERE PID1="+id+") GROUP by PID) loc where l.updated_time = loc.updated_time and l.pid = loc.pid and p.pid = loc.pid";
 		int k_friends = Integer.parseInt(kfriends);
 		double lat_d = Integer.parseInt(lat)*0.000001;
 		double lon_d = Integer.parseInt(lon)*0.000001;
 		String rs = runQuerySpatialKNN (sqlStmt, k_friends, lat_d, lon_d);
 		return rs;
 	}
 	
 	public String queryKNNUsers(String id, String kfriends, String lat, String lon){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT l.PID, l.long_int, l.lat_int, l.updated_time, p.FIRST_NAME, p.LAST_NAME, p.USERNAME FROM LOCATION l,PEOPLE p, (SELECT MAX(updated_time) as updated_time,PID FROM location WHERE PID !="+id+" GROUP by PID) loc where l.updated_time = loc.updated_time and l.pid = loc.pid and p.pid = loc.pid";
 		int k_friends = Integer.parseInt(kfriends);
 		double lat_d = Integer.parseInt(lat)*0.000001;
 		double lon_d = Integer.parseInt(lon)*0.000001;
 		String rs = runQuerySpatialKNN (sqlStmt, k_friends, lat_d, lon_d);
 		return rs;
 	}
 	
 	public String queryFriendsPastLocationsWithinMiles(String id, String radius, String lat, String lon){
 		String table = "LOCATION";
 		String sqlStmt =  "SELECT * FROM "+table+" WHERE PID IN (select PID2 from RELATIONSHIP WHERE PID1="+id+")";
 		double radius_d = Double.parseDouble(radius);
 		double lat_d = Integer.parseInt(lat)*0.000001;
 		double lon_d = Integer.parseInt(lon)*0.000001;
 		String rs = runQuerySpatialFilter (sqlStmt, radius_d, lat_d, lon_d);
 		return rs;
 	}
 	
 	
 	public String convertPeopleToJSON(ResultSet rs){
 		if(rs == null) return "[]";
 		String result = "[";
 		try {
 			while (rs != null && rs.next()) {
 				result +="{";
 				result += "\"PID\":\""+rs.getInt("PID")+"\"";
 				result += ",\"FIRST_NAME\":\""+rs.getString("FIRST_NAME")+"\"";
 				result += ",\"LAST_NAME\":\""+rs.getString("LAST_NAME")+"\"";
 				result += ",\"CREATED_TIME\":\""+rs.getLong("CREATED_TIME")+"\"";
 				result +="},";
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(result.length()>2)
 			result = result.substring(0, result.length()-1);
 		result+="]";
 		return result;
 	}
 	
 	public String convertLocationToJSON(ResultSet rs){
 		if(rs == null) return "[]";
 		String result = "[";
 		try {
 			while (rs != null && rs.next()) {
 				result +="{";
 				result += "\"PID\":\""+rs.getInt("PID")+"\"";
 				result += ",\"LONG_INT\":\""+rs.getInt("LONG_INT")+"\"";
 				result += ",\"LAT_INT\":\""+rs.getInt("LAT_INT")+"\"";
 				result += ",\"UPDATED_TIME\":\""+rs.getLong("UPDATED_TIME")+"\"";
 				result +="},";
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(result.length()>2)
 			result = result.substring(0, result.length()-1);
 		result+="]";
 		return result;
 	}
 	
 	public String convertLocationToJSONSpatialFilter(ResultSet rs, double radius, double lat, double lon){
 		if(rs == null) return "[]";
 		String result = "[";
 		try {
 			while (rs != null && rs.next()) {
 				double friend_lat = rs.getInt("LAT_INT") * 0.000001;
 				double friend_lon = rs.getInt("LONG_INT") * 0.000001;
 				if(HaverSineDistance(friend_lat, friend_lon, lat, lon)<=radius){
 					result +="{";
 					result += "\"PID\":\""+rs.getInt("PID")+"\"";
 					result += ",\"LONG_INT\":\""+rs.getInt("LONG_INT")+"\"";
 					result += ",\"LAT_INT\":\""+rs.getInt("LAT_INT")+"\"";
 					result += ",\"UPDATED_TIME\":\""+rs.getLong("UPDATED_TIME")+"\"";
 					result +="},";
 				}
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(result.length()>2)
 			result = result.substring(0, result.length()-1);
 		result+="]";
 		return result;
 	}
 	
 	public String convertLocationToJSONSpatialKNN(ResultSet rs, int kfriends, double lat, double lon){
 		if(rs == null) return "[]";
 		String result = "[";
 		try {
 			double[] Distances = new double[1000];
 			
 			String[] data = new String[1000];
 			boolean[] ispicked = new boolean[1000];
 			int i=0;
 			while (rs != null && rs.next()) {
 				double friend_lat = rs.getInt("LAT_INT") * 0.000001;
 				double friend_lon = rs.getInt("LONG_INT") * 0.000001;
 				Distances[i]=HaverSineDistance(friend_lat, friend_lon, lat, lon);
 				data[i]="";
 				data[i] +="{";
 				data[i] += "\"PID\":\""+rs.getInt("PID")+"\"";
 				data[i] += ",\"LONG_INT\":\""+rs.getInt("LONG_INT")+"\"";
 				data[i] += ",\"LAT_INT\":\""+rs.getInt("LAT_INT")+"\"";
 				data[i] += ",\"FIRST_NAME\":\""+rs.getString("FIRST_NAME")+"\"";
 				data[i] += ",\"LAST_NAME\":\""+rs.getString("LAST_NAME")+"\"";
 				data[i] += ",\"USERNAME\":\""+rs.getString("USERNAME")+"\"";
 				data[i] += ",\"UPDATED_TIME\":\""+rs.getLong("UPDATED_TIME")+"\"";
 				data[i] +="}";
 				ispicked[i] = false;
 				i++;
 			}
 			double[] sortedDistances = new double[i];
 			for(int j=0;j<i;j++){sortedDistances[j]=Distances[j];}
 			Arrays.sort(sortedDistances);
 			
 			int num_return = kfriends;
 			if(num_return>i)
 				num_return=i;
 			for(int j=0;j<num_return;j++){
				double d = sortedDistances[i-1-j];
 				for(int z=0;z<i;z++){
 					if(Distances[z]==d && !ispicked[z]){
 						result+=data[z]+",";
 						ispicked[z] = true;
 						break;
 					}
 				}
 			}
 			
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(result.length()>2)
 			result = result.substring(0, result.length()-1);
 		result+="]";
 		return result;
 	}
 	
 	public String convertAggregateToJSON(ResultSet rs){
 		if(rs == null) return "[]";
 		String result = "[";
 		try {
 			while (rs != null && rs.next()) {
 				result +="{";
 				result += "\"TOTAL\":\""+rs.getInt(1)+"\"";
 				result +="},";
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(result.length()>2)
 			result = result.substring(0, result.length()-1);
 		result+="]";
 		return result;
 	}
 	
 	public String runQuerySpatialFilter (String sqlStmt, double radius, double lat, double lon) {
 		ResultSet rs = null;
 		String result = "[]";
 		if (this.connection == null) {
 			return result;
 		}
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			rs = pstmt.executeQuery();
 			result = convertLocationToJSONSpatialFilter(rs, radius, lat, lon);
 			pstmt.close();
 			return result;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return result;
 		}
 	}
 	
 	public String runQuerySpatialKNN (String sqlStmt, int kfriends, double lat, double lon) {
 		ResultSet rs = null;
 		String result = "[]";
 		if (this.connection == null) {
 			return result;
 		}
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			rs = pstmt.executeQuery();
 			result = convertLocationToJSONSpatialKNN(rs, kfriends, lat, lon);
 			pstmt.close();
 			return result;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return result;
 		}
 	}
 	
 	public String runQuery (String sqlStmt, String table) {
 		ResultSet rs = null;
 		String result = "[]";
 		if (this.connection == null) {
 			return result;
 		}
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			rs = pstmt.executeQuery();
 			if(table.compareTo("PEOPLE")==0)
 				result = convertPeopleToJSON(rs);
 			else if(table.compareTo("LOCATION")==0)
 				result = convertLocationToJSON(rs);
 			else if(table.compareTo("AGGREGATE")==0)
 				result = convertAggregateToJSON(rs);
 			pstmt.close();
 			return result;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return result;
 		}
 	}
 	
 	
 	public List<UserMessage> retrieveAllRecords () {
 		if (this.connection == null) {
 			return null;
 		}
 		String sqlStmt = "SELECT * FROM EXAMPLE";
 		
 		try {
 			PreparedStatement pstmt = this.connection.prepareStatement(sqlStmt);
 			ResultSet rs = pstmt.executeQuery();
 			List<UserMessage> ret = new ArrayList<UserMessage>();
 			while (rs != null && rs.next()) {
 				long pubDate = rs.getLong("PUBDATE");
 				String userid = rs.getString("USERID");
 				String message = rs.getString("MESSAGE");
 				ret.add(new UserMessage(userid, message, pubDate));
 			}
 			pstmt.close();
 			return ret;
 		}catch (SQLException ex) {
 			ex.printStackTrace();
 			return null;
 		}
 	}
 	
 	//return distances in miles.
 	public static double HaverSineDistance(double lat1, double lng1, double lat2, double lng2) 
 	{
 	    // http://en.wikipedia.org/wiki/Haversine_formula
 		double EARTH_RADIUS = 3958.75;
 
 	    // convert to radians
 	    lat1 = Math.toRadians(lat1);
 	    lng1 = Math.toRadians(lng1);
 	    lat2 = Math.toRadians(lat2);
 	    lng2 = Math.toRadians(lng2);
 
 	    double dlon = lng2 - lng1;
 	    double dlat = lat2 - lat1;
 
 	    double a = Math.pow((Math.sin(dlat/2)),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);
 
 	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 
 	    return EARTH_RADIUS * c;
 	}  
 	
 	/*
 	 * this is to test whether the java program can write data to database
 	 */
 	public static void main(String[] args) {
 		DatabaseHandler handler = new DatabaseHandler();
 		try {
 			//insert an record to the database table 
 //			handler.insertRecordToDB("Ling Hu", "Helloworld!!", System.currentTimeMillis());
 			//retrieve all records from the database table 
 			String first_name = "Luan";
 			String last_name = "Nguyen";
 			int id = 1;
 			long created_date = Long.parseLong("1347774599443");;
 			handler.insertPeopleToDB(id, first_name, last_name,created_date);
 			handler.closeConnection();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
