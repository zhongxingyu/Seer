 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * \file
  * \brief
  */
 
 
 package dk.dbc.opensearch.common.fedora;
 
 
 import dk.dbc.opensearch.common.metadata.AdministrationStream;
 import dk.dbc.opensearch.common.metadata.DublinCore;
 import dk.dbc.opensearch.common.metadata.DublinCoreElement;
 import dk.dbc.opensearch.common.metadata.IPredicate;
 import dk.dbc.opensearch.common.metadata.MetaData;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.InputPair;
 import dk.dbc.opensearch.common.types.ObjectIdentifier;
 import dk.dbc.opensearch.common.types.OpenSearchTransformException;
 import dk.dbc.opensearch.common.types.TargetFields;
 
 import fedora.common.Constants;
 import fedora.server.types.gen.Condition;
 import fedora.server.types.gen.ComparisonOperator;
 import fedora.server.types.gen.Datastream;
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.types.gen.ObjectFields;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * FedoraObjectRepository is an implementation of the
  * IObjectRepository against a fedora digital object repository.
  */
 public class FedoraObjectRepository implements IObjectRepository
 {
     private static Logger log = Logger.getLogger( FedoraObjectRepository.class );
 
 
     /**
      * Dateformat conforming to the fedora requirements.
      */
     protected static final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" );
     private final String hasStr = "has";
     private FedoraHandle fedoraHandle;
 
 
     /**
      * Initializes the FedoraObjectRepository; tries to connect to the
      * underlying objectrepository. If this fails in some way, an
      * ObjectRepositoryException is thrown.
      */
     public FedoraObjectRepository() throws ObjectRepositoryException
     {
         this.fedoraHandle = new FedoraHandle();
     }
 
 
     @Override
     public boolean hasObject( ObjectIdentifier identifier ) throws ObjectRepositoryException
     {       
         try
         {
             return this.fedoraHandle.hasObject( identifier.getIdentifier() );
         }
         catch ( RemoteException e )
         {
             String error = String.format( "RemoteException Error Connecting to fedora: %s", e.getMessage() );
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     @Override
     public void deleteObject( String identifier, String logmessage ) throws ObjectRepositoryException
     {
         String timestamp = null;
         try
         {
             // note that we're never forcing a deletion
             timestamp = this.fedoraHandle.purgeObject( identifier, logmessage, false );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "ConfigurationException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "ServiceException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "MalformedURLException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( RemoteException ex )
         {
             String error = String.format( "RemoteException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "IOException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         if ( timestamp == null )
         {
             String error = String.format( "Could not delete object reference by pid %s (timestamp == null)", identifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
     }
 
 
     public boolean purgeRelationship( String identifier, String predicate, String object, String dataType ) throws ObjectRepositoryException
     {
         boolean literal = true;
         //System.out.println( String.format( "pid: '%s'; predicate: '%s'; object: '%s'; dataType: '%s'", pid, predicate, object, dataType ) );
         try
         {
             return fedoraHandle.purgeRelationship( identifier, predicate, object, literal, dataType );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "ConfigurationException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "ServiceException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "MalformedURLException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( RemoteException ex )
         {
             String error = String.format( "RemoteException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "IOException Could not delete object referenced by pid '%s': %s", identifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
     }
     
 
     /**
      * Stores data in {@code cargo} in the fedora repository
      *
      * @param cargo the data to store
      * @param logmessage message to log the operation with
      * @return the objectIdentifier of the stored object
      * @throws ObjectRepositoryException if the cargo could not be transformed
      * into foxml or the foxml could not be stored
      */
     @Override
     public String storeObject( CargoContainer cargo, String logmessage, String defaultPidNamespace ) throws ObjectRepositoryException
     {
         if( cargo.getCargoObjectCount() == 0 )
         {
             log.error( String.format( "No data in CargoContainer, refusing to store nothing" ) );
             throw new IllegalStateException( String.format( "No data in CargoContainer, refusing to store nothing" ) );
         }
         
         ObjectIdentifier identifier = getOrGenerateIdentifier( cargo, defaultPidNamespace );
         cargo.setIdentifier( identifier );
         String identifierAsString = identifier.getIdentifier();
         
         cargo.addMetaData( constructAdministrationStream( cargo ) );
         
         byte[] foxml;
         try
         {
             foxml = FedoraUtils.CargoContainerToFoxml( cargo );
         
         }
         catch ( OpenSearchTransformException ex )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         
         String returnedobjectIdentifier = null;
         try
         {
             returnedobjectIdentifier = this.fedoraHandle.ingest( foxml, Constants.FOXML1_1.toString(), logmessage );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s", identifierAsString, ex.getMessage() );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s", identifierAsString, ex.getMessage() );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s. Foxml: %s", identifierAsString, ex.getMessage(), new String( foxml ) );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s. Foxml: %s", identifierAsString, ex.getMessage(), new String( foxml ) );
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         
         if ( returnedobjectIdentifier.equals( "" ) )
         {
             log.info( String.format( "For empty identifier, we recieved '%s' from the ingest", returnedobjectIdentifier ) );
         }
         else if ( ! returnedobjectIdentifier.equals( identifierAsString ) )
         {
             log.warn( String.format( "I expected pid '%s' to be returned from the repository, but got '%s'", identifierAsString, returnedobjectIdentifier ) );
         }
         
         return returnedobjectIdentifier;
     }
     
 
     /**
      * Replaces object identified by {@code identifier} in the fedora repository
      * with {@code cargo}
      *
      * @param identifier the object to replace
      * @param cargo the data to replace object with
      *
      * @throws ObjectRepositoryException if either deletion of the old object or storage of the new went wrong
      */
     @Override
     public void replaceObject( String identifier, CargoContainer cargo ) throws ObjectRepositoryException
     {
         if ( cargo.getCargoObjectCount() == 0 )
         {
             String error = String.format( "Identifier '%s' delivered with an empty CargoContainer", identifier );
             log.error( error );
             throw new IllegalStateException( error );
         }
 
         String cargoIdentifier = cargo.getIdentifierAsString();
 
         if ( cargoIdentifier.isEmpty() )
         {
             String[] newPid = null;
             String prefix = cargo.getCargoObject( DataStreamType.OriginalData ).getSubmitter();
 
             try
             {
                 newPid = fedoraHandle.getNextPID( 1, prefix );
             }
             catch( ServiceException ex )
             {
                 String error = String.format( "Could not retrieve new pids from prefix '%s': %s ", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( ConfigurationException ex )
             {
                 String error = String.format( "Could not retrieve new pids from prefix '%s': %s ", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( MalformedURLException ex )
             {
                 String error = String.format( "Could not retrieve new pids from prefix '%s': %s ", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( IOException ex )
             {
                 String error = String.format( "Could not retrieve new pids from prefix '%s': %s ", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( IllegalStateException ex )
             {
                 String error = String.format( "Could not retrieve new pids from prefix '%s': %s ", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             
             if (null == newPid && 1 != newPid.length)
             {
                 String error = String.format( "pid is empty for namespace '%s', but no exception was caught.", prefix );
                 log.error( error );
                 throw new ObjectRepositoryException( error );
             }
             
             cargoIdentifier = newPid[0];
             cargo.setIdentifier( new PID( cargoIdentifier ) );
         }
 
         String logm = String.format( "Replacing object referenced by pid %s with data previously identified by id %s", identifier, cargoIdentifier );
         deleteObject( identifier, logm );
         
         cargo.setIdentifier( new PID( identifier ) );
         
         String storedObjectPid = storeObject( cargo, logm, "auto" );
         
         if ( ! storedObjectPid.equals( identifier ) )
         {
             String error = String.format( "Could not store replacement object with pid '%s': stored with pid '%s' instead", identifier, storedObjectPid );
             log.error( error );
             throw new ObjectRepositoryException( error );
             //rollback?
             // perhaps instead of a purge, we should use mark-as-deleted and then commit if storedObjectPid.equals( identifier )
         }
     }
     
 
     /**
      * Retrieves an object encoded as a {@link CargoContainer} from the fedora
      * object repository. The method also handles the information given in
      * the administration stream of the object
      *
      * @param identifier the fedora pid identifying the object in the repository
      * @return the object encoded as a {@link CargoContainer}
      * @throws ObjectRepositoryException containing an exception explaining why
      * the object could not be retrieved
      */
     @Override
     public CargoContainer getObject( String identifier ) throws ObjectRepositoryException
     {
         byte[] adminbytes = getDataStreamDissemination( identifier, DataStreamType.AdminData.getName() );
 
         if ( adminbytes == null || adminbytes.length < 1 )
         {
             String error = String.format( "Failed to obtain data for administration stream, cannot retrieve data from '%s'", identifier );
             log.error( error );
             throw new IllegalStateException( error );
         }
         
         AdministrationStream adminStream;
         try
         {
             //log.trace( String.format( "Administration stream: %s", new String( adminbytes ) ) );
             adminStream = new AdministrationStream( new ByteArrayInputStream( adminbytes ), true );
         }
         catch( XMLStreamException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( SAXException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         List< InputPair< Integer, InputPair< String, CargoObject > > > adminstreamlist;
         try
         {
             adminstreamlist = adminStream.getStreams();
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to retrieve streams from administrationstream" );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         
         CargoContainer cargo = new CargoContainer();
         cargo.setIdentifier( new PID( identifier ) );
         
         for ( InputPair< Integer, InputPair< String, CargoObject > > cargoobjects : adminstreamlist )
         {
             String streamId = cargoobjects.getSecond().getFirst();
 
             if ( streamId.equals( DataStreamType.DublinCoreData.getName() + ".0" ) )
             {
                 streamId = "DC";
             }
             log.debug( String.format( "id: %s, streamId: %s", identifier, streamId ) );
             
             byte[] datastream;
             try
             {
                 datastream = this.fedoraHandle.getDatastreamDissemination( identifier, streamId, null );
             }
             catch( ConfigurationException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( MalformedURLException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( IOException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( ServiceException ex )
             {
                 String error = String.format( "Failed to retrieve datastream with id '%s' from pid '%s': %s", streamId, identifier, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
 
             CargoObject co = cargoobjects.getSecond().getSecond();
             
             try
             {
                 if ( co.getDataStreamType() == DataStreamType.DublinCoreData )
                 {
                     DublinCore dc;
                     log.trace( String.format( "Trying to contruct DublinCore element from string: %s", new String( datastream ) ) );
                     try
                     {
                         dc = new DublinCore( new ByteArrayInputStream( datastream ) );
                     }
                     catch( XMLStreamException ex )
                     {
                         String error = String.format( "Failed to construct Dublin Core object from datastream %s from pid '%s': %s", new String( datastream ), identifier, ex.getMessage() );
                         log.error( error );
                         throw new ObjectRepositoryException( error, ex );
                     }
                     
                     String dcid = dc.getDCValue( DublinCoreElement.ELEMENT_IDENTIFIER );
                     log.trace( String.format( "Got dc:identifier '%s' from datastream", dcid ) );
                     if( null == dcid )
                     {
                         log.warn( String.format( "Dublin Core data has no identifier, will use '%s' one from the CargoContainer", identifier ) );
                         dc.setIdentifier( identifier );
                     }
                     else
                     {
                         log.info( String.format( "Adding DublinCore data with id '%s' to CargoContainer", dcid ) );
                     }
                     
                     cargo.addMetaData( dc );
                 }
                 else
                 {
                     cargo.add( co.getDataStreamType(), co.getFormat(), co.getSubmitter(), co.getLang(), co.getMimeType(), co.getIndexingAlias(), datastream );
                 }
             }
             catch( IOException ex )
             {
                 String error = String.format( "Failed to add data with administrationstream id '%s' to CargoContainer: %s", streamId, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
         }
 
         if ( cargo.getCargoObjectCount() < 1 && adminStream.getCount() > 0 )
         {
             throw new ObjectRepositoryException( "CargoContainer is empty, even though adminstream says it gave data" );
         }
 
         if ( cargo.getDublinCoreMetaData() == null )
         {
             DublinCore dc = new DublinCore( identifier );
             dc.setCreator( cargo.getCargoObject( DataStreamType.OriginalData ).getSubmitter() );
             dc.setFormat( cargo.getCargoObject( DataStreamType.OriginalData ).getFormat() );
             dc.setLanguage( cargo.getCargoObject( DataStreamType.OriginalData ).getLang() );
         }
 
         return cargo;
     }
 
 
     @Override
     public List< String > getIdentifiers( String verbatimSearchString, List< TargetFields > searchableFields, String cutPid, int maximumResults )
     {
         String[] resultFields = new String[ searchableFields.size() ];
         List< InputPair< TargetFields, String > > resultSearchFields = new ArrayList< InputPair< TargetFields, String > >();
 
         int i = 0;
         for( TargetFields field : searchableFields )
         {
             TargetFields property = field;
             resultFields[i] = field.fieldname();
             InputPair< TargetFields, String > pair = new InputPair< TargetFields, String >( property,  verbatimSearchString );
             resultSearchFields.add( pair );
             i++;
         }
 
         ObjectFields[] objectFields = searchRepository( resultFields, resultSearchFields, hasStr, maximumResults, null );
 
         int ofLength = objectFields.length;
         List< String > pids = new ArrayList< String >( ofLength );
         log.debug( String.format( "No of objectFields lines %s", objectFields.length ) );
         for( int j = 0; j < ofLength; j++ )
         {
             pids.add( objectFields[j].getPid() );
         }
 
         return pids;
     }
     
 
 
     @Override
     public List< String > getIdentifiers( List< InputPair< TargetFields, String > > resultSearchFields, String cutPid, int maximumResults )
     {
         String[] resultFields = new String[ resultSearchFields.size() + 1 ];
         int i = 0;
         for( InputPair< TargetFields, String > field : resultSearchFields )
         {
             TargetFields property = field.getFirst();
             resultFields[i] = property.fieldname();
             i++;
         }
 
         resultFields[ i++ ] = new String( "pid" ); // must be present!
         ObjectFields[] objectFields = searchRepository( resultFields, resultSearchFields, hasStr, maximumResults, null );
 
         int ofLength = objectFields.length;
         List< String > pids = new ArrayList< String >( ofLength );
         for( int j = 0; j < ofLength; j++ )
         {
             String pidValue = objectFields[j].getPid();
             if( cutPid == null )
             {
                 pids.add( pidValue );
             }
             else if( ! pidValue.equals( cutPid ) )
             {
                 pids.add( pidValue );
                 return pids;
             }
         }
 
         return pids;
     }
     
 
     @Override
     public List< String > getIdentifiers( List< InputPair< TargetFields, String > > resultSearchFields, String cutPid, int maximumResults, String namespace )
     {
         String[] resultFields = new String[ resultSearchFields.size() + 1 ];
         int i = 0;
         for( InputPair< TargetFields, String > field : resultSearchFields )
         {
             TargetFields property = field.getFirst();
             resultFields[i] = property.fieldname();
             i++;
         }
 
         resultFields[i++] = "pid"; // must be present!
         ObjectFields[] objectFields = searchRepository( resultFields, resultSearchFields, hasStr, maximumResults, namespace );
         
         int ofLength = objectFields.length;
         List< String > pids = new ArrayList< String >( ofLength );
         for( int j = 0; j < ofLength; j++ )
         {
             String pidValue = objectFields[j].getPid();
             log.debug( String.format( "Matching pid: %s", pidValue ) );
             // Check to weed out in exact matches
            if ( addPidValue( resultSearchFields, objectFields, namespace ) )
             {
                 log.debug( String.format( "Matching do addPidValue", "" ) );
                 if ( pidValue.contains( namespace ) )
                 {
                     if ( cutPid == null )
                     {
                         pids.add( pidValue );
                     }
                     else if ( ! pidValue.equals( cutPid ) )
                     {
                         pids.add( pidValue );
                         return pids;
                     }
                 }
             }
         }
 
         return pids;
     }
 
 
    private boolean addPidValue( List< InputPair< TargetFields, String > > resultFields, ObjectFields[] objectFields, String namespace )
     {
         boolean ret = false;
         log.debug( String.format( "Matching size: '%s'", resultFields.size() ) );
         for ( InputPair< TargetFields, String > pair : resultFields )
         {
             FedoraObjectFields target = (FedoraObjectFields)pair.getFirst();
             log.debug( String.format( "Matching resultField: '%s'", target ) );
             String value = pair.getSecond();
            for ( ObjectFields of : objectFields )
            {
                 String pid = of.getPid().toLowerCase();
                 log.debug( String.format( "Matching pid: '%s'", pid ) );
                 if ( pid.contains( namespace ) )
                 {
                     switch ( target )
                     {
                         case PID:
                             log.debug( String.format( "PID Matching '%s' and '%s'", pid, value ) );
                             if ( pid.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case STATE:
                             String state = of.getState().toLowerCase();
                             log.debug( String.format( "STATE Matching '%s' and '%s'", state, value ) );
                             if ( state.equals( value) )
                             {
                                 ret = true;
                             }
                             break;
                         case OWNERID:
                             String ownerId = of.getOwnerId().toLowerCase();
                             log.debug( String.format( "OWNERID Matching '%s' and '%s'", ownerId, value ) );
                             if ( ownerId.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case CDATE:
                             String cDate = of.getCDate().toLowerCase();
                             log.debug( String.format( "CDATE Matching '%s' and '%s'", cDate, value ) );
                             if ( cDate.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case MDATE:
                             String mDate = of.getMDate().toLowerCase();
                             log.debug( String.format( "MDATE Matching '%s' and '%s'", mDate, value ) );
                             if ( mDate.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case TITLE:
                             String title = of.getTitle()[0].toLowerCase();
                             log.debug( String.format( "TITLE Matching '%s' and '%s'", title, value ) );
                             if ( title.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case CREATOR:
                             String creator = of.getCreator()[0].toLowerCase();
                             log.debug( String.format( "CREATOR Matching '%s' and '%s'", creator, value ) );
                             if ( creator.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case SUBJECT:
                             String subject = of.getSubject()[0].toLowerCase();
                             log.debug( String.format( "SUBJECT Matching '%s' and '%s'", subject, value ) );
                             if ( subject.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case DESCRIPTION:
                             String description = of.getDescription()[0].toLowerCase();
                             log.debug( String.format( "DESCRIPTION Matching '%s' and '%s'", description, value ) );
                             if ( description.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case PUBLISHER:
                             String publisher = of.getPublisher()[0].toLowerCase();
                             log.debug( String.format( "PUBLISHER Matching '%s' and '%s'", publisher, value ) );
                             if ( publisher.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case CONTRIBUTOR:
                             String contributor = of.getContributor()[0].toLowerCase();
                             log.debug( String.format( "CONTRIBUTOR Matching '%s' and '%s'", contributor, value ) );
                             if ( contributor.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case DATE:
                             String date = of.getDate()[0].toLowerCase();
                             log.debug( String.format( "DATE Matching '%s' and '%s'", date, value ) );
                             if ( date.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case TYPE:
                             String type = of.getType()[0].toLowerCase();
                             log.debug( String.format( "TYPE Matching '%s' and '%s'", type, value ) );
                             if ( type.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case FORMAT:
                             String format = of.getFormat()[0].toLowerCase();
                             log.debug( String.format( "FORMAT Matching '%s' and '%s'", format, value ) );
                             if ( format.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case IDENTIFIER:
                             String identifier = of.getIdentifier()[0].toLowerCase();
                             log.debug( String.format( "IDENTIFIER Matching '%s' and '%s'", identifier, value ) );
                             if ( identifier.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case SOURCE:
                             String source = of.getSource()[0].toLowerCase();
                             log.debug( String.format( "SOURCE Matching '%s' and '%s'", source, value ) );
                             if ( source.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case LANGUAGE:
                             String language = of.getLanguage()[0].toLowerCase();
                             log.debug( String.format( "LANGUAGE Matching '%s' and '%s'", language, value ) );
                             if ( language.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case RELATION:
                             String relation = of.getRelation()[0].toLowerCase();
                             log.debug( String.format( "RELATION Matching '%s' and '%s'", relation, value ) );
                             if ( relation.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case COVERAGE:
                             String coverage = of.getCoverage()[0].toLowerCase();
                             log.debug( String.format( "COVERAGE Matching '%s' and '%s'", coverage, value ) );
                             if ( coverage.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case RIGHTS:
                             String rights = of.getRights()[0].toLowerCase();
                             log.debug( String.format( "RIGHTS Matching '%s' and '%s'", rights, value ) );
                             if ( rights.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         case DCMDATE:
                             String dcmDate = of.getDcmDate().toLowerCase();
                             log.debug( String.format( "DCMDATE Matching '%s' and '%s'", dcmDate, value ) );
                             if ( dcmDate.equals( value ) )
                             {
                                 ret = true;
                             }
                             break;
                         default:
                             //throw new ObjectRepositoryException( "No match!" );
                     }
                 }
            }
         }
 
         log.debug( String.format( "RET Matching returning: '%s'", ret ) );
         return ret;
     }
     
 
 
     @Override
     public List< String > getIdentifiersUnqualified( List< InputPair< TargetFields, String > > resultSearchFields, int maximumResults )
     {
         String[] resultFields = new String[ resultSearchFields.size() + 1 ];
         int i = 0;
         for (InputPair< TargetFields, String > field : resultSearchFields )
         {
             TargetFields property = field.getFirst();
             resultFields[i] = property.fieldname();
             i++;
         }
 
         resultFields[i++] = "pid"; // must be present!
         ObjectFields[] objectFields = searchRepository( resultFields, resultSearchFields, hasStr, maximumResults, null );
 
         int ofLength = objectFields.length;
         List< String > pids = new ArrayList< String >( ofLength );
         for( int j = 0; j < ofLength; j++ )
         {
             String pidValue = objectFields[j].getPid();
             pids.add( pidValue );
         }
 
         return pids;
     }
     
 
 
     /***
      * Searches repository with conditions specified by propertiesAndValues using comparisonOperator, e.g. 'has', 'eq'.
      * The parameter 'namespace' if not null is used to limit the result set on pid containing namespace. Beware that
      * the comparison operator in this case cannot be 'eq'.
      *
      * @param resultFields
      * @param propertiesAndVaulues
      * @param comparisonOperator In most case this should be 'has'. 'eq' cannot be used with namespace not null!!!
      * @param maximumResults
      * @param namespace Used to limit on pid containing namespace.
      * @return
      */
     ObjectFields[] searchRepository( String[] resultFields, List< InputPair< TargetFields, String > > propertiesAndVaulues, String comparisonOperator, int maximumResults, String namespace )
     {
         // \Todo: check needed on the operator
         int size = propertiesAndVaulues.size();
         ComparisonOperator comp = ComparisonOperator.fromString( comparisonOperator );
 
         Condition[] cond;
         log.debug( String.format( "Matching namespace: %s", namespace ) );
         if ( namespace != null )
         {
             cond = new Condition[ size + 1 ];
         }
         else
         {
             cond = new Condition[ size ];
         }
 
         int i = 0;
         for( ; i < size; i++ )
         {
             InputPair< TargetFields, String > pair = propertiesAndVaulues.get( i );
             TargetFields property = pair.getFirst();
             String value = pair.getSecond();
             cond[i] = new Condition( property.fieldname(), comp, value );
         }
 
         if ( namespace != null )
         {
             if ( ! namespace.endsWith( ":" ) )
             {
                 namespace = namespace + ":*";
             }
             else if ( namespace.endsWith( ":" ) )
             {
                 namespace = namespace + "*";
             }
 
             cond[i++] = new Condition( FedoraObjectFields.PID.fieldname(), comp, namespace );
         }
 
         FieldSearchQuery fsq = new FieldSearchQuery( cond, null );
         FieldSearchResult fsr = null;
         try
         {
             NonNegativeInteger maxResults = new NonNegativeInteger( Integer.toString( maximumResults ) );
             fsr = this.fedoraHandle.findObjects( resultFields, maxResults, fsq );
             log.debug( String.format( "Result length of resultlist: %s", fsr.getResultList().length ) );
         }
         catch( ConfigurationException ex )
         {
             String warn = String.format( "ConfigurationException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
         catch( ServiceException ex )
         {
             String warn = String.format( "ServiceException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
         catch( MalformedURLException ex )
         {
             String warn = String.format( "MalformedURLException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
         catch( IOException ex )
         {
             String warn = String.format( "IOException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
 
         if( fsr == null )
         {
             log.warn( "Retrieved no hits from search, returning empty List<String>" );
             return new ObjectFields[]{ };
         }
         
         ObjectFields[] objectFields = fsr.getResultList();
 
         return objectFields;
     }
     
 
     /**
      * This code has been commented out since it is not used anywhere
      * and there seems to be no intentions of ever using it. Should
      * anyone encounter this commented-out method later than July 1st
      * 2010, it should be removed altogether.
      */
     // @Override
     // public void storeDataInObject( String identifier, CargoObject object, boolean versionable, boolean overwrite ) throws ObjectRepositoryException
     // {
 
     //     AdministrationStream admStream = getAdministrationStream( identifier );
     //     int count = admStream.getCount( object.getDataStreamType() );
     //     String dsId = object.getDataStreamType().getName() + Integer.toString( count + 1 );
     //     boolean addedData = addDataUpdateAdminstream( identifier, dsId, object );
 
     //     if( !addedData )
     //     {
     //         log.warn( String.format( "Could not add data or update administration stream for pid %s, dsId %s", identifier, dsId ) );
     //     }
 
     //     //upload the content
 
     //     /** \todo: the java.nio.channels should relieve the worl of this ugly hack. Please redeem this code*/
     //     ByteArrayInputStream bais = new ByteArrayInputStream( object.getBytes() );
     //     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     //     byte[] buf = new byte[1024];
     //     int len;
     //     try
     //     {
     //         while( (len = bais.read( buf )) > 0 )
     //         {
     //             baos.write( buf, 0, len );
     //         }
     //     }
     //     catch( IOException ex )
     //     {
     //         String error = String.format( "Could not hack my way through outdated java api for copying inputstreams to outputstreams, sorry" );
     //         log.error( error );
     //         throw new ObjectRepositoryException( error, ex );
     //     }
 
     //     /** end ugly code hack ;)*/
     //     String dsLocation = uploadDatastream( baos );
     //     String logm = String.format( "added %s to the object with pid: %s", dsLocation, identifier );
     //     String returnedSID = null;
     //     try
     //     {
     //         returnedSID = this.fedoraHandle.addDatastream( identifier, dsId, new String[]
     //             {
     //             }, object.getFormat(), versionable, object.getMimeType(), null, dsLocation, "M", "A", null, null, logm );
     //     }
     //     catch( ConfigurationException ex )
     //     {
     //         String error = String.format( "Failed to add Datastream with id '%s' to object with pid '%s': %s", dsId, identifier, ex.getMessage() );
     //         log.error( error );
     //         throw new ObjectRepositoryException( error, ex );
     //     }
     //     catch( ServiceException ex )
     //     {
     //         String error = String.format( "Failed to add Datastream with id '%s' to object with pid '%s': %s", dsId, identifier, ex.getMessage() );
     //         log.error( error );
     //         throw new ObjectRepositoryException( error, ex );
     //     }
     //     catch( MalformedURLException ex )
     //     {
     //         String error = String.format( "Failed to add Datastream with id '%s' to object with pid '%s': %s", dsId, identifier, ex.getMessage() );
     //         log.error( error );
     //         throw new ObjectRepositoryException( error, ex );
     //     }
     //     catch( IOException ex )
     //     {
     //         String error = String.format( "Failed to add Datastream with id '%s' to object with pid '%s': %s", dsId, identifier, ex.getMessage() );
     //         log.error( error );
     //         throw new ObjectRepositoryException( error, ex );
     //     }
 
     //     if( returnedSID == null )
     //     {
     //         String error = String.format( "Failed to add datastream to object with pid '%s'", identifier );
     //         log.error( error );
     //         throw new ObjectRepositoryException( error );
     //     }
     // }
     
 
     @Override
     public void deleteDataFromObject( String objectIdentifier, String dataIdentifier ) throws ObjectRepositoryException
     {
         String logm = String.format( "removed stream %s from object %s", dataIdentifier, objectIdentifier );
         String startDate = null;
         String endDate = null;
         String[] stamp;
         try
         {
             stamp = this.fedoraHandle.purgeDatastream( objectIdentifier, dataIdentifier, startDate, endDate, logm, true );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Failed to remove data with id '%s' from object with pid '%s': %s", dataIdentifier, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Failed to remove data with id '%s' from object with pid '%s': %s", dataIdentifier, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Failed to remove data with id '%s' from object with pid '%s': %s", dataIdentifier, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to remove data with id '%s' from object with pid '%s': %s", dataIdentifier, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         if( stamp == null )
         {
             String error = String.format( "Failed to remove data with id '%s' from object with pid '%s'", dataIdentifier, objectIdentifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
 
         boolean updated = removeDataUpdateAdminstream( objectIdentifier, dataIdentifier );
 
         if( !updated )
         {
             /** \todo: what to do here? the data was deleted, but we
              * could no update the administration stream. We're in
              * deep manure. We should do a rollback here or instead of
              * purging the datastream, we should mark it as deleted
              * and then try to unmark it here.*/
             String error = String.format( "Failed to remove data with id '%s' from object with pid '%s'", dataIdentifier, objectIdentifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
     }
     
 
     @Override
     public CargoContainer getDataFromObject( String objectIdentifier, DataStreamType streamtype ) throws ObjectRepositoryException
     {
         if ( null == streamtype )
         {
             String error = String.format( "DataStreamType was null, cannot perform search in repository" );
             log.error( error );
             throw new ObjectRepositoryException( new IllegalArgumentException( error ) );
         }
         
         AdministrationStream adminStream = getAdministrationStream( objectIdentifier );
         
         CargoContainer cargo = new CargoContainer();
         cargo.setIdentifier( new PID( objectIdentifier ) );
         
         try
         {
             for( InputPair<Integer, InputPair<String, CargoObject>> pairs : adminStream.getStreams() )
             {
                 DataStreamType datastreamtype = pairs.getSecond().getSecond().getDataStreamType();
                 if( datastreamtype == streamtype )
                 {
                     String dsId = pairs.getSecond().getFirst();
                     CargoObject co = pairs.getSecond().getSecond();
                     byte[] data;
                     try
                     {
                         data = this.fedoraHandle.getDatastreamDissemination( objectIdentifier, dsId, null );
                     }
                     catch( ConfigurationException ex )
                     {
                         String error = String.format( "Failed to retrieve data identified by '%s' from objectrepository pid '%s': %s", dsId, objectIdentifier, ex.getMessage() );
                         log.error( error );
                         throw new ObjectRepositoryException( error, ex );
                     }
                     catch( MalformedURLException ex )
                     {
                         String error = String.format( "Failed to retrieve data identified by '%s' from objectrepository pid '%s': %s", dsId, objectIdentifier, ex.getMessage() );
                         log.error( error );
                         throw new ObjectRepositoryException( error, ex );
                     }
                     catch( ServiceException ex )
                     {
                         String error = String.format( "Failed to retrieve data identified by '%s' from objectrepository pid '%s': %s", dsId, objectIdentifier, ex.getMessage() );
                         log.error( error );
                         throw new ObjectRepositoryException( error, ex );
                     }
                     cargo.add( datastreamtype, co.getFormat(), co.getSubmitter(), co.getLang(), co
                             .getMimeType(), co.getIndexingAlias(), data );
                 }
             }
         }
         catch( IOException ex )
         {
             String error = String.format( "Could not retrieve information from administration stream" );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         log.warn( String.format( "Returning empty CargoContainer for request of %s on %s", streamtype.getName(), objectIdentifier ) );
         return cargo;
     }    
 
 
     @Override
     public CargoContainer getDataFromObject( String objectIdentifier, String dataIdentifier ) throws ObjectRepositoryException
     {
         AdministrationStream adminStream = getAdministrationStream( objectIdentifier );
         CargoContainer cargo = new CargoContainer();
         cargo.setIdentifier( new PID( objectIdentifier ) );
         
         try
         {
             for( InputPair<Integer, InputPair<String, CargoObject>> pairs : adminStream.getStreams() )
             {
                 String dsId = pairs.getSecond().getFirst();
 
                 if( dataIdentifier.equals( dsId ) )
                 {
                     CargoObject co = pairs.getSecond().getSecond();
                     byte[] data;
                     try
                     {
                         log.trace( String.format( "getDatastreamDissemination( %s, %s, null )", objectIdentifier, dsId ) );
                         data = this.fedoraHandle.getDatastreamDissemination( objectIdentifier, dsId, null );
                     }
                     catch (ConfigurationException ex)
                     {
                         String error = String.format( "Failed to retrieve data identified by '%s' from objectrepository pid '%s': %s", dsId, objectIdentifier, ex.getMessage() );
                         log.error( error );
                         throw new ObjectRepositoryException( error, ex );
                     }
                     catch( MalformedURLException ex )
                     {
                         String error = String.format( "Failed to retrieve data identified by '%s' from objectrepository pid '%s': %s", dsId, objectIdentifier, ex.getMessage() );
                         log.error( error );
                         throw new ObjectRepositoryException( error, ex );
                     }
                     catch( ServiceException ex )
                     {
                         String error = String.format( "Failed to retrieve data identified by '%s' from objectrepository pid '%s': %s", dsId, objectIdentifier, ex.getMessage() );
                         log.error( error );
                         throw new ObjectRepositoryException( error, ex );
                     }
                     cargo.add( co.getDataStreamType(), co.getFormat(), co.getSubmitter(), co.getLang(), co.getMimeType(), co.getIndexingAlias(), data );
                 }
             }
         }
         catch( IOException ex )
         {
             String error = String.format( "Could not retrieve information from administration stream" );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         return cargo;
     }
     
     /**
      * This code has been commented out since it is not used anywhere
      * and there seems to be no intentions of ever using it. Should
      * anyone encounter this commented-out method later than July 1st
      * 2010, it should be removed altogether.
      */
     // @Override
     // public void replaceDataInObject( String objectIdentifier, String dataIdentifier, CargoObject cargo ) throws ObjectRepositoryException
     // {
 
     //     /** \todo: this is just a wrapper for the two-phase operation
     //      * of deleting and storing data. It inherits the weaknesses
     //      * from both methods, as an exception thrown somewhere in the
     //      * operations renders the object repository in an inconsistent
     //      * state*/
     //     deleteDataFromObject( objectIdentifier, dataIdentifier );
     //     storeDataInObject( objectIdentifier, cargo, true, true );
     // }
     
 
     private AdministrationStream constructAdministrationStream( CargoContainer cargo ) throws ObjectRepositoryException
     {
         String indexingAlias = cargo.getIndexingAlias( DataStreamType.OriginalData );
         if( indexingAlias == null )
         {
             log.warn( String.format( "Supplied CargoContainer (format %s) has no Original Data, I find it hard to construct an indexing alias given the circumstances. Instead, it'll be IndexingAlias.None", cargo.getCargoObject( DataStreamType.OriginalData ).getFormat() ) );
             indexingAlias = "NULL - no alias found";
         }
         AdministrationStream adminStream = new AdministrationStream( indexingAlias );
 
         if( 0 == cargo.getCargoObjectCount() )
         {
             String error = String.format( "Refusing to construct AdministrationStream when CargoContainer (%s) has no data in it", cargo.getIdentifier() );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
         int streamtypecounter = 0;
 
         log.debug( String.format( "Constructing administration stream for %s cargo objects", cargo.getTotalObjectCount() - 1 ) );
         for( CargoObject co : cargo.getCargoObjects() )
         {
             streamtypecounter = adminStream.getCount( co.getDataStreamType() );
             String id = co.getDataStreamType().getName() + "." + Integer.toString( streamtypecounter );
             log.debug( String.format( "Adding stream '%s' with id '%s' to adminstream", co.getDataStreamType().getName(), id ) );
             adminStream.addStream( co, id );
         }
         streamtypecounter = 0;
         for( MetaData meta : cargo.getMetaData() )
         {
             streamtypecounter = adminStream.getCount( meta.getType() );
             String id = meta.getType().getName() + "." + Integer.toString( streamtypecounter );
             log.debug( String.format( "Adding stream '%s' with id '%s' to adminstream", meta.getType().getName(), id ) );
             adminStream.addStream( meta, id );
         }
         return adminStream;
     }
     
 
     /**
      * Wrapper around {@link #getDataStream(java.lang.String, dk.dbc.opensearch.common.types.DataStreamType)}
      * that retrieves an {@link AdministrationStream} and handles intermediary
      * exceptions that surface from the lower layers.
      *
      * @param objectIdentifier the identifies the object in the repository
      * @return the AdministrationStream for the object identified by
      * {@code objectIdentifier}
      * @throws ObjectRepositoryException if the administrationstream could not
      * be retrieved
      */
     private AdministrationStream getAdministrationStream( String objectIdentifier ) throws ObjectRepositoryException
     {
         AdministrationStream adminStream;
         byte[] adminbytes = null;
         try
         {
             adminbytes = getDataStreamDissemination( objectIdentifier, DataStreamType.AdminData.getName() );
             adminStream = new AdministrationStream( new ByteArrayInputStream( adminbytes ), true );
         }
         catch( XMLStreamException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( SAXException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to contruct administration stream from retrieved xml [%s]: %s", new String( adminbytes ), ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         return adminStream;
     }    
 
 
     private boolean addDataUpdateAdminstream( String objectIdentifier, String dataIdentifier, CargoObject obj ) throws ObjectRepositoryException
     {
         AdministrationStream adminStream = getAdministrationStream( objectIdentifier );
 
         boolean added = adminStream.addStream( obj, dataIdentifier );
 
         if( !added )
         {
             log.warn( "Could not add information on added stream to administration stream" );
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             adminStream.serialize( baos, null );
         }
         catch( OpenSearchTransformException ex )
         {
             String error = String.format( "Could not retrieve administration stream serialization" );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         String label = "admin [text/xml]";
         String mime = "text/xml";
         String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
         String adminLogm = "admin stream updated with added stream data" + timeNow;
         String location = uploadDatastream( baos );
         String[] alternatedsids = new String[]
             {
             };
 
         try
         {
             this.fedoraHandle.modifyDatastreamByReference( objectIdentifier, DataStreamType.AdminData.getName(), alternatedsids, label, mime, "", location, null, null, adminLogm, true );
         }
         catch( RemoteException ex )
         {
             String error = String.format( "Failed to replace administration stream on object with pid '%s': %s", objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         return added;
     }    
 
 
     private boolean removeDataUpdateAdminstream( String objectIdentifier, String dataIdentifier ) throws ObjectRepositoryException
     {        
         AdministrationStream adminStream = getAdministrationStream( objectIdentifier );
         
         boolean removed = adminStream.removeStream( dataIdentifier );
 
         if ( !removed )
         {
             log.warn( "Could not remove stream from adminstrationstream" );
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             adminStream.serialize( baos, null );
         }
         catch( OpenSearchTransformException ex )
         {
             String error = String.format( "Could not retrieve administration stream serialization" );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         String adminLabel = "admin [text/xml]";
         String adminMime = "text/xml";
         String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
         String adminLogm = "admin stream updated with added stream data" + timeNow;
 
         String admLocation = uploadDatastream( baos );
 
         String[] empty = new String[]{};
 
         try
         {
             this.fedoraHandle.modifyDatastreamByReference( objectIdentifier, DataStreamType.AdminData.getName(), empty, adminLabel, adminMime, null, admLocation, null, null, adminLogm, true );
         }
         catch( RemoteException ex )
         {
             String error = String.format( "Failed to replace administration stream on object with objectIdentifier '%s'", objectIdentifier );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         return removed;
     }
 
 
     public Datastream getDatastream( String objectIdentifier, String dataStreamID ) throws ObjectRepositoryException
     {
         Datastream ds = null;
 
         try
         {
             ds = this.fedoraHandle.getDatastream( objectIdentifier, dataStreamID );
         }
         catch( RemoteException re )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamID, objectIdentifier, re.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, re );
         }
 
         if ( ds == null )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': Got nothing back from the object repository", dataStreamID, objectIdentifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
 
         return ds;
     }
     
 
     private byte[] getDataStreamDissemination( String objectIdentifier, String dataStreamTypeName ) throws ObjectRepositoryException
     {
         if ( null == objectIdentifier || null == dataStreamTypeName || objectIdentifier.isEmpty() || dataStreamTypeName.isEmpty() )
         {
             String error = String.format( "Necessary parameters for retrieving datastream was null" );
             log.error( error );
             throw new IllegalStateException( error );
         }
 
         byte[] ds = null;
         try
         {
             ds = this.fedoraHandle.getDatastreamDissemination( objectIdentifier, dataStreamTypeName, null );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamTypeName, objectIdentifier, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         if ( null == ds )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': Got nothing back from the object repository", dataStreamTypeName, objectIdentifier );
             log.error( error );
             throw new ObjectRepositoryException( error );
         }
 
         return ds;
     }    
 
 
     private String uploadDatastream( ByteArrayOutputStream datastream ) throws ObjectRepositoryException
     {
         File tmpFile;
         try
         {
             String timeStamp = Long.toString( System.currentTimeMillis() ) + Integer.toString( datastream.hashCode() );
             tmpFile = File.createTempFile( "datastream" + timeStamp, ".tmp" );
         }
         catch( IOException ex )
         {
             String error = "Could not create temporary file";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         tmpFile.deleteOnExit();
         FileOutputStream faos;
         try
         {
             faos = new FileOutputStream( tmpFile );
         }
         catch( FileNotFoundException ex )
         {
             String error = "Could not write to temporary file";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         try
         {
             datastream.writeTo( faos );
         }
         catch( IOException ex )
         {
             String error = "Failed to write administration stream to temporary file";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         String location;
         try
         {
             location = this.fedoraHandle.uploadFile( tmpFile );
         }
         catch( IOException ex )
         {
             String error = "Failed to upload temporary file to fedora server";
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         return location;
     }
     
 
     @Override
     public void addObjectRelation( ObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException
     {
         try
         {
             String relationString = relation.getPredicateString();
             log.debug( String.format( "trying to add %s - %s -> %s", objectIdentifier.getIdentifier(), relationString, subject ) );
             this.fedoraHandle.addRelationship( objectIdentifier.getIdentifier(), relationString, subject, true, null );
         }
         catch( IOException ex )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ConfigurationException e )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
 
         }
         catch( ServiceException e )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     @Override
     public List< InputPair< IPredicate, String > > getObjectRelations( ObjectIdentifier objectIdentifier ) throws ObjectRepositoryException
     {
         return null;
     }
     
 
     @Override
     public void removeObjectRelation( ObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException
     {
         try
         {
             String relationString = relation.getPredicateString();
             String pid = objectIdentifier.getIdentifier();
 
             log.debug( String.format( "trying to removed %s - %s -> %s", pid, relationString, subject ) );
             boolean purgeRelationship = this.fedoraHandle.purgeRelationship( pid, relationString, subject, true, null );
             if( purgeRelationship )
             {
                 log.info( String.format( "Ignored error from purgeRelationeship : on %s-%s->%s", pid, relationString, subject ) );
             }
         }
         catch( IOException ex )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, ex );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( ConfigurationException e )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
             
         }
         catch (ServiceException e)
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
     
 
     /**
      * Internal helper to store
      * 
      * @param cargo
      * @return
      * @throws ObjectRepositoryException
      */
     private ObjectIdentifier getOrGenerateIdentifier( CargoContainer cargo, String defaultPidNameSpace ) throws ObjectRepositoryException
     {
         ObjectIdentifier identifier = cargo.getIdentifier();
         
         if (identifier != null)
         {
             return identifier;
         }
         
         if (defaultPidNameSpace == null)
         {
             defaultPidNameSpace = new String( "auto" );
         }
         
         String newPid = "";
         try
         {
             newPid = fedoraHandle.getNextPID( 1, defaultPidNameSpace )[0];
         }
         catch (ConfigurationException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
         catch (MalformedURLException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
         catch (ServiceException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
         catch (IOException e)
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
         
         return new PID( newPid );        
     }    
 }
