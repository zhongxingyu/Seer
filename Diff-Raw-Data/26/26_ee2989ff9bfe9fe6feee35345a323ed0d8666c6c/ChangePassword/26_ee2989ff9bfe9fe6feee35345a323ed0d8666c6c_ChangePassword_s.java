 /* Ara - capture species and specimen data
  * 
  * Copyright (C) 2009  INBio ( Instituto Naciona de Biodiversidad )
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * changePassword.java
  *
  * Created on October 3, 2007, 5:53 AM
  * Copyright roaguilar
  */
 package org.inbio.ara.web.user;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.Alert;
 import com.sun.webui.jsf.component.PasswordField;
 import com.sun.webui.jsf.component.TextField;
 import javax.faces.FacesException;
 import org.inbio.ara.persistence.security.User;
 import org.inbio.ara.web.SessionManager;
 import org.inbio.ara.web.util.MessageBean;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  */
 public class ChangePassword extends AbstractPageBean {
 
 	// <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 	private int __placeholder;
 
 	/**
 	 * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
 	 * This method is automatically generated, so any user-specified code inserted
 	 * here is subject to being replaced.</p>
 	 */
 	private void _init() throws Exception {
 	}
 	private Alert userAlert1 = new Alert();
 	private TextField fullNameTextField = new TextField();
 	private TextField userNameTextField = new TextField();
 	private PasswordField currentPasswordTextField = new PasswordField();
 	private PasswordField newPasswordTextField = new PasswordField();
 	private PasswordField confirmNewPasswordTextField = new PasswordField();
 
 	// </editor-fold>
 	/**
 	 * <p>Construct a new Page bean instance.</p>
 	 */
 	public ChangePassword() {
 	}
 
 	/**
 	 * <p>Callback method that is called whenever a page is navigated to,
 	 * either directly via a URL, or indirectly via page navigation.
 	 * Customize this method to acquire resources that will be needed
 	 * for event handlers and lifecycle methods, whether or not this
 	 * page is performing post back processing.</p>
 	 *
 	 * <p>Note that, if the current request is a postback, the property
 	 * values of the components do <strong>not</strong> represent any
 	 * values submitted with this request.  Instead, they represent the
 	 * property values that were saved for this view when it was rendered.</p>
 	 */
 	public void init() {
 		// Perform initializations inherited from our superclass
 		super.init();
 		// Perform application initialization that must complete
 		// *before* managed components are initialized
 		// TODO - add your own initialiation code here
 
 		// <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
 		// Initialize automatically managed components
 		// *Note* - this logic should NOT be modified
 		try {
 			_init();
 		} catch (Exception e) {
 			log("Module changePassword Initialization Failure", e);
 			throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
 		}
 
 	// </editor-fold>
 	// Perform application initialization that must complete
 	// *after* managed components are initialized
 	// TODO - add your own initialization code here
 	}
 
 	/**
 	 * <p>Callback method that is called after the component tree has been
 	 * restored, but before any event processing takes place.  This method
 	 * will <strong>only</strong> be called on a postback request that
 	 * is processing a form submit.  Customize this method to allocate
 	 * resources that will be required in your event handlers.</p>
 	 */
 	public void preprocess() {
 	}
 
 	/**
 	 * <p>Callback method that is called just before rendering takes place.
 	 * This method will <strong>only</strong> be called for the page that
 	 * will actually be rendered (and not, for example, on a page that
 	 * handled a postback and then navigated to a different page).  Customize
 	 * this method to allocate resources that will be required for rendering
 	 * this page.</p>
 	 */
 	public void prerender() {
 
 		User user = this.getSessionManager().getUser();
 
 		this.getUserSessionBean().setUserId(user.getId());
 
 		this.fullNameTextField.setText(user.getFullName());
 		this.userNameTextField.setText(user.getUserName());
 	}
 
 	/**
 	 * <p>Callback method that is called after rendering is completed for
 	 * this request, if <code>init()</code> was called (regardless of whether
 	 * or not this was the page that was actually rendered).  Customize this
 	 * method to release resources acquired in the <code>init()</code>,
 	 * <code>preprocess()</code>, or <code>prerender()</code> methods (or
 	 * acquired during execution of an event handler).</p>
 	 */
 	public void destroy() {
 	}
 
 	public String saveAction() {
		String newPassword = (String) this.newPasswordTextField.getText();
 		String confirmNewPassword = (String) this.confirmNewPasswordTextField.getText();
		String currentPassword = (String) currentPasswordTextField.getText();

 		UserSessionBean usb = this.getUserSessionBean();

 		if (!confirmNewPassword.equals(newPassword)) {
 			this.userAlert1.setType(MessageBean.getMessageFromBundle("warning"));
 			this.userAlert1.setSummary(MessageBean.getMessageFromBundle("new_password_dont_match"));
 			return null;
		} else {

 			if (usb.verifyPassword(currentPassword)) {
 				usb.updatePassword(newPassword);
 				return "changePassword";
 			} else {
 				this.userAlert1.setType(MessageBean.getMessageFromBundle("error"));
 				this.userAlert1.setSummary(MessageBean.getMessageFromBundle("change_password_failed"));
 				this.userAlert1.setDetail("");
 				return null;
 			}
 		}
 	}
 
 // Getters y Setters de Variables ----------------------------------------------	
 	public PasswordField getCurrentPasswordTextField() {
 		return currentPasswordTextField;
 	}
 
 	public void setCurrentPasswordTextField(PasswordField currentPasswordTextField) {
 		this.currentPasswordTextField = currentPasswordTextField;
 	}
 
 	public TextField getFullNameTextField() {
 		return fullNameTextField;
 	}
 
 	public void setFullNameTextField(TextField fullNameTextField) {
 		this.fullNameTextField = fullNameTextField;
 	}
 
 	public PasswordField getNewPasswordTextField() {
 		return newPasswordTextField;
 	}
 
 	public void setNewPasswordTextField(PasswordField newPasswordTextField) {
 		this.newPasswordTextField = newPasswordTextField;
 	}
 
 	public Alert getUserAlert1() {
 		return userAlert1;
 	}
 
 	public void setUserAlert1(Alert userAlert1) {
 		this.userAlert1 = userAlert1;
 	}
 
 	public TextField getUserNameTextField() {
 		return userNameTextField;
 	}
 
 	public void setUserNameTextField(TextField userNameTextField) {
 		this.userNameTextField = userNameTextField;
 	}
 
 	public PasswordField getConfirmNewPasswordTextField() {
 		return confirmNewPasswordTextField;
 	}
 
 	public void setConfirmNewPasswordTextField(PasswordField confirmNewPasswordTextField) {
 		this.confirmNewPasswordTextField = confirmNewPasswordTextField;
 	}
 
 // Getter de Beans -------------------------------------------------------------
 	protected UserSessionBean getUserSessionBean() {
 		return (UserSessionBean) getBean("UserSessionBean");
 	}
 
 	protected SessionManager getSessionManager() {
 		return (SessionManager) getBean("SessionManager");
 	}
 }
