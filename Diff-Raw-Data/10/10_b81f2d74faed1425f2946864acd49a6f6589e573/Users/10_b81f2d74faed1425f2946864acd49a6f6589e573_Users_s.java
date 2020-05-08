 package dao;
 
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
import javax.faces.bean.ManagedProperty;

import beans.Session;
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
 		query += "VALUES ('" + u.getFName() + "', '" + u.getLName() + "', '"
 				+ u.getAge() + "', '";
 		query += u.getEmail() + "', '" + u.getLogin() + "', '" + u.getPwd()
 				+ "')";
 		System.out.println(query);
 		try {
 			rs = DB.executeUpdate(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
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
 		query += "SET firstname = '" + u.getFName() + "',\n";
 		query += "lastname  = '" + u.getLName() + "',\n";
 		query += "age       = '" + u.getAge() 	+ "',\n";
 		query += "email     = '" + u.getEmail() + "',\n";
 		query += "login     = '" + u.getLogin() + "',\n";
 		query += "pwd       = '" + u.getPwd() 	+ "'\n";
 		query += "WHERE " + this.idField + " = '" + u.getId() + "'";
 		System.out.println(query);
 		try {
 			rs = DB.executeUpdate(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Rs : " + rs);
 		return (rs == 1);
 	}
 
 	public User getItem(int userId) {
 		HashMap<String, String> dataSet = this.read(userId);
 		int id = Integer.parseInt(dataSet.get("idusers"));
 		int age = Integer.parseInt(dataSet.get("age"));
 		for(String col : dataSet.keySet())
 			System.out.println(col + " " + dataSet.get(col));
 		return new User(id, dataSet.get("login"), dataSet.get("pwd"), dataSet.get("firstname"), dataSet.get("lastname"), age, dataSet.get("email"));
 	}
 
 	public ArrayList<Model> getItems() {
 		ArrayList<Model> users = new ArrayList<Model>();
 		ArrayList<HashMap<String, String>> ds = this.read();
 		int id, age, nbRow = ds.size();
 		for(int i = 0; i < nbRow; i++) {
 			HashMap<String, String> row = ds.get(i);
 			id = Integer.parseInt(row.get("idusers"));
 			age = Integer.parseInt(row.get("age"));
 			users.add(new User(id, row.get("login"), row.get("pwd"), row.get("firstname"), row.get("lastname"), age, row.get("email")));
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
 		}
 		
 		if(row.get("idusers") == null) {
 			return null;
 		}
 		int id, age;
 		id = Integer.parseInt(row.get("idusers"));
 		age = Integer.parseInt(row.get("age"));
 		return new User(id, row.get("login"), row.get("pwd"), row.get("firstname"), row.get("lastname"), age, row.get("email"));
 	}
 	
 	
 }
