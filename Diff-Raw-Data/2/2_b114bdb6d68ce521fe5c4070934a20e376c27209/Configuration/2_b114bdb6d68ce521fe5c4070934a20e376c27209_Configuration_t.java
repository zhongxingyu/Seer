 package org.lazydog.repository.ldap.internal;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import org.lazydog.repository.Entity;
 import org.lazydog.repository.ldap.internal.parser.EntityMappingsParser;
 import org.lazydog.repository.ldap.internal.parser.ParsingException;
 import org.lazydog.repository.ldap.internal.parser.PersistenceLDAPParser;
 import org.slf4j.LoggerFactory;
 
 import static org.lazydog.repository.ldap.internal.Validator.*;
 
 
 
 /**
  * Configuration.
  * 
  * @author  Ron Rickard
  */
 public final class Configuration {
 
     private static final List<Class<?>> SUPPORTED_TYPES = new ArrayList<Class<?>>() {
         private static final long serialVersionUID = 1L;
         {
             add(Entity.class);
             add(Integer.class);
             add(String.class);
         }
     };
     private Map<Class<?>,Map<String,String>> attributeReferentialIntegrityMap;
     private Directory directory;
     private Map<Class<?>,EntityFactory<?>> entityFactoryMap;
     private Map<Class<?>,Set<String>> objectClassValues;
     private Map<Class<?>,Map<String,String>> propertyAttributeMap;
     private Map<Class<?>,Map<String,FetchType>> propertyFetchTypeMap;
     private Map<Class<?>,Map<String,Class<?>>> propertyTargetEntityClassMap;
     private Map<Class<?>,String> searchBaseMap;
     private Map<Class<?>,SearchScope> searchScopeMap;
     
     /**
      * Private constructor.
      * 
      * @throws  ConfigurationException  if unable to get the configuration.
      */
     private Configuration() throws ConfigurationException {
 
         PersistenceLDAPParser persistenceLDAPParser;
 		
         try {
 			
             // Get the parser for the persistence LDAP file.
             persistenceLDAPParser = PersistenceLDAPParser.newInstance();
         }
         catch (ParsingException e) {
             throw new ConfigurationException(
                     "Unable to parse persistence LDAP file '" + e.getConfigurationPathname() + "'.", e);
         }
 		
         // Set the logging level.
         setLoggingLevel(persistenceLDAPParser);
 		
         try {
 			
             // Get the directory.
             this.directory = Directory.newInstance(getDirectoryEnvironment(persistenceLDAPParser));
         }
         catch (DirectoryException e) {
             throw new ConfigurationException("Unable to get the LDAP directory.", e);
         }
 
         // Initialize the maps.
         this.attributeReferentialIntegrityMap = new HashMap<Class<?>,Map<String,String>>();
         this.entityFactoryMap = new HashMap<Class<?>,EntityFactory<?>>();
         this.objectClassValues = new HashMap<Class<?>,Set<String>>();
         this.propertyAttributeMap = new HashMap<Class<?>,Map<String,String>>();
         this.propertyTargetEntityClassMap = new HashMap<Class<?>,Map<String,Class<?>>>();
         this.propertyFetchTypeMap = new HashMap<Class<?>,Map<String,FetchType>>();
         this.searchBaseMap = new HashMap<Class<?>,String>();
         this.searchScopeMap = new HashMap<Class<?>,SearchScope>();
 		
         // Loop through the entity mappings files.
         for (String mappingFileName : persistenceLDAPParser.getMappingFileNames()) {
 
             EntityMappingsParser entityMappingsParser;
 			
             try {
 				
                 // Get the parser for the entity mappings file.
                 entityMappingsParser = EntityMappingsParser.newInstance(mappingFileName);
             }
             catch (ParsingException e) {
                 throw new ConfigurationException(
                         "Unable to parse entity mappings file '" + e.getConfigurationPathname() + "'.", e);
             }
 			
             // Get the class name.
             String className = entityMappingsParser.getClassName();
 			
             Class<?> entityClass = null;
 			
             try {
 
                 // Get the entity class.
                 entityClass = Class.forName(className);
 
                 // Validate the entity class.
                 validEntityClass(entityClass);
             }
             catch (ClassNotFoundException e) {
                 throw new ConfigurationException("Unable to get the entity class " + className + ".", e);
             }
             catch (IllegalArgumentException e) {
                 throw new ConfigurationException("Invalid entity " + entityClass + ".", e);
             }
 			
             // Get the entity factory for the entity class.
             EntityFactory<?> entityFactory = EntityFactory.newInstance(entityClass, SUPPORTED_TYPES);
 
             // Add entries to the maps for the entity class.
             this.attributeReferentialIntegrityMap.put(entityClass, entityMappingsParser.getAttributeReferentialIntegrityMap());
             this.entityFactoryMap.put(entityClass, entityFactory);
             this.objectClassValues.put(entityClass, entityMappingsParser.getObjectClassValues());
             this.propertyAttributeMap.put(entityClass, entityMappingsParser.getPropertyAttributeMap());
             this.propertyTargetEntityClassMap.put(entityClass, getPropertyTargetEntityClasses(entityMappingsParser.getPropertyTargetEntityClassNameMap()));
             this.propertyFetchTypeMap.put(entityClass, entityMappingsParser.getPropertyFetchTypeMap());
             this.searchBaseMap.put(entityClass, entityMappingsParser.getSearchBase());
             this.searchScopeMap.put(entityClass, entityMappingsParser.getSearchScope());
         }
     }
 
     /**
      * Get the attribute name that maps to the property name for the entity class.
      * 
      * @param  entityClass   the entity class.
      * @param  propertyName  the property name.
      * 
      * @return  the attribute name.
      */
     public String getAttributeName(final Class<?> entityClass, final String propertyName) {
     	return this.propertyAttributeMap.get(entityClass).get(propertyName);
     }
     
     /**
      * Get the attribute names for the entity class.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the attribute names.
      */
     public Set<String> getAttributeNames(final Class<?> entityClass) {
         return new HashSet<String>(this.propertyAttributeMap.get(entityClass).values());
     }
     
     /**
      * Get the directory.
      * 
      * @return  the directory.
      */
     public Directory getDirectory() {
         return this.directory;
     }
 	
     /**
      * Get the directory environment.
      * 
      * @param  persistenceLDAPParser  the persistence LDAP parser.
      * 
      * @return  the directory environment.
      * 
      * @throws  ConfigurationException  if unable to get the directory environment.
      */
     private static Properties getDirectoryEnvironment(final PersistenceLDAPParser persistenceLDAPParser) throws ConfigurationException {
 
         // Initialize the directory environment.
         Properties environment = new Properties();
 
         // Check if this is a JNDI setup.
         if (persistenceLDAPParser.isJndiSetup()) {
 
             // Get the JNDI name.
             String jndiName = persistenceLDAPParser.getJndiName();
 
             try {
 
                 // Get the directory environment.
                 Context context = new InitialContext();
                 environment = (Properties)context.lookup(jndiName);
             }
             catch(NamingException e) {
                 throw new ConfigurationException(
                         "Unable to lookup directory environment with the JNDI name '" + jndiName + "'.", e);
             }
         }
 		
         // Otherwise, the directory environment was provided in the configuration file.
         else {
 
             // Get the directory environment.
             environment.setProperty(Directory.INITIAL_CONTEXT_FACTORY, persistenceLDAPParser.getInitialContextFactory());
             environment.setProperty(Directory.PROVIDER_URL, persistenceLDAPParser.getProviderUrl());
             environment.setProperty(Directory.SECURITY_AUTHENTICATION, persistenceLDAPParser.getSecurityAuthentication());
             environment.setProperty(Directory.SECURITY_CREDENTIALS, persistenceLDAPParser.getSecurityCredentials());
             environment.setProperty(Directory.SECURITY_PRINCIPAL, persistenceLDAPParser.getSecurityPrincipal());
         }
 
         return environment;
     }
 	
     /**
      * Get the entity classes.
      * 
      * @return  the entity classes.
      */
     public Set<Class<?>> getEntityClasses() {
         return this.entityFactoryMap.keySet();
     }
 
     /**
      * Get the entity factory for the entity class.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the entity factory.
      */
     @SuppressWarnings("unchecked")
    public <T> EntityFactory<T> getEntityFactory(final Class<T> entityClass) {
     	return (EntityFactory<T>)this.entityFactoryMap.get(entityClass);
     }
 
     /**
      * Get the fetch type for the property.
      * 
      * @param  entityClass   the entity class.
      * @param  propertyName  the property name.
      * 
      * @return  the fetch type.
      */
     public FetchType getFetchType(final Class<?> entityClass, final String propertyName) {
     	return this.propertyFetchTypeMap.get(entityClass).get(propertyName);
     }
 
     /**
      * Get the object class values for the entity class.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the object class values.
      */
     public Set<String> getObjectClassValues(final Class<?> entityClass) {
     	return this.objectClassValues.get(entityClass);
     }
 
     /**
      * Get the property name-attribute name map.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the property name-attribute name map.
      */
     public Map<String,String> getPropertyAttributeMap(final Class<?> entityClass) {
     	return this.propertyAttributeMap.get(entityClass);
     }
     
     /**
      * Get the property name that maps to the attribute name for the entity class.
      * 
      * @param  entityClass    the entity class.
      * @param  attributeName  the attribute name.
      * 
      * @return  the property name.
      */
     public String getPropertyName(final Class<?> entityClass, final String attributeName) {
 
         String foundPropertyName = null;
 
         // Get the property names.
         Set<String> propertyNames = this.getPropertyNames(entityClass);
 
         // Loop through the property names.
         for (String propertyName : propertyNames) {
 
             // Check if the attribute name for the property is the desired attribute name.
             if (this.getAttributeName(entityClass, propertyName).equals(attributeName)) {
                 foundPropertyName = propertyName;
                 break;
             }
         }
 
         return foundPropertyName;
     }
 	
     /**
      * Get the property names for the entity class.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the property names.
      */
     public Set<String> getPropertyNames(final Class<?> entityClass) {
     	return this.propertyAttributeMap.get(entityClass).keySet();
     }
 
     /**
      * Get the property target entity classes.
      * 
      * @param  propertyTargetEntityClassNames  the map of property target entity class names.
      * 
      * @return  the property target entity classes.
      * 
      * @throws  ConfigurationException  if unable to get the property target entity classes.
      */
     private static Map<String, Class<?>> getPropertyTargetEntityClasses(final Map<String,String> propertyTargetEntityClassNames) throws ConfigurationException {
 
         Map<String, Class<?>> propertyTargetEntityClasses = new HashMap<String, Class<?>>();
 		
         // Get the property names.
         Set<String> propertyNames = propertyTargetEntityClassNames.keySet();
 
         // Loop through the property names.
         for (String propertyName : propertyNames) {
 
             // Get the target entity class name.
             String targetEntityClassName = propertyTargetEntityClassNames.get(propertyName);
 
             Class<?> targetEntityClass = null;
 
             try {
 
                 // Get the target entity class.
                 targetEntityClass = Class.forName(targetEntityClassName);
 
                 // Validate the target entity class.
                 validEntityClass(targetEntityClass);
 
                 // Put the property name and target entity class on the map.
                 propertyTargetEntityClasses.put(propertyName, targetEntityClass);
             }
             catch(ClassNotFoundException e) {
                 throw new ConfigurationException(
                         "Unable to get the target entity class '" + targetEntityClassName + "' for property '" + propertyName + "'.", e);
             }
             catch(IllegalArgumentException e) {
                 throw new ConfigurationException("Invalid target entity " + targetEntityClass + ".", e);
             }
         }
 		
         return propertyTargetEntityClasses;
     }
 
     /**
      * Get the referential integrity attribute for the attribute.
      * 
      * @param  entityClass    the entity class.
      * @param  attributeName  the attribute name.
      * 
      * @return  the referential integrity attribute.
      */
     public String getReferentialIntegrityAttribute(final Class<?> entityClass, final String attributeName) {
         return this.attributeReferentialIntegrityMap.get(entityClass).get(attributeName);
     }
 
     /**
      * Get the attribute name-referential integrity attribute name map for the entity class.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the attribute name-referential integrity attribute name map.
      */
     public Map<String,String> getReferentialIntegrityMap(final Class<?> entityClass) {
         return this.attributeReferentialIntegrityMap.get(entityClass);
     }
 	
     /**
      * Get the search base for the entity class.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the search base.
      */
     public String getSearchBase(final Class<?> entityClass) {
     	return this.searchBaseMap.get(entityClass);
     }
 
     /**
      * Get the search scope for the entity class.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return the search scope.
      */
     public SearchScope getSearchScope(final Class<?> entityClass) {
         return this.searchScopeMap.get(entityClass);
     }
     
     /**
      * Get the target entity class for the property.
      * 
      * @param  entityClass   the entity class.
      * @param  propertyName  the property name.
      * 
      * @return  the target entity class.
      */
     public Class<?> getTargetEntityClass(final Class<?> entityClass, final String propertyName) {
     	return this.propertyTargetEntityClassMap.get(entityClass).get(propertyName);
     }
     
     /**
      * Is the property an entity type?
      * 
      * @param  entityClass   the entity class.
      * @param  propertyName  the property name.
      * 
      * @return  true if the property is an entity type, otherwise false.
      */
     public boolean isEntityType(final Class<?> entityClass, final String propertyName) {
     	return (this.propertyTargetEntityClassMap.get(entityClass).get(propertyName) != null) ? true : false;
     }
 
     /**
      * Create a new instance of this class.
      * 
      * @return  a new instance of this class.
      * 
      * @throws  ConfigurationException  if unable to get the configuration.
      */
     public static Configuration newInstance() throws ConfigurationException {
         return new Configuration();
     }
     
     /**
      * Set the logging level.
      * 
      * @param  persistenceLDAPParser  the persistence LDAP parser.
      */
     private void setLoggingLevel(final PersistenceLDAPParser persistenceLDAPParser) {
     	
     	// Get the logging level.
     	Level level = Level.toLevel(persistenceLDAPParser.getLoggingLevel().toString());
     	
     	// Set the Configuration class logging level.
     	Logger logger = (Logger)LoggerFactory.getLogger(Configuration.class);
         logger.setLevel(level);
 
         // Set the Directory class logging level.
         logger = (Logger)LoggerFactory.getLogger(Directory.class);
         logger.setLevel(level);
 
         // Set the EntityFactory class logging level.
         logger = (Logger)LoggerFactory.getLogger(EntityFactory.class);
         logger.setLevel(level);
 
         // Check if the logging level is on.
         if (level != Level.OFF) {
 
             // Turn on logging of the exceptions.
             ConfigurationException.setLogState(true);
             DirectoryException.setLogState(true);
             EntityFactoryException.setLogState(true);
         }
     }
 }
