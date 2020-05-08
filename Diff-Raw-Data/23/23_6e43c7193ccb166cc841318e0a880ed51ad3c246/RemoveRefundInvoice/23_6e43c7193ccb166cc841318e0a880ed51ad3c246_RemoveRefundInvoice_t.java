 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 
 public class RemoveRefundInvoice extends WorkflowActivity<RefundProcess, RemoveRefundInvoiceActivityInformation> {
 
     @Override
     public boolean isActive(RefundProcess process, User user) {
 	return process.getRequestor() == user.getExpenditurePerson() && isUserProcessOwner(process, user)
 		&& process.isInAuthorizedState() && process.isAnyRefundInvoiceAvailable();
     }
 
     /*
      * TODO: This should be only invoice removable when these invoices are as
      * files.
      */
     @Override
     protected void process(RemoveRefundInvoiceActivityInformation activityInformation) {
 	activityInformation.getRefundInvoice().delete();
     }
 
     @Override
     public ActivityInformation<RefundProcess> getActivityInformation(RefundProcess process) {
 	return new RemoveRefundInvoiceActivityInformation(process, this);
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
     public boolean isVisible() {
 	return false;
     }
     
     @Override
    public boolean isConfirmationNeeded(RefundProcess process) {
 	return true;
     }
     
 }
