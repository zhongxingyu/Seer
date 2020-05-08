 package com.nearme;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 
 /**
  * Servlet to handle deletion of POIs from the database
  * 
  * TODO does no checking to ensure you can only delete your own POIs.
  * Either a massive security flaw or a wikipedia-style "new model".
  * 
  */
 public class DeletePOIServlet extends GenericNearMeServlet {
 
 	private static final String DELETE_POI_SQL = "DELETE FROM poi WHERE id = ?";
 
 	private static Logger logger = Logger.getLogger(DeletePOIServlet.class);
 	private static final long serialVersionUID = -5719580871373727060L;
 	
	//TODO move the database related code into DatabasePoiFinder
	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String path = request.getPathInfo();
 		logger.debug("doPost() path="+path);
 		if (path.length()>1) path = path.substring(1, path.length());
 		int id = Integer.parseInt(path);
 
 		Connection conn = null;
 		PreparedStatement st = null;
 		try
 		  {
 			conn = datasource.getConnection();
 			st = conn.prepareStatement(DELETE_POI_SQL);
 			st.setInt(1, id);
 			st.executeUpdate();
 						
 			logger.info("deleted poi " + id);
 			response.sendError(HttpServletResponse.SC_OK);
 		  } catch (SQLException e) {
 			  response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			  logger.error(e);
 			  e.printStackTrace(); 
 		  } finally {
 			  try {
 				  if (st!=null) st.close();
 				  if (conn!=null) conn.close();
 			  } catch (SQLException e) {
 				  e.printStackTrace();
 			  }
 		  }
 	}
 
 }
