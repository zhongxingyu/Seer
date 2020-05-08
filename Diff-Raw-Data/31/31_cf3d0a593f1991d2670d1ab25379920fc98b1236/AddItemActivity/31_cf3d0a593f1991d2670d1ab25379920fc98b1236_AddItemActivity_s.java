 package module.mission.domain.activity;
 
 import module.mission.domain.MissionItem;
 import module.mission.domain.MissionProcess;
 import module.workflow.activities.ActivityInformation;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.util.BundleUtil;
 
 public class AddItemActivity extends MissionProcessActivity<MissionProcess, ItemActivityInformation> {
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
     }
 
     @Override
     public boolean isActive(final MissionProcess missionProcess, final User user) {
	return super.isActive(missionProcess, user) && super.isActive(missionProcess, user) && missionProcess.isUnderConstruction() && missionProcess.isRequestor(user) && missionProcess.hasAnyParticipantes() && missionProcess.getMission().hasAnyFinancer();
     }
 
     @Override
     protected void process(final ItemActivityInformation itemActivityInformation) {
 	final MissionItem missionItem = itemActivityInformation.getMissionItem();
 	missionItem.setInfo(itemActivityInformation);
 	missionItem.distributeCosts();
 	missionItem.hookAfterChanges();
     }
 
     @Override
     public ActivityInformation<MissionProcess> getActivityInformation(final MissionProcess process) {
 	return new ItemActivityInformation(process, this);
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
 	return false;
     }
 
     @Override
     public boolean isVisible() {
 	return false;
     }
 
 }
