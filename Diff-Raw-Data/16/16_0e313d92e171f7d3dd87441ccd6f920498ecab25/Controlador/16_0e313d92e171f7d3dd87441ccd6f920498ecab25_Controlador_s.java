 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controlador;
 
 import Controlador.Adminstrador.HelpOperacionCd;
 import Modelo.*;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author Estevo
  */
 public class Controlador extends HttpServlet {
 
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
         Carro carrito = null;
         String pagina = "";
         Comprador comprador = null;
         String mensaje = "";
         DAOComprador DAOcomprador;
         DAOCd DAOcd;
         HashMap<Integer, Venta> carro = new HashMap<Integer, Venta>();
         String nombre, apellidos, mail, password;
         HttpSession sesion = request.getSession(true);
         carrito = (Carro) sesion.getAttribute("carrito");
         Usuario usuario = (Usuario) sesion.getAttribute("usuario");
 
         if (carrito == null) {
             carrito = new Carro();
             sesion.setAttribute("carrito", carrito);
         }
 
         if (request.getParameter("registro") != null) {
 
             nombre = request.getParameter("nombre");
             apellidos = request.getParameter("apellidos");
             mail = request.getParameter("email");
             password = request.getParameter("contra");
 
             HelpRegistro registro = new HelpRegistro();
             registro.registrar(mail, password, nombre, apellidos);
 
             pagina = "index.html";
 
         }
 
         if (request.getParameter("logeo") != null) {
 
             mail = request.getParameter("mail");
             password = request.getParameter("password");
 
             HelpLogin login = new HelpLogin(mail, password);
             usuario = login.comprobarLogin(mail, password);
 
             if (usuario == null) {
 
                 mensaje = "Usuario o contrasea incorrecta";
                request.setAttribute("mensaje", mensaje);
                 sesion.removeAttribute("cerrarSesion");
                 pagina = "index.jsp";
 
            } else {
 
                 if (usuario.getClass().equals(Comprador.class)) {
                     pagina = "index.jsp";
                 } else {
                     pagina = "VistaAdministrador.jsp";
                 }
 
                 mensaje = "Bienvenid@";
 
                 sesion.setAttribute("usuario", usuario);
                 sesion.setAttribute("cerrarSesion", true);
                 sesion.setAttribute("mensaje", mensaje);
 
 
             }
 
 
             HelperBusquedaInicial busqueda = new HelperBusquedaInicial();
             ArrayList<Cd> cds = busqueda.busca();
             request.setAttribute("listaCds", cds);
 
 
         }
 
         if (request.getParameter("cerrarSesion") != null) {
 
             sesion.removeAttribute("usuario");
             sesion.removeAttribute("mensaje");
             sesion.removeAttribute("cerrarSesion");
 
             pagina = "index.html";
 
 
         }
 
         if (request.getParameter("cdAnhadir") != null) {
 
             if (usuario != null) {
 
                 HelpAnhadirCarro anhadir;
                 anhadir = new HelpAnhadirCarro(carrito);
                 anhadir.setCarro(Integer.parseInt(request.getParameter("cdAnhadir")));
                 pagina = "index.html";
             } else {
                 HelperBusquedaInicial busqueda = new HelperBusquedaInicial();
                 ArrayList<Cd> cds = busqueda.busca();
                 request.setAttribute("mensaje", "Debe loguearse para poder comprar");
                 request.setAttribute("listaCds", cds);
                 pagina = "index.jsp";
             }
         }
 
         if (request.getParameter("irCarro") != null) {
             if (usuario != null) {
                 HelpFactura Hfactura = new HelpFactura(carrito, (Comprador) usuario);
                 Factura factura = Hfactura.importeFinal();
                 sesion.setAttribute("factura", factura);
                 if (factura.getDescuento() != 0) {
                     sesion.setAttribute("serVip", "Aplicado el descuento usuario vip");
                 } else {
                     sesion.setAttribute("serVip", "No dispone de descuento usuario vip");
                 }
                 pagina = "VistaCarrito.jsp";
             } else {
                 HelperBusquedaInicial busqueda = new HelperBusquedaInicial();
                 ArrayList<Cd> cds = busqueda.busca();
                 request.setAttribute("mensaje", "Debe loguearse para poder comprar");
                 request.setAttribute("listaCds", cds);
                 pagina = "index.jsp";
             }
         }
 
 
         if (request.getParameter("busca") != null) {
 
             DAOcd = new DAOCd();
             ArrayList<Cd> cds;
 
             HelpOperacionCd cd = new HelpOperacionCd();
 
             request.setAttribute("listaCds", cd.buscarCd(request.getParameter("titulo"), request.getParameter("autor"), request.getParameter("anho"), request.getParameter("precio")));
 
 
             pagina = "index.jsp";
         }
 
         if (request.getParameter("inicio") != null) {
             HelperBusquedaInicial busqueda = new HelperBusquedaInicial();
             ArrayList<Cd> cds = busqueda.busca();
 
             request.setAttribute("listaCds", cds);
 
             pagina = "index.jsp";
         }
 
 
 
         if (request.getParameter("finalizaCompra") != null) {
 
             HelpCompra compra = new HelpCompra(carrito, (Comprador) usuario);
             compra.resgistarCompra();
             pagina = "index.jsp";
 
 
         }
 
         if (request.getParameter("borrar_elem") != null) {
 
             HelpBorrarCd borrarCd = new HelpBorrarCd(carrito);
 
             borrarCd.borrarCd(Integer.parseInt(request.getParameter("borrarCd")));
             
             HelpFactura Hfactura = new HelpFactura(carrito, (Comprador) usuario);
             Factura factura = Hfactura.importeFinal();
             sesion.setAttribute("factura", factura);
             if (factura.getDescuento() != 0) {
                 sesion.setAttribute("serVip", "Aplicado el descuento usuario vip");
             } else {
                 sesion.setAttribute("serVip", "No dispone de descuento usuario vip");
             }
             pagina = "VistaCarrito.jsp";
 
 
         }
 
         if (request.getParameter("volverTienda") != null) {
 
             pagina = "index.html";
 
         }
 
         if (request.getParameter("eliminaCarro") != null) {
 
             carrito = new Carro();
             sesion.setAttribute("carrito", carrito);
             pagina = "index.html";
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
