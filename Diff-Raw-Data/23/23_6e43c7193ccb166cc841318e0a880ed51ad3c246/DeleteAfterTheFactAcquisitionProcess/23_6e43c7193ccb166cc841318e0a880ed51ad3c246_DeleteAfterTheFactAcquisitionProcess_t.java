 package pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AcquisitionAfterTheFact;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class DeleteAfterTheFactAcquisitionProcess extends
 	WorkflowActivity<AfterTheFactAcquisitionProcess, ActivityInformation<AfterTheFactAcquisitionProcess>> {
 
     @Override
     public boolean isActive(AfterTheFactAcquisitionProcess process, User user) {
 	final Person loggedPerson = Person.getLoggedPerson();
 	final AcquisitionAfterTheFact acquisitionAfterTheFact = process.getAcquisitionAfterTheFact();
 
 	return loggedPerson != null
 		&& ((loggedPerson.hasRoleType(RoleType.ACQUISITION_CENTRAL) || loggedPerson
 			.hasRoleType(RoleType.ACQUISITION_CENTRAL_MANAGER)))
 		&& !acquisitionAfterTheFact.getDeletedState().booleanValue();
     }
 
     @Override
     protected void process(ActivityInformation<AfterTheFactAcquisitionProcess> activityInformation) {
 	activityInformation.getProcess().delete();
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
    public boolean isConfirmationNeeded(AfterTheFactAcquisitionProcess process) {
 	return true;
     }
 
     @Override
     public String getLocalizedConfirmationMessage() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "message.confirm.delete.acquisition.process");
     }
 
 }
