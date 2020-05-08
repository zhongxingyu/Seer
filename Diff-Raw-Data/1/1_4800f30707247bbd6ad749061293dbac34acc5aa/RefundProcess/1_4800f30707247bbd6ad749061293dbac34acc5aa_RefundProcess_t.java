 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.ReleaseProcess;
 import module.workflow.activities.StealProcess;
 import module.workflow.activities.TakeProcess;
 import module.workflow.activities.WorkflowActivity;
 import module.workflow.domain.WorkflowProcess;
 import myorg.domain.exceptions.DomainException;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.ProcessState;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessState;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.AllocateFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.AllocateProjectFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.Approve;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.Authorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.FundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.GenericAddPayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.GenericAssignPayingUnitToItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.GenericRemovePayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.ProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.RemoveCancelProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.RemoveFundsPermanentlyAllocated;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.RemovePermanentProjectFunds;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.UnApprove;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.UnAuthorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.CancelRefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.ChangeFinancersAccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.ChangeProcessRequester;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.ConfirmInvoices;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.CreateRefundInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.CreateRefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.DeleteRefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.DistributeRealValuesForPayingUnits;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.EditRefundInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.EditRefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.RemoveFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.RemoveProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.RemoveRefundInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.RevertInvoiceConfirmationSubmition;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.SetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.SubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.SubmitForInvoiceConfirmation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.UnSubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.UnSubmitForFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.UnconfirmInvoices;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.UnsetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.Util;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateRefundProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class RefundProcess extends RefundProcess_Base {
 
     private static List<WorkflowActivity<RefundProcess, ? extends ActivityInformation<RefundProcess>>> activities = new ArrayList<WorkflowActivity<RefundProcess, ? extends ActivityInformation<RefundProcess>>>();
 
     static {
 	activities.add(new Approve<RefundProcess>());
 	activities.add(new UnApprove<RefundProcess>());
 	activities.add(new CancelRefundProcess());
 	activities.add(new ConfirmInvoices());
 	activities.add(new RemoveFundAllocation());
 	activities.add(new RemoveProjectFundAllocation());
 	activities.add(new RevertInvoiceConfirmationSubmition());
 	activities.add(new SetSkipSupplierFundAllocation());
 	activities.add(new SubmitForApproval());
 	activities.add(new SubmitForInvoiceConfirmation());
 	activities.add(new UnconfirmInvoices());
 	activities.add(new UnsetSkipSupplierFundAllocation());
 	activities.add(new UnSubmitForApproval());
 	activities.add(new UnSubmitForFundAllocation());
 	activities.add(new CreateRefundItem());
 	activities.add(new EditRefundItem());
 	activities.add(new DeleteRefundItem());
 	activities.add(new CreateRefundInvoice());
 	activities.add(new RemoveRefundInvoice());
 	activities.add(new GenericAddPayingUnit<RefundProcess>());
 	activities.add(new GenericRemovePayingUnit<RefundProcess>());
 	activities.add(new Authorize<RefundProcess>());
 	activities.add(new UnAuthorize<RefundProcess>());
 	activities.add(new GenericAssignPayingUnitToItem<RefundProcess>());
 	activities.add(new DistributeRealValuesForPayingUnits());
 	activities.add(new FundAllocation<RefundProcess>());
 	activities.add(new ProjectFundAllocation<RefundProcess>());
 	activities.add(new RemoveFundsPermanentlyAllocated<RefundProcess>());
 	activities.add(new RemovePermanentProjectFunds<RefundProcess>());
 	activities.add(new TakeProcess<RefundProcess>());
 	activities.add(new ReleaseProcess<RefundProcess>());
 	activities.add(new StealProcess<RefundProcess>());
 	// activities.add(new GiveProcess<RefundProcess>());
 	activities.add(new EditRefundInvoice());
 	activities.add(new AllocateProjectFundsPermanently<RefundProcess>());
 	activities.add(new AllocateFundsPermanently<RefundProcess>());
 	activities.add(new ChangeFinancersAccountingUnit());
 	activities.add(new ChangeProcessRequester());
 	activities.add(new RemoveCancelProcess<RefundProcess>());
     }
 
     public RefundProcess(Person requestor, String refundeeName, String refundeeFiscalCode, Unit requestingUnit) {
 	super();
 	new RefundRequest(this, requestor, refundeeName, refundeeFiscalCode, requestingUnit);
 	new RefundProcessState(this, RefundProcessStateType.IN_GENESIS);
 	setSkipSupplierFundAllocation(Boolean.FALSE);
 	setProcessNumber(getYear() + "/" + getAcquisitionProcessNumber());
     }
 
     public RefundProcess(Person requestor, Person refundee, Unit requestingUnit) {
 	super();
 	new RefundRequest(this, requestor, refundee, requestingUnit);
 	new RefundProcessState(this, RefundProcessStateType.IN_GENESIS);
 	setSkipSupplierFundAllocation(Boolean.FALSE);
	setProcessNumber(getYear() + "/" + getAcquisitionProcessNumber());
     }
 
     @Service
     public static RefundProcess createNewRefundProcess(CreateRefundProcessBean bean) {
 
 	RefundProcess process = bean.isExternalPerson() ? new RefundProcess(bean.getRequestor(), bean.getRefundeeName(), bean
 		.getRefundeeFiscalCode(), bean.getRequestingUnit()) : new RefundProcess(bean.getRequestor(), bean.getRefundee(),
 		bean.getRequestingUnit());
 	if (bean.isRequestUnitPayingUnit()) {
 	    process.getRequest().addPayingUnit(bean.getRequestingUnit());
 	}
 	if (bean.isForMission()) {
 	    if (bean.getMissionProcess() == null) {
 		throw new DomainException("mission.process.is.mandatory");
 	    }
 	    process.setMissionProcess(bean.getMissionProcess());
 	}
 
 	return process;
     }
 
     protected RefundProcessState getLastProcessState() {
 	return (RefundProcessState) Collections.max(getProcessStates(), ProcessState.COMPARATOR_BY_WHEN);
     }
 
     public RefundProcessState getProcessState() {
 	return getLastProcessState();
     }
 
     @Override
     public boolean isInGenesis() {
 	return getProcessState().isInGenesis();
     }
 
     public Person getRequestor() {
 	return getRequest().getRequester();
     }
 
     @Override
     public void submitForApproval() {
 	new RefundProcessState(this, RefundProcessStateType.SUBMITTED_FOR_APPROVAL);
     }
 
     public List<Unit> getPayingUnits() {
 	List<Unit> res = new ArrayList<Unit>();
 	for (Financer financer : getRequest().getFinancers()) {
 	    res.add(financer.getUnit());
 	}
 	return res;
     }
 
     public boolean isResponsibleForUnit(final Person person) {
 	Set<Authorization> validAuthorizations = person.getValidAuthorizations();
 	for (Unit unit : getPayingUnits()) {
 	    for (Authorization authorization : validAuthorizations) {
 		if (unit.isSubUnit(authorization.getUnit())) {
 		    return true;
 		}
 	    }
 	}
 
 	return false;
     }
 
     public void unSubmitForApproval() {
 	final RefundProcessState refundProcessState = getProcessState();
 	refundProcessState.setRefundProcessStateType(RefundProcessStateType.IN_GENESIS);
     }
 
     @Override
     public boolean isPendingApproval() {
 	final RefundProcessState refundProcessState = getProcessState();
 	return refundProcessState.isPendingApproval();
     }
 
     public void submitForFundAllocation() {
 	new RefundProcessState(this, RefundProcessStateType.APPROVED);
     }
 
     @Override
     public boolean isInApprovedState() {
 	return getProcessState().isInApprovedState();
     }
 
     @Override
     public boolean isInAllocatedToUnitState() {
 	return getProcessState().isInAllocatedToUnitState();
     }
 
     @Override
     protected void authorize() {
 	new RefundProcessState(this, RefundProcessStateType.AUTHORIZED);
     }
 
     @Override
     public boolean isPendingFundAllocation() {
 	return isInApprovedState();
     }
 
     @Override
     public void allocateFundsToUnit() {
 	new RefundProcessState(this, RefundProcessStateType.FUNDS_ALLOCATED);
     }
 
     @Override
     public boolean isInAuthorizedState() {
 	return getProcessState().isAuthorized();
     }
 
     public void unApproveByAll() {
 	getRequest().unSubmitForFundsAllocation();
     }
 
     public boolean isInSubmittedForInvoiceConfirmationState() {
 	return getProcessState().isInSubmittedForInvoiceConfirmationState();
     }
 
     public List<RefundableInvoiceFile> getRefundableInvoices() {
 	List<RefundableInvoiceFile> invoices = new ArrayList<RefundableInvoiceFile>();
 	for (RequestItem item : getRequest().getRequestItems()) {
 	    invoices.addAll(((RefundItem) item).getRefundableInvoices());
 	}
 	return invoices;
     }
 
     public void confirmInvoicesByPerson(Person person) {
 	for (RequestItem item : getRequest().getRequestItems()) {
 	    item.confirmInvoiceBy(person);
 	}
 
 	if (getRequest().isConfirmedForAllInvoices()) {
 	    confirmInvoices();
 	}
     }
 
     public void unconfirmInvoicesByPerson(Person person) {
 	for (RequestItem item : getRequest().getRequestItems()) {
 	    item.unconfirmInvoiceBy(person);
 	}
 	submitForInvoiceConfirmation();
     }
 
     public void revertInvoiceConfirmationSubmition() {
 	new RefundProcessState(this, RefundProcessStateType.AUTHORIZED);
     }
 
     public void submitForInvoiceConfirmation() {
 	new RefundProcessState(this, RefundProcessStateType.SUBMITTED_FOR_INVOICE_CONFIRMATION);
     }
 
     public void confirmInvoices() {
 	new RefundProcessState(this, RefundProcessStateType.INVOICES_CONFIRMED);
     }
 
     public boolean isPendingInvoicesConfirmation() {
 	return getProcessState().isPendingInvoicesConfirmation();
     }
 
     @Override
     public boolean isActive() {
 	return getProcessState().isActive();
     }
 
     public Integer getYear() {
 	return getPaymentProcessYear().getYear();
     }
 
     /*
      * use getProcessNumber() instead
      */
     @Deprecated
     public String getAcquisitionProcessId() {
 	return getYear() + "/" + getAcquisitionProcessNumber();
     }
 
     @Override
     public boolean isInvoiceConfirmed() {
 	return getProcessState().isInvoiceConfirmed();
     }
 
     @Override
     public void allocateFundsPermanently() {
 	new RefundProcessState(this, RefundProcessStateType.FUNDS_ALLOCATED_PERMANENTLY);
     }
 
     @Override
     public boolean isAllocatedPermanently() {
 	return getProcessState().isAllocatedPermanently();
     }
 
     @Override
     public void resetEffectiveFundAllocationId() {
 	getRequest().resetEffectiveFundAllocationId();
 	confirmInvoice();
     }
 
     protected void confirmInvoice() {
 	new RefundProcessState(this, RefundProcessStateType.INVOICES_CONFIRMED);
     }
 
     public boolean hasFundsAllocatedPermanently() {
 	return getProcessState().hasFundsAllocatedPermanently();
     }
 
     public void refundPerson(final String paymentReference) {
 	getRequest().setPaymentReference(paymentReference);
 	new RefundProcessState(this, RefundProcessStateType.REFUNDED);
     }
 
     @Override
     public boolean isPayed() {
 	return getRequest().isPayed();
     }
 
     public boolean isAnyRefundInvoiceAvailable() {
 	for (RefundItem item : getRequest().getRefundItemsSet()) {
 	    if (!item.getRefundableInvoices().isEmpty()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isAvailableForCurrentUser() {
 	final Person loggedPerson = Person.getLoggedPerson();
 	return loggedPerson != null && isAvailableForPerson(loggedPerson);
     }
 
     public boolean isAvailableForPerson(Person person) {
 	return person.hasRoleType(RoleType.ACQUISITION_CENTRAL) || person.hasRoleType(RoleType.ACQUISITION_CENTRAL_MANAGER)
 		|| person.hasRoleType(RoleType.ACCOUNTING_MANAGER) || person.hasRoleType(RoleType.PROJECT_ACCOUNTING_MANAGER)
 		|| person.hasRoleType(RoleType.TREASURY_MANAGER) || getRequestor() == person
 		|| getRequest().getRequestingUnit().isResponsible(person) || isResponsibleForAtLeastOnePayingUnit(person)
 		|| isAccountingEmployee(person) || isProjectAccountingEmployee(person) || isTreasuryMember(person)
 		|| isObserver(person);
     }
 
     @Override
     public boolean isAuthorized() {
 	return super.isAuthorized() && getRefundableInvoices().isEmpty();
     }
 
     public boolean isCanceled() {
 	return getProcessState().isCanceled();
     }
 
     @Override
     public boolean isRefundProcess() {
 	return true;
     }
 
     public void cancel() {
 	new RefundProcessState(this, RefundProcessStateType.CANCELED);
     }
 
     @Override
     public String getProcessStateDescription() {
 	return getLastProcessState().getLocalizedName();
     }
 
     @Override
     public Set<Supplier> getSuppliers() {
 	Set<Supplier> suppliers = new HashSet<Supplier>();
 	for (RefundableInvoiceFile invoice : getRefundableInvoices()) {
 	    suppliers.add(invoice.getSupplier());
 	}
 	return suppliers;
     }
 
     public boolean isAppiableForYear(final int year) {
 	return Util.isAppiableForYear(year, this);
     }
 
     @Override
     public String getProcessStateName() {
 	return getProcessState().getLocalizedName();
     }
 
     @Override
     public int getProcessStateOrder() {
 	return getProcessState().getRefundProcessStateType().ordinal();
     }
 
     @Override
     public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
 	return (List<T>) activities;
     }
 
     @Override
     public void revertToState(ProcessState processState) {
 	final RefundProcessState refundProcessState = (RefundProcessState) processState;
 	final RefundProcessStateType refundProcessStateType = refundProcessState.getRefundProcessStateType();
 	if (refundProcessStateType != null && refundProcessStateType != RefundProcessStateType.CANCELED) {
 	    new RefundProcessState(this, refundProcessStateType);
 	}
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/AcquisitionResources", "label.RefundProcess");
     }
 
 }
