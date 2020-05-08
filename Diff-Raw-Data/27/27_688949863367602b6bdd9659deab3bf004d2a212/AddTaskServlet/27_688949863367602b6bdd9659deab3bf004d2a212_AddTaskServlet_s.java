 package automaatnehindaja;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.GregorianCalendar;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 /**
  * Servlet implementation class AddTaskServlet
  */
 @WebServlet("/addTask")
 public class AddTaskServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(AddTaskServlet.class);
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		if (request.isUserInRole("admin")
 				|| request.isUserInRole("responsible")) {
 
 			String course = request.getParameter("course");
 			String taskname = request.getParameter("name");
 			String description = request.getParameter("description");
 			String deadline[] = request.getParameter("deadline").split("-");
 			String input = request.getParameter("inputs");
 			String output = request.getParameter("outputs");
 			
 			JSONObject json_input = null;
 			JSONObject json_output = null;
 			try {
 				json_input = new JSONObject(input);
 				json_output = new JSONObject(output);
 			} catch (JSONException e1) {
 				logger.error("JSON parsing error", e1);
 			}

			GregorianCalendar deadlineDate = new GregorianCalendar(
					Integer.parseInt(deadline[0])-1900,
					Integer.parseInt(deadline[1]),
					Integer.parseInt(deadline[2]), 23, 59, 59);
 			
			deadlineDate.toString();

 			Connection c = null;
 			PreparedStatement stmt = null;
 			String statement;
 			ResultSet rs;
 
 			try {
 
 				Class.forName("com.mysql.jdbc.Driver");
 				c = DriverManager.getConnection(
 						"jdbc:mysql://localhost:3306/automaatnehindaja",
 						"ahindaja", "k1rven2gu");
 				logger.info("Inserting a new task by: " + request.getRemoteUser());
 				statement = "INSERT INTO tasks (name, description, deadline, coursename) VALUES (?,?,?,?);";
 				stmt = c.prepareStatement(statement,
 						PreparedStatement.RETURN_GENERATED_KEYS);
 				stmt.setString(1, taskname);
 				stmt.setString(2, description);
				stmt.setDate(
						3,
						(java.sql.Date) new Date(deadlineDate.getTimeInMillis()));
 				stmt.setString(4, course);
 				stmt.executeUpdate();
 				rs = stmt.getGeneratedKeys();
 				int id = -1;
 				if (rs.next()) {
 					id = rs.getInt(1);
 				}
 				else{
 					return;
 				}
 				stmt.close();
 
 				statement = "INSERT INTO tasks_input VALUES (?,?,?,?)";
 				stmt = c.prepareStatement(statement);
 				System.out.println(json_input.length());
 				for (int i = 0; i < json_input.length(); i++) {
 					System.out.println("i: " + i);
 					String inputSet[] = ((String) json_input.get(Integer.toString(i))).split(";");
 					System.out.println("inputSet: " + inputSet.length);
 					for (int j = 0; j<inputSet.length; j++){
 						System.out.println("j: " + j);
 						stmt.setInt(1, id);
 						stmt.setInt(2, i);
 						stmt.setInt(3, j);
 						stmt.setString(4, inputSet[j]);
 						stmt.addBatch();
 					}
 				}
 				stmt.executeBatch();
 				stmt.close();
 				statement = "INSERT INTO tasks_output VALUES (?,?,?,?)";
 				stmt = c.prepareStatement(statement);
 				for (int i = 0; i < json_output.length(); i++) {
 					String outputSet[] = ((String) json_output.get(Integer.toString(i))).split(";");
 					for (int j = 0; j<outputSet.length; j++){
 						stmt.setInt(1, id);
 						stmt.setInt(2, i);
 						stmt.setInt(3, j);
 						stmt.setString(4, outputSet[j]);
 						stmt.addBatch();
 					}
 				}
 				logger.info("Task insertion succeeded, new task ID: " + id);
 				stmt.executeBatch();
 				stmt.close();
 				c.close();
 			} catch (SQLException | ClassNotFoundException | JSONException e) {
 				logger.error("Task insertion failed", e);
 				return;
 			}
 
 		}
 		else{
 			logger.warn("Unauthorized access by" + request.getRemoteUser());
 			response.sendRedirect("logout");
 		}
 	}
 }
