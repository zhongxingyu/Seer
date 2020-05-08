 package automaatnehindaja;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.GregorianCalendar;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 @WebServlet("/changeTask")
 public class ChangeTaskServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(ChangeTaskServlet.class);
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		int id = Integer.parseInt(request.getParameter("id"));
 		Connection c = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String statement = null;
 		
 		if (request.isUserInRole("admin")||request.isUserInRole("responsible")){
 			try{
 				Class.forName("com.mysql.jdbc.Driver");
 				c = DriverManager.getConnection(
 						"jdbc:mysql://localhost:3306/automaatnehindaja",
 						"ahindaja", "k1rven2gu");
 				statement = "SELECT coursename, name, description, DATE_FORMAT(deadline, '%d-%l-%Y') AS deadline FROM tasks"
 						+ " WHERE id = ?;";
 				stmt = c.prepareStatement(statement);
 				stmt.setInt(1, id);
 				rs = stmt.executeQuery();
 				JSONObject json = new JSONObject();
 				while (rs.next()) {
 					json.put("selectedcourse", rs.getString(1));
 					json.put("name", rs.getString(2));
 					json.put("description", rs.getString(3));
 					json.put("deadline", rs.getString(4));
 				}
 				stmt.close();
 				statement = "SELECT outer_seq, input FROM tasks_input WHERE task_id = ? "
 						+ "ORDER BY outer_seq, inner_seq";
 				stmt = c.prepareStatement(statement);
 				stmt.setInt(1, id);
 				rs = stmt.executeQuery();
 				int seq = 0;
 				String input = "";
 				while (rs.next()) {
 					if (rs.getInt(1) == seq){
 						input = input + rs.getString(2) + ";";
 					}
 					else {
 						json.append("inputs", input.substring(0, input.length()-1));
 						input = rs.getString(2) + ";";
 						seq = +1;
 					}
 				}
 				json.append("inputs", input.substring(0, input.length()-1));
 				stmt.close();
				statement = "SELECT outer_seq, output FROM tasks_output WHERE task_id = ? "
 						+ "ORDER BY outer_seq, inner_seq";
 				stmt = c.prepareStatement(statement);
 				stmt.setInt(1, id);
 				rs = stmt.executeQuery();
 				seq = 0;
 				String output = "";
 				while (rs.next()) {
 					if (rs.getInt(1) == seq){
 						output = output + rs.getString(2) + ";";
 					}
 					else {
 						json.append("outputs", output.substring(0, output.length()-1));
 						output = rs.getString(2) + ";";
 						seq = +1;
 					}
 				}
 				json.append("outputs", output.substring(0, output.length()-1));
 				stmt.close();
 				c.close();
 				
 				response.setContentType("application/json");
 				response.getWriter().write(json.toString());
 			}
 			catch (SQLException e){
 				logger.error("Sqlexception", e);
 			} catch (ClassNotFoundException e) {
 				logger.error("ClassNotFoundException", e);
 			} catch (JSONException e) {
 				logger.error("JSONException", e);
 			}
 			
 		}
 		else {
 			logger.warn("Unauthorized access by: " + request.getRemoteUser());
 		}
 	}
 
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if (request.isUserInRole("admin")
 				|| request.isUserInRole("responsible")) {
 			response.setContentType("text/plain");
 			PrintWriter pw = response.getWriter();
 			int id = Integer.parseInt(request.getParameter("id"));
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
 					Integer.parseInt(deadline[2]),
 					Integer.parseInt(deadline[1])-1,
 					Integer.parseInt(deadline[0]), 23, 59, 59);
 			Timestamp tsmp = new Timestamp(deadlineDate.getTimeInMillis());
 			Connection c = null;
 			PreparedStatement stmt = null;
 			String statement;
 
 			try {
 
 				Class.forName("com.mysql.jdbc.Driver");
 				c = DriverManager.getConnection(
 						"jdbc:mysql://localhost:3306/automaatnehindaja",
 						"ahindaja", "k1rven2gu");
 				logger.info("Inserting a new task by: " + request.getRemoteUser());
 				statement = "UPDATE tasks SET name = ?, description = ?, deadline = ? WHERE id = ?;";
 				stmt = c.prepareStatement(statement);
 				stmt.setString(1, taskname);
 				stmt.setString(2, description);
 				stmt.setTimestamp(3, tsmp);
 				stmt.setInt(4, id);
 				stmt.executeUpdate();
 				stmt.close();
 				statement = "DELETE FROM tasks_input WHERE task_id = ?";
 				stmt = c.prepareStatement(statement);
 				stmt.setInt(1, id);
 				stmt.executeUpdate();
 				stmt.close();
 				statement = "INSERT INTO tasks_input VALUES (?,?,?,?)";
 				stmt = c.prepareStatement(statement);
 				for (int i = 0; i < json_input.length(); i++) {
 					String inputSet[] = ((String) json_input.get(Integer.toString(i))).split(";");
 					for (int j = 0; j<inputSet.length; j++){
 						stmt.setInt(1, id);
 						stmt.setInt(2, i);
 						stmt.setInt(3, j);
 						stmt.setString(4, inputSet[j]);
 						stmt.addBatch();
 					}
 				}
 				stmt.executeBatch();
 				stmt.close();
 				statement = "DELETE FROM tasks_output WHERE task_id = ?";
 				stmt = c.prepareStatement(statement);
 				stmt.setInt(1, id);
 				stmt.executeUpdate();
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
 				pw.write("Ülesande muutmine õnnestus!");
 			} catch (SQLException | ClassNotFoundException | JSONException e) {
 				logger.error("Task insertion failed", e);
 				pw.write("Ülesande muutmine ebaõnnestus!");
 				return;
 			}
 
 		}
 		else{
 			logger.warn("Unauthorized access by" + request.getRemoteUser());
 			response.sendRedirect("logout");
 		}
 	}
 
 }
