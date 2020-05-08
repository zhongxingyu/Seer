 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import static net.contextfw.web.application.configuration.Configuration.*;
 
 import java.io.IOException;
 import java.util.Properties;
 import java.net.URL;
 
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.WebApplicationModule;
 import net.contextfw.web.application.configuration.Configuration;
 
 import com.google.inject.AbstractModule;
 import com.mycila.inject.jsr250.Jsr250;
 
 public class MyApplicationModule extends AbstractModule {
 
     @Override
     protected void configure() {
 
         Properties properties = loadProperties();
         
         boolean developmentMode = Boolean.parseBoolean(
                 properties.getProperty("developmentMode", "false"));
         
         Configuration conf = Configuration.getDefaults()
           .add(RESOURCE_PATH, "${package}")
           .add(VIEW_COMPONENT_ROOT_PACKAGE, "${package}.views")
           .add(RELOADABLE_CLASSES.includedPackage("${package}.components"))
           .set(CLASS_RELOADING_ENABLED, true)
           .add(BUILD_PATH, "target/classes")
           .add(BUILD_PATH, "target/test-classes")
           .set(DEVELOPMENT_MODE, developmentMode)
           .set(XML_PARAM_NAME, "xml")
           .set(LOG_XML, developmentMode)
           .set(XML_RESPONSE_LOGGER.as(ResponseLogger.class));
        
         install(new WebApplicationModule(conf));
         install(Jsr250.newJsr250Module());
     }
     
     private Properties loadProperties() {
         Properties properties = new Properties();
         ClassLoader loader = getClass().getClassLoader();
         URL url = loader.getResource("config.properties");
         try {
             properties.load(url.openStream());
         } catch (IOException e) {
             throw new WebApplicationException(e);
         }
         return properties;
     }
 }
