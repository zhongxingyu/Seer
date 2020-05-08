 package test.nchelp.meteor;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.naming.NameNotFoundException;
 import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
 import junit.framework.TestCase;
 
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.LDAPDirectory;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.exception.DirectoryException;
 
 /**
  * @author kimw
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class LDAPDirectoryTest extends TestCase {
 
 	private static final String INST_NAME = "ED.TEST2";
 	private static final String INST_TYPE = Directory.TYPE_DATA_PROVIDER;
 	private static final String[] ALIASES = { "ED.TIM", "ED.BOB" };
 	private static DirContext sContext;
 	private static String baseDN;
 	private static String sFullInstName;
 	private static String sFullFileName;
 	private static String sFullFamilyName;
 	private static final String resourcePrefix = "directory.";
 	private static Hashtable env;
 
 	static {
 		Resource res = ResourceFactory.createResource("directory.properties");
 
 		String initialContextFactory =
 			res.getProperty(resourcePrefix + "initialcontextfactory",
 			"com.sun.jndi.ldap.LdapCtxFactory");
 		String providerURL =
 			res.getProperty(resourcePrefix + "providerurl");
 		baseDN = res.getProperty(resourcePrefix + "basedn");
 		String authType =
 			res.getProperty(resourcePrefix + "authentication.type");
 		String protocol = res.getProperty(resourcePrefix + "protocol");
 
 		env = new Hashtable();
 		env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
 		env.put(Context.PROVIDER_URL, providerURL);
 
 			
 		if ("ssl".equalsIgnoreCase(protocol)) {
 			// This is really the only supported protocol
 			env.put(Context.SECURITY_PROTOCOL, protocol);
 		}
 		else if("custom".equalsIgnoreCase(protocol)){
 			env.put(Context.SECURITY_PROTOCOL, "ssl");
 			env.put("java.naming.ldap.factory.socket", "org.nchelp.meteor.util.MeteorSSLSocketFactory");
 		}
 
 		env.put(Context.SECURITY_AUTHENTICATION, authType);
 		
 		// go get the user id and password from the properties file named:
 		// meteor.build.properties located in your home directory
 		String home = System.getProperty("user.home");
 		home = home.replace('\\', '/');		
 		Resource securityResource = null;
 		try {
			securityResource = new Resource(new URL("file", "localhost", home + "/meteor.build.properties"));
 		}
 		catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
		env.put(Context.SECURITY_PRINCIPAL, securityResource.get("ldap.security.principal"));
		env.put(Context.SECURITY_CREDENTIALS, securityResource.get("ldap.security.credentials"));
 
 		try {
 			sContext = new InitialDirContext(env);		
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		sFullInstName = "Institution=" + INST_NAME + "," + baseDN;	
 		sFullFamilyName = "FileTypeFamily=Meteor," + sFullInstName;
 		sFullFileName = "File=" + Directory.TYPE_DATA_PROVIDER + "," + sFullFamilyName;
 	}
 
 
 	/**
 	 * Constructor for LDAPDirectoryTest.
 	 * @param arg0
 	 */
 	public LDAPDirectoryTest(String arg0) {
 		super(arg0);
 	}
 
 	public void testGettingAliases() {
 		try {
 			LDAPDirectory directory = new LDAPDirectory();
 			
 			// remove institution (if it exists)
 			removeInstitution();
 			try {
 				directory.getAliases(INST_NAME, INST_TYPE).size();
 				fail("Test should have failed - institution not found");
 			}
 			catch (DirectoryException e) {
 				// this should happen - the institution does not exist
 			}
 			
 			// add institution with no aliases
 			addInstitution();
 			try {
 				directory.getAliases(INST_NAME, INST_TYPE);
 				fail("Test should have failed - inst aliases not found");
 			}
 			catch (DirectoryException e) {
 				// to do
 			}
 			
 			// add aliases
 			addAliases();
 		 	List aliases = directory.getAliases(INST_NAME, INST_TYPE);
 			assertEquals(aliases.size(), ALIASES.length);
 			assertEquals(aliases.get(0), LDAPDirectoryTest.ALIASES[0]);
 			assertEquals(aliases.get(1), LDAPDirectoryTest.ALIASES[1]);
 			
 			// remove institution
 			removeInstitution();
 			try {
 				directory.getAliases(INST_NAME, INST_TYPE).size();
 				fail("Test should have failed - institution not found");
 			}
 			catch (DirectoryException e) {
 				// this should happen - the institution does not exist
 			}
 		}
 		catch (Throwable t) {
 			t.printStackTrace();
 			try {
 				removeInstitution();
 			}
 			catch (NamingException e) {
 				e.printStackTrace();
 			}
 			fail(t.getMessage());
 		}
 	}
 
 	private void removeInstitution() throws NamingException {
 		try {
 			sContext.destroySubcontext(sFullFileName);
 			sContext.destroySubcontext(sFullFamilyName);
 			sContext.destroySubcontext(sFullInstName);
 		}
 		catch (NameNotFoundException e) {
 			System.out.println("institution not found to remove");
 		}
 	}
 	
 	private void addInstitution() throws NamingException {
 		
 		Attribute objclass = new BasicAttribute("objectclass");
 		objclass.add("Institution");
 		
 		Attributes attrs = new BasicAttributes();
 		attrs.put(new BasicAttribute("Description", "Description"));
 		attrs.put(new BasicAttribute("Encryption", "SSL"));
 		attrs.put(new BasicAttribute("FileTypeFamily", "Meteor"));
 		attrs.put(new BasicAttribute("Transport", "HPCP"));
 
 		attrs.put(objclass);
 
 		sContext.createSubcontext(sFullInstName, attrs);		
 		
 		addFileTypeFamily();
 	}
 	
 	private void addFileTypeFamily() throws NamingException {
 
 		Attribute objclass = new BasicAttribute("objectclass");
 		objclass.add("Meteor");
 		
 		StringBuffer contactDN = new StringBuffer("person=");
 		contactDN.append("person=meteorcontact");
 		contactDN.append(sFullInstName);
 
 		Attributes attrs = new BasicAttributes();
 		attrs.put(new BasicAttribute("Contact", contactDN.toString()));
 
 		attrs.put(objclass);
 
 		sContext.createSubcontext(sFullFamilyName, attrs);
 		
 		addFile();
 	}
 	
 	private void addFile() throws NamingException {
 
 		Attribute objclass = new BasicAttribute("objectclass");
 		objclass.add("File");
 
 		Attributes attrs = new BasicAttributes();
 		attrs.put(new BasicAttribute("Ver", "1"));
 		attrs.put(new BasicAttribute("Status", "AC"));
 		attrs.put(new BasicAttribute("PreferredEncryption", "SSL=SSL1,Encryption=SSL,Institution=" + INST_NAME + "," + baseDN));
 		attrs.put(new BasicAttribute("PreferredTransport", "HPCP=HPCP2,Transport=HPCP,Institution=" + INST_NAME + "," + baseDN));
 
 		attrs.put(objclass);
 
 		sContext.createSubcontext(sFullFileName, attrs);
 	}
 	
 	private void addAliases() throws NamingException {
 		ModificationItem[] mods = new ModificationItem[ALIASES.length];		
 		for (int i = 0; i < ALIASES.length; i++) {
 			mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE, 
 				new BasicAttribute("Alias", ALIASES[i]));
 		}			
 		sContext.modifyAttributes(sFullFileName, mods);
 	}
 }
