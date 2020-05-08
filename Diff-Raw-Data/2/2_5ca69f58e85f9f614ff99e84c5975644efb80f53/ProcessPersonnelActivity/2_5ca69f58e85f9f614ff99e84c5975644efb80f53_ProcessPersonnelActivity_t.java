 package module.mission.domain.activity;
 
 import module.mission.domain.MissionProcess;
 import module.workflow.activities.ActivityInformation;
 import module.workflow.domain.WorkflowQueue;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 
 public class ProcessPersonnelActivity extends MissionProcessActivity<MissionProcess, ActivityInformation<MissionProcess>> {
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final MissionProcess missionProcess, final User user) {
 	return super.isActive(missionProcess, user)
		//&& !missionProcess.getIsCanceled().booleanValue()
 		&& missionProcess.hasCurrentQueue()
 		&& missionProcess.getCurrentQueue().isCurrentUserAbleToAccessQueue()
 		&& (missionProcess.isAuthorized() || missionProcess.hasNoItemsAndParticipantesAreAuthorized())
 		&& missionProcess.areAllParticipantsAuthorized();
     }
 
     @Override
     protected void process(final ActivityInformation activityInformation) {
 	final MissionProcess missionProcess = (MissionProcess) activityInformation.getProcess();
 	final WorkflowQueue workflowQueue = missionProcess.getCurrentQueue();
 	missionProcess.removeCurrentQueue();
 	workflowQueue.addProcessesHistory(missionProcess);
     }
 
 }
