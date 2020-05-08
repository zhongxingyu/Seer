 package com.node.downvote;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class DownvoteServlet
  */
 @WebServlet("/DownvoteServlet")
 public class DownvoteServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public DownvoteServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		String nodeID = request.getParameter("nodeID").toString();
		String projectId=request.getParameter("projectId");
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			Connection con = DriverManager
 					.getConnection("jdbc:mysql://localhost:3306/test");
 			// Connection con =
 			// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 			// +"user=orbits&password=orbits");
 			// to get get the upvote count
 			java.sql.PreparedStatement stat = con
 					.prepareStatement("select DownVote from Node where NodeID='"
 							+ nodeID + "'");
 			ResultSet result = stat.executeQuery();
 			result.first();
 			int downvote = result.getInt(1);
 			downvote++;
 			java.sql.PreparedStatement stat1 = con
 					.prepareStatement("UPDATE Node SET DownVote=" + downvote
 							+ " Where NodeID='" + nodeID + "'");
 			stat1.executeUpdate();
 			RequestDispatcher dispatcher = request
					.getRequestDispatcher("/DisplayNodesServlet?projectId="+projectId);
 			dispatcher.forward(request, response);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 }
