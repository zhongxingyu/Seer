 /*
  * @(#)AllocateProjectFundsPermanently.java
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
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectFinancer;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationBean;
 
 /**
  * 
  * @author Luis Cruz
  * @author Paulo Abrantes
  * 
  */
 public class AllocateProjectFundsPermanently<P extends PaymentProcess> extends
         WorkflowActivity<P, AllocateProjectFundsPermanentlyActivityInformation<P>> {
 
     @Override
     public boolean isActive(P process, User user) {
         return process.isProjectAccountingEmployee(user.getExpenditurePerson())
                 && isUserProcessOwner(process, user)
                 && !process.hasAllocatedFundsPermanentlyForAllProjectFinancers()
                 && ((!process.hasAllInvoicesAllocatedInProject() && process.getRequest().hasProposalDocument()) || (ExpenditureTrackingSystem
                         .isInvoiceAllowedToStartAcquisitionProcess() && process.isInvoiceConfirmed() && !process.getRequest()
                         .hasProposalDocument()));
     }
 
     @Override
     protected void process(AllocateProjectFundsPermanentlyActivityInformation<P> activityInformation) {
         for (FundAllocationBean fundAllocationBean : activityInformation.getBeans()) {
             final ProjectFinancer projectFinancer = (ProjectFinancer) fundAllocationBean.getFinancer();
             projectFinancer.addEffectiveProjectFundAllocationId(fundAllocationBean.getEffectiveFundAllocationId());
         }
     }
 
     @Override
     public AllocateProjectFundsPermanentlyActivityInformation<P> getActivityInformation(P process) {
         return getActivityInformation(process, true);
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
 
     public AllocateProjectFundsPermanentlyActivityInformation<P> getActivityInformation(P process, boolean takeProcess) {
         return new AllocateProjectFundsPermanentlyActivityInformation<P>(process, this, takeProcess);
     }
 
 }
