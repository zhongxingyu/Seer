 package org.openmrs.module.flowsheet;
 
 import org.hibernate.proxy.HibernateProxy;
 import org.openmrs.Concept;
 import org.openmrs.ConceptNumeric;
 
 public class Numeric {
 
 	private String unit;
 	private Double hi;
 	private Double low;
 
 	public Numeric(Concept concept) {
 		ConceptNumeric numeric = deproxy(concept, ConceptNumeric.class);
 		this.unit = numeric.getUnits();
 		this.hi = numeric.getHiAbsolute();
 		this.low = numeric.getLowAbsolute();
 	}
 
 	private <T> T deproxy(Object maybeProxy, Class<T> baseClass)
 			throws ClassCastException {
 		if (maybeProxy instanceof HibernateProxy)
 			return baseClass.cast(((HibernateProxy) maybeProxy)
 					.getHibernateLazyInitializer().getImplementation());
 		else
 			return baseClass.cast(maybeProxy);
 	}
 
 	public String getUnit() {
 		return unit;
 	}
 
 	public String getHi() {
 		return nullSafeString(hi);
 	}
 
 	public String getLow() {
 		return nullSafeString(low);
 	}
 
	public String nullSafeString(Double value) {
 		return value == null ? "" : "" + value;
 	}
 }
