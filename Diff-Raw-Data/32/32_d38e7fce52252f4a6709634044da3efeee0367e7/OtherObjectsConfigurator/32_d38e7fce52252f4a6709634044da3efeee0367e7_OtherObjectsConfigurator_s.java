 package org.otherobjects.cms.config;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import org.otherobjects.cms.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.util.Assert;
 
 /**
  * Global configuration access class that merges all configured properties files and then reshuffles them to use the correct environment specific 
  * settings. These are then used to override placeholders in the application context.
  * It also provides {@link Properties} like access to the processed properties.
  *  
  * @author joerg
  */
 public class OtherObjectsConfigurator extends PropertyPlaceholderConfigurer
 {
     protected final Logger logger = LoggerFactory.getLogger(getClass());
     public static final String ENVIRONMENT_SYSPROP_KEY = "otherobjects.environment";
 
     public static final String SERVER_NAME_KEY = "site.server.name";
     public static final String CONTEXT_PATH_KEY = "site.server.context.path";
     public static final String DEFAULT_PORT_KEY = "site.server.default.port";
     public static final String DEFAULT_SECURE_PORT_KEY = "site.server.default.secureport";
 
     public static final String JNDI_BASE = "java:comp/env/";
 
    private Properties mergedProperties;
 
     @Override
     protected void convertProperties(Properties props)
     {
         String environmentPrefix = getEnvironmentName();
 
        Properties convertedProperties = new Properties();

         Pattern pattern = Pattern.compile("^" + environmentPrefix + "\\.");
 
         // find all props starting with environmentPrefix and re-add them to the properties losing the prefix, thereby
         //effectively overriding global defaults
        for (Object key : props.keySet())
         {
            Object value = props.getProperty((String) key);
             if (pattern.matcher((String) key).lookingAt())
             {
                 String newKey = pattern.matcher((String) key).replaceFirst("");
                convertedProperties.setProperty(newKey, (String) value);
             }
             else
             {
                convertedProperties.setProperty((String) key, (String) value);
             }
         }
 
         if (environmentPrefix.equals("production"))
            jndiOverride(convertedProperties);
 
        this.mergedProperties = convertedProperties;
        props = convertedProperties;
         super.convertProperties(props);
     }
 
     private void jndiOverride(Properties props)
     {
         //TODO override properties with jndi settings if they exist
     }
 
     public String getEnvironmentName()
     {
         String environment;
         if (StringUtils.isNotBlank(System.getProperty(ENVIRONMENT_SYSPROP_KEY)))
             environment = System.getProperty(ENVIRONMENT_SYSPROP_KEY);
         else
             environment = "dev";
 
         Assert.isTrue(environment.equals("dev") || environment.equals("test") || environment.equals("production"), "environment must be one of 'dev, 'test' or 'production'");
 
         return environment;
     }
 
     public String getProperty(String key)
     {
         Assert.notNull(mergedProperties, "OtherObjectsConfigurator not initialised. Check your setup");
         return mergedProperties.getProperty(key);
     }
 
     public String getProperty(String key, String defaultValue)
     {
         Assert.notNull(mergedProperties, "OtherObjectsConfigurator not initialised. Check your setup");
         return mergedProperties.getProperty(key, defaultValue);
     }
 
     public String listProperties()
     {
         if (mergedProperties != null)
         {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             mergedProperties.list(new PrintStream(baos));
             return baos.toString();
         }
         else
             return "";
     }
 }
