 /**
 * My Net is free software, you can redistribute it and/or modify
 * it under the terms of GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * You should have received a copy of the the GNU Affero
 * General Public License, along with My Net. If not, see
 *
 * http://www.gnu.org/licenses/agpl.txt
 */
 
 package com.perevillega.mynet.action;
 
 import org.jboss.seam.annotations.In;
 import java.util.List;
 import java.util.ArrayList;
 import javax.persistence.EntityManager;
 
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.RaiseEvent;
 import org.jboss.seam.core.Events;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.log.Log;
 import org.jboss.seam.security.Identity;
 
 import com.perevillega.mynet.action.passwordsupport.PasswordBean;
 import com.perevillega.mynet.action.passwordsupport.PasswordManager;
 import com.perevillega.mynet.action.settings.Settings;
 import com.perevillega.mynet.action.validators.UserValidator;
 import com.perevillega.mynet.model.User;
 
 @Name("registerAction")
 public class RegisterAction {
 
 	@Logger	private Log log;
 
 	@In protected EntityManager entityManager;
 
 	@In protected FacesMessages facesMessages;
 
 	@In(create=true) protected PasswordManager passwordManager;
 
 	@In(create=true) protected User newuser;
 
 	@In(create=true) protected PasswordBean passwordBean;
 	
 	@In(create=true) protected UserValidator userValidator;
 	
 	
 	public String register() {
 		log.info("Registering user #{newuser.username}");
 
 		if (!userValidator.validate(newuser, passwordBean)) {
 			log.info("Invalid registration request");
 			facesMessages.addToControls(userValidator.getInvalidValues());
 			return null;
 		}
 		
		newuser.setPasswordHash(passwordManager.hash(passwordBean.getPassword()));		
 		entityManager.persist(newuser);
 		if (Events.exists()) {
 			Events.instance().raiseTransactionSuccessEvent("golferRegistered", newuser);
 		}
 		facesMessages.addFromResourceBundle("registration.welcome", newuser.getUsername());
 		Identity identity = Identity.instance();
 		identity.setUsername(newuser.getUsername());
 		identity.setPassword(passwordBean.getPassword());
 		identity.quietLogin();
 		
 		return "success";
 	}
 	
 	public boolean isUsernameAvailable(String username) {
         return entityManager.createQuery(
             "select u from User u where u.username = :username")
             .setParameter("username", username)
             .getResultList().size() == 0;
     }
 	
 	public boolean isEmailRegistered(String email) {
         return entityManager.createQuery(
             "select u from User u where u.email = :email")
             .setParameter("email", email)
             .getResultList().size() > 0;
     }
 }
