 package edu.northwestern.bioinformatics.studycalendar.osgi;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.core.StaticApplicationContextHelper;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder;
 import edu.northwestern.bioinformatics.studycalendar.tools.osgi.Embedder;
 import edu.northwestern.bioinformatics.studycalendar.tools.osgi.EmbedderConfiguration;
 import edu.northwestern.bioinformatics.studycalendar.tools.osgi.EmbedderFilesystemConfiguration;
 import edu.northwestern.bioinformatics.studycalendar.tools.osgi.FrameworkFactoryFinder;
 import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
 import org.apache.commons.lang.StringUtils;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class OsgiLayerIntegratedTestHelper {
     private static final Logger log = LoggerFactory.getLogger(OsgiLayerIntegratedTestHelper.class);
 
     private static StaticApplicationContextHelper applicationContextHelper = new ApplicationContextBuilder();
     private static BundleContext bundleContext;
     private static File projectRoot;
 
    private static final int TIMEOUT_MS = 300 * 1000;
 
     public static synchronized BundleContext getBundleContext() throws IOException {
         if (bundleContext == null) {
             System.setProperty("catalina.base",
                 getModuleRelativeDirectory("osgi-layer:integrated-tests", "tmp").getAbsolutePath());
 
             EmbedderConfiguration configuration = new EmbedderFilesystemConfiguration(
                 getModuleRelativeDirectory("osgi-layer", "target/test/embedder").getAbsoluteFile());
             Embedder embedder = new Embedder();
             embedder.setConfiguration(configuration);
             embedder.setFrameworkFactory(FrameworkFactoryFinder.getFrameworkFactory());
 
             bundleContext = embedder.start();
         }
         return bundleContext;
     }
 
     public static File getModuleRelativeDirectory(String moduleName, String directory) throws IOException {
         File dir = new File(findProjectRootDirectory(), moduleName.replaceAll(":", "/"));
         dir = new File(dir, directory);
         if (dir.exists()) return dir;
 
         throw new FileNotFoundException(
             String.format("Could not find directory %s relative to module %s from project directory %s",
                 directory, moduleName, findProjectRootDirectory().getCanonicalPath()));
     }
 
     private synchronized static File findProjectRootDirectory() throws FileNotFoundException {
         if (projectRoot == null) {
             File buildfile;
             projectRoot = new File(".");
             do {
                 buildfile = new File(projectRoot, "buildfile");
                 if (buildfile.exists()) {
                     return projectRoot;
                 }
                 projectRoot = new File(projectRoot, "..");
             } while (projectRoot.exists() && projectRoot.isDirectory());
 
             projectRoot = null;
             throw new FileNotFoundException(
                 String.format("Could not find project directory.  Started from %s and walked up to %s.",
                     new File("."), projectRoot));
         }
         return projectRoot;
     }
 
     public static Bundle startBundle(String bundleName) throws IOException, BundleException, InterruptedException {
         Bundle bundle = findBundle(bundleName);
         bundle.start();
         return bundle;
     }
 
     public static void startBundle(String bundleName, String withService) throws BundleException, IOException, InterruptedException {
         waitForService(startBundle(bundleName), withService);
     }
 
     private static void waitForService(Bundle bundle, String withService) throws InterruptedException {
        log.debug("Beginning wait for {} in {}", withService, bundle);

         long start = System.currentTimeMillis();
 
         while (
             !timedOut(start) &&
             (bundle.getRegisteredServices() == null || bundle.getRegisteredServices().length == 0)
         ) {
            log.debug("- waiting for any service registration");
             Thread.sleep(100);
         }
 
         SEARCH: while (!timedOut(start)) {
             for (ServiceReference ref : bundle.getRegisteredServices()) {
                 String[] interfaces = (String[]) ref.getProperty("objectClass");
                 if (interfaces != null) {
                     if (Arrays.asList(interfaces).contains(withService)) {
                         break SEARCH;
                     }
                 }
             }
            log.debug("- waiting for service {} registration", withService);
             Thread.sleep(100);
         }

        log.debug("- {} became available after {}ms", withService, System.currentTimeMillis() - start);
     }
 
     // side effect is the important bit
     private static boolean timedOut(long startTime) {
         if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
             throw new StudyCalendarSystemException("Wait for service timed out");
         } else {
            return false;
         }
     }
 
     public static void waitForService(String bundleName, String withService) throws IOException, InterruptedException {
         waitForService(findBundle(bundleName), withService);
     }
 
     public static void stopBundle(String bundleName) throws IOException, BundleException {
         findBundle(bundleName).stop();
     }
 
     private static Bundle findBundle(String bundleName) throws IOException {
         for (Bundle candidate : getBundleContext().getBundles()) {
             if (candidate.getSymbolicName() == null) {
                 throw new NullPointerException(
                     String.format("Bundle %s (%s) has no symbolic name",
                         candidate, candidate.getBundleId()));
             }
             if (candidate.getSymbolicName().equals(bundleName)) {
                 return candidate;
             }
         }
 
         List<String> bundles = new ArrayList<String>();
         for (Bundle bundle : getBundleContext().getBundles()) {
             bundles.add(bundle.getSymbolicName());
         }
 
         throw new StudyCalendarSystemException("No bundle %s in the testing context:\n- %s",
             bundleName, StringUtils.join(bundles.iterator(), ("\n- ")));
     }
 
     public static ApplicationContext getApplicationContext() {
         return applicationContextHelper.getApplicationContext();
     }
 
     private static class ApplicationContextBuilder extends StaticApplicationContextHelper {
         private ApplicationContext createBundleContextApplicationContext() {
             try {
                 return ConcreteStaticApplicationContext.create(
                     Collections.<String, Object>singletonMap("bundleContext", getBundleContext()));
             } catch (IOException e) {
                 throw new RuntimeException("Creating bundle context failed", e);
             }
         }
 
         @Override
         protected ApplicationContext createApplicationContext() {
             ClassPathXmlApplicationContext ctxt = new ClassPathXmlApplicationContext(createBundleContextApplicationContext());
             ctxt.setConfigLocations(StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS);
             ctxt.refresh();
             return ctxt;
         }
     }
 }
