 /**
  * @author
  * wolfgang
  * Teves
  * @version
  * 0.9
  */
 package ac.at.fhkufstein.login;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.PhaseEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PhaseListener;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 /**
  * this
  * bean
  * is
  * called
  * by
  * the
  * phaselistener
  * it
  * checks
  * wether
  * the
  * session
  * is
  * valid
  * or
  * not.
  * in
  * case
  * of
  * an
  * invalid
  * session,
  * the
  * user
  * gets
  * redirected
  * to
  * the
  * login
  * page
  */
 @ManagedBean(name = "loginData")
 public class login implements PhaseListener {
 
 	Integer uid;
 	FacesContext facesContext;
 	HttpSession session;
 
 	public void afterPhase(PhaseEvent event) {
 
 		//get current uri
 		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
 		facesContext = event.getFacesContext();
 		session = (HttpSession) facesContext.getExternalContext().getSession(false);
 		String currentPage = ((HttpServletRequest) ec.getRequest()).getRequestURI();
 
 		//check if login page
 		boolean isLoginPage = (currentPage.lastIndexOf("/login.xhtml") > -1);
 
 		if (!isLoginPage) {
 			//System.out.println("no Login Page");	
 			if (session == null) {
				System.out.println("Login: Session is null");
 				try {
 					//No login page and session is null
 					ec.redirect(ec.getRequestContextPath() + "/faces/login.xhtml");
 
 				} catch (IOException ex) {
 					Logger.getLogger(login.class.getName()).log(Level.SEVERE, null, ex);
 				}
 
 			} else {
 				//System.out.println("Session not null");
 
 				try {
					System.out.println("Login: Try to access uid");
 					if (session.getAttribute("uid") == null) {
 						//no login page but session var is null, redirect to login page
 						ec.redirect(ec.getRequestContextPath() + "/faces/login.xhtml");
 					}
 
 
 				} catch (Exception x) {
 					try {
 						//no login page but session var is not accessable, redirect to login page
 						ec.redirect(ec.getRequestContextPath() + "/faces/login.xhtml");
 
 					} catch (IOException ex) {
 						Logger.getLogger(login.class.getName()).log(Level.SEVERE, null, ex);
 					}
 				}
 			}
 		}
 
 
 
 	}
 
 	public void beforePhase(PhaseEvent event) {
 	}
 
 	public PhaseId getPhaseId() {
 		return PhaseId.RESTORE_VIEW;
 	}
 }
