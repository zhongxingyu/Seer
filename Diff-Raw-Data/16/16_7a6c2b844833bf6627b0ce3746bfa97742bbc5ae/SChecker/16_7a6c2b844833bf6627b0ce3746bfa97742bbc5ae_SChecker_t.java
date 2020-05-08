 package Search;
 
 import java.util.List;
 import java.util.Vector;
 
 import PropositionalLogic.base.Sentence;
 import RawData.Event;
 import RawData.Timeslot;
 import RawData.UserPreference;
 
 public class SChecker implements Checker{
 
 	public boolean conflict(List<Event> schedule, Event course){
 		if(timeConflict(schedule, course))
 			return true;
 		if(locationConflict(schedule, course))
 			return true;
 		return false;
 	}
 	
 	public boolean conflict(UserPreference usrPre, Event course){
 		if(timeConflict(usrPre, course))
 			return true;
 		if(noPrerequisite(usrPre, course))
 			return true;
 		return false;
 	}
 	
 	@Override
 	public boolean conflict(UserPreference usrPre, List<Event> schedule) {
 		int numC = schedule.size();
         int credit = 0;     
         for(Event e : schedule)
             credit += e.getCredit();
         
         if(numC>usrPre.getMaxCourses())
             return true;
         if(credit>usrPre.getMaxCredit())
             return true;
         
         return false;
 	}
 
 	@Override
 	public boolean fullSchedule(List<Event> schedule, UserPreference usrPre) {
 		int numCourse = schedule.size();
 		int numCredit = 0;
 		for(Event e : schedule)
 			numCredit += e.getCredit();
 		
 		int remainingCourse = usrPre.getMaxCourses() - numCourse;
 		int remainingCredit = usrPre.getMaxCredit() - numCredit;
 		
 		if(remainingCredit >= 3 && remainingCourse >= 1)
 			return false;
 		
 		return true;
 	}
 	
 	/* If conflict, return true. */
 	private boolean timeConflict(List<Event> schedule, Event course) {
 		for(Event e : schedule)
             if(e.conflict(course))
                 return true;
         return false;
 	}
 	
 	/* If conflict, return true. */
 	private boolean timeConflict(UserPreference usrPre, Event course){
 		for(Timeslot t1 : course.getTime()){
 			List<Timeslot> day = new Vector<Timeslot>();
             for(Timeslot t2 : usrPre.getSchedule())
                 if(t1.getDay() == t2.getDay())
                   day.add(t2);
             
             boolean conflict = true;
             for(Timeslot t2 : day){
             	if(t1.getStartTime() >= t2.getStartTime() && t1.getEndTime() <= t2.getEndTime()){
             		conflict = false;
             		break;
             	}
             }
             
            if(conflict)
            	return true;
         }
 
		return false;
 	}
 	
 	/* If conflict, return true. */
 	private boolean locationConflict(List<Event> schedule, Event course) {
 		for(Event e : schedule)
             if(e.getLocation().equalsIgnoreCase(course.getLocation()))
                 return true;
         return false;
 	}
 	
 	/* If user has no prerequisite of course, return true. */
 	private boolean noPrerequisite(UserPreference usrPre, Event course){
 		SearchHelper helper = new SSearchHelper();
 		Sentence pre = course.getPrerequisites().clone();
 		
 		for(String c : usrPre.getCompletedEvents()){
 			helper.cutClauses(pre, c);
 			if(pre.getClauses().size() == 0)
 				return false;
 		}
 		
 		return true;
 	}
 }
