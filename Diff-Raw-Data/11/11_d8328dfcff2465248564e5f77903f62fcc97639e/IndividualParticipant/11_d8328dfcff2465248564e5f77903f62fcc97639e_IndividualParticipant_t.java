 package org.arachb.owlbuilder.lib;
 
 import java.sql.SQLException;
 
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLObject;
 import org.semanticweb.owlapi.model.OWLObjectProperty;
 import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 
 public class IndividualParticipant extends Participant implements AbstractNamedEntity{
 
 	static final private String ROWUPDATE = "UPDATE participant " +
 	"SET generated_id = ? WHERE id = ?";
 	
 	//maybe make this a constructor
 	public void fill(AbstractResults record) throws SQLException{
 		id = record.getInt(DBID);
 		taxon = record.getInt(DBTAXON);
 		substrate = record.getInt(DBSUBSTRATE);
 		anatomy = record.getInt(DBANATOMY);
 		quantification = record.getString(DBQUANTIFICATION);
 		label = record.getString(DBLABEL);
 		generated_id = record.getString(DBGENERATEDID);
 		publication_taxon = record.getString(DBPUBLICATIONTAXON);
 		publication_anatomy = record.getString(DBPUBLICATIONANATOMY);
 		publication_substrate = record.getString(DBPUBLICATIONSUBSTRATE);
 		if (taxon != 0){
 			if (record.getString(DBTAXONSOURCEID) != null){
 				this.set_taxonIRI(record.getString(DBTAXONSOURCEID));
 			}
 			else if (record.getString(DBTAXONGENERATEDID) != null){
 				this.set_taxonIRI(record.getString(DBTAXONGENERATEDID));
 			}
 			else{
				final String msg = String.format(BADTAXONIRI, id, taxon);
				throw new IllegalStateException(msg);
 			}
 		}
 		if (anatomy != 0){
 			if (record.getString(DBANATOMYSOURCEID) != null){
 				this.set_anatomyIRI(record.getString(DBANATOMYSOURCEID));
 			}
 			else if (record.getString(DBANATOMYGENERATEDID) != null){
 				this.set_anatomyIRI(record.getString(DBANATOMYGENERATEDID));
 			}
 			else{
				final String msg = String.format(BADANATOMYIRI, id, anatomy);
				throw new IllegalStateException(msg);
 			}
 		}
 		if (substrate != 0){
 			if (record.getString(DBSUBSTRATESOURCEID) != null){
 				this.set_substrateIRI(record.getString(DBSUBSTRATESOURCEID));
 			}
 			else if (record.getString(DBSUBSTRATEGENERATEDID) != null){
 				this.set_substrateIRI(record.getString(DBSUBSTRATEGENERATEDID));
 			}
 			else{
				final String msg = String.format(BADSUBSTRATEIRI, id, substrate);
				throw new IllegalStateException(msg);
 			}
 		}
 	}
 		
 	public String getUpdateStatement(){
 		return IndividualParticipant.ROWUPDATE;
 	}
 	
 	public String get_generated_id(){
 		return generated_id;
 	}
 	
 	public String get_available_id(){
 		return get_generated_id();
 	}
 
 	@Override
 	public OWLObject generateOWL(OWLOntology o, OWLOntologyManager manager, IRIManager iriManager) {
 		final OWLDataFactory factory = manager.getOWLDataFactory();
 		final OWLObjectProperty partofProperty = factory.getOWLObjectProperty(IRIManager.partOfProperty);
 		IRI individual_id = IRI.create(getIRI_String());
 		OWLIndividual part = factory.getOWLNamedIndividual(individual_id);
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private OWLObject generateAnatomyOWL(OWLOntology o, OWLOntologyManager manager, IRIManager iriManager){
 		OWLObject taxonObject = generateTaxonOWL(o,manager,iriManager);
 		return null;
 	}
 	
 	private OWLObject generateTaxonOWL(OWLOntology o, OWLOntologyManager manager, IRIManager iriManager){
 		return null;
 	}
 	
 	private OWLObject generateSubstrateOWL(OWLOntology o, OWLOntologyManager manager, IRIManager iriManager){
 		return null;
 	}
 	
 	@Override
 	public void setGeneratedID(String id) {
 		generated_id = id;
 	}
 
 	@Override
 	public String getIRI_String() {
 		return get_generated_id();
 	}
 	
 
 }
