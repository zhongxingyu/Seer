 package net.wazari.view.servlet;
 
 import java.io.IOException;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.wazari.service.CarnetLocal;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSession.Action;
 import net.wazari.service.exchange.ViewSession.Special;
 import net.wazari.service.exchange.ViewSessionCarnet;
 import net.wazari.service.exchange.ViewSessionCarnet.ViewSessionCarnetDisplay;
 import net.wazari.service.exchange.ViewSessionCarnet.ViewSessionCarnetEdit;
 import net.wazari.service.exchange.ViewSessionCarnet.ViewSessionCarnetSubmit;
 import net.wazari.service.exchange.xml.carnet.XmlCarnetSubmit;
 import net.wazari.view.servlet.DispatcherBean.Page;
 import net.wazari.view.servlet.exchange.xml.XmlCarnets;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @WebServlet(
     name = "Carnets",
     urlPatterns = {"/Carnets"}
 )
 @Stateless
 public class Carnets extends HttpServlet{
     @EJB private DispatcherBean dispatcher ;
     private static final long serialVersionUID = 1L;
     
     @EJB
     private CarnetLocal carnetService;
 
     public XmlCarnets treatCARNETS(ViewSessionCarnet vSession)
             throws WebAlbumsServiceException {
         XmlCarnets output = new XmlCarnets() ;
         
         Special special = vSession.getSpecial();
         if (special == Special.TOP5) {
             output.topCarnets = carnetService.treatTOP(vSession);
             return output ;
         }
         
         Action action = vSession.getAction();
         XmlCarnetSubmit submit = null;
         if(vSession.isSessionManager()) {
             //prepare SUBMIT messag
             log.info("treat Carnet/"+action);
             if (action == Action.SUBMIT || action == Action.SAVE) {
                 submit = carnetService.treatSUBMIT((ViewSessionCarnetSubmit) vSession);
                 if (action == Action.SAVE) {
                     if (!submit.valid)
                         output.exception = submit.exception ;
                     else
                        output.message = "true" ;
                    log.info("-->"+submit.valid);
                     return output;
                     
                 } else if (submit != null && !submit.valid)
                     action = Action.EDIT;
             }
 
             if (action == Action.EDIT) {
                 output.edit = carnetService.treatEDIT((ViewSessionCarnetEdit) vSession, submit);
             }
         }
 
         if (action != Action.EDIT) {
             //afficher la liste des albums de ce theme
             output.display = carnetService.treatDISPLAY((ViewSessionCarnetDisplay)vSession, submit);
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
         dispatcher.treat(this.getServletContext(), Page.CARNET, request, response);
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
         return "Displays the albums of the theme";
     }// </editor-fold>
     private static final Logger log = LoggerFactory.getLogger(Albums.class.getName());
 }
