 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers.votacion;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author _r
  */
 public class PresidenteVotacionServlet extends HttpServlet {
 
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
         // NO FUCKING CACHE (1.1)
         response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
         // NO FUCKING CACHE (1.0)
         response.setHeader("Pragma", "no-cache");
         response.setDateHeader("Expires", 0);

         request.setAttribute("entityName", "Nuevo usuario");
         request.setAttribute("adminDesc", "Añada un nuevo usuario");
         request.setAttribute("eleccionPresidente", "not null");
 
         request.getRequestDispatcher("/WEB-INF/templates/votacionPresidente.jsp")
                 .forward(request, response);
         return;
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
