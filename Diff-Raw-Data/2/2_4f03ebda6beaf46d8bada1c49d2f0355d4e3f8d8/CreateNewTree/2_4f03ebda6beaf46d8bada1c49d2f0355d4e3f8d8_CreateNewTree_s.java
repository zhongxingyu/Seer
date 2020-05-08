 package com.node.create;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.node.display.DisplayNodesServlet;
 import com.node.utilities.UtilityFunctionsImpl;
 
 /**
  * Servlet implementation class CreateNewTree
  */
 @WebServlet("/CreateNewTree")
 public class CreateNewTree extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private DisplayNodesServlet authLogin = new DisplayNodesServlet();
 	private UtilityFunctionsImpl utility = new UtilityFunctionsImpl();
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CreateNewTree() {
        super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	System.out.println("When u can display all the trees");
 	// get all the projects
 	RequestDispatcher dispatcher = request.getRequestDispatcher("landingPage.jsp");
 	dispatcher.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		HttpSession session = request.getSession();
 		String userId = session.getAttribute("userID").toString();
 		String title = request.getParameter("title");
 		String[] escapeTexts= utility.setEcsapeTitleDesc(title, null);
 		try {
 			String dt = utility.getCuttentDateTime();
 			Class.forName("com.mysql.jdbc.Driver");
 			Connection con = DriverManager
 					.getConnection("jdbc:mysql://localhost:3306/test");
 			// Connection con =
 			// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 			// +"user=orbits&password=orbits");
 			java.sql.PreparedStatement stat = con.prepareStatement("INSERT INTO Tree(Title,AuthorId,CreationTimeDate) VALUES ('"+ escapeTexts[0]+ "','"+ userId+ "','"+ dt+ "')");
 			stat.executeUpdate();
 			//get project Id
 			java.sql.PreparedStatement stat2 = con.prepareStatement("Select ProjectID from Tree where Title='"+ escapeTexts[0]+ "' and AuthorID='"+ userId+ "' and CreationTimeDate='"+ dt+ "'");
 			ResultSet result= stat2.executeQuery();
 			result.first();
 			String projectID=result.getString(1);
 			// insert into Node table
 			java.sql.PreparedStatement stat3 = con.prepareStatement("INSERT INTO Node(Title,AuthorId,CreationTimeDate,ProjectId,Parent,Levelno,countChildren,UpVote,DownVote) VALUES ('"+ escapeTexts[0]+ "','"+ userId+ "','"+ dt+ "',"+projectID+",0,1,0,0,0)");
 			stat3.executeUpdate();
 			enterContributors(request,response,projectID);
 			
 			//get the nodeId that just got created
			java.sql.PreparedStatement stat4 = con.prepareStatement("Select NodeID from Node where ProjectId='"+ projectID);
 			ResultSet resultNode= stat4.executeQuery();
 			resultNode.first();
 			String nodeId=resultNode.getString(1);
 			
 			
 		// request.
 		String path="/DisplayNodesServlet?projectId="+projectID+"&selectedNodeId="+nodeId;
 		RequestDispatcher dispatcher = request
 				.getRequestDispatcher(path);
 		dispatcher.forward(request, response);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Insert the contributors
 	 * @param request
 	 */
 	private void enterContributors(HttpServletRequest request,HttpServletResponse response,String projectID) {
 		for(int i=1;i<=8;i++){
 			String paramName= "person"+i;
 			String name= request.getParameter(paramName);
 			if(name.length() > 0 || !name.isEmpty()){
 				try {
 					Class.forName("com.mysql.jdbc.Driver");
 				
 				Connection con = DriverManager
 						.getConnection("jdbc:mysql://localhost:3306/test");
 				// Connection con =
 				// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 				// +"user=orbits&password=orbits");
 				
 				//check if the name exists in the DB and get the personID
 				java.sql.PreparedStatement stat2 = con.prepareStatement("Select PersonID from Person where Username='"+name.toLowerCase()+ "'");
 				ResultSet result= stat2.executeQuery();
 				result.first();
 				if(result.getInt(1) != 0 ){
 				java.sql.PreparedStatement stat = con.prepareStatement("INSERT INTO PersonTreeCon VALUES ("+ projectID+","+ result.getInt(1)+ ")");
 				stat.executeUpdate();
 				}else{
 					request.setAttribute("nonExistantPerson", name);
 					RequestDispatcher dispatcher = request.getRequestDispatcher("landingPage.jsp");
 					dispatcher.forward(request, response);
 				}
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ServletException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		
 		
 	}
 
 }
