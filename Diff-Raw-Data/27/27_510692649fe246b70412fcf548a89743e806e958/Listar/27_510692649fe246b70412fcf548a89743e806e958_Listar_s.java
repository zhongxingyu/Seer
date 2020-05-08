 
 package Usuarios;
 
 import Clases.Usuario;
 import DBMS.*;
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  *
  * @author sibs
  */
 
 /*Accion que llama a la clase DBMS.java para consultar todos los usuarios
  que estan en la base de datos para luego mandarlos a la vista 
  correspondiente. */
 public class Listar extends org.apache.struts.action.Action {
 
     /* forward name="success" path="" */
     private static final String SUCCESS = "success";
 
     /**
      * This is the action called from the Struts framework.
      *
      * @param mapping The ActionMapping used to select this instance.
      * @param form The optional ActionForm bean for this request.
      * @param request The HTTP Request we are processing.
      * @param response The HTTP Response we are processing.
      * @throws java.lang.Exception
      * @return
      */
     @Override
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
        Usuario u = (Usuario) form;
         
        ArrayList<Usuario> usuariosHab = DBMS.getInstance().consultarUsuariosHabilitados(u);
         request.setAttribute("usuariosHab", usuariosHab);
 
        ArrayList<Usuario> usuariosNoHab = DBMS.getInstance().consultarUsuariosNoHabilitados(u);
         request.setAttribute("usuariosNoHab", usuariosNoHab);
 
        u = DBMS.getInstance().atributosUsuario(u);
        request.setAttribute("autenticado", u);
 
         return mapping.findForward(SUCCESS);
     }
 }
