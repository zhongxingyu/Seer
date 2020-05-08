 package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import pt.ist.expenditureTrackingSystem.domain.ProcessState;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessState;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess.ActivityScope;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateProjectFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.Authorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.FundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAddPayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAssignPayingUnitToItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericRemovePayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.RemoveFundsPermanentlyAllocated;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.UnApprove;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.UnAuthorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.Approve;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.CancelRefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.ChangeFinancersAccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.ConfirmInvoices;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.CreateRefundInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.CreateRefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.DeleteRefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.DistributeRealValuesForPayingUnits;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.EditRefundInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.EditRefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities.RefundPerson;
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
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class RefundProcess extends RefundProcess_Base {
 
     private static Map<ActivityScope, List<AbstractActivity<RefundProcess>>> activityMap = new HashMap<ActivityScope, List<AbstractActivity<RefundProcess>>>();
 
     static {
 	List<AbstractActivity<RefundProcess>> requestActivitites = new ArrayList<AbstractActivity<RefundProcess>>();
 	requestActivitites.add(new CreateRefundItem());
 	requestActivitites.add(new GenericAddPayingUnit<RefundProcess>());
 	requestActivitites.add(new GenericRemovePayingUnit<RefundProcess>());
 	requestActivitites.add(new SubmitForApproval());
 	requestActivitites.add(new UnSubmitForApproval());
 	requestActivitites.add(new Approve());
 	requestActivitites.add(new UnApprove<RefundProcess>());
 	requestActivitites.add(new ProjectFundAllocation<RefundProcess>());
 	requestActivitites.add(new RemoveProjectFundAllocation());
 	requestActivitites.add(new FundAllocation<RefundProcess>());
 	requestActivitites.add(new RemoveFundAllocation());
 	requestActivitites.add(new Authorize<RefundProcess>());
 	requestActivitites.add(new UnAuthorize<RefundProcess>());
 	requestActivitites.add(new UnSubmitForFundAllocation());
 	requestActivitites.add(new SubmitForInvoiceConfirmation());
 	requestActivitites.add(new ConfirmInvoices());
 	requestActivitites.add(new AllocateProjectFundsPermanently<RefundProcess>());
 	requestActivitites.add(new AllocateFundsPermanently<RefundProcess>());
 	requestActivitites.add(new RemoveFundsPermanentlyAllocated<RefundProcess>());
 	requestActivitites.add(new RefundPerson());
 	requestActivitites.add(new CancelRefundProcess());
 	requestActivitites.add(new ChangeFinancersAccountingUnit());
 	requestActivitites.add(new UnconfirmInvoices());
 	requestActivitites.add(new RevertInvoiceConfirmationSubmition());
 	requestActivitites.add(new SetSkipSupplierFundAllocation());
 	requestActivitites.add(new UnsetSkipSupplierFundAllocation());
 	activityMap.put(ActivityScope.REQUEST_INFORMATION, requestActivitites);
 
 	List<AbstractActivity<RefundProcess>> itemActivities = new ArrayList<AbstractActivity<RefundProcess>>();
 	itemActivities.add(new EditRefundItem());
 	itemActivities.add(new DeleteRefundItem());
 	itemActivities.add(new GenericAssignPayingUnitToItem<RefundProcess>());
 	itemActivities.add(new CreateRefundInvoice());
 	itemActivities.add(new RemoveRefundInvoice());
 	itemActivities.add(new EditRefundInvoice());
 	itemActivities.add(new DistributeRealValuesForPayingUnits());
 	activityMap.put(ActivityScope.REQUEST_ITEM, itemActivities);
     }
 
     public RefundProcess(Person requestor, String refundeeName, String refundeeFiscalCode, Unit requestingUnit) {
 	super();
 	new RefundRequest(this, requestor, refundeeName, refundeeFiscalCode, requestingUnit);
 	new RefundProcessState(this, RefundProcessStateType.IN_GENESIS);
	setSkipSupplierFundAllocation(Boolean.FALSE);
     }
 
     public RefundProcess(Person requestor, Person refundee, Unit requestingUnit) {
 	super();
 	new RefundRequest(this, requestor, refundee, requestingUnit);
 	new RefundProcessState(this, RefundProcessStateType.IN_GENESIS);
	setSkipSupplierFundAllocation(Boolean.FALSE);
     }
 
     public List<AbstractActivity<RefundProcess>> getActiveActivities() {
 	List<AbstractActivity<RefundProcess>> activities = new ArrayList<AbstractActivity<RefundProcess>>();
 	for (ActivityScope scope : ActivityScope.values()) {
 	    activities.addAll(getActiveActivitiesForScope(scope));
 	}
 	return activities;
     }
 
     private List<AbstractActivity<RefundProcess>> getActiveActivitiesForScope(ActivityScope scope) {
 	List<AbstractActivity<RefundProcess>> activities = new ArrayList<AbstractActivity<RefundProcess>>();
 	for (AbstractActivity<RefundProcess> activity : activityMap.get(scope)) {
 	    if (activity.isActive(this)) {
 		activities.add(activity);
 	    }
 	}
 	return activities;
     }
 
     public List<AbstractActivity<RefundProcess>> getActiveActivitiesForRequest() {
 	return getActiveActivitiesForScope(ActivityScope.REQUEST_INFORMATION);
     }
 
     public List<AbstractActivity<RefundProcess>> getActiveActivitiesForItem() {
 	return getActiveActivitiesForScope(ActivityScope.REQUEST_ITEM);
     }
 
     @Override
     public AbstractActivity<RefundProcess> getActivityByName(String activityName) {
 	for (ActivityScope scope : ActivityScope.values()) {
 	    for (AbstractActivity<RefundProcess> activity : activityMap.get(scope)) {
 		if (activity.getName().equals(activityName)) {
 		    return activity;
 		}
 	    }
 	}
 	return null;
     }
 
     @Service
     public static RefundProcess createNewRefundProcess(CreateRefundProcessBean bean) {
 
 	RefundProcess process = bean.isExternalPerson() ? new RefundProcess(bean.getRequestor(), bean.getRefundeeName(), bean
 		.getRefundeeFiscalCode(), bean.getRequestingUnit()) : new RefundProcess(bean.getRequestor(), bean.getRefundee(),
 		bean.getRequestingUnit());
 	if (bean.isRequestUnitPayingUnit()) {
 	    process.getRequest().addPayingUnit(bean.getRequestingUnit());
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
 
     @Override
     public boolean hasAnyAvailableActivitity() {
 	return !getActiveActivities().isEmpty();
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
 
     public List<RefundInvoice> getRefundableInvoices() {
 	List<RefundInvoice> invoices = new ArrayList<RefundInvoice>();
 	for (RequestItem item : getRequest().getRequestItems()) {
 	    invoices.addAll(((RefundItem) item).getInvoices());
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
 
     public boolean isActive() {
 	return getProcessState().isActive();
     }
 
     public Integer getYear() {
 	return getPaymentProcessYear().getYear();
     }
 
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
 	    if (item.hasAnyInvoices()) {
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
 		|| isAccountingEmployee(person) || isProjectAccountingEmployee(person) || isTreasuryMember(person);
     }
 
     public boolean isTakenByCurrentUser() {
 	final Person loggedPerson = Person.getLoggedPerson();
 	return loggedPerson != null && isTakenByPerson(loggedPerson);
     }
 
     public boolean isTakenByPerson(final Person person) {
 	return person != null && person == getCurrentOwner();
     }
 
     public boolean isPersonAbleToExecuteActivities() {
 	for (List<AbstractActivity<RefundProcess>> activities : activityMap.values()) {
 	    for (final AbstractActivity<RefundProcess> activity : activities) {
 		if (activity.isActive(this)) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     @Override
     public boolean isAuthorized() {
 	return super.isAuthorized() && getRefundableInvoices().isEmpty();
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
 	for (RefundInvoice invoice : getRefundableInvoices()) {
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
 }
