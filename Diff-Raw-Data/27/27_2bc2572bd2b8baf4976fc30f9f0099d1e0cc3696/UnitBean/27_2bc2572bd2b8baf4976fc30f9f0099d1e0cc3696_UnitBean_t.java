 package module.organization.domain;
 
 import java.io.Serializable;
 
 import pt.ist.fenixWebFramework.util.DomainReference;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class UnitBean implements Serializable {
 
     private static final long serialVersionUID = -952861107508339516L;
     private DomainReference<Unit> unit;

    private DomainReference<Unit> parent;
     private MultiLanguageString name;
     private String acronym;
    private DomainReference<PartyType> partyType;
    private DomainReference<AccountabilityType> accountabilityType;
 
     public UnitBean() {
     }
 
     public UnitBean(final Unit unit) {
 	setUnit(unit);
 	setName(unit.getPartyName());
 	setAcronym(unit.getAcronym());
 	setPartyType(unit.getPartyType());
     }
 
     public Unit getParent() {
 	return (this.parent != null) ? this.parent.getObject() : null;
     }
 
     public void setParent(Unit parent) {
 	this.parent = (parent != null) ? new DomainReference<Unit>(parent) : null;
     }
 
     public boolean hasParent() {
 	return getParent() != null;
     }
 
     public Unit getUnit() {
 	return (this.unit != null) ? this.unit.getObject() : null;
     }
 
     public void setUnit(Unit unit) {
 	this.unit = (unit != null) ? new DomainReference<Unit>(unit) : null;
     }
 
     public MultiLanguageString getName() {
 	return name;
     }
 
     public void setName(MultiLanguageString name) {
 	this.name = name;
     }
 
     public String getAcronym() {
 	return acronym;
     }
 
     public void setAcronym(String acronym) {
 	this.acronym = acronym;
     }
 
     public PartyType getPartyType() {
	return (this.partyType != null) ? this.partyType.getObject() : null;
     }
 
     public void setPartyType(PartyType partyType) {
	this.partyType = (partyType != null) ? new DomainReference<PartyType>(partyType) : null;
     }
 
     public AccountabilityType getAccountabilityType() {
	return (this.accountabilityType != null) ? this.accountabilityType.getObject() : null;
     }
 
     public void setAccountabilityType(AccountabilityType accountabilityType) {
	this.accountabilityType = (accountabilityType != null) ? new DomainReference<AccountabilityType>(accountabilityType)
		: null;
     }
 
    public Unit createUnit() {
	if (!hasParent()) {
 	    return Unit.createRoot(getName(), getAcronym(), getPartyType());
 	} else {
 	    return Unit.create(getParent(), getName(), getAcronym(), getPartyType(), getAccountabilityType());
 	}
     }
 
 }
