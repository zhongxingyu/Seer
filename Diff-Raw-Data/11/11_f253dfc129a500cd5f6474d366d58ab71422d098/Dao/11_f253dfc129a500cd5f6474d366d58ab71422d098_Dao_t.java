 package dao;
 
 import java.sql.*;
 import java.util.*;
 
 import db.Unit;
 import dao.AbstractDao;
 
 public class Dao extends AbstractDao {
 
 	public List<Unit> getAllUnits() throws SQLException {
 		
 		 List<Unit> units = new ArrayList<Unit>(); 
 		  try {
 			  st = getConnection().createStatement(); 
 			  rs = st.executeQuery("SELECT * FROM unit");
 			  
 			  while(rs.next()){
 				  Unit unit = new Unit();
 					unit.setId(rs.getInt(1));
 					unit.setName(rs.getString(2));
 					unit.setCode(rs.getString(3));
 					units.add(unit);
 		        }
 		  } finally {
 			  closeResources(); 
 
 		    }
 		return units; 
 	}
 	
 	public List<Unit> searchUnit(String name) throws SQLException{
 		List<Unit> units = new ArrayList<Unit>();
 		
 		try {
 			pst = getConnection().prepareStatement("SELECT * FROM unit WHERE UPPER(name) LIKE ?");
			pst.setString(1, "%"+name.toUpperCase()+"%");
 			rs = pst.executeQuery();
 			while(rs.next()){
 				Unit unit = new Unit();
 				unit.setId(rs.getInt(1));
				unit.setName(rs.getString(2));
 				unit.setCode(rs.getString(3));
 				units.add(unit);
 			}
 		} finally {
 			closeResources();
 		}
 		
 		return units;
 	}
 
 	public void addUnit(Unit unit) throws SQLException{
 		try {
 			pst = getConnection().prepareStatement("INSERT INTO unit (id, name, code) VALUES (NEXT VALUE FOR seq1, ?, ?)");
 			pst.setString(1, unit.getName());
 			pst.setString(2, unit.getCode());
 			pst.execute();
 		} finally {
 			closeResources();
 		}
 	}
 
 public void deleteList() throws SQLException{
 	try{
 		st = getConnection().createStatement();
 		rs = st.executeQuery("TRUNCATE SCHEMA public and COMMIT");
 	} finally {
 		closeResources();
 	}
 }
 
 public void deleteById(int id) throws SQLException {
 	try {
 		pst = getConnection().prepareStatement("DELETE FROM unit WHERE id = ?;");
 		pst.setInt(1, id);
 		pst.execute();
 		
 	
 	} finally {
 	      closeResources();
 	    }
 }
 
 }
