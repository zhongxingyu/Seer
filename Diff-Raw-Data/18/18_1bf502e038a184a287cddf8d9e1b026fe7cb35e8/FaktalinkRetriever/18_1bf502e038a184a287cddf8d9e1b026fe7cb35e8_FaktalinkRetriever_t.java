 /**
  * 
  */
 package dk.dbc.opensearch.plugins;
 
import dk.dbc.opensearch.common.fedora.FedoraHandler;
 import dk.dbc.opensearch.common.pluginframework.IPluggable;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.DataStreamNames;
 
 import info.fedora.www.definitions._1._0.types.DatastreamDef;
 import info.fedora.www.definitions._1._0.types.MIMETypedStream;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.RemoteException;
 import java.util.NoSuchElementException;
 
 import javax.xml.rpc.ServiceException;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * @author stm
  *
  */
public class FaktalinkRetriever extends FedoraHandler implements IPluggable 
 {
	Logger log = Logger.getLogger( "FaktalinkRetriver" );
 	
 	public FaktalinkRetriever() throws ServiceException 
 	{
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	
     /**
      * \brief creates a cargocontainer by getting a dataobject from the repository, identified by the parameters.
      * \todo: what are these parameters?
      *
      * @param pid 
      * @param itemId
      *
      * @returns The cargocontainer constructed
      *
      * @throws IOException something went wrong initializing the fedora client
      * @throws NoSuchElementException if there is no matching element on the queue to pop
      * @throws RemoteException error in communiction with fedora
      * @throws IllegalStateException pid mismatch when trying to write to fedora
      */    
     public CargoContainer getDatastream( String pid, String itemId ) throws IOException, NoSuchElementException, RemoteException, IllegalStateException
     {
         log.debug( String.format( "getDatastream( pid=%s, itemId=%s ) called", pid, itemId ) );
        
         String pidNS = pid.substring( 0, pid.indexOf( ":" ));
        
         /** \todo: very hardcoded value */
         String itemId_version = itemId+".0";
         
         CargoContainer cargo = null;
         DatastreamDef[] datastreams = null;
         MIMETypedStream ds = null;
 
         log.debug( String.format( "Retrieving datastream information for PID %s", pid ) );
         
         datastreams = super.fea.listDatastreams( pid, null );
         
         log.debug( String.format( "Iterating datastreams" ) );
         
         for ( DatastreamDef def : datastreams )
         {
             log.debug( String.format( "Got DatastreamDef with id=%s", def.getID() ) );
             
             if( def.getID().equals( itemId ) )
             {                
                 log.debug( String.format( "trying to retrieve datastream with pid='%s' and itemId_version='%s'", pid, itemId ) );
                 ds = super.fea.getDatastreamDissemination( pid, itemId, null );
                 //ds = apia.getDatastreamDissemination( pid, itemId, null );
                 // pid and def.getID() are equal, why give them both?
  
                 log.debug( String.format( "Making a bytearray of the datastream" ) );
                 byte[] datastr = ds.getStream();
 
                 log.debug( String.format( "Preparing the datastream for the CargoContainer" ) );
                 InputStream inputStream = new ByteArrayInputStream( datastr );
 
                 log.debug( String.format( "DataStream ID      =%s", itemId ) );
                 log.debug( String.format( "DataStream Label   =%s", def.getLabel() ) );
                 log.debug( String.format( "DataStream MIMEType=%s", def.getMIMEType() ) );
 
                 // dc:format holds mimetype as well
                 /** \todo: need to get language dc:language */
                 String language = "";
 
                 cargo = new CargoContainer();
                 
                 DataStreamNames dsn = DataStreamNames.OriginalData;
                 cargo.add( dsn, itemId, pidNS, language, def.getMIMEType(), inputStream);
             }
         }
         
         if( cargo == null )
         {
             throw new IllegalStateException( String.format( "no cargocontainer with data matching the itemId '%s' in pid '%s' ", itemId, pid ) );
         }
 
         log.debug( String.format( "CargoContainer.mimetype =     %s", cargo.getItemsCount() ) );
         log.info( "Successfully retrieved datastream." );
         return cargo;
     }
 }
