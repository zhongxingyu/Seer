 package module.workingCapital.domain.activity;
 
 import java.math.BigDecimal;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import module.workingCapital.domain.WorkingCapital;
 import module.workingCapital.domain.WorkingCapitalAcquisitionSubmission;
 import module.workingCapital.domain.WorkingCapitalInitialization;
 import module.workingCapital.domain.WorkingCapitalProcess;
 import module.workingCapital.domain.WorkingCapitalTransaction;
 import module.workingCapital.domain.util.PaymentMethod;
 import myorg.domain.util.Money;
 
 public class RequestCapitalActivityInformation extends ActivityInformation<WorkingCapitalProcess> {
 
     private Money requestedValue;
     private PaymentMethod paymentMethod;
 
     public RequestCapitalActivityInformation(final WorkingCapitalProcess workingCapitalProcess,
 	    final WorkflowActivity<WorkingCapitalProcess, ? extends ActivityInformation<WorkingCapitalProcess>> activity) {
 	super(workingCapitalProcess, activity);
 	final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
 	final WorkingCapitalInitialization workingCapitalInitialization = workingCapital.getWorkingCapitalInitialization();
 	final String internationalBankAccountNumber = workingCapitalInitialization.getInternationalBankAccountNumber();
 	paymentMethod = internationalBankAccountNumber == null  || internationalBankAccountNumber.isEmpty() ? PaymentMethod.CHECK : PaymentMethod.WIRETRANSFER;
 	if (workingCapital.hasAnyWorkingCapitalTransactions()) {
 	    final WorkingCapitalTransaction workingCapitalTransaction = workingCapital.getLastTransaction();
 	    if (workingCapitalTransaction instanceof WorkingCapitalAcquisitionSubmission) {
 		final WorkingCapitalAcquisitionSubmission workingCapitalAcquisitionSubmission = (WorkingCapitalAcquisitionSubmission) workingCapitalTransaction;
 		requestedValue = workingCapitalAcquisitionSubmission.getValue();
 	    } else {
 		requestedValue = workingCapitalTransaction.getAccumulatedValue();
 	    }
 	    if (requestedValue.isGreaterThan(workingCapitalInitialization.getAuthorizedAnualValue())) {
 		requestedValue = workingCapitalInitialization.getAuthorizedAnualValue();
 	    }
 	} else {
 	    final Money maxAnualValue = workingCapitalInitialization.getMaxAuthorizedAnualValue();
 	    requestedValue = maxAnualValue.divideAndRound(new BigDecimal(6));
 	    final Money anualValue = workingCapitalInitialization.getAuthorizedAnualValue();
 	    if (requestedValue.isGreaterThan(anualValue)) {
 		requestedValue = anualValue;
 	    }
 	}
     }
 
     public PaymentMethod getPaymentMethod() {
         return paymentMethod;
     }
 
     public void setPaymentMethod(PaymentMethod paymentMethod) {
         this.paymentMethod = paymentMethod;
     }
 
     public Money getRequestedValue() {
         return requestedValue;
     }
 
     public void setRequestedValue(Money requestedValue) {
         this.requestedValue = requestedValue;
     }
 
     @Override
     public boolean hasAllneededInfo() {
 	return isForwardedFromInput() && requestedValue != null && !requestedValue.isZero() && paymentMethod != null;
     }
 
 }
