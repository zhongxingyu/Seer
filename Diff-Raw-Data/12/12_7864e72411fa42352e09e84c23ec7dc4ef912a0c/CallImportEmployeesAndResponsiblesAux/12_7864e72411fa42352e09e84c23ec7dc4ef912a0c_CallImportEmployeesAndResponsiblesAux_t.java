 package module.organizationIst.domain.task;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.Person;
 import module.organizationIst.domain.IstAccountabilityType;
 import myorg.domain.User;
 import myorg.domain.scheduler.WriteCustomTask;
 import net.sourceforge.fenixedu.domain.RemotePerson;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public class CallImportEmployeesAndResponsiblesAux extends WriteCustomTask {
 
     @Override
     protected void doService() {
 	final User user = User.findByUsername("ist24439");
 	final Person somePerson = user.getPerson();
 	final RemotePerson someRemotePerson = somePerson.getRemotePerson();
 
 	final String allTeacherInformation = someRemotePerson.readAllTeacherInformation();
 	updateInformation(IstAccountabilityType.TEACHING_PERSONNEL, allTeacherInformation);
 	final String allResearcherInformation = someRemotePerson.readAllResearcherInformation();
 	updateInformation(IstAccountabilityType.RESEARCH_PERSONNEL, allResearcherInformation);
 	final String allEmployeeInformation = someRemotePerson.readAllEmployeeInformation();
 	updateInformation(IstAccountabilityType.PERSONNEL, allEmployeeInformation);
 	final String allGrantOwnerInformation = someRemotePerson.readAllGrantOwnerInformation();
 	updateInformation(IstAccountabilityType.GRANT_OWNER_PERSONNEL, allGrantOwnerInformation);
   	final String allExternalResearcherInformation = someRemotePerson.readAllExternalResearcherInformation();
 	updateInformation(IstAccountabilityType.EXTERNAL_RESEARCH_PERSONNEL, allExternalResearcherInformation + '|');
     }
 
     private static void updateInformation(final IstAccountabilityType istAccountabilityType, final String allInformation) {
 	final AccountabilityType accountabilityType = istAccountabilityType.readAccountabilityType();
 	final LocalDate now = new LocalDate();
 	for (int i = 0; i < allInformation.length(); ) {
 	    final int sep1 = allInformation.indexOf(':', i);
 	    final int sep2 = allInformation.indexOf(':', sep1 + 1);
 	    final int sep3 = allInformation.indexOf('|', sep2 + 1);
 
 	    if (sep1 > i && sep2 > sep1 && sep3 > sep2) {
 		final String username = allInformation.substring(i, sep1);
 		final String costCenterCode = allInformation.substring(sep2 + 1, sep3);
 
 		updateInformation(now, accountabilityType, username, costCenterCode, allInformation);
 		i = sep3 + 1;
 	    } else {
 		i++;
 	    }
 	}
     }
 
     private static void updateInformation(final LocalDate now, final AccountabilityType accountabilityType, 
 	    final String username, final String costCenterCode, final String allInformation) {
 	final User user = User.findByUsername(username);
 	if (user != null) {
 	    final Person person = user.getPerson();
 	    if (person != null) {
 		final Unit unit = CostCenter.findUnitByCostCenter(costCenterCode);
 		if (unit != null) {
 		    updateInformation(now, accountabilityType, person, unit.getUnit(), allInformation, username);
 		} else {
 		    System.out.println("Did not find cost center with code: " + costCenterCode);
 		}
 	    } else {
 		System.out.println("User with username: " + username + " has no person");
 	    }
 	} else {
 	    System.out.println("Did not find user with username: " + username);
 	}
     }
 
     private static void updateInformation(final LocalDate now, final AccountabilityType accountabilityType,
 	    final Person person, final module.organization.domain.Unit unit, final String allInformation, final String username) {
 	for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == accountabilityType && accountability.isActive(now)) {
 		if (accountability.getParent() == unit) {
 		    return;
 		} else {
 		    final Unit expenditureUnit = ((module.organization.domain.Unit) accountability.getParent()).getExpenditureUnit();
 		    if (expenditureUnit instanceof CostCenter) {
 			final CostCenter center = (CostCenter) expenditureUnit;
 			final String checkString = username + ':' + getAccountabilityTypeString(accountabilityType) + ':' + center.getCostCenter();
 			if (allInformation.indexOf(checkString) < 0) {
			    accountability.setEndDate(now);
 			}
 		    } else {
			accountability.setEndDate(now);
 		    }
 		}
 	    }
 	}
 	unit.addChild(person, accountabilityType, now, null);
     }
 
     private static String getAccountabilityTypeString(final AccountabilityType accountabilityType) {
 	if (accountabilityType == IstAccountabilityType.TEACHING_PERSONNEL.readAccountabilityType()) {
 	    return "TEACHER";
 	}
 	if (accountabilityType == IstAccountabilityType.RESEARCH_PERSONNEL.readAccountabilityType()) {
 	    return "RESEARCHER";
 	}
 	if (accountabilityType == IstAccountabilityType.PERSONNEL.readAccountabilityType()) {
 	    return "EMPLOYEE";
 	}
 	if (accountabilityType == IstAccountabilityType.GRANT_OWNER_PERSONNEL.readAccountabilityType()) {
 	    return "GRANT_OWNER";
 	}
 	if (accountabilityType == IstAccountabilityType.EXTERNAL_RESEARCH_PERSONNEL.readAccountabilityType()) {
 	    return "RESEARCHER";
 	}
 	return null;
     }
 
 }
