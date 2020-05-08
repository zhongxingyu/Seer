 package org.ow2.chameleon.rose.testing;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.ops4j.pax.exam.Inject;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.OptionUtils;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.JUnitOptions;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.ExportReference;
 import org.osgi.service.remoteserviceadmin.ExportRegistration;
 import org.ow2.chameleon.rose.ExporterService;
 import org.ow2.chameleon.rose.introspect.ExporterIntrospection;
 import org.ow2.chameleon.testing.helpers.IPOJOHelper;
 import org.ow2.chameleon.testing.helpers.OSGiHelper;
 
 import java.util.Map;
 
 import static junit.framework.Assert.*;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.ops4j.pax.exam.CoreOptions.*;
 import static org.osgi.service.log.LogService.LOG_WARNING;
 import static org.ow2.chameleon.rose.testing.RoSeHelper.waitForIt;
 
 public abstract class ExporterComponentAbstractTest {
 	
 	protected static String HTTP_PORT = "9027";
 	
     /*
      * Number of mock object by test.
      */
 	protected static final int MAX_MOCK = 10;
 
     @Inject
     protected BundleContext context;
 
     protected OSGiHelper osgi;
     
     protected IPOJOHelper ipojo;
     
     protected RoSeHelper rose;
     
 	@Mock private LogService logService; //Mock LogService
     //@Mock private Device device; //Mock Device
 
     /**
      * Done some initializations.
      */
     @Before
     public void setUp() {
         osgi = new OSGiHelper(context);
         ipojo = new IPOJOHelper(context);
         rose = new RoSeHelper(context);
         
         //initialise the annoted mock object
         initMocks(this);
     }
 
     /**
      * Closing the test.
      */
     @After
     public void tearDown() {
         osgi.dispose();
         ipojo.dispose();
     }
     
     @Configuration
     public static Option[] configure() {
         Option[] platform = options(felix(),systemProperty( "org.osgi.service.http.port" ).value( HTTP_PORT ));
 
         Option[] bundles = options(provision(
                 mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo").versionAsInProject(),
                 mavenBundle().groupId("org.ow2.chameleon.testing").artifactId("osgi-helpers").versionAsInProject(),
                 mavenBundle().groupId("org.ow2.chameleon.rose.testing").artifactId("rose-helpers").versionAsInProject(),
                 mavenBundle().groupId("org.ow2.chameleon.rose").artifactId("rose-core").versionAsInProject(), 
                 mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").versionAsInProject(), 
                 mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").versionAsInProject(),
 				mavenBundle().groupId("org.slf4j").artifactId("slf4j-simple").versionAsInProject()
                 )); 
 
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
      * Basic Test, in order to know if the {@link ExporterService} service is correctly provided.
      */
     @Test
     public void testAvailability() {
     	//wait for the service to be available.
         waitForIt(100);
         
         ExporterService exporter = getExporterService(); //Get the ExporterService 
         
         assertNotNull(exporter); //Check that the exporter != null
     }
 
     /**
      * Test the {@link ExporterService#exportService(ServiceReference, Map)} with 
      * a valid {@link ServiceReference}.
      */
     @Test
     public void testExportService() {
         //wait for the service to be available.
         waitForIt(100);
         
         ExporterService exporter = getExporterService(); //get the service
         
         //Register a mock LogService
         ServiceRegistration regLog = rose.registerService(logService,LogService.class);
         
         //export the logService 
         ExportRegistration xreg = exporter.exportService(regLog.getReference(), null);
         
         //check that xreg is not null
         assertNotNull(xreg); 
         
         //check that there is no exception
         assertNull(xreg.getException());
         
         //check that the export reference is not null
         assertNotNull(xreg.getExportReference());
         
         //check that the ServiceReference is equal to the logService one
         assertEquals(regLog.getReference(), xreg.getExportReference().getExportedService());
         
         //Check that the ExportReference has been published
         ExportReference xref = rose.getServiceObject(ExportReference.class);
         
         //Check that the published ExportReference is equal to the ExportRegistration one
         assertEquals(xreg.getExportReference(), xref);
         
         //get a proxy
         LogService proxy = getProxy(xreg,LogService.class);
         
         //check proxy != null
         assertNotNull(proxy);
         
         //check proxy calls
         for (int i = 1; i <= MAX_MOCK; i++) {
             proxy.log(LOG_WARNING, "YEAHH!!"+i);
             verify(logService).log(LOG_WARNING, "YEAHH!!"+i);
         }
     }
     
     /**
      * Test the {@link ExporterService#exportService(ServiceReference, Map)} with 
      * a valid {@link ServiceReference}. export, destroy and re export.
      */
     @Test
     public void testReExportService() {
         //wait for the service to be available.
         waitForIt(100);
         
         ExporterService exporter = getExporterService(); //get the service
         
         //Register a mock LogService
         ServiceRegistration regLog = rose.registerService(logService,LogService.class);
         
         //export the logService 
         ExportRegistration xreg = exporter.exportService(regLog.getReference(), null);
         
         //destroy the registration 
         xreg.close();
         
         //re export it
         xreg = exporter.exportService(regLog.getReference(), null);
         
         //check that xreg is not null
         assertNotNull(xreg); 
         
         //check that there is no exception
         assertNull(xreg.getException());
         
         //check that the export reference is not null
         assertNotNull(xreg.getExportReference());
         
         //check that the ServiceReference is equal to the logService one
         assertEquals(regLog.getReference(), xreg.getExportReference().getExportedService());
         
         //Check that the ExportReference has been published
         ExportReference xref = rose.getServiceObject(ExportReference.class);
         
         //Check that the published ExportReference is equal to the ExportRegistration one
         assertEquals(xreg.getExportReference(), xref);
         
         //get a proxy
         LogService proxy = getProxy(xreg,LogService.class);
         
         //check proxy != null
         assertNotNull(proxy);
         
         //check proxy calls
         for (int i = 1; i <= MAX_MOCK; i++) {
             proxy.log(LOG_WARNING, "YEAHH!!"+i);
             verify(logService).log(LOG_WARNING, "YEAHH!!"+i);
         }
     }
 
 
     
 
 	/**
      * Test the {@link ExportRegistration#close()}. (destroy the endpoint)
      */
     @Test
     public void testCloseExportRegistration() {
         //wait for the service to be available.
         waitForIt(100);
         
         ExporterService exporter = getExporterService(); //get the service
         
         //Register a mock LogService
         ServiceRegistration regLog = rose.registerService(logService,LogService.class);
         
         //export the logService 
         ExportRegistration xreg = exporter.exportService(regLog.getReference(), null);
         
         //Close the endpoint
         xreg.close();
         
         //Check that the ExportRegistration has been successfully closed
         assertNull(xreg.getExportReference());
         assertNull(xreg.getException());
         
         //Check that the ExportReference has been succesfully destroyed
         // Ok this cast sôcks
         assertNull(((ExporterIntrospection)exporter).getExportReference(regLog.getReference()));
     }
     
     /**
      * @return The {@link ExporterService} to be tested.
      */
     protected abstract ExporterService getExporterService();
     
     /**
      * Create a proxy for <code>xreg</code>
      * @param <T> 
      * @param xreg The {@link ExportRegistration} containing the {@link EndpointDescription} related to the endpoint used in order to create the proxy.
      * @param klass The Interface implemented by the proxy. (i.e. The original service interface). 
      * @return The proxy {@link Object}.
      */
     protected abstract <T> T getProxy(ExportRegistration xreg, Class<T> klass);
 }
