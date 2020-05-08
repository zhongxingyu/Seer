 package module.mission.domain;
 
 import org.joda.time.DateTime;
 
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.VirtualHost;
 
 public class TemporaryMissionItemEntry extends TemporaryMissionItemEntry_Base {
     
     public TemporaryMissionItemEntry() {
         super();
         setMissionSystem(MissionSystem.getInstance());
         setUser(UserView.getCurrentUser());
         setCreated(new DateTime());
     }
 
     public TemporaryMissionItemEntry(final MissionItem missionItem) {
 	this();
 	setMissionItem(missionItem);
     }
 
     public void delete() {
 	removeMissionItem();
 	removeUser();
 	removeMissionSystem();
 	deleteDomainObject();
     }
 
     public void gc() {
 	final MissionItem missionItem = getMissionItem();
 	if (missionItem == null) {
 	    delete();
 	} else if (missionItem.hasMissionVersion()) {
 	    throw new Error("This should never happen!!!");
	} else if (hasPastThreashold()) {
 	    missionItem.delete();
 	}
     }
 
    private boolean hasPastThreashold() {
	final DateTime dateTime = hasUser() ? getUser().getLastLogoutDateTime() : null;
	final DateTime threashold = dateTime == null ? new DateTime().minusDays(1) : dateTime;
	return getCreated().isBefore(threashold);
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
 	return getMissionSystem() == VirtualHost.getVirtualHostForThread().getMissionSystem();
     }
 
 }
