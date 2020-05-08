 package pl.psnc.dl.wf4ever.evo;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.dlibra.ConflictException;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.rosrs.ROSRService;
 import pl.psnc.dlibra.service.AccessDeniedException;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.ontology.OntProperty;
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
         URI target = status.getCopyfrom().resolve("../" + id + "/");
         int i = 1;
         while (ROSRService.SMS.get().containsNamedGraph(target)) {
             target = status.getCopyfrom().resolve("../" + id + "-" + (i++) + "/");
         }
         status.setTarget(target);
 
         ResearchObject targetRO = ResearchObject.findByUri(target);
         ResearchObject sourceRO = ResearchObject.findByUri(status.getCopyfrom());
 
         try {
             ROSRService.createResearchObject(targetRO, status.getType(), sourceRO);
         } catch (ConflictException | DigitalLibraryException | NotFoundException e) {
             throw new OperationFailedException("Could not create the target research object", e);
         }
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(sourceRO.getManifestUri().toString());
         Individual source = model.getIndividual(sourceRO.getUri().toString());
         if (source == null) {
             throw new OperationFailedException("The manifest does not describe the research object");
         }
         OntProperty aggregates = model.createOntProperty("http://www.openarchives.org/ore/terms/aggregates");
         List<RDFNode> aggregatedResources = source.listPropertyValues(aggregates).toList();
         Map<URI, URI> changedURIs = new HashMap<>();
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
             if (resource.hasRDFType("http://purl.org/wf4ever/ro#Resource")) {
                 if (isInternalResource(resourceURI, status.getCopyfrom())) {
                     try {
                         Client client = Client.create();
                         WebResource webResource = client.resource(resourceURI.toString());
                         ClientResponse response = webResource.get(ClientResponse.class);
                         URI resourcePath = status.getCopyfrom().relativize(resourceURI);
                         URI targetURI = target.resolve(resourcePath);
                         ROSRService.aggregateInternalResource(targetRO, targetURI, response.getEntityInputStream(),
                             response.getType().toString(), null);
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
             } else if (resource.hasRDFType("http://purl.org/wf4ever/ro#AggregatedAnnotation")) {
                 OntProperty annotates = model
                         .createOntProperty("http://purl.org/wf4ever/ro#annotatesAggregatedResource");
                 OntProperty body = model.createOntProperty("http://purl.org/ao/body");
                 Resource annBody = resource.getPropertyResourceValue(body);
                 List<URI> targets = new ArrayList<>();
                 List<RDFNode> annotationTargets = resource.listPropertyValues(annotates).toList();
                 for (RDFNode annTarget : annotationTargets) {
                     if (!annTarget.isURIResource()) {
                         LOGGER.warn("Annotation target " + annTarget.toString() + " is not a URI resource");
                         continue;
                     }
                     targets.add(URI.create(annTarget.asResource().getURI()));
                 }
                ROSRService.addAnnotation(targetRO, URI.create(annBody.getURI()), targets);
             }
         }
         for (Map.Entry<URI, URI> e : changedURIs.entrySet()) {
             ROSRService.SMS.get().changeURIInManifestAndAnnotationBodies(targetRO, e.getKey(), e.getValue());
         }
         try {
             storeHistoryInformation(status.getTarget());
         } catch (URISyntaxException e) {
             //@TODO thing about the exception handling
             e.printStackTrace();
         }
     }
 
 
     /**
      * Generate RO evolution history of an RO and store it.
      * 
      * @param freshObjectURI
      *            RO URI
      * @throws URISyntaxException
      *             some URI in SMS is incorrect
      */
     private void storeHistoryInformation(URI freshObjectURI)
             throws URISyntaxException {
         URI liveObjectURI = ROSRService.SMS.get().getLiveURIFromSnapshotOrArchive(freshObjectURI);
         URI antecessorObjectURI = ROSRService.SMS.get().getPreviousSnaphotOrArchive(liveObjectURI, freshObjectURI);
         ROSRService.SMS.get().storeAggregatedDifferences(freshObjectURI, antecessorObjectURI);
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
