 package uk.ac.ic.doc.campusProject.web.servlet;
 
 import java.io.IOException;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import uk.ac.ic.doc.campusProject.utils.db.DatabaseConnectionManager;
 
 public class PlacesApi extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	Logger log = Logger.getLogger(PlacesApi.class);
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
 		Connection conn = DatabaseConnectionManager.getConnection("live");
 		PreparedStatement stmt;
 		String verbose = request.getParameter("verbose");
 		String building = request.getParameter("building");
 		String floor = request.getParameter("floor");
 		String room = request.getParameter("room");
 		String left = request.getParameter("left");
 		String right = request.getParameter("right");
 		String top = request.getParameter("top");
 		String bottom = request.getParameter("bottom");
 		
 		try {
 			if (verbose.equals("F")) {
 				if (left == null || right == null || top == null || bottom == null) {
 					stmt = conn.prepareStatement("SELECT Number, Xpixel, Ypixel FROM Room LEFT JOIN Floor_Contains ON Number=Room AND Room.Building=Floor_Contains.Building LEFT JOIN Building ON Floor_Contains.Building=Building.Name WHERE Building.ShortCode=? AND Floor=?");
 					stmt.setString(1, building);
 					stmt.setString(2, floor);
 					if (stmt.execute()) {
 						ResultSet rs = stmt.getResultSet();
 						response.setContentType("application/json");
 						ServletOutputStream os = response.getOutputStream();
 						StringBuilder jsonArray = new StringBuilder();
 						jsonArray.append("[");
 						while(rs.next()) {
 							JSONObject jsonObject = new JSONObject();
 							String number = rs.getString("Number");
 							jsonObject.put("room", number);
 							jsonObject.put("x", rs.getInt("Xpixel"));
 							jsonObject.put("y", rs.getInt("Ypixel"));
 							jsonArray.append(jsonObject.toString());
 							jsonArray.append(",");
 						}
 						jsonArray = jsonArray.replace(jsonArray.length() - 1, jsonArray.length(), "");
 						jsonArray.append("]");
 						os.write(jsonArray.toString().getBytes());
 						response.setStatus(HttpServletResponse.SC_OK);
 						os.flush();
 					}
 					conn.close();
 				}
 				else {
 					
 				}
 			}
 			else if (verbose.equals("T")) {
 				if ((left == null || right == null || top == null || bottom == null) && room != null) {
 					stmt = conn.prepareStatement("SELECT Number, Type, Description, Image FROM Room LEFT JOIN Floor_Contains ON Room.Number=Floor_Contains.Room AND Room.Building=Floor_Contains.Building LEFT JOIN Building ON Floor_Contains.Building=Building.Name WHERE Building.ShortCode=? AND Floor=?" +
 							" AND Room.Number= ?");
 					stmt.setString(1, building);
 					stmt.setString(2, floor);
 					stmt.setString(3, room);
 					log.info(building + "," + floor + "," + room);
 					if (stmt.execute()) {
 						ResultSet rs = stmt.getResultSet();
 						response.setContentType("application/json");
 						ServletOutputStream os = response.getOutputStream();
						StringBuilder jsonArray = new StringBuilder();
						jsonArray.append("[");
 						while(rs.next()) {
							JSONObject jsonObject = new JSONObject();
 							String number = rs.getString("Number");
 							jsonObject.put("number", number);
 							jsonObject.put("type", rs.getString("Type"));
 							jsonObject.put("description", rs.getString("Description"));
 							Blob blob = rs.getBlob("Image");
 							if (blob == null) {
 								jsonObject.put("image", new byte[]{});
 							}
 							else {
 								jsonObject.put("image", blob.getBytes(1, (int)blob.length()));
 							}
							jsonArray.append(jsonObject.toString());
							jsonArray.append(",");
 							
 						}
						jsonArray = jsonArray.replace(jsonArray.length() - 1, jsonArray.length(), "");
						jsonArray.append("]");
						os.write(jsonArray.toString().getBytes());
 						response.setStatus(HttpServletResponse.SC_OK);
 						os.flush();
 					}
 					conn.close();
 				}
 				else if (room == null){
 					stmt = conn.prepareStatement("SELECT Number, Type, Description, Image FROM Room LEFT JOIN Floor_Contains ON Number=Room AND Room.Building=Floor_Contains.Building LEFT JOIN Building ON Floor_Contains.Building=Building.Name WHERE Building.ShortCode=? AND Floor=?" +
 							" AND Xpixel <= ? AND Xpixel >= ? AND Ypixel <= ? AND Ypixel >= ?");
 					stmt.setString(1, building);
 					stmt.setString(2, floor);
 					stmt.setString(3, right);
 					stmt.setString(4, left);
 					stmt.setString(5, bottom);
 					stmt.setString(6, top);
 					if (stmt.execute()) {
 						ResultSet rs = stmt.getResultSet();
 						response.setContentType("application/json");
 						ServletOutputStream os = response.getOutputStream();
 						StringBuilder jsonArray = new StringBuilder();
 						jsonArray.append("[");
 						while(rs.next()) {
 							JSONObject jsonObject = new JSONObject();
 							String number = rs.getString("Number");
 							jsonObject.put("number", number);
 							jsonObject.put("type", rs.getString("Type"));
 							jsonObject.put("description", rs.getString("Description"));
 							Blob blob = rs.getBlob("Image");
 							if (blob == null) {
 								jsonObject.put("image", new byte[]{});
 							}
 							else {
 								jsonObject.put("image", blob.getBytes(1, (int)blob.length()));
 							}
 							jsonArray.append(jsonObject.toString());
 							jsonArray.append(",");
 							
 						}
 						jsonArray = jsonArray.replace(jsonArray.length() - 1, jsonArray.length(), "");
 						jsonArray.append("]");
 						os.write(jsonArray.toString().getBytes());
 						response.setStatus(HttpServletResponse.SC_OK);
 						os.flush();
 					}
 					conn.close();
 				}
 				
 			}
 			
 		} catch (SQLException | IOException | JSONException e) {
 			e.printStackTrace();
 		}
 		
 		
 		
 	}
 
 }
