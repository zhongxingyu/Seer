 package jewas.util.properties;
 
 
 import jewas.util.file.Files;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author fcamblor
  */
 public class ChainedProperties {
 
     private static final class ChainedPropertiesConfig {
 
         private static final Pattern ENV_VAR_EXTRACTION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
 
         String path;
         String name;
         boolean failIfMissingResource;
 
         private ChainedPropertiesConfig(String path, String name, boolean failIfMissingResource) {
             this.path = path;
             this.name = name;
             this.failIfMissingResource = failIfMissingResource;
         }
 
         private String extrapolatedPath() {
             StringBuffer extrapolatedPath = new StringBuffer();
             Matcher extrapolatedPathMatcher = ENV_VAR_EXTRACTION_PATTERN.matcher(path);
             while (extrapolatedPathMatcher.find()) {
                 String envVarName = extrapolatedPathMatcher.group(1);
                 String envVarValue = System.getProperty(envVarName);
                 if (envVarValue == null) {
                     throw new IllegalArgumentException("Unknown property <" + envVarName + "> for ChainedProperties path <" + path + "> !");
                 }
 
                 extrapolatedPathMatcher.appendReplacement(extrapolatedPath, envVarValue);
             }
             extrapolatedPathMatcher.appendTail(extrapolatedPath);
 
             return extrapolatedPath.toString();
         }
     }
 
     private List<ChainedPropertiesConfig> configs;
 
     public ChainedProperties() {
         this.configs = new ArrayList<>();
     }
 
     public ChainedProperties chainProperties(String path, String configName, boolean failIfMissingResource) {
         this.configs.add(new ChainedPropertiesConfig(path, configName, failIfMissingResource));
         return this;
     }
 
     public Properties load() {
         Properties result = new Properties();
 
         for (ChainedPropertiesConfig config : configs) {
             try (InputStream configStream = Files.getInputStreamFromPath(ChainedProperties.class, config.extrapolatedPath())) {
                 result.load(configStream);
             } catch (IOException e) {
                 if (config.failIfMissingResource) {
                    throw new IllegalStateException("Error while loading " + config.name + " configuration file (" + config.path + ")");
                 }
             }
         }
 
         return result;
     }
 }
