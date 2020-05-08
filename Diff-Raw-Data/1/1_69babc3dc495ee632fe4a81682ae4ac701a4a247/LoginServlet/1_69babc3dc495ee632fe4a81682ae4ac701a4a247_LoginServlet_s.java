 /*
  * Copyright (C) 2012 Helsingfors Segelklubb ry
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package fi.hoski.web.auth;
 
 import fi.hoski.datastore.EmailNotUniqueException;
 import fi.hoski.web.google.DatastoreUserDirectory;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.UnavailableException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.Cookie;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import static fi.hoski.web.auth.UserDirectory.EMAIL;
 
 /**
  * LoginServlet authenticates the user using e-mail and password as the login
  * credentials. The login servlet creates a servlet session where it stores the
  * user object.
  *
  * The Login Servlet is designed to be used through AJAX.
  */
 public class LoginServlet extends HttpServlet {
 
   public static final long serialVersionUID = -1;
   public static final String USER = "fi.hoski.web.user";
   private UserDirectory userDirectory;
 
   @Override
   public void init() {
     userDirectory = new DatastoreUserDirectory();
   }
 
   @Override
   public void doPost(HttpServletRequest request,
     HttpServletResponse response)
     throws ServletException, IOException {
     response.setCharacterEncoding("UTF-8");
 
     String action = request.getParameter("action");
     try {
       if (action == null || action.equals("login")) {
         // login
 
         String email = request.getParameter("email");
         String password = request.getParameter("password");
         email = (email != null) ? email.trim() : null;
 
         // 1. check params
         if (email == null || email.isEmpty()
           || password == null || password.isEmpty()) {
           log("email or password not ok");
           response.sendError(HttpServletResponse.SC_FORBIDDEN);
         } else {
           // 2. check user exists
           Map<String, Object> user = userDirectory.authenticateUser(email, password);
           if (user == null) {
             log("user not found");
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
           } else {
             // 3. create session
             HttpSession session = request.getSession(true);
             session.setAttribute(USER, user);
 
             response.getWriter().println("Logged in");
           }
         }
       } else {
         // logout
 
         HttpSession session = request.getSession(false);
         if (session != null) {
           session.setAttribute(USER, null);
           session.invalidate();
         }
 
         // change Cookie so that Vary: Cookie works
         Cookie c = new Cookie("JSESSIONID", null);
         c.setMaxAge(0);
         response.addCookie(c);
 
         response.getWriter().println("Logged out");
       }
     } catch (UnavailableException ex) {
       log(ex.getMessage(), ex);
       response.sendError(HttpServletResponse.SC_FORBIDDEN,
         ex.getMessage());
     } catch (EmailNotUniqueException ex) {
       log(ex.getMessage(), ex);
       response.sendError(HttpServletResponse.SC_FORBIDDEN,
         ex.getMessage());
     }
   }
 
   @Override
   public void doGet(HttpServletRequest request,
     HttpServletResponse response)
     throws ServletException, IOException {
     response.setCharacterEncoding("UTF-8");
 
     String email = request.getParameter("email");
     String activationKey = request.getParameter("activationKey");
     try {
       if (email != null && activationKey != null) {
         Map<String, Object> user =
           userDirectory.useActivationKey(email, activationKey);
 
         if (user != null) {
           HttpSession session = request.getSession(true);
           session.setAttribute(USER, user);
         }
 
         // redirect always, if user is not logged in,
         // there will be a login screen
         response.sendRedirect("/member"); //TODO target make configurable
       } else {
         HttpSession session = request.getSession(false);
         String etag = request.getHeader("If-None-Match");
         @SuppressWarnings("unchecked")
         Map<String, Object> user = (session != null)
           ? (Map<String, Object>) session.getAttribute(USER)
           : null;
         String userEtag = getEtag(user);
 
         if (etag != null && etag.equals(userEtag)) {
           response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
         } else {
           response.setHeader("ETag", userEtag);
           response.setHeader("Cache-Control", "private");
           response.setHeader("Vary", "Cookie");
 
           writeUserJSON(user, response);
         }
       }
     } catch (UnavailableException ex) {
       log(ex.getMessage(), ex);
       response.sendError(HttpServletResponse.SC_FORBIDDEN,
         ex.getMessage());
     } catch (EmailNotUniqueException ex) {
       log(ex.getMessage(), ex);
       response.sendError(HttpServletResponse.SC_FORBIDDEN,
         ex.getMessage());
     }
   }
 
   private void writeUserJSON(Map<String, Object> user,
     HttpServletResponse response)
     throws ServletException, IOException {
 
     try {
       response.setContentType("application/json");
 
       JSONObject json = new JSONObject();
       json.put("user", getUserJSON(user));
       response.getWriter().println(json.toString(4));
       //json.write(response.getWriter());
     } catch (JSONException e) {
       throw new ServletException("Could not serialize user object");
     }
   }
 
   private Object getUserJSON(Map<String, Object> user)
     throws JSONException {
     if (user != null) {
       JSONObject userjson = new JSONObject();
       for (Map.Entry<String, Object> entry : user.entrySet()) {
         Object ob = entry.getValue();
         if (ob instanceof List) {
           List opt = (List) ob;
           JSONArray optarray = new JSONArray();
           for (Object etr : opt) {
             if (etr instanceof Map) {
               etr = getUserJSON((Map<String, Object>) etr);
             } // else expect it to be primitive type
             optarray.put(etr);
           }
           userjson.put(entry.getKey(), optarray);
         } else {
           userjson.put(entry.getKey(), ob);
         }
       }
       return userjson;
     } else {
       return JSONObject.NULL;
     }
   }
 
   private String getEtag(Map<String, Object> user) {
     if (user == null) {
       return "\"null\"";
     } else {
       String tag = (String) user.get(EMAIL);
       tag = (tag != null) ? tag.replace('"', '_') : null;
       return '"' + tag + '"';
     }
   }
 }
