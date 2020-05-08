 package org.knoesis.umlstoicd10.ontology;
 
 import java.io.File;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAnnotation;
 import org.semanticweb.owlapi.model.OWLAnnotationProperty;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLLiteral;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.reasoner.InferenceType;
 import org.semanticweb.owlapi.reasoner.NodeSet;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
 
 import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
 
 /**
  * This class is used to load the ontology and provide with all the
  * utility methods to get various elements of ontology.
  * @author koneru
  *
  */
 public class OntologyLoader {
 	
 	private OWLOntologyManager manager;
 	private OWLOntology ontology = null;
 	private OWLReasoner reasoner;
 	
 	/* Here in our application as of now, we are using only these two properties */
 	OWLAnnotationProperty comment;
 	OWLAnnotationProperty isDefinedBy;
 	
 	OWLDataFactory factory = null;
 	/** This ontology IRI is used to get the subclasses and in many other cases*/
 	private String ontologyIRI;
 	private static Log log = LogFactory.getLog("OntologyLoader logger");
 	
 	/**
 	 * Contructor: Here we load the ontology and initializing pellet reasoner.
 	 *             Also making reasoner to pre-compute hierarchical inference.
 	 * @param ontologyFile
 	 */
 	public OntologyLoader(File ontologyFile){
 		OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
 		/*Loading the ontology*/
 		manager = OWLManager.createOWLOntologyManager();
 		try {
 			ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
 			ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
 			factory = manager.getOWLDataFactory();
 			comment = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
 			isDefinedBy = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_IS_DEFINED_BY.getIRI());
 		} catch (OWLOntologyCreationException e) {
 			log.error("Unable to load the Ontology.");
 			e.printStackTrace();
 		}
 		reasoner = reasonerFactory.createReasoner(ontology);
 		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
 	}
 	
 	/**
 	 * This method given an icd10 code will return you all the subclasses in the hierarchy
 	 * using the reasoner.
 	 * @param indexLevelIcdCode
 	 * @return {@link NodeSet} of all subClasses
 	 */
 	public NodeSet<OWLClass> getSubClasses(String indexLevelIcdCode){
 		NodeSet<OWLClass> subClasses = null;
 		IRI startingClassIRI = IRI.create(ontologyIRI + "#" + indexLevelIcdCode);
 		OWLClass startingClass = factory.getOWLClass(startingClassIRI);
 		subClasses = reasoner.getSubClasses(startingClass, true);
 		return subClasses;
 	}
 	
 	/**
 	 * This method retrieves the SPARQL query, for a given class, from its
 	 * comments.
 	 * 
 	 * NOTE We are storing SPARQL query as an annotation into comments in our
 	 *      Ontology.
 	 * @return
 	 */
 	public String getSparqlQueryFromComment(OWLClass clazz){
 		OWLLiteral sparqlQueryLiteral = null;
 		String sparqlQuery = null;
 		if(ontology.containsClassInSignature(clazz.getIRI())){
 			Set<OWLAnnotation> annotations = clazz.getAnnotations(ontology, comment);
 			if(!annotations.isEmpty())
 				for(OWLAnnotation annotation : annotations){
 					sparqlQueryLiteral = (OWLLiteral)annotation.getValue();
 					sparqlQuery = sparqlQueryLiteral.getLiteral();
 				}
 			else
 				log.error("Please check the class there is no comments annotation");
 		} else
 			log.error("No such class in this ontology -- Please check");
 		return sparqlQuery;
 	}
 	
 	/**
 	 * This method retrieves the condition that should be checked for the SPARQL query.
 	 * 
 	 * NOTE Here we are storing it as 'isDefinedBy' annotation with boolean type in the
 	 *      Ontology
 	 * @param clazz
 	 * @return {@link Boolean} condition that the SPARQL query should conform to
 	 */
 	public Boolean getBooleanConditionForSparqlQuery(OWLClass clazz){
 		Boolean flag = false;
 		if(ontology.containsClassInSignature(clazz.getIRI())){
 			Set<OWLAnnotation> annotations = clazz.getAnnotations(ontology, isDefinedBy);
 			if(!annotations.isEmpty())
 				for(OWLAnnotation annotation : annotations){
 					OWLLiteral booleanLiteral = (OWLLiteral) annotation.getValue();
 					String booleanValue = booleanLiteral.getLiteral();
 					flag = Boolean.valueOf(booleanValue);
 				}
 			else
				log.error("Please check the class there is no comments annotation");
 		} else
 			log.error("No such class in this ontology -- Please check");
 		return flag;
 	}
 	/**
 	 * This method just returns the class for a given string.
 	 * @param className
 	 * @return
 	 */
 	public OWLClass getClass(String className){
 		IRI classIRI = IRI.create(ontologyIRI + "#" + className);
 		return factory.getOWLClass(classIRI);
 	}
 	
 	public OWLOntologyManager getManager() {
 		return manager;
 	}
 	public void setManager(OWLOntologyManager manager) {
 		this.manager = manager;
 	}
 	public OWLOntology getOntology() {
 		return ontology;
 	}
 	public void setOntology(OWLOntology ontology) {
 		this.ontology = ontology;
 	}
 	public OWLReasoner getReasoner() {
 		return reasoner;
 	}
 	public void setReasoner(OWLReasoner reasoner) {
 		this.reasoner = reasoner;
 	}
 	
 	public static void main(String[] args) {
 		File ontologyFile = new File("data/ICD10Initial.owl");
 		OntologyLoader ontoLoader = new OntologyLoader(ontologyFile);
 //		System.out.println(ontoLoader.getSubClasses("E08"));
 		System.out.println(ontoLoader.getBooleanConditionForSparqlQuery(ontoLoader.getClass("E08.0")));
 	}
 }
