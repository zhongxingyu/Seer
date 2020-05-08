 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.adde.webbapp.frontend.servlet;
 
 import com.adde.webbapp.model.dao.PersonCatalogue;
 import com.adde.webbapp.model.entity.Person;
 import java.io.IOException;
 import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Eric Ahlberg (eahlberg@gmail.com)
  */
 @WebServlet(name = "FrontController", urlPatterns = {"/fc"})
 public class FrontController extends HttpServlet {
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         HttpSession session = request.getSession(true);
         response.setContentType("text/html;charset=UTF-8");
 
         String view = request.getParameter("view");
 
         DAOFactoryWrapper daoFactoryWrapper = (DAOFactoryWrapper) request.getServletContext().getAttribute("DAOFACTORY");
         PersonCatalogue personCatalogue = daoFactoryWrapper.getPersonDAO();
         
         // Navigation
         if (view != null) {
             switch (view) {
                 case "home":
                     request.getRequestDispatcher("WEB-INF/index.jspx").forward(request, response);
                     break;
                 case "signup":
                     request.getRequestDispatcher("WEB-INF/jsp/signup.jspx").forward(request, response);
                     break;
                 case "about":
                     request.getRequestDispatcher("WEB-INF/jsp/about.jspx").forward(request, response);
                     break;
                 case "contact":
                     request.getRequestDispatcher("WEB-INF/jsp/contact.jspx").forward(request, response);
                     break;
                 case "login":
                     request.getRequestDispatcher("WEB-INF/jsp/login.jspx").forward(request, response);
                     break;
                 case "welcome":
                     String username = request.getParameter("username");
                     String email = request.getParameter("email");
                     String password = request.getParameter("password");
 
                    if (username != null && email != null && password != null) {
                        request.setAttribute("username", username);
                        request.setAttribute("email",email);
                        request.setAttribute("password", password);
                         personCatalogue.add(new Person(username, email, password));
                        response.sendRedirect("content/main.xhtml");
                    } else {
                        Logger.getAnonymousLogger().log(Level.INFO, "Some user input was null!");
                        request.getRequestDispatcher("WEB-INF/jsp/signup.jspx").forward(request, response);
                     }
                     //request.getRequestDispatcher("WEB-INF/jsp/rs/welcome.jspx").forward(request, response);
                     break;
                 case "logedin":
                     //request.getRequestDispatcher("WEB-INF/jsp/rs/welcome.jspx").forward(request, response);
                     response.sendRedirect("content/main.xhtml");
                     break;
                 default:
                     ;
             }
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
 }
