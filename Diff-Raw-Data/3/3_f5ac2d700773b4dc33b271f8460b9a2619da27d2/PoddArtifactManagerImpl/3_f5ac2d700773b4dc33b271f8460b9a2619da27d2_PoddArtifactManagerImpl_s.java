 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.impl;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Literal;
 import org.openrdf.model.Model;
 import org.openrdf.model.Namespace;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.util.Namespaces;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.profiles.OWLProfileReport;
 import org.semanticweb.owlapi.profiles.OWLProfileViolation;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.DanglingObjectPolicy;
 import com.github.podd.api.DataReferenceVerificationPolicy;
 import com.github.podd.api.MetadataPolicy;
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.api.UpdatePolicy;
 import com.github.podd.api.file.DataReference;
 import com.github.podd.api.file.DataReferenceManager;
 import com.github.podd.api.file.PoddDataRepositoryManager;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlReference;
 import com.github.podd.exception.ArtifactModifyException;
 import com.github.podd.exception.DeleteArtifactException;
 import com.github.podd.exception.DisconnectedObjectException;
 import com.github.podd.exception.DuplicateArtifactIRIException;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.FileReferenceVerificationFailureException;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.PoddRuntimeException;
 import com.github.podd.exception.PublishArtifactException;
 import com.github.podd.exception.PublishedArtifactModifyException;
 import com.github.podd.exception.PurlProcessorNotHandledException;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedArtifactVersionException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddObjectLabel;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.RdfUtility;
 
 /**
  * Implementation of the PODD Artifact Manager API, to manage the lifecycle for PODD Artifacts.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PoddArtifactManagerImpl implements PoddArtifactManager
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     private DataReferenceManager dataReferenceManager;
     private PoddDataRepositoryManager dataRepositoryManager;
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
     }
     
     @Override
     public InferredOWLOntologyID attachFileReference(final InferredOWLOntologyID artifactId, final URI objectUri,
             final DataReference dataReference) throws OpenRDFException, PoddException
     {
         throw new RuntimeException("TODO: Implement attachFileReference");
     }
     
     @Override
     public InferredOWLOntologyID attachDataReferences(final URI artifactUri, final URI versionUri,
             final InputStream inputStream, final RDFFormat format,
             final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws OpenRDFException,
         IOException, OWLException, PoddException
     {
         final Model model = Rio.parse(inputStream, "", format);
         
         model.removeAll(model.filter(null, PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null));
         
         final Set<Resource> fileReferences =
                 model.filter(null, RDF.TYPE, PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE).subjects();
         final Collection<URI> fileReferenceObjects = new ArrayList<URI>(fileReferences.size());
         for(final Resource nextFileReference : fileReferences)
         {
             if(nextFileReference instanceof URI)
             {
                 fileReferenceObjects.add((URI)nextFileReference);
             }
             else
             {
                 this.log.warn("Will not be updating file reference for blank node reference, will instead be creating a new file reference for it.");
             }
         }
         
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8192);
         
         Rio.write(model, output, RDFFormat.RDFJSON);
         
         final Model resultModel =
                 this.updateArtifact(artifactUri, versionUri, fileReferenceObjects,
                         new ByteArrayInputStream(output.toByteArray()), RDFFormat.RDFJSON,
                         UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.REPORT, dataReferenceVerificationPolicy);
         return OntologyUtils.modelToOntologyIDs(resultModel).get(0);
     }
     
     @Override
     public boolean deleteArtifact(final InferredOWLOntologyID artifactId) throws PoddException
     {
         if(artifactId.getOntologyIRI() == null)
         {
             throw new PoddRuntimeException("Ontology IRI cannot be null");
         }
         
         RepositoryConnection connection = null;
         
         try
         {
             if(this.isPublished(artifactId))
             {
                 throw new DeleteArtifactException("Published Artifacts cannot be deleted", artifactId);
             }
             
             connection = this.getRepositoryManager().getRepository().getConnection();
             connection.begin();
             
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
             
             this.getSesameManager().deleteOntologies(requestedArtifactIds, connection,
                     this.getRepositoryManager().getArtifactManagementGraph());
             connection.commit();
 
             // - ensure deleted ontologies are removed from the OWLOntologyManager's cache
             for (InferredOWLOntologyID deletedOntologyId : requestedArtifactIds)
             {
                 this.getOWLManager().removeCache(deletedOntologyId.getBaseOWLOntologyID());
                 this.getOWLManager().removeCache(deletedOntologyId.getInferredOWLOntologyID());
             }
             
             return !requestedArtifactIds.isEmpty();
         }
         catch(final OpenRDFException | OWLException e)
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
                 this.log.error("Found error rolling back repository connection", e1);
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
     public InferredOWLOntologyID deleteObject(final String artifactUri, final String versionUri,
             final String objectUri, final boolean cascade) throws PoddException, OpenRDFException, IOException,
         OWLException
     {
         // check if the specified artifact URI refers to a managed artifact
         InferredOWLOntologyID artifactID = null;
         try
         {
             artifactID = this.getArtifact(IRI.create(artifactUri));
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             this.log.error("This artifact is unmanaged. [{}]", artifactUri);
             throw e;
         }
         
         if(this.isPublished(artifactID))
         {
             throw new PublishedArtifactModifyException("Attempting to modify a Published Artifact", artifactID);
         }
         
         this.log.debug("deleteObject ({}) from artifact {} with cascade={}", objectUri, artifactUri, cascade);
         
         final URI objectToDelete = PoddRdfConstants.VF.createURI(objectUri);
         
         final Collection<URI> objectsToUpdate = new ArrayList<URI>();
         objectsToUpdate.add(objectToDelete);
         final Model fragments = new LinkedHashModel();
         final Model artifactModel = this.exportArtifact(artifactID, false);
         
         // - find the objectToDelete's parent and remove parent-child link
         final Model parentDetails = this.getParentDetails(artifactID, objectToDelete);
         if(parentDetails.subjects().size() != 1)
         {
             this.log.error("Object {} cannot be deleted. (No parent)", objectUri, artifactUri);
             throw new ArtifactModifyException("Object cannot be deleted. (No parent)", artifactID, objectToDelete);
         }
         final Resource parent = parentDetails.subjects().iterator().next();
         fragments.addAll(artifactModel.filter(parent, null, null));
         fragments.remove(parent, null, objectToDelete);
         objectsToUpdate.add((URI)parent);
         
         // - remove any refersToLinks
         final Model referenceLinks = this.getReferenceLinks(artifactID, objectToDelete);
         final Set<Resource> referrers = referenceLinks.subjects();
         for(final Resource referrer : referrers)
         {
             final Model referrerStatements = artifactModel.filter(referrer, null, null);
             referrerStatements.remove(referrer, null, objectToDelete);
             
             fragments.addAll(referrerStatements);
             objectsToUpdate.add((URI)referrer);
         }
         
         DanglingObjectPolicy danglingObjectPolicy = DanglingObjectPolicy.REPORT;
         if(cascade)
         {
             danglingObjectPolicy = DanglingObjectPolicy.FORCE_CLEAN;
         }
         
         this.updateArtifact(artifactID.getOntologyIRI().toOpenRDFURI(), artifactID.getVersionIRI().toOpenRDFURI(),
                 objectsToUpdate, fragments, UpdatePolicy.REPLACE_EXISTING, danglingObjectPolicy,
                 DataReferenceVerificationPolicy.DO_NOT_VERIFY);
         
         return this.getArtifact(artifactID.getOntologyIRI());
     }
     
     @Override
     public Model exportArtifact(final InferredOWLOntologyID ontologyId, final boolean includeInferred)
         throws OpenRDFException, PoddException, IOException
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
             
             final RepositoryResult<Statement> statements =
                     connection.getStatements(null, null, null, includeInferred, contexts.toArray(new Resource[] {}));
             final Model model = new LinkedHashModel(Iterations.asList(statements));
             final RepositoryResult<Namespace> namespaces = connection.getNamespaces();
             for(final Namespace nextNs : Iterations.asSet(namespaces))
             {
                 model.setNamespace(nextNs);
             }
             return model;
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
     public void exportArtifact(final InferredOWLOntologyID ontologyId, final OutputStream outputStream,
             final RDFFormat format, final boolean includeInferred) throws OpenRDFException, PoddException, IOException
     {
         final Model model = this.exportArtifact(ontologyId, includeInferred);
         Rio.write(model, outputStream, format);
     }
     
     @Override
     public void exportObjectMetadata(final URI objectType, final OutputStream outputStream, final RDFFormat format,
             final boolean includeDoNotDisplayProperties, final MetadataPolicy containsPropertyPolicy,
             final InferredOWLOntologyID artifactID) throws OpenRDFException, PoddException, IOException
     {
         RepositoryConnection connection = null;
         
         try
         {
             connection = this.getRepositoryManager().getRepository().getConnection();
             
             final URI[] contexts =
                     this.sesameManager.versionAndSchemaContexts(artifactID, connection,
                             this.repositoryManager.getSchemaManagementGraph());
             
             Model model;
             if(containsPropertyPolicy == MetadataPolicy.ONLY_CONTAINS)
             {
                 model = this.sesameManager.getObjectTypeContainsMetadata(objectType, connection, contexts);
             }
             else
             {
                 model =
                         this.sesameManager.getObjectTypeMetadata(objectType, includeDoNotDisplayProperties,
                                 containsPropertyPolicy, connection, contexts);
             }
             Rio.write(model, outputStream, format);
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
     public Model fillMissingData(final InferredOWLOntologyID ontologyID, final Model inputModel)
         throws OpenRDFException
     {
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             final URI[] contexts =
                     this.getSesameManager().versionAndSchemaContexts(ontologyID, conn,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             return this.getSesameManager().fillMissingLabels(inputModel, conn, contexts);
         }
         catch(final OpenRDFException e)
         {
             try
             {
                 if(conn != null && conn.isActive())
                 {
                     conn.rollback();
                 }
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Found error rolling back repository connection", e1);
             }
             
             throw e;
         }
         finally
         {
             try
             {
                 if(conn != null && conn.isOpen())
                 {
                     conn.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 throw e;
             }
         }
     }
     
     @Override
     public InferredOWLOntologyID getArtifact(final IRI artifactIRI) throws UnmanagedArtifactIRIException
     {
         return this.getArtifact(artifactIRI, null);
     }
     
     @Override
     public InferredOWLOntologyID getArtifact(final IRI artifactIRI, final IRI versionIRI)
         throws UnmanagedArtifactIRIException
     {
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.getRepositoryManager().getRepository().getConnection();
             
             InferredOWLOntologyID result = null;
             
             if(versionIRI != null)
             {
                 result =
                         this.getSesameManager().getOntologyVersion(versionIRI, repositoryConnection,
                                 this.getRepositoryManager().getArtifactManagementGraph());
             }
             
             if(result == null)
             {
                 result =
                         this.getSesameManager().getCurrentArtifactVersion(artifactIRI, repositoryConnection,
                                 this.getRepositoryManager().getArtifactManagementGraph());
             }
             
             if(result != null)
             {
                 // If the result that was returned contained a different artifact IRI then throw an
                 // exception early instead of returning inconsistent results
                 if(!result.getOntologyIRI().equals(artifactIRI) && !result.getVersionIRI().equals(artifactIRI))
                 {
                     throw new UnmanagedArtifactIRIException(artifactIRI,
                             "Artifact IRI and Version IRI combination did not match");
                 }
             }
             
             return result;
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
      * Wraps PoddSesameManager.getChildObjects()
      * 
      * @see com.github.podd.api.PoddArtifactManager#getChildObjects()
      */
     @Override
     public Set<URI> getChildObjects(final InferredOWLOntologyID ontologyID, final URI objectUri)
         throws OpenRDFException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             
             final URI[] contexts =
                     this.getSesameManager().versionAndSchemaContexts(ontologyID, conn,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             return this.getSesameManager().getChildObjects(objectUri, conn, contexts);
         }
         finally
         {
             conn.close();
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getFileReferenceManager()
      */
     @Override
     public DataReferenceManager getFileReferenceManager()
     {
         return this.dataReferenceManager;
     }
     
     @Override
     public Set<DataReference> getFileReferences(final InferredOWLOntologyID artifactId)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public Set<DataReference> getFileReferences(final InferredOWLOntologyID artifactId, final String alias)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public Set<DataReference> getFileReferences(final InferredOWLOntologyID artifactId, final URI objectUri)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getFileRepositoryManager()
      */
     @Override
     public PoddDataRepositoryManager getFileRepositoryManager()
     {
         return this.dataRepositoryManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * Wraps PoddSesameManager.getObjectDetailsForDisplay()
      * 
      * @see com.github.podd.api.PoddArtifactManager#getObjectDetailsForDisplay()
      */
     @Override
     public Model getObjectDetailsForDisplay(final InferredOWLOntologyID ontologyID, final URI objectUri)
         throws OpenRDFException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             return this.getSesameManager().getObjectDetailsForDisplay(ontologyID, objectUri, conn);
         }
         finally
         {
             conn.close();
         }
     }
     
     @Override
     public PoddObjectLabel getObjectLabel(final InferredOWLOntologyID ontologyID, final URI objectUri)
         throws OpenRDFException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             return this.getSesameManager().getObjectLabel(ontologyID, objectUri, conn);
         }
         finally
         {
             conn.close();
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getObjectTypes(com.github.podd.utils.
      * InferredOWLOntologyID, org.openrdf.model.URI)
      */
     @Override
     public List<PoddObjectLabel> getObjectTypes(final InferredOWLOntologyID artifactId, final URI objectUri)
         throws OpenRDFException
     {
         final List<PoddObjectLabel> results = new ArrayList<PoddObjectLabel>();
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             
             final List<URI> typesList = this.getSesameManager().getObjectTypes(artifactId, objectUri, conn);
             for(final URI objectType : typesList)
             {
                 results.add(this.getSesameManager().getObjectLabel(artifactId, objectType, conn));
             }
         }
         finally
         {
             conn.close();
         }
         return results;
     }
     
     /*
      * (non-Javadoc)
      * 
      * Wraps PoddSesameManager.getOrderedProperties()
      * 
      * @see com.github.podd.api.PoddArtifactManager#getOrderedProperties()
      */
     @Override
     public List<URI> getOrderedProperties(final InferredOWLOntologyID ontologyID, final URI objectUri,
             final boolean excludeContainsProperties) throws OpenRDFException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             
             final URI[] contexts =
                     this.getSesameManager().versionAndSchemaContexts(ontologyID, conn,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             return this.getSesameManager().getWeightedProperties(objectUri, excludeContainsProperties, conn, contexts);
         }
         finally
         {
             conn.close();
         }
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
      * @see com.github.podd.api.PoddArtifactManager#getParentDetails(com.github.podd.utils.
      * InferredOWLOntologyID, org.openrdf.model.URI)
      */
     @Override
     public Model getParentDetails(final InferredOWLOntologyID ontologyID, final URI objectUri) throws OpenRDFException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             final URI[] contexts =
                     this.getSesameManager().versionAndSchemaContexts(ontologyID, conn,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             return this.getSesameManager().getParentDetails(objectUri, conn, contexts);
         }
         catch(final OpenRDFException e)
         {
             try
             {
                 if(conn != null && conn.isActive())
                 {
                     conn.rollback();
                 }
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Found error rolling back repository connection", e1);
             }
             
             throw e;
         }
         finally
         {
             try
             {
                 if(conn != null && conn.isOpen())
                 {
                     conn.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 throw e;
             }
         }
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
     
     public Model getReferenceLinks(final InferredOWLOntologyID ontologyID, final URI objectUri) throws OpenRDFException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             final URI[] contexts =
                     this.getSesameManager().versionAndSchemaContexts(ontologyID, conn,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             return this.getSesameManager().getReferringObjectDetails(objectUri, conn, contexts);
         }
         finally
         {
             try
             {
                 if(conn != null && conn.isOpen())
                 {
                     conn.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 throw e;
             }
         }
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
     
     @Override
     public List<PoddObjectLabel> getTopObjectLabels(final List<InferredOWLOntologyID> artifacts)
         throws OpenRDFException
     {
         final List<PoddObjectLabel> results = new ArrayList<PoddObjectLabel>();
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             for(final InferredOWLOntologyID artifactId : artifacts)
             {
                 final URI objectIRI = this.getSesameManager().getTopObjectIRI(artifactId, conn);
                 results.add(this.getSesameManager().getObjectLabel(artifactId, objectIRI, conn));
             }
         }
         finally
         {
             conn.close();
         }
         return results;
     }
     
     /**
      * Helper method to cache schema ontologies in memory before loading statements into OWLAPI
      */
     private void handleCacheSchemasInMemory(final RepositoryConnection permanentRepositoryConnection,
             final RepositoryConnection tempRepositoryConnection, final URI tempContext) throws OpenRDFException,
         OWLException, IOException, PoddException
     {
         final Set<IRI> importedSchemas =
                 this.getSesameManager().getDirectImports(tempRepositoryConnection, tempContext);
         
         for(final IRI importedSchemaIRI : importedSchemas)
         {
             final InferredOWLOntologyID ontologyVersion =
                     this.getSesameManager().getSchemaVersion(importedSchemaIRI, permanentRepositoryConnection,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             this.getOWLManager().cacheSchemaOntology(ontologyVersion, permanentRepositoryConnection,
                     this.getRepositoryManager().getSchemaManagementGraph());
         }
     }
     
     /**
      * Checks for dangling objects that are not linked to the artifact and deletes them if
      * <i>force</i> is true.
      * 
      * @param artifactID
      * @param repositoryConnection
      * @param context
      * @param force
      *            If true, deletes any dangling objects. If false, throws a
      *            DisconnectedObjectException if any dangling objects are found.
      * @throws RepositoryException
      * @throws DisconnectedObjectException
      */
     private void handleDanglingObjects(final IRI artifactID, final RepositoryConnection repositoryConnection,
             final URI context, final DanglingObjectPolicy policy) throws RepositoryException,
         DisconnectedObjectException
     {
         final Set<URI> danglingObjects =
                 RdfUtility.findDisconnectedNodes(artifactID.toOpenRDFURI(), repositoryConnection, context);
         
         if(!danglingObjects.isEmpty())
         {
             if(policy.equals(DanglingObjectPolicy.REPORT))
             {
                 this.log.error("Found {} dangling object(s) (reporting). \n {}", danglingObjects.size(),
                         danglingObjects);
                 throw new DisconnectedObjectException(danglingObjects, "Update leads to disconnected PODD objects");
             }
             else if(policy.equals(DanglingObjectPolicy.FORCE_CLEAN))
             {
                 this.log.info("Found {} dangling object(s) (force cleaning). \n {}", danglingObjects.size(),
                         danglingObjects);
                 for(final URI danglingObject : danglingObjects)
                 {
                     repositoryConnection.remove(danglingObject, null, null, context);
                     repositoryConnection.remove(null, null, (Value)danglingObject, context);
                 }
             }
         }
     }
     
     /**
      * Helper method to handle File References in a newly loaded/updated set of statements.
      * 
      * TODO: Optionally remove invalid file references or mark them as invalid using RDF
      * statements/OWL Classes
      * 
      * @param repositoryConnection
      * @param context
      * @param policy
      *            If true, verifies that DataReference objects are accessible from their respective
      *            remote File Repositories
      * 
      * @throws OpenRDFException
      * @throws PoddException
      */
     private void handleFileReferences(final RepositoryConnection repositoryConnection,
             final DataReferenceVerificationPolicy policy, final URI... contexts) throws OpenRDFException, PoddException
     {
         if(this.getFileReferenceManager() == null)
         {
             return;
         }
         
         if(DataReferenceVerificationPolicy.VERIFY.equals(policy))
         {
             final Set<DataReference> fileReferenceResults =
                     this.getFileReferenceManager().extractDataReferences(repositoryConnection, contexts);
             
             this.log.debug("Handling File reference validation");
             
             try
             {
                 this.dataRepositoryManager.verifyDataReferences(fileReferenceResults);
             }
             catch(final FileReferenceVerificationFailureException e)
             {
                 this.log.warn("From " + fileReferenceResults.size() + " file references, "
                         + e.getValidationFailures().size() + " failed validation.");
                 throw e;
             }
         }
     }
     
     /**
      * Helper method to handle File References in a newly loaded/updated set of statements
      */
     private Set<PoddPurlReference> handlePurls(final RepositoryConnection repositoryConnection, final URI context)
         throws PurlProcessorNotHandledException, OpenRDFException
     {
         if(this.getPurlManager() == null)
         {
             return Collections.emptySet();
         }
         
         this.log.debug("Handling Purl generation");
         final Set<PoddPurlReference> purlResults =
                 this.getPurlManager().extractPurlReferences(repositoryConnection, context);
         
         this.getPurlManager().convertTemporaryUris(purlResults, repositoryConnection, context);
         return purlResults;
     }
     
     /**
      * Helper method to check schema ontology imports and update use of ontology IRIs to version
      * IRIs.
      */
     private void handleSchemaImports(final IRI ontologyIRI, final RepositoryConnection permanentRepositoryConnection,
             final RepositoryConnection tempRepositoryConnection, final URI tempContext) throws OpenRDFException,
         UnmanagedSchemaIRIException
     {
         final Set<IRI> importedSchemas =
                 this.getSesameManager().getDirectImports(tempRepositoryConnection, tempContext);
         for(final IRI importedSchemaIRI : importedSchemas)
         {
             final InferredOWLOntologyID schemaOntologyID =
                     this.getSesameManager().getSchemaVersion(importedSchemaIRI, permanentRepositoryConnection,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             if(!importedSchemaIRI.equals(schemaOntologyID.getVersionIRI()))
             {
                 // modify import to be a specific version of the schema
                 this.log.debug("Updating import to version <{}>", schemaOntologyID.getVersionIRI());
                 tempRepositoryConnection.remove(ontologyIRI.toOpenRDFURI(), OWL.IMPORTS,
                         importedSchemaIRI.toOpenRDFURI(), tempContext);
                 tempRepositoryConnection.add(ontologyIRI.toOpenRDFURI(), OWL.IMPORTS, schemaOntologyID.getVersionIRI()
                         .toOpenRDFURI(), tempContext);
             }
         }
     }
     
     /**
      * This helper method checks for statements with the given property and having a date-time value
      * with the year 1970 and updates their date-time with the given {@link Value}.
      * 
      * @param repositoryConnection
      * @param propertyUri
      * @param newTimestamp
      * @param context
      * @throws OpenRDFException
      */
     private void handleTimestamps(final RepositoryConnection repositoryConnection, final URI propertyUri,
             final Value newTimestamp, final URI context) throws OpenRDFException
     {
         final List<Statement> statements =
                 Iterations.asList(repositoryConnection.getStatements(null, propertyUri, null, false, context));
         
         for(final Statement s : statements)
         {
             final Value object = s.getObject();
             if(object instanceof Literal)
             {
                 final int year = ((Literal)object).calendarValue().getYear();
                 if(year == 1970)
                 {
                     repositoryConnection.remove(s, context);
                     repositoryConnection.add(s.getSubject(), s.getPredicate(), newTimestamp, context);
                 }
             }
         }
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
     public boolean isPublished(final InferredOWLOntologyID ontologyId) throws OpenRDFException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.repositoryManager.getRepository().getConnection();
             
             return this.getSesameManager().isPublished(ontologyId, conn,
                     this.getRepositoryManager().getArtifactManagementGraph());
         }
         finally
         {
             if(conn != null && conn.isOpen())
             {
                 conn.close();
             }
         }
     }
     
     private List<InferredOWLOntologyID> listArtifacts(final boolean published, final boolean unpublished)
         throws OpenRDFException
     {
         if(!published && !unpublished)
         {
             throw new IllegalArgumentException("Cannot choose to exclude both published and unpublished artifacts");
         }
         
         final List<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
         
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             final Collection<InferredOWLOntologyID> ontologies =
                     this.getSesameManager().getOntologies(true, conn,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             for(final InferredOWLOntologyID nextOntology : ontologies)
             {
                 final boolean isPublished =
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
     
     @Override
     public List<InferredOWLOntologyID> listPublishedArtifacts() throws OpenRDFException
     {
         return this.listArtifacts(true, false);
     }
     
     @Override
     public List<InferredOWLOntologyID> listUnpublishedArtifacts() throws OpenRDFException
     {
         return this.listArtifacts(false, true);
     }
     
     @Override
     public InferredOWLOntologyID loadArtifact(final InputStream inputStream, RDFFormat format) throws OpenRDFException,
         PoddException, IOException, OWLException
     {
         return loadArtifact(inputStream, format, DanglingObjectPolicy.REPORT,
                 DataReferenceVerificationPolicy.DO_NOT_VERIFY);
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream,
      * org.openrdf.rio.RDFFormat)
      */
     @Override
     public InferredOWLOntologyID loadArtifact(final InputStream inputStream, RDFFormat format,
             DanglingObjectPolicy danglingObjectPolicy, DataReferenceVerificationPolicy dataReferenceVerificationPolicy)
         throws OpenRDFException, PoddException, IOException, OWLException
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
             
             // Remove any assertions that the user has made about publication status, as this
             // information is a privileged operation that must be done through the designated API
             // method
             temporaryRepositoryConnection.remove((Resource)null, PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS,
                     (Resource)null, randomContext);
             
             this.handlePurls(temporaryRepositoryConnection, randomContext);
             
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
                     if (currentManagedArtifactID != null)
                     {
                         throw new DuplicateArtifactIRIException(ontologyIRI, "This artifact is already managed");
                     }
                 }
                 catch(final UnmanagedArtifactIRIException e)
                 {
                     // ignore. indicates a new artifact is being uploaded
                     this.log.info("This is an unmanaged artifact IRI {}", ontologyIRI);
                 }
                 
                 IRI newVersionIRI = null;
                 if(currentManagedArtifactID == null || currentManagedArtifactID.getVersionIRI() == null)
                 {
                     newVersionIRI = IRI.create(ontologyIRI.toString() + ":version:1");
                 }
                 
                 // set version IRI in temporary repository
                 this.log.info("Setting version IRI to <{}>", newVersionIRI);
                 temporaryRepositoryConnection.remove(ontologyIRI.toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                         null, randomContext);
                 temporaryRepositoryConnection.add(ontologyIRI.toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                         newVersionIRI.toOpenRDFURI(), randomContext);
             }
             else
             {
                 throw new EmptyOntologyException(null, "Loaded ontology is empty");
             }
             
             // check and update statements with default timestamp values
             final Value now = PoddRdfConstants.VF.createLiteral(new Date());
             this.handleTimestamps(temporaryRepositoryConnection, PoddRdfConstants.PODD_BASE_CREATED_AT, now,
                     randomContext);
             this.handleTimestamps(temporaryRepositoryConnection, PoddRdfConstants.PODD_BASE_LAST_MODIFIED, now,
                     randomContext);
             
             this.handleDanglingObjects(ontologyIRI, temporaryRepositoryConnection, randomContext, danglingObjectPolicy);
             
             // check and ensure schema ontology imports are for version IRIs
             this.handleSchemaImports(ontologyIRI, permanentRepositoryConnection, temporaryRepositoryConnection,
                     randomContext);
             
             // ensure schema ontologies are cached in memory before loading statements into OWLAPI
             this.handleCacheSchemasInMemory(permanentRepositoryConnection, temporaryRepositoryConnection, randomContext);
             
             // TODO: This web service could be used accidentally to insert invalid file references
             inferredOWLOntologyID =
                     this.loadInferStoreArtifact(temporaryRepositoryConnection, permanentRepositoryConnection,
                             randomContext, dataReferenceVerificationPolicy);
             
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
                 this.getOWLManager().removeCache(inferredOWLOntologyID.getBaseOWLOntologyID());
                 this.getOWLManager().removeCache(inferredOWLOntologyID.getInferredOWLOntologyID());
             }
             
             try
             {
                 if(permanentRepositoryConnection != null && permanentRepositoryConnection.isOpen())
                 {
                     permanentRepositoryConnection.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 this.log.error("Found exception closing repository connection", e);
             }
             try
             {
                 if(temporaryRepositoryConnection != null && temporaryRepositoryConnection.isOpen())
                 {
                     temporaryRepositoryConnection.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 this.log.error("Found exception closing repository connection", e);
             }
             tempRepository.shutDown();
         }
     }
     
     /**
      * Helper method to load the artifact into OWLAPI from a temporary location, perform reasoning
      * and store in permanent repository.
      * 
      * @param fileReferencePolicy
      */
     private InferredOWLOntologyID loadInferStoreArtifact(final RepositoryConnection tempRepositoryConnection,
             final RepositoryConnection permanentRepositoryConnection, final URI tempContext,
             final DataReferenceVerificationPolicy fileReferencePolicy) throws OpenRDFException, OWLException,
         IOException, PoddException, OntologyNotInProfileException, InconsistentOntologyException
     {
         // load into OWLAPI
         this.log.debug("Loading podd artifact from temp repository: {}", tempContext);
         final List<Statement> statements =
                 Iterations.asList(tempRepositoryConnection.getStatements(null, null, null, true, tempContext));
         
         final RioMemoryTripleSource owlSource =
                 new RioMemoryTripleSource(statements.iterator(), Namespaces.asMap(Iterations
                         .asSet(tempRepositoryConnection.getNamespaces())));
         
         final OWLOntology nextOntology = this.getOWLManager().loadOntology(owlSource);
         
         // Check the OWLAPI OWLOntology against an OWLProfile to make sure it is in profile
         final OWLProfileReport profileReport = this.getOWLManager().getReasonerProfile().checkOntology(nextOntology);
         if(!profileReport.isInProfile())
         {
             this.getOWLManager().removeCache(nextOntology.getOntologyID());
             
             if(this.log.isInfoEnabled())
             {
                 for(final OWLProfileViolation violation : profileReport.getViolations())
                 {
                     this.log.info(violation.toString());
                 }
             }
             throw new OntologyNotInProfileException(nextOntology, profileReport,
                     "Ontology is not in required OWL Profile");
         }
         
         // Use the OWLManager to create a reasoner over the ontology
         final OWLReasoner nextReasoner = this.getOWLManager().createReasoner(nextOntology);
         
         // Test that the ontology was consistent with this reasoner
         // This ensures in the case of Pellet that it is in the OWL2-DL profile
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
         // axioms to store in the database.
         // Serialise the inferred statements back to a different context in the permanent
         // repository connection.
         // The contexts to use within the permanent repository connection are all encapsulated
         // in the InferredOWLOntologyID object.
         final InferredOWLOntologyID inferredOWLOntologyID =
                 this.getOWLManager().inferStatements(nextOntology, permanentRepositoryConnection);
         
         // Check file references after inferencing to accurately identify the parent object
         this.handleFileReferences(permanentRepositoryConnection, fileReferencePolicy, inferredOWLOntologyID
                 .getVersionIRI().toOpenRDFURI(), inferredOWLOntologyID.getInferredOntologyIRI().toOpenRDFURI());
         
         this.getSesameManager().updateManagedPoddArtifactVersion(inferredOWLOntologyID, true,
                 permanentRepositoryConnection, this.getRepositoryManager().getArtifactManagementGraph());
         return inferredOWLOntologyID;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.
      * OWLOntologyID)
      */
     @Override
     public InferredOWLOntologyID publishArtifact(final InferredOWLOntologyID ontologyId) throws OpenRDFException,
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
             
             final InferredOWLOntologyID published =
                     this.getSesameManager().setPublished(true, currentVersion, repositoryConnection,
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
             try
             {
                 if(repositoryConnection != null && repositoryConnection.isOpen())
                 {
                     repositoryConnection.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 this.log.error("Found exception closing repository connection", e);
             }
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#searchForOntologyLabels(org.semanticweb.owlapi.model.
      * OWLOntologyID, java.lang.String, org.openrdf.model.URI[])
      */
     @Override
     public Model searchForOntologyLabels(final InferredOWLOntologyID ontologyID, final String searchTerm,
             final URI[] searchTypes) throws OpenRDFException
     {
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             final URI[] contexts =
                     this.getSesameManager().versionAndSchemaContexts(ontologyID, conn,
                             this.getRepositoryManager().getSchemaManagementGraph());
             return this.getSesameManager().searchOntologyLabels(searchTerm, searchTypes, 1000, 0, conn, contexts);
         }
         catch(final OpenRDFException e)
         {
             try
             {
                 if(conn != null && conn.isActive())
                 {
                     conn.rollback();
                 }
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Found error rolling back repository connection", e1);
             }
             
             throw e;
         }
         finally
         {
             try
             {
                 if(conn != null && conn.isOpen())
                 {
                     conn.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 throw e;
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
     public void setDataReferenceManager(final DataReferenceManager fileManager)
     {
         this.dataReferenceManager = fileManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setFileRepositoryManager(com.github.podd.api.file
      * .PoddFileRepositoryManager)
      */
     @Override
     public void setDataRepositoryManager(final PoddDataRepositoryManager dataRepositoryManager)
     {
         this.dataRepositoryManager = dataRepositoryManager;
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
      * @see com.github.podd.api.PoddArtifactManager#updateArtifact(org.openrdf.model.URI,
      * java.io.InputStream, org.openrdf.rio.RDFFormat)
      */
     @Override
     public Model updateArtifact(final URI artifactUri, final URI versionUri, final Collection<URI> objectUris,
             final InputStream inputStream, RDFFormat format, final UpdatePolicy updatePolicy,
             final DanglingObjectPolicy danglingObjectAction, final DataReferenceVerificationPolicy fileReferenceAction)
         throws OpenRDFException, IOException, OWLException, PoddException
     {
         if(inputStream == null)
         {
             throw new NullPointerException("Input stream must not be null");
         }
         
         if(format == null)
         {
             format = RDFFormat.RDFXML;
         }
         
         final Model model = Rio.parse(inputStream, "", format);
         
         return this.updateArtifact(artifactUri, versionUri, objectUris, model, updatePolicy, danglingObjectAction,
                 fileReferenceAction);
     }
     
     /**
      * Internal updateArtifact() method which takes a {@link Model} containing the modified triples
      * instead of an InputStream.
      */
     protected Model updateArtifact(final URI artifactUri, final URI versionUri, final Collection<URI> objectUris,
             final Model model, final UpdatePolicy updatePolicy, final DanglingObjectPolicy danglingObjectAction,
             final DataReferenceVerificationPolicy fileReferenceAction) throws OpenRDFException, IOException,
         OWLException, PoddException
     {
         if(model == null)
         {
             throw new NullPointerException("Input Model must not be null");
         }
         
         // check if the specified artifact URI refers to a managed artifact
         InferredOWLOntologyID artifactID = null;
         try
         {
             artifactID = this.getArtifact(IRI.create(artifactUri));
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             this.log.error("This artifact is unmanaged. [{}]", artifactUri);
             throw e;
         }
         
         // check if updating from the most current version of the artifact
         try
         {
             artifactID = this.getArtifact(IRI.create(versionUri));
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             // if the version IRI is not the most current, it is unmanaged
             final String message =
                     "Attempting to update from an invalid version of an artifact [" + versionUri
                             + "]. The current version is [" + artifactID.getVersionIRI().toString() + "]";
             
             this.log.error(message);
             throw new UnmanagedArtifactVersionException(artifactID.getOntologyIRI(), artifactID.getVersionIRI(),
                     IRI.create(versionUri), message, e);
             // FIXME - handle this conflict intelligently instead of rejecting the update.
         }
         
         final Repository tempRepository = this.getRepositoryManager().getNewTemporaryRepository();
         RepositoryConnection tempRepositoryConnection = null;
         RepositoryConnection permanentRepositoryConnection = null;
         InferredOWLOntologyID inferredOWLOntologyID = null;
         
         try
         {
             // create a temporary in-memory repository
             tempRepositoryConnection = tempRepository.getConnection();
             tempRepositoryConnection.begin();
             
             permanentRepositoryConnection = this.getRepositoryManager().getRepository().getConnection();
             permanentRepositoryConnection.begin();
             
             // load and copy the artifact's concrete statements to the temporary store
             final RepositoryResult<Statement> repoResult =
                     permanentRepositoryConnection.getStatements(null, null, null, false, artifactID.getVersionIRI()
                             .toOpenRDFURI());
             final URI tempContext = artifactID.getVersionIRI().toOpenRDFURI();
             tempRepositoryConnection.add(repoResult, tempContext);
             
             // update the artifact statements
             if(UpdatePolicy.REPLACE_EXISTING.equals(updatePolicy))
             {
                 // create an intermediate context and add "edit" statements to it
                 final URI intContext = PoddRdfConstants.VF.createURI("urn:intermediate:", UUID.randomUUID().toString());
                 
                 tempRepositoryConnection.add(model, intContext);
                 
                 final Collection<URI> replaceableObjects = new ArrayList<URI>(objectUris);
                 
                 // If they did not send a list, we create one ourselves.
                 if(replaceableObjects.isEmpty())
                 {
                     // get all Subjects in "edit" statements
                     final RepositoryResult<Statement> statements =
                             tempRepositoryConnection.getStatements(null, null, null, false, intContext);
                     final List<Statement> allEditStatements = Iterations.addAll(statements, new ArrayList<Statement>());
                     
                     // remove all references to these Subjects in "main" context
                     for(final Statement statement : allEditStatements)
                     {
                         if(statement.getSubject() instanceof URI)
                         {
                             replaceableObjects.add((URI)statement.getSubject());
                         }
                         else
                         {
                             // We do not support replacing objects that are not referenced using
                             // URIs, so they must stay for REPLACE_EXISTING
                             // To remove blank node subject statements, replace the entire object
                             // using REPLACE_ALL
                         }
                     }
                 }
                 
                 for(final URI nextReplaceableObject : replaceableObjects)
                 {
                     tempRepositoryConnection.remove(nextReplaceableObject, null, null, tempContext);
                 }
                 
                 // copy the "edit" statements from intermediate context into our "main" context
                 tempRepositoryConnection.add(
                         tempRepositoryConnection.getStatements(null, null, null, false, intContext), tempContext);
             }
             else
             {
                 tempRepositoryConnection.add(model, tempContext);
             }
             
             // check and update statements with default timestamp values
             final Value now = PoddRdfConstants.VF.createLiteral(new Date());
             this.handleTimestamps(tempRepositoryConnection, PoddRdfConstants.PODD_BASE_CREATED_AT, now, tempContext);
             this.handleTimestamps(tempRepositoryConnection, PoddRdfConstants.PODD_BASE_LAST_MODIFIED, now, tempContext);
             
             this.handleDanglingObjects(artifactID.getOntologyIRI(), tempRepositoryConnection, tempContext,
                     danglingObjectAction);
             
             // Remove any assertions that the user has made about publication status, as this
             // information is a privileged operation that must be done through the designated API
             // method
             tempRepositoryConnection.remove((Resource)null, PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS,
                     (Resource)null, tempContext);
             
             final Set<PoddPurlReference> purls = this.handlePurls(tempRepositoryConnection, tempContext);
             
             final Model resultsModel = new LinkedHashModel();
             
             // add (temp-object-URI :hasPurl PURL) statements to Model
             // NOTE: Using nested loops is rather inefficient, but these collections are not
             // expected
             // to have more than a handful of elements
             for(final URI objectUri : objectUris)
             {
                 for(final PoddPurlReference purl : purls)
                 {
                     final URI tempUri = purl.getTemporaryURI();
                     if(objectUri.equals(tempUri))
                     {
                         resultsModel.add(objectUri, PoddRdfConstants.PODD_REPLACED_TEMP_URI_WITH, purl.getPurlURI());
                         break; // out of inner loop
                     }
                 }
             }
             
             // this.handleFileReferences(tempRepositoryConnection, tempContext,
             // fileReferenceAction);
             
             // increment the version
             final OWLOntologyID currentManagedArtifactID =
                     this.getSesameManager().getCurrentArtifactVersion(IRI.create(artifactUri),
                             permanentRepositoryConnection, this.getRepositoryManager().getArtifactManagementGraph());
             final IRI newVersionIRI =
                     IRI.create(this.incrementVersion(currentManagedArtifactID.getVersionIRI().toString()));
             
             // set version IRI in temporary repository
             this.log.info("Setting version IRI to <{}>", newVersionIRI);
             tempRepositoryConnection.remove(artifactID.getOntologyIRI().toOpenRDFURI(),
                     PoddRdfConstants.OWL_VERSION_IRI, null, tempContext);
             tempRepositoryConnection.add(artifactID.getOntologyIRI().toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                     newVersionIRI.toOpenRDFURI(), tempContext);
             
             // check and ensure schema ontology imports are for version IRIs
             this.handleSchemaImports(artifactID.getOntologyIRI(), permanentRepositoryConnection,
                     tempRepositoryConnection, tempContext);
             
             // ensure schema ontologies are cached in memory before loading statements into OWLAPI
             this.handleCacheSchemasInMemory(permanentRepositoryConnection, tempRepositoryConnection, tempContext);
             
             inferredOWLOntologyID =
                     this.loadInferStoreArtifact(tempRepositoryConnection, permanentRepositoryConnection, tempContext,
                             fileReferenceAction);
             
             permanentRepositoryConnection.commit();
             tempRepositoryConnection.rollback();
             
             return OntologyUtils.ontologyIDsToModel(Arrays.asList(inferredOWLOntologyID), resultsModel);
         }
         catch(final Exception e)
         {
             if(tempRepositoryConnection != null && tempRepositoryConnection.isActive())
             {
                 tempRepositoryConnection.rollback();
             }
             
             if(permanentRepositoryConnection != null && permanentRepositoryConnection.isActive())
             {
                 permanentRepositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
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
             
             // release resources
             if(inferredOWLOntologyID != null)
             {
                this.getOWLManager().removeCache(inferredOWLOntologyID);
             }
             
             if(tempRepositoryConnection != null && tempRepositoryConnection.isOpen())
             {
                 try
                 {
                     tempRepositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
             tempRepository.shutDown();
             
         }
     }
     
     @Override
     public InferredOWLOntologyID updateSchemaImports(final InferredOWLOntologyID artifactId,
             final Set<OWLOntologyID> oldSchemaOntologyIds, final Set<OWLOntologyID> newSchemaOntologyIds)
         throws OpenRDFException, PoddException, IOException, OWLException
     {
         if(artifactId == null)
         {
             throw new IllegalArgumentException("Artifact was null");
         }
         
         RepositoryConnection permanentRepositoryConnection = null;
         RepositoryConnection tempRepositoryConnection = null;
         Repository tempRepository = null;
         try
         {
             permanentRepositoryConnection = this.repositoryManager.getRepository().getConnection();
             permanentRepositoryConnection.begin();
             final InferredOWLOntologyID artifactVersion =
                     this.sesameManager.getCurrentArtifactVersion(artifactId.getOntologyIRI(),
                             permanentRepositoryConnection, this.repositoryManager.getArtifactManagementGraph());
             if(!artifactVersion.getVersionIRI().equals(artifactId.getVersionIRI()))
             {
                 throw new UnmanagedArtifactVersionException(artifactId.getOntologyIRI(),
                         artifactVersion.getVersionIRI(), artifactId.getVersionIRI(),
                         "Cannot update schema imports for artifact as the specified version was not found.");
             }
             
             // Export the artifact without including the old inferred triples, and they will be
             // regenerated using the new schema ontologies
             final Model model = this.exportArtifact(artifactVersion, false);
             
             tempRepository = this.repositoryManager.getNewTemporaryRepository();
             tempRepositoryConnection = tempRepository.getConnection();
             tempRepositoryConnection.begin();
             // Bump the version identifier to a new value
             final IRI newVersionIRI = IRI.create(this.incrementVersion(artifactVersion.getVersionIRI().toString()));
             tempRepositoryConnection.add(model, newVersionIRI.toOpenRDFURI());
             
             tempRepositoryConnection.remove(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI, null);
             tempRepositoryConnection.add(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI,
                     newVersionIRI.toOpenRDFURI(), newVersionIRI.toOpenRDFURI());
             
             for(final OWLOntologyID nextOldSchemaOntologyID : oldSchemaOntologyIds)
             {
                 // Remove both a generic import and a version specific import, so this method can be
                 // used to bump generic imports to version specific imports after they are imported,
                 // if necessary.
                 tempRepositoryConnection.remove(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                         nextOldSchemaOntologyID.getOntologyIRI().toOpenRDFURI());
                 tempRepositoryConnection.remove(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                         nextOldSchemaOntologyID.getVersionIRI().toOpenRDFURI());
             }
             
             // Even if the old version of the artifact did not import this schema, we include it now
             // as it may be required by the others
             for(final OWLOntologyID nextNewSchemaOntologyID : newSchemaOntologyIds)
             {
                 // Add import to the specific version
                 tempRepositoryConnection.add(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                         nextNewSchemaOntologyID.getVersionIRI().toOpenRDFURI(), newVersionIRI.toOpenRDFURI());
             }
             
             tempRepositoryConnection.commit();
             final InferredOWLOntologyID result =
                     this.loadInferStoreArtifact(tempRepositoryConnection, permanentRepositoryConnection,
                             newVersionIRI.toOpenRDFURI(), DataReferenceVerificationPolicy.DO_NOT_VERIFY);
             permanentRepositoryConnection.commit();
             
             return result;
         }
         catch(final Throwable e)
         {
             if(permanentRepositoryConnection != null)
             {
                 permanentRepositoryConnection.rollback();
             }
             if(tempRepositoryConnection != null)
             {
                 tempRepositoryConnection.rollback();
             }
             throw e;
         }
         finally
         {
             if(permanentRepositoryConnection != null)
             {
                 permanentRepositoryConnection.close();
             }
             if(tempRepositoryConnection != null)
             {
                 tempRepositoryConnection.close();
             }
             if(tempRepository != null)
             {
                 tempRepository.shutDown();
             }
         }
     }
     
 }
