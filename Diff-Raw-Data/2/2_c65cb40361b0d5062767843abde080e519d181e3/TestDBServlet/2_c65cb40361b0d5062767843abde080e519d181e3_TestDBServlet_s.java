 package com.test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class TestDBServlet
  */
 
 public class TestDBServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	String dbHost=null;
 
     /**
      * Default constructor. 
      */
     public TestDBServlet() {
         // TODO Auto-generated constructor stub
     }
     
     
     public void init() throws ServletException {
      
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(true);
 
       
 
 		response.setContentType("text/html");
 
 		   PrintWriter out = response.getWriter();
		   out.println("<HTML><HEAD><TITLE>Welcome1 is .  "+dbHost+"</TITLE>");
 
 		   out.println("</HEAD>");
 		   out.println("<h1>Employee details</h1>");
 		//   out.println("<p>My first paragraph one ,two.</p>");
 		   
 		   out.println("<TABLE align=center border=1 cellPadding=1 cellSpacing=1 width=\"75%\">");
 
 		   
 
 		   out.println("<TR>");
 
 		   out.println("<TD>Name</TD>");
 
 		   out.println("<TD>Company</TD>");
 
 		  // out.println("<TD>Website</TD></TR>");
 
 		 try{
 			
 			 String userName = "testuser";
 	           String password = "testpass";
 	          // String url = "jdbc:mysql://"+dbHost+":3306/testdb";
 	           
 	           String url = getUrl();
 
 			 Class.forName ("com.mysql.jdbc.Driver").newInstance ();
 	       Connection    conn = DriverManager.getConnection (url, userName, password);
 		  
 
 		    Statement theStatement=conn.createStatement();
 
 		    ResultSet theResult=theStatement.executeQuery("select * from testtb"); //Select all records from emaillists table.
 		 //Fetch all the records and print in table
 		   while(theResult.next()){
 
 		    out.println();
 
 		    out.println("<TR>");
 
 		    out.println("<TD>" + theResult.getString(1) + "</TD>");
 
 		    out.println("<TD>" + theResult.getString(2) + "</TD>");
 
 		    //String s=theResult.getString(3);
 
 		   // out.println("<TD><a href=" + s + ">" + s + "</a></TD>");
 
 		    out.println("</TR>");
 
 		   }
 
 		   theResult.close();//Close the result set
 
 		   theStatement.close();//Close statement
 
 		   conn.close(); //Close database Connection
 
 		   }catch(Exception e){
 
 		    out.println(e.getMessage());//Print trapped error.
 
 		   }
 
 		   out.println("</TABLE></P>");
 
 		   out.println("<body></body></html>");
 
 		  }
 	
 	private String getUrl(){
 		String url=null;
 		File configFile = new File("dbconfig.properties");  
 		Properties config = new Properties();  
 		FileInputStream istream=null;
 		try {
 			istream = new FileInputStream(configFile);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 		try {
 			config.load(istream);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		
 		try {
 			istream.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 		url=config.getProperty("dburl");
 		return url;
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
