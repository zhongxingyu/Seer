 /**
  * 
  */
 package com.github.podd.api;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.repository.Repository;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.resource.ResourceException;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 
 import com.github.podd.api.file.FileReference;
 import com.github.podd.api.file.FileReferenceManager;
 import com.github.podd.api.file.PoddFileRepositoryManager;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.PublishArtifactException;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddObjectLabel;
 
 /**
  * Manages PODD Artifacts, including loading and retrieving the ontologies representing the
  * artifacts and the association of artifacts with schema ontologies.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public interface PoddArtifactManager
 {
     InferredOWLOntologyID attachFileReference(InferredOWLOntologyID artifactId, URI objectUri,
             FileReference fileReference) throws OpenRDFException, PoddException;
     
     InferredOWLOntologyID attachFileReferences(URI artifactUri, URI versionUri, InputStream inputStream,
             RDFFormat format, FileReferenceVerificationPolicy fileReferenceVerificationPolicy) throws OpenRDFException,
         IOException, OWLException, PoddException;
     
     /**
      * Deletes the given artifact if and only if it is available and it is not currently published.
      * <p>
      * If the given artifactId contains a version IRI, then the specific version only must be
      * deleted, as long as it is not currently published.
      * <p>
      * If the artifactId contains a version IRI which is the current version, then another available
      * version may be substituted.
      * <p>
      * To ensure that all versions are deleted the user must ensure that the artifact is not
      * currently published, and that there is no version IRI attached to the given artifactId.
      * 
      * @param artifactId
      *            The {@link OWLOntologyID} containing the details of the artifact to delete.
      * @throws PoddException
      *             If an error occurred while deleting the artifact.
      */
     boolean deleteArtifact(InferredOWLOntologyID artifactId) throws PoddException;
     
     /**
      * Exports the given artifact to the given output stream using an RDF format.
      * 
      * @param ontologyId
      *            The {@link InferredOWLOntologyID} of the artifact to export. This must contain
      *            both an ontology IRI and a version IRI.
      * @param outputStream
      *            The {@link OutputStream} to export the RDF statements to.
      * @param format
      *            The {@link RDFFormat} for the exported RDF statements.
      * @param includeInferred
      *            If true, inferred statements will be included in the exported RDF statements,
      *            otherwise if false, only the concrete RDF statements will be exported.
      * @throws OpenRDFException
      *             If there is an error communicating the RDF storage for the artifact.
      * @throws PoddException
      *             If there is an error in the PODD methods.
      * @throws IOException
      *             If there is an error accessing the RDF storage, or an error writing to the output
      *             stream.
      */
     void exportArtifact(InferredOWLOntologyID ontologyId, OutputStream outputStream, RDFFormat format,
             boolean includeInferred) throws OpenRDFException, PoddException, IOException;
     
     /**
      * Exports metadata about the given object Type to the given output stream using an RDF Format.
      * This method is only to be used to obtain metadata for display purposes.
      * 
      * @param objectType
      * @param outputStream
      * @param format
      * @param includeDoNotDisplayProperties
      * @param includeContainsProperties
      *            If true, includes sub-properties of poddBase:contains
      * @param artifactID
      *            If present, the artifact ID is used to select the schema ontologies from which
      *            necessary metadata is to be extracted
      * @throws OpenRDFException
      * @throws PoddException
      * @throws IOException
      */
     void exportObjectMetadata(URI objectType, OutputStream outputStream, RDFFormat format,
             boolean includeDoNotDisplayProperties, boolean includeContainsProperties,
             final InferredOWLOntologyID artifactID) throws OpenRDFException, PoddException, IOException;
     
     /**
      * Returns the {@link InferredOWLOntologyID} for the artifact identified by the given IRI.
      * 
      * If the IRI maps to more than one version of an artifact, the most current version of the
      * artifact is returned.
      * 
      * @param artifactIRI
      *            The IRI of the Artifact to fetch. Can be either the version or the ontology IRI.
      * @return An {@link InferredOWLOntologyID} containing the full details for the artifact.
      * @throws UnmanagedArtifactIRIException
      *             If the artifact is not managed.
      */
     InferredOWLOntologyID getArtifact(IRI artifactIRI) throws UnmanagedArtifactIRIException;
     
     /**
      * Returns the {@link InferredOWLOntologyID} for the artifact identified by the given IRI and
      * version IRI.
      * <p>
      * If versionIRI is null, or the exact version is not available, then null is returned. In these
      * cases, {@link #getArtifact(IRI)} can be used as a backup to attempt to get the most current
      * version.
      * 
      * @param artifactIRI
      *            The IRI of the Artifact to fetch. Can be either the version or the ontology IRI.
      * @return An {@link InferredOWLOntologyID} containing the full details for the artifact or null
      *         if it was not found.
      * @throws UnmanagedArtifactIRIException
      *             If the artifact is not managed.
      */
     InferredOWLOntologyID getArtifact(IRI artifactIRI, IRI versionIRI) throws UnmanagedArtifactIRIException;
     
     /**
      * 
      * @return The {@link FileReferenceManager} used to create and fetch file references from
      *         artifacts.
      */
     FileReferenceManager getFileReferenceManager();
     
     Set<FileReference> getFileReferences(InferredOWLOntologyID artifactId);
     
     Set<FileReference> getFileReferences(InferredOWLOntologyID artifactId, String alias);
     
     Set<FileReference> getFileReferences(InferredOWLOntologyID artifactId, URI objectUri);
     
     /**
      * 
      * @return The {@link PoddFileRepositoryManager} used to manage external file repository
      *         configurations.
      */
     PoddFileRepositoryManager getFileRepositoryManager();
     
     /**
      * 
      * @return The {@link PoddOWLManager} used to manage OWL validation and inferencing for
      *         artifacts.
      */
     PoddOWLManager getOWLManager();
     
     /**
      * 
      * @return The {@link PoddPurlManager} used to manage PURL creation and validation for URIs in
      *         artifacts.
      */
     PoddPurlManager getPurlManager();
     
     /**
      * 
      * @return The {@link PoddRepositoryManager} used to manage access to the {@link Repository}.
      */
     PoddRepositoryManager getRepositoryManager();
     
     /**
      * 
      * @return The {@link PoddSchemaManager} used to access and verify versions of Schema Ontologies
      *         used in artifacts.
      */
     PoddSchemaManager getSchemaManager();
     
     /**
      * 
      * @return The {@link PoddSesameManager} used to perform operations on a Sesame Repository
      */
     PoddSesameManager getSesameManager();
     
     /**
      * A list of labels for the top objects in the given artifacts.
      * 
      * @param artifacts
      *            A sorted list of artifact identifiers that require labels.
      * @return A list of labels in the same order as the artifacts.
      * @throws OpenRDFException
      */
     List<PoddObjectLabel> getTopObjectLabels(List<InferredOWLOntologyID> artifacts) throws OpenRDFException;
     
     /**
      * 
      * @return The list of artifacts that have been published.
      * @throws OpenRDFException
      */
     List<InferredOWLOntologyID> listPublishedArtifacts() throws OpenRDFException;
     
     /**
      * 
      * @return The list of artifacts that have not been published.
      * @throws OpenRDFException
      */
     List<InferredOWLOntologyID> listUnpublishedArtifacts() throws OpenRDFException;
     
     /**
      * Loads an artifact into the manager.
      * 
      * NOTE: After this method completes the Artifact may no longer be in memory in the
      * {@link PoddOWLManager}, but will be stored in the underlying Sesame {@link Repository}.
      * 
      * @param inputStream
      *            The input stream containing the RDF document for the updated artifact.
      * @param format
      *            The format for the input RDF document.
      * @return An InferredOWLOntologyID object containing the details of the artifact. If the
      *         inferencing is delayed, the object may not contain the inferred ontology IRI.
      * @throws IOException
      * @throws PoddException
      * @throws OpenRDFException
      * @throws OWLException
      */
     InferredOWLOntologyID loadArtifact(InputStream inputStream, RDFFormat format) throws OpenRDFException,
         PoddException, IOException, OWLException;
     
     /**
      * Sets the given OWLOntologyID to be published.
      * 
      * NOTE: After publication PODD Artifacts cannot be modified. New versions must be created as
      * new PODD Artifacts, which may then be published when they are ready.
      * 
      * @param ontologyId
      *            The OWL Ontology ID of the PODD Artifact that needs to be published.
      * @return The full inferred OWL Ontology ID of the published Artifact.
      * @throws PublishArtifactException
      *             If the artifact could not be published for any reason.
      * @throws OpenRDFException
      * @throws UnmanagedArtifactIRIException
      *             If this is not a managed artifact
      */
     InferredOWLOntologyID publishArtifact(InferredOWLOntologyID ontologyId) throws PublishArtifactException,
         OpenRDFException, UnmanagedArtifactIRIException;
     
     Model searchForOntologyLabels(InferredOWLOntologyID ontologyID, String searchTerm, URI[] searchTypes)
         throws OpenRDFException, ResourceException;
     
     /**
      * Sets the {@link FileReferenceManager} to use for verifying file references for PODD
      * artifacts.
      * 
      * @param fileManager
      *            The manager to use for verifying file references for PODD artifacts.
      */
     void setFileReferenceManager(FileReferenceManager fileManager);
     
     /**
      * Sets the {@link PoddFileRepositoryManager} to use for managing file repository
      * configurations.
      * 
      * @param fileRepositoryManager
      *            The manager to use for managing file repository configurations.
      */
     void setFileRepositoryManager(PoddFileRepositoryManager fileRepositoryManager);
     
     /**
      * Sets the {@link PoddOWLManager} instance to use when loading and dealing with Artifacts in
      * memory. This manager may not be used for some queries where SPARQL queries on the underlying
      * Repository can more efficiently complete the operation.
      * 
      * NOTE: Artifacts are not necessarily cached in memory, so no manual cleanup is needed if
      * calling methods from the PoddArtifactManager interface.
      * 
      * @param owlManager
      *            The manager for interactions with OWLAPI.
      */
     void setOwlManager(PoddOWLManager owlManager);
     
     /**
      * Sets the {@link PoddPurlManager} instance to use when processing temporary references into
      * PURLs.
      * 
      * @param purlManager
      *            The manager to use for processing PURLs.
      */
     void setPurlManager(PoddPurlManager purlManager);
     
     /**
      * Sets the {@link PoddRepositoryManager} to use for managing OpenRDF Sesame Repositories used
      * to access and update data for PODD.
      * 
      * @param repositoryManager
      *            The manager to use for managing repositories.
      */
     void setRepositoryManager(PoddRepositoryManager repositoryManager);
     
     /**
      * Sets the {@link PoddSchemaManager} to use for managing the schemas used to validate artifacts
      * for PODD.
      * 
      * @param schemaManager
      *            The manager to use for managing schemas.
      */
     void setSchemaManager(PoddSchemaManager schemaManager);
     
     /**
      * Sets the {@link PoddSesameManager} to use for managing queries to repositories.
      * 
      * @param sesameManager
      *            The manager to use for managing queries to repositories.
      */
     void setSesameManager(PoddSesameManager sesameManager);
     
     /**
      * Updates a managed artifact based on incoming modified RDF statements.
      * 
      * It is sufficient for the input stream to contain only the affected statements. If the update
      * is only addition of new statements, <i>isReplace</i> option can be set to false. If the
      * update involves modifying existing statements (e.g. moving an object or deleting a link
      * between objects) <i>isReplace</i> should be set to true, and all statements making up the
      * modified objects should be included.
      * 
      * @param artifactUri
      *            The URI of the artifact to be updated. This should be an already managed artifact.
      * @param versionUri
      *            The Version URI of the artifact to be updated.
      * @param objectUris
      *            A collection of object URIs to be merged in. If updatePolicy is
      *            {@link UpdatePolicy#REPLACE_EXISTING}, only these object URIs will be updated.
      *            This allows for the new object to be linked to its parent within this update
      *            operation.
      * @param inputStream
      *            The RDF statements that need to be updated. It is not necessary to send the
      *            complete artifact here, only sending the affected statements is sufficient.
      * @param format
      *            Format of the incoming RDF data content
      * @param updatePolicy
      *            Indicates whether the incoming statements should replace existing statements about
      *            the same subjects or whether they should be merged with the existing ones.
      * @param danglingObjectPolicy
      *            The policy for handling any internal PODD objects that become disconnected from
      *            the Top Object as a result of the update. FORCE_CLEAN policy silently deletes any
      *            while REPORT policy throws a DisconnectedObjectException to inform of any
      *            disconnected objects.
      * @param fileReferencePolicy
      *            Indicates whether any File References found should be verified by accessing them
      *            from their source.
      * @return
      * @throws OpenRDFException
      * @throws PoddException
      * @throws IOException
      * @throws OWLException
      */
     InferredOWLOntologyID updateArtifact(final URI artifactUri, URI versionUri, final Collection<URI> objectUris,
             final InputStream inputStream, final RDFFormat format, final UpdatePolicy updatePolicy,
             final DanglingObjectPolicy danglingObjectPolicy, final FileReferenceVerificationPolicy fileReferencePolicy)
         throws OpenRDFException, PoddException, IOException, OWLException;
     
     /**
      * Updates the importing of the given schema ontology in the given PODD Artifact.
      * 
      * The Schema Ontology may have been imported as a different version, and these older versions
      * must be identified during this process to import the given version of the schema ontology in
      * place of the old version.
      * 
      * If the Schema Ontology was not previously imported by this PODD Artifact, then an import
      * statement will be added to indicate that the PODD Artifact imports the given version of the
      * Podd Schema Ontology.
      * 
      * @param artifactId
      *            The Ontology ID for the PODD Artifact which needs to have its Schema Ontology
      *            imports modified.
      * @param oldSchemaOntologyIds
      *            The Ontology IDs for the Schema Ontologies which need to be removed in the imports
      *            for the PODD Artifact.
      * @param newSchemaOntologyIds
      *            The Ontology IDs for the Schema Ontologies which need to be added or modified in
      *            the imports for the PODD Artifact.
      * @return The updated Ontology ID for the PODD Artifact after the schemas were updated.
      */
     InferredOWLOntologyID updateSchemaImports(InferredOWLOntologyID artifactId,
             Set<OWLOntologyID> oldSchemaOntologyIds, Set<OWLOntologyID> newSchemaOntologyIds);
     
 }
