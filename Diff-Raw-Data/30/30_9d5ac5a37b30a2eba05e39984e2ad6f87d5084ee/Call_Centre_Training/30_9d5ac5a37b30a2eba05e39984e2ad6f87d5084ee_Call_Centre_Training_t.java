 /**
 * @(#)Call_Centre_Training.java
 *
 * Call Centre Training Application
 *
 * @authors: Robbie Aftab, Ash Ellis, Steve Glasspool, Matt Kennedy
 */
 
 import javax.swing.*;
 import java.io.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.sql.PreparedStatement;
 import java.sql.Blob;
 import java.io.IOException;
 
 public class Call_Centre_Training 
 {
	static boolean inUniversity = true;
	static boolean initDone = false;
 	
     public static void main(String[] args) throws Exception
     {
         // Set System L&F
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
     	
     	//View v = new View();	
 		//v.createAndShowGUI();
		String fileLocation = "logo.jpg";
	    ImageIcon img = new ImageIcon(View.class.getResource("logo.jpg"));
	    JOptionPane.showMessageDialog(null, "Welcome to the British Gas call centre training program\nPlease wait whilst the program establishes a database connection\n \nPress Ok to continue","British Gas call centre training program",1, img);
	    
 		Connection conn = DriverManager.getConnection("jdbc:mysql://edward.solent.ac.uk:3306/ssd1?user=iaftab&password=rUg9ne4P");
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try
 		{
 			stmt = conn.prepareStatement("SELECT count(*) FROM customer");
			
 			rs = stmt.executeQuery();
 			if(rs.next())
 			{
				inUniversity = true;
 			}else
 			{
				inUniversity = false;
 			}
			initDone = true;
 			conn.close();
 		}
 		catch(SQLException ex)
 		{
 			ex.printStackTrace();
 		}
		CallLogView c = new CallLogView();
		c.createAndShowGUI();
		
		
     }
     
      //This is the class which is called for every database connection
      public static Connection getConnection() 
      {
 		Connection conn = null;
 		try
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			if(inUniversity)
 			{
 				conn = DriverManager.getConnection("jdbc:mysql://edward.solent.ac.uk:3306/ssd1?user=iaftab&password=rUg9ne4P");
 			}else
 			{
 				conn = DriverManager.getConnection("jdbc:mysql://instance10055.db.xeround.com:6860/bgTraining?user=bgAdmin&password=superuser");
 			}
 		}
 		catch(SQLException ex)
 		{
 			ex.printStackTrace();
 		}
 		catch(ClassNotFoundException ex)
 		{
 			ex.printStackTrace();
 		}
 		return conn;
 	}
 }
