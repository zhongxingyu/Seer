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
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.Map.Entry;
 import java.util.Objects;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Graph;
 import org.openrdf.model.Literal;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.SESAME;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.config.RepositoryConfig;
 import org.openrdf.repository.config.RepositoryConfigException;
 import org.openrdf.repository.config.RepositoryConfigSchema;
 import org.openrdf.repository.config.RepositoryImplConfig;
 import org.openrdf.repository.config.RepositoryImplConfigBase;
 import org.openrdf.repository.manager.LocalRepositoryManager;
 import org.openrdf.repository.manager.RemoteRepositoryManager;
 import org.openrdf.repository.manager.RepositoryManager;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.repository.sail.config.SailRepositorySchema;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.openrdf.sail.federation.Federation;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.ansell.propertyutil.PropertyUtil;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.utils.ManualShutdownRepository;
 import com.github.podd.utils.PODD;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PoddRepositoryManagerImpl implements PoddRepositoryManager
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     private URI artifactGraph;
     
     private URI dataRepositoryGraph;
     
     private URI schemaGraph;
     
     private URI repositoryGraph;
     
     private String defaultPermanentRepositoryServerUrl;
     
     private Path poddHomeDirectory;
     
     private ManualShutdownRepository managementRepository;
     
     private ConcurrentMap<Set<? extends OWLOntologyID>, ManualShutdownRepository> permanentRepositories =
             new ConcurrentHashMap<>();
     
     private RepositoryImplConfig permanentRepositoryConfigForNew;
     
     private ConcurrentMap<String, RepositoryManager> sesameRepositoryManagers = new ConcurrentHashMap<>();
     
     /**
      * 
      * @param managementRepository
      * @param permanentRepository
      * @throws RepositoryConfigException
      */
     public PoddRepositoryManagerImpl(final Repository managementRepository,
             final RepositoryImplConfig permanentRepositoryConfigForNew,
             final String defaultPermanentRepositoryServerUrl, final Path poddHomeDirectory, final PropertyUtil props)
         throws RepositoryConfigException
     {
         this.managementRepository = new ManualShutdownRepository(managementRepository);
         // this.sesameRepositoryManager = repositoryManager;
         this.permanentRepositoryConfigForNew = permanentRepositoryConfigForNew;
         this.defaultPermanentRepositoryServerUrl = defaultPermanentRepositoryServerUrl;
         this.poddHomeDirectory = poddHomeDirectory;
         this.artifactGraph =
                 PODD.VF.createURI(props.get(PODD.PROPERTY_ARTIFACT_MANAGEMENT_GRAPH,
                         PODD.DEFAULT_ARTIFACT_MANAGEMENT_GRAPH.stringValue()));
         this.dataRepositoryGraph =
                 PODD.VF.createURI(props.get(PODD.PROPERTY_DATA_REPOSITORY_MANAGEMENT_GRAPH,
                         PODD.DEFAULT_DATA_REPOSITORY_MANAGEMENT_GRAPH.stringValue()));
         this.schemaGraph =
                 PODD.VF.createURI(props.get(PODD.PROPERTY_SCHEMA_MANAGEMENT_GRAPH,
                         PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH.stringValue()));
         this.repositoryGraph =
                 PODD.VF.createURI(props.get(PODD.PROPERTY_REPOSITORY_MANAGEMENT_GRAPH,
                         PODD.DEFAULT_REPOSITORY_MANAGEMENT_GRAPH.stringValue()));
     }
     
     @Override
     public URI getArtifactManagementGraph()
     {
         return this.artifactGraph;
     }
     
     @Override
     public URI getFileRepositoryManagementGraph()
     {
         return this.dataRepositoryGraph;
     }
     
     @Override
     public Repository getManagementRepository() throws OpenRDFException
     {
         return this.managementRepository;
     }
     
     @Override
     public Repository getNewTemporaryRepository() throws OpenRDFException
     {
         final Repository result = new SailRepository(new MemoryStore());
         result.initialize();
         
         return result;
     }
     
     @Override
     public Repository getPermanentRepository(final Set<? extends OWLOntologyID> schemaOntologies)
         throws OpenRDFException, IOException
     {
         Objects.requireNonNull(schemaOntologies, "Schema ontologies must not be null");
         
         ManualShutdownRepository permanentRepository = this.permanentRepositories.get(schemaOntologies);
         // This synchronisation should not inhibit most operations, but is necessary to prevent
         // multiple repositories with the same schema ontologies, given that there is a relatively
         // large latency in the new repository create process
         // ConcurrentMap.putIfAbsent is not applicable to the initial situation as it is very costly
         // to create a repository if it is not needed
         if(permanentRepository == null)
         {
             synchronized(this.permanentRepositories)
             {
                 permanentRepository = this.permanentRepositories.get(schemaOntologies);
                 if(permanentRepository == null)
                 {
                     RepositoryConnection managementConnection = null;
                     try
                     {
                         managementConnection = this.getManagementRepository().getConnection();
                         managementConnection.begin();
                         Map<Resource, RepositoryManager> sesameRepositoryManagerMap =
                                 getRepositoryManager(schemaOntologies, managementConnection, this.repositoryGraph);
                         if(sesameRepositoryManagerMap.isEmpty())
                         {
                             throw new RuntimeException("Could not create repository manager");
                         }
                         if(sesameRepositoryManagerMap.size() > 1)
                         {
                             throw new RuntimeException("Found duplicate repository managers. Failing fast");
                         }
                         Resource repositoryManagerURI = sesameRepositoryManagerMap.keySet().iterator().next();
                         URI repositoryUri = null;
                         
                         List<Statement> repositoriesInManager =
                                 Iterations.asList(managementConnection.getStatements(repositoryManagerURI,
                                         PODD.PODD_REPOSITORY_MANAGER_CONTAINS_REPOSITORY, null, false,
                                         this.repositoryGraph));
                         for(Statement nextRepositoryStatement : repositoriesInManager)
                         {
                             Model model = new LinkedHashModel();
                             managementConnection.exportStatements(nextRepositoryStatement.getSubject(),
                                     PODD.PODD_REPOSITORY_CONTAINS_SCHEMA_VERSION, null, true, new StatementCollector(
                                             model), this.repositoryGraph);
                             
                             boolean missingSchema = false;
                             for(OWLOntologyID nextSchemaOntology : schemaOntologies)
                             {
                                 if(!model.contains(nextRepositoryStatement.getSubject(),
                                         PODD.PODD_REPOSITORY_CONTAINS_SCHEMA_VERSION, nextSchemaOntology
                                                 .getVersionIRI().toOpenRDFURI()))
                                 {
                                     missingSchema = true;
                                     break;
                                 }
                             }
                             
                             if(!missingSchema)
                             {
                                 for(Value nextSchema : model.filter(nextRepositoryStatement.getSubject(),
                                         PODD.PODD_REPOSITORY_CONTAINS_SCHEMA_VERSION, null).objects())
                                 {
                                     if(nextSchema instanceof URI)
                                     {
                                         boolean foundNextSchema = false;
                                         for(OWLOntologyID nextSchemaOntology : schemaOntologies)
                                         {
                                             if(nextSchemaOntology.getVersionIRI().toOpenRDFURI().equals(nextSchema))
                                             {
                                                 foundNextSchema = true;
                                                 break;
                                             }
                                         }
                                         if(!foundNextSchema)
                                         {
                                             missingSchema = true;
                                             break;
                                         }
                                     }
                                 }
                             }
                             
                             if(!missingSchema)
                             {
                                 repositoryUri = (URI)nextRepositoryStatement.getSubject();
                             }
                         }
                         
                         RepositoryManager sesameRepositoryManager =
                                 sesameRepositoryManagerMap.values().iterator().next();
                         // If no existing repository found, then create one, else we regenerate a
                         // reference to the existing repository
                         if(repositoryUri == null)
                         {
                             // Create a new one
                             repositoryUri =
                                     managementConnection.getValueFactory().createURI("urn:podd:repository:",
                                             UUID.randomUUID().toString());
                             // Get a new repository ID using our base name as the starting point
                             final String newRepositoryID =
                                     sesameRepositoryManager.getNewRepositoryID(repositoryUri.stringValue());
                             final RepositoryConfig config =
                                     new RepositoryConfig(newRepositoryID,
                                             "PODD Redesign Repository (Automatically created)",
                                             this.permanentRepositoryConfigForNew);
                             sesameRepositoryManager.addRepositoryConfig(config);
                             
                             final ManualShutdownRepository nextRepository =
                                     new ManualShutdownRepository(sesameRepositoryManager.getRepository(newRepositoryID));
                             // If we somehow created a new repository since we entered this section,
                             // we need to remove the new repository to cleanup
                             final ManualShutdownRepository putIfAbsent =
                                     this.permanentRepositories.putIfAbsent(schemaOntologies, nextRepository);
                             if(putIfAbsent != null)
                             {
                                 this.log.error("Created a new duplicate repository that must now be removed: {}",
                                         newRepositoryID);
                                 final boolean removeRepository =
                                         sesameRepositoryManager.removeRepository(newRepositoryID);
                                 if(!removeRepository)
                                 {
                                     this.log.warn("Could not remove repository");
                                 }
                                 permanentRepository = putIfAbsent;
                             }
                             else
                             {
                                 permanentRepository = nextRepository;
                                 
                                 // In this case, we need to copy the relevant schema ontologies over
                                 // to
                                 // the
                                 // new repository
                                 RepositoryConnection permanentConnection = null;
                                 try
                                 {
                                     permanentConnection = permanentRepository.getConnection();
                                     permanentConnection.begin();
                                     for(final OWLOntologyID nextSchemaOntology : schemaOntologies)
                                     {
                                         // TODO: Check if the ontology version exists in the
                                         // management connection
                                         if(!permanentConnection.hasStatement(null, null, null, false,
                                                 nextSchemaOntology.getVersionIRI().toOpenRDFURI()))
                                         {
                                             permanentConnection.add(managementConnection.getStatements(null, null,
                                                     null, false, nextSchemaOntology.getVersionIRI().toOpenRDFURI()),
                                                     nextSchemaOntology.getVersionIRI().toOpenRDFURI());
                                         }
                                         
                                         final RepositoryResult<Statement> statements =
                                                 managementConnection.getStatements(nextSchemaOntology.getVersionIRI()
                                                         .toOpenRDFURI(), PODD.PODD_BASE_INFERRED_VERSION, null, false,
                                                         this.getSchemaManagementGraph());
                                         
                                         for(final Statement nextInferredStatement : Iterations.asList(statements))
                                         {
                                             if(nextInferredStatement.getObject() instanceof URI)
                                             {
                                                 if(!permanentConnection.hasStatement(null, null, null, false,
                                                         (URI)nextInferredStatement.getObject()))
                                                 {
                                                     permanentConnection.add(managementConnection.getStatements(null,
                                                             null, null, false, (URI)nextInferredStatement.getObject()),
                                                             (URI)nextInferredStatement.getObject());
                                                 }
                                             }
                                         }
                                     }
                                     permanentConnection.commit();
                                 }
                                 catch(final Throwable e)
                                 {
                                     if(permanentConnection != null)
                                     {
                                         permanentConnection.rollback();
                                     }
                                     throw e;
                                 }
                                 finally
                                 {
                                     if(permanentConnection != null)
                                     {
                                         permanentConnection.close();
                                     }
                                 }
                                 
                                 Literal repositoryIdInManager =
                                         managementConnection.getValueFactory().createLiteral(newRepositoryID);
                                 managementConnection.add(repositoryManagerURI,
                                         PODD.PODD_REPOSITORY_MANAGER_CONTAINS_REPOSITORY, repositoryUri,
                                         this.repositoryGraph);
                                 managementConnection.add(repositoryUri, RDF.TYPE, PODD.PODD_REPOSITORY);
                                 managementConnection.add(repositoryUri, PODD.PODD_REPOSITORY_ID_IN_MANAGER,
                                         repositoryIdInManager, this.repositoryGraph);
                                 for(OWLOntologyID nextSchemaOntologyID : schemaOntologies)
                                 {
                                     managementConnection.add(repositoryUri, PODD.PODD_REPOSITORY_CONTAINS_SCHEMA_IRI,
                                             nextSchemaOntologyID.getOntologyIRI().toOpenRDFURI(), this.repositoryGraph);
                                     managementConnection.add(repositoryUri,
                                             PODD.PODD_REPOSITORY_CONTAINS_SCHEMA_VERSION, nextSchemaOntologyID
                                                     .getVersionIRI().toOpenRDFURI(), this.repositoryGraph);
                                 }
                             }
                         }
                         else
                         {
                             // create reference to existing repositoryUri
                             Model model = new LinkedHashModel();
                             managementConnection.exportStatements(repositoryUri, null, null, false,
                                     new StatementCollector(model), this.repositoryGraph);
                             if(!model.contains(repositoryUri, RDF.TYPE, PODD.PODD_REPOSITORY))
                             {
                                 throw new RuntimeException(
                                         "Found repository that was not typed correctly in management graph: "
                                                 + repositoryUri.stringValue());
                             }
                             
                             Literal existingRepositoryId =
                                     model.filter(repositoryUri, PODD.PODD_REPOSITORY_ID_IN_MANAGER, null)
                                             .objectLiteral();
                             
                             Repository nextRepository =
                                     sesameRepositoryManager.getRepository(existingRepositoryId.getLabel());
                             
                             if(nextRepository == null)
                             {
                                 throw new RuntimeException("Failed to get existing repository from manager: "
                                         + existingRepositoryId);
                             }
                             
                             nextRepository = new ManualShutdownRepository(nextRepository);
                             
                             ManualShutdownRepository putIfAbsent =
                                     permanentRepositories.putIfAbsent(schemaOntologies,
                                             (ManualShutdownRepository)nextRepository);
                             if(putIfAbsent != null)
                             {
                                 // TODO: Should we shutdown the repository that is being replaced?
                                 // Ideally we will be shutting down the repository manager later,
                                 // and they should be references to the same Repository anyway
                                 // nextRepository.shutDown();
                                 
                                 nextRepository = putIfAbsent;
                             }
                             permanentRepository = (ManualShutdownRepository)nextRepository;
                         }
                         managementConnection.commit();
                     }
                     catch(final Throwable e)
                     {
                         if(managementConnection != null)
                         {
                             managementConnection.rollback();
                         }
                         throw e;
                     }
                     finally
                     {
                         if(managementConnection != null)
                         {
                             managementConnection.close();
                         }
                     }
                     
                 }
             }
         }
         return permanentRepository;
     }
     
     @Override
     public URI getSchemaManagementGraph()
     {
         return this.schemaGraph;
     }
     
     @Override
     public boolean safeContexts(final URI... contexts)
     {
         boolean returnValue = true;
         if(contexts == null)
         {
             returnValue = false;
         }
         else if(contexts.length == 0)
         {
             returnValue = false;
         }
         else
         {
             for(final URI nextContext : contexts)
             {
                 if(nextContext == null)
                 {
                     returnValue = false;
                 }
                 else if(nextContext.equals(SESAME.NIL))
                 {
                     returnValue = false;
                 }
                 else if(nextContext.equals(this.getArtifactManagementGraph()))
                 {
                     returnValue = false;
                 }
                 else if(nextContext.equals(this.getSchemaManagementGraph()))
                 {
                     returnValue = false;
                 }
                 else if(nextContext.equals(this.getFileRepositoryManagementGraph()))
                 {
                     returnValue = false;
                 }
             }
         }
         
         if(!returnValue)
         {
             this.log.warn("Found unsafe URI contexts: <{}>", Arrays.asList(contexts));
         }
         
         return returnValue;
     }
     
     @Override
     public void setArtifactManagementGraph(final URI artifactManagementGraph)
     {
         this.artifactGraph = artifactManagementGraph;
     }
     
     @Override
     public void setFileRepositoryManagementGraph(final URI dataRepositoryManagementGraph)
     {
         this.dataRepositoryGraph = dataRepositoryManagementGraph;
     }
     
     @Override
     public void setManagementRepository(final Repository repository) throws OpenRDFException
     {
         this.managementRepository = new ManualShutdownRepository(repository);
     }
     
     @Override
     public void setSchemaManagementGraph(final URI schemaManagementGraph)
     {
         this.schemaGraph = schemaManagementGraph;
     }
     
     @Override
     public void shutDown() throws RepositoryException
     {
         RepositoryException foundException = null;
         try
         {
             if(this.managementRepository != null)
             {
                 this.log.info("Shutting down management repository");
                 this.managementRepository.realShutDown();
             }
         }
         catch(final RepositoryException e)
         {
             foundException = e;
         }
         finally
         {
             for(final Entry<Set<? extends OWLOntologyID>, ManualShutdownRepository> nextRepository : this.permanentRepositories
                     .entrySet())
             {
                 try
                 {
                     this.log.info("Shutting down repository for schema ontologies: {} ", nextRepository.getKey());
                     nextRepository.getValue().realShutDown();
                 }
                 catch(final RepositoryException e)
                 {
                     if(foundException == null)
                     {
                         foundException = e;
                     }
                     else
                     {
                         foundException.addSuppressed(e);
                     }
                 }
             }
             
             for(final Entry<String, RepositoryManager> nextRepository : this.sesameRepositoryManagers.entrySet())
             {
                 try
                 {
                    this.log.info("Shutting down repository for schema ontologies: {} ", nextRepository.getKey());
                     nextRepository.getValue().shutDown();
                 }
                 catch(final RuntimeException e)
                 {
                     if(foundException == null)
                     {
                         foundException = new RepositoryException("Could not shutdown a repository manager", e);
                     }
                     else
                     {
                         foundException.addSuppressed(e);
                     }
                 }
             }
         }
         
         if(foundException != null)
         {
             throw foundException;
         }
     }
     
     @Override
     public Repository getReadOnlyFederatedRepository(final Set<? extends OWLOntologyID> schemaImports)
         throws OpenRDFException, IOException
     {
         final Federation federation = new Federation();
         federation.setReadOnly(true);
         federation.addMember(this.getPermanentRepository(schemaImports));
         federation.addMember(this.getManagementRepository());
         federation.initialize();
         final Repository federationRepository = new SailRepository(federation);
         federationRepository.initialize();
         return federationRepository;
     }
     
     private Map<Resource, RepositoryManager> getRepositoryManager(Set<? extends OWLOntologyID> schemaImports,
             RepositoryConnection managementConnection, URI repositoryManagementContext) throws RepositoryException,
         RDFHandlerException, IOException
     {
         Model model = new LinkedHashModel();
         managementConnection.export(new StatementCollector(model), repositoryManagementContext);
         
         for(Resource nextRepositoryManager : model.filter(null, RDF.TYPE, PODD.PODD_REPOSITORY_MANAGER).subjects())
         {
             Set<Value> repositories =
                     model.filter(nextRepositoryManager, PODD.PODD_REPOSITORY_MANAGER_CONTAINS_REPOSITORY, null)
                             .objects();
             for(Value nextRepository : repositories)
             {
                 if(nextRepository instanceof URI)
                 {
                     Model schemas =
                             model.filter((URI)nextRepository, PODD.PODD_REPOSITORY_CONTAINS_SCHEMA_VERSION, null);
                     
                     boolean allMatched = true;
                     // Check that all of the schemas supported by this repository are in the
                     // requested list of schema imports
                     for(Value nextSchema : schemas.objects())
                     {
                         if(nextSchema instanceof URI)
                         {
                             boolean found = false;
                             for(OWLOntologyID nextSchemaImport : schemaImports)
                             {
                                 if(nextSchemaImport.getVersionIRI().toOpenRDFURI().equals(nextSchema))
                                 {
                                     found = true;
                                     break;
                                 }
                             }
                             if(!found)
                             {
                                 allMatched = false;
                                 break;
                             }
                         }
                     }
                     
                     // Check also that all of the requested schema imports are in the repository
                     // Need to perform this check to ensure that there is an exact match
                     // If there is not an exact match, and OWL database may not produce the correct
                     // set of inferences
                     for(OWLOntologyID nextSchemaImport : schemaImports)
                     {
                         boolean found = false;
                         for(Value nextSchema : schemas.objects())
                         {
                             if(nextSchema instanceof URI)
                             {
                                 if(nextSchemaImport.getVersionIRI().toOpenRDFURI().equals(nextSchema))
                                 {
                                     found = true;
                                     break;
                                 }
                             }
                             if(!found)
                             {
                                 allMatched = false;
                                 break;
                             }
                         }
                     }
                     
                     if(allMatched)
                     {
                         URI repositoryManagerType =
                                 model.filter(nextRepositoryManager, PODD.PODD_REPOSITORY_MANAGER_TYPE, null)
                                         .objectURI();
                         
                         if(repositoryManagerType.equals(PODD.PODD_REPOSITORY_MANAGER_TYPE_LOCAL))
                         {
                             Literal directory =
                                     model.filter(nextRepositoryManager, PODD.PODD_REPOSITORY_MANAGER_LOCAL_DIRECTORY,
                                             null).objectLiteral();
                             if(directory != null)
                             {
                                 return Collections.<Resource, RepositoryManager> singletonMap(nextRepositoryManager,
                                         new LocalRepositoryManager(Paths.get(directory.stringValue()).toFile()));
                             }
                             else
                             {
                                 Path path = Files.createTempDirectory("podd-temp-repositories-");
                                 this.log.warn("Temporary local repositories in use!!!: {} {}", path.toString(),
                                         schemaImports);
                                 return Collections.<Resource, RepositoryManager> singletonMap(nextRepositoryManager,
                                         new LocalRepositoryManager(path.toFile()));
                             }
                         }
                         else if(repositoryManagerType.equals(PODD.PODD_REPOSITORY_MANAGER_TYPE_REMOTE))
                         {
                             Literal serverURL =
                                     model.filter(nextRepositoryManager, PODD.PODD_REPOSITORY_MANAGER_REMOTE_SERVER_URL,
                                             null).objectLiteral();
                             return Collections.<Resource, RepositoryManager> singletonMap(nextRepositoryManager,
                                     new RemoteRepositoryManager(serverURL.stringValue()));
                         }
                         else
                         {
                             throw new RuntimeException("Did not recognise repository manager type: "
                                     + repositoryManagerType.stringValue());
                         }
                     }
                 }
             }
         }
         
         // None were found so create a new repository manager based on configuration
         
         RepositoryManager repositoryManager = null;
         
         Resource newRepositoryManagerURI =
                 managementConnection.getValueFactory().createURI(
                         "urn:podd:repositorymanager:" + UUID.randomUUID().toString());
         
         managementConnection.add(newRepositoryManagerURI, RDF.TYPE, PODD.PODD_REPOSITORY_MANAGER,
                 repositoryManagementContext);
         
         // We decide whether we will create a remote or a local repository manager based on the
         // default permanent repository server URL
         final String repositoryManagerUrl = defaultPermanentRepositoryServerUrl;
         
         if(repositoryManagerUrl == null || repositoryManagerUrl.trim().isEmpty())
         {
             Path nextPath = poddHomeDirectory.resolve(newRepositoryManagerURI.stringValue());
             repositoryManager = new LocalRepositoryManager(Files.createDirectory(nextPath).toFile());
             
             Literal nextLiteral = managementConnection.getValueFactory().createLiteral(nextPath.toString());
             
             managementConnection.add(newRepositoryManagerURI, PODD.PODD_REPOSITORY_MANAGER_TYPE,
                     PODD.PODD_REPOSITORY_MANAGER_TYPE_LOCAL, repositoryManagementContext);
             managementConnection.add(newRepositoryManagerURI, PODD.PODD_REPOSITORY_MANAGER_LOCAL_DIRECTORY,
                     nextLiteral, repositoryManagementContext);
         }
         else
         {
             repositoryManager = new RemoteRepositoryManager(repositoryManagerUrl);
             Literal nextUrl = managementConnection.getValueFactory().createLiteral(repositoryManagerUrl);
             managementConnection.add(newRepositoryManagerURI, PODD.PODD_REPOSITORY_MANAGER_TYPE,
                     PODD.PODD_REPOSITORY_MANAGER_TYPE_REMOTE, repositoryManagementContext);
             managementConnection.add(newRepositoryManagerURI, PODD.PODD_REPOSITORY_MANAGER_REMOTE_SERVER_URL, nextUrl,
                     repositoryManagementContext);
         }
         
         repositoryManager.initialize();
         
         return Collections.<Resource, RepositoryManager> singletonMap(newRepositoryManagerURI, repositoryManager);
     }
 }
