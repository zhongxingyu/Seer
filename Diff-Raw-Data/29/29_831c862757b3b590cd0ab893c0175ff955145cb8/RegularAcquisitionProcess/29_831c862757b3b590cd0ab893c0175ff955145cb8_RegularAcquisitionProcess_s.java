 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericLog;
 
 public abstract class RegularAcquisitionProcess extends RegularAcquisitionProcess_Base {
 
     public enum ActivityScope {
 	REQUEST_INFORMATION, REQUEST_ITEM;
     }
 
     public RegularAcquisitionProcess() {
 	super();
     }
 
     public boolean isSimplifiedAcquisitionProcess() {
 	return false;
     }
 
     public boolean isStandardAcquisitionProcess() {
 	return false;
     }
 
     @Override
     public List<OperationLog> getExecutionLogsForState(String stateName) {
 	AcquisitionProcessStateType state = AcquisitionProcessStateType.valueOf(stateName);
 	List<OperationLog> logs = new ArrayList<OperationLog>();
 	for (OperationLog log : getOperationLogs()) {
 	    if (log.getState().equals(state)) {
 		logs.add(log);
 	    }
 	}
 	return logs;
     }
 
     public List<OperationLog> getOperationLogs() {
 	List<OperationLog> logs = new ArrayList<OperationLog>();
 	for (GenericLog log : super.getExecutionLogs()) {
 	    logs.add((OperationLog) log);
 	}
 	return logs;
     }
 
     public Set<AcquisitionInvoice> getConfirmedInvoices(Person person) {
 	Set<AcquisitionInvoice> invoices = new HashSet<AcquisitionInvoice>();
 	for (AcquisitionRequestItem item : getAcquisitionRequest().getAcquisitionRequestItemsSet()) {
 	    invoices.addAll(item.getConfirmedInvoices(person));
 	}
 	return invoices;
     }
 
     public Set<AcquisitionInvoice> getUnconfirmedInvoices(Person person) {
 	Set<AcquisitionInvoice> invoices = new HashSet<AcquisitionInvoice>();
 	for (AcquisitionRequestItem item : getAcquisitionRequest().getAcquisitionRequestItemsSet()) {
 	    if (item.isResponsible(person)) {
 		invoices.addAll(item.getUnconfirmedInvoices(person));
 	    }
 	}
 	return invoices;
     }
 
     public void confirmInvoiceBy(Person person) {
 	getAcquisitionRequest().confirmInvoiceFor(person);
 	if (getAcquisitionRequest().isInvoiceConfirmedBy() && getLastAcquisitionProcessState().isPendingInvoiceConfirmation()) {
 	    confirmInvoice();
 	}
     }
 
     public void cancelInvoiceConfirmationBy(final Person person) {
 	getAcquisitionRequest().unconfirmInvoiceFor(person);
 	if (getLastAcquisitionProcessState().isInvoiceConfirmed()) {
 	    cancelInvoiceConfirmation();
 	}
     }
 
     public void unconfirmInvoiceForAll() {
 	getAcquisitionRequest().unconfirmInvoiceForAll();
 	cancelInvoiceConfirmation();
     }
 
     public boolean isPersonAbleToExecuteActivities() {
 	Map<ActivityScope, List<AbstractActivity<RegularAcquisitionProcess>>> activities = getProcessActivityMap();
 	for (ActivityScope scope : activities.keySet()) {
 	    for (AbstractActivity<RegularAcquisitionProcess> activity : activities.get(scope)) {
 		if (activity.isActive(this)) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     @Override
     public void allocateFundsPermanently() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.FUNDS_ALLOCATED_PERMANENTLY);
     }
 
     public void cancel() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.CANCELED);
     }
 
     protected void authorize() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.AUTHORIZED);
     }
 
     protected void cancelInvoiceConfirmation() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.SUBMITTED_FOR_CONFIRM_INVOICE);
     }
 
     protected void confirmInvoice() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.INVOICE_CONFIRMED);
     }
 
     public void allocateFundsToUnit() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.FUNDS_ALLOCATED);
     }
 
     public void allocateFundsToSupplier() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.FUNDS_ALLOCATED_TO_SERVICE_PROVIDER);
     }
 
     public void acquisitionPayed() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.ACQUISITION_PAYED);
     }
 
     public void invoiceReceived() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.INVOICE_RECEIVED);
     }
 
     public void reject() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.REJECTED);
     }
 
     public void inGenesis() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.IN_GENESIS);
     }
 
     @Override
     public void submitForApproval() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.SUBMITTED_FOR_APPROVAL);
     }
 
     public void processAcquisition() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.ACQUISITION_PROCESSED);
     }
 
     public void revertProcessedAcquisition() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.AUTHORIZED);
     }
 
     public void submitedForInvoiceConfirmation() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.SUBMITTED_FOR_CONFIRM_INVOICE);
     }
 
     public void submitForFundAllocation() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.SUBMITTED_FOR_FUNDS_ALLOCATION);
     }
 
     public void skipFundAllocation() {
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.FUNDS_ALLOCATED_TO_SERVICE_PROVIDER);
     }
 
     @Override
     public void resetEffectiveFundAllocationId() {
 	getAcquisitionRequest().resetEffectiveFundAllocationId();
 	confirmInvoice();
     }
 
     @Override
     public boolean isProcessFlowCharAvailable() {
 	return true;
     }
 
     public abstract Map<ActivityScope, List<AbstractActivity<RegularAcquisitionProcess>>> getProcessActivityMap();
 
     @Override
     public void setSkipSupplierFundAllocation(Boolean skipSupplierFundAllocation) {
 	throw new DomainException("error.illegal.method.use");
     }
 
     public void unSkipSupplierFundAllocation() {
 	for (Supplier supplier : getAcquisitionRequest().getSuppliers()) {
 	    if (!supplier.isFundAllocationAllowed(getAcquisitionRequest().getTotalItemValue())) {
 		throw new DomainException("acquisitionProcess.message.exception.SupplierDoesNotAlloweAmount");
 	    }
 	}
 	super.setSkipSupplierFundAllocation(Boolean.FALSE);
 	if (!getAcquisitionProcessState().isInGenesis()) {
 	    LocalDate now = new LocalDate();
 	    setFundAllocationExpirationDate(now.plusDays(90));
 	}
     }
 
     public void skipSupplierFundAllocation() {
 	super.setSkipSupplierFundAllocation(Boolean.TRUE);
 	setFundAllocationExpirationDate(null);
     }
 
     public boolean isFinanceByAnyUnit(List<Unit> fromUnits) {
 	for (Financer financer : getAcquisitionRequest().getFinancers()) {
 	    if (fromUnits.contains(financer.getUnit())) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isPersonAbleToDirectlyAuthorize(Person person) {
 	return isFinanceByAnyUnit(person.getDirectResponsibleUnits()) ? true : getRequestingUnit().isMostDirectAuthorization(
 		person, getAcquisitionRequest().getTotalItemValueWithAdditionalCostsAndVat());
     }
 
     public List<Unit> getFinancingUnits() {
 	List<Unit> units = new ArrayList<Unit>();
 	for (Financer financer : getAcquisitionRequest().getFinancers()) {
 	    units.add(financer.getUnit());
 	}
 	return units;
     }
 
     public boolean isInAuthorizedState() {
 	return getAcquisitionProcessState().isAuthorized();
     }
 
     @Override
     public <T extends GenericLog> T logExecution(Person person, String operationName, Object... args) {
 	return (T) new OperationLog(this, person, operationName, new DateTime(), getAcquisitionProcessStateType());
     }
 
     public void removeFundAllocationExpirationDate() {
 	setFundAllocationExpirationDate(null);
 	if (!getAcquisitionProcessState().isCanceled()) {
 	    submitForFundAllocation();
 	}
     }
 
     @Override
     public boolean isInAllocatedToUnitState() {
 	return getAcquisitionProcessState().isInAllocatedToUnitState();
     }
 }
