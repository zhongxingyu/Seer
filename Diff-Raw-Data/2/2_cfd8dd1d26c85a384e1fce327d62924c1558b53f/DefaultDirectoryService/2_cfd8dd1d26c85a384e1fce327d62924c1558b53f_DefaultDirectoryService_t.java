 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License. 
  *  
  */
 package org.apache.directory.server.core;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 
 import org.apache.directory.server.core.authz.AuthorizationService;
 import org.apache.directory.server.core.configuration.Configuration;
 import org.apache.directory.server.core.configuration.ConfigurationException;
 import org.apache.directory.server.core.configuration.MutablePartitionConfiguration;
 import org.apache.directory.server.core.configuration.PartitionConfiguration;
 import org.apache.directory.server.core.configuration.StartupConfiguration;
 import org.apache.directory.server.core.interceptor.Interceptor;
 import org.apache.directory.server.core.interceptor.InterceptorChain;
 import org.apache.directory.server.core.interceptor.context.AddContextPartitionOperationContext;
 import org.apache.directory.server.core.interceptor.context.AddOperationContext;
 import org.apache.directory.server.core.interceptor.context.EntryOperationContext;
 import org.apache.directory.server.core.interceptor.context.LookupOperationContext;
 import org.apache.directory.server.core.jndi.AbstractContextFactory;
 import org.apache.directory.server.core.jndi.DeadContext;
 import org.apache.directory.server.core.jndi.ServerLdapContext;
 import org.apache.directory.server.core.partition.DefaultPartitionNexus;
 import org.apache.directory.server.core.partition.PartitionNexus;
 import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
 import org.apache.directory.server.core.schema.PartitionSchemaLoader;
 import org.apache.directory.server.core.schema.SchemaManager;
 import org.apache.directory.server.core.schema.SchemaPartitionDao;
 import org.apache.directory.server.schema.SerializableComparator;
 import org.apache.directory.server.schema.bootstrap.ApacheSchema;
 import org.apache.directory.server.schema.bootstrap.ApachemetaSchema;
 import org.apache.directory.server.schema.bootstrap.BootstrapSchemaLoader;
 import org.apache.directory.server.schema.bootstrap.CoreSchema;
 import org.apache.directory.server.schema.bootstrap.Schema;
 import org.apache.directory.server.schema.bootstrap.SystemSchema;
 import org.apache.directory.server.schema.bootstrap.partition.DbFileListing;
 import org.apache.directory.server.schema.bootstrap.partition.SchemaPartitionExtractor;
 import org.apache.directory.server.schema.registries.AttributeTypeRegistry;
 import org.apache.directory.server.schema.registries.DefaultOidRegistry;
 import org.apache.directory.server.schema.registries.DefaultRegistries;
 import org.apache.directory.server.schema.registries.OidRegistry;
 import org.apache.directory.server.schema.registries.Registries;
 import org.apache.directory.shared.ldap.constants.JndiPropertyConstants;
 import org.apache.directory.shared.ldap.constants.SchemaConstants;
 import org.apache.directory.shared.ldap.constants.ServerDNConstants;
 import org.apache.directory.shared.ldap.exception.LdapAuthenticationNotSupportedException;
 import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
 import org.apache.directory.shared.ldap.exception.LdapNamingException;
 import org.apache.directory.shared.ldap.exception.LdapNoPermissionException;
 import org.apache.directory.shared.ldap.ldif.Entry;
 import org.apache.directory.shared.ldap.message.AttributeImpl;
 import org.apache.directory.shared.ldap.message.AttributesImpl;
 import org.apache.directory.shared.ldap.message.ResultCodeEnum;
 import org.apache.directory.shared.ldap.name.LdapDN;
 import org.apache.directory.shared.ldap.schema.AttributeType;
 import org.apache.directory.shared.ldap.schema.OidNormalizer;
 import org.apache.directory.shared.ldap.util.DateUtils;
 import org.apache.directory.shared.ldap.util.StringTools;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Default implementation of {@link DirectoryService}.
  * 
  * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
  */
 class DefaultDirectoryService extends DirectoryService
 {
     private static final Logger log = LoggerFactory.getLogger( DefaultDirectoryService.class );
     private static final String BINARY_KEY = JndiPropertyConstants.JNDI_LDAP_ATTRIBUTES_BINARY;
 
     private final String instanceId;
 
     private final DirectoryServiceConfiguration configuration = new DefaultDirectoryServiceConfiguration( this );
 
     private DirectoryServiceListener serviceListener;
     
     private SchemaManager schemaManager;
 
     /** the initial context environment that fired up the backend subsystem */
     private Hashtable<String, Object> environment;
 
     /** the configuration */
     private StartupConfiguration startupConfiguration;
 
     /** the registries for system schema objects */
     private Registries registries;
 
     /** the root nexus */
     private DefaultPartitionNexus partitionNexus;
 
     /** whether or not server is started for the first time */
     private boolean firstStart;
 
     /** The interceptor (or interceptor chain) for this service */
     private InterceptorChain interceptorChain;
 
     /** whether or not this instance has been shutdown */
     private boolean started = false;
 
 
     // ------------------------------------------------------------------------
     // Constructor
     // ------------------------------------------------------------------------
 
     /**
      * Creates a new instance.
      */
     public DefaultDirectoryService( String instanceId )
     {
         if ( instanceId == null )
         {
             throw new NullPointerException( "instanceId" );
         }
         this.instanceId = instanceId;
     }
 
 
     // ------------------------------------------------------------------------
     // BackendSubsystem Interface Method Implemetations
     // ------------------------------------------------------------------------
 
     public Context getJndiContext( String rootDN ) throws NamingException
     {
         return this.getJndiContext( null, null, null, "none", rootDN );
     }
 
 
     public synchronized Context getJndiContext( LdapDN principalDn, String principal, byte[] credential, 
         String authentication, String rootDN ) throws NamingException
     {
         checkSecuritySettings( principal, credential, authentication );
 
         if ( !started )
         {
             return new DeadContext();
         }
 
         Hashtable<String, Object> environment = getEnvironment();
         environment.remove( Context.SECURITY_PRINCIPAL );
         environment.remove( Context.SECURITY_CREDENTIALS );
         environment.remove( Context.SECURITY_AUTHENTICATION );
 
         if ( principal != null )
         {
             environment.put( Context.SECURITY_PRINCIPAL, principal );
         }
 
         if ( credential != null )
         {
             environment.put( Context.SECURITY_CREDENTIALS, credential );
         }
 
         if ( authentication != null )
         {
             environment.put( Context.SECURITY_AUTHENTICATION, authentication );
         }
 
         if ( rootDN == null )
         {
             rootDN = "";
         }
         environment.put( Context.PROVIDER_URL, rootDN );
         
         return new ServerLdapContext( this, environment );
     }
 
 
     @SuppressWarnings("unchecked")
     public synchronized void startup( DirectoryServiceListener listener, Hashtable env ) throws NamingException
     {
         if ( started )
         {
             return;
         }
 
         Hashtable<String,Object> envCopy = ( Hashtable ) env.clone();
 
         StartupConfiguration cfg = ( StartupConfiguration ) Configuration.toConfiguration( env );
 
         if ( cfg.isShutdownHookEnabled() )
         {
             Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
             {
                 public void run()
                 {
                     try
                     {
                         shutdown();
                     }
                     catch ( NamingException e )
                     {
                         log.warn( "Failed to shut down the directory service: "
                             + DefaultDirectoryService.this.instanceId, e );
                     }
                 }
             }, "ApacheDS Shutdown Hook (" + instanceId + ')' ) );
 
             log.info( "ApacheDS shutdown hook has been registered with the runtime." );
         }
         else if ( log.isWarnEnabled() )
         {
             log.warn( "ApacheDS shutdown hook has NOT been registered with the runtime."
                 + "  This default setting for standalone operation has been overriden." );
         }
 
         envCopy.put( Context.PROVIDER_URL, "" );
 
         try
         {
             cfg.validate();
         }
         catch ( ConfigurationException e )
         {
             NamingException ne = new LdapConfigurationException( "Invalid configuration." );
             ne.initCause( e );
             throw ne;
         }
 
         this.environment = envCopy;
         this.startupConfiguration = cfg;
 
         listener.beforeStartup( this );
 
         initialize();
         firstStart = createBootstrapEntries();
         showSecurityWarnings();
         this.serviceListener = listener;
         started = true;
         
         if ( !startupConfiguration.getTestEntries().isEmpty() )
         {
             createTestEntries( env );
         }
         
         listener.afterStartup( this );
     }
 
 
     public synchronized void sync() throws NamingException
     {
         if ( !started )
         {
             return;
         }
 
         serviceListener.beforeSync( this );
         try
         {
             this.partitionNexus.sync();
         }
         finally
         {
             serviceListener.afterSync( this );
         }
     }
 
 
     public synchronized void shutdown() throws NamingException
     {
         if ( !started )
         {
             return;
         }
 
         serviceListener.beforeShutdown( this );
         try
         {
             this.partitionNexus.sync();
             this.partitionNexus.destroy();
             this.interceptorChain.destroy();
             this.started = false;
         }
         finally
         {
             serviceListener.afterShutdown( this );
             environment = null;
             interceptorChain = null;
             startupConfiguration = null;
         }
     }
 
 
     public String getInstanceId()
     {
         return instanceId;
     }
 
 
     public DirectoryServiceConfiguration getConfiguration()
     {
         return configuration;
     }
 
 
     @SuppressWarnings("unchecked")
     public Hashtable<String, Object> getEnvironment()
     {
         return ( Hashtable ) environment.clone();
     }
 
 
     public DirectoryServiceListener getServiceListener()
     {
         return serviceListener;
     }
 
 
     public StartupConfiguration getStartupConfiguration()
     {
         return startupConfiguration;
     }
 
 
     public Registries getRegistries()
     {
         return registries;
     }
 
 
     public PartitionNexus getPartitionNexus()
     {
         return partitionNexus;
     }
 
 
     public InterceptorChain getInterceptorChain()
     {
         return interceptorChain;
     }
 
 
     public boolean isFirstStart()
     {
         return firstStart;
     }
 
 
     public boolean isStarted()
     {
         return started;
     }
 
 
     /**
      * Checks to make sure security environment parameters are set correctly.
      *
      * @throws javax.naming.NamingException if the security settings are not correctly configured.
      */
     private void checkSecuritySettings( String principal, byte[] credential, String authentication )
         throws NamingException
     {
         if ( authentication == null )
         {
             authentication = "";
         }
 
         /*
          * If bind is strong make sure we have the principal name
          * set within the environment, otherwise complain
          */
         if ( "strong".equalsIgnoreCase( authentication ) )
         {
             if ( principal == null )
             {
                 throw new LdapConfigurationException( "missing required " + Context.SECURITY_PRINCIPAL
                     + " property for strong authentication" );
             }
         }
         /*
          * If bind is simple make sure we have the credentials and the
          * principal name set within the environment, otherwise complain
          */
         else if ( "simple".equalsIgnoreCase( authentication ) )
         {
             if ( credential == null )
             {
                 throw new LdapConfigurationException( "missing required " + Context.SECURITY_CREDENTIALS
                     + " property for simple authentication" );
             }
 
             if ( principal == null )
             {
                 throw new LdapConfigurationException( "missing required " + Context.SECURITY_PRINCIPAL
                     + " property for simple authentication" );
             }
         }
         /*
          * If bind is none make sure credentials and the principal
          * name are NOT set within the environment, otherwise complain
          */
         else if ( "none".equalsIgnoreCase( authentication ) )
         {
             if ( credential != null )
             {
                 throw new LdapConfigurationException( "ambiguous bind "
                     + "settings encountered where bind is anonymous yet " + Context.SECURITY_CREDENTIALS
                     + " property is set" );
             }
 
             if ( principal != null )
             {
                 throw new LdapConfigurationException( "ambiguous bind "
                     + "settings encountered where bind is anonymous yet " + Context.SECURITY_PRINCIPAL
                     + " property is set" );
             }
 
             if ( !startupConfiguration.isAllowAnonymousAccess() )
             {
                 throw new LdapNoPermissionException( "Anonymous access disabled." );
             }
         }
         else
         {
             /*
              * If bind is anything other than strong, simple, or none we need to complain
              */
             throw new LdapAuthenticationNotSupportedException( "Unknown authentication type: '" + authentication + "'",
                 ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED );
         }
     }
 
 
     /**
      * Returns true if we had to create the bootstrap entries on the first
      * start of the server.  Otherwise if all entries exist, meaning none
      * had to be created, then we are not starting for the first time.
      *
      * @throws javax.naming.NamingException
      */
     private boolean createBootstrapEntries() throws NamingException
     {
         boolean firstStart = false;
         
         // -------------------------------------------------------------------
         // create admin entry
         // -------------------------------------------------------------------
 
         /*
          * If the admin entry is there, then the database was already created
          */
         if ( !partitionNexus.hasEntry( new EntryOperationContext( PartitionNexus.getAdminName() ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.PERSON_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_PERSON_OC );
             objectClass.add( SchemaConstants.INET_ORG_PERSON_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.UID_AT, PartitionNexus.ADMIN_UID );
             attributes.put( SchemaConstants.USER_PASSWORD_AT, PartitionNexus.ADMIN_PASSWORD );
             attributes.put( SchemaConstants.DISPLAY_NAME_AT, "Directory Superuser" );
             attributes.put( SchemaConstants.CN_AT, "system administrator" );
             attributes.put( SchemaConstants.SN_AT, "administrator" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
             attributes.put( SchemaConstants.DISPLAY_NAME_AT, "Directory Superuser" );
 
             partitionNexus.add( new AddOperationContext( PartitionNexus.getAdminName(),
                 attributes ) );
         }
 
         // -------------------------------------------------------------------
         // create system users area
         // -------------------------------------------------------------------
 
         Map<String,OidNormalizer> oidsMap = configuration.getRegistries().getAttributeTypeRegistry().getNormalizerMapping();
         LdapDN userDn = new LdapDN( "ou=users,ou=system" );
         userDn.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( userDn ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.OU_AT, "users" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( userDn, attributes ) );
         }
 
         // -------------------------------------------------------------------
         // create system groups area
         // -------------------------------------------------------------------
 
         LdapDN groupDn = new LdapDN( "ou=groups,ou=system" );
         groupDn.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( groupDn ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.OU_AT, "groups" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( groupDn, attributes ) );
         }
 
         // -------------------------------------------------------------------
         // create administrator group
         // -------------------------------------------------------------------
 
         LdapDN name = new LdapDN( ServerDNConstants.ADMINISTRATORS_GROUP_DN );
         name.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( name ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.GROUP_OF_UNIQUE_NAMES_OC );
             attributes.put( objectClass );
             attributes.put( SchemaConstants.CN_AT, "Administrators" );
             attributes.put( SchemaConstants.UNIQUE_MEMBER_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( name, attributes ) );
             
             Interceptor authzInterceptor = interceptorChain.get( AuthorizationService.NAME );
             
             if ( authzInterceptor == null )
             {
                 log.error( "The Authorization service is null : this is not allowed" );
                 throw new NamingException( "The Authorization service is null" );
             }
             
             if ( !( authzInterceptor instanceof AuthorizationService) )
             {
                 log.error( "The Authorization service is not set correctly : '{}' is an incorect interceptor", 
                     authzInterceptor.getClass().getName() );
                 throw new NamingException( "The Authorization service is incorrectly set" );
                 
             }
 
             AuthorizationService authzSrvc = ( AuthorizationService ) authzInterceptor;
             authzSrvc.cacheNewGroup( name, attributes );
 
         }
 
         // -------------------------------------------------------------------
         // create system configuration area
         // -------------------------------------------------------------------
 
         LdapDN configurationDn = new LdapDN( "ou=configuration,ou=system" );
         configurationDn.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( configurationDn ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.OU_AT, "configuration" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( configurationDn, attributes ) );
         }
 
         // -------------------------------------------------------------------
         // create system configuration area for partition information
         // -------------------------------------------------------------------
 
         LdapDN partitionsDn = new LdapDN( "ou=partitions,ou=configuration,ou=system" );
         partitionsDn.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( partitionsDn ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.OU_AT, "partitions" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( partitionsDn, attributes ) );
         }
 
         // -------------------------------------------------------------------
         // create system configuration area for services
         // -------------------------------------------------------------------
 
         LdapDN servicesDn = new LdapDN( "ou=services,ou=configuration,ou=system" );
         servicesDn.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( servicesDn ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.OU_AT, "services" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( servicesDn, attributes ) );
         }
 
         // -------------------------------------------------------------------
         // create system configuration area for interceptors
         // -------------------------------------------------------------------
 
         LdapDN interceptorsDn = new LdapDN( "ou=interceptors,ou=configuration,ou=system" );
         interceptorsDn.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( interceptorsDn ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.OU_AT, "interceptors" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( interceptorsDn, attributes ) );
         }
 
         // -------------------------------------------------------------------
         // create system preferences area
         // -------------------------------------------------------------------
 
         LdapDN sysPrefRootDn = new LdapDN( "prefNodeName=sysPrefRoot,ou=system");
         sysPrefRootDn.normalize( oidsMap );
         
         if ( !partitionNexus.hasEntry( new EntryOperationContext( sysPrefRootDn ) ) )
         {
             firstStart = true;
 
             Attributes attributes = new AttributesImpl();
             Attribute objectClass = new AttributeImpl( SchemaConstants.OBJECT_CLASS_AT );
             objectClass.add( SchemaConstants.TOP_OC );
             objectClass.add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
             attributes.put( objectClass );
 
             attributes.put( SchemaConstants.OBJECT_CLASS_AT, SchemaConstants.EXTENSIBLE_OBJECT_OC );
             attributes.put( "prefNodeName", "sysPrefRoot" );
             attributes.put( SchemaConstants.CREATORS_NAME_AT, PartitionNexus.ADMIN_PRINCIPAL_NORMALIZED );
             attributes.put( SchemaConstants.CREATE_TIMESTAMP_AT, DateUtils.getGeneralizedTime() );
 
             partitionNexus.add( new AddOperationContext( sysPrefRootDn, attributes ) );
         }
 
         return firstStart;
     }
 
 
     /**
      * Displays security warning messages if any possible secutiry issue is found.
      */
     private void showSecurityWarnings() throws NamingException
     {
         // Warn if the default password is not changed.
         boolean needToChangeAdminPassword = false;
 
         LdapDN adminDn = new LdapDN( PartitionNexus.ADMIN_PRINCIPAL );
         adminDn.normalize( configuration.getRegistries().getAttributeTypeRegistry().getNormalizerMapping() );
         
         Attributes adminEntry = partitionNexus.lookup( new LookupOperationContext( adminDn ) );
         Object userPassword = adminEntry.get( SchemaConstants.USER_PASSWORD_AT ).get();
         if ( userPassword instanceof byte[] )
         {
             needToChangeAdminPassword = PartitionNexus.ADMIN_PASSWORD.equals( new String(
                 ( byte[] ) userPassword ) );
         }
         else if ( userPassword.toString().equals( PartitionNexus.ADMIN_PASSWORD ) )
         {
             needToChangeAdminPassword = PartitionNexus.ADMIN_PASSWORD.equals( userPassword.toString() );
         }
 
         if ( needToChangeAdminPassword )
         {
             log.warn( "You didn't change the admin password of directory service " + "instance '" + instanceId + "'.  "
                 + "Please update the admin password as soon as possible " + "to prevent a possible security breach." );
         }
     }
 
 
     private void createTestEntries( Hashtable env ) throws NamingException
     {
         String principal = AbstractContextFactory.getPrincipal( env );
         byte[] credential = AbstractContextFactory.getCredential( env );
         String authentication = AbstractContextFactory.getAuthentication( env );
         
         LdapDN principalDn = new LdapDN( principal );
 
         ServerLdapContext ctx = ( ServerLdapContext ) 
             getJndiContext( principalDn, principal, credential, authentication, "" );
 
         Iterator i = startupConfiguration.getTestEntries().iterator();
         
         while ( i.hasNext() )
         {
         	try
         	{
 	        	Entry entry =  (Entry)( ( Entry ) i.next() ).clone();
 	            Attributes attributes = entry.getAttributes();
 	            String dn = entry.getDn();
 
 	            try
 	            {
 	                ctx.createSubcontext( dn, attributes );
 	            }
 	            catch ( Exception e )
 	            {
 	                log.warn( dn + " test entry already exists.", e );
 	            }
         	}
         	catch ( CloneNotSupportedException cnse )
         	{
                 log.warn( "Cannot clone the entry ", cnse );
         	}
         }
     }
 
 
     /**
      * Kicks off the initialization of the entire system.
      *
      * @throws javax.naming.NamingException if there are problems along the way
      */
     private void initialize() throws NamingException
     {
         if ( log.isDebugEnabled() )
         {
             log.debug( "---> Initializing the DefaultDirectoryService " );
         }
 
         // --------------------------------------------------------------------
         // Load the bootstrap schemas to start up the schema partition
         // --------------------------------------------------------------------
 
         // setup temporary loader and temp registry 
         BootstrapSchemaLoader loader = new BootstrapSchemaLoader();
         OidRegistry oidRegistry = new DefaultOidRegistry();
         registries = new DefaultRegistries( "bootstrap", loader, oidRegistry );
         
         // load essential bootstrap schemas 
         Set<Schema> bootstrapSchemas = new HashSet<Schema>();
         bootstrapSchemas.add( new ApachemetaSchema() );
         bootstrapSchemas.add( new ApacheSchema() );
         bootstrapSchemas.add( new CoreSchema() );
         bootstrapSchemas.add( new SystemSchema() );
         loader.loadWithDependencies( bootstrapSchemas, registries );
 
         // run referential integrity tests
         java.util.List errors = registries.checkRefInteg();
         if ( !errors.isEmpty() )
         {
             NamingException e = new NamingException();
             e.setRootCause( ( Throwable ) errors.get( 0 ) );
             throw e;
         }
         
         SerializableComparator.setRegistry( registries.getComparatorRegistry() );
         
         // --------------------------------------------------------------------
         // If not present extract schema partition from jar
         // --------------------------------------------------------------------
 
         File schemaDirectory = new File( startupConfiguration.getWorkingDirectory(), "schema" );
         SchemaPartitionExtractor extractor = null;
         if ( ! schemaDirectory.exists() )
         {
             try
             {
                 extractor = new SchemaPartitionExtractor( startupConfiguration.getWorkingDirectory() );
                 extractor.extract();
             }
             catch ( IOException e )
             {
                 NamingException ne = new NamingException( "Failed to extract pre-loaded schema partition." );
                 ne.setRootCause( e );
                 throw ne;
             }
         }
         
         // --------------------------------------------------------------------
         // Initialize schema partition
         // --------------------------------------------------------------------
         
         MutablePartitionConfiguration schemaPartitionConfig = new MutablePartitionConfiguration();
         schemaPartitionConfig.setName( "schema" );
         schemaPartitionConfig.setCacheSize( 1000 );
         
         DbFileListing listing = null;
         try 
         {
             listing = new DbFileListing();
         }
         catch( IOException e )
         {
             throw new LdapNamingException( "Got IOException while trying to read DBFileListing: " + e.getMessage(), 
                 ResultCodeEnum.OTHER );
         }
         
         schemaPartitionConfig.setIndexedAttributes( listing.getIndexedAttributes() );
         schemaPartitionConfig.setOptimizerEnabled( true );
         schemaPartitionConfig.setSuffix( "ou=schema" );
         
         Attributes entry = new AttributesImpl();
         entry.put( SchemaConstants.OBJECT_CLASS_AT, SchemaConstants.TOP_OC );
         entry.get( SchemaConstants.OBJECT_CLASS_AT ).add( SchemaConstants.ORGANIZATIONAL_UNIT_OC );
         entry.put( SchemaConstants.OU_AT, "schema" );
         schemaPartitionConfig.setContextEntry( entry );
         JdbmPartition schemaPartition = new JdbmPartition();
         schemaPartition.init( configuration, schemaPartitionConfig );
         schemaPartitionConfig.setContextPartition( schemaPartition );
 
         // --------------------------------------------------------------------
         // Enable schemas of all indices of partition configurations 
         // --------------------------------------------------------------------
 
         /*
          * We need to make sure that every attribute indexed by a partition is
          * loaded into the registries on the next step.  So here we must enable
          * the schemas of those attributes so they are loaded into the global
          * registries.
          */
         
         SchemaPartitionDao dao = new SchemaPartitionDao( schemaPartition, registries );
         Map<String,Schema> schemaMap = dao.getSchemas();
         PartitionConfiguration pc = startupConfiguration.getSystemPartitionConfiguration();
         Set<PartitionConfiguration> pcs = new HashSet<PartitionConfiguration>();
         if ( pc != null )
         {
             pcs.add( pc );
         }
         else
         {
             log.warn( "Encountered null configuration." );
         }
             
         
         pcs.addAll( startupConfiguration.getPartitionConfigurations() );
         
         for ( PartitionConfiguration pconf : pcs )
         {
             Iterator indices = pconf.getIndexedAttributes().iterator();
             while ( indices.hasNext() )
             {
                 Object indexedAttr = indices.next();
                 String schemaName = dao.findSchema( indexedAttr.toString() );
                 if ( schemaName == null )
                 {
                     throw new NamingException( "Index on unidentified attribute: " + indexedAttr.toString() );
                 }
                 
                 Schema schema = schemaMap.get( schemaName );
                 if ( schema.isDisabled() )
                 {
                     dao.enableSchema( schemaName );
                 }
             }
         }
         
         // --------------------------------------------------------------------
         // Initialize schema subsystem and reset registries
         // --------------------------------------------------------------------
         
         PartitionSchemaLoader schemaLoader = new PartitionSchemaLoader( schemaPartition, registries );
         Registries globalRegistries = new DefaultRegistries( "global", schemaLoader, oidRegistry );
         schemaLoader.loadEnabled( globalRegistries );
         registries = globalRegistries;
         SerializableComparator.setRegistry( globalRegistries.getComparatorRegistry() );
         
         Set<String> binaries = new HashSet<String>();
         if ( this.environment.containsKey( BINARY_KEY ) )
         {
             if ( log.isInfoEnabled() )
             {
                 log.info( "Startup environment contains " + BINARY_KEY );
             }
 
             String binaryIds = ( String ) this.environment.get( BINARY_KEY );
             if ( binaryIds == null )
             {
                 if ( log.isWarnEnabled() )
                 {
                     log.warn( BINARY_KEY + " in startup environment contains null value.  "
                         + "Using only schema info to set binary attributeTypes." );
                 }
             }
             else
             {
                 if ( !StringTools.isEmpty( binaryIds ) )
                 {
                     String[] binaryArray = binaryIds.split( " " );
 
                     for ( int i = 0; i < binaryArray.length; i++ )
                     {
                         binaries.add( StringTools.lowerCaseAscii( StringTools.trim( binaryArray[i] ) ) );
                     }
                 }
 
                 if ( log.isInfoEnabled() )
                 {
                     log.info( "Setting binaries to union of schema defined binaries and those provided in "
                         + BINARY_KEY );
                 }
             }
         }
         
         schemaManager = new SchemaManager( globalRegistries, schemaLoader, 
             new SchemaPartitionDao( schemaPartition, registries ) );
 
         // now get all the attributeTypes that are binary from the registry
         AttributeTypeRegistry registry = registries.getAttributeTypeRegistry();
         Iterator<AttributeType> list = registry.iterator();
         while ( list.hasNext() )
         {
             AttributeType type = list.next();
             
            if ( !type.getSyntax().isHumanReadable() )
             {
                 // add the OID for the attributeType
                 binaries.add( type.getOid() );
 
                 // add the lowercased name for the names for the attributeType
                 String[] names = type.getNames();
                 
                 for ( int ii = 0; ii < names.length; ii++ )
                 {
                     binaries.add( StringTools.lowerCaseAscii( StringTools.trim( names[ii] ) ) );
                 }
             }
         }
 
         this.environment.put( BINARY_KEY, binaries );
         if ( log.isDebugEnabled() )
         {
             log.debug( "binary ids used: " + binaries );
         }
 
         partitionNexus = new DefaultPartitionNexus( new AttributesImpl() );
         partitionNexus.init( configuration, null );
         partitionNexus.addContextPartition( new AddContextPartitionOperationContext( schemaPartitionConfig ) );
 
         interceptorChain = new InterceptorChain();
         interceptorChain.init( configuration );
 
         if ( log.isDebugEnabled() )
         {
             log.debug( "<--- DefaultDirectoryService initialized" );
         }
     }
 
 
     public SchemaManager getSchemaManager()
     {
         return schemaManager;
     }
 }
