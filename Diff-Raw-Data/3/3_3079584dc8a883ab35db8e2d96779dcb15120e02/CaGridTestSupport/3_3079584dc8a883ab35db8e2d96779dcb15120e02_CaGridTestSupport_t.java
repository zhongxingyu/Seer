 package org.cagrid.gts.test;
 
 import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFileExtend;
 import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;
 import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.keepRuntimeFolder;
 import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.logLevel;
 import static org.ops4j.pax.exam.CoreOptions.maven;
 import static org.ops4j.pax.exam.CoreOptions.vmOption;
 import static org.ops4j.pax.exam.CoreOptions.when;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 
 import javax.inject.Inject;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 import org.apache.felix.service.command.CommandProcessor;
 import org.apache.felix.service.command.CommandSession;
 import org.apache.karaf.features.Feature;
 import org.apache.karaf.features.FeaturesService;
 import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
 import org.apache.karaf.tooling.exam.options.LogLevelOption;
 import org.junit.Assert;
 import org.junit.Before;
 import org.ops4j.pax.exam.CoreOptions;
 import org.ops4j.pax.exam.MavenUtils;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.TestProbeBuilder;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.ProbeBuilder;
 import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 
 public abstract class CaGridTestSupport {
 
     static final Long COMMAND_TIMEOUT = 10000L;
     static final Long SERVICE_TIMEOUT = 30000L;
 
     public static final String KARAF_GROUP_ID = "org.apache.karaf";
     public static final String KARAF_ARTIFACT_ID = "apache-karaf";
 
     // public static final String WSRF_FEATURE_VERSION_PROPERTY = "cagrid.wsrf.version";
     public static final String CAGRID_FEATURE_VERSION_PROPERTY = "cagrid.version";
 
     ExecutorService executor = Executors.newCachedThreadPool();
 
     @Inject
     protected BundleContext bundleContext;
 
     @Inject
     protected FeaturesService featuresService;
 
     @ProbeBuilder
     public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
         probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
         return probe;
     }
 
     @Before
     public void setUp() {
 
         // // Add the WSRF Features Repo
         // System.err.println(executeCommand("features:addurl "
         // + maven().groupId("org.cagrid.wsrf").artifactId("wsrf-draft-features").version(System.getProperty(WSRF_FEATURE_VERSION_PROPERTY))
         // .classifier("features").type("xml").getURL()));
 
         // Add the caGrid Features Repo
         System.err.println(executeCommand("features:addurl "
                 + maven().groupId("org.cagrid").artifactId("cagrid-features").version(System.getProperty(CAGRID_FEATURE_VERSION_PROPERTY))
                         .classifier("features").type("xml").getURL()));
 
     }
 
     @Configuration
     public Option[] config() {
         return new Option[] {
                 // pick the distribution to use; and configure it to be kept
                 caGridDistributionConfiguration(),
                 keepRuntimeFolder(),
                 logLevel(LogLevelOption.LogLevel.INFO),
                 // pass in the versions to use for features
                 // systemProperty(WSRF_FEATURE_VERSION_PROPERTY, MavenUtils.getArtifactVersion("org.cagrid.wsrf", "wsrf-draft-features")),
                 systemProperty(CAGRID_FEATURE_VERSION_PROPERTY, MavenUtils.getArtifactVersion("org.cagrid", "cagrid-features")),
                 // Pass local/private maven locations into the forked JVMs (this is needed if you use jenkins with a per-build maven repo)
                 when(System.getProperty("maven.repo.local") != null).useOptions(
                         vmOption("-Dorg.ops4j.pax.url.mvn.localRepository=" + System.getProperty("maven.repo.local"))),
                 when(System.getProperty("org.ops4j.pax.url.mvn.localRepository") != null).useOptions(
                         vmOption("-Dorg.ops4j.pax.url.mvn.localRepository=" + System.getProperty("org.ops4j.pax.url.mvn.localRepository"))),
                 // servicemix defaults to using the felix osgi kernal which has some bugs when dealing with complex feature/bundle dependencies, so let's use
                 // equinox by default
                 new KarafDistributionConfigurationFilePutOption("etc/config.properties", // config file to modify based on karaf.base
                         "karaf.framework", // key to add or change
                         "equinox") // value to add or change
                , new KarafDistributionConfigurationFilePutOption("etc/org.apache.karaf.features.cfg", // config file to modify based on karaf.base
                "featuresBoot", // key to add or change
                "karaf-framework,config,activemq-broker,activemq-spring,saaj,cxf-specs,camel,activemq-camel,camel-cxf,camel-blueprint,war,spring-jdbc,spring-orm")
         };
     }
 
     protected Option caGridDistributionConfiguration() {
         return karafDistributionConfiguration()
                 .frameworkUrl(maven().groupId("org.apache.servicemix").artifactId("apache-servicemix").type("tar.gz").versionAsInProject())
                 .karafVersion(getKarafVersion()).name("Apache Servicemix").unpackDirectory(new File("target/paxexam/"));
     }
 
     /**
      * Executes a shell command and returns output as a String. Commands have a default timeout of 10 seconds.
      * 
      * @param command
      * @return
      */
     protected String executeCommand(final String command) {
         return executeCommand(command, COMMAND_TIMEOUT, false);
     }
 
     /**
      * Executes a shell command and returns output as a String. Commands have a default timeout of 10 seconds.
      * 
      * @param command
      *            The command to execute.
      * @param timeout
      *            The amount of time in millis to wait for the command to execute.
      * @param silent
      *            Specifies if the command should be displayed in the screen.
      * @return
      */
     protected String executeCommand(final String command, final Long timeout, final Boolean silent) {
         String response;
         final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         final PrintStream printStream = new PrintStream(byteArrayOutputStream);
         final CommandProcessor commandProcessor = getOsgiService(CommandProcessor.class);
         final CommandSession commandSession = commandProcessor.createSession(System.in, printStream, System.err);
         FutureTask<String> commandFuture = new FutureTask<String>(new Callable<String>() {
             public String call() {
                 try {
                     if (!silent) {
                         System.err.println(command);
                     }
                     commandSession.execute(command);
                 } catch (Exception e) {
                     e.printStackTrace(System.err);
                 }
                 printStream.flush();
                 return byteArrayOutputStream.toString();
             }
         });
 
         try {
             executor.submit(commandFuture);
             response = commandFuture.get(timeout, TimeUnit.MILLISECONDS);
         } catch (Exception e) {
             e.printStackTrace(System.err);
             response = "SHELL COMMAND TIMED OUT: ";
         }
 
         return response;
     }
 
     protected <T> T getOsgiService(Class<T> type, long timeout) {
         return getOsgiService(type, null, timeout);
     }
 
     protected <T> T getOsgiService(Class<T> type) {
         return getOsgiService(type, null, SERVICE_TIMEOUT);
     }
 
     protected <T> T getOsgiService(Class<T> type, String filter, long timeout) {
         ServiceTracker tracker = null;
         try {
             String flt;
             if (filter != null) {
                 if (filter.startsWith("(")) {
                     flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")" + filter + ")";
                 } else {
                     flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")(" + filter + "))";
                 }
             } else {
                 flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
             }
             Filter osgiFilter = FrameworkUtil.createFilter(flt);
             tracker = new ServiceTracker(bundleContext, osgiFilter, null);
             tracker.open(true);
             // Note that the tracker is not closed to keep the reference
             // This is buggy, as the service reference may change i think
             Object svc = type.cast(tracker.waitForService(timeout));
             if (svc == null) {
                 Dictionary dic = bundleContext.getBundle().getHeaders();
                 System.err.println("Test bundle headers: " + explode(dic));
 
                 for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, null))) {
                     System.err.println("ServiceReference: " + ref);
                 }
 
                 for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, flt))) {
                     System.err.println("Filtered ServiceReference: " + ref);
                 }
 
                 throw new RuntimeException("Gave up waiting for service " + flt);
             }
             return type.cast(svc);
         } catch (InvalidSyntaxException e) {
             throw new IllegalArgumentException("Invalid filter", e);
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     /*
      * Explode the dictionary into a ,-delimited list of key=value pairs
      */
     private static String explode(Dictionary dictionary) {
         Enumeration keys = dictionary.keys();
         StringBuffer result = new StringBuffer();
         while (keys.hasMoreElements()) {
             Object key = keys.nextElement();
             result.append(String.format("%s=%s", key, dictionary.get(key)));
             if (keys.hasMoreElements()) {
                 result.append(", ");
             }
         }
         return result.toString();
     }
 
     /**
      * Provides an iterable collection of references, even if the original array is null
      */
     private static Collection<ServiceReference> asCollection(ServiceReference[] references) {
         return references != null ? Arrays.asList(references) : Collections.<ServiceReference> emptyList();
     }
 
     public JMXConnector getJMXConnector() throws Exception {
         JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
         Hashtable env = new Hashtable();
         String[] credentials = new String[] { "karaf", "karaf" };
         env.put("jmx.remote.credentials", credentials);
         JMXConnector connector = JMXConnectorFactory.connect(url, env);
         return connector;
     }
 
     /**
      * Installs a feature and checks that feature is properly installed.
      * 
      * @param feature
      * @throws Exception
      */
     public void installAndAssertFeature(String feature, Long timeout) throws Exception {
         executeCommand("features:install " + feature, timeout, false);
         assertFeatureInstalled(feature);
     }
 
     public void assertFeatureInstalled(String featureName) {
         Feature[] features = featuresService.listInstalledFeatures();
 
         for (Feature feature : features) {
             if (featureName.equals(feature.getName())) {
                 return;
             }
         }
         Assert.fail("Feature " + featureName + " should be installed but is not");
     }
 
     protected void assertBundleInstalled(String name) {
         Assert.assertTrue("Bundle " + name + " should be installed", isBundleInstalled(name));
     }
 
     protected void assertBundleNotInstalled(String name) {
         Assert.assertFalse("Bundle " + name + " should not be installed", isBundleInstalled(name));
     }
 
     private boolean isBundleInstalled(String symbolicName) {
         for (Bundle bundle : bundleContext.getBundles()) {
             if (bundle.getSymbolicName().equals(symbolicName)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the Version of Karaf to be used.
      * 
      * @return
      */
     protected String getKarafVersion() {
         // karaf is a transitive dependency of SMX, so we can pull the version thusly
         return MavenUtils.getArtifactVersion(KARAF_GROUP_ID, KARAF_ARTIFACT_ID);
     }
 
     public static <T> T[] concatAll(T[] first, T[]... rest) {
         int totalLength = first.length;
         for (T[] array : rest) {
             totalLength += array.length;
         }
         T[] result = Arrays.copyOf(first, totalLength);
         int offset = first.length;
         for (T[] array : rest) {
             System.arraycopy(array, 0, result, offset, array.length);
             offset += array.length;
         }
         return result;
     }
 
     /**
      * Create an provisioning option for the specified maven artifact (groupId and artifactId), using the version found in the list of dependencies of this
      * maven project.
      * 
      * @param groupId
      *            the groupId of the maven bundle
      * @param artifactId
      *            the artifactId of the maven bundle
      * @return the provisioning option for the given bundle
      */
     protected static MavenArtifactProvisionOption mavenBundle(String groupId, String artifactId) {
         return CoreOptions.mavenBundle(groupId, artifactId).versionAsInProject();
     }
 
     /**
      * Create an provisioning option for the specified maven artifact (groupId and artifactId), using the version found in the list of dependencies of this
      * maven project.
      * 
      * @param groupId
      *            the groupId of the maven bundle
      * @param artifactId
      *            the artifactId of the maven bundle
      * @param version
      *            the version of the maven bundle
      * @return the provisioning option for the given bundle
      */
     protected static MavenArtifactProvisionOption mavenBundle(String groupId, String artifactId, String version) {
         return CoreOptions.mavenBundle(groupId, artifactId).version(version);
     }
 
     public static Option systemProperty(String propertyName, String propertyValue) {
         return editConfigurationFileExtend("etc/system.properties", propertyName, propertyValue != null ? propertyValue : "");
     }
 
 }
