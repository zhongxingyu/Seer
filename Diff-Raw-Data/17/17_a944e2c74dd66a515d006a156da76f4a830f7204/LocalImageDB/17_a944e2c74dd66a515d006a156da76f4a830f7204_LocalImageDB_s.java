 package cg3204l;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class LocalImageDB {
 	
 	protected Connection mConnection;
 
 	public LocalImageDB() throws ClassNotFoundException {
 		// load the sqlite-JDBC driver using the current class loader
 	    Class.forName("org.sqlite.JDBC");
 
 	    mConnection = null;
 	    
 	    try
 	    {
 	      // create a database connection
 	      mConnection = DriverManager.getConnection("jdbc:sqlite:image.db");
 	    }
 	    catch(SQLException e)
 	    {
 	      // if the error message is "out of memory", 
 	      // it probably means no database file is found
 	      System.err.println(e.getMessage());
 	    }
	    finally
	    {
	      try
	      {
	        if(mConnection != null)
	          mConnection.close();
	      }
	      catch(SQLException e)
	      {
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
 	}
 	
 	public List<String> search(String keyword, Date date) {
 		List<String> urlList = new ArrayList<String>();
 		
 		try {
 			Statement statement = mConnection.createStatement();
 			ResultSet rs = statement
 					.executeQuery("select * from image where keyword like '%" 
 							+ keyword + "%'");
 			
 			while (rs.next()) {
 				urlList.add(rs.getString("url"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return urlList;
 	}
 
 	public void insert(List<String> urlList, String keyword) {
 		
 		try {
 			Statement statement = mConnection.createStatement();
 			
 			for (String url : urlList) {
				statement.executeUpdate("insert into image values (" + url + "," + keyword + ")");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
