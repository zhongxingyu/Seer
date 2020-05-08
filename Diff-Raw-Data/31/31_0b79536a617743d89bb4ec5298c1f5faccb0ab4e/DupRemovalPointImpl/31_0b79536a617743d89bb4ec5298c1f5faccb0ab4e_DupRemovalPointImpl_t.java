 package spiderman.plugin.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eweb4j.spiderman.plugin.DupRemovalPoint;
 import org.eweb4j.spiderman.spider.SpiderListener;
 import org.eweb4j.spiderman.task.Task;
 import org.eweb4j.spiderman.xml.Site;
 
 import spiderman.plugin.duplicate.DocIDServer;
 
 public class DupRemovalPointImpl implements DupRemovalPoint{
 	
 	private SpiderListener listener;
 	private DocIDServer db = null;
 //	private Collection<String> newUrls = null;
 	private Site site  = null;
 	
 	public void init(Site site, SpiderListener listener) {
 		this.site = site;
 		this.listener = listener;
		if (db == null) {
			db = new DocIDServer(site.getName(), listener);
			listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + site.getName() + " initial success...");
 		}
 	}
 
 	public void destroy() {
		if (db != null) {
			db.close();
			db = null;
			listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + site.getName() + " destroy success...");
 		}
 	}
 	
 //	public void context(Task task, Collection<String> newUrls) {
 //		this.newUrls = newUrls;
 //	}
 	
	public synchronized Collection<Task> removeDuplicateTask(Task task, Collection<String> newUrls, Collection<Task> tasks){
		if (db == null)
			return null;
		
 		Collection<Task> validTasks = new ArrayList<Task>();
 		for (String url : newUrls){
 			Task newTask = new Task(url, site, 10);
 			int docId = db.getDocId(url);
 			if (docId < 0){
 				docId = db.getNewDocID(url);
 				validTasks.add(newTask);
 			}
 		}
 		
 		return validTasks;
 	}
 
 }
