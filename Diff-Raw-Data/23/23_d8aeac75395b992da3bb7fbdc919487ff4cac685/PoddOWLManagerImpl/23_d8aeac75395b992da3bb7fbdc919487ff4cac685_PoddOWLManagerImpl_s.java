 /**
  * 
  */
 package com.github.podd.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
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
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.util.RDFInserter;
 import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyChangeException;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLRuntimeException;
 import org.semanticweb.owlapi.profiles.OWLProfile;
 import org.semanticweb.owlapi.reasoner.InferenceType;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
 import org.semanticweb.owlapi.reasoner.TimeOutException;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.semanticweb.owlapi.rio.RioParserImpl;
 import org.semanticweb.owlapi.rio.RioRenderer;
 import org.semanticweb.owlapi.util.InferredAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredOntologyGenerator;
 import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.PublishArtifactException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * Implementation of PoddOWLManager interface.
  * 
  * @author kutila
  * 
  */
 public class PoddOWLManagerImpl implements PoddOWLManager
 {
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     private OWLOntologyManager owlOntologyManager;
     
     private OWLReasonerFactory reasonerFactory;
     
     @Override
     public void cacheSchemaOntology(final InferredOWLOntologyID ontologyID, final RepositoryConnection conn,
             final URI context) throws OpenRDFException, OWLException, IOException, PoddException
     {
         // -- validate input
         if(ontologyID == null || ontologyID.getOntologyIRI() == null)
         {
             throw new NullPointerException("OWLOntology is incomplete");
         }
         // NOTE: if InferredOntologyIRI is null, only the base ontology is cached
         
         final IRI baseOntologyIRI = ontologyID.getOntologyIRI();
         final IRI baseOntologyVersionIRI = ontologyID.getVersionIRI();
         final IRI inferredOntologyIRI = ontologyID.getInferredOntologyIRI();
         
         // -- check if already cached and silently return.
         if(this.owlOntologyManager.contains(baseOntologyIRI)
                 || this.owlOntologyManager.contains(baseOntologyVersionIRI))
         {
             return;
         }
         
         // Only direct imports and first-level indirect imports are identified. This works for the
         // current PODD schema ontologies which have a maximum import depth of 3
         // (PoddPlant -> PoddScience -> PoddBase)
         // TODO: Fix this using a SPARQL which identifies the complete imports closure and sorts
         // them
         // in the proper order for loading.
         final List<InferredOWLOntologyID> imports = this.buildTwoLevelOrderedImportsList(ontologyID, conn, context);
         this.log.info("The schema ontology {} has {} imports.", baseOntologyVersionIRI, imports.size());
         
         // -- load the imported ontologies into the Manager's cache. It is expected that they are
         // already in the Repository
         for(final InferredOWLOntologyID inferredOntologyID : imports)
         {
             final URI contextToLoadFrom = inferredOntologyID.getVersionIRI().toOpenRDFURI();
             this.log.info("About to load {} from context {}", inferredOntologyID, contextToLoadFrom);
             this.parseRDFStatements(conn, contextToLoadFrom);
             
             final URI inferredContextToLoadFrom = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
             if(inferredContextToLoadFrom != null)
             {
                 this.parseRDFStatements(conn, inferredContextToLoadFrom);
             }
         }
         
         // -- load the requested schema ontology (and inferred statements if they exist) into the
         // Manager's cache
         this.parseRDFStatements(conn, baseOntologyVersionIRI.toOpenRDFURI());
         if(inferredOntologyIRI != null)
         {
             this.parseRDFStatements(conn, inferredOntologyIRI.toOpenRDFURI());
         }
     }
     
     private List<InferredOWLOntologyID> buildTwoLevelOrderedImportsList(final InferredOWLOntologyID ontologyID,
             final RepositoryConnection conn, final URI context) throws OpenRDFException
     {
         // -- find ontologies directly imported by this schema ontology
         final List<InferredOWLOntologyID> directImports = this.buildDirectImportsList(ontologyID, conn, context);
         
         // -- find second level imports
         final Set<InferredOWLOntologyID> secondLevelImports =
                 Collections.newSetFromMap(new ConcurrentHashMap<InferredOWLOntologyID, Boolean>());
         for(final InferredOWLOntologyID inferredOntologyID : directImports)
         {
             final List<InferredOWLOntologyID> directImportsList =
                     this.buildDirectImportsList(inferredOntologyID, conn, context);
             secondLevelImports.addAll(directImportsList);
             // TODO - support multiple levels by converting into a recursive implementation
         }
         
         for(final InferredOWLOntologyID secondImport : secondLevelImports)
         {
             if(directImports.contains(secondImport))
             {
                 directImports.remove(secondImport);
             }
         }
         
         final List<InferredOWLOntologyID> orderedResultsList = new ArrayList<InferredOWLOntologyID>();
         // add any indirect imports first
         for(final InferredOWLOntologyID inferredOntologyID : secondLevelImports)
         {
             this.log.debug("adding {} to results", inferredOntologyID);
             orderedResultsList.add(inferredOntologyID);
         }
         for(final InferredOWLOntologyID inferredOntologyID : directImports)
         {
             this.log.debug("adding {} to results", inferredOntologyID);
             orderedResultsList.add(inferredOntologyID);
         }
         return orderedResultsList;
     }
     
     private List<InferredOWLOntologyID> buildDirectImportsList(final InferredOWLOntologyID ontologyID,
             final RepositoryConnection conn, final URI context) throws OpenRDFException
     {
         final List<InferredOWLOntologyID> importsList = new ArrayList<InferredOWLOntologyID>();
         
         final String subject = ontologyID.getOntologyIRI().toQuotedString();
         final String sparqlQuery =
                 "SELECT ?x ?xv ?xiv WHERE { " + subject + " <" + OWL.IMPORTS.stringValue() + "> ?xv ." + "?x <"
                         + PoddRdfConstants.OWL_VERSION_IRI + "> ?xv ." + "?x <"
                         + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION + "> ?xiv ." + " }";
         this.log.info("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(context);
         dataset.addNamedGraph(context);
         query.setDataset(dataset);
         
         final TupleQueryResult queryResults = query.evaluate();
         while(queryResults.hasNext())
         {
             final BindingSet nextResult = queryResults.next();
             final String ontologyIRI = nextResult.getValue("x").stringValue();
             final String versionIRI = nextResult.getValue("xv").stringValue();
             final String inferredIRI = nextResult.getValue("xiv").stringValue();
             
             final InferredOWLOntologyID inferredOntologyID =
                     new InferredOWLOntologyID(IRI.create(ontologyIRI), IRI.create(versionIRI), IRI.create(inferredIRI));
             
             if(!importsList.contains(inferredOntologyID))
             {
                 this.log.debug("Adding {} to imports list", ontologyIRI);
                 importsList.add(inferredOntologyID);
             }
         }
         return importsList;
     }
     
     @Override
     public OWLReasoner createReasoner(final OWLOntology nextOntology)
     {
         return this.reasonerFactory.createReasoner(nextOntology);
     }
     
     @Override
     public InferredOWLOntologyID generateInferredOntologyID(final OWLOntologyID ontologyID)
     {
         if(ontologyID == null || ontologyID.getOntologyIRI() == null || ontologyID.getVersionIRI() == null)
         {
             throw new NullPointerException("OWLOntology is incomplete");
         }
         
         final IRI inferredOntologyIRI = IRI.create(PoddRdfConstants.INFERRED_PREFIX + ontologyID.getVersionIRI());
         
         return new InferredOWLOntologyID(ontologyID.getOntologyIRI(), ontologyID.getVersionIRI(), inferredOntologyIRI);
     }
     
     @Override
     public OWLOntologyID getCurrentVersion(final IRI ontologyIRI)
     {
         throw new RuntimeException("TODO: Implement getCurrentVersion");
     }
     
     @Override
     public InferredOWLOntologyID getCurrentSchemaVersion(final IRI ontologyIRI,
             final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
         UnmanagedSchemaIRIException
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
             
             return new InferredOWLOntologyID(ontologyIRI, IRI.create(nextVersionIRI), IRI.create(nextInferredIRI));
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
             
             return new InferredOWLOntologyID(IRI.create(nextOntologyIRI), ontologyIRI, IRI.create(nextInferredIRI));
         }
         
         throw new UnmanagedSchemaIRIException(ontologyIRI, "This IRI does not refer to a managed ontology");
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
         final Set<OWLProfile> profiles = this.reasonerFactory.getSupportedProfiles();
         if(!profiles.isEmpty())
         {
             if(profiles.size() > 1)
             {
                 this.log.info("Reasoner factory supports {} profiles. Returning one of: {}", profiles.size(), profiles);
             }
             return profiles.iterator().next();
         }
         else
         {
             this.log.warn("Could not find any supported OWL Profiles");
             return null;
         }
     }
     
     @Override
     public List<OWLOntologyID> getVersions(final IRI ontologyIRI)
     {
         throw new RuntimeException("TODO: Implement getVersions");
     }
     
     /**
      * Computes the inferences using the given reasoner, which has previously been setup based on an
      * ontology.
      * 
      * @param nextReasoner
      *            The reasoner to use to compute the inferences.
      * @param inferredOntologyID
      *            The OWLOntologyID to use for the inferred ontology. This must be unique and not
      *            previously used in either the repository or the OWLOntologyManager
      * @return An OWLOntology instance containing the axioms that were inferred from the original
      *         ontology.
      * @throws ReasonerInterruptedException
      * @throws TimeOutException
      * @throws InconsistentOntologyException
      * @throws OWLOntologyCreationException
      * @throws OWLOntologyChangeException
      */
     private OWLOntology computeInferences(final OWLReasoner nextReasoner, final OWLOntologyID inferredOntologyID)
         throws ReasonerInterruptedException, TimeOutException, OWLOntologyCreationException, OWLOntologyChangeException
     {
         nextReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
         
         final List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators =
                 new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
         axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
         axiomGenerators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
         axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
         axiomGenerators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
         axiomGenerators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
         axiomGenerators.add(new InferredInverseObjectPropertiesAxiomGenerator());
         axiomGenerators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
         
         // NOTE: InferredPropertyAssertionGenerator significantly slows down inference computation
         // axiomGenerators.add(new
         // org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());
         
         axiomGenerators.add(new InferredSubClassAxiomGenerator());
         axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());
         axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
         
         final InferredOntologyGenerator iog = new InferredOntologyGenerator(nextReasoner, axiomGenerators);
         final OWLOntology nextInferredAxiomsOntology = this.owlOntologyManager.createOntology(inferredOntologyID);
         
         iog.fillOntology(nextInferredAxiomsOntology.getOWLOntologyManager(), nextInferredAxiomsOntology);
         
         return nextInferredAxiomsOntology;
     }
     
     @Override
     public InferredOWLOntologyID inferStatements(final OWLOntology nextOntology,
             final RepositoryConnection nextRepositoryConnection) throws OWLRuntimeException, OWLException,
         OpenRDFException, IOException
     {
         final InferredOWLOntologyID inferredOntologyID = this.generateInferredOntologyID(nextOntology.getOntologyID());
         final OWLReasoner nextReasoner = this.createReasoner(nextOntology);
         
         final OWLOntology nextInferredAxiomsOntology =
                 this.computeInferences(nextReasoner, inferredOntologyID.getInferredOWLOntologyID());
         
         this.dumpOntologyToRepository(nextInferredAxiomsOntology, nextRepositoryConnection);
         
         return inferredOntologyID;
     }
     
     @Override
     public boolean isPublished(final IRI ontologyIRI)
     {
         throw new RuntimeException("TODO: Implement isPublished(IRI)");
     }
     
     @Override
     public boolean isPublished(final OWLOntologyID ontologyID, final RepositoryConnection repositoryConnection)
         throws OpenRDFException
     {
         if(ontologyID == null || ontologyID.getOntologyIRI() == null || ontologyID.getVersionIRI() == null)
         {
             throw new NullPointerException("OWLOntology is incomplete");
         }
         
         final OWLOntology ontology = this.owlOntologyManager.getOntology(ontologyID);
         if(ontology == null || ontology.isEmpty())
         {
             return false;
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
     public OWLOntology loadOntology(final OWLOntologyDocumentSource owlSource) throws OWLException, IOException,
         PoddException
     {
         OWLOntology nextOntology;
         if(owlSource instanceof RioMemoryTripleSource)
         {
            
            final RioParserImpl owlParser = new RioParserImpl(null);
             
             nextOntology = this.owlOntologyManager.createOntology();
             
             owlParser.parse(owlSource, nextOntology);
         }
         else
         {
             nextOntology = this.owlOntologyManager.loadOntologyFromOntologyDocument(owlSource);
         }
         
         if(nextOntology.isEmpty())
         {
             throw new EmptyOntologyException(nextOntology, "Loaded ontology is empty");
         }
         return nextOntology;
     }
     
     @Override
     public OWLOntologyID parseRDFStatements(final RepositoryConnection conn, final URI... contexts)
         throws OpenRDFException, OWLException, IOException, PoddException
     {
         final RioMemoryTripleSource owlSource =
                 new RioMemoryTripleSource(conn.getStatements(null, null, null, true, contexts));
         
         final RioParserImpl owlParser = new RioParserImpl(null);
         
         final OWLOntology nextOntology = this.owlOntologyManager.createOntology();
         
         if(conn.size(contexts) == 0)
         {
             throw new EmptyOntologyException(nextOntology, "No statements to create an ontology");
         }
         
         owlParser.parse(owlSource, nextOntology);
         if(nextOntology.isEmpty())
         {
             throw new EmptyOntologyException(nextOntology, "Loaded ontology is empty");
         }
         
         return nextOntology.getOntologyID();
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
     public void setCurrentVersion(final OWLOntologyID ontologyID)
     {
         throw new RuntimeException("TODO: Implement setCurrentVersion");
     }
     
     @Override
     public void setOWLOntologyManager(final OWLOntologyManager manager)
     {
         this.owlOntologyManager = manager;
         
     }
     
     @Override
     public InferredOWLOntologyID setPublished(final OWLOntologyID ontologyID) throws PublishArtifactException
     {
         throw new RuntimeException("TODO: Implement setPublished");
     }
     
     @Override
     public void setReasonerFactory(final OWLReasonerFactory reasonerFactory)
     {
         this.reasonerFactory = reasonerFactory;
     }
     
     /**
      * Dump the triples representing a given ontology into a Sesame Repository.
      * 
      * @param nextOntology
      *            The ontology to dump into the repository.
      * @param nextRepositoryConnection
      *            The repository connection to dump the triples into.
      * @throws IOException
      * @throws RepositoryException
      */
     @Override
     public void dumpOntologyToRepository(final OWLOntology nextOntology,
             final RepositoryConnection nextRepositoryConnection, final URI... contexts) throws IOException,
         RepositoryException
     {
         if(nextOntology.getOntologyID().getVersionIRI() == null)
         {
             throw new IllegalArgumentException(
                     "Cannot dump an ontology to repository if it does not have a version IRI");
         }
         
         // Create an RDFHandler that will insert all triples after they are emitted from OWLAPI
         // into a specific context in the Sesame Repository
         final RDFInserter repositoryHandler = new RDFInserter(nextRepositoryConnection);
         RioRenderer renderer;
         
         if(contexts == null || contexts.length == 0)
         {
             repositoryHandler.enforceContext(nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
             // Render the triples out from OWLAPI into a Sesame Repository
             renderer =
                     new RioRenderer(nextOntology, nextOntology.getOWLOntologyManager(), repositoryHandler, null,
                             nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
         }
         else
         {
             repositoryHandler.enforceContext(contexts);
             // Render the triples out from OWLAPI into a Sesame Repository
             renderer =
                     new RioRenderer(nextOntology, nextOntology.getOWLOntologyManager(), repositoryHandler, null,
                             contexts);
         }
         renderer.render();
     }
     
 }
