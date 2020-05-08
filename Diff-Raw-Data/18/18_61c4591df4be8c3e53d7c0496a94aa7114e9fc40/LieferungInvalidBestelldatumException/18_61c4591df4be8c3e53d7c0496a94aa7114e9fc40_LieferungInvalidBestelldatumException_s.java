 package de.shop.Artikelverwaltung.service;
 
 import java.util.Collection;
 import java.util.Date;
 
 import javax.ejb.ApplicationException;
 import javax.validation.ConstraintViolation;
 
 import de.shop.Artikelverwaltung.domain.Lieferung;
 
 @ApplicationException(rollback = true)
 public class LieferungInvalidBestelldatumException extends LieferungServiceException {
 	private static final long serialVersionUID = 1L;
 	
 	private final Date bestelldatum;
 	private final Collection<ConstraintViolation<Lieferung>> violations;
 	
	public LieferungInvalidBestelldatumException(
		   Date bestelldatum, Collection<ConstraintViolation<Lieferung>> violations) {
 		super("Ungueltiges Bestelldatum: " + bestelldatum + ", Violations: " + violations);
		this.bestelldatum = bestelldatum;
 		this.violations = violations;
 	}
 
 	public Date getBestelldatum() {
		return bestelldatum;
 	}
 
 	public Collection<ConstraintViolation<Lieferung>> getViolations() {
 		return violations;
 	}
 }
