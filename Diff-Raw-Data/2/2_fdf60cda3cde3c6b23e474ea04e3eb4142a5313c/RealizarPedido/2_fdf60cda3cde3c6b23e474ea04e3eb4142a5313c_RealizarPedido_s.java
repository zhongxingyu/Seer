 package upao.paw.compumundo.control.servlet;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import upao.paw.compumundo.BD;
 import upao.paw.compumundo.Carrito;
 import upao.paw.compumundo.modelo.Comprador;
 import upao.paw.compumundo.modelo.LineaPedido;
 import upao.paw.compumundo.modelo.Pedido;
 
 /**
  *
  * @author jahd
  */
 @WebServlet(name = "RealizarPedido", urlPatterns = {"/servlet/RealizarPedido"})
 public class RealizarPedido extends HttpServlet {
 
     private static final String REDIRECCION = "/cm/carrito.jsp";
 
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
         BD bd;
         try {
             bd = BD.getInstance();
         } catch (SQLException ex) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo conectar a la base de datos&error=" + ex.getMessage());
             return;
         }
 
         Pedido pedido = new Pedido();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
         pedido.setFecha(sdf.format(new Date()));
         try {
             bd.getPedidoDao().create(pedido);
         } catch (SQLException ex) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo crear ejemplo en tabla Pedido&error=" + ex.getMessage());
             return;
         }
         Carrito carrito = new Carrito();
         carrito.setSesion(request.getSession());
         List<Integer> items = carrito.getItems();
         LineaPedido lpTemp;
         for (Integer item : items) {
             lpTemp = new LineaPedido();
             lpTemp.setId(item);
             try {
                 bd.getLineaPedidoDao().refresh(lpTemp);
             } catch (SQLException ex) {
                 response.sendRedirect(REDIRECCION
                         + "?mensaje=No se pudo refrescar LineaPedido&error=" + ex.getMessage());
                 return;
             }
             lpTemp.setPedido(pedido);
             try {
                 bd.getLineaPedidoDao().update(lpTemp);
             } catch (SQLException ex) {
                 response.sendRedirect(REDIRECCION
                         + "?mensaje=No se pudo guardar LineaPedido&error=" + ex.getMessage());
                 return;
             }
         }
 
         Comprador comprador = new Comprador();
         comprador.setNombre(request.getParameter("nombre"));
         comprador.setApellido(request.getParameter("apellido"));
         comprador.setDireccion(request.getParameter("direccion"));
         comprador.setCiudad(request.getParameter("ciudad"));
         comprador.setRegion(request.getParameter("region"));
         comprador.setTipoTarjeta(request.getParameter("tipoTarjeta"));
         comprador.setNumeroTarjeta(request.getParameter("numeroTarjeta"));
         try {
             bd.getCompradorDao().create(comprador);
         } catch (SQLException ex) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo crear Comprador&error=" + ex.getMessage());
             return;
         }
 
         pedido.setComprador(comprador);
         pedido.setEstado(Pedido.ESTADO_ACTIVO);
         try {
             bd.getPedidoDao().update(pedido);
         } catch (SQLException ex) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo crear ejemplo en tabla Pedido&error=" + ex.getMessage());
             return;
         }
 
         carrito.setItems(new ArrayList<Integer>());
 
         response.sendRedirect(REDIRECCION
                 + "?mensaje=Pedido realizado con exito");
 
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
