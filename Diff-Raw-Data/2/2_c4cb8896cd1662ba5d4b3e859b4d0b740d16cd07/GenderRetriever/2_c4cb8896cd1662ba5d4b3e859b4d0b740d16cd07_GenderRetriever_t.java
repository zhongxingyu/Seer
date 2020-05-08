 package BothellBirder;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 
 public class GenderRetriever 
 {
 	private LinkedHashMap<String, String> gender = new LinkedHashMap<String, String>();
 	
 	public GenderRetriever() 
 	{
 	}
 	
 	public LinkedHashMap<String, String> getGender(int id) throws SQLException
 	{
 		Connection conn = SimpleDataSource.getconnection();
         Statement stat = null;
         stat = conn.createStatement();
         String query = "SELECT [gender], [uniqueBirdName] FROM" 
                 + " BirdDatabase.dbo.Gender where uniqueBirdId = '" + id + "'";  //get database table
         ResultSet rs = null;
         rs = stat.executeQuery(query);
         int i = 0; 
         while(rs.next())
         {
             i++;
         }        
         rs = stat.executeQuery(query);
         rs.next();
         for(int j= 0; j < i; j++)//populate arraylist
         {    
         	String query2 = "SELECT [name] FROM" 
                     + " BirdDatabase.dbo.name where nameId = '" + 
         			rs.getInt("uniqueBirdName") + "'";
         	ResultSet a = stat.executeQuery(query2);
         	a.next();
         	gender.put(a.getString("name"), rs.getString("gender"));
             rs.next();
         }
         conn.close();
      return gender;
 	}
 	
	public int getNameId(int id, String bName) throws SQLException
 	{
 		Connection conn = SimpleDataSource.getconnection();
         Statement stat = null;
         stat = conn.createStatement();
         String query = "SELECT [uniqueBirdName] FROM" 
                 + " BirdDatabase.dbo.Gender where uniqueBirdId = '" + id + "', name = '"
                 + bName + "'";  //get database table
         ResultSet rs = stat.executeQuery(query);
         rs.next();
         return rs.getInt("uniqueBirdName");
 	}
 }
 
