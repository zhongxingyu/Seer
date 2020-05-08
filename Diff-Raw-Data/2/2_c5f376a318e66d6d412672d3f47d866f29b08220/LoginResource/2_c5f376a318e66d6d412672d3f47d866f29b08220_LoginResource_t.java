 package server.resources;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import server.exceptions.LoginFailedException;
 import server.operations.CookieHandler;
 import server.operations.LoginValidator;
 
 import com.google.gson.Gson;
 
 @Path("login")
 public class LoginResource {
 
 	@POST
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	@Produces(MediaType.APPLICATION_JSON)
 	public String doLogin(
 			@DefaultValue("") @FormParam("user") String userName,
 			@DefaultValue("") @FormParam("password") String password) {
 		final Gson gson = new Gson();
 		String json;
 		try {
 			new LoginValidator().validateLoginData(userName, password);
 			json = gson.toJson(new CookieHandler().createCookie());
 
 		} catch (LoginFailedException e) {
			json = gson.toJson(e.showErrorMessage());
 		}
 		return json;
 	}
 }
