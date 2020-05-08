 package evalimised;
 import com.google.appengine.api.rdbms.AppEngineDriver;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.http.*;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 
public class UserdataServlet extends HttpServlet{
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		
 	  PrintWriter out = resp.getWriter();
 	  Connection c = null;
 	    try {
 	      DriverManager.registerDriver(new AppEngineDriver());
 	      c = DriverManager.getConnection("jdbc:google:rdbms://netivalimised2013:netivalimised/evalimised");//, "root", "");
 	      
 	      String voterID = req.getParameter("voterID");
 	      System.out.println(voterID);
     	  String statement = createQuery(voterID);
     	  PreparedStatement stmt = c.prepareStatement(statement);
 	      ResultSet rs = stmt.executeQuery();
 	      List<Candidate> candidates = new ArrayList<Candidate>();
 	      while(rs.next()){
                 Candidate candidate = new Candidate();
                 candidate.setFName(rs.getString("FirstName"));
                 candidate.setLName(rs.getString("LastName"));
                 candidate.setParty(rs.getString("PartyName"));
                 candidate.setArea(rs.getString("AreaName"));
                 candidates.add(candidate);
 	      
 
 		      Gson gson = new GsonBuilder().create();
 	          String categoriesJson = gson.toJson(candidates);
 	          resp.setContentType("application/json");
 	          resp.setCharacterEncoding("UTF-8");
 	          resp.getWriter().write(categoriesJson);
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
 	
 	private static String createQuery(String voterID) {
 		String kogu="select FirstName,LastName,PartyName,AreaName from Person inner join Party,Area where Area.Area_Id=Person.AreaID and Person.PartyID=Party_Id and Person_Id=( SELECT votedForID from users where UserName="+voterID+")";
 		
 		String query = kogu;
 		System.out.println(query);
 		return query;
 	}
     
 }
 
