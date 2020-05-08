 package MainDriver;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Vector;
 
 import Loader.*;
 import PropositionalLogic.base.*;
 import RawData.*;
 import Search.*;
 
 public class MainDriver {
 	
 	private static String path = "sampleData/dummy/";
 	private static String usrPreFile = path+"preference";
 	private static String reqFile = path+"quests";
 	private static String coursesFile = path+"events.txt";
 	
 	Loader fl;
 	Preprocessor prepro;
 	Maker schedulerMaker;
 	SearchHelper helper;
 	
 	UserPreference usrPre;		// user preference
 	List<Quest> reqList;		// graduation requirement
 	List<Event> courseList;		// all courses
 	List<Event> schedule;		// output schedule	
 	List<Event> domain; 		// courses that are available
 	Sentence subDomain;			// user'major/general requirement
 	
 	public MainDriver(){
 		fl = new FileLoader();
 		schedulerMaker = new SMaker();
 		helper = new SSearchHelper();
 		
 		usrPre = fl.loadPreference(usrPreFile);
 		reqList = fl.loadQuests(reqFile);
 		courseList = fl.loadEvents(coursesFile);
 		prepro = new Preprocessor(courseList, reqList, usrPre);
 		domain = prepro.getUpdatedCourseList();
 		subDomain = prepro.getRequirement();
 	}
 	
 	public static void main(String[] arg){
 		MainDriver student = new MainDriver();
 		
 		boolean goodSchedule = false;
 		do{
 			// make a schedule
 			student.schedule = student.schedulerMaker.make(student.subDomain, student.domain, student.usrPre);
 			
 			// print schedule
 			student.printSchedule();
 			
 			// ask feedback and update domain/sub-domain
 			goodSchedule = student.userFeedback();
 			
 		}while(!goodSchedule);
 		
 		System.out.println("You schedule:");
 		student.printSchedule();
 	}
 	
 	/**
 	 * If user satisfy with the schedule, return true. Otherwise, return false.
 	 * */
 	public boolean userFeedback(){
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		System.out.println("Enter 0 to accept this schedule.");
 		System.out.println("Enter the classIndex seperate by comma of which class you don't like.");
 		System.out.println("Ex: 1,3,4 (Give me another suggestion other than class of index 1, 3, and 4)");
 		
 		try {
 			String feedback = br.readLine();
 			String token[] = feedback.split(",");
 			
 			List<Integer> unlikeIndex = new Vector<Integer>();
 			for(String t : token){
 				Integer ti = Integer.parseInt(t);
 				if(ti == 0)
 					return true;
 				unlikeIndex.add(ti-1);
 			}
 			
 			// update domain and sub-domain with user feedback
 			updateInfor(unlikeIndex);	
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 		return false;	
 	}
 	
 	/* update information (domain and sub-domain) */
 	public void updateInfor(List<Integer> index){
 		for(int i : index){
 			if(i < 0 || i >= schedule.size())
 				continue;
 			Event course = schedule.get(i);
 			domain.remove(course);
 			helper.cutClauses(subDomain, course.getTitle());
 		}
 	}
 	
 	/* Print the schedule */
 	public void printSchedule(){
 		for(Event course : schedule){
 			course.printBriefInfor();
 		}
 	}
 	
 	
 	public static void printScheduler(List<Event> schedule){
 	
 	}
 	
 	
 }
