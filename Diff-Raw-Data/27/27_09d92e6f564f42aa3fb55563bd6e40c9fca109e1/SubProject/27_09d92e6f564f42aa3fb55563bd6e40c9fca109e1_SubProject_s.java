 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import module.organization.domain.Party;
 import module.organization.domain.PartyType;
 import module.organizationIst.domain.IstPartyType;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectFinancer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import dml.runtime.RelationAdapter;
 
 public class SubProject extends SubProject_Base {
 
     public static class SubProjectPartyTypeListener extends RelationAdapter<Party, PartyType> {
 
 	@Override
 	public void afterAdd(final Party party, final PartyType partyType) {
 	    if (party.isUnit() && partyType != null && partyType == PartyType.readBy(IstPartyType.SUB_PROJECT.getType())) {
 		new SubProject((module.organization.domain.Unit) party);
 	    }
 	}
 
     }
 
     static {
 	Party.PartyTypeParty.addListener(new SubProjectPartyTypeListener());
     }
 
     public SubProject(final module.organization.domain.Unit unit) {
 	super();
 	setUnit(unit);
     }
 
     public SubProject(final Project parentUnit, final String name) {
 	super();
 	final String acronym = StringUtils.abbreviate(name, 5);
 	createRealUnit(this, parentUnit, IstPartyType.SUB_PROJECT, acronym, name);
 
 	// TODO : After this object is refactored to retrieve the name and parent from the real unit,
 	//        the following two lines may be deleted.
 	setName(name);
 	setParentUnit(parentUnit);
     }
 
     @Override
     public void setName(final String name) {
         super.setName(name);
        final String acronym = StringUtils.abbreviate(name, 5);
        getUnit().setAcronym(acronym);
     }
 
     @Override
     public String getPresentationName() {
 	return "(" + getUnit().getAcronym() + ") " + " - " + super.getPresentationName();
     }
 
     @Override
     public void setParentUnit(final Unit parentUnit) {
         super.setParentUnit(parentUnit);
         if (parentUnit != null && hasUnit()) {
             getUnit().setAcronym(parentUnit.getUnit().getAcronym());
         }
     }
 
     @Override
     public AccountingUnit getAccountingUnit() {
 	final AccountingUnit accountingUnit = super.getAccountingUnit();
 	return accountingUnit == null ? getParentUnit().getAccountingUnit() : accountingUnit;
     }
 
     @Override
     public Financer finance(final RequestWithPayment acquisitionRequest) {
 	return new ProjectFinancer(acquisitionRequest, this);
     }
 
 }
