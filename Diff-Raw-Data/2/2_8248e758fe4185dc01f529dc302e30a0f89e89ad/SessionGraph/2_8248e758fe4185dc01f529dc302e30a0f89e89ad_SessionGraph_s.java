 package de.anycook.graph;
 
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Cookie;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.NewCookie;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 
 import de.anycook.misc.JsonpBuilder;
 import de.anycook.session.Session;
 import de.anycook.user.User;
 import de.anycook.user.User.Userfields;
 import de.anycook.user.settings.MailSettings;
 import de.anycook.user.settings.Settings;
 import de.anycook.user.settings.MailSettings.Field;
 
 
 @Path("session")
 public class SessionGraph {
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getSession(@Context HttpHeaders hh,
 			@Context HttpServletRequest request,
 			@QueryParam("callback") String callback){
 		Session session = Session.init(request.getSession(true));
 		try{
 			session.checkLogin(hh.getCookies());
 		}catch(WebApplicationException e){
 			return JsonpBuilder.buildResponse(callback, "false");
 		}
 		User user = session.getUser();
 		return JsonpBuilder.buildResponse(callback, user);
 	}
 	
 	@GET
 	@Path("login")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response login(@Context HttpHeaders hh,
 			@Context HttpServletRequest request,
 			@QueryParam("callback") String callback,
 			@QueryParam("username") String username,
 			@QueryParam("password") String password,
 			@QueryParam("stayloggedin") boolean stayloggedin){
 		
 		Session session = Session.init(request.getSession(true));
 		try{
 			session.login(username, password);
 			User user = session.getUser();
 			ResponseBuilder response = Response.ok(JsonpBuilder.build(callback, user));
 			if(stayloggedin){
 				NewCookie cookie = new NewCookie("de.anycook", session.makePermanentCookieId(user.id), "/", ".anycook.de", "", 7 * 24 * 60 *60, true);
 				response.cookie(cookie);				
 			}
 			
 			return response.build();
 		}catch(WebApplicationException e){
 			return JsonpBuilder.buildResponse(callback, "false");
 		}
 	}
 	
 	@GET
 	@Path("logout")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response logout(@Context HttpHeaders hh,
 			@Context HttpServletRequest request,
 			@QueryParam("callback") String callback){
 		Session session = Session.init(request.getSession());
 		Map<String, Cookie> cookies = hh.getCookies();
 		session.checkLogin(hh.getCookies());
 		
 		ResponseBuilder response = Response.ok();
 		if(cookies.containsKey("de.anycook")){
 			Cookie cookie = cookies.get("de.anycook");
 			session.deleteCookieID(cookie.getValue());
 			NewCookie newCookie = new NewCookie(cookie, "", -1, false);
 			response.cookie(newCookie);
 		}
 		session.logout();
 		return response.entity(JsonpBuilder.build(callback, "true")).build();
 	}
 	
 	@GET
 	@Path("couchdb")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getCouchDBCookie(@Context HttpServletRequest request,
 			@QueryParam("callback") String callback){
 		Session session = Session.init(request.getSession());
 		session.checkLogin();
 		User user = session.getUser();
 		String couchdbAuthToken = user.getCouchDBAuthToken();
		Cookie cookie = new Cookie("AuthSession", couchdbAuthToken, "/", ".de.anycook.de");
 		return Response.ok(JsonpBuilder.build(callback, true)).cookie(new NewCookie(cookie))
 				.build();
 		
 	}
 	
 	
 	//settings
 	
 	@GET
 	@Path("settings")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getSettings(@Context HttpServletRequest request,
 			@QueryParam("callback") String callback){
 		Session session = Session.init(request.getSession());
 		User user = session.getUser();
 		MailSettings mailsettings = MailSettings.init(user.id);
 		Map<String, Settings> settings = new HashMap<>();
 		settings.put("mail", mailsettings);
 		return JsonpBuilder.buildResponse(callback, settings);
 	}
 	
 	@POST
 	@Path("settings/account")
 	public Response changeAccountSettings(@Context HttpServletRequest request,
 			@Context HttpHeaders hh,
 			@FormParam("username") String username,
 			@FormParam("text") String text,
 			@FormParam("place") String place){
 		Session session = Session.init(request.getSession());
 		ResponseBuilder response = null;
 		try{
 			session.checkLogin(hh.getCookies());
 			
 			User user = session.getUser();
 			user.changeSetting(Userfields.NAME, username);
 			user.changeSetting(Userfields.TEXT, text);
 			user.changeSetting(Userfields.PLACE, place);
 			response = Response.ok();
 		}catch(WebApplicationException e){
 			response = Response.status(401);
 		}
 		
 		return response.build();
 		
 	}
 	
 	@POST
 	@Path("settings/mail/{type}")
 	public void changeMailSettings(@Context HttpServletRequest request,
 			@Context HttpHeaders hh, 
 			@PathParam("type") String type,
 			@FormParam("value") boolean value){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		MailSettings settings = MailSettings.init(session.getUser().id);
 		if(type.equals("all")){
 			settings.changeAll(value);
 		}else{
 			settings.change(Field.valueOf(type), value);
 		}		
 	}
 }
