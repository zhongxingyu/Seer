 package edu.chl.group10.webapp_project.Login;
 
 import edu.chl.group10.core.Customer;
 import edu.chl.group10.core.Group10;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Hampus
  */
 @WebServlet(name = "LoggingInServlet", urlPatterns = {"/loggedin"})
 public class LoggingInServlet extends HttpServlet {
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
                     String typeOfAction = request.getParameter("action");
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         out.println(typeOfAction);
         try {
             if( typeOfAction.equals("login")) {
                 String email = request.getParameter("username");
                 String password = request.getParameter("password");
                 String hashedPassword = hash(password);
                 Customer customer = Group10.INSTANCE.getCustomerList().
                         getByEmailAndPassword(email, hashedPassword);              
                 
                 if( customer != null ){
                     
                    response.sendRedirect("faces/loggedIn.xhtml?id="+customer.getID());
                 }else {
                     response.sendRedirect("faces/wrongLoggInInfo.xhtml");
                 }
             }
 
         } finally {            
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
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
      * Handles the HTTP
      * <code>POST</code> method.
      *
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
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
     private String hash(String password) {
     String salt = "GR@$OU1@P0.10&#S%^A$L*T";
     String hash = md5(password + salt);
         return hash;
     }
     public static String md5(String input) {
          
         String md5 = null;
          
         if(null == input){ 
             return null;
         }
         try {
              
         //Create MessageDigest object for MD5
         MessageDigest digest = MessageDigest.getInstance("MD5");
          
         //Update input string in message digest
         digest.update(input.getBytes(), 0, input.length());
  
         //Converts message digest value in base 16 (hex) 
         md5 = new BigInteger(1, digest.digest()).toString(16);
  
         } catch (NoSuchAlgorithmException e) {
  
             e.printStackTrace();
         }
         return md5;
     }
 }
