 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Actions;
 
 
 import Clases.Gestion;
 import DBMS.DBMS;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
 
 
 /**
  *
  * @author jaescalante02
  */
 public class gestionAgregada extends org.apache.struts.action.Action {
 
 
     /* forward name="success" path="" */
     private static final String SUCCESS = "success";
     private static final String FAIL = "failure";
     
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
        
         Gestion gest = (Gestion) form;
   //     if (!(gest.getUsuario().equalsIgnoreCase("sofia"))) return mapping.findForward(FAIL);
   //      if (!(gest.getp1().equalsIgnoreCase("w"))) return mapping.findForward(FAIL);

         if ((gest.getnombreusuario()==null) || (gest.getp1().toString().equals(""))
                 || (gest.getp2().toString().equals("")) || (gest.getp3().toString().equals(""))
                || (gest.getp4().toString().equals("")) || (gest.getp5().toString().equals(""))
                || (gest.getp6().toString().equals("")) || (gest.getp7().toString().equals(""))
                || (gest.getp8().toString().equals("")) || (gest.getp9().toString().equals(""))
                || (gest.getp10().toString().equals("")) || (gest.getp11().toString().equals(""))
                || (gest.getp12().toString().equals("")) || (gest.getp13().toString().equals(""))
                || (gest.getp14().toString().equals("")) || (gest.getp15().toString().equals(""))
                || (gest.getp16().toString().equals(""))) return mapping.findForward(FAIL);
         
         if (DBMS.getInstance().agregarGestion(gest)) return mapping.findForward(SUCCESS);
         
         
         return null;
     }
 }
