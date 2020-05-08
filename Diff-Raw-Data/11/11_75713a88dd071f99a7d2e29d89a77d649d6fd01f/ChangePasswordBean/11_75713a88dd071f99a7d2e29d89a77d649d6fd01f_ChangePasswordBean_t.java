 /*
  */
 package org.fit.pis.library.back;
 
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import org.fit.pis.library.data.User;
 import org.fit.pis.library.data.UserManager;
 
 /**
  *
  * @author Lukáš Černý <cerny.l@gmail.com>
  */
 @ManagedBean
 @RequestScoped
 public class ChangePasswordBean {
 
 	@EJB
 	private UserManager userMgr;
 	private User user;
 	private String oldPassword;
 	private String newPassword1;
 	private String newPassword2;
 
 	@ManagedProperty(value = "#{authenticationBean}")
 	private AuthenticationBean authBean;
 
 	public void setAuthBean(AuthenticationBean authBean) {
 		this.authBean = authBean;
 	}
 
 	public void setNewPassword1(String newPassword1) {
 		this.newPassword1 = newPassword1;
 	}
 
 	public void setNewPassword2(String newPassword2) {
 		this.newPassword2 = newPassword2;
 	}
 
 	public void setOldPassword(String oldPassword) {
 		this.oldPassword = oldPassword;
 	}
 
 	public String getNewPassword1() {
 		return newPassword1;
 	}
 
 	public String getNewPassword2() {
 		return newPassword2;
 	}
 
 	public String getOldPassword() {
 		return oldPassword;
 	}
 
 	/** Creates a new instance of ChangePasswordBean */
 	public ChangePasswordBean() {
 	}
 
 	/**
 	 * Change password action
 	 * @return 
 	 */
 	public String actionChange() {
 		user = userMgr.find(authBean.getIduser());
 
 		if (user.getPassword().compareTo(oldPassword) == 0) {
 			if (newPassword1.compareTo(newPassword2) == 0) {
 				try {
 					user.setPassword(newPassword1);
 					userMgr.Save(user);
 				} catch (javax.ejb.EJBException e) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Heslo se nepodařilo změnit, zkuste to prosím později."));
 					return "";
 				}
 			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Nová hesla nejsou stejná."));
 				return "";
 			}
 		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Špatně zadané staré heslo."));
 			return "";
 		}
 
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Heslo bylo úspěšně změněno."));
 		
 		return "";
 	}
 }
