 package ge.edu.freeuni.restaurant.logic;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 
 public class TableInfo {
 	
 	
 	public ArrayList<Table> getAllTables() throws SQLException{
 		DBConnector dbc = DBConnector.getInstance();
 		ResultSet rs = null;
 		try {
 			rs = dbc.getTables();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ArrayList<Table> list = new ArrayList<Table>();
 		try {
 			while(rs.next()){
 				ResultSet rs2 = null;
 				try {
 					rs2 = dbc.getOccupiedBy(rs.getInt(1));
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				boolean b;
 				String occupant = "";
 				if(rs2.next()){
 					b = true;
					occupant = rs2.getString(2);
 				} else {
 					b = false;
 				}
 				Table t = new Table(rs.getInt(1), rs.getInt(2), rs.getString(3), b, occupant);
 				list.add(t);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 //		list.add(new Table(0, 6, "kvazimodo"));
 //		list.add(new Table(1, 4, "safixvnos tavi"));
 		
 		return list;
 	}
 }
