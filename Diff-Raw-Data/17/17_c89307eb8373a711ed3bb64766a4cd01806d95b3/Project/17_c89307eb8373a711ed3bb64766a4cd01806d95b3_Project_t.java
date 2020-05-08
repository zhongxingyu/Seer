 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import java.util.Set;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.Party;
 import module.organization.domain.PartyType;
 import module.organizationIst.domain.IstAccountabilityType;
 import module.organizationIst.domain.IstPartyType;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectFinancer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import dml.runtime.RelationAdapter;
 
 public class Project extends Project_Base {
 
     public static class ProjectPartyTypeListener extends RelationAdapter<Party, PartyType> {
 
 	@Override
 	public void afterAdd(final Party party, final PartyType partyType) {
 	    if (party.isUnit() && partyType != null && partyType == PartyType.readBy(IstPartyType.PROJECT.getType())) {
 		new Project((module.organization.domain.Unit) party);
 	    }
 	}
 
     }
 
     static {
 	Party.PartyTypeParty.addListener(new ProjectPartyTypeListener());
     }
 
     public Project(final module.organization.domain.Unit unit) {
 	super();
 	setUnit(unit);
     }
 
     public Project(final Unit parentUnit, final String name, final String projectCode) {
 	super();
 	createRealUnit(this, parentUnit, IstPartyType.PROJECT, projectCode, name);
 
 	// TODO : After this object is refactored to retrieve the name and parent from the real unit,
 	//        the following three lines may be deleted.
 	setName(name);
 	setProjectCode(projectCode);
 	setParentUnit(parentUnit);
     }
 
     public void setProjectCode(final String projectCode) {
         getUnit().setAcronym("P. " + projectCode);
     }
 
     public String getProjectCode() {
 	return getUnit().getAcronym().substring(3);
     }
 
     @Override
     public void findAcquisitionProcessesPendingAuthorization(final Set<AcquisitionProcess> result, final boolean recurseSubUnits) {
 	final String projectCode = getProjectCode();
 	if (projectCode != null) {
 	    for (final AcquisitionProcess acquisitionProcess : GenericProcess.getAllProcesses(RegularAcquisitionProcess.class)) {
 		if (acquisitionProcess.getPayingUnits().contains(this) && acquisitionProcess.isPendingApproval())
 		    result.add(acquisitionProcess);
 	    }
 	}
     }
 
     @Override
     public String getPresentationName() {
 	return "(P. " + getProjectCode() + ") " + super.getPresentationName();
     }
 
     @Override
     public String getShortIdentifier() {
 	return getProjectCode();
     }
 
     @Override
     public Financer finance(final RequestWithPayment acquisitionRequest) {
 	return new ProjectFinancer(acquisitionRequest, this);
     }
 
     @Override
     public boolean isProjectAccountingEmployee(final Person person) {
 	final AccountingUnit accountingUnit = getAccountingUnit();
 	return accountingUnit != null && accountingUnit.hasProjectAccountants(person);
     }
 
     @Override
     public boolean isAccountingEmployee(final Person person) {
 	final AccountingUnit accountingUnit = getAccountingUnit();
 	return (accountingUnit != null && accountingUnit.hasPeople(person))
 		|| (accountingUnit == null && super.isAccountingEmployee(person));
     }
 
     public static Project findProjectByCode(String projectCode) {
 	for (Unit unit : ExpenditureTrackingSystem.getInstance().getUnits()) {
 	    if (unit instanceof Project) {
 		if (((Project) unit).getProjectCode().equals(projectCode)) {
 		    return (Project) unit;
 		}
 	    }
 	}
 	return null;
     }
 
     public SubProject findSubProjectByName(final String institution) {
 	for (final Accountability accountability : getUnit().getChildAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		final Party party = accountability.getChild();
 		if (party.isUnit()) {
 		    final module.organization.domain.Unit child = (module.organization.domain.Unit) party;
 		    if (child.hasExpenditureUnit()) {
 			final Unit unit = child.getExpenditureUnit();
 			if (unit instanceof SubProject) {
 			    final SubProject subProject = (SubProject) unit;
 			    if (subProject.getName().equals(institution)) {
 				return subProject;
 			    }
 			}
 		    }
 		}
 	    }
 	}
 	return null;
     }
 
     public SubProject findSubProjectByNamePrefix(final String institution) {
 	for (final Accountability accountability : getUnit().getChildAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		final Party party = accountability.getChild();
 		if (party.isUnit()) {
 		    final module.organization.domain.Unit child = (module.organization.domain.Unit) party;
 		    if (child.hasExpenditureUnit()) {
 			final Unit unit = child.getExpenditureUnit();
 			if (unit instanceof SubProject) {
 			    final SubProject subProject = (SubProject) unit;
 			    final String name = subProject.getName();
 			    if (name.indexOf(institution) >= 0) {
 				return subProject;
 			    }
 			}
 		    }
 		}
 	    }
 	}
 	return null;
     }
 
     @Override
     public boolean isAccountingResponsible(Person person) {
 	final AccountingUnit accountingUnit = getAccountingUnit();
 	return accountingUnit != null && person != null && accountingUnit.hasResponsibleProjectAccountants(person);
     }
 
     @Override
     public String getUnitNumber() {
 	return getProjectCode();
     }
 
     public boolean isOpen() {
 	final AccountabilityType accountabilityType = IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType();
 	for (final Accountability accountability : getUnit().getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == accountabilityType
 		    && accountability.isActiveNow()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public void close() {
 	final module.organization.domain.Unit unit = getUnit();
 	final AccountabilityType accountabilityType = IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType();
 	for (final Accountability accountability : getUnit().getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == accountabilityType && accountability.isActiveNow()) {
 		final LocalDate beginDate = accountability.getBeginDate();
 		accountability.editDates(beginDate, new LocalDate());
 	    }
 	}
     }
 
    public void open() {
	final Unit parentProject = getParentUnit();
	if (parentProject != null) {
	    final module.organization.domain.Unit parentUnit = getParentUnit().getUnit();
	    final module.organization.domain.Unit unit = getUnit();
	    final AccountabilityType accountabilityType = IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType();
	    parentUnit.addChild(unit, accountabilityType, new LocalDate(), null);
	}
    }

 }
