 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateProjectFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.Authorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.FundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAddPayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAssignPayingUnitToItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericRemovePayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.RemoveFundsPermanentlyAllocated;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.RemovePermanentProjectFunds;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.UnApprove;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.UnAuthorize;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.AddAcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CancelAcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CancelInvoiceConfirmation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ChangeAcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ChangeFinancersAccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ChangeProcessClassification;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ConfirmInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CreateAcquisitionPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CreateAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.DeleteAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.DistributeRealValuesForPayingUnits;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.EditAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.EditAcquisitionRequestItemRealValues;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.FundAllocationExpirationDate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.LockInvoiceReceiving;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.PayAcquisition;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ReceiveInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RejectAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveCancelProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveFundAllocationExpirationDate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveFundAllocationExpirationDateForResponsible;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RemoveProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertInvoiceSubmission;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertProcessNotConfirmmingFundAllocationExpirationDate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertSkipPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.RevertToInvoiceConfirmation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SelectSupplier;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SendPurchaseOrderToSupplier;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SkipPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForConfirmInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.UnSubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.UnlockInvoiceReceiving;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.UnsetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class SimplifiedProcedureProcess extends SimplifiedProcedureProcess_Base {
 
     public static enum ProcessClassification implements IPresentableEnum {
 
 	CCP(new Money("5000"), true), CT10000(new Money("10000")), CT75000(new Money("75000"));
 
 	final private Money value;
 	final private boolean ccp;
 
 	ProcessClassification(Money value) {
 	    this(value, false);
 	}
 
 	ProcessClassification(Money value, boolean ccp) {
 	    this.value = value;
 	    this.ccp = ccp;
 	}
 
 	public Money getLimit() {
 	    return value;
 	}
 
 	public boolean isCCP() {
 	    return ccp;
 	}
 
 	@Override
 	public String getLocalizedName() {
 	    return BundleUtil.getFormattedStringFromResourceBundle("resources/ExpenditureResources",
 		    "label.processClassification." + name());
 	}
     }
 
     private static Map<ActivityScope, List<AbstractActivity<RegularAcquisitionProcess>>> activities = new HashMap<ActivityScope, List<AbstractActivity<RegularAcquisitionProcess>>>();
 
     private static List<AcquisitionProcessStateType> availableStates = new ArrayList<AcquisitionProcessStateType>();
 
     static {
 	List<AbstractActivity<RegularAcquisitionProcess>> requestInformationActivities = new ArrayList<AbstractActivity<RegularAcquisitionProcess>>();
 	List<AbstractActivity<RegularAcquisitionProcess>> requestItemActivities = new ArrayList<AbstractActivity<RegularAcquisitionProcess>>();
 
 	requestInformationActivities.add(new SelectSupplier());
 	requestInformationActivities.add(new CreateAcquisitionPurchaseOrderDocument());
 	requestInformationActivities.add(new SendPurchaseOrderToSupplier());
 	requestInformationActivities.add(new SkipPurchaseOrderDocument());
 	requestInformationActivities.add(new RevertSkipPurchaseOrderDocument());
 	requestInformationActivities.add(new GenericAddPayingUnit<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new GenericRemovePayingUnit<RegularAcquisitionProcess>());
 
 	requestInformationActivities.add(new AddAcquisitionProposalDocument());
 	requestInformationActivities.add(new ChangeAcquisitionProposalDocument());
 	requestInformationActivities.add(new CreateAcquisitionRequestItem());
 	requestInformationActivities.add(new SubmitForApproval());
 
 	requestInformationActivities.add(new SubmitForFundAllocation());
 	requestInformationActivities.add(new FundAllocationExpirationDate());
 	requestInformationActivities.add(new RevertProcessNotConfirmmingFundAllocationExpirationDate());
 	requestInformationActivities.add(new RevertToInvoiceConfirmation());
 
 	requestInformationActivities.add(new Authorize<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new RejectAcquisitionProcess());
 
 	requestInformationActivities.add(new AllocateProjectFundsPermanently<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new AllocateFundsPermanently<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new RemovePermanentProjectFunds<RegularAcquisitionProcess>());
 
 	requestInformationActivities.add(new RemoveFundsPermanentlyAllocated<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new UnApprove<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new UnAuthorize<RegularAcquisitionProcess>());
 
 	requestInformationActivities.add(new ProjectFundAllocation<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new FundAllocation<RegularAcquisitionProcess>());
 	requestInformationActivities.add(new RemoveFundAllocation());
 	requestInformationActivities.add(new RemoveProjectFundAllocation());
 	requestInformationActivities.add(new RemoveFundAllocationExpirationDate());
 	requestInformationActivities.add(new RemoveFundAllocationExpirationDateForResponsible());
 	requestInformationActivities.add(new CancelAcquisitionRequest());
 
 	requestInformationActivities.add(new PayAcquisition());
 	requestInformationActivities.add(new ReceiveInvoice());
 	requestInformationActivities.add(new UnlockInvoiceReceiving());
 	requestInformationActivities.add(new LockInvoiceReceiving());
 	requestInformationActivities.add(new RemoveInvoice());
 
 	requestInformationActivities.add(new SubmitForConfirmInvoice());
 	requestInformationActivities.add(new ConfirmInvoice());
 	requestInformationActivities.add(new CancelInvoiceConfirmation());
 	requestInformationActivities.add(new UnSubmitForApproval());
 	requestInformationActivities.add(new ChangeFinancersAccountingUnit());
 
 	requestInformationActivities.add(new ChangeProcessClassification());
 	// requestInformationActivities.add(new SetRefundee());
 	// requestInformationActivities.add(new ChangeRefundee());
 	// requestInformationActivities.add(new UnsetRefundee());
 
 	requestInformationActivities.add(new SetSkipSupplierFundAllocation());
 	requestInformationActivities.add(new UnsetSkipSupplierFundAllocation());
 
 	requestInformationActivities.add(new RevertInvoiceSubmission());
 
 	requestInformationActivities.add(new RemoveCancelProcess());
 
 	requestItemActivities.add(new DeleteAcquisitionRequestItem());
 	requestItemActivities.add(new EditAcquisitionRequestItem());
 	requestItemActivities.add(new GenericAssignPayingUnitToItem<RegularAcquisitionProcess>());
 	requestItemActivities.add(new EditAcquisitionRequestItemRealValues());
 	requestItemActivities.add(new DistributeRealValuesForPayingUnits());
 
 	activities.put(ActivityScope.REQUEST_INFORMATION, requestInformationActivities);
 	activities.put(ActivityScope.REQUEST_ITEM, requestItemActivities);
 
 	availableStates.add(AcquisitionProcessStateType.IN_GENESIS);
 	availableStates.add(AcquisitionProcessStateType.SUBMITTED_FOR_APPROVAL);
 	availableStates.add(AcquisitionProcessStateType.SUBMITTED_FOR_FUNDS_ALLOCATION);
 	availableStates.add(AcquisitionProcessStateType.FUNDS_ALLOCATED_TO_SERVICE_PROVIDER);
 	availableStates.add(AcquisitionProcessStateType.FUNDS_ALLOCATED);
 	availableStates.add(AcquisitionProcessStateType.AUTHORIZED);
 	availableStates.add(AcquisitionProcessStateType.ACQUISITION_PROCESSED);
 	availableStates.add(AcquisitionProcessStateType.INVOICE_RECEIVED);
 	availableStates.add(AcquisitionProcessStateType.SUBMITTED_FOR_CONFIRM_INVOICE);
 	availableStates.add(AcquisitionProcessStateType.INVOICE_CONFIRMED);
 	availableStates.add(AcquisitionProcessStateType.FUNDS_ALLOCATED_PERMANENTLY);
 	availableStates.add(AcquisitionProcessStateType.ACQUISITION_PAYED);
 	availableStates.add(AcquisitionProcessStateType.REJECTED);
 	availableStates.add(AcquisitionProcessStateType.CANCELED);
     }
 
     protected SimplifiedProcedureProcess(final Person requester) {
 	super();
 	inGenesis();
 	new AcquisitionRequest(this, requester);
     }
 
     protected SimplifiedProcedureProcess(Supplier supplier, Person person) {
 	super();
 	inGenesis();
 	new AcquisitionRequest(this, supplier, person);
     }
 
     protected SimplifiedProcedureProcess(ProcessClassification classification, List<Supplier> suppliers, Person person) {
 	super();
 	inGenesis();
 	AcquisitionRequest acquisitionRequest = new AcquisitionRequest(this, suppliers, person);
 	// if (classification == ProcessClassification.CT75000 &&
 	// suppliers.size() < 3) {
 	// throw new
 	// DomainException("acquisitionProcess.message.exception.needsMoreSuppliers");
 	// }
 	if (suppliers.size() == 1) {
 	    acquisitionRequest.setSelectedSupplier(suppliers.get(0));
 	}
 
 	setProcessClassification(classification);
     }
 
     @Service
     public static SimplifiedProcedureProcess createNewAcquisitionProcess(
 	    final CreateAcquisitionProcessBean createAcquisitionProcessBean) {
 	if (!isCreateNewProcessAvailable()) {
 	    throw new DomainException("acquisitionProcess.message.exception.invalidStateToRun.create");
 	}
 	SimplifiedProcedureProcess process = new SimplifiedProcedureProcess(createAcquisitionProcessBean.getClassification(),
 		createAcquisitionProcessBean.getSuppliers(), createAcquisitionProcessBean.getRequester());
 	process.getAcquisitionRequest().setRequestingUnit(createAcquisitionProcessBean.getRequestingUnit());
 	if (createAcquisitionProcessBean.isRequestUnitPayingUnit()) {
 	    final Unit unit = createAcquisitionProcessBean.getRequestingUnit();
 	    process.getAcquisitionRequest().addFinancers(unit.finance(process.getAcquisitionRequest()));
 	}
 
 	return process;
     }
 
     public boolean isEditRequestItemAvailable() {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null && loggedPerson.equals(getRequestor()) && getLastAcquisitionProcessState().isInGenesis();
     }
 
     public List<AbstractActivity<RegularAcquisitionProcess>> getActiveActivitiesForItem() {
 	return getActiveActivities(ActivityScope.REQUEST_ITEM);
     }
 
     public List<AbstractActivity<RegularAcquisitionProcess>> getActiveActivitiesForRequest() {
 	return getActiveActivities(ActivityScope.REQUEST_INFORMATION);
     }
 
     public List<AbstractActivity<RegularAcquisitionProcess>> getActiveActivities(ActivityScope scope) {
 	List<AbstractActivity<RegularAcquisitionProcess>> activitiesResult = new ArrayList<AbstractActivity<RegularAcquisitionProcess>>();
 	for (AbstractActivity<RegularAcquisitionProcess> activity : activities.get(scope)) {
 	    if (activity.isActive(this)) {
 		activitiesResult.add(activity);
 	    }
 	}
 	return activitiesResult;
     }
 
     public List<AbstractActivity<RegularAcquisitionProcess>> getActiveActivities() {
 	List<AbstractActivity<RegularAcquisitionProcess>> activitiesResult = new ArrayList<AbstractActivity<RegularAcquisitionProcess>>();
 	for (ActivityScope scope : activities.keySet()) {
 	    activitiesResult.addAll(getActiveActivities(scope));
 	}
 	return activitiesResult;
     }
 
     @Override
     public AbstractActivity<RegularAcquisitionProcess> getActivityByName(String activityName) {
 
 	for (ActivityScope scope : activities.keySet()) {
 	    for (AbstractActivity<RegularAcquisitionProcess> activity : activities.get(scope)) {
 		if (activity.getName().equals(activityName)) {
 		    return activity;
 		}
 	    }
 	}
 	return null;
     }
 
     @Override
     public Money getAcquisitionRequestValueLimit() {
 	return getProcessClassification().getLimit();
     }
 
     @Override
     public boolean isSimplifiedAcquisitionProcess() {
 	return true;
     }
 
     @Override
     public Map<ActivityScope, List<AbstractActivity<RegularAcquisitionProcess>>> getProcessActivityMap() {
 	return activities;
     }
 
     @Override
     public List<AcquisitionProcessStateType> getAvailableStates() {
 	return availableStates;
     }
 
     public static List<AcquisitionProcessStateType> getAvailableStatesForSimplifiedProcedureProcess() {
 	return availableStates;
     }
 
     @Override
     public boolean hasAnyAvailableActivitity() {
 	for (List<AbstractActivity<RegularAcquisitionProcess>> activityList : activities.values()) {
 	    for (AbstractActivity<RegularAcquisitionProcess> activity : activityList) {
 		if (activity.isActive(this)) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     public boolean isSimplifiedProcedureProcess() {
 	return true;
     }
 
     @Override
     public boolean isPayed() {
 	return getRequest().isPayed();
     }
 
     @Override
     public String getProcessStateDescription() {
 	return getLastAcquisitionProcessState().getLocalizedName();
     }
 
     public boolean isAppiableForYear(final int year) {
 	return Util.isAppiableForYear(year, this);
     }
 
     public boolean isCCP() {
 	return getProcessClassification().isCCP();
     }
 
     @Override
     public void setProcessClassification(ProcessClassification processClassification) {
 	if (getSkipSupplierFundAllocation()) {
 	    unSkipSupplierFundAllocation();
 	}
 	if (processClassification.getLimit().isLessThan(this.getAcquisitionRequest().getCurrentValue())) {
 	    throw new DomainException("error.message.processValueExceedsLimitForClassification");
 	}
 	super.setProcessClassification(processClassification);
     }
 
     public void setProcessClassificationWithoutChecks(ProcessClassification processClassification) {
 	if (processClassification.getLimit().isLessThan(this.getAcquisitionRequest().getCurrentValue())) {
 	    System.out.println("Process: " + getAcquisitionProcessId() + " exceed limit with: "
 		    + getAcquisitionRequest().getCurrentValue().toFormatString());
 	}
 	super.setProcessClassification(processClassification);
     }
 
     public boolean isWarnRegardingProcessClassificationNeeded() {
 	return getProcessClassification().isCCP() != getRequestingUnit().getDefaultRegeimIsCCP();
     }
 
     @Override
     public Boolean getShouldSkipSupplierFundAllocation() {
 	return !this.isCCP() || super.getShouldSkipSupplierFundAllocation();
     }
 
     public boolean isWarnForLessSuppliersActive() {
	return classification == ProcessClassification.CT75000 && suppliers.size() < 3;
     }
 }
