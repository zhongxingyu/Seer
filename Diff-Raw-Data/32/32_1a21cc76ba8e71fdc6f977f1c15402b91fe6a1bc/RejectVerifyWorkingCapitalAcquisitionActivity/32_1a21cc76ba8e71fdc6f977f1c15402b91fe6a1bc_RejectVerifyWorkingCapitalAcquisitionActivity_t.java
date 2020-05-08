 package module.workingCapital.domain.activity;
 
import java.util.ResourceBundle;

 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import module.workingCapital.domain.WorkingCapital;
import module.workingCapital.domain.WorkingCapitalAcquisitionTransaction;
 import module.workingCapital.domain.WorkingCapitalProcess;
 import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
 import myorg.util.BundleUtil;
import pt.utl.ist.fenix.tools.util.i18n.Language;
 
public class RejectVerifyWorkingCapitalAcquisitionActivity extends
	WorkflowActivity<WorkingCapitalProcess, WorkingCapitalTransactionInformation> {
 
     @Override
     public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/WorkingCapitalResources", "activity."
		+ getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final WorkingCapitalProcess missionProcess, final User user) {
 	final WorkingCapital workingCapital = missionProcess.getWorkingCapital();
 	return !workingCapital.isCanceledOrRejected() && workingCapital.hasAcquisitionPendingVerification(user);
     }
 
     @Override
     protected void process(final WorkingCapitalTransactionInformation activityInformation) {
	final WorkingCapitalAcquisitionTransaction workingCapitalTransaction = (WorkingCapitalAcquisitionTransaction) activityInformation
		.getWorkingCapitalTransaction();
	if (!workingCapitalTransaction.isPendingVerification()) {
	    throw new DomainException("error.acquisition.already.verified", ResourceBundle.getBundle(
		    "resources/WorkingCapitalResources", Language.getLocale()));
	}
 	workingCapitalTransaction.rejectVerify(getLoggedPerson());
     }
 
     @Override
     public ActivityInformation<WorkingCapitalProcess> getActivityInformation(final WorkingCapitalProcess process) {
	return new WorkingCapitalTransactionInformation(process, this);
     }
 
     @Override
     public boolean isVisible() {
 	return false;
     }
 
     @Override
     public boolean isUserAwarenessNeeded(final WorkingCapitalProcess process, final User user) {
	return false;
     }
 
 }
