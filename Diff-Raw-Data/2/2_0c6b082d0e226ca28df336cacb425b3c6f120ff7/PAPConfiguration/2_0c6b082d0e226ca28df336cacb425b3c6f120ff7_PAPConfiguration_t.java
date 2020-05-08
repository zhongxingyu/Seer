 package org.glite.authz.pap.common;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.INIConfiguration;
 import org.glite.authz.pap.common.exceptions.NullArgumentException;
 import org.glite.authz.pap.common.exceptions.PAPConfigurationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** The configuration implementation for the PAP daemon and client **/
 public class PAPConfiguration {
     
     /** Default directory prefix under which the authorization service apps are expected to live **/ 
     private static final String DEFAULT_AUTHZ_PATH_PREFIX = "/opt/authz";
     
     /** Default name for PAP home directory **/
     private static final String DEFAULT_PAP_HOMEDIR_NAME = "pap";
     
     /** Default directory path for PAP home directory **/
    private static final String DEFAULT_PAP_HOME = DEFAULT_AUTHZ_PATH_PREFIX + "/"+ DEFAULT_PAP_HOMEDIR_NAME; 
     
     /** Default directory under which the PAP will look for its configuration **/
     private static final String DEFAULT_PAP_CONFIGURATION_DIR = DEFAULT_PAP_HOME+"/conf";
     
     /** Default directory under which the PAP will look for its repository **/
     private static final String DEFAULT_PAP_REPOSITORY_DIR = DEFAULT_PAP_HOME+"/repository";
 
     /** The default file name for the PAP authorization layer configuration file **/ 
     private static final String DEFAULT_PAP_AUTHZ_FILE_NAME = "pap_authorization.ini";
     
     /** The default pap configuration file name **/ 
     private static final String DEFAULT_PAP_CONFIGURATION_FILE_NAME = "pap_configuration.ini";
     
     /** The prefix to be used to pick out monitoring properties out of this configuration **/
     public static final String MONITORING_PROPERTY_PREFIX = "pap-monitoring";
     
     /** The name of the stanza that contains repository configuration **/
     public static final String REPOSITORY_STANZA = "repository";
     
     /** The name of the stanza that contains standalone service configuration **/
     public static final String STANDALONE_SERVICE_STANZA = "standalone-service";
     
     /** The name of the stanza that contains local and remote paps configuration **/
     public static final String PAPS_STANZA = "paps";
     
     /** The name of the stanza that contains security configuration **/
     public static final String SECURITY_STANZA = "security";
     
     /** The default context for the PAP web application **/
     public static final String DEFAULT_WEBAPP_CONTEXT = "pap";
 
     final static Logger logger = LoggerFactory
             .getLogger( PAPConfiguration.class );
 
     /** The singleton PAPConfiguration instance **/
     private static PAPConfiguration instance;
 
     /** The Commons configuration base object that stores the actual configuration properties and provides a unified facade to multiple 
      *  configuration files
      **/
     private CompositeConfiguration configuration;
 
     /** The PAP startup configuration object**/
     private INIConfiguration startupConfiguration;
     
     
     
 
     /**
      * Constructor
      * 
      * @param papConfigurationDir, the directory in which the PAP configuration files will be searched for
      */
     private PAPConfiguration( String papConfigurationDir ) {
 
         configuration = new CompositeConfiguration();
 
         if ( papConfigurationDir == null ){
             
             logger.warn( "No configuration dir defined for PAP configuration initialization!" );
             
             if (System.getProperty( "PAP_HOME" ) != null){
                 logger.info( "PAP_HOME system property defined. Will look in $PAP_HOME/conf for the configuration file!" );
                 configuration.setProperty( "papConfigurationDir", System.getProperty( "PAP_HOME" )+"/conf" );
             }else{
                 logger.warn( "Using hardcoded configuration directory: "+ DEFAULT_PAP_CONFIGURATION_DIR );
                 configuration.setProperty( "papConfigurationDir", DEFAULT_PAP_CONFIGURATION_DIR );
             }
         }else
             configuration.setProperty( "papConfigurationDir", papConfigurationDir );
         
         loadStartupConfiguration();
 
     }
 
 
     /**
      * Returns a PAPConfiguration object, if properly initialized.
      * 
      * @return the active instance of the PAPConfiguration class
      * @throws PAPConfigurationException, in case the PAPConfiguration hasn't been properly initialized with the initialize method. 
      */
     public static PAPConfiguration instance() {
 
         if ( instance == null )
             throw new PAPConfigurationException(
                     "Please initialize configuration before calling the instance method!" );
 
         return instance;
 
     }
 
 
     /**
      * Initializes the configuration for this PAP.
      * 
      * @param context, a ServletContext that defines suitable defaults for configuration and repository directories 
      * @return the active instance of the PAPConfiguration class
      */
     public static PAPConfiguration initialize( ServletContext context ) {
         
         if ( context == null )
             throw new NullArgumentException(
                     "Please provide a value for the 'context' argument! null is not a valid value in this context." );
 
         if ( instance == null ) {
             
             String papConfDir = context
                     .getInitParameter( "papConfigurationDir" );
             
             return initialize( papConfDir );
         }
 
         return instance;
     }
 
     /**
      * Initializes the configuration for this PAP.
      * 
      * @return the active instance of the PAPConfiguration class
      */
     public static PAPConfiguration initialize() {
 
         if ( instance == null )
             return initialize( (String)null );
 
         return instance;
 
     }
 
 
     /**
      * Initializes the configuration for this PAP.
      * 
      * 
      * @param papConfigurationDir, the directory in which the PAP configuration files will be searched for 
      * 
      * @return a PAPConfiguration object
      */
     public static PAPConfiguration initialize( String papConfigurationDir ) {
 
         if ( instance == null )
             instance = new PAPConfiguration( papConfigurationDir);
 
         return instance;
     }
 
     /**
      * Loads the PAP startup configuration.
      */
     private void loadStartupConfiguration() {
 
         logger.info( "Loading pap startup configuration..." );
         String papConfDir = configuration.getString( "papConfigurationDir" );
 
         File papConfFile = new File( papConfDir + "/"
                 + DEFAULT_PAP_CONFIGURATION_FILE_NAME);
 
         if ( !papConfFile.exists() )
             throw new PAPConfigurationException(
                     "PAP startup configuration file does not exists on path:"
                             + papConfFile.getAbsolutePath() );
 
         try {
 
             startupConfiguration = new INIConfiguration( papConfFile );
             configuration.addConfiguration( startupConfiguration );
             
             
 
         } catch ( org.apache.commons.configuration.ConfigurationException e ) {
 
             logger.error( "Error parsing PAP distribution configuration: "
                     + e.getMessage(), e );
             throw new PAPConfigurationException(
                     "Error parsing PAP distribution configuration: "
                             + e.getMessage(), e );
 
         }
 
     }
 
     // Getter methods that access the configuration object and other services
     // down here
 
     /** Returns the default PAP configuration directory path **/
     public String getPAPConfigurationDir() {
 
         return configuration.getString( "papConfigurationDir" );
 
     }
 
     /** Returns the default PAP repository directory path **/
     public String getPAPRepositoryDir() {
 
         return configuration.getString( REPOSITORY_STANZA+ ".location", System.getProperty( "PAP_HOME" )+"/repository" );
     }
 
     /** Returns the default PAP authorization configuration file name **/
     public String getPapAuthzConfigurationFileName() {
 
         return getPAPConfigurationDir() + "/" + DEFAULT_PAP_AUTHZ_FILE_NAME;
     }
 
 
     /** {@inheritDoc} **/
     public BigDecimal getBigDecimal( String key, BigDecimal defaultValue ) {
 
         return configuration.getBigDecimal( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public BigDecimal getBigDecimal( String key ) {
 
         return configuration.getBigDecimal( key );
     }
 
     /** {@inheritDoc} **/
     public BigInteger getBigInteger( String key, BigInteger defaultValue ) {
 
         return configuration.getBigInteger( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public BigInteger getBigInteger( String key ) {
 
         return configuration.getBigInteger( key );
     }
     
     /** {@inheritDoc} **/
     public boolean getBoolean( String key, boolean defaultValue ) {
 
         return configuration.getBoolean( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public Boolean getBoolean( String key, Boolean defaultValue ) {
 
         return configuration.getBoolean( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public boolean getBoolean( String key ) {
 
         return configuration.getBoolean( key );
     }
 
     /** {@inheritDoc} **/
     public byte getByte( String key, byte defaultValue ) {
 
         return configuration.getByte( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public Byte getByte( String key, Byte defaultValue ) {
 
         return configuration.getByte( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public byte getByte( String key ) {
 
         return configuration.getByte( key );
     }
 
     /** {@inheritDoc} **/
     public double getDouble( String key, double defaultValue ) {
 
         return configuration.getDouble( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public Double getDouble( String key, Double defaultValue ) {
 
         return configuration.getDouble( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public double getDouble( String key ) {
 
         return configuration.getDouble( key );
     }
 
     /** {@inheritDoc} **/
     public float getFloat( String key, float defaultValue ) {
 
         return configuration.getFloat( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public Float getFloat( String key, Float defaultValue ) {
 
         return configuration.getFloat( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public float getFloat( String key ) {
 
         return configuration.getFloat( key );
     }
 
     /** {@inheritDoc} **/
     public int getInt( String key, int defaultValue ) {
 
         return configuration.getInt( key, defaultValue );
     }
     
     /** {@inheritDoc} **/
     public int getInt( String key ) {
 
         return configuration.getInt( key );
     }
     
     /** {@inheritDoc} **/
     public Integer getInteger( String key, Integer defaultValue ) {
 
         return configuration.getInteger( key, defaultValue );
     }
     
     /** {@inheritDoc} **/
     public Iterator getKeys() {
 
         return configuration.getKeys();
     }
 
     /** {@inheritDoc} **/
     public Iterator getKeys( String key ) {
 
         return configuration.getKeys( key );
     }
     
     /** {@inheritDoc} **/
     public List getList( String key, List defaultValue ) {
 
         return configuration.getList( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public List getList( String key ) {
 
         return configuration.getList( key );
     }
     
     /** {@inheritDoc} **/
     public long getLong( String key, long defaultValue ) {
 
         return configuration.getLong( key, defaultValue );
     }
     
     /** {@inheritDoc} **/
     public Long getLong( String key, Long defaultValue ) {
 
         return configuration.getLong( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public long getLong( String key ) {
 
         return configuration.getLong( key );
     }
 
     /** {@inheritDoc} **/
     public Properties getProperties( String key, Properties defaults ) {
 
         return configuration.getProperties( key, defaults );
     }
 
     /** {@inheritDoc} **/
     public Properties getProperties( String key ) {
 
         return configuration.getProperties( key );
     }
 
     /** {@inheritDoc} **/
     public Object getProperty( String key ) {
 
         return configuration.getProperty( key );
     }
 
     /** {@inheritDoc} **/
     public short getShort( String key, short defaultValue ) {
 
         return configuration.getShort( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public Short getShort( String key, Short defaultValue ) {
 
         return configuration.getShort( key, defaultValue );
     }
     
     /** {@inheritDoc} **/
     public short getShort( String key ) {
 
         return configuration.getShort( key );
     }
 
     /** {@inheritDoc} **/
     public String getString( String key, String defaultValue ) {
 
         return configuration.getString( key, defaultValue );
     }
 
     /** {@inheritDoc} **/
     public String getString( String key ) {
 
         return configuration.getString( key );
     }
 
     /** {@inheritDoc} **/
     public String[] getStringArray( String key ) {
 
         return configuration.getStringArray( key );
     }
 
     /** {@inheritDoc} **/
     public void clearDistributionProperty( String key ) {
 
         configuration.clearProperty( key );
     }
 
     public void setDistributionProperty( String key, Object value ) {
 
         startupConfiguration.setProperty( key, value );
     }
 
     public void saveStartupConfiguration() {
 
         try {
 
             startupConfiguration.save();
 
         } catch ( ConfigurationException e ) {
 
             throw new PAPConfigurationException(
                     "Error saving policy distribution configuration: "
                             + e.getMessage(), e );
         }
 
     }
 
     /**
      * @param prefix
      * @return
      * @see org.apache.commons.configuration.AbstractConfiguration#subset(java.lang.String)
      */
     public Configuration subset( String prefix ) {
 
         return configuration.subset( prefix );
     }
    
     /**
      * Sets a monitoring property in this configuration. A specific prefix
      * is actually prefixed to the property name by this method
      * 
      * @param name, the name of the property
      * @param value, the value of the property
      * @see #MONITORING_PROPERTY_PREFIX
      */
     public void setMonitoringProperty(String name, Object value){
         
         configuration.setProperty( MONITORING_PROPERTY_PREFIX+"."+name, value );
     }
     
     /**
      * Gets a monitoring property in this configuration. A specific prefix
      * is actually prefixed to the property name by this method
      * 
      * @param name, the name of the property
      * @return
      * @see #MONITORING_PROPERTY_PREFIX
      */
     public Object getMonitoringProperty(String name){
         
         return configuration.getProperty( MONITORING_PROPERTY_PREFIX+"."+name );
     }
 
     
 }
