 package de.enwida.web.servlet;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.RollingFileAppender;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import de.enwida.web.service.implementation.CookieSecurityServiceImpl;
 import de.enwida.web.utils.Constants;
 
 /**
  * logs the user web requests
  * @author root
  *
  */
 public class UserLog extends HandlerInterceptorAdapter{
     static Logger logger = Logger.getLogger(UserLog.class);
     
     @Autowired
     private CookieSecurityServiceImpl cookieSecurityService;
 
     @Override
     public void postHandle(HttpServletRequest request,
             HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
                 
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         boolean loggedin = false;
         String logFileName = null;
         // if user name is anonymous
         if (auth.getName().equalsIgnoreCase("anonymousUser")) {
             loggedin = false;
             logFileName = request.getAttribute("clientId").toString();
         } else {
             loggedin = true;
             logFileName = auth.getName();
         }
         
         String param = request.getQueryString();
         if (param == null)
             param = "";
         if (!request.getRequestURL().toString().contains(".")) {
             log(logFileName, request.getServletPath() + param,
                     request.getRemoteAddr(), loggedin,
                     request.getHeader("User-Agent"),
                     request.getHeader("Referer"));
         }        
     }
     //Following information will be stored in log file
     // time, url,IP,loggedIn,UA,Redirect
     public static void log(String userName,String url,String IP,boolean loggedin,String UA,String redirectURL)
     {
         if(Logger.getRootLogger().getAppender(userName)==null){ //create FileAppender if necessary
             RollingFileAppender fa = new RollingFileAppender();
             fa.setName(userName);
             fa.setFile(System.getenv("ENWIDA_HOME")+"/log/"+userName+".log");
             fa.setLayout(new PatternLayout("%n%d{ISO8601} %m"));
             fa.setThreshold(Level.INFO);
             fa.setMaxFileSize("1MB");
             fa.activateOptions();
             Logger.getRootLogger().addAppender(fa);
           }
           FileAppender fa = (FileAppender) Logger.getRootLogger().getAppender(userName);
 
           logger.info("|"+url+"|"+IP+"|"+loggedin+"|"+UA+"|"+redirectURL);               
           Logger.getRootLogger().removeAppender(fa);
     }
     @Override
     public boolean preHandle(HttpServletRequest request,
             javax.servlet.http.HttpServletResponse response, Object handler)
             throws Exception {
         String clientID=null;
         String sessionID=null;
         //check if there is a cookie, get clientID and sessionID
         //(may not be needed if there is already clientID)
         Cookie[] cookies = request.getCookies();
         if (cookies!=null){
             for (Cookie cookie : cookies) {
                 if(cookie.getName().equalsIgnoreCase("enwida.de")){
                     clientID=cookieSecurityService.decryptJsonString(cookie.getValue(), Constants.ENCRYPTION_KEY);
                 }
                 if(cookie.getName().equalsIgnoreCase("JSESSION")){
                     sessionID=cookie.getValue();
                 }
             }
         }
         //no clientID cookie
         if (clientID==null){
             
             if (sessionID==null){
                 SecureRandom random = new SecureRandom();
                 sessionID= new BigInteger(130, random).toString(32);
             }
             //user sessionID as clientID
             clientID=sessionID;
             Cookie cookie=new Cookie("enwida.de",cookieSecurityService.encryptJsonString(clientID, Constants.ENCRYPTION_KEY));
             sessionID=cookie.getValue();
             response.addCookie(cookie);
         }
         
         request.setAttribute("clientId", clientID);
         return super.preHandle(request, response, handler);
     }
 }
