 package edu.northwestern.bioinformatics.studycalendar.security;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.tools.configuration.DictionaryConfiguration;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemLoadingFailure;
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
 import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
 import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
 import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
 import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
 import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
 import org.apache.commons.lang.StringUtils;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.osgi.context.BundleContextAware;
 
 import java.util.Dictionary;
 
 /**
  * @author Rhett Sutphin
  */
 public class AuthenticationSystemConfiguration implements BundleContextAware {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private BundleContext bundleContext;
     private ConfigurationProperties currentConfigurationProperties;
     private Dictionary<String, String> configurationPropertyValues;
     private Configuration newConfiguration;
     private AuthenticationSystem currentSystem, newSystem;
     private ServiceReference currentSystemReference, newSystemReference;
     private boolean propertiesReady, systemReady;
 
     public static final DefaultConfigurationProperties UNIVERSAL_PROPERTIES
         = new DefaultConfigurationProperties(new ClassPathResource(
             "authentication-system-universal.properties", AuthenticationSystemConfiguration.class));
     public static final ConfigurationProperty<String> AUTHENTICATION_SYSTEM
         = UNIVERSAL_PROPERTIES.add(new DefaultConfigurationProperty.Text("authenticationSystem"));
     private static final String SERVICE_NAME = AuthenticationSystem.class.getName();
    private static final String DEFAULT_AUTHENTICATION_SYSTEM_BUNDLE =
        "edu.northwestern.bioinformatics.psc-authentication-local-plugin";
 
     public AuthenticationSystemConfiguration() {
         configurationPropertyValues = new MapBasedDictionary<String, String>();
     }
 
     public synchronized ConfigurationProperties getProperties() {
         initProperties();
         return currentConfigurationProperties;
     }
 
     public synchronized AuthenticationSystem getAuthenticationSystem() {
         initSystem();
         return currentSystem;
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
     public synchronized void updated(Dictionary properties) {
         configurationPropertyValues = properties == null ? new MapBasedDictionary<String, String>() : properties;
         signalRebuildNeeded();
     }
 
     @Deprecated
     public boolean isCustomAuthenticationSystem() {
         return false;
     }
 
     private synchronized void signalRebuildNeeded() {
         propertiesReady = false;
         systemReady = false;
         newConfiguration = null;
     }
 
     // determines system class & instantiates it to update in the properties.
     // Does not initialize the system.
     private synchronized void initProperties() {
         if (propertiesReady) return;
         systemReady = false;
 
         newSystemReference = retrieveAuthenticationSystemReference();
         newSystem = acquireAuthenticationSystem();
         log.debug("Successfully retrieved plugin instance {} of class {}",
             newSystem, newSystem.getClass().getName());
         log.debug("Retrieved authentication system has these configuration properties: {}",
             newSystem.configurationProperties().getAll());
         currentConfigurationProperties = DefaultConfigurationProperties.union(
             UNIVERSAL_PROPERTIES, newSystem.configurationProperties());
         propertiesReady = true;
     }
 
     // Initializes the system using the parameters determined by initProperties
     private synchronized void initSystem() {
         if (systemReady) return;
         initProperties();
         newSystem.validate(getConfiguration());
         log.debug("Successfully validated new authentication system {}.", newSystem);
         newSystem.initialize(getConfiguration());
         log.debug("Successfully initialized new authentication system {}.  Replacing.", newSystem);
         // no errors, so:
         if (currentSystemReference != null) getBundleContext().ungetService(currentSystemReference);
         currentSystemReference = newSystemReference;
         currentSystem = newSystem;
         systemReady = true;
     }
 
     @SuppressWarnings({ "unchecked" })
     private synchronized Configuration getConfiguration() {
         if (newConfiguration == null) {
             newConfiguration = new DictionaryConfiguration(getProperties(), configurationPropertyValues);
         }
         return newConfiguration;
     }
 
     private ServiceReference retrieveAuthenticationSystemReference() {
         String desired = configurationPropertyValues.get(AUTHENTICATION_SYSTEM.getKey());
         ServiceReference ref = null;
         if (!StringUtils.isBlank(desired)) {
             ref = findServiceFromBundle(desired);
             if (ref == null) {
                 log.error("The configured authentication system \"{}\" is no longer available.  " +
                     "Will use the default system instead.  This may be the cause of any " +
                     "subsequent AuthenticationLoadingFailure.", desired);
             }
         } else {
             log.debug("No specific authentication selected.  Will use the OSGi-selected implementation.");
         }
         if (ref == null) {
             // Get the default, either because nothing is explicitly configured or because
             // the explicitly configured plugin is not available.
            ref = findServiceFromBundle(DEFAULT_AUTHENTICATION_SYSTEM_BUNDLE);
         }
         if (ref == null) {
             // Still null?  No auth systems available.
             throw new AuthenticationSystemLoadingFailure(
                 "No authentication system plugins available from the OSGi layer.  Plugins must be both installed and activated to be used.");
         }
         return ref;
     }
 
     // TODO: might be able to do this with an OSGi Filter instead
     private ServiceReference findServiceFromBundle(String desiredBundle) {
         log.debug("Searching for selected authentication system bundle \"{}\"", desiredBundle);
         ServiceReference[] refs;
         try {
             refs = getBundleContext().getServiceReferences(SERVICE_NAME, null);
         } catch (InvalidSyntaxException e) {
             throw new StudyCalendarSystemException("Unexpected exception when retrieving list of authentication systems", e);
         }
        if (refs == null) return null;
         for (ServiceReference serviceReference : refs) {
             if (serviceReference.getBundle().getSymbolicName().equals(desiredBundle)) {
                 log.debug("Found desired service \"{}\" in bundle {}",
                     desiredBundle, serviceReference.getBundle());
                 return serviceReference;
             }
         }
         return null;
     }
 
     private AuthenticationSystem acquireAuthenticationSystem() {
         return (AuthenticationSystem) getBundleContext().getService(newSystemReference);
     }
 
     ////// CONFIGURATION
 
     public void setBundleContext(BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     private BundleContext getBundleContext() {
         if (bundleContext == null) {
             throw new StudyCalendarSystemException(
                 "No bundle context available.  Authentication system cannot be configured.");
         }
         return bundleContext;
     }
 }
