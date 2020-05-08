 package au.csiro.eis.ontology.resource;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.client.ClientProtocolException;
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.io.FileDocumentSource;
 import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
 import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
 import org.semanticweb.owlapi.model.AddAxiom;
 import org.semanticweb.owlapi.model.AddImport;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAnnotation;
 import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLDataProperty;
 import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLLiteral;
 import org.semanticweb.owlapi.model.OWLNamedIndividual;
 import org.semanticweb.owlapi.model.OWLObjectProperty;
 import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyChange;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyStorageException;
 import org.semanticweb.owlapi.model.PrefixManager;
 import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
 import org.semanticweb.owlapi.reasoner.InferenceType;
 import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
 import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
 import org.semanticweb.owlapi.util.DefaultPrefixManager;
 import org.semanticweb.owlapi.util.QNameShortFormProvider;
 import org.semanticweb.owlapi.util.SimpleIRIMapper;
 import org.semanticweb.owlapi.util.SimpleShortFormProvider;
 import org.semanticweb.owlapi.vocab.OWL2Datatype;
 
 import au.csiro.eis.ontology.beans.OwlDataPropertyBean;
 import au.csiro.eis.ontology.beans.OwlIndividualBean;
 import au.csiro.eis.ontology.beans.OwlLiteralBean;
 import au.csiro.eis.ontology.beans.OwlObjectPropertyBean;
 import au.csiro.eis.ontology.beans.SparqlSelectResultBean;
 import au.csiro.eis.ontology.beans.SparqlSelectResultSetBean;
 import au.csiro.eis.ontology.beans.config.OntologyConfig;
 import au.csiro.eis.ontology.beans.config.OntologyConfigMapping;
 import au.csiro.eis.ontology.exception.OntologyInitException;
 import au.csiro.eis.ontology.openrdf.sesame.tools.SesameHttpUtils;
 import au.csiro.eis.ontology.resource.jena.JenaModelManager;
 import au.csiro.eis.ontology.spin.tools.SpinModelManager;
 
 import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
 import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 
 public class CepOntologyManager {
 	OWLOntologyManager ontologyMgr ;	
 	OntologyConfig[] config;
 	Map<String, OntologyConfigMapping> prefixIndex;
 	
 	OWLOntology defaultOntology = null;
 	OWLReasoner reasoner = null;
 	QNameShortFormProvider qnameProvider = new QNameShortFormProvider();	
 	SimpleShortFormProvider shortFormProvider = new SimpleShortFormProvider();
 	Set<OWLOntology> ontologies;
 
 	OWLDataFactory dataFactory;
 	PrefixManager pm;
 	String baseUri = null;
 	
 	String userIri = null;
 	String tripleStoreEndpoint = null;
 	
 	boolean isSilent = false;
 	boolean isUserGraphUsed  = false;
 	
 	SpinModelManager spinMgr;
 	JenaModelManager jenaModelMgr;
 	
 	private boolean isSpinModelManagerInit;
 	
 	private boolean isJenaModelManagerInit;
 
 	String rulesUri = null;
 	
 	
 	public CepOntologyManager(OntologyConfig[] config, boolean isSilent) {
 		this.config = config;
 		
 		this.isSilent = isSilent;
 		prefixIndex = new HashMap<String, OntologyConfigMapping>();
 //		spinMgr = null;
 		spinMgr = null;
 		
 		jenaModelMgr = new JenaModelManager();
 
 	}
 	
 	
 	/**
 	 * UserIri is used to load user named graph from remote triple store (tripleStoreEndpoint)
 	 * 
 	 * @param config
 	 * @param userIri
 	 * @param tripleStoreEndpoint
 	 * @param isSilent
 	 */
 	public CepOntologyManager(OntologyConfig[] config, String userIri, String rulesUri, String tripleStoreEndpoint, boolean isSilent) {
 		this.config = config;
 		/*
 		this.ssnOntologyPath = ssnOntologyPath;
 		this.dulOntologyPath = dulOntologyPath;
 		//this.unitsOntologyPath = unitsOntologyPath;
 		this.quOntologyPath = quOntologyPath;
 		this.quRec20OntologyPath = quRec20OntologyPath;
 		this.domainOntologyPath = domainOntologyPath;
 		*/
 		
 		this.userIri = userIri;
 		this.rulesUri = rulesUri;
 		this.tripleStoreEndpoint = tripleStoreEndpoint;
 		
 		this.isSilent = isSilent;
 		prefixIndex = new HashMap<String, OntologyConfigMapping>();	
 		spinMgr = new SpinModelManager(tripleStoreEndpoint);
 		
 		jenaModelMgr = new JenaModelManager();
 
 
 	}
 	
 	public boolean initialise() throws OntologyInitException {
 		boolean isInitialised = false;
 		System.out.println("Initialising the ontology svc");
 		
 		initOntologyMgr();
 		try {
 			initOntologies();
 		
 			try {
 				
 				if(spinMgr != null) {
 					initSpinMgr();
 				}
 				
 				if(jenaModelMgr != null) {
 					initJenaModelMgr();
 				}
 			} catch (OWLOntologyStorageException e) {
 				throw new OntologyInitException("initSpinMgr failed");
 
 			} catch (IOException e) {
 				throw new OntologyInitException("initSpinMgr failed");
 
 			} catch (URISyntaxException e) {
 				throw new OntologyInitException("initSpinMgr failed");
 
 			}
 			
 			System.out.println("preparing inputs to reasoner");
 			ontologies = this.ontologyMgr.getOntologies();
 			dataFactory = ontologyMgr.getOWLDataFactory();			
 			pm = new DefaultPrefixManager(this.baseUri);
 			
 			System.out.println("Initialising the reasoner...");
 
 			boolean isInitReasoner = initReasoner();
 			if(isInitReasoner) {
 				System.out.println("Reasoner initialised");
 				isInitialised = true;
 			}
 			else {
 				System.out.println("Reasoner not initialised");
 			}
 			
 		} catch (OntologyInitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new OntologyInitException("Failed to init reasoner");
 		}
 
 		return isInitialised;
 	}
 	
 	private void initSpinMgr() throws OWLOntologyStorageException, IOException, URISyntaxException {
 		if(this.spinMgr == null) {
 			return;
 		}
 		
 		boolean result = this.spinMgr.initInputModel(this.defaultOntology, this.ontologyMgr, this.baseUri);
 		if(!result ) {
 			System.out.println("Spin mgr input model failed to init");
 		}
 		else {
 			
 			if(this.rulesUri != null) {
 				this.spinMgr.initSpinModel(this.rulesUri);
 				
 			}
 		}
 		
 		this.isSpinModelManagerInit = result;
 	}
 	
 	private void initJenaModelMgr() throws OWLOntologyStorageException, IOException, URISyntaxException {
 		if(this.jenaModelMgr == null) {
 			return;
 		}
 		
 		boolean result = this.jenaModelMgr.initInputModel(this.defaultOntology, this.ontologyMgr, this.baseUri);
 		if(!result ) {
 			System.out.println("Jena model mgr input model failed to init");
 		}
 		
 		this.isJenaModelManagerInit = result;
 	}
 	
 	private boolean updateJenaModelMgr(File updatedRdfxml) throws OWLOntologyStorageException, IOException, URISyntaxException {
 		if(this.jenaModelMgr == null) {
 			System.out.println("Jena model mgr is null");
 			return false;
 		}
 		
 		boolean result = this.jenaModelMgr.updateInputModel(updatedRdfxml, this.baseUri);
 		if(!result ) {
 			System.out.println("Jena model mgr input model failed to update");
 		}
 		
 		return result;
 	}
 
 	
 	public  Model runSpinInferences() {
 		return spinMgr.runInferences();
 	}
 	
 	public SpinModelManager getSpinModelManager() {
 		return this.spinMgr;
 	}
 
 	public void initOntologyMgr() {
 		this.ontologyMgr = OWLManager.createOWLOntologyManager();
 		this.ontologyMgr.setSilentMissingImportsHandling(true);
 	}
 	
 	public void resetOntologies() {
 		this.ontologyMgr.removeOntology(this.defaultOntology);
 	}
 
 	public void resetReasoner() {
 		this.reasoner.dispose();
 		this.reasoner = null;
 	}
 
 	public OntologyConfig parseOntologyConfigFromJson(String targetConfig) {
 		//Parse ontologyConfig 
 		OntologyConfig targetOntologyConfig = null;
 		for(OntologyConfig c : this.config) {			
 			//process the "mapping" configs - prefix, IRI, path
 			if(c.getType() != null && c.getType().equals("mapping")) {
 				if(c.getValue() != null) {
 					//for each ontology group mapping
 					for(OntologyConfigMapping mapping : c.getValue()) {
 
 						//map each ontology
 						if(mapping.getIRI() != null && mapping.getPath() != null) {
 							System.out.println("Creating IRI mapper: " + mapping.getIRI() +" :: " + mapping.getPath());
 							this.ontologyMgr.addIRIMapper(
 									new SimpleIRIMapper(
 											IRI.create(mapping.getIRI()), 
 											IRI.create(new File(mapping.getPath()))
 											)
 									);
 						}
 
 						//index the item
 						if(mapping.getPrefix() != null) {
 							prefixIndex.put(mapping.getPrefix(), mapping);
 						}
 					}
 				}
 			}
 			else if(c.getType() != null && c.getType().equals(targetConfig)) {
 				System.out.println("Loading target ontology: " + c);
 
 				targetOntologyConfig = c;				
 			}
 			else if(c.getType() != null && c.getType().equals("base")) {
 				if(c.getValue() != null) {
 					String iriString = c.getValue().get(0).getIRI();		
 					this.baseUri = iriString;
 				}
 			}
 		}
 		return targetOntologyConfig;
 	}
 	public File queryTripleStoreForUserGraph(String userIri, String repo) throws ClientProtocolException, IOException, URISyntaxException, OWLOntologyCreationException {
 		System.out.println("Querying  <"+ userIri +"> from triple store...");
 		return getOntologyAsFile(userIri, repo);
 	}
 
 	public boolean updateTripleStoreWithUserGraph(String userIri, String repo, File rdfxml) throws ClientProtocolException, IOException, URISyntaxException  {
 		String context= "<" + userIri +">";
 		
 		
 		boolean result = SesameHttpUtils.updateFromRDFXML(repo, context, rdfxml);
 		
 		return result;
 	}
 
 	
 	public  File getOntologyAsFile(String base, String repo) throws ClientProtocolException, IOException, URISyntaxException {
 		String context= "<" + base +">";
 
 		File result = SesameHttpUtils.getStatementsAsFile(repo, context, null);
 
 		return result;
 	}
 	
 	public void initOntologies() throws OntologyInitException {
 		OWLOntology ont = null;
 		String ontologyPath = "";
 		File ontologyFile = null;
 
 		//setup for default ontology 
 		OntologyConfig defaultOntologyConfig =  parseOntologyConfigFromJson("default") ;
 		//get the first in the list
 		OntologyConfigMapping defConfigMapping = defaultOntologyConfig.getValue().get(0);
 		ontologyPath = defConfigMapping.getPath();
 		ontologyFile = new File(ontologyPath);
 		System.out.println("Default ontology path: " + ontologyPath);
 				
 		if(defConfigMapping.getPrefix() != null)
 			prefixIndex.put(defConfigMapping.getPrefix(), defConfigMapping);
 				
 		
 		//if userIri supplied, then overwrite default - try and get named graph from triple store
 		if(this.userIri != null && this.tripleStoreEndpoint != null) {
 			//query triple store for user iri
 			IRI docIri = IRI.create(userIri);
 
 			File userGraphFile = null;
 			try {
 				userGraphFile = queryTripleStoreForUserGraph(this.userIri, this.tripleStoreEndpoint);
 				
 				if(userGraphFile != null) {
 					ontologyFile = userGraphFile;
 					ontologyPath = userGraphFile.getPath();
 					//add IRI mapping here.
 					this.ontologyMgr.addIRIMapper(
 							new SimpleIRIMapper(
 										docIri,
 										IRI.create(userGraphFile)
 									)
 							);
 					
 					//add prefix mapping
 					OntologyConfigMapping userMapping = new OntologyConfigMapping();
 					userMapping.setIRI(userIri);
 					userMapping.setPath(userGraphFile.getAbsolutePath());
 					userMapping.setPrefix("user");
 					
 					prefixIndex.put("user", userMapping);
 
 				}
 
 				isUserGraphUsed = true;
 				
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 				throw new OntologyInitException("ClientProtocolException");
 			} catch (OWLOntologyCreationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new OntologyInitException("OWLOntologyCreationException");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new OntologyInitException("IOException");
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new OntologyInitException("URISyntaxException");
 			}
 			
 			
 			
 			
 		}
 		
 		//if no userIri, then get default from config
 		else if(defaultOntologyConfig == null || defaultOntologyConfig.getValue() == null || 
 				defaultOntologyConfig.getValue().size() <= 0 ||
 				defaultOntologyConfig.getValue().get(0).getPath() == null) {
 			System.out.println("Loading default ontology from config failed ");
 			throw new OntologyInitException("Error in default ontology config");
 		}
 		
 		
 
 		//ontologyFile = this.domainOntologyPath;
 
 		
 
 		IRI ontFileIri = null;
 		if(ontFileIri == null) {
 			if(ontologyFile.exists()) {
 				ontFileIri = IRI.create(ontologyFile.toURI());
 				//GWT.log("Ontology IRI: " + f.getAbsolutePath());
 				System.out.println("Ontology IRI: " + ontologyFile.getAbsolutePath());
 			}
 			else {
 				System.out.println("Ontology file cannot be resolved: " + ontologyPath);
 			}
 		}
 
 		System.out.println("Ontology IRI: " + ontFileIri);
 		try {
 			//FileDocumentSource documentSource = new FileDocumentSource(f);			
 			//ont = this.ontologyMgr.loadOntologyFromOntologyDocument(documentSource, config);
 			
 			ont = this.ontologyMgr.loadOntologyFromOntologyDocument(ontologyFile);
 			
 
 			//check if the ontology is empty
 			Set<OWLOntology> imports = ont.getImports();
 			if(userIri != null && (imports == null || imports.size() <= 0)) {
 				//not a valid ontology. should at least import the domain ontology or event-detection ontology
 				throw new OntologyInitException(userIri, "Invalid user ontology - does not import anything");			
 
 				/* This should be part of a different process - i.e. user registration process and not part of the loading process
 				IRI docIri = IRI.create(userIri);
 				//could mean user graph is not valid in repo 
 				//so,  create an ontology with the userIri
 				OWLOntology newOnt = this.ontologyMgr.createOntology(docIri);
 				// and add relevant import statement to the default ontology
 				AddImport importStmt = new AddImport(ont, 
 								this.dataFactory.getOWLImportsDeclaration(IRI.create(defaultOntologyConfig.getValue().get(0).getIRI())));
 				this.ontologyMgr.addAxiom(newOnt, importStmt); //FIXX!!!!
 				 */
 			}
 			
 			this.defaultOntology = ont;
 		} catch (OWLOntologyCreationException e) {
 			System.err.println("Failed loading Ontology: " + ontFileIri);
 
 			e.printStackTrace();
 			throw new OntologyInitException(ontologyPath);			
 		}
 	}
 
 	public boolean initReasoner() {
 		if(this.defaultOntology == null) {
 			return false;
 		}
 		//OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
 		//System.out.println("Instantiating pellet...");
 		//OWLReasonerFactory reasonerFactory = PelletReasonerFactory.theInstance(); 
         //OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
 
 		 //OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
 
 		 //OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor, 30000);
 		OWLReasonerConfiguration config;
 		
 		if(!isSilent) {
 			ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
 			config = new SimpleConfiguration(progressMonitor);
 		}
 		else {
 			NullReasonerProgressMonitor progressMonitor = new NullReasonerProgressMonitor();
 			config = new SimpleConfiguration(progressMonitor);
 		}
 		
 
 		PelletReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
         System.out.println("Creating reasoner");
 		PelletReasoner preasoner = reasonerFactory.createReasoner(this.defaultOntology, config);
         this.reasoner = preasoner;
 
 		
         //OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        // reasoner =  reasonerFactory.createReasoner(this.defaultOntology, config);
         
 		System.out.println("Precomputing inferences");
 		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
 		//reasoner.precomputeInferences();
         boolean consistent = reasoner.isConsistent();
         System.out.println("Model Consistency check: " + consistent);
         System.out.println("\n");
         
         return true;
 	}
 
 	
 	public boolean reloadOntologies(String userIri, String tripleStoreEndpoint) throws OntologyInitException {
 		System.out.println("Reloading the ontology");
 		
 		this.resetOntologies();
 		this.ontologyMgr = OWLManager.createOWLOntologyManager();
 		this.userIri = userIri;
 		this.tripleStoreEndpoint = tripleStoreEndpoint;
 		try {
 			initOntologies();
 			System.out.println("Initialising the reasoner");
 
 			this.resetReasoner();
 			initReasoner();
 			ontologies = this.ontologyMgr.getOntologies();
 			dataFactory = ontologyMgr.getOWLDataFactory();
 			pm = new DefaultPrefixManager(this.baseUri);
 		} catch (OntologyInitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return true;
 	}
 	
 	public boolean reloadOntologies() throws OntologyInitException {
 		System.out.println("Reloading the ontology");
 		
 		this.resetOntologies();
 		this.ontologyMgr = OWLManager.createOWLOntologyManager();
 		
 		try {
 			initOntologies();
 			System.out.println("Initialising the reasoner");
 
 			this.resetReasoner();
 			initReasoner();
 			ontologies = this.ontologyMgr.getOntologies();
 			dataFactory = ontologyMgr.getOWLDataFactory();
 			pm = new DefaultPrefixManager(this.baseUri);
 		} catch (OntologyInitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return true;
 	}
 
 	
 	public boolean saveModel() {
 		try {
 			this.ontologyMgr.saveOntology(this.defaultOntology);
 		} catch (OWLOntologyStorageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	
 	
 	public OWLClass getOwlClass(String iri) {
 		return this.dataFactory.getOWLClass(IRI.create(iri));
 	}
 	
 	public OWLNamedIndividual getOwlNamedIndividual(String iri) {
 		return this.dataFactory.getOWLNamedIndividual(IRI.create(iri));
 	}
 	
 	/*
 	public String getSsnOntologyPath() {
 		return ssnOntologyPath;
 	}
 
 	public void setSsnOntologyPath(String ssnOntologyPath) {
 		this.ssnOntologyPath = ssnOntologyPath;
 	}
 
 	public String getDulOntologyPath() {
 		return dulOntologyPath;
 	}
 
 	public void setDulOntologyPath(String dulOntologyPath) {
 		this.dulOntologyPath = dulOntologyPath;
 	}
 	
 	
 	public String getDomainOntologyPath() {
 		return domainOntologyPath;
 	}
 
 	public void setDomainOntologyPath(String domainOntologyPath) {
 		this.domainOntologyPath = domainOntologyPath;
 	}
 	public String getSSN_IRI() {
 		return SSN_IRI;
 	}
 	
 	/*
 	public String getUNITS_IRI() {
 		return UNITS_IRI;
 	}
 	
 	public String getBASE_URI() {
 		return BASE_URI;
 	}
 	
 	
 	public String getQuOntologyPath() {
 		return quOntologyPath;
 	}
 	public void setQuOntologyPath(String quOntologyPath) {
 		this.quOntologyPath = quOntologyPath;
 	}
 	public String getQuRec20OntologyPath() {
 		return quRec20OntologyPath;
 	}
 	public void setQuRec20OntologyPath(String quRec20OntologyPath) {
 		this.quRec20OntologyPath = quRec20OntologyPath;
 	}
 	public String getQU_IRI() {
 		return QU_IRI;
 	}
 	public String getQU_REC20_IRI() {
 		return QU_REC20_IRI;
 	}
 	
 	
 	
 	*/
 
 	/*
 	public String getUnitsOntologyPath() {
 		return unitsOntologyPath;
 	}
 
 	public void setUnitsOntologyPath(String unitsOntologyPath) {
 		this.unitsOntologyPath = unitsOntologyPath;
 	}
 	*/
 
 	public OWLOntologyManager getOntologyMgr() {
 		return ontologyMgr;
 	}
 	public void setOntologyMgr(OWLOntologyManager ontologyMgr) {
 		this.ontologyMgr = ontologyMgr;
 	}
 	public OWLReasoner getReasoner() {
 		return reasoner;
 	}
 	public void setReasoner(OWLReasoner reasoner) {
 		this.reasoner = reasoner;
 	}
 	public QNameShortFormProvider getQnameProvider() {
 		return qnameProvider;
 	}
 	public void setQnameProvider(QNameShortFormProvider qnameProvider) {
 		this.qnameProvider = qnameProvider;
 	}
 	public SimpleShortFormProvider getShortFormProvider() {
 		return shortFormProvider;
 	}
 	public void setShortFormProvider(SimpleShortFormProvider shortFormProvider) {
 		this.shortFormProvider = shortFormProvider;
 	}
 	public Set<OWLOntology> getOntologies() {
 		//return ontologies;
 		return this.ontologyMgr.getOntologies();
 	}
 	public OWLDataFactory getDataFactory() {
 		return dataFactory;
 	}
 	public void setDataFactory(OWLDataFactory dataFactory) {
 		this.dataFactory = dataFactory;
 	}
 	public PrefixManager getPm() {
 		return pm;
 	}
 	public void setPm(PrefixManager pm) {
 		this.pm = pm;
 	}
 	
 	public OWLOntology getDefaultOntology() {
 		return defaultOntology;
 	}
 	public void setDefaultOntology(OWLOntology defaultOntology) {
 		this.defaultOntology = defaultOntology;
 	}
 	
 
 	public SparqlSelectResultSetBean  executeSparqlQueryOnSpinModel(String sparql) {
 		//check if spinmodel is init
 		if(this.spinMgr.getSpinModel() == null)
 			return null;
 		
 		SparqlSelectResultSetBean resultSet = null;
 		
 		boolean inferenceIsOk = false;
 		//check if inferences have run
 		if(this.spinMgr.hasInferencingPerformed() == false) {
 			//try to run inferences
 			if(this.spinMgr.runInferences() == null) {
 				inferenceIsOk = false;
 			}
 			else {
 				inferenceIsOk = true;
 			}
 			
 		}
 		
 		if(inferenceIsOk) {
 			OntModel spinModel = this.spinMgr.getSpinModel();
 			//run sparql query
 
 			resultSet= this.performSparqlQuery(spinModel, sparql);
 		}
 		
 		return resultSet;
 		
 	}
 	
 	public SparqlSelectResultSetBean  executeSparqlSelectQuery(String sparql) {
 		OntModel model = null;
 		
 		if(this.jenaModelMgr != null && this.jenaModelMgr.getInputModel() != null) {
 			model  = this.jenaModelMgr.getInputModel();
 		}
 		else if(this.spinMgr != null && this.spinMgr.getInputModel() != null) {
 			model  = this.spinMgr.getInputModel();
 		}
 		
 		
 		//check if spinmodel is init
 		if(model == null)
 			return null;
 		
 		SparqlSelectResultSetBean resultSet = null;
 		
 		//run sparql query
 		resultSet = this.performSparqlQuery(model, sparql);
 		
 		return resultSet;		
 	}
 
 	private SparqlSelectResultSetBean performSparqlQuery(Model model, String sparqlSelectQuery) {
 		//String queryString = "SELECT ?rule ?sensor WHERE { ?rule a <http://waterinformatics1-cdc.it.csiro.au/resource/event-detection.owl#ValueConstraintEventRule> . ?rule <http://waterinformatics1-cdc.it.csiro.au/resource/event-detection.owl#constraintSensorMatch> ?sensor}";
 		String queryString = sparqlSelectQuery;
 		SparqlSelectResultSetBean resultSetBean = new SparqlSelectResultSetBean();
 
 		if(sparqlSelectQuery == null) {
 			return null;
 		}
 		Query query = QueryFactory.create(queryString) ;
 		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
 		try {
 			ResultSet results = qexec.execSelect() ;
 			//ResultSetFormatter.out(System.out, results, query) ;
 			List<String> resultVars = results.getResultVars();
 
 			List<String> selectVars = new ArrayList<String>(resultVars);
 
 			resultSetBean.setVarNames(selectVars);
 
 			for ( ; results.hasNext() ; )
 			{
 				QuerySolution soln = results.nextSolution() ;
 				SparqlSelectResultBean currResult = new SparqlSelectResultBean();
 				for(String currVar : selectVars) {
 					RDFNode x = soln.get(currVar) ;       // Get a result variable by name.
 					String repr = null;
 					if(x != null) {
 						if(x.isResource()) {
 							repr = x.asResource().getURI();
 						}	
 						else if(x.isLiteral()) {
 							repr = x.asLiteral().getString();
 						}
 						else {
 							repr = x.toString();
 						}
 					}
 					currResult.getEntryList().add(repr);		    	  
 				}
 
 				resultSetBean.addEntry(currResult);
 
 				//RDFNode x = soln.get("varName") ;       // Get a result variable by name.
 				//Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
 				//Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
 			}
 		} finally { qexec.close() ; }
 
 		return resultSetBean;
 	}
 
 
 	public int addOwlIndividual(OwlIndividualBean indiv) throws OntologyInitException {
 		int numSuccessfulSteps = 0; 
 		
 		boolean isTripleStoreUpdated = false;
 		boolean isJenaModelUpdated = false;
 		boolean hasChanges = false;
 		
 		if(indiv == null) {
 			return -1;
 		}
 		
 		List<OWLOntologyChange> listOfChanges = new ArrayList<OWLOntologyChange>();
 
 		//add this as individual using owl api
 		System.out.println("Creating OWL API instance of indiv..." );
 
 		OWLIndividual owlIndiv = this.toIndividual(indiv);
 
 		Set<OWLAxiom> listOfAxioms  = new HashSet<OWLAxiom>();
 		
 		
 		//get parent class (if defined)
 		OWLClass parentClass = null;
 		if(indiv.getType() != null) {
 			String parentClassIri = indiv.getType().getIri();
 		
 			if(parentClassIri != null) {
 				System.out.println("Adding class->instance assertion: " + parentClassIri);
 				
 				parentClass = dataFactory.getOWLClass(IRI.create(parentClassIri));
 				
 				
 				OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(
 		                parentClass, owlIndiv);
 				
 				listOfAxioms.add(classAssertion);
 				
 				/*
 		        // Add the class assertion
 		        List<OWLOntologyChange> changes2 = ontologyMgr.addAxiom(defaultOntology, classAssertion);
 		    	
 		        if(changes2 != null) {
 		        	listOfChanges.addAll(changes2);
 		        }
 		        */		        
 			}
 			else {
 				System.out.println("Instance type cannot be null");
 				return -1;
 			}
 
 		}
 		else {
 			System.out.println("Instance type cannot be null");
 			return -1;
 		}
 		
 		
 		//get list of addAxioms
 		System.out.println("Getting list of assertions from indiv... ");
 		List<OWLAxiom> listOfAxiomsFromIndiv = this.getAddAxiomsFromIndividualBean(indiv, owlIndiv);
 		
 		if(listOfAxiomsFromIndiv != null) {
 	        listOfAxioms.addAll(listOfAxiomsFromIndiv);
 		}
 
 
 		
         
         if(listOfChanges != null) {
         	hasChanges = true;
         	numSuccessfulSteps++; //has changes
             	
     		//create temp ontology to keep new statements
     		OWLOntology tempontology = null;
             try {
             	tempontology = ontologyMgr.createOntology();
             	ontologyMgr.addAxioms(tempontology, listOfAxioms);
     		} catch (OWLOntologyCreationException e1) {
     			// TODO Auto-generated catch block
     			e1.printStackTrace();
     			throw new OntologyInitException("Could not create temp ontology." + e1.getLocalizedMessage());
     		}
 
             
             
         	if(tempontology != null && this.isUserGraphUsed) {
         		//update changes to default ontology
             	ontologyMgr.addAxioms(this.defaultOntology, listOfAxioms);
             	
             	
             	System.out.println("User graph used... ");
         		
         		File f = null;
         		numSuccessfulSteps++; //has changes and user graph used
             				
         		//serialise the user graph to rdfxml and post to triple store
         		//assume the usergraph is maintained at the default ontology
         		
         		try {
         			System.out.println("Serializing ontology...");
         			f = File.createTempFile("ontTemp", ".rdf");
         			FileOutputStream fos = new FileOutputStream(f);
 					ontologyMgr.saveOntology(tempontology, new RDFXMLOntologyFormat(), fos);
 					
 		        	
         		
 	        		//poke the changes through to triple store
 	        		if(f != null) {
 	        			numSuccessfulSteps++; //successfully seriliazed to rdfxml
 	        			
 	        			System.out.println("Updating triple store ...");	
 	        			isTripleStoreUpdated = this.updateTripleStoreWithUserGraph(userIri, tripleStoreEndpoint, f);
 	        			
 
 	        			if(isTripleStoreUpdated) {
 	        				numSuccessfulSteps++; //successfully updated triple store
 	        				
 			        		//update jena model
 	        				System.out.println("Updating Jena store ...");
 		        			isJenaModelUpdated = updateJenaModelMgr(f);
 		        			if(isJenaModelUpdated) {
 		        				numSuccessfulSteps++; //successfully updated jena
 		        			}
 		        			
 	        			}
 	        		}
 	        		
         		} catch (OWLOntologyStorageException e) {
 					e.printStackTrace();
 					throw new OntologyInitException("Could not serialise user graph file for updating triple store. " + e.getLocalizedMessage());					
 				} catch (IOException e) {
 					e.printStackTrace();
 					throw new OntologyInitException("Could not create file for storing user graph file to update triple store. " + e.getLocalizedMessage());
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 					throw new OntologyInitException("Error in request URI . " + e.getLocalizedMessage());
 				}
         		
         		
         	}
         	
         	
         }
 		
         return numSuccessfulSteps;
         
 	}
 	
 	//assume indiv and owlIndiv is set
 	private List<OWLAxiom> getAddAxiomsFromIndividualBean(OwlIndividualBean indivBean, OWLIndividual indiv) {
 
 		if(indiv == null && indivBean == null) {
 			return null;
 		}
 		
 		List<OWLAxiom> list = new ArrayList<OWLAxiom>();
 
 		
 		//add any rdfs:label, rdfs:comment or annotation props
 		if(indivBean.getLabel() != null) {
 			OWLAnnotation labelAnnotation = dataFactory.getOWLAnnotation(
 					dataFactory.getRDFSLabel(),
 					dataFactory.getOWLLiteral(indivBean.getLabel(), "en"));
 	        
 			if(indiv instanceof OWLNamedIndividual) {
 				OWLNamedIndividual i = (OWLNamedIndividual) indiv;
 				
 				OWLAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(i.getIRI(), labelAnnotation);
 				list.add(ax);
 				
 			}
 			else if(indiv instanceof OWLAnonymousIndividual) {
 				OWLAnonymousIndividual anon = (OWLAnonymousIndividual) indiv;
 				OWLAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(anon, labelAnnotation);
 				list.add(ax);
 			}
 
 			
 		}
 		
 		if(indivBean.getRdfsComment() != null) {
 			OWLAnnotation commentAnnotation = dataFactory.getOWLAnnotation(
 					dataFactory.getRDFSComment(),
					dataFactory.getOWLLiteral(indivBean.getRdfsComment(), "en"));
 
 			if(indiv instanceof OWLNamedIndividual) {
 				OWLNamedIndividual i = (OWLNamedIndividual) indiv;
 				
 				OWLAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(i.getIRI(), commentAnnotation);
 				list.add(ax);
 				
 			}
 			else if(indiv instanceof OWLAnonymousIndividual) {
 				OWLAnonymousIndividual anon = (OWLAnonymousIndividual) indiv;
 				OWLAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(anon, commentAnnotation);
 				list.add(ax);
 			}
 			
 		}
 		
 		
 		//get list of data props
 		if(indivBean.getDataProperties() != null) {
 			for(OwlDataPropertyBean dataPropBean : indivBean.getDataProperties().keySet()) {
 				 OWLDataProperty dataProp = dataFactory.getOWLDataProperty(IRI.create(dataPropBean.getPropertyIri()));
 	
 				 for(OwlLiteralBean literalBean : indivBean.getDataProperties().get(dataPropBean)) {
 					 OWLLiteral literal = this.toLiteral(literalBean);
 					 
 					 OWLDataPropertyAssertionAxiom assertion = dataFactory
 				                .getOWLDataPropertyAssertionAxiom(dataProp, indiv, literal);
 				     
 					 list.add(assertion);
 				 }   
 			}
 		}
 		
 		//get list of obj props
 		if(indivBean.getObjectProperties() != null) {
 			for(OwlObjectPropertyBean objPropBean : indivBean.getObjectProperties().keySet()) {
 				 OWLObjectProperty objProp = dataFactory.getOWLObjectProperty(IRI.create(objPropBean.getPropertyIri()));
 
 				 for(OwlIndividualBean assocIndivBean : indivBean.getObjectProperties().get(objPropBean)) {
 					 OWLIndividual assocIndiv = this.toIndividual(assocIndivBean);
 					 
 					OWLObjectPropertyAssertionAxiom assertion = dataFactory
 				                .getOWLObjectPropertyAssertionAxiom(objProp, indiv, assocIndiv);
 					 
 					 list.add(assertion);				 
 				 }   
 			}			
 		}
 
 	        
 	        
 		return list;
 	}
 
 
 	private OWLIndividual toIndividual(OwlIndividualBean indiv) {
 		if(indiv == null) {
 			return null;
 		}
 		
 		OWLIndividual modelIndiv = null;
 		
 		//if indiv has its iri defined
 		if(indiv.getIri() != null) {
 			modelIndiv = dataFactory.getOWLNamedIndividual(IRI.create(indiv.getIri()));
 		}
 		else {
 	        modelIndiv = dataFactory.getOWLAnonymousIndividual();			
 		}
 		
 
 		//set any properties
 		
 		
 		return modelIndiv;
 	}
 
 
 	private OWLLiteral toLiteral(OwlLiteralBean literalBean) {
 		OWLLiteral literal = null;
 		 if(literalBean.getDatatypeIri() != null) {
 			 literal = dataFactory.getOWLLiteral(literalBean.getLiteral(), 
 					 								OWL2Datatype.getDatatype(IRI.create(literalBean.getDatatypeIri())));	 
 		 }
 		 else {
 			 literal = dataFactory.getOWLLiteral(literalBean.getLiteral());
 		 }				 
 	
 		 return literal;
 	}
 	
 	
 }
