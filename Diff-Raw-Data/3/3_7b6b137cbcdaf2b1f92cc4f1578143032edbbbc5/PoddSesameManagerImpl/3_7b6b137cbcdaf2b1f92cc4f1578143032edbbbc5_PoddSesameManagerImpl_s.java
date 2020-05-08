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
 import org.openrdf.model.Model;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.EmptyModel;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.LiteralImpl;
 import org.openrdf.model.impl.ValueFactoryImpl;
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
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     public PoddSesameManagerImpl()
     {
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
         final Model results = new LinkedHashModel();
         sparqlQuery.evaluate(new StatementCollector(results));
         
         return results;
     }
     
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
         
         final QueryResultCollector results = new QueryResultCollector();
         QueryResults.report(sparqlQuery.evaluate(), results);
         
         return results;
     }
     
     @Override
     public List<InferredOWLOntologyID> getAllOntologyVersions(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI ontologyManagementGraph) throws OpenRDFException
     {
         // FIXME: This implementation doesn't seem correct. Verify with tests.
         return this.getCurrentVersionsInternal(ontologyIRI, repositoryConnection, ontologyManagementGraph);
     }
     
     @Override
     public List<InferredOWLOntologyID> getAllSchemaOntologyVersions(final RepositoryConnection repositoryConnection,
             final URI schemaManagementGraph) throws OpenRDFException
     {
         final List<InferredOWLOntologyID> returnList = new ArrayList<InferredOWLOntologyID>();
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT ?ontologyIri ?cv ?civ WHERE { ");
         
         sb.append(" ?ontologyIri <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
         sb.append(" ?ontologyIri <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . ");
         sb.append(" ?ontologyIri <" + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . ");
         
         sb.append(" }");
         
         this.log.debug("Generated SPARQL {} ", sb);
         
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         final QueryResultCollector queryResults = this.executeSparqlQuery(query, schemaManagementGraph);
         
         for(final BindingSet nextResult : queryResults.getBindingSets())
         {
             final String nextOntologyIRI = nextResult.getValue("ontologyIri").stringValue();
             final String nextVersionIRI = nextResult.getValue("cv").stringValue();
             final String nextInferredIRI = nextResult.getValue("civ").stringValue();
             
             returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), IRI
                     .create(nextInferredIRI)));
         }
         return returnList;
     }
     
     /**
      * Given a property URI, this method attempts to return all the valid members in the Range of
      * that property.
      * 
      * @param artifactID
      *            The Collection should either belong to this artifact or be imported by it.
      * @param propertyUri
      *            The property whose members are sought.
      * @param repositoryConnection
      * @return A List of URIs representing all valid members of the given Collection, or an empty
      *         list if the property does not have a pre-defined set of possible members.
      * @throws OpenRDFException
      */
     @Override
     public List<URI> getAllValidMembers(final InferredOWLOntologyID artifactID, final URI propertyUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         /*
          * Example: Triples describing PlatformType enumeration consisting of 3 members.
          * 
          * {poddScience:PlatformType} <owl:equivalentClass> {_:genid1636663090}
          * 
          * {_:genid1636663090} <owl:oneOf> {_:genid72508669}
          * 
          * {_:genid72508669} <rdf:first> {poddScience:Software}
          * 
          * {_:genid72508669} <rdf:rest> {_:genid953844943}
          * 
          * {_:genid953844943} <rdf:first> {poddScience:HardwareSoftware}
          * 
          * {_:genid953844943} <rdf:rest> {_:genid278519207}
          * 
          * {_:genid278519207} <rdf:first> {poddScience:Hardware}
          * 
          * {_:genid278519207} <rdf:rest> {rdf:nil}
          */
         
         final List<URI> results = new ArrayList<URI>();
         
         final StringBuilder sb = new StringBuilder();
         sb.append("SELECT ?member WHERE { ");
         sb.append(" ?poddProperty <" + RDFS.RANGE.stringValue() + "> ?poddConcept . ");
         sb.append(" ?poddConcept <" + OWL.EQUIVALENTCLASS.stringValue() + "> ?x . ");
         sb.append(" ?x <" + OWL.ONEOF.stringValue() + "> ?list . ");
         sb.append(" ?list <" + RDF.REST.stringValue() + ">*/<" + RDF.FIRST.stringValue() + "> ?member . ");
         sb.append(" } ");
         
         this.log.debug("Created SPARQL {} with poddProperty bound to {}", sb, propertyUri);
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("poddProperty", propertyUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery,
                         this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         for(final BindingSet binding : queryResults.getBindingSets())
         {
             final Value member = binding.getValue("member");
             results.add((URI)member);
         }
         return results;
     }
     
     /**
      * Constructs a <code>Model</code> containing cardinality statements for a given PODD object and
      * property.
      * 
      * For example, passing in the URIs for a PoddTopObject and property hasLeadInstitution, the
      * output Model will contain a single Statement of the form
      * "{poddBase:hasLeadInstitution} {owl:maxQualifiedCardinality} {1}".
      * 
      * This method currently handles only Qualified Cardinality statements, which are the only type
      * found in PODD schema ontologies at present. However, as the property's value type is ignored,
      * the output is incomplete if a property has more than one type of possible value with
      * different cardinalities.
      * 
      * @param artifactID
      *            The artifact to which the object under consideration belongs
      * @param objectUri
      *            The object under consideration
      * @param propertyUri
      *            The property under consideration
      * @param repositoryConnection
      * @return a Model containing simplified cardinality statements.
      * @throws OpenRDFException
      */
     @Override
     public Model getCardinality(final InferredOWLOntologyID artifactID, final URI objectUri, final URI propertyUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         /*
          * Example of how a qualified cardinality statement appears in RDF triples
          * 
          * {poddBase:PoddTopObject} <rdfs:subClassOf> {_:node17l3l94qux1}
          * 
          * {_:node17l3l94qux1} <rdf:type> {owl:Restriction}
          * 
          * {_:node17l3l94qux1} <owl#onProperty> {poddBase:hasLeadInstitution}
          * 
          * {_:node17l3l94qux1} <owl:qualifiedCardinality> {"1"^^<xsd:nonNegativeInteger>}
          * 
          * {_:node17l3l94qux1} <owl:onDataRange> {xsd:string}
          */
         
         final StringBuilder sb = new StringBuilder();
         
         sb.append("CONSTRUCT { ");
         sb.append(" ?propertyUri <http://www.w3.org/2002/07/owl#maxQualifiedCardinality> ?maxQualifiedCardinality . ");
         sb.append(" ?propertyUri <http://www.w3.org/2002/07/owl#minQualifiedCardinality> ?minQualifiedCardinality . ");
         sb.append(" ?propertyUri <http://www.w3.org/2002/07/owl#qualifiedCardinality> ?qualifiedCardinality . ");
         
         sb.append(" } WHERE { ");
         
         sb.append(" ?poddObject <" + RDF.TYPE.stringValue() + "> ?somePoddConcept . ");
         sb.append(" ?somePoddConcept <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
         sb.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
         sb.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#maxQualifiedCardinality> ?maxQualifiedCardinality } . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#minQualifiedCardinality> ?minQualifiedCardinality } . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#qualifiedCardinality> ?qualifiedCardinality } . ");
         
         sb.append(" } ");
         
         this.log.debug("Created SPARQL {} with propertyUri {} and poddObject {}", sb, propertyUri, objectUri);
         
         final GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
         query.setBinding("poddObject", objectUri);
         query.setBinding("propertyUri", propertyUri);
         
         final Model queryResults =
                 this.executeGraphQuery(query,
                         this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         return queryResults;
     }
     
     @Override
     public URI getCardinalityValue(final InferredOWLOntologyID artifactID, final URI objectUri, final URI propertyUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         return this.getCardinalityValue(objectUri, propertyUri, false, repositoryConnection,
                 this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
     }
     
     @Override
     public URI getCardinalityValue(final URI objectUri, final URI propertyUri, final boolean findFromType,
             final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
     {
         /*
          * Example of how a qualified cardinality statement appears in RDF triples
          * 
          * {poddBase:PoddTopObject} <rdfs:subClassOf> {_:node17l3l94qux1}
          * 
          * {_:node17l3l94qux1} <rdf:type> {owl:Restriction}
          * 
          * {_:node17l3l94qux1} <owl#onProperty> {poddBase:hasLeadInstitution}
          * 
          * {_:node17l3l94qux1} <owl:qualifiedCardinality> {"1"^^<xsd:nonNegativeInteger>}
          * 
          * {_:node17l3l94qux1} <owl:onDataRange> {xsd:string}
          */
         
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT ?qualifiedCardinality ?minQualifiedCardinality ?maxQualifiedCardinality ");
         sb.append(" WHERE { ");
         
         if(!findFromType)
         {
             sb.append(" ?poddObject <" + RDF.TYPE.stringValue() + "> ?somePoddConcept . ");
         }
         
         sb.append(" ?somePoddConcept <" + RDFS.SUBCLASSOF.stringValue() + ">+ ?x . ");
         sb.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
         sb.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#maxQualifiedCardinality> ?maxQualifiedCardinality } . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#minQualifiedCardinality> ?minQualifiedCardinality } . ");
         sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#qualifiedCardinality> ?qualifiedCardinality } . ");
         
         sb.append(" } ");
         
         this.log.debug("Created SPARQL {} with propertyUri {} and poddObject {}", sb, propertyUri, objectUri);
         
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         query.setBinding("propertyUri", propertyUri);
         if(findFromType)
         {
             query.setBinding("somePoddConcept", objectUri);
         }
         else
         {
             query.setBinding("poddObject", objectUri);
         }
         
         final QueryResultCollector queryResults = this.executeSparqlQuery(query, contexts);
         
         for(final BindingSet next : queryResults.getBindingSets())
         {
             final Value qualifiedCardinalityValue = next.getValue("qualifiedCardinality");
             if(qualifiedCardinalityValue != null)
             {
                 return PoddRdfConstants.PODD_BASE_CARDINALITY_EXACTLY_ONE;
             }
             
             int minCardinality = -1;
             int maxCardinality = -1;
             
             final Value minCardinalityValue = next.getValue("minQualifiedCardinality");
             if(minCardinalityValue != null && minCardinalityValue instanceof LiteralImpl)
             {
                 minCardinality = ((LiteralImpl)minCardinalityValue).intValue();
             }
             
             final Value maxCardinalityValue = next.getValue("maxQualifiedCardinality");
             if(maxCardinalityValue != null && maxCardinalityValue instanceof LiteralImpl)
             {
                 maxCardinality = ((LiteralImpl)maxCardinalityValue).intValue();
             }
             
             if(maxCardinality == 1 && minCardinality < 1)
             {
                 return PoddRdfConstants.PODD_BASE_CARDINALITY_ZERO_OR_ONE;
             }
             else if(minCardinality == 1 && maxCardinality != 1)
             {
                 return PoddRdfConstants.PODD_BASE_CARDINALITY_ONE_OR_MANY;
             }
             else
             // default rule
             {
                 return PoddRdfConstants.PODD_BASE_CARDINALITY_ZERO_OR_MANY;
             }
         }
         
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
         if(ontologyIRI.toString().startsWith("_:"))
         {
             throw new UnmanagedArtifactIRIException(ontologyIRI,
                     "This IRI does not refer to a managed ontology (blank node)");
         }
         
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
      * If the input ontologyIRI is either an Ontology IRI or Version IRI for a managed ontology, the
      * ID of the current version of this ontology is returned.
      * 
      * @param ontologyIRI
      *            Either an Ontology IRI or Version IRI for which the current version is requested.
      * @param repositoryConnection
      * @param managementGraph
      * @return A List of InferredOWLOntologyIDs. If the ontology is managed, the list will contain
      *         one entry for its current version. Otherwise the list will be empty.
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
         final StringBuilder sb1 = new StringBuilder();
         sb1.append("SELECT ?cv ?civ WHERE { ");
         sb1.append(" ?ontologyIri <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
         sb1.append(" ?ontologyIri <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . ");
         sb1.append(" ?ontologyIri <" + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . ");
         
         sb1.append(" }");
         
         this.log.debug("Generated SPARQL {} with ontologyIri bound to {}", sb1, ontologyIRI);
         
         final TupleQuery query1 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb1.toString());
         query1.setBinding("ontologyIri", ontologyIRI.toOpenRDFURI());
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
         final StringBuilder sb2 = new StringBuilder();
         sb2.append("SELECT ?x ?cv ?civ WHERE { ");
         sb2.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
         sb2.append(" ?x <" + OWL.VERSIONIRI.stringValue() + "> ?versionIri . ");
         sb2.append(" ?x <" + OWL.VERSIONIRI.stringValue() + "> ?cv . ");
         sb2.append(" ?x <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . ");
         sb2.append(" ?x <" + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . ");
         sb2.append(" }");
         
         this.log.debug("Generated SPARQL {} with versionIri bound to {}", sb2, ontologyIRI);
         
         final TupleQuery query2 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb2.toString());
         query2.setBinding("versionIri", ontologyIRI.toOpenRDFURI());
         query2.setDataset(dataset);
         
         final TupleQueryResult queryResults2 = query2.evaluate();
         
         final QueryResultCollector nextResults2 = new QueryResultCollector();
         QueryResults.report(queryResults2, nextResults2);
         
         for(final BindingSet nextResult : nextResults2.getBindingSets())
         {
             final String nextOntologyIRI = nextResult.getValue("x").stringValue();
             final String nextVersionIRI = nextResult.getValue("cv").stringValue();
             final String nextInferredIRI = nextResult.getValue("civ").stringValue();
             
             returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), IRI
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
         return this.getDirectImports(repositoryConnection, this.versionAndInferredContexts(ontologyID));
     }
     
     @Override
     public Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI... contexts)
         throws OpenRDFException
     {
         final String sparqlQuery = "SELECT ?x WHERE { ?y <" + OWL.IMPORTS.stringValue() + "> ?x ." + " }";
         this.log.debug("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         for(final URI nextContext : contexts)
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
     public Model getObjectData(final InferredOWLOntologyID artifactID, final URI objectUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         if(objectUri == null)
         {
             return new LinkedHashModel();
         }
         
         final StringBuilder sb = new StringBuilder();
         
         sb.append("CONSTRUCT { ");
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" ?parent ?somePropertyUri ?poddObject . ");
         
         sb.append("} WHERE {");
         
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" ?parent ?somePropertyUri ?poddObject . ");
         
         sb.append("}");
         
         final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
         graphQuery.setBinding("poddObject", objectUri);
         
         final Model queryResults =
                 this.executeGraphQuery(graphQuery, this.versionAndSchemaContexts(artifactID, repositoryConnection));
         
         return queryResults;
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
         
         sb.append(" FILTER NOT EXISTS { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                 + "> true } ");
         
         sb.append(" FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
         sb.append(" FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append(" FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append(" FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
         
         sb.append("}");
         
         final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
         graphQuery.setBinding("poddObject", objectUri);
         
         final Model queryResults =
                 this.executeGraphQuery(graphQuery,
                         this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         return queryResults;
     }
     
     /**
      * This method returns a {@link Model} containing sufficient triples to construct the HTML page
      * for editing an object.
      * 
      * @param objectUri
      * @param repositoryConnection
      * @param contexts
      * @return A Model containing necessary triples
      * @throws OpenRDFException
      */
     @Override
     public Model getObjectDetailsForEdit(final InferredOWLOntologyID artifactID, final URI objectUri,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final Model resultModel = new LinkedHashModel();
         
         // --- add object type
         final List<URI> objectTypes = this.getObjectTypes(artifactID, objectUri, repositoryConnection);
         if(objectTypes != null && objectTypes.size() > 0)
         {
             resultModel.add(objectUri, RDF.TYPE, objectTypes.get(0));
         }
         
         // --- add displayable property values, details of properties, and value labels if present
         final StringBuilder sb = new StringBuilder();
         
         sb.append("CONSTRUCT { ");
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
         sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
         sb.append(" ?propertyUri <" + PoddRdfConstants.PODD_BASE_DISPLAY_TYPE.stringValue()
                 + "> ?propertyDisplayType . ");
         sb.append(" ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel . ");
         
         sb.append("} WHERE {");
         
         sb.append(" ?poddObject ?propertyUri ?value . ");
         sb.append(" OPTIONAL {?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType } . ");
         
         // property may not have a displayType
         sb.append(" OPTIONAL {?propertyUri <" + PoddRdfConstants.PODD_BASE_DISPLAY_TYPE.stringValue()
                 + "> ?propertyDisplayType } . ");
         
         // property may not have a Label
         sb.append(" OPTIONAL {?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel } . ");
         
         // value may not have a Label
         sb.append(" OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
         
         // avoid non-displayable properties (e.g. PoddInternalObject which is an "abstract" concept)
         sb.append(" FILTER NOT EXISTS { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                 + "> true } ");
         
         // avoid, since we add object type and label to the Model up front
         sb.append(" FILTER (?propertyUri != <" + RDF.TYPE.stringValue() + ">) ");
         
         sb.append(" FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
         sb.append(" FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append(" FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append(" FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
         
         sb.append("}");
         
         final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
         graphQuery.setBinding("poddObject", objectUri);
         
         this.log.debug("Created SPARQL {} \n   with poddObject bound to {}", sb, objectUri);
         
         final Model queryResults =
                 this.executeGraphQuery(graphQuery,
                         this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
         
         resultModel.addAll(queryResults);
         
         final boolean excludeContainsProperties = true;
         final List<URI> allProperties =
                 this.getWeightedProperties(artifactID, objectUri, excludeContainsProperties, repositoryConnection);
         for(int i = 0; i < allProperties.size(); i++)
         {
             final URI propertyUri = allProperties.get(i);
             // ---- add property displaying order as "weight"
             resultModel.add(propertyUri, PoddRdfConstants.PODD_BASE_WEIGHT, ValueFactoryImpl.getInstance()
                     .createLiteral(i));
             
             // --- add cardinality for each displayable property
             final URI cardinalityValue =
                     this.getCardinalityValue(artifactID, objectUri, propertyUri, repositoryConnection);
             if(cardinalityValue != null)
             {
                 resultModel.add(propertyUri, PoddRdfConstants.PODD_BASE_HAS_CARDINALITY, cardinalityValue);
             }
             
             // --- for 'drop-down' type properties, add all possible options into Model
             final Model filter = resultModel.filter(propertyUri, PoddRdfConstants.PODD_BASE_DISPLAY_TYPE, null);
             if(filter.size() == 1 && PoddRdfConstants.PODD_BASE_DISPLAY_TYPE_DROPDOWN.equals(filter.objectURI()))
             {
                 final URI propertyValue = resultModel.filter(objectUri, propertyUri, null).objectURI();
                 
                 final List<URI> valueTypes = this.getObjectTypes(artifactID, propertyValue, repositoryConnection);
                 if(valueTypes.size() > 0)
                 {
                     final Model allPossibleValues =
                             this.searchOntologyLabels("", artifactID, 1000, 0, repositoryConnection, valueTypes.get(0));
                     
                     for(final Statement s : allPossibleValues)
                     {
                         resultModel.add(propertyUri, PoddRdfConstants.PODD_BASE_ALLOWED_VALUE, s.getSubject());
                         resultModel.add(s);
                     }
                 }
             }
         }
         
         return resultModel;
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
         
         this.log.debug("Created SPARQL {} with objectUri bound to {}", sb, objectUri);
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("objectUri", objectUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery,
                         this.versionAndInferredAndSchemaContexts(ontologyID, repositoryConnection));
         
         String label = null;
         String description = null;
         
         for(final BindingSet next : queryResults.getBindingSets())
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
     
     @Override
     public Model getObjectTypeMetadata(final URI objectType, final boolean includeDoNotDisplayProperties,
             final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
     {
         final Model results = new LinkedHashModel();
         if(objectType == null)
         {
             return results;
         }
         
         // - find all Properties and their ranges
         /*
          * NOTE: This SPARQL query only finds properties defined as OWL restrictions in the given
          * Object Type and its ancestors.
          */
         final StringBuilder sb1 = new StringBuilder();
         
         sb1.append("CONSTRUCT { ");
         sb1.append(" ?poddObject <" + RDF.TYPE.stringValue() + "> <" + OWL.CLASS.stringValue() + "> . ");
         sb1.append(" ?poddObject <" + RDFS.SUBCLASSOF.stringValue() + ">+ ?x . ");
         sb1.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
         sb1.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
         sb1.append(" ?x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass . ");
         sb1.append(" ?x <http://www.w3.org/2002/07/owl#onClass> ?owlClass . ");
         sb1.append(" ?x <http://www.w3.org/2002/07/owl#onDataRange> ?valueRange . ");
         
         sb1.append("} WHERE {");
         
         sb1.append(" ?poddObject <" + RDFS.SUBCLASSOF.stringValue() + ">+ ?x . ");
         sb1.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
         sb1.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
         sb1.append(" OPTIONAL { ?x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass } . ");
         sb1.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#onClass> ?owlClass } . ");
         sb1.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#onDataRange> ?valueRange } . ");
         
         if(!includeDoNotDisplayProperties)
         {
             sb1.append(" FILTER NOT EXISTS { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                     + "> true . } ");
         }
         
         sb1.append("}");
         
         final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb1.toString());
         graphQuery.setBinding("poddObject", objectType);
         
         this.log.info("Created SPARQL {} \n   with poddObject bound to {}", sb1, objectType);
         
         final Model queryResults1 = this.executeGraphQuery(graphQuery, contexts);
         results.addAll(queryResults1);
         
         // -- for each property, get meta-data about it
         final Set<Value> properties = queryResults1.filter(null, OWL.ONPROPERTY, null).objects();
         for(final Value property : properties)
         {
             if(property instanceof URI)
             {
                 // - find property: type (e.g. object/datatype/annotation), label, display-type,
                 // weight
                 final StringBuilder sb2 = new StringBuilder();
                 
                 sb2.append("CONSTRUCT { ");
                 sb2.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
                 sb2.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
                 sb2.append(" ?propertyUri <" + PoddRdfConstants.PODD_BASE_DISPLAY_TYPE.stringValue()
                         + "> ?propertyDisplayType . ");
                 sb2.append(" ?propertyUri <" + PoddRdfConstants.PODD_BASE_WEIGHT.stringValue() + "> ?propertyWeight . ");
                 
                 sb2.append(" ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                         + "> ?propertyDoNotDisplay . ");
                 
                 sb2.append("} WHERE {");
                 
                 sb2.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
                 
                 sb2.append(" OPTIONAL {?propertyUri <" + PoddRdfConstants.PODD_BASE_DISPLAY_TYPE.stringValue()
                         + "> ?propertyDisplayType . }  ");
                 
                 sb2.append(" OPTIONAL {?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . } ");
                 
                 sb2.append(" OPTIONAL {?propertyUri <" + PoddRdfConstants.PODD_BASE_WEIGHT.stringValue()
                         + "> ?propertyWeight . } ");
                 
                 if(includeDoNotDisplayProperties)
                 {
                     sb2.append(" OPTIONAL { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                             + "> ?propertyDoNotDisplay . } ");
                 }
                 
                 sb2.append("}");
                 
                 final GraphQuery graphQuery2 =
                         repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb2.toString());
                 graphQuery2.setBinding("propertyUri", property);
                 
                 this.log.info("Created SPARQL {} \n   with propertyUri bound to {}", sb2, property);
                 
                 final Model queryResults2 = this.executeGraphQuery(graphQuery2, contexts);
                 results.addAll(queryResults2);
                 
                 // - add cardinality value
                 final URI cardinalityValue =
                         this.getCardinalityValue(objectType, (URI)property, true, repositoryConnection, contexts);
                 if(cardinalityValue != null)
                 {
                     results.add((URI)property, PoddRdfConstants.PODD_BASE_HAS_CARDINALITY, cardinalityValue);
                 }
                 
             }
         }
         
         return results;
     }
     
     /**
      * Retrieves the most specific types of the given object as a List of URIs.
      * 
      * The contexts searched in are, the given ongology's asserted and inferred graphs as well as
      * their imported schema ontology graphs.
      * 
      * This method depends on the poddBase:doNotDisplay annotation to filter out unwanted
      * super-types.
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
         sb.append("SELECT DISTINCT ?poddTypeUri ");
         sb.append(" WHERE { ");
         sb.append(" ?objectUri <" + RDF.TYPE + "> ?poddTypeUri . ");
         
         sb.append(" FILTER NOT EXISTS { ?poddTypeUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                 + "> true } ");
         sb.append(" FILTER isIRI(?poddTypeUri) ");
         
         // filter out TYPE statements for OWL:Thing, OWL:Individual, OWL:NamedIndividual & OWL:Class
         sb.append("FILTER (?poddTypeUri != <" + OWL.THING.stringValue() + ">) ");
         sb.append("FILTER (?poddTypeUri != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append("FILTER (?poddTypeUri != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append("FILTER (?poddTypeUri != <" + OWL.CLASS.stringValue() + ">) ");
         
         sb.append(" }");
         
         this.log.debug("Created SPARQL {} with objectUri bound to {}", sb, objectUri);
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("objectUri", objectUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery,
                         this.versionAndInferredAndSchemaContexts(ontologyID, repositoryConnection));
         
         final List<URI> results = new ArrayList<URI>(queryResults.getBindingSets().size());
         
         for(final BindingSet next : queryResults.getBindingSets())
         {
             results.add((URI)next.getValue("poddTypeUri"));
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
         
         this.log.debug("Generated SPARQL {}", sb);
         
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
                 "SELECT ?nextOntology WHERE { ?nextOntology <" + RDF.TYPE + "> <" + OWL.ONTOLOGY.stringValue()
                         + ">  . " + " ?nextOntology <" + PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT + "> ?y " + " }";
         this.log.debug("Generated SPARQL {}", sparqlQuery);
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
             Value nextOntology = nextResult.getValue("nextOntology");
             if(nextOntology instanceof URI)
             {
                 ontologyIRI = IRI.create(nextOntology.stringValue());
             }
             else
             {
                 ontologyIRI = IRI.create("_:" + nextOntology.stringValue());
             }
         }
         return ontologyIRI;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddSesameManager#getOntologyVersion(org.semanticweb.owlapi.model.IRI,
      * org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)
      */
     @Override
     public InferredOWLOntologyID getOntologyVersion(final IRI versionIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
     {
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(managementGraph);
         
         // see if the given IRI exists as a version IRI
         final StringBuilder sb2 = new StringBuilder();
         sb2.append("SELECT ?ontologyIri ?inferredIri WHERE { ");
         sb2.append(" ?ontologyIri <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
         sb2.append(" ?ontologyIri <" + OWL.VERSIONIRI.stringValue() + "> ?versionIri . ");
         sb2.append(" ?versionIri <" + PoddRdfConstants.PODD_BASE_INFERRED_VERSION.stringValue() + "> ?inferredIri . ");
         sb2.append(" }");
         
         this.log.debug("Generated SPARQL {} with versionIri bound to <{}>", sb2, versionIRI);
         
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb2.toString());
         query.setBinding("versionIri", versionIRI.toOpenRDFURI());
         query.setDataset(dataset);
         
         final TupleQueryResult queryResults = query.evaluate();
         
         final QueryResultCollector resultsCollector = new QueryResultCollector();
         QueryResults.report(queryResults, resultsCollector);
         
         for(final BindingSet nextResult : resultsCollector.getBindingSets())
         {
             final String nextOntologyIRI = nextResult.getValue("ontologyIri").stringValue();
             final String nextInferredIRI = nextResult.getValue("inferredIri").stringValue();
             
             // return the first solution since there should only be only one result
             return new InferredOWLOntologyID(IRI.create(nextOntologyIRI), versionIRI, IRI.create(nextInferredIRI));
         }
         
         // could not find given IRI as a version IRI
         return null;
     }
     
     @Override
     public InferredOWLOntologyID getSchemaVersion(final IRI schemaVersionIRI,
             final RepositoryConnection repositoryConnection, final URI schemaManagementGraph) throws OpenRDFException,
         UnmanagedSchemaIRIException
     {
         final InferredOWLOntologyID ontologyID =
                 this.getOntologyVersion(schemaVersionIRI, repositoryConnection, schemaManagementGraph);
         if(ontologyID != null)
         {
             return ontologyID;
         }
         else
         {
             // not a version IRI, return the current schema version
             return this.getCurrentSchemaVersion(schemaVersionIRI, repositoryConnection, schemaManagementGraph);
         }
     }
     
     /**
      * Internal helper method to retrieve the Top-Object IRI for a given ontology.
      * 
      */
     @Override
     public URI getTopObjectIRI(final InferredOWLOntologyID ontologyIRI, final RepositoryConnection repositoryConnection)
         throws OpenRDFException
     {
         final List<URI> results = this.getTopObjects(ontologyIRI, repositoryConnection);
         
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
             this.log.warn("More than one top object found");
             return results.get(0);
         }
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
         
         sb.append(" ?artifactUri <" + PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT.stringValue() + "> ?topObjectUri . \n");
         
         sb.append(" }");
         
         final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         query.setBinding("artifactUri", ontologyID.getOntologyIRI().toOpenRDFURI());
         
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(query, this.versionAndInferredContexts(ontologyID));
         
         final List<URI> topObjectList = new ArrayList<URI>();
         
         for(final BindingSet next : queryResults.getBindingSets())
         {
             final URI pred = (URI)next.getValue("topObjectUri");
             
             topObjectList.add(pred);
         }
         
         return topObjectList;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddSesameManager#getWeightedProperties(com.github.podd.utils.
      * InferredOWLOntologyID, org.openrdf.model.URI, boolean,
      * org.openrdf.repository.RepositoryConnection)
      */
     @Override
     public List<URI> getWeightedProperties(final InferredOWLOntologyID artifactID, final URI objectUri,
             final boolean excludeContainsProperties, final RepositoryConnection repositoryConnection)
         throws OpenRDFException
     {
         final StringBuilder sb = new StringBuilder();
         
         sb.append("SELECT DISTINCT ?propertyUri ");
         sb.append(" WHERE { ");
         sb.append(" ?poddObject ?propertyUri ?value . ");
         
         // for ORDER BY
         sb.append(" OPTIONAL { ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel } . ");
         
         // for ORDER BY
         sb.append("OPTIONAL { ?propertyUri <" + PoddRdfConstants.PODD_BASE_WEIGHT.stringValue() + "> ?weight } . ");
         
         sb.append("FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
         sb.append("FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
         sb.append("FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
         sb.append("FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
         
         // Exclude as TYPE, Label (title) and Comment (description) are displayed separately
         sb.append("FILTER (?propertyUri != <" + RDF.TYPE.stringValue() + ">) ");
         sb.append("FILTER (?propertyUri != <" + RDFS.LABEL.stringValue() + ">) ");
         sb.append("FILTER (?propertyUri != <" + RDFS.COMMENT.stringValue() + ">) ");
         
         if(excludeContainsProperties)
         {
             sb.append("FILTER NOT EXISTS { ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                     + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> } ");
         }
         
         sb.append(" FILTER NOT EXISTS { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                 + "> true } ");
         
         sb.append(" } ");
         sb.append("  ORDER BY ASC(?weight) ASC(?propertyLabel) ");
         
         this.log.debug("Created SPARQL {} with poddObject bound to {}", sb, objectUri);
         
         final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
         tupleQuery.setBinding("poddObject", objectUri);
         final QueryResultCollector queryResults =
                 this.executeSparqlQuery(tupleQuery, this.versionAndSchemaContexts(artifactID, repositoryConnection));
         
         final List<URI> resultList = new ArrayList<URI>();
         for(final BindingSet next : queryResults.getBindingSets())
         {
             final Value property = next.getValue("propertyUri");
             if(property instanceof URI)
             {
                 resultList.add((URI)property);
             }
         }
         
         return resultList;
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
                         + PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT.stringValue() + "> ?top ." + " ?top <"
                         + PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS.stringValue() + "> <"
                         + PoddRdfConstants.PODD_BASE_PUBLISHED.stringValue() + ">" + " }";
         
         this.log.debug("Generated SPARQL {}", sparqlQuery);
         
         final BooleanQuery booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         // Create a dataset to specify the contexts
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(artifactGraphUri);
         dataset.addNamedGraph(artifactGraphUri);
         booleanQuery.setDataset(dataset);
         
         return booleanQuery.evaluate();
     }
     
     @Override
     public Model searchOntologyLabels(final String searchTerm, final InferredOWLOntologyID artifactID, final int limit,
             final int offset, final RepositoryConnection repositoryConnection, final URI... searchTypes)
         throws OpenRDFException
     {
         final URI[] contexts = this.versionAndSchemaContexts(artifactID, repositoryConnection);
         return this.searchOntologyLabels(searchTerm, searchTypes, limit, offset, repositoryConnection, contexts);
     }
     
     @Override
     public Model searchOntologyLabels(final String searchTerm, final URI[] searchTypes, final int limit,
             final int offset, final RepositoryConnection repositoryConnection, final URI... contexts)
         throws OpenRDFException
     {
         
         final StringBuilder sb = new StringBuilder();
         
         sb.append("CONSTRUCT { ");
         sb.append(" ?uri <" + RDFS.LABEL.stringValue() + "> ?label ");
         sb.append(" } WHERE { ");
         
         // limit the "types" of objects to search for
         if(searchTypes != null)
         {
             for(final URI type : searchTypes)
             {
                 sb.append(" ?uri <" + RDF.TYPE.stringValue() + "> <" + type.stringValue() + "> . ");
             }
         }
         
         sb.append(" ?uri <" + RDFS.LABEL.stringValue() + "> ?label . ");
         
         // filter for "searchTerm" in label
         sb.append(" FILTER(CONTAINS( LCASE(?label) , LCASE(?searchTerm) )) ");
         
         sb.append(" } LIMIT ");
         sb.append(limit);
         
         sb.append(" OFFSET ");
         sb.append(offset);
         
         final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
         graphQuery.setBinding("searchTerm", ValueFactoryImpl.getInstance().createLiteral(searchTerm));
         
         this.log.debug("Created SPARQL {} with searchTerm bound to '{}' ", sb, searchTerm);
         
         final Model queryResults = this.executeGraphQuery(graphQuery, contexts);
         
         return queryResults;
     }
     
     @Override
     public void setPublished(final InferredOWLOntologyID ontologyID, final RepositoryConnection repositoryConnection,
             final URI artifactManagementGraph) throws OpenRDFException
     {
         final URI topObjectIRI = this.getTopObjectIRI(ontologyID, repositoryConnection);
         
         // TODO: Manage the publication status in the artifact management graph if that is going to
         // be easier
         
         // remove previous value for publication status
         repositoryConnection.remove(topObjectIRI, PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, null, ontologyID
                 .getVersionIRI().toOpenRDFURI());
         
         // then insert the publication status as #Published
         repositoryConnection.add(topObjectIRI, PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS,
                 PoddRdfConstants.PODD_BASE_PUBLISHED, ontologyID.getVersionIRI().toOpenRDFURI());
         
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
         
         final List<InferredOWLOntologyID> allOntologyVersions =
                 this.getAllOntologyVersions(nextOntologyID.getOntologyIRI(), repositoryConnection, managementGraph);
         
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
                         final List<Statement> previousInferredVersions =
                                 Iterations.asList(repositoryConnection.getStatements(
                                         (URI)nextPreviousVersion.getObject(),
                                         PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null, false, managementGraph));
                         
                         for(final Statement nextInferredVersion : previousInferredVersions)
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
                                 this.log.error("Found inferred version IRI that was not a URI: {}", nextInferredVersion);
                             }
                         }
                         
                         repositoryConnection.clear((URI)nextPreviousVersion.getObject());
                         repositoryConnection.remove((URI)nextPreviousVersion.getObject(), null, null, managementGraph);
                     }
                     else
                     {
                         this.log.error("Found version IRI that was not a URI: {}", nextPreviousVersion);
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
     
     private URI[] versionAndInferredAndSchemaContexts(final InferredOWLOntologyID ontologyID,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         final Set<IRI> directImports = this.getDirectImports(ontologyID, repositoryConnection);
         
         final List<URI> results = new ArrayList<URI>(directImports.size() + 2);
         
         results.addAll(Arrays.asList(this.versionAndInferredContexts(ontologyID)));
         
         for(final IRI nextDirectImport : directImports)
         {
             results.add(nextDirectImport.toOpenRDFURI());
         }
         
         return results.toArray(new URI[0]);
     }
     
     private URI[] versionAndInferredContexts(final InferredOWLOntologyID ontologyID)
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
     
     /**
      * Intentionally not include the inferred ontology IRI here so that we can search for concrete
      * triples specifically.
      * 
      * @param ontologyID
      * @param repositoryConnection
      * @return
      * @throws OpenRDFException
      */
     private URI[] versionAndSchemaContexts(final InferredOWLOntologyID ontologyID,
             final RepositoryConnection repositoryConnection) throws OpenRDFException
     {
         
         final Set<IRI> directImports = this.getDirectImports(ontologyID, repositoryConnection);
         
         final List<URI> results = new ArrayList<URI>(directImports.size() + 2);
         
         results.add(ontologyID.getVersionIRI().toOpenRDFURI());
         
         for(final IRI nextDirectImport : directImports)
         {
             results.add(nextDirectImport.toOpenRDFURI());
         }
         
         return results.toArray(new URI[0]);
         
     }
     
 }
