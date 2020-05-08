 package cmf.security;
 
 import java.io.ByteArrayInputStream;
 import java.security.PrivilegedAction;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.Hashtable;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.security.auth.Subject;
 import javax.security.auth.login.LoginContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class KeyTabLdapUserInfoRepository implements IUserInfoRepository {
 
 	protected String ldapProviderUrl;
 	protected Logger log;
 	
 	
 	public KeyTabLdapUserInfoRepository(String ldapProviderUrl) {
 		this.ldapProviderUrl = ldapProviderUrl;
 		
 		this.log = LoggerFactory.getLogger(this.getClass());
 	}
 	
 	
 	@Override
 	public String getDistinguishedName(final String accountName) throws Exception {
 		String dn;
 		
 		LoginContext ctx = new LoginContext("cmf.security.ldap.lookup");
 		ctx.login();
 		
 		dn = Subject.doAs(ctx.getSubject(), new PrivilegedAction<String>() {
 
 			@Override
 			public String run() {
 				
 				String dn = null;
 				
 				try {
 					Hashtable<String, Object> props = new Hashtable<String, Object>();
 					props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 					props.put(Context.PROVIDER_URL, ldapProviderUrl);
 					props.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");
 				
 				    /* Create initial context */
 				    DirContext ctx = new InitialDirContext(props);
 
 				    SearchControls controls = new SearchControls();
 				    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 				    controls.setDerefLinkFlag(true);
 				    
 				    String filter = String.format("(&(objectCategory=person)(objectClass=user)(sAMAccountName=%s))", accountName);
 				    
 				    NamingEnumeration<SearchResult> results = ctx.search( 
 				    		"", 
 				    		filter, 
 				    		controls);
 				    
 				    SearchResult result = results.next();
 				    
 			    	log.debug("Name: {}", result.getName());
 			    	log.debug("NameInNamespace: {}", result.getNameInNamespace());
 			    	for (NamingEnumeration<? extends javax.naming.directory.Attribute> e = result.getAttributes().getAll(); e.hasMoreElements();) {
 			    		javax.naming.directory.Attribute attr = e.next();
 			    		log.debug("|-- Found attribute named: {}", attr.getID());
 			    	}
 			    	
 			    	dn = result.getNameInNamespace();
 			    	
 			    	// Close the context when we're done
 				    ctx.close();
 				}
 				catch(Exception ex) {
					log.error("Failed to lookup DN of user: {}" + accountName, ex);
 				}
 				
 				return dn;
 			}
 		}); // end new PrivilegedAction<String>()
 		
 		return dn;
 	}
 
 	@Override
 	public X509Certificate getPublicCertificateFor(final String distinguishedName) throws Exception {
 		X509Certificate credentials = null;
 		
 		LoginContext ctx = new LoginContext("cmf.security.ldap.lookup");
 		ctx.login();
 		
 		credentials = Subject.doAs(ctx.getSubject(), new PrivilegedAction<X509Certificate>() {
 
 			@Override
 			public X509Certificate run() {
 				
 				X509Certificate cert = null;
 				
 				try {
 					Hashtable<String, Object> props = new Hashtable<String, Object>();
 					props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 					props.put(Context.PROVIDER_URL, ldapProviderUrl);
 					props.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");
 				
 				    /* Create initial context */
 				    DirContext ctx = new InitialDirContext(props);
 
 				    SearchControls controls = new SearchControls();
 				    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 				    controls.setDerefLinkFlag(true);
 				    
 				    String dn = distinguishedName.replaceAll(", ", ",");
 				    String filter = String.format("(&(objectCategory=person)(objectClass=user)(distinguishedName=%s))", dn);
 				    
 				    NamingEnumeration<SearchResult> results = ctx.search( 
 				    		"", 
 				    		filter, 
 				    		controls);
 				    
 				    SearchResult result = results.next();
 				    
 			    	log.debug("Name: {}", result.getName());
 			    	log.debug("NameInNamespace: {}", result.getNameInNamespace());
 			    	
 			    	Attributes attrs = result.getAttributes();
 			    	CertificateFactory factory = CertificateFactory.getInstance("X.509");
 			    	Attribute certAttr = attrs.get("userCertificate");
 			    	
 			    	if (null != certAttr && certAttr.size() > 0) {
 			    		ByteArrayInputStream input = new ByteArrayInputStream((byte[])certAttr.get());
 			    		cert = (X509Certificate)factory.generateCertificate(input);
 			    	
 			    		log.debug("Found certificate issued by: {}", cert.getIssuerDN().getName());
 			    	}
 				    // Close the context when we're done
 				    ctx.close();
 				}
 				catch(Exception ex) {
 					log.error("Failed to lookup certificate", ex);
 				}
 				
 				return cert;
 			}
 		});
 		
 		return credentials;
 	}
 }
