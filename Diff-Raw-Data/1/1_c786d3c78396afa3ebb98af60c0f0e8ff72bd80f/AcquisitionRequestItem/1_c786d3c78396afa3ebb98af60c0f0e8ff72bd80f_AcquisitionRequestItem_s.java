 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.DeliveryInfo;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.util.Address;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class AcquisitionRequestItem extends AcquisitionRequestItem_Base {
 
     protected AcquisitionRequestItem() {
 	super();
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     private AcquisitionRequestItem(final AcquisitionRequest acquisitionRequest, final String description, final Integer quantity,
 	    final Money unitValue, final BigDecimal vatValue, final String proposalReference, CPVReference reference) {
 
 	checkLimits(acquisitionRequest, quantity, unitValue);
 
 	setAcquisitionRequest(acquisitionRequest);
 	setDescription(description);
 	setQuantity(quantity);
 	setUnitValue(unitValue);
 	setVatValue(vatValue);
 	setProposalReference(proposalReference);
 	setCPVReference(reference);
     }
 
     private void checkLimits(AcquisitionRequest acquisitionRequest, Integer quantity, Money unitValue) {
 	Money totalValue = unitValue.multiply(quantity.longValue());
 	if (!checkAcquisitionRequestValueLimit(acquisitionRequest, totalValue)) {
 	    throw new DomainException("acquisitionRequestItem.message.exception.totalValueExceed", acquisitionRequest
 		    .getAcquisitionProcess().getAcquisitionRequestValueLimit().toFormatString());
 	}
 
 	if (!checkSupplierFundAllocation(acquisitionRequest, totalValue)) {
 	    throw new DomainException("acquisitionRequestItem.message.exception.fundAllocationNotAllowed");
 	}
     }
 
     private boolean checkAcquisitionRequestValueLimit(AcquisitionRequest acquisitionRequest, Money totalValue) {
 	return acquisitionRequest.isValueAllowed(totalValue);
     }
 
     private boolean checkSupplierFundAllocation(AcquisitionRequest acquisitionRequest, Money totalValue) {
 	return acquisitionRequest.getSupplier().isFundAllocationAllowed(totalValue);
     }
 
     public AcquisitionRequestItem(final AcquisitionRequestItemBean acquisitionRequestItemBean) {
 	this(acquisitionRequestItemBean.getAcquisitionRequest(), acquisitionRequestItemBean.getDescription(),
 		acquisitionRequestItemBean.getQuantity(), acquisitionRequestItemBean.getUnitValue(), acquisitionRequestItemBean
 			.getVatValue(), acquisitionRequestItemBean.getProposalReference(), acquisitionRequestItemBean
 			.getCPVReference());
 	setAdditionalCostValue(acquisitionRequestItemBean.getAdditionalCostValue());
 	setDeliveryInfo(acquisitionRequestItemBean);
     }
 
     protected void setDeliveryInfo(AcquisitionRequestItemBean acquisitionRequestItemBean) {
 	String recipient;
 	Address address;
 	if (acquisitionRequestItemBean.getDeliveryInfo() != null) {
 	    recipient = acquisitionRequestItemBean.getDeliveryInfo().getRecipient();
 	    address = acquisitionRequestItemBean.getDeliveryInfo().getAddress();
 	} else {
 	    recipient = acquisitionRequestItemBean.getRecipient();
 	    address = acquisitionRequestItemBean.getAddress();
 	    acquisitionRequestItemBean.getAcquisitionRequest().getRequester().createNewDeliveryInfo(recipient, address);
 	}
 	setRecipient(recipient);
 	setAddress(address);
     }
 
     public AcquisitionRequestItem(final AcquisitionRequest acquisitionRequest, final String description, final Integer quantity,
 	    final Money unitValue, final BigDecimal vatValue, final Money additionalCostValue, final String proposalReference,
 	    CPVReference reference, String recipient, Address address) {
 	this(acquisitionRequest, description, quantity, unitValue, vatValue, proposalReference, reference);
 	setRecipient(recipient);
 	setAddress(address);
 	setAdditionalCostValue(additionalCostValue);
     }
 
     public Money getTotalItemValue() {
 	if (getAdditionalCostValue() == null) {
 	    return getUnitValue().multiply(getQuantity());
 	}
 	return getUnitValue().multiply(getQuantity()).add(getAdditionalCostValue());
     }
 
     public Money getTotalRealValue() {
 	if (getRealUnitValue() == null || getRealQuantity() == null) {
 	    return null;
 	}
 	Money totalRealValue = getRealUnitValue().multiply(getRealQuantity());
 	return getRealAdditionalCostValue() == null ? totalRealValue : totalRealValue.add(getRealAdditionalCostValue());
     }
 
     public Money getTotalItemValueWithVat() {
 	return getTotalItemValue().addPercentage(getVatValue());
     }
 
     public Money getTotalRealValueWithVat() {
 	return getTotalItemValueWithVat().addPercentage(getRealVatValue());
     }
 
     public Money getTotalAssignedValue() {
 	Money sum = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    sum = sum.add(unitItem.getShareValue());
 	}
 	return sum;
     }
 
     public Money getTotalRealAssignedValue() {
 	Money sum = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		sum = sum.add(unitItem.getRealShareValue());
 	    }
 	}
 	return sum;
     }
 
     public void edit(String description, Integer quantity, Money unitValue, BigDecimal vatValue, String proposalReference,
 	    CPVReference reference, DeliveryInfo deliveryInfo) {
 
 	checkLimits(getAcquisitionRequest(), quantity, unitValue);
 
 	setDescription(description);
 	setQuantity(quantity);
 	setUnitValue(unitValue);
 	setCPVReference(reference);
 	setProposalReference(proposalReference);
 	setVatValue(vatValue);
 	setRecipient(deliveryInfo.getRecipient());
 	setAddress(deliveryInfo.getAddress());
     }
 
     public void edit(AcquisitionRequestItemBean acquisitionRequestItemBean) {
 
 	checkLimits(getAcquisitionRequest(), acquisitionRequestItemBean.getQuantity(), acquisitionRequestItemBean.getUnitValue());
 
 	setDescription(acquisitionRequestItemBean.getDescription());
 	setQuantity(acquisitionRequestItemBean.getQuantity());
 	setUnitValue(acquisitionRequestItemBean.getUnitValue());
 	setProposalReference(acquisitionRequestItemBean.getProposalReference());
 	setVatValue(acquisitionRequestItemBean.getVatValue());
 	setAdditionalCostValue(acquisitionRequestItemBean.getAdditionalCostValue());
 	setDeliveryInfo(acquisitionRequestItemBean);
 	setCPVReference(acquisitionRequestItemBean.getCPVReference());
 
     }
 
     public void editRealValues(AcquisitionRequestItemBean acquisitionRequestItemBean) {
 	setRealQuantity(acquisitionRequestItemBean.getRealQuantity());
 	setRealUnitValue(acquisitionRequestItemBean.getRealUnitValue());
 	setRealAdditionalCostValue(acquisitionRequestItemBean.getShipment());
     }
 
     public void delete() {
 	removeAcquisitionRequest();
 	removeExpenditureTrackingSystem();
 	for (; !getUnitItems().isEmpty(); getUnitItems().get(0).delete())
 	    ;
 	Transaction.deleteObject(this);
     }
 
     public boolean isAssignedTo(Unit unit) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit() == unit) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public UnitItem getUnitItemFor(Unit unit) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit() == unit) {
 		return unitItem;
 	    }
 	}
 	return null;
     }
 
     public boolean isFilledWithRealValues() {
 	return getRealQuantity() != null && getRealUnitValue() != null && (getAdditionalCostValue() == null || getRealAdditionalCostValue() != null);
     }
 
     public boolean isValueFullyAttributedToUnits() {
 	Money totalValue = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    totalValue = totalValue.add(unitItem.getShareValue());
 	}
 
 	return totalValue.equals(getTotalItemValue());
     }
 
     public boolean isRealValueFullyAttributedToUnits() {
 	Money totalValue = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		totalValue = totalValue.add(unitItem.getRealShareValue());
 	    }
 	}
 
 	return totalValue.equals(getTotalRealValue());
     }
 
     public void createUnitItem(Unit unit, Money shareValue) {
 	new UnitItem(unit, this, shareValue, Boolean.FALSE);
     }
 
     public List<Unit> getPayingUnits() {
 	List<Unit> payingUnits = new ArrayList<Unit>();
 	for (UnitItem unitItem : getUnitItems()) {
 	    payingUnits.add(unitItem.getUnit());
 	}
 	return payingUnits;
     }
 
     private void modifyApprovingStateFor(Person person, Boolean value) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person)) {
 		unitItem.setItemApproved(value);
 	    }
 	}
     }
 
     public void approvedBy(Person person) {
 	modifyApprovingStateFor(person, Boolean.TRUE);
     }
 
     public void unapprovedBy(Person person) {
 	modifyApprovingStateFor(person, Boolean.FALSE);
     }
 
     private void modifyInvoiceState(Person person, Boolean value) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person)) {
 		unitItem.setInvoiceConfirmed(value);
 	    }
 	}
     }
 
     public void confirmInvoiceBy(Person person) {
 	modifyInvoiceState(person, Boolean.TRUE);
     }
 
     public void unconfirmInvoiceBy(Person person) {
 	modifyInvoiceState(person, Boolean.FALSE);
     }
 
     public boolean hasAtLeastOneResponsibleApproval() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getItemApproved()) {
 		return true;
 	    }
 	}
 	return false;
     }
     
     public boolean isApproved() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (!unitItem.getItemApproved()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean isInvoiceConfirmed() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (!unitItem.getInvoiceConfirmed()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean hasBeenApprovedBy(Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person) && unitItem.getItemApproved()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isInvoiceConfirmedBy(Person person) {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getUnit().isResponsible(person) && unitItem.getInvoiceConfirmed()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasAtLeastOneInvoiceConfirmation() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getInvoiceConfirmed()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public void clearRealShareValues() {
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		unitItem.setRealShareValue(null);
 	    }
 	}
     }
 
     public List<UnitItem> getSortedUnitItems() {
 	List<UnitItem> unitItems = new ArrayList<UnitItem>(getUnitItems());
 	Collections.sort(unitItems, new Comparator<UnitItem>() {
 
 	    public int compare(UnitItem unitItem1, UnitItem unitItem2) {
 		return unitItem1.getUnit().getPresentationName().compareTo(unitItem2.getUnit().getPresentationName());
 	    }
 	    
 	});
 	
 	return unitItems;
     }
 }
