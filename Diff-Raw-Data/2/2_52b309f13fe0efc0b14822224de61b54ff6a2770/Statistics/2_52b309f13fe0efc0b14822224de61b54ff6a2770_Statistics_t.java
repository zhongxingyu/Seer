 package dashboard.util;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import dashboard.model.*;
 import dashboard.registry.StudentRegistry;
 
 public class Statistics {
 
 	/**
 	 * @param course
 	 * the course you ant to get the time from
 	 * @param moments
 	 * 	the moments you want to use to get the time from
 	 * @return
 	 * 	the total time studied for that course in seconds
 	 * 	|	for(StudyMoment moment : moments))
 	 *	|		if(moment.getCourse().getName().equals(course))
 	 *	|			time += moment.getTime()
 	 */
 	public static long getTime(Course course, ArrayList<StudyMoment> moments){
 		long time = 0;
 		if(moments!=null){
 			for(StudyMoment moment : moments)
 				if(moment.getCourse().equals(course))
 					time += moment.getTime();
 		}
 		return time;
 			
 	}
 
 	/**
 	 * @param moments
 	 * 	the moments you want to use to get the time from
 	 * @return
 	 * 	returns the total time the student has studied in seconds
 	 * 	|	for(StudyMoment moment : moments)
 	 *	|	time += moment.getTime()
 	 */
 	public static long getTotalTime(ArrayList<StudyMoment> moments) {
 		long time = 0;
 		if(moments!=null){
 			for(StudyMoment moment : moments)
 				time += moment.getTime();
 		}
 		return time;
 	}
 	
 	public static int getTotalPages(ArrayList<StudyMoment> moments){
 		int pages = 0;
 		for(StudyMoment moment : moments)
 			if(moment.getKind().equals("Theorie"))
 				pages += moment.getAmount();
 		return pages;
 	}
 	
 	public static int getTotalExcercices(ArrayList<StudyMoment> moments){
 		int excercices = 0;
 		for(StudyMoment moment : moments)
 			if(moment.getKind().equals("Oefeningen"))
 				excercices += moment.getAmount();
 		return excercices;
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments you want to use to get the time from
 	 * @return
 	 * 	an arrayList with the moments the student studied last week
 	 */
 	public static ArrayList<StudyMoment> getMomentsWeek(ArrayList<StudyMoment> moments) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(Calendar.HOUR_OF_DAY,0);
 		calendar.set(Calendar.MINUTE,0);
 		calendar.set(Calendar.SECOND,0);
 		calendar.set(Calendar.MILLISECOND,0);
 		calendar.set(Calendar.DAY_OF_WEEK,calendar.MONDAY);
 		Date start = calendar.getTime();
 		calendar.add(Calendar.WEEK_OF_YEAR,1);
 		Date end = calendar.getTime();
 		return getMomentsPeriod(moments, start, end);
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments you want to use to get the time from
 	 * @return
 	 * 	an arrayList with the moments the student studied last week
 	 */
 	public static ArrayList<StudyMoment> getMomentsMonth(ArrayList<StudyMoment> moments) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(Calendar.HOUR_OF_DAY,0);
 		calendar.set(Calendar.MINUTE,0);
 		calendar.set(Calendar.SECOND,0);
 		calendar.set(Calendar.MILLISECOND,0);
 		calendar.set(Calendar.DAY_OF_MONTH,1);
 		Date start = calendar.getTime();
 		calendar.add(calendar.MONTH,1);
 		Date end = calendar.getTime();
 		return getMomentsPeriod(moments, start, end);
 	}
 	
 	public static ArrayList<StudyMoment> getMomentsPeriod(ArrayList<StudyMoment> moments, Date start,Date end){
 		ArrayList<StudyMoment> goodMoments = new ArrayList<StudyMoment>();
 		for(StudyMoment moment : moments)
 			if(moment.getStart().before(end) &&
 			moment.getStart().after(start))
 				goodMoments.add(moment);
 		return goodMoments;
 	}
 	
 	public static ArrayList<StudyMoment> getMomentsUntil(ArrayList<StudyMoment> moments,Date end){
 		ArrayList<StudyMoment> goodMoments = new ArrayList<StudyMoment>();
 		for(StudyMoment moment : moments)
 			if(moment.getStart().before(end))
 				goodMoments.add(moment);
 		return goodMoments;
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments of a student
 	 * @param courses
 	 * 	the courses of a student
 	 * @return
 	 * 	a hashmap filled with the courses and the percentage of time
 	 *  put into those courses based on total time
 	 */
 	public static HashMap<String,Long> getCourseTimes(ArrayList<StudyMoment> moments,ArrayList<CourseContract> courses){
 		HashMap<String,Long> result = new HashMap<String,Long>();
 		for(CourseContract course: courses){
 			long part = getTime(course.getCourse(), moments);
 			result.put(course.getCourse().getName(), part);
 		}
 		return result;
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments of a student
 	 * @param courses
 	 * 	the courses of a student
 	 * @return
 	 * 	a hashmap filled with the courses and the percentage of time
 	 *  put into those courses based on total time
 	 */
 	public static HashMap<String,Long> getCoursePercents(ArrayList<StudyMoment> moments,ArrayList<CourseContract> courses){
 		HashMap<String,Long> result = new HashMap<String,Long>();
 		long totTime = getTotalTime(moments);
 		for(CourseContract course: courses){
 			long part = getTime(course.getCourse(), moments);
 			long resTime = (part/totTime)*100;
 			result.put(course.getCourse().getName(), resTime);
 		}
 		return result;
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments of a student
 	 * @return
 	 * 	a hashmap filled with days and the corresponding relative amount studied
 	 */
 	public static HashMap<String,Long> getTimeByDay(ArrayList<StudyMoment> moments){
 		HashMap<String, Long> results = new HashMap<String, Long>();
 		String dateString = "geen";
 		for(StudyMoment moment : moments){
 			String newDateString = moment.getStart().toString().substring(0, 10);
 			long time = moment.getTime();
 			if(dateString.equals(newDateString)){
 				time += results.get(newDateString);
 				results.remove(newDateString);
 			}
 			results.put(newDateString, time);		
 		}
 		return results;
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments of a student
 	 * @return
 	 * 	a hashmap filled with locations and the corresponding relative amount studied
 	 */
 	public static HashMap<String,Long> getTimeByLoc(ArrayList<StudyMoment> moments,Student student){
 		HashMap<String,Long> ret = new HashMap<String,Long>();
 		Iterator<StudyMoment> it = moments.iterator();
 		while(it.hasNext()){
 			StudyMoment moment = it.next();
 			Integer amount = moment.getAmount();
 			String name;
 			if(moment.getLocation()==null)
 				name = "Geen data";
 			else{
 				Location match = student.matchStarredLocation(moment.getLocation(), 1000);
 				if(match==null)
 					name = "Overige";
 				else
 					name = match.getAlias();
 			}
 			if(ret.containsKey(name))
 				ret.put(name, amount+ret.get(name));
 			else
 				ret.put(name, amount.longValue());			
 		}
 		return ret;
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments of a student
 	 * @return
 	 * 	a hashmap filled with days and the corresponding relative amount studied
 	 */
 	public static long[] getTimeByDayInWeek(ArrayList<StudyMoment> moments){
 		long[] results = new long[7];
 		for(StudyMoment moment : moments){
 			String dayString = moment.getStart().toString().substring(0,3);
 			if(dayString.equals("Mon"))
 				results[0] += moment.getTime();
 			else if(dayString.equals("Tue"))
 				results[1] += moment.getTime();
 			else if(dayString.equals("Wed"))
 				results[2] += moment.getTime();
 			else if(dayString.equals("Thu"))
 				results[3] += moment.getTime();
 			else if(dayString.equals("Fri"))
 				results[4] += moment.getTime();
 			else if(dayString.equals("Sat"))
 				results[5] += moment.getTime();
 			else if(dayString.equals("Sun"))
 				results[6] += moment.getTime();
 		}	
 			return results;
 	}
 
 	
 	/**
 	 * @param moments
 	 * 	the moments of a student
 	 * @return
 	 * 	a hashmap filled with months and the corresponding relative amount studied
 	 */
 	public static HashMap<String,Long> getTimeByMonth(ArrayList<StudyMoment> moments){
 		HashMap<String, Long> results = new HashMap<String, Long>();
 		String dateString = "geen";
 		for(StudyMoment moment : moments){
 			String newDateString = moment.getStart().toString().substring(4, 7) + moment.getStart().toString().substring(24, 28);
 			long time = moment.getTime();
 			if(dateString.equals(newDateString)){
 				time += results.get(newDateString);
 				results.remove(newDateString);
 			} else
 				dateString = newDateString;			
 			results.put(newDateString, time);		
 		}
 		return results;
 	}
 	
 	/**
 	 * @param moments
 	 * 	the moments of a student
 	 * @param course
 	 * the course to search for
 	 * @return
 	 * 	an arraylist containing moments belonging to the course
 	 */
 	public static ArrayList<StudyMoment> filterMomentsByCourse(ArrayList<StudyMoment> moments,Course course){
 		ArrayList<StudyMoment> results = new ArrayList<StudyMoment>();
 		for(StudyMoment moment : moments){
 			if(moment.getCourse().equals(course))
 				results.add(moment);
 		}
 		return results;
 	}
 	
 	/**
 	 * @param course
 	 * 	a course of the student
 	 * @param student
 	 * the student
 	 * @return
 	 * 	the progress in credits, assuming a credit requires 28 hours of work
 	 */
 	public static double creditProgress(Course course,Student student){
 		double done = getTime(course,student.getStudyMoments());
 		double exp = course.getCredit()*28*60*60;
 		double div = done/exp;
 		if(done<exp)
 			return div;
 		else
 			return 1;
 	}
 	
 	/**
 	 * @param course
 	 * 	a course
 	 * @return
 	 * 	the average progress in credits, assuming a credit requires 28 hours of work
 	 */
 	public static double averageCreditProgress(Course course){
 		List<Student> allStudents = StudentRegistry.getUsers();
 		double all = 0;
 		long total = 0;
 		for(Student student: allStudents){
 			ArrayList<CourseContract> contracts = student.getCourses();
 			if(contracts!=null){
 				for(CourseContract contract : contracts){
 					if(contract.getCourse().equals(course)){
 						all += creditProgress(course,student);
 						total++;
 						break;
 					}
 				}
 			}
 		}
 		double div = all/total;
 		if(total==0)
 			return  0;
 		else
 			return div;
 	}
 	
 	public static long[][] getPeopleStatsCourse(int sections, Course course){
 		long[][] timeMatrix = new long[2][sections];
 		ArrayList<Long> times = new ArrayList<Long>();
 		long maxTime = 0;
 		for(Student student : StudentRegistry.getUsers()){
			if(student.getCourseList().contains(course)){
 				long time = getTime(course, student.getStudyMoments());
 				times.add(time);
 				if(time > maxTime)
 					maxTime = time;
 			}
 		}
 		for(int i=0; i < sections; i++){
 			timeMatrix[0][i] = ((i+1)*maxTime)/sections;
 			timeMatrix[1][i] = 0;
 		}
 		for(long time : times){
 			for(int i=0; i < sections; i++){
 				if(time <= timeMatrix[0][i]){
 					timeMatrix[1][i]++;
 					break;
 				}
 			}
 		}
 		return timeMatrix;
 	}
 	
 	public static long[][] getPeopleStats(int sections){
 		long[][] timeMatrix = new long[2][sections];
 		ArrayList<Long> times = new ArrayList<Long>();
 		long maxTime = 0;
 		for(Student student : StudentRegistry.getUsers()){
 			long time = getTotalTime(student.getStudyMoments());
 			times.add(time);
 			if(time > maxTime)
 				maxTime = time;
 		}
 		for(int i=0; i < sections; i++){
 			timeMatrix[0][i] = ((i+1)*maxTime)/sections;
 			timeMatrix[1][i] = 0;
 		}
 		for(long time : times){
 			for(int i=0; i < sections; i++){
 				if(time <= timeMatrix[0][i]){
 					timeMatrix[1][i]++;
 					break;
 				}
 			}
 		}
 		return timeMatrix;
 	}
 	
 }
