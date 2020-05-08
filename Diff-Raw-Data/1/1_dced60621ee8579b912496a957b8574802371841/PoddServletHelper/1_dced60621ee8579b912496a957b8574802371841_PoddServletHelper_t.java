 package com.github.podd.prototype;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import net.fortytwo.sesametools.URITranslator;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandler;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.Rio;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.profiles.OWLProfile;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Helper class to the PODD web service servlet containing all PODD business logic.
  * 
  * FIXME: pretty much copied from the test class and incomplete
  * 
  * @author kutila
  * @created 2012/10/25
  * 
  */
 public class PoddServletHelper
 {
     public static String PODD_BASE_NAMESPACE = "http://purl.org/podd/ns/poddBase#";
     
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     private Repository nextRepository;
     
     // private RepositoryConnection nextRepositoryConnection;
     
     private ValueFactory nextValueFactory;
     
     private PoddPrototypeUtils utils;
     private FileReferenceUtils fileReferenceUtils;
     
     private OWLOntologyManager manager;
     private OWLReasonerFactory reasonerFactory;
     private String reasonerName;
     private URI schemaOntologyManagementGraph;
     private URI poddArtifactManagementGraph;
     private IRI pelletOwlProfile;
     
     private String poddBasePath;
     private String poddSciencePath;
     
     // private String poddPlantPath;
     
     public void setUp(final Repository repository) throws RepositoryException
     {
         this.log.debug("setUp ... valueFactory");
         this.nextRepository = repository;
         this.nextValueFactory = this.nextRepository.getValueFactory();
         
         // create the manager to use for the test
         this.log.debug("setUp ... OWLOntologyManager");
         this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         
         // We're only supporting OWL2_DL and Pellet in this prototype
         this.log.debug("setUp ... ReasonerFactory");
         this.reasonerName = "Pellet";
         this.reasonerFactory = OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
         
         this.pelletOwlProfile = OWLProfile.OWL2_DL;
         
         this.schemaOntologyManagementGraph =
                 this.nextValueFactory.createURI("urn:test:schemaOntologiesManagementGraph");
         this.poddArtifactManagementGraph = this.nextValueFactory.createURI("urn:test:poddArtifactManagementGraph");
         
         this.log.debug("setUp ... PoddPrototypeUtils");
         this.utils =
                 new PoddPrototypeUtils(this.manager, this.pelletOwlProfile, this.reasonerFactory,
                         this.schemaOntologyManagementGraph, this.poddArtifactManagementGraph);
         
         this.poddBasePath = "/ontologies/poddBase.owl";
         this.poddSciencePath = "/ontologies/poddScience.owl";
         // this.poddPlantPath = "/ontologies/poddPlant.owl";
         
         this.log.debug("setUp ... completed");
     }
     
     /**
      * Use this setter to pass in an initialized FileReferenceUtils for use by this class.
      * 
      * @param fileReferenceUtils
      */
     public void setFileReferenceUtils(final FileReferenceUtils fileReferenceUtils)
     {
         this.fileReferenceUtils = fileReferenceUtils;
     }
     
     public FileReferenceUtils getFileReferenceUtils()
     {
         return this.fileReferenceUtils;
     }
     
     /**
      * The prototype does not yet support uploading of new Schema Ontologies. Therefore, this method
      * should be called at initialization to load the schemas.
      * 
      * @throws PoddException
      * @throws IOException
      * @throws OpenRDFException
      * @throws OWLException
      * 
      */
     public void loadSchemaOntologies() throws OWLException, OpenRDFException, IOException, PoddException
     {
         
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             final long schemaGraphSize = repositoryConnection.size(this.schemaOntologyManagementGraph);
             if(schemaGraphSize > 0)
             {
                 this.log.info("loadSchemaOntology ... from Repository ({})", schemaGraphSize);
                 
                 // -- get current versions of all schema ontologies and load them to the OWL
                 // Ontology Manager
                 final RepositoryResult<Statement> repoResult =
                         repositoryConnection.getStatements(null, PoddPrototypeUtils.OMV_CURRENT_VERSION, null, false,
                                 this.schemaOntologyManagementGraph);
                 while(repoResult.hasNext())
                 {
                     final Value object = repoResult.next().getObject();
                     if(object instanceof Resource)
                     {
                         this.log.info("loadSchemaOntology ... {}", object);
                         this.utils.loadOntology(repositoryConnection, RDFFormat.RDFXML.getDefaultMIMEType(),
                                 (Resource)object);
                     }
                 }
             }
             else
             {
                 this.log.info("loadSchemaOntology ... PODD-BASE ({})", repositoryConnection.size());
                 this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                         repositoryConnection);
                 
                 this.log.info("loadSchemaOntology ... PODD-SCIENCE ({})", repositoryConnection.size());
                 this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                         repositoryConnection);
                 this.log.info("loadSchemaOntology ... completed ({})", repositoryConnection.size());
             }
             repositoryConnection.commit();
         }
         catch(OWLException | OpenRDFException | IOException | PoddException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
         }
     }
     
     /**
      * Add a new artifact to PODD
      * 
      * TODO: support different mime types
      * 
      * @param inputStream
      * @param contentType
      * @return The newly allocated URI that is used by PODD to identify this artifact
      * @throws PoddException
      * @throws OWLException
      * @throws IOException
      * @throws OpenRDFException
      */
     public String loadPoddArtifact(final InputStream inputStream, final String contentType) throws OpenRDFException,
         IOException, OWLException, PoddException
     {
         final InferredOWLOntologyID nextOntology = this.loadPoddArtifactInternal(inputStream, contentType);
         return nextOntology.getOntologyIRI().toString();
     }
     
     /**
      * TODO: Copied from PoddPrototypeUtils. Needs to be checked and modified for the web service
      * 
      * Loading an artifact consists of several steps.
      * 
      * - Assign permanent URIs to any that have temporary values
      * 
      * - Fix imported ontologies to import their current versions
      * 
      * - Check consistency of ontology
      * 
      * - Compute inferences
      * 
      * - Store asserted and inferred ontologies in Repository
      * 
      * @param inputStream
      * @param mimeType
      * @return
      * @throws IOException
      * @throws RepositoryException
      * @throws RDFParseException
      */
     public InferredOWLOntologyID loadPoddArtifactInternal(final InputStream inputStream, final String mimeType)
         throws OpenRDFException, IOException, OWLException, PoddException
     {
         this.log.info("ADD artifact: " + mimeType);
         
         if(inputStream == null)
         {
             final String errorMessage = "Could not read artifact from input stream";
             this.log.error(errorMessage);
             throw new NullPointerException(errorMessage);
         }
         
         // load on to a temporary in-memory repository to create persistent URLs
         Repository tempRepository = null;
         RepositoryConnection tempRepositoryConnection = null;
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             tempRepository = new SailRepository(new MemoryStore());
             tempRepository.initialize();
             tempRepositoryConnection = tempRepository.getConnection();
             tempRepositoryConnection.setAutoCommit(false);
             
             // 1. Create permanent identifiers for any impermanent identifiers in the object...
             final URI randomURN =
                     tempRepositoryConnection.getValueFactory().createURI("urn:random:" + UUID.randomUUID().toString());
             
             tempRepositoryConnection.add(inputStream, "", Rio.getParserFormatForMIMEType(mimeType), randomURN);
             tempRepositoryConnection.commit();
             
             // TODO: set HOST component to match current host so that URI is resolvable
             URITranslator.doTranslation(tempRepositoryConnection, "urn:temp:", "http://example.org/permanenturl/"
                     + UUID.randomUUID().toString() + "/", randomURN);
             
             // retrieve list of imported ontologies and update them to the "current version"
             final URI importURI = IRI.create("http://www.w3.org/2002/07/owl#imports").toOpenRDFURI();
             final RepositoryResult<Statement> importStatements =
                     tempRepositoryConnection.getStatements(null, importURI, null, false, randomURN);
             
             while(importStatements.hasNext())
             {
                 final Statement stmt = importStatements.next();
                 // get current version IRI for imported ontology
                 final RepositoryResult<Statement> currentVersionStatements =
                         repositoryConnection.getStatements(IRI.create(stmt.getObject().stringValue()).toOpenRDFURI(),
                                 IRI.create("http://omv.ontoware.org/ontology#currentVersion").toOpenRDFURI(), null,
                                 false, this.utils.getSchemaGraph());
                 
                 if(currentVersionStatements.hasNext())
                 {
                     final Value currentVersion = currentVersionStatements.next().getObject();
                     tempRepositoryConnection.remove(stmt, randomURN);
                     tempRepositoryConnection.add(stmt.getSubject(), importURI, currentVersion, randomURN);
                 }
             }
             tempRepositoryConnection.commit();
             
             this.log.info("Loading podd artifact from repository: {}", randomURN);
             final OWLOntology nextOntology =
                     this.utils.loadOntology(tempRepositoryConnection, RDFFormat.RDFXML.getDefaultMIMEType(), randomURN);
             
             // regain memory after loading the ontology into OWLAPI
             tempRepositoryConnection.clear();
             
             // 2. Validate the object in terms of the OWL profile
             // 3. Validate the object using a reasoner
             final OWLReasoner reasoner = this.utils.checkConsistency(nextOntology);
             
             // 4. Store the object
             this.utils.dumpOntologyToRepository(nextOntology, repositoryConnection);
             
             // 5. Infer extra statements about the object using a reasoner
             this.log.info("Computing inferences for podd artifact");
             final OWLOntology nextInferredOntology =
                     this.utils.computeInferences(reasoner,
                             this.utils.generateInferredOntologyID(nextOntology.getOntologyID()));
             
             // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
             // Sesame Repository
             // 6. Store the inferred statements
             this.utils.dumpOntologyToRepository(nextInferredOntology, repositoryConnection);
             
             // 7. Update the PODD Artifact management graph to contain the latest
             // update the link in the PODD Artifact management graph
             this.utils.updateCurrentManagedPoddArtifactOntologyVersion(repositoryConnection,
                     nextOntology.getOntologyID(), nextInferredOntology.getOntologyID());
             
             repositoryConnection.commit();
             
             return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology
                     .getOntologyID().getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
         }
         catch(OpenRDFException | OWLException | IOException | PoddException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
             if(tempRepositoryConnection != null)
             {
                 tempRepositoryConnection.rollback();
                 tempRepositoryConnection.close();
             }
             if(tempRepository != null)
             {
                 tempRepository.shutDown();
             }
         }
     }
     
     /**
      * Get the specified artifact if it exists inside PODD.
      * 
      * @param artifactUri
      *            Identifies the requested artifact.
      * @param mimeType
      *            The MIME-type in which the artifact should be returned in.
      * @param includeInferredStatements
      *            If true, the inferred statements for this artifact are returned.
      * @return The artifact serialized in the requested mime type.
      * 
      * @throws RepositoryException
      * @throws RDFHandlerException
      */
     public String getArtifact(final String artifactUri, final String mimeType, final boolean includeInferredStatements)
         throws RepositoryException, RDFHandlerException
     {
         this.log.info("GET artifact: " + artifactUri);
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             final InferredOWLOntologyID ontologyID = this.getInferredOWLOntologyIDForArtifact(artifactUri);
             
             this.checkArtifactExists(repositoryConnection, ontologyID);
             
             final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final RDFHandler rdfWriter =
                     Rio.createWriter(Rio.getWriterFormatForMIMEType(mimeType, RDFFormat.RDFXML), out);
             
             if(includeInferredStatements)
             {
                 repositoryConnection.export(rdfWriter, ontologyID.getVersionIRI().toOpenRDFURI(), ontologyID
                         .getInferredOntologyIRI().toOpenRDFURI());
             }
             else
             {
                 repositoryConnection.export(rdfWriter, ontologyID.getVersionIRI().toOpenRDFURI());
             }
             repositoryConnection.rollback();
             
             // FIXME: Not a good idea to create strings out of ontologies. Why not take in an
             // OutputStream and export to that??
             return out.toString();
         }
         catch(RepositoryException | RDFHandlerException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
         }
     }
     
     /**
      * If the specified String represents to a schema ontology managed by PODD, write the RDF
      * statements belonging to this schema ontology to the given <code>OutputStream</code> using the
      * specified MIME type.
      * 
      * @param uriString
      *            String representation of the ontology requested
      * @param mimeType
      *            The format in which data should be sent back
      * @param out
      *            An output stream into which RDF data is to be written
      * @throws RDFHandlerException
      * @throws RepositoryException
      */
     public void getSchemaOntology(final String uriString, final String mimeType, final OutputStream out)
         throws RDFHandlerException, RepositoryException
     {
         this.log.info("GET schema ontology: " + uriString);
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             // -- get the version IRI, which is the context under which the ontology is stored
             final URI ontologyURI = repositoryConnection.getValueFactory().createURI(uriString);
             URI ontologyVersionURI = null;
             final RepositoryResult<Statement> repoResult =
                     repositoryConnection.getStatements(ontologyURI, PoddPrototypeUtils.OMV_CURRENT_VERSION, null,
                             false, this.schemaOntologyManagementGraph);
             if(repoResult.hasNext())
             {
                 final Statement statement = repoResult.next();
                 if(statement.getObject() instanceof URI)
                 {
                     ontologyVersionURI = (URI)statement.getObject();
                 }
                 else
                 {
                     this.log.error("Object {} was not a URI", statement);
                     throw new RuntimeException("Current Version <" + uriString + "> not a URI");
                 }
             }
             
             if(ontologyVersionURI == null)
             {
                 throw new RuntimeException("Schema Ontology <" + uriString + "> not found.");
             }
             
             // read all statements and write to the output stream
             final RDFHandler rdfWriter =
                     Rio.createWriter(Rio.getWriterFormatForMIMEType(mimeType, RDFFormat.RDFXML), out);
             repositoryConnection.export(rdfWriter, ontologyVersionURI);
             repositoryConnection.rollback();
         }
         catch(RepositoryException | RDFHandlerException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
         }
     }
     
     /**
      * Delete the specified artifact and all references to it.
      * 
      * @param artifactURI
      * @throws RepositoryException
      *             , RDFHandlerException
      */
     public String deleteArtifact(final String artifactUri) throws RepositoryException, RDFHandlerException
     {
         this.log.info("DELETE artifact: " + artifactUri);
         
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             final InferredOWLOntologyID ontologyID = this.getInferredOWLOntologyIDForArtifact(artifactUri);
             this.checkArtifactExists(repositoryConnection, ontologyID);
             
             final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final RDFHandler rdfWriter =
                     Rio.createWriter(Rio.getWriterFormatForMIMEType(PoddServlet.MIME_TYPE_RDF_XML, RDFFormat.RDFXML),
                             out);
             
             repositoryConnection.export(rdfWriter, ontologyID.getVersionIRI().toOpenRDFURI());
             repositoryConnection.clear(ontologyID.getVersionIRI().toOpenRDFURI());
            repositoryConnection.clear(ontologyID.getInferredOntologyIRI().toOpenRDFURI());
             
             // remove references in the artifact management graph
             final RepositoryResult<Statement> repoResult =
                     repositoryConnection.getStatements(ontologyID.getOntologyIRI().toOpenRDFURI(),
                             PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION, null, false,
                             this.poddArtifactManagementGraph);
             while(repoResult.hasNext())
             {
                 final String inferredOnto = repoResult.next().getObject().toString();
                 repositoryConnection.remove(IRI.create(inferredOnto).toOpenRDFURI(), null, null,
                         this.poddArtifactManagementGraph);
             }
             repositoryConnection.remove(ontologyID.getOntologyIRI().toOpenRDFURI(), null, null,
                     this.poddArtifactManagementGraph);
             repositoryConnection.remove(ontologyID.getVersionIRI().toOpenRDFURI(), null, null,
                     this.poddArtifactManagementGraph);
             
             repositoryConnection.commit();
             return out.toString();
         }
         catch(RepositoryException | RDFHandlerException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
         }
         
     }
     
     /**
      * Allows a part of the artifact to be modified
      * 
      * @param artifactUri
      * @param in
      * @param contentType
      * @param isReplace
      * @return
      * @throws IOException
      * @throws PoddException
      * @throws OWLException
      * @throws OpenRDFException
      */
     public String editArtifact(final String artifactUri, final InputStream in, final String contentType,
             final boolean isReplace, final boolean checkFileReferences) throws PoddException, OpenRDFException,
         IOException, OWLException
     {
         this.log.info("EDIT artifact: " + artifactUri);
         RepositoryConnection repositoryConnection = null;
         
         Repository tempRepository = null;
         RepositoryConnection tempRepositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             // get the artifact's IDs
             final InferredOWLOntologyID ontologyID = this.getInferredOWLOntologyIDForArtifact(artifactUri);
             this.checkArtifactExists(repositoryConnection, ontologyID);
             
             final URI context = ontologyID.getVersionIRI().toOpenRDFURI();
             final URI inferredContext = ontologyID.getInferredOntologyIRI().toOpenRDFURI();
             
             // create a temporary in-memory repository
             tempRepository = new SailRepository(new MemoryStore());
             tempRepository.initialize();
             tempRepositoryConnection = tempRepository.getConnection();
             tempRepositoryConnection.setAutoCommit(false);
             
             // load and copy the artifact's asserted statements to the temporary store
             final RepositoryResult<Statement> repoResult =
                     repositoryConnection.getStatements(null, null, null, false, context);
             tempRepositoryConnection.add(repoResult, context);
             tempRepositoryConnection.commit();
             
             if(isReplace)
             {
                 // create an intermediate context and add "edit" statements to it
                 final URI intContext = IRI.create("urn:intermediate:").toOpenRDFURI();
                 tempRepositoryConnection.add(in, "", Rio.getParserFormatForMIMEType(contentType), intContext);
                 
                 // get all Subjects in "edit" statements
                 final RepositoryResult<Statement> statements =
                         tempRepositoryConnection.getStatements(null, null, null, false, intContext);
                 final List<Statement> allEditStatements = Iterations.addAll(statements, new ArrayList<Statement>());
                 
                 // remove all references to these Subjects in "main" context
                 for(final Statement statement : allEditStatements)
                 {
                     tempRepositoryConnection.remove(statement.getSubject(), null, null, context);
                 }
                 
                 // copy the "edit" statements from intermediate context into our "main" context
                 tempRepositoryConnection.add(
                         tempRepositoryConnection.getStatements(null, null, null, false, intContext), context);
             }
             else
             {
                 tempRepositoryConnection.add(in, "", Rio.getParserFormatForMIMEType(contentType), context);
             }
             tempRepositoryConnection.commit();
             
             if(checkFileReferences)
             {
                 // final FileReferenceUtils fileRefUtils = FileReferenceUtils.getInstance();
                 this.fileReferenceUtils.checkFileReferencesInRDF(tempRepositoryConnection, context);
             }
             
             final String uniqueUriString = artifactUri.substring(0, artifactUri.lastIndexOf("/"));
             
             URITranslator.doTranslation(tempRepositoryConnection, "urn:temp:", uniqueUriString + "/", context);
             
             tempRepositoryConnection.commit();
             
             // increment the version
             final URI newVersionURI =
                     IRI.create(PoddServletHelper.incrementVersion(context.stringValue())).toOpenRDFURI();
             
             URITranslator.doTranslation(tempRepositoryConnection, context.stringValue(), newVersionURI.stringValue(),
                     context);
             
             // load into OWLAPI
             this.log.debug("Loading podd artifact from temp repository: {}", context);
             final OWLOntology nextOntology =
                     this.utils.loadOntology(tempRepositoryConnection, RDFFormat.RDFXML.getDefaultMIMEType(), context);
             
             // regain memory after loading the ontology into OWLAPI
             tempRepositoryConnection.clear();
             
             // 2. Validate the object in terms of the OWL profile
             // 3. Validate the object using a reasoner
             final OWLReasoner reasoner = this.utils.checkConsistency(nextOntology);
             
             // 4. Store the object
             this.utils.dumpOntologyToRepository(nextOntology, repositoryConnection);
             
             // 5. Infer extra statements about the object using a reasoner
             this.log.debug("Computing inferences for podd artifact");
             final OWLOntology nextInferredOntology =
                     this.utils.computeInferences(reasoner,
                             this.utils.generateInferredOntologyID(nextOntology.getOntologyID()));
             
             // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
             // Sesame Repository
             // 6. Store the inferred statements
             this.utils.dumpOntologyToRepository(nextInferredOntology, repositoryConnection);
             
             // update the PODD artifact management graph with links to the new version of the
             // artifact
             this.utils.updateCurrentManagedPoddArtifactOntologyVersion(repositoryConnection,
                     nextOntology.getOntologyID(), nextInferredOntology.getOntologyID());
             
             // delete the earlier version from the store
             // NOTE: deleting and computing inferences should be scheduled on a separate thread
             repositoryConnection.clear(context);
             repositoryConnection.clear(inferredContext);
             repositoryConnection.commit();
             
         }
         catch(PoddException | OWLException | OpenRDFException | IOException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             if(tempRepositoryConnection != null)
             {
                 tempRepositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
             if(tempRepositoryConnection != null)
             {
                 tempRepositoryConnection.rollback();
                 tempRepositoryConnection.close();
             }
             if(tempRepository != null)
             {
                 tempRepository.shutDown();
             }
         }
         
         return artifactUri;
     }
     
     /**
      * If the specified artifact and object exist inside PODD, attach the given file reference to
      * the object.
      * 
      * @param fileReference
      * @throws PoddException
      * @throws OpenRDFException
      * @throws IOException
      * @throws OWLException
      */
     public URI attachReference(final FileReference fileReference, final boolean checkFileReferences)
         throws PoddException, OpenRDFException, IOException, OWLException
     {
         this.log.info("REFERENCE attach: " + fileReference.toString());
         
         RepositoryConnection repositoryConnection = null;
         Repository tempRepository = null;
         RepositoryConnection tempRepositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             final InferredOWLOntologyID ontologyID =
                     this.getInferredOWLOntologyIDForArtifact(fileReference.getArtifactUri());
             
             this.checkArtifactExists(repositoryConnection, ontologyID);
             
             // check the object exists in this artifact
             final URI context = ontologyID.getVersionIRI().toOpenRDFURI();
             final URI objectToAttachTo = IRI.create(fileReference.getObjectUri()).toOpenRDFURI();
             final RepositoryResult<Statement> statements =
                     repositoryConnection.getStatements(objectToAttachTo, null, null, false, context);
             if(!statements.hasNext())
             {
                 repositoryConnection.rollback();
                 throw new RuntimeException("Object <" + fileReference.getObjectUri() + "> not found.");
             }
             repositoryConnection.rollback();
             
             // populate a temporary in-memory repository with File Reference statements
             tempRepository = new SailRepository(new MemoryStore());
             tempRepository.initialize();
             tempRepositoryConnection = tempRepository.getConnection();
             tempRepositoryConnection.setAutoCommit(false);
             
             final URI fileReferenceURI =
                     FileReferenceUtils.addFileReferenceAsTriplesToRepository(tempRepositoryConnection, fileReference,
                             context);
             tempRepositoryConnection.commit();
             
             // get the File Reference statements as an OutputStream
             final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final RDFHandler rdfWriter =
                     Rio.createWriter(Rio.getWriterFormatForMIMEType(PoddServlet.MIME_TYPE_RDF_XML, RDFFormat.RDFXML),
                             out);
             tempRepositoryConnection.export(rdfWriter);
             tempRepositoryConnection.clear();
             tempRepositoryConnection.rollback();
             
             // merge the File Referencing statements with the artifact in the repository
             this.editArtifact(fileReference.getArtifactUri(), new ByteArrayInputStream(out.toByteArray()),
                     PoddServlet.MIME_TYPE_RDF_XML, false, checkFileReferences);
             
             return fileReferenceURI;
         }
         catch(PoddException | OWLException | OpenRDFException | IOException e)
         {
             repositoryConnection.rollback();
             tempRepositoryConnection.rollback();
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
         }
     }
     
     /**
      * This method clears the content of the RDF store and reloads the schema ontologies such that
      * it resembles the store of a freshly installed and unused PODD.
      * 
      * @throws RepositoryException
      */
     public void resetPodd() throws RepositoryException
     {
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             repositoryConnection.clear();
             repositoryConnection.commit();
         }
         catch(final RepositoryException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
         }
         
         // -- remove all the ontologies from the manager
         final Set<OWLOntology> setOfOwlOntologies = new HashSet<>(this.manager.getOntologies());
         for(final OWLOntology ontology : setOfOwlOntologies)
         {
             this.manager.removeOntology(ontology);
         }
         
         // -- reload the schema ontologies
         try
         {
             this.log.info("Reloading schema ontologies after resetting the repository");
             this.loadSchemaOntologies();
         }
         catch(OWLException | OpenRDFException | IOException | PoddException e)
         {
             this.log.error("Could not reload schema ontologies {}", e);
         }
     }
     
     /**
      * Checks whether the specified artifact exists within the given RepositoryConnection. A
      * RuntimeException is thrown if the artifact could not be found.
      * 
      * @param artifactUri
      * @param repositoryConnection
      * @param ontologyID
      * @throws RepositoryException
      */
     private void checkArtifactExists(final RepositoryConnection repositoryConnection,
             final InferredOWLOntologyID ontologyID) throws RepositoryException
     {
         if(ontologyID.getVersionIRI() == null
                 || repositoryConnection.size(ontologyID.getVersionIRI().toOpenRDFURI()) < 1)
         {
             // FIXME: Create a subclass of PoddException for this type of exception
             throw new RuntimeException("Artifact <" + ontologyID.getOntologyIRI() + "> not found.");
         }
     }
     
     /**
      * This method takes a URL encoded String terminating with a colon (encoded as %3A) followed by
      * an integer and increments this integer by one. If the input String is not of the expected
      * format, incrementing is carried out by simply appending "1" to the end of the String.
      * 
      * E.g.: "http://purl.org/abcd/artifact%3A25" is converted to
      * "http://purl.org/abcd/artifact%3A26"
      * 
      * @param oldVersion
      * @return
      */
     public static String incrementVersion(final String oldVersion)
     {
         final int positionVersionSeparator = oldVersion.lastIndexOf("%3A");
         if(positionVersionSeparator < 1)
         {
             return oldVersion.concat("1");
         }
         final String prefix = oldVersion.substring(0, positionVersionSeparator);
         final String version = oldVersion.substring(positionVersionSeparator + 3);
         
         try
         {
             int versionInt = Integer.parseInt(version);
             versionInt++;
             return prefix + "%3A" + versionInt;
         }
         catch(final NumberFormatException e)
         {
             return oldVersion.concat("1");
         }
     }
     
     /**
      * Given an Ontology URI (in String format), generates an InferredOWLOntologyID.
      * 
      * @param artifactUri
      * @param repositoryConnection
      * @return
      * @throws RepositoryException
      */
     public InferredOWLOntologyID getInferredOWLOntologyIDForArtifact(final String artifactUri)
         throws RepositoryException
     {
         final IRI ontologyIRI = IRI.create(artifactUri);
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.nextRepository.getConnection();
             repositoryConnection.setAutoCommit(false);
             
             final RepositoryResult<Statement> repoResult =
                     repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(), null, null, true,
                             this.poddArtifactManagementGraph);
             
             IRI ontologyVersionIRI = null;
             IRI ontologyInferredIRI = null;
             while(repoResult.hasNext())
             {
                 final Statement statement = repoResult.next();
                 if(PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION.equals(statement.getPredicate()))
                 {
                     ontologyInferredIRI = IRI.create(statement.getObject().stringValue());
                 }
                 else if(PoddPrototypeUtils.OWL_VERSION_IRI.equals(statement.getPredicate()))
                 {
                     ontologyVersionIRI = IRI.create(statement.getObject().stringValue());
                 }
             }
             
             repositoryConnection.rollback();
             return new InferredOWLOntologyID(ontologyIRI, ontologyVersionIRI, ontologyInferredIRI);
         }
         catch(final RepositoryException e)
         {
             if(repositoryConnection != null)
             {
                 repositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Test repository connection could not be closed", e);
                 }
             }
         }
     }
     
     /**
      * Converts incoming (URL-encoded) URI strings to a format compliant with the one used by PODD.
      * Only URI strings starting with 'http' and 'https' are processed. All others pass through this
      * method unchanged.
      * 
      * - If the scheme in the incoming URI String is separated by a '/' only, this is replaced with
      * '://'.
      * 
      * - The final part of the <i>path</i>, <i>query</i> and <i>fragment</i> are URL encoded
      * 
      * For examples see PoddServletHelperTest.testExtractUri() For URI Encoding/decoding concerns
      * see https://tools.ietf.org/html/rfc3986#section-2.1
      * 
      * NOTE: This method is fairly weak in that anything beyond what has been tested could fail.
      * 
      * @param uriString
      *            URL encoded string of the form "http/host.com/..."
      * @return
      * @throws URISyntaxException
      *             If the URI is too short (i.e. less than 6 characters)
      * @throws UnsupportedEncodingException
      */
     public static String extractUri(String uriString) throws URISyntaxException, UnsupportedEncodingException
     {
         // -- check length (can we have valid URIs less than 6 characters in PODD?)
         if(uriString == null || uriString.length() < 6)
         {
             throw new URISyntaxException(uriString, "Too short to be a URI in the expected format");
         }
         
         // -- add proper scheme separation characters
         if(uriString.startsWith("http/"))
         {
             uriString = uriString.replaceFirst("http/", "http://");
         }
         else if(uriString.startsWith("https/"))
         {
             uriString = uriString.replaceFirst("https/", "https://");
         }
         else if(uriString.startsWith("ftp") || uriString.startsWith("mailto") || uriString.startsWith("ssh"))
         {
             return uriString;
         }
         
         final java.net.URI uri = new java.net.URI(uriString);
         if(uri.isOpaque())
         {
             return uriString;
         }
         
         final StringBuilder b = new StringBuilder();
         b.append(uri.getScheme());
         b.append(":");
         b.append("//"); // works for http, which is all we support now
         b.append(uri.getAuthority());
         
         // final part of the Path may have characters that need to be encoded (e.g. .../artifact:1)
         final String path = uri.getPath();
         if(path != null)
         {
             final String keepAs = path.substring(0, path.lastIndexOf("/") + 1);
             final String toEncode = path.substring(path.lastIndexOf("/") + 1);
             
             final String encoded = java.net.URLEncoder.encode(toEncode, "UTF-8");
             b.append(keepAs);
             b.append(encoded);
         }
         
         // encode query part
         if(uri.getQuery() != null && !uri.getQuery().isEmpty())
         {
             b.append("?");
             b.append(java.net.URLEncoder.encode(uri.getQuery(), "UTF-8"));
         }
         
         // encode fragment part
         if(uri.getFragment() != null && !uri.getFragment().isEmpty())
         {
             b.append("#");
             b.append(java.net.URLEncoder.encode(uri.getFragment(), "UTF-8"));
         }
         
         return b.toString();
     }
     
     public void tearDown() throws RepositoryException
     {
         this.nextValueFactory = null;
         
         if(this.nextRepository != null)
         {
             try
             {
                 this.nextRepository.shutDown();
             }
             catch(final RepositoryException e)
             {
                 this.log.error("Test repository could not be shutdown", e);
                 throw e;
             }
         }
         
         this.nextRepository = null;
     }
     
     public Repository loadDataToNewRepository(final InputStream in, final String mimeType, final Resource... context)
         throws RepositoryException, RDFParseException, IOException
     {
         // create a temporary Repository
         final Repository tempRepository = new SailRepository(new MemoryStore());
         tempRepository.initialize();
         RepositoryConnection tempRepositoryConnection = null;
         
         try
         {
             tempRepositoryConnection = tempRepository.getConnection();
             tempRepositoryConnection.setAutoCommit(false);
             
             // add data to Repository
             tempRepositoryConnection.add(in, "", Rio.getParserFormatForMIMEType(mimeType), context);
             tempRepositoryConnection.commit();
             
             return tempRepository;
         }
         catch(RepositoryException | RDFParseException | IOException e)
         {
             if(tempRepositoryConnection != null)
             {
                 tempRepositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(tempRepositoryConnection != null)
             {
                 tempRepositoryConnection.close();
             }
         }
     }
     
     private void printGraph(final URI context, final RepositoryConnection con)
     {
         System.out.println("====Printing Graph=====");
         org.openrdf.repository.RepositoryResult<Statement> results;
         try
         {
             results = con.getStatements(null, null, null, false, context);
             while(results.hasNext())
             {
                 final Statement triple = results.next();
                 final String tripleStr = java.net.URLDecoder.decode(triple.toString(), "UTF-8");
                 System.out.println(" --- " + tripleStr);
             }
         }
         catch(final Exception e)
         {
             e.printStackTrace();
         }
     }
     
 }
