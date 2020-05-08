 package hudson.plugins.ec2;
 
import hudson.model.Queue;
 import jenkins.model.Jenkins;
 
 
 public class Utils {
 	public static void finishSlaveAndQueuedItems(EC2AbstractSlave slave) {
		Queue.Item[] items = Jenkins.getInstance().getQueue().getItems();
		for (Queue.Item item : items) {
 			if (item.task.getAssignedLabel().getDisplayName().equals(slave.getDisplayName())) {
 				Jenkins.getInstance().getQueue().cancel(item);
 			}
 		}
 		if (!slave.stopOnTerminate)
 			slave.terminate();
 	}
 }
