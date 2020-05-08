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
 
 package com.perevillega.mynet.action.modelsupport;
 
 import org.hibernate.validator.InvalidValue;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Begin;
 import org.jboss.seam.annotations.web.RequestParameter;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.framework.EntityHome;
 
 import com.perevillega.mynet.action.passwordsupport.PasswordBean;
 import com.perevillega.mynet.action.passwordsupport.PasswordManager;
 import com.perevillega.mynet.model.User;
 
 @Name("userHome")
 public class UserHome extends EntityHome<User>
 {
     @RequestParameter Long userId;
     
     @In(create=true) protected PasswordBean passwordBean;
     
     @In protected FacesMessages facesMessages;
     
     @In(create=true) protected PasswordManager passwordManager;
 
     @Override
     public Object getId()
     {
         if (userId == null)
         {
             return super.getId();
         }
         else
         {
             return userId;
         }
     }
 
     @Override 
     public void create() {
         super.create();
     }
 
     @Override
     public String remove() {
     	getInstance().remove();
     	return super.remove();
     }
     
     @Override
     public String update() {    	
     	if (!passwordBean.verify()) {			
 			InvalidValue iv = new InvalidValue("Confirmation password does not match", PasswordBean.class, "confirm", null, null);
 			facesMessages.add(iv);
 			return null;
 		}
     	if(!"".equals(passwordBean.getPassword().trim())) {
     		instance.setPasswordHash(passwordManager.hash(passwordBean.getPassword()));
     	}
     	return super.update();
     }
     
 }
