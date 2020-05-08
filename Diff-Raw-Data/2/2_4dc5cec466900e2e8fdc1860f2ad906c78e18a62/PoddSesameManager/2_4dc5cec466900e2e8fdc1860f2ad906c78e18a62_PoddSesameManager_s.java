 /**
  * 
  */
 package com.github.podd.api;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.repository.RepositoryConnection;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddObjectLabel;
 
 /**
  * Manages interactions with Sesame Repositories for PODD.
  * 
  * @author kutila
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public interface PoddSesameManager
 {
     
     /**
      * Deletes the given ontologies, including removing and rearranging their links in the ontology
      * management graph as necessary.
      * 
      * @param requestedArtifactIds
      *            A collection of InferredOWLOntologyID objects containing the ontologies to be
      *            deleted, including the inferred ontology IRIs.
      * @param repositoryConnection
      *            The connection to the repository to use.
      * @param ontologyManagementGraph
      *            The URI of the context in the repository containing the management information for
      *            the ontologies.
      * @throws OpenRDFException
      */
     void deleteOntologies(Collection<InferredOWLOntologyID> requestedArtifactIds,
             RepositoryConnection repositoryConnection, URI ontologyManagementGraph) throws OpenRDFException;
     
     /**
      * Get all versions of the ontology with the given IRI that are managed in the given context in
      * the given repository connection.
      * <p>
      * If there are multiple versions available, then the most current version must be first in the
      * list.
      * 
      * @param ontologyIRI
      *            The Ontology IRI identifying the ontology for which versions must be accessed.
      * @param connection
      *            The repository connection to use when querying for ontology versions.
      * @param ontologyManagementGraph
      *            The URI identifying the context in which the ontologies are managed.
      * @throws OpenRDFException
      */
     List<InferredOWLOntologyID> getAllOntologyVersions(IRI ontologyIRI, RepositoryConnection connection,
             URI ontologyManagementGraph) throws OpenRDFException;
     
     /**
      * Returns current version details of an artifact ontology which has the given IRI as the
      * Ontology IRI or Version IRI.
      * 
      * @param ontologyIRI
      *            The IRI of the ontology to get current version info.
      * @param repositoryConnection
      * @param managementGraph
      *            The context of the Artifact Management Graph
      * @return An InferredOWLOntologyID containing details of the current managed version of the
      *         ontology.
      * @throws OpenRDFException
      * @throws UnmanagedArtifactIRIException
      *             If the given IRI does not refer to a managed artifact ontology
      * 
      * @since 04/01/2013
      */
     InferredOWLOntologyID getCurrentArtifactVersion(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
         UnmanagedArtifactIRIException;
     
     /**
      * Returns current version details of an ontology which has the given IRI as the Ontology IRI or
      * Version IRI.
      * 
      * @param ontologyIRI
      *            The IRI of the ontology to get current version info.
      * @param repositoryConnection
      * @param managementGraph
      *            The context of the Schema Management Graph
      * @return An InferredOWLOntologyID containing details of the current managed version of the
      *         ontology.
      * @throws OpenRDFException
      * @throws UnmanagedSchemaIRIException
      *             If the given IRI does not refer to a managed schema ontology
      * 
      * @since 18/12/2012
      */
     InferredOWLOntologyID getCurrentSchemaVersion(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
         UnmanagedSchemaIRIException;
     
     /**
      * Retrieves the ontology IRIs for all import statements found in the given Repository
      * Connection.
      * 
      * @param ontologyID
      * @param repositoryConnection
      * @return A Set containing ontology IRIs for all import statements.
      * @throws OpenRDFException
      */
     Set<IRI> getDirectImports(final InferredOWLOntologyID ontologyID, final RepositoryConnection repositoryConnection)
         throws OpenRDFException;
     
     /**
      * Returns a collection of ontologies managed in the given graph, optionally only returning the
      * current version.
      * 
      * @param onlyCurrentVersions
      *            If true, only the current version for each of the managed ontologies are returned.
      * @param ontologyManagementGraph
      *            The URI of the context in the repository containing the management information for
      *            the ontologies.
      * @return The collection of ontologies represented in the given management graph.
      */
     Collection<InferredOWLOntologyID> getOntologies(boolean onlyCurrentVersions,
             RepositoryConnection repositoryConnection, URI ontologyManagementGraph) throws OpenRDFException;
     
     /**
      * Retrieves from the given Repository Connection, an Ontology IRI which identifies an artifact.
      * 
      * @param repositoryConnection
      * @param context
      * @return The IRI of the ontology, or null if the Repository does not contain statements
      *         representing an ontology.
      * @throws OpenRDFException
      */
     IRI getOntologyIRI(final RepositoryConnection repositoryConnection, final URI context) throws OpenRDFException;
     
     /**
      * If the given IRI represents a version IRI of a schema ontology, the Ontology ID for this
     * schema version is returned. If the given IRI represents an ontology IR of a schema ontology,
      * the Ontology ID for the most current version of this schema ontology is returned.
      * 
      * 
      * @param schemaVersionIRI
      *            The IRI of the ontology to get current version info.
      * @param conn
      * @param schemaManagementGraph
      *            The context of the Schema Management Graph
      * @return An InferredOWLOntologyID containing details of the ontology.
      * @throws OpenRDFException
      * @throws UnmanagedSchemaIRIException
      *             If the given IRI does not refer to a managed schema ontology
      * 
      * @since 04/03/2013
      */
     InferredOWLOntologyID getSchemaVersion(IRI schemaVersionIRI, RepositoryConnection conn, URI schemaManagementGraph)
         throws OpenRDFException, UnmanagedSchemaIRIException;
     
     /**
      * Returns true if the combination of the Ontology IRI and the Version IRI in the given
      * ontologyID were previously published.
      * 
      * @param ontologyID
      * @param repositoryConnection
      * @return
      * @throws OpenRDFException
      */
     boolean isPublished(OWLOntologyID ontologyID, RepositoryConnection repositoryConnection, final URI context)
         throws OpenRDFException;
     
     /**
      * Sets the given Ontology IRI to be published. This restricts the ability to publish the
      * ontology again.
      * 
      * @param ontologyID
      *            The {@link InferredOWLOntologyID} identifying the ontology that needs to be
      *            published
      * @param repositoryConnection
      * @param artifactManagementGraph
      * @throws OpenRDFException
      * @throws UnmanagedArtifactIRIException
      *             If this is not a managed ontology
      */
     void setPublished(final InferredOWLOntologyID ontologyID, final RepositoryConnection repositoryConnection,
             final URI artifactManagementGraph) throws OpenRDFException, UnmanagedArtifactIRIException;
     
     /**
      * This method adds information to the Schema Ontology management graph, and updates the links
      * for the current version for both the ontology and the inferred ontology.
      * 
      * @param nextOntologyID
      *            The ontology ID that contains the information about the ontology, including the
      *            inferred ontology IRI.
      * @param updateCurrent
      *            If true, will update the current version if it exists. If false it will only add
      *            the current version if it does not exist. Set this to false when only inferred
      *            ontology information needs to be added. This will never remove statements related
      *            to previous versions of schema ontologies.
      * @param repositoryConnection
      * @param context
      * @throws OpenRDFException
      */
     void updateCurrentManagedSchemaOntologyVersion(InferredOWLOntologyID nextOntologyID, boolean updateCurrent,
             RepositoryConnection repositoryConnection, URI context) throws OpenRDFException;
     
     /**
      * This method adds information to the PODD artifact management graph, and updates the links for
      * the current version for both the ontology and the inferred ontology.
      * 
      * @param nextOntologyID
      *            The ontology ID that contains the information about the ontology, including the
      *            inferred ontology IRI.
      * @param updateCurrentAndRemovePrevious
      *            If true, will update the current version if it exists, and remove all statements
      *            relating to previous versions. If false it will only add the current version if it
      *            does not exist.
      * @param repositoryConnection
      * @param context
      * @throws OpenRDFException
      */
     void updateManagedPoddArtifactVersion(InferredOWLOntologyID nextOntologyID, boolean updateCurrentAndRemovePrevious,
             RepositoryConnection repositoryConnection, URI context) throws OpenRDFException;
     
     List<URI> getObjectTypes(InferredOWLOntologyID ontologyID, URI objectUri, RepositoryConnection repositoryConnection)
         throws OpenRDFException;
     
     URI getTopObjectIRI(InferredOWLOntologyID ontologyIRI, RepositoryConnection repositoryConnection)
         throws OpenRDFException;
     
     List<URI> getTopObjects(InferredOWLOntologyID ontologyID, RepositoryConnection repositoryConnection)
         throws OpenRDFException;
     
     List<URI> getAllValidMembers(InferredOWLOntologyID artifactID, URI conceptUri,
             RepositoryConnection repositoryConnection) throws OpenRDFException;
     
     Model getCardinality(InferredOWLOntologyID artifactID, URI objectUri, URI propertyUri,
             RepositoryConnection repositoryConnection) throws OpenRDFException;
     
     Model getObjectDetailsForDisplay(InferredOWLOntologyID artifactID, URI objectUri,
             RepositoryConnection repositoryConnection) throws OpenRDFException;
     
     Model getObjectDetailsForEdit(InferredOWLOntologyID artifactID, URI objectUri,
             RepositoryConnection repositoryConnection) throws OpenRDFException;
     
     List<URI> getWeightedProperties(InferredOWLOntologyID artifactID, URI objectUri,
             RepositoryConnection repositoryConnection) throws OpenRDFException;
     
     Set<IRI> getDirectImports(RepositoryConnection repositoryConnection, URI... contexts) throws OpenRDFException;
 
     PoddObjectLabel getObjectLabel(InferredOWLOntologyID ontologyID, URI objectUri,
             RepositoryConnection repositoryConnection) throws OpenRDFException;
 
 }
