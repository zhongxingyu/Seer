Fpackage icms_servlet;
 
 import icms_ejb.*;
 import java.io.IOException;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class ArticlesServlet extends HttpServlet {
 
   @EJB
   private GestionnaireArticlesLocal gestionnaireArticles;
   @EJB
   private GestionnaireUsersLocal gestionnaireUsers;
   // Not EJB
   private String page;
 
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
     response.setContentType("text/html;charset=UTF-8");
 
     SessionsServlet.getSession(request);
     User u = SessionsServlet.getUserFromSession(gestionnaireUsers);
     request.setAttribute("current_user", u);
 
     // Priority for the action parameter passed by the page, not by the servlet config
     int action = request.getParameter("action") != null ? Integer.parseInt(request.getParameter(
             "action")) : getServletConfig().getInitParameter("action") != null ? Integer.parseInt(getServletConfig().
             getInitParameter("action")) : -1;
 
     switch (action) {
       case Config.SHOW:
         String perme = request.getPathInfo().substring(request.getPathInfo().
                 lastIndexOf("/") + 1);
         System.out.println("perme: " + perme);
         Article article = gestionnaireArticles.findByPermalink(perme);
         if (article != null) {
           request.setAttribute("article", article);
           page = "article.jsp";
         } else {
           page = "articles"; // redirect
         }
         break;
 
       default:
         List<Article> listeArticles = gestionnaireArticles.all();
         request.setAttribute("listeArticles", listeArticles);
         page = "index.jsp"; // render
         break;
     }
 
     RequestDispatcher dp = request.getRequestDispatcher("/" + page);
     dp.forward(request, response);
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
