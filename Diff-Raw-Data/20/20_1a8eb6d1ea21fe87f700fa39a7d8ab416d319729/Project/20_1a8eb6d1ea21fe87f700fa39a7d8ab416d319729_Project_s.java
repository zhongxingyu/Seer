 package ch.hsr.waktu.domain;
 
 
 
 /**
  * @author simon.staeheli
  * @version 1.0
  * @created 01-Apr-2011 15:36:30
  */
 public class Project {
 
 	private int projectid;
 	private String projectIdentifier;
 	private String description;
 	private Usr projectManager;
 	private boolean active = true;
 	private int plannedTime;
 
 	public Project(){
 
 	}
 	
 	public Project(String projectIdentifier) {
 		this.projectIdentifier = projectIdentifier;
 	}
 	
 	public Project(String projectID, String description, int plannedTime) {
 		this.projectIdentifier = projectID;
 		this.description = description;
 		this.plannedTime = plannedTime;
 	}
 	
 	public Project(String projectID, String description, Usr projectManager, int plannedTime) {
 		this.projectIdentifier = projectID;
 		this.description = description;
 		this.projectManager = projectManager;
 		this.plannedTime = plannedTime;
 	}
 
 	public int getId() {
 		return projectid;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActiveState(boolean toActive) {
 		this.active = toActive;
 	}
 
 	public int getPlannedTime() {
 		return plannedTime;
 	}
 
 	public void setPlannedTime(int plannedTime) {
 		this.plannedTime = plannedTime;
 	}
 
 	public String getProjectIdentifier() {
 		return projectIdentifier;
 	}
 
 	public void setProjectIdentifier(String projectIdentifier) {
 		this.projectIdentifier = projectIdentifier;
 	}
 	
 	public Usr getProjectManager() {
 		return projectManager;
 	}
 	
 	public void setProjectManager(Usr projectManager) {
 		this.projectManager = projectManager;
 	}
 
 	@Override
 	public String toString() {
 		return projectIdentifier + " " + description;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Project) {
 			Project proj = (Project)obj;
			if (proj.projectIdentifier.equals(projectIdentifier) && proj.description.equals(description) && proj.projectid == projectid) {
				return true;
 			} else {
 				return false;
 			}
 		}
 		return super.equals(obj);
 	}
 
 	@Override
 	public int hashCode() {
 		int hashCode = 23;
		hashCode = 31 * projectid;
		hashCode = 31 * projectIdentifier.hashCode();
		hashCode = 31 * description.hashCode();
		hashCode = 31 * projectManager.hashCode();
		hashCode = 31 * plannedTime;
 		return hashCode;
 	}
 }
