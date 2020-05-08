 /* Smooth, backgrounded saving of changed issues.
  * Every time this task runs, one issue will have changes saved to disk.
  * The frequency with which this task runs can be changed to improve performance.
  */
 
 package me.makskay.bukkit.tidy.tasks;
 
 import me.makskay.bukkit.tidy.ConfigAccessor;
 import me.makskay.bukkit.tidy.IssueManager;
 import me.makskay.bukkit.tidy.IssueReport;
 import me.makskay.bukkit.tidy.TidyPlugin;
 
 public class SaveChangedIssuesTask implements Runnable {
 	private ConfigAccessor issuesYml;
 	private IssueManager issueManager;
 	
 	public SaveChangedIssuesTask(TidyPlugin plugin) {
 		this.issuesYml    = plugin.getIssuesYml();
 		this.issueManager = plugin.getIssueManager();
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
 		
 		String path = "issues." + issue.getUid();
 		
 		if (delete) {
 			issuesYml.getConfig().set(path, null);
 		}
 		
 		else {
			issuesYml.getConfig().set(path + ".owner", issue.getOwnerName());
			issuesYml.getConfig().set(path + ".description", issue.getDescription());
			issuesYml.getConfig().set(path + ".location", issue.getLocationString());
 			issuesYml.getConfig().set(path + ".open", issue.isOpen());
 			issuesYml.getConfig().set(path + ".sticky", issue.isSticky());
 			issuesYml.getConfig().set(path + ".comments", issue.getComments());
 			issuesYml.getConfig().set(path + ".timestamp", System.currentTimeMillis());
 		}
 		
 		issuesYml.saveConfig();
 		issuesYml.reloadConfig();
 	}
 }
