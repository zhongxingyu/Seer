 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 
 import javax.naming.NamingException;
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.CacheControl;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
 import pl.psnc.dlibra.service.AccessDeniedException;
 import pl.psnc.dlibra.service.IdNotFoundException;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.sun.jersey.api.ConflictException;
 
 /**
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("ROs/{ro_id}/")
 public class ResearchObjectResource {
 
     private static final Logger logger = Logger.getLogger(ResearchObjectResource.class);
 
     private static final URI portalRoPage = URI.create("http://sandbox.wf4ever-project.org/portal/ro");
 
     @Context
     HttpServletRequest request;
 
     @Context
     UriInfo uriInfo;
 
 
     /**
      * Returns zip archive with contents of RO version.
      * 
      * @param researchObjectId
      *            RO identifier - defined by the user
      * @return
      * @throws UnknownHostException
      * @throws MalformedURLException
      * @throws RemoteException
      * @throws IOException
      * @throws DigitalLibraryException
      * @throws IdNotFoundException
      * @throws OperationNotSupportedException
      * @throws NotFoundException
      */
     @GET
     @Produces({ "application/zip", "multipart/related", "*/*" })
     public Response getZippedRO(@PathParam("ro_id") String researchObjectId)
             throws RemoteException, MalformedURLException, UnknownHostException, DigitalLibraryException,
             NotFoundException {
         return Response.seeOther(getZippedROURI(uriInfo.getBaseUriBuilder(), researchObjectId)).build();
     }
 
 
     @GET
     @Produces({ "application/rdf+xml", "application/x-turtle", "text/turtle", "application/x-trig", "application/trix",
             "text/rdf+n3" })
     public Response getROMetadata(@PathParam("ro_id") String researchObjectId)
             throws RemoteException, MalformedURLException, UnknownHostException, DigitalLibraryException,
             NotFoundException {
         return Response.seeOther(getROMetadataURI(uriInfo.getBaseUriBuilder(), researchObjectId)).build();
     }
 
 
     @GET
     @Produces({ MediaType.TEXT_HTML })
     public Response getROHtml(@PathParam("ro_id") String researchObjectId)
             throws URISyntaxException, RemoteException, MalformedURLException, UnknownHostException,
             DigitalLibraryException, NotFoundException {
         URI uri = getROHtmlURI(uriInfo.getBaseUriBuilder(), researchObjectId);
         return Response.seeOther(uri).build();
     }
 
 
     @DELETE
     public void deleteResearchObject(@PathParam("ro_id") String researchObjectId)
             throws DigitalLibraryException, ClassNotFoundException, IOException, NamingException, SQLException {
         ROSRService.deleteResearchObject(uriInfo.getAbsolutePath());
     }
 
 
     /**
      * Add a resource with no specific MIME type.
      * 
      * @param researchObjectId
      * @param content
      * @return
      * @throws BadRequestException
      *             annotation target is an incorrect URI
      * @throws NotFoundException
      * @throws DigitalLibraryException
      * @throws AccessDeniedException
      */
     @POST
     public Response addResource(@PathParam("ro_id") String researchObjectId, InputStream content)
             throws BadRequestException, AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI researchObject = uriInfo.getAbsolutePath();
         URI resource;
         if (request.getHeader(Constants.SLUG_HEADER) != null) {
             resource = uriInfo.getAbsolutePathBuilder().path(request.getHeader(Constants.SLUG_HEADER)).build();
         } else {
             resource = uriInfo.getAbsolutePathBuilder().path(UUID.randomUUID().toString()).build();
         }
         if (ROSRService.SMS.get().isAggregatedResource(researchObject, resource)) {
             throw new ConflictException("This resource has already been aggregated. Use PUT to update it.");
         }
 
         List<URI> annotationTargets = new ArrayList<>();
         for (@SuppressWarnings("unchecked")
         Enumeration<String> en = request.getHeaders(Constants.LINK_HEADER); en.hasMoreElements();) {
             Matcher m = Constants.AO_LINK_HEADER_PATTERN.matcher(en.nextElement());
             if (m.matches()) {
                 try {
                     annotationTargets.add(new URI(m.group(1)));
                 } catch (URISyntaxException e) {
                     throw new BadRequestException("Annotation target " + m.group(1) + " is incorrect", e);
                 }
             }
         }
 
         if (!annotationTargets.isEmpty()) {
             ROSRService.aggregateInternalResource(researchObject, resource, content, request.getContentType(), null);
             ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, resource);
             return ROSRService.addAnnotation(researchObject, resource, annotationTargets);
         } else {
             ResponseBuilder builder = ROSRService.aggregateInternalResource(researchObject, resource, content,
                 request.getContentType(), null);
             if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
                 ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, resource);
             }
             ResourceInfo resInfo = ROSRService.getResourceInfo(researchObject, resource, null);
             if (resInfo != null) {
                 CacheControl cache = new CacheControl();
                 cache.setMustRevalidate(true);
                 builder = builder.cacheControl(cache).tag(resInfo.getChecksum())
                         .lastModified(resInfo.getLastModified().toDate());
             }
             return builder.build();
         }
     }
 
 
     /**
      * 
      * @param researchObjectId
      * @param content
      * @return
      * @throws BadRequestException
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @POST
     @Consumes(Constants.PROXY_MIME_TYPE)
     public Response addProxy(@PathParam("ro_id") String researchObjectId, InputStream content)
             throws BadRequestException, AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI researchObject = uriInfo.getAbsolutePath();
         URI proxyFor;
         if (request.getHeader(Constants.SLUG_HEADER) != null) {
             // internal resource
             proxyFor = uriInfo.getAbsolutePathBuilder().path(request.getHeader(Constants.SLUG_HEADER)).build();
         } else {
             // external resource
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
             model.read(content, researchObject.toString());
             ExtendedIterator<Individual> it = model.listIndividuals(Constants.ORE_PROXY_CLASS);
             if (it.hasNext()) {
                 NodeIterator it2 = it.next().listPropertyValues(Constants.ORE_PROXY_FOR_PROPERTY);
                 if (it2.hasNext()) {
                     RDFNode proxyForResource = it2.next();
                     if (proxyForResource.isURIResource()) {
                         try {
                             proxyFor = new URI(proxyForResource.asResource().getURI());
                         } catch (URISyntaxException e) {
                             throw new BadRequestException("Wrong target resource URI", e);
                         }
                     } else {
                         throw new BadRequestException("The target is not an URI resource.");
                     }
                 } else {
                    throw new BadRequestException("The ore:Proxy does not have a ore:proxyFor property.");
                 }
             } else {
                 throw new BadRequestException("The entity body does not define any ore:Proxy.");
             }
         }
         return ROSRService.aggregateExternalResource(researchObject, proxyFor).build();
     }
 
 
     /**
      * 
      * @param researchObjectId
      * @param annotation
      * @return
      * @throws NotFoundException
      * @throws DigitalLibraryException
      * @throws AccessDeniedException
      * @throws BadRequestException
      */
     @POST
     @Consumes(Constants.ANNOTATION_MIME_TYPE)
     public Response addAnnotation(@PathParam("ro_id") String researchObjectId, InputStream content)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException, BadRequestException {
         URI researchObject = uriInfo.getAbsolutePath();
         URI body;
         List<URI> targets = new ArrayList<>();
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(content, researchObject.toString());
         ExtendedIterator<Individual> it = model.listIndividuals(Constants.RO_AGGREGATED_ANNOTATION_CLASS);
         if (it.hasNext()) {
             Individual aggregatedAnnotation = it.next();
             NodeIterator it2 = aggregatedAnnotation.listPropertyValues(Constants.AO_BODY_PROPERTY);
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
             it2 = aggregatedAnnotation.listPropertyValues(Constants.AO_ANNOTATES_RESOURCE_PROPERTY);
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
         if (ROSRService.SMS.get().isAggregatedResource(researchObject, body)) {
             ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, body);
         }
 
         return ROSRService.addAnnotation(researchObject, body, targets);
     }
 
 
     private static URI getZippedROURI(UriBuilder baseUriBuilder, String researchObjectId) {
         return baseUriBuilder.path("zippedROs").path(researchObjectId).path("/").build();
     }
 
 
     private static URI getROHtmlURI(UriBuilder baseUriBuilder, String researchObjectId)
             throws URISyntaxException {
         return new URI(portalRoPage.getScheme(), portalRoPage.getAuthority(), portalRoPage.getPath(), "ro="
                 + baseUriBuilder.path("ROs").path(researchObjectId).path("/").build().toString(), null);
     }
 
 
     private static URI getROMetadataURI(UriBuilder baseUriBuilder, String researchObjectId) {
         return baseUriBuilder.path("ROs").path(researchObjectId).path("/.ro/manifest.rdf").build();
     }
 
 
     public static ResponseBuilder addLinkHeaders(ResponseBuilder responseBuilder, UriInfo linkUriInfo,
             String researchObjectId) {
         try {
             return responseBuilder
                     .header(
                         Constants.LINK_HEADER,
                         String.format(Constants.LINK_HEADER_TEMPLATE,
                             getROHtmlURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"))
                     .header(
                         Constants.LINK_HEADER,
                         String.format(Constants.LINK_HEADER_TEMPLATE,
                             getZippedROURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"))
                     .header(
                         Constants.LINK_HEADER,
                         String.format(Constants.LINK_HEADER_TEMPLATE,
                             getROMetadataURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"));
         } catch (URISyntaxException e) {
             logger.error("Could not create RO Portal URI", e);
             return responseBuilder.header(
                 Constants.LINK_HEADER,
                 String.format(Constants.LINK_HEADER_TEMPLATE,
                     getZippedROURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark")).header(
                 Constants.LINK_HEADER,
                 String.format(Constants.LINK_HEADER_TEMPLATE,
                     getROMetadataURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"));
         }
     }
 }
