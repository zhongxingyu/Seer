 package pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates;
 
 import java.util.Set;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public class DefaultPredicate extends SearchPredicate {
 
     @Override
     public boolean evaluate(PaymentProcess process, SearchPaymentProcess searchBean) {
 	RequestWithPayment request = process.getRequest();
 	return request != null && matchesSearchCriteria(request, searchBean)
		&& (process.isAvailableForCurrentUser() || process.isTakenByCurrentUser());
     }
 
     private boolean matchesSearchCriteria(RequestWithPayment request, SearchPaymentProcess searchBean) {
 	final Person person = request.getRequester();
 	final Set<AccountingUnit> accountingUnits = request.getAccountingUnits();
 	final Unit requestingUnit = request.getRequestingUnit();
 
 	return matchCriteria(searchBean.getRequestingPerson(), person)
 		&& matchCriteria(searchBean.getAccountingUnit(), accountingUnits)
 		&& matchCriteria(searchBean.getRequestingUnit(), requestingUnit)
 		&& matchCriteria(searchBean.getPayingUnit(), request.getFinancersSet())
 		&& matchCriteria(searchBean.getProcessId(), request.getProcess().getAcquisitionProcessId());
     }
 }
