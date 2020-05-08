 package servlets;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import quiz.QuizConstants;
 
 import user.Note;
 import user.User;
 import database.DataBaseObject;
 import database.MyDB;
 
 /**
  * Servlet implementation class ModifyUser
  */
 @WebServlet("/ModifyUser")
 public class ModifyUser extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ModifyUser() {
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
 		Connection conn = MyDB.getConnection();
 
 		String user = request.getParameter("user");
 		String radio = request.getParameter("group1");
 		
 		if (radio.equals("promote")) {
 			Statement stmt;
 			try {
 				stmt = conn.createStatement();
 				String query = "SELECT * FROM User WHERE username='" + user + "';";
 				ResultSet rs = stmt.executeQuery(query);
 				
 				if (rs.next()) {
 					String[] attrs = DataBaseObject.getRow(rs, QuizConstants.USER_N_COLUMNS);
 					User usr = new User(attrs, conn);
 					usr.makeAdmin();
 					usr.saveToDataBase(conn);
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
 		} else {
 			Statement stmt;
 			try {
 				stmt = conn.createStatement();
 				String query = "DELETE FROM User WHERE username='" + user + "';";
 				stmt.executeUpdate(query);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		RequestDispatcher disp = request.getRequestDispatcher("AdminServlet");
 		disp.forward(request, response);
 	}
 
 }
