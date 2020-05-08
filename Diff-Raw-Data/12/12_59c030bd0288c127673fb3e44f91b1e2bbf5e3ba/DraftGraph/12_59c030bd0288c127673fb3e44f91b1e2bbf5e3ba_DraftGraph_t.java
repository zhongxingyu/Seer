 package de.anycook.graph;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 
 import de.anycook.db.mongo.RecipeDrafts;
 import de.anycook.graph.drafts.DraftChecker;
 import de.anycook.session.Session;
 import de.anycook.utils.DaemonThreadFactory;
 import de.anycook.utils.JsonpBuilder;
 
 @Path("/drafts")
 public class DraftGraph {
 	
 	
 	
 	private Logger logger;
 	
 	private static ExecutorService exec;
     private static final int numThreads = 10;
 	
 	public static void init() {
 		exec = Executors.newCachedThreadPool(DaemonThreadFactory.singleton());
         for(int i = 0; i<numThreads; i++){
         	exec.execute(new DraftChecker());
         }
 	}
 	
 	public static void stop() {
 		exec.shutdownNow();
 	}
 	
 	public DraftGraph(){
 		logger = Logger.getLogger(getClass());
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response get(@QueryParam("callback") String callback,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		RecipeDrafts drafts = new RecipeDrafts();
 		List<JSONObject> list = drafts.getAll(session.getUser().getId());
 		drafts.close();
 		return JsonpBuilder.buildResponse(callback, list);
 	}
 	
 	@PUT
 	public String newDraft(@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		RecipeDrafts recipeDrafts = new RecipeDrafts();
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		return recipeDrafts.newDraft(session.getUser().getId());
 	}
 	
 //	@GET
 //	@Path("num")
 //	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 //	public Response getDraftNumber(@QueryParam("callback") String callback,
 //			@Context HttpHeaders hh,
 //			@Context HttpServletRequest request){
 //		Session session = Session.init(request.getSession());
 //		session.checkLogin(hh.getCookies());
 //		RecipeDrafts drafts = new RecipeDrafts();
 //		int draftNum = drafts.count(session.getUser().getId());
 //		drafts.close();
 //		return JsonpBuilder.buildResponse(callback, draftNum);
 //	}
 	
 	@GET
 	@Path("{id}")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getDraft(@PathParam("id") String draft_id,
 			@QueryParam("callback") String callback,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		RecipeDrafts recipeDrafts = new RecipeDrafts();
 		int userid = session.getUser().getId();
 		
 		try {
 			JSONObject json = recipeDrafts.loadDraft(draft_id, userid);			
 			return JsonpBuilder.buildResponse(callback, json.toJSONString());
 		} catch (ParseException e) {
 			logger.error(e);
 			throw new WebApplicationException(400);
 		} finally{
 			recipeDrafts.close();
 		}
 		
 	}
 	
 	@POST
 	@Path("{id}")
	public Response setData(@FormParam("data") String data, 
 			@PathParam("id") String draft_id,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){		
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		JSONParser parser = new JSONParser();
 		RecipeDrafts drafts = new RecipeDrafts();
 		try {
 			JSONObject json = (JSONObject)parser.parse(data);
 			
 			int userid = session.getUser().getId();
 			drafts.update(json, userid, draft_id);
 			
 		} catch (ParseException e) {
 			throw new WebApplicationException(400);
 		} finally{
 			drafts.close();
 		}
		
		return Response.ok("true").build();
 	}
 	
 	@DELETE
 	@Path("{id}")
 	public void remove(@PathParam("id") String draft_id,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		RecipeDrafts recipeDrafts = new RecipeDrafts();
 		int user_id = session.getUser().getId();
 		recipeDrafts.remove(user_id, draft_id);
 		recipeDrafts.close();
 	}
 	
 }
