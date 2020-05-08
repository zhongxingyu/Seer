 /*******************************************************************************
  * Copyright (c) 2012 SAP AG
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   SAP AG - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.enterprise.security;
 
 import java.lang.reflect.Field;
 import java.security.Principal;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.security.auth.Subject;
 import javax.security.auth.login.LoginException;
 
 import org.apache.catalina.Realm;
 import org.apache.catalina.Wrapper;
 import org.apache.openejb.core.ThreadContext;
 import org.apache.openejb.core.security.AbstractSecurityService;
 import org.apache.openejb.spi.CallerPrincipal;
 
 public class StandardSecurityService extends AbstractSecurityService {
 	private Wrapper wrapper;
 
 	static protected final ThreadLocal<LinkedList<Subject>> runAsStack = new ThreadLocal<LinkedList<Subject>>() {
 		protected LinkedList<Subject> initialValue() {
 			return new LinkedList<Subject>();
 		}
 	};
 
 	public Object enterWebApp(Wrapper wrapper, Principal principal, String runAs) {
 		this.wrapper = wrapper;
 		Identity newIdentity = null;
 		if (principal != null) {
 			Subject newSubject = createSubject(wrapper.getRealm(), principal);
 			newIdentity = new Identity(newSubject, null);
 		}
 
 		Identity oldIdentity = clientIdentity.get();
 		WebAppState webAppState = new WebAppState(oldIdentity, runAs != null);
 		clientIdentity.set(newIdentity);
 
 		if (runAs != null) {
 			Subject runAsSubject = createRunAsSubject(runAs);
 			runAsStack.get().addFirst(runAsSubject);
 		}
 		return webAppState;
 	}
 
 	public void exitWebApp(Object state) {
 		if (state instanceof WebAppState) {
 			WebAppState webAppState = (WebAppState) state;
 			clientIdentity.set(webAppState.oldIdentity);
 			if (webAppState.hadRunAs) {
 				runAsStack.get().removeFirst();
 			}
 		}
 		wrapper = null;
 	}
 
 	protected Subject createSubject(Realm realm, Principal principal) {
 		if (realm == null)
 			throw new NullPointerException("realm is null");
 		if (principal == null)
 			throw new NullPointerException("tomcatPrincipal is null");
 
 		TomcatUserWrapper tomcatUser = new TomcatUserWrapper(realm, principal);
 		HashSet<Principal> principals = new HashSet<Principal>();
 		principals.add(tomcatUser);
 
 		Subject subject = new Subject(true, principals, new HashSet(),
 				new HashSet());
 		return subject;
 	}
 
 	protected Subject createRunAsSubject(String role) {
 		if (role == null)
 			return null;
 		RunAsRole runAsRole = new RunAsRole(role);
 		HashSet<Principal> principals = new HashSet<Principal>();
 		principals.add(runAsRole);
 		return new Subject(true, principals, new HashSet(), new HashSet());
 	}
 
 	@Override
 	public Set<String> getLogicalRoles(Principal[] principals,
 			Set<String> logicalRoles) {
 		LinkedHashSet<String> roles = new LinkedHashSet<String>(
 				logicalRoles.size());
 		for (Object role : logicalRoles) {
 			String logicalRole = (String) role;
 			for (Principal principal : principals) {
 				if (principal instanceof TomcatUserWrapper) {
 					TomcatUserWrapper user = (TomcatUserWrapper) principal;
 					if (user.getRealm().hasRole(wrapper,
 							user.getTomcatPrincipal(), logicalRole)) {
 						roles.add(logicalRole);
 						break;
 					}
 				} else if (principal instanceof RunAsRole) {
 					RunAsRole runAsRole = (RunAsRole) principal;
 					String name = runAsRole.getName();
 					if (logicalRole.equals(name)) {
 						roles.add(logicalRole);
 					}
 				}
 			}
 		}
 		return roles;
 	}
 	
 	@Override
 	public boolean isCallerInRole(String role) {
 		if (role == null)
 			throw new IllegalArgumentException("Role must not be null");
 
 		ThreadContext threadContext = ThreadContext.getThreadContext();
 		SecurityContext securityContext = threadContext
 				.get(SecurityContext.class);
 
 		Field field;
 		try {
 			field = securityContext.getClass().getDeclaredField("subject");
 			field.setAccessible(true);
 	    	final Set<TomcatUserWrapper> users = ((Subject)field.get(securityContext)).getPrincipals(TomcatUserWrapper.class);
 	    	boolean inRole = false;
 	    	for(TomcatUserWrapper user : users) {
				inRole = wrapper.getRealm().hasRole(wrapper,
						user.getTomcatPrincipal(), role);
 	    		if(inRole)
 	    			return true;
 	    	}	  
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 		}
 		return false;
 	}
 
 	protected static class RunAsRole implements Principal {
 		private final String roleName;
 
 		public RunAsRole(String roleName) {
 			this.roleName = roleName;
 		}
 
 		public String getName() {
 			return roleName;
 		}
 
 		public String toString() {
 			return "[RunAsRole: " + roleName + "]";
 		}
 
 		public boolean equals(Object o) {
 			if (this == o)
 				return true;
 			if (o == null || getClass() != o.getClass())
 				return false;
 
 			RunAsRole runAsRole = (RunAsRole) o;
 
 			return roleName.equals(runAsRole.roleName);
 		}
 
 		public int hashCode() {
 			return roleName.hashCode();
 		}
 	}
 
 	private static class WebAppState {
 		private final Identity oldIdentity;
 		private final boolean hadRunAs;
 
 		public WebAppState(Identity oldIdentity, boolean hadRunAs) {
 			this.oldIdentity = oldIdentity;
 			this.hadRunAs = hadRunAs;
 		}
 	}
 
 	@CallerPrincipal
 	protected static class TomcatUserWrapper implements Principal {
 		private final Realm realm;
 		private final Principal tomcatPrincipal;
 
 		public TomcatUserWrapper(Realm realm, Principal tomcatPrincipal) {
 			this.realm = realm;
 			this.tomcatPrincipal = tomcatPrincipal;
 		}
 
 		public Realm getRealm() {
 			return realm;
 		}
 
 		public Principal getTomcatPrincipal() {
 			return tomcatPrincipal;
 		}
 
 		public String getName() {
 			return tomcatPrincipal.getName();
 		}
 
 		public String toString() {
 			return "[TomcatUser: " + tomcatPrincipal + "]";
 		}
 
 		public boolean equals(Object o) {
 			if (this == o)
 				return true;
 			if (o == null || getClass() != o.getClass())
 				return false;
 
 			TomcatUserWrapper user = (TomcatUserWrapper) o;
 
 			return realm.equals(user.realm)
 					&& tomcatPrincipal.equals(user.tomcatPrincipal);
 		}
 
 		public int hashCode() {
 			int result;
 			result = realm.hashCode();
 			result = 31 * result + tomcatPrincipal.hashCode();
 			return result;
 		}
 	}
 
 	@Override
 	public UUID login(String arg0, String arg1, String arg2)
 			throws LoginException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
