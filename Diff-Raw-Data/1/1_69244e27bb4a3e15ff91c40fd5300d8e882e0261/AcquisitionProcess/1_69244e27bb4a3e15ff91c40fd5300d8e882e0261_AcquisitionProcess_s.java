 /*
  * @(#)AcquisitionProcess.java
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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import myorg.util.ClassNameBundle;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.ProcessState;
 import pt.ist.expenditureTrackingSystem.domain.dto.PayingUnitTotalBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 @ClassNameBundle(bundle = "resources/ExpenditureResources")
 /**
  * 
  * @author Paulo Abrantes
  * @author Luis Cruz
  * @author João Alfaiate
  * 
  */
 public abstract class AcquisitionProcess extends AcquisitionProcess_Base {
 
     public AcquisitionProcess() {
 	super();
 	setOjbConcreteClass(getClass().getName());
 	super.setSkipSupplierFundAllocation(Boolean.FALSE);
 	setProcessNumber(constructProcessNumber());
     }
 
     protected String constructProcessNumber() {
 	final ExpenditureTrackingSystem instance = getExpenditureTrackingSystem();
 	if (instance.hasProcessPrefix()) {
 	    return instance.getInstitutionalProcessNumberPrefix() + "/" + getYear() + "/" + getAcquisitionProcessNumber();
 	}
 	return getYear() + "/" + getAcquisitionProcessNumber();
     }
 
     @Override
     public void migrateProcessNumber() {
 	final ExpenditureTrackingSystem instance = getExpenditureTrackingSystem();
 	if (!getProcessNumber().startsWith(instance.getInstitutionalProcessNumberPrefix())) {
 	    setProcessNumber(constructProcessNumber());
 	}
     }
 
     public boolean isAvailableForCurrentUser() {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null && isAvailableForPerson(loggedPerson);
     }
 
     public boolean isAvailableForPerson(final Person person) {
 	final User user = person.getUser();
 	return ExpenditureTrackingSystem.isAcquisitionCentralGroupMember(user)
 		|| ExpenditureTrackingSystem.isAcquisitionCentralManagerGroupMember(user)
 		|| ExpenditureTrackingSystem.isAccountingManagerGroupMember(user)
 		|| ExpenditureTrackingSystem.isProjectAccountingManagerGroupMember(user)
 		|| ExpenditureTrackingSystem.isTreasuryMemberGroupMember(user)
 		|| ExpenditureTrackingSystem.isAcquisitionsProcessAuditorGroupMember(user)
 		|| ExpenditureTrackingSystem.isFundCommitmentManagerGroupMember(user)
 		|| getRequestor() == person
 		|| isTakenByPerson(person.getUser())
 		|| getRequestingUnit().isResponsible(person)
 		|| isResponsibleForAtLeastOnePayingUnit(person)
 		|| isAccountingEmployee(person)
 		|| isProjectAccountingEmployee(person)
 		|| isTreasuryMember(person)
 		|| isObserver(person);
     }
 
     @Override
     public boolean isAccessible(User user) {
 	return isAvailableForPerson(user.getExpenditurePerson());
     }
 
     public boolean isActive() {
 	return getLastAcquisitionProcessState().isActive();
     }
 
     public AcquisitionProcessState getAcquisitionProcessState() {
 	return getLastAcquisitionProcessState();
     }
 
     protected AcquisitionProcessState getLastAcquisitionProcessState() {
 	AcquisitionProcessState state = (AcquisitionProcessState) getCurrentProcessState();
 	if (state == null) {
 	    state = (AcquisitionProcessState) Collections.max(getProcessStates(), ProcessState.COMPARATOR_BY_WHEN);
 	}
 	return state;
     }
 
     public AcquisitionProcessStateType getAcquisitionProcessStateType() {
 	return getLastAcquisitionProcessState().getAcquisitionProcessStateType();
     }
 
     @Override
     public boolean isPendingApproval() {
 	return getLastAcquisitionProcessState().isPendingApproval();
     }
 
     public boolean isApproved() {
 	final AcquisitionProcessStateType acquisitionProcessStateType = getAcquisitionProcessStateType();
 	return acquisitionProcessStateType.compareTo(AcquisitionProcessStateType.SUBMITTED_FOR_FUNDS_ALLOCATION) <= 0
 		&& isActive();
     }
 
     public boolean isAllocatedToSupplier() {
 	return getLastAcquisitionProcessState().isAllocatedToSupplier();
     }
 
     public boolean isAllocatedToUnit() {
 	return getLastAcquisitionProcessState().isAllocatedToUnit();
     }
 
     public boolean isPayed() {
 	return getLastAcquisitionProcessState().isPayed();
     }
 
     public boolean isAllocatedToUnit(Unit unit) {
 	return isAllocatedToUnit() && getPayingUnits().contains(unit);
     }
 
     public boolean isAcquisitionProcessed() {
 	return getLastAcquisitionProcessState().isAcquisitionProcessed();
     }
 
     public boolean isInvoiceReceived() {
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	return getLastAcquisitionProcessState().isInvoiceReceived();
     }
 
     public boolean isPastInvoiceReceived() {
 	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
 	return getLastAcquisitionProcessState().isPastInvoiceReceived();
     }
 
     public Unit getUnit() {
 	return getRequestingUnit();
     }
 
     public Money getAmountAllocatedToUnit(Unit unit) {
 	return getAcquisitionRequest().getAmountAllocatedToUnit(unit);
     }
 
     public Money getAcquisitionRequestValueLimit() {
 	return null;
     }
 
     public Unit getRequestingUnit() {
 	return getAcquisitionRequest().getRequestingUnit();
     }
 
     public boolean isAllowedToViewCostCenterExpenditures() {
 	try {
 	    return (getUnit() != null && isResponsibleForUnit())
 	    	|| ExpenditureTrackingSystem.isAccountingManagerGroupMember()
 	    	|| ExpenditureTrackingSystem.isProjectAccountingManagerGroupMember()
 	    	|| isAccountingEmployee()
 	    	|| isProjectAccountingEmployee()
 	    	|| ExpenditureTrackingSystem.isManager();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    throw new Error(e);
 	}
     }
 
     public boolean isAllowedToViewSupplierExpenditures() {
 	return ExpenditureTrackingSystem.isAcquisitionCentralGroupMember()
 		|| ExpenditureTrackingSystem.isAcquisitionCentralManagerGroupMember()
 		|| ExpenditureTrackingSystem.isManager();
     }
 
     public boolean checkRealValues() {
 	return getAcquisitionRequest().checkRealValues();
     }
 
     public Integer getYear() {
 	return getPaymentProcessYear().getYear();
     }
 
     /*
      * use getProcessNumber() instead
      */
     @Deprecated
     public String getAcquisitionProcessId() {
 	return getProcessNumber();
     }
 
     public boolean isProcessFlowCharAvailable() {
 	return false;
     }
 
     public List<AcquisitionProcessStateType> getAvailableStates() {
 	return Collections.emptyList();
     }
 
     public String getAllocationIds() {
 	StringBuilder builder = new StringBuilder();
 	for (PayingUnitTotalBean bean : getAcquisitionRequest().getTotalAmountsForEachPayingUnit()) {
 	    builder.append(bean.getFinancer().getFundAllocationIds());
 	}
 	return builder.toString();
     }
 
     public String getEffectiveAllocationIds() {
 	StringBuilder builder = new StringBuilder();
 	for (PayingUnitTotalBean bean : getAcquisitionRequest().getTotalAmountsForEachPayingUnit()) {
 	    builder.append(bean.getFinancer().getEffectiveFundAllocationIds());
 	}
 	return builder.toString();
     }
 
     public AcquisitionRequest getRequest() {
 	return getAcquisitionRequest();
     }
 
     @Override
     public boolean isInGenesis() {
 	return getAcquisitionProcessState().isInGenesis();
     }
 
     @Override
     public boolean isInApprovedState() {
 	return getAcquisitionProcessState().isInApprovedState();
     }
 
     @Override
     public boolean isPendingFundAllocation() {
 	return getAcquisitionProcessState().isInAllocatedToSupplierState();
     }
 
     @Override
     public boolean isInAuthorizedState() {
 	return getAcquisitionProcessState().isAuthorized();
     }
 
     @Override
     public boolean isInvoiceConfirmed() {
 	return getAcquisitionProcessState().isInvoiceConfirmed();
     }
 
     @Override
     public boolean isAllocatedPermanently() {
 	return getAcquisitionProcessState().isAllocatedPermanently();
     }
 
     @Override
     public Collection<Supplier> getSuppliers() {
 	return getRequest().getSuppliers();
 
     }
 
     @Override
     public String getProcessStateName() {
 	return getLastAcquisitionProcessState().getLocalizedName();
     }
 
     @Override
     public int getProcessStateOrder() {
 	return getLastAcquisitionProcessState().getAcquisitionProcessStateType().ordinal();
     }
 
     public Boolean getShouldSkipSupplierFundAllocation() {
 	return getSkipSupplierFundAllocation();
     }
 
     public String getAcquisitionRequestDocumentID() {
 	return hasPurchaseOrderDocument() ? getPurchaseOrderDocument().getRequestId() : ExpenditureTrackingSystem.getInstance()
 		.nextAcquisitionRequestDocumentID();
     }
 
     // TODO: delete this method... it's not used.
     public AcquisitionProposalDocument getAcquisitionProposalDocument() {
 	List<AcquisitionProposalDocument> files = getFiles(AcquisitionProposalDocument.class);
 	return files.isEmpty() ? null : files.get(0);
     }
 
     public boolean hasAcquisitionProposalDocument() {
 	return !getFiles(AcquisitionProposalDocument.class).isEmpty();
     }
 
     public void setPurchaseOrderDocument(PurchaseOrderDocument document) {
 	addFiles(document);
     }
 
     public PurchaseOrderDocument getPurchaseOrderDocument() {
 	List<PurchaseOrderDocument> files = getFiles(PurchaseOrderDocument.class);
 	if (files.size() > 1) {
 	    throw new DomainException("error.should.only.have.one.purchaseOrder");
 	}
 	return files.isEmpty() ? null : files.get(0);
     }
 
     public boolean hasPurchaseOrderDocument() {
 	return !getFiles(PurchaseOrderDocument.class).isEmpty();
     }
 
     @Override
     public boolean isCanceled() {
 	return getLastAcquisitionProcessState().isCanceled()
 		|| getLastAcquisitionProcessState().isRejected();
     }
 
     @Override
     public void revertToState(ProcessState processState) {
 	final AcquisitionProcessState acquisitionProcessState = (AcquisitionProcessState) processState;
 	final AcquisitionProcessStateType acquisitionProcessStateType = acquisitionProcessState.getAcquisitionProcessStateType();
 	if (acquisitionProcessStateType != null && acquisitionProcessStateType != AcquisitionProcessStateType.CANCELED) {
 	    new AcquisitionProcessState(this, acquisitionProcessStateType);
 	}
     }
     
 }
