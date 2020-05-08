 package backingbeans;
 
 import java.io.Serializable;
 import java.util.ResourceBundle;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.SelectItem;
 import javax.faces.bean.ManagedProperty;
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 import javax.faces.event.ActionEvent;
 import javax.annotation.PostConstruct;
 import javax.naming.InitialContext;
 import java.util.Map;
 import java.util.HashMap;
 import javax.faces.event.ComponentSystemEvent;
 import java.util.Collection;
 import java.util.Date;
 import javax.servlet.http.HttpSession;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 
 
 @ManagedBean
 @RequestScoped
 public class LoginController implements Serializable {
     private final Logger l = Logger.getLogger(this.getClass().getName());
     private static final String CLASSNAME=LoginController.class.getName();
 
 
     private String username;
     
     public String getUsername() {
         return username;
     }
     
     public void setUsername(String username) {
         this.username = username;
     }
 
     private String password;
     
     public String getPassword() {
         return password;
     }
     
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String login() {
 	String navOutcome = null;
 	UsernamePasswordToken token = new UsernamePasswordToken(getUsername(), getPassword());
 	try {
                System.out.println("1");
 		Subject subject = SecurityUtils.getSubject();
                System.out.println("2");
 		subject.login(token);
                System.out.println("3");
 		token.clear();
                        if (subject.isAuthenticated()) {
                            try {
                                if ( subject.hasRole("admin") ) System.out.println(subject.getPrincipal() + " has admin role");
                                else                            System.out.println(subject.getPrincipal() +  " doesn't have admin role");
                                if (subject.hasRole("user"))    System.out.println(subject.getPrincipal() + " has user role");
                                else                            System.out.println(subject.getPrincipal() + " doesn't have user role");
                                if (subject.hasRole("staff"))   System.out.println(subject.getPrincipal() + " has staff role");
                                else                            System.out.println(subject.getPrincipal() + " doesn't have staff role");
                                if (subject.hasRole("cacher"))   System.out.println(subject.getPrincipal() + " has cacher role");
                                else                            System.out.println(subject.getPrincipal() + " doesn't have cacher role");
                                
                                if       (subject.isPermitted("secure")) System.out.println(subject.getPrincipal()+" has the 'secure' permission");
                                if       (subject.isAuthenticated() && subject.isPermitted("secure"))  navOutcome = "goToStaffArea";
                                else if  (subject.isAuthenticated() && subject.hasRole    ("user"  ))  navOutcome = "goToUserArea";
                                else if  (subject.isAuthenticated() && subject.hasRole    ("admin" ))  navOutcome = "goToAdminArea";
                                else if  (subject.isAuthenticated() && subject.hasRole    ("cacher" )) navOutcome = "goToEhCacheTest";
                                else                                                                   navOutcome = "unauthorized";
                            } catch (Exception e) { // due to the 'if' above we shouldn't see any exceptions in the above section
                                e.printStackTrace();
                                throw new RuntimeException("panic: "+e);
                            }
                        }
 	} catch (UnknownAccountException ex) {
 		ex.printStackTrace();
 		// request.setAttribute("error", ex.getMessage() );
 		
 	} catch (IncorrectCredentialsException ex) {
 		ex.printStackTrace();
 		// request.setAttribute("error", ex.getMessage());
 	}
 	
 	catch (Exception ex) {
 		ex.printStackTrace();
 		// request.setAttribute("error", "Login NOT SUCCESSFUL - cause not known!");
 	}
         System.out.println("returning: "+navOutcome);
         return navOutcome;
     }
 
     public String logout() {
 	Subject subject = SecurityUtils.getSubject();
         if (subject != null) {
             //see:  http://jsecurity.org/api/index.html?org/jsecurity/web/DefaultWebSecurityManager.html
             subject.logout();
         }
         FacesContext facesContext = FacesContext.getCurrentInstance();
         HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
         if( session != null ) {
             session.invalidate();
         }        
         return "logOut";
     }
 }
