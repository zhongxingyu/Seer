 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 
 public class SetSkipSupplierFundAllocation extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	User user = getUser();
 	return user != null && (user.getPerson() == process.getRequestor() || userHasRole(RoleType.ACQUISITION_CENTRAL));
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
 	return super.isAvailable(process)
		&& ((process.getAcquisitionProcessState().isInGenesis() && getUser().getPerson() == process.getRequestor()) || (process
			.getAcquisitionProcessState().isApproved()
			&& userHasRole(RoleType.ACQUISITION_CENTRAL))) && !process.getSkipSupplierFundAllocation();
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	process.skipSupplierFundAllocation();
     }
 
 }
