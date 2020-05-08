 package upao.paw.control;
 
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import upao.paw.compumundo.BD;
 import upao.paw.compumundo.modelo.*;
 
 /**
  *
  * @author jahd
  */
@WebServlet(name = "CrearBD", urlPatterns = {"/servlet/EliminarBD"})
 public class EliminarBD extends HttpServlet {
 
     private static final String REDIRECCION = "/cm/admin/baseDeDatos.jsp";
 
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
         ConnectionSource conexion;
         try {
             conexion = BD.getInstance().getConexion();
         } catch (SQLException ex) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo conectar a la base de datos&error=" + ex.getMessage());
             return;
         }
         try {
             TableUtils.dropTable(conexion, Comprador.class, true);
             TableUtils.dropTable(conexion, Configuracion.class, true);
             TableUtils.dropTable(conexion, ConfiguracionInicial.class, true);
             TableUtils.dropTable(conexion, LineaPedido.class, true);
             TableUtils.dropTable(conexion, Pedido.class, true);
             TableUtils.dropTable(conexion, Personalizacion.class, true);
             TableUtils.dropTable(conexion, Producto.class, true);
             TableUtils.dropTable(conexion, TipoPersonalizacion.class, true);
         } catch (SQLException ex) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudieron eliminar las tablas&error=" + ex.getMessage());
             return;
         }
         response.sendRedirect(REDIRECCION
                 + "?mensaje=Tablas eliminadas con exito");
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
