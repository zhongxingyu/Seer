 package org.codehaus.xfire.plexus.java;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.plexus.configuration.PlexusConfiguration;
 import org.codehaus.plexus.configuration.PlexusConfigurationException;
 import org.codehaus.plexus.logging.LogEnabled;
 import org.codehaus.plexus.logging.Logger;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.Configurable;
 import org.codehaus.xfire.java.mapping.TypeMapping;
 import org.codehaus.xfire.java.type.BooleanType;
 import org.codehaus.xfire.java.type.DoubleType;
 import org.codehaus.xfire.java.type.FloatType;
 import org.codehaus.xfire.java.type.IntType;
 import org.codehaus.xfire.java.type.LongType;
 import org.codehaus.xfire.java.type.Type;
 import org.codehaus.xfire.soap.SoapConstants;
 
 /**
  * Extends and configures the TypeMappingRegistry.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 31, 2004
  */
 public class TypeMappingRegistry
     extends org.codehaus.xfire.java.mapping.DefaultTypeMappingRegistry
     implements LogEnabled, Configurable
 {
     private Logger logger;
 
     public void configure(PlexusConfiguration config) 
         throws PlexusConfigurationException
     {
         PlexusConfiguration tmConfig[] = config.getChildren("typeMapping");
         
         for ( int i = 0; i < tmConfig.length; i++ )
         {
             configureTypeMapping( tmConfig[i] );
         }
     }
 
     private void configureTypeMapping(PlexusConfiguration configuration)
         throws PlexusConfigurationException
     {
         TypeMapping tm = createTypeMapping(false);
         
         register( configuration.getAttribute( "namespace" ), tm );
         
         if ( Boolean.valueOf( configuration.getAttribute("default", "false") ).booleanValue() )
             registerDefault( tm );
         
         PlexusConfiguration[] types = configuration.getChildren( "type" );
         
         // register primitive types manually since there is no way
         // to do Class.forName("boolean") et al.
         tm.register(boolean.class, new QName(SoapConstants.XSD,"boolean"), new BooleanType());
         tm.register(int.class, new QName(SoapConstants.XSD,"int"), new IntType());
         tm.register(double.class, new QName(SoapConstants.XSD,"double"), new DoubleType());
         tm.register(float.class, new QName(SoapConstants.XSD,"float"), new FloatType());
         tm.register(long.class, new QName(SoapConstants.XSD,"long"), new LongType());
         
         for ( int i = 0; i < types.length; i++ )
         {
             configureType( types[i], tm );
         }
     }
     
     private void configureType( PlexusConfiguration configuration, TypeMapping tm )
         throws PlexusConfigurationException
     {
         try
         {
             String ns = configuration.getAttribute("namespace");
             String name = configuration.getAttribute("name");
             QName qname = new QName(ns, name);
             
             Class clazz = loadClass( configuration.getAttribute("class") );
             Class typeClass = loadClass( configuration.getAttribute("type") );
 
             tm.register( clazz,
                          qname,
                          (Type) typeClass.newInstance() );
             
             logger.debug( "Registered " + typeClass.getName() + 
                               " for " + qname + " with class " + clazz.getName() );
         }
         catch (Exception e)
         {
             if ( e instanceof PlexusConfigurationException )
                 throw (PlexusConfigurationException) e;
             
             throw new PlexusConfigurationException( "Could not configure type.", e );
         }                     
     }
 
     public void enableLogging(Logger logger)
     {
         this.logger = logger;
     }
 }
