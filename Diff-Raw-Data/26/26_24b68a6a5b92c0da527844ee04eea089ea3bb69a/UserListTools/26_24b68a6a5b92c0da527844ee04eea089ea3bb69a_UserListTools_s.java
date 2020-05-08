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
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 
 import com.perevillega.mynet.model.Role;
 import com.perevillega.mynet.model.User;
 
 @Name("userListTools")
 public class UserListTools {
 
 	@In
 	private EntityManager entityManager;
 	
 	@In
 	private UserList userList;
 	
     
     public void remove(User user) {  	
    	entityManager.merge(user);
     	
     	user.remove();
     	entityManager.remove(user);
    	entityManager.flush();
     	
     	userList.refresh();    	
     }
     
     public void removeSelected() {
     	List<User> list = userList.getStaticResultList();
     	for(User user: list) {
     		if(user.isSelected()){
     			user.remove();
     			entityManager.remove(user);    			
     		}
     	}
     	entityManager.flush();    	
     	userList.refresh();    	    	
     }
 }
