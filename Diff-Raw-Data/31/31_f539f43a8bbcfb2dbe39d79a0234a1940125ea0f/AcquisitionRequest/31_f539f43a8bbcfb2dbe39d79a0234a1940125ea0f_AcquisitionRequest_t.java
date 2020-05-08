 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.PayingUnitTotalBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.util.ByteArray;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 
 public class AcquisitionRequest extends AcquisitionRequest_Base {
 
     public AcquisitionRequest(final AcquisitionProcess acquisitionProcess, final Person person) {
 	super();
 	checkParameters(acquisitionProcess, person);
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	setAcquisitionProcess(acquisitionProcess);
 	setRequester(person);
     }
 
     private void checkParameters(AcquisitionProcess acquisitionProcess, Person person) {
 	if (acquisitionProcess == null) {
 	    throw new DomainException("error.acquisition.request.wrong.acquisition.process");
 	}
 	if (person == null) {
 	    throw new DomainException("acquisitionProcess.message.exception.anonymousNotAllowedToCreate");
 	}
     }
 
     public AcquisitionRequest(AcquisitionProcess acquisitionProcess, List<Supplier> suppliers, Person person) {
 	this(acquisitionProcess, person);
 	for (Supplier supplier : suppliers) {
 	    addSuppliers(supplier);
 	}
     }
 
     public AcquisitionRequest(AcquisitionProcess acquisitionProcess, Supplier supplier, Person person) {
 	this(acquisitionProcess, person);
 	addSuppliers(supplier);
     }
 
     public void edit(Supplier supplier, Unit requestingUnit, Boolean isRequestingUnitPayingUnit) {
 	addSuppliers(supplier);
 	setRequestingUnit(requestingUnit);
 	if (isRequestingUnitPayingUnit) {
 	    addFinancers(requestingUnit.finance(this));
 	}
     }
 
     public void addAcquisitionProposalDocument(final String filename, final byte[] bytes, String proposalId) {
 	AcquisitionProposalDocument acquisitionProposalDocument = getAcquisitionProposalDocument();
 	if (acquisitionProposalDocument == null) {
 	    acquisitionProposalDocument = new AcquisitionProposalDocument();
 	    setAcquisitionProposalDocument(acquisitionProposalDocument);
 	}
 	acquisitionProposalDocument.setFilename(filename);
 	acquisitionProposalDocument.setContent(new ByteArray(bytes));
 	acquisitionProposalDocument.setProposalId(proposalId != null && !proposalId.isEmpty() ? proposalId : null);
     }
 
     public AcquisitionRequestItem createAcquisitionRequestItem(AcquisitionRequestItemBean requestItemBean) {
 
 	return new AcquisitionRequestItem(requestItemBean);
     }
 
     @Override
     public void delete() {
 	for (AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    acquisitionRequestItem.delete();
 	}
 	for (; !getFinancers().isEmpty(); getFinancers().get(0).delete())
 	    ;
 
 	removeAcquisitionProposalDocument();
 	removeRequester();
 	for (; !getSuppliers().isEmpty(); removeSuppliers(getSuppliers().get(0)))
 	    ;
 	removeAcquisitionProcess();
 	super.delete();
     }
 
     public String getFiscalIdentificationCode() {
 	if (getSuppliersCount() == 0) {
 	    return null;
 	}
 	StringBuilder builder = new StringBuilder("");
 	Iterator<Supplier> suppliersIterator = getSuppliersIterator();
 	while (suppliersIterator.hasNext()) {
 	    builder.append(suppliersIterator.next().getFiscalIdentificationCode());
 	    if (suppliersIterator.hasNext()) {
 		builder.append(", ");
 	    }
 	}
 	return builder.toString();
     }
 
     public Money getTotalVatValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalVatValue());
 	}
 	return result;
     }
 
     public Money getRealTotalVatValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalRealVatValue());
 	}
 	return result;
     }
 
     public Money getTotalAdditionalCostsValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    if (acquisitionRequestItem.getAdditionalCostValue() != null) {
 		result = result.add(acquisitionRequestItem.getAdditionalCostValue());
 	    }
 	}
 	return result;
 
     }
 
     public Money getRealTotalAdditionalCostsValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    if (acquisitionRequestItem.getRealAdditionalCostValue() != null) {
 		result = result.add(acquisitionRequestItem.getRealAdditionalCostValue());
 	    }
 	}
 	return result;
     }
 
     public Money getTotalItemValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalItemValue());
 	}
 	return result;
     }
 
     public Money getTotalItemValueWithVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalItemValueWithVat());
 	}
 	return result;
     }
 
     public Money getTotalItemValueWithAdditionalCosts() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalItemValueWithAdditionalCosts());
 	}
 	return result;
     }
 
     public Money getTotalItemValueWithAdditionalCostsAndVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.addAndRound(acquisitionRequestItem.getTotalItemValueWithAdditionalCostsAndVat());
 	}
 	return result;
     }
 
     public Money getRealTotalValue() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalRealValue());
 	}
 	return result;
     }
 
     public Money getRealTotalValueWithVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalRealValueWithVat());
 	}
 	return result;
     }
 
     public Money getRealTotalValueWithAdditionalCosts() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.add(acquisitionRequestItem.getTotalRealValueWithAdditionalCosts());
 	}
 	return result;
     }
 
     public Money getRealTotalValueWithAdditionalCostsAndVat() {
 	Money result = Money.ZERO;
 	for (final AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    result = result.addAndRound(acquisitionRequestItem.getTotalRealValueWithAdditionalCostsAndVat());
 	}
 	return result;
     }
 
     @Override
     public void receiveInvoice(final String filename, final byte[] bytes, final String invoiceNumber, final LocalDate invoiceDate) {
 	super.receiveInvoice(filename, bytes, invoiceNumber, invoiceDate);
 	if (!isRealCostAvailableForAtLeastOneItem()) {
 	    copyEstimateValuesToRealValues();
 	}
     }
 
     public boolean isRealCostAvailableForAtLeastOneItem() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (item.getRealQuantity() != null && item.getRealUnitValue() != null) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private void copyEstimateValuesToRealValues() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    item.setRealQuantity(item.getQuantity());
 	    item.setRealUnitValue(item.getUnitValue());
 	    item.setRealVatValue(item.getVatValue());
 	    item.setRealAdditionalCostValue(item.getAdditionalCostValue());
 
 	    for (UnitItem unitItem : item.getUnitItems()) {
 		unitItem.setRealShareValue(unitItem.getShareValue());
 	    }
 	}
 
     }
 
     public boolean isFilled() {
 	return hasAcquisitionProposalDocument() && getAcquisitionRequestItemsCount() > 0;
     }
 
     public boolean isEveryItemFullyAttributedToPayingUnits() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (!item.isValueFullyAttributedToUnits()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean isEveryItemFullyAttributeInRealValues() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (!item.isRealValueFullyAttributedToUnits()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public void approvedBy(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    item.approvedBy(person);
 	}
     }
 
     public void unapproveBy(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    item.unapprovedBy(person);
 	}
     }
 
     public boolean isApprovedByAtLeastOneResponsible() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (item.hasAtLeastOneResponsibleApproval()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isApprovedByAllResponsibles() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (!item.isApproved()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean hasBeenApprovedBy(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (item.hasBeenApprovedBy(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasAtLeastOneConfirmation() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (item.hasAtLeastOneInvoiceConfirmation()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isInvoiceConfirmedBy(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (item.isInvoiceConfirmedBy(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public void confirmInvoiceFor(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    item.confirmInvoiceBy(person);
 	}
     }
 
     public void unconfirmInvoiceFor(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    item.unconfirmInvoiceBy(person);
 	}
     }
 
     public boolean isInvoiceConfirmedBy() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (!item.isInvoiceConfirmed()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public Money getValueAllocated() {
 	if (getAcquisitionProcess().isActive()) {
 	    return (getAcquisitionProcess().getAcquisitionProcessState().hasBeenAllocatedPermanently()) ? getRealTotalValue()
 		    : getTotalItemValue();
 	}
 	return Money.ZERO;
     }
 
     public boolean isValueAllowed(Money value) {
 	Money totalItemValue = getTotalItemValue();
 	Money totalValue = totalItemValue.add(value);
 	return totalValue.isLessThanOrEqual(getAcquisitionProcess().getAcquisitionRequestValueLimit());
     }
 
     public List<PayingUnitTotalBean> getTotalAmountsForEachPayingUnit() {
 	List<PayingUnitTotalBean> beans = new ArrayList<PayingUnitTotalBean>();
 	for (Financer financer : getFinancers()) {
 	    beans.add(new PayingUnitTotalBean(financer));
 	}
 	return beans;
     }
 
     public boolean hasPayingUnit(Unit unit) {
 	Financer financer = getFinancer(unit);
 	return financer != null;
     }
 
     public void removePayingUnit(Unit unit) {
 	Financer financer = getFinancer(unit);
 	if (financer != null) {
 	    financer.delete();
 	}
     }
 
     public Financer addPayingUnit(final Unit unit) {
 	Financer financer = getFinancer(unit);
 	return financer != null ? financer : unit.finance(this);
     }
 
     public Financer getFinancer(Unit unit) {
 	for (Financer financer : getFinancers()) {
 	    if (financer.getUnit() == unit) {
 		return financer;
 	    }
 	}
 	return null;
     }
 
     public Set<Unit> getPayingUnits() {
 	Set<Unit> res = new HashSet<Unit>();
 	for (Financer financer : getFinancers()) {
 	    res.add(financer.getUnit());
 	}
 	return res;
     }
 
     public Money getAmountAllocatedToUnit(Unit unit) {
 	Financer financer = getFinancer(unit);
 	return financer == null ? Money.ZERO : financer.getAmountAllocated();
     }
 
     public Set<Financer> getFinancersWithFundsAllocated() {
 	Set<Financer> res = new HashSet<Financer>();
 	for (Financer financer : getFinancers()) {
 	    if (financer.getAmountAllocated().isPositive()) {
 		res.add(financer);
 	    }
 	}
 	return res;
     }
 
     public Set<Financer> getFinancersWithFundsAllocated(Person person) {
 	Set<Financer> res = new HashSet<Financer>();
 	for (Financer financer : getFinancers()) {
 	    if (financer.isAccountingEmployee(person) && financer.getAmountAllocated().isPositive()) {
 		res.add(financer);
 	    }
 	}
 	return res;
     }
 
     public Set<ProjectFinancer> getProjectFinancersWithFundsAllocated() {
 	Set<ProjectFinancer> res = new HashSet<ProjectFinancer>();
 	for (Financer financer : getFinancers()) {
 	    if (financer instanceof ProjectFinancer && financer.getAmountAllocated().isPositive()) {
 		res.add((ProjectFinancer) financer);
 	    }
 	}
 	return res;
     }
 
     public Set<ProjectFinancer> getProjectFinancersWithFundsAllocated(Person person) {
 	Set<ProjectFinancer> res = new HashSet<ProjectFinancer>();
 	for (Financer financer : getFinancers()) {
 	    if (financer instanceof ProjectFinancer && financer.isProjectAccountingEmployee(person)
 		    && financer.getAmountAllocated().isPositive()) {
 		res.add((ProjectFinancer) financer);
 	    }
 	}
 	return res;
     }
 
     public void resetFundAllocationId() {
 	for (Financer financer : getFinancersSet()) {
 	    financer.setFundAllocationId(null);
 	}
 
     }
 
     public void resetFundAllocationId(final Person person) {
 	for (Financer financer : getFinancersSet()) {
 	    if (financer.isAccountingEmployee(person)) {
 		financer.setFundAllocationId(null);
 	    }
 	}
     }
 
     public void resetProjectFundAllocationId(final Person person) {
 	for (Financer financer : getFinancersSet()) {
 	    if (financer.isProjectFinancer() && financer.isProjectAccountingEmployee(person)) {
 		ProjectFinancer projectFinancer = (ProjectFinancer) financer;
 		projectFinancer.setProjectFundAllocationId(null);
 	    }
 	}
     }
 
     public void resetProjectFundAllocationId() {
 	for (Financer financer : getFinancersSet()) {
 	    if (financer.isProjectFinancer()) {
 		ProjectFinancer projectFinancer = (ProjectFinancer) financer;
 		projectFinancer.setProjectFundAllocationId(null);
 	    }
 	}
     }
 
     public void resetEffectiveFundAllocationId() {
 	for (Financer financer : getFinancersSet()) {
 	    financer.setEffectiveFundAllocationId(null);
 	}
 
     }
 
     public boolean hasAllFundAllocationId() {
 	for (Financer financer : getFinancersWithFundsAllocated()) {
 	    if (financer.getFundAllocationId() == null) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean hasAllFundAllocationId(Person person) {
 	for (Financer financer : getFinancersWithFundsAllocated()) {
 	    if (financer.isAccountingEmployee(person) && financer.getFundAllocationId() == null) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean hasAnyFundAllocationId() {
 	for (Financer financer : getFinancersWithFundsAllocated()) {
 	    if (financer.hasFundAllocationId()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasAnyFundAllocationId(Person person) {
 	for (Financer financer : getFinancersWithFundsAllocated()) {
	    if (financer.getFundAllocationId() != null && financer.isAccountingEmployee(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public void submittedForFundsAllocation(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    item.submittedForFundsAllocation(person);
 	}
     }
 
     public boolean isSubmittedForFundsAllocationByAllResponsibles() {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (!item.isSubmittedForFundsAllocation()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean hasBeenSubmittedForFundsAllocationBy(Person person) {
 	for (AcquisitionRequestItem item : getAcquisitionRequestItems()) {
 	    if (item.hasBeenSubmittedForFundsAllocationBy(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public void unSubmitForFundsAllocation() {
 	for (AcquisitionRequestItem acquisitionRequestItem : getAcquisitionRequestItemsSet()) {
 	    acquisitionRequestItem.unSubmitForFundsAllocation();
 	}
 
     }
 
     public boolean checkRealValues() {
 	return isRealTotalValueEqualsRealShareValue() && isRealUnitShareValueLessThanUnitShareValue()
 		&& isRealValueLessThanTotalValue();
     }
 
     public Money getTotalRealShareValue() {
 	Money res = Money.ZERO;
 	for (Financer financer : getFinancersSet()) {
 	    res = res.add(financer.getRealShareValue());
 	}
 	return res;
     }
 
     public boolean isRealValueLessThanTotalValue() {
 	return getRealTotalValue().isLessThanOrEqual(getTotalItemValue());
     }
 
     public boolean isRealUnitShareValueLessThanUnitShareValue() {
 	for (Financer financer : getFinancersSet()) {
 	    if (!financer.isRealUnitShareValueLessThanUnitShareValue()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean isRealTotalValueEqualsRealShareValue() {
 	return getRealTotalValueWithAdditionalCostsAndVat().equals(getTotalRealShareValue());
     }
 
     public String getAcquisitionProcessId() {
 	return getAcquisitionProcess().getAcquisitionProcessId();
     }
 
     public boolean isAccountingEmployee(final Person person) {
 	for (final Financer financer : getFinancersSet()) {
 	    if (financer.isAccountingEmployee(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isAccountingEmployeeForOnePossibleUnit(final Person person) {
 	for (final Financer financer : getFinancersSet()) {
 	    if (!financer.isProjectFinancer()) {
 		if (financer.isAccountingEmployeeForOnePossibleUnit(person)) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     public boolean isProjectAccountingEmployee(final Person person) {
 	for (final Financer financer : getFinancersSet()) {
 	    if (financer.isProjectAccountingEmployee(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasAllocatedFundsForAllProjectFinancers() {
 	for (final Financer financer : getFinancersSet()) {
 	    if (!financer.hasAllocatedFundsForAllProject()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean hasAllocatedFundsForAllProjectFinancers(Person person) {
 	for (final Financer financer : getFinancersSet()) {
 	    if (financer.isProjectAccountingEmployee(person) && !financer.hasAllocatedFundsForAllProject()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean hasAllocatedFundsPermanentlyForAllProjectFinancers() {
 	for (final Financer financer : getFinancersSet()) {
 	    if (!financer.hasAllocatedFundsPermanentlyForAllProjectFinancers()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public String getAcquisitionProposalDocumentId() {
 	if (hasAcquisitionProposalDocument()) {
 	    return getAcquisitionProposalDocument().getProposalId();
 	}
 	return null;
     }
 
     public boolean isFundAllocationAllowed(Money totalValue) {
 	for (Supplier supplier : getSuppliers()) {
 	    if (!supplier.isFundAllocationAllowed(totalValue)) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public Supplier getSupplier() {
 	if (getSuppliersCount() > 1) {
 	    throw new DomainException("This method should be used only when 1 supplier is present");
 	}
 
 	return getSuppliersCount() == 0 ? null : getSuppliers().get(0);
     }
 
     public String getAcquisitionRequestDocumentID() {
 	return hasPurchaseOrderDocument() ? getPurchaseOrderDocument().getRequestId() : ExpenditureTrackingSystem.getInstance()
 		.nextAcquisitionRequestDocumentID();
     }
 
     public boolean hasAnyAccountingUnitFinancerWithNoFundsAllocated(final Person person) {
 	for (Financer financer : getFinancersSet()) {
 	    if (financer.isAccountingEmployeeForOnePossibleUnit(person) && financer.getAmountAllocated().isPositive()
 		    && financer.getFundAllocationId() == null) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public Set<Financer> getAccountingUnitFinancerWithNoFundsAllocated(final Person person) {
 	Set<Financer> res = new HashSet<Financer>();
 	for (Financer financer : getFinancersSet()) {
 	    if (financer.isAccountingEmployeeForOnePossibleUnit(person) && financer.getAmountAllocated().isPositive()
 		    && financer.getFundAllocationId() == null) {
 		res.add(financer);
 	    }
 	}
 	return res;
     }
 
     public String getRefundeeName() {
 	final Person refundee = getRefundee();
 	return refundee == null ? null : refundee.getName();
     }
 
     public boolean hasAnyAllocatedFunds() {
 	for (Financer financer : getFinancers()) {
 	    if (financer.hasAnyFundsAllocated()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public Set<AccountingUnit> getAccountingUnits() {
 	Set<AccountingUnit> units = new HashSet<AccountingUnit>();
 	for (Financer financer : getFinancers()) {
 	    units.add(financer.getAccountingUnit());
 	}
 	return units;
     }
 
 }
