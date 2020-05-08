 package gov.usgs;
 
 import gov.usgs.URLUtil;
 
 import java.io.IOException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 
 public class BBoxServlet extends HttpServlet {
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("application/json");   
 
 		ServletOutputStream os = resp.getOutputStream();
 		String params = req.getQueryString();
 		URL baseUrl = new URL("http",req.getServerName(),req.getServerPort(),req.getContextPath() + "/templates");
   	
 		Statement statement = null;
 		ResultSet rset = null;
 		Connection connection = null;
 		
 		try {
 
 			String query = URLUtil.getResponseAsStringFromURL(baseUrl.toExternalForm() + "/base_query.jsp;jsessionid=" + req.getSession().getId(), params + "&queryId=bbox");
System.out.println(query);
			
 			Context ctx = new InitialContext();
 			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/gwDataPortalUserDS");
 			connection = ds.getConnection();
 
 			statement = connection.createStatement();
 			statement.setMaxRows(1);
 			rset = statement.executeQuery(query);      
 			if (rset.next()) {  
 				
 				
 				os.print("{\n" + 
 						"bbox: \"" + rset.getString("bbox") + "\",\n" + 
 						"count: " + rset.getString("num_points") + ",\n" +
 						"query: \"" + query + "\"\n" +
 					"}"
 				);
 				
 			}
 			
 			statement.close();
 			statement = null;
 			rset.close();  
 			rset = null;
 			connection.close();  
 			connection = null;
 		} catch (Exception e) {
 			System.out.println("gw data portal map - fit bbox servlet - query data retrieved failed");   
 			e.printStackTrace();
 		} finally {
 	    if (rset != null) {
 	      try { rset.close(); } catch (SQLException e1) { ; }
 	      rset = null;
 	    }
 	    if (statement != null) {
 	      try { statement.close(); } catch (SQLException e2) { ; }
 	      statement = null;
 	    }
 	    if (connection != null) {
 	      try { connection.close(); } catch (SQLException e3) { ; }
 	      connection = null;
 	    }
 		}      
 	}
 }
