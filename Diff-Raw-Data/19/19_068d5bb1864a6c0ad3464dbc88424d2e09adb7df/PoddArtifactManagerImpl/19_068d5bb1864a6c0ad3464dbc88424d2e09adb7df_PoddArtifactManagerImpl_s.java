 /**
  * 
  */
 package com.github.podd.impl;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
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
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.api.file.PoddFileReference;
 import com.github.podd.api.file.PoddFileReferenceManager;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlReference;
 import com.github.podd.exception.DeleteArtifactException;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.PoddRuntimeException;
 import com.github.podd.exception.PublishArtifactException;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
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
     
     private PoddSesameManager sesameManager;
     
     /**
      * 
      */
     public PoddArtifactManagerImpl()
     {
         // TODO Auto-generated constructor stub
     }
     
     @Override
     public boolean deleteArtifact(final OWLOntologyID artifactId) throws PoddException
     {
         if(artifactId.getOntologyIRI() == null)
         {
             throw new PoddRuntimeException("Ontology IRI cannot be null");
         }
         
         RepositoryConnection connection = null;
         
         try
         {
             connection = this.getRepositoryManager().getRepository().getConnection();
             
             List<InferredOWLOntologyID> requestedArtifactIds =
                     this.getSesameManager().getAllOntologyVersions(artifactId.getOntologyIRI(), connection,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             if(artifactId.getVersionIRI() != null)
             {
                 final IRI requestedVersionIRI = artifactId.getVersionIRI();
                 
                 for(final InferredOWLOntologyID nextVersion : new ArrayList<InferredOWLOntologyID>(requestedArtifactIds))
                 {
                     if(requestedVersionIRI.equals(nextVersion.getVersionIRI()))
                     {
                         requestedArtifactIds = Arrays.asList(nextVersion);
                     }
                 }
             }
             
             connection.begin();
             this.getSesameManager().deleteOntologies(requestedArtifactIds, connection,
                     this.getRepositoryManager().getArtifactManagementGraph());
             connection.commit();
             
             return !requestedArtifactIds.isEmpty();
         }
         catch(final OpenRDFException e)
         {
             try
             {
                 if(connection != null && connection.isActive())
                 {
                     connection.rollback();
                 }
             }
             catch(final RepositoryException e1)
             {
             }
             
             throw new DeleteArtifactException("Repository exception occurred", e, artifactId);
         }
         finally
         {
             try
             {
                 if(connection != null && connection.isOpen())
                 {
                     connection.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 throw new DeleteArtifactException("Repository exception occurred", e, artifactId);
             }
         }
     }
     
     @Override
     public void exportArtifact(final InferredOWLOntologyID ontologyId, final OutputStream outputStream,
             final RDFFormat format, final boolean includeInferred) throws OpenRDFException, PoddException, IOException
     {
         if(ontologyId.getOntologyIRI() == null || ontologyId.getVersionIRI() == null)
         {
             throw new PoddRuntimeException("Ontology IRI and Version IRI cannot be null");
         }
         
         if(includeInferred && ontologyId.getInferredOntologyIRI() == null)
         {
             throw new PoddRuntimeException("Inferred Ontology IRI cannot be null");
         }
         
         List<URI> contexts;
         
         if(includeInferred)
         {
             contexts =
                     Arrays.asList(ontologyId.getVersionIRI().toOpenRDFURI(), ontologyId.getInferredOntologyIRI()
                             .toOpenRDFURI());
         }
         else
         {
             contexts = Arrays.asList(ontologyId.getVersionIRI().toOpenRDFURI());
         }
         
         RepositoryConnection connection = null;
         
         try
         {
             connection = this.getRepositoryManager().getRepository().getConnection();
             
             connection.export(Rio.createWriter(format, outputStream), contexts.toArray(new Resource[] {}));
         }
         finally
         {
             if(connection != null)
             {
                 connection.close();
             }
         }
     }
     
     @Override
     public InferredOWLOntologyID getArtifactByIRI(final IRI artifactIRI) throws UnmanagedArtifactIRIException
     {
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.getRepositoryManager().getRepository().getConnection();
             
             return this.getSesameManager().getCurrentArtifactVersion(artifactIRI, repositoryConnection,
                     this.getRepositoryManager().getArtifactManagementGraph());
         }
         catch(final OpenRDFException e)
         {
             throw new UnmanagedArtifactIRIException(artifactIRI, e);
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
                     this.log.error("Failed to close repository connection", e);
                 }
             }
         }
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
     
     @Override
     public PoddSesameManager getSesameManager()
     {
         return this.sesameManager;
     }
     
     /**
      * This is not an API method. QUESTION: Should this be moved to a separate utility class?
      * 
      * This method takes a String terminating with a colon (":") followed by an integer and
      * increments this integer by one. If the input String is not of the expected format, appends
      * "1" to the end of the String.
      * 
      * E.g.: "http://purl.org/ab/artifact:55" is converted to "http://purl.org/ab/artifact:56"
      * "http://purl.org/ab/artifact:5A" is converted to "http://purl.org/ab/artifact:5A1"
      * 
      * @param oldVersion
      * @return
      */
     public String incrementVersion(final String oldVersion)
     {
         final char versionSeparatorChar = ':';
         
         final int positionVersionSeparator = oldVersion.lastIndexOf(versionSeparatorChar);
         if(positionVersionSeparator > 1)
         {
             final String prefix = oldVersion.substring(0, positionVersionSeparator);
             final String version = oldVersion.substring(positionVersionSeparator + 1);
             try
             {
                 int versionInt = Integer.parseInt(version);
                 versionInt++;
                 return prefix + versionSeparatorChar + versionInt;
             }
             catch(final NumberFormatException e)
             {
                 return oldVersion.concat("1");
             }
         }
         return oldVersion.concat("1");
     }
     
     @Override
     public Collection<InferredOWLOntologyID> listPublishedArtifacts() throws OpenRDFException
     {
         return listArtifacts(true, false);
     }
     
     @Override
     public Collection<InferredOWLOntologyID> listUnpublishedArtifacts() throws OpenRDFException
     {
         return listArtifacts(false, true);
     }
     
     private Collection<InferredOWLOntologyID> listArtifacts(boolean published, boolean unpublished)
         throws OpenRDFException
     {
         if(!published && !unpublished)
         {
             throw new IllegalArgumentException("Cannot choose to exclude both published and unpublished artifacts");
         }
         
         Set<InferredOWLOntologyID> results = new HashSet<InferredOWLOntologyID>();
         
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             Collection<InferredOWLOntologyID> ontologies =
                     this.getSesameManager().getOntologies(true, conn,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             for(InferredOWLOntologyID nextOntology : ontologies)
             {
                 boolean isPublished =
                         this.getSesameManager().isPublished(nextOntology, conn,
                                 this.getRepositoryManager().getArtifactManagementGraph());
                 
                 if(isPublished)
                 {
                     if(published)
                     {
                         results.add(nextOntology);
                     }
                 }
                 else if(unpublished)
                 {
                     results.add(nextOntology);
                 }
             }
         }
         finally
         {
             if(conn != null && conn.isOpen())
             {
                 conn.close();
             }
         }
         return results;
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
             if(this.getFileReferenceManager() != null)
             {
                 this.log.info("Handling File reference validation");
                 // calls, to setup the results collection
                 final Set<PoddFileReference> fileReferenceResults =
                         this.getFileReferenceManager().extractFileReferences(temporaryRepositoryConnection,
                                 randomContext);
                 
                 // optionally verify the file references
                 if(fileReferenceResults.size() > 0)
                 {
                     this.getFileReferenceManager().verifyFileReferences(fileReferenceResults,
                             temporaryRepositoryConnection, randomContext);
                     // TODO: Optionally remove invalid file references or mark them as invalid using
                     // RDF statements/OWL Classes
                 }
             }
             
             final Repository permanentRepository = this.getRepositoryManager().getRepository();
             permanentRepositoryConnection = permanentRepository.getConnection();
             permanentRepositoryConnection.begin();
             
             // Set a Version IRI for this artifact
             /*
              * Version information need not be available in uploaded artifacts (any existing values
              * are ignored).
              * 
              * For a new artifact, a Version IRI is created based on the Ontology IRI while for a
              * new version of a managed artifact, the most recent version is incremented.
              */
             final IRI ontologyIRI =
                     this.getSesameManager().getOntologyIRI(temporaryRepositoryConnection, randomContext);
             if(ontologyIRI != null)
             {
                 // check for managed version from artifact graph
                 OWLOntologyID currentManagedArtifactID = null;
                 try
                 {
                     currentManagedArtifactID =
                             this.getSesameManager().getCurrentArtifactVersion(ontologyIRI,
                                     permanentRepositoryConnection,
                                     this.getRepositoryManager().getArtifactManagementGraph());
                 }
                 catch(final UnmanagedArtifactIRIException e)
                 {
                     // ignore. indicates a new artifact is being uploaded
                     this.log.debug("This is an unmanaged artifact IRI {}", ontologyIRI);
                 }
                 
                 IRI newVersionIRI = null;
                 if(currentManagedArtifactID == null || currentManagedArtifactID.getVersionIRI() == null)
                 {
                     newVersionIRI = IRI.create(ontologyIRI.toString() + ":version:1");
                 }
                 else
                 {
                     newVersionIRI =
                             IRI.create(this.incrementVersion(currentManagedArtifactID.getVersionIRI().toString()));
                 }
                 
                 // set version IRI in temporary repository
                 this.log.info("Setting version IRI to <{}>", newVersionIRI);
                 temporaryRepositoryConnection.remove(ontologyIRI.toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                         null, randomContext);
                 temporaryRepositoryConnection.add(ontologyIRI.toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                         newVersionIRI.toOpenRDFURI(), randomContext);
             }
             
             // Before loading the statements into OWLAPI, ensure that the schema ontologies are
             // cached in memory
             final Set<IRI> directImports =
                     this.getSesameManager().getDirectImports(temporaryRepositoryConnection, randomContext);
             for(final IRI schemaOntologyIRI : directImports)
             {
                 // Get the current version
                 final InferredOWLOntologyID ontologyVersion =
                         this.getSesameManager().getCurrentSchemaVersion(schemaOntologyIRI,
                                 permanentRepositoryConnection, this.getRepositoryManager().getSchemaManagementGraph());
                 
                 // Make sure it is cached in memory
                 this.getOWLManager().cacheSchemaOntology(ontologyVersion, permanentRepositoryConnection,
                         this.getRepositoryManager().getSchemaManagementGraph());
             }
             
             // Load the statements into an OWLAPI OWLOntology
             final List<Statement> statements =
                     Iterations.asList(temporaryRepositoryConnection
                             .getStatements(null, null, null, true, randomContext));
             
             final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(statements.iterator());
             
             owlSource.setNamespaces(temporaryRepositoryConnection.getNamespaces());
             
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
             
             this.getSesameManager().updateManagedPoddArtifactVersion(inferredOWLOntologyID, true,
                     permanentRepositoryConnection, this.getRepositoryManager().getArtifactManagementGraph());
             
             permanentRepositoryConnection.commit();
             
             return inferredOWLOntologyID;
         }
         catch(final Exception e)
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
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.
      * OWLOntologyID)
      */
     @Override
     public InferredOWLOntologyID publishArtifact(final OWLOntologyID ontologyId) throws OpenRDFException,
         PublishArtifactException, UnmanagedArtifactIRIException
     {
         final IRI ontologyIRI = ontologyId.getOntologyIRI();
         final IRI versionIRI = ontologyId.getVersionIRI();
         
         if(versionIRI == null)
         {
             throw new PublishArtifactException("Could not publish artifact as version was not specified.", ontologyId);
         }
         
         Repository repository = null;
         RepositoryConnection repositoryConnection = null;
         try
         {
             repository = this.getRepositoryManager().getRepository();
             repositoryConnection = repository.getConnection();
             repositoryConnection.begin();
             
             if(this.getSesameManager().isPublished(ontologyId, repositoryConnection,
                     this.getRepositoryManager().getArtifactManagementGraph()))
             {
                 // Cannot publish multiple versions of a single artifact
                 throw new PublishArtifactException("Could not publish artifact as a version was already published",
                         ontologyId);
             }
             
             final InferredOWLOntologyID currentVersion =
                     this.getSesameManager().getCurrentArtifactVersion(ontologyIRI, repositoryConnection,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             if(!currentVersion.getVersionIRI().equals(versionIRI))
             {
                 // User must make the given artifact version the current version manually before
                 // publishing, to ensure that work from the current version is not lost accidentally
                 throw new PublishArtifactException(
                         "Could not publish artifact as it was not the most current version.", ontologyId);
             }
             
             this.getSesameManager().setPublished(currentVersion, repositoryConnection,
                     this.getRepositoryManager().getArtifactManagementGraph());
             
             final InferredOWLOntologyID published =
                     this.getSesameManager().getCurrentArtifactVersion(ontologyIRI, repositoryConnection,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             repositoryConnection.commit();
             
             return published;
         }
         catch(final OpenRDFException | PublishArtifactException | UnmanagedArtifactIRIException e)
         {
             if(repositoryConnection != null && repositoryConnection.isActive())
             {
                 repositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
             // release resources
             if(repositoryConnection != null && repositoryConnection.isOpen())
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
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
     
     @Override
     public void setSesameManager(final PoddSesameManager sesameManager)
     {
         this.sesameManager = sesameManager;
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
