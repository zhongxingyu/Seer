 package dao;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import beans.User;
 import interfaces.Model;
 
 public class Users extends DAO {
 
 	/*
 	 * `idusers` INTEGER(11) NOT NULL AUTO_INCREMENT, 
 	 * `firstname` VARCHAR(45) COLLATE latin1_swedish_ci DEFAULT NULL, 
 	 * `lastname` VARCHAR(45) COLLATE latin1_swedish_ci NOT NULL, 
 	 * `age` INTEGER(11) DEFAULT NULL, 
 	 * `email` VARCHAR(100) COLLATE latin1_swedish_ci NOT NULL, 
 	 * `login` VARCHAR(45) COLLATE latin1_swedish_ci NOT NULL, 
 	 * `pwd` VARCHAR(45) COLLATE latin1_swedish_ci DEFAULT NULL,
 	 */
 
 	public Users() {
 		this.tableName = "users";
 		this.idField = "idusers";
 	}
 
 	@Override
 	public Boolean create(Model item) {
 		User u = (User) item;
 		int rs = 0;
 		String query = "";
 		
 		query += "INSERT INTO " + this.tableName
 				+ "( firstname, lastname, age, email, login, pwd) \n";
		query += "VALUES ( ?, ?, ?, ? ,?)";
 		System.out.println(query);
 		
 		try {
 			stmt = conn.prepareStatement(query);
 			stmt.setString(1, u.getFName());
 			stmt.setString(2, u.getLName());
 			stmt.setInt(3, u.getAge());
 			stmt.setString(4, u.getEmail());
 			stmt.setString(5, u.getLogin());
 			stmt.setString(6, u.getPwd());
 			rs = stmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
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
 		User u = (User) item;
 		int rs = 0;
 		System.out.println(u);
 		String query = "";
 		query += "UPDATE " + this.tableName + "\n";
 		query += "SET firstname = ?,\n";
 		query += "lastname  = ?,\n";
 		query += "age       = ?,\n";
 		query += "email     = ?,\n";
 		query += "login     = ?,\n";
 		query += "pwd       = ?\n";
 		query += "WHERE " + this.idField + " = '" + u.getId() + "'";
 		System.out.println(query);
 		try {
 			stmt = conn.prepareStatement(query);
 			stmt.setString(1, u.getFName());
 			stmt.setString(2, u.getLName());
 			stmt.setInt(3, u.getAge());
 			stmt.setString(4, u.getEmail());
 			stmt.setString(5, u.getLogin());
 			stmt.setString(6, u.getPwd());
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
 
 	public User getItem(int userId) {
 		return this.fromRow(this.read(userId));
 	}
 
 	public ArrayList<Model> getItems() {
 		ArrayList<Model> users = new ArrayList<Model>();
 		ArrayList<HashMap<String, String>> ds = this.read();
 		int nbRow = ds.size();
 		for(int i = 0; i < nbRow; i++) {
 			users.add(this.fromRow(ds.get(i)));
 		}
 		
 		return users;
 	}
 
 	public Boolean delete(Model m) {
 		User u = (User)m;
 		return this.delete(u.getId());
 	}
 	
 	public User getUser(String login, String pwd) {
 		java.sql.ResultSet rs;
 		ResultSetMetaData resultMeta;
 		HashMap<String, String> row = new HashMap<String, String>();
 		int nbCol = 0;
 		String query = "SELECT * FROM " + this.tableName + " WHERE login = '" + login + "' AND pwd = '" + pwd + "' LIMIT 1";
 		//System.out.println(query);
 		try { 
 			rs = DB.executeQuery(query);
 			resultMeta= rs.getMetaData();
 			nbCol = resultMeta.getColumnCount();
 			
 			while (rs.next()) {
 				for (int i = 1; i <= nbCol; i++) {
 					String val = "";
 					if(rs.getObject(i) != null)
 						val = rs.getObject(i).toString();
 					row.put(resultMeta.getColumnName(i), val);
 				}
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
 		
 		if(row.get("idusers") == null) {
 			return null;
 		}
 		return this.fromRow(row);
 	}
 
 	public User fromRow(HashMap<String, String> row) {
 		int id = Integer.parseInt(row.get("idusers"));
 		int age = Integer.parseInt(row.get("age"));
 		return new User(id, row.get("login"), row.get("pwd"), row.get("firstname"), row.get("lastname"), age, row.get("email"));
 	}
 	
 	
 }
