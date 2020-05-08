 /**
  * This JAAS LoginModule does -not- perform authentication, 
  * but -does- return as user Principals those roles found in the 
  * directory in fields configured for role discovery.
  *
  * Though comparable to the JAAS JNDILoginModule, it is dissimilar
  * in not performing authentication and in returning user roles
  * as additional Principals.  It is not based on JNDILoginModule code.
  * 
  * Instead it is based on and includes code from Tomcat's JNDIRealm,
  * to return user roles.  Unlike JNDIRealm, it wraps roles
  * as Principals, as required by a JAAS LoginModule.  It also allows
  * multiple role fields; JNDIRealm allows only one such field.
  * Roles are namespaced to indicate the field of origin.
  * It omits JNDIRealm's authenticatation function. 
  * 
  * @author wdn5e
  * 
  * This product includes software developed by the
  * Apache Software Foundation (http://www.apache.org/).
  * Copyright (c) 1999-2003 The Apache Software Foundation.
  * All rights reserved.
  */
 
 package org.apache.catalina.realm;
import java.io.File;
 import java.io.IOException;
 import java.util.Hashtable;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import javax.security.auth.login.LoginException;

import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.spi.LoginModule;
 
import org.apache.catalina.Container;

import fedora.common.Constants;
import fedora.server.Context;
import fedora.server.errors.ModuleInitializationException;
import fedora.server.errors.NotAuthorizedException;
import fedora.server.security.PolicyEnforcementPoint;
import fedora.server.security.ReducedPolicyEnforcementPoint;

 public final class JAASJNDILoginModule extends JNDIRealm implements LoginModule {
 	
 	private CallbackHandler callbackHandler = null;
 	
 	protected CallbackHandler getCallbackHandler() {
 		return callbackHandler;
 	}
 	
 	private boolean authenticate = false; // safest default
 
 	protected boolean debug = false;
 
 	protected Map options = null;
 
 	protected Map sharedState = null;
 
 	private Subject subject = null;
 	
 	//private Realm realm = null;
 	private String namespace = null;
 	
 	private GenericPrincipal principal = null;
 	
 	protected static final int UNREADY = 0;
 	protected static final int READY = 1;
 	protected static final int ACK = 2;
 	protected static final int NACK = 3;
 	protected static final int IN_SUBJ = 4;
 	
 	protected int state = UNREADY;
	

 
 
 	// --------------------------------------------------------- Public Methods
 
 
 	/**
 	 * Initialize this <code>LoginModule</code> with the specified
 	 * configuration information.
 	 *
 	 * @param subject The <code>Subject</code> to be authenticated
 	 * @param callbackHandler A <code>CallbackHandler</code> for communicating
 	 *  with the end user as necessary
 	 * @param sharedState State information shared with other
 	 *  <code>LoginModule</code> instances
 	 * @param options Configuration information for this specific
 	 *  <code>LoginModule</code> instance
 	 */
 	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
 		log("in ldap init() ");
 		log("ldap initialize()");
		setContainer(JAASJNDILoginModule.staticContainer); //fixup		
 		principal = null;
 		this.state = UNREADY;		
 		if ((subject != null) && (callbackHandler != null)) {
 			log("ldap init() ");
 			this.subject = subject;
 			this.callbackHandler = callbackHandler;
 			this.sharedState = sharedState;
 			if (this.sharedState == null) {
 				this.sharedState = new Hashtable();
 			}
 			this.options = options;
 			if (this.options == null) {
 				this.options = new Hashtable();
 			}
 			this.debug = "true".equalsIgnoreCase((String) options.get("debug"));
 			log("ldap init(), debug set to " + debug);
 			log("ldap init(), dumping options...");
 			Iterator it = options.keySet().iterator();
 			while (it.hasNext()) {
 				String key = (String) it.next();
 				log(key + "=" + options.get(key));
 			}
 			//String realmClassName = "";
 			namespace = this.getClass().getName();
 			try {
 				//realmClassName = (String) options.get("realm");
 				//Class[] formalArgs = new Class[] {};
 				//Object[] actualArgs = new Object[] {};
 				//Constructor constructor = Class.forName(realmClassName).getConstructor(formalArgs);
 				//realm = (Realm) constructor.newInstance(actualArgs);
 				//namespace =  realm.getClass().getName();
 				//if ("org.apache.catalina.realm.MemoryRealm".equals(realmClassName)) {
 					//((MemoryRealm)realm).start();
 					localInitialize(subject, callbackHandler, sharedState, options);
 					start();					
 				//}
 				this.state = READY;				
 			} catch (Throwable e) {
 				//log("couldn't call constructor " + realmClassName + "():  " + e.getClass().getName(), e);
 			}				
 		}
 		log("/initialize()");
 	}
 	
 	private void localInitialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
 		
 		String connectionURL = ""; //"ldap://ldapvs.itc.virginia.edu:389";
 		String userBase = ""; //"o=University of Virginia,c=US";
 		String userSearch = ""; //"(uid={0})";
 		String userRoleName = ""; //"description,sn,uid,edupersonscopedaffiliation";
 		String userPassword = "";
 		boolean userSubtree = true;
 
 		connectionURL = (String) options.get("connectionURL");
 		userBase = (String) options.get("userBase");
 		userSearch = (String) options.get("userSearch");
 		userRoleName = (String) options.get("userRoleName");
 		userPassword = (String) options.get("userPassword");
 		userSubtree = "true".equalsIgnoreCase((String) options.get("userSubtree"));
 
 		setConnectionURL(connectionURL);
 		setUserBase(userBase);
 		setUserSubtree(userSubtree);
 		setUserSearch(userSearch);
 		setUserRoleName(userRoleName);		
 		setUserPassword(userPassword);
 		authenticate = ! JNDIRealm.NO_AUTHENTICATION.equalsIgnoreCase(userPassword);
 	}
 
 
 	private void rollback() {
     	Iterator principals = subject.getPrincipals().iterator();
     	while (principals.hasNext()) {
     		AbstractPrincipal testPrincipal = ((AbstractPrincipal)principals.next());
     		if (namespace.equals(testPrincipal.getNamespace())) {
             	subject.getPrincipals().remove(testPrincipal);
     		}
     	}	
 		principal = null;
 		state = READY;		
 	}
 		
 	/**
 	 * Phase 1 of authenticating a <code>Subject</code>.
 	 *
 	 * @return <code>true</code> if the authentication succeeded, or
 	 *  <code>false</code> if this <code>LoginModule</code> should be
 	 *  ignored
 	 *
 	 * @exception LoginException if the authentication fails
 	 */
 	public boolean login() throws LoginException {
 		log("login()");
 		state = NACK;
 		principal = null;
 		String username = null;
 		String password = null;
         if (callbackHandler == null)
             throw new LoginException("No CallbackHandler specified");
         Callback callbacks[] = new Callback[2];
         callbacks[0] = new NameCallback("Username: ");
         callbacks[1] = new PasswordCallback("Password: ", false);
         try {
             callbackHandler.handle(callbacks);
             username = ((NameCallback) callbacks[0]).getName();
             password = new String(((PasswordCallback) callbacks[1]).getPassword());
         } catch (IOException e) {
             throw new LoginException(e.toString());
         } catch (UnsupportedCallbackException e) {
             throw new LoginException(e.toString());
         }
 		log("login(): username=" + username);
 		log("login(): password=" + password);
 		//log("login(): realm=" + realm);
 		//log("login(): getPath()=" + getPathname());
 		//log("login(): getPath()=" + ((MemoryRealm)realm).getPathname());
 		principal = (GenericPrincipal) authenticate(username, password);
 		//GenericPrincipal principal = (GenericPrincipal) realm.authenticate(username, password);
 		log("login(): principal=" + principal);		
 		if (principal != null) {
 			state = ACK;
 		}
 		boolean rc = authenticate ? false : (state == ACK);
 		//rc indicates whether this module has logged in
 		//attributeStoreOnly whether this module itself counts toward login
 		//original wording was equiv. to rc = (state == ACK)
 		//attributeStoreOnly == true will be UVa usage; == false may be what Cornell wants
 		log("/login(): " + rc);
 		return rc;
 	}
 	
 	public boolean commit() throws LoginException {
 		log("commit()");
 		boolean rc = (state == ACK);
 		if (rc) {
 			log("commit(): subject=" + subject);
 			log("commit(): subject.getPrincipals()=" + subject.getPrincipals());
 			log("commit(): namespace=" + namespace);
 			log("commit(): principal=" + principal);
 			log("commit(): no id subject here, chap!!!");
 			if (! authenticate) {
 	        	subject.getPrincipals().add(new IdPrincipal(namespace, principal.getName()));				
 			}
 			if (authenticate && subject.getPrincipals().isEmpty()) {
 				log("login():  prev. login modules were not successful; do -not- return roles");
 			} else {
 	           	String[] roles = principal.getRoles();
 	           	for (int i = 0; i < roles.length; i++) {
 	            	subject.getPrincipals().add(new RolePrincipal(namespace, roles[i]));
 	           	}
 			}
 			state = IN_SUBJ;
 		}
 		log("/commit(): true");
 		return true;
 	}
 
 	/**
 	 * Phase 2 of authenticating a <code>Subject</code> when Phase 1
 	 * fails.  This method is called if the <code>LoginContext</code>
 	 * failed somewhere in the overall authentication chain.
 	 *
 	 * @return <code>true</code> if this method succeeded, or
 	 *  <code>false</code> if this <code>LoginModule</code> should be
 	 *  ignored
 	 *
 	 * @exception LoginException if the abort fails
 	 */
 	public boolean abort() throws LoginException {
 		log("abort()");
 		boolean rc = (state == ACK);
 		//clear(); ?
 		//state == ???
 		rollback();
 		log("/abort(): " + rc);
 		return rc;
 	}
 		
 	/**
 	 * Log out this user.
 	 *
 	 * @return <code>true</code> in all cases because the
 	 *  <code>LoginModule</code> should not be ignored
 	 *
 	 * @exception LoginException if logging out failed
 	 */
 	public boolean logout() throws LoginException {
 		log("logout()");		
 		rollback();
 		//clear(); ?
 		//state == ???
 		boolean rc = true;
 		log("/logout(): " + rc);
 		return rc;
 	}
 
 
 	static public int logN = 0;
 	
 	/**
 	 * Log a message.
 	 *
 	 * @param message The message to be logged
 	 */
 	protected void log(String message) {
 		if (debug) {
 			System.out.print(logN++ + this.getClass().getName() + ":  ");
 			System.out.println(message);
 		}
 	}
 
 	/**
 	 * Log a message and associated exception.
 	 *
 	 * @param message The message to be logged
 	 * @param exception The associated exception
 	 */
 	protected void log(String message, Throwable exception) {
 		if (debug) {
 			log(message);
 			exception.printStackTrace(System.out);
 		}
 	}
	
	//fixup
	private static Container staticContainer = null;
	public static void setStaticContainer(Container container) {
		staticContainer = container;
	}
 
 }
