 /*
  * @(#)MissionStageView.java
  *
  * Copyright 2011 Instituto Superior Tecnico
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
 package module.mission.domain.util;
 
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import module.mission.domain.Mission;
 import module.mission.domain.MissionProcess;
 
 /**
  * 
  * @author João Neves
  * @author Luis Cruz
  * 
  */
 public class MissionStageView {
 
     private final MissionProcess missionProcess;
 
     public MissionStageView(final MissionProcess missionProcess) {
         this.missionProcess = missionProcess;
     }
 
     public SortedMap<MissionStage, MissionStageState> getMissionStageStates() {
         final SortedMap<MissionStage, MissionStageState> result = new TreeMap<MissionStage, MissionStageState>();
         final Mission mission = missionProcess.getMission();
 
         result.put(MissionStage.PROCESS_APPROVAL, getApprovalState());
 
         if (mission.hasAnyMissionItems()) {
             if (mission.hasAnyVehicleItems()) {
                 result.put(MissionStage.VEHICLE_APPROVAL, getVehicleApprovalState());
             }
 
             result.put(MissionStage.FUND_ALLOCATION, getFundAllocationState());
         }
 
         result.put(MissionStage.PARTICIPATION_AUTHORIZATION, getParticipationAuthorizationState());
 
         if (mission.hasAnyMissionItems()) {
             result.put(MissionStage.EXPENSE_AUTHORIZATION, getExpenseAuthorizationState());
         }
 
         result.put(MissionStage.PERSONEL_INFORMATION_PROCESSING, getPersonelInformationProcessingState());
 
         result.put(MissionStage.ARCHIVED, getArchivedState());
 
         return result;
     }
 
     protected MissionStageState getApprovalState() {
         return missionProcess.isApprovedByResponsible() ? MissionStageState.COMPLETED : getApprovalStateUnderConstruction();
     }
 
     protected MissionStageState getApprovalStateUnderConstruction() {
         return !missionProcess.isUnderConstruction() && !missionProcess.getIsCanceled() ? MissionStageState.UNDER_WAY : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     protected MissionStageState getVehicleApprovalState() {
         if (getApprovalState() != MissionStageState.COMPLETED) {
             return MissionStageState.NOT_YET_UNDER_WAY;
         }
         if (getParticipationAuthorizationState() == MissionStageState.COMPLETED) {
             return MissionStageState.COMPLETED;
         }
         if (missionProcess.getMission().areAllVehicleItemsAuthorized()) {
             return MissionStageState.COMPLETED;
         }
         return MissionStageState.UNDER_WAY;
     }
 
     protected MissionStageState getFundAllocationState() {
         if (getApprovalState() != MissionStageState.COMPLETED) {
             return MissionStageState.NOT_YET_UNDER_WAY;
         }
 
         if (getVehicleApprovalState() == MissionStageState.UNDER_WAY) {
             return MissionStageState.NOT_YET_UNDER_WAY;
         }
 
         if (missionProcess.getIsCanceled().booleanValue()) {
             return missionProcess.hasAnyAllocatedFunds() || missionProcess.hasAnyAllocatedProjectFunds() ? MissionStageState.UNDER_WAY : MissionStageState.NOT_YET_UNDER_WAY;
         }
         return missionProcess.hasAllAllocatedFunds() && missionProcess.hasAllCommitmentNumbers()
                 && (!missionProcess.hasAnyProjectFinancer() || missionProcess.hasAllAllocatedProjectFunds()) ? MissionStageState.COMPLETED : MissionStageState.UNDER_WAY;
     }
 
     protected MissionStageState getParticipationAuthorizationState() {
        return missionProcess.isApprovedByResponsible()
                 && (!missionProcess.getMission().hasAnyFinancer() || (missionProcess.hasAllAllocatedFunds() && missionProcess
                         .hasAllCommitmentNumbers())) ? getParticipationAuthorizationStateForApproved() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     private MissionStageState getParticipationAuthorizationStateForApproved() {
         return missionProcess.isCanceled() ? MissionStageState.NOT_YET_UNDER_WAY : (missionProcess.areAllParticipantsAuthorized() ? MissionStageState.COMPLETED : MissionStageState.UNDER_WAY);
     }
 
     protected MissionStageState getExpenseAuthorizationState() {
        return !missionProcess.isCanceled() && missionProcess.isApprovedByResponsible() && missionProcess.hasAllAllocatedFunds()
                 && missionProcess.areAllParticipantsAuthorized() ? getExpenseAuthorizationStateCompletedOrUnderWay() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     private MissionStageState getExpenseAuthorizationStateCompletedOrUnderWay() {
         return missionProcess.isAuthorized() ? MissionStageState.COMPLETED : MissionStageState.UNDER_WAY;
     }
 
     protected MissionStageState getPersonelInformationProcessingState() {
         return missionProcess.areAllParticipantsAuthorized()
                 && (missionProcess.isAuthorized() || missionProcess.hasNoItemsAndParticipantesAreAuthorized()) ? getPersonelInformationProcessingStateForAuthorizedParticipantes() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     protected MissionStageState getPersonelInformationProcessingStateForAuthorizedParticipantes() {
         return missionProcess.hasAnyCurrentQueues() ? MissionStageState.UNDER_WAY : (missionProcess.getIsCanceled()
                 .booleanValue() ? MissionStageState.NOT_YET_UNDER_WAY : MissionStageState.COMPLETED);
     }
 
     protected MissionStageState getArchivedState() {
         return missionProcess.isTerminated() ? getTerminatedArchivedState() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     protected MissionStageState getTerminatedArchivedState() {
         return missionProcess.isArchived() ? MissionStageState.COMPLETED : MissionStageState.UNDER_WAY;
     }
 
 }
