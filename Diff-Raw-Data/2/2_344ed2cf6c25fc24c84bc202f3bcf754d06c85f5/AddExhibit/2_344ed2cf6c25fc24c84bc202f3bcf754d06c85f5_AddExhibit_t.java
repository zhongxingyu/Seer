 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import businessDomainObjects.Exhibit;
 import businessDomainObjects.ExhibitManager;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import logging.Logger;
 import logging.Logger;
 import utility.Redirector;
 
 /**
  *
  * @author Oliver Brooks <oliver2.brooks@live.uwe.ac.uk>
  */
 @WebServlet(name = "AddExhibit", urlPatterns = {"/addExhibit.do"})
 public class AddExhibit extends HttpServlet {
 
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
         String exhibitName = request.getParameter("name");
         String exhibitDescription = request.getParameter("description");
         String audioLevel1ID = request.getParameter("audioIDLevel1");
         String audioLevel2ID = request.getParameter("audioIDLevel2");
         String audioLevel3ID = request.getParameter("audioIDLevel3");
         String audioLevel4ID = request.getParameter("audioIDLevel4");
 
         if (exhibitName == null || exhibitName == "") {
             request.setAttribute("message", "<h2 style='color:red'>Please enter a name for the exhibit</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         } else if (exhibitDescription == null || exhibitDescription == "") {
             request.setAttribute("message", "<h2 style='color:red'>Please enter a description for the exhibit</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         } else if (audioLevel1ID == null || audioLevel1ID == "") {
             request.setAttribute("message", "<h2 style='color:red'>Please select an audio file for level one</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         } else if (audioLevel2ID == null || audioLevel2ID == "") {
             request.setAttribute("message", "<h2 style='color:red'>Please select an audio file for level two</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         } else if (audioLevel3ID == null || audioLevel3ID == "") {
             request.setAttribute("message", "<h2 style='color:red'>Please select an audio file for level three</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         } else if (audioLevel4ID == null || audioLevel4ID == "") {
             request.setAttribute("message", "<h2 style='color:red'>Please select an audio file for level four</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         }
 
         int level1ID, level2ID, level3ID, level4ID;
         try {
             level1ID = Integer.parseInt(audioLevel1ID);
             level2ID = Integer.parseInt(audioLevel2ID);
             level3ID = Integer.parseInt(audioLevel3ID);
             level4ID = Integer.parseInt(audioLevel4ID);
         } catch (NumberFormatException e) {
             request.setAttribute("message", "<h2 style='color:red'>Error parsing audio ID as integer.</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         }
 
         ExhibitManager manager = (ExhibitManager) getServletContext().getAttribute("exhibitManager");
         if (!manager.addExhibit(exhibitName, exhibitDescription, level1ID, level2ID, level3ID, level4ID)) {
             request.setAttribute("message", "<h2 style='color:red'>Error creating exhibit. Please try again</h2>");
             Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
             return;
         }
 
         Exhibit exhibitJustAdded = manager.getListOfExhibits().get(manager.getListOfExhibits().size()-1);
        Logger.Log(Logger.LogType.EXHIBITADD, new String[]{String.valueOf(exhibitJustAdded.getExhibitID()),exhibitJustAdded.getName(), (String)request.getSession().getAttribute("username")});
         request.setAttribute("message", "<h2>Successfully added a new exhibit.</h2>");
         Redirector.redirect(request, response, "/admin/addNewExhibit.jsp");
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
