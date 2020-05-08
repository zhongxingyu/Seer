 package automaatnehindaja;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 @WebServlet("/tasktable")
 public class TasktableServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public TasktableServlet() {
 		super();
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		Connection c = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String username = request.getUserPrincipal().getName();
 		String taskid = request.getParameter("id");
 
 		try {
 			
 			Class.forName("com.mysql.jdbc.Driver");
 			c = DriverManager.getConnection(
 					"jdbc:mysql://localhost:3306/automaatnehindaja", "root",
 					"t6urott");
 			String statement;
 			if (request.isUserInRole("tudeng")){
				statement = "select users.fullname, attempt.time, attempt.result, attempt.language "
 						+ "FROM attempt "
 						+ "INNER JOIN users "
 						+ "ON users.username=attempt.username WHERE attempt.username = ? "
 						+ "and attempt.task = ?;";
 				stmt = c.prepareStatement(statement);
 				stmt.setString(1, username);
 				stmt.setString(2, taskid);
 			}
 			else if (request.isUserInRole("admin")){
				statement = "select users.fullname, attempt.time, attempt.result, attempt.language "
 						+ "FROM attempt "
 						+ "INNER JOIN users "
 						+ "ON users.username=attempt.username WHERE "
 						+ "attempt.task = ?;";
 				stmt = c.prepareStatement(statement);
 				stmt.setString(1, taskid);
 			}
 			
 			rs = stmt.executeQuery();
 
 			response.setContentType("application/json");
 
 			JSONObject json = new JSONObject();
 
 			while (rs.next()) {
 				try {
 					json.append("fullname", rs.getString(1));				
 					json.append("result", rs.getString(3));
 					json.append("time", rs.getDate(2).toString());
 					json.append("language", rs.getString(4));
 				} catch (JSONException e) {
 					response.sendRedirect("/automaatnehindaja/error.html");
 				}
 			}
 			response.getWriter().write(json.toString());
 		} catch (SQLException e) {
 			response.sendRedirect("/automaatnehindaja/error.html");
 		} catch (ClassNotFoundException f) {
 			response.sendRedirect("/automaatnehindaja/error.html");
 		}
 
 	}
 }
