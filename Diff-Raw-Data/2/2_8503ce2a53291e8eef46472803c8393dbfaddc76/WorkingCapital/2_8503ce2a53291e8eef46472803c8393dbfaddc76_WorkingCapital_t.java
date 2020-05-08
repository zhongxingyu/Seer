 /*
  * @(#)WorkingCapital.java
  *
  * Copyright 2010 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Working Capital Module.
  *
  *   The Working Capital Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Working Capital Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Working Capital Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.workingCapital.domain;
 
 import java.util.Collections;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.organization.domain.Person;
 import module.workflow.util.PresentableProcessState;
 
 import org.joda.time.DateTime;
 
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.domain.exceptions.DomainException;
 import pt.ist.bennu.core.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 /**
  * 
  * @author João Neves
  * @author Luis Cruz
  * 
  */
 public class WorkingCapital extends WorkingCapital_Base {
 
     public static final String bundleResource = "resources/WorkingCapitalResources";
 
     public static WorkingCapital find(final WorkingCapitalYear workingCapitalYear, final Unit unit) {
         for (final WorkingCapital workingCapital : unit.getWorkingCapitalsSet()) {
             if (workingCapital.getWorkingCapitalYear() == workingCapitalYear) {
                 return workingCapital;
             }
         }
         return null;
     }
 
     public static WorkingCapital find(final Integer year, final Unit unit) {
         for (final WorkingCapital workingCapital : unit.getWorkingCapitalsSet()) {
             if (workingCapital.getWorkingCapitalYear().getYear().intValue() == year.intValue()) {
                 return workingCapital;
             }
         }
         return null;
     }
 
     public WorkingCapital() {
         super();
         setWorkingCapitalSystem(WorkingCapitalSystem.getInstanceForCurrentHost());
     }
 
     public WorkingCapital(final WorkingCapitalYear workingCapitalYear, final Unit unit, final Person movementResponsible) {
         this();
         if (find(workingCapitalYear, unit) != null) {
             throw new DomainException("message.working.capital.exists.for.year.and.unit");
         }
         setWorkingCapitalYear(workingCapitalYear);
         setUnit(unit);
         if (movementResponsible == null) {
             throw new DomainException("message.working.capital.movementResponsible.cannot.be.null");
         }
         setMovementResponsible(movementResponsible);
         new WorkingCapitalProcess(this);
     }
 
     public WorkingCapital(final Integer year, final Unit unit, final Person movementResponsible) {
         this(WorkingCapitalYear.findOrCreate(year), unit, movementResponsible);
     }
 
     public SortedSet<WorkingCapitalInitialization> getSortedWorkingCapitalInitializations() {
         final SortedSet<WorkingCapitalInitialization> result =
                 new TreeSet<WorkingCapitalInitialization>(WorkingCapitalInitialization.COMPARATOR_BY_REQUEST_CREATION);
         result.addAll(getWorkingCapitalInitializationsSet());
         return result;
     }
 
     public Authorization findUnitResponsible(final Person person, final Money amount) {
         final Unit unit = getUnit();
         return findUnitResponsible(person, amount, unit);
     }
 
     public Authorization findDirectUnitResponsible(final Person person, final Money amount) {
         final Unit unit = getUnit();
         return findDirectUnitResponsible(person, amount, unit);
     }
 
     private Authorization findUnitResponsible(final Person person, final Money amount, final Unit unit) {
         if (unit != null && person != null) {
             //boolean hasAtLeastOneResponsible = false;
             for (final Authorization authorization : unit.getAuthorizationsSet()) {
                 if (authorization.isValid() && authorization.getMaxAmount().isGreaterThanOrEqual(amount)) {
                     //hasAtLeastOneResponsible = true;
                     if (authorization.getPerson().getUser() == person.getUser()) {
                         return authorization;
                     }
                 }
             }
             //if (!hasAtLeastOneResponsible) {
             final Unit parent = unit.getParentUnit();
             return findUnitResponsible(person, amount, parent);
             //}
         }
         return null;
     }
 
     private Authorization findDirectUnitResponsible(final Person person, final Money amount, final Unit unit) {
         if (unit != null && person != null) {
             boolean hasAtLeastOneResponsible = false;
             for (final Authorization authorization : unit.getAuthorizationsSet()) {
                 if (authorization.isValid() && authorization.getMaxAmount().isGreaterThanOrEqual(amount)) {
                     hasAtLeastOneResponsible = true;
                     if (authorization.getPerson().getUser() == person.getUser()) {
                         return authorization;
                     }
                 }
             }
             if (!hasAtLeastOneResponsible) {
                 final Unit parent = unit.getParentUnit();
                 return findDirectUnitResponsible(person, amount, parent);
             }
         }
         return null;
     }
 
     public boolean isPendingAproval() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isPendingAproval();
     }
 
     public boolean isPendingAproval(final User user) {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isPendingAproval(user);
     }
 
     public boolean isPendingDirectAproval(final User user) {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isPendingDirectAproval(user);
     }
 
     public boolean isPendingVerification(final User user) {
         return isAccountingResponsible(user) && isPendingVerification();
     }
 
     public boolean isPendingVerification() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isPendingVerification();
     }
 
     public boolean isPendingFundAllocation(final User user) {
         return isAccountingEmployee(user) && isPendingFundAllocation();
     }
 
     public boolean isPendingFundUnAllocation(final User user) {
         return isAccountingEmployee(user) && isPendingFundUnAllocation();
     }
 
     public boolean isPendingDirectFundAllocation(final User user) {
         return isDirectAccountingEmployee(user) && isPendingFundAllocation();
     }
 
     public boolean isPendingFundAllocation() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isPendingFundAllocation();
     }
 
     public boolean isPendingFundUnAllocation() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isPendingFundUnAllocation();
     }
 
     public boolean isPendingAuthorization(final User user) {
         final WorkingCapitalSystem workingCapitalSystem = WorkingCapitalSystem.getInstanceForCurrentHost();
         return workingCapitalSystem.isManagementMember(user) && isPendingAuthorization();
     }
 
     public boolean isPendingAuthorization() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isPendingAuthorization();
     }
 
     public boolean isPendingPayment(final User user) {
         return isTreasuryMember(user) && isPendingPayment();
     }
 
     public boolean isPendingPayment() {
         for (final WorkingCapitalRequest workingCapitalRequest : getWorkingCapitalRequestsSet()) {
             if (workingCapitalRequest.getProcessedByTreasury() == null) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isAvailable(final User user) {
         if (user == null) {
             return false;
         }
         final WorkingCapitalSystem workingCapitalSystem = WorkingCapitalSystem.getInstanceForCurrentHost();
         if ((hasMovementResponsible() && user == getMovementResponsible().getUser()) || isAccountingResponsible(user)
                 || isAccountingEmployee(user) || workingCapitalSystem.isManagementMember(user) || isTreasuryMember(user)
                 || findUnitResponsible(user.getPerson(), Money.ZERO) != null) {
             return true;
         }
         return isRequester(user);
     }
 
     public User getRequester() {
         final WorkingCapitalInitialization workingCapitalInitialization =
                 Collections.min(getWorkingCapitalInitializationsSet(),
                         WorkingCapitalInitialization.COMPARATOR_BY_REQUEST_CREATION);
         return workingCapitalInitialization.getRequestor().getUser();
     }
 
     public boolean isRequester(final User user) {
         for (final WorkingCapitalInitialization workingCapitalInitialization : getWorkingCapitalInitializationsSet()) {
             if (user == workingCapitalInitialization.getRequestor().getUser()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isCanceledOrRejected() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.isCanceledOrRejected();
     }
 
     public WorkingCapitalInitialization getWorkingCapitalInitialization() {
         return Collections
                 .max(getWorkingCapitalInitializationsSet(), WorkingCapitalInitialization.COMPARATOR_BY_REQUEST_CREATION);
     }
 
     public boolean hasAnyPendingWorkingCapitalRequests() {
         for (final WorkingCapitalRequest workingCapitalRequest : getWorkingCapitalRequestsSet()) {
             if (!workingCapitalRequest.isRequestProcessedByTreasury()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isTreasuryMember(final User user) {
         final AccountingUnit accountingUnit = getAccountingUnit();
         if (accountingUnit != null) {
             return accountingUnit.getTreasuryMembersSet().contains(user.getExpenditurePerson());
         }
         final Unit unit = getUnit();
         return unit.isTreasuryMember(user.getExpenditurePerson());
     }
 
     public Money getAvailableCapital() {
         Money result = Money.ZERO;
         for (final WorkingCapitalRequest workingCapitalRequest : getWorkingCapitalRequestsSet()) {
             if (workingCapitalRequest.isRequestProcessedByTreasury()) {
                 result = result.add(workingCapitalRequest.getRequestedValue());
             }
         }
         return result;
     }
 
     public WorkingCapitalTransaction getLastTransaction() {
         final Set<WorkingCapitalTransaction> workingCapitalTransactionsSet = getWorkingCapitalTransactionsSet();
         return workingCapitalTransactionsSet.isEmpty() ? null : Collections.max(workingCapitalTransactionsSet,
                 WorkingCapitalTransaction.COMPARATOR_BY_NUMBER);
     }
 
     public SortedSet<WorkingCapitalTransaction> getSortedWorkingCapitalTransactions() {
         SortedSet<WorkingCapitalTransaction> result =
                 new TreeSet<WorkingCapitalTransaction>(WorkingCapitalTransaction.COMPARATOR_BY_NUMBER);
         result.addAll(getWorkingCapitalTransactionsSet());
         return result;
     }
 
     public Money getBalance() {
         WorkingCapitalTransaction workingCapitalTransaction = getLastTransaction();
         return workingCapitalTransaction == null ? Money.ZERO : workingCapitalTransaction.getBalance();
     }
 
     public boolean areAllAcquisitionsApproved() {
         for (WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             WorkingCapitalTransaction workingCapitalTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
            if (!workingCapitalTransaction.isCanceledOrRejected() && !workingCapitalTransaction.isApproved()) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean hasAnyExceptionalAcquisitionPendingManagementApproval() {
         for (WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             WorkingCapitalTransaction workingCapitalTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
             if (workingCapitalTransaction.isExceptionalAcquisition()) {
                 ExceptionalWorkingCapitalAcquisitionTransaction exceptionalTransaction =
                         (ExceptionalWorkingCapitalAcquisitionTransaction) workingCapitalTransaction;
                 if (exceptionalTransaction.isPendingManagementApproval()) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean hasAcquisitionPendingApproval() {
         for (WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             WorkingCapitalTransaction workingCapitalTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
             if (workingCapitalTransaction.isPendingApproval()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasAcquisitionPendingApproval(User user) {
         Money valueForAuthorization = Money.ZERO;
         return hasAcquisitionPendingApproval() && findUnitResponsible(user.getPerson(), valueForAuthorization) != null;
     }
 
     public boolean hasAcquisitionPendingDirectApproval(User user) {
         Money valueForAuthorization = Money.ZERO;
         return hasAcquisitionPendingApproval() && findDirectUnitResponsible(user.getPerson(), valueForAuthorization) != null;
     }
 
     public boolean hasAcquisitionPendingVerification() {
         for (WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             WorkingCapitalTransaction workingCapitalTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
             if (workingCapitalTransaction.isPendingVerification()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasAcquisitionPendingSubmission() {
         for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             final WorkingCapitalTransaction workingCapitalTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
             if (workingCapitalTransaction.isPendingSubmission()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasAcquisitionPendingVerification(final User user) {
         return hasAcquisitionPendingVerification() && isAccountingEmployee(user);
     }
 
     public boolean hasAcquisitionPendingDirectVerification(final User user) {
         return hasAcquisitionPendingVerification() && isDirectAccountingEmployee(user);
     }
 
     private boolean hasVerifiedAcquisition() {
         for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             final WorkingCapitalTransaction workingCapitalTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
             if (workingCapitalTransaction.isVerified()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasVerifiedAcquisition(User user) {
         return hasVerifiedAcquisition() && isAccountingEmployee(user);
     }
 
     public boolean hasAllPaymentsRequested() {
         if (!hasAnyWorkingCapitalTransactions()) {
             return false;
         }
         for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
             if (workingCapitalTransaction.isAcquisition() && !workingCapitalTransaction.isPaymentRequested()) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean isMovementResponsible(final User user) {
         return hasMovementResponsible() && getMovementResponsible().getUser() == user;
     }
 
     public boolean hasApprovedAndUnSubmittedAcquisitions() {
         for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
             if (workingCapitalAcquisitionTransaction.isApproved()
                     && workingCapitalAcquisition.getSubmitedForVerification() == null) {
                 return true;
             }
         }
         return false;
     }
 
     public void submitAcquisitionsForValidation() {
         final DateTime now = new DateTime();
         for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction =
                     workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
             if (workingCapitalAcquisitionTransaction.isApproved()
                     && workingCapitalAcquisition.getSubmitedForVerification() == null) {
                 workingCapitalAcquisition.setSubmitedForVerification(now);
             }
         }
     }
 
     public void unsubmitAcquisitionsForValidation() {
         for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
             workingCapitalAcquisition.setSubmitedForVerification(null);
         }
     }
 
     public boolean canRequestCapital() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && !isCanceledOrRejected()
                 && workingCapitalInitialization.getLastSubmission() == null && workingCapitalInitialization.isAuthorized()
                 && !hasAnyPendingWorkingCapitalRequests() && hasCapitalPendingRequest();
     }
 
     public boolean canRevertTermination() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && !isCanceledOrRejected()
                 && workingCapitalInitialization.getLastSubmission() != null
                 && workingCapitalInitialization.getRefundRequested() == null && workingCapitalInitialization.isAuthorized()
                 && !hasAnyPendingWorkingCapitalRequests() && !hasAnyAquisitionPendingVerification();
     }
 
     public boolean canRequestCapitalRefund() {
         return canRevertTermination(); // && hasCapitalToRefund();
     }
 
     private boolean hasAnyAquisitionPendingVerification() {
         for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
             if (workingCapitalTransaction.isAcquisition() && !workingCapitalTransaction.isCanceledOrRejected()
                     && !workingCapitalTransaction.isVerified()) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean hasCapitalToRefund() {
         final WorkingCapitalTransaction lastWorkingCapitalTransaction = getLastTransaction();
         return lastWorkingCapitalTransaction != null && lastWorkingCapitalTransaction.getBalance().isPositive();
     }
 
     private boolean hasCapitalPendingRequest() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         final WorkingCapitalTransaction lastWorkingCapitalTransaction = getLastTransaction();
         if ((lastWorkingCapitalTransaction == null && workingCapitalInitialization.getAuthorizedAnualValue().isPositive())
                 || (lastWorkingCapitalTransaction != null && lastWorkingCapitalTransaction.getDebt().isLessThan(
                         workingCapitalInitialization.getAuthorizedAnualValue()))) {
             return true;
         }
 
         /*for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
             if (workingCapitalTransaction.isAcquisition() && !workingCapitalTransaction.isCanceledOrRejected()) {
         	final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction = (WorkingCapitalAcquisitionTransaction) workingCapitalTransaction;
         	if (!workingCapitalAcquisitionTransaction.isVerified()) {
         	    return false;
         	}
             }
         }
         return true;
         */
         boolean hasSomeAcquisition = false;
 
         DateTime lastPayment = null;
         for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
             if (workingCapitalTransaction.isPayment()
                     && (lastPayment == null || lastPayment.isBefore(workingCapitalTransaction.getTransationInstant()))) {
                 lastPayment = workingCapitalTransaction.getTransationInstant();
             }
         }
 
         for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
             if (workingCapitalTransaction.isAcquisition()
                     && workingCapitalTransaction.getTransationInstant().isAfter(lastPayment)) {
                 final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction =
                         (WorkingCapitalAcquisitionTransaction) workingCapitalTransaction;
                 final WorkingCapitalAcquisition workingCapitalAcquisition =
                         workingCapitalAcquisitionTransaction.getWorkingCapitalAcquisition();
                 if (workingCapitalAcquisition.getSubmitedForVerification() != null) {
                     if (workingCapitalTransaction.isVerified() && !workingCapitalAcquisition.isCanceledOrRejected()) {
                         hasSomeAcquisition = true;
                     }
                 }
             }
         }
         return hasSomeAcquisition && lastWorkingCapitalTransaction.getAccumulatedValue().isPositive();
     }
 
     public boolean hasWorkingCapitalRequestPendingTreasuryProcessing() {
         for (final WorkingCapitalRequest workingCapitalRequest : getWorkingCapitalRequestsSet()) {
             if (workingCapitalRequest.getProcessedByTreasury() == null) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isPendingAcceptResponsability() {
         for (final WorkingCapitalInitialization workingCapitalInitialization : getWorkingCapitalInitializationsSet()) {
             if (!workingCapitalInitialization.isCanceledOrRejected()
                     && workingCapitalInitialization.getAcceptedResponsability() == null) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isPendingAcceptResponsability(User user) {
         return isMovementResponsible(user) && isPendingAcceptResponsability();
     }
 
     public boolean canRequestValue(final Money requestedValue) {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         if (workingCapitalInitialization != null && !workingCapitalInitialization.isCanceledOrRejected()
                 && workingCapitalInitialization.isAuthorized()) {
             final Money maxAuthorizedAnualValue = workingCapitalInitialization.getMaxAuthorizedAnualValue();
             final Money allocatedValue = calculateAllocatedValue();
             if (maxAuthorizedAnualValue.isGreaterThanOrEqual(allocatedValue.add(requestedValue))) {
                 return true;
             }
         }
         return false;
     }
 
     public Money calculateAllocateableValue() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         final Money maxAuthorizedAnualValue = workingCapitalInitialization.getMaxAuthorizedAnualValue();
         final Money allocatedValue = calculateAllocatedValue();
         return maxAuthorizedAnualValue.subtract(allocatedValue);
     }
 
     private Money calculateAllocatedValue() {
         Money result = Money.ZERO;
         for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
             if (!workingCapitalTransaction.isCanceledOrRejected()) {
                 if (workingCapitalTransaction.isPayment()) {
                     final WorkingCapitalPayment workingCapitalPayment = (WorkingCapitalPayment) workingCapitalTransaction;
                     final WorkingCapitalRequest workingCapitalRequest = workingCapitalPayment.getWorkingCapitalRequest();
                     final Money requestedValue = workingCapitalRequest.getRequestedValue();
                     result = result.add(requestedValue);
                 } else if (workingCapitalTransaction.isRefund()) {
                     final WorkingCapitalRefund workingCapitalRefund = (WorkingCapitalRefund) workingCapitalTransaction;
                     final Money refundedValue = workingCapitalRefund.getRefundedValue();
                     result = result.subtract(refundedValue);
                 }
             }
         }
         return result;
     }
 
     public boolean isAccountingResponsible(final User user) {
         final AccountingUnit accountingUnit = getAccountingUnit();
         final pt.ist.expenditureTrackingSystem.domain.organization.Person person = user.getExpenditurePerson();
         if (accountingUnit != null) {
             final Unit unit = getUnit();
             if (unit instanceof Project || unit instanceof SubProject) {
                 return accountingUnit.getResponsibleProjectAccountantsSet().contains(person);
             }
             return accountingUnit.getResponsiblePeopleSet().contains(person);
         }
         return false;
     }
 
     public boolean isAccountingEmployee(final User user) {
         final Unit unit = getUnit();
         final AccountingUnit accountingUnit = getAccountingUnit();
         final pt.ist.expenditureTrackingSystem.domain.organization.Person person = user.getExpenditurePerson();
         if (unit != null && accountingUnit != null && person != null) {
             if (unit instanceof Project || unit instanceof SubProject) {
                 return accountingUnit.getProjectAccountantsSet().contains(person);
             }
             return accountingUnit.getPeopleSet().contains(person);
         }
         return false;
     }
 
     public boolean isAnyAccountingEmployee(final User user) {
         final Unit unit = getUnit();
         final AccountingUnit accountingUnit = getAccountingUnit();
         final pt.ist.expenditureTrackingSystem.domain.organization.Person person = user.getExpenditurePerson();
         return unit != null && accountingUnit != null && person != null
                 && (accountingUnit.getProjectAccountantsSet().contains(person) || accountingUnit.getPeopleSet().contains(person));
     }
 
     public boolean isDirectAccountingEmployee(final User user) {
         final Unit unit = getUnit();
         final AccountingUnit accountingUnit = getAccountingUnit();
         final pt.ist.expenditureTrackingSystem.domain.organization.Person person = user.getExpenditurePerson();
         if (unit != null && accountingUnit != null && person != null) {
             if (unit instanceof Project || unit instanceof SubProject) {
                 return accountingUnit.getProjectAccountantsSet().contains(person)
                         && (!unit.hasSomeAccountManager() || unit.isAccountManager(person));
             }
             return accountingUnit.getPeopleSet().contains(person);
         }
         return false;
     }
 
     public Money getPossibaySpent() {
         Money result = Money.ZERO;
         for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
             if (workingCapitalTransaction.isPayment()) {
                 result = result.add(workingCapitalTransaction.getValue());
             }
             if (workingCapitalTransaction.isRefund()) {
                 result = result.subtract(workingCapitalTransaction.getValue());
             }
         }
         return result;
     }
 
     public boolean isResponsibleFor(final User user) {
         final Money valueForAuthorization = Money.ZERO;
         final Authorization authorization = findUnitResponsible(user.getPerson(), valueForAuthorization);
         return authorization != null;
     }
 
     public AccountingUnit getAccountingUnit() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization == null ? getUnit().getAccountingUnit() : workingCapitalInitialization
                 .getAccountingUnit();
     }
 
     public boolean canChangeAccountingUnit() {
         final Unit unit = getUnit();
         final AccountingUnit accountingUnit = unit == null ? null : unit.getAccountingUnit();
         return accountingUnit == null ? false : !accountingUnit.getName().equals("10");
     }
 
     public boolean canTerminateFund() {
         final WorkingCapitalTransaction workingCapitalTransaction = getLastTransaction();
         return workingCapitalTransaction != null && !isTerminated()
                 && workingCapitalTransaction.getAccumulatedValue().equals(Money.ZERO);
     }
 
     private boolean isTerminated() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization != null && workingCapitalInitialization.getLastSubmission() != null;
     }
 
     public boolean isPendingRefund() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization.getRefundRequested() != null && getBalance().isPositive();
     }
 
     public boolean isRefunded() {
         final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
         return workingCapitalInitialization.getRefundRequested() != null && getBalance().isZero();
     }
 
     public PresentableProcessState getPresentableAcquisitionProcessState() {
         if (isCanceledOrRejected()) {
             return WorkingCapitalProcessState.CANCELED;
         }
         if (isPendingAcceptResponsability()) {
             return WorkingCapitalProcessState.PENDING_ACCEPT_RESPONSIBILITY;
         }
         if (isPendingAproval()) {
             return WorkingCapitalProcessState.PENDING_APPROVAL;
         }
         if (isPendingVerification()) {
             return WorkingCapitalProcessState.PENDING_VERIFICATION;
         }
         if (isPendingFundAllocation()) {
             return WorkingCapitalProcessState.PENDING_FUND_ALLOCATION;
         }
         if (isPendingAuthorization()) {
             return WorkingCapitalProcessState.PENDING_AUTHORIZATION;
         }
         if (isPendingPayment()) {
             return WorkingCapitalProcessState.PENDING_PAYMENT;
         }
         if (isPendingRefund()) {
             return WorkingCapitalProcessState.SENT_FOR_FUND_REFUND;
         }
         if (isRefunded()) {
             return WorkingCapitalProcessState.TERMINATED;
         }
         if (isTerminated()) {
             return WorkingCapitalProcessState.SENT_FOR_TERMINATION;
         }
         return WorkingCapitalProcessState.WORKING_CAPITAL_AVAILABLE;
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
         return getWorkingCapitalSystem() == WorkingCapitalSystem.getInstanceForCurrentHost();
     }
 
     @Deprecated
     public java.util.Set<module.workingCapital.domain.WorkingCapitalInitialization> getWorkingCapitalInitializations() {
         return getWorkingCapitalInitializationsSet();
     }
 
     @Deprecated
     public boolean hasAnyWorkingCapitalInitializations() {
         return !getWorkingCapitalInitializationsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasMovementResponsible() {
         return getMovementResponsible() != null;
     }
 
     @Deprecated
     public java.util.Set<module.workingCapital.domain.WorkingCapitalTransaction> getWorkingCapitalTransactions() {
         return getWorkingCapitalTransactionsSet();
     }
 
     @Deprecated
     public boolean hasAnyWorkingCapitalTransactions() {
         return !getWorkingCapitalTransactionsSet().isEmpty();
     }
 
     @Deprecated
     public boolean hasUnit() {
         return getUnit() != null;
     }
 
     @Deprecated
     public boolean hasWorkingCapitalProcess() {
         return getWorkingCapitalProcess() != null;
     }
 
     @Deprecated
     public boolean hasWorkingCapitalYear() {
         return getWorkingCapitalYear() != null;
     }
 
     @Deprecated
     public boolean hasWorkingCapitalSystem() {
         return getWorkingCapitalSystem() != null;
     }
 
     @Deprecated
     public java.util.Set<module.workingCapital.domain.WorkingCapitalRequest> getWorkingCapitalRequests() {
         return getWorkingCapitalRequestsSet();
     }
 
     @Deprecated
     public boolean hasAnyWorkingCapitalRequests() {
         return !getWorkingCapitalRequestsSet().isEmpty();
     }
 
     @Deprecated
     public java.util.Set<module.workingCapital.domain.WorkingCapitalAcquisition> getWorkingCapitalAcquisitions() {
         return getWorkingCapitalAcquisitionsSet();
     }
 
     @Deprecated
     public boolean hasAnyWorkingCapitalAcquisitions() {
         return !getWorkingCapitalAcquisitionsSet().isEmpty();
     }
 
 }
