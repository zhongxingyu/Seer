 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public class UnitItem extends UnitItem_Base {
 
     public UnitItem(Financer financer, RequestItem item, Money shareValue, Boolean isAuthorized,
 	    Boolean isSubmitedForFundsAllocation) {
 
 	checkParameters(financer, item, shareValue, isAuthorized);
 
 	setFinancer(financer);
 	setItem(item);
 	setShareValue(shareValue);
 	setItemAuthorized(isAuthorized);
 	setSubmitedForFundsAllocation(isSubmitedForFundsAllocation);
 	setInvoiceConfirmed(Boolean.FALSE);
     }
 
     private void checkParameters(Financer financer, RequestItem item, Money shareValue, Boolean isApproved) {
 	if (financer == null || item == null || shareValue == null || isApproved == null) {
 	    throw new DomainException("unitItem.message.exception.parametersCannotBeNull", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
 
 	if (shareValue.isZero()) {
 	    throw new DomainException("error.share.value.cannot.be.zero", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
 
 	if (shareValue.isNegative()) {
 	    throw new DomainException("error.share.value.cannot.be.negative", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
 	}
 
 	Money currentAssignedValue = item.getTotalAssigned();
 	if (currentAssignedValue.addAndRound(shareValue).isGreaterThan(item.getValue().round())) {
 	    throw new DomainException("unitItem.message.exception.assignedValuedBiggerThanTotal", DomainException
 		    .getResourceFor("resources/AcquisitionResources"));
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
 
    public Money getShareVatValue() {
	return getShareValue().multiplyAndRound(getVatValue());
     }
 
     public void delete() {
 	removeFinancer();
 	removeItem();
 	getConfirmedInvoicesSet().clear();
 	for (final ProjectAcquisitionFundAllocationRequest request : getProjectAcquisitionFundAllocationRequestSet()) {
 	    if (request.isCanceled()) {
 		request.removeUnitItem();
 	    } else {
 		throw new Error("request.must.be.canceled.first");
 	    }
 	}
 	deleteDomainObject();
     }
 
     @Override
     public void setRealShareValue(Money realShareValue) {
 	if (realShareValue != null) {
 	    Money totalAmount = getItem().getRealValue();
 	    Money currentAssignedValue = getItem().getTotalRealAssigned();
 
 	    if (currentAssignedValue.addAndRound(realShareValue).isGreaterThan(totalAmount.round())) {
 		throw new DomainException("unitItem.message.exception.cannotASsignMoreThanTotalAmount", DomainException
 			.getResourceFor("resources/AcquisitionResources"));
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
 
     @Override
     @Deprecated
     public Boolean getSubmitedForFundsAllocation() {
 	return super.getSubmitedForFundsAllocation();
     }
 
     public boolean isApproved() {
 	return getSubmitedForFundsAllocation();
     }
 
     public boolean isWithAllInvoicesConfirmed() {
 	List<PaymentProcessInvoice> invoicesFiles = getItem().getInvoicesFiles();
 	List<PaymentProcessInvoice> confirmedInvoices = getConfirmedInvoices();
 
 	return !invoicesFiles.isEmpty() && confirmedInvoices.containsAll(invoicesFiles);
 
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
 	return hasFinancer() && getFinancer().isConnectedToCurrentHost();
     }
 
     public String getProjectFundAllocationId() {
 	for (final ProjectAcquisitionFundAllocationRequest request : getProjectAcquisitionFundAllocationRequestSet()) {
 	    if (!request.isCanceled()) {
 		final String result = request.getFundAllocationNumber();
 		if (result != null && !result.isEmpty()) {
 		    return result;
 		}
 	    }
 	}
 	return null;
     }
 
     public void cancelFundAllocationRequest(final boolean isFinalFundAllocation) {
 	if (!isFinalFundAllocation) {
 	    for (final ProjectAcquisitionFundAllocationRequest request : getProjectAcquisitionFundAllocationRequestSet()) {
 		request.cancelFundAllocationRequest();
 	    }
 	}
     }
 
 }
