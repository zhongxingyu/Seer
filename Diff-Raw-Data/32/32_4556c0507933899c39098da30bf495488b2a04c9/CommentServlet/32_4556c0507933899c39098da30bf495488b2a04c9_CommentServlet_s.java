 package com.node.edit;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.text.ParseException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.node.utilities.UtilityFunctionsImpl;
 
 /**
  * Servlet implementation class CommentServlet
  */
 @WebServlet("/CommentServlet")
 public class CommentServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private UtilityFunctionsImpl utility = new UtilityFunctionsImpl();
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public CommentServlet() {
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
 		// TODO Auto-generated method stub
 		String nodeId = request.getParameter("nodeID");
 		String comment = request.getParameter("comment");
 		HttpSession session = request.getSession(true);
 		int personID = 0;
 		if (null != session.getAttribute("userID")) {
 			// already authenticated
 			personID = Integer.parseInt(session.getAttribute("userID")
 					.toString());
 		} else {
 			// if it is not authenticated
 		}
 		String[] escapetexts = utility.setEcsapeTitleDesc(comment, null);
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			Connection con = DriverManager
 					.getConnection("jdbc:mysql://localhost:3306/test");
 			// Connection con =
 			// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 			// +"user=orbits&password=orbits");
 			// Update the title and desc with new data
 
 			// insert tag id and node id in the connection table
 			java.sql.PreparedStatement stat2;
 			try {
 				stat2 = con
 						.prepareStatement("INSERT INTO NodeConvo(NodeID,AuthorID,CreationTimeDate,CommentText) VALUES ("
 								+ nodeId
 								+ ","
 								+ personID
 								+ ",'"
 								+ utility.getCuttentDateTime()
 								+ "','"
 								+ escapetexts[0] + "')");
 				System.out.println("stat-" + stat2.toString());
 				stat2.executeUpdate();
 				RequestDispatcher dispatcher = request
						.getRequestDispatcher("/DisplayNodesServlet?projectId="+nodeId);
 				dispatcher.forward(request, response);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 }
