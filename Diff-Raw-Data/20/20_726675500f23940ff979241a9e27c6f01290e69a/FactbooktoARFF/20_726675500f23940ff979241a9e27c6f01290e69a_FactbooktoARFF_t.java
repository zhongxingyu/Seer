 package com.conbit.factbookparser.convertors;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLLiteral;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 
 import com.conbit.factbookparser.concept.TableEntry;
 import com.conbit.factbookparser.owl.OwlHandler;
 import com.conbit.factbookparser.parser.factbook.FactbookParser;
 
 public class FactbooktoARFF {
 	
 	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
 		PropertyConfigurator.configure("log4j.conf");
 		FactbooktoARFF convertor = new FactbooktoARFF("/home/leendert/Total.owl");
		convertor.convert("/home/leendert/homework6_3.arff");
 	}
 
 	private OwlHandler handler;
 	private Logger logger = Logger.getLogger(this.toString());
 	
 	public FactbooktoARFF(String location) throws OWLOntologyCreationException{
 		logger.debug("Start parsing ontology");
 		handler = new OwlHandler(location);
 		TableEntry.init(handler);
 		logger.debug("Finished parsing ontology");
 	}
 	
 	public void convert(String outputFileLocation) throws IOException{
 		logger.debug("Start converting...");
 		logger.debug("Searching attacks...");
 		int attackCounter = 1;
 		Set<OWLIndividual> attacks = handler.getIndividuals("Attack");
 		Iterator<OWLIndividual> attackIterator = attacks.iterator();
 		while(attackIterator.hasNext()){
 			OWLIndividual attack = attackIterator.next();
 			logger.debug("Handling attack "+attack.toStringID());
 			OWLIndividual perpetrator = getPerpetrator(attack);
 			String perpetratorName = getPerpetratorName(perpetrator);
 			if(perpetratorName == null)
 				continue;
 			logger.debug("From perpetrator "+perpetratorName);
 			TableEntry entry = TableEntry.createEntry(perpetratorName);
 			OWLIndividual country = addCountry(attack,entry);
 			if(country == null){
 				continue;
 			} else {
 				addNeighbours(country, entry);
 			}
 			logger.debug("Countries added");
 			if(!addAttackType(attack,entry))
 				continue;
 			logger.debug("Attacks added");
 			if(!addVictimType(attack,entry))
 				continue;
 			logger.debug("Victims added");
 			if(!addClassification(perpetrator,entry))
 				continue;
 			logger.debug("Classification added");
 			TableEntry.save(entry);
 			logger.debug("Entry saved");
 			logger.debug("Attacks done:" + attackCounter);
 			attackCounter++;
 		}
 		TableEntry.writoToArff(outputFileLocation);
 	}
 	
 	private boolean addNeighbours(OWLIndividual country, TableEntry entry) {
 		Set<OWLIndividual> borders = handler.getObjectRelationIndividuals(country, "border");
 		boolean isSet = false;
 		if(borders == null)
 			return false;
 		isSet = addBorders(entry, borders);
 		return isSet;
 	}
 
 	private boolean addBorders(TableEntry entry, Set<OWLIndividual> borders) {
 		boolean isSet = false;
 		Iterator<OWLIndividual> borderIterator = borders.iterator();
 		while(borderIterator.hasNext()){
 			OWLIndividual currentBorder = borderIterator.next();
 			isSet = addBorder(entry, currentBorder);
 		}
 		return isSet;
 	}
 	
 	private boolean addBorder(TableEntry entry, OWLIndividual border){
 		Set<OWLIndividual> borderCountries = handler.getObjectRelationIndividuals(border, "country");
 		if(borderCountries == null){
 			return false;
 		}
 		OWLIndividual borderCountry = borderCountries.iterator().next();
 		Set<OWLLiteral> borderCountryNames = handler.getDataRelationIndividuals(borderCountry, "name");
 		if(borderCountryNames == null){
 			return false;
 		}
 		String neighbourCountryName = borderCountryNames.iterator().next().getLiteral();
 		entry.addNeighbour(neighbourCountryName);
 		return true;
 	}
 
 	private String getPerpetratorName(OWLIndividual perpetrator) {
 		Set<OWLLiteral> names = handler.getDataRelationIndividuals(perpetrator, "name");
 		if(names == null){
 			logger.debug("Perpetrator name failed, ignoring...");
 			return null;
 		}
 		return names.iterator().next().getLiteral();
 	}
 
 	private boolean addClassification(OWLIndividual perpetrator, TableEntry entry) {
 		Set<OWLIndividual> classifications = handler.getObjectRelationIndividuals(perpetrator, "hasClassification");
 		boolean isSet = false;
 		if(classifications == null)
 			return false;
 		Iterator<OWLIndividual> typeIterator = classifications.iterator();
 		while(typeIterator.hasNext()){
 			OWLIndividual currentype = typeIterator.next();
 			Set<OWLLiteral> typeNames = handler.getDataRelationIndividuals(currentype, "name");
 			if(typeNames == null)
 				continue;
 			String typeString = typeNames.iterator().next().getLiteral();
 			entry.setType(typeString);
 			isSet = true;
 		}
 		return isSet;
 	}
 
 	private boolean addVictimType(OWLIndividual attack, TableEntry entry) {
 		Set<OWLIndividual> types = handler.getObjectRelationIndividuals(attack, "hasVictim");
 		boolean oneAdded = false;
 		if(types == null)
 			return false;
 		Iterator<OWLIndividual> typeIterator = types.iterator();
 		while(typeIterator.hasNext()){
 			OWLIndividual type = typeIterator.next();
 			Set<OWLLiteral> typeNames = handler.getDataRelationIndividuals(type, "name");
 			if(typeNames == null)
 				continue;
 			entry.addVictimType(typeNames.iterator().next().getLiteral());
 			oneAdded = true;
 		}
 		return oneAdded;
 	}
 
 	private boolean addAttackType(OWLIndividual attack, TableEntry entry) {
 		Set<OWLIndividual> types = handler.getObjectRelationIndividuals(attack, "ofType");
 		boolean oneAdded = false;
 		if(types == null)
 			return false;
 		Iterator<OWLIndividual> typeIterator = types.iterator();
 		while(typeIterator.hasNext()){
 			OWLIndividual type = typeIterator.next();
 			Set<OWLLiteral> typeNames = handler.getDataRelationIndividuals(type, "name");
 			if(typeNames == null)
 				continue;
 			entry.addAttackType(typeNames.iterator().next().getLiteral());
 			oneAdded = true;
 		}
 		return oneAdded;
 	}
 
 	private OWLIndividual addCountry(OWLIndividual attack, TableEntry entry) {
 		Set<OWLIndividual> countries = handler.getObjectRelationIndividuals(attack, "occured");
 		if(countries == null)
 			return null;
 		OWLIndividual country = countries.iterator().next();
 		Set<OWLLiteral> countryName = handler.getDataRelationIndividuals(country, "name");
 		if(countryName == null)
 			return null;
 		entry.addCountry(countryName.iterator().next().getLiteral());
 		return country;
 	}
 
 	public OWLIndividual getPerpetrator(OWLIndividual individual){
 		Set<OWLIndividual> perpetrators = handler.getObjectRelationIndividuals(individual, "executedBy");
 		if(perpetrators == null)
 			return null;
 		return perpetrators.iterator().next();
 	}
 }
