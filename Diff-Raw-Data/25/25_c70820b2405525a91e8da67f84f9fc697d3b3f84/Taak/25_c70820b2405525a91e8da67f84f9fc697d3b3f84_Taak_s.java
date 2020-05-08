 package chronomatic.server;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.ResultsetConverter;
 
 import chronomatic.database.Database;
 import chronomatic.database.DatabaseContainer;
 
 @Path("task/")
 public class Taak {
 	
 	@GET
 	@Path("create/{sessionKey}/{naam}/{beginTijd}/{estimatedEnd}/{commentaar}/{projectID}/{voltooid}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String createTaak(@PathParam("sessionKey") String sessionKey,@PathParam("beginTijd") int beginTijd, @PathParam("naam") String naam, @PathParam("estimatedEnd") int estimatedEnd, @PathParam("commentaar") String commentaar, @PathParam("projectID") int projectID,@PathParam("voltooid") boolean voltooid) {
 		// Om een taak te maken moeten er enkele voorwaarden voltooid zijn: 
 		//  - Project moet toebehoren aan gebruiker
 		//  - gebruiker moet ingelogd zijn
 		
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		String query = "INSERT INTO taken (naam, begin_tijd,verwacht_eind,commentaar,projecten_ID,voltooid) VALUES('" + naam + "',"  + beginTijd + "," + estimatedEnd + ",'" + commentaar + "','" + projectID + "','" + userID + "')";
 		
 		JSONArray returnObject = new JSONArray();
 		
 		try{
 			if(userID > 0) { 
 				ResultSet rs = Database.executeGetQuery(con,query);
 				System.out.println(rs);
 				returnObject = ResultsetConverter.convert(rs);
 			}
 			else{ 
 				JSONObject error = new JSONObject();
 				error.put("error","Session is not valid");
 				returnObject.put(error);
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		return returnObject.toString();
 	}
 	
 	@GET
 	@Path("read/{sessionKey}/{taakID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String readTask(@PathParam("sessionKey") String sessionKey,@PathParam("taakID") int taakID) 
 	{ 
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		
 		JSONArray returnObject = new JSONArray();
 		
 		try {
 			if(userID > 0) { 
 				String query = "SELECT begin_tijd,verwacht_eind,commentaar,projecten_ID,voltooid FROM taken AS T INNER JOIN projecten AS P ON T.ID = " + taakID + " AND P.gebruikers_ID = " + userID + " AND T.projecten_ID = P.ID";
 				ResultSet rs = Database.executeQuery(con,query);
 				
 				returnObject = ResultsetConverter.convert(rs);
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		
 		return returnObject.toString();
 	}
 	
 	@GET
	@Path("update/{sessionKey}/{beginTijd}/{estimatedEnd}/{commentaar}/{taakID}/{voltooid}")
 	@Produces(MediaType.APPLICATION_JSON)
	public String updateTaak(@PathParam("sessionKey") String sessionKey,@PathParam("beginTijd") int beginTijd, @PathParam("estimatedEnd") int estimatedEnd, @PathParam("commentaar") String commentaar,@PathParam("voltooid") boolean voltooid, @PathParam("taakID") int taakID) {
 		
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		
 		try {
 			if(userID > 0) { 
 				// Conditionele query
				String query = "UPDATE taken AS T, projecten AS P SET T.begin_tijd = "+beginTijd+", T.verwacht_eind = "+estimatedEnd+", T.commentaar = '"+commentaar+"', T.voltooid = "+voltooid+" WHERE T.ID = "+taakID+" AND P.ID = T.projecten_ID AND P.gebruikers_ID = "+userID;
 				System.out.println(query);
 				if(Database.executeNullQuery(con,query))
 					return "[{\"success\":true}]";
 				else 
 					return "[{\"success\":false}]";
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		
 		return "[{\"success\":false}]";
 	}
 	
 	@GET
 	@Path("delete/{sessionKey}/{taakID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String destroyTask(@PathParam("sessionKey") String sessionKey,@PathParam("taakID") int taakID) 
 	{ 
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		
 		JSONArray returnObject = new JSONArray();
 		
 		try {
 			if(userID > 0) { 
				String query = "DELETE FROM taken WHERE ID = " + taakID + " AND (SELECT COUNT(P.ID) FROM projecten AS P INNER JOIN taken AS T ON T.ID = " + taakID + " AND T.projecten_ID = T.ID AND P.gebruikers_ID = " + userID + ") > 0";
 				if(Database.executeNullQuery(con,query))
 					return "[{\"success\":true}]";
 				else 
 					return "[{\"success\":false}]";
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		
 		return returnObject.toString();
 	}
 	
 	
 	
 	@GET 
 	@Path("tijdspannes/{sessionKey}/{taakID}/{pauze}")
 	@Produces(MediaType.APPLICATION_JSON) 
 	public String getSessions(@PathParam("sessionKey") String sessionKey, @PathParam("taakID") int taakID, @PathParam("pauze") boolean pauze) { 	
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		String query = "SELECT G.ID, begin_uur, eind_uur, pauze FROM projecten AS P INNER JOIN taken AS T INNER JOIN tijdspanne AS G ON P.gebruikers_id = " + userID + " AND  T.ID = " + taakID + " AND P.ID = T.projecten_ID AND T.ID = G.taken_ID WHERE G.pauze = " + pauze;	
 
 		JSONArray returnObject = new JSONArray();
 		try{ 
 			if(userID > 0) { 
 				ResultSet rs = Database.executeQuery(con, query);
 				
 				returnObject = ResultsetConverter.convert(rs);			
 			}
 			else { 
 				JSONObject error = new JSONObject();
 				error.put("error","Invalid session");
 				returnObject.put(error);
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		
 		return returnObject.toString();
 	}
 }
