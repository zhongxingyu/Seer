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
 package org.eclipse.riena.security.common.authentication;
 
 import java.io.Serializable;
 import java.security.Principal;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.riena.security.common.session.Session;
 
 /**
  * An object of this class will be returned by the authentication process. It
  * contains the references to the authentication module and the session service.
  * 
  */
 public class AuthenticationTicket implements Serializable {
 
	private static final long serialVersionUID = 4136974967239039236L;

 	private Session session;
 	private Set<Principal> principals;
 
 	/**
 	 * constructor.
 	 */
 	public AuthenticationTicket() {
 		super();
 	}
 
 	/**
 	 * constructor.
 	 * 
 	 * @param session
 	 *            session
 	 * @param authenticationContext
 	 *            authentication context
 	 * @pre session != null
 	 * @pre authenticationContext != nul
 	 */
 	public AuthenticationTicket(Session session) {
 		super();
 		assert session != null : "Missing session"; //$NON-NLS-1$
 		this.session = session;
 		this.principals = new HashSet<Principal>();
 	}
 
 	/**
 	 * get the session
 	 * 
 	 * @return session
 	 */
 	public Session getSession() {
 		return session;
 	}
 
 	/**
 	 * get the authentication context
 	 * 
 	 * @return authentication context object reference
 	 */
 	public Set<Principal> getPrincipals() {
 		if (principals == null) {
 			principals = new HashSet<Principal>();
 		}
 		return principals;
 	}
 
 	/**
 	 * @param session
 	 *            The session to set.
 	 */
 	public void setSession(Session session) {
 		this.session = session;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return "AuthenticationTicket(session=" + getSession() + ",principals=" + getPrincipals() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 }
