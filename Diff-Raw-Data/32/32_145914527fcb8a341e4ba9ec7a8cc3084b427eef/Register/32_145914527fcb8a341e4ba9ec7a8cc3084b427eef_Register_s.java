 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import com.mysql.jdbc.Connection;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import DataDAO.*;
 import javax.servlet.RequestDispatcher;
 
 /**
  *
  * @author Michelle
  */
 public class Register extends HttpServlet {
 
     private static Connection con;
 
     /**
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (ClassNotFoundException e) {
             throw new AssertionError(e);
         }
         String connectionStr = "jdbc:mysql://localhost/DisasterAssessment";
         //String connectionStr = "jdbc:mysql:///DisasterAssessment";
         String user = "root";
         String pw_con = "";
         try {
             con = (Connection) DriverManager.getConnection(connectionStr, user, pw_con);
             String firstName = request.getParameter("volunteer_firstname");
             String lastName = request.getParameter("volunteer_lastname");
             String email_input = request.getParameter("volunteer_email");
             String email = email_input.toLowerCase();
             String password = request.getParameter("volunteer_password");
             String passwordConfirm = request.getParameter("volunteer_password1");
             User Client = null;
             if (password.equals(passwordConfirm) && email != null) {
                 String registerMessage = null;
                 String destination = null;
                 if (User.lookup(email, con) != null) {
                    registerMessage = "Sorry! The email address has already been registered. Please try another one.";
                    destination = "/index.jsp";
                 } else {
 
                     Client = new User(password, lastName, firstName, "", "", email);
 
                     int flag = Client.Insert(con);
 
                     if (flag == 1) {
                        registerMessage = "Congradulations! You've been registered successfully!";
                         destination = "/index.jsp";
                     } else {
                        registerMessage = "We're sorry! You failed to register. Please try again!";
                         destination = "/register.jsp";
                     }
                 }
                 request.setAttribute("RegisterMessage", registerMessage);
                 RequestDispatcher red = getServletContext().getRequestDispatcher(destination);
                 red.forward(request, response);
 
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try {
                 con.close();
             } catch (SQLException ex) {
                 Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
             }
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
