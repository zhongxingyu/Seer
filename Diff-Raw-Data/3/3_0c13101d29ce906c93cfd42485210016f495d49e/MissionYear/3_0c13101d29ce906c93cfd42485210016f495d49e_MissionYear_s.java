 /*
  * @(#)MissionYear.java
  *
  * Copyright 2010 Instituto Superior Tecnico
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
 package module.mission.domain;
 
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.mission.domain.util.MissionAuthorizationMap;
 import module.mission.domain.util.MissionPendingProcessCounter;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import module.workflow.domain.WorkflowProcess;
 import module.workflow.widgets.ProcessListWidget;
 
 import org.joda.time.DateTime;
 
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.domain.VirtualHost;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.services.Service;
 
 /**
  * 
  * @author João Neves
  * @author Luis Cruz
  * 
  */
 public class MissionYear extends MissionYear_Base {
 
     static {
         ProcessListWidget.register(new MissionPendingProcessCounter());
     }
 
     public static final Comparator<MissionYear> COMPARATOR_BY_YEAR = new Comparator<MissionYear>() {
         @Override
         public int compare(MissionYear o1, MissionYear o2) {
             final Integer year1 = o1.getYear();
             final Integer year2 = o2.getYear();
             return year1.compareTo(year2);
         }
     };
 
     public static Integer getBiggestYearCounter() {
         int biggestCounter = 0;
         for (MissionYear year : MissionSystem.getInstance().getMissionYearSet()) {
             if (year.getCounter() > biggestCounter) {
                 biggestCounter = year.getCounter();
             }
         }
         return biggestCounter;
     }
 
     private MissionYear(final int year) {
         super();
         if (findMissionByYearAux(year) != null) {
             throw new Error("There can only be one! (MissionYear object for each year)");
         }
         final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
         if (year != currentYear && year != currentYear + 1) {
             throw new Error("There is absolutly no need to create a year other that the current and the next!");
         }
         setMissionSystem(MissionSystem.getInstance());
         setYear(new Integer(year));
         setCounter(Integer.valueOf(0));
     }
 
     private static MissionYear findMissionByYearAux(final int year) {
         final MissionSystem missionSystem = MissionSystem.getInstance();
         for (final MissionYear missionYear : missionSystem.getMissionYearSet()) {
             if (missionYear.getYear().intValue() == year) {
                 return missionYear;
             }
         }
         return null;
     }
 
     public static MissionYear findMissionYear(final int year) {
         return findMissionByYearAux(year);
     }
 
     @Service
     public static MissionYear findOrCreateMissionYear(final int year) {
         final MissionYear missionYear = findMissionByYearAux(year);
         return missionYear == null ? new MissionYear(year) : missionYear;
     }
 
     public Integer nextNumber() {
         return getAndIncreaseNextNumber();
     }
 
     private Integer getAndIncreaseNextNumber() {
         setCounter(getCounter().intValue() + 1);
         return getCounter();
     }
 
     public static MissionYear getCurrentYear() {
         final int year = new DateTime().getYear();
         return findOrCreateMissionYear(year);
     }
 
     private abstract class MissionProcessSearch {
 
         private final SortedSet<MissionProcess> result;
 
         abstract boolean shouldAdd(final MissionProcess missionProcess, final User user);
 
         private MissionProcessSearch() {
             result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
         }
 
         private MissionProcessSearch(final SortedSet<MissionProcess> result) {
             this.result = result;
         }
 
         SortedSet<MissionProcess> search() {
             final User user = UserView.getCurrentUser();
             for (final MissionProcess missionProcess : getMissionProcessSet()) {
                 if (shouldAdd(missionProcess, user)) {
                     result.add(missionProcess);
                 }
             }
             return result;
         }
 
     }
 
     private class PendingAprovalSearch extends MissionProcessSearch {
 
         private PendingAprovalSearch() {
         }
 
         private PendingAprovalSearch(final SortedSet<MissionProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final MissionProcess missionProcess, final User user) {
             return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
                     && !missionProcess.isUnderConstruction() && !missionProcess.getIsCanceled()
                     && missionProcess.isPendingApprovalBy(user);
         }
 
     }
 
     private class PendingVehicleAuthorizationSearch extends MissionProcessSearch {
 
         private PendingVehicleAuthorizationSearch() {
         }
 
         private PendingVehicleAuthorizationSearch(final SortedSet<MissionProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final MissionProcess missionProcess, final User user) {
             if (missionProcess.isCanceled()) {
                 return false;
             }
             if (!missionProcess.isApprovedByResponsible()) {
                 return false;
             }
             if (missionProcess.hasCurrentOwner() && !missionProcess.isTakenByCurrentUser()) {
                 return false;
             }
 
             return missionProcess.getMission().isVehicleAuthorizationNeeded()
                     && !missionProcess.getMission().areAllVehicleItemsAuthorized();
         }
     }
 
     private class PendingAuthorizationSearch extends MissionProcessSearch {
 
         private PendingAuthorizationSearch() {
         }
 
         private PendingAuthorizationSearch(final SortedSet<MissionProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final MissionProcess missionProcess, final User user) {
             return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
                     && missionProcess.isApproved()
                     && !missionProcess.getIsCanceled()
                     && ((missionProcess.isPendingParticipantAuthorisationBy(user) && (!missionProcess.getMission()
                             .hasAnyFinancer() || (missionProcess.hasAllAllocatedFunds() && missionProcess
                             .hasAllCommitmentNumbers()))) || (//missionProcess.areAllParticipantsAuthorizedForPhaseOne()
                     missionProcess.areAllParticipantsAuthorized() && missionProcess.hasAllAllocatedFunds() && missionProcess
                                 .isPendingDirectAuthorizationBy(user)));
         }
 
     }
 
     private class PendingFundAllocationSearch extends MissionProcessSearch {
 
         private PendingFundAllocationSearch() {
         }
 
         private PendingFundAllocationSearch(final SortedSet<MissionProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final MissionProcess missionProcess, final User user) {
             return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
                     && (isPendingFundAllocation(missionProcess, user) || isPendingFundUnAllocation(missionProcess, user));
         }
 
         private boolean isPendingFundAllocation(MissionProcess missionProcess, User user) {
             return missionProcess.isApproved()
                     && !missionProcess.getIsCanceled()
                     && (((!missionProcess.hasAnyProjectFinancer() || missionProcess.hasAllAllocatedProjectFunds())
                             && !missionProcess.hasAllAllocatedFunds() && missionProcess.canAllocateFund())
                             || (!missionProcess.hasAllAllocatedProjectFunds() && missionProcess.canAllocateProjectFund()) || (missionProcess
                             .getMission().hasAnyFinancer()
                             && missionProcess.hasAllAllocatedFunds()
                             && !missionProcess.hasAllCommitmentNumbers() && missionProcess.isAccountingEmployee(user
                             .getExpenditurePerson())));
         }
 
         private boolean isPendingFundUnAllocation(final MissionProcess missionProcess, final User user) {
             return missionProcess.getIsCanceled().booleanValue()
                     && ((missionProcess.hasAnyAllocatedFunds() && missionProcess
                             .isAccountingEmployee(user.getExpenditurePerson())) || (missionProcess.hasAnyAllocatedProjectFunds())
                             && missionProcess.isProjectAccountingEmployee(user.getExpenditurePerson()));
         }
 
     }
 
     private class PendingProcessingPersonelInformationSearch extends MissionProcessSearch {
 
         private PendingProcessingPersonelInformationSearch() {
         }
 
         private PendingProcessingPersonelInformationSearch(final SortedSet<MissionProcess> result) {
             super(result);
         }
 
         @Override
         boolean shouldAdd(final MissionProcess missionProcess, final User user) {
             return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
                     && (missionProcess.hasAnyCurrentQueues() && missionProcess.isCurrentUserAbleToAccessAnyQueues()
                             && (missionProcess.isAuthorized() || missionProcess.hasNoItemsAndParticipantesAreAuthorized()) && missionProcess
                                 .areAllParticipantsAuthorized()) || missionProcess.isReadyForMissionTermination(user)
                     || (missionProcess.isTerminated() && !missionProcess.isArchived() && missionProcess.canArchiveMission());
         }
 
     }
 
     public SortedSet<MissionProcess> getPendingAproval() {
         return new PendingAprovalSearch().search();
     }
 
     public SortedSet<MissionProcess> getPendingAproval(final SortedSet<MissionProcess> result) {
         return new PendingAprovalSearch(result).search();
     }
 
     public SortedSet<MissionProcess> getPendingVehicleAuthorization() {
         return new PendingVehicleAuthorizationSearch().search();
     }
 
     public SortedSet<MissionProcess> getPendingVehicleAuthorization(final SortedSet<MissionProcess> result) {
         return new PendingVehicleAuthorizationSearch(result).search();
     }
 
     public SortedSet<MissionProcess> getPendingAuthorization() {
         return new PendingAuthorizationSearch().search();
     }
 
     public SortedSet<MissionProcess> getPendingAuthorization(final SortedSet<MissionProcess> result) {
         return new PendingAuthorizationSearch(result).search();
     }
 
     public SortedSet<MissionProcess> getPendingFundAllocation() {
         try {
             return new PendingFundAllocationSearch().search();
         } catch (Throwable t) {
             t.printStackTrace();
             throw new Error(t);
         }
     }
 
     public SortedSet<MissionProcess> getPendingFundAllocation(final SortedSet<MissionProcess> result) {
         try {
             return new PendingFundAllocationSearch(result).search();
         } catch (Throwable t) {
             t.printStackTrace();
             throw new Error(t);
         }
     }
 
     public SortedSet<MissionProcess> getDirectPendingFundAllocation() {
         try {
             return new MissionProcessSearch() {
                 @Override
                 boolean shouldAdd(final MissionProcess missionProcess, final User user) {
                     return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
                             && (isPendingFundAllocation(missionProcess, user) || isPendingFundUnAllocation(missionProcess, user));
                 }
 
                 private boolean isPendingFundAllocation(MissionProcess missionProcess, User user) {
                     return missionProcess.isApproved()
                             && !missionProcess.getIsCanceled()
                             && (((!missionProcess.hasAnyProjectFinancer() || missionProcess.hasAllAllocatedProjectFunds())
                                     && !missionProcess.hasAllAllocatedFunds() && missionProcess.canAllocateFund()) || (!missionProcess
                                     .hasAllAllocatedProjectFunds() && missionProcess
                                     .isDirectResponsibleForPendingProjectFundAllocation()));
                 }
 
                 private boolean isPendingFundUnAllocation(final MissionProcess missionProcess, final User user) {
                     return missionProcess.getIsCanceled().booleanValue()
                             && ((missionProcess.hasAnyAllocatedFunds() && missionProcess.isAccountingEmployee(user
                                     .getExpenditurePerson())) || (missionProcess.hasAnyAllocatedProjectFunds())
                                     && missionProcess.isDirectProjectAccountingEmployee(user.getExpenditurePerson()));
                 }
             }.search();
         } catch (Throwable t) {
             t.printStackTrace();
             throw new Error(t);
         }
     }
 
     public SortedSet<MissionProcess> getPendingProcessingPersonelInformation() {
         return new PendingProcessingPersonelInformationSearch().search();
     }
 
     public SortedSet<MissionProcess> getPendingProcessingPersonelInformation(final SortedSet<MissionProcess> result) {
         return new PendingProcessingPersonelInformationSearch(result).search();
     }
 
     public SortedSet<MissionProcess> getPendingDirectProcessingPersonelInformation() {
         return new MissionProcessSearch() {
             @Override
             boolean shouldAdd(final MissionProcess missionProcess, final User user) {
                 return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
                         && (missionProcess.hasAnyCurrentQueues() && missionProcess.isCurrentUserAbleToAccessAnyQueues()
                                 && (missionProcess.isAuthorized() || missionProcess.hasNoItemsAndParticipantesAreAuthorized()) && missionProcess
                                     .areAllParticipantsAuthorized())
                         || missionProcess.isReadyForMissionTermination(user)
                         || (missionProcess.isTerminated() && !missionProcess.isArchived() && missionProcess
                                 .canArchiveMissionDirect());
             }
         }.search();
     }
 
     public SortedSet<MissionProcess> getRequested() {
         final User user = UserView.getCurrentUser();
         final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
         final Person person = user.getPerson();
         if (person != null) {
             for (final Mission mission : person.getRequestedMissionsSet()) {
                 final MissionProcess missionProcess = mission.getMissionProcess();
                 if (missionProcess.getMissionYear() == this && !missionProcess.getIsCanceled() && !missionProcess.isArchived()) {
                     result.add(missionProcess);
                 }
             }
         }
         return result;
     }
 
     private boolean hasValidAuthorization(final Set<Authorization> authorizations) {
         for (final Authorization authorization : authorizations) {
             if (authorization.isValid()) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean isDirectlyResponsibleFor(final Set<Authorization> authorizations,
             final pt.ist.expenditureTrackingSystem.domain.organization.Unit unit) {
         final Set<Authorization> authorizationsFromUnit = unit.getAuthorizationsSet();
         if (intersect(authorizations, authorizationsFromUnit)) {
             return true;
         }
         if (hasValidAuthorization(authorizationsFromUnit)) {
             return false;
         }
         final pt.ist.expenditureTrackingSystem.domain.organization.Unit parentUnit = unit.getParentUnit();
         return parentUnit != null && isDirectlyResponsibleFor(authorizations, parentUnit);
     }
 
     private boolean intersect(final Set<Authorization> authorizations, final Set<Authorization> authorizationsFromUnit) {
         for (final Authorization authorization : authorizationsFromUnit) {
             if (authorizations.contains(authorization)) {
                 return true;
             }
         }
         return false;
     }
 
     private Set<Authorization> getAuthorizations(final User user) {
         final Set<Authorization> authorizations = new HashSet<Authorization>();
         for (final Authorization authorization : user.getExpenditurePerson().getAuthorizationsSet()) {
             if (authorization.isValid()) {
                 authorizations.add(authorization);
             }
         }
         return authorizations;
     }
 
     public SortedSet<MissionProcess> getAprovalResponsible() {
         final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
         final User user = UserView.getCurrentUser();
         if (user.hasExpenditurePerson()) {
             final Set<Authorization> authorizations = getAuthorizations(user);
             for (final MissionProcess missionProcess : getMissionProcessSet()) {
                 if (!missionProcess.getIsCanceled() && !missionProcess.isArchived()) {
                     final Mission mission = missionProcess.getMission();
                     final Party missionResponsible = mission.getMissionResponsible();
                     if (missionResponsible != null) {
                         if (missionResponsible.isPerson()) {
                             if (missionResponsible == user.getPerson()) {
                                 result.add(missionProcess);
                             }
                         } else if (missionResponsible.isUnit() && !authorizations.isEmpty()) {
                             final pt.ist.expenditureTrackingSystem.domain.organization.Unit unit =
                                     getExpenditureUnit(mission, (module.organization.domain.Unit) missionResponsible);
                             if (unit != null && isDirectlyResponsibleFor(authorizations, unit)) {
                                 result.add(missionProcess);
                             }
                         }
                     }
                 }
             }
         }
         return result;
     }
 
     private Unit getExpenditureUnit(final Mission mission, final module.organization.domain.Unit unit) {
         return unit.hasExpenditureUnit() ? unit.getExpenditureUnit() : getExpenditureUnit(mission, unit.getParentUnits()
                 .iterator().next());
     }
 
     public SortedSet<MissionProcess> getParticipate() {
         final User user = UserView.getCurrentUser();
         final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
         final Person person = user.getPerson();
         if (person != null) {
             for (final Mission mission : person.getMissionsSet()) {
                 final MissionProcess missionProcess = mission.getMissionProcess();
                 if (missionProcess.getMissionYear() == this && !missionProcess.getIsCanceled() && !missionProcess.isArchived()) {
                     result.add(missionProcess);
                 }
             }
         }
         return result;
     }
 
     public MissionAuthorizationMap getMissionAuthorizationMap() {
         return new MissionAuthorizationMap(this);
     }
 
     public SortedSet<MissionProcess> getTaken() {
         final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
         return getTaken(result);
     }
 
     public SortedSet<MissionProcess> getTaken(final SortedSet<MissionProcess> result) {
         final User user = UserView.getCurrentUser();
         for (final WorkflowProcess workflowProcess : user.getUserProcessesSet()) {
             if (workflowProcess instanceof MissionProcess) {
                 final MissionProcess missionProcess = (MissionProcess) workflowProcess;
                 if (missionProcess.getMissionYear() == this && !missionProcess.getIsCanceled()) {
                     result.add(missionProcess);
                 }
             }
         }
         return result;
     }
 
     public void delete() {
         if (hasAnyMissionProcess()) {
             throw new Error("cannot.delete.because.mission.process.exist");
         }
         removeMissionSystem();
         deleteDomainObject();
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
         return getMissionSystem() == VirtualHost.getVirtualHostForThread().getMissionSystem();
     }
 
 }
