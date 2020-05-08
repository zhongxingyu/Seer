 package org.synyx.minos.core.configuration;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.ResourceLoaderAware;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.ResourceLoader;
 import org.springframework.core.io.support.ResourcePatternResolver;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

 
 /**
  * Specialisation of {@link PropertyPlaceholderConfigurer} that is able to use a different location depending on an
  * system-enviromnment. This takes a list of resourcePatterns where {env} gets replaced within each with the value of a
  * given environmentPropertyName. If you configure this with a environmentProperty target.system=prod it replaces all
  * occurences of {env} within the given resourcepatterns with prod (eg. application-{env}.properties with
  * application-prod.properties.
  * 
  * @author Marc Kannegießer - kannegiesser@synyx.de
  */
 public class EnvironmentPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements
         ResourceLoaderAware {
 
     public static final Log LOG = LogFactory.getLog(EnvironmentPropertyPlaceholderConfigurer.class);
 
     private String environmentPropertyName = "target.system";
     private List<String> resourcePatterns;
     private ResourceLoader resourceLoader;
 
     private String defaultEnvironment = "dev";
 
 
     /**
      * Replaces the placeholders {env} in all given resource-patterns and loads them.
      */
     @Override
     protected void loadProperties(Properties props) throws IOException {
 
         String env = System.getProperty(environmentPropertyName, defaultEnvironment);
 
         LOG.info(String.format(">>> Resolved environment to %s (from system property %s with default %s)", env,
                 environmentPropertyName, defaultEnvironment));
 
         List<Resource> resources = new ArrayList<Resource>();
 
         ResourcePatternResolver patternResolver = null;
         if (resourceLoader instanceof ResourcePatternResolver) {
             patternResolver = (ResourcePatternResolver) resourceLoader;
         }
 
         for (int i = 0; i < resourcePatterns.size(); i++) {
             String resourcePattern = resourcePatterns.get(i);
             String replaced = resourcePattern.replaceAll("\\{env\\}", env);
             if (patternResolver == null) {
                 Resource resource = resourceLoader.getResource(replaced);
                 resources.add(resource);
             } else {
                 Resource[] rs = patternResolver.getResources(replaced);
                 resources.addAll(Arrays.asList(rs));
             }
         }
 
        super.setLocations(resources.toArray(new Resource[resources.size()]));
 
         super.loadProperties(props);
     }
 
 
     /**
      * @see org.springframework.context.ResourceLoaderAware#setResourceLoader(org.springframework.core.io.ResourceLoader)
      */
     @Override
     public void setResourceLoader(ResourceLoader rl) {
 
         this.resourceLoader = rl;
     }
 
 
     /**
      * Sets a {@link List} of patterns which may contain placeholder {env} which will be replaced with the target system
      * name
      * 
      * @param resourcePatterns
      */
     public void setResourcePatterns(List<String> resourcePatterns) {
 
         this.resourcePatterns = resourcePatterns;
     }
 
 
     /**
      * Sets the name of the System property that is used to resolve the target environment (defaults to target.system).
      * 
      * @param environmentPropertyName the name of the System property containing the environment name
      */
     public void setEnvironmentPropertyName(String environmentPropertyName) {
 
         this.environmentPropertyName = environmentPropertyName;
     }
 
 
     /**
      * Sets the default-environment that is used when the {@link #environmentPropertyName} is not set (defaults to dev).
      * 
      * @param defaultEnvironment the name of the default environment
      */
     public void setDefaultEnvironment(String defaultEnvironment) {
 
         this.defaultEnvironment = defaultEnvironment;
     }
 
 }
