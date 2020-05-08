 package together;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import net.sf.json.JSONObject;
 
 public class RequestHandler {
 	private Connection conn = null;
 	
 	public RequestHandler() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
 		conn = DBConnection.getConnection();
 	}
 	
 	public ArrayList<JSONObject> listUser(String uid, String radius) throws SQLException {
 		ArrayList<JSONObject> array = new ArrayList<JSONObject>();
 		String queryUserAll = "select * from user";
 		PreparedStatement pstmtQueryUserAll = conn.prepareStatement(queryUserAll);
 		ResultSet rs = pstmtQueryUserAll.executeQuery();
 		while(rs.next()) {
 			JSONObject obj = new JSONObject();
 			obj.put("uid", rs.getString(1));
 			obj.put("uname", rs.getString(2));
 			obj.put("longitude", rs.getString(3));
 			obj.put("latitude", rs.getString(4));
 			array.add(obj);
 		}
 		return array;
 	}
 	
 	public ArrayList<JSONObject> listEvent(String uid, String radius) throws SQLException{
 		ArrayList<JSONObject> array = new ArrayList<JSONObject>();
 		String queryEventAll = "select * from event";
 		PreparedStatement pstmtQueryEventAll = conn.prepareStatement(queryEventAll);
 		ResultSet rs = pstmtQueryEventAll.executeQuery();
 		while(rs.next()) {
 			JSONObject obj = new JSONObject();
 			obj.put("eid", rs.getString(1));
			obj.put("ename", rs.getString(2));
 			obj.put("uid", rs.getString(3));
 			obj.put("type", rs.getString(4));
			obj.put("longitude", rs.getString(5));
			obj.put("latitude", rs.getString(6));
			obj.put("startDate", rs.getString(7));
			obj.put("startTime", rs.getString(8));
			obj.put("endDate", rs.getString(9));
			obj.put("endTime", rs.getString(10));
 			array.add(obj);
 		}
 		return array;
 	}
 	
 	public int newEvent(String place, String uid, String type, String description,
 			String longitude, String latitude, String startDate, 
 			String startTime, String endDate, String endTime) throws SQLException{
 		String newEvent = "insert into event values(null,?,?,?,?,?,?,?,?,?,?)";
 		PreparedStatement pstmtNewEvent = conn.prepareStatement(newEvent);
 		pstmtNewEvent.setString(1, place);
 		pstmtNewEvent.setString(2, uid);
 		pstmtNewEvent.setString(3, type);
 		pstmtNewEvent.setString(4, description);
 		pstmtNewEvent.setString(5, longitude);
 		pstmtNewEvent.setString(6, latitude);
 		pstmtNewEvent.setString(7, startDate);
 		pstmtNewEvent.setString(8, startTime);
 		pstmtNewEvent.setString(9, endDate);
 		pstmtNewEvent.setString(10, endTime);
 		int success = pstmtNewEvent.executeUpdate();
 		return success;
 	}
 	
 	public boolean updateUserLocation(String uid, String logitude, String latitude) throws SQLException{
 		String updateUserLocation = "update user set longitude=?,latitude=? where uid=?";
 		PreparedStatement pstmtUpdateUserLocation = conn.prepareStatement(updateUserLocation);
 		pstmtUpdateUserLocation.setString(1, logitude);
 		pstmtUpdateUserLocation.setString(2, latitude);
 		pstmtUpdateUserLocation.setString(3, uid);
 		boolean success = pstmtUpdateUserLocation.execute();
 		return success;
 	}
 }
