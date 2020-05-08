 package com.dtolabs.rundeck.plugin.resources.url;
 
 import com.dtolabs.rundeck.core.common.Framework;
 import com.dtolabs.rundeck.core.plugins.Plugin;
 import com.dtolabs.rundeck.core.plugins.configuration.*;
 import com.dtolabs.rundeck.core.resources.ResourceModelSource;
 import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
 import com.dtolabs.rundeck.core.resources.format.UnsupportedFormatException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Properties;
 import org.apache.log4j.Logger;
 
 @Plugin(name="asynchronous-nodes-provider", service="ResourceModelSource")
 public class AsynchronousResourceModelSourceFactory implements ResourceModelSourceFactory, Describable {
 
     public static final Logger logger = Logger.getLogger(AsynchronousResourceModelSourceFactory.class);
 
     public static final String PROVIDER_NAME = "asynchronous-nodes-provider";
 
     public static final String RESOURCES_FORMAT_KEY = "resourcesFormatKey";
     public static final String RESOURCES_FORMAT_XML = "xml";
     public static final String RESOURCES_FORMAT_YML = "yml";
     public static final String RESOURCES_FORMAT_DEFAULT = RESOURCES_FORMAT_XML;
 
     public static final String RESOURCES_URL_KEY = "resourcesUrlKey";
     public static final String RESOURCES_URL_DEFAULT = "http://localhost/resources.xml";
 
     public static final String REFRESH_INTERVAL_KEY = "refreshIntervalKey";
     public static final int REFRESH_INTERVAL_DEFAULT = 30;
 
     private Framework framework;
 
     private static List<Property> descriptionProperties = new ArrayList<Property>();
 
     public AsynchronousResourceModelSourceFactory(final Framework framework) {
         this.framework = framework;
     }
 
     static private ResourceModelSource singleton;
 
     public ResourceModelSource createResourceModelSource(final Properties properties) throws ConfigurationException {
         if (null == singleton) {
         logger.debug("constructing factory singleton");
         XMLResourceModelSource xmlResourceModelSource = null;
         try {
            xmlResourceModelSource = new XMLResourceModelSource(this.framework, properties);
         } catch (UnsupportedFormatException e) {
            logger.error("caught UnsupportedFormatException exception: " +  e.getMessage());
            throw new ConfigurationException(e);
         }
         xmlResourceModelSource.validate();
         this.singleton = xmlResourceModelSource;
         return xmlResourceModelSource;
         } else {
            logger.debug("factory singleton already exists");
            return this.singleton;
         }
     }
  
 
     static {
 
        List<String> resourceFormats = new ArrayList<String>();
         resourceFormats.add(RESOURCES_FORMAT_XML);
         resourceFormats.add(RESOURCES_FORMAT_YML);
 
         descriptionProperties.add(PropertyUtil.freeSelect(RESOURCES_FORMAT_KEY, "resources format", "source resource format (default is " + RESOURCES_FORMAT_DEFAULT + ")", true, RESOURCES_FORMAT_DEFAULT, resourceFormats));
         descriptionProperties.add(PropertyUtil.string(RESOURCES_URL_KEY, "resources url", "source resource url", false, RESOURCES_URL_DEFAULT));
         descriptionProperties.add(PropertyUtil.integer(REFRESH_INTERVAL_KEY, "Refresh Interval", "Minimum time in seconds between url requests (default is " + Integer.toString(REFRESH_INTERVAL_DEFAULT) + ")", false,  Integer.toString(REFRESH_INTERVAL_DEFAULT)));
     }
 
 
     static Description DESC = new Description() {
         public String getName() {
             return PROVIDER_NAME;
         }
 
         public String getTitle() {
             return "asynchronous resources provider";
         }
 
         public String getDescription() {
             return "produces cached resources xml data from an upstream url";
         }
 
         public List<Property> getProperties() {
             return descriptionProperties;
         }
 
         public Map<String, String> getPropertiesMapping() {
             return null;
         }
     };
 
     public Description getDescription() {
         return DESC;
     }
 
 }
