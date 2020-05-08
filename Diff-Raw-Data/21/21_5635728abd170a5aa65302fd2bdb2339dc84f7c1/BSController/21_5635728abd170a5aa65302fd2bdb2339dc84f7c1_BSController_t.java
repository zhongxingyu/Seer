 package controllers;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Set;
 import models.Attendance;
 import models.AttendanceLine;
 import models.BS;
 import models.Department;
 import models.Person;
 import models.Resident;
 import models.User;
 import models.User;
 import notifiers.Notifier;
 import play.mvc.Controller;
 import play.mvc.With;
 import static play.modules.pdf.PDF.*;
 import utils.Dates;
 import utils.Utils;
 
 @With(Secure.class)
 public class BSController extends Controller {
     
     @Check(value = {"adminMA","userMA"})
     public static void index(){
        List<Person> residents = Resident.getAllResidents();
         render(residents);
     }
 
     @Check(value = {"adminMA","userMA"})
     public static void add(Calendar bsDate, long residentID, long departmentID) {
         Person resident = Person.findById(residentID);
         Department department = Department.findById(departmentID);
 
         render(bsDate, resident, department);
     }
 
     @Check(value = {"adminMA","userMA"})
     public static void save(BS bs) throws Exception {
         boolean isNew = bs.id == null;
         boolean hasChief = bs.department.person != null;
 
         User user = User.loadFromSession();
         bs.update(user.person,bs.department.person);
 
         if (isNew && hasChief) {
             Notifier.sendAlertToChief(bs);
         }
         
         flash.success("saved");
 
         edit(bs.id);
     }
     
     @Check(value = {"adminMA","userMA"})
     public static void edit(long id){
         BS bs = BS.findById(id);
         render(bs);
     }
     
     @Check(value = {"adminMA","userMA"})
     public static void delete(long id){
         BS bs = BS.findById(id);
         bs.delete();
         flash.success("deleted");
         
         index();
     }
     
     @Check(value = {"adminMA","userMA"})
     public static void exportPDF(long id){
         BS bs = BS.findById(id);
         renderPDF(bs);
     }
     
     @Check(value = {"adminMA","userMA"})
     public static void showPlanning(long id,Calendar current, int page) {
         if (Utils.isNull(current)) {
             current = new GregorianCalendar();
         }
         
         Person resident = Person.findById(id);
         Department department = resident.department;
 
        boolean isDepartmentX = false;
        Department departmentX = Department.getX();

        if(department != null && department.equals(departmentX)){
            isDepartmentX = true;
        }

         current.add(Calendar.WEEK_OF_YEAR, page);
         Calendar firstDay = Dates.getFirstDayOfWeek(current);
         Calendar lastDay = Dates.getLastDayOfWeek(current);
 
         Set<AttendanceLine> attendanceLines = Attendance.byWeekAndDepartment(current, department);
 
         List<Calendar> dates = Dates.getDatesOfWeek(current);
         int weekYear = current.get(Calendar.WEEK_OF_YEAR);
         
         List<BS> bsList = BS.byResident(resident);
         
         render(attendanceLines, department, current, page,
                dates, weekYear, firstDay, lastDay,resident,bsList,isDepartmentX);
     }
     
     public static void byDepartmentChief(){
         Person chief = User.loadFromSession().person;
         List<BS> bsList = BS.byDepartmentChief(chief);
         
         render(bsList);
     }
     
     public static void validate(long id){
         BS bs = BS.findById(id);
         bs.validate();
         
         flash.success("bsValidated");
         byDepartmentChief();
     }
 }
