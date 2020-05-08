 package org.ow2.chameleon.rose.supervisor.it;
 
 import static org.apache.felix.ipojo.ComponentInstance.INVALID;
 import static org.apache.felix.ipojo.ComponentInstance.VALID;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyNoMoreInteractions;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.ops4j.pax.exam.CoreOptions.felix;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.options;
 import static org.ops4j.pax.exam.CoreOptions.provision;
 import static org.osgi.framework.Constants.OBJECTCLASS;
 import static org.ow2.chameleon.rose.supervisor.it.ITTools.waitForIt;
 
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import org.apache.felix.ipojo.ComponentInstance;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.ops4j.pax.exam.Inject;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.OptionUtils;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.ops4j.pax.exam.junit.JUnitOptions;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.device.Device;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.ExportReference;
 import org.osgi.service.remoteserviceadmin.ExportRegistration;
 import org.ow2.chameleon.rose.ExporterService;
 import org.ow2.chameleon.rose.introspect.ExportSupervisorIntrospection;
 import org.ow2.chameleon.testing.helpers.IPOJOHelper;
 import org.ow2.chameleon.testing.helpers.OSGiHelper;
 
 @RunWith(JUnit4TestRunner.class)
 public class ExportSupervisorTest {
     private static final String EXPORT_SUPERVISOR_FACTORY = "rose.export.supervisor";
 
     /*
      * Number of mock object by test.
      */
     private static final int MAX_MOCK = 10;
 
     //Properties used to track the service to be exported
     private static final String EXPORT_PROPERTY = "export.service";
 
     @Inject
     private BundleContext context;
 
     private OSGiHelper osgi;
     
     private IPOJOHelper ipojo;
     
     @Mock private ExporterService exporter; //Mock ExporterService
     @Mock private ExportRegistration expreg; //Mock export registration
     @Mock private ExportReference expref; //Mock ExportReference
 
     @Before
     public void setUp() {
         osgi = new OSGiHelper(context);
         ipojo = new IPOJOHelper(context);
         
         //initialise the annoted mock object
         initMocks(this);
     }
 
     @After
     public void tearDown() {
         osgi.dispose();
        ipojo.dispose();
     }
 
     @Configuration
     public static Option[] configure() {
         Option[] platform = options(felix());
 
         Option[] bundles = options(provision(
         		mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo").versionAsInProject(),
                 mavenBundle().groupId("org.ow2.chameleon.testing").artifactId("osgi-helpers").versionAsInProject(), 
                 mavenBundle().groupId("org.ow2.chameleon.rose").artifactId("rose-core").versionAsInProject(), 
                 mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").versionAsInProject(),
                 mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").versionAsInProject(),
                 mavenBundle().groupId("org.slf4j").artifactId("slf4j-simple").versionAsInProject(),
                 // The target
                 mavenBundle().groupId("org.ow2.chameleon.rose.supervisor").artifactId("export-supervisor").versionAsInProject())); 
 
         Option[] r = OptionUtils.combine(platform, bundles);
 
         return r;
     }
 
     /**
      * Mockito bundles
      */
     @Configuration
     public static Option[] mockitoBundle() {
         return options(JUnitOptions.mockitoBundles());
     }
 
     /**
      * Configure some test bundles
      * @return
      */
 /*    @Configuration
     public static Option[] mockBundles() {
 
         return options(provision(newBundle().set(Constants.BUNDLE_SYMBOLICNAME, REMOTE_SERVICE_TRACKER_BUNDLE).set(Constants.BUNDLE_NAME,
                 REMOTE_SERVICE_TRACKER_BUNDLE).add(RemoteServiceTracker.class).build(withBnd())));
     }*/
 
     /**
      * Test if the factory is valid and able to create instances.
      */
     @Test
     public void testInstanceCreation() {
     	ComponentInstance instance = createInstance();
     	
 		//The instance must be invalid since there is no ExporterService
 		assertEquals(INVALID, instance.getState()); 
 		
 		//register an exporter service
 		ServiceRegistration reg = registerExporterService();
 		
 		waitForIt(200);
 		
 		//The instance must be valid since there is an ExporterService ;)
 		assertEquals(VALID, instance.getState()); 
 		
 		//Goodbye ExporterService
 		reg.unregister();
     }
 
     /**
      * Test if the ExporterService is called once we track a service which require to 
      * be exported.
      */
     @Test 
     public void testServiceExport(){
     	ComponentInstance instance = createInstance();
     	registerExporterService();
     	
     	//register a mock lock service which must be exported
     	ServiceRegistration reg = createAndRegisterServiceToBeExported(LogService.class);
     	
     	waitForIt(200);
     	
     	//Check is the exported has been successfully called
     	verify(exporter).exportService(reg.getReference(), null);
     	
     	//Unregister the tracked service
     	reg.unregister();
     	waitForIt(200);
     	
     	//Check that the instance is still valid
     	assertEquals(VALID, instance.getState());
     	
     	//dispose the instance
     	instance.dispose();
     	
     	//getConfigPrefix is called by the supervisor in order to set the config property
     	verify(exporter).getConfigPrefix();
     	
     	//Check that there is no unexpected behavior
     	verifyNoMoreInteractions(exporter);
     }
     
     /**
      * Test if the ExporterService is called for each service which require to 
      * be exported.
      */
     @Test 
     public void testMultipleServiceExport(){
     	ComponentInstance instance = createInstance();
     	registerExporterService();
     	
     	Collection<ServiceRegistration> sregs = new HashSet<ServiceRegistration>();
     	
     	//register MAX_MOCK mock lock service which must be exported
     	for (int i = 0; i < MAX_MOCK; i++) {
 			sregs.add(createAndRegisterServiceToBeExported(LogService.class));
 		}
     	
     	waitForIt(200);
     	
     	//Check is the exported has been successfully called
     	for (ServiceRegistration reg : sregs) {
     		verify(exporter).exportService(reg.getReference(), null);
     		
     		//Unregister the tracked service
     		reg.unregister();
 		}
     	
     	waitForIt(200);
     	
     	//Check that the instance is still valid
     	assertEquals(VALID, instance.getState());
     	
     	//dispose the instance
     	instance.dispose();
     	
     	//getConfigPrefix has been called by the supervisor in order to set the config property
     	verify(exporter).getConfigPrefix();
     	
     	//Check that there is no unexpected behavior
     	verifyNoMoreInteractions(exporter);
     }
     
     /**
      * Test that the ExportRegistration is closed once the service has been unregistered
      */
     @Test 
     public void testServiceExportAndClosed(){
     	//register an ExporterService
     	registerExporterService();
     	
     	//Register a mock log service which must be exported
     	ServiceRegistration reg = createAndRegisterServiceToBeExported(Device.class);
 
     	//define the behavior of the exporter
     	when(exporter.exportService(reg.getReference(), null)).thenReturn(expreg);
 
     	//create the export-supervisor instance
     	ComponentInstance instance = createInstance();
     	
     	waitForIt(200);
     	
     	//unregister the service which is exported
     	reg.unregister();
     	
     	waitForIt(200);
     	
     	//Check that the ExportRegistration has been closed
     	verify(expreg).close();
     	
     	//dispose the instance
     	instance.dispose();
     }
     
     /**
 	 * Test that the ExportRegistration is closed once the service has been unregistered. (Multiple version)
      */
     @Test 
     public void testMultipleServiceExportAndClosed(){
     	//register an ExporterService
     	registerExporterService();    
     	
     	Collection<ServiceRegistration> sregs = new HashSet<ServiceRegistration>();
     	
     	//register MAX_MOCK mock lock service which must be exported
     	for (int i = 0; i < MAX_MOCK; i++) {
 			sregs.add(createAndRegisterServiceToBeExported(LogService.class));
 		}
     	
     	waitForIt(200);
     	
     	//Check is the exported has been successfully called
     	for (ServiceRegistration reg : sregs) {
     		//define the behavior of the exporter
         	when(exporter.exportService(reg.getReference(), null)).thenReturn(expreg);
 		}
     	
     	//create the export-supervisor instance
     	ComponentInstance instance = createInstance();
 
     	//unregister services which are exported
     	for (ServiceRegistration reg : sregs) {
     		reg.unregister();
     	}
     	
     	waitForIt(200);
     	
     	//Check that the ExportRegistration has been closed MAX_MOCK times
     	verify(expreg,times(MAX_MOCK)).close();
     	
     	//dispose the instance
     	instance.dispose();
     }
     
     /**
      * Test that the ExportRegistration is closed once the export-supervisor has been disposed.
      */
     @Test 
     public void testServiceUnExportAfterDispose(){
     	//register an ExporterService
     	registerExporterService();
     	
     	//Register a mock device service which must be exported
     	ServiceRegistration reg = createAndRegisterServiceToBeExported(Device.class);
 
     	//define the behavior of the exporter
     	when(exporter.exportService(reg.getReference(), null)).thenReturn(expreg);
 
     	//create the export-supervisor instance
     	ComponentInstance instance = createInstance();
     	
     	waitForIt(200);
     	
     	//unregister the service which is exported
     	instance.dispose();
     	
     	waitForIt(200);
     	
     	//Check that the ExportRegistration has been closed
     	verify(expreg).close();
     }
     
     /**
 	 * Test that the ExportRegistration is closed once the export-supervisor has been disposed. (Multiple version)
      */
     @Test 
     public void testMultipleServiceUnExportAfterDispose(){
     	//register an ExporterService
     	registerExporterService();    
     	
     	Collection<ServiceRegistration> sregs = new HashSet<ServiceRegistration>();
     	
     	//register MAX_MOCK mock lock service which must be exported
     	for (int i = 0; i < MAX_MOCK; i++) {
 			sregs.add(createAndRegisterServiceToBeExported(LogService.class));
 		}
     	
     	waitForIt(200);
     	
     	//Check is the exported has been successfully called
     	for (ServiceRegistration reg : sregs) {
     		//define the behavior of the exporter
         	when(exporter.exportService(reg.getReference(), null)).thenReturn(expreg);
 		}
     	
     	//create the export-supervisor instance
     	ComponentInstance instance = createInstance();
 
     	waitForIt(200);
     	
     	//dispose the instance
     	instance.dispose();
     	
     	//Check that the ExportRegistration has been closed MAX_MOCK times
     	verify(expreg,times(MAX_MOCK)).close();
     	
     	//dispose the instance
     	instance.dispose();
     }
     
     /**
      * Test dynamic change of the export.filter property.
      */
     @Test
     public void testDynamicFilterChange(){
     	registerExporterService();
     	
     	//Register a mock log service which must be exported
     	ServiceRegistration regLog = createAndRegisterServiceToBeExported(LogService.class);
     	
     	//Register a mock device service 
     	ServiceRegistration regDev = context.registerService(Device.class.getName(), mock(Device.class), null);
     	
     	//define the behavior of the exporter
     	when(exporter.exportService(regLog.getReference(), null)).thenReturn(expreg);
     	when(exporter.exportService(regDev.getReference(), null)).thenReturn(expreg);
     	
     	
     	ComponentInstance instance = createInstance();
     	
     	waitForIt(200);
     	
     	//Verify that the log service has been exported
     	verify(exporter).exportService(regLog.getReference(), null);
     	
     	//getConfigPrefix has been called by the supervisor in order to set the config property
     	verify(exporter).getConfigPrefix();
     	
     	//Check that only the LogService has been exported
     	verifyNoMoreInteractions(exporter);
     	
     	//change the export.filter property, export only the Device service
     	Dictionary<String, String> dico = new Hashtable<String, String>();
     	dico.put("export.filter", "("+OBJECTCLASS+"="+Device.class.getName()+")");
     	instance.reconfigure(dico);
     	
     	waitForIt(200);
     	
     	//Check that the device service has been exported
     	verify(exporter).exportService(regDev.getReference(), null);
     	
     	//getConfigPrefix has been called by the supervisor in order to set the config property
     	verify(exporter).getConfigPrefix();
     	
     	//Check that only the Device has been exported
     	verifyNoMoreInteractions(exporter);
     	
     	//Check that the export registration has been closed, due to the fact that the Log Service is no more tracked.
     	verify(expreg).close();
     
     	//Dispose the instance
     	instance.dispose();
     }
     
     /**
      * Test dynamic change of the export.filter property.
      */
 	@Test
     public void testIntrospectionService(){
     	
     	registerExporterService();
     	
     	//Export one service
     	ServiceRegistration reg = createAndRegisterServiceToBeExported(LogService.class);
     	
     	//define the behavior of the exporter and registration
     	when(exporter.exportService(reg.getReference(), null)).thenReturn(expreg);
     	when(expreg.getExportReference()).thenReturn(expref);
     	
     	//create the instance
     	ComponentInstance instance = createInstance();
     	
     	waitForIt(200);
     	
     	//Get the introspection service
     	ExportSupervisorIntrospection service = (ExportSupervisorIntrospection) osgi.getServiceObject(ExportSupervisorIntrospection.class.getName(), "(instance.name="+instance.getInstanceName()+")");
     	
     	//Check that the service has been provided
     	assertNotNull(service);
     	
     	waitForIt(200);
     	
     	//Verify the export
     	verify(exporter).exportService(reg.getReference(),null);
     	
     	//Check getAllExportReference have one element
     	assertEquals(1,service.getAllExportReference().size());
     	
     	//Check getExportReference
     	assertEquals(expref, service.getExportReference(reg.getReference()));
     	
     	//Unregister the exported service
     	reg.unregister();
     	
     	waitForIt(200);
     	
     	//Check getAllExportReference is now empty
     	assertEquals(0,service.getAllExportReference().size());
     	
     	//dispose the instance
     	instance.dispose();
     }
     
     
     private ComponentInstance createInstance(){
     	Dictionary<String, String> properties = new Hashtable<String, String>();
 		properties.put("export.filter", "("+EXPORT_PROPERTY+"=true)");
 		ComponentInstance instance = null;
 		try {
 			instance = ipojo.createComponentInstance(
 					EXPORT_SUPERVISOR_FACTORY, properties);
 		} catch (Exception e) {
 			fail("Unable to create an export-supervisor instance, "+e.getMessage());
 		}
 		
 		return instance;
     }
     
     private ServiceRegistration registerExporterService(){
     	return context.registerService(ExporterService.class.getName(), exporter, null);
     }
     
     private ServiceRegistration createAndRegisterServiceToBeExported(Class<?> clazz){
     	return createAndRegisterServiceToBeExported(clazz, EXPORT_PROPERTY);
     }
     
     private ServiceRegistration createAndRegisterServiceToBeExported(Class<?> clazz,String exportProperty){
     	Dictionary<String, Object> properties = new Hashtable<String, Object>();
     	properties.put(exportProperty, true);
     	Object service = mock(clazz);
     	return context.registerService(clazz.getName(), service , properties);
     }
 }
 
