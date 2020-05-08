 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.engine.*;
 import org.webmacro.servlet.*;
 import org.melati.util.*;
 import org.melati.*;
 import org.melati.poem.*;
 
 /**
  * Flags up when something was illegal or not supported about an incoming HTTP
  * authorization
  */
 
 class HttpAuthorizationMelatiException extends MelatiRuntimeException {
   public HttpAuthorizationMelatiException(String message) {
     super(message, null);
   }
 }
 
 /**
  * The information contained in an HTTP authorization
  */
 
 class HttpAuthorization {
   public String type;
   public String username;
   public String password;
 
   public HttpAuthorization(String type, String username, String password) {
     this.type = type;
     this.username = username;
     this.password = password;
   }
 
   public static HttpAuthorization from(String authHeader) {
     // FIXME single space probably not only valid sep
 
     if (authHeader.regionMatches(0, "Basic ", 0, 6)) {
 
       String logpas = Base64.decode(authHeader.substring(6));
 
       int colon = logpas.indexOf(':');
 
       if (colon == -1)
 	throw new HttpAuthorizationMelatiException(
             "The browser sent Basic Authorization credentials with no colon " +
 	    "(that's not legal)");
 
       return new HttpAuthorization("Basic",
 				   logpas.substring(0, colon).trim(),
 				   logpas.substring(colon + 1).trim());
     }
     else {
       int space = authHeader.indexOf(' ');
       if (space == -1)
 	throw new HttpAuthorizationMelatiException(
             "The browser sent an Authorization header without a space, " +
             "so it can't be anything Melati understands: " +
             authHeader);
 
       String type = authHeader.substring(0, space);
       throw new HttpAuthorizationMelatiException(
           "The browser tried to authenticate using an authorization type " +
 	  "`" + type + "' which Melati doesn't understand");
     }
   }
 
   public static HttpAuthorization from(HttpServletRequest request) {
     String header = request.getHeader("Authorization");
     return header == null ? null : from(header);
   }
 }
 
 /**
  * An <TT>AccessHandler</TT> which uses the HTTP Basic Authentication scheme to
  * elicit and maintain the user's login and password.  This implementation
  * doesn't use the servlet session at all, so it doesn't try to send cookies or
  * do URL rewriting.
  *
  * @see MelatiServlet#handle(org.webmacro.servlet.WebContext, org.melati.Melati)
  * @see MelatiServlet#init
  */
 
 public class HttpBasicAuthenticationAccessHandler implements AccessHandler {
   private static final String className =
       new HttpBasicAuthenticationAccessHandler().getClass().getName();
 
   public final String REALM = className + ".realm";
   public final String USER = className + ".user";
 
   protected boolean useSession() {
     return false;
   }
 
   /**
    * Force a login by sending a 401 error back to the browser.  FIXME
    * Apache/Netscape appear not to do anything with <TT>message</TT>, which is
    * why it's just left as a <TT>String</TT>.
    */
 
   protected void forceLogin(HttpServletResponse resp,
 			    String realm, String message) throws IOException {
     String desc = realm == null ? "<unknown>" : StringUtils.tr(realm, '"', ' ');
     resp.setHeader("WWW-Authenticate", "Basic realm=\"" + desc + "\"");
     resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
   }
 
   public Template handleAccessException(
       WebContext context, AccessPoemException accessException)
           throws Exception {
 
     String capName = accessException.capability.getName();
     if (useSession())
       context.getSession().putValue(REALM, capName);
     forceLogin(context.getResponse(), capName, accessException.getMessage());
     return null;
   }
 
   public WebContext establishUser(WebContext context, Database database)
       throws PoemException, IOException, ServletException {
 
     HttpAuthorization auth = HttpAuthorization.from(context.getRequest());
 
     if (auth == null) {
       // No attempt to log in: become `guest'
 
       PoemThread.setAccessToken(database.guestAccessToken());
       return context;
     }
     else {
       // They are trying to log in
 
       // If allowed, we store the User in the session to avoid repeating the
       // SELECTion implied by firstWhereEq for every hit
 
       User sessionUser =
 	  useSession() ? (User)context.getSession().getValue(USER) : null;
       User user = null;
 
       if (sessionUser == null ||
 	  !sessionUser.getLogin().equals(auth.username))
 	try {
 	  user = (User)database.getUserTable().getLoginColumn().
 		     firstWhereEq(auth.username);
 	}
         catch (NoSuchRowPoemException e) {
 	}
         catch (AccessPoemException e) {
 	  // paranoia
 	}
       else
 	user = sessionUser;
 
       if (user == null || !user.getPassword().equals(auth.password)) {
 
 	// Login/password authentication failed; we must trigger another
	// attempt.  But do we know the "realm" (= POEM capability name) for
 	// which they were originally found not to be authorized?
 
 	String storedRealm;
 	if (useSession() &&
 	    (storedRealm = (String)context.getSession().getValue(REALM)) !=
 	         null) {
 
 	  // The "realm" is stored in the session
 
 	  forceLogin(context.getResponse(), storedRealm,
 		     "Login/password not recognised");
 	  return null;
 	}
 	else {
 
 	  // We don't know the "realm", so we just let the user try again as
 	  // `guest' and hopefully trigger the same problem and get the same
 	  // message all over again.  Not very satisfactory but the alternative
 	  // is providing a default realm like "<unknown>".
 
 	  PoemThread.setAccessToken(database.guestAccessToken());
 	  return context;
 	}
       }
       else {
 
 	// Login/password authentication succeeded
 
 	PoemThread.setAccessToken(user);
 
 	if (useSession() && user != sessionUser)
 	  context.getSession().putValue(USER, user);
 
 	return context;
       }
     }
   }
 }
