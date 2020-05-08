 package pl.psnc.dl.wf4ever.evo;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.common.HibernateUtil;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.rosrs.ROSRService;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * Copy one research object to another.
  * 
  * @author piotrekhol
  * 
  */
 public class CopyOperation implements Operation {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(CopyOperation.class);
 
     /** operation id. */
     private String id;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            operation id
      */
     public CopyOperation(String id) {
         this.id = id;
     }
 
 
     @Override
     public void execute(JobStatus status)
             throws OperationFailedException {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         try {
             URI target = status.getCopyfrom().resolve("../" + id + "/");
             int i = 1;
             String sufix = "";
            while (ROSRService.SMS.get().containsNamedGraph(target)) {
                 sufix = "-" + Integer.toString(i);
                 target = status.getCopyfrom().resolve("../" + id + "-" + (i++) + "/");
             }
             status.setTarget(id + sufix);
 
             ResearchObject targetRO = ResearchObject.create(target);
             ResearchObject sourceRO = ResearchObject.create(status.getCopyfrom());
 
             try {
                 ROSRService.createResearchObject(targetRO, status.getType(), sourceRO);
             } catch (ConflictException | DigitalLibraryException | NotFoundException | AccessDeniedException e) {
                 throw new OperationFailedException("Could not create the target research object", e);
             }
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sourceRO.getManifestUri().toString());
             Individual source = model.getIndividual(sourceRO.getUri().toString());
             if (source == null) {
                 throw new OperationFailedException("The manifest does not describe the research object");
             }
             List<RDFNode> aggregatedResources = source.listPropertyValues(ORE.aggregates).toList();
             Map<URI, URI> changedURIs = new HashMap<>();
             changedURIs.put(sourceRO.getManifestUri(), targetRO.getManifestUri());
             for (RDFNode aggregatedResource : aggregatedResources) {
                 if (Thread.interrupted()) {
                     try {
                         ROSRService.deleteResearchObject(targetRO);
                     } catch (DigitalLibraryException | NotFoundException e) {
                         LOGGER.error("Could not delete the target when aborting: " + target, e);
                     }
                     return;
                 }
                 if (!aggregatedResource.isURIResource()) {
                     LOGGER.warn("Aggregated node " + aggregatedResource.toString() + " is not a URI resource");
                     continue;
                 }
                 Individual resource = aggregatedResource.as(Individual.class);
                 URI resourceURI = URI.create(resource.getURI());
                 if (resource.hasRDFType(RO.AggregatedAnnotation)) {
                     Resource annBody = resource.getPropertyResourceValue(AO.body);
                     List<URI> targets = new ArrayList<>();
                     List<RDFNode> annotationTargets = resource.listPropertyValues(RO.annotatesAggregatedResource)
                             .toList();
                     for (RDFNode annTarget : annotationTargets) {
                         if (!annTarget.isURIResource()) {
                             LOGGER.warn("Annotation target " + annTarget.toString() + " is not a URI resource");
                             continue;
                         }
                         targets.add(URI.create(annTarget.asResource().getURI()));
                     }
                     try {
                         //FIXME use a dedicated class for an Annotation
                         String[] segments = resource.getURI().split("/");
                         if (segments.length > 0) {
                             ROSRService.addAnnotation(targetRO, URI.create(annBody.getURI()), targets,
                                 segments[segments.length - 1]);
                         } else {
                             ROSRService.addAnnotation(targetRO, URI.create(annBody.getURI()), targets);
                         }
                     } catch (AccessDeniedException | DigitalLibraryException | NotFoundException e1) {
                         LOGGER.error("Could not add the annotation", e1);
                     }
                 } else {
                     if (isInternalResource(resourceURI, status.getCopyfrom())) {
                         try {
                             Client client = Client.create();
                             WebResource webResource = client.resource(resourceURI.toString());
                             ClientResponse response = webResource.get(ClientResponse.class);
                             URI resourcePath = status.getCopyfrom().relativize(resourceURI);
                             URI targetURI = target.resolve(resourcePath);
                             ROSRService.aggregateInternalResource(targetRO, targetURI, response.getEntityInputStream(),
                                 response.getType().toString(), null);
                             //TODO improve resource type detection mechanism!!
                             if (!resource.hasRDFType(RO.Resource)) {
                                 ROSRService.convertAggregatedResourceToAnnotationBody(targetRO, targetURI);
                             }
                             changedURIs.put(resourceURI, targetURI);
                         } catch (AccessDeniedException | DigitalLibraryException | NotFoundException e) {
                             throw new OperationFailedException("Could not create aggregate internal resource: "
                                     + resourceURI, e);
                         }
                     } else {
                         try {
                             ROSRService.aggregateExternalResource(targetRO, resourceURI);
                         } catch (AccessDeniedException | DigitalLibraryException | NotFoundException e) {
                             throw new OperationFailedException("Could not create aggregate external resource: "
                                     + resourceURI, e);
                         }
                     }
                 }
             }
             for (Map.Entry<URI, URI> e : changedURIs.entrySet()) {
                 ROSRService.SMS.get().changeURIInManifestAndAnnotationBodies(targetRO, e.getKey(), e.getValue(), false);
             }
             for (Map.Entry<URI, URI> e : changedURIs.entrySet()) {
                 ROSRService.SMS.get().changeURIInManifestAndAnnotationBodies(targetRO, e.getKey(), e.getValue(), true);
             }
         } finally {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         }
 
     }
 
 
     /**
      * Small hack done for manifest annotation problem. If the annotation is the manifest annotation the the target must
      * be changed.
      * 
      * @param status
      * @param targets
      * @param annBodyUri
      * @return
      */
     private List<URI> changeManifestAnnotationTarget(JobStatus status, List<URI> targets, URI annBodyUri) {
         List<URI> results = new ArrayList<URI>();
         if (status.getCopyfrom() != null && annBodyUri != null && annBodyUri.toString().contains("manifest.rdf")) {
             for (URI t : targets) {
                 if (t.toString().equals(status.getCopyfrom().toString())) {
                     results.add(status.getCopyfrom().resolve("../" + status.getTarget()));
                 } else {
                     results.add(t);
                 }
             }
         }
         return results;
     }
 
 
     /**
      * Check if a resource is internal to the RO. This will not work if they have different domains, for example if the
      * RO URI uses purl.
      * 
      * @param resource
      *            resource URI
      * @param ro
      *            RO URI
      * @return true if the resource URI starts with the RO URI, false otherwise
      */
     private boolean isInternalResource(URI resource, URI ro) {
         return resource.normalize().toString().startsWith(ro.normalize().toString());
     }
 
 }
