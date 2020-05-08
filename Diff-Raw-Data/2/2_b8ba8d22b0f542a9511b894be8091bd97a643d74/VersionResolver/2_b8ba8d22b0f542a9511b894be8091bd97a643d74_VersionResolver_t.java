 package sorcer.resolver;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.core.SorcerEnv;
 import sorcer.util.ArtifactCoordinates;
 import sorcer.util.PropertiesLoader;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 public class VersionResolver {
     final private static Logger log = LoggerFactory.getLogger(VersionResolver.class);
 
     // groupId_artifactId -> version
     protected Map<String, String> versions = new HashMap<String, String>();
     static PropertiesLoader propertiesLoader = new PropertiesLoader();
     static File VERSIONS_PROPS_FILE = new File(SorcerEnv.getHomeDir() , "configs/groupversions.properties");
 
     {
         versions = propertiesLoader.loadAsMap("META-INF/maven/groupversions.properties", Thread.currentThread()
                 .getContextClassLoader());
         versions.putAll(propertiesLoader.loadAsMap(VERSIONS_PROPS_FILE));
     }
 
     /**
      * Resolve version of artifact using groupversions.properties or pom.properties
      * from individual artifact jar the jar must be already in the classpath of
      * current thread context class loader in order to load its pom.version
      *
      * @param groupId    maven artifacts groupId
      * @param artifactId maven artifacts artifactId
      * @return artifacts version
      * @throws IllegalArgumentException if version could not be found
      */
     public String resolveVersion(String groupId, String artifactId) {
         String version = resolveCachedVersion(groupId, artifactId);
         if (version != null && checkFileExists(groupId, artifactId, version)) {
             return version;
         }
 
         version = loadVersionFromPomProperties(groupId, artifactId);
         if (version == null) {
             throw new IllegalArgumentException("Could not load version " + groupId + ':' + artifactId);
         }
         versions.put(key(groupId, artifactId), version);
         return version;
     }
 
     String loadVersionFromPomProperties(String groupId, String artifactId) {
         String resourceName = String.format("META-INF/maven/%1$s/%2$s/pom.properties", groupId, artifactId);
         Properties properties;
         try {
             ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
             properties = propertiesLoader.loadAsProperties(resourceName, contextClassLoader);
         } catch (IllegalArgumentException x) {
             log.debug("Could not find pom.properties for {}:{}", groupId, artifactId);
             return null;
         }
 
         String version = properties.getProperty("version");
         if (version == null) {
             throw new IllegalArgumentException("Could not load version " + groupId + ':' + artifactId
                     + " from groupversions.properties");
         }
         return version;
     }
 
     /**
      * @return cached version, may be null
      */
     String resolveCachedVersion(String groupId, String artifactId) {
         if (versions.containsKey(groupId)) {
             return versions.get(groupId);
         }
         // may be null
         return versions.get(key(groupId, artifactId));
     }
 
     protected void close(Closeable inputStream) {
         if (inputStream != null) {
             try {
                 inputStream.close();
             } catch (IOException e) {
                 // igonre
             }
         }
     }
 
     private String key(String groupId, String artifactId) {
         return groupId + "_" + artifactId;
     }
 
     private boolean checkFileExists(String groupId, String artifactId, String version) {
         String path = Resolver.resolveAbsolute(ArtifactCoordinates.coords(groupId, artifactId, version));
        return path!=null ? new File(path).exists() : false;
     }
 }
