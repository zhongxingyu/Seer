 package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class RemovePermanentProjectFunds<P extends PaymentProcess> extends WorkflowActivity<P, ActivityInformation<P>> {
 
     @Override
     public boolean isActive(P process, User user) {
 	return process.isProjectAccountingEmployee(user.getExpenditurePerson()) && isUserProcessOwner(process, user)
		&& process.isInvoiceConfirmed() && process.hasAllocatedFundsPermanentlyForAllProjectFinancers();
     }
 
     @Override
     protected void process(ActivityInformation<P> activityInformation) {
 	activityInformation.getProcess().getRequest().resetPermanentProjectFundAllocationId(Person.getLoggedPerson());
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
     public boolean isUserAwarenessNeeded(P process, User user) {
 	return false;
     }
 }
