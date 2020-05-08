 package au.csiro.eis.ontology.resource.owlapi;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 
 import org.semanticweb.owlapi.model.AddAxiom;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLDataProperty;
 import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLNamedIndividual;
 import org.semanticweb.owlapi.model.OWLObjectProperty;
 import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyChange;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLPropertyExpression;
 import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
 import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
 import org.semanticweb.owlapi.reasoner.Node;
 import org.semanticweb.owlapi.reasoner.NodeSet;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
 import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;
 
 import au.csiro.eis.ontology.OntologyMediatorInterface;
 import au.csiro.eis.ontology.beans.OwlClassBean;
 import au.csiro.eis.ontology.beans.OwlIndividualBean;
 import au.csiro.eis.ontology.beans.OwlObjectPropertyBean;
 import au.csiro.eis.ontology.beans.OwlOntologyBean;
 import au.csiro.eis.ontology.beans.SwrlBuiltInBean;
 import au.csiro.eis.ontology.beans.WqCepRule;
 import au.csiro.eis.ontology.beans.config.OntologyConfig;
 import au.csiro.eis.ontology.beans.config.OntologyConfigMapping;
 import au.csiro.eis.ontology.exception.OntologyInitException;
 import au.csiro.eis.ontology.owlapi.tools.RestrictionVisitor;
 import au.csiro.eis.ontology.resource.CepOntologyManager;
 import au.csiro.eis.utils.FileIOUtils;
 
 import com.google.gson.Gson;
 
 
 public class OwlApiOntologyMediator implements OntologyMediatorInterface {
 
 	CepOntologyManager cepOntMgr;
 	OntologyBeanFactory ontBeanFactory;
 	
 	
 	public OwlApiOntologyMediator() {
 		
 		
 	}
 	
 	public OwlApiOntologyMediator(CepOntologyManager cepOntMgr, OntologyBeanFactory ontBeanFactory) {
 		this.cepOntMgr = cepOntMgr;
 		this.ontBeanFactory = ontBeanFactory;
 		
 	}
 	
 	public void init(CepOntologyManager cepOntMgr,
 			OntologyBeanFactory ontBeanFactory) {
 		this.cepOntMgr = cepOntMgr;
 		this.ontBeanFactory = ontBeanFactory;
 		
 	}
 	
 	
 
 	public List<String> getClasses() throws OntologyInitException {
 		//assumes that the ontologies are all loaded...
 		List<String> listOfClasses = new ArrayList<String>();
 
 		Set<OWLOntology> ontologies = this.cepOntMgr.getOntologies();
 		
 		for(OWLOntology ont : ontologies) {		
 			listOfClasses.addAll(this.ontBeanFactory.doOntologyClassWalking(ont));
 
 			for(OWLOntology importedOntology : ont.getImports()) {
 				List<String> importedListOfClasses = this.ontBeanFactory.doOntologyClassWalking(importedOntology);
 				listOfClasses.addAll(importedListOfClasses);
 			}
 		}
 
 		SortedSet<String> setOfClasses = Collections.synchronizedSortedSet(new TreeSet<String>(listOfClasses));
 
 		return new ArrayList<String>(setOfClasses);
 	}
 	
 
 	/**
 	 * Gets ontology bean representation. Processes the complete set of ontologies.
 	 * Then renders everything under the #Thing class;
 	 */
 	public OwlOntologyBean getOntologyBean() throws OntologyInitException {
 		//assumes that the ontologies are all loaded...
 		Map<String,OwlClassBean> mapOfClasses = new HashMap<String,OwlClassBean>();
 		//Map<String,OwlIndividualBean> mapOfIndividuals = new HashMap<String,OwlIndividualBean>();
 
 		Set<OWLOntology> ontologies = this.cepOntMgr.getOntologies();
 		
 		Set<OWLOntology> completeSetOfOntologies = new HashSet<OWLOntology>();
 		
 		completeSetOfOntologies.addAll(ontologies);
 		
 		//classes
 		for(OWLOntology ont : ontologies) {		
 			mapOfClasses.putAll(this.ontBeanFactory.doOntologyClassWalkingForBean(ont, false));
 			//mapOfIndividuals.putAll(this.doOntologyIndividualWalkingForBean(ont));
 			
 			Set<OWLOntology> importedOntologies = ont.getImports();
 			completeSetOfOntologies.addAll(importedOntologies);
 			
 			for(OWLOntology importedOntology : importedOntologies) {
 				Map<String,OwlClassBean> importedListOfClasses = this.ontBeanFactory.doOntologyClassWalkingForBean(importedOntology, false);
 				//Map<String,OwlIndividualBean> importedListOfInstances = this.doOntologyIndividualWalkingForBean(importedOntology);
 				
 				mapOfClasses.putAll(importedListOfClasses);
 				
 				//mapOfIndividuals.putAll(importedListOfInstances);
 				
 			}
 		}
 		
 		OWLClass owlThing =  this.cepOntMgr.getDataFactory().getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#Thing"));
 
 		Set<OWLClass> rootClasses = this.cepOntMgr.getReasoner().getSubClasses(owlThing, true).getFlattened();
 		
 		List<OwlClassBean> rootClassBeanList = new ArrayList<OwlClassBean>();
 
 		for(OWLClass c : rootClasses) {
 			OwlClassBean bean = mapOfClasses.get(c.getIRI().toString());
 			rootClassBeanList.add(bean);
 		}
 		
 
 		OwlOntologyBean ontologyBean = new OwlOntologyBean();
 		ontologyBean.setClassIndex(mapOfClasses);
 		
 		//get prefixes
 		ontologyBean.setPrefixes(this.cepOntMgr.getPm().getPrefixName2PrefixMap());
 
 		//individuals
 		ontologyBean.setClassIndex(mapOfClasses);
 		//ontologyBean.setIndividuals(mapOfIndividuals);
 		
 	
 		ontologyBean.setRootClasses(rootClassBeanList);
 		
 		
 		return ontologyBean;		
 	}
 
 	
 	
 	/**
 	 * Gets ontology bean representation. Processes the complete set of ontologies.
 	 * Then renders classes belonging to input ontology URI;
 	 */
 	public OwlOntologyBean getOntologyBean(String ontology_uri) throws OntologyInitException {
 		//assumes that the ontologies are all loaded...
 		Map<String,OwlClassBean> mapOfClasses = new HashMap<String,OwlClassBean>();
 		//Map<String,OwlIndividualBean> mapOfIndividuals = new HashMap<String,OwlIndividualBean>();
 
 		Set<OWLOntology> ontologies = this.cepOntMgr.getOntologies();
 		
 		Set<OWLOntology> completeSetOfOntologies = new HashSet<OWLOntology>();
 		
 		completeSetOfOntologies.addAll(ontologies);
 		
 		Map<String,OwlClassBean> mapOfClassesFromSelectedOntology = new HashMap<String,OwlClassBean>();
 		OWLOntology selectedOntology = this.cepOntMgr.getOntologyMgr().getOntology(IRI.create(ontology_uri));
 		
 		//classes
 		for(OWLOntology ont : ontologies) {
 			Map<String, OwlClassBean> classes = this.ontBeanFactory.doOntologyClassWalkingForBean(ont, false);
 			mapOfClasses.putAll(classes);
 			//mapOfIndividuals.putAll(this.doOntologyIndividualWalkingForBean(ont));
 			
 			OWLOntologyID currOntId = ont.getOntologyID();
 			if(currOntId != null && currOntId.getDefaultDocumentIRI() != null
 					&& !currOntId.isAnonymous()) {
 				IRI currOntDocIri = currOntId.getDefaultDocumentIRI();
 				
 				if(currOntDocIri != null) {
 					if(ontology_uri != null && ontology_uri.equals(currOntDocIri.toString())) {
 						mapOfClassesFromSelectedOntology.putAll(classes);						
 					}
 				}
 
 
 			}
 			
 			Set<OWLOntology> importedOntologies = ont.getImports();
 			completeSetOfOntologies.addAll(importedOntologies);
 			
 			
 			for(OWLOntology importedOntology : importedOntologies) {
 				Map<String,OwlClassBean> importedListOfClasses = this.ontBeanFactory.doOntologyClassWalkingForBean(importedOntology, false);
 				//Map<String,OwlIndividualBean> importedListOfInstances = this.doOntologyIndividualWalkingForBean(importedOntology);
 				
 				mapOfClasses.putAll(importedListOfClasses);
 				
 				//mapOfIndividuals.putAll(importedListOfInstances);
 				
 			}
 		}
 		
 		
 		
 		OWLClass owlThing =  this.cepOntMgr.getDataFactory().getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#Thing"));
 
 		if(mapOfClassesFromSelectedOntology.size() > 0) {
 			
 			//OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
 	        //OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
 			//OWLReasonerFactory reasonerFactory = HermitReasonerFactory.getInstance();
 	        //OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
 
 	        //ReasonerFactory reasonerFactory = new ReasonerFactory();
 			//OWLReasonerFactory reasonerFactory = new OWLReasonerFactory();
 			//StructuralReasonerFactory f;
 			
 			ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
 			//OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor, 30000);
 			OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
 
 /* DISABLE REASONING FOR NOW
 			OWLReasoner selectedOntReasoner; 
 			selectedOntReasoner = reasonerFactory.createReasoner(selectedOntology, config);
 			
 			
 	        selectedOntReasoner.precomputeInferences();
 			
 			Set<OWLClass> rootClasses = selectedOntReasoner.getSubClasses(owlThing, true).getFlattened();
 			
 			List<OwlClassBean> rootClassBeanList = new ArrayList<OwlClassBean>();
 
 			for(OWLClass c : rootClasses) {
 				OwlClassBean bean = mapOfClasses.get(c.getIRI().toString());
 				rootClassBeanList.add(bean);
 			}
 			
 
 			OwlOntologyBean ontologyBean = new OwlOntologyBean();
 			ontologyBean.setClassIndex(mapOfClasses);
 
 			//individuals
 			ontologyBean.setClassIndex(mapOfClasses);
 			//ontologyBean.setIndividuals(mapOfIndividuals);
 			
 		
 			ontologyBean.setRootClasses(rootClassBeanList);
 			
 			
 			return ontologyBean;		
 	*/
 		}
 		
 		return null;
 	}
 
 	
 	public List<String> getSubClasses(String iri_string) throws OntologyInitException {
 		//assumes that the ontologies are all loaded...
 		List<String> listOfSubClasses = new ArrayList<String>();
 
 		Set<OWLOntology> ontologies = this.cepOntMgr.getOntologies();
 		
 		for(OWLOntology ont : ontologies) {		
 			listOfSubClasses.addAll(this.ontBeanFactory.doOntologySubClassWalking(iri_string, ont));
 
 			for(OWLOntology importedOntology : ont.getImports()) {
 				List<String> importedListOfClasses = this.ontBeanFactory.doOntologySubClassWalking(iri_string, importedOntology);
 				listOfSubClasses.addAll(importedListOfClasses);
 			}
 		}
 
 		SortedSet<String> setOfSubClasses = Collections.synchronizedSortedSet(new TreeSet<String>(listOfSubClasses));
 
 		for(String item : setOfSubClasses) {
 			System.out.println("Subclass: " + item);
 		}
 		
 		//return (String[]) setOfSubClasses.toArray(new String[0]);
 		return new ArrayList<String>(setOfSubClasses);
 	}
 	
 	public OwlClassBean getOntClass(String iri_string) throws OntologyInitException {
 		boolean loadIndividuals = false, loadParents = false , loadChildren = false;
 		
         OWLClass ont_cls = this.cepOntMgr.getDataFactory().getOWLClass(IRI.create(iri_string));
         return this.ontBeanFactory.convertToOwlClassBean(ont_cls, 0, loadIndividuals, loadParents, loadChildren);		
 	}
 	
 	
 	
 	public OwlClassBean getOntClass(String iri_string, boolean loadIndividuals, boolean loadParents, boolean loadChildren) {
         OWLClass ont_cls = this.cepOntMgr.getDataFactory().getOWLClass(IRI.create(iri_string));
         
         return this.ontBeanFactory.convertToOwlClassBean(ont_cls, 0, loadIndividuals, loadParents, loadChildren);		
 	}
 	
 
 	public List<OwlIndividualBean> getOntIndividualsForClass(String class_iri_string)
 			throws OntologyInitException {
 		
 		return getOntIndividualsForClass(class_iri_string, false);		
 	}
 	
 	/**
 	 * Gets the individuals for the input OWLClass IRI string
 	 */
 	public List<OwlIndividualBean> getOntIndividualsForClass(String class_iri_string, boolean isDirect)
 			throws OntologyInitException {
 	
 		
 		return this.ontBeanFactory.getOntIndividualsForClass(class_iri_string, isDirect);		
 	}
 	
 	public List<OwlIndividualBean> getOntIndividuals(List<String> indivIriList, boolean skipPropMappings, boolean resolveParents)
 			throws OntologyInitException {
 	
 		List<OwlIndividualBean> beanList = new ArrayList<OwlIndividualBean>();
 		
 		for(String indivIri : indivIriList) {
 			beanList.add(this.ontBeanFactory.getOntIndividual(indivIri, skipPropMappings, resolveParents));
 		}
 		
 		return beanList;		
 	}
 	
 	public OwlIndividualBean getOntIndividual(String indivIri, boolean skipPropMappings, boolean resolveParents)
 			throws OntologyInitException {
 	
 		return this.ontBeanFactory.getOntIndividual(indivIri, skipPropMappings, resolveParents);
 
 	}
 	
 	
 
 	/*
 	public boolean addIndividual(OwlClassBean clsBean, GSNVirtualSensorInfoBean bean) {
 
 		OWLDataFactory dataFactory = this.cepOntMgr.getDataFactory();
 		IRI iri= IRI.create(clsBean.getIri());
 		OWLClass cls = dataFactory.getOWLClass(iri);
 		
 		
         OWLNamedIndividual vsensorIndividual = dataFactory.getOWLNamedIndividual(":"+bean.getName(), this.cepOntMgr.getPm());
         OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(cls, vsensorIndividual);
 
 
         List<OWLOntologyChange> changes = this.cepOntMgr.getOntologyMgr().addAxiom(this.cepOntMgr.getDefaultOntology(), classAssertion);
 		
         
         this.cepOntMgr.getOntologyMgr().applyChanges(changes);
 		
 		
 		List<OWLOntologyChange> reasonerPendingChanges = this.cepOntMgr.getReasoner().getPendingChanges();
 		System.out.println("Pending changes");
 		for(OWLOntologyChange change : reasonerPendingChanges) {
 			System.out.println(change);
             System.out.println();      
 		}
 		
 		Set<OWLAxiom> reasonerPendingAdditions = this.cepOntMgr.getReasoner().getPendingAxiomAdditions();
 		System.out.println("Pending axiom additions");
 		for(OWLAxiom ax : reasonerPendingAdditions) {
 			System.out.println(ax);
             System.out.println(); 
 		}
 		
 		this.cepOntMgr.getReasoner().flush();
 		
 		reasonerPendingChanges = this.cepOntMgr.getReasoner().getPendingChanges();
 		System.out.println("Pending changes after flush");
 		for(OWLOntologyChange change : reasonerPendingChanges) {
 			System.out.println(change);
             System.out.println();      
 		}
 		
 		reasonerPendingAdditions = this.cepOntMgr.getReasoner().getPendingAxiomAdditions();
 		System.out.println("Pending axiom additions after flush");
 		for(OWLAxiom ax : reasonerPendingAdditions) {
 			System.out.println(ax);
             System.out.println(); 
 		}
 		
 		return true;
 	}
 	*/
 	/*
 	public boolean addIndividual(OwlClassBean clsBean, GSNVirtualSensorInfoBean bean, List<OwlObjectPropertyBean> properties) {
 
 		OWLDataFactory dataFactory = this.cepOntMgr.getDataFactory();
 		
 		IRI iri= IRI.create(clsBean.getIri());
 		OWLClass cls = dataFactory.getOWLClass(iri);
 		
 		
         OWLNamedIndividual vsensorIndividual = dataFactory.getOWLNamedIndividual(":"+bean.getName(), this.cepOntMgr.getPm());
         OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(cls, vsensorIndividual);
 
         List<OWLOntologyChange> changes = this.cepOntMgr.getOntologyMgr().addAxiom(this.cepOntMgr.getDefaultOntology(), classAssertion);
 
         
         for(OwlObjectPropertyBean p : properties ) {
             OWLObjectProperty owlprop = dataFactory.getOWLObjectProperty(IRI.create(p.getPropertyIri()));
             OWLIndividual target = dataFactory.getOWLNamedIndividual(IRI.create(p.getFillerIri()));
 
             
             if(p.getPropertyIri().equals("http://www.csiro.au/cep/chaffey-data-streams.owl#hasFieldMapping")) {
             	OWLIndividual anonFieldMapping = dataFactory.getOWLAnonymousIndividual();
             	
             	
             }
             
             OWLIndividual source = vsensorIndividual;
             if(p.getSourceIri() != null && p.getSourceIri().length()!=0) {
                 source = dataFactory.getOWLNamedIndividual(IRI.create(p.getFillerIri()));
 
             }
             
             
         	OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(owlprop, source, target);
             AddAxiom addPropAxiomChange = new AddAxiom(this.cepOntMgr.getDefaultOntology(), assertion);
             changes.add(addPropAxiomChange);
         }
         
         
         
 
 		
         this.cepOntMgr.getOntologyMgr().applyChanges(changes);
 		
 		
 		List<OWLOntologyChange> reasonerPendingChanges = this.cepOntMgr.getReasoner().getPendingChanges();
 		System.out.println("Pending changes");
 		for(OWLOntologyChange change : reasonerPendingChanges) {
 			System.out.println(change);
             System.out.println();      
 		}
 		
 		Set<OWLAxiom> reasonerPendingAdditions = this.cepOntMgr.getReasoner().getPendingAxiomAdditions();
 		System.out.println("Pending axiom additions");
 		for(OWLAxiom ax : reasonerPendingAdditions) {
 			System.out.println(ax);
             System.out.println(); 
 		}
 		
 		this.cepOntMgr.getReasoner().flush();
 		
 		reasonerPendingChanges = this.cepOntMgr.getReasoner().getPendingChanges();
 		System.out.println("Pending changes after flush");
 		for(OWLOntologyChange change : reasonerPendingChanges) {
 			System.out.println(change);
             System.out.println();      
 		}
 		
 		reasonerPendingAdditions = this.cepOntMgr.getReasoner().getPendingAxiomAdditions();
 		System.out.println("Pending axiom additions after flush");
 		for(OWLAxiom ax : reasonerPendingAdditions) {
 			System.out.println(ax);
             System.out.println(); 
 		}
 		
 		return true;
 	}
 	*/
 	
 	/*
 	public boolean addSensorMappingIndividual(OwlClassBean clsBean, GSNVirtualSensorInfoBean bean, List<OwlObjectPropertyBean> properties,
 			List<FieldMappingBean> fieldMappings) {
 
 		OWLDataFactory dataFactory = this.cepOntMgr.getDataFactory();
 		IRI iri= IRI.create(clsBean.getIri());
 		OWLClass cls = dataFactory.getOWLClass(iri);
 		
 		
         OWLNamedIndividual vsensorIndividual = dataFactory.getOWLNamedIndividual(":"+bean.getName(), this.cepOntMgr.getPm());
         OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(cls, vsensorIndividual);
 
         List<OWLOntologyChange> changes = this.cepOntMgr.getOntologyMgr().addAxiom(this.cepOntMgr.getDefaultOntology(), classAssertion);
 
         OWLIndividual source = vsensorIndividual;
 
         for(OwlObjectPropertyBean p : properties ) {
             OWLObjectProperty owlprop = dataFactory.getOWLObjectProperty(IRI.create(p.getPropertyIri()));
             OWLIndividual target = dataFactory.getOWLNamedIndividual(IRI.create(p.getFillerIri()));
 
             if(p.getSourceIri() != null && p.getSourceIri().length()!=0) {
                 source = dataFactory.getOWLNamedIndividual(IRI.create(p.getFillerIri()));
             }
             
         	OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(owlprop, source, target);
             AddAxiom addPropAxiomChange = new AddAxiom(this.cepOntMgr.getDefaultOntology(), assertion);
             changes.add(addPropAxiomChange);
         }
         
         
         for(FieldMappingBean fmb : fieldMappings ) {
         		OWLObjectProperty fieldMappingProp = dataFactory.getOWLObjectProperty(
         				IRI.create(WqCepOntologyConstants.HAS_FIELD_MAPPING));
 
         		OWLIndividual anonFieldMappingIndiv = dataFactory.getOWLAnonymousIndividual();
             	
         		OWLClass gsnFieldMappingCls = dataFactory.getOWLClass(
         				IRI.create(WqCepOntologyConstants.GSN_FIELD_MAPPING_CLS));
         		OWLObjectProperty isRelatedObsProp = dataFactory.getOWLObjectProperty(
         				IRI.create(WqCepOntologyConstants.IS_REL_OBSERVED_PROPERTY));
         		OWLDataProperty isFieldName = dataFactory.getOWLDataProperty(
         				IRI.create(WqCepOntologyConstants.HAS_GSN_FIELDNAME));
 
         		//assert that there is an anon field mapping individual of GsnFieldMapping type
                 OWLClassAssertionAxiom newInstanceOfFieldMappingAssertion = 
                 		dataFactory.getOWLClassAssertionAxiom(gsnFieldMappingCls, anonFieldMappingIndiv);
                 AddAxiom addAnonIndivAxiom = new AddAxiom(this.cepOntMgr.getDefaultOntology(), newInstanceOfFieldMappingAssertion);
                 changes.add(addAnonIndivAxiom);
                 
                 
               //assert that the anon indiv has properties
         		
         		OWLIndividual obsPropIndiv = dataFactory.getOWLNamedIndividual(IRI.create(fmb.getObservedPropertyIri()));
 
         		OWLObjectPropertyAssertionAxiom isRelatedObsPropAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(
                 		isRelatedObsProp, anonFieldMappingIndiv, obsPropIndiv);
         		AddAxiom addPropAxiomChange1 = new AddAxiom(this.cepOntMgr.getDefaultOntology(), isRelatedObsPropAssertion);
         		changes.add(addPropAxiomChange1);
                 
         		OWLDataPropertyAssertionAxiom isFieldNameAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(
                 		isFieldName, anonFieldMappingIndiv,fmb.getVsensorFieldName());
         		AddAxiom addPropAxiomChange2 = new AddAxiom(this.cepOntMgr.getDefaultOntology(), isFieldNameAssertion);
         		changes.add(addPropAxiomChange2);
             	
         		//assert that the source sensor named indiv has a field mapping to the anon indiv
                 OWLObjectPropertyAssertionAxiom sensorFieldMappingPropAssertion = 
                 		dataFactory.getOWLObjectPropertyAssertionAxiom(fieldMappingProp, source, anonFieldMappingIndiv);
                 AddAxiom propAssertionAxiomChange = new AddAxiom(this.cepOntMgr.getDefaultOntology(), sensorFieldMappingPropAssertion);
                 changes.add(propAssertionAxiomChange);
                 
         }
 
         this.cepOntMgr.getOntologyMgr().applyChanges(changes);
 		
 		List<OWLOntologyChange> reasonerPendingChanges = this.cepOntMgr.getReasoner().getPendingChanges();
 		System.out.println("Pending changes");
 		for(OWLOntologyChange change : reasonerPendingChanges) {
 			System.out.println(change);
             System.out.println();      
 		}
 		
 		Set<OWLAxiom> reasonerPendingAdditions = this.cepOntMgr.getReasoner().getPendingAxiomAdditions();
 		System.out.println("Pending axiom additions");
 		for(OWLAxiom ax : reasonerPendingAdditions) {
 			System.out.println(ax);
             System.out.println(); 
 		}
 		
 		this.cepOntMgr.getReasoner().flush();
 		
 		reasonerPendingChanges = this.cepOntMgr.getReasoner().getPendingChanges();
 		System.out.println("Pending changes after flush");
 		for(OWLOntologyChange change : reasonerPendingChanges) {
 			System.out.println(change);
             System.out.println();      
 		}
 		
 		reasonerPendingAdditions = this.cepOntMgr.getReasoner().getPendingAxiomAdditions();
 		System.out.println("Pending axiom additions after flush");
 		for(OWLAxiom ax : reasonerPendingAdditions) {
 			System.out.println(ax);
             System.out.println(); 
 		}
 		
 		return true;
 	}
 	*/
 	
 	
 	public List<String> getRestrictions(String iri_string) {
 		
 		List<String> restrictionList = new ArrayList<String>();
 		RestrictionVisitor restrictionVisitor = new RestrictionVisitor(this.cepOntMgr.getOntologies(), this.cepOntMgr.getReasoner());
         // In this case, restrictions are used as (anonymous) superclasses, so to get the restrictions on
         // margherita pizza we need to obtain the subclass axioms for margherita pizza.
         
 		OWLClass cls = this.cepOntMgr.getDataFactory().getOWLClass(IRI.create(iri_string));
 		if(cls != null) {
 			
 			for (OWLSubClassOfAxiom ax : this.cepOntMgr.getDefaultOntology().getSubClassAxiomsForSubClass(cls)) {
 	            OWLClassExpression superCls = ax.getSuperClass();
 	            // Ask our superclass to accept a visit from the RestrictionVisitor - if it is an
 	            // existential restiction then our restriction visitor will answer it - if not our
 	            // visitor will ignore it
 	            superCls.accept(restrictionVisitor);
 	        }
 	        // Our RestrictionVisitor has now collected all of the properties that have been restricted in existential
 	        // restrictions - print them out.
 	        System.out.println("Restricted properties for " + cls + ": " + restrictionVisitor.getRestrictedProperties().size());
 	        for (OWLPropertyExpression prop : restrictionVisitor.getRestrictedProperties()) {
 	            System.out.println("    " + prop);
 	        }
 		
 		}
 		
 		return restrictionList; 
 	}
 	
 
 	public List<OwlClassBean> getOntSubClasses(String iri_string)
 			throws OntologyInitException {
 	
 		
 		boolean isDirect = true;
 		boolean loadIndiv = true;
 		boolean loadParents = false;
 		boolean loadChildren = true;
 		
 		
 		return this.getOntSubClasses(iri_string, isDirect, loadIndiv, loadParents, loadChildren);
 		
 	}
 
 	public List<OwlClassBean> getOntSubClasses(String iri_string, boolean isDirect,	boolean loadIndiv,  
 			boolean loadParents, boolean loadChildren )
 					throws OntologyInitException {
 
 
 		//assumes that the ontologies are all loaded...
 		Set<OWLClass> setOfSubClasses = new TreeSet<OWLClass>();
 
 		OWLClass sensorCls = this.cepOntMgr.getDataFactory().getOWLClass(IRI.create(iri_string));
 		NodeSet<OWLClass> subClses = this.cepOntMgr.getReasoner().getSubClasses(sensorCls, isDirect);
 		for(Node<OWLClass> cls : subClses.getNodes()) {
 			Set<OWLClass> entities = cls.getEntitiesMinusTop();
 			for(OWLClass ent : entities) {
 				if(!ent.isOWLNothing()) {
 					setOfSubClasses.add(ent);
 				}
 			}
 		}
 
 		//populate beans to return
 		ArrayList<OwlClassBean> beans = new ArrayList<OwlClassBean>();
 
 
 		for(OWLClass c : setOfSubClasses) {
 			//boolean loadIndiv = false;
 			//boolean loadChildren = false;
 			//boolean loadParents = false; 
 
 			OwlClassBean resultBean = this.ontBeanFactory.convertToOwlClassBean(c, 0, loadIndiv, loadParents, loadChildren);
 			if(resultBean != null) {
 				beans.add(resultBean);
 			}
 
 		}
 
 		return beans;		
 
 	}
 	
 
 
 
 	public List<OwlClassBean> getOntClasses(List<String> iriList) throws OntologyInitException {
 		return getOntClasses(iriList, false, false, false);		
 	}
 
 	public List<OwlClassBean> getOntClasses(List<String> iriList, boolean loadIndividuals, boolean loadParents, boolean loadChildren) throws OntologyInitException {
 		List<OwlClassBean> beans = new ArrayList<OwlClassBean>();
 		for(String curr_iri : iriList) {
 			OwlClassBean resultBean = this.getOntClass(curr_iri, loadIndividuals, loadParents, loadChildren);
 			if(resultBean != null) {
 				beans.add(resultBean);
 			}
 		}
 		return beans;
 	}
 
 	public List<SwrlBuiltInBean> getSwrlBuiltInFunctions() {
 		List<SwrlBuiltInBean> sList = new ArrayList<SwrlBuiltInBean>();
 		
 		for(SWRLBuiltInsVocabulary v : SWRLBuiltInsVocabulary.values()) {
 			sList.add(new SwrlBuiltInBean(v.getShortName(), v.getIRI().toString()));
 		}
 		
 		return sList;
 		
 		
 	}
 	
 	public boolean isSubclassOf(String iri, String parentIri, boolean isDirect) {
 		boolean result = false;
 
 
 		OWLReasoner reasoner = this.cepOntMgr.getReasoner();
 		
 		OWLClass parentOwlClass = this.cepOntMgr.getOwlClass(parentIri);
 		OWLClass candidateChildClass = this.cepOntMgr.getOwlClass(iri);
 		
 		
 		if(reasoner.getSubClasses(parentOwlClass, isDirect).containsEntity(candidateChildClass)) {
 			result = true;
 		}
 		
 		
 		return result;
 	}
 
 /*
 	public String generateVsensor(String vsName, WqCepRule rule) {
 		
 		return rp.generateVsensor(vsName, rule);
 	}
 */
 
 	public CepOntologyManager getCepOntMgr() {
 		return cepOntMgr;
 	}
 
 
 	public void setCepOntMgr(CepOntologyManager cepOntMgr) {
 		this.cepOntMgr = cepOntMgr;
 	}
 
 
 	public OntologyBeanFactory getOntBeanFactory() {
 		return ontBeanFactory;
 	}
 
 
 	public void setOntBeanFactory(OntologyBeanFactory ontBeanFactory) {
 		this.ontBeanFactory = ontBeanFactory;
 	}
 
 	@Override
 	public int addOwlIndividual(OwlIndividualBean indiv) throws OntologyInitException {
 		
 		return this.cepOntMgr.addOwlIndividual(indiv);
 		
 	}
 
 	@Override
	public int addDeleteIndividual(OwlIndividualBean indiv)
 			throws OntologyInitException {
 		
 		return this.cepOntMgr.deleteOwlIndividual(indiv);
 
 	}
 
 	
 }
