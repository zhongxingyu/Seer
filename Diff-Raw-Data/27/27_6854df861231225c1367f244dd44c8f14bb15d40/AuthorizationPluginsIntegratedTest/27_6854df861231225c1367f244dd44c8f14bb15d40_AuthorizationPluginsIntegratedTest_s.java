 package edu.northwestern.bioinformatics.studycalendar.osgi.authorization;
 
 import edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestCase;
 import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.DefaultMembrane;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.dao.UserSearchCriteria;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.osgi.framework.ServiceReference;
 
 import java.io.IOException;
 
 import static edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestHelper.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Rhett Sutphin
  */
 @RunWith(JUnit4.class)
 public class AuthorizationPluginsIntegratedTest extends OsgiLayerIntegratedTestCase {
     private static final String MOCK_PLUGIN_SYMBOLIC_NAME =
         "edu.northwestern.bioinformatics.psc-authorization-mock-plugin";
     private static final String SOCKET_SYMBOLIC_NAME =
         "edu.northwestern.bioinformatics.psc-authorization-socket";
 
     @Before
     public void before() throws Exception {

     }
 
     @Test
     public void pluginLayerGivesTheCsmAuthorizationManagerByDefault() throws Exception {
         assertThat(getCurrentAuthorizationManager().toString(),
             containsString("gov.nih.nci.security.provisioning.AuthorizationManagerImpl"));
     }
 
     @Test
     public void pluginLayerGivesAnAuthorizationSocketIfAnAuthorizationPluginIsAvailable() throws Exception {
         startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
         waitForService(SOCKET_SYMBOLIC_NAME, AuthorizationManager.class.getName());
 
         assertThat(getCurrentAuthorizationManager().getClass().getName(),
             is("gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.SuiteAuthorizationSocket"));
     }
 
     @Test
     public void pluginLayerGivesTheCsmAuthorizationManagerIfAllAuthorizationPluginsAreDisabled() throws Exception {
         startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
         stopBundle(MOCK_PLUGIN_SYMBOLIC_NAME);
 
         assertThat(getCurrentAuthorizationManager().toString(),
             containsString("gov.nih.nci.security.provisioning.AuthorizationManagerImpl"));
     }
 
     @Test
     public void anAuthorizationPluginCanBeQueriedForUsers() throws Exception {
         startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
         waitForService(SOCKET_SYMBOLIC_NAME, AuthorizationManager.class.getName());
 
         assertThat(
             getWrappedAuthorizationManager().getObjects(new UserSearchCriteria(new User())).size(),
             is(1));
     }
 
     private Object getCurrentAuthorizationManager() throws IOException {
         ServiceReference sr = getBundleContext().
             getServiceReference(AuthorizationManager.class.getName());
         assertThat("No AuthorizationManager registered", sr, is(not(nullValue())));
 
         Object service = getBundleContext().getService(sr);
         assertThat("AuthorizationManager ref not resolvable", service, is(not(nullValue())));
         return service;
     }
 
     private AuthorizationManager getWrappedAuthorizationManager() throws IOException {
         return (AuthorizationManager) createCsmMembrane().farToNear(getCurrentAuthorizationManager());
     }
 
     private DefaultMembrane createCsmMembrane() {
         return new DefaultMembrane(getClass().getClassLoader(),
             "gov.nih.nci.security",
             "gov.nih.nci.security.dao",
             "gov.nih.nci.security.exceptions",
             "gov.nih.nci.security.authorization.domainobjects",
             "gov.nih.nci.security.authorization.jaas"
         );
     }
 }
