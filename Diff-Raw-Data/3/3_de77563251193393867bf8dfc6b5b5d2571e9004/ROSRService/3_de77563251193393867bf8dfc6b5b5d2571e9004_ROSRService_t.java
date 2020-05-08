 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.UUID;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.ResourceInfo;
 import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.evo.EvoType;
 import pl.psnc.dl.wf4ever.exceptions.ManifestTraversingException;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl;
 import pl.psnc.dl.wf4ever.utils.zip.MemoryZipFile;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 
 import com.google.common.collect.Multimap;
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * Utility class for distributing the RO tasks to dLibra and SMS.
  * 
  * @author piotrekhol
  * 
  */
 public final class ROSRService {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(ROSRService.class);
 
     /** Thread local DL instance. */
     public static final ThreadLocal<DigitalLibrary> DL = new ThreadLocal<>();
 
     /** Thread local SMS instance. */
     public static final ThreadLocal<SemanticMetadataService> SMS = new ThreadLocal<>();
 
 
     /**
      * Private constructor.
      */
     private ROSRService() {
         //nope
     }
 
 
     /**
      * Create a new research object.
      * 
      * @param researchObject
      *            the research object
      * @return research object URI
      * @throws ConflictException
      *             the research object id is already used
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             no permissions
      */
     public static URI createResearchObject(ResearchObject researchObject)
             throws ConflictException, DigitalLibraryException, NotFoundException, AccessDeniedException {
         return createResearchObject(researchObject, null, null);
     }
 
 
     /**
      * Create a new research object of a specific ROEVO type.
      * 
      * @param researchObject
      *            the research object URI
      * @param type
      *            ROEVO type, may be null
      * @param sourceRO
      *            live RO for snapshots and archives, any URI for live ROs, may be null if type is null
      * @return research object URI
      * @throws ConflictException
      *             the research object id is already used
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             no permissions
      */
     public static URI createResearchObject(ResearchObject researchObject, EvoType type, ResearchObject sourceRO)
             throws ConflictException, DigitalLibraryException, NotFoundException, AccessDeniedException {
         InputStream manifest;
         try {
             if (type == null) {
                 LOGGER.debug(String.format("%s\t\tcreate RO start", new DateTime().toString()));
                 ROSRService.SMS.get().createResearchObject(researchObject);
                 LOGGER.debug(String.format("%s\t\tcreate RO end", new DateTime().toString()));
             } else {
                 switch (type) {
                     default:
                     case LIVE:
                         ROSRService.SMS.get().createLiveResearchObject(researchObject, sourceRO);
                         break;
                     case SNAPSHOT:
                         ROSRService.SMS.get().createSnapshotResearchObject(researchObject, sourceRO);
                         break;
                     case ARCHIVED:
                         ROSRService.SMS.get().createArchivedResearchObject(researchObject, sourceRO);
                         break;
                 }
             }
             manifest = ROSRService.SMS.get().getManifest(researchObject, RDFFormat.RDFXML);
         } catch (IllegalArgumentException e) {
             // RO already existed in sms, maybe created by someone else
             throw new ConflictException("The RO with URI " + researchObject.getUri() + " already exists");
         }
 
         LOGGER.debug(String.format("%s\t\tcreate RO start", new DateTime().toString()));
         ROSRService.DL.get().createResearchObject(researchObject, manifest, ResearchObject.MANIFEST_PATH,
             RDFFormat.RDFXML.getDefaultMIMEType());
         LOGGER.debug(String.format("%s\t\tcreate RO end", new DateTime().toString()));
         return researchObject.getUri();
     }
 
 
     /**
      * Delete a research object.
      * 
      * @param researchObject
      *            research object URI
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws NotFoundException
      *             Research Object not found neither in dLibra nor in SMS
      */
     public static void deleteResearchObject(ResearchObject researchObject)
             throws DigitalLibraryException, NotFoundException {
         try {
             ROSRService.DL.get().deleteResearchObject(researchObject);
         } finally {
             try {
                 ROSRService.SMS.get().removeResearchObject(researchObject);
             } catch (IllegalArgumentException e) {
                 LOGGER.warn("URI not found in SMS: " + researchObject.getUri());
             } finally {
                 researchObject.delete();
             }
         }
     }
 
 
     /**
      * Aggregate a new internal resource in the research object, upload the resource and add a proxy.
      * 
      * @param researchObject
      *            the research object
      * @param resource
      *            resource URI, must be internal
      * @param entity
      *            request entity
      * @param contentType
      *            resource content type
      * @param original
      *            original resource in case of using a format specific URI
      * @return 201 Created with proxy URI
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder aggregateInternalResource(ResearchObject researchObject, URI resource,
             InputStream entity, String contentType, String original)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         if (original != null) {
             resource = resource.resolve(original);
         }
         String filePath = researchObject.getUri().relativize(resource).getPath();
         ResourceInfo resourceInfo = ROSRService.DL.get().createOrUpdateFile(researchObject, filePath, entity,
             contentType != null ? contentType : "text/plain");
         ROSRService.SMS.get().addResource(researchObject, resource, resourceInfo);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         return addProxy(researchObject, resource);
     }
 
 
     /**
      * Aggregate a resource in the research object without uploading its content and add a proxy.
      * 
      * @param researchObject
      *            research object
      * @param resource
      *            resource that will be aggregated
      * @return 201 Created response pointing to the proxy
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder aggregateExternalResource(ResearchObject researchObject, URI resource)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         ROSRService.SMS.get().addResource(researchObject, resource, null);
         ResponseBuilder builder = addProxy(researchObject, resource);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         return builder;
     }
 
 
     /**
      * De-aggregate an internal resource, delete its content and proxy.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            resource to delete
      * @return 204 No Content response
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response deaggregateInternalResource(ResearchObject researchObject, URI resource)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String filePath = researchObject.getUri().relativize(resource).getPath();
         ROSRService.DL.get().deleteFile(researchObject, filePath);
         if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
             ROSRService.SMS.get().removeAnnotationBody(researchObject, resource);
         } else {
             ROSRService.SMS.get().removeResource(researchObject, resource);
         }
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         return Response.noContent().build();
     }
 
 
     /**
      * Remove the aggregation of a resource in a research object and its proxy.
      * 
      * @param researchObject
      *            research object that aggregates the resource
      * @param proxy
      *            external resource proxy
      * @return 204 No Content
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response deaggregateExternalResource(ResearchObject researchObject, URI proxy)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI resource = ROSRService.SMS.get().getProxyFor(researchObject, proxy);
         ROSRService.SMS.get().removeResource(researchObject, resource);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         return deleteProxy(researchObject, proxy);
     }
 
 
     /**
      * Check if the resource is internal. Resource is internal only if its content has been deployed under the control
      * of the service. A resource that has "internal" URI but the content has not been uploaded is considered external.
      * 
      * @param researchObject
      *            research object that aggregates the resource
      * @param resource
      *            the resource that may be internal
      * @return true if the resource content is deployed under the control of the service, false otherwise
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      */
     public static boolean isInternalResource(ResearchObject researchObject, URI resource)
             throws NotFoundException, DigitalLibraryException {
         String filePath = researchObject.getUri().relativize(resource).getPath();
         return ROSRService.DL.get().fileExists(researchObject, filePath);
     }
 
 
     /**
      * Get The evolution Information.
      * 
      * @param researchObject
      *            the RO URI
      * @return InputStream with evolution information written in TTL format
      **/
     public static InputStream getEvoInfo(ResearchObject researchObject) {
         ROSRService.SMS.get();
         return null;
     }
 
 
     /**
      * Add a proxy to the research object.
      * 
      * @param researchObject
      *            the research object
      * @param proxyFor
      *            resource for which the proxy is
      * @return 201 Created response pointing to the proxy
      */
     private static ResponseBuilder addProxy(ResearchObject researchObject, URI proxyFor) {
         URI proxy = ROSRService.SMS.get().addProxy(researchObject, proxyFor);
 
         String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, proxyFor.toString(),
             Constants.ORE_PROXY_FOR_HEADER);
         return Response.created(proxy).header(Constants.LINK_HEADER, proxyForHeader);
     }
 
 
     /**
      * Delete the proxy. Does not delete the resource that it points to.
      * 
      * @param researchObject
      *            research object that aggregates the proxy
      * @param proxy
      *            proxy to delete
      * @return 204 No Content
      */
     private static Response deleteProxy(ResearchObject researchObject, URI proxy) {
         ROSRService.SMS.get().deleteProxy(researchObject, proxy);
         return Response.noContent().build();
     }
 
 
     /**
      * Update an aggregated resource, which may be a regular file or RO metadata. The resource must be aggregated by the
      * research object but may have no content.
      * 
      * @param researchObject
      *            research object which aggregates the resource
      * @param resource
      *            resource URI
      * @param entity
      *            resource content
      * @param contentType
      *            resource content type
      * @return 200 OK response or 201 Created if there had been no content
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder updateInternalResource(ResearchObject researchObject, URI resource, String entity,
             String contentType)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         String filePath = researchObject.getUri().relativize(resource).getPath();
         boolean contentExisted = ROSRService.DL.get().fileExists(researchObject, filePath);
         if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
             RDFFormat format = RDFFormat.forMIMEType(contentType);
             if (format == null) {
                 format = RDFFormat.forFileName(resource.getPath(), RDFFormat.RDFXML);
             }
             ROSRService.SMS.get().addAnnotationBody(researchObject, resource,
                 new ByteArrayInputStream(entity.getBytes()), format);
             // update the named graph copy in dLibra, the manifest is not changed
             updateNamedGraphInDlibra(filePath, researchObject, resource);
             updateROAttributesInDlibra(researchObject);
         } else {
             ResourceInfo resourceInfo = ROSRService.DL.get().createOrUpdateFile(researchObject, filePath,
                 new ByteArrayInputStream(entity.getBytes()), contentType != null ? contentType : "text/plain");
             ROSRService.SMS.get().addResource(researchObject, resource, resourceInfo);
             // update the manifest that describes the resource in dLibra
             updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         }
         if (contentExisted) {
             return Response.ok();
         } else {
             return Response.created(resource);
         }
     }
 
 
     /**
      * Get resource information.
      * 
      * @param researchObject
      *            RO URI
      * @param resource
      *            resource URI
      * @param original
      *            original resource name in case of annotation bodies
      * @return resource info
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResourceInfo getResourceInfo(ResearchObject researchObject, URI resource, String original)
             throws AccessDeniedException, NotFoundException, DigitalLibraryException {
         String filePath = researchObject.getUri().relativize(original != null ? resource.resolve(original) : resource)
                 .getPath();
         return ROSRService.DL.get().getFileInfo(researchObject, filePath);
     }
 
 
     /**
      * Return the content of an internal resource.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            resource URI
      * @param accept
      *            requested MIME type
      * @param original
      *            original file name in case of annotation bodies
      * @param resInfo
      *            resource info, if already known, or null
      * @return 200 OK with resource content
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder getInternalResource(ResearchObject researchObject, URI resource, String accept,
             String original, ResourceInfo resInfo)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String filePath = researchObject.getUri().relativize(resource).getPath();
 
         // check if request is for a specific format
         if (original != null) {
             URI originalResource = resource.resolve(original);
             filePath = researchObject.getUri().relativize(originalResource).getPath();
             if (ROSRService.SMS.get().containsNamedGraph(originalResource)) {
                 RDFFormat format = RDFFormat.forMIMEType(accept);
                 if (format == null) {
                     format = RDFFormat.forFileName(resource.getPath(), RDFFormat.RDFXML);
                 }
                 InputStream graph = ROSRService.SMS.get().getNamedGraph(originalResource, format);
                 ContentDisposition cd = ContentDisposition.type(format.getDefaultMIMEType())
                         .fileName(getFilename(resource)).build();
                 return Response.ok(graph).type(format.getDefaultMIMEType()).header("Content-disposition", cd);
             } else {
                 return Response.status(Status.NOT_FOUND).type("text/plain").entity("Original resource not found");
             }
         }
 
         if (ROSRService.SMS.get().containsNamedGraph(resource)) {
             RDFFormat acceptFormat = RDFFormat.forMIMEType(accept);
             RDFFormat extensionFormat = RDFFormat.forFileName(resource.getPath());
             if (extensionFormat != null && (acceptFormat == null || extensionFormat == acceptFormat)) {
                 // 1. GET manifest.rdf Accept: application/rdf+xml
                 // 2. GET manifest.rdf
                 InputStream graph = ROSRService.SMS.get().getNamedGraph(resource, extensionFormat);
                 ContentDisposition cd = ContentDisposition.type(extensionFormat.getDefaultMIMEType())
                         .fileName(getFilename(resource)).build();
                 return Response.ok(graph).type(extensionFormat.getDefaultMIMEType()).header("Content-disposition", cd);
             }
             // 3. GET manifest.rdf Accept: text/turtle
             // 4. GET manifest Accept: application/rdf+xml
             // 5. GET manifest
             URI formatSpecificURI = createFormatSpecificURI(resource, extensionFormat,
                 (acceptFormat != null ? acceptFormat : RDFFormat.RDFXML));
             return Response.temporaryRedirect(formatSpecificURI);
         }
 
         if (resInfo == null) {
             resInfo = getResourceInfo(researchObject, resource, original);
         }
         ContentDisposition cd = ContentDisposition.type(resInfo.getMimeType()).fileName(getFilename(resource)).build();
         InputStream body = ROSRService.DL.get().getFileContents(researchObject, filePath);
         return Response.ok(body).type(resInfo.getMimeType()).header("Content-disposition", cd);
     }
 
 
     /**
      * Take out the resource file name from a URI.
      * 
      * @param uri
      *            the URI
      * @return the last segment of the path
      */
     private static String getFilename(URI uri) {
         return uri.resolve(".").relativize(uri).toString();
     }
 
 
     /**
      * Create a URI pointing to a specific format of an RDF resource.
      * 
      * @param absolutePath
      *            resource path
      * @param extensionFormat
      *            original RDF format
      * @param newFormat
      *            the requested RDF format
      * @return URI pointing to the serialization of the resource in the requested RDF format
      */
     private static URI createFormatSpecificURI(URI absolutePath, RDFFormat extensionFormat, RDFFormat newFormat) {
         String path = absolutePath.getPath().toString();
         if (extensionFormat != null) {
             for (String extension : extensionFormat.getFileExtensions()) {
                 if (path.endsWith(extension)) {
                     path = path.substring(0, path.length() - extension.length() - 1);
                     break;
                 }
             }
         }
         path = path.concat(".").concat(newFormat.getDefaultFileExtension());
         try {
             return new URI(absolutePath.getScheme(), absolutePath.getAuthority(), path,
                     "original=".concat(getFilename(absolutePath)), null);
         } catch (URISyntaxException e) {
             LOGGER.error("Can't create a format-specific URI", e);
             return null;
         }
     }
 
 
     /**
      * An existing aggregated resource is being used as an annotation body now. Add it to the triplestore. If the
      * aggregated resource is external, do nothing.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            URI of the resource that is converted
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static void convertAggregatedResourceToAnnotationBody(ResearchObject researchObject, URI resource)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String filePath = researchObject.getUri().relativize(resource).getPath();
         InputStream data = ROSRService.DL.get().getFileContents(researchObject, filePath);
         if (data != null) {
             RDFFormat format = RDFFormat.forMIMEType(ROSRService.DL.get().getFileInfo(researchObject, filePath)
                     .getMimeType());
             ROSRService.SMS.get().removeResource(researchObject, resource);
             ROSRService.SMS.get().addAnnotationBody(researchObject, resource, data, format);
             // update the named graph copy in dLibra, the manifest is not changed
             updateNamedGraphInDlibra(filePath, researchObject, resource);
             updateROAttributesInDlibra(researchObject);
         }
     }
 
 
     /**
      * An aggregated resource is no longer an annotation body. Remove it from the triplestore. If the annotation body is
      * not inside the triplestore (i.e. it's an external resource), do nothing.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            URI of the resource that is converted
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     private static void convertAnnotationBodyToAggregatedResource(ResearchObject researchObject, URI resource)
             throws NotFoundException, DigitalLibraryException, AccessDeniedException {
         if (ROSRService.SMS.get().containsNamedGraph(resource)
                 && !SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
             ResourceInfo info = DL.get().getFileInfo(researchObject,
                 researchObject.getUri().relativize(resource).toString());
             ROSRService.SMS.get().removeAnnotationBody(researchObject, resource);
             ROSRService.SMS.get().addResource(researchObject, resource, info);
             updateROAttributesInDlibra(researchObject);
         }
     }
 
 
     /**
      * Add and aggregate a new annotation to the research object.
      * 
      * @param researchObject
      *            the research object
      * @param annotationBody
      *            annotation body URI
      * @param annotationTargets
      *            list of annotated resources URIs
      * @return 201 Created response
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response addAnnotation(ResearchObject researchObject, URI annotationBody, List<URI> annotationTargets)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI annotation = ROSRService.SMS.get().addAnnotation(researchObject, annotationTargets, annotationBody);
         // update the manifest that contains the annotation in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
 
        String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotationBody.toString(), AO.body);
         ResponseBuilder response = Response.created(annotation).header(Constants.LINK_HEADER, annotationBodyHeader);
         for (URI target : annotationTargets) {
             String targetHeader = String
                     .format(Constants.LINK_HEADER_TEMPLATE, target.toString(), AO.annotatesResource);
             response = response.header(Constants.LINK_HEADER, targetHeader);
         }
         return response.build();
     }
 
 
     /**
      * Update the annotation targets and the annotation body of an annotation. This method does not add/delete the
      * annotation body to the triplestore.
      * 
      * @param researchObject
      *            the research object
      * @param annotation
      *            annotation URI
      * @param annotationBody
      *            annotation body URI
      * @param annotationTargets
      *            list of annotated resources URIs
      * @return 200 OK response
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response updateAnnotation(ResearchObject researchObject, URI annotation, URI annotationBody,
             List<URI> annotationTargets)
             throws NotFoundException, DigitalLibraryException, AccessDeniedException {
         URI oldAnnotationBody = ROSRService.getAnnotationBody(researchObject, annotation, null);
         ROSRService.SMS.get().updateAnnotation(researchObject, annotation, annotationTargets, annotationBody);
 
         if (oldAnnotationBody == null || !oldAnnotationBody.equals(annotationBody)) {
             ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, oldAnnotationBody);
             if (ROSRService.SMS.get().isAggregatedResource(researchObject, annotationBody)) {
                 ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, annotationBody);
             }
         }
 
         String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotationBody.toString(),
             AO.annotatesResource);
         ResponseBuilder response = Response.ok().header(Constants.LINK_HEADER, annotationBodyHeader);
         for (URI target : annotationTargets) {
             String targetHeader = String
                     .format(Constants.LINK_HEADER_TEMPLATE, target.toString(), AO.annotatesResource);
             response = response.header(Constants.LINK_HEADER, targetHeader);
         }
         return response.build();
     }
 
 
     /**
      * Get the URI of an annotation body. If acceptHeader is not null, will return a format-specific URI.
      * 
      * @param researchObject
      *            research object in which the annotation is defined
      * @param annotation
      *            the annotation
      * @param acceptHeader
      *            MIME type requested (content negotiation) or null
      * @return URI of the annotation body
      * @throws NotFoundException
      *             could not find the resource in DL when checking if it's internal
      * @throws DigitalLibraryException
      *             could not connect to the DL to check if it's internal
      */
     public static URI getAnnotationBody(ResearchObject researchObject, URI annotation, String acceptHeader)
             throws NotFoundException, DigitalLibraryException {
         URI body = ROSRService.SMS.get().getAnnotationBody(researchObject, annotation);
         RDFFormat acceptFormat = RDFFormat.forMIMEType(acceptHeader);
         if (acceptFormat != null && isInternalResource(researchObject, body)) {
             RDFFormat extensionFormat = RDFFormat.forFileName(annotation.getPath());
             return createFormatSpecificURI(body, extensionFormat, acceptFormat);
         } else {
             return body;
         }
     }
 
 
     /**
      * Delete and deaggregate an annotation. Does not delete the annotation body.
      * 
      * @param researchObject
      *            research object in which the annotation is defined
      * @param annotation
      *            the annotation
      * @return 204 No Content
      * @throws NotFoundException
      *             could not find the resource in DL when checking if it's internal
      * @throws DigitalLibraryException
      *             could not connect to the DL to check if it's internal
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response deleteAnnotation(ResearchObject researchObject, URI annotation)
             throws NotFoundException, DigitalLibraryException, AccessDeniedException {
         URI annotationBody = ROSRService.getAnnotationBody(researchObject, annotation, null);
         ROSRService.SMS.get().deleteAnnotation(researchObject, annotation);
         ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, annotationBody);
         return Response.noContent().build();
     }
 
 
     /**
      * Create a new research object submitted in zip format.
      * 
      * @param freshResearchObjectURI
      *            the uri of created object
      * @param zip
      *            the zip file
      * @return HTTP response (created in case of success, 404 in case of error)
      * @throws BadRequestException .
      * @throws AccessDeniedException .
      * @throws IOException
      *             error creating the temporary file
      */
     public static Response createNewResearchObjectFromZip(URI freshResearchObjectURI, MemoryZipFile zip)
             throws BadRequestException, AccessDeniedException, IOException {
         URI createdResearchObjectURI;
         ResearchObject createdResearchObject;
         try {
             createdResearchObjectURI = createResearchObject(ResearchObject.create(freshResearchObjectURI));
             createdResearchObject = ResearchObject.create(createdResearchObjectURI);
         } catch (ConflictException | DigitalLibraryException | NotFoundException e) {
             throw new BadRequestException("Research Object creation problem", e);
         }
 
         SemanticMetadataService tmpSms = new SemanticMetadataServiceImpl(ROSRService.SMS.get().getUserProfile(),
                 createdResearchObject, zip.getManifestAsInputStream(), RDFFormat.RDFXML);
 
         List<AggregatedResource> aggregatedList;
         List<Annotation> annotationsList;
 
         try {
             aggregatedList = tmpSms.getAggregatedResources(createdResearchObject);
             annotationsList = tmpSms.getAnnotations(createdResearchObject);
         } catch (ManifestTraversingException e) {
             throw new BadRequestException("manifest is not correct", e);
         }
 
         for (AggregatedResource aggregated : aggregatedList) {
 
             String resourceName = createdResearchObjectURI.relativize(aggregated.getUri()).getPath();
             InputStream is = zip.getEntryAsStream(resourceName);
             UUID uuid = UUID.randomUUID();
             File tmpFile = File.createTempFile("tmp_resource", uuid.toString());
             try {
                 if (is != null) {
                     BufferedInputStream inputStream = new BufferedInputStream(is);
                     FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
                     IOUtils.copy(inputStream, fileOutputStream);
                     String mimeType = new MimetypesFileTypeMap().getContentType(tmpFile);
                     aggregateInternalResource(ResearchObject.create(createdResearchObjectURI),
                         createdResearchObjectURI.resolve(resourceName), new FileInputStream(tmpFile), mimeType, null);
                 } else {
                     aggregateExternalResource(ResearchObject.create(createdResearchObjectURI),
                         createdResearchObjectURI.resolve(resourceName));
                 }
             } catch (AccessDeniedException | DigitalLibraryException | NotFoundException e) {
                 e.printStackTrace();
             } finally {
                 tmpFile.delete();
             }
         }
         for (Annotation annotation : annotationsList) {
             try {
                 addAnnotation(ResearchObject.create(createdResearchObjectURI), annotation.getBody().getUri(),
                     annotation.getAnnotatedToURIList());
             } catch (DigitalLibraryException | NotFoundException e) {
                 LOGGER.error("Error when adding annotations", e);
             }
         }
 
         tmpSms.close();
         return Response.created(createdResearchObjectURI).build();
     }
 
 
     /**
      * Find out all annotations of the RO and store them in dLibra as attributes.
      * 
      * @param researchObject
      *            RO URI
      */
     private static void updateROAttributesInDlibra(ResearchObject researchObject) {
         Multimap<URI, Object> roAttributes = ROSRService.SMS.get().getAllAttributes(researchObject.getUri());
         roAttributes.put(URI.create("Identifier"), researchObject);
         try {
             ROSRService.DL.get().storeAttributes(researchObject, roAttributes);
         } catch (Exception e) {
             LOGGER.error("Caught an exception when updating RO attributes, will continue", e);
         }
     }
 
 
     /**
      * Take out an RDF graph from SMS and serialize it in dLibra with relative URI references.
      * 
      * @param filePath
      *            resource path
      * @param researchObject
      *            RO URI
      * @param namedGraphURI
      *            RDF graph URI
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     private static void updateNamedGraphInDlibra(String filePath, ResearchObject researchObject, URI namedGraphURI)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         RDFFormat format = RDFFormat.forFileName(filePath, RDFFormat.RDFXML);
         InputStream dataStream = ROSRService.SMS.get().getNamedGraphWithRelativeURIs(namedGraphURI, researchObject,
             format);
         ROSRService.DL.get().createOrUpdateFile(researchObject, filePath, dataStream, format.getDefaultMIMEType());
     }
 
 
     /**
      * Create a new ro:Folder with the specified folder entries.
      * 
      * @param researchObject
      *            research object
      * @param folder
      *            folder with folder entries
      * @return folder with all fields filled in
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Folder createFolder(ResearchObject researchObject, Folder folder)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         folder = SMS.get().addFolder(researchObject, folder);
         String path = researchObject.getUri().relativize(folder.getResourceMapUri()).toString();
         updateNamedGraphInDlibra(path, researchObject, folder.getResourceMapUri());
         return folder;
     }
 }
