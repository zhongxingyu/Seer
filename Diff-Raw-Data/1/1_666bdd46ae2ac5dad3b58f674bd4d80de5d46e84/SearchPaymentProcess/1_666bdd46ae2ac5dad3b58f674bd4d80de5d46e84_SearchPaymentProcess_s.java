 package pt.ist.expenditureTrackingSystem.domain.acquisitions.search;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import pt.ist.expenditureTrackingSystem.domain.SavedSearch;
 import pt.ist.expenditureTrackingSystem.domain.Search;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProcessesThatAreAuthorizedByUserPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.DefaultPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.RefundProcessPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.SearchPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.predicates.SimplifiedAcquisitionPredicate;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.fenixWebFramework.util.DomainReference;
 
 public class SearchPaymentProcess extends Search<PaymentProcess> {
 
     private DomainReference<SavedSearch> savedSearch;
     private Class<? extends PaymentProcess> searchClass;
     private String processId;
     private String requestDocumentId;
     private DomainReference<Person> requestingPerson;
     private DomainReference<Unit> requestingUnit;
     private AcquisitionProcessStateType acquisitionProcessStateType;
     private RefundProcessStateType refundProcessStateType;
     private DomainReference<Supplier> supplier;
     private String proposalId;
     private DomainReference<AccountingUnit> accountingUnit;
     private Boolean hasAvailableAndAccessibleActivityForUser = Boolean.TRUE;
     private Boolean responsibleUnitSetOnly = Boolean.FALSE;
     private Boolean showOnlyAcquisitionsExcludedFromSupplierLimit = Boolean.FALSE;
     private Boolean showOnlyAcquisitionsWithAdditionalCosts = Boolean.FALSE;
     private String refundeeName;
     private DomainReference<PaymentProcessYear> year;
 
     private final static Map<Class<? extends PaymentProcess>, SearchPredicate> predicateMap = new HashMap<Class<? extends PaymentProcess>, SearchPredicate>();
     private final static SearchPredicate defaultPredicate = new DefaultPredicate();
 
     static {
 	predicateMap.put(SimplifiedProcedureProcess.class, new SimplifiedAcquisitionPredicate());
 	predicateMap.put(RefundProcess.class, new RefundProcessPredicate());
     }
 
     public SearchPaymentProcess() {
 	setRequestingPerson(null);
 	setRequestingUnit(null);
 	setSupplier(null);
 	setAccountingUnit(null);
 	setSavedSearch(null);
     }
 
     public SearchPaymentProcess(SavedSearch savedSearch) {
 	setSearchClass(savedSearch.getSearchClass());
 	setProcessId(savedSearch.getProcessId());
 	setRequestDocumentId(savedSearch.getRequestDocumentId());
 	setRequestingPerson(savedSearch.getRequestor());
 	setRequestingUnit(savedSearch.getUnit());
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
 	return responsibleUnitSetOnly ? getProcessesWithResponsible(Person.getLoggedPerson()) : GenericProcess.getAllProcesses(
 		getProcessClass(), getPaymentProcessYear());
     }
 
     private Set<? extends PaymentProcess> getProcessesWithResponsible(final Person person) {
 	if (person == null) {
 	    return Collections.emptySet();
 	}
 
 	return GenericProcess.getAllProcess(getProcessClass(), new ProcessesThatAreAuthorizedByUserPredicate(person),
 		getPaymentProcessYear());
     }
 
     @Override
     protected void persist(String name) {
 	new SavedSearch(name, Person.getLoggedPerson(), this);
     }
 
     public String getProcessId() {
 	return processId;
     }
 
     public void setProcessId(String processId) {
 	this.processId = processId;
     }
 
     public String getRequestDocumentId() {
 	return requestDocumentId;
     }
 
     public void setRequestDocumentId(String requestDocumentId) {
 	this.requestDocumentId = requestDocumentId;
     }
 
     public Person getRequestingPerson() {
 	return requestingPerson.getObject();
     }
 
     public void setRequestingPerson(Person requestingPerson) {
 	this.requestingPerson = new DomainReference<Person>(requestingPerson);
     }
 
     public Unit getRequestingUnit() {
 	return requestingUnit.getObject();
     }
 
     public void setRequestingUnit(Unit requestingUnit) {
 	this.requestingUnit = new DomainReference<Unit>(requestingUnit);
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
 	return supplier.getObject();
     }
 
     public void setSupplier(Supplier supplier) {
 	this.supplier = new DomainReference<Supplier>(supplier);
     }
 
     public String getProposalId() {
 	return proposalId;
     }
 
     public void setProposalId(String proposalId) {
 	this.proposalId = proposalId;
     }
 
     public AccountingUnit getAccountingUnit() {
 	return accountingUnit.getObject();
     }
 
     public void setAccountingUnit(AccountingUnit accountingUnit) {
 	this.accountingUnit = new DomainReference<AccountingUnit>(accountingUnit);
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
 	return searchClass;
     }
 
     public void setSearchClass(Class searchClass) {
 	this.searchClass = searchClass;
     }
 
     public SavedSearch getSavedSearch() {
 	return savedSearch.getObject();
     }
 
     public void setSavedSearch(SavedSearch savedSearch) {
 	this.savedSearch = new DomainReference<SavedSearch>(savedSearch);
     }
 
     public void setPaymentProcessYear(PaymentProcessYear year) {
 	this.year = new DomainReference<PaymentProcessYear>(year);
     }
 
     public PaymentProcessYear getPaymentProcessYear() {
 	return this.year.getObject();
     }
 }
