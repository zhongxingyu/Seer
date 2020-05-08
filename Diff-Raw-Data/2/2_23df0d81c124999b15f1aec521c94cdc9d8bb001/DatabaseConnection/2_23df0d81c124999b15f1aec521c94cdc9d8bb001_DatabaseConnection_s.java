 package com.baggers.bagboy;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 public class DatabaseConnection {
 
 	public DatabaseConnection() {
 		
 	}
 	
 	public boolean checkLogin(String email, String password) {
 		//db code to check if the email and password are in the database
 		if (email == null || password == null)
 			return false;
 		Connection c = null;
 		ResultSet rs = null;
 		PreparedStatement pst = null;
 		try {
 			Class.forName("org.postgresql.Driver");
 			c = DriverManager.getConnection(
 					"jdbc:postgresql://128.61.57.241/:5432/bagboy", "postgres",
 					"australia3");
 			pst = c.prepareStatement("SELECT * FROM USERS ;");
 			rs = pst.executeQuery();
 			while (rs.next()) {
 				if (rs.getString("user_email").equals(email)
 						&& rs.getString("password").equals(password)) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		// //////end of db code
		//if the user is in the database return true, else return false
 
 	}
 	
 	public void registerUser(String email, String password) {
 		
 		//put in a new user with that email and password
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			c = DriverManager.getConnection(
 					"jdbc:postgresql://128.61.57.241:5432/bagboy", "postgres",
 					"australia3");
 			stmt = c.createStatement();
 			String sql = "insert into users (user_id, user_email, password)"
 					+ "values (DEFAULT,'" + email + "','" + password + "');";
 			stmt.execute(sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean checkEmail(String email) {
 		//check to see if that email is already in the database
 		//return true if it is
 		Connection c = null;
 		ResultSet rs = null;
 		PreparedStatement pst = null;
 		try {
 			Class.forName("org.postgresql.Driver");
 			c = DriverManager.getConnection(
 					"jdbc:postgresql://128.61.57.241:5432/bagboy", "postgres",
 					"australia3");
 			pst = c.prepareStatement("SELECT * FROM USERS ;");
 			rs = pst.executeQuery();
 			while (rs.next()) {
 				if (rs.getString("user_email").equals(email)) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	 
 	public void createList(String currUser, String listName) {
 		//db code to create a new list in the list table
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			c = DriverManager.getConnection(
 					"jdbc:postgresql://128.61.57.241:5432/bagboy", "postgres",
 					"australia3");
 			stmt = c.createStatement();
 			String sql = "insert into lists (list_id, list_name, user_email)"
 					+ "values (3,'" + listName + "','" + currUser + "');";
 			stmt.execute(sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void addToList(String list, String newProduct) {
 		//db code to add new product the list 
 		ResultSet rs2 = null;
 		ResultSet rs3 = null;
 		PreparedStatement pst2 = null;
 		PreparedStatement pst3 = null;
 		int productId = 0;
 		int listId = 0;
 		Statement stmt2 = null;
 		
 //		try {
 //			Class.forName("org.postgresql.Driver");
 //			c = DriverManager.getConnection(
 //					"jdbc:postgresql://localhost:5432/bagboy", "postgres",
 //					"australia3");
 //			pst2 = c.prepareStatement("select product_id from products where product_name = '"
 //					+ prod + "');");
 //			rs2 = pst2.executeQuery();
 //			while (rs2.next()) {
 //				if (rs2.getString("product_name").equals(prod)) {
 //					productId = rs2.getInt("product_id");
 //					break;
 //				}
 //			}
 //			pst3 = c.prepareStatement("select product_id from products where product_name = '"
 //					+ prod + "');");
 //			rs3 = pst3.executeQuery();
 //			while (rs3.next()) {
 //				if (rs3.getString("product_name").equals(prod)) {
 //					productId = rs3.getInt("product_id");
 //					break;
 //				}
 //			}
 //			
 //			stmt2 = c.createStatement();
 //			String sql2 = "insert into lists (list_id, list_name, user_email)"
 //					+ "values ("+listId+"'"+list+"','" +currUser+"');";
 //			stmt2.execute(sql2);
 //			
 //			
 //		} catch (SQLException e) {
 //			e.printStackTrace();
 //		} catch (ClassNotFoundException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 	}
 	
 	public ArrayList<String> loadLists (String username) {
 		ArrayList<String> lists = new ArrayList<String>();
 		//db code to get all the list names associated with a username
 		return lists;
 	}
 	
 	public ArrayList<String> loadStores() {
 		ArrayList<String> stores = new ArrayList<String>();
 		//db code to get all the stores
 		stores.add("Kroger");
 		stores.add("Publix");
 		return stores;
 	}
 	
 	public static ArrayList<String> loadCategories() {
 		ArrayList<String> categories = new ArrayList<String>();
 		//db code to return an array list of all categories
 		categories.add("cookies");
 		categories.add("chips");
 		categories.add("soda");
 		return categories;
 	}
 	
 	public static ArrayList<String> loadItemsFromCategory (String categoryName) {
 		ArrayList<String> items = new ArrayList<String>();
 		//db code to return an array list of all items in a category
 		
 		return items;
 	}
 	
 	public static int getAisle (String category) {
 		//db code to get the aisle of a category
 		//maybe with a given store - need to figure this out 
 	
 		return 0;
 	}
 	
 	public static boolean isCold (String productName) {
 		
 		//db code to see whether a product is cold 
 		return false;
 	}
 	
 	public static ArrayList<String> loadItemsFromList (String listName) {
 		ArrayList<String> items = new ArrayList<String>();
 		//db code to return an array list of all items in a list
 		
 		return items;
 	}
 }
