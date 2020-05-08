 package web.servlets;
 
 import galleryService.interfaces.AutentificationService;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import persistence.exception.ValidationException;
 import persistence.struct.User;
 import web.utils.JSPUtils;
 import web.utils.SessionUtils;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: agnidash
  * Date: 4/22/13
  * Time: 12:57 PM
  */
 @Controller
 @RequestMapping("/LogIn")
 public class LogIn {
 
     //Session params
     public static final String USER = "user";
     public static final String WARNING = "warning";
 
     //Actions
     public static final String AUTENTIFICATION = "autentification";
     public static final String REGISTRATION = "registration";
 
     //Fields
     public static final String LOGIN = "login";
     public static final String PASSWORD = "password";
 
     //Logger
     private static final Logger LOGGER = Logger.getLogger(LogIn.class);
 
     @Required
     public void setService(AutentificationService service) {
         this.service = service;
     }
 
     private AutentificationService service;
 
     @RequestMapping(method = RequestMethod.POST)
     protected void doPost(HttpServletRequest in, HttpServletResponse out) throws ServletException, IOException {
         identificationOrRegistration(in, out);
     }
 
     @RequestMapping(method = RequestMethod.GET)
     protected void doGet(HttpServletRequest in, HttpServletResponse out) throws ServletException, IOException {
         identificationOrRegistration(in, out);
     }
 
     private void identificationOrRegistration(HttpServletRequest in, HttpServletResponse out) throws ServletException, IOException {
 
         // always remove old warnings and notifications
         SessionUtils.removeAttribute(in, WARNING);
         SessionUtils.removeAttribute(in, ImageUpload.UPLOAD_MESSAGE);
 
         if (isUserAuthenticated(in)) {
             success(out);
 
         } else {
 
             if (isAutentification(in)) {
                 restoreUserInSession(in, out, AUTENTIFICATION);
 
             } else if (isRegistration(in)) {
                 restoreUserInSession(in, out, REGISTRATION);
 
             } else {
                 JSPUtils.showHomePage(out);
             }
         }
     }
 
     private void restoreUserInSession(HttpServletRequest in, HttpServletResponse out, String type) throws IOException {
 
         String login = getLogin(in);
         String password = getPassword(in);
 
         try {
 
             AutentificationService service = getAutentificationService();
 
             if (AUTENTIFICATION.equals(type)) {
                 User user = service.autentificate(login, password);
                 addToSessionAndRedirect(in, out, user);
 
             } else if (REGISTRATION.equals(type)) {
                 User user = service.register(login, password);
                 addToSessionAndRedirect(in, out, user);
 
             } else {
                 /*
                  if user get access to this class like this  ../LogIn
                  redirect him to login page without error message
                  */
                 logInPage(out);
             }
 
         } catch (ValidationException e) {
             errorOutput(in, out, e);
 
         } catch (RuntimeException e) {
            e.printStackTrace();

             LOGGER.error("Failed on auth user", e);
 
             errorOutput(in, out, e);
         }
     }
 
     private void addToSessionAndRedirect(HttpServletRequest in, HttpServletResponse out, User user) throws IOException {
         SessionUtils.addAttribute(in, USER, user);
 
         success(out);
     }
 
     private void errorOutput(HttpServletRequest in, HttpServletResponse out, Throwable e) throws IOException {
         SessionUtils.addAttribute(in, WARNING, e.getMessage());
 
         logInPage(out);
     }
 
     private void logInPage(HttpServletResponse out) throws IOException {
         out.sendRedirect("/LogIn.jsp");
     }
 
     private void success(HttpServletResponse out) throws IOException {
         JSPUtils.showHomePage(out);
     }
 
     private boolean isAutentification(HttpServletRequest in) {
         return in.getParameterMap().containsKey(AUTENTIFICATION);
     }
 
     private boolean isRegistration(HttpServletRequest in) {
         return in.getParameterMap().containsKey(REGISTRATION);
     }
 
     private String getLogin(HttpServletRequest in) {
         return in.getParameter(LOGIN);
     }
 
     private String getPassword(HttpServletRequest in) {
         return in.getParameter(PASSWORD);
     }
 
     public static boolean isUserAuthenticated(HttpServletRequest in) {
         return SessionUtils.isAttributePresent(in, LogIn.USER);
     }
 
     public AutentificationService getAutentificationService() {
         return service;
     }
 }
