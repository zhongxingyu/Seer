/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package views.votacion;
 
 import java.io.IOException;
import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import models.Alumno;
 import utils.HelperSQL;
 
 /**
  *
  * @author _r
  */
 public class VotacionIndexPresidenteServlet extends HttpServlet {
 
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
 
         String message = "DONT HURS ME DONT HURS ME PLEASE";
         String votacion_realizada;
         votacion_realizada =
                 String.valueOf(request.getSession().getAttribute("voto_p"));
 
         if (votacion_realizada != null) {
             if (votacion_realizada == "true") {
                 message = "El alumno "
                         + request.getSession().getAttribute("codigo")
                         + " ya ha realizado la votacion";
                 request.getSession().invalidate();
             }
         }
 
         request.setAttribute("message", message);
         request.getRequestDispatcher(
                 "/WEB-INF/templates/votacionPresidenteIndex.jsp")
                 .forward(request, response);
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
 
         System.out.println("\n\nBAYBE DON?T HURRS ME BAYBE DON?T HURS ME\n\n");
         
         Alumno alumno = new Alumno();
         ArrayList<String> values = new ArrayList<>();
         values.add("*");
         String alumnoCodigo = "";
         if (request.getParameter("codigo") != null) {
             alumnoCodigo = request.getParameter("codigo");
         } else {
             response.sendRedirect(request.getContextPath()
                     + "/login/votacion/presidente");
         }
         List<HashMap<String, Object>> alumnos = HelperSQL.obtenerFilas(
                 alumno.getTableName(), values, "WHERE codigo = "
                 + alumnoCodigo);
 //shame on me.
         if (alumnos.size() == 1) {
             HttpSession session = request.getSession(true);
             session.setAttribute("codigo", alumnos.get(0).get("codigo"));
            session.setAttribute("nie", alumnos.get(0).get("NIE"));
            session.setAttribute("votoP", alumnos.get(0).get("voto_p"));
             response.sendRedirect(request.getContextPath()
                     + "/votacion/presidente");
         } else {
             response.sendRedirect(request.getContextPath()
                    + "/login/votacion/presidente");
         }
 
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
