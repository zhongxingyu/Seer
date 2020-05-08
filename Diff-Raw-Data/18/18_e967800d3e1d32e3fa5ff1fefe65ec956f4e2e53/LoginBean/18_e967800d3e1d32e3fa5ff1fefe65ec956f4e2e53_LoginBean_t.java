 package com.spontecorp.ferreasesor.controller;
 
 import com.spontecorp.ferreasesor.entity.Usuario;
 import com.spontecorp.ferreasesor.security.Login;
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Valida y permite o niega el ingreso del usuario a la aplicaión
  *
  * @author jgcastillo
  */
 @ManagedBean(name = "loginBean")
 @SessionScoped
 public class LoginBean implements Serializable {
 
     private String usuario;
     private String password;
     private Usuario current;
     private Date fechaActual;
     private static final Logger logger = LoggerFactory.getLogger(LoginBean.class);
 
     /**
      * Creates a new instance of LogindBean
      */
     public LoginBean() {
     }
 
     public String getUsuario() {
         return usuario;
     }
 
     public void setUsuario(String usuario) {
         this.usuario = usuario;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Usuario getCurrent() {
         return current;
     }
 
     public String getFechaActual() {
         Calendar cal = new GregorianCalendar();
         cal.setTime(new Date());
 
         int mes = cal.get(Calendar.MONTH);
         int year = cal.get(Calendar.YEAR);
         int thisDay = cal.get(Calendar.DATE);
 
         cal.set(year, mes, thisDay);
         Date fecha = cal.getTime();
 
         return new SimpleDateFormat("dd/MM/yyyy").format(fecha);
     }
 
     public void setFechaActual(Date fechaActual) {
         this.fechaActual = fechaActual;
     }
 
     public String login() {
         char[] pswChar = password.toCharArray();
         FacesMessage msg = null;
         String result = "";
         current = Login.authenticate(usuario, pswChar);
         if (current != null) {
             FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("username", usuario);
             msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Bienvenido", usuario);
             if (current.getPerfilId().getPermiso() == 1 || current.getPerfilId().getPermiso() == 2) {
                result = "/main1?faces-redirect=true";
             } else {
                result = "/main?faces-redirect=true";
             }
         } else {
             FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("username", "");
             msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Ingreso", "Credenciales no válidas");
         }
         FacesContext.getCurrentInstance().addMessage(null, msg);
         return result;
     }
 
     public String logout() {
         FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(usuario);
         FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
         current = null;
        return "/index?faces-redirect=true";
     }
 
     public boolean isLoggedIn() {
         return (current != null);
     }
 
     public boolean isAdmin() {
         String perfil = current.getPerfilId().getNombre();
         if (perfil.equals("Administrador")) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean isGerente() {
         String perfil = current.getPerfilId().getNombre();
         if (perfil.equals("Gerente")) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean isAsesor() {
         String perfil = current.getPerfilId().getNombre();
         if (perfil.equals("Usuario")) {
             return true;
         } else {
             return false;
         }
     }
 }
