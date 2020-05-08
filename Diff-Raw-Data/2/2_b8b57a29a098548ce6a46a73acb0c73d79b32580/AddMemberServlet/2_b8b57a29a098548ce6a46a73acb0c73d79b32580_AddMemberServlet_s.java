 /*
    Copyright 2012 Marc Lijour
     This file is part of TOPSMDB.
 
     TOPSMDB is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
   
     TOPSMDB is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package tops.servlet;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Servlet implementation class AddMemberServlet
  */
 @WebServlet("/AddMemberServlet")
 public class AddMemberServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(AddMemberServlet.class);
 	private DataSource ds;
 	private String deployDir = "ERROR";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddMemberServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     @Override
     public void init(ServletConfig config) throws ServletException {
     	super.init(config);
     	
     	// deployment directory
     	ServletContext context = getServletContext();
     	deployDir = context.getInitParameter("deploy-dir");	// defined in WEB-INF/web.xml
     	if(!deployDir.contains("TOPS"))
     		logger.warn("The deploy directory (" + deployDir + " does not contain \"TOPS\"");
     	
     	// setting up data source 
         Context ctx;
 		try {
 			ctx = new InitialContext();
 	        ds = (DataSource) ctx.lookup("java:comp/env/jdbc/topsDB");
 	        //logger.debug("hooked up embedded database though JNDI");
 	        
 		} catch (NamingException e) {
 			logger.fatal(e.getMessage());
 		}
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(true);
 		String user = (String)session.getAttribute("user");
 		
 		if(user == null) {
 			logger.debug("user = null");
			response.sendRedirect("/" + deployDir + ""/login.jsp");
 			return;
 		}	
 		
 				
 		String firstname = request.getParameter("firstname");
 		String lastname = request.getParameter("lastname");
 		String jobtitle = request.getParameter("jobtitle");
 		String branch = request.getParameter("branch");
 		String ministry = request.getParameter("ministry");
 		String city = request.getParameter("city");
 		String phone = request.getParameter("phone");
 		String email = request.getParameter("email");
 		String heardfrom = request.getParameter("heardfrom");
 		String creatdate = request.getParameter("creatdate");
 		String chapter = request.getParameter("chapter");
 		
 		
 		
 		Connection conn = null;
 		Statement stmt = null;			// to check for dups first
 		ResultSet results = null;		
 		PreparedStatement pstmt = null; // to insert data
 		
 		try{
 			conn = ds.getConnection();
 	        stmt = conn.createStatement();
 	        
 
 			/*
 			 * 1. Check that no other member has the same email
 			 */
 			results = stmt.executeQuery("select * from members where email='" + email + "'");
 			if(results.next()) {
 				logger.warn("Aborting: already a member with same email: " + email);
 				results.close();
 	            results = null;
 	    		stmt.close(); 
 	    		stmt = null;
 	    		conn.close();
 	    		conn = null;
 	    		
 				response.sendRedirect("/" + deployDir + "/do/error.jsp?dupemail");
 				return;
 			}
 			
 			results.close();
             results = null;
     		stmt.close(); 
     		stmt = null;
 	        
 
 			/*
 			 * 2. Insert
 			 */
 			String sql = "INSERT INTO members (firstname, lastname, jobtitle, branch, ministry, "
 					+	"city, phone, email, heardfrom, creatdate, chapter) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 			pstmt = conn.prepareStatement(sql);
 
 			pstmt.setString(1, firstname);
 			pstmt.setString(2, lastname);
 			pstmt.setString(3, jobtitle);
 			pstmt.setString(4, branch);
 			pstmt.setString(5, ministry);
 			pstmt.setString(6, city);
 			pstmt.setString(7, phone);
 			pstmt.setString(8, email);
 			pstmt.setString(9, heardfrom);
 			pstmt.setString(10, creatdate);
 			pstmt.setString(11, chapter);
 			
 			int i = pstmt.executeUpdate();
 					
 	        String topsmember = new StringBuffer(firstname).append(" ").append(lastname).append(" (").append(email).append(")").toString();
 	        switch(i) {
 	        case 1:
 	        	logger.info("User " + user + " added: " + topsmember);
 	        	break;
 	        	
 	        default:
 	        	logger.error("User " + session.getAttribute("user") + " unable to add " + topsmember);
 	        }
 	        
 	        pstmt.close(); 
     		pstmt = null;
     		conn.close();
     		conn = null;
 	        
 		}catch(SQLException se) {
 			logger.error(se.getMessage());	
 			
 		} finally {
 		    // Always make sure result sets and statements are closed,
 		    // and the connection is returned to the pool
 		    if (results != null) {
 		      try { results.close(); } catch (SQLException e) { ; }
 		      results = null;
 		    }
 		    if (stmt != null) {
 		      try { stmt.close(); } catch (SQLException e) { ; }
 		      stmt = null;
 		    }
 		    if (pstmt != null) {
 			      try { pstmt.close(); } catch (SQLException e) { ; }
 			      pstmt = null;
 			    }
 		    if (conn != null) {
 		      try { conn.close(); } catch (SQLException e) { ; }
 		      conn = null;
 		    }
 		}
 		
 		// Back to display
 		response.sendRedirect("/" + deployDir + "/");
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 }
