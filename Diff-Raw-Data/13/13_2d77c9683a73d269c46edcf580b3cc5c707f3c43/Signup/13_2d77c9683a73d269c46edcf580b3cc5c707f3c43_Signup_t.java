 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.uni.sushilkumar.geodine.auth;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import me.uni.sushilkumar.geodine.db.DBConnection;
 
 /**
  *
  * @author sushil
  */
 @WebServlet(name = "Signup", urlPatterns = {"/signup"})
 public class Signup extends HttpServlet {
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException, SQLException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         DBConnection con=null;
         try {
             
             /* The DB LOGIN logic  */
             String dbHost=getServletContext().getInitParameter("dbHost");
             String dbUser=getServletContext().getInitParameter("dbUser");
             String dbPassword=getServletContext().getInitParameter("dbPassword");
             String dbName=getServletContext().getInitParameter("dbName");
             con=new DBConnection(dbUser,dbPassword,dbHost,dbName);
             String fname=request.getParameter("first-name");
             String lname=request.getParameter("last-name");
             String email=request.getParameter("user-name");
             String pass=request.getParameter("user-pass");
             boolean alreadyExists=con.alreadyExists(email);
             if(alreadyExists)
                 out.println("Email already exists");
             else
             {
                 con.signup(fname, lname, email, pass);
                 out.println("Congratulations. Signup Complete");
             }
             
             
            
 
         } finally {            
             out.close();
             con.close();
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
          PrintWriter out = response.getWriter();
         try {
              
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Error at geodine :(</title>");  
             out.println("</head>");
             out.println("<body>");
             out.println("Use <strong>POST</strong> instead of <strong>GET</strong>.");
             out.println("</body>");
             out.println("</html>");
             
              
         } finally {            
             out.close();
         }
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
         try {
             processRequest(request, response);
         } catch (SQLException ex) {
             Logger.getLogger(Signup.class.getName()).log(Level.SEVERE, null, ex);
         }
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
