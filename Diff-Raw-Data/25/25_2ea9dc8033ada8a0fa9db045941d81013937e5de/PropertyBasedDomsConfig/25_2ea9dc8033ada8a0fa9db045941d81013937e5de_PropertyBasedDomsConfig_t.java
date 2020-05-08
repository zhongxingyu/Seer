 package dk.statsbiblioteket.doms.transformers.common;
 
 import dk.statsbiblioteket.doms.transformers.common.PropertyBasedConfig;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Properties extended with DOMS properties.
  */
 public class PropertyBasedDomsConfig extends PropertyBasedConfig implements DomsConfig {
     /** Property for DOMS URL. */
     public static final String DOMS_WEBSERVICE_URL_PROPERTY
             = "dk.statsbiblioteket.doms.transformers.domsurl";
     /** Property for DOMS Username. */
    public static final String DOMS_USERNAME_PROPERTY = "dk.statsbiblioteket.doms.transformers.domsuser";
     /** Property for DOMS Password. */
    public static final String DOMS_PASSWORD_PROPERTY = "dk.statsbiblioteket.doms.transformers.domspass";
 
     public PropertyBasedDomsConfig() {
         super();
     }
 
     public PropertyBasedDomsConfig(File configfile) throws IOException {
         super(configfile);
     }
 
     @Override
     public String getDomsWebserviceUrl() {
         return properties.getProperty(DOMS_WEBSERVICE_URL_PROPERTY,
                                       "http://localhost:7880/centralWebservice-service/central/?wsdl");
     }
 
     @Override
     public String getDomsUsername() {
         return properties.getProperty(DOMS_USERNAME_PROPERTY, "fedoraAdmin");
     }
 
     @Override
     public String getDomsPassword() {
         return properties.getProperty(DOMS_PASSWORD_PROPERTY, "fedoraAdminPass");
     }}
