 package eas.asu.edu.snac.activitymanager.servlets;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import eas.asu.edu.snac.activitymanager.networking.MessageSender;
 import edu.asu.eas.snac.activitymanager.messages.FeedbackMessage;
 import edu.asu.eas.snac.activitymanager.messages.FeedbackType;
 import edu.asu.eas.snac.activitymanager.messages.RegisterMessage;
 import edu.asu.edu.snac.activitymanager.util.Constants;
 import edu.asu.edu.snac.activitymanager.util.SHA1;
 import edu.asu.edu.snac.activitymanager.util.Validate;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Fred
  */
 public class RegisterNewUser extends HttpServlet {
    
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
     {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try
         {
             // Get all the information from the POST/GET header
             String username     = request.getParameter("username");
             String password     = request.getParameter("password");
             String password2    = request.getParameter("password2");
             String email        = request.getParameter("email");
             String phone        = request.getParameter("phoneNumber");
            String messageResponse = "";
             boolean isValid = false;
 
             // Check if data is valid
             if (!Validate.Username(username))
             {
                messageResponse = "Invalid Username";
             }
             else if (!Validate.Password(password))
             {
                messageResponse = "Invalid Password";
             }
             else if( !password.equals(password2) )
             {
                messageResponse = "Passwords do not match!";
             }
             else if( !Validate.Email(email) )
             {
                messageResponse = "Invalid email address.";
             }
             else if (!Validate.Phone(phone))
             {
                messageResponse = "Invalid phone number";
             }
             else
             {
                 isValid = true;
             }
             
             
             if( isValid )
             {
                 // Create registration message and send it
                 RegisterMessage reg = new RegisterMessage();
                 reg.setUsername(username);
                 reg.setPassword(SHA1.sha1(password));
                 reg.setEmail(email);
                 reg.setPhone(Validate.StripPhoneNum(phone));
 
                 //TODO: I don't know what this is for, ask fredzilla
                 out.println("Phone Number: " + request.getParameter("phoneNumber"));
                 FeedbackMessage fbm = (FeedbackMessage) MessageSender.sendMessage(reg);
                 // TODO: check if fbm returns
                 if (fbm.getMsgType() == FeedbackType.SUCCESS)
                 {
                     request.getSession(true).setAttribute(Constants.LOGGED_IN_TOKEN, username);
                     // Redirect to Main Menu
                     response.sendRedirect("Mobile/Menu.jsp");
                 }
                 else
                 {
                     //TODO: Send some session variables to acknowledge user
                     response.sendRedirect("Mobile/Register.jsp");
                 }
 
             }
             else
             {
                 // Something in the data was not valid
                 out.println("Your Registration Information is bad.");
                 request.getSession().setAttribute("username", username);
                 request.getSession().setAttribute("password", password);
                 request.getSession().setAttribute("email", email);
                 request.getSession().setAttribute("phone", phone);
                 request.getSession().setAttribute("message", messageResponse);
                 response.sendRedirect("InvalidData.jsp");
             }
         }
         finally
         {
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         processRequest(request, response);
     } 
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
 }
