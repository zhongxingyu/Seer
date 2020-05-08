 package automaatnehindaja;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Servlet implementation class StudentsTable
  */
 @WebServlet("/StudentsTable")
 public class StudentsTable extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(StudentsTable.class);
        
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	Connection c = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String statement;
 		String course = request.getParameter("course");
 		
 		try {
 			c = new SqlConnectionService().getConnection();
 			if (request.isUserInRole("admin") || request.isUserInRole("responsible")){
				statement = "SELECT users_courses.username, count(attempt.id) FROM users_courses "
						+ "LEFT JOIN attempt ON users_courses.username = attempt.username "
						+ "INNER JOIN users_roles ON users_courses.username = users_roles.username "
						+ "WHERE users_roles.rolename = 'tudeng' "
						+ "AND users_courses.coursename = ? "
						+ "GROUP BY users_courses.username;";
 				stmt = c.prepareStatement(statement);
 				stmt.setString(1, course);
 				rs = stmt.executeQuery();
 				response.setContentType("application/json");
 				JSONObject json = new JSONObject();
 				while (rs.next()){
 					json.append("name", rs.getString(1));
 					json.append("attemptCount", rs.getString(2));
 				}
 				response.getWriter().write(json.toString());
 			}
 			else{
 				response.setContentType("text/plain");
 				response.getWriter().write("Not Authorized!");
 			}
 			c.close();
 		}
 		catch (SQLException e){
 			logger.error("SqlException", e);
 		} catch (JSONException e) {
 			logger.error("JSONException", e);
 		}
 	}
 
 }
