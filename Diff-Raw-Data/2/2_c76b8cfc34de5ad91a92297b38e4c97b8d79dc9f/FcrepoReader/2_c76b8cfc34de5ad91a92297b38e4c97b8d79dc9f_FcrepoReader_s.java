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
 import dk.dbc.opensearch.types.CargoContainer;
 import dk.dbc.opensearch.types.CargoObject;
 import dk.dbc.opensearch.types.DataStreamType;
 import dk.dbc.opensearch.types.ITargetField;
 import java.io.ByteArrayInputStream;
 import org.fcrepo.client.FedoraClient;
 import org.fcrepo.server.access.FedoraAPIA;
 import org.fcrepo.server.types.gen.DatastreamDef;
 import org.fcrepo.server.types.gen.FieldSearchQuery;
 import org.fcrepo.server.types.gen.FieldSearchResult;
 import org.fcrepo.server.types.gen.MIMETypedStream;
 
 import java.net.MalformedURLException;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.fcrepo.server.types.gen.Condition;
 
 import org.fcrepo.server.types.gen.ComparisonOperator;
 import org.fcrepo.server.types.gen.ListSession;
 import org.fcrepo.server.types.gen.ObjectFields;
 import org.xml.sax.SAXException;
 
 public class FcrepoReader
 {
     private final static Logger log = LoggerFactory.getLogger( FcrepoReader.class );
     private final FedoraAPIA fea;
     private final String fedora_base_url;
 
 
     public FcrepoReader( String host, String port ) throws ObjectRepositoryException
     {
         this.fedora_base_url = String.format( "http://%s:%s/fedora", host, port );
         log.debug( String.format( "connecting to fedora base using %s", fedora_base_url ) );
         FedoraClient fc;
         try
         {
            fc = new FedoraClient( fedora_base_url, "", "" );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Failed to obtain connection to fedora repository: %s", ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         try
         {
             fea = fc.getAPIA();
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Failed to obtain connection to fedora repository: %s", ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to obtain connection to fedora repository: %s", ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
     }
 
 
     private FedoraAPIA getAPIA()
     {
         log.trace( "FcrepoReader getAPIA" );
         return fea;
     }
 
 
     /**
      * Queries the object repository for the existence of the object
      * based on the identifier of the object
      *
      * @param objectIdentifier identifies the object in the scope of
      * the object repository
      *
      * @return true if the object exists in the repository, false otherwise
      */
     public boolean hasObject( String identifier )
     {
         long timer = System.currentTimeMillis();
 
         boolean retValue = true;
         try
         {
             DatastreamDef[] d = this.fea.listDatastreams( identifier, null );
             if( d == null )
             {
                 log.info( String.format( "Could not list datastreams for object %s. We take this as an indication that the object doesn't exist", identifier ) );
                 retValue = false;
             }
             else
             {
                 log.debug( String.format( "length of DatastreamDef: '%s'", d.length ) );
             }
         }
         catch( RemoteException e )
         {
             log.info( String.format( "Could not list datastreams for object %s. We take this as an indication that the object doesn't exist", identifier ) );
             retValue = false;
         }
 
         timer = System.currentTimeMillis() - timer;
         log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
 
         return retValue;
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
     public CargoContainer getObject( String identifier ) throws ObjectRepositoryException
     {
         byte[] adminbytes = getDatastreamDissemination( identifier, DataStreamType.AdminData.getName() );
 
         if( adminbytes == null || adminbytes.length < 1 )
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
 
         List<Pair<Integer, Pair<String, CargoObject>>> adminstreamlist;
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
 
         for( Pair<Integer, Pair<String, CargoObject>> cargoobjects : adminstreamlist )
         {
             String streamId = cargoobjects.getSecond().getFirst();
 
             if( streamId.equals( DataStreamType.DublinCoreData.getName() + ".0" ) )
             {
                 streamId = "DC";
             }
             log.debug( String.format( "id: %s, streamId: %s", identifier, streamId ) );
 
             byte[] datastream;
             datastream = getDatastreamDissemination( identifier, streamId );
 
             CargoObject co = cargoobjects.getSecond().getSecond();
 
             try
             {
                 cargo.add( co.getDataStreamType(), co.getFormat(), co.getSubmitter(), co.getLang(), co.getMimeType(), datastream );
             }
             catch( IOException ex )
             {
                 String error = String.format( "Failed to add data with administrationstream id '%s' to CargoContainer: %s", streamId, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
         }
 
         if( cargo.getCargoObjectCount() < 1 && adminStream.getCount() > 0 )
         {
             throw new ObjectRepositoryException( "CargoContainer is empty, even though adminstream says it gave data" );
         }
         return cargo;
     }
 
 
     /***
      * Searches repository with conditions specified by propertiesAndValues using comparisonOperator, e.g. 'has', 'eq'.
      * The parameter 'namespace' if not null is used to limit the result set on pid containing namespace. Beware that
      * the comparison operator in this case cannot be 'eq'.
      *
      * @param resultFields
      * @param conditions
      * @param maximumResults
      *
      * @return An array of ObjectFields.
      */
     ObjectFields[] searchRepository( String[] resultFields, List<OpenSearchCondition> conditions, int maximumResults )
     {
         // We will convert from OpenSearchCondition to Condition in two steps:
         // 1) converting operators and test validity of search-value
         // 2) put valid conditions into a Condition[].
         //
 
         // Convert operators and test validity of search values
         List<Condition> validConditions = new ArrayList<Condition>( conditions.size() );
 
         for( OpenSearchCondition condition : conditions )
         {
             ITargetField field = (ITargetField) condition.getField();
             String value = condition.getValue();
 
             // Set the fedora ComparisonOperator:
             // Notice: ComparisonOperator from Fedora is not an enum, and as such has no function valueOf().
             //         It do, however, has a function fromString() which seems to do the same - hopefully.
             String compStr = "eq";
             switch( condition.getOperator() )
             {
                 case EQUALS:
                     compStr = "eq";
                     break;
                 case CONTAINS:
                     compStr = "has";
                     break;
                 case GREATER_THAN:
                     compStr = "gt";
                     break;
                 case GREATER_OR_EQUAL:
                     compStr = "ge";
                     break;
                 case LESS_THAN:
                     compStr = "lt";
                     break;
                 case LESS_OR_EQUAL:
                     compStr = "le";
                     break;
                 default:
                     compStr = "eq";
             }
 
             ComparisonOperator comp = ComparisonOperator.fromString( compStr );
             log.info( String.format( "Setting fedora-condition: field[%s] comp[%s] value[%s]", field.fieldname(), comp.toString(), value ) );
 
             if( value.isEmpty() )
             {
                 log.warn( "Ignoring condition: We do not allow searches with empty search values." );
                 continue;
             }
 
             validConditions.add( new Condition( field.fieldname(), comp, value ) );
         }
 
         // If there are no valid conditions, there is no need to perform the search:
         if( validConditions.size() == 0 )
         {
             // \todo: I do not like to return in a middle of a function!
             return new ObjectFields[0];
         }
 
         // Convert validConditions-list into a Condition-array:
         Condition[] cond = new Condition[validConditions.size()];
         int i = 0;
         for( Condition condition : validConditions )
         {
             cond[i++] = condition;
         }
 
         // Create query end result:
         FieldSearchQuery fsq = new FieldSearchQuery( cond, null );
         FieldSearchResult fsr = null;
 
         // A list to contain arrays of ObjectFields.
         // Whenever a new array of ObjectFields are found, either from findObjects or
         // resumeFindObjects, it is added to the list.
         // The Arrays are later collected into a single array.
         LinkedList<ObjectFields[]> objFieldsList = new LinkedList<ObjectFields[]>();
         int numberOfResults = 0;
         try
         {
             NonNegativeInteger maxResults = new NonNegativeInteger( Integer.toString( maximumResults ) );
             fsr = this.getAPIA().findObjects( resultFields, maxResults, fsq );
 
             numberOfResults += fsr.getResultList().length;
             objFieldsList.push( fsr.getResultList() );
 
             ListSession listSession = fsr.getListSession();
             while( listSession != null )
             {
                 String token = listSession.getToken();
                 fsr = this.getAPIA().resumeFindObjects( token );
                 if( fsr != null )
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
         catch( IOException ex )
         {
             String warn = String.format( "IOException -> Could not conduct query: %s", ex.getMessage() );
             log.warn( warn );
         }
 
         if( fsr == null )
         {
             log.warn( "Retrieved no hits from search, returning empty List<String>" );
             return new ObjectFields[]
                     {
                     };
         }
 
         ObjectFields[] objectFields = new ObjectFields[numberOfResults];
 
         // Collecting ObjectFields arrays into a single array:
         int destPos = 0; // destination position
         for( ObjectFields[] of : objFieldsList )
         {
             System.arraycopy( of, 0, objectFields, destPos, of.length );
             destPos += of.length;
         }
 
         return objectFields;
     }
 
 
     /**
      * Retrieves all identifiers matching {@code conditions} from objects
      * having one of the specified {@code states}.
      *
      * Specifying state in both {@code conditions} and {@code states} will
      * result only in identifiers matching both criteria.
      *
      *
      * @param conditions A {@link List} of {@link OpenSearchCondition}s
      * @param maximumResults the largest number of results to retrieve
      * @param states A {@link Set} of object states {@link String} for which to
      * include object identifiers
      *
      * @return A {@link List} containing all matching identifiers.
      *
      * @throws IllegalArgumentException if {@code maximumResults} is
      * less than one, since neither a negative number of results or
      * zero results makes any sense.
      */
     public List<String> getIdentifiersByState( List<OpenSearchCondition> conditions, int maximumResults, Set<String> states ) throws IllegalArgumentException
     {
         if( maximumResults < 1 )
         {
             throw new IllegalArgumentException( String.format( "The maximum number of results retrived cannot be null or less than one. Argument given: %s", maximumResults ) );
         }
 
         String[] resultFields =
         {
             "pid", "state"
         }; // we will only return pids!
         // \todo: Perhaps NonNegativeInteger should be replaced with PositiveInteger,
         // since this function disallows values < 1.
         NonNegativeInteger maxRes = new NonNegativeInteger( Integer.toString( maximumResults ) );
 
         ObjectFields[] objectFields = searchRepository( resultFields, conditions, maximumResults );
 
         int ofLength = objectFields.length;
         List<String> pids = new ArrayList<String>( ofLength );
         for( int j = 0; j < ofLength; j++ )
         {
             String pidValue = objectFields[j].getPid();
             // Only add pid to result if object is in an accepted state.
             String objectState = objectFields[j].getState();
             if( objectState != null && states.contains( objectState ) )
             {
                 pids.add( pidValue );
             }
         }
 
         return pids;
     }
 
 
     public String[] listDatastreamIds( String pid ) throws ObjectRepositoryException
     {
         try
         {
             DatastreamDef[] streamDefs = this.getAPIA().listDatastreams( pid, null );
             String ids[] = new String[streamDefs.length];
             for (int i = 0 ; i < streamDefs.length ; i++)
             {
                 ids[i] = streamDefs[i].getID();
             }
             return ids;
         }
         catch( RemoteException ex )
         {
             String error = String.format( "Failed to list datastreams from object with objectIdentifier '%s': %s", pid, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
     }
 
     public byte[] getDatastreamDissemination( String pid, String datastreamId ) throws ObjectRepositoryException
     {
         try
         {
             long timer = System.currentTimeMillis();
             MIMETypedStream ds = this.getAPIA().getDatastreamDissemination( pid, datastreamId, null );
             timer = System.currentTimeMillis() - timer;
             log.info( String.format( "HANDLE Timing: ( %s ) %s", this.getClass(), timer ) );
             return ds.getStream();
         }
         catch( RemoteException ex )
         {
             String error = String.format( "Failed to retrieve datastream with name '%s' from object with objectIdentifier '%s': %s",
                     datastreamId, pid, ex.getMessage() );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
     }
 }
