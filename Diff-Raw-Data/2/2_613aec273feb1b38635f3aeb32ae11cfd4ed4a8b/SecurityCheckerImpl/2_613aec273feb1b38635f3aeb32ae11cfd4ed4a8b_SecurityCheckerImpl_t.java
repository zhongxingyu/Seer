 /*
  * Copyright 2000 - 2010 Ivan Khalopik. All Rights Reserved.
  */
 
 package org.greatage.security;
 
 import org.greatage.util.CollectionUtils;
 
 import java.util.List;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class SecurityCheckerImpl implements SecurityChecker {
 	private final UserContext userContext;
 	private final AccessControlManager accessControlManager;
 
 	public SecurityCheckerImpl(final UserContext userContext, final AccessControlManager accessControlManager) {
 		this.userContext = userContext;
 		this.accessControlManager = accessControlManager;
 	}
 
 	public void checkPermission(final Object securedObject, final String permission) {
 		final List<String> authorities = getAuthorities();
 		final AccessControlList acl = accessControlManager.getAccessControlList(securedObject);
 		for (String authority : authorities) {
 			final AccessControlEntry ace = acl.getAccessControlEntry(authority, permission);
 			if (ace.isGranted()) {
 				return;
 			}
 		}
 		throw new AccessDeniedException(String.format("Access denied for object %s. Need permission missed: '%s'", securedObject, permission));
 	}
 
 	public void checkAuthority(final String authority) {
 		final List<String> authorities = getAuthorities();
		if (!authorities.contains(authority)) {
 			throw new AccessDeniedException(String.format("Access denied. Needed authority missed: '%s'", authority));
 		}
 	}
 
 	private List<String> getAuthorities() {
 		final Authentication user = userContext.getUser();
 		return user != null ? user.getAuthorities() : CollectionUtils.<String>newList();
 	}
 }
