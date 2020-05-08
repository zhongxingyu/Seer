 package de.anycook.graph;
 
 import java.util.HashMap;
 import java.util.Map;
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
 import javax.ws.rs.core.Cookie;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.NewCookie;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 
 import org.apache.log4j.Logger;
 
 import de.anycook.utils.JsonpBuilder;
 import de.anycook.mailprovider.MailProvider;
 import de.anycook.session.Session;
 import de.anycook.user.User;
 import de.anycook.user.settings.MailSettings;
 import de.anycook.user.settings.Settings;
 
 
 @Path("session")
 public class SessionGraph {
 	
 	private final Logger logger;
 	
 	public SessionGraph() {
 		logger = Logger.getLogger(getClass());
 	}
 	
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
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
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
 				NewCookie cookie = new NewCookie("de.anycook", session.makePermanentCookieId(user.getId()), "/", ".anycook.de", "", 7 * 24 * 60 *60, true);
 				response.cookie(cookie);				
 			}
 			
 			return response.build();
 		}catch(WebApplicationException e){
 			return JsonpBuilder.buildResponse(callback, "false");
 		}
 	}
 	
 	@GET
 	@Path("logout")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
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
 	
 	@POST
 	@Path("activate")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response activateAccount(@FormParam("activationkey") String activationKey){
 		boolean check = User.activateById(activationKey);
 		return Response.ok(Boolean.toString(check)).build();
 	}
 	
 	
 	//settings
 	
 	@GET
 	@Path("settings")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response getSettings(@Context HttpServletRequest request,
 			@QueryParam("callback") String callback){
 		Session session = Session.init(request.getSession());
 		User user = session.getUser();
 		MailSettings mailsettings = MailSettings.init(user.getId());
 		Map<String, Settings> settings = new HashMap<>();
 		settings.put("mail", mailsettings);
 		return JsonpBuilder.buildResponse(callback, settings);
 	}
 	
 	@GET
 	@Path("mailprovider")
 	@Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 	public Response checkMailAnbieter(@QueryParam("domain") String domain){
 		if(domain == null) 
 			throw new WebApplicationException(400);
 		MailProvider provider = MailProvider.getMailanbieterfromDomain(domain);
 		
 		if(provider != null)
 			return JsonpBuilder.buildResponse(null, provider);
 		return JsonpBuilder.buildResponse(null, ""); 
 	}
 	
 	@POST
 	@Path("settings/account/{type}")
 	public Response changeAccountSettings(@Context HttpServletRequest request,
 			@Context HttpHeaders hh,
 			@PathParam("type") String type,
 			@FormParam("value") String value){
 		Session session = Session.init(request.getSession());
 		
 		session.checkLogin(hh.getCookies());
 		
 		
 		User user = session.getUser();
 		boolean check = false;
 		
 		switch (type) {
 		case "text":
 			check = user.setText(value);
 			break;
 		case "place":
 			check = user.setPlace(value);
 			break;
 		case "name":
 			check = user.setName(value);
 			break;
 
 		default:
			break;
 		}
 		
 		
 		if(!check){
 			logger.warn("check failed");
 			throw new WebApplicationException(400);
 		}
 		
 		return Response.ok("true").build();
 		
 	}
 	
 	@PUT
 	@Path("settings/mail/{type}")
 	public Response addMailSettings(@Context HttpServletRequest request,
 			@Context HttpHeaders hh, 
 			@PathParam("type") String type){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		MailSettings settings = MailSettings.init(session.getUser().getId());
 		logger.debug("add mailtype:"+type);
 		
 		if(type.equals("all")){
 			settings.changeAll(true);
 		}else{
 			switch (type.toLowerCase()) {
 			case "recipeactivation":
 				settings.setRecipeactivation(true);				
 				break;
 			case "recipediscussion":
 				settings.setRecipediscussion(true);				
 				break;
 			case "tagaccepted":
 				settings.setTagaccepted(true);				
 				break;
 			case "tagdenied":
 				settings.setTagdenied(true);				
 				break;
 			case "discussionanswer":
 				settings.setDiscussionanswer(true);				
 				break;
 			case "schmeckt":
 				settings.setSchmeckt(true);				
 				break;
 
 			default:
 				break;
 			}
 		}
 		
 		return Response.ok().build();
 	}
 	
 	@DELETE
 	@Path("settings/mail/{type}")
 	public Response removeMailSettings(@Context HttpServletRequest request,
 			@Context HttpHeaders hh, 
 			@PathParam("type") String type){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		MailSettings settings = MailSettings.init(session.getUser().getId());
 		logger.debug("remove mailtype:"+type);
 		
 		if(type.equals("all")){
 			settings.changeAll(false);
 		}else{
 			switch (type.toLowerCase()) {
 			case "recipeactivation":
 				settings.setRecipeactivation(false);				
 				break;
 			case "recipediscussion":
 				settings.setRecipediscussion(false);				
 				break;
 			case "tagaccepted":
 				settings.setTagaccepted(false);				
 				break;
 			case "tagdenied":
 				settings.setTagdenied(false);				
 				break;
 			case "discussionanswer":
 				settings.setDiscussionanswer(false);				
 				break;
 			case "schmeckt":
 				settings.setSchmeckt(false);				
 				break;
 
 			default:
 				break;
 			}
 		}
 		
 		return Response.ok().build();
 	}
 }
