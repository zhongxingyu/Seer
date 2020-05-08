 package osa3;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 public class DBAccess {
 	static AbstractDAO db = new AbstractDAO() { };
 	
 	public static ArrayList<Unit> getAllUnits() {
 		db.executeQuery("SELECT * FROM unit");
 		
 		return fillUnits(db.rs);
 	}
 	
 	public static ArrayList<Unit> getAllUnitsNameLike(String nameLike) {
		String sqlStatement = "SELECT * FROM unit WHERE UPPER(name) LIKE ?";
 		LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
 		map.put(1, "%" + nameLike.toUpperCase() + "%");
 		db.executeQueryPrep(sqlStatement, map);
 		
 		return fillUnits(db.rs);
 	}
 	
 	public static void addUnit (String name, String code){
 		String sqlStatement = "INSERTINTOunit(id, name,code)VALUES(NEXT VALUE FOR seq1, ?, ?);";
 		LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
 		map.put(1, name);
 		map.put(2, code);
 		db.executeQueryPrep(sqlStatement, map);
 	}
 	
 	public static void delUnit (String id){
 		String sqlStatement = "DELETE FROM unit WHERE id = ?;";
 		LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
 		map.put(1, id);
 		db.executeQueryPrep(sqlStatement, map);
 	}
 	
 	private static ArrayList<Unit> fillUnits (ResultSet rs){
 		ArrayList<Unit> units = new ArrayList<Unit>();
 		try {
 			while(rs.next()){
 				units.add(new Unit(rs.getInt(Config.unitIdColumn), rs.getString(Config.unitNameColumn), rs.getString(Config.unitCodeColumn)));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return units;
 	}
 }
