 package handler;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import scheduleGenerator.student.SelectedCourse;
 
 
 import scheduleGenerator.Constants;
 import scheduleGenerator.Constants.Day;
 import scheduleGenerator.Constants.Season;
 import scheduleGenerator.ScheduleFactory;
 import scheduleGenerator.student.Preference;
 import scheduleGenerator.student.StudentSchedule;
 
 import scheduleGenerator.time.TimeSlot;
 
 import authenticator.Authenticator;
 
 import database.Database;
 
 import java.lang.String;
 
 /**
  * Servlet implementation class Handler
  */
 
 
 public class Handler extends HttpServlet {
 	/**
 	 * Global variables
 	 */
 	static Preference pref = new Preference(0, Season.NONE);
 	/***************************/
 	
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Handler() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see Servlet#init(ServletConfig)
 	 */
 	/*public void init(ServletConfig config) throws ServletException {
 		// TODO Auto-generated method stub
 	}*/
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		System.out.println("INSIDE DO GET");
 		
 		/**
 		 * Authenticator
 		 */
 		
 		if(request.getParameter("action").equals("authenticator")){
 			String username = request.getParameter("username");
 			String password = request.getParameter("password");
 			response.setContentType("text/html");
 			PrintWriter out =  response.getWriter();
 
 			Authenticator authen = new Authenticator();
 			boolean log = authen.login(username, password);
 			out.print(log);
 			out.close();
 		}
 		
 		/*********************************************/
 		
 		
 		/**
 		 * Preferences
 		 */
 		if(request.getParameter("action").equals("preference")){
 			response.setContentType("text/html");
 			PrintWriter out =  response.getWriter();
 			
 			if(request.getParameter("option").equals("add")){
 				boolean addr=false;
 				String startTime = request.getParameter("start_time");
 				String endTime = request.getParameter("end_time");
 				String day = request.getParameter("day");
 				
 				System.out.println("starttime: "+startTime);
 				System.out.println("endtime: "+endTime);
 				System.out.println("day: "+day);
 				addr=pref.addBreak(startTime, endTime, Constants.ConvertStringToDay(day));
 				out.print(addr);
 			}
 			
 			else if(request.getParameter("option").equals("remove")){
 				boolean remover=false;
 				int index = Integer.parseInt(request.getParameter("index"));
 				System.out.println("breakindex: "+index);
 				remover=pref.removeBreak(index);
 				out.print(remover);
 			}
 			
 			else if(request.getParameter("option").equals("setMaxCourses")){
 				int maxCourses = Integer.parseInt(request.getParameter("maxCoursesNum"));
 				pref.setMaxCourseNum(maxCourses);
 			}
 			
 			else if(request.getParameter("option").equals("setSemester")){
 				String semester = request.getParameter("newSemester");
 				pref.setSeason(Constants.ConvertStringToSeason(semester));
 			}
 			
 			out.close();
 		}
 		
 		
 		/*********************************************/
 		
 		/**
 		 * View Schedule
 		 */
 		
 		if(request.getParameter("action").equals("view_schedule")){
 			System.out.println("in view");
 			StudentSchedule schedule = StudentSchedule.getFakeSchedule();
 			response.setContentType("text/html");
 			PrintWriter out =  response.getWriter();
 			out.println(schedule.printSchedule());
 			out.close();
 		}
 		
 		/*********************************************/
 		
 		/**
 		 * Generate Schedule
 		 */
 		
 		if(request.getParameter("action").equals("gen_schedule")){
 			//Preference pref1 = new Preference(4, Season.WINTER);
 			System.out.println("in gen");
 			String studentId = request.getParameter("username");
 			System.out.println("in gen: "+studentId);
 			ScheduleFactory factory = new ScheduleFactory();
 			System.out.println("pref "+pref.getSeason().toString());
			StudentSchedule schedule = factory.makeStudentSchedule(studentId, pref);
 			response.setContentType("text/html");
 			PrintWriter out =  response.getWriter();
 			out.println(schedule.printSchedule());
 			out.close();
 		}
 		
 		/*********************************************/
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 
 	}
 
 }
