 /* 
  * Smooth, backgrounded saving of changed issues.
  * Every time this task runs, one issue will have changes saved to disk.
  * The frequency with which this task runs can be changed to improve performance.
  */
 
 package me.makskay.bukkit.tidy.tasks;
 
 import me.makskay.bukkit.tidy.IssueManager;
 import me.makskay.bukkit.tidy.IssueReport;
 import me.makskay.bukkit.tidy.StorageManager;
 import me.makskay.bukkit.tidy.TidyPlugin;
 
 public class SaveChangedIssuesTask implements Runnable {
 	private IssueManager issueManager;
 	private StorageManager storageManager;
 	
 	public SaveChangedIssuesTask(TidyPlugin plugin) {
		this.issueManager  = plugin.getIssueManager();
 	}
 	
 	public void run() {
 		IssueReport issue = null;
 		boolean delete = false;
 		for (IssueReport iss : issueManager.getCachedIssues()) {
 			if (iss.hasChanged()) {
 				issue = iss;
 				issue.setHasChanged(false);
 				break;
 			}
 			
 			delete = iss.shouldBeDeleted();
 			if (delete) {
 				issue = iss;
 				issueManager.getCachedIssues().remove(issue);
 				break;
 			}
 		}
 		
 		if (issue == null) {
 			return; // there were no issues with changes waiting to be saved
 		}
 		
 		if (delete) {
 			storageManager.deleteIssue(issue);
 		}
 		
 		else {
 			storageManager.saveIssue(issue);
 		}
 	}
 }
