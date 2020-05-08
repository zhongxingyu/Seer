 package com.redhat.rcm.nexus.capture;
 
 import static com.redhat.rcm.nexus.capture.CaptureLogUtils.deleteLogs;
 import static com.redhat.rcm.nexus.capture.CaptureLogUtils.queryLogs;
 import static com.redhat.rcm.nexus.capture.model.ModelSerializationUtils.getGson;
 import static com.redhat.rcm.nexus.capture.request.RequestUtils.mediaTypeOf;
 import static com.redhat.rcm.nexus.capture.request.RequestUtils.modeOf;
 import static com.redhat.rcm.nexus.capture.request.RequestUtils.query;
 import static com.redhat.rcm.nexus.util.ProtocolUtils.getXStreamForREST;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jsecurity.SecurityUtils;
 import org.jsecurity.subject.Subject;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
 import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
 import org.sonatype.plexus.rest.resource.PlexusResource;
 
 import com.redhat.rcm.nexus.capture.model.CaptureSession;
 import com.redhat.rcm.nexus.capture.model.CaptureSessionRef;
 import com.redhat.rcm.nexus.capture.request.RequestMode;
 import com.redhat.rcm.nexus.capture.store.CaptureSessionQuery;
 import com.redhat.rcm.nexus.capture.store.CaptureStore;
 import com.redhat.rcm.nexus.capture.store.CaptureStoreException;
 import com.redhat.rcm.nexus.capture.template.TemplateConstants;
 import com.redhat.rcm.nexus.capture.template.TemplateException;
 import com.redhat.rcm.nexus.capture.template.TemplateFormatter;
 import com.redhat.rcm.nexus.protocol.CaptureSessionRefResource;
 import com.redhat.rcm.nexus.protocol.CaptureSessionResource;
 import com.redhat.rcm.nexus.protocol.ProtocolConstants;
 
 @Named( "captureMyLog" )
 public class CaptureMyLogResource
     extends AbstractNexusPlexusResource
     implements PlexusResource
 {
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     @Inject
     @Named( "json" )
     private CaptureStore captureStore;
 
     @Inject
     @Named( "velocity" )
     private TemplateFormatter templateFormatter;
 
     public CaptureMyLogResource()
     {
         this.setModifiable( true );
     }
 
     @Override
     public Object getPayloadInstance()
     {
         return CaptureSessionRefResource.payloadPrototype();
     }
 
     @Override
     public PathProtectionDescriptor getResourceProtection()
     {
         return new PathProtectionDescriptor( ProtocolConstants.MY_LOGS_RESOURCE_FRAGMENT + "/*",
                                              String.format( "authcBasic,perms[%s]",
                                                             CaptureResourceConstants.PRIV_LOG_ACCESS ) );
     }
 
     @Override
     public String getResourceUri()
     {
         return ProtocolConstants.MY_LOGS_RESOURCE_FRAGMENT + "/{" + CaptureResourceConstants.ATTR_BUILD_TAG_REPO_ID
                         + "}";
     }
 
     @Override
     public Object get( final Context context, final Request request, final Response response, final Variant variant )
         throws ResourceException
     {
         final String buildTag =
             request.getAttributes().get( CaptureResourceConstants.ATTR_BUILD_TAG_REPO_ID ).toString();
 
         final Subject subject = SecurityUtils.getSubject();
         final String user = subject.getPrincipal().toString();
 
         Object data = null;
 
         final RequestMode mode = modeOf( request );
         if ( mode != null )
         {
             if ( mode == RequestMode.TABLE_OF_CONTENTS )
             {
                 try
                 {
                     final CaptureSessionQuery query = new CaptureSessionQuery().setUser( user ).setBuildTag( buildTag );
 
                     data = queryLogs( captureStore, query, request.getRootRef().toString() );
                 }
                 catch ( final CaptureStoreException e )
                 {
                     logger.error( "Failed to retrieve capture-log listing. Error: {}\nMessage: {}", e.getClass()
                                                                                                      .getName(),
                                   e.getMessage() );
                     e.printStackTrace();
 
                     throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
                 }
             }
         }
 
         if ( data == null )
         {
             try
             {
                 final CaptureSession session = captureStore.readLatestLog( user, buildTag );
                 if ( session != null )
                 {
                     data = session.asResource( request.getRootRef().toString(), getRepositoryRegistry() );
                 }
             }
             catch ( final CaptureStoreException e )
             {
                 logger.error( "Failed to retrieve capture log. Error: {}\nMessage: {}", e.getClass().getName(),
                               e.getMessage() );
                 e.printStackTrace();
 
                 throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
             }
         }
 
         final MediaType mt = mediaTypeOf( request, variant );
         final String result = serialize( data, mt, request );
 
         return new StringRepresentation( result, mt );
     }
 
     private String serialize( final Object data, final MediaType mt, final Request request )
         throws ResourceException
     {
         String result = null;
         if ( mt == MediaType.APPLICATION_XML )
         {
             result = getXStreamForREST().toXML( data );
         }
         else if ( mt == MediaType.APPLICATION_JSON )
         {
             result = getGson().toJson( data );
         }
         else if ( mt == MediaType.TEXT_PLAIN )
         {
             final Map<String, Object> templateContext = new HashMap<String, Object>();
             templateContext.put( "data", data );
 
             final Form query = query( request );
             final String template = query.getFirstValue( CaptureResourceConstants.PARAM_TEMPLATE );
 
             try
             {
                 result = templateFormatter.format( TemplateConstants.LOG_TEMPLATE_BASEPATH, template, templateContext );
             }
             catch ( final TemplateException e )
             {
                 throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
             }
         }
         else
         {
             throw new ResourceException( Status.CLIENT_ERROR_NOT_ACCEPTABLE );
         }
 
         return result;
     }
 
     @Override
     public Object post( final Context context, final Request request, final Response response, final Object payload )
         throws ResourceException
     {
         final String buildTag =
             request.getAttributes().get( CaptureResourceConstants.ATTR_BUILD_TAG_REPO_ID ).toString();
 
         final Subject subject = SecurityUtils.getSubject();
         final String user = subject.getPrincipal().toString();
 
         CaptureSessionResource resource = null;
         try
         {
             final CaptureSessionRef ref = captureStore.closeCurrentLog( user, buildTag );
            CaptureSession session = null;
             if ( ref != null )
             {
                session = captureStore.readLog( ref );
            }
 
            if ( session == null )
            {
                session = captureStore.readLatestLog( user, buildTag );
             }

            resource =
                session == null ? null : session.asResource( request.getRootRef().toString(), getRepositoryRegistry() );
         }
         catch ( final CaptureStoreException e )
         {
             logger.error( "Failed to close current capture log. Error: {}\nMessage: {}", e.getClass().getName(),
                           e.getMessage() );
 
             e.printStackTrace();
 
             throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
         }
 
         logger.info( "Returning session-ref resource:\n\n" + getXStreamForREST().toXML( resource ) );
 
         final MediaType mt = mediaTypeOf( request );
         final String result = serialize( resource, mt, request );
 
         return new StringRepresentation( result, mt );
     }
 
     @Override
     public void delete( final Context context, final Request request, final Response response )
         throws ResourceException
     {
         final String buildTag =
             request.getAttributes().get( CaptureResourceConstants.ATTR_BUILD_TAG_REPO_ID ).toString();
 
         final Subject subject = SecurityUtils.getSubject();
         final String user = subject.getPrincipal().toString();
 
         deleteLogs( captureStore, user, buildTag, request );
     }
 
     @Override
     public List<Variant> getVariants()
     {
         final List<Variant> variants = new ArrayList<Variant>();
 
         variants.add( new Variant( MediaType.APPLICATION_XML ) );
         variants.add( new Variant( MediaType.APPLICATION_JSON ) );
         variants.add( new Variant( MediaType.TEXT_PLAIN ) );
 
         return variants;
     }
 }
