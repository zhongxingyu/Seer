 package handler;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.dom.Document;
 
 import com.sun.org.apache.bcel.internal.Constants;
 
 import database.Database;
 
 import scheduleGenerator.Constants.Day;
 import scheduleGenerator.Constants.Season;
 import scheduleGenerator.Constants.Year;
 import scheduleGenerator.course.Course;
 import scheduleGenerator.course.CourseId;
 import scheduleGenerator.course.ElectiveList;
 import scheduleGenerator.courseSequence.CourseSequence;
 import scheduleGenerator.student.Preference;
 import scheduleGenerator.student.SelectedCourse;
 import scheduleGenerator.student.StudentRecord;
 import scheduleGenerator.student.StudentSchedule;
 import scheduleGenerator.time.TimeSlot;
 import scheduleGenerator.ScheduleFactory;
 
 /**
  * Servlet implementation class Test
  */
 public class Test extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Test() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     
     /**
      * test makeStudentSchedule
      */
     private StudentSchedule testGenerateSchedule() {
     	System.out.println("************testing make schedule");
     	Preference preference = new Preference(5, Season.WINTER);
    	preference.addBreak("1010", "1210", Day.MONDAY);
 		System.out.println("PREFERENCE SIZE: " + preference.getTimeSlotList().size());
     	ScheduleFactory scheduleFactory = new ScheduleFactory();
 
     	StudentSchedule studentSchedule = scheduleFactory
     			.makeStudentSchedule("9384759", preference, 0, 0);
 
     	boolean testOverlap = testOverlappingOfCourses(studentSchedule);
     	System.out.println("OVERLAP " + testOverlap);
     	return studentSchedule;
     }
     
     /**
      * Tests if times are overlapping in the schedule
      * @param studentSchedule
      * @return
      */
     private boolean testOverlappingOfCourses(StudentSchedule studentSchedule) {
     	boolean overlapping = false;
     	for (int i = 0; i < studentSchedule.getSelectedCourses().size(); i++) {
 			for (int j = 0; j + 1 < studentSchedule.getSelectedCourses().get(i)
 					.getSection().getTimeSlotList().size(); j++) {
 				TimeSlot timeSlot1 = studentSchedule.getSelectedCourses().get(i)
 						.getSection().getTimeSlotList().get(j);
 				TimeSlot timeSlot2 = studentSchedule.getSelectedCourses().get(i)
 						.getSection().getTimeSlotList().get(j + 1);
 
 				if (TimeSlot.isTimeSlotOverlapping(timeSlot1, timeSlot2)) {
 					overlapping = true;
 					return overlapping;
 
 				}
 			}
 		}
     	return overlapping;
     }
     
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		System.out.println("STARTING TEST");
 		PrintWriter out =  response.getWriter();
 		//System.out.println("Testing");
 //		ScheduleFactory factory = new ScheduleFactory();
 //		for(Course c: factory.getScheduleCourseList()){
 //			if(c.getSeason() == scheduleGenerator.Constants.Season.WINTER)
 //				out.println(c.toString());
 //		}
 		out.println(testGenerateSchedule().toString());
 		out.close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		PrintWriter out =  response.getWriter();
 		out.println("servlet response");
 		out.close();
 	}
 	
 	
 
 }
