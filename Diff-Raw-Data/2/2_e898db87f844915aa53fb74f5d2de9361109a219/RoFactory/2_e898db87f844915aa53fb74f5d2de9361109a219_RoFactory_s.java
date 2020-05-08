 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal.services;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.wicket.Application;
 import org.apache.wicket.request.UrlDecoder;
 import org.apache.wicket.util.crypt.Base64;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.purl.wf4ever.rosrs.client.common.AnonId;
 import org.purl.wf4ever.rosrs.client.common.ROSRService;
 import org.purl.wf4ever.rosrs.client.common.Vocab;
 
 import pl.psnc.dl.wf4ever.portal.PortalApplication;
 import pl.psnc.dl.wf4ever.portal.model.AggregatedResource;
 import pl.psnc.dl.wf4ever.portal.model.Annotation;
 import pl.psnc.dl.wf4ever.portal.model.Creator;
 import pl.psnc.dl.wf4ever.portal.model.ResearchObject;
 import pl.psnc.dl.wf4ever.portal.model.ResourceGroup;
 import pl.psnc.dl.wf4ever.portal.model.RoTreeModel;
 import pl.psnc.dl.wf4ever.portal.model.Statement;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.shared.DoesNotExistException;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 /**
  * Utility class for creating various objects representing the RO model elements.
  * 
  * @author piotrhol
  * 
  */
 public final class RoFactory {
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(RoFactory.class);
 
 
     /**
      * Private constructor.
      */
     private RoFactory() {
         //nope
     }
 
 
     /**
      * Create a {@link ResearchObject}.
      * 
      * @param rodlURI
      *            RODL URI
      * @param researchObjectURI
      *            RO URI
      * @param includeAnnotations
      *            should the RO annotations be loaded too
      * @param usernames
      *            usernames cache
      * @return Research Object
      * @throws URISyntaxException
      *             problem with parsing URIs
      */
     public static ResearchObject createResearchObject(URI rodlURI, URI researchObjectURI, boolean includeAnnotations,
             Map<URI, Creator> usernames)
             throws URISyntaxException {
         OntModel model = ROSRService.createManifestModel(researchObjectURI);
         return createResearchObject(model, rodlURI, researchObjectURI, includeAnnotations, usernames);
     }
 
 
     /**
      * Create a {@link ResearchObject} based on a model.
      * 
      * @param model
      *            the Jena model of the manifest
      * @param rodlURI
      *            RODL URI
      * @param researchObjectURI
      *            RO URI
      * @param includeAnnotations
      *            should the RO annotations be loaded too
      * @param usernames
      *            usernames cache
      * @return Research Object
      * @throws URISyntaxException
      *             problem with parsing URIs
      */
     public static ResearchObject createResearchObject(OntModel model, URI rodlURI, URI researchObjectURI,
             boolean includeAnnotations, Map<URI, Creator> usernames)
             throws URISyntaxException {
         return (ResearchObject) createResource(model, rodlURI, researchObjectURI, researchObjectURI,
             includeAnnotations, usernames);
     }
 
 
     /**
      * Create a set of aggregated resources based on a model. Will include annotations.
      * 
      * @param model
      *            the Jena model of the manifest
      * @param rodlURI
      *            RODL URI
      * @param researchObjectURI
      *            RO URI
      * @param usernames
      *            usernames cache
      * @return Research Object
      * @throws URISyntaxException
      *             problem with parsing URIs
      */
     public static Map<URI, AggregatedResource> getAggregatedResources(OntModel model, URI rodlURI,
             URI researchObjectURI, Map<URI, Creator> usernames)
             throws URISyntaxException {
         Map<URI, AggregatedResource> resources = new HashMap<URI, AggregatedResource>();
         ResearchObject researchObject = createResearchObject(model, rodlURI, researchObjectURI, true, usernames);
         resources.put(researchObjectURI, researchObject);
         Individual ro = model.getIndividual(researchObjectURI.toString());
         NodeIterator it = model.listObjectsOfProperty(ro, Vocab.ORE_AGGREGATES);
         while (it.hasNext()) {
             Individual res = it.next().as(Individual.class);
             if (res.hasRDFType(Vocab.RO_RESOURCE)) {
                 AggregatedResource resource = createResource(model, rodlURI, researchObjectURI, new URI(res.getURI()),
                     true, usernames);
                 resources.put(resource.getURI(), resource);
             }
         }
         return resources;
     }
 
 
     /**
      * Create an {@link AggregatedResource}.
      * 
      * @param rodlURI
      *            RODL URI
      * @param researchObjectURI
      *            aggregating RO URI
      * @param resourceURI
      *            resource URI
      * @param includeAnnotations
      *            should the RO annotations be loaded too
      * @param usernames
      *            usernames cache
      * @return the aggregated resource
      */
     public static AggregatedResource createResource(URI rodlURI, URI researchObjectURI, URI resourceURI,
             boolean includeAnnotations, Map<URI, Creator> usernames) {
         OntModel model = ROSRService.createManifestModel(researchObjectURI);
         return createResource(model, rodlURI, researchObjectURI, resourceURI, includeAnnotations, usernames);
     }
 
 
     /**
      * Create an {@link AggregatedResource} based on a model.
      * 
      * @param model
      *            Jena model with data about the resource
      * @param rodlURI
      *            RODL URI
      * @param researchObjectURI
      *            aggregating RO URI
      * @param resourceURI
      *            resource URI
      * @param includeAnnotations
      *            should the RO annotations be loaded too
      * @param usernames
      *            usernames cache
      * @return the aggregated resource
      */
     public static AggregatedResource createResource(OntModel model, URI rodlURI, URI researchObjectURI,
             URI resourceURI, boolean includeAnnotations, Map<URI, Creator> usernames) {
 
         Individual res = model.getIndividual(resourceURI.toString());
         Calendar created = null;
         List<Creator> creators = new ArrayList<>();
         long size = -1;
 
         try {
             created = ((XSDDateTime) res.getPropertyValue(DCTerms.created).asLiteral().getValue()).asCalendar();
         } catch (Exception e) {
             LOG.trace("Could not parse dcterms:created for " + resourceURI, e);
         }
         try {
             NodeIterator it = res.listPropertyValues(DCTerms.creator);
             while (it.hasNext()) {
                 RDFNode node = it.next();
                 creators.add(getCreator(rodlURI, usernames, node));
             }
         } catch (Exception e) {
             LOG.trace("Could not parse dcterms:creator for " + resourceURI, e);
         }
         try {
             size = res.getPropertyValue(Vocab.FILESIZE).asLiteral().getLong();
         } catch (Exception e) {
             LOG.trace("Could not parse filesize for " + resourceURI, e);
         }
 
         String name = UrlDecoder.PATH_INSTANCE.decode(researchObjectURI.relativize(resourceURI).toString(), "UTF-8");
         AggregatedResource resource;
         if (res.hasRDFType("http://purl.org/wf4ever/ro#ResearchObject")) {
             resource = new ResearchObject(resourceURI, created, creators);
         } else {
             resource = new AggregatedResource(resourceURI, created, creators, name);
             if (size >= 0) {
                 resource.setSize(size);
             }
         }
         if (includeAnnotations) {
             resource.setAnnotations(createAnnotations(model, rodlURI, researchObjectURI, resourceURI, usernames));
         }
         return resource;
     }
 
 
     /**
      * Assign resource groups to a given collection of aggregated resources.
      * 
      * @param model
      *            the model with RDF classes of resources
      * @param researchObjectURI
      *            RO URI
      * @param resourceGroups
      *            resource groups
      * @param resources
      *            aggregated resources
      */
     public static void assignResourceGroupsToResources(OntModel model, URI researchObjectURI,
             Set<ResourceGroup> resourceGroups, Map<URI, AggregatedResource> resources) {
         ResearchObject ro = (ResearchObject) resources.get(researchObjectURI);
 
         // TODO take care of proxies
         for (AggregatedResource resource : resources.values()) {
             if (resource.equals(ro)) {
                 continue;
             }
             Individual res = model.getIndividual(resource.getURI().toString());
             for (ResourceGroup resourceGroup : resourceGroups) {
                 for (URI classURI : resourceGroup.getRdfClasses()) {
                     if (res.hasRDFType(classURI.toString())) {
                         resource.getMatchingGroups().add(resourceGroup);
                         break;
                     }
                 }
             }
         }
     }
 
 
     /**
      * Create a {@link RoTreeModel} where resource groups are folders and resources are assigned to each resource group
      * they belong to, or to root node if they have no resource groups.
      * 
      * @param researchObjectURI
      *            RO URI
      * @param resources
      *            aggregated resources
      * @return a tree model
      */
     public static RoTreeModel createConceptualResourcesTree(URI researchObjectURI,
             Map<URI, AggregatedResource> resources) {
         ResearchObject ro = (ResearchObject) resources.get(researchObjectURI);
         RoTreeModel treeModel = new RoTreeModel(ro);
 
         // TODO take care of proxies
         for (AggregatedResource resource : resources.values()) {
             if (resource.equals(ro)) {
                 continue;
             }
             treeModel.addAggregatedResource(resource, true);
         }
         return treeModel;
     }
 
 
     /**
      * Create a {@link RoTreeModel} where all resources are assigned to the root node.
      * 
      * @param researchObjectURI
      *            RO URI
      * @param resources
      *            aggregated resources
      * @return a tree model
      */
 
     public static RoTreeModel createPhysicalResourcesTree(URI researchObjectURI, Map<URI, AggregatedResource> resources) {
         ResearchObject ro = (ResearchObject) resources.get(researchObjectURI);
         RoTreeModel treeModel = new RoTreeModel(resources.get(researchObjectURI));
 
         // TODO take care of proxies & folders
         for (AggregatedResource resource : resources.values()) {
             if (resource.equals(ro)) {
                 continue;
             }
             if (isResourceInternal(researchObjectURI, resource.getURI())) {
                 treeModel.addAggregatedResource(resource, false);
             }
         }
         return treeModel;
     }
 
 
     /**
      * Create a JSON that represents aggregated resources of an RO and its relations. The JSON is compliant with the
      * JavaScript InfoVis Toolkit API (@see http://thejit.org/).
      * 
      * @param resources
      *            aggregated resources
      * @param colors
      *            set of colors to choose from
      * @return a JSON string
      * @throws IOException
      *             when the Jackson JSON generator encounters an error
      */
     public static String createRoJSON(Map<URI, AggregatedResource> resources, String[] colors)
             throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         JsonFactory jsonFactory = new JsonFactory();
         JsonGenerator jg = jsonFactory.createJsonGenerator(out);
         jg.writeStartArray();
         for (AggregatedResource resource : resources.values()) {
             int rdfClassHashCode = 0;
             for (ResourceGroup g : resource.getMatchingGroups()) {
                 rdfClassHashCode += g.hashCode();
             }
             String color = colors[Math.abs(rdfClassHashCode) % colors.length];
             String tooltip = resource instanceof ResearchObject ? "Research Object" : StringUtils.join(
                 resource.getMatchingGroups(), ", ");
 
             jg.writeStartObject();
             jg.writeStringField("id", Base64.encodeBase64URLSafeString(resource.getURI().toString().getBytes()));
             jg.writeStringField("name", resource.getName());
             jg.writeObjectFieldStart("data");
             jg.writeStringField("$color", color);
             jg.writeStringField("tooltip", tooltip);
             jg.writeEndObject();
             jg.writeArrayFieldStart("adjacencies");
             for (AggregatedResource adjacency : resource.getRelations().values()) {
                 jg.writeString(Base64.encodeBase64URLSafeString(adjacency.getURI().toString().getBytes()));
             }
             jg.writeEndArray();
             jg.writeEndObject();
         }
         jg.writeEndArray();
         jg.close(); // important: will force flushing of output, close underlying output stream
         return out.toString();
     }
 
 
     /**
      * Create a {@link Creator} from an RDF node.
      * 
      * @param rodlURI
      *            RODL URI
      * @param usernames
      *            usernames cache
      * @param creator
      *            the RDF node with a literal or URI of the creator
      * @return a {@link Creator} instance
      */
     public static Creator getCreator(URI rodlURI, final Map<URI, Creator> usernames, RDFNode creator) {
         if (creator == null) {
             return null;
         }
         if (creator.isURIResource()) {
             final URI uri = URI.create(creator.asResource().getURI());
             try {
                 if (usernames.containsKey(uri)) {
                     // 1. already fetched
                     LOG.trace("Retrieving username from cache for user: " + uri);
                 } else if (creator.asResource().hasProperty(Vocab.FOAF_NAME)) {
                     // 2. FOAF data defined inline
                     usernames.put(uri, new Creator(creator.as(Individual.class).getPropertyValue(Vocab.FOAF_NAME)
                             .asLiteral().getString()));
                 } else {
                     //3. load in a separate thread
                     usernames.put(uri, new Creator(rodlURI, uri));
                 }
             } catch (Exception e) {
                 LOG.error("Error when getting username", e);
                 usernames.put(uri, new Creator(uri.toString()));
             }
             return usernames.get(uri);
         } else if (creator.isResource()) {
             return new Creator(creator.asResource().getId().toString());
         } else {
             return new Creator(creator.asLiteral().getString());
         }
     }
 
 
     /**
      * Create a {@link Creator} from a string, which may or may not be an URI.
      * 
      * @param rodlURI
      *            RODL URI
      * @param usernames
      *            usernames cache
      * @param nameOrUri
      *            the name or URI
      * @return a {@link Creator} instance
      */
     public static Creator getCreator(URI rodlURI, final Map<URI, Creator> usernames, final String nameOrUri) {
         try {
             URI uri = new URI(nameOrUri);
             if (!usernames.containsKey(uri)) {
                 //2. load in a separate thread
                 usernames.put(uri, new Creator(rodlURI, uri));
             }
             return usernames.get(uri);
         } catch (URISyntaxException e) {
             return new Creator(nameOrUri);
         }
     }
 
 
     /**
      * Calculate stability measures for a research object and its resources.
      * 
      * @param model
      *            RO model
      * @param researchObjectURI
      *            RO URI
      * @param resources
      *            aggregated resources
      */
     public static void createStabilities(OntModel model, URI researchObjectURI, Map<URI, AggregatedResource> resources) {
         PortalApplication app = (PortalApplication) Application.get();
         try {
             QueryExecution qexec = QueryExecutionFactory.create(
                 MyQueryFactory.getProvenanceTraces(researchObjectURI.toString()), model);
             ResultSet result = qexec.execSelect();
             while (result.hasNext()) {
                 QuerySolution solution = result.next();
                 Resource resource = solution.get("resource").asResource();
                 Resource trace = solution.get("trace").asResource();
                 if (resource.isURIResource() && trace.isURIResource()) {
                     AggregatedResource resourceAR = resources.get(URI.create(resource.getURI()));
                     if (resourceAR != null) {
                         try {
                             double score = StabilityService.calculateStability(app.getStabilityEndpointURL().toURI(),
                                 URI.create(trace.getURI()));
                             resourceAR.setStability(score);
                             resourceAR.setProvenanceTraceURI(URI.create(trace.getURI()));
                         } catch (Exception e) {
                             LOG.error(e);
                             resourceAR.setStability(-1);
                         }
                     }
                 }
             }
             qexec.close();
         } catch (IOException e) {
             LOG.error(e.getMessage());
         }
     }
 
 
     /**
      * Discover relations between a set of aggregated resources.
      * 
      * @param model
      *            Jena model in which to look for relations
      * @param researchObjectURI
      *            RO URI
      * @param resources
      *            aggregated resources
      */
     public static void createRelations(OntModel model, URI researchObjectURI, Map<URI, AggregatedResource> resources) {
         for (AggregatedResource resourceAR : resources.values()) {
             Individual resource = model.getIndividual(resourceAR.getURI().toString());
             StmtIterator it = model.listStatements(resource, null, (Resource) null);
             while (it.hasNext()) {
                 com.hp.hpl.jena.rdf.model.Statement statement = it.next();
                 if (statement.getObject().isURIResource()) {
                     URI objectURI = URI.create(statement.getObject().asResource().getURI());
                     if (resources.containsKey(objectURI)) {
                         AggregatedResource objectAR = resources.get(objectURI);
                         Property property = statement.getPredicate();
                         addRelation(resourceAR, property, objectAR);
                     }
                 }
             }
         }
     }
 
 
     /**
      * Store a relation between 2 aggregated resources. A relation is stored in subject's
      * {@link AggregatedResource#getRelations()} and in object's {@link AggregatedResource#getInverseRelations()}. An
      * inverse relation is stored the other way round. If no inverse relation can be found, the name of the relation
      * with a prefix "(inverse of) " is used.
      * 
      * @param resourceAR
      *            subject resource
      * @param property
      *            RDF property
      * @param objectAR
      *            object resource
      */
     public static void addRelation(AggregatedResource resourceAR, Property property, AggregatedResource objectAR) {
         String propertyName = RoFactory.splitCamelCase(property.getLocalName()).toLowerCase();
         resourceAR.getRelations().put(propertyName, objectAR);
         objectAR.getInverseRelations().put(propertyName, resourceAR);
 
         Property inverse = property.as(OntProperty.class).getInverse();
         String inversePropertyName = (inverse != null ? RoFactory.splitCamelCase(inverse.getLocalName()).toLowerCase()
                 : "(inverse of) " + propertyName);
         objectAR.getRelations().put(inversePropertyName, resourceAR);
         resourceAR.getInverseRelations().put(inversePropertyName, objectAR);
 
     }
 
 
     /**
      * Store a relation when no RDF property is known, tries to create a property before calling
      * {@link RoFactory#addRelation(AggregatedResource, Property, AggregatedResource)}.
      * 
      * @param resourceAR
      *            subject resource
      * @param propertyURI
      *            property URI
      * @param objectAR
      *            object resource
      */
     public static void addRelation(AggregatedResource resourceAR, URI propertyURI, AggregatedResource objectAR) {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
         Property property = model.createProperty(propertyURI.toString());
         try {
             model.read(property.getNameSpace());
         } catch (DoesNotExistException e) {
             // do nothing, model will be empty
             LOG.trace("Could not load model for property URI " + propertyURI, e);
         }
         addRelation(resourceAR, property, objectAR);
     }
 
 
     /**
      * Create annotations for an aggregated resource.
      * 
      * @param rodlURI
      *            RODL URI
      * @param researchObjectURI
      *            RO URI
      * @param resourceURI
      *            resource URI
      * @param usernames
      *            usernames cache
      * @return a list of Annotations of a resource
      */
     public static List<Annotation> createAnnotations(URI rodlURI, URI researchObjectURI, URI resourceURI,
             Map<URI, Creator> usernames) {
         OntModel model = ROSRService.createManifestModel(researchObjectURI);
         return createAnnotations(model, rodlURI, researchObjectURI, resourceURI, usernames);
     }
 
 
     /**
      * Create annotations for an aggregated resource based on a model.
      * 
      * @param model
      *            Jena model that contains the annotations
      * @param rodlURI
      *            RODL URI
      * @param researchObjectURI
      *            RO URI
      * @param resourceURI
      *            resource URI
      * @param usernames
      *            usernames cache
      * @return a list of Annotations of a resource
      */
     public static List<Annotation> createAnnotations(OntModel model, URI rodlURI, URI researchObjectURI,
             URI resourceURI, Map<URI, Creator> usernames) {
         List<Annotation> anns = new ArrayList<>();
 
         Individual res = model.getIndividual(resourceURI.toString());
         ResIterator it = model.listSubjectsWithProperty(Vocab.ORE_ANNOTATES_AGGREGATED_RESOURCE, res);
         while (it.hasNext()) {
             Individual ann = it.next().as(Individual.class);
             if (!ann.hasRDFType(Vocab.RO_AGGREGATED_ANNOTATION)) {
                 continue;
             }
             try {
                 Resource body = ann.getPropertyResourceValue(Vocab.AO_BODY);
                 Calendar created = null;
                 List<Creator> creators = new ArrayList<>();
 
                 try {
                     created = ((XSDDateTime) res.getPropertyValue(DCTerms.created).asLiteral().getValue()).asCalendar();
                 } catch (Exception e) {
                     LOG.trace("Could not parse dcterms:created for " + resourceURI, e);
                 }
                 try {
                     NodeIterator it2 = res.listPropertyValues(DCTerms.creator);
                     while (it2.hasNext()) {
                         RDFNode node = it2.next();
                         creators.add(getCreator(rodlURI, usernames, node));
                     }
                 } catch (Exception e) {
                     LOG.trace("Could not parse dcterms:creator for " + resourceURI, e);
                 }
                 if (ann.isURIResource()) {
                     URI annURI = new URI(ann.getURI());
                     String name = UrlDecoder.PATH_INSTANCE.decode(researchObjectURI.relativize(annURI).toString(),
                         "UTF-8");
                     anns.add(new Annotation(annURI, created, creators, name, new URI(body.getURI())));
                 } else {
                     anns.add(new Annotation(new AnonId(ann.getId()), created, creators, ann.getId().getLabelString(),
                             new URI(body.getURI())));
                 }
             } catch (Exception e) {
                 LOG.warn("Could not add annotation " + ann.getURI() + ": " + e.getMessage());
             }
         }
         Collections.sort(anns, new Comparator<Annotation>() {
 
             @Override
             public int compare(Annotation a1, Annotation a2) {
                 if (a1.getCreated() == null && a2.getCreated() == null) {
                     return 0;
                 }
                 if (a1.getCreated() == null) {
                     return 1;
                 }
                if (a1.getCreated() == null) {
                     return -1;
                 }
                 return a1.getCreated().compareTo(a2.getCreated());
             }
         });
         return anns;
     }
 
 
     /**
      * Create an RDF annotation body for an annotation.
      * 
      * @param annotation
      *            an annotation, stored in the annotation body statements for reference
      * @param annotationBodyURI
      *            the URI of the annotation body
      * @return list of {@link Statement}
      * @throws URISyntaxException
      *             when there was a problem with parsing URIs
      */
     public static List<Statement> createAnnotationBody(Annotation annotation, URI annotationBodyURI)
             throws URISyntaxException {
         Model body = ModelFactory.createDefaultModel();
         try {
             body.read(annotationBodyURI.toString());
         } catch (Exception e) {
             return null;
         }
 
         List<Statement> statements = new ArrayList<Statement>();
         StmtIterator it = body.listStatements();
         while (it.hasNext()) {
             statements.add(new Statement(it.next(), annotation));
         }
         Collections.sort(statements, new Comparator<Statement>() {
 
             @Override
             public int compare(Statement s1, Statement s2) {
                 return s1.getPropertyLocalName().compareTo(s2.getPropertyLocalName());
             }
         });
         return statements;
     }
 
 
     /**
      * Convert an annotation body (a list of {@link Statement}) to an RDF graph.
      * 
      * @param statements
      *            a list of {@link Statement}
      * @return input stream of an RDF graph in RDF/XML format
      */
     public static InputStream wrapAnnotationBody(List<Statement> statements) {
         Model body = ModelFactory.createDefaultModel();
         for (Statement stmt : statements) {
             body.add(stmt.createJenaStatement());
         }
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         body.write(out);
         return new ByteArrayInputStream(out.toByteArray());
     }
 
 
     /**
      * Split a camel case string into separate words. Taken from
      * http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human -readable-names-in-java
      * 
      * @param s
      *            the camel case string
      * @return string separated into words
      */
     public static String splitCamelCase(String s) {
         return s.replaceAll(
             String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"),
             " ");
     }
 
 
     /**
      * A hack method to decide whether a resource is internal to an RO, checks if the resource URI starts with the RO
      * URI.
      * 
      * @param roURI
      *            RO URI
      * @param resourceURI
      *            resource URI
      * @return true if the resource is internal, false otherwise
      */
     public static boolean isResourceInternal(URI roURI, URI resourceURI) {
         return resourceURI != null && resourceURI.normalize().toString().startsWith(roURI.normalize().toString());
     }
 }
