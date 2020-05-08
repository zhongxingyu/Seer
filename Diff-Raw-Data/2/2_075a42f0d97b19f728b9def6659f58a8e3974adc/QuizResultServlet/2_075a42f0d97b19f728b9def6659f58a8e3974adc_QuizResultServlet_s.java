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
 
 import database.DataBaseObject;
 
 import quiz.QuizConstants;
 import quiz.QuizResult;
 
 /**
  * Servlet implementation class QuizResultServlet
  */
 @WebServlet("/QuizResultServlet")
 public class QuizResultServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public QuizResultServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection conn = (Connection) getServletContext().getAttribute("database");
 		int resultID =  Integer.parseInt(request.getParameter("id"));
 		
 		try {
 			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM Quiz_Result WHERE id='" + resultID +";";
 			ResultSet rs = stmt.executeQuery(query);
 			if (rs.next()) {
 				String[] attrs = DataBaseObject.getRow(rs, QuizConstants.QUIZ_RESULT_N_COLS);
 				QuizResult quiz = new QuizResult(attrs, conn);
 				request.setAttribute("result", quiz);
 			}
 			
 			RequestDispatcher dispatch = request.getRequestDispatcher("quiz_result.jsp");
 			dispatch.forward(request, response);
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
