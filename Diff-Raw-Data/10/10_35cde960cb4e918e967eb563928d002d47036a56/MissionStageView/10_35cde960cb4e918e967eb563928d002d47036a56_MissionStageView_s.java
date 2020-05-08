 package module.mission.domain.util;
 
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import module.mission.domain.Mission;
 import module.mission.domain.MissionProcess;
 
 public class MissionStageView {
 
     private final MissionProcess missionProcess;
 
     public MissionStageView(final MissionProcess missionProcess) {
 	this.missionProcess = missionProcess;;
     }
 
     public SortedMap<MissionStage, MissionStageState> getMissionStageStates() {
 	final SortedMap<MissionStage, MissionStageState> result = new TreeMap<MissionStage, MissionStageState>();
 	final Mission mission = missionProcess.getMission();
 
 	result.put(MissionStage.PROCESS_APPROVAL, getApprovalState());
 
 	if (mission.hasAnyMissionItems()) {
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
 	return missionProcess.isApproved() ? MissionStageState.COMPLETED : getApprovalStateUnderConstruction();
     }
 
     protected MissionStageState getApprovalStateUnderConstruction() {
 	return !missionProcess.isUnderConstruction() && !missionProcess.getIsCanceled() ?
 		MissionStageState.UNDER_WAY : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     protected MissionStageState getFundAllocationState() {
 	return missionProcess.isApproved() ? getFundAllocationStateForApproved() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     protected MissionStageState getFundAllocationStateForApproved() {
 	if (missionProcess.getIsCanceled().booleanValue()) {
 	    return missionProcess.hasAnyAllocatedFunds() || missionProcess.hasAnyAllocatedProjectFunds() ? 
 		    MissionStageState.UNDER_WAY : MissionStageState.NOT_YET_UNDER_WAY;
 	}
 	return missionProcess.hasAllAllocatedFunds() && (!missionProcess.hasAnyProjectFinancer()
 		|| missionProcess.hasAllAllocatedProjectFunds()) ? 
 			MissionStageState.COMPLETED : MissionStageState.UNDER_WAY;
     }
 
     protected MissionStageState getParticipationAuthorizationState() {
	return missionProcess.isApproved() && missionProcess.hasAllAllocatedFunds() ? getParticipationAuthorizationStateForApproved() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     private MissionStageState getParticipationAuthorizationStateForApproved() {
 	return missionProcess.getIsCanceled().booleanValue() ? MissionStageState.NOT_YET_UNDER_WAY :
 	    (missionProcess.areAllParticipantsAuthorized() ? MissionStageState.COMPLETED : MissionStageState.UNDER_WAY);
     }
 
     protected MissionStageState getExpenseAuthorizationState() {
 	return !missionProcess.isCanceled()
 		&& missionProcess.isApproved()
 		&& missionProcess.hasAllAllocatedFunds()
 		//&& missionProcess.areAllParticipantsAuthorizedForPhaseOne() ?
 		&& missionProcess.areAllParticipantsAuthorized() ?
 		getExpenseAuthorizationStateCompletedOrUnderWay() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     private MissionStageState getExpenseAuthorizationStateCompletedOrUnderWay() {
 	return missionProcess.isAuthorized() ? MissionStageState.COMPLETED : MissionStageState.UNDER_WAY;
     }
 
     protected MissionStageState getPersonelInformationProcessingState() {
 	return missionProcess.areAllParticipantsAuthorized() && (missionProcess.isAuthorized() || missionProcess.hasNoItemsAndParticipantesAreAuthorized()) ?
 		getPersonelInformationProcessingStateForAuthorizedParticipantes() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     protected MissionStageState getPersonelInformationProcessingStateForAuthorizedParticipantes() {
 	return missionProcess.hasCurrentQueue() ? MissionStageState.UNDER_WAY : 
 	    (missionProcess.getIsCanceled().booleanValue() ? MissionStageState.NOT_YET_UNDER_WAY : MissionStageState.COMPLETED);
     }
 
     protected MissionStageState getArchivedState() {
 	return missionProcess.isTerminated() ? getTerminatedArchivedState() : MissionStageState.NOT_YET_UNDER_WAY;
     }
 
     protected MissionStageState getTerminatedArchivedState() {
 	return missionProcess.isArchived() ? MissionStageState.COMPLETED : MissionStageState.UNDER_WAY;
     }
 
 }
