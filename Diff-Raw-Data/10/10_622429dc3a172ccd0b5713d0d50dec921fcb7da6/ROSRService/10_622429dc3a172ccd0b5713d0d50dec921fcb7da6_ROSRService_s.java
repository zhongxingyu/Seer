 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.common.EvoType;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
 import pl.psnc.dl.wf4ever.exceptions.ManifestTraversingException;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
 import pl.psnc.dl.wf4ever.model.RO.Resource;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceTdb;
 import pl.psnc.dl.wf4ever.utils.zip.MemoryZipFile;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
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
                 type = EvoType.LIVE;
             }
             switch (type) {
                 default:
                 case LIVE:
                     ROSRService.SMS.get().createLiveResearchObject(researchObject, sourceRO);
                     break;
                 case SNAPSHOT:
                     ROSRService.SMS.get().createSnapshotResearchObject(researchObject, sourceRO);
                     break;
                 case ARCHIVE:
                     ROSRService.SMS.get().createArchivedResearchObject(researchObject, sourceRO);
                     break;
             }
 
             manifest = ROSRService.SMS.get().getManifest(researchObject, RDFFormat.RDFXML);
         } catch (IllegalArgumentException e) {
             // RO already existed in sms, maybe created by someone else
             throw new ConflictException("The RO with URI " + researchObject.getUri() + " already exists");
         }
 
         LOGGER.debug(String.format("%s\t\tcreate RO start", new DateTime().toString()));
         ROSRService.DL.get().createResearchObject(researchObject.getUri(), manifest, ResearchObject.MANIFEST_PATH,
             RDFFormat.RDFXML.getDefaultMIMEType());
         if (type == EvoType.LIVE) {
             generateEvoInfo(researchObject, null, EvoType.LIVE);
         }
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
             ROSRService.DL.get().deleteResearchObject(researchObject.getUri());
         } finally {
             try {
                 ROSRService.SMS.get().removeResearchObject(researchObject);
             } catch (IllegalArgumentException e) {
                 LOGGER.warn("URI not found in SMS: " + researchObject.getUri());
             }
         }
     }
 
 
     /**
      * Aggregate a new internal resource in the research object, upload the resource and add a proxy.
      * 
      * @param researchObject
      *            the research object
      * @param resourceUri
      *            resource URI, must be internal
      * @param entity
      *            request entity
      * @param contentType
      *            resource content type
      * @param original
      *            original resource in case of using a format specific URI
      * @param syntax
      *            response RDF format, default is RDF/XML
      * @return 201 Created with proxy URI
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Resource aggregateInternalResource(ResearchObject researchObject, URI resourceUri,
             InputStream entity, String contentType, String original)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         if (original != null) {
             resourceUri = resourceUri.resolve(original);
         }
         String filePath = researchObject.getUri().relativize(resourceUri).getPath();
         ResourceMetadata resourceInfo = ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), filePath,
             entity, contentType != null ? contentType : "text/plain");
         Resource resource = ROSRService.SMS.get().addResource(researchObject, resourceUri, resourceInfo);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         URI proxy = ROSRService.SMS.get().addProxy(researchObject, resourceUri);
         resource.setProxyUri(proxy);
         return resource;
     }
 
 
     /**
      * Aggregate a resource in the research object without uploading its content and add a proxy.
      * 
      * @param researchObject
      *            research object
      * @param resource
      *            resource that will be aggregated
      * @param syntax
      *            format in which the response should be returned
      * @return 201 Created response pointing to the proxy
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder aggregateExternalResource(ResearchObject researchObject, URI resource,
             RDFFormat syntax)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         ROSRService.SMS.get().addResource(researchObject, resource, null);
         ResponseBuilder builder = addProxy(researchObject, resource, syntax);
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
         ROSRService.DL.get().deleteFile(researchObject.getUri(), filePath);
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
         return !filePath.isEmpty() && ROSRService.DL.get().fileExists(researchObject.getUri(), filePath);
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
      * @param syntax
      *            format in which the response should be returned, default is RDF/XML
      * @return 201 Created response pointing to the proxy
      */
     private static ResponseBuilder addProxy(ResearchObject researchObject, URI proxyFor, RDFFormat syntax) {
         if (syntax == null) {
             syntax = RDFFormat.RDFXML;
         }
         URI proxy = ROSRService.SMS.get().addProxy(researchObject, proxyFor);
         String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, proxyFor.toString(),
             Constants.ORE_PROXY_FOR_HEADER);
         InputStream proxyDesc = SMS.get().getResource(researchObject, syntax, proxy);
         return Response.created(proxy).entity(proxyDesc).type(syntax.getDefaultMIMEType())
                 .header(Constants.LINK_HEADER, proxyForHeader);
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
         boolean contentExisted = ROSRService.DL.get().fileExists(researchObject.getUri(), filePath);
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
             ResourceMetadata resourceInfo = ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), filePath,
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
     public static ResourceMetadata getResourceInfo(ResearchObject researchObject, URI resource, String original)
             throws AccessDeniedException, NotFoundException, DigitalLibraryException {
         String filePath = researchObject.getUri().relativize(original != null ? resource.resolve(original) : resource)
                 .getPath();
         return ROSRService.DL.get().getFileInfo(researchObject.getUri(), filePath);
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
             String original, ResourceMetadata resInfo)
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
         if (resInfo == null) {
             throw new NotFoundException("Resource not found: " + resource);
         }
         InputStream body = ROSRService.DL.get().getFileContents(researchObject.getUri(), filePath);
         ContentDisposition cd = ContentDisposition.type(resInfo.getMimeType()).fileName(getFilename(resource)).build();
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
      * @return
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static AggregatedResource convertRoResourceToAnnotationBody(ResearchObject researchObject, URI resource)
             throws DigitalLibraryException, AccessDeniedException {
         String filePath = researchObject.getUri().relativize(resource).getPath();
         try {
             InputStream data = ROSRService.DL.get().getFileContents(researchObject.getUri(), filePath);
             if (data != null) {
                 RDFFormat format = RDFFormat.forMIMEType(ROSRService.DL.get()
                         .getFileInfo(researchObject.getUri(), filePath).getMimeType());
                 SMS.get().removeResource(researchObject, resource);
                 AggregatedResource res = SMS.get().addAnnotationBody(researchObject, resource, data, format);
                 res.setProxyUri(SMS.get().addProxy(researchObject, resource));
                 // update the named graph copy in dLibra, the manifest is not changed
                 updateNamedGraphInDlibra(filePath, researchObject, resource);
                 updateROAttributesInDlibra(researchObject);
                 return res;
             } else {
                 return null;
             }
         } catch (NotFoundException e) {
             LOGGER.debug("Could not find an aggregated resource, must be external: " + e.getMessage());
             return null;
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
             ResourceMetadata info = DL.get().getFileInfo(researchObject.getUri(),
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
     public static Annotation addAnnotation(ResearchObject researchObject, URI annotationBody,
             List<URI> annotationTargets)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         Annotation annotation = ROSRService.SMS.get().addAnnotation(researchObject, annotationTargets, annotationBody);
         // update the manifest that contains the annotation in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         return annotation;
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
      * @param annotationPrefix
      *            the prefix of annotation URI
      * @return 201 Created response
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response addAnnotation(ResearchObject researchObject, URI annotationBody,
             List<URI> annotationTargets, String annotationPrefix)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         Annotation annotation = ROSRService.SMS.get().addAnnotation(researchObject, annotationTargets, annotationBody,
             annotationPrefix);
         // update the manifest that contains the annotation in dLibra
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
 
         String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotationBody.toString(), AO.body);
         ResponseBuilder response = Response.created(annotation.getUri()).header(Constants.LINK_HEADER,
             annotationBodyHeader);
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
     public static Response updateAnnotation(ResearchObject researchObject, URI annotationUri, URI annotationBody,
             List<URI> annotationTargets)
             throws NotFoundException, DigitalLibraryException, AccessDeniedException {
         URI oldAnnotationBody = ROSRService.getAnnotationBody(researchObject, annotationUri, null);
         ROSRService.SMS.get().updateAnnotation(researchObject,
             new Annotation(annotationUri, annotationTargets, annotationBody));
 
         if (oldAnnotationBody == null || !oldAnnotationBody.equals(annotationBody)) {
             ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, oldAnnotationBody);
             if (ROSRService.SMS.get().isAggregatedResource(researchObject, annotationBody)) {
                 ROSRService.convertRoResourceToAnnotationBody(researchObject, annotationBody);
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
      * @param annotationUri
      *            the annotation
      * @param acceptHeader
      *            MIME type requested (content negotiation) or null
      * @return URI of the annotation body
      * @throws NotFoundException
      *             could not find the resource in DL when checking if it's internal
      * @throws DigitalLibraryException
      *             could not connect to the DL to check if it's internal
      */
     public static URI getAnnotationBody(ResearchObject researchObject, URI annotationUri, String acceptHeader)
             throws NotFoundException, DigitalLibraryException {
         Annotation annotation = ROSRService.SMS.get().getAnnotation(researchObject, annotationUri);
         RDFFormat acceptFormat = RDFFormat.forMIMEType(acceptHeader);
         if (acceptFormat != null && isInternalResource(researchObject, annotation.getBody())) {
             RDFFormat extensionFormat = RDFFormat.forFileName(annotation.getUri().getPath());
             return createFormatSpecificURI(annotation.getBody(), extensionFormat, acceptFormat);
         } else {
             return annotation.getBody();
         }
     }
 
 
     /**
      * Delete and deaggregate an annotation. Does not delete the annotation body.
      * 
      * @param researchObject
      *            research object in which the annotation is defined
      * @param annotationUri
      *            the annotation URI
      * @return 204 No Content
      * @throws NotFoundException
      *             could not find the resource in DL when checking if it's internal
      * @throws DigitalLibraryException
      *             could not connect to the DL to check if it's internal
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response deleteAnnotation(ResearchObject researchObject, URI annotationUri)
             throws NotFoundException, DigitalLibraryException, AccessDeniedException {
         Annotation annotation = ROSRService.SMS.get().getAnnotation(researchObject, annotationUri);
         ROSRService.SMS.get().deleteAnnotation(researchObject, annotation);
         ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, annotation.getBody());
         return Response.noContent().build();
     }
 
 
     /**
      * Create a new research object submitted in ZIP format.
      * 
      * @param researchObject
      *            the new research object
      * @param zip
      *            the ZIP file
      * @return HTTP response (created in case of success, 404 in case of error)
      * @throws BadRequestException .
      * @throws AccessDeniedException .
      * @throws IOException
      *             error creating the temporary file
      */
     public static Response createNewResearchObjectFromZip(ResearchObject researchObject, MemoryZipFile zip)
             throws BadRequestException, AccessDeniedException, IOException {
         try {
             URI createdResearchObjectURI = createResearchObject(researchObject);
             researchObject = ResearchObject.create(createdResearchObjectURI);
         } catch (ConflictException | DigitalLibraryException | NotFoundException e) {
             throw new BadRequestException("Research Object creation problem", e);
         }
 
         SemanticMetadataService tmpSms = new SemanticMetadataServiceTdb(ROSRService.SMS.get().getUserProfile(),
                 researchObject, zip.getManifestAsInputStream(), RDFFormat.RDFXML);
 
         List<AggregatedResource> aggregatedList;
         List<Annotation> annotationsList;
 
         try {
             aggregatedList = tmpSms.getAggregatedResources(researchObject);
             annotationsList = tmpSms.getAnnotations(researchObject);
             aggregatedList = tmpSms.removeSpecialFilesFromAggergated(aggregatedList);
             annotationsList = tmpSms.removeSpecialFilesFromAnnotatios(annotationsList);
 
         } catch (ManifestTraversingException e) {
             throw new BadRequestException(e.getMessage(), e);
         }
 
         InputStream mimeTypesIs = ROSRService.class.getClassLoader().getResourceAsStream("mime.types");
         MimetypesFileTypeMap mfm = new MimetypesFileTypeMap(mimeTypesIs);
         mimeTypesIs.close();
         for (AggregatedResource aggregated : aggregatedList) {
             String originalResourceName = researchObject.getUri().relativize(aggregated.getUri()).getPath();
             URI resourceURI = UriBuilder.fromUri(researchObject.getUri()).path(originalResourceName).build();
             UUID uuid = UUID.randomUUID();
             File tmpFile = File.createTempFile("tmp_resource", uuid.toString());
             try {
                 InputStream is = zip.getEntryAsStream(originalResourceName);
                 if (is != null) {
                     try {
                         FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
                         IOUtils.copy(is, fileOutputStream);
                         String mimeType = mfm.getContentType(resourceURI.getPath());
                         aggregateInternalResource(researchObject, resourceURI, new FileInputStream(tmpFile), mimeType,
                             null);
                     } finally {
                         is.close();
                     }
                 } else {
                     aggregateExternalResource(researchObject, aggregated.getUri(), null);
                 }
             } catch (AccessDeniedException | DigitalLibraryException | NotFoundException e) {
                 LOGGER.error("Error when aggregating resources", e);
             } finally {
                 tmpFile.delete();
             }
         }
         for (Annotation annotation : annotationsList) {
             try {
                 if (SMS.get().isAggregatedResource(researchObject, annotation.getBody())) {
                     convertRoResourceToAnnotationBody(researchObject, annotation.getBody());
                 }
                 addAnnotation(researchObject, annotation.getBody(), annotation.getAnnotated());
             } catch (DigitalLibraryException | NotFoundException e) {
                 LOGGER.error("Error when adding annotations", e);
             }
         }
 
         tmpSms.close();
         return Response.created(researchObject.getUri()).build();
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
             ROSRService.DL.get().storeAttributes(researchObject.getUri(), roAttributes);
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
         ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), filePath, dataStream,
             format.getDefaultMIMEType());
     }
 
 
     /**
      * Create a folder instance out of an RDF/XML description.
      * 
      * @param researchObject
      *            research object used as RDF base
      * @param folderURI
      *            folder URI
      * @param content
      *            RDF/XML folder description
      * @return a folder instance
      * @throws BadRequestException
      *             the folder description is incorrect
      */
     public static Folder assembleFolder(ResearchObject researchObject, URI folderURI, InputStream content)
             throws BadRequestException {
         Folder folder = new Folder();
         folder.setUri(folderURI);
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(content, researchObject.getUri().toString());
         List<Individual> folders = model.listIndividuals(RO.Folder).toList();
         Map<URI, FolderEntry> entries = new HashMap<>();
         if (folders.size() == 1) {
             Individual folderInd = folders.get(0);
             List<RDFNode> aggregatedResources = folderInd.listPropertyValues(ORE.aggregates).toList();
             for (RDFNode aggregatedResource : aggregatedResources) {
                 if (aggregatedResource.isURIResource()) {
                     try {
                         URI res = new URI(aggregatedResource.asResource().getURI());
                         entries.put(res, new FolderEntry(res, null));
                     } catch (URISyntaxException e) {
                         throw new BadRequestException("Aggregated resource has an incorrect URI", e);
                     }
                 } else {
                     throw new BadRequestException("Aggregated resources cannot be blank nodes.");
                 }
             }
         } else {
             throw new BadRequestException("The entity body must define exactly one ro:Folder.");
         }
         List<Individual> folderEntries = model.listIndividuals(RO.FolderEntry).toList();
         for (Individual folderEntryInd : folderEntries) {
             URI proxyFor;
             if (folderEntryInd.hasProperty(ORE.proxyFor)) {
                 RDFNode proxyForNode = folderEntryInd.getPropertyValue(ORE.proxyFor);
                 if (!proxyForNode.isURIResource()) {
                     throw new BadRequestException("Folder entry ore:proxyFor must be a URI resource.");
                 }
                 try {
                     proxyFor = new URI(proxyForNode.asResource().getURI());
                 } catch (URISyntaxException e) {
                     throw new BadRequestException("Folder entry proxy for URI is incorrect", e);
                 }
             } else {
                 throw new BadRequestException("Folder entries must have the ore:proxyFor property.");
             }
             if (!entries.containsKey(proxyFor)) {
                 throw new BadRequestException(
                         "Found a folder entry for a resource that is not aggregated by the folder");
             }
             FolderEntry folderEntry = entries.get(proxyFor);
             if (folderEntryInd.hasProperty(RO.entryName)) {
                 RDFNode nameNode = folderEntryInd.getPropertyValue(RO.entryName);
                 if (!nameNode.isLiteral()) {
                     throw new BadRequestException("Folder entry ro name must be a literal");
                 }
                 folderEntry.setEntryName(nameNode.asLiteral().toString());
             }
         }
         for (FolderEntry folderEntry : entries.values()) {
             if (folderEntry.getEntryName() == null) {
                 if (folderEntry.getProxyFor().getPath() != null) {
                     String[] segments = folderEntry.getProxyFor().getPath().split("/");
                     folderEntry.setEntryName(segments[segments.length - 1]);
                 } else {
                     folderEntry.setEntryName(folderEntry.getProxyFor().getHost());
                 }
             }
         }
         folder.setFolderEntries(new ArrayList<>(entries.values()));
         return folder;
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
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         return folder;
     }
 
 
     public static void generateEvoInfo(ResearchObject researchObject, ResearchObject parent, EvoType type)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         SMS.get().generateEvoInformation(researchObject, parent, type);
         updateNamedGraphInDlibra(researchObject.getUri()
                 .relativize(researchObject.getFixedEvolutionAnnotationBodyUri()).toString(), researchObject,
             researchObject.getFixedEvolutionAnnotationBodyUri());
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
         if (parent != null) {
             updateNamedGraphInDlibra(
                 parent.getUri().relativize(parent.getFixedEvolutionAnnotationBodyUri()).toString(), parent,
                 parent.getFixedEvolutionAnnotationBodyUri());
             updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, parent, parent.getManifestUri());
         }
     }
 
 
     /**
      * Create a folder entry instance out of an RDF/XML description.
      * 
      * @param folder
      *            folder used as RDF base
      * @param content
      *            RDF/XML folder description
      * @return a folder instance
      * @throws BadRequestException
      *             the folder description is incorrect
      */
     public static FolderEntry assembleFolderEntry(Folder folder, InputStream content)
             throws BadRequestException {
         FolderEntry entry = new FolderEntry();
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(content, folder.getUri().toString());
         ExtendedIterator<Individual> it = model.listIndividuals(RO.FolderEntry);
         if (it.hasNext()) {
             Individual entryI = it.next();
             NodeIterator it2 = entryI.listPropertyValues(ORE.proxyFor);
             if (it2.hasNext()) {
                 RDFNode proxyForResource = it2.next();
                 if (proxyForResource.isURIResource()) {
                     try {
                         entry.setProxyFor(new URI(proxyForResource.asResource().getURI()));
                     } catch (URISyntaxException e) {
                         throw new BadRequestException("Wrong ore:proxyFor URI", e);
                     }
                 } else {
                     throw new BadRequestException("The ore:proxyFor object is not an URI resource.");
                 }
             } else {
                 throw new BadRequestException("ore:proxyFor is missing.");
             }
             RDFNode entryName = entryI.getPropertyValue(RO.entryName);
             if (entryName == null) {
                 entry.setEntryName(FolderEntry.generateEntryName(entry.getProxyFor()));
             } else {
                 entry.setEntryName(entryName.asLiteral().getString());
             }
         } else {
             throw new BadRequestException("The entity body does not define any ro:FolderEntry.");
         }
         return entry;
     }
 
 
     /**
      * Update a folder.
      * 
      * @param folder
      *            the folder
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static void updateFolder(Folder folder)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         ROSRService.SMS.get().updateFolder(folder);
         ResearchObject researchObject = ResearchObject.create(folder.getAggregationUri());
         updateNamedGraphInDlibra(researchObject.getUri().relativize(folder.getResourceMapUri()).toString(),
             researchObject, folder.getResourceMapUri());
     }
 
 
     /**
      * Delete the folder and update the manifest.
      * 
      * @param folder
      *            folder
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static void deleteFolder(Folder folder)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         ResearchObject researchObject = ResearchObject.create(folder.getAggregationUri());
         String filePath = researchObject.getUri().relativize(folder.getResourceMapUri()).getPath();
         ROSRService.DL.get().deleteFile(researchObject.getUri(), filePath);
         ROSRService.SMS.get().deleteFolder(folder);
         updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
     }
 
 
     /**
      * Delete a folder entry.
      * 
      * @param entry
      *            folder entry
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static void deleteFolderEntry(FolderEntry entry)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         Folder folder = ROSRService.SMS.get().getFolder(entry.getProxyIn());
         folder.getFolderEntries().remove(entry);
         updateFolder(folder);
     }
 
 }
