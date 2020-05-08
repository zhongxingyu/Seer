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
 
 
 package dk.dbc.opensearch.common.fedora;
 
 
 import dk.dbc.opensearch.common.config.FedoraConfig;
 
 import fedora.client.FedoraClient;
 import fedora.server.access.FedoraAPIA;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.types.gen.Datastream;
 import fedora.server.types.gen.DatastreamDef;
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.types.gen.MIMETypedStream;
 import fedora.server.types.gen.RelationshipTuple;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.Map;
 
 import javax.xml.rpc.ServiceException;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.trippi.TupleIterator;
 
 
 public class FedoraHandle
 {
     private static Logger log = Logger.getLogger( FedoraHandle.class );
 
 
     private FedoraAPIM fem;
     private FedoraAPIA fea;
     private FedoraClient fc;
 
     private String fedora_base_url;
 
 
     FedoraHandle() throws ObjectRepositoryException
     {
         String host;
         String port;
         String user;
         String pass;
 
         log.debug( "FedoraHandle constructor" );
 
         try
         {
             host = FedoraConfig.getHost();
             port = FedoraConfig.getPort();
             user = FedoraConfig.getUser();
             pass = FedoraConfig.getPassPhrase();
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Failed to obtain configuration values for FedoraHandle");
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         this.fedora_base_url = String.format( "http://%s:%s/fedora", host, port );
         log.debug( String.format( "connecting to fedora base using %s, user=%s, pass=%s", fedora_base_url, user, pass ) );
         try
         {
             fc = new FedoraClient( fedora_base_url, user, pass );
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
             fem = fc.getAPIM();
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
 
     private FedoraAPIA getAPIA() throws ServiceException
     {
         log.trace( "FedoraHandle getAPIA" );
         return fea;
     }
 
 
     private FedoraAPIM getAPIM()
     {
         log.trace( "FedoraHandle getAPIM" );
         return fem;
     }
 
 
     private FedoraClient getFC()
     {
         log.trace( "FedoraHandle getFC()" );
         return fc;
     }
 
 
     String ingest( byte[] data, String datatype, String logmessage ) throws ConfigurationException, ServiceException, ServiceException, IOException
     {
         long timer = 0;
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         String pid = this.getAPIM().ingest( data, datatype, logmessage );
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s f) %s", this.getClass(), timer ) );
         }
 
         return pid;
     }
 
 
     String uploadFile( File fileToUpload ) throws IOException
     {
         long timer = 0;
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         String msg = this.getFC().uploadFile( fileToUpload );
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return msg;
     }
 
 
     String modifyDatastreamByReference( String pid, String datastreamID, String[] alternativeDsIds, String dsLabel, String MIMEType, String formatURI, String dsLocation, String checksumType, String checksum, String logMessage, boolean force ) throws RemoteException
     {
         long timer = 0;
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         String timestamp = this.getAPIM().modifyDatastreamByReference( pid, datastreamID, alternativeDsIds, dsLabel, MIMEType, formatURI, dsLocation, checksumType, checksum, logMessage, force);
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return timestamp;
     }
 
     String addDatastream( String pid, String datastreamID, String[] alternativeDsIds, String dsLabel, boolean versionable, String MIMEType, String formatURI, String dsLocation, String controlGroup, String datastreamState, String checksumType, String checksum, String logmessage ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         long timer = 0;
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         String returnedSID = this.getAPIM().addDatastream( pid, datastreamID, alternativeDsIds, dsLabel, versionable, MIMEType, formatURI, dsLocation, controlGroup, datastreamState, checksumType, checksum, logmessage );
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return returnedSID;
     }
 
 
     byte[] getDatastreamDissemination( String pid, String datastreamId, String asOfDateTime ) throws ConfigurationException, MalformedURLException, IOException, ServiceException
     {
         MIMETypedStream ds = null;
         long timer = 0;
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         ds = this.getAPIA().getDatastreamDissemination( pid, datastreamId, asOfDateTime );
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return ds.getStream();
     }
 
 
     String getDataStreamsXML( String pid ) throws IOException {
 
         long timer = 0;
         if( log.isDebugEnabled() )  {
             timer = System.currentTimeMillis();
         }
         Datastream[] res=getAPIM().getDatastreams( pid, null, null );
 
 
 
 
         return "";
     }
 
     String[] getNextPID( int numberOfPids, String prefix ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         String[] pidlist = null;
         long timer = 0;
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
         pidlist = this.getAPIM().getNextPID( new NonNegativeInteger( Integer.toString( numberOfPids ) ) , prefix);
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         if ( pidlist == null )
         {
             log.error( "Could not retrieve pids from Fedora repository" );
             throw new IllegalStateException( "Could not retrieve pids from Fedora repository" );
         }
 
         return pidlist;
     }
 
 
     TupleIterator getTuples( Map< String,String > params ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         long timer = 0;
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         TupleIterator tuples = this.getFC().getTuples( params );
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return tuples;
     }
 
 
     boolean addRelationship( String pid, String predicate, String object, boolean isLiteral, String datatype ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         long timer = 0;
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         boolean ret = this.getAPIM().addRelationship( pid, predicate, object, isLiteral, datatype );
 
         if( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return ret;
     }
 
     
     boolean purgeRelationship( String pid, String predicate, String object, boolean isLiteral, String datatype ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         long timer = 0;
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         boolean ret = this.getAPIM().purgeRelationship( pid, predicate, object, isLiteral, datatype );
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return ret;
     }
 
 
     FieldSearchResult findObjects( String[] resultFields, NonNegativeInteger maxResults, FieldSearchQuery fsq ) throws ConfigurationException, MalformedURLException, IOException, ServiceException
     {
         long timer = 0;
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         FieldSearchResult fsr = this.getAPIA().findObjects( resultFields, maxResults, fsq );
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return fsr;
     }
 
 
     RelationshipTuple[] getRelationships( String subject, String predicate ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         long timer = 0;
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         RelationshipTuple[] rt = this.getAPIM().getRelationships( subject, predicate);
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return rt;
     }
 
 
     String[] purgeDatastream( String pid, String sID, String startDate, String endDate, String logm, boolean breakDependencies ) throws ConfigurationException, ServiceException, MalformedURLException, IOException
     {
         long timer = 0;
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         String[] rt = this.getAPIM().purgeDatastream( pid, sID, startDate, endDate, logm, breakDependencies);
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return rt;
     }
 
 
     String purgeObject( String identifier, String logmessage, boolean force ) throws ConfigurationException, ServiceException, MalformedURLException, IOException, RemoteException
     {
         long timer = 0;
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis();
         }
 
         String timestamp = this.getAPIM().purgeObject( identifier, logmessage, force );
 
         if ( log.isDebugEnabled() )
         {
             timer = System.currentTimeMillis() - timer;
             log.trace( String.format( "Timing: ( %s ) %s", this.getClass(), timer ) );
         }
 
         return timestamp;
     }
 
     
     boolean hasObject( String identifier ) throws RemoteException
     {
         try
         {
             DatastreamDef[] d = this.fea.listDatastreams( identifier, null );
             log.debug( String.format( "length of DatastreamDef: '%s'", d.length ) );
         }
         catch ( IOException ioe )
         {
            log.warn( String.format( "Could not list datastreams for object %s. Error message: %s ", identifier, ioe.getMessage() ) );
             return false;
         }
 
         return true;
     }
 }
