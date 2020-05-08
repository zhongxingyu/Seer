 /*
  * Copyright (c) 2002-2004
  * All rights reserved.
  */
 
 package com.innovalog.jmwe.plugins.functions;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Category;
 
 import webwork.action.ServletActionContext;
 
 import com.atlassian.jira.ComponentManager;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.MutableIssue;
 import com.atlassian.jira.project.Project;
 import com.atlassian.jira.security.roles.DefaultRoleActors;
 import com.atlassian.jira.security.roles.ProjectRole;
 import com.atlassian.jira.security.roles.ProjectRoleManager;
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.user.User;
 import com.opensymphony.workflow.FunctionProvider;
 
 
 // This post function will assign the issue to the first default user of teh specified role 
 public class AssignToRoleMemberFunction implements FunctionProvider
 {
     private static final Category log = Category.getInstance(AssignToRoleMemberFunction.class);
 
     public void execute(Map transientVars, Map args, PropertySet ps)
     {
     	HttpServletRequest request = ServletActionContext.getRequest();
     	if (request!=null)
     	{
 	    	String[] assigneeSelected = request.getParameterValues("assignee");
 	    	if( assigneeSelected != null && !assigneeSelected[0].equals("-1") && args.get("skipIfAssignee")!=null && ((String)args.get("skipIfAssignee")).equalsIgnoreCase("yes")) {
 	    	    // the user explicitly selected an assignee (and not "Unassigned")
 	    	    return;
 	    	}
     	}              
         
         Long projectRoleId= null;        
         String rawprojectRoleId= null;        
         rawprojectRoleId=(String)args.get("jira.projectrole.id");
         projectRoleId=null;
         if(StringUtils.isBlank(rawprojectRoleId))
         {
           log.warn("AssignToRoleMember not configured with a valid projectroleid. (no assignment will be made)");
           return;       
         }
         try
         {
         	projectRoleId = new Long(Long.parseLong(rawprojectRoleId));          
        
         }
         catch(NumberFormatException  e)
         {
           StringBuffer sb = new StringBuffer();
           log.warn(sb.append("AssignToRoleMember not configured with a valid projectroleid, the project role id: ").append(projectRoleId).append(" can not be parsed. (no assignment will be made)").toString());
           return;
        
         }
                 
         ProjectRoleManager projectRoleManager=(ProjectRoleManager)ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class);
         ProjectRole projectRole=projectRoleManager.getProjectRole(projectRoleId);
         if(projectRole== null)
         {
           StringBuffer sb = new StringBuffer();
           log.warn(sb.append("AssignToRoleMember is configured to assign to the default user in project role that doesn\'t exist: id is ").append(projectRoleId).append(" (no assignment will be made)").toString());
           return;
        
         }
         
         User assignToUser = null;
         Issue genericIssue =  (Issue) transientVars.get("issue");
         Project project = genericIssue.getProjectObject();
 		DefaultRoleActors roleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
 		if (roleActors!=null) {
 			Set users = roleActors.getUsers();
 			if (users != null && users.size() > 0) {
 				
 				//Try to see if there is a user that has a following property:
 				// 1. name: [ProjectName]x[RoleName], value: default
 				// or 2. name: defaultAssignee[x], value: [ProjectName]x[RoleName]
				// or 3. name: defaultAssignee, value: [ProjectKey]=>[RoleName], [ProjectKey]->[RoleName], ...
 				String propertyNameOrValue = project.getName() + "x" + projectRole.getName();
 				Iterator iterator = users.iterator();
 				userIterator: while (iterator.hasNext()) {
 					User user = (User)iterator.next();
 					
 					PropertySet userProperties = user.getPropertySet();
 					//1.
 					String property = userProperties.getString("jira.meta." + propertyNameOrValue); 
 					if (property != null && "default".equals(property)) {
 						assignToUser = user;
 						break;
 					}
 					//2.
 					for (int i=1;  (property = userProperties.getString("jira.meta.defaultAssignee"+i)) != null; i++)
 						if (property.equalsIgnoreCase(propertyNameOrValue)) {
 							assignToUser = user;
 							break userIterator;
 						}
 					//3.
 					if (userProperties.getString("jira.meta.defaultAssignee") != null)
 						for (String s : userProperties.getString("jira.meta.defaultAssignee").split(","))
 						{
 							String[] t = s.split("->");
 							if (t.length == 2 && t[0].trim().equalsIgnoreCase(project.getKey()) && t[1].trim().equalsIgnoreCase(projectRole.getName()))
 							{
 								assignToUser = user;
 								break userIterator;
 							}
 						}
 				}
 				
 				//We couldn't find a user with the specified property, pick the first guy
 				if (assignToUser == null) {
 					assignToUser = (User)users.iterator().next();
 					StringBuffer sb = new StringBuffer();
 			          log.warn(sb.append("AssignToRoleMember was not able to find a user with the property named ").append(propertyNameOrValue).append(" and value set to \"default\". First user from the role will be used."));					
 				}
 								
 								
 			} else {
 				StringBuffer sb = new StringBuffer();
 	        	log.warn(sb.append("There are no users assigned in the project role ").append(projectRole.getName()).append(" (no assignment will be made)").toString());
 	        	return;				
 			}
 		}
 		if (assignToUser == null) {
 			StringBuffer sb = new StringBuffer();
         	log.warn(sb.append("There is no default user assigned in the project role ").append(projectRole.getName()).append(" (no assignment will be made)").toString());
         	return;
         }
 		
 		// Assign the issue
 		log.info("AssignToRoleMember assigning to: " + assignToUser.getFullName());
         MutableIssue issue = (MutableIssue) transientVars.get("issue");        
         issue.setAssignee(assignToUser);
         issue.store();
     }
 
 }
