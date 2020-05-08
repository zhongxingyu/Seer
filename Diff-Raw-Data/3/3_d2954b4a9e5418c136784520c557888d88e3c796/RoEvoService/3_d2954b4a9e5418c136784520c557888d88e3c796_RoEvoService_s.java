 package pl.psnc.dl.wf4ever.portal.services;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.purl.wf4ever.rosrs.client.common.Vocab;
 
 import pl.psnc.dl.wf4ever.portal.model.RoEvoNode;
 import pl.psnc.dl.wf4ever.portal.model.RoEvoNode.EvoClass;
 import pl.psnc.dl.wf4ever.portal.model.RoEvoNode.EvoClassModifier;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * RoEvo description generation service.
  * 
  * @author piotrekhol
  * 
  */
 public final class RoEvoService {
 
     /**
      * Constructor.
      */
     private RoEvoService() {
         // nope
     }
 
 
     /**
      * Generate a description of a RO.
      * 
      * @param sparqlEndpointURI
      *            RODL SPARQL endpoint URI
      * @param researchObjectURI
      *            RO URI
      * @return a collection of {@link RoEvoNode}
      * @throws IOException
      *             can't connect to RODL
      * @throws URISyntaxException
      *             the URIs in the SPARQL response are incorrect
      */
     public static Collection<RoEvoNode> describeRO(URI sparqlEndpointURI, URI researchObjectURI)
             throws IOException, URISyntaxException {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         QueryExecution x = QueryExecutionFactory.sparqlService(sparqlEndpointURI.toString(),
             MyQueryFactory.getResourceClass(researchObjectURI.toString()));
         x.execConstruct(model);
 
         Query query = null;
         Individual i = model.getIndividual(researchObjectURI.toString());
         if (i != null) {
             if (i.hasRDFType(Vocab.ROEVO_SNAPSHOT_RO)) {
                 query = MyQueryFactory.getSnapshotEvolution(researchObjectURI.toString());
             } else if (i.hasRDFType(Vocab.ROEVO_LIVE_RO)) {
                 query = MyQueryFactory.getLiveEvolution(researchObjectURI.toString());
             } else if (i.hasRDFType(Vocab.ROEVO_ARCHIVED_RO)) {
                 query = MyQueryFactory.getArchivedEvolution(researchObjectURI.toString());
             }
         }
         if (query == null) {
             query = MyQueryFactory.getSnapshotEvolution(researchObjectURI.toString());
         }
 
         x = QueryExecutionFactory.sparqlService(sparqlEndpointURI.toString(), query);
         x.execConstruct(model);
 
         Map<URI, RoEvoNode> nodes = new HashMap<>();
         StmtIterator it = model.listStatements();
         while (it.hasNext()) {
             Statement statement = it.next();
             URI subjectURI = new URI(statement.getSubject().getURI());
             if (!nodes.containsKey(subjectURI)) {
                 nodes.put(subjectURI, new RoEvoNode(subjectURI));
             }
             RoEvoNode node = nodes.get(subjectURI);
             Property property = statement.getPredicate();
             RDFNode object = statement.getObject();
             if (property.equals(RDFS.label)) {
                 node.setLabel(object.asLiteral().getString());
            } else if (property.equals(Vocab.ROEVO_IS_SNAPSHOT_OF) && object.isURIResource()) {
                 node.getItsLiveROs().add(createNode(nodes, object.asResource().getURI()));
             } else if (property.equals(Vocab.ROEVO_HAS_PREVIOUS_VERSION) && object.isURIResource()) {
                 node.getPreviousSnapshots().add(createNode(nodes, object.asResource().getURI()));
             } else if (property.equals(Vocab.ROEVO_DERIVED_FROM) && object.isURIResource()) {
                 RoEvoNode source = createNode(nodes, object.asResource().getURI());
                 node.getDerivedResources().add(source);
                 source.setEvoClassModifier(EvoClassModifier.SOURCE);
                 node.setEvoClassModifier(EvoClassModifier.FORK);
             } else if (property.equals(RDF.type)) {
                 if (object.equals(Vocab.RO_RESEARCH_OBJECT)) {
                     node.setResearchObject(true);
                 } else if (object.equals(Vocab.ROEVO_SNAPSHOT_RO)) {
                     node.setResearchObject(true);
                     node.setEvoClass(EvoClass.SNAPSHOT);
                 } else if (object.equals(Vocab.ROEVO_LIVE_RO)) {
                     node.setResearchObject(true);
                     node.setEvoClass(EvoClass.LIVE);
                 } else if (object.equals(Vocab.ROEVO_ARCHIVED_RO)) {
                     node.setResearchObject(true);
                     node.setEvoClass(EvoClass.ARCHIVED);
                 }
 
             }
         }
         return nodes.values();
     }
 
 
     /**
      * Create a {@link RoEvoNode}.
      * 
      * @param nodes
      *            other nodes to reuse one if applicable
      * @param objectURIString
      *            URI
      * @return a node
      * @throws URISyntaxException
      *             the URI is incorrect
      */
     private static RoEvoNode createNode(Map<URI, RoEvoNode> nodes, String objectURIString)
             throws URISyntaxException {
         URI objectURI = new URI(objectURIString);
         if (!nodes.containsKey(objectURI)) {
             nodes.put(objectURI, new RoEvoNode(objectURI));
         }
         return nodes.get(objectURI);
     }
 }
