 package me.navigation.server;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import me.navigation.server.BoundingBox;
 import me.navigation.shared.LatLong;
 
 
 /**
  * @author Aditya
  * All database related operations are done from this class.
  * Used as an abstaction layer for all database related calls
  */
 public class DatabaseOperations {
 
 	
 	Connection con = null;
     Statement st = null;
     ResultSet rs = null;
     String url, username, password;
     
     public DatabaseOperations()
     {
     	this.url = "jdbc:mysql://localhost:3306/project";
         this.username = "adityauv";
         this.password = "uvnavigation";
     }
     public DatabaseOperations(String url, String username, String password)
     {
     	this.url=url;
     	this.username = username;
     	this.password = password;
     }
     public void getData(LatLong p1, LatLong p2) throws SQLException
 	{
     	con = DriverManager.getConnection(url, username, password);
     	BoundingBox x = new BoundingBox(20);
     	x.getBoundingBox(p1, p2);
     	LatLong min = x.getMin();
     	LatLong max = x.getMax();
     	
     	String sql = "select distinct Latitude, Longitude, UVA1, UVA2, UVB1, UVB2 " +
    				"from uvReadings_copy where Time > '2012-10-22' " +
     				"and UVB1 > 7.289  and Latitude > ?  and Longitude > ? " +
     				"and Latitude < ? and Longitude < ?";
     	PreparedStatement p = con.prepareStatement(sql);
     	p.setDouble(1, min.getLatitude());
     	p.setDouble(2, min.getLongitude());
     	p.setDouble(3,max.getLatitude());
     	p.setDouble(4, max.getLongitude());
     	ResultSet rs = p.executeQuery();
     	
     	while(rs.next())
     	{
     		System.out.println("\t"+rs.getString(1)+","+rs.getString(2)+","+rs.getString("UVA1")+","+rs.getString("UVB1"));
     	}
     	rs.close();
     	con.close();
 		
 	}
     
 	@SuppressWarnings("deprecation")
 	public int insertIntoDatabase(String filename) throws IOException, SQLException, ParseException
 	{
 		//database related block
 		con = DriverManager.getConnection(url, username, password);
 		//st = con.createStatement();
 		
 		String sql;
 		PreparedStatement prep=null;
 		String line;
 		Date dt;
 		String[] lineArr;
 		File f = new File(filename);
 		FileInputStream fStream = new FileInputStream(f);
 		DataInputStream in = new DataInputStream(fStream);
 		BufferedReader buff = new BufferedReader(new InputStreamReader(in));
 		while((line=buff.readLine())!=null)
 		{
 			lineArr = line.split(",");
 			
 			dt = new Date(lineArr[0]);
 			//Date d = new java.sql.Date(dt.getTime());
 //			java.text.SimpleDateFormat sdf = 
 //			     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 //			
 //			dt = sdf.parse(lineArr[0]);
 //			String currentTime = sdf.format(dt);
 			String currentTime= new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt);
 			
 			
 			//sql="Insert into uvReadings(Time,Latitude, Longitude) values('"+currentTime+"',"+(float)Double.parseDouble(lineArr[1])+","+(float)Double.parseDouble(lineArr[2])+")";
 			sql="INSERT INTO uvReadings(Time,Latitude, Longitude, UVA1,UVA2,UVB1,UVB2) VALUES (?,?,?,?,?,?,?)";
 			prep =con.prepareStatement(sql);
 			
 			
 			//prep.setString(1,"testStreet");
 			prep.setString(1, currentTime);
 			prep.setString(2, lineArr[1]);
 			prep.setString(3, lineArr[2]);
 			prep.setFloat(4, new Float(lineArr[3]));
 			prep.setFloat(5, new Float(lineArr[4]));
 			prep.setFloat(6, new Float(lineArr[5]));
 			prep.setFloat(7, new Float(lineArr[6]));
 			//st.executeUpdate(sql);
 			prep.executeUpdate();
 		}
 		prep.close();
 		//st.close();
 		con.close();
 //		try{
 //			
 //			
 //			prep  = con.prepareStatement(sql);
 //			prep.setString(1, "Jay");
 //			rs = prep.executeQuery();
 //			while(rs.next())
 //			{
 //				System.out.println(rs.getString(1));
 //			}
 //			rs.close();
 //			prep.close();
 //			con.close();
 //		}catch(Exception e)
 //		{
 //			System.out.println(e.getMessage());
 //		}
 //		
 		return 0;
 	}
 }
