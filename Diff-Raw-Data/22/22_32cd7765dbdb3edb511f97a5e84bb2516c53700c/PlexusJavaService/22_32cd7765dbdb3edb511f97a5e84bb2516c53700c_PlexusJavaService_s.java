 package org.codehaus.xfire.plexus.java;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.codehaus.plexus.configuration.PlexusConfiguration;
 import org.codehaus.plexus.configuration.PlexusConfigurationException;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.Configurable;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
 import org.codehaus.xfire.SOAPConstants;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.fault.FaultHandler;
 import org.codehaus.xfire.fault.SOAP11FaultHandler;
 import org.codehaus.xfire.fault.SOAP12FaultHandler;
 import org.codehaus.xfire.handler.Handler;
 import org.codehaus.xfire.handler.SoapHandler;
 import org.codehaus.xfire.java.DefaultJavaService;
 import org.codehaus.xfire.java.JavaService;
 import org.codehaus.xfire.java.JavaServiceHandler;
 import org.codehaus.xfire.java.mapping.TypeMapping;
 import org.codehaus.xfire.java.mapping.TypeMappingRegistry;
 import org.codehaus.xfire.java.wsdl.JavaWSDLBuilder;
 import org.codehaus.xfire.service.ServiceRegistry;
 import org.codehaus.xfire.transport.TransportManager;
 
 /**
  * A service that is created from an XML configuration within Plexus.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class PlexusJavaService
 	extends DefaultJavaService
 	implements Configurable
 {
     private PlexusConfiguration[] types;
     
     private PlexusConfiguration[] handlers;
     
     private String faultHandlerHint;
     
     private ServiceLocator manager;
     
     private Handler serviceHandler;
 
     public FaultHandler getFaultHandler()
     {
         try
         {
             return (FaultHandler) getServiceLocator().lookup( FaultHandler.ROLE, faultHandlerHint );
         }
         catch (ComponentLookupException e)
         {
             throw new XFireRuntimeException( "Couldn't find service provider!", e );
         }
     }
     
     public String getFaultHandlerHint()
     {
         return faultHandlerHint;
     }
     
     public void setFaultHandlerHint(String faultHandlerHint)
     {
         this.faultHandlerHint = faultHandlerHint;
     }
     
     
     /**
      * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
      */
     public void configure(PlexusConfiguration config) throws PlexusConfigurationException
     {
         configureService(config);
         
         configureTypes(config);
         
         SoapHandler handler = new SoapHandler( new JavaServiceHandler() );
         setServiceHandler( handler );
     }
 
     /**
      * @param config
      * @throws PlexusConfigurationException
      */
     private void configureService(PlexusConfiguration config) throws PlexusConfigurationException
     {
         setName( config.getChild("name").getValue() );
         
         setDefaultNamespace( config.getChild( "namespace" ).getValue("") );
     
        setWSDLURL( config.getChild("wsdlURL").getValue("") );
         
         setUse( config.getChild("use").getValue("literal") );
         
         setStyle( config.getChild("style").getValue("wrapped") );
         
         try
         {
             setServiceClass( config.getChild( SERVICE_CLASS ).getValue() );
         }
         catch (ClassNotFoundException e)
         {
             throw new PlexusConfigurationException( "Couldn't find service class.", e );
         }
         
         // TODO use allowed methods attribute
         setProperty( ALLOWED_METHODS, config.getChild( ALLOWED_METHODS ).getValue("") );
         
         String soapNS = config.getChild( "soapVersion" ).getValue("1.1");
         
         if ( soapNS.equals("1.1") )
         {
             setSoapVersion( SOAPConstants.SOAP11_ENVELOPE_NS );
             setFaultHandlerHint( SOAP11FaultHandler.NAME );
         }
         else if ( soapNS.equals("1.2") )
         {
             setFaultHandlerHint( SOAP12FaultHandler.NAME );
             setSoapVersion( SOAPConstants.SOAP12_ENVELOPE_NS );
         }
         else
             throw new PlexusConfigurationException("Invalid soap version.  Must be 1.1 or 1.2.");
         
         String scope = config.getChild("scope").getValue("application");
         if ( scope.equals("application") )
             setScope(JavaService.SCOPE_APPLICATION);
         else if ( scope.equals("session") )
             setScope(JavaService.SCOPE_SESSION);
         else if ( scope.equals("request") )
             setScope(JavaService.SCOPE_REQUEST);
         
         setFaultHandlerHint( soapNS );
         
         setAutoTyped( Boolean.valueOf(config.getChild( "autoTyped" ).getValue("false")).booleanValue() );
     }
 
     /**
      * @param config
      * @throws PlexusConfigurationException
      */
     private void configureTypes(PlexusConfiguration config) throws PlexusConfigurationException
     {
        types  = config.getChild("types").getChildren("type");
     }
     
     public void initialize() throws Exception
     {
         TypeMappingRegistry tmr = getTypeMappingRegistry();
         TypeMapping tm = tmr.createTypeMapping(SOAPConstants.XSD, isAutoTyped());
         tmr.register(getDefaultNamespace(), tm);
         setTypeMapping(tm);
         
         for ( int i = 0; i < types.length; i++ )
         {
             initializeType( types[i], getTypeMapping() );   
         }
         
         setWSDLBuilder( new JavaWSDLBuilder( getTransportManager() ) );
 
         getServiceRegistry().register( this );
     }
 
     private void initializeType(PlexusConfiguration configuration, TypeMapping tm) throws PlexusConfigurationException
     {
         try
         {
             String ns = configuration.getAttribute( "namespace", getDefaultNamespace() );
             String name = configuration.getAttribute("name");
             
             tm.register( loadClass( configuration.getAttribute("class") ),
                          new QName( ns, name ),
                          loadClass( configuration.getAttribute("type") ) );
         }
         catch (Exception e)
         {
             if ( e instanceof PlexusConfigurationException )
                 throw (PlexusConfigurationException) e;
             
             throw new PlexusConfigurationException( "Could not configure type.", e );
         }                     
     }
     
     protected ServiceRegistry getServiceRegistry()
     {
         ServiceRegistry registry = null;
         
         try
         {
             registry = (ServiceRegistry) getServiceLocator().lookup( ServiceRegistry.ROLE );
         }
         catch (ComponentLookupException e)
         {
             throw new RuntimeException( "Couldn't find the ServiceRegistry!", e );
         }
         
         return registry;
     }
 
     protected TransportManager getTransportManager()
     {
         TransportManager transMan = null;
         
         try
         {
             transMan = (TransportManager) getServiceLocator().lookup( TransportManager.ROLE );
         }
         catch (ComponentLookupException e)
         {
             throw new RuntimeException( "Couldn't find the TransportManager!", e );
         }
         
         return transMan;
     }
     
     
     public TypeMappingRegistry getTypeMappingRegistry()
     {
         TypeMappingRegistry registry = null;
         
         try
         {
             registry = (TypeMappingRegistry) getServiceLocator().lookup( TypeMappingRegistry.ROLE );
         }
         catch (ComponentLookupException e)
         {
             throw new RuntimeException( "Couldn't find the TypeMappingRegistry!", e );
         }
         
         return registry;
     }
     
     public void service( ServiceLocator manager )
     {
         this.manager = manager;
     }
     
     /**
      * @return Returns the service manager.
      */
     protected ServiceLocator getServiceLocator()
     {
         return manager;
     }
 }
