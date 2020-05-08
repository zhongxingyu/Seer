 package pl.psnc.dl.wf4ever.sms;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.common.EvoType;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.util.SafeURI;
 import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.exceptions.ManifestTraversingException;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.FOAF;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.PROV;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 import pl.psnc.dl.wf4ever.vocabulary.ROEVO;
 import pl.psnc.dl.wf4ever.vocabulary.W4E;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.shared.JenaException;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.ng4j.sparql.NamedGraphDataset;
 
 public class SemanticMetadataServiceImpl implements SemanticMetadataService {
 
     private static final Logger log = Logger.getLogger(SemanticMetadataServiceImpl.class);
 
     private final NamedGraphSet graphset;
 
     private final String getResourceQueryTmpl = "DESCRIBE <%s> WHERE { }";
 
     private final String getUserQueryTmpl = "DESCRIBE <%s> WHERE { }";
 
     private final String findResearchObjectsQueryTmpl = "PREFIX ro: <" + RO.NAMESPACE + "> SELECT ?ro "
             + "WHERE { ?ro a ro:ResearchObject. FILTER regex(str(?ro), \"^%s\") . }";
 
     private final String findResearchObjectsByCreatorQueryTmpl = "PREFIX ro: <" + RO.NAMESPACE + "> PREFIX dcterms: <"
             + DCTerms.NS + "> SELECT ?ro WHERE { ?ro a ro:ResearchObject ; dcterms:creator <%s> . }";
 
     private final Connection connection;
 
     private final UserMetadata user;
     private static final Syntax SPARQL_SYNTAX = Syntax.syntaxARQ;
 
 
     public SemanticMetadataServiceImpl(UserMetadata user)
             throws IOException, NamingException, SQLException, ClassNotFoundException {
         this(user, true);
     }
 
 
     public SemanticMetadataServiceImpl(UserMetadata user, boolean useDb)
             throws IOException, NamingException, SQLException, ClassNotFoundException {
         this.user = user;
 
         if (useDb) {
             connection = getConnection("connection.properties");
             if (connection == null) {
                 throw new RuntimeException("Connection could not be created");
             }
             graphset = new NamedGraphSetDB(connection, "sms");
         } else {
             this.connection = null;
             graphset = new NamedGraphSetImpl();
         }
         W4E.DEFAULT_MODEL.setNsPrefixes(W4E.STANDARD_NAMESPACES);
         createUserProfile(user);
     }
 
 
     public SemanticMetadataServiceImpl(UserMetadata userMetadata, ResearchObject researchObject, InputStream manifest,
             RDFFormat rdfFormat) {
         this.user = userMetadata;
         this.connection = null;
         graphset = new NamedGraphSetImpl();
         W4E.DEFAULT_MODEL.setNsPrefixes(W4E.STANDARD_NAMESPACES);
         createUserProfile(userMetadata);
 
         createResearchObject(researchObject);
         updateManifest(researchObject, manifest, rdfFormat);
     }
 
 
     private void createUserProfile(UserMetadata user2) {
         if (!containsNamedGraph(user2.getUri())) {
             OntModel userModel = createOntModelForNamedGraph(user2.getUri());
             userModel.removeAll();
             Individual agent = userModel.createIndividual(user2.getUri().toString(), FOAF.Agent);
             userModel.add(agent, FOAF.name, user2.getName());
         }
     }
 
 
     private Connection getConnection(String filename)
             throws IOException, NamingException, SQLException, ClassNotFoundException {
         InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
         Properties props = new Properties();
         props.load(is);
 
         if (props.containsKey("datasource")) {
             String datasource = props.getProperty("datasource");
             if (datasource != null) {
                 InitialContext ctx = new InitialContext();
                 DataSource ds = (DataSource) ctx.lookup(datasource);
                 return ds.getConnection();
             }
         } else {
             String driver_class = props.getProperty("driver_class");
             String url = props.getProperty("url");
             String username = props.getProperty("username");
             String password = props.getProperty("password");
             if (driver_class != null && url != null && username != null && password != null) {
                 Class.forName(driver_class);
                 return DriverManager.getConnection(url, username, password);
             }
         }
 
         return null;
     }
 
 
     @Override
     public UserMetadata getUserProfile() {
         return user;
     }
 
 
     @Override
     public void createResearchObject(ResearchObject researchObject) {
         createLiveResearchObject(researchObject, null);
     }
 
 
     @Override
     public void createLiveResearchObject(ResearchObject researchObject, ResearchObject source) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual manifest = manifestModel.getIndividual(researchObject.getManifestUri().toString());
 
         if (manifest != null) {
             throw new IllegalArgumentException("URI already exists: " + researchObject.getManifestUri());
         }
 
         manifest = manifestModel.createIndividual(researchObject.getManifestUri().toString(), RO.Manifest);
         Individual ro = manifestModel.createIndividual(researchObject.getUri().toString(), RO.ResearchObject);
 
         manifestModel.add(ro, ORE.isDescribedBy, manifest);
         manifestModel.add(ro, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
         manifestModel.add(ro, DCTerms.creator, manifestModel.createResource(user.getUri().toString()));
         manifestModel.add(manifest, ORE.describes, ro);
         manifestModel.add(manifest, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 
         if (source != null) {
             manifestModel.add(ro, PROV.hadOriginalSource, manifestModel.createResource(source.getUri().toString()));
         }
 
     }
 
 
     @Override
     public void createSnapshotResearchObject(ResearchObject researchObject, ResearchObject liveResearchObject) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual manifest = manifestModel.getIndividual(researchObject.getManifestUri().toString());
         if (manifest != null) {
             throw new IllegalArgumentException("URI already exists: " + researchObject.getManifestUri());
         }
 
         OntModel liveManifestModel = createOntModelForNamedGraph(liveResearchObject.getManifestUri());
 
         Individual liveManifest = liveManifestModel.getIndividual(liveResearchObject.getManifestUri().toString());
 
         manifest = manifestModel.createIndividual(researchObject.getManifestUri().toString(), RO.Manifest);
         Individual ro = manifestModel.createIndividual(researchObject.getUri().toString(), RO.ResearchObject);
 
         RDFNode creator, created;
         Resource liveRO;
         if (liveManifest == null) {
             log.warn("Live RO is not an RO: " + liveResearchObject.getUri());
             liveRO = manifestModel.createResource(liveResearchObject.getUri().toString());
             creator = manifestModel.createResource(user.getUri().toString());
             created = manifestModel.createTypedLiteral(Calendar.getInstance());
         } else {
             liveRO = liveManifestModel.getIndividual(liveResearchObject.getUri().toString());
             if (liveRO == null) {
                 throw new IllegalArgumentException("Live RO does not describe the research object");
             }
             creator = liveRO.getPropertyResourceValue(DCTerms.creator);
             created = liveRO.as(Individual.class).getPropertyValue(DCTerms.created);
 
         }
 
         manifestModel.add(ro, ORE.isDescribedBy, manifest);
         manifestModel.add(ro, DCTerms.created, created);
         manifestModel.add(ro, DCTerms.creator, creator);
 
         manifestModel.add(manifest, ORE.describes, ro);
         manifestModel.add(manifest, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 
         //TODO add wasRevisionOf
     }
 
 
     @Override
     public void createArchivedResearchObject(ResearchObject researchObject, ResearchObject liveResearchObject) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual manifest = manifestModel.getIndividual(researchObject.getManifestUri().toString());
         if (manifest != null) {
             throw new IllegalArgumentException("URI already exists: " + researchObject.getManifestUri());
         }
 
         OntModel liveManifestModel = createOntModelForNamedGraph(liveResearchObject.getManifestUri());
         Individual liveManifest = liveManifestModel.getIndividual(liveResearchObject.getManifestUri().toString());
 
         manifest = manifestModel.createIndividual(researchObject.getManifestUri().toString(), RO.Manifest);
         Individual ro = manifestModel.createIndividual(researchObject.getUri().toString(), RO.ResearchObject);
 
         RDFNode creator, created;
         Resource liveRO;
         if (liveManifest == null) {
             log.warn("Live RO is not an RO: " + liveResearchObject.getManifestUri());
             liveRO = manifestModel.createResource(liveResearchObject.getManifestUri().toString());
             creator = manifestModel.createResource(user.getUri().toString());
             created = manifestModel.createTypedLiteral(Calendar.getInstance());
         } else {
             liveRO = liveManifestModel.getIndividual(liveResearchObject.getManifestUri().toString());
             if (liveRO == null) {
                 throw new IllegalArgumentException("Live RO does not describe the research object");
             }
             creator = liveRO.getPropertyResourceValue(DCTerms.creator);
             created = liveRO.as(Individual.class).getPropertyValue(DCTerms.created);
         }
 
         manifestModel.add(ro, ORE.isDescribedBy, manifest);
         manifestModel.add(ro, DCTerms.created, created);
         manifestModel.add(ro, DCTerms.creator, creator);
 
         manifestModel.add(manifest, ORE.describes, ro);
         manifestModel.add(manifest, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 
         //TODO add wasRevisionOf
     }
 
 
     @Override
     public void updateManifest(ResearchObject researchObject, InputStream is, RDFFormat rdfFormat) {
         // TODO validate the manifest?
         addNamedGraph(researchObject.getManifestUri(), is, rdfFormat);
     }
 
 
     @Override
     public InputStream getManifest(ResearchObject researchObject, RDFFormat rdfFormat) {
         return getNamedGraph(researchObject.getManifestUri(), rdfFormat);
     }
 
 
     @Override
     public boolean addResource(ResearchObject researchObject, URI resourceURI, ResourceMetadata resourceInfo) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         if (ro == null) {
             throw new IllegalArgumentException("URI not found: " + researchObject.getUri());
         }
         boolean created = false;
         Individual resource = manifestModel.getIndividual(resourceURI.toString());
         if (resource == null) {
             created = true;
             resource = manifestModel.createIndividual(resourceURI.toString(), RO.Resource);
         }
         manifestModel.add(ro, ORE.aggregates, resource);
         if (resourceInfo != null) {
             if (resourceInfo.getName() != null) {
                 manifestModel.add(resource, RO.name, manifestModel.createTypedLiteral(resourceInfo.getName()));
             }
             manifestModel.add(resource, RO.filesize, manifestModel.createTypedLiteral(resourceInfo.getSizeInBytes()));
             if (resourceInfo.getChecksum() != null && resourceInfo.getDigestMethod() != null) {
                 manifestModel.add(resource, RO.checksum, manifestModel.createResource(String.format("urn:%s:%s",
                     resourceInfo.getDigestMethod(), resourceInfo.getChecksum())));
             }
         }
         manifestModel.add(resource, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
         manifestModel.add(resource, DCTerms.creator, manifestModel.createResource(user.getUri().toString()));
         return created;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#removeResource(java.net .URI,
      * java.net.URI)
      */
     @Override
     public void removeResource(ResearchObject researchObject, URI resourceURI) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         if (ro == null) {
             throw new IllegalArgumentException("URI not found: " + researchObject.getUri());
         }
         Resource resource = manifestModel.getResource(resourceURI.toString());
         manifestModel.remove(ro, ORE.aggregates, resource);
         manifestModel.removeAll(resource, null, null);
 
         ResIterator it2 = manifestModel.listSubjectsWithProperty(RO.annotatesAggregatedResource, resource);
         while (it2.hasNext()) {
             Resource ann = it2.next();
             manifestModel.remove(ann, RO.annotatesAggregatedResource, resource);
             if (!ann.hasProperty(RO.annotatesAggregatedResource)) {
                 Resource annBody = ann.getPropertyResourceValue(AO.body);
                 if (annBody != null && annBody.isURIResource()) {
                     URI annBodyURI = URI.create(annBody.getURI());
                     if (containsNamedGraph(annBodyURI)) {
                         removeNamedGraph(annBodyURI);
                     }
                 }
                 manifestModel.removeAll(ann, null, null);
                 manifestModel.removeAll(null, null, ann);
             }
         }
 
         URI proxy = getProxyForResource(researchObject, resourceURI);
         if (proxy != null) {
             deleteProxy(researchObject, proxy);
         }
     }
 
 
     @Override
     public InputStream getResource(ResearchObject researchObject, URI resourceURI, RDFFormat rdfFormat) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual resource = manifestModel.getIndividual(resourceURI.toString());
         if (resource == null) {
             return null;
         }
 
         String queryString = String.format(getResourceQueryTmpl, resourceURI.toString());
         Query query = QueryFactory.create(queryString);
 
         QueryResult result = processDescribeQuery(query, rdfFormat);
 
         if (!result.getFormat().equals(rdfFormat)) {
             log.warn(String.format("Possible RDF format mismatch: %s requested, %s returned", rdfFormat.getName(),
                 result.getFormat().getName()));
         }
 
         return result.getInputStream();
     }
 
 
     @Override
     public InputStream getNamedGraph(URI namedGraphURI, RDFFormat rdfFormat) {
         namedGraphURI = namedGraphURI.normalize();
         if (!graphset.containsGraph(namedGraphURI.toString())) {
             return null;
         }
         NamedGraphSet tmpGraphSet = new NamedGraphSetImpl();
         if (rdfFormat.supportsContexts()) {
             addGraphsRecursively(tmpGraphSet, namedGraphURI);
         } else {
             tmpGraphSet.addGraph(graphset.getGraph(namedGraphURI.toString()));
         }
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         tmpGraphSet.write(out, rdfFormat.getName().toUpperCase(), null);
         return new ByteArrayInputStream(out.toByteArray());
     }
 
 
     @Override
     public InputStream getNamedGraphWithRelativeURIs(URI namedGraphURI, ResearchObject researchObject,
             RDFFormat rdfFormat) {
         ResearchObjectRelativeWriter writer;
         if (rdfFormat != RDFFormat.RDFXML && rdfFormat != RDFFormat.TURTLE) {
             throw new RuntimeException("Format " + rdfFormat + " is not supported");
         } else if (rdfFormat == RDFFormat.RDFXML) {
             writer = new RO_RDFXMLWriter();
         } else {
             writer = new RO_TurtleWriter();
         }
         namedGraphURI = namedGraphURI.normalize();
         if (!graphset.containsGraph(namedGraphURI.toString())) {
             return null;
         }
         NamedGraphSet tmpGraphSet = new NamedGraphSetImpl();
         if (rdfFormat.supportsContexts()) {
             addGraphsRecursively(tmpGraphSet, namedGraphURI);
         } else {
             tmpGraphSet.addGraph(graphset.getGraph(namedGraphURI.toString()));
         }
 
         writer.setResearchObjectURI(researchObject.getUri());
         writer.setBaseURI(namedGraphURI);
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         writer.write(tmpGraphSet.asJenaModel(namedGraphURI.toString()), out, null);
         return new ByteArrayInputStream(out.toByteArray());
     }
 
 
     private void addGraphsRecursively(NamedGraphSet tmpGraphSet, URI namedGraphURI) {
         tmpGraphSet.addGraph(graphset.getGraph(namedGraphURI.toString()));
 
         OntModel annotationModel = createOntModelForNamedGraph(namedGraphURI);
         NodeIterator it = annotationModel.listObjectsOfProperty(AO.body);
         while (it.hasNext()) {
             RDFNode annotationBodyRef = it.next();
             URI childURI = URI.create(annotationBodyRef.asResource().getURI());
             if (graphset.containsGraph(childURI.toString()) && !tmpGraphSet.containsGraph(childURI.toString())) {
                 addGraphsRecursively(tmpGraphSet, childURI);
             }
         }
     }
 
 
     @Override
     public Set<URI> findResearchObjectsByPrefix(URI partialURI) {
         String queryString = String.format(findResearchObjectsQueryTmpl, partialURI != null ? partialURI.normalize()
                 .toString() : "");
         Query query = QueryFactory.create(queryString);
 
         // Execute the query and obtain results
         QueryExecution qe = QueryExecutionFactory.create(query, createOntModelForAllNamedGraphs());
         ResultSet results = qe.execSelect();
         Set<URI> uris = new HashSet<URI>();
         while (results.hasNext()) {
             QuerySolution solution = results.nextSolution();
             Resource manifest = solution.getResource("ro");
             uris.add(URI.create(manifest.getURI()));
         }
 
         // Important - free up resources used running the query
         qe.close();
         return uris;
     }
 
 
     @Override
     public Set<URI> findResearchObjectsByCreator(URI user) {
         if (user == null) {
             throw new IllegalArgumentException("User cannot be null");
         }
         String queryString = String.format(findResearchObjectsByCreatorQueryTmpl, user.toString());
         Query query = QueryFactory.create(queryString);
 
         // Execute the query and obtain results
         QueryExecution qe = QueryExecutionFactory.create(query, createOntModelForAllNamedGraphs());
         ResultSet results = qe.execSelect();
         Set<URI> uris = new HashSet<URI>();
         while (results.hasNext()) {
             QuerySolution solution = results.nextSolution();
             Resource manifest = solution.getResource("ro");
             uris.add(URI.create(manifest.getURI()));
         }
 
         // Important - free up resources used running the query
         qe.close();
         return uris;
     }
 
 
     @Override
     public Set<URI> findResearchObjects() {
         return findResearchObjectsByPrefix(null);
     }
 
 
     /**
      * @param namedGraphURI
      * @return
      */
     private OntModel createOntModelForNamedGraph(URI namedGraphURI) {
         NamedGraph namedGraph = getOrCreateGraph(graphset, namedGraphURI);
         OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
             ModelFactory.createModelForGraph(namedGraph));
         ontModel.addSubModel(W4E.DEFAULT_MODEL);
         return ontModel;
     }
 
 
     private NamedGraph getOrCreateGraph(NamedGraphSet graphset, URI namedGraphURI) {
         return graphset.containsGraph(namedGraphURI.toString()) ? graphset.getGraph(namedGraphURI.toString())
                 : graphset.createGraph(namedGraphURI.toString());
     }
 
 
     @Override
     public void close() {
         graphset.close();
     }
 
 
     @Override
     public boolean isRoFolder(ResearchObject researchObject, URI resourceURI) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual resource = manifestModel.getIndividual(resourceURI.toString());
         if (resource == null) {
             return false;
         }
         return resource.hasRDFType(RO.Folder);
     }
 
 
     @Override
     public boolean addNamedGraph(URI graphURI, InputStream inputStream, RDFFormat rdfFormat) {
         boolean created = !containsNamedGraph(graphURI);
         OntModel namedGraphModel = createOntModelForNamedGraph(graphURI);
         namedGraphModel.removeAll();
         namedGraphModel.read(inputStream, graphURI.resolve(".").toString(), rdfFormat.getName().toUpperCase());
         return created;
     }
 
 
     @Override
     public boolean isROMetadataNamedGraph(ResearchObject researchObject, URI graphURI) {
         Node manifest = Node.createURI(researchObject.getManifestUri().toString());
         Node deprecatedManifest = Node.createURI(getDeprecatedManifestURI(researchObject.getUri()).toString());
         Node bodyNode = Node.createURI(AO.body.getURI());
         Node annBody = Node.createURI(graphURI.toString());
         boolean isManifest = researchObject.getManifestUri().equals(graphURI);
         boolean isDeprecatedManifest = getDeprecatedManifestURI(researchObject.getUri()).equals(graphURI);
         boolean isAnnotationBody = graphset.containsQuad(new Quad(manifest, Node.ANY, bodyNode, annBody));
         boolean isDeprecatedAnnotationBody = graphset.containsQuad(new Quad(deprecatedManifest, Node.ANY, bodyNode,
                 annBody));
         return isManifest || isAnnotationBody || isDeprecatedManifest || isDeprecatedAnnotationBody;
     }
 
 
     @Override
     public void removeNamedGraph(URI graphURI) {
         //@TODO Remove evo_inf file
         graphURI = graphURI.normalize();
         if (!graphset.containsGraph(graphURI.toString())) {
             return;
         }
 
         List<URI> graphsToDelete = new ArrayList<>();
         graphsToDelete.add(graphURI);
 
         int i = 0;
         while (i < graphsToDelete.size()) {
             OntModel model = createOntModelForNamedGraph(graphsToDelete.get(i));
             List<RDFNode> annotationBodies = model.listObjectsOfProperty(AO.body).toList();
             for (RDFNode annotationBodyRef : annotationBodies) {
                 if (annotationBodyRef.isURIResource()) {
                     URI graphURI2 = URI.create(annotationBodyRef.asResource().getURI());
                     // TODO make sure that this named graph is internal
                     if (graphset.containsGraph(graphURI2.toString()) && !graphsToDelete.contains(graphURI2)) {
                         graphsToDelete.add(graphURI2);
                     }
                 }
             }
             List<RDFNode> evos = model.listObjectsOfProperty(ROEVO.wasChangedBy).toList();
             for (RDFNode evo : evos) {
                 if (evo.isURIResource()) {
                     URI graphURI2 = URI.create(evo.asResource().getURI());
                     if (graphset.containsGraph(graphURI2.toString()) && !graphsToDelete.contains(graphURI2)) {
                         graphsToDelete.add(graphURI2);
                     }
                 }
             }
             List<Individual> folders = model.listIndividuals(RO.Folder).toList();
             for (Individual folder : folders) {
                 if (folder.isURIResource()) {
                     Folder f = new Folder();
                     f.setUri(URI.create(folder.asResource().getURI()));
                     if (graphset.containsGraph(f.getResourceMapUri().toString())
                             && !graphsToDelete.contains(f.getResourceMapUri())) {
                         graphsToDelete.add(f.getResourceMapUri());
                     }
                 }
             }
             i++;
         }
         for (URI graphURI2 : graphsToDelete) {
             graphset.removeGraph(graphURI2.toString());
         }
     }
 
 
     private OntModel createOntModelForAllNamedGraphs() {
         return createOntModelForAllNamedGraphs(OntModelSpec.OWL_MEM);
     }
 
 
     private OntModel createOntModelForAllNamedGraphs(OntModelSpec spec) {
         OntModel ontModel = ModelFactory.createOntologyModel(spec, graphset.asJenaModel("sms"));
         ontModel.addSubModel(W4E.DEFAULT_MODEL);
         return ontModel;
     }
 
 
     /**
      * 
      * @param roURI
      *            must end with /
      * @return
      */
     private URI getDeprecatedManifestURI(URI roURI) {
         return roURI.resolve(".ro/manifest");
     }
 
 
     @Override
     public void removeResearchObject(ResearchObject researchObject) {
         try {
             removeNamedGraph(getDeprecatedManifestURI(researchObject.getUri()));
         } catch (IllegalArgumentException e) {
             // it is a hack so ignore exceptions
         }
         removeNamedGraph(researchObject.getManifestUri());
     }
 
 
     @Override
     public boolean containsNamedGraph(URI graphURI) {
         return graphset.containsGraph(SafeURI.URItoString(graphURI));
     }
 
 
     @Override
     public QueryResult executeSparql(String queryS, RDFFormat rdfFormat) {
         if (queryS == null)
             throw new NullPointerException("Query cannot be null");
         Query query = null;
         try {
             query = QueryFactory.create(queryS, SPARQL_SYNTAX);
         } catch (Exception e) {
             throw new IllegalArgumentException("Wrong query syntax: " + e.getMessage());
         }
 
         switch (query.getQueryType()) {
             case Query.QueryTypeSelect:
                 return processSelectQuery(query, rdfFormat);
             case Query.QueryTypeConstruct:
                 return processConstructQuery(query, rdfFormat);
             case Query.QueryTypeDescribe:
                 return processDescribeQuery(query, rdfFormat);
             case Query.QueryTypeAsk:
                 return processAskQuery(query, rdfFormat);
             default:
                 return null;
         }
     }
 
 
     private QueryResult processSelectQuery(Query query, RDFFormat rdfFormat) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         RDFFormat outputFormat;
         QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
         if (SemanticMetadataService.SPARQL_JSON.equals(rdfFormat)) {
             outputFormat = SemanticMetadataService.SPARQL_JSON;
             ResultSetFormatter.outputAsJSON(out, qexec.execSelect());
         } else {
             outputFormat = SemanticMetadataService.SPARQL_XML;
             ResultSetFormatter.outputAsXML(out, qexec.execSelect());
         }
         qexec.close();
 
         return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
     }
 
 
     private QueryResult processAskQuery(Query query, RDFFormat rdfFormat) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         RDFFormat outputFormat;
         QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
         if ("application/sparql-results+json".equals(rdfFormat.getDefaultMIMEType())) {
             outputFormat = SemanticMetadataService.SPARQL_JSON;
             ResultSetFormatter.outputAsJSON(out, qexec.execAsk());
         } else {
             outputFormat = SemanticMetadataService.SPARQL_XML;
             ResultSetFormatter.outputAsXML(out, qexec.execAsk());
         }
         qexec.close();
 
         return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
     }
 
 
     private QueryResult processConstructQuery(Query query, RDFFormat rdfFormat) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
         Model resultModel = qexec.execConstruct();
         qexec.close();
 
         RDFFormat outputFormat;
         if (RDFFormat.values().contains(rdfFormat)) {
             outputFormat = rdfFormat;
         } else {
             outputFormat = RDFFormat.RDFXML;
         }
 
         resultModel.write(out, outputFormat.getName().toUpperCase());
         return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
     }
 
 
     private QueryResult processDescribeQuery(Query query, RDFFormat rdfFormat) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
         Model resultModel = qexec.execDescribe();
         qexec.close();
 
         RDFFormat outputFormat;
         if (RDFFormat.values().contains(rdfFormat)) {
             outputFormat = rdfFormat;
         } else {
             outputFormat = RDFFormat.RDFXML;
         }
 
         resultModel.removeNsPrefix("xml");
 
         resultModel.write(out, outputFormat.getName().toUpperCase());
         return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
     }
 
 
     @Override
     public Multimap<URI, Object> getAllAttributes(URI subjectURI) {
         Multimap<URI, Object> attributes = HashMultimap.<URI, Object> create();
         // This could be an inference model but it slows down the lookup process and
         // generates super-attributes
         OntModel model = createOntModelForAllNamedGraphs(OntModelSpec.OWL_MEM);
         Resource subject = model.getResource(subjectURI.toString());
         if (subject == null) {
             return attributes;
         }
         StmtIterator it = model.listStatements(subject, null, (RDFNode) null);
         while (it.hasNext()) {
             Statement s = it.next();
             try {
                 URI propURI = new URI(s.getPredicate().getURI());
                 Object object;
                 if (s.getObject().isResource()) {
                     // Need to check both because the model has no inference
                     if (s.getObject().as(Individual.class).hasRDFType(FOAF.Agent)
                             || s.getObject().as(Individual.class).hasRDFType(FOAF.Person)) {
                         object = s.getObject().asResource().getProperty(FOAF.name).getLiteral().getValue();
                     } else {
                         if (s.getObject().isURIResource()) {
                             object = new URI(s.getObject().asResource().getURI());
                         } else {
                             continue;
                         }
                     }
                 } else {
                     object = s.getObject().asLiteral().getValue();
                 }
                 if (object instanceof XSDDateTime) {
                     object = ((XSDDateTime) object).asCalendar();
                 }
                 attributes.put(propURI, object);
             } catch (URISyntaxException e) {
                 log.error(e);
             }
         }
         return attributes;
     }
 
 
     @Override
     public QueryResult getUser(URI userURI, RDFFormat rdfFormat) {
         userURI = userURI.normalize();
 
         String queryString = String.format(getUserQueryTmpl, userURI.toString());
         Query query = QueryFactory.create(queryString);
 
         return processDescribeQuery(query, rdfFormat);
     }
 
 
     @Override
     public void removeUser(URI userURI) {
         if (graphset.containsGraph(userURI.toString())) {
             graphset.removeGraph(userURI.toString());
         }
     }
 
 
     @Override
     public boolean isAggregatedResource(ResearchObject researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource researchObjectR = manifestModel.createResource(researchObject.getUri().toString());
         Resource resourceR = manifestModel.createResource(resource.normalize().toString());
         return manifestModel.contains(researchObjectR, ORE.aggregates, resourceR);
     }
 
 
     @Override
     public boolean isAnnotation(ResearchObject researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual resourceR = manifestModel.getIndividual(resource.normalize().toString());
         return resourceR != null && resourceR.hasRDFType(RO.AggregatedAnnotation);
     }
 
 
     @Override
     public URI addProxy(ResearchObject researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource researchObjectR = manifestModel.createResource(researchObject.getUri().toString());
         Resource resourceR = manifestModel.createResource(resource.normalize().toString());
         URI proxyURI = generateProxyURI(researchObject);
         Individual proxy = manifestModel.createIndividual(proxyURI.toString(), ORE.Proxy);
         manifestModel.add(proxy, ORE.proxyIn, researchObjectR);
         manifestModel.add(proxy, ORE.proxyFor, resourceR);
         return proxyURI;
     }
 
 
     private static URI generateProxyURI(ResearchObject researchObject) {
         return researchObject.getUri().resolve(".ro/proxies/" + UUID.randomUUID());
     }
 
 
     @Override
     public boolean isProxy(ResearchObject researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual resourceR = manifestModel.getIndividual(resource.normalize().toString());
         return resourceR != null && resourceR.hasRDFType(ORE.Proxy);
     }
 
 
     @Override
     public boolean existsProxyForResource(ResearchObject researchObject, URI resource) {
         return getProxyForResource(researchObject, resource) != null;
     }
 
 
     @Override
     public URI getProxyForResource(ResearchObject researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource resourceR = manifestModel.createResource(resource.normalize().toString());
         ResIterator it = manifestModel.listSubjectsWithProperty(ORE.proxyFor, resourceR);
         while (it.hasNext()) {
             Individual r = it.next().as(Individual.class);
             if (r != null && r.hasRDFType(ORE.Proxy)) {
                 return URI.create(r.getURI());
             }
         }
         return null;
     }
 
 
     @Override
     public URI getProxyFor(ResearchObject researchObject, URI proxy) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual proxyR = manifestModel.getIndividual(proxy.normalize().toString());
         if (proxyR.hasProperty(ORE.proxyFor)) {
             return URI.create(proxyR.getPropertyResourceValue(ORE.proxyFor).getURI());
         } else {
             return null;
         }
     }
 
 
     @Override
     public void deleteProxy(ResearchObject researchObject, URI proxy) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource proxyR = manifestModel.getResource(proxy.normalize().toString());
         manifestModel.removeAll(proxyR, null, null);
     }
 
 
     @Override
     public URI addAnnotation(ResearchObject researchObject, List<URI> annotationTargets, URI annotationBody) {
         return addAnnotation(researchObject, annotationTargets, annotationBody, null);
     }
 
 
     @Override
     public URI addAnnotation(ResearchObject researchObject, List<URI> annotationTargets, URI annotationBody,
             String annotationUUID) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource researchObjectR = manifestModel.createResource(researchObject.getUri().toString());
         Resource body = manifestModel.createResource(annotationBody.normalize().toString());
         URI annotationURI = generateAnnotationURI(researchObject, annotationUUID);
         Individual annotation = manifestModel.createIndividual(annotationURI.toString(), RO.AggregatedAnnotation);
         manifestModel.add(researchObjectR, ORE.aggregates, annotation);
         manifestModel.add(annotation, AO.body, body);
         for (URI targetURI : annotationTargets) {
             Resource target = manifestModel.createResource(targetURI.normalize().toString());
             manifestModel.add(annotation, RO.annotatesAggregatedResource, target);
         }
         manifestModel.add(annotation, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
         Resource agent = manifestModel.createResource(user.getUri().toString());
         manifestModel.add(annotation, DCTerms.creator, agent);
         return annotationURI;
     }
 
 
     private static URI generateAnnotationURI(ResearchObject researchObject, String uuid) {
         if (uuid == null) {
             uuid = UUID.randomUUID().toString();
         }
         return researchObject.getUri().resolve(".ro/annotations/" + uuid);
     }
 
 
     private static URI generateAnnotationURI(ResearchObject researchObject) {
         return generateAnnotationURI(researchObject, null);
     }
 
 
     @Override
     public void updateAnnotation(ResearchObject researchObject, URI annotationURI, List<URI> annotationTargets,
             URI annotationBody) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource body = manifestModel.createResource(annotationBody.normalize().toString());
         Individual annotation = manifestModel.getIndividual(annotationURI.toString());
         manifestModel.removeAll(annotation, AO.body, null);
         manifestModel.removeAll(annotation, RO.annotatesAggregatedResource, null);
         manifestModel.add(annotation, AO.body, body);
         for (URI targetURI : annotationTargets) {
             Resource target = manifestModel.createResource(targetURI.normalize().toString());
             manifestModel.add(annotation, RO.annotatesAggregatedResource, target);
         }
     }
 
 
     @Override
     public URI getAnnotationBody(ResearchObject researchObject, URI annotation) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual annotationR = manifestModel.getIndividual(annotation.normalize().toString());
         if (annotationR.hasProperty(AO.body)) {
             return URI.create(annotationR.getPropertyResourceValue(AO.body).getURI());
         } else {
             return null;
         }
     }
 
 
     @Override
     public void deleteAnnotation(ResearchObject researchObject, URI annotation) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource annotationR = manifestModel.getResource(annotation.normalize().toString());
         manifestModel.removeAll(annotationR, null, null);
         manifestModel.removeAll(null, null, annotationR);
     }
 
 
     @Override
     public int migrateRosr5To6(String datasource)
             throws NamingException, SQLException {
         if (datasource == null) {
             throw new IllegalArgumentException("Datasource cannot be null");
         }
         InitialContext ctx = new InitialContext();
         DataSource ds = (DataSource) ctx.lookup(datasource);
         Connection connection = ds.getConnection();
         if (connection == null) {
             throw new IllegalArgumentException("Connection could not be created");
         }
 
         NamedGraphSetDB oldGraphset = new NamedGraphSetDB(connection, "sms");
 
         int cnt = 0;
         Iterator<Quad> it = oldGraphset.findQuads(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
         while (it.hasNext()) {
             Quad quad = it.next();
             Node g = quad.getGraphName();
             if (g.isURI()) {
                 g = Node.createURI(g.getURI().replaceFirst("rosrs5", "rodl"));
             }
             Node o = quad.getObject();
             if (o.isURI()) {
                 o = Node.createURI(o.getURI().replaceFirst("rosrs5", "rodl"));
             }
             Node s = quad.getSubject();
             if (s.isURI()) {
                 s = Node.createURI(s.getURI().replaceFirst("rosrs5", "rodl"));
             }
             Quad newQuad = new Quad(g, s, quad.getPredicate(), o);
             if (!graphset.containsQuad(newQuad)) {
                 cnt++;
             }
             graphset.addQuad(newQuad);
         }
 
         return cnt;
     }
 
 
     @Override
     public int changeURI(URI oldUri, URI uri) {
         int cnt = 0;
         Node old = Node.createURI(oldUri.toString());
         Node newu = Node.createURI(uri.toString());
         Iterator<Quad> it = graphset.findQuads(Node.ANY, old, Node.ANY, Node.ANY);
         while (it.hasNext()) {
             Quad quad = it.next();
             graphset.removeQuad(quad);
             graphset.addQuad(new Quad(quad.getGraphName(), newu, quad.getPredicate(), quad.getObject()));
             cnt++;
         }
         it = graphset.findQuads(Node.ANY, Node.ANY, Node.ANY, old);
         while (it.hasNext()) {
             Quad quad = it.next();
             graphset.removeQuad(quad);
             graphset.addQuad(new Quad(quad.getGraphName(), quad.getSubject(), quad.getPredicate(), newu));
             cnt++;
         }
         return cnt;
     }
 
 
     @Override
     public Individual getIndividual(ResearchObject ro) {
         OntModel roManifestModel = createOntModelForNamedGraph(ro.getManifestUri());
         OntModel roEvolutionModel = createOntModelForNamedGraph(ro.getFixedEvolutionAnnotationBodyPath());
         return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, roManifestModel.union(roEvolutionModel))
                 .getIndividual(ro.getUriString());
     }
 
 
     @Override
     public boolean isSnapshot(ResearchObject ro) {
         return getIndividual(ro).hasRDFType(ROEVO.SnapshotRO);
 
     }
 
 
     @Override
     public boolean isArchive(ResearchObject ro) {
         return getIndividual(ro).hasRDFType(ROEVO.ArchivedRO);
     }
 
 
     @Override
     public URI getLiveURIFromSnapshotOrArchive(ResearchObject snaphotOrArchive)
             throws URISyntaxException {
         Individual source = getIndividual(snaphotOrArchive);
         if (isSnapshot(snaphotOrArchive)) {
             RDFNode roNode = source.getProperty(ROEVO.isSnapshotOf).getObject();
             return new URI(roNode.toString());
         } else if (isArchive(snaphotOrArchive)) {
             RDFNode roNode = source.getProperty(ROEVO.isArchiveOf).getObject();
             return new URI(roNode.toString());
         }
         return null;
     }
 
 
     @Override
     public URI getPreviousSnaphotOrArchive(ResearchObject liveRO, ResearchObject freshSnapshotOrArchive) {
         Individual liveSource = getIndividual(liveRO);
         StmtIterator snaphotsIterator;
         snaphotsIterator = liveSource.listProperties(ROEVO.hasSnapshot);
         StmtIterator archiveItertator;
         archiveItertator = liveSource.listProperties(ROEVO.hasArchive);
 
         Individual freshSource = getIndividual(freshSnapshotOrArchive);
         RDFNode dateNode;
         if (isSnapshot(freshSnapshotOrArchive)) {
             dateNode = freshSource.getProperty(ROEVO.snapshottedAtTime).getObject();
         } else if (isArchive(freshSnapshotOrArchive)) {
             dateNode = freshSource.getProperty(ROEVO.archivedAtTime).getObject();
         } else {
             return null;
         }
 
         DateTime freshTime = new DateTime(dateNode.asLiteral().getValue().toString());
         DateTime predecessorTime = null;
         URI result = null;
 
         while (snaphotsIterator.hasNext()) {
             URI tmpURI = URI.create(snaphotsIterator.next().getObject().toString());
             if (tmpURI == freshSnapshotOrArchive.getUri()) {
                 continue;
             }
             RDFNode node = getIndividual(ResearchObject.create(tmpURI)).getProperty(ROEVO.snapshottedAtTime)
                     .getObject();
             DateTime tmpTime = new DateTime(node.asLiteral().getValue().toString());
             if ((tmpTime.compareTo(freshTime) == -1)
                     && ((predecessorTime == null) || (tmpTime.compareTo(predecessorTime) == 1))) {
                 predecessorTime = tmpTime;
                 result = tmpURI;
             }
         }
         while (archiveItertator.hasNext()) {
             URI tmpURI = URI.create(archiveItertator.next().getObject().toString());
             RDFNode node = getIndividual(ResearchObject.create(tmpURI)).getProperty(ROEVO.archivedAtTime).getObject();
             DateTime tmpTime = new DateTime(node.asLiteral().getValue().toString());
             if ((tmpTime.compareTo(freshTime) == -1)
                     && ((predecessorTime == null) || (tmpTime.compareTo(predecessorTime) == 1))) {
                 predecessorTime = tmpTime;
                 result = tmpURI;
             }
         }
         return result;
     }
 
 
     private enum Direction {
         NEW,
         DELETED
     }
 
 
     @Override
     public String storeAggregatedDifferences(ResearchObject freshRO, ResearchObject oldRO)
             throws URISyntaxException, IOException {
 
         if (oldRO == null) {
             return "";
         }
         if (freshRO == null) {
             throw new IllegalArgumentException("Fresh object can not be null");
         }
         if (freshRO == oldRO) {
             throw new IllegalArgumentException("Fresh and old RO can not be this same");
         }
         if (compareTwoResearchObjectDateOfCreation(freshRO, oldRO) <= 0) {
             throw new IllegalArgumentException("Fresh RO can not be older them old RO");
         }
 
         List<RDFNode> freshAggreagted = getAggregatedWithNoEvoAndBody(freshRO);
         List<RDFNode> oldAggreagted = getAggregatedWithNoEvoAndBody(oldRO);
 
         OntModel evoInfoModel = createOntModelForNamedGraph(freshRO.getFixedEvolutionAnnotationBodyPath());
 
         Individual changeSpecificationIndividual = evoInfoModel.createIndividual(
             generateRandomUriRelatedToResource(freshRO, "change_specification"), ROEVO.ChangeSpecification);
 
         evoInfoModel.getIndividual(freshRO.getUriString()).addProperty(ROEVO.wasChangedBy,
             changeSpecificationIndividual);
 
         String result = lookForAggregatedDifferents(freshRO, oldRO, freshAggreagted, oldAggreagted, evoInfoModel,
             changeSpecificationIndividual, Direction.NEW);
         result += lookForAggregatedDifferents(freshRO, oldRO, oldAggreagted, freshAggreagted, evoInfoModel,
             changeSpecificationIndividual, Direction.DELETED);
         return result;
 
     }
 
 
     private int compareTwoResearchObjectDateOfCreation(ResearchObject first, ResearchObject second) {
         DateTime dateFirst = null;
         DateTime dateSecond = null;
         Individual firstIndividual = getIndividual(first);
         Individual secondIndividual = getIndividual(second);
 
         if (isSnapshot(first)) {
             dateFirst = new DateTime(firstIndividual.getPropertyValue(ROEVO.snapshottedAtTime).asLiteral().getValue()
                     .toString());
         } else if (isArchive(first)) {
             dateFirst = new DateTime(firstIndividual.getPropertyValue(ROEVO.archivedAtTime).asLiteral().getValue()
                     .toString());
         } else {
             dateFirst = new DateTime(firstIndividual.getPropertyValue(DCTerms.created).asLiteral().getValue()
                     .toString());
         }
 
         if (isSnapshot(second)) {
             dateSecond = new DateTime(secondIndividual.getPropertyValue(ROEVO.snapshottedAtTime).asLiteral().getValue()
                     .toString());
         } else if (isArchive(second)) {
             dateSecond = new DateTime(secondIndividual.getPropertyValue(ROEVO.archivedAtTime).asLiteral().getValue()
                     .toString());
         } else {
             dateSecond = new DateTime(secondIndividual.getPropertyValue(DCTerms.created).asLiteral().getValue()
                     .toString());
         }
         return dateFirst.compareTo(dateSecond);
     }
 
 
     private List<RDFNode> getAggregatedWithNoEvoAndBody(ResearchObject researchObject) {
         Individual roIndividual = getIndividual(researchObject);
         DateTime date = null;
         if (isSnapshot(researchObject)) {
             date = new DateTime(roIndividual.getPropertyValue(ROEVO.snapshottedAtTime).asLiteral().getValue()
                     .toString());
         } else if (isArchive(researchObject)) {
             date = new DateTime(roIndividual.getPropertyValue(ROEVO.archivedAtTime).asLiteral().getValue().toString());
         }
 
         List<RDFNode> aggreageted = roIndividual.listPropertyValues(ORE.aggregates).toList();
         List<RDFNode> aggreagetedWithNoEvo = new ArrayList<RDFNode>();
 
         for (RDFNode a : aggreageted) {
             try {
                 if (a.isResource()) {
                     DateTime newDate = new DateTime(a.asResource().getProperty(DCTerms.created).getObject().asLiteral()
                             .getValue().toString());
                     if (date.compareTo(newDate) > 0) {
                         aggreagetedWithNoEvo.add(a);
                     }
                 }
             } catch (NullPointerException e) {
                 continue;
             }
         }
         for (RDFNode a : aggreageted) {
             try {
                 if (a.as(Individual.class).hasRDFType(RO.AggregatedAnnotation)) {
                     RDFNode node = a.as(Individual.class).getPropertyValue(AO.body);
                     aggreagetedWithNoEvo.remove(node);
                 }
             } catch (Exception e) {
                 continue;
             }
         }
         return aggreagetedWithNoEvo;
     }
 
 
     private String generateRandomUriRelatedToResource(ResearchObject ro, String description) {
         return ro.getUri().resolve(description) + "/" + UUID.randomUUID().toString();
     }
 
 
     private String lookForAggregatedDifferents(ResearchObject fresRO, ResearchObject oldRO, List<RDFNode> pattern,
             List<RDFNode> compared, OntModel freshROModel, Individual changeSpecificationIndividual, Direction direction) {
         String result = "";
         Boolean tmp = null;
         for (RDFNode patternNode : pattern) {
             Boolean loopResult = null;
             for (RDFNode comparedNode : compared) {
                 if (direction == Direction.NEW) {
                     tmp = compareProprties(patternNode, comparedNode, fresRO.getUri(), oldRO.getUri());
                 } else {
                     tmp = compareProprties(patternNode, comparedNode, oldRO.getUri(), fresRO.getUri());
                 }
                 if (tmp != null) {
                     loopResult = tmp;
                 }
             }
             result += serviceDetectedEVOmodification(loopResult, fresRO, oldRO, patternNode, freshROModel,
                 changeSpecificationIndividual, direction);
         }
         return result;
     }
 
 
     private String serviceDetectedEVOmodification(Boolean loopResult, ResearchObject freshRO, ResearchObject oldRO,
             RDFNode node, OntModel freshRoModel, Individual changeSpecificatinIndividual, Direction direction) {
         String result = "";
         //Null means they are not comparable. Resource is new or deleted depends on the direction. 
         if (loopResult == null) {
             if (direction == Direction.NEW) {
                 Individual changeIndividual = freshRoModel.createIndividual(
                     generateRandomUriRelatedToResource(freshRO, "change"), ROEVO.Change);
                 changeIndividual.addRDFType(ROEVO.Addition);
                 changeIndividual.addProperty(ROEVO.relatedResource, node);
                 changeSpecificatinIndividual.addProperty(ROEVO.hasChange, changeIndividual);
                 result += freshRO + " " + node.toString() + " " + direction + "\n";
             } else {
                 Individual changeIndividual = freshRoModel.createIndividual(
                     generateRandomUriRelatedToResource(freshRO, "change"), ROEVO.Change);
                 changeIndividual.addRDFType(ROEVO.Removal);
                 changeIndividual.addProperty(ROEVO.relatedResource, node);
                 changeSpecificatinIndividual.addProperty(ROEVO.hasChange, changeIndividual);
                 result += freshRO + " " + node.toString() + " " + direction + "\n";
             }
         }
         //False means there are some changes (Changes exists in two directions so they will be stored onlu once)
         else if (loopResult == false && direction == Direction.NEW) {
             Individual changeIndividual = freshRoModel.createIndividual(
                 generateRandomUriRelatedToResource(freshRO, "change"), ROEVO.Change);
             changeIndividual.addRDFType(ROEVO.Modification);
             changeIndividual.addProperty(ROEVO.relatedResource, node);
             changeSpecificatinIndividual.addProperty(ROEVO.hasChange, changeIndividual);
             result += freshRO + " " + node.toString() + " MODIFICATION" + "\n";
         }
         //True means there are some unchanges objects
         else if (loopResult == false && direction == Direction.NEW) {
             result += freshRO + " " + node.toString() + " UNCHANGED" + "\n";
         }
         return result;
     }
 
 
     private Boolean compareProprties(RDFNode pattern, RDFNode compared, URI patternROURI, URI comparedROURI) {
         if (pattern.isResource() && compared.isResource()) {
             return compareTwoResources(pattern.asResource(), compared.asResource(), patternROURI, comparedROURI);
         } else if (pattern.isLiteral() && compared.isLiteral()) {
             return compareTwoLiterals(pattern.asLiteral(), compared.asLiteral());
         }
         return null;
     }
 
 
     private Boolean compareTwoLiterals(Literal pattern, Literal compared) {
         //@TODO compare checksums
         Boolean result = null;
         if (pattern.equals(compared)) {
             //@TODO compare checksums
             return true;
         }
         return result;
     }
 
 
     private Boolean compareRelativesURI(URI patternURI, URI comparedURI, URI baseForPatternURI, URI baseForComparedURI) {
         return baseForPatternURI.relativize(patternURI).toString()
                 .equals(baseForComparedURI.relativize(comparedURI).toString());
     }
 
 
     private Boolean compareTwoResources(Resource pattern, Resource compared, URI patternROURI, URI comparedROURI) {
         Individual patternSource = pattern.as(Individual.class);
         Individual comparedSource = compared.as(Individual.class);
         if (patternSource.hasRDFType(RO.AggregatedAnnotation) && comparedSource.hasRDFType(RO.AggregatedAnnotation)) {
             try {
                 if (compareRelativesURI(new URI(pattern.getURI()), new URI(compared.getURI()), patternROURI,
                     comparedROURI)) {
                     return compareTwoAggreagatedResources(patternSource, comparedSource, patternROURI, comparedROURI)
                             && compareTwoAggregatedAnnotationBody(patternSource, comparedSource, patternROURI,
                                 comparedROURI);
                 } else {
                     return null;
                 }
             } catch (URISyntaxException e) {
                 log.debug(e);
                 return null;
             }
         } else {
             try {
                 if (compareRelativesURI(new URI(pattern.getURI()), new URI(compared.getURI()), patternROURI,
                     comparedROURI)) {
                     //@TODO compare checksums
                     return true;
                 } else {
                     return null;
                 }
             } catch (URISyntaxException e) {
                 log.info(e);
                 return null;
             }
         }
     }
 
 
     private Boolean compareTwoAggreagatedResources(Individual pattern, Individual compared, URI patternROURI,
             URI comparedROURI) {
         List<RDFNode> patternList = pattern.listPropertyValues(RO.annotatesAggregatedResource).toList();
         List<RDFNode> comparedList = compared.listPropertyValues(RO.annotatesAggregatedResource).toList();
         if (patternList.size() != comparedList.size())
             return false;
         for (RDFNode patternNode : patternList) {
             Boolean result = null;
             for (RDFNode comparedNode : comparedList) {
                 if (comparedNode.isResource() && patternNode.isResource()) {
                     try {
                         if (compareRelativesURI(new URI(patternNode.asResource().getURI()), new URI(comparedNode
                                 .asResource().getURI()), patternROURI, comparedROURI)) {
                             result = true;
                         }
                     } catch (URISyntaxException e) {
                         log.debug(e);
                     }
                 } else if (comparedNode.isLiteral() && patternNode.isLiteral()) {
                     if (compareTwoLiterals(comparedNode.asLiteral(), patternNode.asLiteral())) {
                         result = true;
                     }
                 }
             }
             if (result == null) {
                 return false;
             }
         }
         for (RDFNode comparedNode : comparedList) {
             Boolean result = null;
             for (RDFNode patternNode : patternList) {
                 if (comparedNode.isResource() && patternNode.isResource()) {
                     try {
                         if (compareRelativesURI(new URI(patternNode.asResource().getURI()), new URI(comparedNode
                                 .asResource().getURI()), patternROURI, comparedROURI)) {
                             result = true;
                         }
                     } catch (URISyntaxException e) {
                         log.debug(e);
                         return null;
                     }
                 } else if (comparedNode.isLiteral() && patternNode.isLiteral()) {
                     if (compareTwoLiterals(comparedNode.asLiteral(), patternNode.asLiteral())) {
                         result = true;
                     }
                 }
             }
             if (result == false || result == null) {
                 return false;
             }
         }
         return true;
     }
 
 
     private Boolean compareTwoAggregatedAnnotationBody(Individual patternSource, Individual comparedSource,
             URI patternROURI, URI comparedROURI) {
         Resource patternBody = patternSource.getProperty(AO.body).getResource();
         Resource comparedBody = comparedSource.getProperty(AO.body).getResource();
         OntModel patternModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         RDFFormat patternFormat = RDFFormat.forFileName(patternBody.getURI(), RDFFormat.RDFXML);
         try {
             patternModel.read(patternBody.getURI(), patternBody.getURI(), patternFormat.getName().toUpperCase());
         } catch (JenaException e) {
             log.warn("Could not read annotation body " + patternBody.getURI() + ", assuming unchanged. ", e);
             return true;
         }
         OntModel comparedModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         RDFFormat comparedFormat = RDFFormat.forFileName(comparedBody.getURI(), RDFFormat.RDFXML);
         try {
             comparedModel.read(comparedBody.getURI(), comparedBody.getURI(), comparedFormat.getName().toUpperCase());
         } catch (JenaException e) {
             log.warn("Could not read annotation body " + comparedBody.getURI() + ", assuming unchanged. ", e);
             return true;
         }
 
         List<Statement> patternList = patternModel.listStatements().toList();
         List<Statement> comparedList = comparedModel.listStatements().toList();
 
         for (Statement s : patternList) {
             try {
                 if (!isStatementInList(s, comparedList, patternROURI, comparedROURI)) {
                     return false;
                 }
             } catch (URISyntaxException e) {
                 log.debug(e);
                 return true;
             }
         }
         for (Statement s : comparedList) {
             try {
                 if (!isStatementInList(s, patternList, comparedROURI, patternROURI)) {
                     return false;
                 }
             } catch (URISyntaxException e) {
                 log.debug(e);
                 return true;
             }
         }
         return true;
     }
 
 
     private Boolean isStatementInList(Statement statement, List<Statement> list, URI patternURI, URI comparedURI)
             throws URISyntaxException {
         for (Statement listStatement : list) {
             try {
                 if (compareRelativesURI(new URI(statement.getSubject().asResource().getURI()), new URI(listStatement
                         .getSubject().asResource().getURI()), patternURI, comparedURI)) {
                     if (compareRelativesURI(new URI(statement.getPredicate().asResource().getURI()), new URI(
                             listStatement.getPredicate().asResource().getURI()), patternURI, comparedURI)) {
                         if (statement.getObject().isResource() && listStatement.getObject().isResource()) {
                             if (compareRelativesURI(new URI(statement.getObject().asResource().getURI()), new URI(
                                     listStatement.getObject().asResource().getURI()), patternURI, comparedURI)) {
                                 return true;
                             }
                         }
                         if (listStatement.getObject().isLiteral() && statement.getObject().isLiteral()) {
                             if (compareTwoLiterals(listStatement.getObject().asLiteral(), statement.getObject()
                                     .asLiteral())) {
                                 return true;
                             }
                         }
                     }
                 }
             } finally {
                 continue;
             }
         }
         return false;
     }
 
 
     @Override
     public URI resolveURI(URI base, String second) {
         if ("file".equals(base.getScheme())) {
             URI incorrectURI = base.resolve(second);
             return URI.create(incorrectURI.toString().replaceFirst("file:/", "file:///"));
         } else {
             return base.resolve(second);
         }
     }
 
 
     @Override
     public int changeURIInManifestAndAnnotationBodies(ResearchObject researchObject, URI oldURI, URI newURI,
             Boolean withBodies) {
         int cnt = changeURIInNamedGraph(researchObject.getManifestUri(), oldURI, newURI);
         if (withBodies) {
             OntModel model = createOntModelForNamedGraph(researchObject.getManifestUri());
             List<RDFNode> bodies = model.listObjectsOfProperty(AO.body).toList();
             for (RDFNode body : bodies) {
                 URI bodyURI = URI.create(body.asResource().getURI());
                 cnt += changeURIInNamedGraph(bodyURI, oldURI, newURI);
             }
         }
         return cnt;
     }
 
 
     private int changeURIInNamedGraph(URI graph, URI oldURI, URI newURI) {
         OntModel model = createOntModelForNamedGraph(graph);
         Resource oldResource = model.createResource(SafeURI.URItoString(oldURI));
         Resource newResource = model.createResource(SafeURI.URItoString(newURI));
 
         List<Statement> s1 = model.listStatements(oldResource, null, (RDFNode) null).toList();
         for (Statement s : s1) {
             model.remove(s.getSubject(), s.getPredicate(), s.getObject());
             model.add(newResource, s.getPredicate(), s.getObject());
         }
         List<Statement> s2 = model.listStatements(null, null, oldResource).toList();
         for (Statement s : s2) {
             model.remove(s.getSubject(), s.getPredicate(), s.getObject());
             model.add(s.getSubject(), s.getPredicate(), newResource);
         }
         return s1.size() + s2.size();
     }
 
 
     public InputStream getEvoInfo(ResearchObject researchObject) {
         return getNamedGraph((researchObject.getFixedEvolutionAnnotationBodyPath()), RDFFormat.TURTLE);
     }
 
 
     @Override
     public List<AggregatedResource> getAggregatedResources(ResearchObject researchObject)
             throws ManifestTraversingException {
         OntModel model = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual source = model.getIndividual(researchObject.getUri().toString());
         if (source == null) {
             throw new ManifestTraversingException();
         }
         List<RDFNode> aggregatesList = source.listPropertyValues(ORE.aggregates).toList();
         List<AggregatedResource> aggregated = new ArrayList<AggregatedResource>();
         for (RDFNode node : aggregatesList) {
             try {
                 if (node.isURIResource()) {
                     if (!isAnnotation(researchObject, new URI(node.asResource().getURI()))) {
                         aggregated.add(new AggregatedResource(new URI(node.asResource().getURI())));
                     }
                 } else if (node.isResource()) {
                     URI nodeUri = changeBlankNodeToUriResources(researchObject, model, node);
                     if (!isAnnotation(researchObject, nodeUri)) {
                         aggregated.add(new AggregatedResource(nodeUri));
                     }
                 }
             } catch (URISyntaxException e) {
                 continue;
             }
         }
         return aggregated;
     }
 
 
     private URI changeBlankNodeToUriResources(ResearchObject researchObject, OntModel model, RDFNode node)
             throws URISyntaxException {
         Resource r = model.createResource(researchObject.getUri().resolve(UUID.randomUUID().toString()).toString());
         List<Statement> statements = new ArrayList<Statement>();
         for (Statement s : model.listStatements(node.asResource(), null, (RDFNode) null).toList()) {
             Statement s2 = model.createStatement(r, s.getPredicate(), s.getObject());
             statements.add(s);
             model.add(s2);
         }
 
         for (Statement s : model.listStatements(null, null, node).toList()) {
             Statement s2 = model.createStatement(s.getSubject(), s.getPredicate(), r);
             model.add(s2);
             statements.add(s);
         }
         model.remove(statements);
         return new URI(r.getURI());
     }
 
 
     @Override
     public List<Annotation> getAnnotations(ResearchObject researchObject)
             throws ManifestTraversingException {
         OntModel model = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual source = model.getIndividual(researchObject.getUri().toString());
         if (source == null) {
             throw new ManifestTraversingException();
         }
         List<RDFNode> aggregatesList = source.listPropertyValues(ORE.aggregates).toList();
         List<Annotation> annotations = new ArrayList<Annotation>();
         for (RDFNode node : aggregatesList) {
             try {
                 if (node.isURIResource()) {
                     if (isAnnotation(researchObject, new URI(node.asResource().getURI()))) {
                         annotations.add(new Annotation(new URI(node.asResource().getURI()), model));
                     }
                 } else if (node.isResource()) {
                     URI nodeUri = changeBlankNodeToUriResources(researchObject, model, node);
                     if (isAnnotation(researchObject, nodeUri)) {
                         annotations.add(new Annotation(nodeUri, model));
                     }
                 }
             } catch (URISyntaxException e) {
                 continue;
             }
         }
         return annotations;
     }
 
 
     @Override
     public Folder addFolder(ResearchObject researchObject, Folder folder) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         if (ro == null) {
             throw new IllegalArgumentException("URI not found: " + researchObject.getUri());
         }
         folder.setAggregationUri(researchObject.getUri());
         Individual resource = manifestModel.createIndividual(folder.getUri().toString(), RO.Folder);
         resource.addRDFType(RO.Resource);
         manifestModel.add(ro, ORE.aggregates, resource);
         if (!ro.hasProperty(RO.rootFolder)) {
             manifestModel.add(ro, RO.rootFolder, resource);
         }
         URI proxyURI = generateProxyURI(researchObject);
         Individual proxy = manifestModel.createIndividual(proxyURI.toString(), ORE.Proxy);
         manifestModel.add(proxy, ORE.proxyIn, ro);
         manifestModel.add(proxy, ORE.proxyFor, resource);
         folder.setProxyUri(proxyURI);
 
         OntModel folderModel = createOntModelForNamedGraph(folder.getResourceMapUri());
         Resource manifestRes = folderModel.createResource(researchObject.getManifestUri().toString());
         Individual roInd = folderModel.createIndividual(researchObject.getUri().toString(), RO.ResearchObject);
         folderModel.add(roInd, ORE.isDescribedBy, manifestRes);
 
         Resource folderRMRes = folderModel.createResource(folder.getResourceMapUri().toString());
         Individual folderInd = folderModel.createIndividual(folder.getUri().toString(), RO.Folder);
         folderInd.addRDFType(ORE.Aggregation);
         folderModel.add(folderInd, ORE.isAggregatedBy, roInd);
         folderModel.add(folderInd, ORE.isDescribedBy, folderRMRes);
 
         for (FolderEntry entry : folder.getFolderEntries()) {
             addFolderEntry(folder, folderInd, entry);
         }
         return folder;
     }
 
 
     @Override
     public void generateEvoInformation(ResearchObject researchObject, ResearchObject liveRO, EvoType type) {
         switch (type) {
             case LIVE:
                 generateLiveRoEvoInf(researchObject);
                 break;
             case SNAPSHOT:
                 generateSnaphotEvoInf(researchObject, liveRO);
                 break;
             case ARCHIVED:
                 generateArchiveEvoInf(researchObject, liveRO);
                 break;
         }
     }
 
 
     private void generateLiveRoEvoInf(ResearchObject researchObject) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual manifest = manifestModel.getIndividual(researchObject.getManifestUri().toString());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         OntModel evoModel = createOntModelForNamedGraph(researchObject.getFixedEvolutionAnnotationBodyPath());
         Individual evoInfo = evoModel.getIndividual(researchObject.getFixedEvolutionAnnotationBodyPath().toString());
         if (evoInfo != null) {
             throw new IllegalArgumentException("URI already exists: "
                     + researchObject.getFixedEvolutionAnnotationBodyPath());
         }
         evoInfo = manifestModel.createIndividual(researchObject.getFixedEvolutionAnnotationBodyPath().toString(),
             ORE.AggregatedResource);
         Individual roEvo = evoModel.createIndividual(researchObject.getUri().toString(), ROEVO.LiveRO);
 
         manifestModel.add(evoInfo, ORE.describes, ro);
         manifestModel.add(evoInfo, DCTerms.created, evoModel.createTypedLiteral(Calendar.getInstance()));
 
         addAnnotation(researchObject, Arrays.asList(researchObject.getUri()),
             researchObject.getFixedEvolutionAnnotationBodyPath()).toString();
         ro.addProperty(ORE.aggregates,
             manifestModel.getResource(researchObject.getFixedEvolutionAnnotationBodyPath().toString()));
     }
 
 
     private void generateSnaphotEvoInf(ResearchObject researchObject, ResearchObject liveRO) {
 
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual manifest = manifestModel.getIndividual(researchObject.getManifestUri().toString());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         OntModel evoModel = createOntModelForNamedGraph(researchObject.getFixedEvolutionAnnotationBodyPath());
         Individual evoInfo = evoModel.getIndividual(researchObject.getFixedEvolutionAnnotationBodyPath().toString());
         if (evoInfo != null) {
             throw new IllegalArgumentException("URI already exists: "
                     + researchObject.getFixedEvolutionAnnotationBodyPath());
         }
         evoInfo = manifestModel.createIndividual(researchObject.getFixedEvolutionAnnotationBodyPath().toString(),
             ORE.AggregatedResource);
 
         Individual roEvo = evoModel.createIndividual(researchObject.getUri().toString(), ROEVO.SnapshotRO);
 
         manifestModel.add(evoInfo, ORE.describes, ro);
         manifestModel.add(evoInfo, DCTerms.created, evoModel.createTypedLiteral(Calendar.getInstance()));
 
         evoModel.add(ro, ROEVO.isSnapshotOf, liveRO.getUri().toString());
         evoModel.add(ro, ROEVO.snapshottedAtTime, evoModel.createTypedLiteral(Calendar.getInstance()));
         evoModel.add(ro, ROEVO.snapshottedBy, evoModel.createResource(user.getUri().toString()));
 
         addAnnotation(researchObject, Arrays.asList(researchObject.getUri()),
             researchObject.getFixedEvolutionAnnotationBodyPath()).toString();
         ro.addProperty(ORE.aggregates, researchObject.getFixedEvolutionAnnotationBodyPath().toString());
         OntModel liveEvoModel = createOntModelForNamedGraph(liveRO.getFixedEvolutionAnnotationBodyPath());
         liveEvoModel.add(createOntModelForNamedGraph(researchObject.getManifestUri())
                 .getResource(liveRO.getUriString()), ROEVO.hasSnapshot, ro);
 
         try {
             if (getPreviousSnaphotOrArchive(liveRO, researchObject) != null) {
                 storeAggregatedDifferences(researchObject,
                     ResearchObject.create(getPreviousSnaphotOrArchive(liveRO, researchObject)));
             }
         } catch (URISyntaxException | IOException e) {
             //any way to informa about the problem?
             e.printStackTrace();
         }
     }
 
 
     private void generateArchiveEvoInf(ResearchObject researchObject, ResearchObject liveRO) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual manifest = manifestModel.getIndividual(researchObject.getManifestUri().toString());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         OntModel evoModel = createOntModelForNamedGraph(researchObject.getFixedEvolutionAnnotationBodyPath());
         Individual evoInfo = evoModel.getIndividual(researchObject.getFixedEvolutionAnnotationBodyPath().toString());
         if (evoInfo != null) {
             throw new IllegalArgumentException("URI already exists: "
                     + researchObject.getFixedEvolutionAnnotationBodyPath());
         }
         evoInfo = manifestModel.createIndividual(researchObject.getFixedEvolutionAnnotationBodyPath().toString(),
             ORE.AggregatedResource);
 
         Individual roEvo = evoModel.createIndividual(researchObject.getUri().toString(), ROEVO.ArchivedRO);
 
         manifestModel.add(evoInfo, ORE.describes, ro);
         manifestModel.add(evoInfo, DCTerms.created, evoModel.createTypedLiteral(Calendar.getInstance()));
 
         evoModel.add(ro, ROEVO.isArchiveOf, liveRO.getUri().toString());
         evoModel.add(ro, ROEVO.archivedAtTime, evoModel.createTypedLiteral(Calendar.getInstance()));
         evoModel.add(ro, ROEVO.archivedBy, evoModel.createResource(user.getUri().toString()));
 
         addAnnotation(researchObject, Arrays.asList(researchObject.getUri()),
             researchObject.getFixedEvolutionAnnotationBodyPath()).toString();
         ro.addProperty(ORE.aggregates, researchObject.getFixedEvolutionAnnotationBodyPath().toString());
         OntModel liveEvoModel = createOntModelForNamedGraph(liveRO.getFixedEvolutionAnnotationBodyPath());
         liveEvoModel.add(createOntModelForNamedGraph(researchObject.getManifestUri())
                 .getResource(liveRO.getUriString()), ROEVO.hasArchive, ro);
         try {
             if (getPreviousSnaphotOrArchive(liveRO, researchObject) != null) {
                 storeAggregatedDifferences(researchObject,
                     ResearchObject.create(getPreviousSnaphotOrArchive(liveRO, researchObject)));
             }
         } catch (URISyntaxException | IOException e) {
             //any way to informa about the problem?
             e.printStackTrace();
         }
     }
 
 
     @Override
     public Annotation findAnnotationForBody(ResearchObject researchObject, URI body) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Resource bodyR = manifestModel.createResource(SafeURI.URItoString(body));
         StmtIterator it = manifestModel.listStatements(null, AO.body, bodyR);
         if (!it.hasNext()) {
             return null;
         }
         return new Annotation(URI.create(it.next().getSubject().getURI()));
     }
 
 
     @Override
     public boolean addAnnotationBody(ResearchObject researchObject, URI graphURI, InputStream inputStream,
             RDFFormat rdfFormat) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         if (ro == null) {
             throw new IllegalArgumentException("URI not found: " + researchObject.getUri());
         }
         Resource resource = manifestModel.createIndividual(graphURI.toString(), ORE.AggregatedResource);
         manifestModel.add(ro, ORE.aggregates, resource);
         manifestModel.add(resource, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
         manifestModel.add(resource, DCTerms.creator, manifestModel.createResource(user.getUri().toString()));
         return addNamedGraph(graphURI, inputStream, rdfFormat);
     }
 
 
     @Override
     public void removeAnnotationBody(ResearchObject researchObject, URI graphURI) {
         graphURI = graphURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         if (ro == null) {
             throw new IllegalArgumentException("URI not found: " + researchObject.getUri());
         }
         Resource resource = manifestModel.getResource(graphURI.toString());
         if (resource == null) {
             throw new IllegalArgumentException("URI not found: " + graphURI);
         }
         manifestModel.remove(ro, ORE.aggregates, resource);
 
         URI proxy = getProxyForResource(researchObject, graphURI);
         if (proxy != null) {
             deleteProxy(researchObject, proxy);
         }
         if (graphset.containsGraph(graphURI.toString())) {
             graphset.removeGraph(graphURI.toString());
         }
     }
 
 
     @Override
     public Folder getRootFolder(ResearchObject researchObject) {
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         Individual ro = manifestModel.getIndividual(researchObject.getUri().toString());
         Resource r = ro.getPropertyResourceValue(RO.rootFolder);
         if (r == null) {
             return null;
         } else {
             return getFolder(URI.create(r.getURI()));
         }
     }
 
 
     @Override
     public Folder getFolder(URI folderURI) {
         Folder folder = new Folder();
         folder.setUri(folderURI);
         if (!containsNamedGraph(folder.getResourceMapUri())) {
             return null;
         }
         OntModel model = createOntModelForNamedGraph(folder.getResourceMapUri());
         Individual folderI = model.getIndividual(folder.getUri().toString());
         List<RDFNode> aggregations = folderI.listPropertyValues(ORE.isAggregatedBy).toList();
         Individual ro = null;
         for (RDFNode node : aggregations) {
             if (node.isURIResource() && node.as(Individual.class).hasRDFType(RO.ResearchObject)) {
                 ro = node.as(Individual.class);
                 break;
             }
         }
         // this would work if we turned inference on
         //        ObjectProperty aggregatedByRO = model.createObjectProperty(RO.NAMESPACE.concat("aggregatedByRO"), true);
         //        aggregatedByRO.addSuperProperty(ORE.isAggregatedBy);
         //        aggregatedByRO.addRange(RO.ResearchObject);
         //        Resource ro = folderI.getPropertyResourceValue(aggregatedByRO);
         URI roURI = URI.create(ro.getURI());
         folder.setAggregationUri(roURI);
         URI proxyURI = getProxyForResource(ResearchObject.create(roURI), folder.getUri());
         folder.setProxyUri(proxyURI);
 
         List<Individual> entries = model.listIndividuals(RO.FolderEntry).toList();
         for (Individual entryI : entries) {
             String proxyIn = entryI.getPropertyResourceValue(ORE.proxyIn).getURI();
             if (!proxyIn.equals(folder.getUri().toString())) {
                 continue;
             }
             FolderEntry entry = new FolderEntry();
             entry.setUri(URI.create(entryI.getURI()));
             entry.setProxyIn(URI.create(proxyIn));
             String proxyFor = entryI.getPropertyResourceValue(ORE.proxyFor).getURI();
             entry.setProxyFor(URI.create(proxyFor));
             String name = entryI.getPropertyValue(RO.entryName).asLiteral().getString();
             entry.setEntryName(name);
             folder.getFolderEntries().add(entry);
         }
 
         return folder;
     }
 
 
     @Override
     public void updateFolder(Folder folder) {
         OntModel folderModel = createOntModelForNamedGraph(folder.getResourceMapUri());
         Individual folderInd = folderModel.createIndividual(folder.getUri().toString(), RO.Folder);
         List<Individual> entries = folderModel.listIndividuals(RO.FolderEntry).toList();
         for (Individual entry : entries) {
             deleteFolderEntry(entry);
         }
 
         for (FolderEntry entry : folder.getFolderEntries()) {
             addFolderEntry(folder, folderInd, entry);
         }
     }
 
 
     /**
      * Save a folder entry in the triplestore.
      * 
      * @param folder
      *            folder that contains the entry
      * @param folderInd
      *            folder individual
      * @param entry
      *            entry
      */
     private void addFolderEntry(Folder folder, Individual folderInd, FolderEntry entry) {
         OntModel folderModel = folderInd.getOntModel();
         if (entry.getUri() == null) {
             entry.setUri(folder.getUri().resolve("entries/" + UUID.randomUUID()));
         }
        entry.setProxyIn(folder.getUri());
         Individual entryInd = folderModel.createIndividual(entry.getUri().toString(), RO.FolderEntry);
         entryInd.addRDFType(ORE.Proxy);
         //FIXME we should check if the resource is really aggregated and what classes it has
         Individual resInd = folderModel.createIndividual(entry.getProxyFor().toString(), RO.Resource);
         folderModel.add(folderInd, ORE.aggregates, resInd);
         folderModel.add(resInd, ORE.isAggregatedBy, folderInd);
         folderModel.add(entryInd, ORE.proxyFor, resInd);
         folderModel.add(entryInd, ORE.proxyIn, folderInd);
         Literal name = folderModel.createLiteral(entry.getEntryName());
         folderModel.add(entryInd, RO.entryName, name);
 
         OntModel entryModel = createOntModelForNamedGraph(entry.getUri());
         entryInd = entryModel.createIndividual(entry.getUri().toString(), RO.FolderEntry);
         entryInd.addProperty(ORE.isAggregatedBy, folderInd);
     }
 
 
     /**
      * Delete a folder entry.
      * 
      * @param entry
      *            entry
      */
     private void deleteFolderEntry(Individual entry) {
         entry.remove();
         removeNamedGraph(URI.create(entry.getURI()));
     }
 
 
     @Override
     public void deleteFolder(Folder folder) {
         ResearchObject researchObject = ResearchObject.create(folder.getAggregationUri());
         OntModel manifestModel = createOntModelForNamedGraph(researchObject.getManifestUri());
         manifestModel.getIndividual(folder.getUri().toString()).remove();
 
         OntModel folderModel = createOntModelForNamedGraph(folder.getResourceMapUri());
         List<Individual> entries = folderModel.listIndividuals(RO.FolderEntry).toList();
         for (Individual entry : entries) {
             deleteFolderEntry(entry);
         }
 
         removeNamedGraph(folder.getResourceMapUri());
     }
 
 
     @Override
     public FolderEntry getFolderEntry(URI entryUri) {
         OntModel entryModel = createOntModelForNamedGraph(entryUri);
         Individual entryInd = entryModel.getIndividual(entryUri.toString());
         if (entryInd == null) {
             return null;
         }
         Resource folderR = entryInd.getPropertyResourceValue(ORE.isAggregatedBy);
         Folder folder = getFolder(URI.create(folderR.getURI()));
         for (FolderEntry entry : folder.getFolderEntries()) {
             if (entry.getUri().equals(entryUri)) {
                 return entry;
             }
         }
         return null;
     }
 }
