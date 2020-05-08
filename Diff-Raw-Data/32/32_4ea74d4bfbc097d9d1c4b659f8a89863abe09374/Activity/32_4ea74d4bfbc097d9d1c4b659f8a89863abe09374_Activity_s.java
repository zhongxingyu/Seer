 package planner.app;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class Activity {
 
 	public static final double DEFAULT_ALL_WORK_HOURS = 0;
 	public static final String
 			DEFAULT_NAME = "Unnamed",
 			MSG_NULL_NAME = "Name must not be null",
 			MSG_EARLY_END_DATE = "End date must be after start date.",
 			MSG_LATE_START_DATE = "Start date must be before end date.",
 			MSG_NEG_WORK_HOURS = "Work hours must be positive.";
 
 	private double allocatedWorkHours, workHours = 0;
 	private String name, description;
 	private Date startDate, endDate;
 	private List<User> users = new ArrayList<User>();
 
 	public Activity(String name, String description, double allocatedWorkHours) {
 		try {
 			setName(name);
 		} catch (OperationNotAllowedException e) {
			e.printStackTrace();
 			this.name = DEFAULT_NAME;
 		}
 		setDescription(description);
 		setAllocatedWorkHours(allocatedWorkHours);
 	}
 
 	public Activity(String name, String description) {
 		this(name, description, DEFAULT_ALL_WORK_HOURS);
 	}
 
 	public Activity(String name, int allocatedWorkHours) {
 		this(name, null, allocatedWorkHours);
 	}
 
 	public Activity(String name) {
 		this(name, null, DEFAULT_ALL_WORK_HOURS);
 	}
 
 	public void assignUser(User user) {
 		if(!containsUser(user))
 			users.add(user);
 	}
 
 	public boolean containsUser(User user) {
 		return users.contains(user);
 	}
 
 	//Untested
 	public double getAllocatedWorkHours() {
 		return allocatedWorkHours;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public List<User> getUsers() {
 		return users;
 	}
 
 	//Untested
 	public double getWorkHours() {
 		return workHours;
 	}
 
 	//Untested
 	public boolean hasEndDate() {
 		return endDate != null;
 	}
 
 	//Untested
 	public boolean hasStartDate() {
 		return startDate != null;
 	}
 
 	public void removeUser(User user) {
 		users.remove(user);
 	}
 
 	//Should this throw an exception?
 	public void setAllocatedWorkHours(double allocatedWorkHours) {
 		if(allocatedWorkHours >= 0)
 			this.allocatedWorkHours = allocatedWorkHours;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	//Untested
 	public void setEndDate(Date endDate) throws OperationNotAllowedException {
 		if(hasStartDate()) {
 			if(getStartDate().before(endDate))
 				this.endDate = endDate;
 			else
 				throw new OperationNotAllowedException(Operation.ACT_SET_END_DATE, MSG_EARLY_END_DATE);
 		} else 
 			this.endDate = endDate;
 	}
 
 	//Should this throw an exception?
 	public void setName(String name) throws OperationNotAllowedException {
 		if(name != null)
 			this.name = name;
 		else
 			throw new OperationNotAllowedException(Operation.ACT_SET_NAME, MSG_NULL_NAME);
 	}
 
 	//Untested
 	public void setStartDate(Date startDate) throws OperationNotAllowedException {
 		if(hasEndDate()) {
 			if (getEndDate().after(startDate))
 				this.startDate = startDate;
 			else
 				throw new OperationNotAllowedException(Operation.ACT_SET_START_DATE, MSG_LATE_START_DATE);
 		} else
 			this.startDate = startDate; 
 	}
 
 	//Untested
 	public void setWorkHours(double workHours) throws OperationNotAllowedException {
 		if(workHours > 0)
 			this.workHours += workHours;
 		else
 			throw new OperationNotAllowedException(Operation.ACT_SET_WORK_HOURS, MSG_NEG_WORK_HOURS);
 	}
 
 }
