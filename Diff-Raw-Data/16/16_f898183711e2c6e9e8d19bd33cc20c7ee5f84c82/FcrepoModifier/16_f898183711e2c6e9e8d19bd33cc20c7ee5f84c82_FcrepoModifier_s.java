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
  * \file FedoraHandle.java
  * \brief
  */
 package dk.dbc.opensearch.fedora;
 
 import dk.dbc.commons.types.Pair;
 import dk.dbc.opensearch.metadata.AdministrationStream;
 import dk.dbc.opensearch.metadata.IMetaData;
 import dk.dbc.opensearch.metadata.IPredicate;
 import dk.dbc.opensearch.types.CargoContainer;
 import dk.dbc.opensearch.types.CargoObject;
 import dk.dbc.opensearch.types.IObjectIdentifier;
 import org.fcrepo.client.FedoraClient;
 import org.fcrepo.server.management.FedoraAPIM;
 import org.fcrepo.server.types.gen.Datastream;
 import org.fcrepo.server.types.gen.RelationshipTuple;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.transform.TransformerException;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.log4j.Logger;
 import org.fcrepo.common.Constants;
 import org.xml.sax.SAXException;
 
 /**
  * @author thp
  */
 public class FcrepoModifier
 {
     private static final Logger log = Logger.getLogger( FcrepoModifier.class );
     private static final String DeletedState = "D";
     private final FedoraAPIM fem;
     private final FedoraClient fc;
     private final String fedora_base_url;
 
 
    FcrepoModifier( String host, String port, String user, String pass ) throws ObjectRepositoryException
     {
         this.fedora_base_url = String.format( "http://%s:%s/fedora", host, port );
         log.debug( String.format( "connecting to fedora base using %s, user=%s, pass=%s", fedora_base_url, user, pass ) );
         try
         {
             fc = new FedoraClient( fedora_base_url, user, pass );
         }
         catch( MalformedURLException e )
         {
             String error = String.format( "Failed to obtain connection to fedora repository: %s", e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
 
         try
         {
             fem = fc.getAPIM();
         }
         catch( ServiceException e )
         {
             String error = String.format( "Failed to obtain connection to fedora repository: %s", e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
         catch( IOException e )
         {
             String error = String.format( "Failed to obtain connection to fedora repository: %s", e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     private FedoraAPIM getAPIM()
     {
         log.trace( "FcrepoModifier getAPIM" );
         return fem;
     }
 
 
     private FedoraClient getFC()
     {
         log.trace( "FcrepoModifier getFC()" );
         return fc;
     }
 
 
     private String ingest( byte[] data, String datatype, String logmessage ) throws RemoteException
     {
         long timer = System.currentTimeMillis();
 
         String pid = this.getAPIM().ingest( data, datatype, logmessage );
 
         timer = System.currentTimeMillis() - timer;
         log.info( String.format( "HANDLE Timing: ( %s f) %s", this.getClass(), timer ) );
 
         return pid;
     }
 
 
     private String uploadFile( File fileToUpload ) throws IOException
     {
         long timer = System.currentTimeMillis();
 
         String msg = this.getFC().uploadFile( fileToUpload );
 
         timer = System.currentTimeMillis() - timer;
         log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
         return msg;
     }
 
 
     public Datastream getDatastream( String objectIdentifier, String dataStreamID ) throws ObjectRepositoryException
     {
         try
         {
             long timer = System.currentTimeMillis();
 
             Datastream ds = getAPIM().getDatastream( objectIdentifier, dataStreamID, null );
 
             timer = System.currentTimeMillis() - timer;
             log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
             if( ds == null )
             {
                 String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': Got nothing back from the object repository", dataStreamID, objectIdentifier );
                 log.error( error );
                 throw new ObjectRepositoryException( error );
             }
             return ds;
         }
         catch( RemoteException e )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s", dataStreamID, objectIdentifier, e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     private String[] getNextPID( int numberOfPids, String prefix ) throws RemoteException
     {
         long timer = System.currentTimeMillis();
 
         String[] pidlist = this.getAPIM().getNextPID( new NonNegativeInteger( Integer.toString( numberOfPids ) ), prefix );
 
         timer = System.currentTimeMillis() - timer;
         log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
         if( pidlist == null )
         {
             log.error( "Could not retrieve pids from Fedora repository" );
             throw new IllegalStateException( "Could not retrieve pids from Fedora repository" );
         }
 
         return pidlist;
     }
 
 
     /**
      *  Adds
      *
      *
      * @param objectIdentifier
      * @param relation
      * @param subject
      * @throws ObjectRepositoryException
      */
     public boolean addObjectRelation( String objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException
     {
         String relationString = relation.getPredicateString();
         return this.addUncheckedObjectRelation( objectIdentifier, relationString, subject );
     }
 
 
     /**
      * @param objectIdentifier
      * @param relation
      * @param subject
      * @throws ObjectRepositoryException
      */
     public boolean addUncheckedObjectRelation( String objectIdentifier, String relationString, String subject ) throws ObjectRepositoryException
     {
         try
         {
             log.info( String.format( "trying to add %s - %s -> %s", objectIdentifier, relationString, subject ) );
             long timer = System.currentTimeMillis();
 
             if( objectIdentifier.equals( subject ) )
             {
                 log.warn( String.format( "We do not allow for a relation=[%s] to have identical subject=[%s] and object=[%s]", 
                         relationString, objectIdentifier, subject ) );
                 return false;
             }
 
             boolean ret = this.getAPIM().addRelationship( objectIdentifier, relationString, subject, true, null );
 
             timer = System.currentTimeMillis() - timer;
             log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
             return ret;
         }
         catch( RemoteException e )
         {
             String error = "Failed to add Relation to fedora Object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     public void removeObjectRelation( IObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException
     {
         removeObjectRelation( objectIdentifier.getIdentifier(), relation.getPredicateString(), subject );
     }
 
 
     void removeObjectRelation( String objectIdentifier, String relation, String subject ) throws ObjectRepositoryException
     {
         String pid = objectIdentifier;
 
         log.debug( String.format( "trying to remove object %s with relation %s from pid %s", subject, relation, pid ) );
         try
         {
             long timer = System.currentTimeMillis();
 
             boolean purgeRelationship = this.getAPIM().purgeRelationship( pid, relation, subject, true, null );
 
             timer = System.currentTimeMillis() - timer;
             log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
             if( purgeRelationship )
             {
                 log.debug( "purged" );
                 log.info( String.format( "Ignored error from purgeRelationeship : on %s-%s->%s", pid, relation, subject ) );
             }
             else
             {
                 log.debug( "not purged" );
             }
 
         }
         catch( RemoteException e )
         {
             String error = String.format( "RemoteException Could not delete object referenced by pid '%s': %s", pid, e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     /**
      * This method finds the targets of the relations identified by the predicate argument for the object
      * identified by the subject argument.
      *
      * @param subject {@link String} identifying the object to get relations for.
      * @param predicate {@link String} telling what relation to get targets for.
      * @return a {@link List} of {@link Pair}s of {@link IPredicate} and {@link String}.
      * In each {@link Pair} is the {@link IPredicate} representing the relation and the {@link String} the object.
      *
      * \Todo: Why do we return the IPredicate telleing what relation we found, when we in the predicate param
      * specifies what relation type to search for?
      */
     public List<Pair<IPredicate, String>> getObjectRelations( String subject, String predicate ) throws ObjectRepositoryException
     {
         try
         {
             log.debug( String.format( "calling with subject '%s' and predicate '%s'", subject, predicate ) );
 
             RelationshipTuple[] tuple = this.getAPIM().getRelationships( subject, predicate );
             log.debug( String.format( "calling with subject '%s' and predicate '%s'", subject, predicate ) );
             long timer = System.currentTimeMillis();
 
             timer = System.currentTimeMillis() - timer;
             log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
             if( tuple != null )
             {
                 List<Pair<IPredicate, String>> ret = new ArrayList<Pair<IPredicate, String>>();
                 for( RelationshipTuple relationship : tuple )
                 {
                     String object = relationship.getObject();
                     Pair<IPredicate, String> pair = new Pair( predicate, object ); //we put the predicate param into the return value, why?
                     ret.add( pair );
                 }
 
                 return ret;
             }
         }
         catch( RemoteException e )
         {
             String error = "Failed to get relation from fedora object";
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
 
         log.debug( "returning null" );
         return null;
     }
 
 
     String[] purgeDatastream( String pid, String sID, String startDate, String endDate, String logm, boolean breakDependencies ) throws RemoteException
     {
         long timer = System.currentTimeMillis();
 
         String[] rt = this.getAPIM().purgeDatastream( pid, sID, startDate, endDate, logm, breakDependencies );
 
         timer = System.currentTimeMillis() - timer;
         log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
         return rt;
     }
 
 
     /**
      * Purges the object identified by {@code identifier} from the
      * repository.
      *
      * @param identifier identifying the object in the repository
      * @param logmessage describing the reasons for the purging
      *
      * @throws ObjectRepositoryException if the object cannot be purged
      */
     public String purgeObject( String identifier, String logmessage ) throws ObjectRepositoryException
     {
         long timer = System.currentTimeMillis();
 
         try
         {
             String timestamp = this.getAPIM().purgeObject( identifier, logmessage, false );
             if( timestamp == null )
             {
                 String error = String.format( "Could not delete object reference by pid %s (timestamp == null)", identifier );
                 log.error( error );
                 throw new ObjectRepositoryException( error );
             }
             timer = System.currentTimeMillis() - timer;
             log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
             return timestamp;
         }
         catch( RemoteException e )
         {
             String error = String.format( "RemoteException Could not delete object referenced by pid '%s': %s", identifier, e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     /**
      * Stores data in {@code cargo} in the fedora repository
      *
      * @param cargo the data to store
      * @param logmessage message to log the operation with
      * @return the objectIdentifier of the stored object
      * @throws IllegalStateException if the cargo is empty
      * @throws ObjectRepositoryException if the cargo could not be transformed
      * into foxml or the foxml could not be stored
      */
     public String storeObject( CargoContainer cargo, String logmessage, String defaultPidNamespace ) throws ObjectRepositoryException
     {
         if( cargo.getCargoObjectCount() == 0 )
         {
             log.error( String.format( "No data in CargoContainer, refusing to store nothing" ) );
             throw new IllegalStateException( String.format( "No data in CargoContainer, refusing to store nothing" ) );
         }
 
         IObjectIdentifier identifier = getOrGenerateIdentifier( cargo, defaultPidNamespace );
         cargo.setIdentifier( identifier );
         String identifierAsString = identifier.getIdentifier();
 
         cargo.addMetaData( constructAdministrationStream( cargo ) );
 
         byte[] foxml;
         try
         {
             foxml = FcrepoUtils.CargoContainerToFoxml( cargo );
 
         }
         catch( XMLStreamException e )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
         catch( TransformerException e )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
         catch( SAXException e )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
         catch( IOException e )
         {
             String error = String.format( "Failed in serializing CargoContainer with id '%s': %s", identifierAsString, e.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, e );
         }
 
         String returnedobjectIdentifier = null;
         try
         {
             returnedobjectIdentifier = ingest( foxml, Constants.FOXML1_1.uri, logmessage );
         }
         catch( RemoteException e )
         {
             String error = String.format( "Failed to ingest object with pid '%s' into repository: %s. Foxml: %s", identifierAsString, e.getMessage(), new String( foxml ) );
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
 
         if( returnedobjectIdentifier.equals( "" ) )
         {
             log.info( String.format( "For empty identifier, we recieved '%s' from the ingest", returnedobjectIdentifier ) );
         }
         else if( !returnedobjectIdentifier.equals( identifierAsString ) )
         {
             log.warn( String.format( "I expected pid '%s' to be returned from the repository, but got '%s'", identifierAsString, returnedobjectIdentifier ) );
         }
 
         return returnedobjectIdentifier;
     }
 
 
     public void deleteObject( String objectIdentifier, String logMessage ) throws ObjectRepositoryException
     {
         try
         {
             modifyObject( objectIdentifier, DeletedState, null, null, logMessage );
             log.debug( String.format( "marked object '%s' as deleted", objectIdentifier ) );
         }
         catch( RemoteException e )
         {
             String error = String.format( "RemoteException Error Connecting to fedora: %s", e.getMessage() );
             log.error( error, e );
             throw new ObjectRepositoryException( error, e );
         }
     }
 
 
     /**
      * Wrapper function to FedoraAPIM.
      * No magic here.
      * <p>
      * The functions primary functionality is to modify the {@code state} of an object, but it can also be 
      * used to modify the {@code label} or the {@code ownerId}. If you intende to modify an then please notice,
      * that it is unknown what happens if you change the {@code label} or the {@code ownerId}.
      * The reason it is unknown is because the internal datastreams such as adminStream probably does
      * not change its submitter or format through this function.
      * </p>
      * @param pid The identifier for the object to modify.
      * @param state The state you wish to change into. The legal states are: {@code A}, {@code I} or {@code D}. Specify null to leave existing value unchanged
      * @param label The label you wish to change into. Commonly this is the format. Specify null to leave existing value unchanged
      * @param ownerId The ownerid you wish to change into. commonly this is the submitter. Specify null to leave existing value unchanged
      * @param logMessage Any message you want to pass on to fedoras log.
      *
      * @return Server-date of modification as a {@link String}.
      *
      * @throws RemoteException if an error of any kind occurs. This is probably both communication errors and malformed data errors.
      */
     private String modifyObject( String pid, String state, String label, String ownerId, String logMessage ) throws RemoteException
     {
         long timer = System.currentTimeMillis();
 
         String date = this.getAPIM().modifyObject( pid, state, label, ownerId, logMessage );
 
         timer = System.currentTimeMillis() - timer;
         log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
         return date;
     }
 
 
     /**
      * Internal helper to store
      *
      * @param cargo
      * @return
      * @throws ObjectRepositoryException
      */
     private IObjectIdentifier getOrGenerateIdentifier( CargoContainer cargo, String defaultPidNameSpace ) throws ObjectRepositoryException
     {
         IObjectIdentifier identifier = cargo.getIdentifier();
 
         if( identifier != null )
         {
             return identifier;
         }
 
         if( defaultPidNameSpace == null )
         {
             defaultPidNameSpace = new String( "auto" );
         }
 
         String newPid = "";
         try
         {
             newPid = getNextPID( 1, defaultPidNameSpace )[0];
         }
         catch( RemoteException e )
         {
             throw new ObjectRepositoryException( String.format( "Unable to get next pid for Prefix ", defaultPidNameSpace ), e );
         }
 
         return new PID( newPid );
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
         for( IMetaData meta : cargo.getMetaData() )
         {
             streamtypecounter = adminStream.getCount( meta.getType() );
             String id = meta.getType().getName() + "." + Integer.toString( streamtypecounter );
             log.debug( String.format( "Adding stream '%s' with id '%s' to adminstream", meta.getType().getName(), id ) );
             adminStream.addStream( meta, id );
         }
         return adminStream;
     }
 }
