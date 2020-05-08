 package net.wazari.view.servlet;
 
 import java.io.IOException;
 import java.security.Principal;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.wazari.service.exchange.ViewSession.Action;
 import net.wazari.service.exchange.ViewSessionLogin;
 import net.wazari.service.exchange.xml.XmlLogin;
 import net.wazari.view.servlet.DispatcherBean.Page;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @WebServlet(
     name = "Users",
     urlPatterns = {"/Users"}
 )
 @Stateless
 public class Users extends HttpServlet {
     private static final Logger log = LoggerFactory.getLogger(Users.class.getCanonicalName()) ;
     private static final long serialVersionUID = 1L;
     @EJB private DispatcherBean dispatcher ;
 
     public XmlLogin treatLogin(ViewSessionLogin vSession, HttpServletRequest request, HttpServletResponse response) throws IOException {
         XmlLogin output = new XmlLogin();
         try {
             Action action = vSession.getAction();
             log.info("Action: {}", action);
             if (Action.LOGIN == action) {
                 String userName = vSession.getUserName();
                 
                 if (userName == null) {
                     log.info("userName empty, force login page");
                     output.login = true ;
                     return output;
                 }
 
                 String pass = vSession.getUserPass();
                 
                 Principal pr = request.getUserPrincipal();
                 if (pr != null) {
                     log.info("already logged in: {}", pr.getName());
                    if (pr.getName().equals(userName)) {
                         return null;
                    } else {
                         request.logout();
                    }
                 }
                 log.info("try to login: {}", userName);
                 request.login(userName, pass);
                 output.valid = true ;
                 Boolean dontRedirect = vSession.dontRedirect();
                 log.info("authentication valid {}",dontRedirect);
                if (dontRedirect == null || !dontRedirect) {
                     response.sendRedirect("Index");
                }
                 
                 return null ;
             } else if (Action.CHANGE_IS_MANAGER == action) {
                 boolean wantManager = vSession.getwantManager();
                 log.warn("set Session manager to "+wantManager);
                 vSession.setSessionManager(wantManager);
             } else {
                 output.login = true ;
             }
 
         } catch (javax.servlet.ServletException e) {
             log.info("authentication failed : {}", e.getMessage());
             output.denied = true ;
             output.login = true ;
         } 
         return output ;
     }
 
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         dispatcher.treat(this.getServletContext(), Page.USER, request, response);
     }
 
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
         return "Login page";
     }// </editor-fold>
 }
