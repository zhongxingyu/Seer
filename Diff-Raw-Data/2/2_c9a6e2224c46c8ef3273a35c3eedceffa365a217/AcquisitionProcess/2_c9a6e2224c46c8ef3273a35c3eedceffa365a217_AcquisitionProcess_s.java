 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AddAcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AddPayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ApproveAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AssignPayingUnitToItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ConfirmInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.CreateAcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.CreateAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.DeleteAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.DeleteAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.DistributeRealValuesForPayingUnits;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.EditAcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.EditAcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.EditAcquisitionRequestItemRealValues;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.FundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.FundAllocationExpirationDate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.PayAcquisition;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ReceiveInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.RejectAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.RemovePayingUnit;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.SubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.UnApproveAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericLog;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.fenixWebFramework.security.UserView;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class AcquisitionProcess extends AcquisitionProcess_Base {
 
     private static Map<ActivityScope, List<GenericAcquisitionProcessActivity>> activities = new HashMap<ActivityScope, List<GenericAcquisitionProcessActivity>>();
 
     public enum ActivityScope {
 	REQUEST_INFORMATION, REQUEST_ITEM;
     }
 
     static {
 	List<GenericAcquisitionProcessActivity> requestInformationActivities = new ArrayList<GenericAcquisitionProcessActivity>();
 	List<GenericAcquisitionProcessActivity> requestItemActivities = new ArrayList<GenericAcquisitionProcessActivity>();
 
 	requestInformationActivities.add(new AddAcquisitionProposalDocument());
 	requestInformationActivities.add(new AllocateFundsPermanently());
 	requestInformationActivities.add(new ApproveAcquisitionProcess());
 	requestInformationActivities.add(new UnApproveAcquisitionProcess());
 	requestInformationActivities.add(new RejectAcquisitionProcess());
 	requestInformationActivities.add(new AddPayingUnit());
 	requestInformationActivities.add(new RemovePayingUnit());
 	requestInformationActivities.add(new CreateAcquisitionRequest());
 	requestInformationActivities.add(new CreateAcquisitionRequestItem());
 	requestInformationActivities.add(new DeleteAcquisitionProcess());
 	requestInformationActivities.add(new FundAllocation());
 	requestInformationActivities.add(new FundAllocationExpirationDate());
 	requestInformationActivities.add(new PayAcquisition());
 	requestInformationActivities.add(new ReceiveInvoice());
 	requestInformationActivities.add(new ConfirmInvoice());
 	requestInformationActivities.add(new SubmitForApproval());
 	requestInformationActivities.add(new EditAcquisitionRequest());
 
 	requestItemActivities.add(new DeleteAcquisitionRequestItem());
 	requestItemActivities.add(new EditAcquisitionRequestItem());
 	requestItemActivities.add(new AssignPayingUnitToItem());
 	requestItemActivities.add(new EditAcquisitionRequestItemRealValues());
 	requestItemActivities.add(new DistributeRealValuesForPayingUnits());
 
 	activities.put(ActivityScope.REQUEST_INFORMATION, requestInformationActivities);
 	activities.put(ActivityScope.REQUEST_ITEM, requestItemActivities);
 
     }
 
     protected AcquisitionProcess(final Person requester) {
 	super();
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.IN_GENESIS);
 	new AcquisitionRequest(this, requester);
     }
 
     protected AcquisitionProcess(Supplier supplier, Person person) {
 	super();
 	new AcquisitionProcessState(this, AcquisitionProcessStateType.IN_GENESIS);
 	new AcquisitionRequest(this, supplier, person);
     }
 
     public static boolean isCreateNewAcquisitionProcessAvailable() {
 	return UserView.getUser() != null;
     }
 
     @Service
     public static AcquisitionProcess createNewAcquisitionProcess(final CreateAcquisitionProcessBean createAcquisitionProcessBean) {
 	if (!isCreateNewAcquisitionProcessAvailable()) {
 	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.createNewAcquisitionProcess");
 	}
 	AcquisitionProcess process = new AcquisitionProcess(createAcquisitionProcessBean.getSupplier(),
 		createAcquisitionProcessBean.getRequester());
 	process.getAcquisitionRequest().setRequestingUnit(createAcquisitionProcessBean.getRequestingUnit());
 	if (createAcquisitionProcessBean.isRequestUnitPayingUnit()) {
 	    process.getAcquisitionRequest().addPayingUnits(createAcquisitionProcessBean.getRequestingUnit());
 	}
 
 	return process;
     }
 
     public Person getRequestor() {
 	return getAcquisitionRequest().getRequester();
     }
 
     public Unit getRequestingUnit() {
 	return getAcquisitionRequest().getRequestingUnit();
     }
 
     public List<Unit> getPayingUnits() {
 	return getAcquisitionRequest().getPayingUnits();
     }
 
     public boolean isResponsibleForUnit(Person person) {
 	List<Unit> payingUnits = getPayingUnits();
 
 	for (Authorization authorization : person.getAuthorizations()) {
 	    if (payingUnits.contains(authorization.getUnit())) {
 		return true;
 	    }
 	}
 	return false;
 
     }
 
     public boolean isResponsibleForUnit() {
 	User user = UserView.getUser();
 	if (user == null) {
 	    return false;
 	}
	return isResponsibleForUnit(user.getPerson();
     }
 
     public void delete() {
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	acquisitionRequest.delete();
 	removeExpenditureTrackingSystem();
 	Transaction.deleteObject(this);
     }
 
     public boolean isEditRequestItemAvailable() {
 	User user = UserView.getUser();
 	return user != null && user.getPerson().equals(getRequestor())
 		&& isProcessInState(AcquisitionProcessStateType.IN_GENESIS);
     }
 
     public boolean isPendingApproval() {
 	return isProcessInState(AcquisitionProcessStateType.SUBMITTED_FOR_APPROVAL);
     }
 
     public boolean isApproved() {
 	return isProcessInState(AcquisitionProcessStateType.APPROVED);
     }
 
     public boolean isProcessInState(AcquisitionProcessStateType state) {
 	return getLastAcquisitionProcessStateType().equals(state);
     }
 
     protected AcquisitionProcessState getLastAcquisitionProcessState() {
 	return Collections.max(getAcquisitionProcessStates(), AcquisitionProcessState.COMPARATOR_BY_WHEN);
     }
 
     protected AcquisitionProcessStateType getLastAcquisitionProcessStateType() {
 	return getLastAcquisitionProcessState().getAcquisitionProcessStateType();
     }
 
     public AcquisitionProcessState getAcquisitionProcessState() {
 	return getLastAcquisitionProcessState();
     }
 
     public AcquisitionProcessStateType getAcquisitionProcessStateType() {
 	return getLastAcquisitionProcessStateType();
     }
 
     public List<GenericAcquisitionProcessActivity> getActiveActivitiesForItem() {
 	return getActiveActivities(ActivityScope.REQUEST_ITEM);
     }
 
     public List<GenericAcquisitionProcessActivity> getActiveActivitiesForRequest() {
 	return getActiveActivities(ActivityScope.REQUEST_INFORMATION);
     }
 
     public List<GenericAcquisitionProcessActivity> getActiveActivities(ActivityScope scope) {
 	List<GenericAcquisitionProcessActivity> activitiesResult = new ArrayList<GenericAcquisitionProcessActivity>();
 	for (GenericAcquisitionProcessActivity activity : activities.get(scope)) {
 	    if (activity.isActive(this)) {
 		activitiesResult.add(activity);
 	    }
 	}
 	return activitiesResult;
     }
 
     public List<GenericAcquisitionProcessActivity> getActiveActivities() {
 	List<GenericAcquisitionProcessActivity> activitiesResult = new ArrayList<GenericAcquisitionProcessActivity>();
 	for (ActivityScope scope : activities.keySet()) {
 	    activitiesResult.addAll(getActiveActivities(scope));
 	}
 	return activitiesResult;
     }
 
     public boolean isPersonAbleToExecuteActivities() {
 	for (ActivityScope scope : activities.keySet()) {
 	    for (AbstractActivity<AcquisitionProcess> activity : activities.get(scope)) {
 		if (activity.isActive(this)) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     public boolean isAcquisitionProcessed() {
 	return isProcessInState(AcquisitionProcessStateType.ACQUISITION_PROCESSED);
     }
 
     public boolean isInvoiceReceived() {
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	return isProcessInState(AcquisitionProcessStateType.INVOICE_RECEIVED) && acquisitionRequest.isInvoiceReceived();
     }
 
     public Unit getUnit() {
 	return getRequestingUnit();
     }
 
     public boolean isAllowedToViewCostCenterExpenditures() {
 	try {
 	    return getUnit() != null && isResponsibleForUnit() || userHasRole(RoleType.ACCOUNTABILITY);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    throw new Error(e);
 	}
     }
 
     protected boolean userHasRole(final RoleType roleType) {
 	final User user = UserView.getUser();
 	return user != null && user.getPerson().hasRoleType(roleType);
     }
 
     public boolean isAllowedToViewSupplierExpenditures() {
 	return userHasRole(RoleType.ACQUISITION_CENTRAL);
     }
 
     @Override
     public GenericAcquisitionProcessActivity getActivityByName(String activityName) {
 
 	for (ActivityScope scope : activities.keySet()) {
 	    for (GenericAcquisitionProcessActivity activity : activities.get(scope)) {
 		if (activity.getName().equals(activityName)) {
 		    return activity;
 		}
 	    }
 	}
 	return null;
     }
 
     public List<OperationLog> getOperationLogsInState(AcquisitionProcessStateType state) {
 	List<OperationLog> logs = new ArrayList<OperationLog>();
 	for (OperationLog log : getOperationLogs()) {
 	    if (log.getState() == state) {
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
 
     public boolean isAvailableForPerson(Person person) {
 	return person.hasRoleType(RoleType.ACCOUNTABILITY) || person.hasRoleType(RoleType.ACQUISITION_CENTRAL)
 		|| getRequestor() == person || getRequestingUnit().isResponsible(person)
 		|| isResponsibleForAtLeastOnePayingUnit(person);
     }
 
     public boolean isResponsibleForAtLeastOnePayingUnit(Person person) {
 	for (Unit unit : getPayingUnits()) {
 	    if (unit.isResponsible(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isAvailableForCurrentUser() {
 	User user = UserView.getUser();
 	return user != null && isAvailableForPerson(user.getPerson());
     }
 
     public boolean isRealValueEqualOrLessThanFundAllocation() {
 	Money allocatedMoney = this.getAcquisitionRequest().getTotalItemValue();
 	Money realMoney = this.getAcquisitionRequest().getRealTotalValue();
 
 	return realMoney.isLessThanOrEqual(allocatedMoney);
     }
 
     public boolean isRejected() {
 	return isProcessInState(AcquisitionProcessStateType.REJECTED);
     }
 
 }
