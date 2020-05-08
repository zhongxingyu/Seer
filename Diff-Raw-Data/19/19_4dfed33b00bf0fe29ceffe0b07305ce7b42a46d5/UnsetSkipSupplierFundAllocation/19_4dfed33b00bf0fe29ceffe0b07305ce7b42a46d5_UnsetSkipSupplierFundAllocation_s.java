 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 
 public class UnsetSkipSupplierFundAllocation extends WorkflowActivity<RefundProcess, ActivityInformation<RefundProcess>> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
 	Person person = user.getExpenditurePerson();
 
 	return isUserProcessOwner(process, user)
 		&& process.getSkipSupplierFundAllocation()
 		&& (((process.isInGenesis() || process.getProcessState().getRefundProcessStateType() == RefundProcessStateType.AUTHORIZED) && person == process
 			.getRequestor()) || person.hasRoleType(RoleType.SUPPLIER_FUND_ALLOCATION_MANAGER));
 
     }
 
     @Override
     protected void process(ActivityInformation<RefundProcess> activityInformation) {
 	RefundProcess process = activityInformation.getProcess();
 
 	for (Supplier supplier : process.getRequest().getSuppliers()) {
 	    if (!supplier.isFundAllocationAllowed(process.getRequest().getTotalValue())) {
		throw new DomainException("acquisitionProcess.message.exception.SupplierDoesNotAlloweAmount");
 	    }
 	}
 	process.setSkipSupplierFundAllocation(Boolean.FALSE);
 
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
 	return "resources/AcquisitionResources";
     }
 }
