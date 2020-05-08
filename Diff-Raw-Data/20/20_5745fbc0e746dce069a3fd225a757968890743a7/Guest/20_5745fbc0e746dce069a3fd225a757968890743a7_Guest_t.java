 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import com.mysql.jdbc.Connection;
 
 public class Guest {
 	public static void add(Connection conn, String name, String address) {
 		try {
 			// get the last guest ID from the table
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT guestID FROM guest ORDER BY guestID DESC LIMIT 1");
 			ResultSet rs = ps.executeQuery();
 
 			int lastGuestID = 1; // if there are no guests in the table
 			// if there are guests in the table,
 			if(rs.next()) {
 				// Increment
 				lastGuestID = Integer.parseInt(rs.getString(1)) + 1;
 			} 
 			
 			// Insert guest into table
 			Statement s = conn.createStatement();
 			s.executeUpdate("INSERT INTO guest (guestID, guestAddress, guestName) " +
 					        "VALUES ('" + formatID(lastGuestID) + "', '" + address + "', '" + name + "')");	
 		} catch (SQLException e) {
 			System.out.println("Error: An unexpected error occurred, and the guest could not be added. " +
 		                       "Please try again.");
 			return;
 		}
 		System.out.println("Successfully added guest.");
 	}
 	
 	public static void update(Connection conn, String id, String name, String address){
 		try {			
 			Statement s = conn.createStatement();
 			
 			String sql = "UPDATE guest SET ";
 			// if agent does not want to update anything....
 			if (name == null && address == null) {
 				return; //do nothing
 			// if agent only wants to update address,
 			} else if (name == null) {
 				sql = sql + "guestAddress = '" + address + "' ";
 			// if agent only wants to update name,
 			} else if (address == null) {
 				sql = sql + "guestName = '" + name + "' ";
 			// if agent wants to update both,
 			} else {
 				sql = sql + "guestName = '" + name + "', " +
 				        	"guestAddress = '" + address + "' ";
 			}
 			sql = sql + "WHERE guestID = '" + 
 			  			formatID(Integer.parseInt(id)) + "'";
 			
 			s.executeUpdate(sql);
 		} catch (Exception e) {
			System.out.println("Error: An unexpected error occurred, and guest could not be updated. Please try again.");
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Successfully updated guest.");
 	}
 	
 	public static void delete(Connection conn, String id){
 		try {			
 			Statement s = conn.createStatement();
 			s.executeUpdate("DELETE FROM guest " +
 					  		"WHERE guestID = '" + 
 					        formatID(Integer.parseInt(id)) + "'"
 					       );	
 		} catch (SQLException e) {
			System.out.println("Error: An unexpected error occurred, and the guest could not be deleted. Please try again.");
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Successfully deleted guest.");
 	}	
 	
 // ------------------------ FORMAT
 	public static String formatID(int id) {
 		String idStr = "";
 		
 		int first = (int)(id/1000)*1000;
 		int second = (int)((id - first)/100)*100;
 		int third = (int)((id - first - second)/10)*10;
 		int fourth = (int)(id - first - second - third);
 		
 		if ((int)(id/1000) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (id/1000);
 		}
 		
 		if ((int)((id - first)/100) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (int)((id - first)/100);
 		}
 		
 		if ((int)((id - first - second)/10) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (int)((id - first - second)/10);
 		}
 		
 		if ((int)(id - first - second - third) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (int)(id - first - second - third);
 		}
 		
 		return idStr;
 	}
 	
 // ------------------------ VALIDATION
 	public static boolean isNameValid(String name) {
 		// make sure name is not empty or more than 30 characters
 		if (name.trim().length() == 0 || name.trim().length() > 30) {
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean isNameLengthValid(String name) {
 		// make sure name is not empty or more than 30 characters
 		if (name.trim().length() > 30) {
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean isGuestIdValid(Connection conn, String id) {
 		try {
 			// make sure id is an integer
 			int guestID = Integer.parseInt(id);
 
 			// make sure guest id is a 4 digit number
 			if ((int)(guestID / 10000) != 0) {
 				return false;
 			}
 						
 			// get the last guest ID from the table
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT * FROM guest WHERE guestID = '" + formatID(guestID) + "'");
 			ResultSet rs = ps.executeQuery();
 			
 			// if id exists in database,
 			if (rs.next()) {
 				return true;
 			} else { // if id does not exist in database,
 				return false;
 			}
 		} catch (Exception e) {
			System.out.println("Error: An unexpected error occurred. Please try again.");
 			return false;
 		}
 	}
 }
