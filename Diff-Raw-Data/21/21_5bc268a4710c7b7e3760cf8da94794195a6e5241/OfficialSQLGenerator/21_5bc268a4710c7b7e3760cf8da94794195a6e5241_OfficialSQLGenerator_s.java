 package codeGenerator;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import jsonObjects.boxScoreObjects.OfficialJson;
 
 public class OfficialSQLGenerator 
 {
 	private ArrayList<OfficialJson> officials;
 	private int gameID;
 	
 	public OfficialSQLGenerator(int gameID, ArrayList<OfficialJson> officials) 
 	{
 		this.gameID = gameID;
 		this.officials = officials;
 	}
 
 	public void compile(String path,
 			String userName, String password)
 	{
 		Connection conn;
 		PreparedStatement stmt;
 		
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(path,userName,password);
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`game_officials` (`game_id`,`official_id`)" +
 					"VALUES (?,?);");
 			
 			for (OfficialJson official : this.officials)
 			{
 				stmt.setInt(1, this.gameID);
 				stmt.setInt(2, official.getOfficialID());
 				stmt.executeUpdate();
 			}
 			
 			
 			stmt.close();
 			conn.close();
 			
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void updateOfficials(ArrayList<OfficialJson> officials, String path,
 			String userName, String password)
 	{
 		Connection conn;
 		PreparedStatement stmt;
 		ResultSet rs;
 		
 		Collections.sort(officials, OfficialJson.COMPARE_BY_ID);
 		ArrayList<OfficialJson> newOfficals = new ArrayList<OfficialJson>(officials);
 		
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(path,userName,password);
 			
 			stmt = conn.prepareStatement("SELECT * FROM `nba2`.`official`");
 			rs = stmt.executeQuery();
 			
 			while(rs.next())
 		    {
				newOfficals.remove(Collections.binarySearch(newOfficals,
		    			new OfficialJson(rs.getInt("official_id"), "", "", ""), OfficialJson.COMPARE_BY_ID));
 		    }
 			
 			for(OfficialJson official : newOfficals)
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`official` (`official_id`,`first_name`,`last_name`," +
 						"`jersey_number`) VALUES (?,?,?,?);");
 			    stmt.setInt(1, official.getOfficialID());
 			    stmt.setString(2, official.getFirstName());
 			    stmt.setString(3, official.getLastName());
 			    stmt.setString(4, official.getJerseyNum());
 			    stmt.executeUpdate();
 			}
 			
 			rs.close();
 			stmt.close();
 			conn.close();
 			
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 }
