 package org.eclipse.swordfish.core.test.util.base;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.InputStreamResource;
 import org.springframework.core.io.Resource;
 import org.springframework.util.Assert;
 
 public class TargetPlatformOsgiTestCase extends BaseOsgiTestCase {
     public static final String TARGET_PLATFORM_SYS_PORPERTY = "swordfishTargetPlatform";
     @Override
     protected Resource getTestingFrameworkBundlesConfiguration() {
         return new InputStreamResource(
            getClass().getClassLoader().getResourceAsStream("boot-bundles.properties"));
     }
     private String getTargetPlatformPath() {
         String ret = null;
         ret = System.getProperty(TARGET_PLATFORM_SYS_PORPERTY);
         try {
         if (ret == null) {
             File dir = new File(getClass().getClassLoader().getResource(".").getPath());
             dir = dir.getParentFile().getParentFile().getParentFile();
             String path = dir.getPath() + "/org.eclipse.swordfish.bundles/target/bundles/";
             dir = new File(path);
             if (dir.exists() && dir.isDirectory()) {
                 ret = dir.getPath();
             }
         }
         } catch (Exception ex) {
             LOG.error(ex.getMessage(), ex);
         }
         return ret;
     }
 
     protected List<Pattern> getExcludeBundlePatterns() {
         return Arrays.asList(Pattern.compile("org.eclipse.swordfish.samples.cxfendpoint.*"), Pattern.compile("org.eclipse.swordfish.samples.http.*"), Pattern.compile("org.eclipse.osgi-3.4.2.*"));
     }
 
     @Override
     protected Resource[] getTestBundles() {
         String targetPlatformPath = getTargetPlatformPath();
         Assert.notNull(targetPlatformPath, "Either system property [" + TARGET_PLATFORM_SYS_PORPERTY +
                 "] should be set or there should be the org.eclipse.swordfish.bundles project containing bundles " +
                 "needed to launch the Swordfish env");
         List<Resource> bundles = new ArrayList<Resource>();
         List<Pattern> excludePatterns = getExcludeBundlePatterns();
         boolean exclude;

         for (File bundle : new File(targetPlatformPath).listFiles()) {
             exclude = false;
             for (Pattern pattern : excludePatterns) {
                 if (pattern.matcher(bundle.getName()).matches()) {
                     exclude = true;
                 }
             }
             if (!exclude && bundle.isFile() && bundle.getName().endsWith("jar")) {
                 bundles.add(new FileSystemResource(bundle));
             }
         }
        /*Because of the strange behavior of the spring osgi
         *test framework we need to load org.eclipse.swordfish.core.configuration and org.eclipse.swordfish.core.event bundles
         *before org.eclipse.swordfish.core
         *TODO: Could anyone propose any less uglier solution  */
        Collections.reverse(bundles);
         return bundles.toArray(new Resource[bundles.size()]);
     }
 
 }
