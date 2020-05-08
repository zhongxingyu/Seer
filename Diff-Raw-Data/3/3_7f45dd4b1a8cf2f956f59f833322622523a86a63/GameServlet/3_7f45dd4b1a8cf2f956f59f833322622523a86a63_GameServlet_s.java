 package com.cloudbees.service;
 
 import java.io.StringWriter;
 
 import javax.servlet.http.HttpServlet;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.StatusType;
 
 import com.cloudbees.model.Game;
 import com.google.gson.stream.JsonWriter;
 
 @Path("/game")
 public class GameServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = 1L;
 	private MongoDAO dao = new MongoDAO();
 	
 	@GET
     @Path("{id}")	
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getGame(@PathParam("id") String id ) {
 		
 		StatusType statusCode = null;
 		String msg = null;
 		
 		try {
 			dao.connect();	
 			msg = dao.getGame( id );
 			
 			if ( msg != null )
 				statusCode = Response.Status.OK;
 			else
 				// IllegalArgumentException/NullPointerException
 				statusCode = Response.Status.BAD_REQUEST;
 		}		
 		catch (Exception e) {
 			e.printStackTrace();
 
 			// Others: Return 500 Internal Server Error
     		statusCode = Response.Status.INTERNAL_SERVER_ERROR;			
 		}
 		finally {
 			dao.getMongo().close();
 		}
 
 		if (statusCode != Response.Status.OK)
 			return Response.status(statusCode).build();
 		else
 			return Response.status(statusCode).entity(msg).build();		
 	}
 	
 	@POST
     @Path("new")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response newGame(Game game) {
 		
 		StatusType statusCode = null;
 		String msg = null;
 		StringWriter sw = new StringWriter();
 		JsonWriter writer = new JsonWriter(sw);
 
 		try {			
 		    dao.connect();
 
 		    // Create a new game (key = game id)
 			String id = dao.newGame( game );
 			if (id == null) {
     			// Return 400 Bad Request
 	    		statusCode = Response.Status.BAD_REQUEST;
 			} else {	
 				writer.beginObject();
 				writer.name("id").value( id );
 				writer.endObject();
 				writer.close();
 
 				statusCode = Response.Status.OK;
 				msg = sw.toString();
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 
 			// Return 500 Internal Server Error
     		statusCode = Response.Status.INTERNAL_SERVER_ERROR;
 		}
 		finally {
			//dao.getMongo().close();
 		}
 
 		if (statusCode != Response.Status.OK)
 			return Response.status(statusCode).build();
 		else
 			return Response.status(statusCode).entity(msg).build();	
 	}
 	
 	public MongoDAO getDao() {
 		return dao;
 	}
 	public void setDao(MongoDAO dao) {
 		this.dao = dao;
 	}
 }
