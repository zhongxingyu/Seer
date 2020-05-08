 package BothellBirder;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 public class BirdNameRetriever 
 {
 	private ArrayList<BirdName> bird;
     /**
      * reads data from an s.q.l server and creates an array list of 
      * Bird objects
      * @return list of birds
      * @throws SQLException 
      */    
 	public ArrayList<BirdName> readData() throws SQLException
 	{
         Connection conn = SimpleDataSource.getconnection();
         Statement stat = null;
         stat = conn.createStatement();
         String query = "SELECT [nameID], [name], [uniqueBirdID] FROM" 
                 + " BirdDatabase.dbo.name";  //get database table
         ResultSet rs = null;
         rs = stat.executeQuery(query);
         int i = 0; 
         while(rs.next())
         {
             i++;
         }
         bird = new ArrayList<BirdName>();
         
         rs = stat.executeQuery(query);
         rs.next();
         for(int j= 0; j < i; j++)//populate arraylist
         {    
         	BirdName aBirdName = new BirdName(rs.getString("Name"), 
             rs.getInt("nameID"), rs.getInt("uniqueBirdID"));
         	bird.add(aBirdName);
             rs.next();
         }
         conn.close();
      return bird;
 	}
 	
 	public ArrayList<BirdName> updateData(int feature, int features) throws SQLException
 	{
         Connection conn = SimpleDataSource.getconnection();
         Statement stat = null;
         stat = conn.createStatement();
         String featured = "";
         boolean fakeOut = false;
         String featuredA = "UniqueFamilyID";
         switch(feature)
         {
         case 0:
         	fakeOut = true;
         	featured = "BirdFamilies";
         	break;
         case 1:
         	featured = "BirdSecondaryColors";
         	break;
         case 2:
         	featured = "BirdPrimaryColor";
         	break;
         case 3:
         	featured = "BirdFeederFrequency";
         	break;
         case 4:
         	featured = "BirdHabitat";
         	break;
         case 5: 
         	featured = "BirdConservationStatus";
         	break;
         case 6:
         	featured = "BirdSize";
         	break;
         case 7:
         	featured = "BirdLocation";
         	break;
         }
         String query = "SELECT * FROM BirdDatabase.dbo.name JOIN ON" + 
         "BirdDatabase.dbo." + featured +  "BirdDatabase.dbo.name.uniqueBirdID"
         + "= BirdDatabase.dbo." + featured + ".uniqueBirdID WHERE BirdDatabase.dbo." + featured + "." + 
         featured;  //get database table
         if(fakeOut)
         	query = "SELECT * FROM BirdDatabase.dbo.name JOIN ON" + 
         	        "BirdDatabase.dbo." + featured +  "BirdDatabase.dbo.name.uniqueBirdID"
         	        + "= BirdDatabase.dbo." + featured + ".uniqueBirdID WHERE BirdDatabase.dbo." + featured + "." + 
         	        featuredA;  //get database table
         ResultSet rs = null;
         rs = stat.executeQuery(query);
         int i = 0; 
         while(rs.next())
         {
             i++;
         }
         bird = new ArrayList<BirdName>();
         
         rs = stat.executeQuery(query);
         rs.next();
         for(int j= 0; j < i; j++)//populate arraylist
         {    
         	BirdName aBirdName = new BirdName(rs.getString("Name"), 
             rs.getInt("nameID"), rs.getInt("uniqueBirdID"));
         	bird.add(aBirdName);
             rs.next();
         }
         conn.close();
      return bird;
 	}
 	
 	public String getScientificName(int id) throws SQLException
 	{
 		Connection conn = SimpleDataSource.getconnection();
         Statement stat = null;
         stat = conn.createStatement();
         String query = "SELECT [scientificName] FROM" 
                 + " BirdDatabase.dbo.UniqueBirdID where id = '" + id + "'";  //get database table
         ResultSet rs = null;
         rs = stat.executeQuery(query);
         rs.next();
         conn.close();
         return rs.getString("scientificName");
 	}
 	
 	public ArrayList<String> getCommonNames(int id) throws SQLException
 	{
		ArrayList<String> common = new ArrayList<String>();
 		Connection conn = SimpleDataSource.getconnection();
         Statement stat = null;
         stat = conn.createStatement();
         String query = "SELECT [name] FROM" 
                 + " BirdDatabase.dbo.UniqueBirdID where id = '" + id + "'";  //get database table
         ResultSet rs = null;
         rs = stat.executeQuery(query);
         rs.next();
         for(int j= 0; j < i; j++)//populate arraylist
         {    
         	common.add(rs.getString("name"));
             rs.next();
         }
         conn.close();
         return common;
 	}
 	
 	
 }
