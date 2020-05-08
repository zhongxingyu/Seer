 package org.arachb.owlbuilder.lib;
 
 import java.sql.SQLException;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.arachb.owlbuilder.Owlbuilder;
 import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassAxiom;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 
 public abstract class Participant implements AbstractEntity{
 
 	
 	static final private String PRIMARYQUERY = "SELECT part.id, " + 
 	"part.taxon, part.substrate, part.anatomy, " +
 	"part.quantification, part.generated_id, part.publication_taxon, " +
 	"part.label, part.publication_anatomy, part.publication_substrate, " +
 	"taxon.source_id, taxon.generated_id, " +
 	"substrate.source_id, substrate.generated_id, " +
 	"anatomy.source_id, anatomy.generated_id " +
 	"FROM participant2assertion as p2a " + 
 	"JOIN participant AS part ON (p2a.participant = part.id) " +
 	"LEFT JOIN term AS taxon ON (part.taxon = taxon.id) " +
 	"LEFT JOIN term AS substrate ON (part.substrate = substrate.id) " +
 	"LEFT JOIN term AS anatomy ON (part.anatomy = anatomy.id) " +
 	"WHERE p2a.assertion = ? AND p2a.participant_index = 1";
 	
 	static final private String RESTQUERY = "SELECT part.id, " + 
 	"part.taxon, part.substrate, part.anatomy, " +
 	"part.quantification, part.generated_id, part.publication_taxon, " +
 	"part.label, part.publication_anatomy, part.publication_substrate, " +
 	"taxon.source_id, taxon.generated_id, " +
 	"substrate.source_id, substrate.generated_id, " +
 	"anatomy.source_id, anatomy.generated_id " +
 	"FROM participant2assertion as p2a " + 
 	"JOIN participant AS part ON (p2a.participant = part.id) " +
 	"LEFT JOIN term AS taxon ON (part.taxon = taxon.id) " +
 	"LEFT JOIN term AS substrate ON (part.substrate = substrate.id) " +
 	"LEFT JOIN term AS anatomy ON (part.anatomy = anatomy.id) " +
 	"WHERE p2a.assertion = ? AND p2a.participant_index > 1";
 
 	
 	final static int DBID = 1;
 	final static int DBTAXON = 2;
 	final static int DBSUBSTRATE = 3;
 	final static int DBANATOMY = 4;
 	final static int DBQUANTIFICATION = 5;
 	final static int DBGENERATEDID = 6;
 	final static int DBPUBLICATIONTAXON = 7;
 	final static int DBLABEL = 8;
 	final static int DBPUBLICATIONANATOMY = 9;
 	final static int DBPUBLICATIONSUBSTRATE = 10;
 	final static int DBTAXONSOURCEID = 11;
 	final static int DBTAXONGENERATEDID = 12;
     final static int DBSUBSTRATESOURCEID = 13;
     final static int DBSUBSTRATEGENERATEDID = 14;
 	final static int DBANATOMYSOURCEID = 15;
     final static int DBANATOMYGENERATEDID = 16;
 
 	final static String BADTAXONIRI =
 			"Term without IRI referenced as participant taxon: participant id = %s; taxon id = %s";
 	final static String BADANATOMYIRI =
 			"Term without IRI referenced as participant anatomy: participant id = %s; anatomy id = %s";
 	final static String BADSUBSTRATEIRI =
 			"Term without IRI referenced as participant substrate; participant id = %s; substrate id = %s";
 	
     
 	private int id;
 	private int taxon;
 	private int substrate;
 	private int anatomy;
 	private String quantification;
 	private String generatedId;
 	private String publicationTaxon;
 	private String label;
 	private String publicationAnatomy;
 	private String publicationSubstrate;
 	private String taxonIRI = null;
 	private String substrateIRI = null;
 	private String anatomyIRI = null;
 	
 	public static String getPrimaryQuery(){
 		return Participant.PRIMARYQUERY;
 	}
 	
 	public static String getRestQuery(){
 		return Participant.RESTQUERY;
 	}
 	
 	private static String INDIVIDUALQUANTIFIER = "INDIVIDUAL";
 	private static String SOMEQUANTIFIER = "SOME";
 	
 	private static Logger log = Logger.getLogger(Participant.class);
 
 	
 	public static Participant makeParticipant(AbstractResults record) throws SQLException{
 		final String quantification = record.getString(DBQUANTIFICATION);
 		if (INDIVIDUALQUANTIFIER.equalsIgnoreCase(quantification)){
 			IndividualParticipant result = new IndividualParticipant();
 			result.fill(record);
 			return result;
 		}
 		else if (SOMEQUANTIFIER.equalsIgnoreCase(quantification)){
 			QuantifiedParticipant result = new QuantifiedParticipant();
 			result.fill(record);
 			return result;
 		}
 		else{
 			final String msg = "Participant had bad quantification: " + quantification;
 			log.error(msg);
 			throw new IllegalArgumentException(msg);
 		}
 	}
 	
 	
 	//maybe make this a constructor
 	public void fill(AbstractResults record) throws SQLException{
 		id = record.getInt(DBID);
 		taxon = record.getInt(DBTAXON);
 		substrate = record.getInt(DBSUBSTRATE);
 		anatomy = record.getInt(DBANATOMY);
 		quantification = record.getString(DBQUANTIFICATION);
 		label = record.getString(DBLABEL);
 		generatedId = record.getString(DBGENERATEDID);
 		publicationTaxon = record.getString(DBPUBLICATIONTAXON);
 		publicationAnatomy = record.getString(DBPUBLICATIONANATOMY);
 		publicationSubstrate = record.getString(DBPUBLICATIONSUBSTRATE);
 		if (taxon != 0){
 			if (record.getString(DBTAXONSOURCEID) != null){
 				this.setTaxonIri(record.getString(DBTAXONSOURCEID));
 			}
 			else if (record.getString(DBTAXONGENERATEDID) != null){
 				this.setTaxonIri(record.getString(DBTAXONGENERATEDID));
 			}
 			else{
 				final String msg = String.format(BADTAXONIRI, id, taxon);
 				throw new IllegalStateException(msg);
 			}
 		}
 		if (anatomy != 0){
 			if (record.getString(DBANATOMYSOURCEID) != null){
 				this.setAnatomyIri(record.getString(DBANATOMYSOURCEID));
 			}
 			else if (record.getString(DBANATOMYGENERATEDID) != null){
 				this.setAnatomyIri(record.getString(DBANATOMYGENERATEDID));
 			}
 			else{
 				final String msg = String.format(BADANATOMYIRI, id, anatomy);
 				throw new IllegalStateException(msg);
 			}
 		}
 		if (substrate != 0){
 			if (record.getString(DBSUBSTRATESOURCEID) != null){
 				this.setSubstrateIri(record.getString(DBSUBSTRATESOURCEID));
 			}
 			else if (record.getString(DBSUBSTRATEGENERATEDID) != null){
 				this.setSubstrateIri(record.getString(DBSUBSTRATEGENERATEDID));
 			}
 			else{
 				final String msg = String.format(BADSUBSTRATEIRI, id, substrate);
 				throw new IllegalStateException(msg);
 			}
 		}
 	}
 
 
 	public int getId(){
 		return id;
 	}
 	
 	public int getTaxon(){
 		return taxon;
 	}
 	
 	public int getSubstrate(){
 		return substrate;
 	}
 	
 	public int getAnatomy(){
 		return anatomy;
 	}
 	
 	public String getQuantification(){
 		return quantification;
 	}
 	
 	public String getPublicationTaxon(){
 		return publicationTaxon;
 	}
 	
 	public String getLabel(){
 		return label;
 	}
 	
 	public String getPublicationAnatomy(){
 		return publicationAnatomy;
 	}
 	
 	public String getPublicationSubstrate(){
 		return publicationSubstrate;
 	}
 
 	public String getTaxonIri(){
 		return taxonIRI;
 	}
 	
 	public void setTaxonIri(String s){
 		taxonIRI = s;
 	}
 	
 	public String getSubstrateIri(){
 		return substrateIRI;
 	}
 	
 	public void setSubstrateIri(String s){
 		substrateIRI= s;
 	}
 	
 	public String getAnatomyIri(){
 		return anatomyIRI;
 	}
 
 	public void setAnatomyIri(String s){
 		anatomyIRI = s;
 	}
 	
 	public String getGeneratedId(){
 		return generatedId;
 	}
 	
 	public void setGeneratedId(String s){
 		generatedId = s;
 	}
 	
 	
 	void processTaxon(Owlbuilder builder,OWLClass taxon){
 		final OWLOntologyManager manager = builder.getOntologyManager();
 		final OWLOntology merged = builder.getMergedSources();
 		final OWLOntology target = builder.getTarget();
 		if (true){  //add appropriate when figured out
 			log.info("Need to add taxon: " + taxon.getIRI());
 			//log.info("Defining Axioms");
 			Set<OWLClassAxiom> taxonAxioms =  merged.getAxioms(taxon);
 			for (OWLClassAxiom a : taxonAxioms){
 				//log.info("   Axiom: " + a.toString());
 			}
 			manager.addAxioms(target, taxonAxioms);
 			//log.info("Annotations");
 			Set<OWLAnnotationAssertionAxiom> taxonAnnotations = merged.getAnnotationAssertionAxioms(taxon.getIRI());
 			for (OWLAnnotationAssertionAxiom a : taxonAnnotations){
 				//log.info("   Annotation Axiom: " + a.toString());
 				if (a.getAnnotation().getProperty().isLabel()){
 					log.info("Label is " + a.getAnnotation().getValue().toString());
 					manager.addAxiom(target, a);
 				}
 			}
 		}
 	}
 
 	public void processAnatomy(Owlbuilder builder, OWLClass anatomyClass) {
     	builder.initializeMiscTermAndParents(anatomyClass);
 		
 	}
 
 	public void processSubstrate(Owlbuilder builder, OWLClass substrateClass) {
 		builder.initializeMiscTermAndParents(substrateClass);
 		
 	}
 
 
 
 }
