 package com.bradmcevoy.http.webdav;
 
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bradmcevoy.http.Request.Method;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.http11.Http11ResponseHandler;
 import java.net.URI;
 
 
 public class MoveHandler implements ExistingEntityHandler {
     
     private Logger log = LoggerFactory.getLogger(MoveHandler.class);
 
     private final Http11ResponseHandler responseHandler;
     private final HandlerHelper handlerHelper;
     private final ResourceHandlerHelper resourceHandlerHelper;
 
     public MoveHandler( Http11ResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper ) {
         this.responseHandler = responseHandler;
         this.handlerHelper = handlerHelper;
         this.resourceHandlerHelper = resourceHandlerHelper;
     }
 
 
 
     public String[] getMethods() {
         return new String[]{Method.MOVE.code};
     }
         
     @Override
     public boolean isCompatible(Resource handler) {
         return (handler instanceof MoveableResource);
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
         log.debug("dest header1: " + sDest);
         URI destUri = URI.create(sDest);
         sDest = destUri.getPath();
         log.debug("dest header2: " + sDest);
         Dest dest = new Dest(destUri.getHost(),sDest);
         log.debug("looking for destination parent: " + dest.host + " - " + dest.url);
         Resource rDest = manager.getResourceFactory().getResource(dest.host, dest.url);        
         log.debug("process: moving from: " + r.getName() + " -> " + dest.url + " with name: " + dest.name);
         if( rDest == null ) {
             log.debug("process: destination parent does not exist: " + sDest);
             responseHandler.respondConflict(resource, response, request, "Destination parent does not exist: " + sDest);
         } else if( !(rDest instanceof CollectionResource) ) {
             log.debug("process: destination exists but is not a collection");
             responseHandler.respondConflict(resource, response, request, "Destination exists but is not a collection: " + sDest);
         } else { 
             CollectionResource colDest = (CollectionResource) rDest;
             // check if the dest exists
             Resource rExisting = colDest.child( dest.name);
             if( rExisting != null ) {
                 // check for overwrite header
                if( !request.getOverwriteHeader() ) {
                     log.debug( "destination resource exists, and overwrite header is not set");
                     responseHandler.respondConflict( resource, response, request, "A resource exists at the destination");
                     return ;
                 } else {
                     if( rExisting instanceof DeletableResource) {
                         log.debug( "deleting existing resource");
                         DeletableResource drExisting = (DeletableResource) rExisting;
                         drExisting.delete();
                     } else {
                         log.warn( "destination exists, and overwrite header is set, but destination is not a DeletableResource");
                         responseHandler.respondConflict( resource, response, request, "A resource exists at the destination, and it cannot be deleted");
                         return ;
                     }
                 }
             }
             log.debug("process: moving resource to: " + rDest.getName());
             try {
                 r.moveTo( (CollectionResource) rDest, dest.name );
                 responseHandler.respondCreated(resource, response, request);
             } catch( ConflictException ex ) {
                 log.warn( "conflict", ex);
                 responseHandler.respondConflict( resource, response, request, sDest );
             }
         }
         log.debug("process: finished");
     }
 
 
 }
