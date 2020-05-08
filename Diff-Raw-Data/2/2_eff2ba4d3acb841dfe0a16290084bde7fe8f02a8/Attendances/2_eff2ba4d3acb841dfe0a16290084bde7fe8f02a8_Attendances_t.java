 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers;
 
 import java.util.Map;
 import models.CommentDay;
 import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Set;
 import models.Attendance;
 import models.AttendanceLine;
 import models.Department;
 import models.Employer;
 import models.Person;
 import models.Planning;
 import models.User;
 import play.mvc.Controller;
 import play.mvc.With;
 import utils.Dates;
 import utils.Validate;
 import static play.modules.pdf.PDF.*;
 
 /**
  *
  * @author inf04
  */
 @With(Secure.class)
 public class Attendances extends Controller {
 
     @Check(value = {"adminRH", "userRH", "adminINS", "userINS", "divisionChief"})
     public static void byWeekAndDepartment(Date current, int page, long departmentID) {
 
         if (Validate.isNull(current)) {
             current = new Date();
         }
 
         List<Department> departments = null;
 
         User user = User.find("username = ?", Secure.Security.connected()).first();
 
         boolean canViewPlanning = user.canViewPlanning();
 
         departments = Department.getDepartmentsFromUser(user);
 
         Department department = null;
         if (departmentID > 0) {
             department = Department.findById(departmentID);
         } else {
             department = departments.get(0);
         }
 
         if (!departments.contains(department)) {
             forbidden();
         }
 
         Planning planning = null;
 
         if (canViewPlanning) {
             planning = Planning.byDepartment(department);
         }
 
         GregorianCalendar date = new GregorianCalendar();
         date.setTimeInMillis(current.getTime());
 
         date.add(Calendar.WEEK_OF_YEAR, page);
         current = date.getTime();
 
         Calendar firstDay = Dates.getFirstDayOfWeek(date);
         Calendar lastDay = Dates.getLastDayOfWeek(date);
         int weekYear = date.get(Calendar.WEEK_OF_YEAR);
 
 
         List<Calendar> dates = Dates.getDatesOfWeek(date);
 
         Set<AttendanceLine> attendanceLines = null;
         if (canViewPlanning) {
             attendanceLines = Attendance.byWeekAndDepartment(date, department);
         } else {
             attendanceLines = Attendance.byWeekAndDepartmentForINS(date, department);
         }
 
         render(attendanceLines, department, current, page, departments,
                 dates, weekYear, firstDay, lastDay, planning, canViewPlanning);
     }
 
     @Check(value = {"adminRH", "userRH", "adminINS", "userINS", "divisionChief"})
     public static void byMonthAndDepartment(Date current, int page, long departmentID) {
         if (Validate.isNull(current)) {
             current = new Date();
         }
 
         List<Department> departments = null;
 
         User user = User.find("username = ?", Secure.Security.connected()).first();
         if (user.hasRole("allDepartment")) {
             departments = Department.getAll();
         } else {
             departments = user.person.getHisDepartments();
         }
 
         Department department = null;
         if (departmentID > 0) {
             department = Department.findById(departmentID);
         } else {
             department = departments.get(0);
         }
 
         if (!departments.contains(department)) {
             forbidden();
         }
 
         GregorianCalendar date = new GregorianCalendar();
         date.setTimeInMillis(current.getTime());
 
         date.add(Calendar.MONTH, page);
         current = new Date(date.getTimeInMillis());
 
         String monthName = Dates.getMonthName(date);
         List<Calendar> monthDays = Dates.getDaysOfMonth(date);
 
         Set<AttendanceLine> attendanceLines = null;
         boolean canViewPlanning = user.canViewPlanning();
         if (canViewPlanning) {
             attendanceLines = Attendance.byMonthAndDepartment(date, department);
         } else {
             attendanceLines = Attendance.byMonthAndDepartmentForINS(date, department);
         }
 
 
         render(attendanceLines, department, current, page, departments,
                 monthDays, monthName);
     }
 
     @Check(value = {"adminRH", "userRH", "adminINS", "userINS", "divisionChief"})
     public static void save(Attendance attendance, boolean noWorkThisDay,
             boolean shortAttendance, boolean cpas) {
         Calendar dayOfMonth = attendance.dayOfMonth;
         Person person = attendance.person;
         Department department = attendance.department;
 
         if (noWorkThisDay) {
             Attendance.remove(attendance);
         } else {
             Attendance.update(attendance);
         }
 
         render(attendance, dayOfMonth, person, department, shortAttendance);
     }
 
     public static void load(long attendanceID, Calendar dayOfMonth, long personID,
             long departmentID, long employerID) {
         Attendance attendance = Attendance.findById(attendanceID);
         Person person = Person.findById(personID);
         Department department = Department.findById(departmentID);
         Employer employer = Employer.findById(employerID);
 
         render(attendance, dayOfMonth, person, department, employer);
     }
 
     @Check(value = {"adminINS", "userINS"})
     public static void byMonhtAndCPAS(Date current, int page, long cpasID) {
         if (Validate.isNull(current)) {
             current = new Date();
         }
 
         GregorianCalendar date = new GregorianCalendar();
         date.setTimeInMillis(current.getTime());
 
         date.add(Calendar.MONTH, page);
         current = new Date(date.getTimeInMillis());
 
         String monthName = Dates.getMonthName(date);
         List<Calendar> monthDays = Dates.getDaysOfMonth(date);
 
         List<Employer> cpasList = Employer.getCPAS();
 
         Employer cpas = Employer.findById(cpasID);
         if (cpas == null) {
             cpas = cpasList.get(0);
         }
        
        flash.remove("cpas");
 
         Set<AttendanceLine> attendanceLines = Attendance.byMonthAndCPAS(date, cpas);
 
         render(attendanceLines, cpas, current, page, cpasList,
                 monthDays, monthName);
     }
 
     @Check(value = {"adminINS", "userINS"})
     public static void byMonhtAndCPASPDF(Date current, int page, long cpasID) {
         if (Validate.isNull(current)) {
             current = new Date();
         }
 
         GregorianCalendar date = new GregorianCalendar();
         date.setTimeInMillis(current.getTime());
 
         date.add(Calendar.MONTH, page);
         current = new Date(date.getTimeInMillis());
 
         String monthName = Dates.getMonthName(date);
         List<Calendar> monthDays = Dates.getDaysOfMonth(date);
 
         List<Employer> cpasList = Employer.getCPAS();
 
         Employer cpas = Employer.findById(cpasID);
         if (cpas == null) {
             cpas = cpasList.get(0);
         }
 
         Set<AttendanceLine> attendanceLines = Attendance.byMonthAndCPAS(date, cpas);
         Map<Person, List<CommentDay>> commentsByPerson =
                 Attendance.commentsByPerson(attendanceLines);
 
         flash.put("cpas", "true");
 
         Options o = new Options();
         o.pageSize = IHtmlToPdfTransformer.A4L;
 
         renderPDF(o, attendanceLines, cpas, current, page, cpasList,
                 monthDays, monthName, commentsByPerson);
     }
 }
