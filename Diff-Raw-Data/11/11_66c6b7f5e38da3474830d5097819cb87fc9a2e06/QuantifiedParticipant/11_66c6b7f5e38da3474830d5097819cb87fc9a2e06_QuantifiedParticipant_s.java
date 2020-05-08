 package org.arachb.owlbuilder.lib;
 
 import java.sql.SQLException;
 
 import org.semanticweb.owlapi.model.OWLObject;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 
 public class QuantifiedParticipant extends Participant {
 
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
				throw new IllegalStateException(BADTAXONIRI + taxon);
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
				throw new IllegalStateException(BADANATOMYIRI + taxon);
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
				throw new IllegalStateException(BADSUBSTRATEIRI + taxon);
 			}
 		}
 	}
 
 	@Override
 	public OWLObject generateOWL(OWLOntology o, OWLOntologyManager manager,	IRIManager iriManager) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
