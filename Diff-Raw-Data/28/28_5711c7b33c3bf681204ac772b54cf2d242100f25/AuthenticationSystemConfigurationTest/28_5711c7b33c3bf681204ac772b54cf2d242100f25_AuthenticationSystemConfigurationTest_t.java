 package edu.northwestern.bioinformatics.studycalendar.security;
 
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AbstractAuthenticationSystem;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemInitializationFailure;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemLoadingFailure;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.local.LocalAuthenticationSystem;
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
 import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertContains;
 import static org.easymock.EasyMock.notNull;
 import static org.easymock.classextension.EasyMock.expect;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.springframework.osgi.mock.MockBundle;
 import org.springframework.osgi.mock.MockServiceReference;
 
 import javax.sql.DataSource;
 import java.util.Dictionary;
 
 /**
  * @author Rhett Sutphin
  */
 public class AuthenticationSystemConfigurationTest extends AuthenticationTestCase {
     private AuthenticationSystemConfiguration configuration;
     private BundleContext mockBundleContext;
 
     private final MockPlugin localPlugin
         = new MockPlugin(
             "edu.northwestern.bioinformatics.psc-authentication-local-plugin",
             new LocalAuthenticationSystem());
     private final MockPlugin testablePlugin
         = new MockPlugin(
             "edu.northwestern.bioinformatics.psc-authentication-testable-plugin",
             new TestableAuthenticationSystem());
     private final MockPlugin stubPlugin
         = new MockPlugin(
             "edu.northwestern.bioinformatics.psc-authentication-stub-plugin",
             new StubAuthenticationSystem());
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         userDetailsService.addUser("alice");
         userDetailsService.addUser("barbara");
         mockBundleContext = registerMockFor(BundleContext.class);
 
         expectSingleService(DataSource.class, dataSource);
         expectSingleService(PscUserDetailsService.class, userDetailsService);
 
         configuration = new AuthenticationSystemConfiguration();
         configuration.setBundleContext(mockBundleContext);
     }
 
     private <T> void expectSingleService(Class<T> serviceClass, T instance) {
         ServiceReference dsRef = new MockServiceReference(new String[] { serviceClass.getName() });
         expect(mockBundleContext.getServiceReference(serviceClass.getName())).andReturn(dsRef);
         expect(mockBundleContext.getService(dsRef)).andReturn(instance);
     }
 
     private void expectServiceReferences(MockPlugin... plugins) throws InvalidSyntaxException {
         ServiceReference[] refs = new ServiceReference[plugins.length];
         for (int i = 0; i < plugins.length; i++) refs[i] = plugins[i].getServiceReference();
         expect(mockBundleContext.getServiceReferences(AuthenticationSystem.class.getName(), null)).andStubReturn(refs);
     }
 
     private void expectOsgiSelectedServiceReferenceIs(MockPlugin plugin) {
         expect(mockBundleContext.getServiceReference(AuthenticationSystem.class.getName())).
             andStubReturn(plugin.getServiceReference());
     }
 
     private void expectSelectedService(MockPlugin plugin) {
         expect(mockBundleContext.getService(plugin.getServiceReference())).andReturn(plugin.getSystem());
         // these are side effects of using an OsgiBundleApplicationContext
         expect(mockBundleContext.getBundle()).andReturn(plugin.getBundle()).anyTimes();
         expect(mockBundleContext.registerService((String[]) notNull(), notNull(), (Dictionary<?, ?>) notNull())).
             andReturn(null).anyTimes();
     }
 
     private void expectUngetService(MockPlugin plugin) {
         expect(mockBundleContext.ungetService(plugin.getServiceReference())).andReturn(true);
     }
 
     public void testUnknownSelectedSystemSelectsDefault() throws Exception {
        expectServiceReferences(testablePlugin, localPlugin);
         expectSelectedService(localPlugin);
         replayMocks();
 
         selectAuthenticationSystem(stubPlugin.getSymbolicName());
         assertEquals("Wrong service selected", localPlugin.getSystem().name(),
             configuration.getAuthenticationSystem().name());
     }
 
     public void testFailureIfNoAuthenticationSystemsAvailable() throws Exception {
        expect(mockBundleContext.getServiceReferences(AuthenticationSystem.class.getName(), null)).
            andStubReturn(null);
         replayMocks();
 
         try {
             configuration.getAuthenticationSystem();
             fail("Exception not thrown");
         } catch (AuthenticationSystemLoadingFailure failure) {
             assertEquals(
                 "No authentication system plugins available from the OSGi layer.  Plugins must be both installed and activated to be used.",
                 failure.getMessage());
         }
     }
 
     public void testPropertiesReflectConfiguredAuthenticationSystem() throws Exception {
        expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
         expectSelectedService(localPlugin);
         replayMocks();
 
         assertNotNull("No properties for local", configuration.getProperties());
         assertEquals("Wrong number of properties for local", 1, configuration.getProperties().size());
         assertContains("Sole property for local should be auth system", configuration.getProperties().getAll(), AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
 
         resetMocks();
         expectServiceReferences(localPlugin, testablePlugin);
         expectSelectedService(testablePlugin);
         replayMocks();
         
         setMinimumTestSystemParameters();
         selectAuthenticationSystem(testablePlugin.getSymbolicName());
         assertNotNull("No properties for TAS", configuration.getProperties());
         assertNotNull("TAS props should contain auth system",
             configuration.getProperties().get(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM.getKey()));
         assertNotNull("TAS props should contain TAS system props",
             configuration.getProperties().get(TestableAuthenticationSystem.APPLICATION_URL.getKey()));
         assertEquals("Wrong number of properties for TAS: " + configuration.getProperties(),
             3, configuration.getProperties().size());
         // TODO: not sure if this is still a requirement
         // assertEquals("Property details should be available for per-system props",
         //    "Service URL", configuration.getProperties().get(TestableAuthenticationSystem.SERVICE_URL.getKey()).getName());
     }
 
     public void testCreateDefaultAuthenticationSystem() throws Exception {
        expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
         expectSelectedService(localPlugin);
         replayMocks();
 
         AuthenticationSystem system = configuration.getAuthenticationSystem();
         assertNotNull("No system created", system);
         assertEquals("Wrong system created", localPlugin.getSystem().name(), system.name());
     }
 
     public void testCreateNewAuthenticationSystemAfterSystemSwitch() throws Exception {
        expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
         expectSelectedService(localPlugin);
         replayMocks();
 
         AuthenticationSystem initialSystem = configuration.getAuthenticationSystem();
         assertNotNull("No system created initially", initialSystem);
 
         resetMocks();
         expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
         expectUngetService(localPlugin);
         expectSelectedService(stubPlugin);
         replayMocks();
         
         setMinimumTestSystemParameters();
         selectAuthenticationSystem(stubPlugin.getSymbolicName());
         AuthenticationSystem newSystem = configuration.getAuthenticationSystem();
         assertNotSame("New system is not new", initialSystem, newSystem);
         assertEquals("Wrong new system created", "stub", newSystem.name());
         verifyMocks();
     }
 
     public void testCreatedAuthenticationSystemIsInitialized() throws Exception {
         expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
         expectSelectedService(stubPlugin);
         replayMocks();
 
         selectAuthenticationSystem(stubPlugin.getSymbolicName());
 
         assertEquals("System is wrong kind", "stub", configuration.getAuthenticationSystem().name());
     }
     
     public void testInitializationExceptionIsPropagated() throws Exception {
         expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
         expectSelectedService(stubPlugin);
         replayMocks();
 
         selectAuthenticationSystem(stubPlugin.getSymbolicName());
         configuration.updated(new MapBuilder<String, Object>().
             put(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM.getKey(), stubPlugin.getSymbolicName()).
             put(StubAuthenticationSystem.EXPECTED_INITIALIZATION_ERROR_MESSAGE.getKey(), "Bad news").
             toDictionary());
 
         try {
             configuration.getAuthenticationSystem();
             fail("Exception not thrown");
         } catch (AuthenticationSystemInitializationFailure asif) {
             assertEquals("Bad news", asif.getMessage());
         }
     }
     
     private void setMinimumTestSystemParameters() {
         configuration.updated(
             new MapBuilder<String, Object>().
                 put(TestableAuthenticationSystem.SERVICE_URL.getKey(), "not-null").
                 put(TestableAuthenticationSystem.APPLICATION_URL.getKey(), "not-null").
                 toDictionary()
         );
     }
 
     private void selectAuthenticationSystem(String requested) {
         configuration.updated(new MapBuilder<String, Object>().
             put(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM.getKey(), requested).
             toDictionary());
     }
 
     private class MockPlugin {
         private final String symbolicName;
         private Bundle bundle;
         private ServiceReference serviceReference;
 
         private AuthenticationSystem system;
 
         private MockPlugin(final String symbolicName, AuthenticationSystem instance) {
             this.symbolicName = symbolicName;
             bundle = new MockBundle() {
                 @Override public String getSymbolicName() { return symbolicName; }
             };
             serviceReference = new MockServiceReference(bundle) {
                 @Override
                 public String toString() {
                     return new StringBuilder().append("MockServiceReference")
                         .append('@').append(Integer.toHexString(System.identityHashCode(this)))
                         .append("[bundle=").append(getBundle().getSymbolicName()).append(']')
                         .toString();
                 }
             };
             system = instance;
         }
 
         public String getSymbolicName() {
             return symbolicName;
         }
 
         public Bundle getBundle() {
             return bundle;
         }
 
         public ServiceReference getServiceReference() {
             return serviceReference;
         }
 
         public AuthenticationSystem getSystem() {
             if (system instanceof AbstractAuthenticationSystem) {
                 ((AbstractAuthenticationSystem) system).setBundleContext(mockBundleContext);
             }
             return system;
         }
     }
 }
