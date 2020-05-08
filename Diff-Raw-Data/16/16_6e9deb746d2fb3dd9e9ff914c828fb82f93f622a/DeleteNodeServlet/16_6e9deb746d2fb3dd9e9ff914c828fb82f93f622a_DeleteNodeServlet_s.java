 package com.node.edit;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class DeleteNodeServlet
  */
 @WebServlet("/DeleteNodeServlet")
 public class DeleteNodeServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public DeleteNodeServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String nodeID = request.getParameter("nodeID").toString();
 		String projectId=request.getParameter("projectId");
 		System.out.println("parentId--" + nodeID);
 		HttpSession session = request.getSession(true);
 		int personID = 0;
 		String selectedId="0";
 		boolean origin=false;
 		if (null != session.getAttribute("userID")) {
 			// In session
 			personID = Integer.parseInt(session.getAttribute("userID")
 					.toString());
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 				Connection con = DriverManager
 						.getConnection("jdbc:mysql://localhost:3306/test");
 				// Connection con =
 				// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 				// +"user=orbits&password=orbits");
 				
 				
 				// find parent
 				java.sql.PreparedStatement stat2 = con.prepareStatement("select Parent from Node where NodeID='"+ nodeID + "'");
 				ResultSet parentId = stat2.executeQuery();
 				while(parentId.next()){
 					selectedId=parentId.getString(1) ;
 					if(selectedId.equalsIgnoreCase("0")){
 						origin=true;
 						//break;
 					}
 				}
 				stat2.close();
 				if(origin){
 					// it is origin node 
 					// delete nodes
 					deleteNode(con,nodeID);
 					//delete project
 					deleteProject(con,projectId);
					String aDestinationPage="DisplayNodesServlet?projectId="+projectId+"&selectedNodeId="+selectedId;
 					String urlWithSessionID = response.encodeRedirectURL(aDestinationPage.toString());
 				    response.sendRedirect( urlWithSessionID );
 					
 				}else{
 					deleteNode(con,nodeID);	
 				}
				con.close();
				
				String aDestinationPage="AuthAndDisplayProjects";
				String urlWithSessionID = response.encodeRedirectURL(aDestinationPage.toString());
			    response.sendRedirect( urlWithSessionID );
 				//RequestDispatcher dispatcher = request
 				//		.getRequestDispatcher("/DisplayNodesServlet?projectId="+projectId+"&selectedNodeId="+nodeID);
 				//dispatcher.forward(request, response);
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}else{
 			request.setAttribute("timedOut", "true");
 			RequestDispatcher dispatcher = request
 					.getRequestDispatcher("index.jsp");
 			dispatcher.forward(request, response);
 		}
 		
 
 	}
 	
 	private void deleteNode(Connection con, String nodeID){
 		try {
 		java.sql.PreparedStatement  stat1 = con.prepareStatement("DELETE FROM VoteNodeCon WHERE NodeID='" + nodeID + "'");
 		stat1.executeUpdate();
 		stat1 = con.prepareStatement("DELETE FROM VoteModTracker WHERE NodeID='" + nodeID + "'");
 		stat1.executeUpdate();
 		stat1 = con.prepareStatement("Delete from UpDownVote where NodeID='" + nodeID + "'");
 		stat1.executeUpdate();
 		stat1 = con.prepareStatement("Delete from NodeTagsCon where NodeID='" + nodeID + "'");
 		stat1.executeUpdate();
 		stat1 = con.prepareStatement("Delete from NodeConvo where NodeID='" + nodeID + "'");
 		stat1.executeUpdate();
 		stat1 = con.prepareStatement("Delete from Node where NodeID='" + nodeID + "'");
 		stat1.executeUpdate();
 		stat1.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	private void deleteProject(Connection con, String projectID){
 		try {
 		java.sql.PreparedStatement  stat1 = con.prepareStatement("Delete from PersonTreeCon where ProjectID='" + projectID + "'");
 		stat1.executeUpdate();
 		stat1 = con.prepareStatement("Delete from Tree where ProjectID='" + projectID + "'");
 		stat1.executeUpdate();
 		stat1.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 }
