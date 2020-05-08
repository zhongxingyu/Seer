 /**
  * 
  */
 package com.github.podd.impl;
 
 import java.util.List;
 
 import org.openrdf.model.URI;
 import org.openrdf.repository.RepositoryConnection;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.profiles.OWLProfile;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.exception.PublishArtifactException;
 import com.github.podd.utils.InferredOWLOntologyID;
 
 /**
  * Implementation of PoddOWLManager interface.
  * 
  * @author kutila
  * 
  */
 public class PoddOWLManagerImpl implements PoddOWLManager
 {
     
     private OWLOntologyManager owlOntologyManager;
     
     private OWLReasonerFactory reasonerFactory;
     
     @Override
     public void cacheSchemaOntology(final InferredOWLOntologyID ontology, final RepositoryConnection conn)
     {
         throw new RuntimeException("TODO: Implement cacheSchemaOntology");
     }
     
     @Override
     public OWLReasoner createReasoner(final OWLOntology nextOntology)
     {
         return this.reasonerFactory.createReasoner(nextOntology);
     }
     
     @Override
     public InferredOWLOntologyID generateInferredOntologyID(final OWLOntologyID ontologyID)
     {
         throw new RuntimeException("TODO: Implement generateInferredOntologyID");
     }
     
     @Override
     public OWLOntologyID getCurrentVersion(final IRI ontologyIRI)
     {
         throw new RuntimeException("TODO: Implement getCurrentVersion");
     }
     
     @Override
     public OWLOntology getOntology(final OWLOntologyID ontologyID) throws IllegalArgumentException, OWLException
     {
         return this.owlOntologyManager.getOntology(ontologyID);
     }
     
     @Override
     public OWLOntologyManager getOWLOntologyManager()
     {
         return this.owlOntologyManager;
     }
     
     @Override
     public OWLReasonerFactory getReasonerFactory()
     {
         return this.reasonerFactory;
     }
     
     @Override
     public OWLProfile getReasonerProfile()
     {
         throw new RuntimeException("TODO: Implement getReasonerProfile");
     }
     
     @Override
     public List<OWLOntologyID> getVersions(final IRI ontologyIRI)
     {
         throw new RuntimeException("TODO: Implement getVersions");
     }
     
     @Override
     public InferredOWLOntologyID inferStatements(final OWLOntologyID inferredOWLOntologyID,
             final RepositoryConnection permanentRepositoryConnection)
     {
         throw new RuntimeException("TODO: Implement inferStatements");
     }
     
     @Override
     public boolean isPublished(final IRI ontologyIRI)
     {
         throw new RuntimeException("TODO: Implement isPublished(IRI)");
     }
     
     @Override
     public boolean isPublished(final OWLOntologyID ontologyId)
     {
         throw new RuntimeException("TODO: Implement isPublished(OWLOntologyID)");
     }
     
     @Override
     public OWLOntology loadOntology(final RioMemoryTripleSource owlSource) throws OWLException
     {
         return this.owlOntologyManager.loadOntologyFromOntologyDocument(owlSource);
     }
     
     @Override
     public OWLOntologyID parseRDFStatements(final RepositoryConnection conn, final URI... contexts)
     {
         throw new RuntimeException("TODO: Implement parseRDFStatements");
     }
     
     @Override
     public boolean removeCache(final OWLOntologyID ontologyID) throws OWLException
     {
         // TODO: Verify that this .contains method matches our desired semantics
         final boolean containsOntology = this.owlOntologyManager.contains(ontologyID);
         
         if(containsOntology)
         {
             this.owlOntologyManager.removeOntology(ontologyID);
             
            // return true if the ontology manager does not contain the ontology at this point
            return !this.owlOntologyManager.contains(ontologyID);
         }
         else
         {
             return false;
         }
     }
     
     @Override
     public void setCurrentVersion(final OWLOntologyID ontologyId)
     {
         throw new RuntimeException("TODO: Implement setCurrentVersion");
     }
     
     @Override
     public void setOWLOntologyManager(final OWLOntologyManager manager)
     {
         this.owlOntologyManager = manager;
         
     }
     
     @Override
     public InferredOWLOntologyID setPublished(final OWLOntologyID ontologyId) throws PublishArtifactException
     {
         throw new RuntimeException("TODO: Implement setPublished");
     }
     
     @Override
     public void setReasonerFactory(final OWLReasonerFactory reasonerFactory)
     {
         this.reasonerFactory = reasonerFactory;
     }
     
 }
