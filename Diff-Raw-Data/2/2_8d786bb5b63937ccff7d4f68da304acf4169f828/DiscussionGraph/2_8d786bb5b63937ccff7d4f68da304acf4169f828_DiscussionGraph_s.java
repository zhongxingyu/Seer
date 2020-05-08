 package de.anycook.api;
 
 import com.google.common.base.Preconditions;
 import de.anycook.discussion.Discussion;
 import de.anycook.discussion.db.DBDiscussion;
 import de.anycook.session.Session;
 import org.apache.log4j.Logger;
 import org.glassfish.jersey.server.ManagedAsync;
 
 import javax.ejb.Asynchronous;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.container.AsyncResponse;
 import javax.ws.rs.container.Suspended;
 import javax.ws.rs.container.TimeoutHandler;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.sql.SQLException;
 import java.util.concurrent.TimeUnit;
 
 
 @Path("discussion")
 public class DiscussionGraph {
 
     private final Logger logger = Logger.getLogger(getClass());
     @Context HttpHeaders hh;
     @Context HttpServletRequest request;
 
     @GET
     @ManagedAsync
     @Asynchronous
     @Path("{recipeName}")
     @Produces(MediaType.APPLICATION_JSON)
     public void get(@Suspended final AsyncResponse asyncResponse, @PathParam("recipeName") final String recipeName,
                     @QueryParam("lastid") final Integer lastId, @Context HttpServletRequest request){
         Session session = Session.init(request.getSession());
         int userId = -1;
         try{
             session.checkLogin(request.getCookies());
             userId = session.getUser().getId();
         }catch(WebApplicationException e){
             userId = -1;
         } catch (SQLException e) {
             logger.error(e);
             asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
             return;
         }
 
         asyncResponse.setTimeoutHandler(new TimeoutHandler() {
             @Override
             public void handleTimeout(AsyncResponse asyncResponse) {
                 logger.info("reached timeout");
                 asyncResponse.resume(Response.ok().build());
             }
         });
 
         asyncResponse.setTimeout(5, TimeUnit.MINUTES);
 
         while (!asyncResponse.isDone() && !asyncResponse.isCancelled()){
             Discussion newDiscussion;
             try(DBDiscussion dbDiscussion = new DBDiscussion()) {
 
                 newDiscussion = dbDiscussion.getDiscussion(recipeName, lastId, userId);
                 if(newDiscussion.size() > 0){
                     logger.debug("found new disscussion elements for " + recipeName);
                     if(asyncResponse.isSuspended())
                         asyncResponse.resume(Response.ok(newDiscussion, MediaType.APPLICATION_JSON_TYPE).build());
                     break;
                 } else
                    Thread.currentThread().wait(2000);
             } catch (SQLException e) {
                 logger.error(e);
                 if(asyncResponse.isSuspended())
                     asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
                 break;
             } catch (InterruptedException e) {
                 break;
             }
         }
     }
 
 	@POST
 	@Path("{recipeName}")
 	public Response discuss(@PathParam("recipeName") String recipe,
 			@FormParam("comment") String comment, @FormParam("pid") Integer pid){
 		
 		Preconditions.checkNotNull(comment);
 		
 		Session shandler = Session.init(request.getSession());
 		shandler.checkLogin(hh.getCookies());
 		int userid = shandler.getUser().getId();
 
         try {
             if(pid == null) Discussion.discuss(comment, userid, recipe);
             else Discussion.answer(comment, pid, userid, recipe);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
 		return Response.ok("true").build();
 	}
 	
 	@PUT
 	@Path("like/{recipeName}/{id}")
 	public Response like(@PathParam("recipeName") String recipe,
 			@PathParam("id") int id){
 		Session shandler = Session.init(request.getSession());
 		shandler.checkLogin(hh.getCookies());
 		int userid = shandler.getUser().getId();
 
         try {
             Discussion.like(userid, recipe, id);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
         return Response.ok("true").build();
 	}
 	
 	@DELETE
 	@Path("like/{recipeName}/{id}")
 	public Response unlike(@PathParam("recipeName") String recipe,
 			@PathParam("id") int id,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session shandler = Session.init(request.getSession());
 		shandler.checkLogin(hh.getCookies());
 		int userid = shandler.getUser().getId();
 
         try {
             Discussion.unlike(userid, recipe, id);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
         return Response.ok("true").build();
 	}
 }
