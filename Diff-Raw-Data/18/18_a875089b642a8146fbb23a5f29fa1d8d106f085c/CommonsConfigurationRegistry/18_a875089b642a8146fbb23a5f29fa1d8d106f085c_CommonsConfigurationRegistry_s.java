 package org.apache.archiva.redback.components.registry.commons;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.apache.archiva.redback.components.registry.Registry;
 import org.apache.archiva.redback.components.registry.RegistryException;
 import org.apache.archiva.redback.components.registry.RegistryListener;
 import org.apache.commons.configuration.CombinedConfiguration;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.DefaultConfigurationBuilder;
 import org.apache.commons.configuration.FileConfiguration;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.configuration.SystemConfiguration;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.commons.configuration.event.EventSource;
 import org.apache.commons.configuration.tree.DefaultExpressionEngine;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.plexus.interpolation.InterpolationException;
 import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
 import org.codehaus.plexus.interpolation.StringSearchInterpolator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.PostConstruct;
 import java.io.File;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Implementation of the registry component using
 * <a href="http://jakarta.apache.org/commons/configuration">Commons Configuration</a>. The use of Commons Configuration
  * enables a variety of sources to be used, including XML files, properties, JNDI, JDBC, etc.
  * <p/>
  * The component can be configured using the {@link #properties} configuration item, the content of which should take
  * the format of an input to the Commons Configuration
 * <a href="http://jakarta.apache.org/commons/configuration/howto_configurationbuilder.html">configuration
  * builder</a>.
  */
 @Service( "commons-configuration" )
 public class CommonsConfigurationRegistry
     implements Registry
 {
     /**
      * The combined configuration instance that houses the registry.
      */
     private Configuration configuration;
 
     private Logger logger = LoggerFactory.getLogger( getClass() );
 
     private String propertyDelimiter = ".";
 
     /**
      * The configuration properties for the registry. This should take the format of an input to the Commons
      * Configuration
      * <a href="http://commons.apache.org/configuration/howto_configurationbuilder.html">configuration
      * builder</a>.
      */
     private String properties;
 
 
     public CommonsConfigurationRegistry()
     {
         // default constructor
         logger.debug( "empty constructor" );
     }
 
     public CommonsConfigurationRegistry( Configuration configuration )
     {
         if ( configuration == null )
         {
             throw new NullPointerException( "configuration can not be null" );
         }
 
         this.configuration = configuration;
     }
 
     public String dump()
     {
         StringBuilder buffer = new StringBuilder();
         buffer.append( "Configuration Dump." );
         for ( Iterator i = configuration.getKeys(); i.hasNext(); )
         {
             String key = (String) i.next();
             Object value = configuration.getProperty( key );
             buffer.append( "\n\"" ).append( key ).append( "\" = \"" ).append( value ).append( "\"" );
         }
         return buffer.toString();
     }
 
     public boolean isEmpty()
     {
         return configuration.isEmpty();
     }
 
     public Registry getSubset( String key )
     {
         return new CommonsConfigurationRegistry( configuration.subset( key ) );
     }
 
     public List getList( String key )
     {
         return configuration.getList( key );
     }
 
     public List<Registry> getSubsetList( String key )
     {
         List<Registry> subsets = new ArrayList<>();
 
         boolean done = false;
         do
         {
             Registry registry = getSubset( key + "(" + subsets.size() + ")" );
             if ( !registry.isEmpty() )
             {
                 subsets.add( registry );
             }
             else
             {
                 done = true;
             }
         }
         while ( !done );
 
         return subsets;
     }
 
     public Properties getProperties( String key )
     {
         Configuration configuration = this.configuration.subset( key );
 
         Properties properties = new Properties();
         for ( Iterator i = configuration.getKeys(); i.hasNext(); )
         {
             String property = (String) i.next();
             List l = configuration.getList( property );
             StringBuilder sb = new StringBuilder();
            for( Object element : l )
             {
                sb.append( ( String ) element );
                sb.append( "," ); 
             }
            if( sb.length() > 0 )
            { 
                 sb.deleteCharAt( sb.length() - 1 );
             }
             properties.setProperty( property, sb.toString() );
         }
         return properties;
     }
 
     public void save()
         throws RegistryException
     {
         if ( configuration instanceof FileConfiguration )
         {
             FileConfiguration fileConfiguration = (FileConfiguration) configuration;
             try
             {
                 fileConfiguration.save();
             }
             catch ( ConfigurationException e )
             {
                 throw new RegistryException( e.getMessage(), e );
             }
         }
         else
         {
             throw new RegistryException( "Can only save file-based configurations" );
         }
     }
 
     public void addChangeListener( RegistryListener listener )
     {
         EventSource eventSource = EventSource.class.cast( this.configuration );
 
         eventSource.addConfigurationListener( new ConfigurationListenerDelegate( listener, this ) );
     }
 
     @Override
     public boolean removeChangeListener( RegistryListener listener )
     {
         EventSource eventSource = EventSource.class.cast( this.configuration );
 
         boolean removed =
             eventSource.removeConfigurationListener( new ConfigurationListenerDelegate( listener, this ) );
 
         return removed;
     }
 
 
     public int getChangeListenersSize()
     {
         return EventSource.class.cast( this.configuration ).getConfigurationListeners().size();
     }
 
     public Collection<String> getKeys()
     {
         Set<String> keys = new HashSet<String>();
 
         for ( Iterator<String> i = configuration.getKeys(); i.hasNext(); )
         {
             String key = i.next();
 
             int index = key.indexOf( '.' );
             if ( index < 0 )
             {
                 keys.add( key );
             }
             else
             {
                 keys.add( key.substring( 0, index ) );
             }
         }
 
         return keys;
     }
 
     public Collection getFullKeys()
     {
         Set<String> keys = new HashSet<String>();
 
         for ( Iterator<String> i = configuration.getKeys(); i.hasNext(); )
         {
             keys.add( i.next() );
         }
 
         return keys;
     }
 
     public void remove( String key )
     {
         configuration.clearProperty( key );
     }
 
     public void removeSubset( String key )
     {
         // create temporary list since removing a key will modify the iterator from configuration
         List keys = new ArrayList();
         for ( Iterator i = configuration.getKeys( key ); i.hasNext(); )
         {
             keys.add( i.next() );
         }
 
         for ( Iterator i = keys.iterator(); i.hasNext(); )
         {
             configuration.clearProperty( (String) i.next() );
         }
     }
 
     public String getString( String key )
     {
         return configuration.getString( key );
     }
 
     public String getString( String key, String defaultValue )
     {
         return configuration.getString( key, defaultValue );
     }
 
     public void setString( String key, String value )
     {
         configuration.setProperty( key, value );
     }
 
     public int getInt( String key )
     {
         return configuration.getInt( key );
     }
 
     public int getInt( String key, int defaultValue )
     {
         return configuration.getInt( key, defaultValue );
     }
 
     public void setInt( String key, int value )
     {
         configuration.setProperty( key, Integer.valueOf( value ) );
     }
 
     public boolean getBoolean( String key )
     {
         return configuration.getBoolean( key );
     }
 
     public boolean getBoolean( String key, boolean defaultValue )
     {
         return configuration.getBoolean( key, defaultValue );
     }
 
     public void setBoolean( String key, boolean value )
     {
         configuration.setProperty( key, Boolean.valueOf( value ) );
     }
 
     public void addConfigurationFromResource( String resource )
         throws RegistryException
     {
         addConfigurationFromResource( resource, null );
     }
 
     public void addConfigurationFromResource( String resource, String prefix )
         throws RegistryException
     {
         CombinedConfiguration configuration = (CombinedConfiguration) this.configuration;
         if ( resource.endsWith( ".properties" ) )
         {
             try
             {
                 logger.debug( "Loading properties configuration from classloader resource: {}", resource );
                 configuration.addConfiguration( new PropertiesConfiguration( resource ), null, prefix );
             }
             catch ( ConfigurationException e )
             {
                 throw new RegistryException(
                     "Unable to add configuration from resource '" + resource + "': " + e.getMessage(), e );
             }
         }
         else if ( resource.endsWith( ".xml" ) )
         {
             try
             {
                 logger.debug( "Loading XML configuration from classloader resource: {}", resource );
                 configuration.addConfiguration( new XMLConfiguration( resource ), null, prefix );
             }
             catch ( ConfigurationException e )
             {
                 throw new RegistryException(
                     "Unable to add configuration from resource '" + resource + "': " + e.getMessage(), e );
             }
         }
         else
         {
             throw new RegistryException(
                 "Unable to add configuration from resource '" + resource + "': unrecognised type" );
         }
     }
 
     public void addConfigurationFromFile( File file )
         throws RegistryException
     {
         addConfigurationFromFile( file, null );
     }
 
     public void addConfigurationFromFile( File file, String prefix )
         throws RegistryException
     {
         CombinedConfiguration configuration = (CombinedConfiguration) this.configuration;
         if ( file.getName().endsWith( ".properties" ) )
         {
             try
             {
                 logger.debug( "Loading properties configuration from file: {}", file );
                 configuration.addConfiguration( new PropertiesConfiguration( file ), null, prefix );
             }
             catch ( ConfigurationException e )
             {
                 throw new RegistryException(
                     "Unable to add configuration from file '" + file.getName() + "': " + e.getMessage(), e );
             }
         }
         else if ( file.getName().endsWith( ".xml" ) )
         {
             try
             {
                 logger.debug( "Loading XML configuration from file: {}", file );
                 configuration.addConfiguration( new XMLConfiguration( file ), null, prefix );
             }
             catch ( ConfigurationException e )
             {
                 throw new RegistryException(
                     "Unable to add configuration from file '" + file.getName() + "': " + e.getMessage(), e );
             }
         }
         else
         {
             throw new RegistryException(
                 "Unable to add configuration from file '" + file.getName() + "': unrecognised type" );
         }
     }
 
     @PostConstruct
     public void initialize()
         throws RegistryException
     {
         try
         {
             CombinedConfiguration configuration;
             if ( StringUtils.isNotBlank( properties ) )
             {
                 try
                 {
                     DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                     DefaultExpressionEngine expressionEngine = new DefaultExpressionEngine();
                     expressionEngine.setPropertyDelimiter( propertyDelimiter );
                     builder.setExpressionEngine( expressionEngine );
 
                     StringSearchInterpolator interpolator = new StringSearchInterpolator( "${", "}" );
                     // interpolation as plexus did it before
                     interpolator.addValueSource( new PropertiesBasedValueSource( System.getProperties() ) );
 
                     String interpolatedProps = interpolator.interpolate( properties );
 
                     logger.debug( "Loading configuration into commons-configuration, xml {}", interpolatedProps );
                     builder.load( new StringReader( interpolatedProps ) );
                     configuration = builder.getConfiguration( false );
                     configuration.setExpressionEngine( expressionEngine );
                     //configuration.set
                 }
                 catch ( InterpolationException e )
                 {
                     throw new RuntimeException( e.getMessage(), e );
                 }
             }
             else
             {
                 logger.debug( "Creating a default configuration - no configuration was provided" );
                 configuration = new CombinedConfiguration();
             }
 
             configuration.addConfiguration( new SystemConfiguration() );
 
             this.configuration = configuration;
         }
         catch ( ConfigurationException e )
         {
             throw new RuntimeException( e.getMessage(), e );
         }
     }
 
     public void setProperties( String properties )
     {
         this.properties = properties;
     }
 
     public Registry getSection( String name )
     {
         CombinedConfiguration combinedConfiguration = (CombinedConfiguration) configuration;
         Configuration configuration = combinedConfiguration.getConfiguration( name );
         return configuration == null ? null : new CommonsConfigurationRegistry( configuration );
     }
 
     public String getPropertyDelimiter()
     {
         return propertyDelimiter;
     }
 
     public void setPropertyDelimiter( String propertyDelimiter )
     {
         this.propertyDelimiter = propertyDelimiter;
     }
 }
