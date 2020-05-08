 /*
  * @(#)AllocateFundsPermanently.java
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
 package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons;
 
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionFundAllocationDiaryAndTransactionReportRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectFinancer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.UnitItem;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 /**
  * 
  * @author Luis Cruz
  * @author Paulo Abrantes
  * 
  */
 public class AllocateFundsPermanently<P extends PaymentProcess> extends
 	WorkflowActivity<P, AllocateFundsPermanentlyActivityInformation<P>> {
 
     @Override
     public boolean isActive(P process, User user) {
 	return process.isAccountingEmployee(user.getExpenditurePerson())
 		&& isUserProcessOwner(process, user)
 		&& process.hasAllocatedFundsPermanentlyForAllProjectFinancers()
 		&& ((!process.hasAllInvoicesAllocated() && process.getRequest().hasProposalDocument())
 			|| (ExpenditureTrackingSystem.isInvoiceAllowedToStartAcquisitionProcess()
 				&& !process.getRequest().hasProposalDocument()
 				&& process.isInvoiceConfirmed()));
     }
 
     @Override
     protected void process(AllocateFundsPermanentlyActivityInformation<P> activityInformation) {
 	for (FundAllocationBean fundAllocationBean : activityInformation.getBeans()) {
 	    fundAllocationBean.getFinancer().addEffectiveFundAllocationId(fundAllocationBean.getEffectiveFundAllocationId());
 	}
 	PaymentProcess process = activityInformation.getProcess();
 	for (final FundAllocationBean bean : activityInformation.getBeans()) {
 	    final Financer financer = bean.getFinancer();
 	    final String diaryNumber = bean.getDiaryNumber();
 	    financer.addPaymentDiaryNumber(diaryNumber);
 	    final String transactionNumber = bean.getTransactionNumber();
 	    financer.addTransactionNumber(transactionNumber);
 
 	    if (financer instanceof ProjectFinancer) {
 		final Unit unit = financer.getUnit();
 		final AccountingUnit accountingUnit = financer.getAccountingUnit();
 
 		for (final UnitItem unitItem : financer.getUnitItemsSet()) {
 		    new AcquisitionFundAllocationDiaryAndTransactionReportRequest(unitItem, process.getProcessNumber(),
 			    unit.getUnitNumber(), accountingUnit.getName(), diaryNumber, transactionNumber);
 		}
 	    }
 	}
 	if (process.isInvoiceConfirmed() && process.areAllFundsPermanentlyAllocated()) {
 	    process.allocateFundsPermanently();
 	}
 
     }
 
     public AllocateFundsPermanentlyActivityInformation<P> getActivityInformation(P process) {
 	return new AllocateFundsPermanentlyActivityInformation<P>(process, this, true);
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
 	return false;
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
 	return "resources/AcquisitionResources";
     }
 
 }
