 package pl.psnc.dl.wf4ever.model.RDF;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URI;
 import java.nio.file.Paths;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.common.Builder;
 import pl.psnc.dl.wf4ever.common.UserProfile;
 import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
 import pl.psnc.dl.wf4ever.rosrs.ROSRService;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceTdb;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.FOAF;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.W4E;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ReadWrite;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.tdb.TDB;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * The root class for the model.
  * 
  * @author piotrekhol
  * 
  */
 public class Thing {
 
     /** resource URI. */
     protected URI uri;
 
     /** creator URI. */
     protected UserMetadata creator;
 
     /** creation date. */
     protected DateTime created;
 
     /** all contributors. */
     protected Set<URI> contributors = new HashSet<>();
 
     /** last modification date. */
     protected DateTime modified;
 
     /** Use tdb transactions. */
     protected boolean useTransactions;
 
     /** Jena dataset. */
     protected Dataset dataset;
 
     /** Triple store location. */
     protected static final String TRIPLE_STORE_DIR = getStoreDirectory("connection.properties");
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(SemanticMetadataServiceTdb.class);
 
     /** Jena model of the named graph. */
     protected OntModel model;
 
     /** User creating the instance. */
     protected UserMetadata user;
 
     /** builder creating the instance, which may be reused for loading other resources. */
     protected Builder builder;
 
     static {
         init();
     }
 
 
     /**
      * Constructor that allows to specify a custom dataset.
      * 
      * @param user
      *            user creating the instance
      * @param dataset
      *            custom dataset
      * @param useTransactions
      *            should transactions be used. Note that not using transactions on a dataset which already uses
      *            transactions may make it unreadable.
      * @param uri
      *            resource URI
      */
     public Thing(UserMetadata user, Dataset dataset, Boolean useTransactions, URI uri) {
         this.user = user;
         this.dataset = dataset;
         this.useTransactions = useTransactions;
         this.uri = uri;
     }
 
 
     /**
      * Constructor that uses a default dataset on a local drive using transactions.
      * 
      * @param user
      *            user creating the instance
      * @param uri
      *            resource URI
      */
     public Thing(UserMetadata user, URI uri) {
         this(user, null, true, uri);
     }
 
 
     /**
      * Constructor that uses a default dataset on a local drive using transactions.
      * 
      * @param user
      *            user creating the instance
      */
     public Thing(UserMetadata user) {
         this(user, null, true, null);
     }
 
 
     /**
      * Init .
      * 
      */
     public static void init() {
         TDB.getContext().set(TDB.symUnionDefaultGraph, true);
         W4E.DEFAULT_MODEL.setNsPrefixes(W4E.STANDARD_NAMESPACES);
     }
 
 
     public URI getUri() {
         return uri;
     }
 
 
     /**
      * Return an RDF format specific URI.
      * 
      * @param format
      *            RDF format
      * @return the URI
      */
     public URI getUri(RDFFormat format) {
         if (uri == null) {
             return null;
         }
         if (format == null) {
             return getUri();
         }
         URI base = uri.resolve(".");
         String name = base.relativize(uri).getPath();
         String specific;
         if (RDFFormat.forFileName(name) != null) {
             specific = name.substring(0, name.lastIndexOf(".")) + "." + format.getDefaultFileExtension();
         } else {
             specific = name + "." + format.getDefaultFileExtension();
         }
         return UriBuilder.fromUri(base).path(specific).queryParam("original", name).build();
     }
 
 
     /**
      * Return a shortest possible name of the Thing.
      * 
      * @return a filename or URI if there is no path
      */
     public String getFilename() {
         if (uri.getPath() == null || uri.getPath().isEmpty()) {
             return uri.toString();
         }
         return Paths.get(uri.getPath()).getFileName().toString();
     }
 
 
     public void setUri(URI uri) {
         this.uri = uri;
     }
 
 
     public UserMetadata getCreator() {
         return creator;
     }
 
 
     public void setCreator(UserMetadata creator) {
         this.creator = creator;
     }
 
 
     public DateTime getCreated() {
         return created;
     }
 
 
     public void setCreated(DateTime created) {
         this.created = created;
     }
 
 
     public DateTime getModified() {
         return modified;
     }
 
 
     public void setModified(DateTime modified) {
         this.modified = modified;
     }
 
 
     public Set<URI> getContributors() {
         return contributors;
     }
 
 
     /**
      * Check if the dataset contains a named graph for this resource.
      * 
      * @return true if the named graph exists, false otherwise
      */
     public boolean isNamedGraph() {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             return dataset.containsNamedModel(uri.toString());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     public Builder getBuilder() {
         return builder;
     }
 
 
     public void setBuilder(Builder builder) {
         this.builder = builder;
     }
 
 
     /**
      * Take out an RDF graph from the triplestore and serialize it in storage (e.g. dLibra) with relative URI
      * references.
      * 
      * @param base
      *            the object whose URI is the base
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public void serialize(URI base)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String filePath = base.relativize(uri).toString();
         RDFFormat format = RDFFormat.forFileName(filePath, RDFFormat.RDFXML);
         InputStream dataStream = ROSRService.SMS.get().getNamedGraphWithRelativeURIs(uri, base, format);
         ROSRService.DL.get().createOrUpdateFile(base, filePath, dataStream, format.getDefaultMIMEType());
     }
 
 
     /**
      * Return this resource as a named graph in a selected RDF format.
      * 
      * @param syntax
      *            RDF format
      * @return an input stream or null of no model is found
      */
     public InputStream getGraphAsInputStream(RDFFormat syntax) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             if (!dataset.containsNamedModel(uri.toString())) {
                 return null;
             }
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             if (syntax.supportsContexts()) {
                 Dataset tmpDataset = TDBFactory.createDataset();
                 addNamedModelsRecursively(tmpDataset);
 
                 NamedGraphSet ngs = new NamedGraphSetImpl();
                 Iterator<String> it = tmpDataset.listNames();
                 while (it.hasNext()) {
                     String graphUri = it.next();
                     Model ng4jModel = ModelFactory.createModelForGraph(ngs.createGraph(graphUri));
                     Model tdbModel = tmpDataset.getNamedModel(graphUri);
                     List<Statement> statements = tdbModel.listStatements().toList();
                     ng4jModel.add(statements);
                 }
                 ngs.write(out, syntax.getName().toUpperCase(), null);
             } else {
                 dataset.getNamedModel(uri.toString()).write(out, syntax.getName().toUpperCase());
             }
             return new ByteArrayInputStream(out.toByteArray());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Return this resource as a named graph in a selected RDF format. Only triples describing the resources given as
      * parameters will be returned.
      * 
      * @param syntax
      *            RDF format
      * @param resources
      *            resources which will be included
      * @return an input stream or null of no model is found
      */
     public InputStream getGraphAsInputStream(RDFFormat syntax, Thing... resources) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             Model result = ModelFactory.createDefaultModel();
             for (Thing resource : resources) {
                 String queryString = String.format("DESCRIBE <%s>", resource.getUri().toString());
                 Query query = QueryFactory.create(queryString);
 
                 QueryExecution qexec = QueryExecutionFactory.create(query, model);
                 result.add(qexec.execDescribe());
                 qexec.close();
             }
             if (result.isEmpty()) {
                 return null;
             }
 
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             result.removeNsPrefix("xml");
             result.write(out, syntax.getName().toUpperCase());
             return new ByteArrayInputStream(out.toByteArray());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Find the dcterms:creator of a resource in the model.
      * 
      * @param thing
      *            the resource
      * @return creator URI or null if not defined
      */
     public UserMetadata extractCreator(Thing thing) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             Individual ro = model.getIndividual(thing.getUri().toString());
             if (ro == null) {
                 throw new IncorrectModelException("RO not found in the manifest" + thing.getUri());
             }
             com.hp.hpl.jena.rdf.model.Resource c = ro.getPropertyResourceValue(DCTerms.creator);
             URI curi = c != null ? URI.create(c.getURI()) : null;
             RDFNode n = c.getProperty(FOAF.name).getObject();
             String name = n != null ? n.asLiteral().getString() : null;
             return new UserProfile(null, name, null, curi);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Find the dcterms:created date of a resource in the model.
      * 
      * @param thing
      *            the resource
      * @return creation date or null if not defined
      */
     public DateTime extractCreated(Thing thing) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             Individual ro = model.getIndividual(thing.getUri().toString());
             if (ro == null) {
                 throw new IncorrectModelException("RO not found in the manifest" + thing.getUri());
             }
             RDFNode d = ro.getPropertyValue(DCTerms.created);
             if (d == null || !d.isLiteral()) {
                 return null;
             }
             return DateTime.parse(d.asLiteral().getString());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     @Override
     public String toString() {
         return getUri().toString();
     }
 
 
     /**
      * Start a TDB transaction provided that the flag useTransactions is set, the dataset supports transactions and
      * there is no open transaction. According to TDB, many read or one write transactions are allowed.
      * 
      * @param mode
      *            read or write
      * @return true if a new transaction has been started, false otherwise
      */
     protected boolean beginTransaction(ReadWrite mode) {
         if (dataset == null) {
             dataset = TDBFactory.createDataset(TRIPLE_STORE_DIR);
         }
         boolean started = false;
         if (useTransactions && dataset.supportsTransactions() && !dataset.isInTransaction()) {
             dataset.begin(mode);
             started = true;
         }
         if (mode == ReadWrite.WRITE || dataset.containsNamedModel(uri.toString())) {
             model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, dataset.getNamedModel(uri.toString()));
         }
         return started;
     }
 
 
     /**
      * Commit the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
      * parameter is true.
      * 
      * @param wasStarted
      *            a convenience parameter to specify if the transaction should be committed
      */
     protected void commitTransaction(boolean wasStarted) {
         if (useTransactions && dataset.supportsTransactions() && wasStarted) {
             dataset.commit();
         }
     }
 
 
     /**
      * End the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
      * parameter is true.
      * 
      * @param wasStarted
      *            a convenience parameter to specify if the transaction should be ended
      */
     protected void endTransaction(boolean wasStarted) {
         if (useTransactions && dataset.supportsTransactions() && wasStarted) {
             if (model != null) {
                 TDB.sync(model);
                 model = null;
             }
             dataset.end();
         }
     }
 
 
     /**
      * Abort the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
      * parameter is true.
      * 
      * @param wasStarted
      *            a convenience parameter to specify if the transaction should be aborted
      */
     protected void abortTransaction(boolean wasStarted) {
         if (useTransactions && dataset.supportsTransactions() && wasStarted) {
             dataset.abort();
         }
     }
 
 
     // *****Private***** 
 
     /**
      * Load the triple store location from the properties file. In case of any exceptions, log them and return null.
      * 
      * @param filename
      *            properties file name
      * @return the path to the triple store directory
      */
     private static String getStoreDirectory(String filename) {
         try (InputStream is = Thing.class.getClassLoader().getResourceAsStream(filename)) {
             Properties props = new Properties();
             props.load(is);
             return props.getProperty("store.directory");
 
         } catch (Exception e) {
             LOGGER.error("Trple store location can not be loaded from the properties file", e);
         }
         return null;
     }
 
 
     /**
      * Add this model to a dataset and call this method recursively for all dependent models, unless they have already
      * been added to the dataset.
      * 
      * @param tmpDataset
      *            the dataset to which to add the model
      */
     private void addNamedModelsRecursively(Dataset tmpDataset) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             if (model == null) {
                 LOGGER.warn("Could not find model for URI " + uri);
                 return;
             }
             tmpDataset.addNamedModel(uri.toString(), model);
             List<RDFNode> it = model.listObjectsOfProperty(AO.body).toList();
             it.addAll(model.listObjectsOfProperty(ORE.isDescribedBy).toList());
             for (RDFNode namedModelRef : it) {
                 URI childURI = URI.create(namedModelRef.asResource().getURI());
                 if (dataset.containsNamedModel(childURI.toString())
                         && !tmpDataset.containsNamedModel(childURI.toString())) {
                     Thing relatedModel = new Thing(user, childURI);
                     relatedModel.addNamedModelsRecursively(tmpDataset);
                 }
             }
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Save the resource to the triplestore and data storage backend.
      */
     public void save() {
     }
 
 
     /**
      * Delete the named graph if exists.
      */
     public void delete() {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             if (model != null) {
                 dataset.removeNamedModel(uri.toString());
                 model = null;
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Delete a resource from the model.
      * 
      * @param resource
      *            resource to delete
      */
     public void deleteResource(Thing resource) {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             Resource resR = model.getResource(resource.getUri().toString());
             if (resR != null) {
                 model.removeAll(resR, null, null);
                 model.removeAll(null, null, resR);
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Save the dcterms:creator and dcterms:created in the current model.
      * 
      * @param subject
      *            the resource being described
      */
     public void saveAuthor(Thing subject) {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
             if (!subjectR.hasProperty(DCTerms.created) && subject.getCreated() != null) {
                 model.add(subjectR, DCTerms.created, model.createTypedLiteral(subject.getCreated().toCalendar(null)));
             }
             if (!subjectR.hasProperty(DCTerms.creator) && subject.getCreator() != null) {
                Individual author = model.createIndividual(subject.getCreator().toString(), FOAF.Agent);
                 model.add(subjectR, DCTerms.creator, author);
                 author.setPropertyValue(FOAF.name, model.createLiteral(subject.getCreator().getName()));
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Save the dcterms:contributor and dcterms:modified in the current model.
      * 
      * @param subject
      *            the resource being described
      */
     public void saveContributors(Thing subject) {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
             model.removeAll(subjectR, DCTerms.modified, null);
             if (subject.getModified() != null) {
                 model.add(subjectR, DCTerms.modified, model.createTypedLiteral(subject.getModified().toCalendar(null)));
             }
             for (URI contributor : subject.getContributors()) {
                 model.add(subjectR, DCTerms.contributor, model.createResource(contributor.toString()));
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Is the resource writable only by RODL (the manifest or the evolution annotation body).
      * 
      * @return true if object represents a special object, false otherwise.
      */
     public Boolean isSpecialResource() {
         if (uri.toString().matches("(.+)?manifest\\.rdf(\\/)?") || uri.toString().matches("(.+)?evo_info\\.ttl(\\/)?")) {
             return true;
         }
         return false;
     }
 
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((uri == null) ? 0 : uri.hashCode());
         return result;
     }
 
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Thing other = (Thing) obj;
         if (uri == null) {
             if (other.uri != null) {
                 return false;
             }
         } else if (!uri.equals(other.uri)) {
             return false;
         }
         return true;
     }
 
 }
