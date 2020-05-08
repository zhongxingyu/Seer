 package au.csiro.eis.ontology;
 
 import java.util.List;
 import au.csiro.eis.ontology.beans.OwlClassBean;
 import au.csiro.eis.ontology.beans.OwlIndividualBean;
 import au.csiro.eis.ontology.beans.OwlObjectPropertyBean;
 import au.csiro.eis.ontology.beans.OwlOntologyBean;
 import au.csiro.eis.ontology.beans.SwrlBuiltInBean;
 import au.csiro.eis.ontology.exception.OntologyInitException;
 
 public interface OntologyMediatorInterface {
 	
 	public List<String> getClasses() throws OntologyInitException;	
 
 	public OwlOntologyBean getOntologyBean() throws OntologyInitException ;
 	
 	public OwlOntologyBean getOntologyBean(String ontology_uri) throws OntologyInitException ;
 		
 	
 	public List<String> getSubClasses(String iri_string) throws OntologyInitException ;
 	public OwlClassBean getOntClass(String iri_string) throws OntologyInitException ;
 
 	public OwlClassBean getOntClass(String iri_string, boolean loadIndividuals, boolean loadParents, boolean loadChildren) ;
 
 	public List<OwlIndividualBean> getOntIndividualsForClass(String class_iri_string)
 			throws OntologyInitException ;
 
 	public List<OwlIndividualBean> getOntIndividualsForClass(String class_iri_string, boolean isDirect)
 			throws OntologyInitException ;
 	
 	public List<OwlIndividualBean> getOntIndividuals(List<String> indiv_iri_string, boolean skipPropMappings, boolean resolveParents)
 			throws OntologyInitException ;
 	
 	public OwlIndividualBean getOntIndividual(String indivIri, boolean skipPropMappings, boolean resolveParents)
 			throws OntologyInitException ;
 		/*
 	
 	public boolean addIndividual(OwlClassBean clsBean, GSNVirtualSensorInfoBean bean) ;
 
 	public boolean addIndividual(OwlClassBean clsBean, GSNVirtualSensorInfoBean bean, List<OwlObjectPropertyBean> properties) ;
 	
 	public boolean addSensorMappingIndividual(OwlClassBean clsBean, GSNVirtualSensorInfoBean bean, List<OwlObjectPropertyBean> properties,
 			List<FieldMappingBean> fieldMappings) ;
 	*/
 	
 	public List<String> getRestrictions(String iri_string) ;
 	
 
 	public List<OwlClassBean> getOntSubClasses(String iri_string)
 			throws OntologyInitException ;
 		
 	public List<OwlClassBean> getOntSubClasses(String iri_string, boolean isDirect,	boolean loadIndiv,  
 			boolean loadParents, boolean loadChildren )			throws OntologyInitException ;
 		
 	public List<OwlClassBean> getOntClasses(List<String> iriList) throws OntologyInitException ;
 
 	public List<OwlClassBean> getOntClasses(List<String> iriList, boolean loadIndividuals, boolean loadParents, boolean loadChildren) throws OntologyInitException ;
 
 	public List<SwrlBuiltInBean> getSwrlBuiltInFunctions() ;
 	
 	public boolean isSubclassOf(String iri, String parentIri, boolean isDirect) ;
 
 	public int addOwlIndividual(OwlIndividualBean indiv) throws OntologyInitException;
 

	public int deleteOwlIndividual(OwlIndividualBean indiv) throws OntologyInitException;
 
 	
 	//public String generateVsensor(String vsName, WqCepRule rule) ;
 }
