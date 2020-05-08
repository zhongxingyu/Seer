 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Actions.Carrera;
 
 import Clases.Carrera;
 import Clases.Decanato;
 import Clases.Usuario;
 import DBMS.DBMS;
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 /**
  *
  * @author smaf
  */
 public class EliminaCarreraA extends org.apache.struts.action.Action {
     
     private static final String FAILURE = "failure";
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
 
         Carrera u = (Carrera) form;
         HttpSession session = request.getSession(true);
         Usuario usuario = (Usuario) session.getAttribute("usuario");
         String tipousuario = usuario.getTipousuario();
         ActionErrors error = new ActionErrors();
         //obtengo una lista de carreras registradas
 
         //valido los campos de formulario
         /*error = u.validate(mapping, request);
         boolean huboError = false;
         
         if (error.size() != 0) {
             huboError = true;
         }
 
         //si los campos no son validos
         if (huboError) {
             saveErrors(request, error);
             request.setAttribute("noEliminado",FAILURE);
             System.out.println("ERROR!!");
             return mapping.findForward(FAILURE);
             //si los campos son validos
         } else {*/
             boolean eliminado = DBMS.getInstance().eliminarCarrera(u);
             //retorno a pagina de exito
             if (!eliminado) {
                 request.setAttribute("noEliminado",FAILURE);
                return mapping.findForward(SUCCESS);
             } else {
                 request.setAttribute("eliminado",SUCCESS);
                 ArrayList<Carrera> [] carreras = null;
                 
                 if (tipousuario.equals("administrador")) {
                     Decanato d = (Decanato) session.getAttribute("decanato_actual");
                     carreras = DBMS.getInstance().listarCarrerasDecanato(d.getCodigo());
                     d = DBMS.getInstance().obtenerNombreDecanato(d);
                 } else if (tipousuario.equals("decanato")){
                     carreras = DBMS.getInstance().listarCarrerasDecanato(usuario.getUsbid());
                 }
                 //si existen carreras registradas
 
                 //retorno a pagina de exito
                  session.setAttribute("carreras_visibles", carreras[0]);
                  session.setAttribute("carreras_ocultas", carreras[1]);
                  return mapping.findForward(tipousuario);
             }
         // }
     }
 }
