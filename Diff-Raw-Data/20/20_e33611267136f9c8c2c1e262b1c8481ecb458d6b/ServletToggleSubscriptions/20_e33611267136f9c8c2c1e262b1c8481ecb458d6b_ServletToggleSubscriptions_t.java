 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.yace.web.servlets.admin;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.yace.entity.Ysetting;
 import net.yace.facade.YsettingFacade;
 import net.yace.web.utils.ServicesLocator;
 import net.yace.web.utils.YaceUtils;
 
 /**
  *
  * @author MaBoy <bruno.boi@student.helha.be>
  */
 public class ServletToggleSubscriptions extends HttpServlet {
 
     private final static String VUE_PRESENTATION = "welcome.jsp";
     private final static String VUE_INSCRIPTIONS_ADMIN = "WEB-INF/view/admin/inscriptions.jsp";
 
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
         YaceUtils.SessionState state = YaceUtils.getSessionState(request);
         
         if (state == YaceUtils.SessionState.admin) {
 
             // Aide contextuelle
             Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
 
             List<String> infoBoxes = new ArrayList<String>();
             List<String> tipBoxes = new ArrayList<String>();
 
             infoBoxes.add("En cours de rédaction");
             tipBoxes.add("En cours de rédaction");
 
             asideHelp.put("tip", tipBoxes);
             asideHelp.put("info", infoBoxes);
 
             request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
 
             // On nomme et affiche la page
             request.setAttribute("pageTitle", "Gestion des inscriptions - Administration du site");
             request.getRequestDispatcher(VUE_INSCRIPTIONS_ADMIN).forward(request, response);
 
         } else if (state == YaceUtils.SessionState.noauth) {
             request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
         } else {
             YaceUtils.displayAdminError(request, response);
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
         YaceUtils.SessionState state = YaceUtils.getSessionState(request);
 
         if (state == YaceUtils.SessionState.admin) {
 
             YsettingFacade settingFac = ServicesLocator.getSettingFacade();
             Ysetting setSubscriptions = settingFac.findByName("subscribeOk");
 
             if (setSubscriptions.getVal().equals("true")) {
                 setSubscriptions.setVal("false");
             } else {
                 setSubscriptions.setVal("true");
             }
 
             settingFac.edit(setSubscriptions);
 
         }
         
         doGet(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "(Dés)activation des inscriptions";
     }
 }
