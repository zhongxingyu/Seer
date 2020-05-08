 package com.bradmcevoy.http.webdav;
 
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bradmcevoy.http.Request.Method;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import java.net.URI;
 
 public class MoveHandler implements ExistingEntityHandler {
 
     private Logger log = LoggerFactory.getLogger( MoveHandler.class );
     private final WebDavResponseHandler responseHandler;
     private final HandlerHelper handlerHelper;
     private final ResourceHandlerHelper resourceHandlerHelper;
     private DeleteHelper deleteHelper;
     private UserAgentHelper userAgentHelper = new DefaultUserAgentHelper();
 
     /**
      * Sets userAgentHelper to DefaultUserAgentHelper, which can be overridden by
      * setting the property
      *
      * deleteHelper is set to DeleteHelperImpl
      *
      * @param responseHandler
      * @param handlerHelper
      * @param resourceHandlerHelper
      */
     public MoveHandler( WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper ) {
         this.responseHandler = responseHandler;
         this.handlerHelper = handlerHelper;
         this.resourceHandlerHelper = resourceHandlerHelper;
         this.deleteHelper = new DeleteHelperImpl( handlerHelper );
     }
 
     public String[] getMethods() {
         return new String[]{Method.MOVE.code};
     }
 
     @Override
     public boolean isCompatible( Resource handler ) {
         return ( handler instanceof MoveableResource );
     }
 
     public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
         resourceHandlerHelper.processResource( manager, request, response, r, this );
     }
 
     public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
         resourceHandlerHelper.process( httpManager, request, response, this );
     }
 
     public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
         MoveableResource r = (MoveableResource) resource;
         String sDest = request.getDestinationHeader();
         //sDest = HttpManager.decodeUrl(sDest);
         log.debug( "dest header1: " + sDest );
         URI destUri = URI.create( sDest );
         sDest = destUri.getPath();
         log.debug( "dest header2: " + sDest );
         Dest dest = new Dest( destUri.getHost(), sDest );
         log.debug( "looking for destination parent: " + dest.host + " - " + dest.url );
         Resource rDest = manager.getResourceFactory().getResource( dest.host, dest.url );
         log.debug( "process: moving from: " + r.getName() + " -> " + dest.url + " with name: " + dest.name );
         if( rDest == null ) {
             log.debug( "process: destination parent does not exist: " + sDest );
             responseHandler.respondConflict( resource, response, request, "Destination parent does not exist: " + sDest );
         } else if( !( rDest instanceof CollectionResource ) ) {
             log.debug( "process: destination exists but is not a collection" );
             responseHandler.respondConflict( resource, response, request, "Destination exists but is not a collection: " + sDest );
         } else {
             CollectionResource colDest = (CollectionResource) rDest;
             // check if the dest exists
             Resource rExisting = colDest.child( dest.name );
             if( rExisting != null ) {
                 // check for overwrite header
                if( !canOverwrite( request ) ) {
                     log.debug( "destination resource exists, and overwrite header is not set" );
                     responseHandler.respondPreconditionFailed( request, response, rExisting );
                     return;
                 } else {
                     if( rExisting instanceof DeletableResource ) {
                         log.debug( "deleting existing resource" );
                         DeletableResource drExisting = (DeletableResource) rExisting;
                         if( deleteHelper.isLockedOut( request, drExisting ) ) {
                             log.debug( "destination resource exists but is locked" );
                             responseHandler.respondLocked( request, response, drExisting );
                             return;
                         }
                         log.debug( "deleting pre-existing destination resource" );
                         deleteHelper.delete( drExisting );
                     } else {
                         log.warn( "destination exists, and overwrite header is set, but destination is not a DeletableResource" );
                         responseHandler.respondConflict( resource, response, request, "A resource exists at the destination, and it cannot be deleted" );
                         return;
                     }
                 }
             }
             log.debug( "process: moving resource to: " + rDest.getName() );
             try {
                 r.moveTo( (CollectionResource) rDest, dest.name );
                 responseHandler.respondCreated( resource, response, request );
             } catch( ConflictException ex ) {
                 log.warn( "conflict", ex );
                 responseHandler.respondConflict( resource, response, request, sDest );
             }
         }
         log.debug( "process: finished" );
     }
 
     private boolean canOverwrite( Request request ) {
         Boolean ow = request.getOverwriteHeader();
         boolean bHasOverwriteHeader = ( ow != null && request.getOverwriteHeader().booleanValue() );
         if( bHasOverwriteHeader) {
             return true;
         } else {
             String us = request.getUserAgentHeader();
             if( userAgentHelper.isMacFinder( us)) {
                 log.debug( "no overwrite header, but user agent is Finder so permit overwrite");
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     public UserAgentHelper getUserAgentHelper() {
         return userAgentHelper;
     }
 
     public void setUserAgentHelper( UserAgentHelper userAgentHelper ) {
         this.userAgentHelper = userAgentHelper;
     }
 
     public DeleteHelper getDeleteHelper() {
         return deleteHelper;
     }
 
     public void setDeleteHelper( DeleteHelper deleteHelper ) {
         this.deleteHelper = deleteHelper;
     }
 }
