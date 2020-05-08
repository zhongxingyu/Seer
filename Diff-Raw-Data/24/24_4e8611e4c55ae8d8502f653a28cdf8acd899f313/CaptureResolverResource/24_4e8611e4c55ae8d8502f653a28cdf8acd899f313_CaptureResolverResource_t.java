 package com.redhat.tools.nexus.capture;
 
 import org.jsecurity.SecurityUtils;
 import org.jsecurity.authz.AuthorizationException;
 import org.jsecurity.subject.Subject;
 import org.restlet.Context;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.Variant;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonatype.nexus.proxy.AccessDeniedException;
 import org.sonatype.nexus.proxy.IllegalOperationException;
 import org.sonatype.nexus.proxy.ItemNotFoundException;
 import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
 import org.sonatype.nexus.proxy.ResourceStore;
 import org.sonatype.nexus.proxy.ResourceStoreRequest;
 import org.sonatype.nexus.proxy.StorageException;
 import org.sonatype.nexus.proxy.item.StorageItem;
 import org.sonatype.nexus.proxy.repository.Repository;
 import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
 import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
 import org.sonatype.plexus.rest.resource.PlexusResource;
 
 import com.redhat.tools.nexus.capture.config.CaptureConfiguration;
 import com.redhat.tools.nexus.capture.config.InvalidConfigurationException;
 import com.redhat.tools.nexus.capture.store.CaptureStore;
 import com.redhat.tools.nexus.capture.store.CaptureStoreException;
 import com.redhat.tools.nexus.protocol.ProtocolConstants;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import java.io.IOException;
 
 /**
  * Capture resource, which will try to resolve first from a build-tag repository (first part of URL after /capture/),
  * then from a capture-source repository (second part of the URL after /capture/). <br/>
  * NOTE: If the user does not have access to the capture-source repository, the retrieve attempt will fail.
  */
 @Named( "captureResolver" )
 public class CaptureResolverResource
     extends AbstractResourceStoreContentPlexusResource
     implements PlexusResource
 {
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     @Inject
     @Named( "json" )
     private CaptureStore captureStore;
 
     @Inject
     @Named( "modello" )
     private CaptureConfiguration configuration;
 
     @Override
     public Object getPayloadInstance()
     {
         // if you allow PUT or POST you would need to return your object.
         return null;
     }
 
     @Override
     public PathProtectionDescriptor getResourceProtection()
     {
         return new PathProtectionDescriptor( ProtocolConstants.RESOLVE_RESOURCE_FRAGMENT + "/*/**",
                                              String.format( "authcBasic,perms[%s]",
                                                             CaptureResourceConstants.PRIV_ACCESS ) );
     }
 
     @Override
     public String getResourceUri()
     {
         return ProtocolConstants.RESOLVE_RESOURCE_FRAGMENT + "/{" + CaptureResourceConstants.ATTR_BUILD_TAG_REPO_ID
             + "}";
     }
 
     @Override
     public Object get( final Context context, final Request request, final Response response, final Variant variant )
         throws ResourceException
     {
         final ResourceStoreRequest req = getResourceStoreRequest( request );
 
         final String buildTag =
             request.getAttributes().get( CaptureResourceConstants.ATTR_BUILD_TAG_REPO_ID ).toString();
 
         String capture;
         try
         {
             capture = configuration.getModel().getCaptureSource();
         }
         catch ( final InvalidConfigurationException e )
         {
             throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
         }
 
         final Subject subject = SecurityUtils.getSubject();
         final String user = subject.getPrincipal().toString();
 
         Object result = null;
 
        logger.info( String.format( "CAPTURE REPO: Using user: '%s' build-tag: '%s' and capture-source: '%s'", user,
                                    buildTag, capture ) );
 
         try
         {
             if ( logger.isDebugEnabled() )
             {
                 logger.debug( "Attempting to resolve: '{}' from build-tag repository: '{}'", req.getRequestPath(),
                               buildTag );
             }
 
             final Repository buildTagRepo = getUnprotectedRepositoryRegistry().getRepository( buildTag );
 
             StorageItem item = null;
             try
             {
                 // NOTE: Not recording successful resolution from the build-tag repo.
                 item = buildTagRepo.retrieveItem( req );
 
                 result = renderItem( context, request, response, variant, item );
             }
             catch ( final ItemNotFoundException e )
             {
                 if ( capture == null )
                 {
                     // NOTE: Not recording this...
                     result = handleNotFound( e, context, request, response, variant, req );
                 }
                 else
                 {
                     try
                     {
                         result = resolveExternal( user, buildTag, capture, context, request, response, variant, req );
                         if ( result == null )
                         {
                             result = handleNotFound( e, context, request, response, variant, req );
                         }
                     }
                     catch ( final AuthorizationException authzEx )
                     {
                        logger.info( "User: '" + user
                            + "' does not have permission to resolve dependencies from capture source." );
 
                         // NOTE: Not recording this...
                         result = handleNotFound( e, context, request, response, variant, req );
                     }
                 }
             }
         }
         catch ( final Exception e )
         {
             logger.error( "Capture failed. Error: {}\nMessage: {}", e.getClass().getName(), e.getMessage() );
             e.printStackTrace();
 
             handleException( request, response, e );
         }
 
         return result;
     }
 
     private Object resolveExternal( final String user, final String buildTag, final String capture,
                                     final Context context, final Request request, final Response response,
                                     final Variant variant, final ResourceStoreRequest req )
         throws CaptureStoreException, AccessDeniedException, IOException, NoSuchResourceStoreException,
         ItemNotFoundException, ResourceException, IllegalOperationException
     {
         final Subject subject = SecurityUtils.getSubject();
 
         if ( !subject.isPermitted( CaptureResourceConstants.PERM_EXTERNAL_GET ) )
         {
             logger.warn( "User: '" + user + "' does not have permission to resolve dependencies from capture source." );
             return null;
         }
 
         if ( logger.isDebugEnabled() )
         {
             logger.debug(
                           "Resolve from build-tag repository: '{}' MISSED! Attempting to resolve from capture-source: '{}'",
                           buildTag, capture );
         }
 
         final Repository captureRepo = getUnprotectedRepositoryRegistry().getRepository( capture );
 
         try
         {
             final StorageItem item = captureRepo.retrieveItem( req );
             captureStore.logResolved( user, buildTag, capture, req.getProcessedRepositories(), req.getRequestPath(),
                                       item );
 
             return renderItem( context, request, response, variant, item );
         }
         catch ( final ItemNotFoundException eCap )
         {
             captureStore.logUnresolved( user, buildTag, capture, req.getProcessedRepositories(), req.getRequestPath() );
 
             // FIXME: This will hide the build-tag instance of ItemNotFoundException...
             return handleNotFound( eCap, context, request, response, variant, req );
         }
         catch ( final AccessDeniedException accessEx )
         {
             if ( logger.isDebugEnabled() )
             {
                 logger.debug( "Capture failed. Access to: '{}' was denied.", capture );
             }
 
             return null;
         }
     }
 
     private Object handleNotFound( final ItemNotFoundException e, final Context context, final Request request,
                                    final Response response, final Variant variant, final ResourceStoreRequest req )
         throws AccessDeniedException, StorageException, IOException, NoSuchResourceStoreException,
         IllegalOperationException, ItemNotFoundException, ResourceException
     {
         if ( isDescribe( request ) )
         {
             return renderDescribeItem( context, request, response, variant, req, null );
         }
         else
         {
             throw e;
         }
     }
 
     // NOTE: Not Used. We're overriding the method that requires this.
     @Override
     protected ResourceStore getResourceStore( final Request request )
         throws NoSuchResourceStoreException, ResourceException
     {
         return null;
     }
 }
