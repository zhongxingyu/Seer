 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import entity.Tipomeiotransporte;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import session.TipomeiotransporteFacade;
 
 /**
  *
  * @author miltonnunes52
  */
 @WebServlet(name = "TipoMeioTransporteServlet", 
   urlPatterns = {"/TipoMeioTransporte",
                 "/TipoMeioTransporte/register",
                 "/TipoMeioTransporte/add",
                 "/TipoMeioTransporte/view",
                 "/TipoMeioTransporte/update"})
 public class TipoMeioTransporteServlet extends HttpServlet {
     
     @EJB
     private  TipomeiotransporteFacade tipomeiotransporteFacade;
 
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
        
         HttpSession session = request.getSession();
         
         String userPath = request.getServletPath();
         String url = "";
        
         String erro = "";
         boolean ok = true;
         
         Tipomeiotransporte tipomeiotransporte;
         
         String nome;
         
         if(userPath.equals("/TipoMeioTransporte")){
            //session.setAttribute("listmeiostransporte", tipomeiotransporteFacade.findAll());
             getServletContext().setAttribute("listmeiostransporte", tipomeiotransporteFacade.findAll());
             url = "tiposmeiotransporte";
         } else if(userPath.equals("/TipoMeioTransporte/register")){
             url= "register";
         } else if(userPath.equals("/TipoMeioTransporte/add")){
             nome = request.getParameter("nome");
             ok = validate.TipoMeioTransporteValidator.validateFormRegister(nome, request);
             
             if (!ok || !erro.isEmpty()) {
                 session.setAttribute("MessageError", erro);
                 response.sendRedirect("/EuroTripsFinder/TipoMeioTransporte/register");
                 return;
             }
             
             
             tipomeiotransporte = new Tipomeiotransporte();
             tipomeiotransporte.setNome(nome);
             
            //session.setAttribute("tipo", tipomeiotransporte);
            getServletContext().setAttribute("tipo", tipomeiotransporte);
             
             try {
                 tipomeiotransporteFacade.create(tipomeiotransporte);
             } catch (Exception ex) {
                 erro = "Erro ao inserir tipo de meio de transporte ";
                 session.setAttribute("MessageError", erro);
                 response.sendRedirect("/EuroTripsFinder/TipoMeioTransporte/register");
                 return;
             }
 
             session.setAttribute("MessageSuccess", "Tipo de meio de transporte inserido.");
 
             response.sendRedirect("/EuroTripsFinder/TipoMeioTransporte");
             return;
         } else if(userPath.equals("/TipoMeioTransporte/view")){
             
             url= "view";
             //deve faltar aqui meter qual o tipo que vai ser mostrado
         } else if(userPath.equals("/TipoMeioTransporte/update")){
             nome = request.getParameter("nome");
             ok = validate.TipoMeioTransporteValidator.validateFormRegister(nome, request);
             
             if (!ok || !erro.isEmpty()) {
                 session.setAttribute("MessageError", erro);
                 response.sendRedirect("/EuroTripsFinder/TipoMeioTransporte/register");
                 return;
             }
             
            //tipomeiotransporte = (Tipomeiotransporte) session.getAttribute("tipo");
            tipomeiotransporte = (Tipomeiotransporte) getServletContext().getAttribute("tipo");
             tipomeiotransporte.setNome(nome);
             
             try {
                 tipomeiotransporteFacade.edit(tipomeiotransporte);
             } catch (Exception ex) {
                 erro = "Erro ao actualizar tipo de meio de transporte ";
                 session.setAttribute("MessageError", erro);
                 response.sendRedirect("/EuroTripsFinder/TipoMeioTransporte/register");
                 return;
             }
 
             session.setAttribute("MessageSuccess", "Tipo de meio de transporte actualizado.");
 
             response.sendRedirect("/EuroTripsFinder/TipoMeioTransporte");
             return;
             
         }
         
         try {
             request.getRequestDispatcher("/WEB-INF/view/TipoMeioTransporte/" + url + ".jsp").forward(request, response);
         } catch (Exception e) {
         
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
