 package edu.utep.cybershare.vlc.ontology.axioms;
 
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLDataProperty;
 import org.semanticweb.owlapi.model.OWLLiteral;
 
 import edu.utep.cybershare.vlc.ontology.Institution;
 
 public class InstitutionAxioms extends AxiomSetter {
 	
 	public InstitutionAxioms(Institution anInstitution) {
 		super(anInstitution.getHasName(), Vocabulary.CLASS_IRI_Institution, anInstitution);
 		institution = anInstitution;
 	}
 
 	@Override
 	protected void populateIndividualAxioms() {
 		addHasLatitude();
 		addHasLongitude();
 		addHasNameAssertion();
 		addHasAddressAssertion();
 		addHasCityAssertion();
 		addHasStateAssertion();
 		addHasZipcodeAssertion();
 	}
 	
 	private void addHasLongitude(){
 		OWLDataProperty hasLongitude = bundle.getDataFactory().getOWLDataProperty(IRI.create(Vocabulary.DATA_PROPERTY_IRI_hasLongitude));		
 		OWLLiteral longitude = bundle.getDataFactory().getOWLLiteral(institution.getLongitude());
 		OWLAxiom assertion = bundle.getDataFactory().getOWLDataPropertyAssertionAxiom(hasLongitude, individual, longitude);
 		owlAxioms.add(assertion);		
 	}
 
 	private void addHasLatitude(){
 		OWLDataProperty hasLatitude = bundle.getDataFactory().getOWLDataProperty(IRI.create(Vocabulary.DATA_PROPERTY_IRI_hasLatitude));		
 		OWLLiteral latitude = bundle.getDataFactory().getOWLLiteral(institution.getLatitude());
 		OWLAxiom assertion = bundle.getDataFactory().getOWLDataPropertyAssertionAxiom(hasLatitude, individual, latitude);
 		owlAxioms.add(assertion);	
 	}
 	
 	private void addHasNameAssertion(){
 		OWLDataProperty hasName = bundle.getDataFactory().getOWLDataProperty(IRI.create(Vocabulary.DATA_PROPERTY_IRI_hasName));		
 		OWLLiteral name = bundle.getDataFactory().getOWLLiteral(institution.getHasName());
 		OWLAxiom assertion = bundle.getDataFactory().getOWLDataPropertyAssertionAxiom(hasName, individual, name);
 		owlAxioms.add(assertion);		
 	}
 	
 	private void addHasAddressAssertion(){
 		OWLDataProperty hasAddress = bundle.getDataFactory().getOWLDataProperty(IRI.create(Vocabulary.DATA_PROPERTY_IRI_hasAddress));		
 		OWLLiteral address = bundle.getDataFactory().getOWLLiteral(institution.getHasAddress());
 		OWLAxiom assertion = bundle.getDataFactory().getOWLDataPropertyAssertionAxiom(hasAddress, individual, address);
 		owlAxioms.add(assertion);		
 	}
 	
 	private void addHasCityAssertion(){
 		OWLDataProperty hasCity = bundle.getDataFactory().getOWLDataProperty(IRI.create(Vocabulary.DATA_PROPERTY_IRI_hasCity));		
		OWLLiteral city = bundle.getDataFactory().getOWLLiteral(institution.getHasCity());
 		OWLAxiom assertion = bundle.getDataFactory().getOWLDataPropertyAssertionAxiom(hasCity, individual, city);
 		owlAxioms.add(assertion);		
 	}
 	
 	private void addHasStateAssertion(){
 		OWLDataProperty hasState = bundle.getDataFactory().getOWLDataProperty(IRI.create(Vocabulary.DATA_PROPERTY_IRI_hasState));		
		OWLLiteral state = bundle.getDataFactory().getOWLLiteral(institution.getHasState());
 		OWLAxiom assertion = bundle.getDataFactory().getOWLDataPropertyAssertionAxiom(hasState, individual, state);
 		owlAxioms.add(assertion);		
 	}
 
 	private void addHasZipcodeAssertion(){
 		OWLDataProperty hasZipcode = bundle.getDataFactory().getOWLDataProperty(IRI.create(Vocabulary.DATA_PROPERTY_IRI_hasZipcode));		
		OWLLiteral zipcode = bundle.getDataFactory().getOWLLiteral(institution.getHasZipCode());
 		OWLAxiom assertion = bundle.getDataFactory().getOWLDataPropertyAssertionAxiom(hasZipcode, individual, zipcode);
 		owlAxioms.add(assertion);
 	}
}
