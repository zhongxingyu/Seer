 package org.fiteagle.delivery.rest.fiteagle;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.UUID;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.Response;
 import javax.xml.bind.DatatypeConverter;
 
 import org.fiteagle.core.userdatabase.UserDBManager;
 import org.fiteagle.core.userdatabase.UserPersistable.DatabaseException;
 import org.fiteagle.core.userdatabase.UserPersistable.RecordNotFoundException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.jersey.core.util.Base64;
 
 public class AuthenticationFilter implements Filter{
 
   private final static String COOKIE_NAME = "fiteagle_user_cookie";
   private HashMap<String, Cookie> cookies = new HashMap<>();
   
   private UserDBManager manager = null;
   
   private Logger log = LoggerFactory.getLogger(getClass());
   
   private static AuthenticationFilter authenticationFilter;
   public static AuthenticationFilter getInstance(){
     if(authenticationFilter == null){
       authenticationFilter = new AuthenticationFilter();
     }
     return authenticationFilter;
   }
   
   @Override
   public void destroy() {   
   }
 
   @Override
   public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
       ServletException {
     HttpServletRequest request = (HttpServletRequest) req;
     HttpServletResponse response = (HttpServletResponse) resp;
     if(!request.isSecure()){
       response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());      
       return;   
     }
     //ONLY PUT && USER is allowed without password // TODO refactor
     String method = request.getMethod(); 
     if(method.equals("PUT")){
       chain.doFilter(req, resp);
       return;
     }    
     
    String username = getTargetFromRequest(request);
    if(username == null){
      response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());      
      return;
    }
    
    if(!authenticateWithCookie(username, request)){
      if(!authenticateWithUsernameAndPassword(username, request, response)){
         return;
      }      
     
      if(getAuthCookieFromRequest(request) == null){
         response.addCookie(createNewCookie(username));
       }
     }
     
     chain.doFilter(req, resp);
   }
 
   @Override
   public void init(FilterConfig arg0) throws ServletException {    
   }
   
   private String[] decode(String auth){ 
     if(auth == null || (!auth.startsWith("Basic ") && !auth.startsWith("basic "))){
       return null;
     }
     auth = auth.replaceFirst("[B|b]asic ", "");
     byte[] decoded = DatatypeConverter.parseBase64Binary(auth);
     if(decoded == null || decoded.length == 0){
       return null;
     }
     return new String(decoded).split(":", 2);
   }
   
  private boolean authenticateWithUsernameAndPassword(String username, HttpServletRequest request, HttpServletResponse response){
     String auth = request.getHeader("authorization");  
 
     String[] credentials = decode(auth);    
     if(credentials == null || credentials.length != 2){      
       response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());      
       return false;
     }
 
    if(!username.equals(credentials[0])){
       response.setStatus(Response.Status.FORBIDDEN.getStatusCode());      
       return false;
     }
 
     manager = UserDBManager.getInstance();
     try {
       if(!manager.verifyCredentials(credentials[0], credentials[1])){
         response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());      
         return false;
       }
     } catch (NoSuchAlgorithmException | IOException | DatabaseException e) {
         log.error(e.getMessage());
         response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());      
         return false;
     } catch (RecordNotFoundException e){
       response.setStatus(Response.Status.NOT_FOUND.getStatusCode());      
       return false;       
     }
     return true;
   }
 
  private boolean authenticateWithCookie(String username, HttpServletRequest request){    
     Cookie authCookieFromRequest = getAuthCookieFromRequest(request);
    Cookie cookieFromStorage = cookies.get(username);
     if(authCookieFromRequest != null && cookieFromStorage != null && authCookieFromRequest.getValue().equals(cookieFromStorage.getValue())){
       return true;
     }
     return false;
   }
 
   private Cookie getAuthCookieFromRequest(HttpServletRequest request) {
     Cookie[] cookiesFromRequest = request.getCookies();
     if(cookiesFromRequest != null){
       for(Cookie cookie : cookiesFromRequest){
         if(cookie.getName().equals(COOKIE_NAME)){
           return cookie;
         }
       }
     }
     return null;
   }
 
   private String getTargetFromRequest(HttpServletRequest req) {
     String path = req.getRequestURI();
     String[] splitted = path.split("/");
     for(int i = 0; i < splitted.length -1; i++){
       if(splitted[i].equals("user")){
         return splitted[i+1];
       }
     }
     return null;
   }
   
   private Cookie createNewCookie(String username){
     String authToken = createRandomAuthToken(username);
     Cookie cookie = new Cookie(COOKIE_NAME, authToken);
     cookie.setSecure(true);
     cookie.setMaxAge(1800);
     cookies.put(username, cookie);
     return cookie;
   }
 
   private String createRandomAuthToken(String username) {
     String u = UUID.randomUUID().toString();
     u+="-username:"+username;
     return new String(Base64.encode(u.getBytes()));
   }
   
   public static String getCookieName(){
     return COOKIE_NAME;
   }
   
   protected void deleteCookie(String username){
     cookies.remove(username);
   }
 }
