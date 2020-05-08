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
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 @WebServlet("/Taskstable")
 public class TaskstableServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(TaskstableServlet.class);
 
 	public TaskstableServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
 		Connection c = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String statement;
 		String course = request.getParameter("course");
 		System.out.println(course);
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			c = DriverManager.getConnection(
 					"jdbc:mysql://localhost:3306/automaatnehindaja", "ahindaja",
 					"k1rven2gu");
 
 			if (request.isUserInRole("tudeng")) {
 				statement = "SELECT "
 						+ "tasks.id, tasks.name, tasks.deadline, attempt.result "
 						+ "FROM tasks " 
 						+ "LEFT OUTER JOIN "
 						+ "attempt on tasks.id = attempt.task " + "AND "
 						+ "attempt.username = ? "
 						+ "WHERE tasks.coursename = ?;";
 				stmt = c.prepareStatement(statement);
 				stmt.setString(2, course);
 				stmt.setString(1, request.getUserPrincipal().getName());
 			} else if (request.isUserInRole("admin") || request.isUserInRole("responsible")) {
 				statement = "SELECT tasks.id, tasks.name, tasks.deadline, count(attempt.task) AS attempts, "
 						+ "(SELECT count(*) FROM attempt where attempt.task = tasks.id AND "
 						+ "attempt.result = 'OK') AS successful "
 						+ "FROM tasks " + "LEFT JOIN attempt "
 						+ "ON tasks.id = attempt.task " 
 						+ "WHERE tasks.coursename = ? "
 						+ "GROUP BY tasks.id;";
 				stmt = c.prepareStatement(statement);
 				stmt.setString(1, course);
 			} else {
 				return;
 			}
 
 			rs = stmt.executeQuery();
 
 			response.setContentType("application/json");
 
 			JSONObject json = new JSONObject();
 
 			if (request.isUserInRole("tudeng")) {
 				try {
 					json.put("role", "tudeng");
 					while (rs.next()) {
 						json.append("id", rs.getString(1));
 						json.append("name", rs.getString(2));
 						json.append("deadline", rs.getDate(3).toString());
 						String tulemus = rs.getString(4);
 						if (tulemus == null) {
 							tulemus = "Esitamata";
 						}
 						json.append("result", tulemus);
 					}
 				} catch (JSONException e) {
 					logger.debug("JSONEXCEPTION", e);
 				}
 			}
 
 			else if (request.isUserInRole("admin") || request.isUserInRole("responsible")) {
 				try {
 					json.put("role", "admin");
 					while (rs.next()) {
 						json.append("id", rs.getString(1));
 						json.append("name", rs.getString(2));
 						json.append("deadline", rs.getDate(3).toString());
 						json.append("resultCount", rs.getInt(4));
 						json.append("successCount", rs.getInt(5));
 					}
 				} catch (JSONException e) {
 					logger.debug("JSONEXCEPTION", e);
 				}
 			} else {
 				response.sendRedirect("/automaatnehindaja/error.html");
 				return;
 			}
 			
 			c.close();
 			response.getWriter().write(json.toString());
 
 		} catch (SQLException e) {
 			logger.debug("SQLEXCEPTION", e);
 		} catch (ClassNotFoundException f) {
 			logger.debug("CLASSNOTFOUNDEXCEPTION", f);
 		}
 
 	}
 
 }
