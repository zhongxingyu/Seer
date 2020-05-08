 package org.ops4j.pax.configmanager.internal;
 
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.ops4j.lang.NullArgumentException;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.Configuration;
 
 /**
  * The {@code ConfigurationAdmin} service will maintain 0 or more {@code Configuration} 
  * objects for a {@code ManagedServiceFactory}. Thus, the contents of the 
  * properties file must contain information to initialize several instances of one
  * service.
  * <p>
  * An example of the contents of the properties file (in no particular order):
  * <pre>
  * service.pid=org.ops4j.pax.counterservice [OPTIONAL]
  * instances=2 [REQUIRED]
  * keys=userid,password,comment [REQUIRED]
  * userid.1=simone
  * password.1=beauvoir
  * comment.1=feminist from Paris
  * userid.2=edith
  * password.2=piaf
  * </pre>
  *
  * @author Gavin
  */
 final class ManagedFactoryPropertiesProcessor
 {
     private static final Log LOGGER = LogFactory.getLog( ManagedFactoryPropertiesProcessor.class );
     public final static String[] EMPTY_STRING_ARRAY = new String[0];
     
     public ManagedFactoryPropertiesProcessor()
     {
     }
 
     /**
      * @param configAdminService 
      * @param servicePid the contents of key {@code service.pid} or the name of the property file.
      * 
      * @throws IllegalArgumentException if any one of the parameters is null
      */
     final void process( ConfigurationAdmin configAdminService, String servicePid, Properties prop )
     {
         NullArgumentException.validateNotNull( configAdminService, "configAdminService" );
         NullArgumentException.validateNotNull( servicePid, "servicePid" );
         
         if( ! validate( prop ))
             return; // noop
         
         int numberOfInstances = getInstanceCount( prop );
         String[] keys = getKeys( prop );
 
         for( int i=1; i<numberOfInstances+1; i++ )
         {
             Properties instanceProps = filterProps( keys, prop, i );
             LOGGER.debug( instanceProps );
             try
             {
                 String filter = "( &(service.factoryPid=" + servicePid + ") (service.pid=" + servicePid + "." + (i-1) + ") )";
                 LOGGER.debug( "filter = " + filter );
                 Configuration[] configs = configAdminService.listConfigurations( filter );
                 
                 Configuration conf;
                 
                 if( configs != null && configs.length > 0 )
                 {
                     conf = configs[ 0 ];
                     LOGGER.debug( "\tfound -> " + conf.getPid() );
                 }
                 else
                 {
                     conf = configAdminService.createFactoryConfiguration( servicePid, null );
                     LOGGER.debug( "\tcreate -> " + conf.getPid() );
                 }
 
                 conf.update( instanceProps );
 
             } catch( Exception e )
             {
                 LOGGER.error( e );
                 continue;
             }                        
         }
     }
 
     private final Properties filterProps( String[] keys, Properties prop, int instance )
     {
         Properties retval = new Properties();
         
         for( int i=0; i<keys.length; i++)
         {
             String key = keys[i] + "." + instance;            
             String value = prop.getProperty( key );
             
             if( value == null )
                 continue;
             
            retval.put( keys[i], value );
         }
         
         return retval;
     }
 
     /**
      * @param prop
      * @return -1 if {@code instances} key is invalid/missing. Otherwise a positive integer
      */
     private final int getInstanceCount( Properties prop )
     {
         if( !prop.containsKey( "instances" ) )
             return -1;
 
         String instancesRaw = ( String ) prop.get( "instances" );
 
         try
         {
             Integer instances = Integer.valueOf( instancesRaw );
 
             if( instances.intValue() <= 0 )
             {
                 return -1;
             }
             return instances;
             
         } catch( NumberFormatException nfe )
         {
             return -1;
         }
     }
     
     private final String[] getKeys( Properties prop )
     {
         if( !prop.containsKey( "keys" ) )
             return EMPTY_STRING_ARRAY;
         
         String keysRaw = ( String ) prop.get( "keys" );
         if( keysRaw == null || keysRaw.trim().length() == 0 )
             return EMPTY_STRING_ARRAY;
 
         String[] keys = keysRaw.trim().split( "," );
         if( keys.length == 0 )
             return EMPTY_STRING_ARRAY;
         
         return keys;
     }
     
     private final boolean validate( Properties prop )
     {
         /**
          * Condition #1:
          * 
          * MUST contain key {@code instances} with an integer value && value >= 1
          */ 
         int instances = getInstanceCount( prop );
         
         if( instances == -1 )
             return false;
         
         /**
          * Condition #2:
          * 
          * MUST contain key {@code keys} with at least one item
          */        
         String[] keys = getKeys( prop );
         if ( keys.length == 0 )
             return false;
         
         return true;
     }
 
 }
