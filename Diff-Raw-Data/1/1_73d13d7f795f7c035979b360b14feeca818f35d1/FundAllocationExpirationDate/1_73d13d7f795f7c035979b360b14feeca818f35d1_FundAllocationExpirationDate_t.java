 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class FundAllocationExpirationDate extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null
 		//&& process.isResponsibleForUnit(loggedPerson)
 		&& userHasRole(RoleType.ACQUISITION_CENTRAL) 
 		;
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
 	return  super.isAvailable(process)
 		//&& process.getAcquisitionProcessState().isPendingFundAllocation()
 		&& process.getAcquisitionRequest().isSubmittedForFundsAllocationByAllResponsibles()
 		&& !process.isPendingFundAllocation()
		&& !process.getAcquisitionRequest().hasAnyFundAllocationId()
 		;
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	if (process.getAcquisitionRequest().isSubmittedForFundsAllocationByAllResponsibles()) {
 	    if (!process.getSkipSupplierFundAllocation()) {
 		LocalDate now = new LocalDate();
 		process.setFundAllocationExpirationDate(now.plusDays(90));
 	    }
 	    else {
 		process.skipFundAllocation();
 	    }
 	}
 
 	process.allocateFundsToSupplier();
     }
 
 }
