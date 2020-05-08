 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: Oct 10, 2003 / 21:46:35 PM
  * net.jforum.security.PermissionControl.java
  * The JForum Project
  * http://www.jforum.net
  * 
 * $Id: PermissionControl.java,v 1.2 2004/04/21 23:57:36 rafaelsteil Exp $
  */
 package net.jforum.security;
 
 import net.jforum.model.security.SecurityModel;
 
 /**
  * Methods and properties for all classes that need make use of security
  * actions.  
  * 
  * @author Rafael Steil
  */
 public class PermissionControl 
 {
 	public static int ROLE_DENY = 0;
 	public static int ROLE_ALLOW = 1;
 
 	private RoleCollection roles;
 	private SecurityModel smodel;
 	
 	public void setRoles(RoleCollection roles)
 	{
 		this.roles = roles;
 	}
 	
 	public void setSecurityModel(SecurityModel smodel)
 	{
 		this.smodel = smodel;
 	}
 	
 	public void addRole(int id, Role role) throws Exception
 	{
 		this.smodel.addRole(id, role);
 	}
 	
 	public void addRole(int id, Role role, RoleValueCollection roleValues) throws Exception
 	{
 		this.smodel.addRole(id, role, roleValues);
 	}
 	
 	public void deleteAllRoles(int id) throws Exception
 	{
 		this.smodel.deleteAllRoles(id);
 	}
 	
 	/**
 	 * Gets a role.
 	 * 
 	 * @param roleName The role's name
 	 * @return A <code>Role</code> object if the role was found, or <code>null</code> if not found.
 	 */
 	public Role getRole(String roleName)
 	{
 		return this.roles.get(roleName);
 	}
 	
 	/* 
 	 * @see net.jforum.security.PermissionControl#canAccess(java.lang.String)
 	 */
 	public boolean canAccess(String roleName) 
 	{
 		Role role = this.roles.get(roleName);
 		if (role == null || role.getType() == PermissionControl.ROLE_DENY) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/* 
 	 * @see net.jforum.security.PermissionControl#canAccess(java.lang.String, java.lang.String)
 	 */
 	public boolean canAccess(String roleName, String roleValue) 
 	{
 		Role role = this.roles.get(roleName);
 		if (role == null) {
 			return false;
 		}
 		
 		RoleValue rv = new RoleValue();
 		rv.setType(PermissionControl.ROLE_ALLOW);
		rv.setValue(roleValue);
		
		return role.getValues().contains(rv);
 	}
 }
