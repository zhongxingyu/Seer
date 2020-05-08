 package ss.apiImpl;
 
 public class BackupItem {
 	private int finishedProjects;
 	private int cost;
 	private int totalProjects;
 
 	public BackupItem(int finishedProjects, int cost, int totalProjects) {
 		this.finishedProjects = finishedProjects;
 		this.cost = cost;
 		this.totalProjects = totalProjects;
 	}
 
 	public int getFinishedProjects() {
 		return finishedProjects;
 	}
 
 	public int getCost() {
 		return cost;
 	}
 
 	public int getTotalProjects() {
 		return totalProjects;
 	}
 
 }
