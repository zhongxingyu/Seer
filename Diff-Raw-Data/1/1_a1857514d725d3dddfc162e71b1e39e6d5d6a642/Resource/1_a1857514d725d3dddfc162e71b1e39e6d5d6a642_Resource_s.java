 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.InputStream;
 import java.net.URI;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.CacheControl;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.EntityTag;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.http.HttpStatus;
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.auth.RequestAttribute;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.exceptions.ForbiddenException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.ORE.Proxy;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObjectComponent;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * An aggregated resource REST API resource.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("ROs/{ro_id}/{filePath: .+}")
 public class Resource {
 
     /** URI info. */
     @Context
     private UriInfo uriInfo;
 
     /** Resource builder. */
     @RequestAttribute("Builder")
     private Builder builder;
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(ZippedResearchObjectResource.class);
 
 
     /**
      * Update an exiting resource or upload a one for which a proxy or annotation exists.
      * 
      * @param researchObjectId
      *            RO id
      * @param filePath
      *            file path relative to the RO URI
      * @param original
      *            original format in case of annotation bodies
      * @param accept
      *            Accept header
      * @param contentType
      *            content type header
      * @param entity
      *            resource content
      * @return 201 Created or 307 Temporary Redirect
      * @throws BadRequestException
      *             if it is expected to be an RDF file and isn't
      */
     @PUT
     public Response putResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original, @HeaderParam("Accept") String accept,
             @HeaderParam("Content-Type") String contentType, InputStream entity)
             throws BadRequestException {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         URI resourceUri = uriInfo.getAbsolutePath();
         if (original != null) {
             resourceUri = resourceUri.resolve(original);
         }
         RDFFormat responseSyntax = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
 
         if (researchObject.getResourceMaps().containsKey(resourceUri)) {
             throw new ForbiddenException("Can't update resource maps");
         } else if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(resourceUri)) {
             throw new ForbiddenException("Can't update the evo info");
         } else if (researchObject.getProxies().containsKey(resourceUri)) {
             return updateProxy(researchObject.getProxies().get(resourceUri));
         } else if (researchObject.getAnnotations().containsKey(resourceUri)) {
             String message = String.format("This resource is an annotation, only %s media type is accepted",
                 Constants.ANNOTATION_MIME_TYPE);
             return Response.status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE).entity(message).build();
         } else if (researchObject.getFolderEntries().containsKey(resourceUri)) {
             String message = String.format("This resource is a folder entry, only %s media type is accepted",
                 Constants.FOLDERENTRY_MIME_TYPE);
             return Response.status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE).entity(message).build();
         } else if (researchObject.getAggregatedResources().containsKey(resourceUri)) {
             AggregatedResource resource = researchObject.getAggregatedResources().get(resourceUri);
             boolean exists = resource.isInternal();
             resource.update(entity, contentType);
             CacheControl cache = new CacheControl();
             cache.setMustRevalidate(true);
             ResponseBuilder rb = exists ? Response.ok() : Response.created(resource.getUri());
             researchObject.updateIndexAttributes();
             return rb.cacheControl(cache).tag(resource.getStats().getChecksum())
                     .lastModified(resource.getStats().getLastModified().toDate()).build();
         } else if (researchObject.getAnnotationsByBodyUri().containsKey(resourceUri)) {
             AggregatedResource resource = researchObject.aggregate(filePath, entity, contentType);
             String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, resource.getUri().toString(),
                 ORE.proxyFor.getURI());
             InputStream proxyAndResourceDesc = researchObject.getManifest().getGraphAsInputStream(responseSyntax,
                 resource.getProxy(), resource);
             ResponseBuilder rb = Response.created(resource.getProxy().getUri()).entity(proxyAndResourceDesc)
                     .type(responseSyntax.getDefaultMIMEType()).header(Constants.LINK_HEADER, proxyForHeader);
             if (resource.getStats() != null) {
                 CacheControl cache = new CacheControl();
                 cache.setMustRevalidate(true);
                 rb = rb.cacheControl(cache).tag(resource.getStats().getChecksum())
                         .lastModified(resource.getStats().getLastModified().toDate());
             }
             researchObject.updateIndexAttributes();
             return rb.build();
         } else {
             throw new NotFoundException(
                     "You cannot use PUT to create new resources unless they have been referenced in a proxy or an annotation. Use POST instead.");
         }
     }
 
 
     /**
      * Update the proxy.
      * 
      * @param proxy
      *            the proxy
      * @return 307 with a redirection to the proxied resource
      */
     private Response updateProxy(Proxy proxy) {
         return Response.status(Status.TEMPORARY_REDIRECT).location(proxy.getProxyFor().getUri()).build();
     }
 
 
     /**
      * Make a PUT to update an annotation.
      * 
      * @param researchObjectId
      *            research object ID
      * @param filePath
      *            the file path
      * @param original
      *            original resource in case of a format-specific URI
      * @param content
      *            RDF/XML representation of an annotation
      * @return 200 OK
      * @throws BadRequestException
      *             the RDF/XML content is incorrect
      */
     @PUT
     @Consumes(Constants.ANNOTATION_MIME_TYPE)
     public Response updateAnnotation(@PathParam("ro_id") String researchObjectId,
             @PathParam("filePath") String filePath, @QueryParam("original") String original, InputStream content)
             throws BadRequestException {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         URI resourceUri = uriInfo.getAbsolutePath();
         Annotation annotation = researchObject.getAnnotations().get(resourceUri);
         if (annotation == null) {
             if (researchObject.isUriUsed(resourceUri)) {
                 String message = "This resource is not an annotation";
                 return Response.status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE).entity(message).build();
             } else {
                 throw new NotFoundException("Annotation not found");
             }
         }
         Annotation newAnnotation = Annotation.assemble(builder, researchObject, resourceUri, content);
         annotation = annotation.update(newAnnotation);
 
         String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotation.getBody().getUri()
                 .toString(), AO.annotatesResource);
         ResponseBuilder response = Response.ok().header(Constants.LINK_HEADER, annotationBodyHeader);
         for (Thing target : annotation.getAnnotated()) {
             String targetHeader = String
                     .format(Constants.LINK_HEADER_TEMPLATE, target.toString(), AO.annotatesResource);
             response = response.header(Constants.LINK_HEADER, targetHeader);
         }
         researchObject.updateIndexAttributes();
         return response.build();
     }
 
 
     /**
      * Make a PUT to update a folder entry.
      * 
      * @param researchObjectId
      *            research object ID
      * @param filePath
      *            the file path
      * @param original
      *            original resource in case of a format-specific URI
      * @param content
      *            RDF/XML representation of a folder entry
      * @return 200 OK
      * @throws BadRequestException
      *             the RDF/XML content is incorrect
      */
     @PUT
     @Consumes(Constants.FOLDERENTRY_MIME_TYPE)
     public Response updateFolderEntry(@PathParam("ro_id") String researchObjectId,
             @PathParam("filePath") String filePath, @QueryParam("original") String original, InputStream content)
             throws BadRequestException {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         URI resourceUri = uriInfo.getAbsolutePath();
         FolderEntry entry = researchObject.getFolderEntries().get(resourceUri);
         if (entry == null) {
             if (researchObject.isUriUsed(resourceUri)) {
                 String message = "This resource is not a folder entry";
                 return Response.status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE).entity(message).build();
             } else {
                 throw new NotFoundException("Folder entry not found");
             }
         }
         FolderEntry newEntry = FolderEntry.assemble(builder, entry.getFolder(), content);
         entry = entry.update(newEntry);
 
         String link = String.format(Constants.LINK_HEADER_TEMPLATE, entry.getProxyFor(), ORE.proxyFor.getURI());
         return Response.ok().header("Link", link).build();
     }
 
 
     /**
      * Get a resource.
      * 
      * @param researchObjectId
      *            RO id
      * @param filePath
      *            the file path
      * @param original
      *            original resource in case of a format-specific URI
      * @param accept
      *            Accept header
      * @param request
      *            HTTP request for cacheing
      * @return 200 OK or 303 See Other
      */
     @GET
     public Response getResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original, @HeaderParam("Accept") String accept, @Context Request request) {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         URI resourceUri = uriInfo.getAbsolutePath();
         String specificName = null;
         if (original != null) {
             specificName = resourceUri.resolve(".").relativize(resourceUri).getPath();
             resourceUri = UriBuilder.fromUri(resourceUri.resolve(".")).path(original).build();
         }
         //FIXME this won't work for Accept headers with more than one RDF format
         RDFFormat format = RDFFormat.forMIMEType(accept);
 
         if (researchObject.getProxies().containsKey(resourceUri)) {
             return getProxy(researchObject.getProxies().get(resourceUri));
         }
         if (researchObject.getFolderEntries().containsKey(resourceUri)) {
             return getProxy(researchObject.getFolderEntries().get(resourceUri));
         }
         if (researchObject.getAnnotations().containsKey(resourceUri)) {
             return getAnnotation(researchObject.getAnnotations().get(resourceUri), format);
         }
         if (researchObject.getFolders().containsKey(resourceUri)) {
             return getFolder(researchObject.getFolders().get(resourceUri), format);
         }
 
         ResearchObjectComponent resource;
         if (researchObject.getAggregatedResources().containsKey(resourceUri)) {
             resource = researchObject.getAggregatedResources().get(resourceUri);
         } else if (researchObject.getResourceMaps().containsKey(resourceUri)) {
             resource = researchObject.getResourceMaps().get(resourceUri);
         } else {
             throw new NotFoundException("Resource not found");
         }
         ResponseBuilder rb = resource.getStats() != null ? request.evaluatePreconditions(resource.getStats()
                 .getLastModified().toDate(), new EntityTag(resource.getStats().getChecksum())) : null;
         if (rb != null) {
             return rb.build();
         }
         InputStream data = null;
         String mimeType;
         String filename = resource.getName();
         if (!resource.isInternal()) {
             throw new NotFoundException("Resource has no content");
         }
         if (resource.isNamedGraph()) {
             // check if request is for a specific format
             if (specificName != null) {
                 URI specificResourceUri = UriBuilder.fromUri(resource.getUri().resolve(".")).path(specificName).build();
                 if (format == null) {
                     format = specificResourceUri.getPath() != null ? RDFFormat.forFileName(
                         specificResourceUri.getPath(), RDFFormat.RDFXML) : RDFFormat.RDFXML;
                 }
                 data = resource.getPublicGraphAsInputStream(format);
                 mimeType = format.getDefaultMIMEType();
                 filename = specificName;
             } else {
                 RDFFormat extensionFormat = RDFFormat.forFileName(resource.getUri().getPath());
                 if (extensionFormat != null && (format == null || extensionFormat == format)) {
                     // 1. GET manifest.rdf Accept: application/rdf+xml
                     // 2. GET manifest.rdf
                     data = resource.getPublicGraphAsInputStream(extensionFormat);
                     mimeType = extensionFormat.getDefaultMIMEType();
                 } else {
                     // 3. GET manifest.rdf Accept: text/turtle
                     // 4. GET manifest Accept: application/rdf+xml
                     // 5. GET manifest
                     if (format == null) {
                         format = RDFFormat.RDFXML;
                     }
                     return Response.temporaryRedirect(resource.getUri(format)).build();
                 }
             }
         } else {
             data = resource.getSerialization();
             // backward compatibility
             mimeType = resource.getStats() != null ? resource.getStats().getMimeType() : "text/plain";
         }
         if (data == null) {
             throw new NotFoundException("Resource has no content");
         }
 
         ContentDisposition cd = ContentDisposition.type("attachment").fileName(filename).build();
         CacheControl cache = new CacheControl();
         cache.setMustRevalidate(true);
         if (resource.getStats() != null) {
             return Response.ok(data).type(mimeType).header("Content-disposition", cd).cacheControl(cache)
                     .tag(resource.getStats().getChecksum())
                     .lastModified(resource.getStats().getLastModified().toDate()).build();
         } else {
             // backwards compatibility
             return Response.ok(data).type(mimeType).header("Content-disposition", cd).cacheControl(cache).build();
         }
     }
 
 
     /**
      * Get a folder.
      * 
      * @param folder
      *            folder
      * @param format
      *            format requested
      * @return 303 See Other redirecting to the resource map
      */
     private Response getFolder(Folder folder, RDFFormat format) {
         return Response.status(Status.SEE_OTHER).location(folder.getResourceMap().getUri(format)).build();
     }
 
 
     /**
      * Get an annotation.
      * 
      * @param annotation
      *            annotation
      * @param format
      *            format requested
      * @return 303 See Other redirecting to the body
      */
     private Response getAnnotation(Annotation annotation, RDFFormat format) {
         URI bodyUri = annotation.getBody().getUri();
         if (annotation.getResearchObject().getAggregatedResources().containsKey(bodyUri) && format != null) {
             AggregatedResource resource = annotation.getResearchObject().getAggregatedResources().get(bodyUri);
             if (resource.isInternal()) {
                 bodyUri = resource.getUri(format);
             }
         }
         return Response.status(Status.SEE_OTHER).location(bodyUri).build();
     }
 
 
     /**
      * Get a proxy.
      * 
      * @param proxy
      *            proxy
      * @return 303 See Other redirecting to the proxied resource
      */
     private Response getProxy(Proxy proxy) {
         return Response.status(Status.SEE_OTHER).location(proxy.getProxyFor().getUri()).build();
     }
 
 
     /**
      * Delete a resource.
      * 
      * @param researchObjectId
      *            RO id
      * @param filePath
      *            the file path
      * @param original
      *            original resource in case of a format-specific URI
      * @return 204 No Content or 307 Temporary Redirect
      */
     @DELETE
     public Response deleteResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original) {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         URI resourceUri = uriInfo.getAbsolutePath();
         if (original != null) {
             resourceUri = resourceUri.resolve(original);
         }
 
         Thing resource;
         if (researchObject.getResourceMaps().containsKey(resourceUri)) {
             throw new ForbiddenException("Can't delete the resource map");
         } else if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(resourceUri)) {
             throw new ForbiddenException("Can't delete the evo info");
         } else if (researchObject.getProxies().containsKey(resourceUri)) {
             return deleteProxy(researchObject.getProxies().get(resourceUri));
         } else if (researchObject.getFolderEntries().containsKey(resourceUri)) {
             resource = researchObject.getFolderEntries().get(resourceUri);
         } else if (researchObject.getAnnotations().containsKey(resourceUri)) {
             Annotation annotation = researchObject.getAnnotations().get(resourceUri);
             if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(annotation.getBody().getUri())) {
                 throw new ForbiddenException("Can't delete the evo annotation");
             }
             resource = annotation;
         } else if (researchObject.getAggregatedResources().containsKey(resourceUri)) {
             resource = researchObject.getAggregatedResources().get(resourceUri);
         } else {
             throw new NotFoundException("Resource not found");
         }
         resource.delete();
         researchObject.updateIndexAttributes();
         return Response.noContent().build();
     }
 
 
     /**
      * Delete the proxy.
      * 
      * @param proxy
      *            proxy to delete
      * @return 303 for internal resources, 204 for external.
      */
     private Response deleteProxy(Proxy proxy) {
         AggregatedResource resource = proxy.getProxyFor();
         if (resource.isInternal()) {
             return Response.status(Status.TEMPORARY_REDIRECT).location(resource.getUri()).build();
         } else {
             return Response.noContent().build();
         }
     }
 
 
     /**
      * Create a new folder entry.
      * 
      * @param content
      *            folder entry description
      * @return 201 Created response pointing to the folder entry
      * @throws BadRequestException
      *             wrong request body
      */
     @POST
     @Consumes(Constants.FOLDERENTRY_MIME_TYPE)
     public Response addFolderEntry(InputStream content)
             throws BadRequestException {
         URI uri = uriInfo.getAbsolutePath();
         Folder folder = Folder.get(builder, uri);
         if (folder == null) {
             throw new NotFoundException("Folder not found; " + uri);
         }
         FolderEntry entry = folder.createFolderEntry(content);
         String link = String.format(Constants.LINK_HEADER_TEMPLATE, entry.getProxyFor(), ORE.proxyFor.getURI());
         return Response.created(entry.getUri()).header("Link", link).build();
     }
 }
