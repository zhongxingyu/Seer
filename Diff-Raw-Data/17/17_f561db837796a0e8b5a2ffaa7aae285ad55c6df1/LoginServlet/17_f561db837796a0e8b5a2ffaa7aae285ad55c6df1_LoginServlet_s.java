 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.grocerypricebook.servlet;
 
 import net.grocerypricebook.model.dbmanagers.UsersManager;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 
 @WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
 public class LoginServlet extends HttpServlet {
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
             String username = request.getParameter("username");
             String password = request.getParameter("password");
             UsersManager users = new UsersManager();
             RequestDispatcher dispatch;
             HttpSession session = request.getSession();
             try{
                 if(users.passwordMatch(username, password)){
                     int userId = users.getUserId(username);
                     session.setAttribute("userId", userId);
                     dispatch = request.getRequestDispatcher("welcome.jsp");
                 } else {
                     request.setAttribute("loginError", new Boolean(true));
                     dispatch = request.getRequestDispatcher("index.jsp");
                 }
                 dispatch.forward(request,response);
 
             } catch (SQLException e){
                 System.out.println(e);
             }
     }
 
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
         return "Processes login information.";
     }// </editor-fold>
 }
