 package uk.co.vurt.hakken.security.auth;
 
 import java.util.Hashtable;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class LdapAuthenticator implements Authenticator {
 
 	private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticator.class);
 			
 	private Hashtable<String, String> environment;
 	private String prefix = null;
 	private String suffix = null;
 	private String errorMessage = "";
 	private Exception exception = null;
 	
 	public LdapAuthenticator(){
 		environment = new Hashtable<String, String>();
 		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
 	}
 	
 	@Override
 	public boolean authenticate(String username, String password) {
 		errorMessage = "";
 		exception = null;
 		
 		StringBuffer userCredentials = new StringBuffer();
 		if(prefix != null){
 			userCredentials.append(prefix);
 		}
 		userCredentials.append(username);
 		if(suffix != null){
 			userCredentials.append(suffix);
 		}
 		environment.put(Context.SECURITY_PRINCIPAL, userCredentials.toString());
 		environment.put(Context.SECURITY_CREDENTIALS, password);
 		
 		try {
 			DirContext context = new InitialDirContext(environment);
 			//if the context is created successfully, then we have authenticated.
 			//tidy up and then return true
 			logger.debug("Authentication succeeded for " + username);
 			context.close();
 			context = null;
 			return true;
 		} catch (NamingException e) {
 			//TODO: Handle more exceptions, as per the LDAP error codes:
 			//http://docs.oracle.com/javase/tutorial/jndi/ldap/exceptions.html
 			
 			logger.debug("Authentication failed", e);
 			if(e.getMessage().contains("error code 49")){
 				errorMessage = "Authentication failed: Invalid credentials";
 			}else {
 				errorMessage = "Authentication failed. " + e.getMessage();
 			}
 			exception = e;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public String getErrorMessage() {
 		return errorMessage;
 	}
 
 	public void setContextFactory(String contextFactory){
 		//"com.sun.jndi.ldap.LdapCtxFactory"
 		environment.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
 	}
 	
 	public void setServer(String serverUrl){
 		environment.put(Context.PROVIDER_URL, serverUrl); 
 	}
 	
 	public void setAuthenticationType(String authType){
 		environment.put(Context.SECURITY_AUTHENTICATION, authType);
 	}
 
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 
 	public void setSuffix(String suffix) {
 		this.suffix = suffix;
 	}
 	
 }
