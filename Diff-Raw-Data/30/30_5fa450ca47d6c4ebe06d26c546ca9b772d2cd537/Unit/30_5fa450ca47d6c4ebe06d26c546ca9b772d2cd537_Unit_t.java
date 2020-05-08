 package module.organization.domain;
 
import java.util.Collection;

 import myorg.domain.MyOrg;
 import myorg.domain.exceptions.DomainException;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class Unit extends Unit_Base {
 
     protected Unit() {
 	super();
     }
 
     protected Unit(final Party parent, final MultiLanguageString name, final String acronym, final PartyType partyType,
 	    final AccountabilityType accountabilityType) {
 	this();
 
 	check(partyType, name, acronym);
	checkName(parent, name);
 	checkAcronym(parent, acronym);
 
 	setPartyType(partyType);
 	setPartyName(name);
 	setAcronym(acronym);
 
 	if (parent != null) {
 	    check(accountabilityType, "error.Unit.invalid.accountability.type");
 	    new Accountability(parent, this, accountabilityType);
 	} else {
 	    setMyOrgFromTopUnit(MyOrg.getInstance());
 	}
     }
 
    private void checkName(final Party parent, final MultiLanguageString name) {
	checkName(parent != null ? parent.getChildren() : MyOrg.getInstance().getTopUnits(), name);
    }

    private void checkName(final Collection<? extends Party> parties, final MultiLanguageString name) {
	for (final Party party : parties) {
	    if (party.isUnit()) {
		final Unit unit = (Unit) party;
		if (unit.getPartyName().equalInAnyLanguage(name)) {
		    throw new DomainException("error.Unit.found.child.with.same.name", name.getContent());
		}
	    }
	}
    }

     private void checkAcronym(final Party parent, final String acronym) {
	checkAcronym(parent != null ? parent.getChildren() : MyOrg.getInstance().getTopUnits(), acronym);
    }

    private void checkAcronym(final Collection<? extends Party> parties, final String acronym) {
	for (final Party party : parties) {
 	    if (party.isUnit()) {
 		final Unit unit = (Unit) party;
 		if (unit.getAcronym().equalsIgnoreCase(acronym)) {
		    throw new DomainException("error.Unit.found.child.with.same.acronym", acronym);
 		}
 	    }
 	}
     }
 
     private void check(final Object obj, final String message) {
 	if (obj == null) {
 	    throw new DomainException(message);
 	}
     }
 
     private void check(final PartyType partyType, final MultiLanguageString name, final String acronym) {
 	check(partyType, "error.Unit.invalid.party.type");
 
 	if (name == null || name.isEmpty()) {
 	    throw new DomainException("error.Unit.invalid.name");
 	}
 
 	if (acronym == null || acronym.isEmpty()) {
 	    throw new DomainException("error.Unit.invalid.acronym");
 	}
     }
 
     @Override
     final public boolean isUnit() {
 	return true;
     }
 
     public boolean isTop() {
 	return !hasAnyParentAccountabilities() && hasMyOrgFromTopUnit();
     }
 
     @Service
     static public Unit create(final Party parent, final MultiLanguageString name, final String acronym,
 	    final PartyType partyType, final AccountabilityType accountabilityType) {
 	return new Unit(parent, name, acronym, partyType, accountabilityType);
     }
 
     @Service
     static public Unit createRoot(final MultiLanguageString name, final String acronym, final PartyType partyType) {
 	return create(null, name, acronym, partyType, null);
     }
 
 }
