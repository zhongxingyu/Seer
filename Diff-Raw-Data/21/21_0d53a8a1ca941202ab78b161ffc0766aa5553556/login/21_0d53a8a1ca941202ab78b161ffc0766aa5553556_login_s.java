 package src.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.json.JSONException;
import org.json.JSONObject;
 
 import src.utils.DBManager;
 
 
 public class login extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public login() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String user = request.getParameter("username");
 		String pass = request.getParameter("password");
 		DBManager db = new DBManager();
 		Connection conn = null;
 		PrintWriter out = response.getWriter();
 		JSONObject obj = new JSONObject();
 		System.out.println("[login servlet] Attempting connection. Username: " + user + " Password: " + pass);
 
 		try{
 			conn = db.connect(user, pass);
 			if(!conn.isClosed()){
 				System.out.println("[Login]Connection is valid!");
 
 				if(user.equalsIgnoreCase("admin")){
 					obj.put("Response", "admin.jsp");
 					System.out.println("Admin logged in!");
 				}else if (!user.isEmpty()){
 					obj.put("Response", "mydata.jsp");
 					System.out.println("User logged in!");
 				}else{
 					obj.put("Response", "Invalid");
 					System.out.println("Invalid login.");	
 				}
 			}else{
 				System.out.println("[Login]Invalid Connection!");
 				obj.put("Response", "Invalid");
 				System.out.println("Invalid login.");
 			}
		} catch (JSONException e) {
			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			
 		}
 		response.setContentType("application/json");
 		response.getWriter().write(obj.toString());     
 	}
 
 }
