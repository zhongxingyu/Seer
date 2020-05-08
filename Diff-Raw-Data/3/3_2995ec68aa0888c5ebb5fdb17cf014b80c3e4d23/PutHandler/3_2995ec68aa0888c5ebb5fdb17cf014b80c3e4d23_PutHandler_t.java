 package com.bradmcevoy.http.http11;
 
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.quota.StorageChecker.StorageErrorReason;
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bradmcevoy.common.ContentTypeUtils;
 import com.bradmcevoy.common.Path;
 import com.bradmcevoy.http.Request.Method;
 import com.bradmcevoy.http.Response.Status;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.webdav.WebDavResponseHandler;
 
 public class PutHandler implements Handler {
 
     private static final Logger log = LoggerFactory.getLogger( PutHandler.class );
     private final Http11ResponseHandler responseHandler;
     private final HandlerHelper handlerHelper;
 
     public PutHandler( Http11ResponseHandler responseHandler, HandlerHelper handlerHelper ) {
         this.responseHandler = responseHandler;
         this.handlerHelper = handlerHelper;
         checkResponseHandler();
     }
 
     private void checkResponseHandler() {
         if( !( responseHandler instanceof WebDavResponseHandler ) ) {
             log.warn( "response handler is not a WebDavResponseHandler, so locking and quota checking will not be enabled" );
         }
     }
 
     public String[] getMethods() {
         return new String[]{Method.PUT.code};
     }
 
     @Override
     public boolean isCompatible( Resource handler ) {
         return ( handler instanceof PutableResource );
     }
 
     @Override
     public void process( HttpManager manager, Request request, Response response ) throws NotAuthorizedException, ConflictException, BadRequestException {
         if( !handlerHelper.checkExpects( responseHandler, request, response ) ) {
             return;
         }
 
         String host = request.getHostHeader();
         String urlToCreateOrUpdate = HttpManager.decodeUrl( request.getAbsolutePath() );
         log.debug( "process request: host: " + host + " url: " + urlToCreateOrUpdate );
 
         Path path = Path.path( urlToCreateOrUpdate );
         urlToCreateOrUpdate = path.toString();
 
         Resource existingResource = manager.getResourceFactory().getResource( host, urlToCreateOrUpdate );
         ReplaceableResource replacee;
 
 
 
         StorageErrorReason res = null;
         if( existingResource != null ) {
             //Make sure the parent collection is not locked by someone else
             if( handlerHelper.isLockedOut( request, existingResource ) ) {
                 log.warn( "resource is locked, but not by the current user" );
                 respondLocked( request, response, existingResource );
                 return;
             }
             Resource parent = manager.getResourceFactory().getResource( host, path.getParent().toString() );
             if( parent instanceof CollectionResource ) {
                 CollectionResource parentCol = (CollectionResource) parent;
                 res = handlerHelper.checkStorageOnReplace( request, parentCol, existingResource, host );
             } else {
                 log.warn( "parent exists but is not a collection resource: " + path.getParent() );
             }
         } else {
             CollectionResource parentCol = findNearestParent( manager, host, path );
             res = handlerHelper.checkStorageOnAdd( request, parentCol, path.getParent(), host );
         }
 
 
         if( res != null ) {
             respondInsufficientStorage( request, response, res );
             return;
         }
 
 
         if( existingResource != null && existingResource instanceof ReplaceableResource ) {
             replacee = (ReplaceableResource) existingResource;
         } else {
             replacee = null;
         }
 
         if( replacee != null ) {
             long t = System.currentTimeMillis();
             try {
                 manager.onProcessResourceStart( request, response, replacee );
                 processReplace( manager, request, response, (ReplaceableResource) existingResource );
             } finally {
                 t = System.currentTimeMillis() - t;
                 manager.onProcessResourceFinish( request, response, replacee, t );
             }
         } else {
             // either no existing resource, or its not replaceable. check for folder
             String nameToCreate = path.getName();
             CollectionResource folderResource = findOrCreateFolders( manager, host, path.getParent() );
             if( folderResource != null ) {
                 long t = System.currentTimeMillis();
                 try {
                     if( folderResource instanceof PutableResource ) {
 
                         //Make sure the parent collection is not locked by someone else
                         if( handlerHelper.isLockedOut( request, folderResource ) ) {
                             respondLocked( request, response, folderResource );
                             return;
                         }
 
                         PutableResource putableResource = (PutableResource) folderResource;
                         processCreate( manager, request, response, putableResource, nameToCreate );
                     } else {
                         manager.getResponseHandler().respondMethodNotImplemented( folderResource, response, request );
                     }
                 } finally {
                     t = System.currentTimeMillis() - t;
                     manager.onProcessResourceFinish( request, response, folderResource, t );
                 }
             } else {
                 responseHandler.respondNotFound( response, request );
             }
         }
     }
 
     private void processCreate( HttpManager manager, Request request, Response response, PutableResource folder, String newName ) throws ConflictException {
         log.debug( "processCreate: " + newName + " in " + folder.getName() );
         if( !handlerHelper.checkAuthorisation( manager, folder, request ) ) {
             responseHandler.respondUnauthorised( folder, response, request );
             return;
         }
 
         log.debug( "process: putting to: " + folder.getName() );
         try {
             Long l = getContentLength( request );
             String ct = findContentTypes( request, newName );
             log.debug( "PutHandler: creating resource of type: " + ct );
             folder.createNew( newName, request.getInputStream(), l, ct );
             log.debug( "PutHandler: DONE creating resource" );
         } catch( IOException ex ) {
             log.warn( "IOException reading input stream. Probably interrupted upload: " + ex.getMessage() );
             return;
         }
         manager.getResponseHandler().respondCreated( folder, response, request );
 
         log.debug( "process: finished" );
     }
 
     private Long getContentLength( Request request ) {
         Long l = request.getContentLengthHeader();
         if( l == null ) {
             String s = request.getRequestHeader( Request.Header.X_EXPECTED_ENTITY_LENGTH );
             if( s != null && s.length() > 0 ) {
                 log.debug( "no content-length given, but founhd non-standard length header: " + s );
                 try {
                     l = Long.parseLong( s );
                 } catch( NumberFormatException e ) {
                     throw new RuntimeException( "invalid length for header: " + Request.Header.X_EXPECTED_ENTITY_LENGTH.code + ". value is: " + s );
                 }
             }
         }
         return l;
     }
 
     /**
      * returns a textual representation of the list of content types for the
      * new resource. This will be the content type header if there is one,
      * otherwise it will be determined by the file name
      *
      * @param request
      * @param newName
      * @return
      */
     private String findContentTypes( Request request, String newName ) {
         String ct = request.getContentTypeHeader();
         if( ct != null ) return ct;
 
         return ContentTypeUtils.findContentTypes( newName );
     }
 
     private CollectionResource findOrCreateFolders( HttpManager manager, String host, Path path ) throws NotAuthorizedException, ConflictException {
         log.debug( "findOrCreateFolders" );
 
         if( path == null ) return null;
 
         Resource thisResource = manager.getResourceFactory().getResource( host, path.toString() );
         if( thisResource != null ) {
             if( thisResource instanceof CollectionResource ) {
                 return (CollectionResource) thisResource;
             } else {
                 log.warn( "parent is not a collection: " + path );
                 return null;
             }
         }
 
         CollectionResource parent = findOrCreateFolders( manager, host, path.getParent() );
         if( parent == null ) {
             log.warn( "couldnt find parent: " + path );
             return null;
         }
 
         Resource r = parent.child( path.getName() );
         if( r == null ) {
             if( parent instanceof MakeCollectionableResource ) {
                 MakeCollectionableResource mkcol = (MakeCollectionableResource) parent;
                 log.debug( "autocreating new folder: " + path.getName() );
                 return mkcol.createCollection( path.getName() );
             } else {
                 log.debug( "parent folder isnt a MakeCollectionableResource: " + parent.getName() );
                 return null;
             }
         } else if( r instanceof CollectionResource ) {
             return (CollectionResource) r;
         } else {
             log.debug( "parent in URL is not a collection: " + r.getName() );
             return null;
         }
     }
 
     private CollectionResource findNearestParent( HttpManager manager, String host, Path path ) throws NotAuthorizedException, ConflictException {
         log.debug( "findOrCreateFolders" );
 
         if( path == null ) return null;
 
         Resource thisResource = manager.getResourceFactory().getResource( host, path.toString() );
         if( thisResource != null ) {
             if( thisResource instanceof CollectionResource ) {
                 return (CollectionResource) thisResource;
             } else {
                 log.warn( "parent is not a collection: " + path );
                 return null;
             }
         }
 
         CollectionResource parent = findNearestParent( manager, host, path.getParent() );
         return parent;
     }
 
     /**
      * "If an existing resource is modified, either the 200 (OK) or 204 (No Content) response codes SHOULD be sent to indicate successful completion of the request."
      * 
      * @param request
      * @param response
      * @param replacee
      */
     private void processReplace( HttpManager manager, Request request, Response response, ReplaceableResource replacee ) {
         if( !handlerHelper.checkAuthorisation( manager, replacee, request ) ) {
             responseHandler.respondUnauthorised( replacee, response, request );
             return;
         }
         try {
             Long l = request.getContentLengthHeader();
             replacee.replaceContent( request.getInputStream(), l );
             log.debug( "PutHandler: DONE creating resource" );
         } catch( IOException ex ) {
             log.warn( "IOException reading input stream. Probably interrupted upload: " + ex.getMessage() );
             return;
         }
         responseHandler.respondCreated( replacee, response, request );
 
         log.debug( "process: finished" );
     }
 
     public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
         String host = request.getHostHeader();
         String urlToCreateOrUpdate = HttpManager.decodeUrl( request.getAbsolutePath() );
         log.debug( "process request: host: " + host + " url: " + urlToCreateOrUpdate );
 
         Path path = Path.path( urlToCreateOrUpdate );
         urlToCreateOrUpdate = path.toString();
 
         Resource existingResource = manager.getResourceFactory().getResource( host, urlToCreateOrUpdate );
         ReplaceableResource replacee;
 
         if( existingResource != null ) {
             //Make sure the parent collection is not locked by someone else
             if( handlerHelper.isLockedOut( request, existingResource ) ) {
                 log.warn( "resource is locked, but not by the current user" );
                 response.setStatus( Status.SC_LOCKED ); //423
                 return;
             }
 
         }
         if( existingResource != null && existingResource instanceof ReplaceableResource ) {
             replacee = (ReplaceableResource) existingResource;
         } else {
             replacee = null;
         }
 
         if( replacee != null ) {
             processReplace( manager, request, response, (ReplaceableResource) existingResource );
         } else {
             // either no existing resource, or its not replaceable. check for folder
             String urlFolder = path.getParent().toString();
             String nameToCreate = path.getName();
             CollectionResource folderResource = findOrCreateFolders( manager, host, path.getParent() );
             if( folderResource != null ) {
                 if( log.isDebugEnabled() ) {
                     log.debug( "found folder: " + urlFolder + " - " + folderResource.getClass() );
                 }
                 if( folderResource instanceof PutableResource ) {
 
                     //Make sure the parent collection is not locked by someone else
                     if( handlerHelper.isLockedOut( request, folderResource ) ) {
                         response.setStatus( Status.SC_LOCKED ); //423
                         return;
                     }
 
                     PutableResource putableResource = (PutableResource) folderResource;
                     processCreate( manager, request, response, putableResource, nameToCreate );
                 } else {
                     responseHandler.respondMethodNotImplemented( folderResource, response, request );
                 }
             } else {
                 responseHandler.respondNotFound( response, request );
             }
         }
 
     }
 
     private void respondLocked( Request request, Response response, Resource existingResource ) {
         if( responseHandler instanceof WebDavResponseHandler ) {
             WebDavResponseHandler rh = (WebDavResponseHandler) responseHandler;
             rh.respondLocked( request, response, existingResource );
         } else {
             response.setStatus( Status.SC_LOCKED ); //423
         }
     }
 
     private void respondInsufficientStorage( Request request, Response response, StorageErrorReason storageErrorReason ) {
         if( responseHandler instanceof WebDavResponseHandler ) {
             WebDavResponseHandler rh = (WebDavResponseHandler) responseHandler;
             rh.respondInsufficientStorage( request, response, storageErrorReason );
         } else {
             response.setStatus( Status.SC_INSUFFICIENT_STORAGE );
         }
     }
 }
