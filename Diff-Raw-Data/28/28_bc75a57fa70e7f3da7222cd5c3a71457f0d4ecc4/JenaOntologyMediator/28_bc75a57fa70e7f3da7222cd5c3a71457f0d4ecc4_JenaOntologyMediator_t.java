 package au.csiro.eis.ontology.resource.sparql;
 
 import java.util.List;
 
 import au.csiro.eis.ontology.OntologyMediatorInterface;
 import au.csiro.eis.ontology.beans.OwlClassBean;
 import au.csiro.eis.ontology.beans.OwlIndividualBean;
 import au.csiro.eis.ontology.beans.OwlOntologyBean;
 import au.csiro.eis.ontology.beans.SwrlBuiltInBean;
 import au.csiro.eis.ontology.exception.OntologyInitException;
 
 //TODO: Fill in these classes with Jena lib equivalents
 public class JenaOntologyMediator implements OntologyMediatorInterface {
 
 	public JenaOntologyMediator(){
 		
 	}
 
 	public List<String> getClasses() throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public OwlOntologyBean getOntologyBean() throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public OwlOntologyBean getOntologyBean(String ontology_uri)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<String> getSubClasses(String iri_string)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public OwlClassBean getOntClass(String iri_string)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public OwlClassBean getOntClass(String iri_string, boolean loadIndividuals,
 			boolean loadParents, boolean loadChildren) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<OwlIndividualBean> getOntIndividualsForClass(
 			String class_iri_string) throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<OwlIndividualBean> getOntIndividualsForClass(
 			String class_iri_string, boolean isDirect)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<OwlIndividualBean> getOntIndividuals(
 			List<String> indiv_iri_string, boolean skipPropMappings,
 			boolean resolveParents) throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<String> getRestrictions(String iri_string) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<OwlClassBean> getOntSubClasses(String iri_string)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<OwlClassBean> getOntSubClasses(String iri_string,
 			boolean isDirect, boolean loadIndiv, boolean loadParents,
 			boolean loadChildren) throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<OwlClassBean> getOntClasses(List<String> iriList)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<OwlClassBean> getOntClasses(List<String> iriList,
 			boolean loadIndividuals, boolean loadParents, boolean loadChildren)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<SwrlBuiltInBean> getSwrlBuiltInFunctions() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean isSubclassOf(String iri, String parentIri, boolean isDirect) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public OwlIndividualBean getOntIndividual(String indivIri,
 			boolean skipPropMappings, boolean resolveParents)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int addOwlIndividual(OwlIndividualBean indiv) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
	public int deleteOwlIndividual(OwlIndividualBean indiv)
 			throws OntologyInitException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	
 	
 	
 	
 
 }
