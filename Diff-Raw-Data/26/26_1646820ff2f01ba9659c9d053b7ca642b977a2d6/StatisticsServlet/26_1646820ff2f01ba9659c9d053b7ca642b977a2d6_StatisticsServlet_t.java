 package evalimised;
 
 import channel.StatisticsUpdater;
 
 import com.google.appengine.api.rdbms.AppEngineDriver;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.*;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 
 public class StatisticsServlet extends HttpServlet{
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
       String option = req.getParameter("option");
 
 	  Connection c = null;
 	    try {
 	      DriverManager.registerDriver(new AppEngineDriver());
 	      c = DriverManager.getConnection("jdbc:google:rdbms://netivalimised2013:netivalimised/evalimised");
 
 	      if(option != null || option != "") {
 	    	  String statement = createQuery(option);
 	    	  PreparedStatement stmt = c.prepareStatement(statement);
 		      ResultSet rs = stmt.executeQuery();
 		      String jsonData = createJSON(rs, option);
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
 	  }
 	
 	public static String createQuery(String option) {
 		String query = "";
 		if(option.equals("region")){	
			query = "SELECT Area.AreaName, COUNT(Vote.Vote_Id) AS Votes FROM Area LEFT JOIN Person ON Person.AreaID=Area.Area_Id " +
					"LEFT JOIN Vote ON Person.Person_Id = Vote.PersonID  GROUP BY Area_Id ORDER BY Votes DESC";
 		}
 		else if(option.equals("party")){
			query = "SELECT Party.PartyName, COUNT(Vote.Vote_Id) AS Votes FROM Party LEFT JOIN Person ON Person.PartyID=Party.Party_Id " +
					"LEFT JOIN Vote ON Person.Person_Id = Vote.PersonID  GROUP BY Party_Id ORDER BY Votes DESC";
 		}
 		else if(option.equals("candidate")){
			query = "SELECT Person.FirstName, Person.LastName, Area.AreaName, Party.PartyName, COUNT(Vote.Vote_Id) AS Votes " +
					"FROM Person LEFT JOIN Vote ON Person.Person_Id = Vote.PersonID LEFT JOIN Party ON Person.PartyID=Party.Party_Id " +
					"LEFT JOIN Area ON Person.AreaID=Area.Area_Id GROUP BY Person_Id ORDER BY Votes DESC";	
		}
		else if(option.equals("regionbyparty")){
			query = "SELECT Party.PartyName, Area.AreaName, COUNT(Vote.Vote_Id) AS Votes FROM Area " +
					"LEFT JOIN Person ON Person.AreaID=Area.Area_Id LEFT JOIN Party ON Person.PartyID=Party.Party_Id " +
					"LEFT JOIN Vote ON Person.Person_Id = Vote.PersonID  GROUP BY 1,2 ORDER BY AreaName DESC";
		}
		else if(option.equals("partybyregion")){
			query ="SELECT Party.PartyName, Area.AreaName, COUNT(Vote.Vote_Id) AS Votes FROM Party " +
					"LEFT JOIN Person ON Person.PartyID=Party.Party_Id LEFT JOIN Area ON Person.AreaID=Area.Area_Id " +
					"LEFT JOIN Vote ON Person.Person_Id = Vote.PersonID  GROUP BY 1,2 ORDER BY PartyName DESC";
 		}
 		System.out.println(query);
 		return query;
 	}
 	
 	public static String createJSON(ResultSet rs, String option){
 	      List<VoteData> data = new ArrayList<VoteData>();
 	      try {
 			while(rs.next()){
 			      VoteData element = new VoteData();
 			      if(option.equals("party")){
 			    	  element.setParty(rs.getString("PartyName"));
 			      }
 			      else if(option.equals("region")){
 			    	  element.setRegion(rs.getString("AreaName"));
 			      }
 			      else if(option.equals("candidate")){
 			    	  element.setName(rs.getString("FirstName") + " " + rs.getString("LastName"));
 			    	  element.setParty(rs.getString("PartyName"));
 			    	  element.setRegion(rs.getString("AreaName"));
 			      }
 			      element.setVotes(rs.getInt("Votes"));
 			      data.add(element);
 			  }
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	    Gson gson = new GsonBuilder().create();
         String candidatesJson = gson.toJson(data);
         return candidatesJson;
 	}
     
 }
