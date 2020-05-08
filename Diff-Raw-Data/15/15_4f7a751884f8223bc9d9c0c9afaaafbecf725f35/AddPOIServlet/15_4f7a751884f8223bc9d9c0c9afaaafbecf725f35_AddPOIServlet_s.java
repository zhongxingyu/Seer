 package com.nearme;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 /**
  * Servlet implementation class AddPOIServlet
  */
 
 
 public class AddPOIServlet extends HttpServlet {
 	
 	//TODO: why are these class variables, and why static?
 	
 	private static final long serialVersionUID = -1593856664522697355L;
 	private static String latitude;
 	private static String longitude;
 	private static String name;
 	private static String type;
 	private static int id_type;  
 	static Connection conn = null;
 	static String bd = "nearme";
 	static String login = "nearme";
 	static String password = "nearme";
 	static String url = "jdbc:mysql://localhost/" + bd;
 	
 	private static Logger logger = Logger.getLogger(AddPOIServlet.class);
 	
 	
 	public static void main(String args[]) throws Exception {
 
 		Class.forName("com.mysql.jdbc.Driver").newInstance(); // load the driver of mysql
 		
 
 		
 	}
 
    public AddPOIServlet() {
         super();
         
     }
 
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		doPost (request, response);
 	
 	}
 
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	
 		latitude = request.getParameter("lat");
 		longitude = request.getParameter("lng");
 		name = request.getParameter("name");
 		type = request.getParameter("type"); 
 	    //System.out.println("try");
 		
 		//TODO move this code into the POIFinder, and rename POIFinder to POIDAO
 		// This is so we keep database code in as few places as possible, thus enforcing a bit of modularity
 		// and a consistent style across the project
 		
 		try
 		  {
 			conn = DriverManager.getConnection(url, login, password); // connect with data base
 			//System.out.println("try 2");
 			ResultSet rs = null;
 			PreparedStatement typeSearch =  conn.prepareStatement("Select id from type Where name = ?");
 			typeSearch.setString(1, type);
 			rs = typeSearch.executeQuery();
 			
 	          
 			while(rs.next())
 			{
 				// get the id of the type name
 				id_type =Integer.parseInt(rs.getString("id")); 
 			}    
 			rs.close();
 			typeSearch.close();
 			
 			//System.out.println(id_type);
 			
 			//insert the new favourite place in the database 
 			PreparedStatement locationAdd = conn.prepareStatement("insert into poi values (default, ?, ?, ?, ?)");
 			locationAdd.setString(1, name);
 			locationAdd.setString(2, latitude);
 			locationAdd.setString(3, longitude);
 			locationAdd.setInt(4, id_type);
 			locationAdd.executeUpdate();
 			
 			locationAdd.close();
 			logger.info("received new POI " + name + " at (" + latitude + "," + "longitude)");
 
 			System.out.println("Data Saved");
 			conn.close();
 			
 		  }
 		  catch (SQLException e) {
 			  e.printStackTrace(); 
 		  }
 	
 		
 		response.setContentType("text/plain");
 		response.getOutputStream().println("OK");
 	}
 
 }
