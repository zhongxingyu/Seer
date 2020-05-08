 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import com.mysql.*;
 import com.mysql.jdbc.Connection;
 
 
 public class Guest {
 	public static void add(Connection conn, String name, String address) {
 		try {
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT guestID FROM guest ORDER BY guestID DESC LIMIT 1");
 			ResultSet rs = ps.executeQuery();
 
 			int lastGuestID = 1;
 			if(rs.next()) {
 			  lastGuestID = Integer.parseInt(rs.getString(1));
 			}
 			
 			Statement s = conn.createStatement();
 			s.executeUpdate("INSERT INTO guest (guestID, guestAddress, guestName) " +
					  "VALUES ('lastGuestID', 'address', 'name')");	
 		} catch (SQLException e) {
 			System.out.println("Error: Could not add guest.");
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Successfully added guest.");
 	}
 	
	public static void update(){
		
 	}
 	
	public static void delete(){
		
 	}	
 }
