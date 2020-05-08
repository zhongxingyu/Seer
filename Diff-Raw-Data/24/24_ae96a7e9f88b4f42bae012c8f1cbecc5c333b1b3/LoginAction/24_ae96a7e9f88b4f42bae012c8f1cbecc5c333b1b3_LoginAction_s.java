 /*
   SiluSoft
     Copyright (C) 2012  Hugo Langa Roy
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package es.pfc.actions;
 
 import es.pfc.forms.LoginForm;
 import es.pfc.model.Permiso;
 import es.pfc.model.Usuario;
 import es.pfc.negocio.IdentificarBO;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  *
  * @author ko
  */
 public class LoginAction extends org.apache.struts.action.Action {
 
     /* forward name="success" path="" */
     private static final String SUCCESS = "correcto";
     private static final String FAILURE = "incorrecto";
 
     /**
      * This is the action called from the Struts framework.
      * @param mapping The ActionMapping used to select this instance.
      * @param form The optional ActionForm bean for this request.
      * @param request The HTTP Request we are processing.
      * @param response The HTTP Response we are processing.
      * @throws java.lang.Exception
      * @return
      */
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
        //Recogo los datos del formulario
        LoginForm logon_form = (LoginForm) form;
        Usuario miusuario = new Usuario();
        List listaPermisosUsuario = new ArrayList();
        Permiso permiso;
        
        
        String Nombre=logon_form.getNombre();
        String Password=logon_form.getPassword();
       System.out.println(Nombre);
       System.out.println(Password);
        //UsuarioDAOImpl usuariodao = new UsuarioDAOImpl ();
         
        
        //intento crear un objeto de tipo Usuario. 
        //Si me lo permite es que los datos son correctos y creo la variable sesión
        try{
            miusuario=IdentificarBO.esUsuarioAutorizado(Nombre,Password);
           //creo la lista de permisos del usuario (quitar la llamada a consultarPermisos de IdentificarBO.esUsuarioAutorizado)
           listaPermisosUsuario = IdentificarBO.damePermisosdeUsuario(miusuario.getIdUsuario());
           if( miusuario!=null){ 
                int idCentro = IdentificarBO.dameCentro(miusuario);
                miusuario.setIdCentro(idCentro);
               HttpSession session = request.getSession(true);
                int rol=miusuario.getRol();
               
                session.setAttribute("usuario", miusuario);
                Iterator iter = listaPermisosUsuario.iterator();
                while(iter.hasNext()){
                     permiso = new Permiso();
                     permiso = (Permiso)iter.next();                    
                    session.setAttribute(permiso.getPermiso(), permiso);
                     System.out.println("Atributos en sesión: "+permiso.getPermiso()+", "+permiso.getActivado());
                }
                if(rol==1){
                    return mapping.findForward("paneladmin");
                }
                //request.getSession().setAttribute("usuario", logon_form.getUsuario());                  
                return mapping.findForward(SUCCESS);
            }else{
               return mapping.findForward(FAILURE);
           }        
        }catch(Exception e){
            throw new RuntimeException("Excepción en LoginAction:"+e);
        }
        
        
     }   
 }
