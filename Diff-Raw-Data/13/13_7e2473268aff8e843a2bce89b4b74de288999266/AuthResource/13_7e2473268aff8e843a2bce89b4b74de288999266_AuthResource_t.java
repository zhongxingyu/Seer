 /*
  * Copyright (C) 2013 Michael Koppen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.fhb.paperfly.server.rest.v1.service.resources.auth;
 
 import com.qmino.miredot.annotations.ReturnType;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import com.sun.jersey.oauth.server.spi.OAuthProvider;
 import de.fhb.paperfly.server.logging.service.LoggingServiceLocal;
 import de.fhb.paperfly.server.rest.v1.dto.output.TokenDTO;
 import de.fhb.paperfly.server.rest.v1.dto.output.ErrorDTO;
 import de.fhb.paperfly.server.rest.v1.service.PaperFlyRestService;
 import de.fhb.paperfly.server.rest.v1.service.provider.DefaultOAuthProvider;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 /**
  *
  * @author Michael Koppen <michael.koppen@googlemail.com>
  */
 @Stateless
 //@Path("auth/")
 public class AuthResource {
 
 	@EJB
 	public LoggingServiceLocal LOG;
 
 	/**
 	 * [TODO LARGE DESC]
 	 *
 	 * @title Login
 	 * @summary Log into the service an retrieve your OAuth-Token.
 	 * @param request
 	 * @param provider
 	 * @return Returns the OAuth-Token of the logged in user.
 	 */
 	@GET
 	@Path("login")
 	@Produces(PaperFlyRestService.JSON_MEDIA_TYPE)
 	@ReturnType("de.fhb.paperfly.server.rest.v1.dto.output.TokenDTO")
 	public Response login(@Context HttpServletRequest request, @Context OAuthProvider provider) {
 		System.out.println("LOGIN...");
 		Response resp;
 		try {
//			request.logout();
 			HttpSession session = request.getSession(true);
			System.out.println("isSession new?: " + session.isNew());
			System.out.println("trackingmodes");
			for (SessionTrackingMode object : session.getServletContext().getEffectiveSessionTrackingModes()) {
				System.out.println("trackingmode: " + object);
			}

 			request.login(request.getHeader("user"), request.getHeader("pw"));
 
 //			String sessionID = request.changeSessionId();
 //			System.out.println("sessionID: " + sessionID);
 			System.out.println("SessionID: " + session.getId());
 			MultivaluedMap<String, String> roles = new MultivaluedMapImpl();
 
 			if (request.isUserInRole("ADMINISTRATOR")) {
 				System.out.println("User is in role ADMINISTRATOR");
 				roles.add("roles", "ADMINISTRATOR");
 			}
 			if (request.isUserInRole("USER")) {
 				System.out.println("User is in role USER");
 				roles.add("roles", "USER");
 			}
 			if (request.isUserInRole("ANONYMOUS")) {
 				System.out.println("User is in role ANONYMOUS");
 				roles.add("roles", "ANONYMOUS");
 			}
 			DefaultOAuthProvider.Consumer c = ((DefaultOAuthProvider) provider).registerConsumer(request.getUserPrincipal().toString(), request.getUserPrincipal(), roles);
 			System.out.println("Consumer Owner: " + c.getOwner());
 			System.out.println("Consumer Secret: " + c.getSecret());
 			System.out.println("Consumer Key: " + c.getKey());
 			System.out.println("Consumer Principal: " + c.getPrincipal());
 
 
 			String consumerKey = "";
 			String callbackURL = "";
 			Map<String, List<String>> attributes = null;
 //			OAuthToken oauthtoken = provider.newRequestToken(consumerKey, callbackURL, attributes);
 
 			System.out.println("Successfully logged in!");
 			System.out.println("User: " + request.getUserPrincipal());
 			resp = Response.ok(new TokenDTO(c.getKey(), c.getSecret())).build();
 		} catch (Exception e) {
 			LOG.log(this.getClass().getName(), Level.SEVERE, "Exception: {0}", e.getMessage());
 			resp = Response.status(500).build();
 		}
 		return resp;
 	}
 
 	/**
 	 * [TODO LARGE DESC]
 	 *
 	 * @title Logout
 	 * @summary Log's out the actual user.
 	 * @param request
 	 * @return Nothing to return.
 	 */
 	@GET
 	@Path("logout")
 	@Produces(PaperFlyRestService.JSON_MEDIA_TYPE)
 	public Response logout(@Context HttpServletRequest request) throws ServletException {
		System.out.println("LOGOUT...");
 		Response resp;
 		request.logout();
 		resp = Response.ok().build();
 		return resp;
 	}
 }
