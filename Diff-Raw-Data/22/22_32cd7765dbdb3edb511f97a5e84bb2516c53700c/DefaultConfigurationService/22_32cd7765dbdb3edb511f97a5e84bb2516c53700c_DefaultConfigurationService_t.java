 package org.codehaus.xfire.plexus.config;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import org.codehaus.plexus.PlexusConstants;
 import org.codehaus.plexus.PlexusContainer;
 import org.codehaus.plexus.configuration.PlexusConfiguration;
 import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
 import org.codehaus.plexus.context.Context;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
 import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
 import org.codehaus.xfire.plexus.PlexusXFireComponent;
 import org.codehaus.xfire.service.Service;
 
 /**
  * Loads in XFire components from the XFire configuration file.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class DefaultConfigurationService
     extends PlexusXFireComponent
     implements Initializable, Contextualizable, ConfigurationService
 { 
     private PlexusContainer container;
     
     private Hashtable configurators = new Hashtable();
     
 	public void initialize() throws Exception
 	{
 	    try
 	    {
 	        List confs = getServiceLocator().lookupList(Configurator.ROLE);
 	        
 	        for ( Iterator itr = confs.iterator(); itr.hasNext(); )
 	        {
 	            Configurator conf = (Configurator) itr.next();
 	            
 	            register( conf );
 	        }
 	        
 	        Reader reader = findConfigurationReader();
 	             
 	        if ( reader == null )
 	        {
 	           return;
 	        }             
 	       
 	        PlexusConfiguration configuration = new XmlPlexusConfiguration( Xpp3DomBuilder.build(reader) );
 	        createServices( configuration.getChild("services") );
 	    }
 	    catch( Exception e )
 	    {
 	        getLogger().error("Could not start the configuration service.", e);
 	        throw e;
 	    }
 	}
     
 	private void createServices(PlexusConfiguration child) 
         throws Exception
     {
         PlexusConfiguration[] service = child.getChildren("service");
         
         for ( int i = 0; i < service.length; i++ )
         {
             createService( service[i] );
         }
     }
     
     private void createService(PlexusConfiguration c) 
         throws Exception
     {
         String type = c.getChild("type").getValue("simple");
         
         if ( type == null )
         {
             getLogger().error("Service " + c.getAttribute("name") 
                     + " has no type.");
             return;
         }
         
         getLogger().info("Creating service " + c.getChild("name").getValue() + " with type " + type);
         Configurator builder = 
             (Configurator) getServiceLocator().lookup( Configurator.ROLE, type );
         
         if ( builder == null )
         {
            getLogger().error("Error creating service " + c.getAttribute("name") + ": No Configurator.");
             return;
         }
         else
         {
             Service service = builder.createService(c);
         }
     }
 
 	protected Reader findConfigurationReader() throws FileNotFoundException
 	{
 		String configFileName = System.getProperty("xfire.config");
         
         Reader reader = null;
         
         if ( configFileName == null )
         {
             getLogger().info("No configuration file specified.");
             configFileName = "xfire.xml";
         }
 
         File file = new File( configFileName );
         
         if ( file.exists() )
         {
             getLogger().info("Found configuration file " + file.getAbsolutePath());
         	reader = new FileReader( file );
         }
         else
         {
             getLogger().info("Could not find configuration file " + file.getAbsolutePath());
             getLogger().info("Looking in the classpath.");
         
             InputStream is = getClass().getResourceAsStream(configFileName);
             
             if ( is == null )
             {
                 is = getClass().getResourceAsStream("META-INF/xfire/xfire.xml");
                 
                 if ( is == null )
                     return null;
             }
             
             reader = new InputStreamReader( is );
         }
         
 		return reader;
 	}
 
 	/**
 	 * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable#contextualize(org.codehaus.plexus.context.Context)
 	 */
 	public void contextualize(Context context) throws Exception
 	{
 		container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
 	}
 
     /**
      * @see org.codehaus.xfire.plexus.config.ConfigurationService#register(org.codehaus.xfire.plexus.config.ServiceConfigurator)
      */
     public void register( Configurator configurator )
     {
         configurators.put( configurator.getServiceType(),
                            configurator );
     }
 }
