 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class CancelRefundProcess extends WorkflowActivity<RefundProcess, ActivityInformation<RefundProcess>> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
 	final Person executor = user.getExpenditurePerson();
 
 	return isUserProcessOwner(process, user)
 		&& (((process.isInGenesis() || process.isInAuthorizedState()) && process.getRequestor() == executor)
 			|| (process.isPendingApproval() && process.isResponsibleForAtLeastOnePayingUnit(executor))
 			|| (process.isResponsibleForUnit(executor, process.getRequest().getTotalValue())
 				&& !process.getRequest().hasBeenAuthorizedBy(executor) && process.isInAllocatedToUnitState()) || ((process
 			.isPendingInvoicesConfirmation() || process.isPendingFundAllocation()) && ((process
 			.isAccountingEmployee(executor) && !process.hasProjectsAsPayingUnits()) || (process
 			.isProjectAccountingEmployee(executor) && process.hasProjectsAsPayingUnits()))));
     }
 
     @Override
     protected void process(ActivityInformation<RefundProcess> activityInformation) {
 	activityInformation.getProcess().cancel();
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
 	return "resources/AcquisitionResources";
     }
 
     @Override
    public boolean isConfirmationNeeded() {
 	return true;
     }
 }
