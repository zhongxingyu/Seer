 /**
  *
  */
 package dk.dbc.opensearch.plugins;
 
 import java.io.IOException;
 
 import java.text.ParseException;
 
 import javax.xml.rpc.ServiceException;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.log4j.Logger;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.ValidationException;
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 
import dk.dbc.opensearch.common.fedora.FedoraHandler;
 import dk.dbc.opensearch.common.fedora.FedoraTools;
 import dk.dbc.opensearch.common.pluginframework.IPluggable;
 import dk.dbc.opensearch.common.pluginframework.IRepositoryStore;
 
 
 /**
  * @author stm
  *
  * This plugin handles the Datadock communication with the fedora
  * base. The CargoContainers contents is stored in a DigitalObject
  * object and saved to the fedorabase
  */
public class FaktalinkStore extends FedoraHandler implements IRepositoryStore 
 {
 	Logger log = Logger.getLogger( FaktalinkStore.class );
     
 	private CargoContainer cargo;
 
 	
 	public FaktalinkStore() throws ServiceException 
     {
 		super();
 	}	
 	
     
     /*
      * Initialises the plugin with a cargocontainer.
      */
     public void init( CargoContainer cargo ) throws ServiceException 
     {
         this.cargo = cargo;
     }
 
     
     @Override
 	public String storeCargoContainer() throws MarshalException, ValidationException, IllegalStateException, ServiceException, IOException, ParseException 
 	{
 		return this.storeCargo();
 	}
 
 
     private String storeCargo( )throws ServiceException, MarshalException, ValidationException, IOException, ParseException, IllegalStateException
     {
         NonNegativeInteger nni = new NonNegativeInteger( "1" );
         String[] pids = super.fem.getNextPID( nni, "" );
 
         String descriptive_label = String.format( "Faktalink" );
         
         byte[] foxml = FedoraTools.constructFoxml( this.cargo, pids[0], descriptive_label );
         
         String logm = String.format( "Faktalink inserted" );
         
         String pid = super.fem.ingest( foxml, "Faktalink", logm);
 
         /** \todo: We need a pid-manager for getting lists of available pids for a given ns 
          *  this whole getNextPID vs. ingest return type looks dubious. I fear it will break in a multithreaded environment
          */
         if( ! pid.equals( pids[0] ) )
         {
             log.fatal( String.format( "we expected pid=%s, but got pid=%s", pids[0], pid ) );
             throw new IllegalStateException( String.format( "expected pid=%s, but got pid=%s", pids[0], pid ) );
         }
 
         log.info( String.format( "Submitted data, returning pid %s", pid ) );
         
         return pid;
     }
 
 
 }
