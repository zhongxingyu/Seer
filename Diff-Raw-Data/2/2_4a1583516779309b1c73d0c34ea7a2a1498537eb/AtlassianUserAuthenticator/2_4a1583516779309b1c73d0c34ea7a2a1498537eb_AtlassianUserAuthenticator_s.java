 package com.atlassian.refapp.auth.internal;
 
 import java.io.IOException;
 import java.security.Principal;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.atlassian.refapp.auth.external.WebSudoSessionManager;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.atlassian.seraph.auth.AbstractAuthenticator;
 import com.atlassian.seraph.auth.AuthenticatorException;
 import com.atlassian.seraph.auth.RoleMapper;
 import com.atlassian.seraph.config.SecurityConfig;
 import com.atlassian.seraph.config.SecurityConfigFactory;
 import com.atlassian.seraph.cookie.CookieFactory;
 import com.atlassian.seraph.cookie.CookieHandler;
 import com.atlassian.seraph.interceptor.LogoutInterceptor;
 import com.atlassian.seraph.util.RedirectUtils;
 import com.atlassian.user.EntityException;
 import com.atlassian.user.UserManager;
 import com.atlassian.user.security.authentication.Authenticator;
 import com.atlassian.user.util.Base64Encoder;
 
 public class AtlassianUserAuthenticator extends AbstractAuthenticator
 {
     /**
      * The key used to store the user object in the session
      */
     public static final String LOGGED_IN_KEY = "seraph_defaultauthenticator_user";
 
     /**
      * The key used to indicate that the user has logged out and session regarding of it containing a cookie is not
      * logged in.
      */
     public static final String LOGGED_OUT_KEY = "seraph_defaultauthenticator_logged_out_user";
 
     private final Log log = LogFactory.getLog(getClass());
 
     // --------------------------------------------------------------------------------------------------------- members
 
     private final UserManager userManager;
     private final Authenticator authenticator;
     private final WebSudoSessionManager websudoManager;
 
     private String loginCookieKey;
     private String authType;
     private int autoLoginCookieAge;
     private String loginCookiePath;
 
     public AtlassianUserAuthenticator(UserManager userManager, Authenticator authenticator, WebSudoSessionManager websudoManager)
     {
         this.userManager = userManager;
         this.authenticator = authenticator;
         this.websudoManager = websudoManager;
     }
     
     public void init(Map params, SecurityConfig config)
     {
         if (log.isDebugEnabled())
         {
             log.debug(this.getClass().getName() + " $Revision: 16581 $ initializing");
         }
         super.init(params, config);
         this.loginCookieKey = config.getLoginCookieKey();
         this.authType = config.getAuthType();
         this.autoLoginCookieAge = config.getAutoLoginCookieAge();
         this.loginCookiePath = config.getLoginCookiePath();
     }
 
     /**
      * @deprecated Use {@link RoleMapper} directly
      */
     public boolean isUserInRole(HttpServletRequest request, String role)
     {
         return getRoleMapper().hasRole(getUser(request), request, role);
     }
 
     /**
      * Tries to authenticate a user (via OSUser). If successful, sets a session attribute and cookie indicating their
      * logged-in status.
      *
      * @return Whether the user was authenticated. This base implementation returns false if any errors occur, rather
      *         than throw an exception.
      */
     public boolean login(HttpServletRequest request, HttpServletResponse response, String username, String password, boolean cookie)
             throws AuthenticatorException
     {
         final boolean dbg = log.isDebugEnabled();
         CookieHandler cookieHandler = CookieFactory.getCookieHandler();
 
         // check that they can login (they have the USE permission or ADMINISTER permission)
         boolean authenticated = authenticate(username, password);
         if (dbg)
         {
             log.debug("User : " + username + " has " + (authenticated ? "been" : "no been") + " authenticated");
         }
         if (authenticated)
         {
             Principal user = getUser(username);
             request.getSession().setAttribute(LOGGED_IN_KEY, user);
             request.getSession().setAttribute(LOGGED_OUT_KEY, null);
            if (request.getParameter("os_websudo") != null)
             {
                 websudoManager.createWebSudoSession(request);
             }
 
             final boolean canLogin = getRoleMapper().canLogin(user, request);
             if (dbg)
             {
                 log.debug("User : " + username + " " + (canLogin ? "can" : "CANT") + " login according to the RoleMapper");
             }
             if (canLogin)
             {
                 if (cookie && response != null)
                 {
                     cookieHandler.setCookie(request, response, getLoginCookieKey(), encodeCookie(username, password), autoLoginCookieAge, getCookiePath(request));
                 }
                 return true;
             }
             else
             {
                 request.getSession().removeAttribute(LOGGED_IN_KEY);
             }
         }
 
         if (response != null && cookieHandler.getCookie(request, getLoginCookieKey()) != null)
         {
             log.warn("User: " + username + " tried to login but they do not have USE permission or weren't found. Deleting cookie.");
 
             try
             {
                 cookieHandler.invalidateCookie(request, response, getLoginCookieKey(), getCookiePath(request));
             }
             catch (Exception e)
             {
                 log.error("Could not invalidate cookie: " + e, e);
             }
         }
 
         return false;
     }
 
     // override this method if you need to retrieve the role mapper from elsewhere than the singleton-factory (injected
     // depency for instance)
     protected RoleMapper getRoleMapper()
     {
         return SecurityConfigFactory.getInstance().getRoleMapper();
     }
 
 
     public boolean logout(HttpServletRequest request, HttpServletResponse response) throws AuthenticatorException
     {
         boolean dbg = log.isDebugEnabled();
         if (dbg)
         {
             log.debug("logout requested.  Calling interceptors and clearing cookies");
         }
         List interceptors = getLogoutInterceptors();
         CookieHandler cookieHandler = CookieFactory.getCookieHandler();
 
         for (Iterator iterator = interceptors.iterator(); iterator.hasNext();)
         {
             LogoutInterceptor interceptor = (LogoutInterceptor) iterator.next();
             interceptor.beforeLogout(request, response);
         }
 
         request.getSession().setAttribute(LOGGED_IN_KEY, null);
         request.getSession().setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
 
         // Logout is sometimes called as part of a getUser request, if the user is not found
         // logout may be called, but some getUser calls only pass in the request, and null response.
         if (response != null && cookieHandler.getCookie(request, getLoginCookieKey()) != null)
         {
             try
             {
                 cookieHandler.invalidateCookie(request, response, getLoginCookieKey(), getCookiePath(request));
             }
             catch (Exception e)
             {
                 log.error("Could not invalidate cookie: " + e, e);
             }
         }
 
         for (Iterator iterator = interceptors.iterator(); iterator.hasNext();)
         {
             LogoutInterceptor interceptor = (LogoutInterceptor) iterator.next();
             interceptor.afterLogout(request, response);
         }
 
         return true;
     }
 
     /**
      * Returns the currently logged in user, trying in order: <p/>
      * <ol>
      * <li>Session, only if one exists</li>
      * <li>Cookie, only if no session exists</li>
      * <li>Basic authentication, if the above fail, and authType=basic</li>
      * </ol>
      * <p/> Warning: only in the case of cookie and basic auth will the user be authenticated.
      *
      * @param response a response object that may be modified if basic auth is enabled
      * @return a Principal object for the user if found, otherwise null
      */
     public Principal getUser(HttpServletRequest request, HttpServletResponse response)
     {
         final boolean dbg = log.isDebugEnabled();
         if (request.getSession(false) != null)
         {
             Principal sessionUser = getUserFromSession(request);
             if (sessionUser != null)
             {
                 if (dbg)
                 {
                     log.debug("Session found; BUT user doesn't exist");
                 }               
                 return sessionUser;
             }
         }
         else
         {
             Principal cookieUser = getUserFromCookie(request, response);
             if (cookieUser != null) return cookieUser;
             log.debug("Cannot log user in via a cookie");
         }
 
         if (RedirectUtils.isBasicAuthentication(request, authType))
         {
             Principal basicAuthUser = getUserFromBasicAuthentication(request, response);
             if (basicAuthUser != null) return basicAuthUser;
         }
 
         if (dbg)
         {
             log.debug("User not logged in.");
         }
 
         return null;
     }
 
     /**
      * Extracts the username and password from the cookie and calls login to authenticate, and if successful store the
      * token in the session.
      *
      * @return a Principal object for the user if successful, otherwise null
      */
     protected Principal getUserFromCookie(HttpServletRequest request, HttpServletResponse response)
     {
         final boolean dbg = log.isDebugEnabled();
         final String cookieName = getLoginCookieKey();
         final Cookie cookie = CookieFactory.getCookieHandler().getCookie(request, cookieName);
         if (cookie == null)
         {
             return null;
         }
 
         final String cookieValue = cookie.getValue();
         if (dbg)
         {
             log.debug("Found cookie : '" + cookieName + "' with value : '" + cookieValue + "'");
         }
         final String[] values = decodeCookie(cookieValue);
         if (values == null)
         {
             if (dbg)
             {
                 log.debug("Unable to decode " + cookieName + " cookie with value : '" + cookieValue + "'");
             }
             return null;
         }
 
         final String username = values[0];
         final String password = values[1];
         if (dbg)
         {
             log.debug("Got username : '" + username + "' and password from cookie, attempting to authenticate user");
         }
 
         try
         {
             if (!login(request, response, username, password, false))
             {
                 return null;
             }
         }
         catch (Exception e)
         {
             log.warn("Cookie login for user : '" + username + "' failed with exception: " + e, e);
             return null;
         }
 
         if (dbg)
         {
             log.debug("Logged user : '" + username + "' in via a cookie");
         }
         return getUserFromSession(request);
     }
 
     /**
      * <p>
      * Tries to get a logged in user from the session.
      * </p>
      *
      * @param request the current {@link HttpServletRequest}
      * @return the logged in user in the session. <code>null</code> if there is no logged in user in the session, or
      *         the {@link #LOGGED_OUT_KEY} is set because the user has logged out.
      */
     protected Principal getUserFromSession(HttpServletRequest request)
     {
         final boolean dbg = log.isDebugEnabled();
         try
         {
             if (request.getSession().getAttribute(LOGGED_OUT_KEY) != null)
             {
                 if (dbg)
                 {
                     log.debug("Session found; user has already logged out");
                 }
                 return null;
             }
             if (request.getSession().getAttribute(LOGGED_IN_KEY) == null)
             {
                 return null;
             }
             final Principal principal = (Principal) request.getSession().getAttribute(LOGGED_IN_KEY);
             if (dbg)
             {
                 if (principal == null)
                 {
                     log.debug("Session found; BUT it has no Principal in it");
                 }
                 else
                 {
                     log.debug("Session found; user : '" + principal.getName() + "' already logged in");
                 }
             }
             return principal;
         }
         catch (Exception e)
         {
             log.warn("Exception when retrieving user from session: " + e, e);
             return null;
         }
     }
 
     /**
      * Checks the Authorization header to see whether basic auth token is provided. If it is, decode it, login and
      * return the valid user. If it isn't, basic auth is still required, so return a 401 Authorization Required header
      * in the response.
      *
      * @param response a response object that <i>will</i> be modified if no token found
      */
     protected Principal getUserFromBasicAuthentication(HttpServletRequest request, HttpServletResponse response)
     {
         final boolean dbg = log.isDebugEnabled();
         String header = request.getHeader("Authorization");
 
         if (header != null && header.startsWith("Basic "))
         {
             if (dbg)
             {
                 log.debug("Looking in Basic Auth headers");
             }
             String base64Token = header.substring(6);
             String token = new String(Base64Encoder.decode(base64Token.getBytes()));
 
             String username = "";
             String password = "";
 
             int delim = token.indexOf(":");
 
             if (delim != -1)
             {
                 username = token.substring(0, delim);
                 password = token.substring(delim + 1);
             }
 
             try
             {
                 if (login(request, response, username, password, false))
                 {
                     if (dbg)
                     {
                         log.debug("Logged in user : '" + username + "' via basic auth");
                     }
                     return getUser(username);
                 }
             }
             catch (AuthenticatorException e)
             {
                 log.warn("Exception trying to login user : '" + username + "' via basic auth:" + e, e);
             }
             try
             {
                 response.sendError(401);
             }
             catch (IOException e)
             {
                 log.warn("Exception trying to send basic auth failed error: " + e, e);
             }
             return null;
         }
 
         if (response == null)
         {
             return null;
         }
 
         response.setStatus(401);
         response.setHeader("WWW-Authenticate", "BASIC realm=\"protected-area\"");
         return null;
     }
 
     /**
      * Root the login cookie at the same location as the webapp. <p/> Anyone wanting a different cookie path policy can
      * override the authenticator and provide one.
      */
     protected String getCookiePath(HttpServletRequest request)
     {
         if (getLoginCookiePath() != null)
             return getLoginCookiePath();
 
         String path = request.getContextPath();
         if (path == null || path.equals(""))
         {
             return "/";
         }
 
         // The spec says this should never happen, but just to be sure...
         if (!path.startsWith("/"))
         {
             return "/" + path;
         }
 
         return path;
     }
 
     protected String getLoginCookieKey()
     {
         return loginCookieKey;
     }
 
     public String getAuthType()
     {
         return authType;
     }
 
     protected List getLogoutInterceptors()
     {
         return getConfig().getInterceptors(LogoutInterceptor.class);
     }
 
     protected String encodeCookie(final String username, final String password)
     {
         return CookieFactory.getCookieEncoder().encodePasswordCookie(username, password, getConfig().getCookieEncoding());
     }
 
     protected String[] decodeCookie(final String value)
     {
         return CookieFactory.getCookieEncoder().decodePasswordCookie(value, getConfig().getCookieEncoding());
     }
 
     protected String getLoginCookiePath()
     {
         return loginCookiePath;
     }
 
     protected Principal getUser(String username)
     {
         try
         {
             return userManager.getUser(username);
         }
         catch (EntityException e)
         {
             return null;
         }
     }
     
     protected boolean authenticate(String username, String password)
     {
         try
         {
             boolean authenticated = authenticator.authenticate(username, password);
             if (authenticated)
                 log.info("User '" + username + "' successfully logged in");
             else
                 log.info("Cannot login user '" + username + "' as they used an incorrect password");
             return authenticated;
         } catch (EntityException e)
         {
             log.info("Cannot login user '" + username + "' as they do not exist.");
             return false;
         }
     }
 }
