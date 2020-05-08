 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 
 public class DeleteRefundItem extends WorkflowActivity<RefundProcess, DeleteRefundItemActivityInformation> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
 	return process.getRequestor() == user.getExpenditurePerson() && isUserProcessOwner(process, user)
 		&& process.isInGenesis();
     }
 
     @Override
     protected void process(DeleteRefundItemActivityInformation activityInformation) {
 	activityInformation.getItem().delete();
     }
 
     @Override
     public ActivityInformation<RefundProcess> getActivityInformation(RefundProcess process) {
 	return new DeleteRefundItemActivityInformation(process, this);
     }
 
     @Override
     public boolean isVisible() {
 	return false;
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
 
     @Override
     public String getLocalizedConfirmationMessage() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "activity.confirmation." + getClass().getName());
     }
 }
