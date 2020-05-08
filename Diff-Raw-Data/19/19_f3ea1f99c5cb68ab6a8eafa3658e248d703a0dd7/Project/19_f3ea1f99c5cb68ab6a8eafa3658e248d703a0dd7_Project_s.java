 package dk.softwarehuset.projectmanagement.app;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Project {
 	private String id;
 	private String name;
 	private List<Employee> employees = new ArrayList<Employee>();
 	private Employee projectLeader;
 	private List<Activity> activities = new ArrayList<Activity>();
 	private Week startDate;
 	private Week endDate;
 
 	public Project(String id, String name) {
 		this.id = id;
 		this.name = name;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<Employee> getEmployees() {
 		return employees;
 	}
 
 	public void addEmployee(Employee newEmployee) throws NonUniqueIdentifierException {
 		for (Employee employee : employees) {
 			if (id.equals(employee.getId())) {
 				throw new NonUniqueIdentifierException("Employee on project already");
 			}
 		}
 
 		employees.add(newEmployee);
 	}
 
 	public void removeEmployee(Employee employee) {
 		employees.remove(employee);
 	}
 
 	public void setProjectLeader(Employee employee) {
 		projectLeader = employee;
 	}
 
 	public Employee getProjectLeader() {
 		return projectLeader;
 	}
 
 	public List<Activity> getActivities() {
 		return activities;
 	}
 
 	public void addActivity(Activity activity) {
 		activities.add(activity);
 	}
 
 	public void setStartDate(Week startDate) throws IllegalArgumentException {
 		if (endDate != null && startDate.compareTo(endDate) == 1) {
 			throw new IllegalArgumentException("Start date after end date");
 		}
		
 		this.startDate = startDate;
 	}
	
 	public void setEndDate(Week endDate) throws IllegalArgumentException {
 		if (startDate != null && endDate.compareTo(startDate) == -1) {
 			throw new IllegalArgumentException("End date before start date");
 		}
		
 		this.endDate = endDate;
 	}
	
 	public Week getStartDate() {
 		return startDate;
 	}
	
 	public Week getEndDate() {
 		return endDate;
 	}
 }
