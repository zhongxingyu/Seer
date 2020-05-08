 /*
  *  This file is part of xor
  *  Copyright © 2009, Steen Manniche.
  *
  *  xor is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  xor is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with xor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package net.manniche.xor.server.rmi;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.manniche.xor.types.ObjectIdentifier;
 import net.manniche.xor.storage.StorageProvider;
 import net.manniche.xor.types.DigitalObject;
 import net.manniche.xor.server.RepositoryServer;
 import net.manniche.xor.exceptions.RepositoryServiceException;
 import net.manniche.xor.server.RepositoryObserver;
 import net.manniche.xor.types.DefaultIdentifier;
 import net.manniche.xor.types.ObjectRepositoryContentType;
 import net.manniche.xor.types.RepositoryAction;
 import net.manniche.xor.utils.RepositoryUtilities;
 
 /**
  * The connection handler for RMI requests. This class represents the RMI
  * server implementation.
  *
  * {@link RepositoryObserver}s can register with this class through the
  * {@link #registerObservers() } method and recieve notifications for each of the
  * actions defined in the {@link RepositoryAction} enum. The nitifications
  * have an accompanying content type defined by the
  * {@link RMIRepositoryServerContentType} enum.
  * 
  * @author stm
  */
 public final class RMIRepositoryServer extends RepositoryServer implements RMIObjectManagement
 {
     static final long serialVersionUID = -8975965744773865183L;
 
     private final static Logger Log = Logger.getLogger( RMIRepositoryServer.class.getName() );
 
     private final List< RepositoryObserver > observers;
 
     private final String storagePath;
     private final String metadataStoragePath;
 
     /**
      * Sets up the RMI server for the object repository.
      *
      * @param storage the StorageProvider that handles storage of objects for
      * this RMI server instance
      * @param storagePath path to which data will be stored for this server instance
      * @param metadataStoragePath path to which metadata of data will be stored for this server instance
      * @param logMessageHandler handles the log message storing/notifications
      * @throws RemoteException if the server could not be started
      */
     public RMIRepositoryServer( StorageProvider storage, String storagePath, String metadataStoragePath ) throws RemoteException
     {
         super( storage );
         this.storagePath = storagePath;
         this.metadataStoragePath = metadataStoragePath;
         this.observers = new ArrayList<RepositoryObserver>();
         Log.info( "Constructed repository server over RMI" );
     }
 
     /**
      * Content types defined for the RMIServer. The content types will be
      * communicated to the registered
      * {@link net.manniche.xor.server.RepositoryObserver observers} to
      * {@link net.manniche.xor.types.RepositoryAction}. Additional
      * {@link net.manniche.xor.types.ObjectRepositoryContentType}s are defined
      * in
      * {@link net.manniche.xor.server.RepositoryServer.RepositoryServerContentType}
      *
      */
     public enum RMIRepositoryServerContentType implements ObjectRepositoryContentType
     {
         /**
          * The default/fallback content type.
          */
         BINARY_CONTENT( "application/octet-stream" ),
         /**
          * Defines the content type DublinCore as specified by the Dublin Core
          * Meta Data standard {@link http://dublincore.org}. It is implicit that
          * the contenttype has the mimetype text/xml
          */
         DUBLIN_CORE( "text/xml" );
 
         String mimeType;
         RMIRepositoryServerContentType( String mime )
         {
             this.mimeType = mime;
         }
 
         public static ObjectRepositoryContentType getContentType( String contentType ) throws TypeNotPresentException
         {
             for( RMIRepositoryServerContentType type: RMIRepositoryServerContentType.values() )
             {
                 if( contentType.toUpperCase().equals( type.toString() ) )
                 {
                     return type;
                 }
             }
             throw new TypeNotPresentException( contentType, new IllegalArgumentException( String.format( "No content type exists for %s", contentType ) ) );
         }
     }
 
     /**
      * Retrieves a {@link DigitalObject} identified by {@code identifier} from
      * the object repository. A RemoteException is thrown if no object matches
      * the identifier given.
      *
      * @param identifier identifying the object to be retrieved
      * @return a DigitalObject if the identifier matches
      * @throws RemoteException if anything goes wrong in the object retrieval
      * process
      */
     @Override
     public DigitalObject getRepositoryObject( ObjectIdentifier identifier ) throws RemoteException
     {
         DigitalObject digitalObject = null;
 
         Log.info( String.format( "Getting object from %s", identifier.getURI() ) );
         try
         {
             digitalObject = super.getObject( identifier );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to retrieve object identified by %s: %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
 
         ObjectRepositoryContentType contentTypeForObject;
         try
         {
             contentTypeForObject = this.getContentTypeForObject( identifier );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to retrieve content type for object identified by %s: %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
         catch( RepositoryServiceException ex )
         {
             String error = String.format( "Failed to retrieve content type for object identified by %s: %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
         catch( URISyntaxException ex )
         {
             String error = String.format( "Failed to retrieve content type for object identified by %s: %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
 
         this.notifyObservers( identifier, RepositoryAction.REQUEST, contentTypeForObject );
 
         return digitalObject;
     }
 
 
     /**
      * Stores a {@link DigitalObject} {@code data} into the object repository,
      * returning the {@link ObjectIdentifier identifier} uniquely identifying
      * the object.
      * 
      * @param data the object to be stored
      * @param contentType the content type of the data, as defined by {@link RMIRepositoryServerContentType}
      * @param logmessage a message describing the operation, provided by the user
      * @return an {@link ObjectIdentifier} that identifies the object within the
      * object repository
      * @throws RemoteException if either the metadata could not be retrived or
      * some lower level operation in the storage implementation failed. Detailed
      * information is available in the wrapped exception.
      */
     @Override
     public final ObjectIdentifier storeRepositoryObject( DigitalObject data, ObjectRepositoryContentType contentType, String logmessage ) throws RemoteException
     {
         Log.info( "Storing object without predefined identifier" );
         ObjectIdentifier identifier = null;
         try
         {
             identifier = this.storeRepositoryObject( data, contentType, null, logmessage );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to store object: %s", ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
         catch( RepositoryServiceException ex )
         {
             String error = String.format( "Failed to store object: %s", ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.SEVERE, error, ex );
             throw new RemoteException(error, ex );
         }
 
         this.notifyObservers( identifier, RepositoryAction.ADD, contentType );
         
         return identifier;
     }
 
 
     /**
      * Stores a {@link DigitalObject} {@code data} into the object repository,
      * returning the {@link ObjectIdentifier identifier} uniquely identifying
      * the object.
      *
      * @param data the object to be stored
      * @param identifier the object, when stored will be identified by this id
      * @param logmessage a message describing the operation, provided by the user
      * @return an {@link ObjectIdentifier} that identifies the object within the
      * object repository
      * @throws RemoteException if either the metadata could not be retrived or
      * some lower level operation in the storage implementation failed. Detailed
      * information is available in the wrapped exception.
      */
     private ObjectIdentifier storeRepositoryObject( DigitalObject data,
                                                     ObjectRepositoryContentType contentType,
                                                     ObjectIdentifier identifier,
                                                     String logmessage ) throws RemoteException, IOException, RepositoryServiceException
     {
 
         Log.info( "Storing object" );
         ObjectIdentifier oIdentifier = null;
         oIdentifier = super.storeObject( data.getBytes(), this.storagePath, identifier , logmessage);
 
         Log.info( String.format( "Stored object with uri %s", oIdentifier.getURI() ) );
 
         // we return the given, if it's non-null, the constructed otherwise
         if( null == identifier )
         {
             Log.info( String.format( "given id=%s, returned id=%s", identifier, oIdentifier ) );
             identifier = oIdentifier;
         }
 
         Log.info( String.format( "URL path: %s, URL toString: %s", identifier.getURI().getPath(), identifier.getURI().toString() ) );
 
 
         try
         {
             //URI metadataURI = XMLUtils.generateURI( this.metadataStoragePath, Integer.toString( contentType.hashCode() ) );
             //ObjectIdentifier contentIdentifier = new DefaultIdentifier( metadataURI.toURL() );
             Log.info( String.format( "Storing content type at uri %s", identifier.getURI() ) );
 
             String objectName = identifier.getName();
             URI contentURI = RepositoryUtilities.generateURI( "file", this.metadataStoragePath, objectName );
             ObjectIdentifier contentIdentifier = new DefaultIdentifier( contentURI );
             super.storeObject( contentType.toString().getBytes(), this.metadataStoragePath, contentIdentifier, "Storing content type" );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to store object content type: %s", ex.getMessage() );
             Log.log( Level.WARNING, error, ex);
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
         catch( URISyntaxException ex )
         {
             String error = String.format( "Failed to create uri for object content type: %s", ex.getMessage() );
             Log.log( Level.WARNING, error, ex);
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
 
         Log.info( String.format( "Stored object at %s", identifier.getURI() ) );
 
         return identifier;
     }
 
 
     /**
      * Deletes a {@link DigitalObject} identified by {@code identifier} from
      * the object repository. If the object could be found _and_ deleted, this
      * method returns true, otherwise it returns false. E.g. a RemoteException
      * will be thrown if the object can be found, but not deleted.
      *
      * @param identifier identifying the object to be retrieved
      * @param logmessage user supplied message describing the context of the action
      * @throws RemoteException if anything goes wrong in the object deletion.
      * process
      */
     @Override
     public void deleteRepositoryObject( ObjectIdentifier identifier, String logmessage ) throws RemoteException
     {
         String.format( String.format( "Deleting object from %s", identifier.getURI() ) );
         ObjectRepositoryContentType contentTypeForObject;
         try
         {
             contentTypeForObject = this.getContentTypeForObject( identifier );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to retrieve content type for object identified by %s: %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
         catch( RepositoryServiceException ex )
         {
             String error = String.format( "Failed to retrieve content type for object identified by %s: %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
         catch( URISyntaxException ex )
         {
             String error = String.format( "Failed to retrieve content type for object identified by %s: %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
 
         try
         {
             super.deleteObject( identifier, logmessage );
            super.deleteContentType( identifier );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to delete object identified by '%s': %s", identifier, ex.getMessage() );
             Logger.getLogger( RMIRepositoryServer.class.getName() ).log( Level.WARNING, error, ex );
             //wrap and send to RMI client
             throw new RemoteException( error, ex );
         }
         this.notifyObservers( identifier, RepositoryAction.DELETE, contentTypeForObject );
 
     }
 
     private ObjectRepositoryContentType getContentTypeForObject( ObjectIdentifier objectIdentifier ) throws IOException, RepositoryServiceException, URISyntaxException
     {
         String name = objectIdentifier.getName();
 
         ObjectIdentifier metadataIdentifier = null;
         Log.info( String.format( "Trying to generate uri from '%s' + '%s'", this.metadataStoragePath, name ) );
         URI metaurl = RepositoryUtilities.generateURI( "file", this.metadataStoragePath, name );
         metadataIdentifier = new DefaultIdentifier( metaurl );
         Log.info( String.format( "Constructed identifier for metadata with uri %s", metadataIdentifier.getURI() ) );
 
         DigitalObject object = super.getObject( metadataIdentifier );
 
         String contentTypeString = new String( object.getBytes() );
         ObjectRepositoryContentType contentType = RMIRepositoryServerContentType.getContentType( contentTypeString );
         return contentType;
     }
 
 
 
     private void registerObservers()
     {
         //read a configuration file for implementations of the ObjectRepositoryService
         // that should be added as observers of actions taken by the RepositoryServer
     }
 
     @Override
     public synchronized void addObserver( RepositoryObserver observer )
     {
         this.observers.add( observer );
     }
 
     @Override
     public synchronized void removeObserver( RepositoryObserver observer )
     {
         this.observers.remove( observer );
     }
 
 
     @Override
     public void notifyObservers( ObjectIdentifier identifier, RepositoryAction action, ObjectRepositoryContentType contentType )
     {
         Log.info( String.format( "Notifying on %s for object %s with contenttype %s", identifier, action, contentType ) );
         synchronized( this.observers )
         {
             for( RepositoryObserver observer : this.observers )
             {
                 observer.notifyMe( identifier, action, contentType );
             }
         }
     }
 
 }
