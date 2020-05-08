 package org.opentox.mysql;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.opentox.pol.OpenssoHelper;
 
 public class MySQL {
 	
 	private Connection conn;
 	
 	public void open() {
 		try {
 			InputStream fis = null;
 			String pw = "";
 			String propfile = "org/opentox/pol/admin.properties";
 			fis = OpenssoHelper.class.getClassLoader().getResourceAsStream(propfile);
 			Properties config = new Properties();
 			try {
 				config.load(fis);
 				pw = config.getProperty("pw");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		    conn = DriverManager.getConnection("jdbc:mysql://localhost/Pol?" +
 		                                   "user=root&password=" + pw);		   
 		} catch (SQLException e) {
 		    // handle any errors
 		    System.out.println("SQLException: " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} 
 	}
 	
 	public void close() {
 		try {
 			conn.close();
 		} catch (SQLException e) {
 			System.out.println("SQLException: " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		}
 	}
 	
 	public void add(String pol, String user, String res) {
 		Statement stmt = null;
 		try {
 		    stmt = conn.createStatement();
 		    stmt.executeUpdate("INSERT INTO pol VALUES ('" + pol + "','" + user + "','" + res + "');");
 		}
 		catch (SQLException ex){
 		    System.out.println("SQLException: " + ex.getMessage());
 		    System.out.println("SQLState: " + ex.getSQLState());
 		    System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		finally {
 		    if (stmt != null) {
 		        try {
 		            stmt.close();
 		        } catch (SQLException sqlEx) { } // ignore
 		        stmt = null;
 		    }
 		}
 		
 	}
 	
 	
 	public void delete_pol(String pol) {
 		Statement stmt = null;
 		try {
 		    stmt = conn.createStatement();
 		    String query = "DELETE FROM pol WHERE pol='"+pol+"';";
 		    stmt.executeUpdate(query);
 		}
 		catch (SQLException ex){
 		    System.out.println("SQLException: " + ex.getMessage());
 		    System.out.println("SQLState: " + ex.getSQLState());
 		    System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		finally {
 		    if (stmt != null) {
 		        try {
 		            stmt.close();
 		        } catch (SQLException sqlEx) { } // ignore
 		        stmt = null;
 		    }
 		}
 		
 	}
 
 	
 	public String search_user_by_pol(String pol) {
 	 	String resres = null;
 	  	ResultSet rs = null;
 	    Statement stat = null;
 		try {
 			stat = conn.createStatement();
 		    rs = stat.executeQuery("SELECT * FROM pol WHERE pol='"+pol+"';");
 		    while (rs.next()) {
 		    	//System.out.println("search_user_by_pol: pol = " + rs.getString("pol"y));
 		    	//System.out.println("search_user_by_pol: user = " + rs.getString("user"));
 		    	//System.out.println("search_user_by_pol: res = " + rs.getString("res"));
 				resres = rs.getString("user");
 				break;
 			}
 		} catch (SQLException e) {
 		    System.out.println("SQLException: " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		}
 		finally {
 		    if (rs != null) {
 		        try {
 		            rs.close();
 		        } catch (SQLException sqlEx) { } // ignore
 			        rs = null;
 		    }
 		    if (stat != null) {
 		        try {
 		            stat.close();
 		        } catch (SQLException sqlEx) { } // ignore
 		        stat = null;
 		    }
 		}
 	    return resres;
 	}
 	
 	// returns true if policy name exists in DB
 	public boolean search_pol_name(String pol)  {
 		boolean res = false;
 		ResultSet rs = null;
 	    Statement stat = null;
 		try {
 			stat = conn.createStatement();
 		    rs = stat.executeQuery("SELECT * FROM pol WHERE pol='"+pol+"';");   
 		    while (rs.next()) {
 		    	res = true;
 		    	break;
 			}
 		} catch (SQLException e) {
 		    System.out.println("SQLException: " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		}
 		finally {
 		    if (rs != null) {
 		        try {
 		            rs.close();
 		        } catch (SQLException sqlEx) { } // ignore
 			        rs = null;
 		    }
 		    if (stat != null) {
 		        try {
 		            stat.close();
 		        } catch (SQLException sqlEx) { } // ignore
 		        stat = null;
 		    }
 		}
 	    return(res);
 	}
 	
 	public List<String> search_pol(String pol) throws Exception {
 		Statement stat = null;
 		ResultSet rs = null;
 		List<String> res = new ArrayList<String>();
 
 		try {
 			stat = conn.createStatement();
 			rs = stat.executeQuery("SELECT * FROM pol WHERE pol='"+pol+"';");
 			while (rs.next()) {
 				res.add(rs.getString("pol") + ";" + rs.getString("user") + ";" + rs.getString("res"));
 			}
 		} catch (SQLException e) {
 			System.out.println("SQLException: " + e.getMessage());
 			System.out.println("SQLState: " + e.getSQLState());
 			System.out.println("VendorError: " + e.getErrorCode());
 		}
 		finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException sqlEx) { } // ignore
 				rs = null;
 			}
 			if (stat != null) {
 				try {
 					stat.close();
 				} catch (SQLException sqlEx) { } // ignore
 				stat = null;
 			}
 		}
 
 		return res;
 
 	}
 	
 	public String search_users_pols(String user) {
 		String res = "";
 		Statement stat = null;
 		ResultSet rs = null;
 		Iterator<String> it = null;
 		HashSet<String> hs = new HashSet<String>();
 
 		try {
 			stat = conn.createStatement();
 			rs = stat.executeQuery("SELECT * FROM pol WHERE user='"+user+"';");
 			while (rs.next()) {
 				String s = rs.getString("pol");
 				if (s!=null) hs.add(s);
 			}
 			it = hs.iterator();
 			while (it.hasNext()) {
 				String s = it.next();
 				if (s != null) res += s + "\n";	
 			}
 		} catch (SQLException e) {
 			System.out.println("SQLException: " + e.getMessage());
 			System.out.println("SQLState: " + e.getSQLState());
 			System.out.println("VendorError: " + e.getErrorCode());
 		}
 		finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException sqlEx) { } // ignore
 				rs = null;
 			}
 			if (stat != null) {
 				try {
 					stat.close();
 				} catch (SQLException sqlEx) { } // ignore
 				stat = null;
 			}
 		}
 		return res;
 	}
 	
 	public String[] search_res(String res) {
 		String[] resres = new String[3];
 		Statement stat = null;
 		ResultSet rs = null;
 
 		try {
 			stat = conn.createStatement();
 		    String query = "SELECT * FROM pol WHERE res='"+res+"';";
 		   	rs = stat.executeQuery(query);
 		   	resres[2]="";
 		    while (rs.next()) {
 		    	resres[0] = rs.getString("res");
 				resres[1] = rs.getString("user");
 				break; // Assumes the same user for a resource name in all records
 			}
 			
 		} catch (SQLException e) {
 			System.out.println("SQLException: " + e.getMessage());
 			System.out.println("SQLState: " + e.getSQLState());
 			System.out.println("VendorError: " + e.getErrorCode());
 		}
 		finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException sqlEx) { } // ignore
 				rs = null;
 			}
 			if (stat != null) {
 				try {
 					stat.close();
 				} catch (SQLException sqlEx) { } // ignore
 				stat = null;
 			}
 		}
 		return resres;
 	}
 	
 	
 	//	get user who created res and all policy names associated with it
 	public String[] search_res_pol(String res) {
 		String[] resres = new String[3];
 		Statement stat = null;
 		ResultSet rs = null;
 
 		try {
 			stat = conn.createStatement();
 		    String query = "SELECT * FROM pol WHERE res='"+res+"';";
 		    //System.out.println(query);
 		   	rs = stat.executeQuery(query);
 		   	int i=0;
 		   	resres[2]="";
 		    while (rs.next()) {
 				if (i==0) { 
 					resres[0] = rs.getString("res");
 					resres[1] = rs.getString("user");
 				}
 				resres[2] += "\n" + rs.getString("pol");
 				i++;
 			}
 			
 		} catch (SQLException e) {
 			System.out.println("SQLException: " + e.getMessage());
 			System.out.println("SQLState: " + e.getSQLState());
 			System.out.println("VendorError: " + e.getErrorCode());
 		}
 		finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException sqlEx) { } // ignore
 				rs = null;
 			}
 			if (stat != null) {
 				try {
 					stat.close();
 				} catch (SQLException sqlEx) { } // ignore
 				stat = null;
 			}
 		}
 		return resres;
 	}
 	
 }
