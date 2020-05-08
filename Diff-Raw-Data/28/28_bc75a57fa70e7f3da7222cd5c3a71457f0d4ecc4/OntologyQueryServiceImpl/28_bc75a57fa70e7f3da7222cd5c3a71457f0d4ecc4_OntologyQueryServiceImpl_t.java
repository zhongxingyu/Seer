 package au.csiro.eis.ontology.gwt.rpc.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLOntologyStorageException;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;
 
 import com.google.gson.Gson;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import au.csiro.eis.ontology.OntologyMediatorInterface;
 import au.csiro.eis.ontology.beans.OwlClassBean;
 import au.csiro.eis.ontology.beans.OwlIndividualBean;
 import au.csiro.eis.ontology.beans.OwlOntologyBean;
 import au.csiro.eis.ontology.beans.RdfResourceBean;
 import au.csiro.eis.ontology.beans.SparqlSelectResultSetBean;
 import au.csiro.eis.ontology.beans.SwrlBuiltInBean;
 import au.csiro.eis.ontology.beans.config.OntologyConfig;
 import au.csiro.eis.ontology.beans.config.OntologyConfigMapping;
 import au.csiro.eis.ontology.exception.OntologyInitException;
 import au.csiro.eis.ontology.gwt.rpc.client.ontologyService.OntologyQueryService;
 import au.csiro.eis.ontology.gwt.rpc.shared.ConfigBean;
 import au.csiro.eis.ontology.resource.CepOntologyManager;
 import au.csiro.eis.ontology.resource.owlapi.OntologyBeanFactory;
 import au.csiro.eis.ontology.resource.owlapi.OwlApiOntologyMediator;
 import au.csiro.eis.utils.FileIOUtils;
 
 
 public class OntologyQueryServiceImpl extends RemoteServiceServlet 
 						implements OntologyQueryService {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	OntologyMediatorInterface ontMediator;
 	CepOntologyManager cepOntMgr;
 	OntologyBeanFactory ontBeanFactory;
 	private final String CONFIG_FILEPATH = "config.properties";
 	Properties prop = null ;
 	ConfigBean userConfig = null;
 	Map<String,String> listNamedGraphs = new HashMap<String,String>();
 	
 	final int MAX_DEPTH_TRAVERSAL = 1;
 	
 	//String ONTOLOGY_CONFIG_JSON = "chaffey_dam_ontology_config.json";
 	final String VS_TEMPLATE_PATH = "virtual-sensor/scriptlet-template.xml";
 
 	final String VS_TEMPLATE_PROP = "gsn.vs.template.path";
 	final String TRIPLE_STORE_REPO_PROP = "triple.store.statements.endpoint";
 
 	String DOMAIN_NAMED_GRAPH = null;
 	String RULES_NAMED_GRAPH = null;
 	
 	String tripleStoreRepo = null;
 	boolean hasInitialised = false;
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		 
 		if(userConfig != null) {
 			
 			try {
 				initialiseSvc(userConfig);	
 				this.hasInitialised = true;
 			} catch (OntologyInitException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 		}
 	
 	}
 	
 	public OntologyConfig[] initOntologyConfigs() throws OntologyInitException {
 		String ontPath = this.getRealPath(this.userConfig.getConfigFileName());
 		
 		File f = new File(ontPath);
 		String json;
 		try {
 			//json = FileUtils.readFile(f);
 			json = FileIOUtils.readFile(f);
 			//Type collectionType = new TypeToken<Collection>(){}.getType();
 			//Collection<OntologyConfig> ints2 = new Gson().fromJson(json, collectionType);
 
 	        OntologyConfig[] arrConfig = new Gson().fromJson(json, OntologyConfig[].class);
 	        System.out.println("Performing path resolution");
 	        for(OntologyConfig config : arrConfig) {        	
 	        	if(config.getType() != null) {
 	        		if(config.getType().equals("mapping") || config.getType().equals("default"))  {
 		        		if(config.getValue() != null) {
 		        			for(OntologyConfigMapping mapping : config.getValue()) {
 		        				if(mapping.getPath() != null) {
 		        					System.out.println(mapping.getPath());
 		        					//resolve paths
 		        					String resolvedPath = getRealPath(mapping.getPath());
 		        					
 		        					mapping.setPath(resolvedPath);
 		        				}
 		        			}		        			
 		        		}
 		        	}
 		        	else if(config.getType().equals("named-graph")) {
 		        		//store named graphs?
 		        		listNamedGraphs.put(config.getName(), config.getIri());
 		        	}
 	        	}	
 	        }
 	        
 	        System.out.println("After Resolved paths");
 	        for(OntologyConfig config : arrConfig) {        	
 	        	System.out.println(config.toString());
 	        }
 	        return arrConfig;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new OntologyInitException();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new OntologyInitException();
 		}
 		
 
 	}
 	
 	public boolean initialiseSvc(ConfigBean inputUserConfig) throws OntologyInitException {
 		boolean isInitialised = false;
 		
 		if(inputUserConfig != null) {
 			this.userConfig = inputUserConfig;
 			
 		}
 		
 		if(this.userConfig == null) {
 			throw new OntologyInitException("","User config is null");			
 		}
 		
 		prop = new Properties();
 		FileReader freader;
 		try {
 			String configRealPath =  this.getRealPath(CONFIG_FILEPATH);
 			ServletContext servletContext = this.getServletContext();
 			String pathContext = servletContext.getRealPath("/WEB-INF/");
 			System.out.println("in initOntologyMediator: pathContext = " + pathContext);
 
 			File f = new File(pathContext);
 			if(f.isDirectory()) {
 				for(String s : f.list()) {
 					System.out.println("File: " + s);
 				}
 			}
 			
 			System.out.println("in initOntologyMediator: contextPath = " + this.getServletContext().getContextPath());
 			System.out.println("in initOntologyMediator: getServletContextName = " + this.getServletContext().getServletContextName());
 			//System.out.println("in initOntologyMediator: getResource = " + this.getServletContext().getResource(CONFIG_FILEPATH));
 
 			System.out.println("in initOntologyMediator: config.properties location = " + configRealPath);
 			freader = new FileReader(new File(configRealPath));
 			
 			prop.load(freader);			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 
 
 		OntologyConfig[] arrConfig = initOntologyConfigs();
 		
 		if(this.listNamedGraphs.get("domain-triples") != null) {
 			this.DOMAIN_NAMED_GRAPH = this.listNamedGraphs.get("domain-triples") ;
 		}
 		if(this.listNamedGraphs.get("rules-triples") != null) {
 			this.RULES_NAMED_GRAPH = this.listNamedGraphs.get("rules-triples");
 		}
 		//boolean isConsoleOutputSilent = true;
 		boolean isConsoleOutputSilent = false;
 
 		//check if the userGraphIri is valid
 		boolean isUserGraphLoaded = false;
 		
 		if(this.userConfig != null && this.userConfig.getUserIri() != null) {
 			String repo = prop.getProperty(TRIPLE_STORE_REPO_PROP);
 			this.tripleStoreRepo = repo;
 			//load user graph
 			
 			cepOntMgr = new CepOntologyManager(
 										arrConfig,
 										this.userConfig.getUserIri(),
 										this.RULES_NAMED_GRAPH,
 										repo,
 										isConsoleOutputSilent);
 			ontBeanFactory = new OntologyBeanFactory(cepOntMgr);
 			this.ontMediator  = new OwlApiOntologyMediator(cepOntMgr, ontBeanFactory);
 			
 			try {
 				isInitialised = cepOntMgr.initialise();		
 				isUserGraphLoaded = true;
 			} catch (OntologyInitException e) {
 				//catch any failed attempts to load user graph
 				isUserGraphLoaded = false;
 			}
 		}
 		if(!isUserGraphLoaded) {
 			//try and load default graph if the user graph is not loaded
 			cepOntMgr = new CepOntologyManager(arrConfig, isConsoleOutputSilent);
 			ontBeanFactory = new OntologyBeanFactory(cepOntMgr);
 			this.ontMediator  = new OwlApiOntologyMediator(cepOntMgr, ontBeanFactory);
 
 			isInitialised = cepOntMgr.initialise();		
 		}
 
 		
 		
 		String resolvedPath = null;
 		if(prop.get(VS_TEMPLATE_PROP) == null) {
 			resolvedPath = this.getRealPath(VS_TEMPLATE_PATH);		
 		}
 		else {
 			resolvedPath = this.getRealPath(prop.getProperty(VS_TEMPLATE_PROP));	
 		}
 
 		if(isInitialised) {
 			this.hasInitialised = true;
 		}
 		
 		return isInitialised;
 	}
 	
 	
 
 	public boolean reloadOntologies() throws OntologyInitException {
 		//boolean result = this.cepOntMgr.reloadOntologies();
 		
 		this.cepOntMgr.resetOntologies();
 		this.cepOntMgr.resetReasoner();
 		
 		boolean result = this.initialiseSvc(this.userConfig);
 		this.getClasses();
 		
 		return result;
 	}
 
 	public boolean switchDomain(ConfigBean inputConfigBean) throws OntologyInitException {
 		boolean result = false;
 		
 		this.userConfig = inputConfigBean;
 		
 		
 		this.reloadOntologies();
 		
 		return result;
 	}
 	
 	
 	@Override
 	public void destroy() {
 		super.destroy();
 	}
 	
 	public boolean saveModel() {
 		return this.cepOntMgr.saveModel();
 	}
 	
 	public String getRealPath(String filePath) {
 		ServletContext c = super.getServletContext();
 		return c.getRealPath(filePath);
 	}
 	
 	
 	public List<String> getClasses() throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getClasses();
 	}
 	
 
 	/**
 	 * Gets ontology bean representation. Processes the complete set of ontologies.
 	 * Then renders everything under the #Thing class;
 	 */
 	public OwlOntologyBean getOntologyBean() throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntologyBean();	
 	}
 
 	
 	
 	/**
 	 * Gets ontology bean representation. Processes the complete set of ontologies.
 	 * Then renders classes belonging to input ontology URI;
 	 */
 	public OwlOntologyBean getOntologyBean(String ontology_uri) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntologyBean(ontology_uri);
 	}
 
 	
 	public List<String> getSubClasses(String iri_string) throws OntologyInitException {		
 		return this.ontMediator.getSubClasses(iri_string);
 	}
 	
 	public void checkHasServiceInitialised() throws OntologyInitException {
 		if(!this.hasInitialised) {
 			throw new OntologyInitException("","Service has not initialised yet");
 		}
 	}
 	
 		
 	public OwlClassBean getOntClass(String iri_string) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
         return this.ontMediator.getOntClass(iri_string);		
 	}
 	
 	public OwlClassBean getOntClass(String iri_string, boolean loadIndividuals, boolean loadParents, boolean loadChildren) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntClass(iri_string, loadIndividuals, loadParents, loadChildren);	
 	}
 	
 	public List<OwlIndividualBean> getOntIndividualsForClass(String class_iri_string)
 			throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return getOntIndividualsForClass(class_iri_string, false);		
 	}
 	
 	/**
 	 * Gets the individuals for the input OWLClass IRI string
 	 */
 	public List<OwlIndividualBean> getOntIndividualsForClass(String class_iri_string, boolean isDirect)
 			throws OntologyInitException {
 		checkHasServiceInitialised();
 				
 		return this.ontMediator.getOntIndividualsForClass(class_iri_string, isDirect);		
 	}
 	
 	
 	/**
 	 * Gets the individuals for the input list of named indiv IRI strings
 	 */
 	public List<OwlIndividualBean> getOntIndividuals(List<String> indivIriList, boolean skipPropMappings, boolean resolveParents)
 			throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntIndividuals(indivIriList, skipPropMappings, resolveParents );				
 	}
 	
 
 		
 	public List<String> getRestrictions(String iri_string) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getRestrictions(iri_string);
 	}
 	
 
 	public List<OwlClassBean> getOntSubClasses(String iri_string)
 			throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		
 		boolean isDirect = true;
 		boolean loadIndiv = true;
 		boolean loadParents = false;
 		boolean loadChildren = true;
 		
 		
 		return this.ontMediator.getOntSubClasses(iri_string, isDirect, loadIndiv, loadParents, loadChildren);
 		
 	}
 
 	public List<OwlClassBean> getOntSubClasses(String iri_string, boolean isDirect,	boolean loadIndiv,  
 												boolean loadParents, boolean loadChildren )
 			throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntSubClasses(iri_string, isDirect, loadIndiv, loadParents, loadChildren)	;		
 	}
 
 
 	//default is not to load any individuals, parents or subclasses
 	public List<OwlClassBean> getOntClasses(List<String> iriList) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntClasses(iriList, false, false, false);		
 	}
 
 	public List<OwlClassBean> getOntClasses(List<String> iriList, boolean loadIndividuals, boolean loadParents, boolean loadChildren) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntClasses(iriList, loadIndividuals, loadParents, loadChildren);
 	}
 
 	public List<SwrlBuiltInBean> getSwrlBuiltInFunctions() throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		List<SwrlBuiltInBean> sList = new ArrayList<SwrlBuiltInBean>();
 		
 		for(SWRLBuiltInsVocabulary v : SWRLBuiltInsVocabulary.values()) {
 			sList.add(new SwrlBuiltInBean(v.getShortName(), v.getIRI().toString()));
 		}
 		
 		return sList;
 		
 		
 	}
 	
 	public boolean isSubclassOf(String iri, String parentIri, boolean isDirect) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		boolean result = false;
 
 
 		OWLReasoner reasoner = this.cepOntMgr.getReasoner();
 		
 		OWLClass parentOwlClass = this.cepOntMgr.getOwlClass(parentIri);
 		OWLClass candidateChildClass = this.cepOntMgr.getOwlClass(iri);
 		
 		
 		if(reasoner.getSubClasses(parentOwlClass, isDirect).containsEntity(candidateChildClass)) {
 			result = true;
 		}
 		
 		
 		return result;
 	}
 
 
 	
 	private void queryForMatchingSensorFromEventRule() throws OntologyInitException {
 		String rulesUri = "http://waterinformatics1-cdc.it.csiro.au/resource/event-detection/chaffey-rules";
 		
 		//run inferences on spin model
 		try {
 			this.cepOntMgr.getSpinModelManager().initSpinModel(rulesUri);
 			
 			this.cepOntMgr.runSpinInferences();
 			OntModel model = this.cepOntMgr.getSpinModelManager().getSpinModel();
 			
 			
 			String queryString = "SELECT ?rule ?sensor WHERE { ?rule a <http://waterinformatics1-cdc.it.csiro.au/resource/event-detection.owl#ValueConstraintEventRule> . ?rule <http://waterinformatics1-cdc.it.csiro.au/resource/event-detection.owl#constraintSensorMatch> ?sensor}";
 		
 			  Query query = QueryFactory.create(queryString) ;
 			  QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
 			  try {
 			    ResultSet results = qexec.execSelect() ;
 			    ResultSetFormatter.out(System.out, results, query) ;
 			    /*
 			    for ( ; results.hasNext() ; )
 			    {
 			      QuerySolution soln = results.nextSolution() ;
 			      RDFNode x = soln.get("varName") ;       // Get a result variable by name.
 			      Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
 			      Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
 			    }*/
 			  } finally { qexec.close() ; }
 			
 		} catch (OWLOntologyStorageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new OntologyInitException("SPIN Model manager failed to init");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new OntologyInitException("SPIN Model manager failed to init");
 
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new OntologyInitException("SPIN Model manager failed to init");
 
 		}
 		
 	}
 
 	public SparqlSelectResultSetBean performSparqlQueryOnSpinModel(String sparql) {
 		return this.cepOntMgr.executeSparqlQueryOnSpinModel(sparql);
 	}
 
 	public SparqlSelectResultSetBean performSparqlSelectQuery(String sparql) {
 		return this.cepOntMgr.executeSparqlSelectQuery(sparql);
 	}
 	
 
 	public RdfResourceBean getRdfResource(String iri_string) throws OntologyInitException {
 		checkHasServiceInitialised();
 		RdfResourceBean resultBean = null;
 		
 		//determine if the iri is a class or individual
 		if(iri_string == null) {
 			return null;
 		}
 		
         OwlClassBean classBean = this.ontMediator.getOntClass(iri_string);
         if(classBean != null) {
         	resultBean = classBean;
         }
 		
         else {
         	OwlIndividualBean indivBean = this.ontMediator.getOntIndividual(iri_string, false, true);	
         	if(indivBean != null) {
         		resultBean = this.ontMediator.getOntIndividual(iri_string, false, true);
         	}
         }
         
 		return resultBean;
 	}
 
 	
 
 	public OwlIndividualBean getOntIndividual(String indivIri, boolean skipPropMappings, boolean resolveParents)
 			throws OntologyInitException {		
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.getOntIndividual(indivIri, skipPropMappings, resolveParents);	
 	}
 
 	@Override
 	public int addOwlIndividual(OwlIndividualBean indiv) throws OntologyInitException {
 		checkHasServiceInitialised();
 		
 		return this.ontMediator.addOwlIndividual(indiv);	
 	}
 
	@Override
	public int deleteOwlIndividual(OwlIndividualBean indiv)
			throws OntologyInitException {
		return this.ontMediator.deleteOwlIndividual(indiv);	
	}

 
 	
 }
