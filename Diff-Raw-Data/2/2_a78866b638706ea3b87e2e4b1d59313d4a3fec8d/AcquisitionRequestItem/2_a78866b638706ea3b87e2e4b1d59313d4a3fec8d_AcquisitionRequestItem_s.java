 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Address;
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public class AcquisitionRequestItem extends AcquisitionRequestItem_Base {
 
     public static final Comparator<AcquisitionRequestItem> COMPARATOR_BY_REFERENCE = new Comparator<AcquisitionRequestItem>() {
 
 	@Override
 	public int compare(final AcquisitionRequestItem acquisitionRequestItem1,
 		final AcquisitionRequestItem acquisitionRequestItem2) {
 	    final int c = acquisitionRequestItem1.getProposalReference()
 		    .compareTo(acquisitionRequestItem2.getProposalReference());
 	    return c == 0 ? acquisitionRequestItem1.getExternalId().compareTo(acquisitionRequestItem2.getExternalId()) : c;
 	}
 
     };
 
     protected AcquisitionRequestItem() {
 	super();
     }
 
     private AcquisitionRequestItem(final AcquisitionRequest acquisitionRequest, final String description, final Integer quantity,
 	    final Money unitValue, final BigDecimal vatValue, final String proposalReference, CPVReference reference) {
 
 	this();
 	checkLimits(acquisitionRequest, quantity, unitValue);
 
 	setRequest(acquisitionRequest);
 	setDescription(description);
 	setQuantity(quantity);
 	setUnitValue(unitValue);
 	setVatValue(vatValue);
 	setProposalReference(proposalReference);
 	setCPVReference(reference);
     }
 
     public AcquisitionRequestItem(final AcquisitionRequest acquisitionRequest, final String description, final Integer quantity,
 	    final Money unitValue, final BigDecimal vatValue, final Money additionalCostValue, final String proposalReference,
 	    CPVReference reference, String recipient, Address address, String phone, String email) {
 	this(acquisitionRequest, description, quantity, unitValue, vatValue, proposalReference, reference);
 	setAdditionalCostValue(additionalCostValue);
 	setRecipient(recipient);
 	setAddress(address);
 	setRecipientEmail(email);
 	setRecipientPhone(phone);
 
 	createUnitItem();
     }
 
     private void checkLimits(AcquisitionRequest acquisitionRequest, Integer quantity, Money unitValue) {
 	Money totalValue = unitValue.multiply(quantity.longValue());
 
 	if (getUnitValue() != null && getQuantity() != null) {
	    Money currentValue = getUnitValue().multiply(quantity.longValue());
 	    totalValue = totalValue.subtract(currentValue);
 	}
 
 	if (!checkAcquisitionRequestValueLimit(acquisitionRequest, totalValue)) {
 	    throw new DomainException("acquisitionRequestItem.message.exception.totalValueExceed", DomainException
 		    .getResourceFor("resources/AcquisitionResources"), acquisitionRequest.getAcquisitionProcess()
 		    .getAcquisitionRequestValueLimit().toFormatString());
 	}
 
 	if (!acquisitionRequest.getAcquisitionProcess().getShouldSkipSupplierFundAllocation()
 		&& !checkSupplierFundAllocation(acquisitionRequest, totalValue)) {
 	    throw new DomainException("acquisitionRequestItem.message.exception.fundAllocationNotAllowed", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
     }
 
     private boolean checkAcquisitionRequestValueLimit(AcquisitionRequest acquisitionRequest, Money totalValue) {
 	return acquisitionRequest.isValueAllowed(totalValue);
     }
 
     private boolean checkSupplierFundAllocation(AcquisitionRequest acquisitionRequest, Money totalValue) {
 	return acquisitionRequest.isFundAllocationAllowed(totalValue);
     }
 
     private void createUnitItem() {
 	if (getAcquisitionRequest().getFinancersCount() == 1) {
 	    createUnitItem(getAcquisitionRequest().getFinancers().iterator().next(), getTotalItemValueWithAdditionalCostsAndVat());
 	}
     }
 
     public Money getTotalItemValueWithAdditionalCosts() {
 	if (getAdditionalCostValue() == null) {
 	    return getTotalItemValue();
 	}
 	return getTotalItemValue().add(getAdditionalCostValue());
     }
 
     public Money getTotalItemValueWithAdditionalCostsAndVat() {
 	return getAdditionalCostValue() != null ? getTotalItemValueWithVat().add(getAdditionalCostValue())
 		: getTotalItemValueWithVat();
     }
 
     public Money getTotalItemValue() {
 	return getUnitValue().multiply(getQuantity());
     }
 
     public Money getTotalRealValue() {
 	if (getRealUnitValue() == null || getRealQuantity() == null) {
 	    return null;
 	}
 	return getRealUnitValue().multiply(getRealQuantity());
     }
 
     public Money getTotalRealValueWithAdditionalCosts() {
 	if (getRealUnitValue() == null || getRealQuantity() == null) {
 	    return null;
 	}
 	Money totalRealValue = getTotalRealValue();
 	return getRealAdditionalCostValue() == null ? totalRealValue : totalRealValue.add(getRealAdditionalCostValue());
     }
 
     public Money getTotalRealValueWithAdditionalCostsAndVat() {
 	return getRealAdditionalCostValue() != null ? (getTotalRealVatValue() != null ? getTotalRealValueWithVat().add(
 		getRealAdditionalCostValue()) : null) : getTotalRealValueWithVat();
     }
 
     public Money getTotalItemValueWithVat() {
 	return getTotalItemValue().addPercentage(getVatValue());
     }
 
     public Money getTotalRealValueWithVat() {
 	return getTotalRealValue() != null ? getTotalRealValue().addPercentage(getRealVatValue()) : null;
     }
 
     public void edit(final AcquisitionRequest acquisitionRequest, final String description, final Integer quantity,
 	    final Money unitValue, final BigDecimal vatValue, final Money additionalCostValue, final String proposalReference,
 	    CPVReference reference, String recipient, Address address, String phone, String email) {
 
 	checkLimits(getAcquisitionRequest(), quantity, unitValue);
 	setDescription(description);
 	setQuantity(quantity);
 	setUnitValue(unitValue);
 	setProposalReference(proposalReference);
 	setVatValue(vatValue);
 	setAdditionalCostValue(additionalCostValue);
 	setRecipient(recipient);
 	setAddress(address);
 	setRecipientEmail(email);
 	setRecipientPhone(phone);
 	setCPVReference(reference);
     }
 
     public void editRealValues(Integer realQuantity, Money realUnitValue, Money shipment, BigDecimal realVatValue) {
 	setRealQuantity(realQuantity);
 	setRealUnitValue(realUnitValue);
 	setRealAdditionalCostValue(shipment);
 	setRealVatValue(realVatValue);
     }
 
     @Override
     public void delete() {
 	removeRequest();
 	removeExpenditureTrackingSystem();
 	for (; !getUnitItems().isEmpty(); getUnitItems().get(0).delete())
 	    ;
 	super.delete();
     }
 
     public boolean isAssignedTo(Unit unit) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit() == unit) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     public boolean isFilledWithRealValues() {
 	return getRealQuantity() != null && getRealUnitValue() != null
 		&& (getAdditionalCostValue() == null || getRealAdditionalCostValue() != null);
     }
 
     @Override
     public void createUnitItem(Unit unit, Money shareValue) {
 	createUnitItem(getAcquisitionRequest().addPayingUnit(unit), shareValue);
     }
 
     public List<Unit> getPayingUnits() {
 	List<Unit> payingUnits = new ArrayList<Unit>();
 	for (UnitItem unitItem : getUnitItems()) {
 	    payingUnits.add(unitItem.getUnit());
 	}
 	return payingUnits;
     }
 
     public boolean hasAtLeastOneResponsibleApproval() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getItemAuthorized()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     public List<AcquisitionInvoice> getConfirmedInvoices() {
 	return super.getConfirmedInvoices();
     }
 
     @Override
     public List<AcquisitionInvoice> getConfirmedInvoices(Person person) {
 	return super.getConfirmedInvoices(person);
     }
 
     @Override
     public List<AcquisitionInvoice> getUnconfirmedInvoices(Person person) {
 	return super.getUnconfirmedInvoices(person);
     }
 
     public List<AcquisitionInvoice> getAllUnconfirmedInvoices() {
 	return super.getUnconfirmedInvoices(null);
     }
 
     public Money getTotalVatValue() {
 	Money percentage = getTotalItemValue().percentage(getVatValue());
 	return new Money(percentage.getRoundedValue());
     }
 
     public Money getTotalRealVatValue() {
 	if (getTotalRealValue() == null) {
 	    return null;
 	}
 
 	Money percentage = getTotalRealValue().percentage(getRealVatValue());
 	return new Money(percentage.getRoundedValue());
     }
 
     // replaced with hasBeenApprovedBy()
     // public boolean hasBeenSubmittedForFundsAllocationBy(Person person) {
     // for (UnitItem unitItem : getUnitItems()) {
     // if (unitItem.getUnit().isResponsible(person) &&
     // unitItem.getSubmitedForFundsAllocation()) {
     // return true;
     // }
     // }
     // return false;
     // }
 
     @Override
     public void unapprove() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    unitItem.setSubmitedForFundsAllocation(false);
 	}
     }
 
     @Override
     public Money getRealValue() {
 	return getTotalRealValueWithAdditionalCostsAndVat();
     }
 
     @Override
     public Money getValue() {
 	return getTotalItemValueWithAdditionalCostsAndVat();
     }
 
     public AcquisitionRequest getAcquisitionRequest() {
 	return (AcquisitionRequest) getRequest();
     }
 
     public Integer getCurrentQuantity() {
 	final Integer realQuantity = getRealQuantity();
 	return realQuantity == null ? getQuantity() : realQuantity;
     }
 
     public Money getCurrentUnitValue() {
 	final Money realUnitValue = getRealUnitValue();
 	return realUnitValue == null ? getUnitValue() : realUnitValue;
     }
 
     public Money getCurrentSupplierAllocationValue() {
 	final Money currentUnitValue = getCurrentUnitValue();
 	return currentUnitValue.multiply(getCurrentQuantity());
     }
 
     public BigDecimal getCurrentVatValue() {
 	final BigDecimal realVatValue = getRealVatValue();
 	return realVatValue == null ? getVatValue() : realVatValue;
     }
 
     public Money getCurrentAdditionalCostValue() {
 	final Money realAdditionalCostValue = getRealAdditionalCostValue();
 	return realAdditionalCostValue == null ? getAdditionalCostValue() : realAdditionalCostValue;
     }
 
     public Money getCurrentTotalVatValue() {
 	final Money totalRealVatValue = getTotalRealVatValue();
 	return totalRealVatValue == null ? getTotalVatValue() : totalRealVatValue;
     }
 
     public Money getCurrentTotalItemValueWithAdditionalCostsAndVat() {
 	final Money totalRealItemValueWithAdditionalCostsAndVat = getTotalRealValueWithAdditionalCostsAndVat();
 	return totalRealItemValueWithAdditionalCostsAndVat == null ? getTotalItemValueWithAdditionalCostsAndVat()
 		: totalRealItemValueWithAdditionalCostsAndVat;
     }
 
     public Money getCurrentTotalItemValueWithAdditionalCosts() {
 	final Money totalRealItemValueWithAdditionalCosts = getTotalRealValueWithAdditionalCosts();
 	return totalRealItemValueWithAdditionalCosts == null ? getTotalItemValueWithAdditionalCosts()
 		: totalRealItemValueWithAdditionalCosts;
     }
 
     @Override
     public Money getTotalAmountForCPV(final int year) {
 	return isAppliableForCPV(year) ? getCurrentSupplierAllocationValue() : Money.ZERO;
     }
 
     private boolean isAppliableForCPV(final int year) {
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	final AcquisitionProcess acquisitionProcess = acquisitionRequest.getProcess();
 	return acquisitionProcess.isActive() && acquisitionProcess.isAppiableForYear(year);
     }
 
 }
