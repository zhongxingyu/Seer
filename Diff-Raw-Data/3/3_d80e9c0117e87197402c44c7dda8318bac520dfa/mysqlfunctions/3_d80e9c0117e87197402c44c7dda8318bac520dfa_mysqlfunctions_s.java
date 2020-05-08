  package com.htmlthor;
 
 import java.io.*;
 import java.util.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /*
 
 */
 public class Mysqlfunctions {
 
 	//Nested Class for DB Connect
 	private class ConnectDB {
 	
 			ResultSet result = null;
 			Connection con = null;
 			
 			public ConnectDB() {
 			
 			}
 	
 			public ResultSet run(String Q) {
 
 				String url = "htmlthor.com";
 					
 				try {
 					Class.forName("com.mysql.jdbc.Driver");
 				} catch (Exception ex) {
 					
 				}
 				
 				try {
 					con = DriverManager.getConnection("jdbc:mysql://htmlthor.com/htmlthor_db?" + "user=htmlthor_udb&password=test1");
 					Statement stmt = con.createStatement();
 					result = stmt.executeQuery(Q);
 				
 				} catch (SQLException ex) {
 					System.out.println("SQLException: " + ex.getMessage());
 					System.out.println("SQLState: " + ex.getSQLState());
 					System.out.println("VendorError: " + ex.getErrorCode());
 				}
 				return result;
 			}
 			
 			
 			public void close() {
 			
 				try {
 					con.close();
 				} catch (SQLException ex) {
 					System.out.println("SQLException: " + ex.getMessage());
 					System.out.println("SQLState: " + ex.getSQLState());
 					System.out.println("VendorError: " + ex.getErrorCode());
 				}
 			
 				return;
 			}
 	}
 
 	
 
 
 	public String getErrMsg(int eID) {
 		String msg = null;
 		
 		String query = new StringBuilder("SELECT * FROM Error WHERE eID = '").append(eID).append("'").toString();
 		
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return "No message";
 		}
 		try {
 			if (result.next()) {
 				msg = result.getString("eMessage");
 			}
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		
 	   con.close();
 	   
 	   return msg;
 	}
 
 
 	//Options for tbl are - dep,elem,att (deprecated element and attribte repesctively)
 	public List<String> getDBanswer(String tbl, String tag) {
 		List<String> list = new ArrayList<String>();
 		
 		if (tbl.equalsIgnoreCase("dep")) {
 		
 			String query = new StringBuilder("SELECT * FROM Deprecated WHERE depTag = ").append(tag).toString();
 			ConnectDB con = new ConnectDB();
 			ResultSet result = con.run(query);
 
 			try {
 				while(result.next())
 				{
 				 list.add(result.getString("depTag"));
 				 list.add(result.getString("eID"));
 				} 
 			} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			}
 			
 			con.close();
 			
 		} else if  (tbl.equalsIgnoreCase("elem")) {
 		
 			
 			String query = new StringBuilder("SELECT * FROM Element WHERE EName = ").append(tag).toString();
 			ConnectDB con = new ConnectDB();
 			ResultSet result = con.run(query);
 
 			
 			try {
 				while(result.next())
 				{
 				 list.add(result.getString("EName"));
 				 list.add(result.getString("IsDeprecated"));
 				 list.add(result.getString("CanSelfClose"));
 				 list.add(result.getString("IsFormElem"));
 				 list.add(result.getString("IsSingular"));
 				}
 			} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 			}
 			
 			con.close();
 		
 		} else if  (tbl.equalsIgnoreCase("att")) {
 		
 			String query = new StringBuilder("SELECT * FROM RequiredAttributes WHERE EName = ").append(tag).toString();
 			ConnectDB con = new ConnectDB();
 			ResultSet result = con.run(query);
 			
 			try {
 				while(result.next())
 				{
 				 list.add(result.getString("EName"));
 				 list.add(result.getString("AttributeName"));
 				} 
 			} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 			}
 			
 			con.close();
 			
 		} else {
 			//exit with not a valid table
 		}
 		return (ArrayList)list; 
 	}
 
 	//Returns a list of Arrays - get deprecated tags * NOTE!
 	public ArrayList<String> getTags() {
 		
 		List<String> list = new ArrayList<String>();
 		
 		String query = "SELECT * FROM Element";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return (ArrayList<String>) list;
 		}
 		
 		try {
 			while (result.next()) {
 				list.add(result.getString("eName"));
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		con.close();
 		
 		return (ArrayList<String>) list;
 	}
 
 	//Return true or false for deprecated tag or not
 	public boolean isDeprecated(String tagName) {
 		Boolean msg = false;
 		
 		String query = "SELECT * FROM Element WHERE EName = '" + tagName + "'";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return false;
 		}
 		
 		try {
 			if (result.next()) {
 				if (result.getInt("IsDeprecated") == 0) {
 					msg = true;
 				}
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();	
 
 	   return msg;
 	}
 	
 	
 	//Return true or false for deprecated attribute or not
 	public boolean isDeprecatedAttribute(String attName, String tagName) {
 		Boolean msg = false;
 		
 		String query = "SELECT eID FROM Element WHERE EName = '" + tagName + "'";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		int eID = 0;
 		
 		if (result != null) {
 			
 		
 			try {
 		
 				while (result.next()) {
 					eID = result.getInt("eID");
 				}
 		
 			}catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 			}
 		}
 		query = "SELECT * FROM Attribute WHERE (eID = " + Integer.toString(eID) + " OR isGlobal = 1) AND Name = '" + attName + "'";
 		result = con.run(query);
 		
 		if (result == null) {
 			return false;
 		}
 		
 		try {
 			if (result.next()) {
 				if (result.getInt("isDeprecated") == 1) {
 					msg = true;
 				}
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();
 		return msg;
 	}
 
 	//Returns true if tag requires an Attribute
 	public List<String> requiresAttr(String tagName) {
 		List<String> DBRequires = new ArrayList<String>();
 		
		String query = new StringBuilder("SELECT * FROM Attribute WHERE isRequired = 1 AND eID = (SELECT eID FROM Element WHERE Ename =" ).append(tagName).toString() +(")");
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return DBRequires;
 		}
 		
 		try {
 			while(result.next()) {
 				if (result.getString("Name") != null) {
 					DBRequires.add(result.getString("Name"));
 				}
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();	
 		return DBRequires;
 	}
 
 	//Returns a list of all Attribtes for a tagName
 	public ArrayList<String> getAttr(String tagName) {
 		List<String> list = new ArrayList<String>();
 		String query = "SELECT * FROM Attribute WHERE eID = (SELECT eID FROM Element WHERE EName = '"+tagName+"') OR isGlobal=1";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 				
 		if (result == null) {
 			return (ArrayList<String>) list;
 		}
 		try {
 		
 			while (result.next()) {
 				list.add(result.getString("Name"));
 			}
 		
 		}catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();	
 		return (ArrayList<String>) list;
 	}
 
 
 	//If a tag exists
 	public boolean checkValidTag(String tagName) {
 		List<String> list = new ArrayList<String>();
 		list = getTags();
 		
 		for (int i=0;i<list.size();i++) {
 			if (list.get(i).equalsIgnoreCase(tagName)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public boolean isSelfClosing(String tagName) {
 		boolean msg = false;
 		
 		String query = "SELECT * FROM Element WHERE EName = '" + tagName + "'";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return false;
 		}
 		
 		try {
 			if (result.next()) {
 				if (result.getInt("CanSelfClose") == 1) {
 					return true;
 				}
 			} 
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 				
 			
 
 		con.close();
 
 		return msg;
 	}
 	
 	/*
 	 * Checks whether an element is a metadata/script element.
 	 * Needs data to be added to database tables before it can be coded.
 	 * 
 	 * UNIMPLEMENTED
 	 */
 	public boolean isMeta(String tagName) {
 		
 		Boolean msg = false;
 		
 		String query = "SELECT * FROM Element WHERE EName = '" + tagName + "'";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return false;
 		}
 		
 		try {
 			if (result.next()) {
 				if (result.getInt("IsMeta") == 1) {
 					msg = true;
 				}
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();
 	
 		return msg;
 	}
 	
 	/*
 	 * Checks whether an element is a table element.
 	 * 
 	 * UNIMPLEMENTED
 	 */
 	public boolean isTableElement(String tagName) {
 		Boolean msg = false;
 		
 		String query = "SELECT * FROM Element WHERE EName = '" + tagName + "'";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return false;
 		}
 		
 		try {
 			if (result.next()) {
 				if (result.getInt("IsTableElement") == 1) {
 					msg = true;
 				}
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();	
 
 		return msg;
 	}
 	
 	/*
 	 * Checks whether an element is a form element.
 	 * 
 	 * UNIMPLEMENTED
 	 */
 	public boolean isFormElement(String tagName) {
 		Boolean msg = false;
 		
 		String query = "SELECT * FROM Element WHERE EName = '" + tagName + "'";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return false;
 		}
 		
 		try {
 			if (result.next()) {
 				if (result.getInt("IsFormElement") == 1) {
 					msg = true;
 				}
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();	
 		
 		return msg;
 	}
 	
 	/*
 	 * Checks whether an Attribte is, isBoolean
 	 * 
 	 * Returns 1 for yes and 0 for no
 	 */
 	public boolean isAttrBool(String attributeName) {
 		Boolean msg = false;
 		
 		String query = "SELECT * FROM Attribute WHERE Name = '"+attributeName+"'";
 		ConnectDB con = new ConnectDB();
 		ResultSet result = con.run(query);
 		
 		if (result == null) {
 			return false;
 		}
 		
 		try {
 			if (result.next()) {
 				if (result.getInt("IsBoolean") == 1) {
 					msg = true;
 				}
 			}
 		} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		con.close();	
 
 		return msg;
 	}
 	
 }
