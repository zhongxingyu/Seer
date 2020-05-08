 package module.workingCapital.domain.activity;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import module.workingCapital.domain.WorkingCapital;
 import module.workingCapital.domain.WorkingCapitalInitialization;
 import module.workingCapital.domain.WorkingCapitalProcess;
 import module.workingCapital.domain.WorkingCapitalSystem;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 
 public class UnAuthorizeActivity extends WorkflowActivity<WorkingCapitalProcess, WorkingCapitalInitializationInformation> {
 
     @Override
     public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/WorkingCapitalResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final WorkingCapitalProcess missionProcess, final User user) {
	final WorkingCapitalSystem workingCapitalSystem = WorkingCapitalSystem.getInstance(); 
 	if (workingCapitalSystem.isManagementeMember(user)) {
 	    final WorkingCapital workingCapital = missionProcess.getWorkingCapital();
	    for (final WorkingCapitalInitialization workingCapitalInitialization : workingCapital.getWorkingCapitalInitializationsSet()) {
 		if (workingCapitalInitialization.hasResponsibleForUnitAuthorization()) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     @Override
     protected void process(final WorkingCapitalInitializationInformation activityInformation) {
 	final WorkingCapitalInitialization workingCapitalInitialization = activityInformation.getWorkingCapitalInitialization();
 	workingCapitalInitialization.unauthorize();
     }
 
     @Override
     public ActivityInformation<WorkingCapitalProcess> getActivityInformation(final WorkingCapitalProcess process) {
        return new WorkingCapitalInitializationInformation(process, this);
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
 	return true;
     }
 
     @Override
     public boolean isVisible() {
 	return false;
     }
 
 }
