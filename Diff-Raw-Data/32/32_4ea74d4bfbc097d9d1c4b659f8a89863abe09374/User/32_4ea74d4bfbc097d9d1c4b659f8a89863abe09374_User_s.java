 package planner.app;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TreeSet;
 
 public class User {
 
 	public static final String
 			DEFAULT_USERNAME = "Unnamed",
 			MSG_WORK_OVERLAP = "Work is already registered in the desired interval.",
 			MSG_NULL_USERNAME = "Username must not be null.";
 
 	private String username, password, email;
 	private List<Activity> activities = new ArrayList<Activity>();
 	private TreeSet<Work> workSet = new TreeSet<Work>();
 //	private Calendar startWork;
 //	private Calendar endWork;
 
 	public User(String username, String password, String email) {
 		try {
 			setUsername(username);
 		} catch (OperationNotAllowedException e) {
			e.printStackTrace();
 			this.username = DEFAULT_USERNAME;
 		}
 		this.password = password;
 		this.email = email;
 	}
 
 	public void assignActivity(Activity activity) {
 		activities.add(activity);
 	}
 
 	public List<Activity> getActivities() {
 		return activities;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public TreeSet<Work> getWorkSet() {
 		return workSet;
 	}
 
 	public boolean isWorking(Date fromDate, Date toDate) {
 		Work work = new Work(fromDate, toDate, ConstantActivities.NONE.getActivity()), border = workSet.floor(work);
 		if(border != null && border.getToDate().after(fromDate))
 			return true;
 		else {
 			border = workSet.ceiling(work);
 			if(border != null && border.getFromDate().before(toDate))
 				return true;
 			else
 				return false;
 		}
 	}
 
 	public void registerWork(Work work) throws OperationNotAllowedException {
 		if(isWorking(work.getFromDate(), work.getToDate()))
 			throw new OperationNotAllowedException(Operation.USER_REGISTER_WORK, MSG_WORK_OVERLAP);
 		else
 			workSet.add(work);
 	}
 
 	public void registerWork(Date fromDate, Date toDate, Activity activity) throws OperationNotAllowedException {
 		registerWork(new Work(fromDate, toDate, activity));
 	}
 
 	public void removeWork(Work work) {
 		workSet.remove(work);
 	}
 
 	public void setActivities(ArrayList<Activity> activities) {
 		this.activities = activities;
 	}
 
 	public void setUsername(String username) throws OperationNotAllowedException {
 		if(username != null)
 			this.username = username;
 		else
 			throw new OperationNotAllowedException(Operation.USER_NULL_USERNAME, MSG_NULL_USERNAME);
 	}
 
 	//<<<<<<< HEAD
 //		private boolean isWorking() {
 	//
 //			return startWork != null;
 //		}
 	//
 //		void setStartWork(Calendar date) {
 //			startWork = date;
 	//
 //		}
 	//
 //		public Calendar getStartWork() {
 //			return startWork;
 //		}
 	//
 //		public Calendar DaysWorking(int days) {
 //			if (!isWorking()) {
 //				return null;
 //			}
 //			Calendar date = getStartWork();
 //			endWork = new GregorianCalendar();
 //			endWork.setTime(date.getTime());
 //			endWork.add(Calendar.DAY_OF_YEAR, days);
 //			return endWork;
 //		}
 	//=======
 
 
 
 }
