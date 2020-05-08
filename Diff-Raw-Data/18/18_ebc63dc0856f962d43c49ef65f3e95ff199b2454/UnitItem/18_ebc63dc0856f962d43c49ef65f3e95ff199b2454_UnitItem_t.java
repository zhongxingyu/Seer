 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class UnitItem extends UnitItem_Base {
 
     public UnitItem(Unit unit, AcquisitionRequestItem item, Money shareValue, Boolean isApproved) {
 
 	checkParameters(unit, item, shareValue, isApproved);
 
 	setUnit(unit);
 	setItem(item);
 	setShareValue(shareValue);
 	setItemApproved(isApproved);
 	setInvoiceConfirmed(Boolean.FALSE);
     }
 
     private void checkParameters(Unit unit, AcquisitionRequestItem item, Money shareValue, Boolean isApproved) {
 	if (unit == null || item == null || shareValue == null || isApproved == null) {
 	    throw new DomainException("error.parameters.cannot.be.null");
 	}
 
 	if (shareValue.isZero()) {
 	    throw new DomainException("error.share.value.cannot.be.zero");
 	}
 
 	if (shareValue.isNegative()) {
 	    throw new DomainException("error.share.value.cannot.be.negative");
 	}
 
 	Money currentAssignedValue = item.getTotalAssignedValue();
	if (currentAssignedValue.add(shareValue).isGreaterThan(item.getTotalItemValue())) {
 	    throw new DomainException("error.assigned.value.bigger.than.total.amount");
 	}
     }
 
     public BigDecimal getVatValue() {
 	return getItem().getVatValue();
     }
 
     public Money getShareValueWithVat() {
 	return getShareValue().addPercentage(getVatValue());
     }
     
     public void delete() {
 	removeUnit();
 	removeItem();
 	Transaction.deleteObject(this);
     }
 }
