 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 
 public class RemoveFundAllocationExpirationDate extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	return (process.isAccountingEmployee() && !hasAnyAssociatedProject(process)) || process.isProjectAccountingEmployee();
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
	return (checkActiveConditions(process) || checkCanceledConditions(process)) && !process.hasAnyAllocatedFunds() && process.getFundAllocationExpirationDate() != null;
     }
 
     private boolean checkActiveConditions(RegularAcquisitionProcess process) {
	return super.isAvailable(process) && process.getAcquisitionProcessState().isInAllocatedToSupplierState();
     }
 
     private boolean checkCanceledConditions(RegularAcquisitionProcess process) {
	return super.isAvailable(process) && process.getAcquisitionProcessState().isCanceled();
     }
 
     private boolean hasAnyAssociatedProject(final RegularAcquisitionProcess process) {
 	for (final Financer financer : process.getAcquisitionRequest().getFinancersSet()) {
 	    if (financer.isProjectFinancer()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	process.setFundAllocationExpirationDate(null);
 	process.getAcquisitionRequest().unSubmitForFundsAllocation();
 	if (!process.getAcquisitionProcessState().isCanceled()) {
 	    process.submitForApproval();
 	}
     }
 
 }
