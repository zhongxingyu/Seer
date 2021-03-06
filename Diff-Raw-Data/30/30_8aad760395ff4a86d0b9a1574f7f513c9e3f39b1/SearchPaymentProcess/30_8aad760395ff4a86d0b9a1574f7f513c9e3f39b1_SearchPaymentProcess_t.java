 package pt.ist.expenditureTrackingSystem.domain.acquisitions.search;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import myorg.util.BundleUtil;
 
 import pt.ist.expenditureTrackingSystem.domain.SavedSearch;
 import pt.ist.expenditureTrackingSystem.domain.Search;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessStateType;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.DefaultPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.RefundProcessPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.SearchPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.SimplifiedAcquisitionPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
 
 public class SearchPaymentProcess extends Search<PaymentProcess> {
 
     public static enum SearchProcessValues implements IPresentableEnum {
 	RS5000(SimplifiedProcedureProcess.class, ProcessClassification.CCP), CT1000(SimplifiedProcedureProcess.class,
 		ProcessClassification.CT10000), CT75000(SimplifiedProcedureProcess.class, ProcessClassification.CT75000), ACQUISITIONS(
 		SimplifiedProcedureProcess.class, null), REFUND(RefundProcess.class, null);
 
 	private Class<? extends PaymentProcess> searchClass;
 	private ProcessClassification searchClassification;
 
 	private SearchProcessValues(Class<? extends PaymentProcess> searchClass, ProcessClassification searchClassification) {
 	    this.searchClass = searchClass;
 	    this.searchClassification = searchClassification;
 	}
 
 	@Override
 	public String getLocalizedName() {
 	    return (searchClassification != null) ? searchClassification.getLocalizedName() : BundleUtil
 		    .getStringFromResourceBundle("resources/ExpenditureResources", "label.search." + searchClass.getSimpleName()
 			    + ".description");
 	}
 
 	public Class<? extends PaymentProcess> getSearchClass() {
 	    return searchClass;
 	}
 
 	public ProcessClassification getSearchClassification() {
 	    return searchClassification;
 	}
 
     }
 
     private SavedSearch savedSearch;
     private SearchProcessValues searchProcess;
     private String processId;
     private String requestDocumentId;
     private Person requestingPerson;
     private Person taker;
     private Unit requestingUnit;
     private Unit payingUnit;
     private AcquisitionProcessStateType acquisitionProcessStateType;
     private RefundProcessStateType refundProcessStateType;
     private Supplier supplier;
     private String proposalId;
     private AccountingUnit accountingUnit;
     private Boolean hasAvailableAndAccessibleActivityForUser = Boolean.TRUE;
     private Boolean responsibleUnitSetOnly = Boolean.FALSE;
     private Boolean showOnlyAcquisitionsExcludedFromSupplierLimit = Boolean.FALSE;
     private Boolean showOnlyAcquisitionsWithAdditionalCosts = Boolean.FALSE;
     private Boolean showOnlyWithUnreadComments = Boolean.FALSE;
     private String refundeeName;
     private PaymentProcessYear year;
     private Boolean showPriorityOnly = Boolean.FALSE;
 
     private final static Map<Class<? extends PaymentProcess>, SearchPredicate> predicateMap = new HashMap<Class<? extends PaymentProcess>, SearchPredicate>();
     private final static SearchPredicate defaultPredicate = new DefaultPredicate();
 
     static {
 	predicateMap.put(SimplifiedProcedureProcess.class, new SimplifiedAcquisitionPredicate());
 	predicateMap.put(RefundProcess.class, new RefundProcessPredicate());
	predicateMap.put(AfterTheFactAcquisitionProcess.class, new AfterTheFactPredicate());
     }
 
     public SearchPaymentProcess() {
 	setRequestingPerson(null);
 	setRequestingUnit(null);
 	setPayingUnit(null);
 	setSupplier(null);
 	setAccountingUnit(null);
 	setSavedSearch(null);
 	setPaymentProcessYear(null);
 	setTaker(null);
     }
 
     public SearchPaymentProcess(SavedSearch savedSearch) {
 	setProcessId(savedSearch.getProcessId());
 	setRequestDocumentId(savedSearch.getRequestDocumentId());
 	setRequestingPerson(savedSearch.getRequestor());
 	setRequestingUnit(savedSearch.getUnit());
 	setPayingUnit(savedSearch.getPayingUnit());
 	setAcquisitionProcessStateType(savedSearch.getAcquisitionProcessStateType());
 	setRefundProcessStateType(savedSearch.getRefundProcessStateType());
 	setSupplier(savedSearch.getSupplier());
 	setProposalId(savedSearch.getProposalId());
 	setAccountingUnit(savedSearch.getAccountingUnit());
 	setHasAvailableAndAccessibleActivityForUser(savedSearch.getPendingOperations());
 	setResponsibleUnitSetOnly(savedSearch.getShowOnlyResponsabilities());
 	setShowOnlyAcquisitionsExcludedFromSupplierLimit(savedSearch.getShowOnlyAcquisitionsExcludedFromSupplierLimit());
 	setShowOnlyAcquisitionsWithAdditionalCosts(savedSearch.getShowOnlyAcquisitionsWithAdditionalCosts());
 	setSavedSearch(savedSearch);
 	setPaymentProcessYear(savedSearch.getYear());
 	setTaker(savedSearch.getTakenBy());
 	setShowOnlyWithUnreadComments(savedSearch.getShowOnlyWithUnreadComments());
 	setShowPriorityOnly(savedSearch.getShowPriorityOnly());
 	setSearchProcess(savedSearch.getSearchProcessValues());
     }
 
     @Override
     public Set<PaymentProcess> search() {
 	try {
 	    return new SearchResult(getProcesses());
 	} catch (Exception ex) {
 	    ex.printStackTrace();
 	    throw new Error(ex);
 	}
 
     }
 
     public boolean isSearchObjectAvailable() {
 	return getSavedSearch() != null;
     }
 
     private class SearchResult extends SearchResultSet<PaymentProcess> {
 
 	public SearchResult(Collection<? extends PaymentProcess> c) {
 	    super(c);
 	}
 
 	@Override
 	protected boolean matchesSearchCriteria(PaymentProcess process) {
 	    Class clazz = getSearchClass();
 
 	    return (clazz == null || process.getClass().equals(getSearchClass()))
 		    && getPredicateFor(process.getClass()).evaluate(process, SearchPaymentProcess.this);
 	}
 
     }
 
     private SearchPredicate getPredicateFor(Class<? extends PaymentProcess> clazz) {
 	SearchPredicate predicate = predicateMap.get(clazz);
 	return predicate == null ? defaultPredicate : predicate;
     }
 
     private Class<? extends PaymentProcess> getProcessClass() {
 	Class<? extends PaymentProcess> clazz = getSearchClass();
 	return clazz == null ? PaymentProcess.class : clazz;
     }
 
     private Set<? extends PaymentProcess> getProcesses() {
 	return (responsibleUnitSetOnly ? GenericProcess.getProcessesWithResponsible(getSearchClass(), Person.getLoggedPerson(),
 		getPaymentProcessYear()) : GenericProcess.getAllProcesses(getProcessClass(), getPaymentProcessYear()));
     }
 
     @Override
     protected void persist(String name) {
 	new SavedSearch(name, Person.getLoggedPerson(), this);
     }
 
     public SearchProcessValues getSearchProcess() {
 	return searchProcess;
     }
 
     public void setSearchProcess(SearchProcessValues searchProcess) {
 	this.searchProcess = searchProcess;
     }
 
     public String getProcessId() {
 	return processId;
     }
 
     public void setProcessId(String processId) {
 	this.processId = processId != null ? processId.trim() : processId;
     }
 
     public String getRequestDocumentId() {
 	return requestDocumentId;
     }
 
     public void setRequestDocumentId(String requestDocumentId) {
 	this.requestDocumentId = requestDocumentId;
     }
 
     public Person getRequestingPerson() {
 	return requestingPerson;
     }
 
     public void setRequestingPerson(Person requestingPerson) {
 	this.requestingPerson = requestingPerson;
     }
 
     public Unit getRequestingUnit() {
 	return requestingUnit;
     }
 
     public void setRequestingUnit(Unit requestingUnit) {
 	this.requestingUnit = requestingUnit;
     }
 
     public Unit getPayingUnit() {
 	return payingUnit;
     }
 
     public void setPayingUnit(final Unit payingUnit) {
 	this.payingUnit = payingUnit;
     }
 
     public AcquisitionProcessStateType getAcquisitionProcessStateType() {
 	return acquisitionProcessStateType;
     }
 
     public void setAcquisitionProcessStateType(AcquisitionProcessStateType acquisitionProcessStateType) {
 	this.acquisitionProcessStateType = acquisitionProcessStateType;
     }
 
     public RefundProcessStateType getRefundProcessStateType() {
 	return refundProcessStateType;
     }
 
     public void setRefundProcessStateType(RefundProcessStateType refundProcessStateType) {
 	this.refundProcessStateType = refundProcessStateType;
     }
 
     public Supplier getSupplier() {
 	return supplier;
     }
 
     public void setSupplier(Supplier supplier) {
 	this.supplier = supplier;
     }
 
     public String getProposalId() {
 	return proposalId;
     }
 
     public void setProposalId(String proposalId) {
 	this.proposalId = proposalId;
     }
 
     public AccountingUnit getAccountingUnit() {
 	return accountingUnit;
     }
 
     public void setAccountingUnit(AccountingUnit accountingUnit) {
 	this.accountingUnit = accountingUnit;
     }
 
     public Boolean getHasAvailableAndAccessibleActivityForUser() {
 	return hasAvailableAndAccessibleActivityForUser;
     }
 
     public void setHasAvailableAndAccessibleActivityForUser(Boolean hasAvailableAndAccessibleActivityForUser) {
 	this.hasAvailableAndAccessibleActivityForUser = hasAvailableAndAccessibleActivityForUser;
     }
 
     public Boolean getResponsibleUnitSetOnly() {
 	return responsibleUnitSetOnly;
     }
 
     public void setResponsibleUnitSetOnly(Boolean responsibleUnitSetOnly) {
 	this.responsibleUnitSetOnly = responsibleUnitSetOnly;
     }
 
     public Boolean getShowOnlyAcquisitionsExcludedFromSupplierLimit() {
 	return showOnlyAcquisitionsExcludedFromSupplierLimit;
     }
 
     public void setShowOnlyAcquisitionsExcludedFromSupplierLimit(Boolean showOnlyAcquisitionsExcludedFromSupplierLimit) {
 	this.showOnlyAcquisitionsExcludedFromSupplierLimit = showOnlyAcquisitionsExcludedFromSupplierLimit;
     }
 
     public Boolean getShowOnlyAcquisitionsWithAdditionalCosts() {
 	return showOnlyAcquisitionsWithAdditionalCosts;
     }
 
     public void setShowOnlyAcquisitionsWithAdditionalCosts(Boolean showOnlyAcquisitionsWithAdditionalCosts) {
 	this.showOnlyAcquisitionsWithAdditionalCosts = showOnlyAcquisitionsWithAdditionalCosts;
     }
 
     public String getRefundeeName() {
 	return refundeeName;
     }
 
     public void setRefundeeName(String refundeeName) {
 	this.refundeeName = refundeeName;
     }
 
     public Class getSearchClass() {
 	SearchProcessValues searchProcess = getSearchProcess();
 	return searchProcess != null ? searchProcess.getSearchClass() : null;
     }
 
     public SavedSearch getSavedSearch() {
 	return savedSearch;
     }
 
     public void setSavedSearch(SavedSearch savedSearch) {
 	this.savedSearch = savedSearch;
     }
 
     public void setPaymentProcessYear(PaymentProcessYear year) {
 	this.year = year;
     }
 
     public PaymentProcessYear getPaymentProcessYear() {
 	return this.year;
     }
 
     public void setTaker(Person taker) {
 	this.taker = taker;
     }
 
     public Person getTaker() {
 	return taker;
     }
 
     public void setShowOnlyWithUnreadComments(Boolean showOnlyWithUnreadComments) {
 	this.showOnlyWithUnreadComments = showOnlyWithUnreadComments;
     }
 
     public Boolean getShowOnlyWithUnreadComments() {
 	return showOnlyWithUnreadComments;
     }
 
     public void setShowPriorityOnly(Boolean showPriorityOnly) {
 	this.showPriorityOnly = showPriorityOnly != null ? showPriorityOnly : Boolean.FALSE;
     }
 
     public Boolean getShowPriorityOnly() {
 	return showPriorityOnly;
     }
 
 }
