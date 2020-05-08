 package com.synacor.soa.ark;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.WatchedEvent;
 import org.apache.zookeeper.Watcher.Event.EventType;
 import org.apache.zookeeper.Watcher.Event.KeeperState;
 
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.api.CuratorWatcher;
 
 /**
  * Sets and re-creates watches as necessary to notify a client of create/delete events for a path that may include wildcards.
  * The path format is (/name)+ where name may be a regular expression which must not include the '/' character.
  * examples:
  *  /services/a*b/deployments/1\.*.0/instances/.*-test/lifecycleState
  *  /services/a*b/deployments/1\.*.0/instances/.*-test/autoScaling
  *  /services/a*b/deployments/[1-4]*\.0\.0/instances/.*
  * usage:
  *  List<String> initialLeaves = new WildChild(client, "/services/*", watcher).getMatchingLeaves();
  *  
  *  The CuratorWatcher will be called initially for all pre-existing matching leaves, and can be used to build the initial list of matches.
  */
 public class WildChild {
 	CuratorFramework client;
 	private String path;
 	private String wildPath;
 	private String matchCriteria; // the next part of the fullPath
 
 	private CuratorWatcher leafWatcher;
 	private Set<String> trackedChildren = new HashSet<String>();
 
 	public WildChild(CuratorFramework client, String wildPath, CuratorWatcher leafWatcher) throws Exception {
 		this(client, "", wildPath, leafWatcher);
 	}
 	
 	private WildChild(CuratorFramework client, String path, String wildPath, CuratorWatcher leafWatcher) throws Exception {
 		this.client = client;
 		this.path = path;
 		this.wildPath = wildPath;
 		this.leafWatcher = leafWatcher;
 
 		int index = path.split("/").length;
 		this.matchCriteria = wildPath.split("/")[index];
 
 		List<String> children = client.getChildren().usingWatcher(new WildChildWatcher()).forPath(path);
 		for(String child : children) {
 			String childPath = path + "/" + child;
 			boolean childIsLeaf = childPath.split("/").length == wildPath.split("/").length;
 			trackedChildren.add(child);
 			if(childIsLeaf) {
 				WatchedEvent createdEvent = new WatchedEvent(EventType.NodeCreated, KeeperState.SyncConnected, childPath);
 				leafWatcher.process(createdEvent);
 			} else {
 				new WildChild(client, childPath, wildPath, leafWatcher);
 			}
 		}
 	}
 
 	/**
 	 * Watcher class that monitors a node for child changes,
 	 * and manages setting watchers on child nodes or notifying the primary watcher.
 	 */
 	private class WildChildWatcher implements CuratorWatcher {
 		private List<String> getChildrenSafe() {
 			try {
 				return client.getChildren().usingWatcher(this).forPath(path);
 			} catch (KeeperException.NoNodeException exc) {
 				return new ArrayList<String>();
 			} catch (Exception exc) {
 				throw new RuntimeException(exc);
 			}
 		}
 		
 		public void process(WatchedEvent event) throws Exception {
			if(!path.equals(event.getPath())) throw new RuntimeException("incorrect path");
 			
 			boolean childIsLeaf = wildPath.split("/").length == path.split("/").length+1;
 
 			if(event.getType() == EventType.NodeChildrenChanged) {
 				List<String> children = getChildrenSafe();
 				
 				// Remove missing children from tracking list
 				Set<String> trackedCopy = new HashSet<String>(trackedChildren);
 				for(String trackedChild : trackedCopy) {
 					if(!children.contains(trackedChild)) {
 						trackedChildren.remove(trackedChild);
 						if(childIsLeaf) {
 							WatchedEvent deletedEvent = new WatchedEvent(EventType.NodeDeleted, KeeperState.SyncConnected, path + "/" + trackedChild);
 							leafWatcher.process(deletedEvent);
 						}
 					}
 				}
 
 				// Add new children to tracking list
 				for(String child : children) {
 					if(child.matches(matchCriteria) && !trackedChildren.contains(child)) {
 						String childPath = path + "/" + child;
 						if(childIsLeaf) {
 							WatchedEvent createdEvent = new WatchedEvent(EventType.NodeCreated, KeeperState.SyncConnected, childPath);
 							leafWatcher.process(createdEvent);
 						} else {
 							new WildChild(client, childPath, wildPath, leafWatcher);
 						}
 						trackedChildren.add(child);
 					}
 				}
 			}
 		}
 	}
 }
