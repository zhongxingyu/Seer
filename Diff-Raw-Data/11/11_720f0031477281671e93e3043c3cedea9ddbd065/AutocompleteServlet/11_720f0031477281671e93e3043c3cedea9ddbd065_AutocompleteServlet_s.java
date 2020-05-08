 package evalimised;
 import com.google.appengine.api.rdbms.AppEngineDriver;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.*;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 
public class KandidaadileheServlet extends HttpServlet{
 
 		
 	/**
 	 * enne kandidaatservlet
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
       String Name = req.getParameter("Name");
 	  Connection c = null;
 	    try {
 	      DriverManager.registerDriver(new AppEngineDriver());
 	      c = DriverManager.getConnection("jdbc:google:rdbms://netivalimised2013:netivalimised/evalimised", "root", "");
 
 	      if(Name != "" ) {
 	    	  String statement = createQuery(Name);
 	    	  PreparedStatement stmt = c.prepareStatement(statement);
 		      ResultSet rs = stmt.executeQuery();
 		      String jsonData = createJSON(rs);
 	          resp.setContentType("application/json");
 	          resp.setCharacterEncoding("UTF-8");
 	          resp.getWriter().write(jsonData);
 	      }
 	    } 
 	    catch (SQLException e) {
 	        e.printStackTrace();
 	    } 
 	    finally {
 	        if (c != null){
 	        	try {
 					c.close();
 				} 
 	        	catch (SQLException ignore) {}
 	        }
 	    } 
 	    //resp.setHeader("Refresh","3; url=/evalimised.jsp");
 	  }
 	
 	private static String createQuery(String nimi) {
 		String beginning = "SELECT CONCAT_WS(' ',firstname, lastname) as nimi FROM Person where FirstName LIKE ";
		
 		String end="'"+nimi+"%'";
 		String query = beginning + end;
 		System.out.println(query);
 		return query;
 	}
 	
 	private static String createJSON(ResultSet rs){
 	      List<Candidate> candidates = new ArrayList<Candidate>();
 	      try {
 			while(rs.next()){
 			      Candidate candidate = new Candidate();
 			      candidate.setFName(rs.getString("nimi"));
 			      
 			      candidates.add(candidate);
 			  }
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	      Gson gson = new GsonBuilder().create();
           String candidatesJson = gson.toJson(candidates);
           return candidatesJson;
 	}
     
 }
 
