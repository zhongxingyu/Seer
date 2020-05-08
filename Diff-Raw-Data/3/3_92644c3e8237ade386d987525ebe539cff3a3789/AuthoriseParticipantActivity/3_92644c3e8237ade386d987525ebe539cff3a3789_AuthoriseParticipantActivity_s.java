 package module.mission.domain.activity;
 
 import module.mission.domain.MissionProcess;
 import module.mission.domain.PersonMissionAuthorization;
 import module.organization.domain.Person;
 import module.workflow.activities.ActivityInformation;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 
 public class AuthoriseParticipantActivity extends MissionProcessActivity<MissionProcess, AuthoriseParticipantActivityInformation> {
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final MissionProcess missionProcess, final User user) {
 	return super.isActive(missionProcess, user)
 		&& !missionProcess.getIsCanceled()
 		&& missionProcess.isApproved()
 		&& missionProcess.canAuthoriseParticipantActivity()
		&& (!missionProcess.getMission().hasAnyFinancer() || missionProcess.hasAllAllocatedFunds());
     }
 
     @Override
     protected void process(final AuthoriseParticipantActivityInformation authoriseParticipantActivityInformation) {
 	final PersonMissionAuthorization personMissionAuthorization = authoriseParticipantActivityInformation.getPersonMissionAuthorization();
 	final User user = UserView.getCurrentUser();
 	final Person person = user.getPerson();
 	personMissionAuthorization.setAuthority(person);
 	final MissionProcess missionProcess = authoriseParticipantActivityInformation.getProcess();
 	missionProcess.setProcessParticipantInformationQueue();
     }
 
     @Override
     public ActivityInformation<MissionProcess> getActivityInformation(final MissionProcess process) {
 	return new AuthoriseParticipantActivityInformation(process, this);
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
 	return true;
     }
 
     @Override
     public boolean isVisible() {
 	return false;
     }
 
 }
