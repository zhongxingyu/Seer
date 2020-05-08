 /*
  * @(#)UnitItem.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 
 import pt.ist.bennu.core.domain.exceptions.DomainException;
 import pt.ist.bennu.core.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 /**
  * 
  * @author Paulo Abrantes
  * @author Luis Cruz
  * 
  */
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
             throw new DomainException("unitItem.message.exception.parametersCannotBeNull",
                     DomainException.getResourceFor("resources/AcquisitionResources"));
         }
 
         if (shareValue.isZero()) {
             throw new DomainException("error.share.value.cannot.be.zero",
                     DomainException.getResourceFor("resources/AcquisitionResources"));
         }
 
         if (shareValue.isNegative()) {
             throw new DomainException("error.share.value.cannot.be.negative",
                     DomainException.getResourceFor("resources/AcquisitionResources"));
         }
 
         Money currentAssignedValue = item.getTotalAssigned();
         if (currentAssignedValue.addAndRound(shareValue).isGreaterThan(item.getValue().round())) {
             throw new DomainException("unitItem.message.exception.assignedValuedBiggerThanTotal",
                     DomainException.getResourceFor("resources/AcquisitionResources"));
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
         setFinancer(null);
         setItem(null);
         getConfirmedInvoicesSet().clear();
         for (final ProjectAcquisitionFundAllocationRequest request : getProjectAcquisitionFundAllocationRequestSet()) {
             request.setUnitItem(null);
         }
         deleteDomainObject();
     }
 
     @Override
     public void setRealShareValue(Money realShareValue) {
         if (realShareValue != null) {
             Money totalAmount = getItem().getRealValue();
             Money currentAssignedValue = getItem().getTotalRealAssigned();
 
            if (currentAssignedValue.add(realShareValue).round().isGreaterThan(totalAmount.round())) {
                 throw new DomainException("unitItem.message.exception.cannotASsignMoreThanTotalAmount",
                         DomainException.getResourceFor("resources/AcquisitionResources"));
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
         Collection<PaymentProcessInvoice> invoicesFiles = getItem().getInvoicesFiles();
         Collection<PaymentProcessInvoice> confirmedInvoices = getConfirmedInvoices();
 
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
         for (final ProjectAcquisitionFundAllocationRequest request : getProjectAcquisitionFundAllocationRequestSet()) {
             if (isFinalFundAllocation == request.getFinalFundAllocation().booleanValue()) {
                 request.cancelFundAllocationRequest();
             }
         }
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessInvoice> getConfirmedInvoices() {
         return getConfirmedInvoicesSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectAcquisitionFundAllocationRequest> getProjectAcquisitionFundAllocationRequest() {
         return getProjectAcquisitionFundAllocationRequestSet();
     }
 
     @Deprecated
     public java.util.Set<pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionFundAllocationDiaryAndTransactionReportRequest> getAcquisitionFundAllocationDiaryAndTransactionReportRequest() {
         return getAcquisitionFundAllocationDiaryAndTransactionReportRequestSet();
     }
 
     @Deprecated
     public boolean hasAnyConfirmedInvoices() {
         return !getConfirmedInvoicesSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyProjectAcquisitionFundAllocationRequest() {
         return !getProjectAcquisitionFundAllocationRequestSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasAnyAcquisitionFundAllocationDiaryAndTransactionReportRequest() {
         return !getAcquisitionFundAllocationDiaryAndTransactionReportRequestSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasSubmitedForFundsAllocation() {
         return getSubmitedForFundsAllocation() != null;
     }
 
     @Deprecated
     public boolean hasItemAuthorized() {
         return getItemAuthorized() != null;
     }
 
     @Deprecated
     public boolean hasInvoiceConfirmed() {
         return getInvoiceConfirmed() != null;
     }
 
     @Deprecated
     public boolean hasShareValue() {
         return getShareValue() != null;
     }
 
     @Deprecated
     public boolean hasRealShareValue() {
         return getRealShareValue() != null;
     }
 
     @Deprecated
     public boolean hasFinancer() {
         return getFinancer() != null;
     }
 
     @Deprecated
     public boolean hasItem() {
         return getItem() != null;
     }
 
 }
