 package ca.wasabistudio.chat.support;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 /**
  * This is a generic update notifier that pushes notifications to all watchers
  * on the queue. As soon as the watcher is notified, it is removed.
  *
  * @author wasabi <wasabi@wasabistudio.ca>
  */
 public class UpdateQueue {
 
 	private final Queue<UpdateWatcher> watchers;
 
 	public UpdateQueue() {
 		watchers = new LinkedList<UpdateWatcher>();
 	}
 
 	public synchronized void pushUpdate(Object data) {
 		UpdateWatcher watcher = null;
 		Queue<UpdateWatcher> notFinished = new LinkedList<UpdateWatcher>();
 		while ((watcher = watchers.poll()) != null) {
 			watcher.pushUpdate(data);
 			if (!watcher.isFinished()) {
 				notFinished.add(watcher);
 			}
 		}
 		watchers.addAll(notFinished);
 	}
 
 	public synchronized void cancel(Object data) {
		for (UpdateWatcher watcher : watchers) {
 			watcher.cancel(data);
 		}
 	}
 
 	public synchronized void addWathcer(UpdateWatcher watcher) {
 		watchers.add(watcher);
 	}
 
 	public synchronized void removeWatcher(UpdateWatcher watcher) {
 		watchers.remove(watcher);
 	}
 
 }
