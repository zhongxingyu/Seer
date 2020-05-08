 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.accesscontrol.authentication.verifiers;
 
 import java.util.Properties;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.connection.ServerKeyStoreManager;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 
 /**
  * Verifies username/password using LDAP.
  * 
  * @author Wesendonk
  */
 public class LDAPVerifier extends AbstractAuthenticationControl {
 
 	private final String ldapUrl;
 	private final String ldapBase;
 	private final String searchDn;
 	private boolean useSSL;
 
 	private static final String DEFAULT_CTX = "com.sun.jndi.ldap.LdapCtxFactory";
 	private final String authUser;
 	private final String authPassword;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param ldapUrl url, if url starts with ldaps:// SSL is used.
 	 * @param ldapBase base
 	 * @param searchDn dn
 	 * @param authUser user to allow access to server
 	 * @param authPassword password of user to allow access to server
 	 */
 	public LDAPVerifier(String ldapUrl, String ldapBase, String searchDn, String authUser, String authPassword) {
 		this.ldapUrl = ldapUrl;
 		this.ldapBase = ldapBase;
 		this.searchDn = searchDn;
 		this.authUser = authUser;
 		this.authPassword = authPassword;
 
 		if (ldapUrl.startsWith("ldaps://")) {
 			useSSL = true;
 			ServerKeyStoreManager.getInstance().setJavaSSLProperties();
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.server.accesscontrol.authentication.verifiers.AbstractAuthenticationControl#verifyPassword(org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser,
 	 *      java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean verifyPassword(ACUser resolvedUser, String username, String password) throws AccessControlException {
 		DirContext dirContext = null;
 
 		// anonymous bind and resolve user
 		try {
 			if (authUser != null && authPassword != null) {
 				// authenticated bind and resolve user
 				final Properties authenticatedBind = authenticatedBind(authUser, authPassword);
 				authenticatedBind.put(Context.SECURITY_PRINCIPAL, authUser);
 				dirContext = new InitialDirContext(authenticatedBind);
 			} else {
 				// anonymous bind and resolve user
 				dirContext = new InitialDirContext(anonymousBind());
 			}
 		} catch (final NamingException e) {
 			ModelUtil.logWarning("LDAP Directory " + ldapUrl + " not found.", e);
 			return false;
 		}
 		final String resolvedName = resolveUser(username, dirContext);
 		if (resolvedName == null) {
 			return false;
 		}
 
 		// Authenticated bind and check user's password
 		try {
 			dirContext = new InitialDirContext(authenticatedBind(resolvedName, password));
 		} catch (final NamingException e) {
 			e.printStackTrace();
 			ModelUtil.logWarning("Login failed on " + ldapBase + " .", e);
 			return false;
 		}
 		return true;
 	}
 
 	private Properties anonymousBind() {
 		final Properties props = new Properties();
 		props.put("java.naming.ldap.version", "3");
 		props.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CTX);
 		props.put(Context.PROVIDER_URL, ldapUrl);
 		props.put("java.naming.ldap.factory.socket",
			"org.eclipse.emf.emfstore.internal.server.accesscontrol.authentication.LDAPSSLSocketFactory");
 
 		if (useSSL()) {
 			props.put(Context.SECURITY_PROTOCOL, "ssl");
 		}
 
 		return props;
 	}
 
 	private boolean useSSL() {
 		return useSSL;
 	}
 
 	private Properties authenticatedBind(String principal, String credentials) {
 		final Properties bind = anonymousBind();
 
 		bind.put(Context.SECURITY_AUTHENTICATION, "simple");
 		bind.put(Context.SECURITY_CREDENTIALS, credentials);
 
 		return bind;
 	}
 
 	private String resolveUser(String username, DirContext dirContext) {
 		final SearchControls constraints = new SearchControls();
 		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		NamingEnumeration<SearchResult> results = null;
 		try {
 			results = dirContext.search(ldapBase, "(& (" + searchDn + "=" + username + ") (objectclass=*))",
 				constraints);
 		} catch (final NamingException e) {
 			ModelUtil.logWarning("Search failed, base = " + ldapBase, e);
 			return null;
 		}
 
 		if (results == null) {
 			return null;
 		}
 
 		String resolvedName = null;
 		try {
 			while (results.hasMoreElements()) {
 				final SearchResult sr = results.next();
 				if (sr != null) {
 					resolvedName = sr.getName();
 				}
 				break;
 			}
 		} catch (final NamingException e) {
 			ModelUtil.logException("Search returned invalid results, base = " + ldapBase, e);
 			return null;
 		}
 
 		if (resolvedName == null) {
 			ModelUtil.logWarning("Distinguished name not found on " + ldapBase);
 			return null;
 		}
 		return resolvedName;
 	}
 
 }
