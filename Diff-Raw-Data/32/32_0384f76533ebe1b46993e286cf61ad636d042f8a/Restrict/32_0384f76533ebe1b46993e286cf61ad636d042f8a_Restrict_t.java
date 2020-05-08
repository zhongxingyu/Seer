 package me.bahadir.bsemantix.ngraph;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
 import com.hp.hpl.jena.ontology.ConversionException;
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.Restriction;
 import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
 import com.hp.hpl.jena.ontology.UnionClass;
 import com.hp.hpl.jena.rdf.model.Property;
 
 public class Restrict implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4550267765831103782L;
 
 	protected static Logger log = Logger.getLogger(Restrict.class.getSimpleName());
 
 	public static final int STAR_CARDINAL = -1;
 
 	private OntClass range;
 	private OntClass source;
 	private Property predicate;
 	private int minCardinality = STAR_CARDINAL;
 	private int maxCardinality = STAR_CARDINAL;
 	private int exactCardinality = STAR_CARDINAL;
 	private Restriction owlRestriction;
 
 	private static Property pQualifiedCard, pMinQualifiedCard, pMaxQualifiedCard, pOnClass, pOnDataRange;
 
 	public static Restrict createFromOwlRestriction(OntClass ontClass, Restriction owlRestriction) {
 		try {
 			return new Restrict(ontClass, owlRestriction);
		} catch (UnsupportedOperationException ue) {
			log.warning(ue.getLocalizedMessage());
		} catch (ConversionException ce) {
			log.warning(ce.getLocalizedMessage());
 		}
 		return null;
 	}
 
 	private Restrict(OntClass ontClass, Restriction owlRestriction) {
 		this.owlRestriction = owlRestriction;
 		setSource(ontClass);
 		setPredicate(owlRestriction.getOnProperty());
 
 		pQualifiedCard = owlRestriction.getModel().getProperty("http://www.w3.org/2002/07/owl#qualifiedCardinality");
 		pMinQualifiedCard = owlRestriction.getModel().getProperty("http://www.w3.org/2002/07/owl#minQualifiedCardinality");
 		pMaxQualifiedCard = owlRestriction.getModel().getProperty("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
 		pOnClass = owlRestriction.getModel().getProperty("http://www.w3.org/2002/07/owl#onClass");
 		pOnDataRange = owlRestriction.getModel().getProperty("http://www.w3.org/2002/07/owl#onDataRange");
 
		if (owlRestriction.hasProperty(pOnDataRange)) {
			throw new UnsupportedOperationException("Data range restrictions not implemented yet");
 		} else {
			resolve();
 		}
 
 	}
	

 
 	private void resolve() {
 
 		OntClass rangeClass = null;
 
 		// CARDINALITY 0-*
 		if (owlRestriction.isSomeValuesFromRestriction()) { // some values
 															// restriction
 			setMinCardinality(0);
 			// log.info("\t\t(some)");
 			SomeValuesFromRestriction someRest = owlRestriction.asSomeValuesFromRestriction();
 
 			rangeClass = someRest.getSomeValuesFrom().as(OntClass.class);
 
		} else if (owlRestriction.isAllValuesFromRestriction()) { // CARDINALITY
																	// 0-*
 			setMinCardinality(0);
 			AllValuesFromRestriction allRest = owlRestriction.asAllValuesFromRestriction();
 			rangeClass = allRest.getAllValuesFrom().as(OntClass.class);
 		} else {
 
			if (owlRestriction.hasProperty(pQualifiedCard)) { // CARDINALITY
																// n
 				int cardInt = owlRestriction.getPropertyValue(pQualifiedCard).asLiteral().getInt();
 				setExactCardinality(cardInt);
 				rangeClass = owlRestriction.getPropertyValue(pOnClass).as(OntClass.class);
 			} else if (owlRestriction.hasProperty(pMinQualifiedCard)) { // CARDINALITY
 																		// n-*
 				int cardInt = owlRestriction.getPropertyValue(pQualifiedCard).asLiteral().getInt();
 				rangeClass = owlRestriction.getPropertyValue(pOnClass).as(OntClass.class);
 				setMinCardinality(cardInt);
 			} else if (owlRestriction.hasProperty(pMaxQualifiedCard)) { // CARDINALITY
 																		// *-n
 				int cardInt = owlRestriction.getPropertyValue(pQualifiedCard).asLiteral().getInt();
 				rangeClass = owlRestriction.getPropertyValue(pOnClass).as(OntClass.class);
 				setMaxCardinality(cardInt);
 			} else {
 				// log.warning("Restriction format is not supported");
 			}
 
 		}
 
 		if (rangeClass != null) {
 			setRange(rangeClass);
 		}
 	}
 
 	public Property getPredicate() {
 		return predicate;
 	}
 
 	public void setPredicate(Property predicate) {
 		this.predicate = predicate;
 	}
 
 	public OntClass getRange() {
 		return range;
 	}
 
 	public void setRange(OntClass range) {
 		this.range = range;
 	}
 
 	public OntClass getSource() {
 		return source;
 	}
 
 	public void setSource(OntClass source) {
 		this.source = source;
 	}
 
 	public int getMinCardinality() {
 		return minCardinality;
 	}
 
 	public void setMinCardinality(int minCardinality) {
 		this.minCardinality = minCardinality;
 	}
 
 	public int getMaxCardinality() {
 		return maxCardinality;
 	}
 
 	public void setMaxCardinality(int maxCardinality) {
 		this.maxCardinality = maxCardinality;
 	}
 
 	public int getExactCardinality() {
 		return exactCardinality;
 	}
 
 	public void setExactCardinality(int exactCardinality) {
 		this.exactCardinality = exactCardinality;
 	}
 
 	public boolean isComplete() {
 		return range != null && source != null;
 	}
 
 }
