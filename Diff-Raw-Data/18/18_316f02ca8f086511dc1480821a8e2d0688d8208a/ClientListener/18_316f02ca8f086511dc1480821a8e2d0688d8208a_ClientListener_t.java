 package de.rallye.rest;
 
 import java.io.IOException;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import de.rallye.model.ScottlandYardRallye;
 import de.rallye.resource.DataHandler;
 import de.rallye.resource.exceptions.SQLHandlerException;
 
 /**
  * @author Felix Hbner
  * @version 1.0
  * 
  */
 @Path("/StadtRallye")
 public class ClientListener {
 	
 	private Logger logger =  LogManager.getLogger(ClientListener.class.getName());
 
 
 	private DataHandler data = new ScottlandYardRallye();
 
 	// ==================================================================//
 	// Map Commands
 	// ==================================================================//
 
 	@Path("map/get/nodes")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getAllNodes() {
 		logger.entry();
 		// return
 		// Response.status(201).entity(MapHandler.getAllNodes(this.data).toString()).build();
 		return logger.exit(this.data.getAllNodes());
 	}
 
 	// ==================================================================//
 	// Chat Commands
 	// ==================================================================//
 
 	@Path("chat/get")
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getChatEntries(JSONObject o) throws SQLHandlerException, JSONException {
 		logger.entry();
 		return logger.exit(this.data.getChatEntries(o));
 	}
 
 	@Path("chat/add")
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response setNewChatEntry(JSONObject o) {
 		logger.entry();
 		return logger.exit(this.data.setNewChatEntry(o));
 	}
 
 	// ==================================================================//
 	// Picture Commands
 	// ==================================================================//
 
 	@Path("pic/add")
 	@POST
 	@Consumes("image/jpeg")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response addPicture(byte[] pic) throws JSONException {
 		logger.entry();
 		return logger.exit(this.data.setPicture(pic));
 	}
 	
 	@Path("pic/get/{picID}/{size}")
 	@GET
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	@Produces("image/jpeg")
 	public Response returnPicture(@PathParam("picID") int picID, @PathParam("size") String size) {
 		logger.entry();
 		char s = size.toLowerCase().charAt(0);
 		if (s == 't' || s == 's' || s == 'l') {
 			return logger.exit(this.data.returnPic(picID, s));
 		}
 		else {
 			return logger.exit(Response.status(400).entity("size is not valid").build());
 		}
 	}
 
 	// ==================================================================//
 	// User Commands
 	// ==================================================================//
 
 	@Path("user/register")
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces({MediaType.APPLICATION_JSON , MediaType.TEXT_HTML})
 	public Response registerUser(JSONObject req) {
 		logger.entry();
 		return logger.exit(this.data.registerUser(req));
 	}
 	
 	@Path("user/unregister")
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_HTML)
 	public Response unregisterUser(JSONObject req) {
 		logger.entry();
 		return logger.exit(this.data.unregisterUser(req));
 	}
 
 	// ==================================================================//
 	// Other Commands
 	// ==================================================================//
 
	@Path("status")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(JSONObject o) {
 		logger.entry();
		return logger.exit(this.data.getStatus(o));
 	}
 
 	/*@Path("postPic")
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	public JSONObject test(byte[] pic) {
 		logger.entry();
 		JSONObject o = new JSONObject();
 
 		try {
 			o.put("pic1", ClientListener.hexEncode(pic));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return logger.exit(o);
 	}*/
 
 	// ==================================================================//
 	// Helper Methods
 	// ==================================================================//
 
 	private static Appendable hexEncode(byte buf[], Appendable sb) {
 		// final Formatter formatter = new Formatter(sb);
 		for (int i = 0; i < buf.length; i++) {
 			int low = buf[i] & 0xF;
 			int high = (buf[i] >> 8) & 0xF;
 			try {
 				sb.append(Character.forDigit(high, 16));
 				sb.append(Character.forDigit(low, 16));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return sb;
 	}
 
 	private static String hexEncode(byte buf[]) {
 		String s = hexEncode(buf, new StringBuilder()).toString();
 		// System.out.println(s);
 		return s;
 	}
 
 	/**
 	 * this method will respond to a chat event.
 	 * 
 	 * @param type
 	 * @return
 	 * @author Felix Hbner
 	 */
 	/*
 	 * @Path("chat/{command}")
 	 * 
 	 * @GET
 	 * 
 	 * @Produces({"text/plain", "application/json"}) public Response
 	 * getChatEvent(@PathParam("command") String command) { command =
 	 * command.toLowerCase();
 	 * 
 	 * //if (false) { //return this.data.map.getAllNodes(); //} else {
 	 * 
 	 * StringBuilder str = new StringBuilder();
 	 * str.append("Respond to CHAT-Event"); return
 	 * Response.status(201).entity(str.toString()).build(); //} }
 	 */
 	/**
 	 * this method will respond to a map event.
 	 * 
 	 * @param type
 	 * @return
 	 * @author Felix Hbner
 	 */
 	/*
 	 * @Path("map/{command}")
 	 * 
 	 * @GET //@Produces("application/json")
 	 * 
 	 * @Produces(MediaType.APPLICATION_JSON) public Response
 	 * getMapEvent(@PathParam("command") String command) { //error handling (the
 	 * boolean must be an AND to be sure to stop checking if data does not
 	 * exist) if (this.data == null) { throw new WebApplicationException(500);
 	 * //return Response.status(500).entity("data does not exist!!!").build(); }
 	 * command = command.toLowerCase();
 	 * 
 	 * 
 	 * if (command.equals("getallnodes")) { return
 	 * Response.status(201).entity(MapHandler
 	 * .getAllNodes(this.data).toString()).build(); } else { //StringBuilder str
 	 * = new StringBuilder();
 	 * //str.append("Respond to MAP-Event on host: http://"
 	 * +this.data.getUri()+":"+this.data.getPort()); throw new
 	 * WebApplicationException(400); //return
 	 * Response.status(201).entity(str.toString()).build(); } }
 	 */
 
 	// only for debug
 	/*
 	 * @Path("{x: .*}")
 	 * 
 	 * @GET public Response getOtherEvent() { return
 	 * Response.status(404).entity("Unknown Event!!").build(); //throw new
 	 * WebApplicationException(404); }
 	 */
 
 	// @Consumes(MediaType.APPLICATION_JSON)
 	// @Produces(MediaType.APPLICATION_JSON)
 }
