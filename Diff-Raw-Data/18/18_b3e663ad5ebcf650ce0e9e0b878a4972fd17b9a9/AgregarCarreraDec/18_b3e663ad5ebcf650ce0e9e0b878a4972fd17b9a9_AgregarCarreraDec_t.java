 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Actions.Carrera;
 import Clases.Decanato;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  *
  * @author admin
  */
 public class AgregarCarreraDec extends org.apache.struts.action.Action {
 
     /* forward name="success" path="" */
     private static final String SUCCESS = "success";
     private static final String FAILURE = "failure";
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
         HttpSession session = request.getSession(true);
         Decanato d = (Decanato) form;
        System.out.println(d.getNombre().equals(""));
        // No se trajo el decanato desde el form.
        if (d.getNombre().equals("")) {
            d = (Decanato) session.getAttribute("decanato_actual");
        }
         System.out.println("Decanato: "+d.getNombre());
         System.out.println("Decanato: "+d.getCodigo());
         session.setAttribute("decanato_actual",d);
         return mapping.findForward(SUCCESS);
     }
 }
