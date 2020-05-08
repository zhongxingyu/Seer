 /**
  * User: vlad
  * Date: 2/16/13
  * Time: 11:20 AM
  */
 
 
 import org.jboss.security.SimplePrincipal;
 import org.jboss.security.auth.callback.UsernamePasswordHandler;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import javax.security.auth.Subject;
 import javax.security.auth.kerberos.KerberosPrincipal;
 import javax.security.auth.login.AppConfigurationEntry;
 import javax.security.auth.login.Configuration;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.security.Principal;
 import java.security.acl.Group;
 import java.util.*;
 
 import static org.junit.Assert.assertTrue;
 
 public class FuzzrBoxModulesTest {
 
 
     private static Properties testVals;
 
     @BeforeClass
     public static void initialize() throws IOException
     {
         testVals = new Properties();
         InputStream is = new FileInputStream(
                 System.getProperty("user.home") + "/.krbTests/TVals.props");
         testVals.load(is);
 
 
 
     }
 
     @Before
     public void setUp()  {
         Configuration.setConfiguration(new TestConfig());
     }
 
     static class TestConfig extends Configuration
     {
 
 
         public void refresh()
         {
         }
 
         public AppConfigurationEntry[] getAppConfigurationEntry(String name)
         {
             AppConfigurationEntry[] entry = null;
             try
             {
                 Class[] parameterTypes = {};
                 Method m = getClass().getDeclaredMethod(name, parameterTypes);
                 Object[] args = {};
                 entry = (AppConfigurationEntry[]) m.invoke(this, args);
             }
             catch(Exception e)
             {
             }
             return entry;
         }
 
         AppConfigurationEntry[] innerKrbContext()
         {
 
             String name = "com.sun.security.auth.module.Krb5LoginModule";
             Map<String, String> options = new HashMap<String, String>();
 
             AppConfigurationEntry krbEntry = new AppConfigurationEntry(name,
                     AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
             return new AppConfigurationEntry[]{krbEntry};
         }
 
 
         AppConfigurationEntry[] KerberosLoginModule()
         {
 
 
             String newKrbModuleName = "com.dblfuzzr.jboss.auth.spi.KerberosLoginModule";
 
             HashMap<String, String> nKrbOptions = new HashMap<String, String>();
 
             nKrbOptions.put("password-stacking","useFirstPass");
             nKrbOptions.put("realm",testVals.getProperty("domain"));
             nKrbOptions.put("kdc"  ,testVals.getProperty("kdc"));
 
             AppConfigurationEntry nkrbModule = new AppConfigurationEntry(newKrbModuleName,
                     AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, nKrbOptions);
 
 
             AppConfigurationEntry[] entry = {nkrbModule};
             return entry;
 
         }
 
         AppConfigurationEntry[] DebugLoginModule()
         {
 
             String innerKrbModule = "com.dblfuzzr.jboss.auth.spi.KerberosLoginModule";
             Map<String, String> innerKrbOpts = new HashMap<String, String>();
 
             innerKrbOpts.put("password-stacking", "useFirstPass");
             innerKrbOpts.put("realm",testVals.getProperty("domain"));
             innerKrbOpts.put("kdc"  ,testVals.getProperty("kdc"));
 
             AppConfigurationEntry krbEntry = new AppConfigurationEntry(innerKrbModule,
                     AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, innerKrbOpts);
 
 
 
             String debugModuleCls = "com.dblfuzzr.jboss.auth.spi.DebugLoginModule";
 
             HashMap<String, String> nOptions = new HashMap<String, String>();
 
             nOptions.put("password-stacking", "useFirstPass");
             nOptions.put("allPass", "true");
 
             AppConfigurationEntry debugModule = new AppConfigurationEntry(debugModuleCls,
                     AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, nOptions);
 
 
             AppConfigurationEntry[] entry = {krbEntry,debugModule};
             return entry;
 
         }
 
 
     }
 
     @Test
     public void KerberosAuthNwoRealm () throws LoginException {
 
         String samaccount = testVals.get("samaccount").toString();
 
 
         UsernamePasswordHandler handler = new UsernamePasswordHandler(samaccount, testVals.get("pass"));
         LoginContext lc = new LoginContext("KerberosLoginModule", handler);
         lc.login();
 
         Set<Principal> principals = lc.getSubject().getPrincipals();
 
         String realm = testVals.getProperty("domain");
         KerberosPrincipal principal = new KerberosPrincipal(samaccount + "@" + realm);
         assertTrue("Principals contains sam account", principals.contains(principal));
 
         lc.logout();
     }
 
     @Test
     public void KerberosAuthN () throws LoginException {
 
         String samaccount = testVals.get("user").toString();
 
 
         UsernamePasswordHandler handler = new UsernamePasswordHandler(samaccount, testVals.get("pass"));
         LoginContext lc = new LoginContext("KerberosLoginModule", handler);
         lc.login();
 
         Set<Principal> principals = lc.getSubject().getPrincipals();
 
         assertTrue("Principals contains sam account", principals.contains(new KerberosPrincipal(samaccount)));
 
         lc.logout();
     }
 
 
     @Test
     public void KerberosAuthZ () throws LoginException {
 
         String samaccount = testVals.get("user").toString();
 
         UsernamePasswordHandler handler = new UsernamePasswordHandler(samaccount, testVals.get("pass"));
         LoginContext lc = new LoginContext("KerberosLoginModule", handler);
         lc.login();
 
         Set<Principal> principals = lc.getSubject().getPrincipals();
         Set<Group> groups = lc.getSubject().getPrincipals(Group.class);
 
         Iterator<Group> pIter = groups.iterator();
         pIter.next();
         Group roles = pIter.next();
 
         assertTrue("Contains Role #1", roles.isMember(new SimplePrincipal("valid-user")));
 
         assertTrue("Principals contains sam account", principals.contains(new KerberosPrincipal(samaccount)));
 
         lc.logout();
     }
 
     @Test(expected = LoginException.class)
     public void NegKerberosAuthN () throws LoginException {
 
         String samaccount = testVals.get("user").toString();
 
 
         UsernamePasswordHandler handler = new UsernamePasswordHandler(samaccount, testVals.get("pass") + "1");
         LoginContext lc = new LoginContext("KerberosLoginModule", handler);
         lc.login();
 
         Set<Principal> principals = lc.getSubject().getPrincipals();
         assertTrue("Principals contains sam account", principals.contains(new KerberosPrincipal(samaccount)));
         lc.logout();
 
     }
 
 
     @Test
     public void DebugAuthN () throws LoginException {
 
        String samaccount = testVals.get("samaccount").toString();
 
 
         UsernamePasswordHandler handler = new UsernamePasswordHandler(samaccount, testVals.get("pass"));
         LoginContext lc = new LoginContext("DebugLoginModule", handler);
         lc.login();
 
 
         Subject subject = lc.getSubject();
 
         Set<Group> groups = subject.getPrincipals(Group.class);
         assertTrue("Principals contains CN", subject.getPrincipals().contains(new KerberosPrincipal(samaccount)));
         assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
 
         Iterator<Group> pIter = groups.iterator();
         pIter.next();
 
         Group roles = pIter.next();
 
         assertTrue("Contains Role #1", roles.isMember(new SimplePrincipal("Managers")));
         assertTrue("Contains Role #2", roles.isMember(new SimplePrincipal("Operators")));
 
         lc.logout();
 
 
     }
 
 
 }
