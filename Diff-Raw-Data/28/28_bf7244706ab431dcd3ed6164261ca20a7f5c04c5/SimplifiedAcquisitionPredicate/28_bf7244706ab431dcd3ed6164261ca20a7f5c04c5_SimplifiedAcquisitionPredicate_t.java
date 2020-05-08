 package pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates;
 
 import java.util.List;
 import java.util.Set;
 
 import module.workflow.domain.ProcessFile;
 import myorg.domain.User;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess.SearchProcessValues;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 
 public class SimplifiedAcquisitionPredicate extends SearchPredicate {
 
     @Override
     public boolean evaluate(PaymentProcess process, SearchPaymentProcess searchBean) {
 	final AcquisitionRequest acquisitionRequest = process.getRequest();
 	final Person taker = searchBean.getTaker();
 	return matchesSearchCriteria(acquisitionRequest, searchBean)
		&& (acquisitionRequest.getProcess().isAccessibleToCurrentUser() || process.isTakenByCurrentUser() || (taker != null && process
 			.isTakenByPerson(taker.getUser())));
     }
 
     private boolean matchesSearchCriteria(final AcquisitionRequest acquisitionRequest, SearchPaymentProcess searchBean) {
 
 	final Person person = acquisitionRequest.getRequester();
 	AcquisitionProcess acquisitionProcess = acquisitionRequest.getAcquisitionProcess();
 
 	final List<Supplier> suppliers = acquisitionRequest.getSuppliers();
 	final String identification = acquisitionRequest.getAcquisitionProcessId();
 	final String acquisitionRequestDocumentID = acquisitionProcess.hasPurchaseOrderDocument() ? acquisitionProcess
 		.getAcquisitionRequestDocumentID() : null;
 	final AcquisitionProcessStateType type = acquisitionProcess.getAcquisitionProcessStateType();
 	final Set<AccountingUnit> accountingUnits = acquisitionRequest.getAccountingUnits();
 	User currentOwner = acquisitionProcess.getCurrentOwner();
 	final Person taker = currentOwner != null ? currentOwner.getExpenditurePerson() : null;
 	final Boolean showOnlyWithUnreadComments = searchBean.getShowOnlyWithUnreadComments();
 	final SimplifiedProcedureProcess process = (SimplifiedProcedureProcess) acquisitionRequest.getProcess();
 	final Boolean showPrioritiesOnly = searchBean.getShowPriorityOnly();
 	SearchProcessValues searchProcess = searchBean.getSearchProcess();
 	final ProcessClassification searchClassification = searchProcess != null ? searchProcess.getSearchClassification() : null;
 
 	Person loggedPerson = Person.getLoggedPerson();
 
 	return matchCriteria(searchBean.getProcessId(), identification)
 		&& matchCriteria(searchClassification, process.getProcessClassification())
 		&& matchCriteria(searchBean.getRequestingPerson(), person)
 		&& (matchCriteria(searchBean.getRequestingUnit(), acquisitionRequest.getRequestingUnit()))
 		&& (matchCriteria(searchBean.getPayingUnit(), acquisitionRequest.getFinancersSet()))
 		&& matchCriteria(searchBean.getSupplier(), suppliers)
 		&& matchAcquisitionProposalId(searchBean.getProposalId(), acquisitionRequest.getAcquisitionProcess())
 		&& matchCriteria(searchBean.getHasAvailableAndAccessibleActivityForUser(), acquisitionRequest)
 		&& matchCriteria(searchBean.getAcquisitionProcessStateType(), type)
 		&& matchCriteria(searchBean.getAccountingUnit(), accountingUnits)
 		&& matchCriteria(searchBean.getRequestDocumentId(), acquisitionRequestDocumentID)
 		&& (!showPrioritiesOnly || process.isPriorityProcess())
 		&& matchShowOnlyCriteris(acquisitionRequest, searchBean)
 		&& matchCriteria(searchBean.getTaker(), taker)
 		&& (!showOnlyWithUnreadComments || (!process.getUnreadCommentsForPerson(loggedPerson).isEmpty() && process
 			.hasActivitiesFromUser(loggedPerson)));
     }
 
     private boolean matchCriteria(ProcessClassification searchClassification, ProcessClassification processClassification) {
 	return searchClassification == null || searchClassification == processClassification;
     }
 
     private boolean matchAcquisitionProposalId(final String proposalId, final AcquisitionProcess acquisitionProcess) {
 	if (proposalId == null || proposalId.length() == 0) {
 	    return true;
 	}
 	for (final ProcessFile processFile : acquisitionProcess.getFilesSet()) {
 	    if (processFile instanceof AcquisitionProposalDocument) {
 		final AcquisitionProposalDocument acquisitionProposalDocument = (AcquisitionProposalDocument) processFile;
 		if (matchCriteria(proposalId, acquisitionProposalDocument.getProposalId())) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     protected boolean matchCriteria(AcquisitionProcessStateType acquisitionProcessStateType, AcquisitionProcessStateType type) {
 	return acquisitionProcessStateType == null || acquisitionProcessStateType.equals(type);
     }
 
     private boolean matchCriteria(final Boolean hasAvailableAndAccessibleActivityForUser,
 	    final AcquisitionRequest acquisitionRequest) {
 	return hasAvailableAndAccessibleActivityForUser == null
 		|| !hasAvailableAndAccessibleActivityForUser.booleanValue()
 		|| (acquisitionRequest.getProcess() instanceof RegularAcquisitionProcess && acquisitionRequest.getProcess()
 			.hasAnyAvailableActivity(true));
     }
 
     private boolean matchShowOnlyCriteris(final AcquisitionRequest acquisitionRequest, final SearchPaymentProcess searchBean) {
 	if (searchBean.getShowOnlyAcquisitionsExcludedFromSupplierLimit().booleanValue()
 		&& !acquisitionRequest.getProcess().getShouldSkipSupplierFundAllocation().booleanValue()) {
 	    return false;
 	}
 	if (searchBean.getShowOnlyAcquisitionsWithAdditionalCosts().booleanValue()) {
 	    return acquisitionRequest.hasAdditionalCosts();
 	}
 	return true;
     }
 }
