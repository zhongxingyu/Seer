 /**
  * 
  */
 package com.github.podd.impl;
 
import info.aduna.iteration.Iterations;

import java.util.List;

 import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
 
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PoddRepositoryManagerImpl implements PoddRepositoryManager
 {
     
     private Repository repository;
     
     private URI artifactGraph = PoddRdfConstants.DEFAULT_ARTIFACT_MANAGEMENT_GRAPH;
     
     private URI schemaGraph = PoddRdfConstants.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
     
     /**
      * Default constructor, which sets up an in-memory MemoryStore repository.
      */
     public PoddRepositoryManagerImpl()
     {
         this.repository = new SailRepository(new MemoryStore());
         try
         {
             this.repository.initialize();
         }
         catch(final RepositoryException e)
         {
             throw new RuntimeException("Could not initialise PoddRepositoryManager with an in-memory repository");
         }
     }
     
     /**
      * 
      * @param repository
      *            An initialized implementation of Repository.
      */
     public PoddRepositoryManagerImpl(final Repository repository)
     {
         this.repository = repository;
     }
     
     @Override
     public URI getArtifactManagementGraph()
     {
         return this.artifactGraph;
     }
     
     @Override
     public Repository getNewTemporaryRepository() throws OpenRDFException
     {
         final Repository result = new SailRepository(new MemoryStore());
         result.initialize();
         
         return result;
     }
     
     @Override
     public Repository getRepository() throws OpenRDFException
     {
         return this.repository;
     }
     
     @Override
     public URI getSchemaManagementGraph()
     {
         return this.schemaGraph;
     }
     
     @Override
     public void setArtifactManagementGraph(final URI artifactManagementGraph)
     {
         this.artifactGraph = artifactManagementGraph;
     }
     
     @Override
     public void setRepository(final Repository repository) throws OpenRDFException
     {
         this.repository = repository;
     }
     
     @Override
     public void setSchemaManagementGraph(final URI schemaManagementGraph)
     {
         this.schemaGraph = schemaManagementGraph;
     }
     
 }
