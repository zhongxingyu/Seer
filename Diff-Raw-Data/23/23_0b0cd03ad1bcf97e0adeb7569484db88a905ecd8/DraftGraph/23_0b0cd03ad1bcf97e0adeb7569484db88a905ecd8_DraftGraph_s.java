 package de.anycook.api;
 
 import java.sql.SQLException;
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
 import javax.ws.rs.container.AsyncResponse;
 import javax.ws.rs.container.Suspended;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import de.anycook.db.mysql.DBRecipe;
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 
 import de.anycook.db.mongo.recipedrafts.RecipeDrafts;
 import de.anycook.api.drafts.DraftChecker;
 import de.anycook.recipe.Recipe;
 import de.anycook.session.Session;
 import de.anycook.utils.DaemonThreadFactory;
 import de.anycook.utils.JsonpBuilder;
 
 @Path("/drafts")
 public class DraftGraph {
 	private static ExecutorService exec;
     private static final int numThreads = 10;
 	
 	public static void init() {
 		exec = Executors.newCachedThreadPool(DaemonThreadFactory.singleton());
         for(int i = 0; i<numThreads; i++){
         	exec.execute(new DraftChecker());
         }
 	}
 	
 	public static void destroyThreadPool() {
 		exec.shutdownNow();
 	}
 
     private Logger logger;
     @Context HttpHeaders hh;
     @Context HttpServletRequest request;
 
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
 
     @GET
     @Path("num")
     public void getDraftNum(@QueryParam("lastNum") Integer lastNum,
                             @Suspended AsyncResponse asyncResponse){
         Session session = Session.init(request.getSession(true));
        session.checkLogin();
        //DraftChecker.addContext(asyncResponse, lastNum, session.getUser().getId());
     }
 	
 	@PUT
 	@Path("{recipeName}")
 	public String initWithRecipe(@Context HttpHeaders hh,
 			@Context HttpServletRequest request,
 			@PathParam("recipeName") String recipeName,
 			@FormParam("versionid") Integer versionid){
 		if(recipeName == null) throw new WebApplicationException(400);
 		
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		int user_id = session.getUser().getId();
         try {
             return Recipe.initDraftWithRecipe(recipeName, versionid, user_id);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBRecipe.RecipeNotFoundException e) {
             logger.warn(e);
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         }
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
