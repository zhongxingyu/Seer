 package evalimised;
 import com.google.appengine.api.rdbms.AppEngineDriver;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.*;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 
 public class SearchServlet extends HttpServlet{
 
 		
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
       String fName = req.getParameter("fName");
       String lName = req.getParameter("lName");
       String party = req.getParameter("party");
       String area = req.getParameter("area");
 
 	  Connection c = null;
 	    try {
 	      DriverManager.registerDriver(new AppEngineDriver());
 	      c = DriverManager.getConnection("jdbc:google:rdbms://netivalimised2013:netivalimised/evalimised");
 	      String statement;
	      if((fName.equals("") || fName == null) && (lName.equals("") || lName == null) && 
	    		  (party.equals("") || party == null) && (area.equals("")|| area == null)) {
 	    	  System.out.println("Getting all candidates");
 	    	  statement = "SELECT Person.FirstName, Person.LastName, Party.PartyName, Area.AreaName " +
 	    	  		"FROM Person JOIN Party ON Person.PartyID = Party.Party_Id JOIN Area ON Person.AreaID = Area.Area_Id";
 	      }
 	      else
 	    	  statement = createQuery(fName,lName,party, area);
     	  PreparedStatement stmt = c.prepareStatement(statement);
 	      ResultSet rs = stmt.executeQuery();
 	      String jsonData = createJSON(rs, party,area);
           resp.setContentType("application/json");
           resp.setCharacterEncoding("UTF-8");
           resp.getWriter().write(jsonData);
 
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
 	
 	private static String createQuery(String fname, String lname, String party, String area) {
 		String beginning = "SELECT Person.FirstName, Person.LastName";
 		String middle = "";
 		String end = "WHERE ";
		if(!(fname.equals("")) && fname != null)
			end += "CONCAT(FirstName, ' ', LastName) LIKE '%"+ fname + "%' AND ";	
		if(!(lname.equals("")) && lname != null)
 			end += "LastName=\""+lname+"\" AND ";
		if(!(party.equals("")) && party != null) {
 			beginning += ", Area.AreaName";
 			end += "PartyID=\""+party+"\" AND ";
 		}
		if(!(area.equals("")) && area != null) {
 			beginning += ", Party.PartyName";
 			end += "AreaID=\""+area+"\" AND ";
 		}
		if(party.equals("") && area.equals("") && party != null && area != null){
 			beginning += ", Area.AreaName";
 			beginning += ", Party.PartyName";
 		}
 		middle += "JOIN Party ON Person.PartyID = Party.Party_Id ";
 		middle += "JOIN Area ON Person.AreaID = Area.Area_Id ";
 		//remove last " AND "
 		end = end.substring(0, end.length() - 5);
 		beginning +=" FROM Person ";
 		String query = beginning + middle + end;
 		System.out.println(query);
 		return query;
 	}
 	
 	private static String createJSON(ResultSet rs, String party, String area){
 	      List<Candidate> candidates = new ArrayList<Candidate>();
 	      try {
 			while(rs.next()){
 			      Candidate candidate = new Candidate();
 			      candidate.setFName(rs.getString("FirstName"));
 			      candidate.setLName(rs.getString("LastName"));
			      if(party.equals("") || party == null){
 			    	  candidate.setParty(rs.getString("PartyName"));
 			      }
			      if(area.equals("") || area == null){
 			    	  candidate.setArea(rs.getString("AreaName"));
 			      }
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
 
