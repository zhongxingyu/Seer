 package au.com.sensis.mobile.crf.config;
 
 import java.util.Iterator;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.apache.commons.configuration.HierarchicalConfigurationXMLReader;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.log4j.Logger;
 import org.exolab.castor.xml.UnmarshalHandler;
 import org.exolab.castor.xml.Unmarshaller;
 import org.springframework.core.io.ClassPathResource;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderAdapter;
 
 import au.com.sensis.mobile.crf.exception.ContentRenderingFrameworkRuntimeException;
 import au.com.sensis.wireless.common.volantis.devicerepository.api.DefaultDevice;
 import au.com.sensis.wireless.common.volantis.devicerepository.api.Device;
 
 
 /**
  * Factory for returning a {@link UiConfiguration}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class ConfigurationFactoryBean
     implements ConfigurationFactory {
 
     private static final Logger LOGGER = Logger.getLogger(
             ConfigurationFactoryBean.class);
 
     private static final String SCHEMA_CLASSPATH_LOCATION
         = "/au/com/sensis/mobile/crf/config/crf-config.xsd";
 
     /**
      * Classpath of the file containing the configuration.
      */
     private final String mappingConfigurationClasspath;
 
     private UiConfiguration uiConfiguration;
 
     /**
      * The constructor which sets up the mappingConfiguration.
      *
      * @param mappingConfigurationClasspath
      *            Classpath of the file containing the configuration.
      */
     public ConfigurationFactoryBean(
             final String mappingConfigurationClasspath) {
         this.mappingConfigurationClasspath = mappingConfigurationClasspath;
 
         validateConfigAgainstSchema();
         loadConfiguration();
         validateConfigData();
     }
 
     private void validateConfigData() {
         final Iterator<Group> groupIterator = getUiConfiguration().groupIterator();
         while (groupIterator.hasNext()) {
             groupIterator.next().validate(createDefaultDevice());
         }
 
     }
 
     private Device createDefaultDevice() {
         return new DefaultDevice();
     }
 
     // TODO: extract this method into an interface/implementation pair.
     private void loadConfiguration() {
         try {
             final XMLConfiguration config = new XMLConfiguration();
             config.load(new ClassPathResource(
                     getMappingConfigurationClasspath()).getFile());
 
             if (LOGGER.isInfoEnabled()) {
                 LOGGER.info("Loaded configuration from '"
                         + getMappingConfigurationClasspath() + "'.");
             }
 
             final XMLReader reader =
                     new HierarchicalConfigurationXMLReader(config);
 
             final XMLReaderAdapter adapter = new XMLReaderAdapter(reader);
 
             final Unmarshaller unmarsh =
                     new Unmarshaller(UiConfiguration.class);
             final UnmarshalHandler handler = unmarsh.createHandler();
 
             adapter.setDocumentHandler(handler);
             adapter.parse(new InputSource());
 
             setUiConfiguration((UiConfiguration) handler.getObject());
 
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Loaded UiConfiguration: " + getUiConfiguration());
             }
 
         } catch (final Exception e) {
             throw new ContentRenderingFrameworkRuntimeException(
                     "Error loading config from classpath: '"
                             + getMappingConfigurationClasspath() + "'", e);
         }
 
     }
 
     // TODO: extract this method into an interface/implementation pair.
     private void validateConfigAgainstSchema() {
         try {
 
             final ClassPathResource schemaClasspathResource =
                     new ClassPathResource(SCHEMA_CLASSPATH_LOCATION);
             final SchemaFactory factory = SchemaFactory.newInstance(
                     "http://www.w3.org/2001/XMLSchema");
             final Schema schema =
                    factory.newSchema(schemaClasspathResource.getFile());
 
             final Validator validator = schema.newValidator();
             final Source configurationSourceToValidate =
                new StreamSource(new ClassPathResource(
                        getMappingConfigurationClasspath()).getFile());
             validator.validate(configurationSourceToValidate);
         } catch (final Exception e) {
             throw new ContentRenderingFrameworkRuntimeException(
                     "Error loading config from classpath: '"
                             + getMappingConfigurationClasspath() + "'", e);
         }
     }
 
     /**
      * @return {@link UiConfiguration} loaded from the file passed to
      *         {@link #ConfigurationFactoryBean(String)}.
      */
     public UiConfiguration getUiConfiguration() {
         return uiConfiguration;
     }
 
     /**
      * @return the mappingConfigurationClasspath
      */
     private String getMappingConfigurationClasspath() {
         return mappingConfigurationClasspath;
     }
 
     /**
      * @param uiConfiguration the uiConfiguration to set
      */
     private void setUiConfiguration(final UiConfiguration uiConfiguration) {
         this.uiConfiguration = uiConfiguration;
     }
 
 }
