 package module.mission.domain;
 
 import pt.ist.bennu.core.domain.MyOrg;
 import pt.ist.bennu.core.domain.VirtualHost;
 
 public class ClearTemporaryMissionItems extends ClearTemporaryMissionItems_Base {
     
     public ClearTemporaryMissionItems() {
         super();
     }
 
     @Override
     public void executeTask() {
 	for (VirtualHost vHost : MyOrg.getInstance().getVirtualHosts()) {
	    try {
		VirtualHost.setVirtualHostForThread(vHost);
		for (final TemporaryMissionItemEntry temporaryMissionItemEntry : MissionSystem.getInstance()
			.getTemporaryMissionItemEntriesSet()) {
		    temporaryMissionItemEntry.gc();
		}
	    } finally {
		VirtualHost.releaseVirtualHostFromThread();
 	    }
 	}
     }
 
     @Override
     public String getLocalizedName() {
 	return getClass().getName();
     }
     
 }
