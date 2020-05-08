 package org.dataportal;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.dataportal.datasources.Mail;
 import org.dataportal.users.User;
 import org.dataportal.Config;
 
 public class LoginServlet extends HttpServlet implements DataportalCodes{
 	private static final long serialVersionUID = 1L;
 
     private static final String ACCESS = "access";
     private static final String CHANGE_PASS = "changePass";
     private static final String GENERATE_PASS = "generatePass";
     private static final String REGISTER = "register";
     
     private static final String CONTACT_MAIL = Config.get("mail.address");
 
     public LoginServlet() {
         super();
     }
 
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         if(req.getParameter("hash") != null &&
             (req.getParameter("request").equals(REGISTER) || req.getParameter("request").equals(GENERATE_PASS)) ) {
                 doPost(req, resp);
         } else {
             resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
         }
     }
 
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.setCharacterEncoding("UTF-8");
         resp.setContentType("text/json");
         PrintWriter out = resp.getWriter();
         
         try {
             String request = req.getParameter("request");
             String user = req.getParameter("user");
             
             if(request == null) {
                 resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
                 return;
             }
             
             if (request.equals(ACCESS)) {
                 String password = req.getParameter("password");
                 User u = new User(user, password);
                 org.dataportal.model.User dtUser = new org.dataportal.model.User(user);
                 if(u.isActive()) {
                    out.print("{success:true,message:\""+user+"\"}");
                     HttpSession session = req.getSession(true);
                     session.setAttribute(USERACCESS, dtUser);
                 } else {
                     out.print("{success:false,message:\"Access denied.\"}");
                 }
                 
             } else if (request.equals(CHANGE_PASS)) {
                 String password = req.getParameter("password");
                 String newPassword = req.getParameter("newPassword");
                 User u = new User(user, password);
 
                 if(u.changePass(newPassword)) {
                     out.print("{success:true,message:\"Password changed. Use new password to Login.\"}");
                 } else {
                     out.print("{success:false,message:\"Access denied.\"}");
                 }
                 
             } else if (request.equals(GENERATE_PASS)) {
                 if(req.getParameter("hash")==null){
                     String hash = new User().setHash(user, User.ACTIVE);
                     if(hash==null) {
                         out.print("{success:false,message:\"No user registered with this mail address.\"}");
                     } else {
                         Map<String, String> params = new HashMap<String, String>();
                         params.put("link", req.getRequestURL().append("?request="+GENERATE_PASS+"&hash="+hash).toString());
                         params.put("contact", CONTACT_MAIL);
                         Mail.send(user, "[ICOS Data Portal] Password change confirmation", "newPassAskConfirmation", params);
                         
                         out.print("{success:true,message:\"We have sent you instructions to proceed with password change. Check your inbox.\"}");
                     }
                 } else {
                     String hash = req.getParameter("hash");
                     User u = new User();
                     String newPass = u.newPass(hash);
                     if(hash==null) {
                         resp.sendError(HttpServletResponse.SC_FORBIDDEN, "This link is no longer valid.");
                     } else {
                         Map<String, String> params = new HashMap<String, String>();
                         params.put("pass", newPass);
                         params.put("contact", CONTACT_MAIL);
                         Mail.send(u.login(), "[ICOS Data Portal] Your new password", "newPassCreated", params);
                         
                         resp.sendRedirect("./PasswordChanged.html");
                     }
                 }
                 
             } else if (request.equals(REGISTER)) {
                 if(req.getParameter("hash")==null){
                     String password = req.getParameter("password");
                     User u = new User(user, password);
                     if (u.existsUsername()) {
                         out.print("{success:false,message:\"The user already exists.\"}");
                     } else {
                         String hash = u.save();
                         if(hash==null) {
                             out.print("{success:false,message:\"User could not be created.\"}");
                         } else {
                             Map<String, String> params = new HashMap<String, String>();
                             params.put("link", req.getRequestURL().append("?request="+REGISTER+"&hash="+hash).toString());
                             params.put("contact", CONTACT_MAIL);
                             Mail.send(user, "[ICOS Data Portal] New user confirmation", "newUserAskConfirmation", params);
                             
                             out.print("{success:true,message:\"We've sent you an email with further instructions to complete the registration.\"}");
                         }
                     }
                 } else {
                     String hash = req.getParameter("hash");
                     user = new User().activate(hash);
                     if(user==null) {
                         resp.sendError(HttpServletResponse.SC_FORBIDDEN, "This link is no longer valid.");
                     } else {
                         Map<String, String> params = new HashMap<String, String>();
                         params.put("contact", CONTACT_MAIL);
                         Mail.send(user, "[ICOS Data Portal] Welcome to ICOS Data Portal", "newUserCreated", params);
                         
                         resp.sendRedirect("./NewUser.html");
                     }
                 }               
                 
             } else {
                 resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
             } 
         
         } catch (Exception e) {
             e.printStackTrace();
             resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
         }
     }
 }
