 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import myorg.domain.MyOrg;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Address;
 import myorg.domain.util.Money;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.SavedSearch;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AcquisitionAfterTheFact;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundableInvoiceFile;
 import pt.ist.expenditureTrackingSystem.domain.announcements.CCPAnnouncement;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateSupplierBean;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.plugins.luceneIndexing.IndexableField;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.IndexDocument;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Indexable;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Searchable;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 
 public class Supplier extends Supplier_Base implements Indexable, Searchable {
 
     public static enum SupplierIndexes implements IndexableField {
 	FISCAL_CODE("nif"), NAME("supplierName");
 
 	private String name;
 
 	private SupplierIndexes(String name) {
 	    this.name = name;
 	}
 
 	@Override
 	public String getFieldName() {
 	    return this.name;
 	}
 
     }
 
     private Supplier() {
 	super();
 	setMyOrg(MyOrg.getInstance());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     public Supplier(String fiscalCode) {
 	this();
 	if (fiscalCode == null || fiscalCode.length() == 0) {
 	    throw new DomainException("error.fiscal.code.cannot.be.empty");
 	}
 	setFiscalIdentificationCode(fiscalCode);
     }
 
     public Supplier(String name, String abbreviatedName, String fiscalCode, Address address, String phone, String fax,
 	    String email, String nib) {
 	this(fiscalCode);
 	setName(name);
 	setAbbreviatedName(abbreviatedName);
 	setAddress(address);
 	setPhone(phone);
 	setFax(fax);
 	setEmail(email);
 	setNib(nib);
     }
 
     @Override
     @Service
     public void delete() {
 	if (checkIfCanBeDeleted()) {
	    removeMyOrg();
 	    removeExpenditureTrackingSystem();
 	    super.delete();
 	}
     }
 
     @Override
     protected boolean checkIfCanBeDeleted() {
 	return !hasAnyAcquisitionRequests() && !hasAnyAcquisitionsAfterTheFact() && !hasAnyRefundInvoices()
 		&& !hasAnyAnnouncements() && !hasAnySupplierSearches() && !hasAnyPossibleAcquisitionRequests()
 		&& super.checkIfCanBeDeleted();
     }
 
     public static Supplier readSupplierByFiscalIdentificationCode(String fiscalIdentificationCode) {
 	for (Supplier supplier : MyOrg.getInstance().getSuppliersSet()) {
 	    if (supplier.getFiscalIdentificationCode().equals(fiscalIdentificationCode)) {
 		return supplier;
 	    }
 	}
 	return null;
     }
 
     public static Supplier readSupplierByName(final String name) {
 	for (Supplier supplier : MyOrg.getInstance().getSuppliersSet()) {
 	    if (supplier.getName().equalsIgnoreCase(name)) {
 		return supplier;
 	    }
 	}
 	return null;
     }
 
     @Deprecated
     public Money getTotalAllocated() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequest acquisitionRequest : getAcquisitionRequestsSet()) {
 	    if (acquisitionRequest.isInAllocationPeriod()) {
 		final AcquisitionProcess acquisitionProcess = acquisitionRequest.getAcquisitionProcess();
 		if (acquisitionProcess.isActive() && acquisitionProcess.isAllocatedToSupplier()) {
 		    result = result.add(acquisitionRequest.getValueAllocated());
 		}
 	    }
 	}
 	for (final AcquisitionAfterTheFact acquisitionAfterTheFact : getAcquisitionsAfterTheFactSet()) {
 	    if (acquisitionAfterTheFact.isInAllocationPeriod()) {
 		if (!acquisitionAfterTheFact.getDeletedState().booleanValue()) {
 		    result = result.add(acquisitionAfterTheFact.getValue());
 		}
 	    }
 	}
 	for (final RefundableInvoiceFile refundInvoice : getRefundInvoicesSet()) {
 	    if (refundInvoice.isInAllocationPeriod()) {
 		final RefundProcess refundProcess = refundInvoice.getRefundItem().getRequest().getProcess();
 		if (refundProcess.isActive() && !refundProcess.getShouldSkipSupplierFundAllocation()) {
 		    result = result.add(refundInvoice.getRefundableValue());
 		}
 	    }
 	}
 	return result;
     }
 
     @Deprecated
     public Money getSoftTotalAllocated() {
 	Money result = Money.ZERO;
 	result = result.add(getTotalAllocatedByAcquisitionProcesses(true));
 
 	for (final AcquisitionAfterTheFact acquisitionAfterTheFact : getAcquisitionsAfterTheFactSet()) {
 	    if (acquisitionAfterTheFact.isInAllocationPeriod()) {
 		if (!acquisitionAfterTheFact.getDeletedState().booleanValue()) {
 		    result = result.add(acquisitionAfterTheFact.getValue());
 		}
 	    }
 	}
 	for (final RefundableInvoiceFile refundInvoice : getRefundInvoicesSet()) {
 	    if (refundInvoice.isInAllocationPeriod()) {
 		final RefundProcess refundProcess = refundInvoice.getRefundItem().getRequest().getProcess();
 		if (refundProcess.isActive()) {
 		    result = result.add(refundInvoice.getRefundableValue());
 		}
 	    }
 	}
 	return result;
     }
 
     private Money getTotalAllocatedByAcquisitionProcesses(boolean allProcesses) {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequest acquisitionRequest : getAcquisitionRequestsSet()) {
 	    if ((allProcesses && !acquisitionRequest.getProcess().getShouldSkipSupplierFundAllocation())
 		    || acquisitionRequest.getAcquisitionProcess().isAllocatedToSupplier()) {
 		if (acquisitionRequest.isInAllocationPeriod()) {
 		    result = result.add(acquisitionRequest.getValueAllocated());
 		}
 	    }
 	}
 	return result;
     }
 
     public Money getTotalAllocatedByAcquisitionProcesses() {
 	return getTotalAllocatedByAcquisitionProcesses(false);
     }
 
     public Money getTotalAllocatedByAfterTheFactAcquisitions(final AfterTheFactAcquisitionType afterTheFactAcquisitionType) {
 	Money result = Money.ZERO;
 	for (final AcquisitionAfterTheFact acquisitionAfterTheFact : getAcquisitionsAfterTheFactSet()) {
 	    if (acquisitionAfterTheFact.isInAllocationPeriod()) {
 		if (!acquisitionAfterTheFact.getDeletedState().booleanValue()) {
 		    if (acquisitionAfterTheFact.getAfterTheFactAcquisitionType() == afterTheFactAcquisitionType) {
 			result = result.add(acquisitionAfterTheFact.getValue());
 		    }
 		}
 	    }
 	}
 	return result;
     }
 
     public Money getTotalAllocatedByPurchases() {
 	return getTotalAllocatedByAfterTheFactAcquisitions(AfterTheFactAcquisitionType.PURCHASE);
     }
 
     public Money getTotalAllocatedByWorkingCapitals() {
 	return getTotalAllocatedByAfterTheFactAcquisitions(AfterTheFactAcquisitionType.WORKING_CAPITAL);
     }
 
     public Money getTotalAllocatedByRefunds() {
 	return getTotalAllocatedByAfterTheFactAcquisitions(AfterTheFactAcquisitionType.REFUND);
     }
 
     @Service
     public static Supplier createNewSupplier(CreateSupplierBean createSupplierBean) {
 	return new Supplier(createSupplierBean.getName(), createSupplierBean.getAbbreviatedName(),
 		createSupplierBean.getFiscalIdentificationCode(), createSupplierBean.getAddress(), createSupplierBean.getPhone(),
 		createSupplierBean.getFax(), createSupplierBean.getEmail(), createSupplierBean.getNib());
     }
 
     @Override
     public boolean isFundAllocationAllowed(final Money value) {
 	final Money totalAllocated = getTotalAllocated();
 	final Money totalValue = totalAllocated; // .add(value);
 	return totalValue.isLessThan(SUPPLIER_LIMIT) && totalValue.isLessThan(getSupplierLimit());
     }
 
     @Service
     public void merge(final Supplier supplier) {
 	if (supplier != this) {
 	    final Set<AcquisitionAfterTheFact> acquisitionAfterTheFacts = supplier.getAcquisitionsAfterTheFactSet();
 	    getAcquisitionsAfterTheFactSet().addAll(acquisitionAfterTheFacts);
 	    acquisitionAfterTheFacts.clear();
 
 	    final Set<RefundableInvoiceFile> refundInvoices = supplier.getRefundInvoicesSet();
 	    getRefundInvoicesSet().addAll(refundInvoices);
 	    refundInvoices.clear();
 
 	    final Set<CCPAnnouncement> announcements = supplier.getAnnouncementsSet();
 	    getAnnouncementsSet().addAll(announcements);
 	    announcements.clear();
 
 	    final Set<SavedSearch> savedSearches = supplier.getSupplierSearchesSet();
 	    getSupplierSearchesSet().addAll(savedSearches);
 	    savedSearches.clear();
 
 	    final Set<AcquisitionRequest> acquisitionRequests = supplier.getAcquisitionRequestsSet();
 	    getAcquisitionRequestsSet().addAll(acquisitionRequests);
 	    acquisitionRequests.clear();
 
 	    final Set<AcquisitionRequest> possibleAcquisitionRequests = supplier.getPossibleAcquisitionRequestsSet();
 	    getPossibleAcquisitionRequestsSet().addAll(possibleAcquisitionRequests);
 	    possibleAcquisitionRequests.clear();
 
 	    supplier.delete();
 	}
     }
 
     @Override
     public IndexDocument getDocumentToIndex() {
 	IndexDocument indexDocument = new IndexDocument(this);
 	if (!StringUtils.isEmpty(getFiscalIdentificationCode())) {
 	    indexDocument.indexField(SupplierIndexes.FISCAL_CODE, getFiscalIdentificationCode());
 	}
 	indexDocument.indexField(SupplierIndexes.NAME, StringNormalizer.normalize(getName()));
 	return indexDocument;
     }
 
     @Override
     public Set<Indexable> getObjectsToIndex() {
 	Set<Indexable> set = new HashSet<Indexable>();
 	set.add(this);
 	return set;
     }
 
 }
