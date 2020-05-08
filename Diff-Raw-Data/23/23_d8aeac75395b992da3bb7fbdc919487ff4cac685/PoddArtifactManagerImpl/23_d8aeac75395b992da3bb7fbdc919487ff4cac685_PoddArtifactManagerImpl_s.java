 /**
  * 
  */
 package com.github.podd.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.rio.RDFFormat;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.profiles.OWLProfileReport;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.file.PoddFileReference;
 import com.github.podd.api.file.PoddFileReferenceManager;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlReference;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.PublishArtifactException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * Implementation of the PODD Artifact Manager API, to manage the lifecycle for PODD Artifacts.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PoddArtifactManagerImpl implements PoddArtifactManager
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     private PoddFileReferenceManager fileReferenceManager;
     private PoddOWLManager owlManager;
     private PoddPurlManager purlManager;
     private PoddSchemaManager schemaManager;
     private PoddRepositoryManager repositoryManager;
     
     /**
      * 
      */
     public PoddArtifactManagerImpl()
     {
         // TODO Auto-generated constructor stub
     }
     
     @Override
     public void exportArtifact(final OWLOntologyID ontologyId, final OutputStream outputStream, final RDFFormat format,
             final boolean includeInferred) throws OpenRDFException, PoddException, IOException
     {
         throw new RuntimeException("TODO: Implement exportArtifact");
     }
     
     @Override
     public InferredOWLOntologyID getArtifactByIRI(final IRI artifactIRI)
     {
         throw new RuntimeException("TODO: Implement getArtifactByIRI");
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getFileReferenceManager()
      */
     @Override
     public PoddFileReferenceManager getFileReferenceManager()
     {
         return this.fileReferenceManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getOWLManager()
      */
     @Override
     public PoddOWLManager getOWLManager()
     {
         return this.owlManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getPurlManager()
      */
     @Override
     public PoddPurlManager getPurlManager()
     {
         return this.purlManager;
     }
     
     @Override
     public PoddRepositoryManager getRepositoryManager()
     {
         return this.repositoryManager;
     }
     
     @Override
     public PoddSchemaManager getSchemaManager()
     {
         return this.schemaManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream,
      * org.openrdf.rio.RDFFormat)
      */
     @Override
     public InferredOWLOntologyID loadArtifact(final InputStream inputStream, RDFFormat format) throws OpenRDFException,
         PoddException, IOException, OWLException
     {
         if(inputStream == null)
         {
             throw new NullPointerException("Input stream must not be null");
         }
         
         if(format == null)
         {
             format = RDFFormat.RDFXML;
         }
         
         // connection to the temporary repository that the artifact RDF triples will be stored while
         // they are initially parsed by OWLAPI.
         final Repository tempRepository = this.repositoryManager.getNewTemporaryRepository();
         RepositoryConnection temporaryRepositoryConnection = null;
         
         RepositoryConnection permanentRepositoryConnection = null;
         InferredOWLOntologyID inferredOWLOntologyID = null;
         try
         {
             temporaryRepositoryConnection = tempRepository.getConnection();
             final URI randomContext =
                     ValueFactoryImpl.getInstance().createURI("urn:uuid:" + UUID.randomUUID().toString());
             
             // Load the artifact RDF triples into a random context in the temp repository, which may
             // be shared between different uploads
             temporaryRepositoryConnection.add(inputStream, "", format, randomContext);
             
             if(this.getPurlManager() != null)
             {
                 this.log.info("Handling Purl generation");
                 final Set<PoddPurlReference> purlResults =
                         this.getPurlManager().extractPurlReferences(temporaryRepositoryConnection, randomContext);
                 
                 this.getPurlManager().convertTemporaryUris(purlResults, temporaryRepositoryConnection, randomContext);
             }
             
             // Then work on the file references
             // FIXME: implement file reference manager
             this.log.info("Skipping file reference verification");
             final boolean isFileRefsFixed = false;
             if(this.getFileReferenceManager() != null && isFileRefsFixed)
             {
                 // calls, to setup the results collection
                 final Set<PoddFileReference> fileReferenceResults =
                         this.getFileReferenceManager().extractFileReferences(temporaryRepositoryConnection,
                                 randomContext);
                 
                 // optionally verify the file references
                 this.getFileReferenceManager().verifyFileReferences(fileReferenceResults,
                         temporaryRepositoryConnection, randomContext);
                 
                 // TODO: Optionally remove invalid file references or mark them as invalid using RDF
                 // statements/OWL Classes
             }
             
             final Repository permanentRepository = this.getRepositoryManager().getRepository();
             permanentRepositoryConnection = permanentRepository.getConnection();
             permanentRepositoryConnection.begin();
             
             // Before loading the statements into OWLAPI, ensure that the schema ontologies are
             // cached in memory
             final Set<IRI> directImports = this.getDirectImports(temporaryRepositoryConnection, randomContext);
             for(final IRI schemaOntologyIRI : directImports)
             {
                 // Get the current version
                 final InferredOWLOntologyID ontologyVersion =
                         this.getOWLManager().getCurrentSchemaVersion(schemaOntologyIRI, permanentRepositoryConnection,
                                 this.getRepositoryManager().getSchemaManagementGraph());
                 
                 // Make sure it is cached in memory. This will not attempt to load the ontology
                 // again if it is already cached or already being loaded
                 this.getOWLManager().cacheSchemaOntology(ontologyVersion, permanentRepositoryConnection,
                         this.getRepositoryManager().getSchemaManagementGraph());
             }
             
             // Load the statements into an OWLAPI OWLOntology
             final List<Statement> statements =
                     temporaryRepositoryConnection.getStatements(null, null, null, true, randomContext).asList();
             
             final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(statements.iterator());
 
            //FIXME: setting namespaces leads to a NullPointerException in
            // RioOWLRDFConsumerAdapter.handleNamespace() line 70
            //owlSource.setNamespaces(temporaryRepositoryConnection.getNamespaces());
             
             final OWLOntology nextOntology = this.getOWLManager().loadOntology(owlSource);
             
             // Check the OWLAPI OWLOntology against an OWLProfile to make sure it is in profile
             final OWLProfileReport profileReport =
                     this.getOWLManager().getReasonerProfile().checkOntology(nextOntology);
             if(!profileReport.isInProfile())
             {
                 this.getOWLManager().removeCache(nextOntology.getOntologyID());
                 throw new OntologyNotInProfileException(nextOntology, profileReport,
                         "Ontology is not in required OWL Profile");
             }
             
             // Use the OWLManager to create a reasoner over the ontology
             final OWLReasoner nextReasoner = this.getOWLManager().createReasoner(nextOntology);
             
             // Test that the ontology was consistent with this reasoner
             // This ensures in the case of Pellet that it is in the OWL2-DL profile
             // if(!nextReasoner.isConsistent()
             // || nextReasoner.getUnsatisfiableClasses().getSize() > 0)
             // Check the OWLAPI OWLOntology using a reasoner for .isConsistent
             if(!nextReasoner.isConsistent())
             {
                 this.getOWLManager().removeCache(nextOntology.getOntologyID());
                 throw new InconsistentOntologyException(nextReasoner, "Ontology is inconsistent");
             }
             
             // Copy the statements to permanentRepositoryConnection
             this.getOWLManager().dumpOntologyToRepository(nextOntology, permanentRepositoryConnection,
                     nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
             
             // NOTE: At this stage, a client could be notified, and the artifact could be streamed
             // back to them from permanentRepositoryConnection
             
             // Use an OWLAPI InferredAxiomGenerator together with the reasoner to create inferred
             // axioms to store in the database
             // Serialise the inferred statements back to a different context in the permanent
             // repository connection
             // The contexts to use within the permanent repository connection are all encapsulated
             // in the InferredOWLOntologyID object
             inferredOWLOntologyID = this.getOWLManager().inferStatements(nextOntology, permanentRepositoryConnection);
             
             permanentRepositoryConnection.commit();
             
             return inferredOWLOntologyID;
         }
        catch(OpenRDFException | PoddException | IOException | OWLException e)
         {
             if(temporaryRepositoryConnection != null && temporaryRepositoryConnection.isActive())
             {
                 temporaryRepositoryConnection.rollback();
             }
             
             if(permanentRepositoryConnection != null && permanentRepositoryConnection.isActive())
             {
                 permanentRepositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
             // release resources
            
             if(inferredOWLOntologyID != null)
             {
                 this.getOWLManager().removeCache(inferredOWLOntologyID);
             }
             
             if(temporaryRepositoryConnection != null && temporaryRepositoryConnection.isOpen())
             {
                 try
                 {
                     temporaryRepositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
             tempRepository.shutDown();
             
             if(permanentRepositoryConnection != null && permanentRepositoryConnection.isOpen())
             {
                 try
                 {
                     permanentRepositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
     }
     
     /**
      * This is not an API method. Retrieves the ontology IRIs for all import statements found in the
      * given repository.
      * 
      * QUESTION: Should this be moved to a separate utility class or even to the PoddOWLManager?
      * 
      * @param repositoryConnection
      * @param context
      * @return
      * @throws OpenRDFException
      */
     public Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI context)
         throws OpenRDFException
     {
         final String sparqlQuery = "SELECT ?x WHERE { ?y <" + OWL.IMPORTS.stringValue() + "> ?x ." + " }";
         this.log.info("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(context);
         dataset.addNamedGraph(context);
         query.setDataset(dataset);
         
         final Set<IRI> results = Collections.newSetFromMap(new ConcurrentHashMap<IRI, Boolean>());
         
         final TupleQueryResult queryResults = query.evaluate();
         while(queryResults.hasNext())
         {
             final BindingSet nextResult = queryResults.next();
             final String ontologyIRI = nextResult.getValue("x").stringValue();
             results.add(IRI.create(ontologyIRI));
             
         }
         return results;
     }
     
     /**
      * This is not an API method. Extracts an OWLOntologyID from the statements in the given
      * RepositoryConnection.
      * 
      * QUESTION: Should this be moved to a separate utility class or even to the PoddOWLManager?
      * 
      * @param repositoryConnection
      * @param context
      * @return
      * @throws OpenRDFException
      */
     public OWLOntologyID extractOWLOntologyIDFromRepository(final RepositoryConnection repositoryConnection,
             final URI context) throws OpenRDFException
     {
         final String sparqlQuery =
                 "SELECT ?x ?xv WHERE { ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> ."
                         + " ?x <" + PoddRdfConstants.OWL_VERSION_IRI + "> ?xv . }";
         this.log.info("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(context);
         dataset.addNamedGraph(context);
         query.setDataset(dataset);
         
         final TupleQueryResult queryResults = query.evaluate();
         if(queryResults.hasNext())
         {
             final BindingSet nextResult = queryResults.next();
             final String ontologyIRI = nextResult.getValue("x").stringValue();
             final String versionIRI = nextResult.getValue("xv").stringValue();
             
             return new OWLOntologyID(IRI.create(ontologyIRI), IRI.create(versionIRI));
         }
         return new OWLOntologyID();
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.
      * OWLOntologyID)
      */
     @Override
     public InferredOWLOntologyID publishArtifact(final OWLOntologyID ontologyId) throws PublishArtifactException
     {
         if(ontologyId.getVersionIRI() == null)
         {
             throw new PublishArtifactException("Could not publish artifact as version was not specified.", ontologyId);
         }
         
         if(this.getOWLManager().isPublished(ontologyId.getOntologyIRI()))
         {
             // Cannot publish multiple versions of a single artifact
             throw new PublishArtifactException("Could not publish artifact as a version was already published",
                     ontologyId);
         }
         
         final OWLOntologyID currentVersion = this.getOWLManager().getCurrentVersion(ontologyId.getOntologyIRI());
         
         if(!currentVersion.getVersionIRI().equals(ontologyId.getVersionIRI()))
         {
             // User must make the given artifact version the current version manually before
             // publishing, to ensure that work from the current version is not lost accidentally
             throw new PublishArtifactException("Could not publish artifact as it was not the most current version.",
                     ontologyId);
         }
         
         final InferredOWLOntologyID published = this.getOWLManager().setPublished(ontologyId);
         
         return published;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setFileReferenceManager(com.github.podd.api.file.
      * PoddFileReferenceManager)
      */
     @Override
     public void setFileReferenceManager(final PoddFileReferenceManager fileManager)
     {
         this.fileReferenceManager = fileManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setOwlManager(com.github.podd.api.PoddOWLManager)
      */
     @Override
     public void setOwlManager(final PoddOWLManager owlManager)
     {
         this.owlManager = owlManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setPurlManager(com.github.podd.api.purl.PoddPurlManager
      * )
      */
     @Override
     public void setPurlManager(final PoddPurlManager purlManager)
     {
         this.purlManager = purlManager;
     }
     
     @Override
     public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
     {
         this.repositoryManager = repositoryManager;
     }
     
     @Override
     public void setSchemaManager(final PoddSchemaManager schemaManager)
     {
         this.schemaManager = schemaManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.
      * OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)
      */
     @Override
     public void updateSchemaImport(final OWLOntologyID artifactId, final OWLOntologyID schemaOntologyId)
     {
         throw new RuntimeException("TODO: Implement updateSchemaImport");
     }
     
 }
