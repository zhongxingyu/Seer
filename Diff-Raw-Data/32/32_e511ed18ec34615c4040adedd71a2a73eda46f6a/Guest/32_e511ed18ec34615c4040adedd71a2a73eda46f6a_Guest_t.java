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
					        "VALUES (" + lastGuestID + ", '" + address + "', '" + name + "')");	
 		} catch (SQLException e) {
 			System.out.println("Error: Could not add guest.");
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Successfully added guest.");
 	}
 	
	public static void update(Connection conn, String id, String name, String address){
		try {			
			Statement s = conn.createStatement();
			s.executeUpdate("UPDATE guest SET " +
							"guestName = '" + name + "', " +
					        "guestAddress = '" + address + "' " +
					  		"WHERE guestID = " + id);	
		} catch (SQLException e) {
			System.out.println("Error: Could not update guest.");
			e.printStackTrace();
			return;
		}
		System.out.println("Successfully updated guest.");
 	}
 	
	public static void delete(Connection conn, String id){
		try {			
			Statement s = conn.createStatement();
			s.executeUpdate("DELETE FROM guest " +
					  		"WHERE guestID = " + id);	
		} catch (SQLException e) {
			System.out.println("Error: Could not delete guest.");
			e.printStackTrace();
			return;
		}
		System.out.println("Successfully deleted guest.");
 	}	
 }
