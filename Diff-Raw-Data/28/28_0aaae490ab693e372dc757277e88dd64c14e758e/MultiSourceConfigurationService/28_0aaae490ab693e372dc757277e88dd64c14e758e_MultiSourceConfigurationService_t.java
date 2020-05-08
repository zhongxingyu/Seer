 package gitoscope.service.impl;
 
 import gitoscope.service.ConfigurationService;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  * Looking for configured paths in Spring bean configuration and
  * environment variables.
  *
  * @author Roman Kashitsyn
  */
 public class MultiSourceConfigurationService implements ConfigurationService {
 
    private String baseDirectory;

    private static final Logger LOG =
            LoggerFactory.getLogger(MultiSourceConfigurationService.class);
 
     public Collection<File> listConfiguredProjectPaths() {
         Collection<String> candidates = new ArrayList<String>();
        candidates.add(baseDirectory);
         candidates.addAll(listPropertiesDirs());
         candidates.addAll(listEnvironmentDirs());
         return filterValidDirectories(candidates);
     }
 
    public void setBaseDirectory(String baseDirectory) {
        LOG.info("Setting base directory to {}", baseDirectory);
        this.baseDirectory = baseDirectory;
     }
 
     private Collection<String> listPropertiesDirs() {
         String propertyPaths = System.getProperty("gitoscope.project.path");
         return (propertyPaths != null) ? splitPaths(propertyPaths) : Collections.<String>emptyList();
     }
 
     private Collection<String> listEnvironmentDirs() {
         String envPaths = System.getenv("GITOSCOPE_PROJECT_PATH");
         return (envPaths != null) ? splitPaths(envPaths) : Collections.<String>emptyList();
     }
 
     private Collection<String> splitPaths(String monolithPath) {
         return Arrays.asList(monolithPath.split("[;:]"));
     }
 
     private Collection<File> filterValidDirectories(Collection<String> dirNames) {
         if (dirNames == null || dirNames.isEmpty()) {
            LOG.warn("No configured directories found");
             return Collections.emptyList();
         }
         Collection<File> validDirs = new ArrayList<File>(dirNames.size());
         for (String dirName : dirNames) {
             File dirToTest = new File(dirName);
             if (dirToTest.exists() && dirToTest.isDirectory()) {
                LOG.info("Using directory {} for project discovery", dirName);
                 validDirs.add(dirToTest);
             }
         }
         return validDirs;
     }
 }
