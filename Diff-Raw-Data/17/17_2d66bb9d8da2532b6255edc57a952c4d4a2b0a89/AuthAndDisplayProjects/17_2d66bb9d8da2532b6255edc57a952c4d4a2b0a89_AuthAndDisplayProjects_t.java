 package com.user.project;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class AuthAndDisplayProjects
  */
 @WebServlet("/AuthAndDisplayProjects")
 public class AuthAndDisplayProjects extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AuthAndDisplayProjects() {
          super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	
 		HttpSession session = request.getSession(true);
 		int personID = 0;
 		if (null != session.getAttribute("userID")) {
 			// already authenticated
 			personID = Integer.parseInt(session.getAttribute("userID")
 					.toString());
 			setAttributes(session,personID, request,response);
 		} else {
 			// needs to be authenticated
 			try {
 				String username = request.getParameter("username");
 				String password = request.getParameter("password");
 				personID = authLogin(username, password);
 				if (personID == 0) {
 					request.setAttribute("results", "none");
 					RequestDispatcher dispatcher = request
 							.getRequestDispatcher("index.jsp");
 					dispatcher.forward(request, response);
 
 				} else {
 					HttpSession newsession = request.getSession(false);
 					newsession.setAttribute("userID", personID);
 					// set the attributes in request and session
 					setAttributes(newsession,personID,request, response);
 				}
 			} catch (ClassNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		
 		
 
 	}
 
 
 
 	/**
 	 * Authenticate the user
 	 * @param username
 	 * @param password
 	 * @return
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	int authLogin(String username, String password)
 	throws ClassNotFoundException, SQLException {
 
 	int parseInt = 0;
 	Class.forName("com.mysql.jdbc.Driver");
 	Connection con = DriverManager
 		.getConnection("jdbc:mysql://localhost:3306/test");
 	// Connection con =
 	// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 	// +"user=orbits&password=orbits");
 	java.sql.PreparedStatement stat = con
 		.prepareStatement("select PersonID from Person where Username='"
 				+ username + "' and MyPassword='" + password + "'");
 	ResultSet result = stat.executeQuery();
 	while (result.next()) {
 	parseInt = Integer.parseInt(result.getString(1));
 							}
 	return parseInt;
 	// return ((Number) result.getObject(1)).intValue();
 
 	}
 	
 	/**
 	 * Set the request attribute with all his projects
 	 * @throws IOException 
 	 * @throws ServletException 
 	 */
 	void setAttributes(HttpSession newsession,int userId, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");	
 			//set user name in session
 			java.sql.PreparedStatement stat = con.prepareStatement("select Username from Person where PersonID='"
 					+ userId + "'");
 				ResultSet result = stat.executeQuery();
 				result.first();
 				newsession.setAttribute("username", result.getString(1));
 				
 			java.sql.PreparedStatement stat2 = con.prepareStatement("select ProjectID,Title,CreationTimeDate from Tree where AuthorID='"+ userId + "'");
 			ResultSet projectdets = stat2.executeQuery();
 			HashMap<String, List<String>> projects= new HashMap<String,List<String>>();
 			if(null != projectdets){
 			while (projectdets.next()) {
 				List<String> messages = new ArrayList<String>();
 				messages.add(projectdets.getString("Title"));
 				messages.add(projectdets.getString("CreationTimeDate"));
 				messages.add(numberofContributors(projectdets.getString("ProjectID")));
 				projects.put(projectdets.getString("ProjectID"), messages);
 								}	
 			}
 			request.setAttribute("projects", projects);
 			
 			//get projects that you contributed in
 			request.setAttribute("contributions", getContributions(userId));
 			
 			RequestDispatcher dispatcher = request.getRequestDispatcher("landingPage.jsp");
 			dispatcher.forward(request, response);
 			
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 
 	private String numberofContributors(String projectId){
		Integer number=1;
 		try {
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection con;
 	
 			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test");
 		
 	// Connection con =
 	// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 	// +"user=orbits&password=orbits");	
 		//set user name in session
 		java.sql.PreparedStatement stat = con.prepareStatement("select COUNT(DISTINCT PersonID) from PersonTreeCon where ProjectID='"
 				+ projectId + "'");
 		ResultSet result = stat.executeQuery();
 		result.first();
		number=Integer.parseInt(result.getString(1))+1;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		return number.toString();
 	}
 	
 	/**
 	 * Get the projects that the user contributed in
 	 * @param userId 
 	 * @return
 	 */
 	private HashMap<String, List<String>> getContributions(int userId) {
 		HashMap<String, List<String>> projects= new HashMap<String,List<String>>();
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");	
 			//set user name in session
 			java.sql.PreparedStatement stat = con.prepareStatement("select ProjectID from PersonTreeCon where PersonID='"
 					+ userId + "'");
 			ResultSet result = stat.executeQuery();
 			while(result.next()){
 				java.sql.PreparedStatement stat2 = con.prepareStatement("select ProjectID,Title,CreationTimeDate,AuthorID from Tree where ProjectID='"+ result.getInt(1) + "'");
 				ResultSet projectdets = stat2.executeQuery();
 				projectdets.first();
 				if(null != projectdets){
 						List<String> messages = new ArrayList<String>();
 						messages.add(projectdets.getString("Title"));
 						messages.add(projectdets.getString("CreationTimeDate"));
 						
 						//get the author id
 						int authorId=projectdets.getInt("AuthorID");
 						java.sql.PreparedStatement stat1 = con.prepareStatement("select Username from Person where PersonID='"
 								+ authorId + "'");
 						ResultSet authorRes = stat1.executeQuery();
 						authorRes.first();
 						messages.add(authorRes.getString(1));
 						messages.add(numberofContributors(projectdets.getString("ProjectID")));
 						projects.put(projectdets.getString("ProjectID"), messages);
 										
 					}
 				
 			}
 
 			
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return projects;
 		
 	}
 	
 }
 
 
