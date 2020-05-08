 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Actions.Carrera;
 
 import Clases.Carrera;
 import Clases.Usuario;
 import Clases.Decanato;
 import DBMS.DBMS;
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 
 /**
  *
  * @author admin
  */
 public class RegistrarCarrera extends org.apache.struts.action.Action {
 
     /* forward name="success" path="" */
     private static final String SUCCESS = "success";
     private static final String DECANATO = "decanato";
     private static final String ADMINISTRADOR = "administrador";
     private static final String FAILURE = "failure";
     private static final String YAREGISTRADA = "yaregistrada";
 
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
 
         //valido los campos de formulario
         error = u.validate(mapping, request);
         boolean huboError = false;
 
 
         if (error.size() != 0) {
             huboError = true;
         }
 
         //si los campos no son validos
         if (huboError) {
             saveErrors(request, error);
             return mapping.findForward(FAILURE);
             //si los campos son validos
         } else {
             ArrayList<Carrera> [] carreras = null;
             
             if (tipousuario.equals("administrador")) {
                 Decanato d = (Decanato) session.getAttribute("decanato_actual");
                System.out.println("decanato_actual: "+d.getNombre());
                 boolean registro = DBMS.getInstance().registrarCarreraDec(u,d.getCodigo());
 
                 if (registro) {
 
                     carreras = DBMS.getInstance().listarCarrerasDecanato(d.getCodigo());
                     session.setAttribute("carreras_visibles", carreras[0]);
                     session.setAttribute("carreras_ocultas", carreras[1]);
                     request.setAttribute("registro",SUCCESS);
                     request.setAttribute("decanato",d);
                     return mapping.findForward(ADMINISTRADOR);
                 } else {
                     error.add("registro", new ActionMessage("error.codigoexistente"));
                     saveErrors(request, error);
                     return mapping.findForward(YAREGISTRADA);
                 }
             } else if (tipousuario.equals("decanato")) {
                 boolean registro = DBMS.getInstance().registrarCarreraDec(u,usuario.getUsbid());
 
                 if (registro) {
 
                     carreras = DBMS.getInstance().listarCarrerasDecanato(usuario.getUsbid());
                     session.setAttribute("carreras_visibles", carreras[0]);
                     session.setAttribute("carreras_ocultas", carreras[1]);
                     request.setAttribute("registro",SUCCESS);
                     return mapping.findForward(DECANATO);
                 } else {
                     error.add("registro", new ActionMessage("error.codigoexistente"));
                     saveErrors(request, error);
                     return mapping.findForward(YAREGISTRADA);
                 }
             }
         }
         return mapping.findForward(FAILURE);
     }
 }
