 /**
  * 
  */
 package com.github.podd.impl;
 
 import info.aduna.iteration.Iterations;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Literal;
 import org.openrdf.model.Model;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.BooleanQuery;
 import org.openrdf.query.GraphQuery;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.QueryResults;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.query.resultio.helpers.QueryResultCollector;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddObjectLabel;
 import com.github.podd.utils.PoddObjectLabelImpl;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author kutila
  * 
  */
 public class PoddSesameManagerImpl implements PoddSesameManager
 {
     public PoddSesameManagerImpl()
     {
     }
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /**
      * Helper method to execute a given SPARQL Tuple query, which may have had bindings attached.
      * 
      * @param sparqlQuery
      * @param contexts
      * @return
      * @throws OpenRDFException
      */
     private QueryResultCollector executeSparqlQuery(final TupleQuery sparqlQuery, final URI... contexts)
         throws OpenRDFException
     {
         final DatasetImpl dataset = new DatasetImpl();
         for(final URI uri : contexts)
         {
             dataset.addDefaultGraph(uri);
         }
         sparqlQuery.setDataset(dataset);
         
         QueryResultCollector results = new QueryResultCollector();
         QueryResults.report(sparqlQuery.evaluate(), results);
         
         return results;
     }
     
     /**
      * Helper method to execute a given SPARQL Graph query.
      * 
      * @param sparqlQuery
      * @param contexts
      * @return
      * @throws OpenRDFException
      */
     private Model executeGraphQuery(final GraphQuery sparqlQuery, final URI... contexts) throws OpenRDFException
     {
         final DatasetImpl dataset = new DatasetImpl();
         for(final URI uri : contexts)
         {
             dataset.addDefaultGraph(uri);
         }
         sparqlQuery.setDataset(dataset);
         Model results = new LinkedHashModel();
         sparqlQuery.evaluate(new StatementCollector(results));
         
         return results;
     }
     
     @Override
     public void deleteOntologies(final Collection<InferredOWLOntologyID> givenOntologies,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
     {
         for(final InferredOWLOntologyID nextOntologyID : givenOntologies)
         {
             final List<InferredOWLOntologyID> versionInternal =
                     this.getCurrentVersionsInternal(nextOntologyID.getOntologyIRI(), repositoryConnection,
                             managementGraph);
             boolean updateCurrentVersion = false;
             InferredOWLOntologyID newCurrentVersion = null;
             
             // If there were managed versions, and the head of the list, which is the current
             // version by convention, is the same as out version, then we need to update it,
             // otherwise we don't need to update it.
             if(!versionInternal.isEmpty()
                     && versionInternal.get(0).getVersionIRI().equals(nextOntologyID.getVersionIRI()))
             {
                 updateCurrentVersion = true;
                 if(versionInternal.size() > 1)
                 {
                     // FIXME: Improve this version detection...
                     newCurrentVersion = versionInternal.get(1);
                 }
             }
             
             // clear out the direct and inferred ontology graphs
             repositoryConnection.remove((URI)null, null, null, nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
             repositoryConnection.remove((URI)null, null, null, nextOntologyID.getVersionIRI().toOpenRDFURI());
             
             // clear out references attached to the version and inferred IRIs in the management
             // graph
             repositoryConnection.remove(nextOntologyID.getVersionIRI().toOpenRDFURI(), null, null, managementGraph);
             repositoryConnection.remove(nextOntologyID.getInferredOntologyIRI().toOpenRDFURI(), null, null,
                     managementGraph);
             
             // clear out references linked to the version and inferred IRIs in the management graph
             repositoryConnection
                     .remove((URI)null, null, nextOntologyID.getVersionIRI().toOpenRDFURI(), managementGraph);
             repositoryConnection.remove((URI)null, null, nextOntologyID.getInferredOntologyIRI().toOpenRDFURI(),
                     managementGraph);
             
             if(updateCurrentVersion)
             {
                 final List<Statement> asList =
                         Iterations.asList(repositoryConnection.getStatements(nextOntologyID.getOntologyIRI()
                                 .toOpenRDFURI(), PoddRdfConstants.OMV_CURRENT_VERSION, null, false, managementGraph));
                 
                 if(asList.size() != 1)
                 {
                     this.log.error(
                             "Did not find a unique managed current version for ontology with ID: {} List was: {}",
                             nextOntologyID, asList);
                 }
                 
                 // remove the current versions from the management graph
                 repositoryConnection.remove(asList, managementGraph);
                 
                 // If there is no replacement available, then wipe the slate clean in the management
                 // graph
                 if(newCurrentVersion == null)
                 {
                     repositoryConnection.remove(nextOntologyID.getOntologyIRI().toOpenRDFURI(), null, null,
                             managementGraph);
                 }
                 else
                 {
                     // Push the next current version into the management graph
                     repositoryConnection.add(nextOntologyID.getOntologyIRI().toOpenRDFURI(),
                             PoddRdfConstants.OMV_CURRENT_VERSION, newCurrentVersion.getVersionIRI().toOpenRDFURI(),
                             managementGraph);
                 }
             }
         }
     }
     
     @Override
     public List<InferredOWLOntologyID> getAllOntologyVersions(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI ontologyManagementGraph) throws OpenRDFException
     {
         return this.getCurrentVersionsInternal(ontologyIRI, repositoryConnection, ontologyManagementGraph);
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
         final List<InferredOWLOntologyID> list =
                 this.getCurrentVersionsInternal(ontologyIRI, repositoryConnection, managementGraph);
         
         if(list.isEmpty())
         {
             return null;
         }
         else
         {
             return list.get(0);
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
     private List<InferredOWLOntologyID> getCurrentVersionsInternal(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
     {
         final List<InferredOWLOntologyID> returnList = new ArrayList<InferredOWLOntologyID>();
         
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
         
         final QueryResultCollector nextResults1 = new QueryResultCollector();
         QueryResults.report(query1Results, nextResults1);
         
         for(final BindingSet nextResult : nextResults1.getBindingSets())
         {
             final String nextVersionIRI = nextResult.getValue("cv").stringValue();
             final String nextInferredIRI = nextResult.getValue("civ").stringValue();
             
             returnList.add(new InferredOWLOntologyID(ontologyIRI, IRI.create(nextVersionIRI), IRI
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
         
         final QueryResultCollector nextResults2 = new QueryResultCollector();
         QueryResults.report(queryResults2, nextResults2);
         
         for(final BindingSet nextResult : nextResults2.getBindingSets())
         {
             final String nextOntologyIRI = nextResult.getValue("x").stringValue();
             final String nextInferredIRI = nextResult.getValue("civ").stringValue();
             
             returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), ontologyIRI, IRI
                     .create(nextInferredIRI)));
         }
         
         return returnList;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddSesameManager#getDirectImports(org.openrdf.repository.
      * RepositoryConnection, org.openrdf.model.URI)
      */
     @Override
     public Set<IRI> getDirectImports(final InferredOWLOntologyID ontologyID,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         return getDirectImports(repositoryConnection, versionAndInferredContexts(ontologyID));
     }
     
     @Override
     public Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI... contexts)
         throws OpenRDFException
     {
         final String sparqlQuery = "SELECT ?x WHERE { ?y <" + OWL.IMPORTS.stringValue() + "> ?x ." + " }";
         this.log.info("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         for(URI nextContext : contexts)
         {
             dataset.addDefaultGraph(nextContext);
         }
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
     
     @Override
     public Collection<InferredOWLOntologyID> getOntologies(final boolean onlyCurrentVersions,
             final RepositoryConnection repositoryConnection, final URI ontologyManagementGraph) throws OpenRDFException
     {
         final List<InferredOWLOntologyID> returnList = new ArrayList<InferredOWLOntologyID>();
         
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(ontologyManagementGraph);
         dataset.addNamedGraph(ontologyManagementGraph);
         
         // 1: see if the given IRI exists as an ontology IRI
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT ?ontology ?version ?inferredVersion WHERE { ?ontology <");
         sb.append(RDF.TYPE.stringValue());
         sb.append("> <");
         sb.append(OWL.ONTOLOGY.stringValue());
         sb.append("> . ");
         if(onlyCurrentVersions)
         {
             sb.append(" ?ontology <");
             sb.append(PoddRdfConstants.OMV_CURRENT_VERSION.stringValue());
             sb.append("> ?version . ");
         }
         else
         {
             sb.append(" ?ontology <");
             sb.append(OWL.VERSIONIRI.stringValue());
             sb.append("> ?version . ");
         }
         sb.append("OPTIONAL{ ?version <");
         sb.append(PoddRdfConstants.PODD_BASE_INFERRED_VERSION.stringValue());
         sb.append("> ?inferredVersion . ");
         sb.append(" }");
         sb.append("}");
         
         this.log.info("Generated SPARQL {}", sb);
         
         final TupleQuery query1 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         query1.setDataset(dataset);
         
         final TupleQueryResult query1Results = query1.evaluate();
         
         final QueryResultCollector nextResults1 = new QueryResultCollector();
         QueryResults.report(query1Results, nextResults1);
         
         for(final BindingSet nextResult : nextResults1.getBindingSets())
         {
             final String nextOntologyIRI = nextResult.getValue("ontology").stringValue();
             final String nextVersionIRI = nextResult.getValue("version").stringValue();
             String nextInferredIRI = null;
             
             if(nextResult.hasBinding("inferredVersion"))
             {
                 nextInferredIRI = nextResult.getValue("inferredVersion").stringValue();
                 returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), IRI
                         .create(nextInferredIRI)));
             }
             else
             {
                 returnList
                         .add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), null));
             }
         }
         
         return returnList;
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
      * Retrieve a list of Top Objects that are contained in the given ontology.
      * 
      * @param repositoryConnection
      * @param contexts
      * @return
      * @throws OpenRDFException
      */
     @Override
     public List<URI> getTopObjects(final InferredOWLOntologyID ontologyID,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT ?topObjectUri ?artifactUri ");
         
         sb.append(" WHERE { ");
         
         sb.append(" ?artifactUri <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?topObjectUri . \n");
         
         sb.append(" }");
         
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         query.setBinding("artifactUri", ontologyID.getOntologyIRI().toOpenRDFURI());
         
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(query, versionAndInferredContexts(ontologyID));
         
         final List<URI> topObjectList = new ArrayList<URI>();
         
         for(BindingSet next : queryResults.getBindingSets())
         {
             final URI pred = (URI)next.getValue("topObjectUri");
             
             topObjectList.add(pred);
         }
         
         return topObjectList;
     }
     
     /**
      * Internal helper method to retrieve the Top-Object IRI for a given ontology.
      * 
      */
     @Override
     public URI getTopObjectIRI(final InferredOWLOntologyID ontologyIRI, final RepositoryConnection repositoryConnection)
         throws OpenRDFException
     {
         List<URI> results = getTopObjects(ontologyIRI, repositoryConnection);
         
         if(results.isEmpty())
         {
             return null;
         }
         else if(results.size() == 1)
         {
             return results.get(0);
         }
         else
         {
             log.warn("More than one top object found");
             return results.get(0);
         }
     }
     
     /**
      * Retrieves the most specific types of the given object as a List of URIs.
      * 
      * The contexts searched in are, the given ongology's asserted and inferred graphs as well as
      * their imported schema ontology graphs.
      * 
      * FIXME: Fix the algorithm for this to return only the most specific types
      * 
      * @param ontologyID
      *            The artifact to which the object belongs
      * @param objectUri
      *            The object whose type is to be determined
      * @param repositoryConnection
      * @return A list of URIs for the identified object Types
      * @throws OpenRDFException
      */
     @Override
     public List<URI> getObjectTypes(final InferredOWLOntologyID ontologyID, final URI objectUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final StringBuilder sb = new StringBuilder();
         sb.append("SELECT ?poddTypeUri ");
         sb.append(" WHERE { ");
         sb.append(" ?objectUri <" + RDF.TYPE + "> ?poddTypeUri . ");
         
         // filter out TYPE statements for OWL:Thing, OWL:Individual, OWL:NamedIndividual & OWL:Class
         sb.append("FILTER (?poddTypeUri != <" + OWL.THING.stringValue() + ">) ");
         sb.append("FILTER (?poddTypeUri != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append("FILTER (?poddTypeUri != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append("FILTER (?poddTypeUri != <" + OWL.CLASS.stringValue() + ">) ");
         
         sb.append(" }");
         
         this.log.info("Created SPARQL {} with objectUri bound to {}", sb.toString(), objectUri);
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("objectUri", objectUri);
         final QueryResultCollector queryResults =
                this.executeSparqlQuery(tupleQuery,
                        this.versionAndInferredAndSchemaContexts(ontologyID, repositoryConnection));
         
         List<URI> results = new ArrayList<URI>(queryResults.getBindingSets().size());
         
         for(BindingSet next : queryResults.getBindingSets())
         {
             results.add((URI)next.getValue("poddTypeUri"));
         }
         
         return results;
     }
     
     /**
      * Given an object URI, this method attempts to retrieve its label (rdfs:label) and description
      * (rdfs:comment) encapsulated in a <code>PoddObjectLabel</code> instance.
      * 
      * If a label is not found, the local name from the Object URI is used as the label.
      * 
      * @param ontologyID
      *            Is used to decide on the graphs in which to search for a label. This includes the
      *            given ontology as well as its imports.
      * @param objectUri
      *            The object whose label and description are sought.
      * @param repositoryConnection
      * @return
      * @throws OpenRDFException
      */
     @Override
     public PoddObjectLabel getObjectLabel(final InferredOWLOntologyID ontologyID, final URI objectUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final StringBuilder sb = new StringBuilder();
         sb.append("SELECT ?label ?description ");
         sb.append(" WHERE { ");
         sb.append(" OPTIONAL { ?objectUri <" + RDFS.LABEL + "> ?label . } ");
         sb.append(" OPTIONAL { ?objectUri <" + RDFS.COMMENT + "> ?description . } ");
         sb.append(" }");
         
         this.log.info("Created SPARQL {} with objectUri bound to {}", sb.toString(), objectUri);
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("objectUri", objectUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery,
                         this.versionAndInferredAndSchemaContexts(ontologyID, repositoryConnection));
         
         String label = null;
         String description = null;
         
         for(BindingSet next : queryResults.getBindingSets())
         {
             if(next.getValue("label") != null)
             {
                 label = next.getValue("label").stringValue();
             }
             else
             {
                 label = objectUri.getLocalName();
             }
             
             if(next.getValue("description") != null)
             {
                 description = next.getValue("description").stringValue();
             }
         }
         
         return new PoddObjectLabelImpl(null, objectUri, label, description);
     }
     
     /**
      * Intentionally not include the inferred ontology IRI here so that we can search for concrete
      * triples specifically.
      * 
      * @param ontologyID
      * @param repositoryConnection
      * @return
      * @throws OpenRDFException
      */
     private URI[] versionAndSchemaContexts(InferredOWLOntologyID ontologyID, RepositoryConnection repositoryConnection)
         throws OpenRDFException
     {
         
         Set<IRI> directImports = getDirectImports(ontologyID, repositoryConnection);
         
         List<URI> results = new ArrayList<URI>(directImports.size() + 2);
         
         results.add(ontologyID.getVersionIRI().toOpenRDFURI());
         
         for(IRI nextDirectImport : directImports)
         {
             results.add(nextDirectImport.toOpenRDFURI());
         }
         
         return results.toArray(new URI[0]);
         
     }
     
     private URI[] versionAndInferredContexts(InferredOWLOntologyID ontologyID)
     {
         if(ontologyID.getInferredOntologyIRI() != null)
         {
             return new URI[] { ontologyID.getVersionIRI().toOpenRDFURI(),
                     ontologyID.getInferredOntologyIRI().toOpenRDFURI() };
         }
         else
         {
             return new URI[] { ontologyID.getVersionIRI().toOpenRDFURI() };
         }
     }
     
     private URI[] versionAndInferredAndSchemaContexts(InferredOWLOntologyID ontologyID,
             RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         Set<IRI> directImports = getDirectImports(ontologyID, repositoryConnection);
         
         List<URI> results = new ArrayList<URI>(directImports.size() + 2);
         
         results.addAll(Arrays.asList(versionAndInferredContexts(ontologyID)));
         
         for(IRI nextDirectImport : directImports)
         {
             results.add(nextDirectImport.toOpenRDFURI());
         }
         
         return results.toArray(new URI[0]);
     }
     
     @Override
     public boolean isPublished(final OWLOntologyID ontologyID, final RepositoryConnection repositoryConnection,
             final URI managementGraph) throws OpenRDFException
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
     public void setPublished(final InferredOWLOntologyID ontologyID, final RepositoryConnection repositoryConnection,
             final URI artifactManagementGraph) throws OpenRDFException
     {
         final URI topObjectIRI = this.getTopObjectIRI(ontologyID, repositoryConnection);
         
         // TODO: Manage the publication status in the artifact management graph if that is going to
         // be easier
         
         // remove previous value for publication status
         repositoryConnection.remove(topObjectIRI, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS, null, ontologyID
                 .getVersionIRI().toOpenRDFURI());
         
         // then insert the publication status as #Published
         repositoryConnection.add(topObjectIRI, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                 PoddRdfConstants.PODDBASE_PUBLISHED, ontologyID.getVersionIRI().toOpenRDFURI());
         
         this.log.info("{} was set as Published", topObjectIRI);
     }
     
     @Override
     public void updateCurrentManagedSchemaOntologyVersion(final InferredOWLOntologyID nextOntologyID,
             final boolean updateCurrent, final RepositoryConnection repositoryConnection, final URI context)
         throws OpenRDFException
     {
         final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
         final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
         // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must
         // be generated for each new inferred ontology generation. For reference though, the
         // version is equal to the ontology IRI in the prototype code. See
         // generateInferredOntologyID method for the corresponding code.
         final URI nextInferredOntologyUri = nextOntologyID.getInferredOntologyIRI().toOpenRDFURI();
         
         // type the ontology
         repositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, context);
         // setup a version number link for this version
         repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri, context);
         
         final List<Statement> currentVersions =
                 Iterations.asList(repositoryConnection.getStatements(nextOntologyUri,
                         PoddRdfConstants.OMV_CURRENT_VERSION, null, false, context));
         
         // If there are no current versions, or we must update the current version, then do it
         // here
         if(currentVersions.isEmpty() || updateCurrent)
         {
             // remove whatever was previously there for the current version marker
             repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null, context);
             
             // then insert the new current version marker
             repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri, context);
         }
         
         // then do a similar process with the inferred axioms ontology
         repositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, context);
         
         // remove whatever was previously there for the current inferred version marker
         repositoryConnection
                 .remove(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null, context);
         
         // link from the ontology IRI to the current inferred axioms ontology version
         repositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 nextInferredOntologyUri, context);
         
         // link from the ontology version IRI to the matching inferred axioms ontology version
         repositoryConnection.add(nextVersionUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION, nextInferredOntologyUri,
                 context);
         
     }
     
     @Override
     public void updateManagedPoddArtifactVersion(final InferredOWLOntologyID nextOntologyID,
             final boolean updateCurrentAndDeletePrevious, final RepositoryConnection repositoryConnection,
             final URI managementGraph) throws OpenRDFException
     {
         final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
         final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
         // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must
         // be generated for each new inferred ontology generation. For reference though, the
         // version is equal to the ontology IRI in the prototype code. See
         // generateInferredOntologyID method for the corresponding code.
         final URI nextInferredOntologyUri = nextOntologyID.getInferredOntologyIRI().toOpenRDFURI();
         
         List<InferredOWLOntologyID> allOntologyVersions =
                 getAllOntologyVersions(nextOntologyID.getOntologyIRI(), repositoryConnection, managementGraph);
         
         // If there are no current versions then the steps are relatively simple
         if(allOntologyVersions.isEmpty())
         {
             // type the ontology
             repositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
             // type the version of the ontology
             repositoryConnection.add(nextVersionUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
             // type the inferred ontology
             repositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
             
             // then insert the new current version marker
             repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri,
                     managementGraph);
             // link from the ontology IRI to the current inferred axioms ontology version
             repositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                     nextInferredOntologyUri, managementGraph);
             
             // setup a version number link for this version
             repositoryConnection
                     .add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri, managementGraph);
             // link from the ontology version IRI to the matching inferred axioms ontology version
             repositoryConnection.add(nextVersionUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                     nextInferredOntologyUri, managementGraph);
         }
         else
         {
             // else, do find and replace to add the version into the system
             
             // type the ontology
             repositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
             // type the version of the ontology
             repositoryConnection.add(nextVersionUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
             // type the inferred ontology
             repositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
             
             // Update the current version and cleanup previous versions
             if(updateCurrentAndDeletePrevious)
             {
                 // remove the content of any contexts that are the object of versionIRI statements
                 final List<Statement> previousVersions =
                         Iterations.asList(repositoryConnection.getStatements(nextOntologyUri,
                                 PoddRdfConstants.OWL_VERSION_IRI, null, true, managementGraph));
                 
                 for(final Statement nextPreviousVersion : previousVersions)
                 {
                     if(nextPreviousVersion.getObject() instanceof URI)
                     {
                         List<Statement> previousInferredVersions =
                                 Iterations.asList(repositoryConnection.getStatements(
                                         (URI)nextPreviousVersion.getObject(),
                                         PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null, false, managementGraph));
                         
                         for(Statement nextInferredVersion : previousInferredVersions)
                         {
                             if(nextInferredVersion.getObject() instanceof URI)
                             {
                                 // clear inferred statements for previous inferred version
                                 repositoryConnection.clear((URI)nextInferredVersion.getObject());
                                 
                                 // remove all references from artifact management graph
                                 repositoryConnection.remove((URI)nextInferredVersion.getObject(), null, null,
                                         managementGraph);
                             }
                             else
                             {
                                 log.error("Found inferred version IRI that was not a URI: {}", nextInferredVersion);
                             }
                         }
                         
                         repositoryConnection.clear((URI)nextPreviousVersion.getObject());
                         repositoryConnection.remove((URI)nextPreviousVersion.getObject(), null, null, managementGraph);
                     }
                     else
                     {
                         log.error("Found version IRI that was not a URI: {}", nextPreviousVersion);
                     }
                 }
                 
                 // remove whatever was previously there for the current version marker
                 repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null,
                         managementGraph);
                 
                 // then insert the new current version marker
                 repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri,
                         managementGraph);
                 
                 // remove whatever was previously there for the current inferred version marker
                 repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null,
                         managementGraph);
                 
                 // link from the ontology IRI to the current inferred axioms ontology version
                 repositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                         nextInferredOntologyUri, managementGraph);
                 
                 // remove previous versionIRI statements if they are no longer needed, before adding
                 // the new version below
                 repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, null, managementGraph);
             }
             
             // always setup a version number link for this version
             repositoryConnection
                     .add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri, managementGraph);
             
             // always setup an inferred axioms ontology version for this version
             repositoryConnection.add(nextVersionUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                     nextInferredOntologyUri, managementGraph);
         }
     }
     
     /**
      * Spike method only.
      * 
      * Retrieves the cardinalities from the schema ontologies, for the given concept and property.
      * 
      * NOTE: does not work on "unqualified" cardinality statements yet.
      * 
      * @param objectUri
      * @param propertyUri
      * @param repositoryConnection
      * @param contexts
      * @return an integer array of size 3.
      * @throws OpenRDFException
      */
     @Override
     public Model getCardinality(final InferredOWLOntologyID artifactID, final URI objectUri, final URI propertyUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final int[] cardinalities = { -1, -1, -1 };
         
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT ?maxCardinality ?minCardinality ?qualifiedCardinality ");
         sb.append(" WHERE { ");
         
         sb.append(" ?poddObject <" + RDF.TYPE.stringValue() + "> ?poddConcept . ");
         sb.append(" ?poddConcept <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
         sb.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#maxQualifiedCardinality> ?maxCardinality } . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#minQualifiedCardinality> ?minCardinality } . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#qualifiedCardinality> ?qualifiedCardinality } . ");
         
         sb.append(" } ");
         
         this.log.info("Created SPARQL {}", sb.toString());
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("poddConcept", objectUri);
         tupleQuery.setBinding("propertyUri", propertyUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery,
                         versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         for(BindingSet binding : queryResults.getBindingSets())
         {
             Value minCardinality = binding.getValue("minCardinality");
             if(minCardinality != null && minCardinality instanceof Literal)
             {
                 cardinalities[0] = ((Literal)minCardinality).intValue();
             }
             
             Value qualifiedCardinality = binding.getValue("qualifiedCardinality");
             if(qualifiedCardinality != null && qualifiedCardinality instanceof Literal)
             {
                 cardinalities[1] = ((Literal)qualifiedCardinality).intValue();
             }
             
             Value maxCardinality = binding.getValue("maxCardinality");
             if(maxCardinality != null && maxCardinality instanceof Literal)
             {
                 cardinalities[2] = ((Literal)maxCardinality).intValue();
             }
         }
         
         // return cardinalities;
         // FIXME: TODO: Implement me
         return new LinkedHashModel();
     }
     
     /*
      * Spike method.
      * 
      * {http://purl.org/podd/ns/poddScience#PlatformType}
      * <http://www.w3.org/2002/07/owl#equivalentClass> {_:genid1636663090} {_:genid1636663090}
      * <http://www.w3.org/2002/07/owl#oneOf> {_:genid72508669}
      * 
      * {_:genid72508669} <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>
      * {http://purl.org/podd/ns/poddScience#Software} {_:genid72508669}
      * <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> {_:genid953844943}
      * 
      * {_:genid953844943} <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>
      * {http://purl.org/podd/ns/poddScience#HardwareSoftware} {_:genid953844943}
      * <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> {_:genid278519207}
      * 
      * {_:genid278519207} <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>
      * {http://purl.org/podd/ns/poddScience#Hardware} {_:genid278519207}
      * <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>
      * {http://www.w3.org/1999/02/22-rdf-syntax-ns#nil}
      * 
      * SELECT ?member WHERE { ?conceptUri :equivalentClass ?b0 . ?b0 :oneOf ?b1 . ?b1 rdf:rest * /
      * rdf:first ?member . }
      */
     @Override
     public List<URI> getAllValidMembers(final InferredOWLOntologyID artifactID, final URI conceptUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         List<URI> results = new ArrayList<URI>();
         
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT ?member ?memberLabel ?memberDescription ");
         sb.append(" WHERE { ");
         
         sb.append(" ?poddConcept <" + OWL.EQUIVALENTCLASS.stringValue() + "> ?x . ");
         sb.append(" ?x <" + OWL.ONEOF.stringValue() + "> ?list . ");
         sb.append(" ?list <" + RDF.REST.stringValue() + ">*/<" + RDF.FIRST.stringValue() + "> ?member . ");
         sb.append(" OPTIONAL { ?member <" + RDFS.LABEL.stringValue() + "> ?memberLabel } . ");
         sb.append(" OPTIONAL { ?member <" + RDFS.COMMENT.stringValue() + "> ?memberDescription } . ");
         sb.append(" } ");
         
         this.log.info("Created SPARQL {}", sb.toString());
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("poddConcept", conceptUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery,
                         versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         for(BindingSet binding : queryResults.getBindingSets())
         {
             Value member = binding.getValue("member");
             results.add((URI)member);
         }
         return results;
     }
     
     /**
      * The result of this method is a Model containing all data required for displaying the details
      * of the object in HTML+RDFa.
      * 
      * The returned graph has the following structure.
      * 
      * poddObject :propertyUri :value
      * 
      * propertyUri RDFS:Label "property label"
      * 
      * value RDFS:Label "value label"
      * 
      * @param objectUri
      * @param repositoryConnection
      * @param contexts
      * @return
      * @throws OpenRDFException
      */
     @Override
     public Model getObjectDetailsForDisplay(final InferredOWLOntologyID artifactID, final URI objectUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final StringBuilder sb = new StringBuilder();
         
         sb.append("CONSTRUCT { ");
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
         sb.append(" ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel . ");
         
         sb.append("} WHERE {");
         
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
         // value may not have a Label
         sb.append(" OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
         
         sb.append(" FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
         sb.append(" FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append(" FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append(" FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
         
         sb.append("}");
         
         final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
         graphQuery.setBinding("poddObject", objectUri);
         
         final Model queryResults =
                 this.executeGraphQuery(graphQuery,
                         versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         return queryResults;
     }
     
     /**
      * Work in progress [25/02/2013]
      * 
      * Attempting to retrieve sufficient triples to display the object_edit page
      * 
      * @param objectUri
      * @param repositoryConnection
      * @param contexts
      * @return
      * @throws OpenRDFException
      */
     @Override
     public Model getObjectDetailsForEdit(final InferredOWLOntologyID artifactID, final URI objectUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final StringBuilder sb = new StringBuilder();
         
         sb.append("CONSTRUCT { ");
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
         sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
         sb.append(" ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel . ");
         
         sb.append("} WHERE {");
         
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
         sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
         // value may not have a Label
         sb.append(" OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
         
         sb.append(" FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
         sb.append(" FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append(" FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append(" FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
         
         sb.append("}");
         
         this.log.info("Created SPARQL {}", sb.toString());
         
         final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
         graphQuery.setBinding("poddObject", objectUri);
         
         final Model queryResults =
                 this.executeGraphQuery(graphQuery,
                         versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         return queryResults;
     }
     
     /**
      * Retrieve a list of <b>asserted</b> properties about the given object. The list is ordered
      * based on property weights and secondarily based on property labels.
      * 
      * Properties RDF:Type, RDFS:Comment and RDFS:Label as well as properties whose values are
      * generic OWL concepts (i.e. OWL:Thing, OWL:Individial, OWL:NamedIndividual, OWL:Class) are not
      * included in the results.
      * 
      * @param artifactID
      *            The artifact to which this object belongs
      * @param objectUri
      *            The object whose properties are sought
      * @param repositoryConnection
      * @return A List containing URIs of sorted properties about the object
      * 
      * @throws OpenRDFException
      */
     @Override
     public List<URI> getWeightedProperties(final InferredOWLOntologyID artifactID, final URI objectUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT DISTINCT ?propertyUri ");
         sb.append(" WHERE { ");
         sb.append(" ?poddObject ?propertyUri ?value . ");
         
         // for ORDER BY
         sb.append(" OPTIONAL { ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel } . ");
         
         // value may not have a Label
         sb.append(" OPTIONAL { ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
         
         // for ORDER BY
         sb.append("OPTIONAL { ?propertyUri <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue() + "> ?weight } . ");
         
         sb.append("FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
         sb.append("FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append("FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append("FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
         
         // Exclude as TYPE, Label (title) and Comment (description) are displayed separately
         sb.append("FILTER (?propertyUri != <" + RDF.TYPE.stringValue() + ">) ");
         sb.append("FILTER (?propertyUri != <" + RDFS.LABEL.stringValue() + ">) ");
         sb.append("FILTER (?propertyUri != <" + RDFS.COMMENT.stringValue() + ">) ");
         
         sb.append(" } ");
         sb.append("  ORDER BY ASC(?weight) ASC(?propertyLabel) ");
         
         this.log.info("Created SPARQL {} with poddObject bound to {}", sb.toString(), objectUri);
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("poddObject", objectUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery, this.versionAndSchemaContexts(artifactID, repositoryConnection));
         
         final List<URI> resultList = new ArrayList<URI>();
         for(BindingSet next : queryResults.getBindingSets())
         {
             final Value property = next.getValue("propertyUri");
             if(property instanceof URI)
             {
                 resultList.add((URI)property);
             }
         }
         
         return resultList;
     }
 }
