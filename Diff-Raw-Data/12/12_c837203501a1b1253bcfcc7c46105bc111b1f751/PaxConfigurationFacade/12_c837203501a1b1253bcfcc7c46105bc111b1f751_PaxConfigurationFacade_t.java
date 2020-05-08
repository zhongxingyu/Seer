 /*
  * Copyright 2006 Edward Yakop.
  *
  * Licensed  under the  Apache License,  Version 2.0  (the "License");
  * you may not use  this file  except in  compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed  under the  License is distributed on an "AS IS" BASIS,
  * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
  * implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.ops4j.pax.cm.agent.configuration;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.ops4j.lang.NullArgumentException;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 /**
  * {@code PaxConfigurationFacade} provides convenient methods to retrieve, delete and update a configuration.
  * {@code PaxConfigurationFacade} must be initialized by invoking {@code setContext( bundleContext )} before used.
  *
  * @author Edward Yakop
  * @since 0.1.0
  */
 public final class PaxConfigurationFacade
 {
 
     private static final Log m_logger = LogFactory.getLog( PaxConfigurationFacade.class );
 
     private static final String CONFIGURATION_ADMIN_CLASSNAME = ConfigurationAdmin.class.getName();
 
     private static BundleContext m_bundleContext;
 
     /**
      * This is an internal API
      *
      * @since 0.1.0
      */
     public static void setContext( BundleContext bundleContext )
     {
         synchronized( PaxConfigurationFacade.class )
         {
             m_bundleContext = bundleContext;
         }
     }
 
     /**
      * Delete the specified {@code configuration} from configuration admin service.
      *
      * @param paxConfiguration The pax configuration. This argument must not be {@code null}.
      *
      * @throws ConfigurationAdminServiceIsNotAvailableException
      *                                  Thrown if configuration admin service is not available.
      * @throws IllegalArgumentException Thrown if the specified {@code paxConfiguration} argument is {@code null}.
      * @throws PaxConfigurationFacadeIsNotInitializedException
      *                                  Thrown if {@code PaxConfigurationFacade} is not initialized.
      * @see PaxConfigurationFacade#setContext(org.osgi.framework.BundleContext)
      * @since 0.1.0
      */
     public static void deleteConfiguration( PaxConfiguration paxConfiguration )
         throws ConfigurationAdminServiceIsNotAvailableException,
                ConfigurationDeletionFailedException,
                IllegalArgumentException,
                IOException,
                PaxConfigurationFacadeIsNotInitializedException
     {
         NullArgumentException.validateNotNull( paxConfiguration, "paxConfiguration" );
 
         if( paxConfiguration.isNew() )
         {
             return;
         }
 
         String pid = paxConfiguration.getPid();
        Configuration configuration;
        if( pid != null )
        {
            configuration = getConfigurationByPID( pid, false );
        }
        else
        {
            String factoryPid = paxConfiguration.getFactoryPid();
            configuration = getConfigurationByPID( factoryPid, true );
        }

         try
         {
             configuration.delete();
         } catch( IOException e )
         {
             throw new ConfigurationDeletionFailedException( pid, e );
         }
     }
 
     private static Configuration getConfigurationByPID( String pid, boolean isFactory )
         throws ConfigurationAdminServiceIsNotAvailableException,
                IOException,
                PaxConfigurationFacadeIsNotInitializedException
     {
         synchronized( PaxConfigurationFacade.class )
         {
             if( m_bundleContext == null )
             {
                 throw new PaxConfigurationFacadeIsNotInitializedException();
             }
 
             ServiceReference reference = m_bundleContext.getServiceReference( CONFIGURATION_ADMIN_CLASSNAME );
             if( reference == null )
             {
                 throw new ConfigurationAdminServiceIsNotAvailableException();
             }
 
             ConfigurationAdmin configAdmin = (ConfigurationAdmin) m_bundleContext.getService( reference );
 
             try
             {
                 if( !isFactory )
                 {
                     try
                     {
                         return configAdmin.getConfiguration( pid );
                     } catch( RuntimeException re )
                     {
                         m_logger.error( "Configuration [" + pid + "] failed to retreived.", re );
                         throw re;
                     }
                 }
                 else
                 {
                     return configAdmin.createFactoryConfiguration( pid );
                 }
             }
             finally
             {
                 m_bundleContext.ungetService( reference );
             }
         }
     }
 
     /**
      * Returns a new instance of {@code PaxConfiguration} of the specified {@code pid}.
      *
      * @param pid The configuration PID. This argument must not be {@code null} or empty.
      *
      * @throws ConfigurationAdminServiceIsNotAvailableException
      *                                  Thrown if configuration admin service is not available.
      * @throws IllegalArgumentException Thrown if the specified {@code pid} argument is either empty or {@code null}.
      * @throws PaxConfigurationFacadeIsNotInitializedException
      *                                  Thrown if {@code PaxConfigurationFacade} is not initialized.
      * @see PaxConfigurationFacade#setContext(org.osgi.framework.BundleContext)
      * @since 0.1.0
      */
     public static PaxConfiguration getConfiguration( String pid, boolean isFactory )
         throws ConfigurationAdminServiceIsNotAvailableException,
                IllegalArgumentException,
                IOException,
                PaxConfigurationFacadeIsNotInitializedException
     {
         NullArgumentException.validateNotEmpty( pid, "pid" );
         Configuration configuration = getConfigurationByPID( pid, isFactory );
         return new PaxConfiguration( configuration );
     }
 
     /**
      * Update a configuration specified by {@code paxConfiguration} argument to configuration admin service.
      *
      * @param paxConfiguration The pax configuration data to be persisted to configuration admin service. This argument
      *                         must not be {@code null}.
      *
      * @throws IOException              Thrown if configuration admin service failed to persist the updated
      *                                  configuration.
      * @throws IllegalArgumentException Thrown if the specified {@code paxConfiguration} is {@code null} or contains
      *                                  illegal properties.
      * @throws IllegalStateException    Thrown if the configuration represented by the specified
      *                                  {@code paxConfiguration} has been deleted.
      * @since 0.1.0
      */
     public static void updateConfiguration( PaxConfiguration paxConfiguration )
         throws IOException, IllegalArgumentException, IllegalStateException
     {
         NullArgumentException.validateNotNull( paxConfiguration, "paxConfiguration" );
 
         Configuration configuration;
         boolean isConfigurationNew = paxConfiguration.isNew();
         if( isConfigurationNew )
         {
             String pid = paxConfiguration.getFactoryPid();
             boolean isFactory = pid != null;
             if( !isFactory )
             {
                 pid = paxConfiguration.getPid();
             }
             configuration = getConfigurationByPID( pid, isFactory );
             paxConfiguration.setIsNew( false );
         }
         else
         {
             String pid = paxConfiguration.getPid();
             configuration = getConfigurationByPID( pid, false );
         }
 
         // Copy properties across
         String bundleLocation = paxConfiguration.getBundleLocation();
         configuration.setBundleLocation( bundleLocation );
 
         Dictionary properties = paxConfiguration.getProperties();
         configuration.update( properties );
     }
 
     private PaxConfigurationFacade()
     {
     }
 }
