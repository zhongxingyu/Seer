 package dashboard.model.achievement;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import dashboard.model.Course;
 import dashboard.model.Location;
 import dashboard.model.Student;
 import dashboard.model.StudyMoment;
 import dashboard.registry.StudentRegistry;
 import dashboard.util.Statistics;
 
 public class Achievement implements Serializable {
 	
 	enum Repeat implements Serializable{
 		
 		DAILY("daily"),
 		WEEKLY("weekly"),
 		MONTHLY("monthley"),
 		YEARLY("yearly");
 		
 		private String reccuring;
 		private Calendar calendar = Calendar.getInstance();
 		
 		private Repeat(String reccuring){
 			this.reccuring = reccuring;
 		}
 		
 		public String getReccuring() {
 			return reccuring;
 		}
 		
 		private Date changeDate(Date date, Date momentDate){
 			calendar.setTime(date);
 			Calendar momentCalendar = Calendar.getInstance();
 			momentCalendar.setTime(momentDate);
 			calendar.set(Calendar.YEAR, momentCalendar.get(Calendar.YEAR));
 			if(reccuring.equals("yearly"))
 				return calendar.getTime();
 			calendar.set(Calendar.MONTH, momentCalendar.get(Calendar.MONTH));
 			if(reccuring.equals("monthly"))
 				return calendar.getTime();
 			calendar.set(Calendar.WEEK_OF_YEAR, momentCalendar.get(Calendar.WEEK_OF_YEAR));
 			if(reccuring.equals("weekly"))
 				return calendar.getTime();
 			calendar.set(Calendar.DAY_OF_YEAR, momentCalendar.get(Calendar.DAY_OF_YEAR));
 			if(reccuring.equals("daily"))
 				return calendar.getTime();
 			else
 				return null;
 		}
 	}	
 	private static final long serialVersionUID = -4773912395822659711L;
 	//info
 	private String id;
 	private String name;
 	private String desc;
 	private Course course;
 	private String icon;
 	private boolean visible;
 	//parameters
 	private boolean needTime;
 	private long time;
 	private boolean needNumber;
 	private int number;
 	private boolean needPeriod;
 	//if you chose needExpiration it only needs an end date
 	private boolean needExpiration;
 	private Date startDate;
 	private Date endDate;
 	private boolean needRepeating;
 	private Repeat repeat;
 	private boolean needLocations;
 	private int numberOfLocations;
 	private boolean needPages;
 	private int numberOfPages;
 	private boolean needExercises;
 	private int numberOfExercises;
 	private boolean needFriends;
 	private int numberOfFriends;
 	
 	
 	public Achievement(String id, String name, String desc, Course course, String icon, boolean visible){
 		this.id = id;
 		this.name = name;
 		this.desc = desc;
 		this.course = course;
 		this.icon = icon;
 		this.visible = visible;
 	}
 	
 	public void addTimeRequirement(long time){
 		needTime = true;
 		this.time = time;
 	}
 	
 	public void addNumberRequirement(int number){
 		needNumber = true;
 		this.number = number;
 	}
 	
 	
 	public void addExpirationRequirement(Date endDate){
 		needExpiration = true;
 		this.endDate = endDate;
 	}
 	
 	public void addPeriodRequirement(Date startDate,Date endDate){
 		needPeriod = true;
 		this.startDate = startDate;
 		this.endDate = endDate;
 	}
 	
 	public void addLocationRequirement(int numberOfLocations){
 		needLocations = true;
 		this.numberOfLocations = numberOfLocations;
 	}
 	
 	public void addPagesRequirement(int numberOfPages){
 		needPages = true;
 		this.numberOfPages = numberOfPages;
 	}
 	
 	public void addExercicesRequirement(int numberOfexercices){
 		needExercises = true;
 		this.numberOfExercises = numberOfexercices;
 	}
 	
 	public void addFriendssRequirement(int numberOfFriends){
 		needFriends = true;
 		this.numberOfFriends = numberOfFriends;
 	}
 	
 	public void addRepeatingRequirement(String sortRepeat){
 		needRepeating = true;
 		if(sortRepeat.equals("daily"))
 			repeat = Repeat.DAILY;
 		else if(sortRepeat.equals("weekly"))
 			repeat = Repeat.WEEKLY;
 		else if(sortRepeat.equals("monthly"))
 			repeat = Repeat.MONTHLY;
 		else if(sortRepeat.equals("yearly"))
 			repeat = Repeat.YEARLY;
 		else
 			needRepeating = false;
 	}
 	
 	public String getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getDesc() {
 		return desc;
 	}
 
 	public Course getCourse() {
 		return course;
 	}
 	
 	public String getIcon(){
 		return icon;
 	}
 
 	public long getTime() {
 		return time;
 	}
 	
 	public int getNumber() {
 		return number;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 	
 	public Date getEndDate() {
 		return endDate;
 	}
 	
 	public int getNumberOfLocations() {
 		return numberOfLocations;
 	}
 	
 	public int getNumberOfPages() {
 		return numberOfPages;
 	}
 	
 	public int getNumberOfExercises() {
 		return numberOfExercises;
 	}
 	
 	public int getNumberOfFriends() {
 		return numberOfFriends;
 	}
 	
 	public float checkTimeProgress(long seconds){
 		float progress = 0;
 		float x = getTime();
 		progress = seconds/x;
 		if(progress > 1)
 			progress = 1;
 		return progress;
 	}
 	
 	public float checkNumberProgress(int number){
 		float progress = 0;
 		float x = getNumber();
 		progress = number/x;
 		if(progress > 1)
 			progress = 1;
 		return progress;
 	}
 	
 	public float checkLocationsProgress(int numberOfLocations){
 		float progress = 0;
 		float x = getNumberOfLocations();
 		progress = numberOfLocations/x;
 		if(progress > 1)
 			progress = 1;
 		return progress;
 	}
 	
 	public float checkExercicesProgress(int numberOfExercices){
 		float progress = 0;
 		float x = getNumberOfExercises();
 		progress = numberOfExercices/x;
 		if(progress > 1)
 			progress = 1;
 		return progress;
 	}
 	
 	public float checkPagesProgress(int numberOfPages){
 		float progress = 0;
 		float x = getNumberOfPages();
 		progress = numberOfPages/x;
 		if(progress > 1)
 			progress = 1;
 		return progress;
 	}
 
 	public float checkFriendsProgress(int numberOfFriends){
 		float progress = 0;
 		float x = getNumberOfFriends();
 		progress = numberOfFriends/x;
 		if(progress > 1)
 			progress = 1;
 		return progress;
 	}
 	
 	private float checkProgress(ArrayList<StudyMoment> moments,Student student) {
 		float progress = 0;
 		float parameters = 0;
 		if(needTime){
 			long momentsTime = Statistics.getTotalTime(moments);
 			progress += checkTimeProgress(momentsTime);
 			parameters++;
 		}
 		if(needNumber){
 			int number = moments.size();
 			progress += checkNumberProgress(number);
 			parameters++;
 		}
 		if(needExercises){
 			int number = Statistics.getTotalExcercices(moments);
 			progress += checkExercicesProgress(number);
 			parameters++;
 		}
 		if(needPages){
 			int number = Statistics.getTotalPages(moments);
 			progress += checkPagesProgress(number);
 			parameters++;
 		}
 		if(needFriends){
 			int number = StudentRegistry.getTotalFriends(student);
			progress += checkPagesProgress(number);
 			parameters++;
 		}
 		if(needLocations){
 			ArrayList<String> locations = new ArrayList<String>();
 			for(StudyMoment moment: moments)
 				if(moment.getLocation() != null && !student.getStarredLocations().isEmpty() && student.getStarredLocations() != null){
 					String alias = student.matchStarredLocation(moment.getLocation(), 1000).getAlias();
 					if(!locations.contains(alias))
 						locations.add(alias);
 				}
 			int numberOfLocations = locations.size();
 			progress += checkLocationsProgress(numberOfLocations);
 			parameters++;
 		}
 		progress = progress/parameters;
 		return progress;
 	}
 
 	private Date checkDiffDates(Date modify, Date start, Date end) {
 		Calendar calendarStart = Calendar.getInstance();
 		calendarStart.setTime(start);
 		Calendar calendarEnd = Calendar.getInstance();
 		calendarEnd.setTime(end);
 		Calendar calendarMod = Calendar.getInstance();
 		calendarMod.setTime(modify);
 		int yearBetween = calendarEnd.get(Calendar.YEAR) - calendarStart.get(Calendar.YEAR); 
 		calendarMod.add(Calendar.YEAR,yearBetween);
 		if(repeat.getReccuring().equals("yearly"))
 			return calendarMod.getTime();
 		int monthBetween = calendarEnd.get(Calendar.MONTH) - calendarStart.get(Calendar.MONTH);
 		if(monthBetween != 0)
 			calendarMod.add(Calendar.MONTH,yearBetween);
 		if(repeat.getReccuring().equals("monthly"))
 			return calendarMod.getTime();
 		int weekBetween = calendarEnd.get(Calendar.WEEK_OF_YEAR) - calendarStart.get(Calendar.WEEK_OF_YEAR);
 		if(weekBetween != 0)
 			calendarMod.add(Calendar.WEEK_OF_YEAR,yearBetween);
 		if(repeat.getReccuring().equals("weekly"))
 			return calendarMod.getTime();
 		int dayBetween = calendarEnd.get(Calendar.DAY_OF_YEAR) - calendarStart.get(Calendar.DAY_OF_YEAR);
 		if(dayBetween != 0)
 			calendarMod.add(Calendar.DAY_OF_YEAR,yearBetween);
 		if(repeat.getReccuring().equals("daily"))
 			return calendarMod.getTime();
 		return null;
 	}
 
 	public float getProgress(Student student){
 		ArrayList<StudyMoment> moments = (ArrayList<StudyMoment>)student.getStudyMoments().clone();
 		if(getCourse() != null)
 			moments = Statistics.filterMomentsByCourse(moments, getCourse());
 		if(needExpiration)
 			moments = Statistics.getMomentsUntil(moments, getEndDate());
 		if(needRepeating){
 			for(StudyMoment moment : moments){
 				Date start = repeat.changeDate(getStartDate(), moment.getStart());
 				Date end = repeat.changeDate(getEndDate(), moment.getStart());
 				end = checkDiffDates(end, getStartDate(),getEndDate());
 				ArrayList<StudyMoment>localMoments = Statistics.getMomentsPeriod(moments, start, end);
 				if(checkProgress(localMoments,student) == 1)
 					return 1;
 			}
 			Date now = new Date();
 			Date start = repeat.changeDate(getStartDate(),now);
 			Date end = repeat.changeDate(getEndDate(), now);
 			end = checkDiffDates(end, getStartDate(),getEndDate());
 			moments = Statistics.getMomentsPeriod(moments, start, end);
 			return checkProgress(moments,student);
 		}
 		if(needPeriod)
 			moments = Statistics.getMomentsPeriod(moments, getStartDate(), getEndDate());
 		return checkProgress(moments,student);
 	}
 }
