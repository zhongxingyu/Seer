 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
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
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.http.HttpStatus;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.auth.ForbiddenException;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 
 /**
  * An aggregated resource REST API resource.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("ROs/{ro_id}/{filePath: .+}")
 public class Resource {
 
     /** HTTP request. */
     @Context
     private HttpServletRequest servletRequest;
 
     /** URI info. */
     @Context
     private UriInfo uriInfo;
 
 
     /**
      * Update an exiting resource or upload a one for which a proxy exists.
      * 
      * @param researchObjectId
      *            RO id
      * @param filePath
      *            file path relative to the RO URI
      * @param original
      *            original format in case of annotation bodies
      * @param entity
      *            resource content
      * @return 201 Created or 307 Temporary Redirect
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @PUT
     public Response putResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original, String entity)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.create(uri);
         URI resource = uriInfo.getAbsolutePath();
 
         if (ROSRService.SMS.get().isProxy(researchObject, resource)) {
             return Response.status(Status.TEMPORARY_REDIRECT)
                     .location(ROSRService.SMS.get().getProxyFor(researchObject, resource)).build();
         }
         if (ROSRService.SMS.get().isAggregatedResource(researchObject, resource)) {
             if (ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
                 return Response
                         .status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                         .entity(
                             "This resource is an annotation, only \"application/vnd.wf4ever.annotation\" media type is accepted")
                         .build();
             }
             if (original != null) {
                 resource = resource.resolve(original);
             }
             if (researchObject.getManifestUri().equals(resource)) {
                 throw new ForbiddenException("Can't update the manifest");
             } else if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(resource)) {
                 throw new ForbiddenException("Can't update the evo info");
             }
             ResponseBuilder builder = ROSRService.updateInternalResource(researchObject, resource, entity,
                 servletRequest.getContentType());
             ResourceMetadata resInfo = ROSRService.getResourceInfo(researchObject, resource, original);
             if (resInfo != null) {
                 CacheControl cache = new CacheControl();
                 cache.setMustRevalidate(true);
                 builder = builder.cacheControl(cache).tag(resInfo.getChecksum())
                         .lastModified(resInfo.getLastModified().toDate());
             }
             return builder.build();
         } else {
             throw new ForbiddenException(
                     "You cannot use PUT to create new resources unless they have been referenced in a proxy or an annotation. Use POST instead.");
         }
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
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      * @throws BadRequestException
      *             the RDF/XML content is incorrect
      */
     @PUT
     @Consumes(Constants.ANNOTATION_MIME_TYPE)
     public Response updateAnnotation(@PathParam("ro_id") String researchObjectId,
             @PathParam("filePath") String filePath, @QueryParam("original") String original, InputStream content)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException, BadRequestException {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.create(uri);
         URI resource = uriInfo.getAbsolutePath();
         URI body;
         List<URI> targets = new ArrayList<>();
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(content, researchObject.getUri().toString());
         ExtendedIterator<Individual> it = model.listIndividuals(RO.AggregatedAnnotation);
         if (it.hasNext()) {
             Individual aggregatedAnnotation = it.next();
             NodeIterator it2 = aggregatedAnnotation.listPropertyValues(AO.body);
             if (it2.hasNext()) {
                 RDFNode bodyResource = it2.next();
                 if (bodyResource.isURIResource()) {
                     try {
                         body = new URI(bodyResource.asResource().getURI());
                     } catch (URISyntaxException e) {
                         throw new BadRequestException("Wrong body resource URI", e);
                     }
                 } else {
                     throw new BadRequestException("The body is not an URI resource.");
                 }
             } else {
                 throw new BadRequestException("The ro:AggregatedAnnotation does not have a ao:body property.");
             }
             it2 = aggregatedAnnotation.listPropertyValues(AO.annotatesResource);
             while (it2.hasNext()) {
                 RDFNode targetResource = it2.next();
                 if (targetResource.isURIResource()) {
                     try {
                         targets.add(new URI(targetResource.asResource().getURI()));
                     } catch (URISyntaxException e) {
                         throw new BadRequestException("Wrong target resource URI", e);
                     }
                 } else {
                     throw new BadRequestException("The target is not an URI resource.");
                 }
             }
         } else {
             throw new BadRequestException("The entity body does not define any ro:AggregatedAnnotation.");
         }
 
         if (!ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
             throw new ForbiddenException("You cannot create a new annotation using PUT, use POST instead.");
         }
         return ROSRService.updateAnnotation(researchObject, resource, body, targets);
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
      *            HTTP request for cache'ing
      * @return 200 OK or 303 See Other
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @GET
     public Response getResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original, @HeaderParam("Accept") String accept, @Context Request request)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.create(uri);
         URI resource = uriInfo.getAbsolutePath();
 
         if (ROSRService.SMS.get().isProxy(researchObject, resource)) {
             return Response.status(Status.SEE_OTHER)
                     .location(ROSRService.SMS.get().getProxyFor(researchObject, resource)).build();
         }
         if (ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
             return Response
                     .status(Status.SEE_OTHER)
                     .location(
                         ROSRService.getAnnotationBody(researchObject, resource,
                             servletRequest.getHeader(Constants.ACCEPT_HEADER))).build();
         }
         if (ROSRService.SMS.get().isRoFolder(researchObject, resource)) {
             Folder folder = new Folder();
             folder.setUri(resource);
             RDFFormat format = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
             return Response.status(Status.SEE_OTHER).location(folder.getResourceMapUri(format)).build();
         }
 
         ResourceMetadata resInfo = ROSRService.getResourceInfo(researchObject, resource, original);
         if (resInfo != null) {
             ResponseBuilder rb = request.evaluatePreconditions(resInfo.getLastModified().toDate(), new EntityTag(
                     resInfo.getChecksum()));
             if (rb != null) {
                 return rb.build();
             }
         }
 
         ResponseBuilder builder = ROSRService.getInternalResource(researchObject, resource,
             servletRequest.getHeader("Accept"), original, resInfo);
 
         if (resInfo != null) {
             CacheControl cache = new CacheControl();
             cache.setMustRevalidate(true);
             builder = builder.cacheControl(cache).tag(resInfo.getChecksum())
                     .lastModified(resInfo.getLastModified().toDate());
         }
         return builder.build();
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
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @DELETE
     public Response deleteResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         ResearchObject researchObject = ResearchObject.create(uri);
         URI resource = uriInfo.getAbsolutePath();
 
         if (ROSRService.SMS.get().isProxy(researchObject, resource)) {
             URI proxyFor = ROSRService.SMS.get().getProxyFor(researchObject, resource);
             if (ROSRService.isInternalResource(researchObject, proxyFor)) {
                 return Response.status(Status.TEMPORARY_REDIRECT).location(proxyFor).build();
             } else {
                 return ROSRService.deaggregateExternalResource(researchObject, resource);
             }
         }
         if (ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
             if (ROSRService.SMS.get()
                     .findAnnotationForBody(researchObject, researchObject.getFixedEvolutionAnnotationBodyUri())
                     .getUri().equals(resource)) {
                 throw new ForbiddenException("Can't delete the evo annotation");
             }
             return ROSRService.deleteAnnotation(researchObject, resource);
         }
         if (ROSRService.SMS.get().isRoFolder(researchObject, resource)) {
             Folder folder = ROSRService.SMS.get().getFolder(resource);
             ROSRService.deleteFolder(folder);
             return Response.noContent().build();
         }
         FolderEntry entry = ROSRService.SMS.get().getFolderEntry(resource);
         if (entry != null) {
             ROSRService.deleteFolderEntry(entry);
             return Response.noContent().build();
         }
         if (original != null) {
             resource = resource.resolve(original);
         }
         if (researchObject.getManifestUri().equals(resource)) {
             throw new ForbiddenException("Can't delete the manifest");
         } else if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(resource)) {
             throw new ForbiddenException("Can't delete the evo info");
         }
 
         return ROSRService.deaggregateInternalResource(researchObject, resource);
     }
 
 
     /**
      * Create a new folder entry.
      * 
      * @param content
      *            folder entry description
      * @return 201 Created response pointing to the folder entry
      * @throws BadRequestException
      *             wrong request body
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @POST
     @Consumes(Constants.FOLDERENTRY_MIME_TYPE)
     public Response addFolderEntry(InputStream content)
             throws BadRequestException, AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI uri = uriInfo.getAbsolutePath();
         Folder folder = ROSRService.SMS.get().getFolder(uri);
 
         FolderEntry entry = ROSRService.assembleFolderEntry(folder, content);
         folder.getFolderEntries().add(entry);
         ROSRService.updateFolder(folder);
 
         return Response
                 .created(entry.getUri())
                 .header(
                     "Link",
                     String.format(Constants.LINK_HEADER_TEMPLATE, entry.getProxyFor(),
                         "http://www.openarchives.org/ore/terms/proxyFor")).build();
     }
 }
