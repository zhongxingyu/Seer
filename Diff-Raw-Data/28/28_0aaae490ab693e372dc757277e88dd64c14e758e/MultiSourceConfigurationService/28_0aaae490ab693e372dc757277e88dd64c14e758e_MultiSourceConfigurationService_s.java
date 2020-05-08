 package gitoscope.service.impl;
 
 import gitoscope.service.ConfigurationService;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 
 /**
  * Looking for configured paths in Spring bean configuration and
  * environment variables.
  *
  * @author Roman Kashitsyn
  */
 public class MultiSourceConfigurationService implements ConfigurationService {
 
    private Collection<String> baseDirectoryNames;
 
     public Collection<File> listConfiguredProjectPaths() {
         Collection<String> candidates = new ArrayList<String>();
        candidates.addAll(baseDirectoryNames);
         candidates.addAll(listPropertiesDirs());
         candidates.addAll(listEnvironmentDirs());
         return filterValidDirectories(candidates);
     }
 
    public void setBaseDirectoryNames(Collection<String> baseDirectoryNames) {
        this.baseDirectoryNames = baseDirectoryNames;
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
             return Collections.emptyList();
         }
         Collection<File> validDirs = new ArrayList<File>(dirNames.size());
         for (String dirName : dirNames) {
             File dirToTest = new File(dirName);
             if (dirToTest.exists() && dirToTest.isDirectory()) {
                 validDirs.add(dirToTest);
             }
         }
         return validDirs;
     }
 }
