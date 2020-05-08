 package module.workingCapital.domain;
 
 import module.organization.domain.Person;
 import myorg.domain.util.Money;
 import myorg.util.BundleUtil;
 
 public class WorkingCapitalAcquisitionSubmission extends WorkingCapitalAcquisitionSubmission_Base {
     
     public WorkingCapitalAcquisitionSubmission() {
         super();
     }
 
     public WorkingCapitalAcquisitionSubmission(final WorkingCapital workingCapital, final Person person, final Money value) {
 	this();
 	setWorkingCapital(workingCapital);
 	setPerson(person);
 	setValue(value);
     }
 
     @Override
     public String getDescription() {
 	return BundleUtil.getStringFromResourceBundle("resources/WorkingCapitalResources", "label." + getClass().getName());
     }
 
     @Override
     public Money getAccumulatedValue() {
 	return Money.ZERO;
     }
 
     @Override
     protected void restoreDebtOfFollowingTransactions(final Money debtValue, final Money accumulatedValue) {
 	restoreDebt(debtValue, accumulatedValue);
 	final WorkingCapitalTransaction workingCapitalTransaction = getNext();
 	if (workingCapitalTransaction != null) {
 	    workingCapitalTransaction.restoreDebtOfFollowingTransactions(debtValue, Money.ZERO);
 	}	
     }
 
    @Override
    public void restoreDebt(Money debtValue, Money accumulatedValue) {
        super.restoreDebt(debtValue, accumulatedValue);
        setValue(getValue().subtract(debtValue));
    }

 }
