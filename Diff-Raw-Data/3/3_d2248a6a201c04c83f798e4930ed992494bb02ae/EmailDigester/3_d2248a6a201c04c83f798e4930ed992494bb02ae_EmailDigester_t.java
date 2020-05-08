 package module.workingCapital.domain;
 
 import myorg.domain.MyOrg;
 import myorg.domain.VirtualHost;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class EmailDigester extends EmailDigester_Base {
 
     public EmailDigester() {
 	super();
     }
 
     @Override
     @Service
     public void executeTask() {
 	for (final VirtualHost virtualHost : MyOrg.getInstance().getVirtualHostsSet()) {
	    if (!virtualHost.getHostname().startsWith("dot")) {
		continue;
	    }
 	    try {
 		VirtualHost.setVirtualHostForThread(virtualHost);
 		EmailDigesterUtil.executeTask();
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
