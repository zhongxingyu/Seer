 package at.ac.tuwien.secsyseng.sqlinjection.service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import at.ac.tuwien.secsyseng.sqlinjection.dao.Var3Dao;
 
 public class Var3Service {
 
 public Var3Service () {}
 	
 	/**
 	 * Returns true if inserted or false if not
 	 * @param id
 	 * @param name
 	 * @param secure
 	 * @return
 	 * @throws SQLException
 	 */
 	public int transferVar3Response (int id, String name, boolean secure) throws SQLException {
 		Var3Dao var3dao = new Var3Dao();
 		int  rs;
 		
 		if (secure) {
 			rs = var3dao.Var3InsertSecure(id, name);
 			
 		}
 		else {
 			rs = var3dao.Var3InsertUnsecure(id, name);
 			
 		}
 		
 		return rs;
 	}
 	/**
 	 * Returns the Data in aresultset
 	 * @return
 	 * @throws SQLException
 	 */
 	public ResultSet getvar3Data () throws SQLException {
 		Var3Dao var3dao = new Var3Dao();
 		return var3dao.getData();
 	}
 }
