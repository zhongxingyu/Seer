 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import java.util.Set;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.Party;
 import module.organization.domain.PartyType;
 import module.organizationIst.domain.IstAccountabilityType;
 import module.organizationIst.domain.IstPartyType;
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
	final String subProjectName = getName() + " - " + institution + " - ";

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
			    if (name.startsWith(subProjectName)) {
 				return subProject;
 			    }
 			}
 		    }
 		}
 	    }
 	}
 	return null;
     }
 
 }
