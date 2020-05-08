 package aic.monitor;
 
import aic.monitor.util.PropertyManager;
 
 public abstract class AbstractMonitorStrategy {
 	protected LaunchMonitor launchMonitor;
 	protected SSHMonitor sshMonitor;
 
 	protected AbstractMonitorStrategy() {
 		initMonitors();
 	}

 	private void initMonitors(){
 		LaunchMonitor launchMonitor = new LaunchMonitor(PropertyManager
 				.getInstance().getProperties());
 
 //		try {
 			// TODO: think about ssh monitors
 //			SSHMonitor sshMonitor = new SSHMonitor("", "");
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 	}
 
 	public abstract void monitor();
 }
