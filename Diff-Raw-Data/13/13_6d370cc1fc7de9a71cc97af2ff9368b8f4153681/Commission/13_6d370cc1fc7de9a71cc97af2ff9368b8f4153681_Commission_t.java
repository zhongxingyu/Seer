 /**
  * 
  */
 package org.esupportail.opi.domain.beans.references.commission;
 
 
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.esupportail.opi.domain.beans.NormeSI;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarCmi;
import org.esupportail.opi.domain.beans.references.calendar.ReunionCmi;
 import org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV;
 
 /**
  * @author cleprous
  * 
  */
 public class Commission extends NormeSI {
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = -5300722125770519937L;
 
 	/*
 	 ******************* PROPERTIES ******************* */
 
 	/**
 	 * The Commission code.
 	 */
 	private String code;
 
 	/**
 	 * The members of this commission.
 	 */
 	private Set<Member> members;
 	
 	/**
 	 * The etape and cge for this.
 	 */
 	private Set<TraitementCmi> traitementCmi;
 	
 	/**
 	 * The cmi calendar.
 	 */
 	private CalendarCmi calendarCmi;
 
 	/**
 	 * The rdv calendar.
 	 */
 	private CalendarRDV calendrierRdv;
 	
 	/**
 	 * Map of the contacts of the commission.
 	 */
 	private Map<String, ContactCommission> contactsCommission;
 	
 	/*
 	 ******************* INIT ************************* */
 
 	/**
 	 * Constructors.
 	 */
 	public Commission() {
 		super();
 		contactsCommission = new HashMap<String, ContactCommission>();
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((code == null) ? 0 : code.hashCode());
 		return result;
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) { return true; }
 		if (!super.equals(obj)) { return false; }
 		if (!(obj instanceof Commission)) {	return false; }
 		Commission other = (Commission) obj;
 		if (code == null) {
 			if (other.getCode() != null) { return false; }
 		} else if (!code.equals(other.getCode())) { return false; }
 		return true;
 	}
 
 	/**
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public Commission clone() {
 		Commission c = new Commission();
 		c = (Commission) super.clone(c);
 		c.setCode(code);
 		c.setMembers(members);
 		c.setTraitementCmi(traitementCmi);
 		c.setCalendarCmi(calendarCmi);
 		c.setContactsCommission(contactsCommission);
 		return c;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Commission#" + hashCode() + "[code=[" + code
 				+ "],  [" + super.toString() + "]]";
 	}
 
 	/*
 	 ******************* METHODS ********************** */
 
 	/*
 	 ******************* ACCESSORS ******************** */
 
 	/**
 	 * @return the code
 	 */
 	public String getCode() {
 		return code;
 	}
 
 	/**
 	 * @param code
 	 *            the code to set
 	 */
 	public void setCode(final String code) {
 		this.code = code;
 	}
 
 	/**
 	 * @return the members
 	 */
 	public Set<Member> getMembers() {
 		return members;
 	}
 
 	/**
 	 * @param members
 	 *            the members to set
 	 */
 	public void setMembers(final Set<Member> members) {
 		this.members = members;
 	}
 
 	/**
 	 * @return the traitementCmi
 	 */
 	public Set<TraitementCmi> getTraitementCmi() {
 		return traitementCmi;
 	}
 
 	/**
 	 * @param traitementCmi the traitementCmi to set
 	 */
 	public void setTraitementCmi(final Set<TraitementCmi> traitementCmi) {
 		this.traitementCmi = traitementCmi;
 	}
 
 	/**
 	 * @return the calendarCmi
 	 */
 	public CalendarCmi getCalendarCmi() {
 		return calendarCmi;
 	}
 
 	/**
 	 * @param calendarCmi the calendarCmi to set
 	 */
 	public void setCalendarCmi(final CalendarCmi calendarCmi) {
 		this.calendarCmi = calendarCmi;
 	}
 	
 	/**
 	 * 
 	 * @return calendrierRdv
 	 */
 	public CalendarRDV getCalendrierRdv() {
 		return calendrierRdv;
 	}
 	
 	/**
 	 * 
 	 * @param calendrierRdv
 	 */
 	public void setCalendrierRdv(final CalendarRDV calendrierRdv) {
 		this.calendrierRdv = calendrierRdv;
 	}
 	
 	/**
 	 * @return the contactsCommission
 	 */
 	public Map<String, ContactCommission> getContactsCommission() {
 		return contactsCommission;
 	}
 
 	/**
 	 * @param contactsCommission the contactsCommission to set
 	 */
 	public void setContactsCommission(
 			final Map<String, ContactCommission> contactsCommission) {
 		this.contactsCommission = contactsCommission;
 	}
 
 
 }
