 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controlador.Adminstrador;
 
 import Controlador.HelperBusquedaInicial;
 import Modelo.Administrador;
 import Modelo.Cd;
 import Modelo.Comprador;
 import Modelo.Usuario;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Estevo
  */
 public class ControladorAdministrador extends HttpServlet {
 
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
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         String pagina = null;
 
         if (request.getParameter("busca_usuario") != null) {
             HelpBuscaUsuario buscaUsuario = new HelpBuscaUsuario();
 
             request.setAttribute("listaCompradores",
                                  buscaUsuario.buscaCompradores(
                                  request.getParameter("mail"),
                                  request.getParameter("nombre"),
                                  request.getParameter("apellidos"),
                                  request.getParameter("descuento")));
             request.setAttribute("listaAdministradores",
                                  buscaUsuario.buscaAdministradores(
                                  request.getParameter("mail"),
                                  request.getParameter("nombre"),
                                  request.getParameter("apellidos")));
             pagina = "VistaAdministrarUsuarios.jsp";
         }
         
         if (request.getParameter("borrar_User") != null) {
             HelpBorrarUsuario borraUser = new HelpBorrarUsuario();
             borraUser.borrarUsuario(new Comprador(request.getParameter("borrar_User")));
               HelpBuscaUsuario busqueda = new HelpBuscaUsuario();
             ArrayList<Comprador> comp =busqueda.buscaCompradores("", "", "", "");
             ArrayList<Administrador> admin=busqueda.buscaAdministradores("", "", "");
             request.setAttribute("listaCompradores", comp);
             request.setAttribute("listaAdministradores", admin);
             pagina = "VistaAdministrarUsuarios.jsp";
         }
         
         if (request.getParameter("borrar_Admin") != null) {
             HelpBorrarUsuario borraUser = new HelpBorrarUsuario();
             borraUser.borrarAdministrador(new Administrador(request.getParameter("borrar_Admin")));
             
             HelpBuscaUsuario busqueda = new HelpBuscaUsuario();
             ArrayList<Comprador> comp =busqueda.buscaCompradores("", "", "", "");
             ArrayList<Administrador> admin=busqueda.buscaAdministradores("", "", "");
             request.setAttribute("listaCompradores", comp);
             request.setAttribute("listaAdministradores", admin);
             pagina = "VistaAdministrarUsuarios.jsp";
         }
         
         if (request.getParameter("busca_cd") != null) {
             HelpOperacionCd operacionCd = new HelpOperacionCd();
             request.setAttribute("listaCds", operacionCd.buscarCd(
                                  request.getParameter("titulo"),
                                  request.getParameter("autor"),
                                  request.getParameter("anho"),
                                  request.getParameter("precio")));
              HelperBusquedaInicial busqueda = new HelperBusquedaInicial();
             ArrayList<Cd> cds = busqueda.busca();
             request.setAttribute("listaCds", cds);
             
 
             pagina = "VistaVisualizarCd.jsp";
         }
         
         if (request.getParameter("anadir_cd") != null) {
             String mensaje = "Insertado Correctamente";
             HelpOperacionCd operacionCd = new HelpOperacionCd();
             
             if (!operacionCd.aadirCd(request.getParameter("titulo"),
                                       request.getParameter("autor"),
                                       request.getParameter("pais"),
                                       Float.parseFloat(request.getParameter("precio")),
                                       request.getParameter("anho"))) {
                 mensaje = "El elemento ya est insertado";
 
             }
 
             request.setAttribute("mensaje2", mensaje);
             pagina = "VistaAnadirCd.jsp";
         }
         
         if (request.getParameter("borrar_cd") != null) {
             HelpOperacionCd operacionCd = new HelpOperacionCd();
             operacionCd.borrarCd(request.getParameter("borrar_cd"));
             HelperBusquedaInicial busqueda = new HelperBusquedaInicial();
             ArrayList<Cd> cds = busqueda.busca();
             request.setAttribute("listaCds", cds);
             pagina = "VistaVisualizarCd.jsp";
         }
         
         if (request.getParameter("anadir_stock") != null) {
             HelpOperacionCd operacionCd = new HelpOperacionCd();
             operacionCd.anhadirStockCd(request.getParameter("anadir_stock"),
                                         request.getParameter("cantidad"));
             HelperBusquedaInicial busqueda = new HelperBusquedaInicial();
             ArrayList<Cd> cds = busqueda.busca();
             request.setAttribute("listaCds", cds);
             
             
            
              pagina = "VistaVisualizarCd.jsp";
         }
             
         if (request.getParameter("cambiar_pass") != null) {
             HelpPassword helpPassword = new HelpPassword();
             helpPassword.cambiarPassword(request.getParameter("cambiar_pass"),
                                           request.getParameter("pass"));
             HelpBuscaUsuario busqueda = new HelpBuscaUsuario();
             ArrayList<Comprador> comp =busqueda.buscaCompradores("", "", "", "");
             ArrayList<Administrador> admin=busqueda.buscaAdministradores("", "", "");
             request.setAttribute("listaCompradores", comp);
             request.setAttribute("listaAdministradores", admin);
              
               pagina = "VistaAdministrarUsuarios.jsp";
              }
 
 
         RequestDispatcher view = request.getRequestDispatcher(pagina);
         view.forward(request, response);
 
 
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
