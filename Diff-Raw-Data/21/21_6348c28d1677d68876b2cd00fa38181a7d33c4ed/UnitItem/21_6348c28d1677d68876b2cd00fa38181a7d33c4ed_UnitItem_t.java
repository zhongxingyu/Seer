 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class UnitItem extends UnitItem_Base {
 
     public UnitItem(Financer financer, AcquisitionRequestItem item, Money shareValue, Boolean isApproved,
 	    Boolean isSubmitedForFundsAllocation) {
 
 	checkParameters(financer, item, shareValue, isApproved);
 
 	setFinancer(financer);
 	setItem(item);
 	setShareValue(shareValue);
 	setItemApproved(isApproved);
 	setSubmitedForFundsAllocation(isSubmitedForFundsAllocation);
 	setInvoiceConfirmed(Boolean.FALSE);
     }
 
     private void checkParameters(Financer financer, AcquisitionRequestItem item, Money shareValue, Boolean isApproved) {
 	if (financer == null || item == null || shareValue == null || isApproved == null) {
 	    throw new DomainException("unitItem.message.exception.parametersCannotBeNull");
 	}
 
 	if (shareValue.isZero()) {
 	    throw new DomainException("error.share.value.cannot.be.zero");
 	}
 
 	if (shareValue.isNegative()) {
 	    throw new DomainException("error.share.value.cannot.be.negative");
 	}
 
 	Money currentAssignedValue = item.getTotalAssignedValue();
 	if (currentAssignedValue.add(shareValue).isGreaterThan(item.getTotalItemValueWithAdditionalCostsAndVat())) {
 	    throw new DomainException("unitItem.message.exception.assignedValuedBiggerThanTotal");
 	}
     }
 
    public Money getRoundedShareValue() {
	return getShareValue() != null ? new Money(getShareValue().getRoundedValue()) : null;
    }

    public Money getRoundedRealShareValue() {
	return getRealShareValue() != null ? new Money(getRealShareValue().getRoundedValue()) : null;
    }

     public BigDecimal getVatValue() {
 	return getItem().getVatValue();
     }
 
     public Money getShareValueWithVat() {
 	return getShareValue().addPercentage(getVatValue());
     }
 
     public void delete() {
 	removeFinancer();
 	removeItem();
 	Transaction.deleteObject(this);
     }
 
     @Override
     public void setRealShareValue(Money realShareValue) {
 	if (realShareValue != null) {
 	    Money totalAmount = getItem().getTotalRealValueWithAdditionalCostsAndVat();
 	    Money currentAssignedValue = getItem().getTotalRealAssignedValue();
 
 	    if (currentAssignedValue.add(realShareValue).isGreaterThan(totalAmount)) {
 		throw new DomainException("unitItem.message.exception.cannotASsignMoreThanTotalAmount");
 	    }
 	}
 	super.setRealShareValue(realShareValue);
     }
 
     public Unit getUnit() {
 	return getFinancer().getUnit();
     }
 
     public AccountingUnit getAccountingUnit() {
 	return getFinancer().getAccountingUnit();
     }
 }
