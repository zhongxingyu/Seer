 package module.workingCapital.domain.activity;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import module.workingCapital.domain.WorkingCapital;
 import module.workingCapital.domain.WorkingCapitalAcquisitionSubmission;
 import module.workingCapital.domain.WorkingCapitalAcquisitionSubmissionDocument;
 import module.workingCapital.domain.WorkingCapitalAcquisitionTransaction;
 import module.workingCapital.domain.WorkingCapitalInitialization;
 import module.workingCapital.domain.WorkingCapitalProcess;
 import module.workingCapital.domain.WorkingCapitalTransaction;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import myorg.util.BundleUtil;
 import net.sf.jasperreports.engine.JRException;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.util.ReportUtils;
 
 public class SubmitForValidationActivity extends WorkflowActivity<WorkingCapitalProcess, SubmitForValidationActivityInformation> {
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/WorkingCapitalResources", "activity."
 		+ getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final WorkingCapitalProcess missionProcess, final User user) {
 	final WorkingCapital workingCapital = missionProcess.getWorkingCapital();
 	return !workingCapital.isCanceledOrRejected() && workingCapital.isMovementResponsible(user)
 		&& workingCapital.hasApprovedAndUnSubmittedAcquisitions() && !workingCapital.hasAcquisitionPendingApproval();
     }
 
     @Override
     protected void process(final SubmitForValidationActivityInformation activityInformation) {
 	final WorkingCapitalProcess workingCapitalProcess = activityInformation.getProcess();
 	workingCapitalProcess.submitAcquisitionsForValidation();
 	final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
 	if (activityInformation.isLastSubmission()) {
 	    final WorkingCapitalInitialization workingCapitalInitialization = workingCapital.getWorkingCapitalInitialization();
 	    workingCapitalInitialization.setLastSubmission(new DateTime());
 	}
 	final Money accumulatedValue = workingCapital.getLastTransaction().getAccumulatedValue();
 	WorkingCapitalAcquisitionSubmission acquisitionSubmission = new WorkingCapitalAcquisitionSubmission(workingCapital,
 		getLoggedPerson().getPerson(), accumulatedValue, activityInformation.isPaymentRequired());
 	WorkingCapitalTransaction previousTransaction = acquisitionSubmission.getPreviousTransaction();
 	while (previousTransaction != null) {
 	    if (previousTransaction.isSubmission()) {
 		break;
 	    }
 	    if ((previousTransaction.isAcquisition()) && previousTransaction.isApproved()) {
 		acquisitionSubmission
 			.addWorkingCapitalAcquisitionTransactions((WorkingCapitalAcquisitionTransaction) previousTransaction);
 	    }
 	    previousTransaction = previousTransaction.getPreviousTransaction();
 	}
 
 	byte[] contents = createAcquisitionSubmissionDocument(acquisitionSubmission);
 	WorkingCapitalAcquisitionSubmissionDocument document = new WorkingCapitalAcquisitionSubmissionDocument(
 		acquisitionSubmission, contents, ".pdf");
 	document.setFilename("Submission" + document.getOid() + document.getFilename());
     }
 
     private byte[] createAcquisitionSubmissionDocument(WorkingCapitalAcquisitionSubmission acquisitionSubmission) {
 	final Map<String, Object> paramMap = new HashMap<String, Object>();
 	paramMap.put("workingCapital", acquisitionSubmission.getWorkingCapital());
 	paramMap.put("responsibleName", acquisitionSubmission.getPerson().getName());
 	paramMap.put("IBAN", acquisitionSubmission.getWorkingCapital().getWorkingCapitalInitialization()
 		.getInternationalBankAccountNumber());
 
 	paramMap.put("submissionTransactionNumber", acquisitionSubmission.getNumber());
 	paramMap.put("submissionDescription", acquisitionSubmission.getDescription());
 	paramMap.put("submissionValue", acquisitionSubmission.getValue());
 	paramMap.put("submissionAccumulatedValue", acquisitionSubmission.getAccumulatedValue());
 	paramMap.put("submissionBalance", acquisitionSubmission.getBalance());
 	paramMap.put("submissionDebt", acquisitionSubmission.getDebt());
 
 	paramMap.put("paymentRequired", BundleUtil.getStringFromResourceBundle("resources/MyorgResources", acquisitionSubmission
 		.getPaymentRequired().toString()));
 
 	final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources/WorkingCapitalResources");
 	try {
 	    byte[] byteArray = ReportUtils.exportToPdfFileAsByteArray("workingCapitalAcquisitionSubmissionDocument", paramMap,
 		    resourceBundle, acquisitionSubmission.getWorkingCapitalAcquisitionTransactionsSorted());
 	    return byteArray;
 	} catch (JRException e) {
 	    e.printStackTrace();
 	    throw new DomainException("workingCapitalAcquisitionSubmissionDocument.exception.failedCreation",
 		    DomainException.getResourceFor("resources/WorkingCapitalResources"));
 	}
     }
 
     @Override
     public ActivityInformation<WorkingCapitalProcess> getActivityInformation(final WorkingCapitalProcess process) {
 	return new SubmitForValidationActivityInformation(process, this);
     }
 
     @Override
     public boolean isUserAwarenessNeeded(final WorkingCapitalProcess process, final User user) {
 	return false;
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
 	return false;
     }
 
 }
