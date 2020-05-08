package Actions;
 import java.io.*;
 import java.sql.*;
 import java.util.logging.Logger;
 
 
 import javax.servlet.*;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 import javax.sql.DataSource;
 
 @WebServlet(urlPatterns={"/Login"})
 public class Login extends HttpServlet implements DataSource {
 
 	private String User =  null;
 	private int UserID = 0;
 	Connection connection = null;
 	private String password =  null;
 
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		if(request.getParameter("User") != null){ 
 			this.setUser((String) request.getParameter("User").toString());
 		}
 		if(request.getParameter("password") != null){
 			this.setPassword((String) request.getParameter("password").toString());
 		}
 		
 		
 		
 		try {
 		    System.out.println("Loading driver...");
 		    Class.forName("com.mysql.jdbc.Driver");
 		    System.out.println("Driver loaded!");
 		} catch (ClassNotFoundException e) {
 		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
 		}
 		
 		Login ds = new Login();
         try {
 			connection = ds.getConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		PrintWriter out = response.getWriter();
 		if(connection != null){
 			
 			//out.println(User  + "   " + password);
 			
 			//Check if user exists in database
 			if(User!= null){
 				
 				Statement stmt;
 				ResultSet rs, rs2;
 				try {
 					stmt = connection.createStatement();
 					rs = stmt.executeQuery("SELECT * FROM tblUsers WHERE Username = '" + User + "';");
 					
 					
 					if(!rs.next()){
 						out.println("Username: " + User + " was not found in Users table.");
 					}
 					else{
 						//out.print("Pass: "+password);
 						//out.print("getString: "+rs.getString(3));
 						
 						//Reset UserID
 						UserID = 0;
 						
 						//User was found now check if password is correct
 						if(rs.getString(3).equals(password)){
 							//out.println("User:" + rs.getInt(1));
 							
 							UserID = rs.getInt(1);
 							
 							//Check if user is in game
 							rs2 = stmt.executeQuery("SELECT PlayerGameID FROM tblPlayers WHERE PlayerUserID = "+UserID+" AND PlayerStatus = 1;");
 							
 							if(rs2.next()){
 								out.println("User;" + UserID + ";"+ rs2.getInt(1));
 							}
 							else
 							{
 								out.println("User;" + UserID);
 							}
 							
 							rs2.close();
 						}
 						else if(rs.getString(3).equals(password) == false){
 							//password was incorrect
 							out.println("Password incorrect!");
 						}
 						
 					}
 					
 					//Reset UserID
 					UserID = 0;
 						
 					rs.close();
 					stmt.close();
 					connection.close();
 					
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 		
 		}
 		
 	}
 
 
 
 	@Override
 	public PrintWriter getLogWriter() throws SQLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public void setLogWriter(PrintWriter out) throws SQLException {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void setLoginTimeout(int seconds) throws SQLException {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public int getLoginTimeout() throws SQLException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 	@Override
 	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public <T> T unwrap(Class<T> iface) throws SQLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public boolean isWrapperFor(Class<?> iface) throws SQLException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public Connection getConnection() throws SQLException {
         if (connection != null) {
             System.out.println("Cant craete a Connection");
     } else {
             connection = DriverManager.getConnection(
                             "jdbc:mysql://ourdbinstance.cbvrc3frdaal.us-east-1.rds.amazonaws.com:3306/dbAppData", "AWSCards", "Cards9876");
     }
     return connection;
 	}
 
 
 	@Override
 	public Connection getConnection(String username, String password)
 			throws SQLException {
 		// TODO Auto-generated method stub
         if (connection != null) {
                 System.out.println("Cant craete a Connection");
         } else {
                 connection = DriverManager.getConnection(
                                 "jdbc:mysql://ourdbinstance.cbvrc3frdaal.us-east-1.rds.amazonaws.com:3306/dbAppData", username, password);
         }
         return connection;
 	}
 
 
 	public String getUser() {
 		return User;
 	}
 
 
 	public void setUser(String user) {
 		User = user;
 	}
 
 
 
 	public String getPassword() {
 		return password;
 	}
 
 
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 }
