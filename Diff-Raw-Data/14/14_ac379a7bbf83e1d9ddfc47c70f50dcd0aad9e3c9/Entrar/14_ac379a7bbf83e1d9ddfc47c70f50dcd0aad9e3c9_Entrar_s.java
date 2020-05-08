 package upao.paw.compumundo.control.servlet;
 
 import com.j256.ormlite.dao.Dao;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import upao.paw.compumundo.BD;
 import upao.paw.compumundo.Login;
 import upao.paw.compumundo.modelo.Usuario;
 
 /**
  *
  * @author jahd
  */
 @WebServlet(name = "Entrar", urlPatterns = {"/servlet/Entrar"})
 public class Entrar extends HttpServlet {
 
     private static final String REDIRECCION = "/cm/index.jsp";
 
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
         String user = request.getParameter(Login.LOGIN_USUARIO);
         String pass = request.getParameter(Login.LOGIN_PASS);
         if (user == null || user.isEmpty()) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo iniciar sesión&error=Falta el usuario");
             return;
         }
         if (pass == null || pass.isEmpty()) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo iniciar sesión&error=Falta la contraseña");
             return;
         }
         Dao<Usuario, Integer> usuarioDao;
         List<Usuario> lista;
         try {
             usuarioDao = BD.getInstance().getUsuarioDao();
             lista = usuarioDao.queryForEq("nombre", user.toLowerCase());
         } catch (SQLException ex) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo iniciar sesión&error=No se pudo conectar a la base de datos");
             return;
         }
         if (lista.isEmpty()) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo iniciar sesión&error=No existe el usuario ingresado");
             return;
         }
         Usuario usuario = lista.get(0);
         if (!usuario.getPass().equals(pass)) {
             response.sendRedirect(REDIRECCION
                     + "?mensaje=No se pudo iniciar sesión&error=La contraseña ingresada es incorrecta");
             return;
         }
         request.getSession().setAttribute(Login.LOGIN_USUARIO, user);

        String redireccion = usuario.isEsAdmin() ? "/cm/admin/" : REDIRECCION;
         request.getSession().setAttribute(Login.LOGIN_ESADMIN, usuario.isEsAdmin());
        request.getSession().setAttribute(Login.LOGIN_ID,usuario.getId());
 
         response.sendRedirect(redireccion);
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
