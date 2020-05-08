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
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
 import pl.psnc.dl.wf4ever.dlibra.UserProfile;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntClass;
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
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.shared.PrefixMapping;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.ng4j.sparql.NamedGraphDataset;
 
 /**
  * @author piotrhol
  * 
  */
 public class SemanticMetadataServiceImpl implements SemanticMetadataService {
 
     private static final Logger log = Logger.getLogger(SemanticMetadataServiceImpl.class);
 
     private static final Syntax sparqlSyntax = Syntax.syntaxARQ;
 
     private static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";
 
     private static final String RO_NAMESPACE = "http://purl.org/wf4ever/ro#";
 
     private static final String ROEVO_NAMESPACE = "http://purl.org/wf4ever/roevo#";
 
     private static final String FOAF_NAMESPACE = "http://xmlns.com/foaf/0.1/";
 
     private static final String AO_NAMESPACE = "http://purl.org/ao/";
 
     private static final URI DEFAULT_NAMED_GRAPH_URI = URI.create("sms");
 
     private static final PrefixMapping standardNamespaces = PrefixMapping.Factory.create()
             .setNsPrefix("ore", ORE_NAMESPACE).setNsPrefix("ro", RO_NAMESPACE).setNsPrefix("roevo", ROEVO_NAMESPACE)
             .setNsPrefix("dcterms", DCTerms.NS).setNsPrefix("foaf", FOAF_NAMESPACE).lock();
 
     private final NamedGraphSet graphset;
 
     private static final OntModel defaultModel = ModelFactory.createOntologyModel(
         new OntModelSpec(OntModelSpec.OWL_MEM),
         ModelFactory.createDefaultModel().read(RO_NAMESPACE).read(ROEVO_NAMESPACE));
 
     private static final OntClass researchObjectClass = defaultModel.getOntClass(RO_NAMESPACE + "ResearchObject");
 
     private static final OntClass liveROClass = defaultModel.getOntClass(ROEVO_NAMESPACE + "LiveRO");
 
     private static final OntClass snapshotROClass = defaultModel.getOntClass(ROEVO_NAMESPACE + "SnapshotRO");
 
     private static final OntClass archivedROClass = defaultModel.getOntClass(ROEVO_NAMESPACE + "ArchivedRO");
 
     private static final OntClass manifestClass = defaultModel.getOntClass(RO_NAMESPACE + "Manifest");
 
     private static final OntClass resourceClass = defaultModel.getOntClass(RO_NAMESPACE + "Resource");
 
     private static final OntClass roFolderClass = defaultModel.getOntClass(RO_NAMESPACE + "Folder");
 
     private static final OntClass roAggregatedAnnotationClass = defaultModel.getOntClass(RO_NAMESPACE
             + "AggregatedAnnotation");
 
     private static final OntClass oreProxyClass = defaultModel.getOntClass(ORE_NAMESPACE + "Proxy");
 
     private static final OntClass foafAgentClass = defaultModel.getOntClass(FOAF_NAMESPACE + "Agent");
 
     private static final OntClass foafPersonClass = defaultModel.getOntClass(FOAF_NAMESPACE + "Person");
 
     private static final Property describes = defaultModel.getProperty(ORE_NAMESPACE + "describes");
 
     private static final Property isDescribedBy = defaultModel.getProperty(ORE_NAMESPACE + "isDescribedBy");
 
     private static final Property aggregates = defaultModel.getProperty(ORE_NAMESPACE + "aggregates");
 
     private static final Property oreProxyIn = defaultModel.getProperty(ORE_NAMESPACE + "proxyIn");
 
     private static final Property oreProxyFor = defaultModel.getProperty(ORE_NAMESPACE + "proxyFor");
 
     private static final Property foafName = defaultModel.getProperty(FOAF_NAMESPACE + "name");
 
     private static final Property name = defaultModel.getProperty(RO_NAMESPACE + "name");
 
     private static final Property filesize = defaultModel.getProperty(RO_NAMESPACE + "filesize");
 
     private static final Property checksum = defaultModel.getProperty(RO_NAMESPACE + "checksum");
 
     private static final Property aoBody = defaultModel.getProperty(AO_NAMESPACE + "body");
 
     private static final Property roevoIsSnapshotOf = defaultModel.getProperty(ROEVO_NAMESPACE + "isSnapshotOf");
 
     private static final Property roevoHasSnapshot = defaultModel.getProperty(ROEVO_NAMESPACE + "hasSnapshot");
 
     private static final Property roevoSnapshottedAtTime = defaultModel.getProperty(ROEVO_NAMESPACE
             + "snapshottedAtTime");
 
     private static final Property roevoArchivedBy = defaultModel.getProperty(ROEVO_NAMESPACE + "archivedBy");
 
     private static final Property roevoIsArchiveOf = defaultModel.getProperty(ROEVO_NAMESPACE + "isArchiveOf");
 
     private static final Property roevoHasArchive = defaultModel.getProperty(ROEVO_NAMESPACE + "hasArchive");
 
     private static final Property roevoArchivedAtTime = defaultModel.getProperty(ROEVO_NAMESPACE + "archivedAtTime");
 
     private static final Property roevoSnapshottedBy = defaultModel.getProperty(ROEVO_NAMESPACE + "snapshottedBy");
 
     private static final Property provHadOriginalSource = defaultModel
             .getProperty("http://www.w3.org/ns/prov#hadOriginalSource");
 
     public static final Property annotatesAggregatedResource = defaultModel.getProperty(RO_NAMESPACE
             + "annotatesAggregatedResource");
 
     private final String getResourceQueryTmpl = "DESCRIBE <%s> WHERE { }";
 
     private final String getUserQueryTmpl = "DESCRIBE <%s> WHERE { }";
 
     private final String findResearchObjectsQueryTmpl = "PREFIX ro: <" + RO_NAMESPACE + "> SELECT ?ro "
             + "WHERE { ?ro a ro:ResearchObject. FILTER regex(str(?ro), \"^%s\") . }";
 
     private final String findResearchObjectsByCreatorQueryTmpl = "PREFIX ro: <" + RO_NAMESPACE + "> PREFIX dcterms: <"
             + DCTerms.NS + "> SELECT ?ro WHERE { ?ro a ro:ResearchObject ; dcterms:creator <%s> . }";
 
     private final Connection connection;
 
     private final UserProfile user;
 
 
     public SemanticMetadataServiceImpl(UserProfile user)
             throws IOException, NamingException, SQLException, ClassNotFoundException {
         this.user = user;
         connection = getConnection("connection.properties");
         if (connection == null) {
             throw new RuntimeException("Connection could not be created");
         }
 
         graphset = new NamedGraphSetDB(connection, "sms");
 
         defaultModel.setNsPrefixes(standardNamespaces);
 
         createUserProfile(user);
     }
 
 
     private void createUserProfile(UserProfile user) {
         if (!containsNamedGraph(user.getUri())) {
             OntModel userModel = createOntModelForNamedGraph(user.getUri());
             userModel.removeAll();
             Individual agent = userModel.createIndividual(user.getUri().toString(), foafAgentClass);
             userModel.add(agent, foafName, user.getName());
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
                 log.debug("" + this + " opens a connection");
                 return DriverManager.getConnection(url, username, password);
             }
         }
 
         return null;
     }
 
 
     @Override
     public UserProfile getUserProfile() {
         return user;
     }
 
 
     @Override
     public void createResearchObject(URI researchObjectURI) {
         createLiveResearchObject(researchObjectURI, null);
     }
 
 
     @Override
     public void createLiveResearchObject(URI researchObjectURI, URI source) {
         URI manifestURI = getManifestURI(researchObjectURI.normalize());
         OntModel manifestModel = createOntModelForNamedGraph(manifestURI);
         Individual manifest = manifestModel.getIndividual(manifestURI.toString());
         if (manifest != null) {
             throw new IllegalArgumentException("URI already exists: " + manifestURI);
         }
         manifest = manifestModel.createIndividual(manifestURI.toString(), manifestClass);
         Individual ro = manifestModel.createIndividual(researchObjectURI.toString(), researchObjectClass);
         ro.addRDFType(liveROClass);
 
         manifestModel.add(ro, isDescribedBy, manifest);
         manifestModel.add(ro, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
         manifestModel.add(ro, DCTerms.creator, manifestModel.createResource(user.getUri().toString()));
 
         manifestModel.add(manifest, describes, ro);
         manifestModel.add(manifest, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 
         if (source != null) {
             manifestModel.add(ro, provHadOriginalSource, manifestModel.createResource(source.toString()));
         }
     }
 
 
     @Override
     public void createSnapshotResearchObject(URI researchObject, URI liveROURI) {
         URI manifestURI = getManifestURI(researchObject.normalize());
         OntModel manifestModel = createOntModelForNamedGraph(manifestURI);
         Individual manifest = manifestModel.getIndividual(manifestURI.toString());
         if (manifest != null) {
             throw new IllegalArgumentException("URI already exists: " + manifestURI);
         }
 
         URI liveManifestURI = getManifestURI(liveROURI.normalize());
         OntModel liveManifestModel = createOntModelForNamedGraph(liveManifestURI);
         Individual liveManifest = liveManifestModel.getIndividual(liveManifestURI.toString());
 
         manifest = manifestModel.createIndividual(manifestURI.toString(), manifestClass);
         Individual ro = manifestModel.createIndividual(researchObject.toString(), researchObjectClass);
         ro.addRDFType(snapshotROClass);
 
         RDFNode creator, created;
         Resource liveRO;
         if (liveManifest == null) {
             log.warn("Live RO is not an RO: " + liveROURI);
             liveRO = manifestModel.createResource(liveROURI.toString());
             creator = manifestModel.createResource(user.getUri().toString());
             created = manifestModel.createTypedLiteral(Calendar.getInstance());
         } else {
             liveRO = liveManifestModel.getIndividual(liveROURI.toString());
             if (liveRO == null) {
                 throw new IllegalArgumentException("Live RO does not describe the research object");
             }
             creator = liveRO.getPropertyResourceValue(DCTerms.creator);
             created = liveRO.as(Individual.class).getPropertyValue(DCTerms.created);
             liveManifestModel.add(liveRO, roevoHasSnapshot, ro);
         }
 
         manifestModel.add(ro, isDescribedBy, manifest);
         manifestModel.add(ro, DCTerms.created, created);
         manifestModel.add(ro, DCTerms.creator, creator);
 
         manifestModel.add(manifest, describes, ro);
         manifestModel.add(manifest, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 
         manifestModel.add(ro, roevoIsSnapshotOf, liveRO);
         manifestModel.add(ro, roevoSnapshottedAtTime, manifestModel.createTypedLiteral(Calendar.getInstance()));
         manifestModel.add(ro, roevoSnapshottedBy, manifestModel.createResource(user.getUri().toString()));
 
         //TODO add wasRevisionOf
     }
 
 
     @Override
     public void createArchivedResearchObject(URI researchObject, URI liveROURI) {
         URI manifestURI = getManifestURI(researchObject.normalize());
         OntModel manifestModel = createOntModelForNamedGraph(manifestURI);
         Individual manifest = manifestModel.getIndividual(manifestURI.toString());
         if (manifest != null) {
             throw new IllegalArgumentException("URI already exists: " + manifestURI);
         }
 
         URI liveManifestURI = getManifestURI(liveROURI.normalize());
         OntModel liveManifestModel = createOntModelForNamedGraph(liveManifestURI);
         Individual liveManifest = liveManifestModel.getIndividual(liveManifestURI.toString());
 
         manifest = manifestModel.createIndividual(manifestURI.toString(), manifestClass);
         Individual ro = manifestModel.createIndividual(researchObject.toString(), researchObjectClass);
         ro.addRDFType(archivedROClass);
 
         RDFNode creator, created;
         Resource liveRO;
         if (liveManifest == null) {
             log.warn("Live RO is not an RO: " + liveROURI);
             liveRO = manifestModel.createResource(liveROURI.toString());
             creator = manifestModel.createResource(user.getUri().toString());
             created = manifestModel.createTypedLiteral(Calendar.getInstance());
         } else {
             liveRO = liveManifestModel.getIndividual(liveROURI.toString());
             if (liveRO == null) {
                 throw new IllegalArgumentException("Live RO does not describe the research object");
             }
             creator = liveRO.getPropertyResourceValue(DCTerms.creator);
             created = liveRO.as(Individual.class).getPropertyValue(DCTerms.created);
             liveManifestModel.add(liveRO, roevoHasArchive, ro);
         }
 
         manifestModel.add(ro, isDescribedBy, manifest);
         manifestModel.add(ro, DCTerms.created, created);
         manifestModel.add(ro, DCTerms.creator, creator);
 
         manifestModel.add(manifest, describes, ro);
         manifestModel.add(manifest, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 
         manifestModel.add(ro, roevoIsArchiveOf, liveRO);
         manifestModel.add(ro, roevoArchivedAtTime, manifestModel.createTypedLiteral(Calendar.getInstance()));
         manifestModel.add(ro, roevoArchivedBy, manifestModel.createResource(user.getUri().toString()));
 
         //TODO add wasRevisionOf
     }
 
 
     @Override
     public void updateManifest(URI manifestURI, InputStream is, RDFFormat rdfFormat) {
         // TODO validate the manifest?
         addNamedGraph(manifestURI, is, rdfFormat);
 
         //
         // // leave only one dcterms:created - the earliest
         // Individual manifest = manifestModel.getIndividual(manifestURI.toString());
         // NodeIterator it = manifestModel.listObjectsOfProperty(manifest,
         // DCTerms.created);
         // Calendar earliest = null;
         // while (it.hasNext()) {
         // Calendar created = ((XSDDateTime)
         // it.next().asLiteral().getValue()).asCalendar();
         // if (earliest == null || created.before(earliest))
         // earliest = created;
         // }
         // if (earliest != null) {
         // manifestModel.removeAll(manifest, DCTerms.created, null);
         // manifestModel.add(manifest, DCTerms.created,
         // manifestModel.createTypedLiteral(earliest));
         // }
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#getManifest(java.net.URI,
      * pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation)
      */
     @Override
     public InputStream getManifest(URI manifestURI, RDFFormat rdfFormat) {
         return getNamedGraph(manifestURI, rdfFormat);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#addResource(java.net.URI,
      * java.net.URI, pl.psnc.dl.wf4ever.dlibra.ResourceInfo)
      */
     @Override
     public boolean addResource(URI roURI, URI resourceURI, ResourceInfo resourceInfo) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(roURI.normalize()));
         Individual ro = manifestModel.getIndividual(roURI.toString());
         if (ro == null) {
             throw new IllegalArgumentException("URI not found: " + roURI);
         }
         boolean created = false;
         Individual resource = manifestModel.getIndividual(resourceURI.toString());
         if (resource == null) {
             created = true;
             resource = manifestModel.createIndividual(resourceURI.toString(), resourceClass);
         }
         manifestModel.add(ro, aggregates, resource);
         if (resourceInfo != null) {
             if (resourceInfo.getName() != null) {
                 manifestModel.add(resource, name, manifestModel.createTypedLiteral(resourceInfo.getName()));
             }
             manifestModel.add(resource, filesize, manifestModel.createTypedLiteral(resourceInfo.getSizeInBytes()));
             if (resourceInfo.getChecksum() != null && resourceInfo.getDigestMethod() != null) {
                 manifestModel.add(resource, checksum, manifestModel.createResource(String.format("urn:%s:%s",
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
     public void removeResource(URI roURI, URI resourceURI) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(roURI.normalize()));
         Individual ro = manifestModel.getIndividual(roURI.toString());
         if (ro == null) {
             throw new IllegalArgumentException("URI not found: " + roURI);
         }
         Individual resource = manifestModel.getIndividual(resourceURI.toString());
         if (resource == null) {
             throw new IllegalArgumentException("URI not found: " + resourceURI);
         }
         manifestModel.remove(ro, aggregates, resource);
 
         StmtIterator it = manifestModel.listStatements(null, aggregates, resource);
         if (!it.hasNext()) {
             manifestModel.removeAll(resource, null, null);
         }
 
         ResIterator it2 = manifestModel.listSubjectsWithProperty(annotatesAggregatedResource, resource);
         while (it2.hasNext()) {
             Resource ann = it2.next();
             manifestModel.remove(ann, annotatesAggregatedResource, resource);
             if (!ann.hasProperty(annotatesAggregatedResource)) {
                 Resource annBody = ann.getPropertyResourceValue(aoBody);
                 if (annBody != null && annBody.isURIResource()) {
                     URI annBodyURI = URI.create(annBody.getURI());
                     if (containsNamedGraph(annBodyURI)) {
                         removeNamedGraph(roURI, annBodyURI);
                     }
                 }
                 manifestModel.removeAll(ann, null, null);
                 manifestModel.removeAll(null, null, ann);
             }
         }
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#getResource(java.net.URI,
      * pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation)
      */
     @Override
     public InputStream getResource(URI roURI, URI resourceURI, RDFFormat rdfFormat) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(roURI.normalize()));
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
 
 
     /*
      * (non-Javadoc)
      * 
      * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#getAnnotationBody(java
      * .net.URI, pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation)
      */
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
     public InputStream getNamedGraphWithRelativeURIs(URI namedGraphURI, URI researchObjectURI, RDFFormat rdfFormat) {
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
 
         writer.setResearchObjectURI(researchObjectURI);
         writer.setBaseURI(namedGraphURI);
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         writer.write(tmpGraphSet.asJenaModel(namedGraphURI.toString()), out, null);
         return new ByteArrayInputStream(out.toByteArray());
     }
 
 
     private void addGraphsRecursively(NamedGraphSet tmpGraphSet, URI namedGraphURI) {
         tmpGraphSet.addGraph(graphset.getGraph(namedGraphURI.toString()));
 
         OntModel annotationModel = createOntModelForNamedGraph(namedGraphURI);
         NodeIterator it = annotationModel.listObjectsOfProperty(aoBody);
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
         ontModel.addSubModel(defaultModel);
         return ontModel;
     }
 
 
     private NamedGraph getOrCreateGraph(NamedGraphSet graphset, URI namedGraphURI) {
         return graphset.containsGraph(namedGraphURI.toString()) ? graphset.getGraph(namedGraphURI.toString())
                 : graphset.createGraph(namedGraphURI.toString());
     }
 
 
     @Override
     public void close() {
         log.debug("" + this + " closes a connection");
         graphset.close();
     }
 
 
     @Override
     public boolean isRoFolder(URI researchObjectURI, URI resourceURI) {
         resourceURI = resourceURI.normalize();
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObjectURI.normalize()));
         Individual resource = manifestModel.getIndividual(resourceURI.toString());
         if (resource == null) {
             return false;
         }
         return resource.hasRDFType(roFolderClass);
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
     public boolean isROMetadataNamedGraph(URI researchObjectURI, URI graphURI) {
         Node manifest = Node.createURI(getManifestURI(researchObjectURI).toString());
         Node deprecatedManifest = Node.createURI(getDeprecatedManifestURI(researchObjectURI).toString());
         Node bodyNode = Node.createURI(aoBody.getURI());
         Node annBody = Node.createURI(graphURI.toString());
         boolean isManifest = getManifestURI(researchObjectURI).equals(graphURI);
         boolean isDeprecatedManifest = getDeprecatedManifestURI(researchObjectURI).equals(graphURI);
         boolean isAnnotationBody = graphset.containsQuad(new Quad(manifest, Node.ANY, bodyNode, annBody));
         boolean isDeprecatedAnnotationBody = graphset.containsQuad(new Quad(deprecatedManifest, Node.ANY, bodyNode,
                 annBody));
         return isManifest || isAnnotationBody || isDeprecatedManifest || isDeprecatedAnnotationBody;
     }
 
 
     @Override
     public void removeNamedGraph(URI researchObjectURI, URI graphURI) {
         graphURI = graphURI.normalize();
         if (!graphset.containsGraph(graphURI.toString())) {
             throw new IllegalArgumentException("URI not found: " + graphURI);
         }
 
         List<URI> graphsToDelete = new ArrayList<>();
         graphsToDelete.add(graphURI);
 
         int i = 0;
         while (i < graphsToDelete.size()) {
             OntModel manifestModel = createOntModelForNamedGraph(graphsToDelete.get(i));
             NodeIterator it = manifestModel.listObjectsOfProperty(aoBody);
             while (it.hasNext()) {
                 RDFNode annotationBodyRef = it.next();
                 URI graphURI2 = URI.create(annotationBodyRef.asResource().getURI());
                 // TODO make sure that this named graph is internal
                 if (graphset.containsGraph(graphURI2.toString()) && !graphsToDelete.contains(graphURI2)) {
                     graphsToDelete.add(graphURI2);
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
         OntModel ontModel = ModelFactory.createOntologyModel(spec,
             graphset.asJenaModel(DEFAULT_NAMED_GRAPH_URI.toString()));
         ontModel.addSubModel(defaultModel);
         return ontModel;
     }
 
 
     /**
      * 
      * @param roURI
      *            must end with /
      * @return
      */
     private URI getManifestURI(URI roURI) {
         return roURI.resolve(".ro/manifest.rdf");
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
     public void removeResearchObject(URI researchObjectURI) {
         try {
             removeNamedGraph(researchObjectURI, getDeprecatedManifestURI(researchObjectURI));
         } catch (IllegalArgumentException e) {
             // it is a hack so ignore exceptions
         }
         removeNamedGraph(researchObjectURI, getManifestURI(researchObjectURI));
     }
 
 
     @Override
     public boolean containsNamedGraph(URI graphURI) {
         return graphset.containsGraph(graphURI.toString());
     }
 
 
     @Override
     public QueryResult executeSparql(String queryS, RDFFormat rdfFormat) {
         if (queryS == null)
             throw new NullPointerException("Query cannot be null");
         Query query = null;
         try {
             query = QueryFactory.create(queryS, sparqlSyntax);
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
         if (subject == null)
             return attributes;
         StmtIterator it = model.listStatements(subject, null, (RDFNode) null);
         while (it.hasNext()) {
             Statement s = it.next();
             try {
                 URI propURI = new URI(s.getPredicate().getURI());
                 Object object;
                 if (s.getObject().isResource()) {
                     // Need to check both because the model has no inference
                     if (s.getObject().as(Individual.class).hasRDFType(foafAgentClass)
                             || s.getObject().as(Individual.class).hasRDFType(foafPersonClass)) {
                         object = s.getObject().asResource().getProperty(foafName).getLiteral().getValue();
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
     public boolean isAggregatedResource(URI researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Resource researchObjectR = manifestModel.createResource(researchObject.toString());
         Resource resourceR = manifestModel.createResource(resource.normalize().toString());
         return manifestModel.contains(researchObjectR, aggregates, resourceR);
     }
 
 
     @Override
     public boolean isAnnotation(URI researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Individual resourceR = manifestModel.getIndividual(resource.normalize().toString());
         return resourceR != null && resourceR.hasRDFType(roAggregatedAnnotationClass);
     }
 
 
     @Override
     public URI addProxy(URI researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Resource researchObjectR = manifestModel.createResource(researchObject.toString());
         Resource resourceR = manifestModel.createResource(resource.normalize().toString());
         URI proxyURI = generateProxyURI(researchObject);
         Individual proxy = manifestModel.createIndividual(proxyURI.toString(), oreProxyClass);
         manifestModel.add(proxy, oreProxyIn, researchObjectR);
         manifestModel.add(proxy, oreProxyFor, resourceR);
         return proxyURI;
     }
 
 
     private static URI generateProxyURI(URI researchObject) {
         return researchObject.resolve(".ro/proxies/" + UUID.randomUUID());
     }
 
 
     @Override
     public boolean isProxy(URI researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Individual resourceR = manifestModel.getIndividual(resource.normalize().toString());
         return resourceR != null && resourceR.hasRDFType(oreProxyClass);
     }
 
 
     @Override
     public boolean existsProxyForResource(URI researchObject, URI resource) {
         return getProxyForResource(researchObject, resource) != null;
     }
 
 
     @Override
     public URI getProxyForResource(URI researchObject, URI resource) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Resource resourceR = manifestModel.createResource(resource.normalize().toString());
         ResIterator it = manifestModel.listSubjectsWithProperty(oreProxyFor, resourceR);
         while (it.hasNext()) {
             Individual r = it.next().as(Individual.class);
             if (r != null && r.hasRDFType(oreProxyClass)) {
                 return URI.create(r.getURI());
             }
         }
         return null;
     }
 
 
     @Override
     public URI getProxyFor(URI researchObject, URI proxy) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Individual proxyR = manifestModel.getIndividual(proxy.normalize().toString());
         if (proxyR.hasProperty(oreProxyFor)) {
             return URI.create(proxyR.getPropertyResourceValue(oreProxyFor).getURI());
         } else {
             return null;
         }
     }
 
 
     @Override
     public void deleteProxy(URI researchObject, URI proxy) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Resource proxyR = manifestModel.getResource(proxy.normalize().toString());
         manifestModel.removeAll(proxyR, null, null);
     }
 
 
     @Override
     public URI addAnnotation(URI researchObject, List<URI> annotationTargets, URI annotationBody) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Resource researchObjectR = manifestModel.createResource(researchObject.toString());
         Resource body = manifestModel.createResource(annotationBody.normalize().toString());
         URI annotationURI = generateAnnotationURI(researchObject);
         Individual annotation = manifestModel.createIndividual(annotationURI.toString(), roAggregatedAnnotationClass);
         manifestModel.add(researchObjectR, aggregates, annotation);
         manifestModel.add(annotation, aoBody, body);
         for (URI targetURI : annotationTargets) {
             Resource target = manifestModel.createResource(targetURI.normalize().toString());
             manifestModel.add(annotation, annotatesAggregatedResource, target);
         }
         manifestModel.add(annotation, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
         Resource agent = manifestModel.createResource(user.getUri().toString());
         manifestModel.add(annotation, DCTerms.creator, agent);
         return annotationURI;
     }
 
 
     private static URI generateAnnotationURI(URI researchObject) {
         return researchObject.resolve(".ro/annotations/" + UUID.randomUUID());
     }
 
 
     @Override
     public void updateAnnotation(URI researchObject, URI annotationURI, List<URI> annotationTargets, URI annotationBody) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Resource body = manifestModel.createResource(annotationBody.normalize().toString());
         Individual annotation = manifestModel.getIndividual(annotationURI.toString());
        manifestModel.removeAll(annotation, aoBody, null);
        manifestModel.removeAll(annotation, annotatesAggregatedResource, null);
         manifestModel.add(annotation, aoBody, body);
         for (URI targetURI : annotationTargets) {
             Resource target = manifestModel.createResource(targetURI.normalize().toString());
             manifestModel.add(annotation, annotatesAggregatedResource, target);
         }
     }
 
 
     @Override
     public URI getAnnotationBody(URI researchObject, URI annotation) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
         Individual annotationR = manifestModel.getIndividual(annotation.normalize().toString());
         if (annotationR.hasProperty(aoBody)) {
             return URI.create(annotationR.getPropertyResourceValue(aoBody).getURI());
         } else {
             return null;
         }
     }
 
 
     @Override
     public void deleteAnnotation(URI researchObject, URI annotation) {
         OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObject.normalize()));
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
 }
