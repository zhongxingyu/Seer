 package planner.app;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Project {
 
 	public static final String
 			DEFAULT_NAME = "Unnamed",
 			MSG_NULL_NAME = "Project name must not be null";
 
 	private String name;
 	private User projectLeader;
 	private List<Activity> activities = new ArrayList<Activity>();
 
 	//Untested catch block
 	public Project(String name, User projectLeader) {
 		try {
 			setName(name);
 		} catch (OperationNotAllowedException e) {
 			this.name = DEFAULT_NAME;
 		}
 		setProjectLeader(projectLeader);
 	}
 
 	public Project(String name) {
 		this(name, null); 
 	}
 
 	public void addActivity(Activity activity) {
 		activities.add(activity);
 	}
 
 	public List<Activity> getActivities() {
 		return activities;
 	}
 
 	public User getProjectLeader() {
 		return projectLeader; 
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean hasProjectLeader() {
 		return projectLeader != null;
 	}
 
 	public void removeProjectLeader() {
 		setProjectLeader(null);
 	}
 
 	public void setProjectLeader(User projectLeader) {
 		this.projectLeader = projectLeader;
 	}
 
 	public void setName(String name) throws OperationNotAllowedException {
 		if(name != null)
 			this.name = name;
 		else
 			throw new OperationNotAllowedException(Operation.PROJ_SET_NAME, MSG_NULL_NAME);
 	}
 
 }
