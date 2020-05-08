 /**
  * 
  */
 package com.github.podd.impl;
 
import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.URI;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.BooleanQuery;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.repository.RepositoryConnection;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author kutila
  * 
  */
 public class PoddSesameManagerImpl implements PoddSesameManager
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     @Override
     public List<OWLOntologyID> getAllOntologyVersions(final IRI ontologyIRI, final RepositoryConnection connection,
             final URI ontologyManagementGraph)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model
      * .IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)
      */
     @Override
     public InferredOWLOntologyID getCurrentArtifactVersion(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
         UnmanagedArtifactIRIException
     {
         final InferredOWLOntologyID inferredOntologyID =
                 this.getCurrentVersionInternal(ontologyIRI, repositoryConnection, managementGraph);
         
         if(inferredOntologyID != null)
         {
             return inferredOntologyID;
         }
         else
         {
             throw new UnmanagedArtifactIRIException(ontologyIRI, "This IRI does not refer to a managed ontology");
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model
      * .IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)
      */
     @Override
     public InferredOWLOntologyID getCurrentSchemaVersion(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
         UnmanagedSchemaIRIException
     {
         final InferredOWLOntologyID inferredOntologyID =
                 this.getCurrentVersionInternal(ontologyIRI, repositoryConnection, managementGraph);
         
         if(inferredOntologyID != null)
         {
             return inferredOntologyID;
         }
         else
         {
             throw new UnmanagedSchemaIRIException(ontologyIRI, "This IRI does not refer to a managed ontology");
         }
     }
     
     /**
      * Inner helper method for getCurrentArtifactVersion() and getCurrentSchemaVersion().
      * 
      * @param ontologyIRI
      * @param repositoryConnection
      * @param managementGraph
      * @return
      * @throws OpenRDFException
      */
     private InferredOWLOntologyID getCurrentVersionInternal(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
     {
         // FIXME: TODO
         return null;
     }
     
     /**
      * Inner helper method for getCurrentArtifactVersion() and getCurrentSchemaVersion().
      * 
      * @param ontologyIRI
      * @param repositoryConnection
      * @param managementGraph
      * @return
      * @throws OpenRDFException
      */
     private List<InferredOWLOntologyID> getAllVersionsInternal(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
     {
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(managementGraph);
         dataset.addNamedGraph(managementGraph);
         
         // 1: see if the given IRI exists as an ontology IRI
         final String sparqlQuery1 =
                 "SELECT ?cv ?civ WHERE { " + ontologyIRI.toQuotedString() + " <" + RDF.TYPE.stringValue() + "> <"
                         + OWL.ONTOLOGY.stringValue() + "> . " + ontologyIRI.toQuotedString() + " <"
                         + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . "
                         + ontologyIRI.toQuotedString() + " <"
                         + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . " + " }";
         // SELECT ?cv ?civ WHERE { <iri> :type owl:ontology . <iri> omv:current-version ?cv . <iri>
         // :current-inferred-version ?civ . }
         this.log.info("Generated SPARQL {}", sparqlQuery1);
         
         final TupleQuery query1 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery1);
         query1.setDataset(dataset);
         
         final TupleQueryResult query1Results = query1.evaluate();
         if(query1Results.hasNext())
         {
             final BindingSet nextResult = query1Results.next();
             final String nextVersionIRI = nextResult.getValue("cv").stringValue();
             final String nextInferredIRI = nextResult.getValue("civ").stringValue();
             
            return Arrays.asList(new InferredOWLOntologyID(ontologyIRI, IRI.create(nextVersionIRI), IRI
                    .create(nextInferredIRI)));
         }
         
         // 2: see if the given IRI exists as a version IRI
         final String sparqlQuery2 =
                 "SELECT ?x ?civ WHERE { " + " ?x <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> "
                         + ontologyIRI.toQuotedString() + " . " + " ?x <" + RDF.TYPE.stringValue() + "> <"
                         + OWL.ONTOLOGY.stringValue() + "> . " + " ?x <"
                         + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . " + " }";
         // SELECT ?x ?civ WHERE { ?x omv:current-version <iri> . ?x :type owl:ontology . ?x
         // :current-inferred-version ?civ . }
         this.log.info("Generated SPARQL {}", sparqlQuery2);
         
         final TupleQuery query2 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery2);
         query2.setDataset(dataset);
         
         final TupleQueryResult queryResults2 = query2.evaluate();
         if(queryResults2.hasNext())
         {
             final BindingSet nextResult = queryResults2.next();
             final String nextOntologyIRI = nextResult.getValue("x").stringValue();
             final String nextInferredIRI = nextResult.getValue("civ").stringValue();
             
            return Arrays.asList(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), ontologyIRI, IRI
                    .create(nextInferredIRI)));
         }
        return Collections.emptyList();
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddSesameManager#getDirectImports(org.openrdf.repository.
      * RepositoryConnection, org.openrdf.model.URI)
      */
     @Override
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
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddSesameManager#getOntologyIRI(org.openrdf.repository.RepositoryConnection
      * , org.openrdf.model.URI)
      */
     @Override
     public IRI getOntologyIRI(final RepositoryConnection repositoryConnection, final URI context)
         throws OpenRDFException
     {
         // get ontology IRI from the RepositoryConnection using a SPARQL SELECT query
         final String sparqlQuery =
                 "SELECT ?x WHERE { ?x <" + RDF.TYPE + "> <" + OWL.ONTOLOGY.stringValue() + ">  . " + " ?x <"
                         + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT + "> ?y " + " }";
         this.log.info("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(context);
         dataset.addNamedGraph(context);
         query.setDataset(dataset);
         
         IRI ontologyIRI = null;
         
         final TupleQueryResult queryResults = query.evaluate();
         if(queryResults.hasNext())
         {
             final BindingSet nextResult = queryResults.next();
             ontologyIRI = IRI.create(nextResult.getValue("x").stringValue());
         }
         return ontologyIRI;
     }
     
     /**
      * Internal helper method to retrieve the Top-Object IRI for a given ontology.
      * 
      */
     private IRI getTopObjectIRI(final IRI ontologyIRI, final RepositoryConnection repositoryConnection,
             final URI context) throws OpenRDFException
     {
         // get ontology IRI from the RepositoryConnection using a SPARQL SELECT query
         final String sparqlQuery =
                 "SELECT ?y WHERE { " + ontologyIRI.toQuotedString() + " <" + RDF.TYPE + "> <"
                         + OWL.ONTOLOGY.stringValue() + ">  . " + ontologyIRI.toQuotedString() + " <"
                         + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT + "> ?y " + " }";
         this.log.info("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(context);
         dataset.addNamedGraph(context);
         query.setDataset(dataset);
         
         IRI topObjectIRI = null;
         
         final TupleQueryResult queryResults = query.evaluate();
         if(queryResults.hasNext())
         {
             final BindingSet nextResult = queryResults.next();
             topObjectIRI = IRI.create(nextResult.getValue("y").stringValue());
         }
         return topObjectIRI;
     }
     
     @Override
     public boolean isPublished(final OWLOntologyID ontologyID, final RepositoryConnection repositoryConnection)
         throws OpenRDFException
     {
         if(ontologyID == null || ontologyID.getOntologyIRI() == null || ontologyID.getVersionIRI() == null)
         {
             throw new NullPointerException("OWLOntology is incomplete");
         }
         
         final URI artifactGraphUri = ontologyID.getVersionIRI().toOpenRDFURI();
         
         /*
          * ASK {
          * 
          * ?artifact owl:versionIRI ontology-version .
          * 
          * ?artifact poddBase:hasTopObject ?top .
          * 
          * ?top poddBase:hasPublicationStatus poddBase:Published .
          * 
          * }
          */
         final String sparqlQuery =
                 "ASK { " + "?artifact <" + PoddRdfConstants.OWL_VERSION_IRI.stringValue() + "> "
                         + ontologyID.getVersionIRI().toQuotedString() + " . " + "?artifact <"
                         + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top ." + " ?top <"
                         + PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS.stringValue() + "> <"
                         + PoddRdfConstants.PODDBASE_PUBLISHED.stringValue() + ">" + " }";
         
         this.log.info("Generated SPARQL {}", sparqlQuery);
         
         final BooleanQuery booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         // Create a dataset to specify the contexts
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(artifactGraphUri);
         dataset.addNamedGraph(artifactGraphUri);
         booleanQuery.setDataset(dataset);
         
         return booleanQuery.evaluate();
     }
     
     @Override
     public void setPublished(final IRI ontologyIRI, final RepositoryConnection repositoryConnection, final URI context)
         throws OpenRDFException
     {
         final IRI topObjectIRI = this.getTopObjectIRI(ontologyIRI, repositoryConnection, context);
         
         // remove previous value for publication status
         repositoryConnection.remove(topObjectIRI.toOpenRDFURI(), PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                 null, context);
         
         // then insert the publication status as #Published
         repositoryConnection.add(topObjectIRI.toOpenRDFURI(), PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                 PoddRdfConstants.PODDBASE_PUBLISHED, context);
         
         this.log.info("{} was set as Published", topObjectIRI);
     }
     
 }
