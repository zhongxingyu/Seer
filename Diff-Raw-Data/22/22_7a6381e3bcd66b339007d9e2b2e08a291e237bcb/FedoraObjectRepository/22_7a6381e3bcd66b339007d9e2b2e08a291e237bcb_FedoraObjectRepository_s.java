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
 import dk.dbc.opensearch.common.metadata.IPredicate;
 import dk.dbc.opensearch.common.metadata.MetaData;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.Pair;
 import dk.dbc.opensearch.common.types.ObjectIdentifier;
 import dk.dbc.opensearch.common.types.OpenSearchTransformException;
 import dk.dbc.opensearch.common.types.TargetFields;
 
 import fedora.common.Constants;
 import fedora.server.types.gen.Condition;
 import fedora.server.types.gen.ComparisonOperator;
 import fedora.server.types.gen.Datastream;
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.types.gen.ListSession;
 import fedora.server.types.gen.ObjectFields;
 import fedora.server.types.gen.RelationshipTuple;
 
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
 import java.util.LinkedList;
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
             returnedobjectIdentifier = this.fedoraHandle.ingest( foxml, Constants.FOXML1_1.uri, logmessage );
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
 
         List< Pair< Integer, Pair< String, CargoObject > > > adminstreamlist;
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
 
         for ( Pair< Integer, Pair< String, CargoObject > > cargoobjects : adminstreamlist )
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
                 cargo.add( co.getDataStreamType(), co.getFormat(), co.getSubmitter(), co.getLang(), co.getMimeType(),  datastream );
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
 
         return cargo;
     }
 
 
     @Override
     @Deprecated
     public List< String > getIdentifiers( String verbatimSearchString, List< TargetFields > searchableFields, String cutPid, int maximumResults )
     {
         String[] resultFields = new String[ searchableFields.size() ];
         List< Pair< TargetFields, String > > resultSearchFields = new ArrayList< Pair< TargetFields, String > >();
 
         int i = 0;
         for( TargetFields field : searchableFields )
         {
             TargetFields property = field;
             resultFields[i] = field.fieldname();
             Pair< TargetFields, String > pair = new Pair< TargetFields, String >( property,  verbatimSearchString );
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
     public List< String > getIdentifiers( List< Pair< TargetFields, String > > resultSearchFields, String cutPid, int maximumResults )
     {
         String[] resultFields = new String[ resultSearchFields.size() + 1 ];
         int i = 0;
         for( Pair< TargetFields, String > field : resultSearchFields )
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
 
     /**
      *\Todo: This method is nearly identical to the one just below. The reason
      * why that isnt changed right now is that the method below is used for the
      * current matching of works and this method is used for the next generation
      * of matching including javascript to define the business logic
      * Please do notice that there is no cutPid param
      *
      * The difference between the method below and this method is the call to
      * either addPidValue or addPidValueExpanded and that there is no cutPid param.
      */
     @Override
     public List< String > getIdentifiersWithNamespace( List< Pair< TargetFields, String > > resultSearchFields, int maximumResults, String namespace )
     {
         String[] resultFields = new String[ resultSearchFields.size() + 1 ];
         int i = 0;
         for( Pair< TargetFields, String > field : resultSearchFields )
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
             ObjectFields of = objectFields[j];
 
             /**
              * The addPidValueExpanded method is an expansion of the addPidValue method
              * used in the very similar method below.
              */
             if ( addPidValueExpanded( resultSearchFields, of, namespace ) )
             {
                 log.debug( String.format( "Matching do addPidValueExpanded %s", pidValue ) );
                 if ( pidValue.contains( namespace ) )
                 {
                     pids.add( pidValue );
                 }
             }
         }
         return pids;
     }
 
 
     /**
      * Method for getting pids from the fedora that has fields that matches
      * the match depends on the searchstring. If there are no '*' in the
      * beginning or the end, the searchstring is matched with the equals method
      * otherwise it is accepted. The switch is different then in the addPidValue
      * method in the way that it returns false if the strings arent equal.
      */
     private boolean addPidValueExpanded( List< Pair< TargetFields, String > > resultFields, ObjectFields of /* objectFields */, String namespace )
     {
         boolean ret = true;
         log.debug( String.format( "Matching size: '%s'", resultFields.size() ) );
         for ( Pair< TargetFields, String > pair : resultFields )
         {
             FedoraObjectFields target = (FedoraObjectFields)pair.getFirst();
             log.debug( String.format( "Matching resultField: '%s'", target ) );
             String value = pair.getSecond();
             //check the the value neither starts nor ends in a '*'
             //if it does skip the switch
             if( ! ( value.startsWith( "*" ) || value.endsWith( "*" ) ) )
             {
                 String pid = of.getPid().toLowerCase();
                 log.debug( String.format( "Matching pid: '%s'", pid ) );
 
                 switch ( target )
                 {
                 case PID:
                     log.debug( String.format( "PID Matching '%s' and '%s'", pid, value ) );
                     if ( ! pid.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case STATE:
                     String state = of.getState().toLowerCase();
                     log.debug( String.format( "STATE Matching '%s' and '%s'", state, value ) );
                     if ( ! state.equals( value) )
                     {
                         return false;
                     }
                     break;
                 case OWNERID:
                     String ownerId = of.getOwnerId().toLowerCase();
                     log.debug( String.format( "OWNERID Matching '%s' and '%s'", ownerId, value ) );
                     if ( ! ownerId.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case CDATE:
                     String cDate = of.getCDate().toLowerCase();
                     log.debug( String.format( "CDATE Matching '%s' and '%s'", cDate, value ) );
                     if ( ! cDate.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case MDATE:
                     String mDate = of.getMDate().toLowerCase();
                     log.debug( String.format( "MDATE Matching '%s' and '%s'", mDate, value ) );
                     if ( ! mDate.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case TITLE:
                     String title = of.getTitle()[0].toLowerCase();
                     log.debug( String.format( "TITLE Matching '%s' and '%s'", title, value ) );
                     if ( ! title.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case CREATOR:
                     String creator = of.getCreator()[0].toLowerCase();
                     log.debug( String.format( "CREATOR Matching '%s' and '%s'", creator, value ) );
                     if ( ! creator.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case SUBJECT:
                     String subject = of.getSubject()[0].toLowerCase();
                     log.debug( String.format( "SUBJECT Matching '%s' and '%s'", subject, value ) );
                     if ( ! subject.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case DESCRIPTION:
                     String description = of.getDescription()[0].toLowerCase();
                     log.debug( String.format( "DESCRIPTION Matching '%s' and '%s'", description, value ) );
                     if ( ! description.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case PUBLISHER:
                     String publisher = of.getPublisher()[0].toLowerCase();
                     log.debug( String.format( "PUBLISHER Matching '%s' and '%s'", publisher, value ) );
                     if ( ! publisher.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case CONTRIBUTOR:
                     String contributor = of.getContributor()[0].toLowerCase();
                     log.debug( String.format( "CONTRIBUTOR Matching '%s' and '%s'", contributor, value ) );
                     if ( ! contributor.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case DATE:
                     String date = of.getDate()[0].toLowerCase();
                     log.debug( String.format( "DATE Matching '%s' and '%s'", date, value ) );
                     if ( ! date.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case TYPE:
                     String type = of.getType()[0].toLowerCase();
                     log.debug( String.format( "TYPE Matching '%s' and '%s'", type, value ) );
                     if ( ! type.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case FORMAT:
                     String format = of.getFormat()[0].toLowerCase();
                     log.debug( String.format( "FORMAT Matching '%s' and '%s'", format, value ) );
                     if ( ! format.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case IDENTIFIER:
                     String identifier = of.getIdentifier()[0].toLowerCase();
                     log.debug( String.format( "IDENTIFIER Matching '%s' and '%s'", identifier, value ) );
                     if ( ! identifier.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case SOURCE:
                     String source = of.getSource()[0].toLowerCase();
                     log.debug( String.format( "SOURCE Matching '%s' and '%s'", source, value ) );
                     if ( ! source.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case LANGUAGE:
                     String language = of.getLanguage()[0].toLowerCase();
                     log.debug( String.format( "LANGUAGE Matching '%s' and '%s'", language, value ) );
                     if ( ! language.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case RELATION:
                     String relation = of.getRelation()[0].toLowerCase();
                     log.debug( String.format( "RELATION Matching '%s' and '%s'", relation, value ) );
                     if ( ! relation.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case COVERAGE:
                     String coverage = of.getCoverage()[0].toLowerCase();
                     log.debug( String.format( "COVERAGE Matching '%s' and '%s'", coverage, value ) );
                     if ( ! coverage.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case RIGHTS:
                     String rights = of.getRights()[0].toLowerCase();
                     log.debug( String.format( "RIGHTS Matching '%s' and '%s'", rights, value ) );
                     if ( ! rights.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 case DCMDATE:
                     String dcmDate = of.getDcmDate().toLowerCase();
                     log.debug( String.format( "DCMDATE Matching '%s' and '%s'", dcmDate, value ) );
                     if ( ! dcmDate.equals( value ) )
                     {
                         return false;
                     }
                     break;
                 default:
 
                 }
             }//end if check for '*' in value start or end
         }
 
         log.debug( String.format( "RET Matching returning: '%s'", ret ) );
         return ret;
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
      * @return An array of ObjectFields.
      */
     ObjectFields[] searchRepository( String[] resultFields, List< Pair< TargetFields, String > > propertiesAndVaulues, String comparisonOperator, int maximumResults, String namespace )
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
             Pair< TargetFields, String > pair = propertiesAndVaulues.get( i );
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
 
         // A list to contain arrays of ObjectFields.
         // Whenever a new array of ObjectFields are found, either from findObjects or
         // resumeFindObjects, it is added to the list.
         // The Arrays are later collected into a single array.
         LinkedList< ObjectFields[] > objFieldsList = new LinkedList< ObjectFields[] >();
         int numberOfResults = 0;
         try
         {
             NonNegativeInteger maxResults = new NonNegativeInteger( Integer.toString( maximumResults ) );
             fsr = this.fedoraHandle.findObjects( resultFields, maxResults, fsq );
 
             numberOfResults += fsr.getResultList().length;
             objFieldsList.push( fsr.getResultList() );
 
             ListSession listSession = fsr.getListSession();
             while ( listSession != null )
             {
                 String token = listSession.getToken();
                 fsr = this.fedoraHandle.resumeFindObjects( token );
                 if ( fsr != null )
                 {
                     numberOfResults += fsr.getResultList().length;
                     objFieldsList.push( fsr.getResultList() );
                     listSession = fsr.getListSession();
                 }
                 else
                 {
                     listSession = null;
                 }
             }
 
             log.debug( String.format( "Result length of resultlist: %s", numberOfResults ) );
 
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
 
         ObjectFields[] objectFields = new ObjectFields[ numberOfResults ];
 
         // Collecting ObjectFields arrays into a single array:
         int destPos = 0; // destination position
         for ( ObjectFields[] of : objFieldsList )
         {
             System.arraycopy( of, 0, objectFields, destPos, of.length );
             destPos += of.length;
         }
 
         return objectFields;
     }
 
 
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
             for( Pair<Integer, Pair<String, CargoObject>> pairs : adminStream.getStreams() )
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
 
                     cargo.add( datastreamtype, co.getFormat(), co.getSubmitter(), co.getLang(), co.getMimeType(), data );
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
             for( Pair<Integer, Pair<String, CargoObject>> pairs : adminStream.getStreams() )
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
                     cargo.add( co.getDataStreamType(), co.getFormat(), co.getSubmitter(), co.getLang(), co.getMimeType(),  data );
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
 
 
     private AdministrationStream constructAdministrationStream( CargoContainer cargo ) throws ObjectRepositoryException
     {
 
         AdministrationStream adminStream = new AdministrationStream();
 
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
             //System.out.println( "object id: " + objectIdentifier );
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
         String relationString = relation.getPredicateString();
         this.addUncheckedObjectRelation( objectIdentifier, relationString, subject );
     }
 
     @Override
     public void addUncheckedObjectRelation( ObjectIdentifier objectIdentifier, String relationString, String subject ) throws ObjectRepositoryException
     {
         try
         {
             log.info( String.format( "trying to add %s - %s -> %s", objectIdentifier.getIdentifier(), relationString, subject ) );
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
    public List< Pair< IPredicate, String > > getObjectRelations( ObjectIdentifier objectIdentifier ) throws ObjectRepositoryException
    {
        return null;
    }


     public List< Pair< IPredicate, String > > getObjectRelations( String subject, String predicate ) throws ObjectRepositoryException
     {
         try
         {
             log.debug( String.format( "calling with subject '%s' and predicate '%s'", subject, predicate ) );
             RelationshipTuple[] tuple = this.fedoraHandle.getRelationships( subject, predicate );
             if ( tuple != null )
             {
                 List< Pair< IPredicate, String > > ret = new ArrayList< Pair< IPredicate, String> >();
                 for ( RelationshipTuple relationship : tuple )
                 {
                     String object = relationship.getObject();
                     Pair< IPredicate, String > pair = new Pair( predicate, object );
                     ret.add( pair );
                 }
 
                 return ret;
             }
         }
         catch ( ConfigurationException ce )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, ce );
             throw new ObjectRepositoryException( error, ce );
         }
         catch( ServiceException se )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, se );
             throw new ObjectRepositoryException( error, se );
         }
         catch( MalformedURLException mue )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, mue );
             throw new ObjectRepositoryException( error, mue );
         }
         catch( IOException ioe )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, ioe );
             throw new ObjectRepositoryException( error, ioe );
         }
 
         log.debug( "returning null" );
         return null;
     }
 
 
     @Override
     public void removeObjectRelation( ObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException
     {
         try
         {
             String relationString = relation.getPredicateString();
             String pid = objectIdentifier.getIdentifier();
 
             log.debug( String.format( "trying to remove object %s with relation %s from pid %s", subject, relationString, pid ) );
             boolean purgeRelationship = this.fedoraHandle.purgeRelationship( pid, relationString, subject, true, null );
             if ( purgeRelationship )
             {
                 log.debug("purged");
                 log.info( String.format( "Ignored error from purgeRelationeship : on %s-%s->%s", pid, relationString, subject ) );
             }
             else
             {
                 log.debug("not purged");
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
