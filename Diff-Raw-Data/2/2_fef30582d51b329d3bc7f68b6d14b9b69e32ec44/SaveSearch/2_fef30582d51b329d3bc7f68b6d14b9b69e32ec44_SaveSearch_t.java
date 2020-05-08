 package SearchHistory;
 
 import java.sql.*;
 import javax.naming.*;
 import javax.sql.*;
 import java.text.*;
 import java.util.Locale;
 import java.sql.Date;
 
 public class SaveSearch {
 
     	public Connection getDsConnection() {
 		Connection conn = null;
 		try {
 	    		Context ctx = new InitialContext();
 	    		Context envCtx = (Context)ctx.lookup("java:comp/env");
 	    		DataSource ds = (DataSource)envCtx.lookup("jdbc/database");
 	    		conn = ds.getConnection();
 
 		} catch (SQLException se) {
 	    	//Handle Later
 		}
 		catch (NamingException ne) {
 	    	//Handle Later
 		}
 		
 		return conn;
     	}
 
 	public boolean save(int uid, String name, String dob, String phone, String address, String city, String state, String country, String job, String degree, String colleges, int fbActive, String fb, int gpActive, String gp, int twtActive, String twt, int liActive, String li, int igActive, String ig) throws SQLException {
 
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		try {
 			conn = getDsConnection();
 			if(conn == null) {
 				return false;
 			}
 			
 			stmt = conn.prepareStatement("INSERT INTO search (uid,date,name,dob,phone,address,city,state,country,job,degree,colleges,fb,fbActive,gp,gpActive,twt,twtActive,li,liActive,ig,igActive) VALUES (?,date('now'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 			stmt.setInt(1,uid);
 			stmt.setString(2,name);
 			stmt.setString(3,dob);
 			stmt.setString(4,phone);
 			stmt.setString(5,address);
 			stmt.setString(6,city);
 			stmt.setString(7,state);
 			stmt.setString(8,country);
 			stmt.setString(9,job);
 			stmt.setString(10,degree);
 			stmt.setString(11,colleges);
 			stmt.setString(12,fb);
 			stmt.setInt(13,fbActive);
 			stmt.setString(14,gp);
 			stmt.setInt(15,gpActive);
 			stmt.setString(16,twt);
 			stmt.setInt(17,twtActive);
 			stmt.setString(18,li);
 			stmt.setInt(19,liActive);
 			stmt.setString(20,ig);
 			stmt.setInt(21,igActive);
 
 			stmt.executeUpdate();
 
			//stmt.close();
 			conn.close();
 			return true;
 		} catch(SQLException e) {
 			throw e;
 		} catch(Exception e){
 			return false;
 		}
 	}
 }
 
