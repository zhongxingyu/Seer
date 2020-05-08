 package org.codehaus.xfire.plexus.xmlbeans;
 
 import org.codehaus.plexus.configuration.PlexusConfiguration;
 import org.codehaus.xfire.handler.SoapHandler;
 import org.codehaus.xfire.plexus.PlexusXFireComponent;
 import org.codehaus.xfire.plexus.config.Configurator;
 import org.codehaus.xfire.plexus.java.PlexusJavaService;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.xmlbeans.XMLBeansServiceHandler;
 
 /**
  * Configures java services for plexus.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Sep 20, 2004
  */
 public class XMLBeansConfigurator
     extends PlexusXFireComponent
     implements Configurator
 {
     final public static String SERVICE_TYPE = "xmlbeans";
     
     /**
      * @see org.codehaus.xfire.plexus.config.Configurator#getServiceType()
      */
     public String getServiceType()
     {
         return SERVICE_TYPE;
     }
 
     /**
      * @see org.codehaus.xfire.plexus.config.Configurator#createService(org.codehaus.plexus.configuration.PlexusConfiguration)
      */
     public Service createService( PlexusConfiguration config ) throws Exception
     {
         PlexusJavaService s = new PlexusJavaService();
         s.service(getServiceLocator());
         s.configure(config);
         
         XMLBeansServiceHandler handler = new XMLBeansServiceHandler();
 		SoapHandler sHandler = new SoapHandler(handler);
 		s.setServiceHandler(sHandler);
 		
        return s;
     }
 }
