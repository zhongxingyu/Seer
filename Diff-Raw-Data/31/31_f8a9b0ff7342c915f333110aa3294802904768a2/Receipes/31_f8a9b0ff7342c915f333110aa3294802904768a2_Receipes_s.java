 package dao;
 
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import beans.Receipe;
 import interfaces.Model;
 
 public class Receipes extends DAO {
 
 
 	public Receipes() {
 		this.tableName = "receipes";
 		this.idField = "idreceipes";
 	}
 
 	@Override
 	public Boolean create(Model item) {
 		Receipe r = (Receipe) item;
 		int rs = 0;
 		String query = "";
 		query += "INSERT INTO " + this.tableName+ "( title, sumup, description, imageref, nbofperson, cooktype, cookexpertise, preparationduration) \n";
 		query += "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
 		System.out.println(query);
 		try {
 			stmt = conn.prepareStatement(query);
 			stmt.setString(1, r.getTitle());
 			stmt.setString(2, r.getSumup());
 			stmt.setString(3, r.getDescription());
 			stmt.setString(4, r.getImageref());
 			stmt.setInt(5, r.getNbofperson());
 			stmt.setString(6, r.getCooktype());
 			stmt.setInt(7, r.getCookexpertise());
 			stmt.setInt(8, r.getPreparationduration());
 			rs = stmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 		      try {
 		          if (stmt != null) { stmt.close(); }
 		       }
 		       catch (Exception e) {
 		    	   e.printStackTrace();
 		       }
 		}
 		System.out.println("Rs : " + rs);
 		return (rs == 1);
 	}
 
 	@Override
 	public Boolean update(Model item) {
 		Receipe r = (Receipe) item;
 		int rs = 0;
 		System.out.println(r);
 		String query = "";
 		query += "UPDATE " + this.tableName + "\n";
 		query += "SET title            = ?,\n";
 		query += "sumup                = ?,\n";
 		query += "description          = ?,\n";
 		query += "imageref             = ?,\n";
 		query += "nbofperson           = ?,\n";
 		query += "cooktype             = ?,\n";
 		query += "cookexpertise        = ?,\n";
		query += "preparationduration  = ?,\n";
 		query += "WHERE " + this.idField + " = '" + r.getId() + "'";
 		System.out.println(query);
 		try {
 			stmt = conn.prepareStatement(query);
 			stmt.setString(1, r.getTitle());
 			stmt.setString(2, r.getSumup());
 			stmt.setString(3, r.getDescription());
 			stmt.setString(4, r.getImageref());
 			stmt.setInt(5, r.getNbofperson());
 			stmt.setString(6, r.getCooktype());
 			stmt.setInt(7, r.getCookexpertise());
 			stmt.setInt(8, r.getPreparationduration());
 			rs = stmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 		      try {
 		          if (stmt != null) { stmt.close(); }
 		       }
 		       catch (Exception e) {
 		    	   e.printStackTrace();
 		       }
 		}
 		System.out.println("Rs : " + rs);
 		return (rs == 1);
 	}
 
 	public Receipe getItem(int receipeId) {
 		return this.fromRow(this.read(receipeId));
 	}
 
 	public ArrayList<Model> getItems() {
 		ArrayList<Model> users = new ArrayList<Model>();
 		ArrayList<HashMap<String, String>> ds = this.read();
 		int  nbRow = ds.size();
 		for(int i = 0; i < nbRow; i++) {
 			users.add(this.fromRow(ds.get(i)));
 		}
 		
 		return users;
 	}
 
 	public Boolean delete(Model m) {
 		Receipe r = (Receipe) m;
 		return this.delete(r.getId());
 	}
 	
 	public ArrayList<Receipe> getReceipes(Integer duration, Integer nbPers, Integer exp, String type) {
 		java.sql.ResultSet rs;
 		ResultSetMetaData resultMeta;
 		HashMap<String, String> row = new HashMap<String, String>();
 		ArrayList<Receipe> result = new ArrayList<Receipe>();
 		int nbCol = 0;
 		String query = "SELECT * FROM " + this.tableName + " WHERE preparationduration = ? ";
 		query += "AND nbofperson = ? ";
 		query += "AND cookexpertise = ? ";
 		query += "AND cooktype = ? ";
 		System.out.println(query);
 		try { 
 			stmt = conn.prepareStatement(query);
 			stmt.setInt(2, nbPers);
 			stmt.setString(4, type);
 			stmt.setInt(3, exp);
 			stmt.setInt(1, duration);
 			rs = stmt.executeQuery();
 			resultMeta= rs.getMetaData();
 			nbCol = resultMeta.getColumnCount();
 			
 			while (rs.next()) {
 				for (int i = 1; i <= nbCol; i++) {
 					String val = "";
 					if(rs.getObject(i) != null)
 						val = rs.getObject(i).toString();
 					row.put(resultMeta.getColumnName(i), val);
 				}
 				result.add(this.fromRow(row));
 			}
 		} catch (Exception e) {
 			System.out.println("Oooopps");
 			e.printStackTrace();
 		} finally {
 		      try {
 		          if (stmt != null) { stmt.close(); }
 		       }
 		       catch (Exception e) {
 		    	   e.printStackTrace();
 		       }
 		}
 		return result;
 	}
 	
 	public Receipe fromRow(HashMap<String, String> row) {
 		Receipe r;
 		try {
 			int id = Integer.parseInt(row.get(this.idField));
 			int nbPers = Integer.parseInt(row.get("nbofperson"));
 			int exp = Integer.parseInt(row.get("cookexpertise"));
 			int duration = Integer.parseInt(row.get("preparationduration"));
 			r = new Receipe(id, row.get("title"), row.get("sumup"), row.get("description"), row.get("imageref"), nbPers, row.get("cooktype"), exp, duration);
 		} catch (Exception E) {
 			r = null;
 		}
 		
 		return r;
 	}
 	
 }
