 package module.mission.domain;
 
 public class ClearTemporaryMissionItems extends ClearTemporaryMissionItems_Base {
     
     public ClearTemporaryMissionItems() {
         super();
     }
 
     @Override
     public void executeTask() {
	for (final TemporaryMissionItemEntry temporaryMissionItemEntry : MissionSystem.getInstance().getTemporaryMissionItemEntriesSet()) {
	    temporaryMissionItemEntry.gc();
 	}
     }
 
     @Override
     public String getLocalizedName() {
 	return getClass().getName();
     }
     
 }
