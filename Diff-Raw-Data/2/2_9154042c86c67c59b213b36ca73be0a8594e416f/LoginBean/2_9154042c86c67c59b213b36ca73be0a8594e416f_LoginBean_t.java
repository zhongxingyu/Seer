 package com.maynar.managedbeans;
 
 
 import java.io.Serializable;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpSession;
 import org.primefaces.context.RequestContext;
 
 
 import com.maynar.model.Usuario;
 import com.maynar.service.IGestion_Usuarios;
 import com.maynar.spring.Acceso_ApplicationContext;
 import com.maynar.util.AjaxMessages;
 
 public class LoginBean implements Serializable {
 	
   private static final long serialVersionUID = -2152389656664659476L;
   private String nombre;
   private String clave;
   private boolean logeado = false;
   private static String SHOW_MENU = "/xhtml/menu.xhtml";
  private static String SHOW_LOGIN = "/index.jsp";
   private IGestion_Usuarios gestion_usuario;
   
   public boolean estaLogeado() {
     return logeado;
   }
 
   public String getNombre() {
     return nombre;
   }
 
   public void setNombre(String nombre) {
     this.nombre = nombre;
   }
 
   public String getClave() {
     return clave;
   }
 
   public void setClave(String clave) {
     this.clave = clave;
   }
 
   public String login() {
 	
     RequestContext context = RequestContext.getCurrentInstance();
     FacesMessage msg = null;
 
     gestion_usuario = (IGestion_Usuarios) Acceso_ApplicationContext.getBean("ges_usuarios");
 	Usuario usuario = gestion_usuario.consultarporNombre(getNombre());
     if (nombre != null && nombre.equals(usuario.getNombre()) && clave != null
         && clave.equals(usuario.getClave())) {
       logeado = true;
       msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Bienvenid@", nombre);
     } else {
       logeado = false;
       msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Login Error",
                              "Credenciales no válidas");
       AjaxMessages.addMessage("Error en el Login");
       return SHOW_LOGIN;
     }
 
     FacesContext.getCurrentInstance().addMessage(null, msg);
     context.addCallbackParam("estaLogeado", logeado);
 //    if (logeado)
 //      context.addCallbackParam("view", "gauge.xhtml");
     return SHOW_MENU;
   }
 
   public String logout() {
     HttpSession session = (HttpSession) FacesContext.getCurrentInstance() 
                                         .getExternalContext().getSession(false);
     session.invalidate();
     logeado = false;
     return SHOW_LOGIN;
   }
 }
