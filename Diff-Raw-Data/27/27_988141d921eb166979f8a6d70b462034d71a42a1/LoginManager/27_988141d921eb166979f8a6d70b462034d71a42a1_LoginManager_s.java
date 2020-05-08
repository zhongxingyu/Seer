 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package projekt.teama.login;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import java.util.Set;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import projekt.fhv.teama.classes.personen.IGast;
 import projekt.fhv.teama.hibernate.dao.personen.GastDao;
 import projekt.teama.reservierung.ReservationManager;
 
 /**
  *
  * @author GIGI
  */
 @ManagedBean
 @SessionScoped
 public class LoginManager {
 
     //<editor-fold defaultstate="collapsed" desc="properties">
     private String useremail;
     private String password;
     private IGast guest;
     private HttpSession session;
    
     @ManagedProperty(value = "#{reservationManager}")
     private ReservationManager reservationManager;
     //</editor-fold>
 
     public LoginManager() {
         FacesContext context = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
         this.session = ((HttpServletRequest) request).getSession();
     }
 
     public String checkLogin() {
 
         boolean validLoginData = getGuest();
 
         if (validLoginData) {
             this.session.setAttribute("loggedIn", true);
             this.reservationManager.setGuest(this.guest);
         } else {
             this.session.setAttribute("loggedIn", false);
         }
 
         return "reservation";
     }
 
     public String logOut() {
         this.useremail = "";
         this.password = "";
        this.session.removeAttribute("loggedIn");
        this.session.removeAttribute("guest");
 
         // Nullpointer beim logout
        this.reservationManager.setGuest(null);
        this.reservationManager.setAddress(null);
        this.reservationManager.setCountry(null);
         return "reservation";
     }
 
     //<editor-fold defaultstate="collapsed" desc="getter und setter">
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getUseremail() {
         return useremail;
     }
 
     public void setUseremail(String useremail) {
         this.useremail = useremail;
     }
 
     public ReservationManager getReservationManager() {
         return reservationManager;
     }
 
     public void setReservationManager(ReservationManager reservationManager) {
         this.reservationManager = reservationManager;
     }
     //</editor-fold>
 
     private boolean getGuest() {
 
         try {
             Set<IGast> guests = GastDao.getInstance().getAll();
             for (IGast g : guests) {
                 if (g.getEmail().equals(this.useremail) && g.getPasswort().equals(this.password)) {
                     this.guest = g;
                     return true;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return false;
     }
 }
