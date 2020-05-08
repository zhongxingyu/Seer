 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import tim.application.Config;
 import tim.application.utils.DateHelper;
 import tim.controller.ApplicationController;
 import tim.controller.CalendarController;
 import tim.model.AppointmentModel;
 import tim.model.Employee;
 import tim.model.EmployeeModel;
 
 
 public class CalendarTest {
 
 	public static void main(String[] args) {
 		ApplicationController applicationController = new ApplicationController();
 		applicationController.init();
 		
 		CalendarController calendarController = new CalendarController();
 		calendarController.addModel(new AppointmentModel());
 		calendarController.addModel(new EmployeeModel());
 		
 		Date begin = null;
 		try {
			begin = DateHelper.StringToDate("2011-05-14");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Date end = null;
 		try {
 			end = DateHelper.StringToDate("2011-06-15");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		ArrayList<Employee> employees = calendarController.getCalendars(begin, end);
 		
 		for (Employee employee : employees) {
			System.out.println(employee.getCalendar().getAppointments().get(0));
 		}
 	}
 
 }
