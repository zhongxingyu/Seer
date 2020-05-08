 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.security.common;
 
 import java.security.Permission;
 import java.security.Permissions;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Set;
 
 import javax.security.auth.Subject;
 
 import org.eclipse.riena.security.common.ISubjectHolder;
 import org.eclipse.riena.security.common.ISubjectHolderService;
 import org.eclipse.riena.security.common.authorization.IAuthorizationService;
 import org.eclipse.riena.security.common.authorization.IPermissionCache;
 import org.eclipse.riena.security.common.authorization.ISentinelService;
 
 /**
  * Implementation of Sentinel that is registered as OSGi Service so that it can
  * be overwritten.
  */
 public class SentinelServiceImpl implements ISentinelService {
 
 	private IPermissionCache permCache;
 	private ISubjectHolderService subjectHolderService;
 	private IAuthorizationService authService;
 
 	public void bind(IPermissionCache permCache) {
 		this.permCache = permCache;
 	}
 
 	public void unbind(IPermissionCache permCache) {
 		if (permCache == this.permCache) {
 			this.permCache = null;
 		}
 	}
 
 	public void bind(ISubjectHolderService subjectHolderService) {
 		this.subjectHolderService = subjectHolderService;
 	}
 
 	public void unbind(ISubjectHolderService subjectHolderService) {
 		if (subjectHolderService == this.subjectHolderService) {
 			this.subjectHolderService = null;
 		}
 	}
 
 	public void bind(IAuthorizationService authService) {
 		this.authService = authService;
 	}
 
 	public void unbind(IAuthorizationService authService) {
 		if (authService == this.authService) {
 			this.authService = null;
 		}
 	}
 
 	/**
 	 * checkAccess reads the current Subject from the SubjectHolderService,
 	 * reads all its permissions from the cache and checks if this permission is
 	 * allowed for this subject
 	 * 
 	 * @param permission
 	 *            permission to be checked
 	 * @return
 	 */
 	public boolean checkAccess(Permission permission) {
 		ISubjectHolder subjectHolder = getSubjectHolderService().fetchSubjectHolder();
 		Subject subject = subjectHolder.getSubject();
 		if (subject != null) {
 			Permissions permissions = getPermissions(subject);
 			boolean result = permissions.implies(permission);
 			return result;
 		} else {
 			return false;
 		}
 	}
 
 	protected IPermissionCache getPermissionCache() {
 		return permCache;
 	}
 
 	protected ISubjectHolderService getSubjectHolderService() {
 		return subjectHolderService;
 	}
 
 	/**
 	 * Get Permissions for a specific Subject
 	 * 
 	 * @param subject
 	 * @return
 	 */
 	private Permissions getPermissions(Subject subject) {
 		Set<Principal> principals = subject.getPrincipals();
 		Permissions allPerms = new Permissions();
 		ArrayList<Principal> missingPrincipals = new ArrayList<Principal>();
		IPermissionCache permCache = getPermissionCache();
 
 		// iterate over the principals in the subject and try to find an entry in the PermissionCache
 		// add principals for which there are no permissions into the missingPrincipals ArrayList
 		for (Principal principal : principals) {
			Permissions perms = (Permissions) permCache.getPermissions(principal);
 			if (perms == null) {
 				missingPrincipals.add(principal);
 			} else {
 				// if we find permissions add them to the pool of permissions
 				Enumeration<Permission> permEnum = perms.elements();
 				while (permEnum.hasMoreElements()) {
 					allPerms.add(permEnum.nextElement());
 				}
 			}
 		}
 
 		// if there are principals with no permissions, retrieve them from the server
 		if (missingPrincipals.size() > 0) {
 			Permissions[] permissionsArray = authService.getPermissions(missingPrincipals
 					.toArray(new Principal[missingPrincipals.size()]));
 			for (int i = 0; i < missingPrincipals.size(); i++) {
				permCache.putPermissions(missingPrincipals.get(i), permissionsArray[i]);
 				Enumeration<Permission> permEnum = permissionsArray[i].elements();
 				while (permEnum.hasMoreElements()) {
 					allPerms.add(permEnum.nextElement());
 				}
 			}
 		}
 		return allPerms;
 	}
 
 }
