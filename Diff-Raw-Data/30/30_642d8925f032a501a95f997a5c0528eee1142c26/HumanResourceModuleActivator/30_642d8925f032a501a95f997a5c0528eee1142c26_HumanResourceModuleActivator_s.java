 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.hr;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Role;
 import org.openmrs.api.UserService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.Activator;
 import org.openmrs.module.ModuleActivator;
 import org.openmrs.util.OpenmrsConstants;
 
 /**
  * This class contains the logic that is run every time this module is either started or shutdown
  */
 public class HumanResourceModuleActivator implements ModuleActivator {
 	
 	private Log log = LogFactory.getLog(this.getClass());
 	
 	public void contextRefreshed() {
 			
 	}
 
 	public void started() {
 		
 	}
 
 	public void stopped() {
		UserService us = Context.getUserService();
		Role humanResourceManager = us.getRole("Human Resource Manager");
		Role humanResourceClerk=us.getRole("Human Resource Clerk");
		if(humanResourceManager!=null){
			us.purgeRole(humanResourceManager);
		}
		if(humanResourceClerk!=null){
			us.purgeRole(humanResourceClerk);
		}
		log.info("Shutting down Human Resource Module");
 		
 	}
 
 	public void willRefreshContext() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void willStart() {
 		Set<Role> inheritedRoleSet=new HashSet<Role>();
 		inheritedRoleSet.add(Context.getUserService().getRole(OpenmrsConstants.AUTHENTICATED_ROLE));
 		UserService us = Context.getUserService();
		Role humanResourceManager = us.getRole("Human Resource Manager");
 		// Create role if it doesn't exist
 		if (humanResourceManager == null){
			humanResourceManager=new Role("Human Resource Manager", "HR Role");
 			humanResourceManager.setInheritedRoles(inheritedRoleSet);
 			us.saveRole(humanResourceManager); 
 		}
		Role humanResourceClerk=us.getRole("Human Resource Clerk");
 		// Create role if it doesn't exist
 		if (humanResourceClerk == null ){
		 humanResourceClerk=new Role("Human Resource Clerk", "HR Role");
 		 humanResourceClerk.setInheritedRoles(inheritedRoleSet);
 		 us.saveRole(humanResourceClerk);
 		 } 
		addPrivilegesToRole(humanResourceManager,Arrays.asList("Add Post","Add Assignments","Find Human Resources","View Posts","View Staff Demographics","View Staff", "Manage Job Titles","Manage Posts","Manage Staff","Manage Staff Attribute Types","View Reports"));
		addPrivilegesToRole(humanResourceClerk,Arrays.asList("Add Post","Add Assignments","Find Human Resources","View Posts","View Staff Demographics","View Staff"));
 		log.info("Starting Human Resource Module");
 		
 	}
 
 	public void willStop() {
		// TODO Auto-generated method stub
 		
 	}
 	
 	public void addPrivilegesToRole(Role role, List<String> privs) {
 		UserService us = Context.getUserService();
 		for (int i = 0; i < privs.size(); i++) {
 			role.addPrivilege(us.getPrivilege(privs.get(i)));
 		}
 		us.saveRole(role);
 	}
 	
 }
 
