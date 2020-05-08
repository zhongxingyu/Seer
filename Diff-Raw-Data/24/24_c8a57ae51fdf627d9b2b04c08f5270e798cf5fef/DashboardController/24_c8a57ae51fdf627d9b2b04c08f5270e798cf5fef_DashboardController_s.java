 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.controller;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import mantech.controller.helpers.TemplateKeys;
 import mantech.repository.CategoryRepository;
 import mantech.repository.ComplaintRepository;
 import mantech.repository.EquipmentRepository;
 import mantech.repository.UserRepository;
 
 /**
  * 
  * @author Tien Nguyen
  * @version $Id: DashboardController.java,v 1.0 2011/10/13 2:50:25 lilylnx Exp $
  */
 @Controller
 public class DashboardController {
   
   @Autowired
   private UserRepository userRepo;
   
   @Autowired
   private ComplaintRepository complaintRepo;
   
   @Autowired
   private EquipmentRepository equipmentRepo;
   
   @Autowired
   private CategoryRepository categoryRepo;
   
   @RequestMapping(value = "/dashboard", params = "action=overview", method = RequestMethod.GET)
   public String dashboard(ModelMap model) {
     Calendar cal = Calendar.getInstance();
     int year = cal.get(Calendar.YEAR);
     int month = (cal.get(Calendar.MONTH)) + 1;
     int month2 = month - 1;
     int dateMonth = cal.get(Calendar.DAY_OF_MONTH);
     int dateMonth2 = dateMonth - 1;
     
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 
     Date date = null,
         date2 = null;
     String s = Integer.toString(year) + "/" +
         ((month < 10)? ("0" + Integer.toString(month)): Integer.toString(month))+ "/" +
         ((dateMonth < 10)? ("0" + Integer.toString(dateMonth)): Integer.toString(dateMonth)),
         s2 = Integer.toString(year) + "/" +
             ((month < 10)? ("0" + Integer.toString(month)): Integer.toString(month))+ "/" +
             ((dateMonth2 < 10)? ("0" + Integer.toString(dateMonth2)): Integer.toString(dateMonth2));
       
     try {
       date = sdf.parse(s);
       date2 = sdf.parse(s2);
     }
     catch (ParseException e) {
       e.printStackTrace();
     }
     
     model.addAttribute("totalUsers", userRepo.count().intValue())
       .addAttribute("totalComplaints", complaintRepo.count().intValue())
       .addAttribute("totalCategories", categoryRepo.count().intValue())
       .addAttribute("totalEquipments", equipmentRepo.count().intValue())
       .addAttribute("totalComplaintsToDay", complaintRepo.countByDate(date))
       .addAttribute("totalComplaintsYesterday", complaintRepo.countByDate(date2))
       .addAttribute("totalComplaintLastMonth", complaintRepo.summaryInMonth(month2));
       
     return TemplateKeys.DASHBOARD_PAGE;
   }
   
   @RequestMapping(value = "/dashboard", params = "action=viewreports", method = RequestMethod.GET)
   public String viewReports(ModelMap model) throws Exception {
     Calendar cal = Calendar.getInstance();
     int year = cal.get(Calendar.YEAR);
     int month = (cal.get(Calendar.MONTH)) + 1;
     int dateMonth = cal.get(Calendar.DAY_OF_MONTH);
     int currWeek = cal.get(Calendar.WEEK_OF_YEAR);
 
     String s;
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 
     Date date = null;
     List<String> listDateString = new ArrayList<String>();
     List<Integer> listDateNumber = new ArrayList<Integer>();
     List<Integer> listMonth = new ArrayList<Integer>();
     List<Integer> listMonthNumber = new ArrayList<Integer>();
     List<Integer> listComplaintsInWeek = new ArrayList<Integer>();
     List<Integer> listWeeks = new ArrayList<Integer>();
     
     for (int i = 0; i < 10; i++) {
       if (i > 0) {
         dateMonth--;
       }
       try {
         s = Integer.toString(year) + "/" +
             ((month < 10)? ("0" + Integer.toString(month)): Integer.toString(month))+ "/" +
             ((dateMonth < 10)? ("0" + Integer.toString(dateMonth)): Integer.toString(dateMonth));
         date = sdf.parse(s);
         listDateNumber.add(complaintRepo.countByDate(date));
         listDateString.add(s);
       }
       catch (Exception e) {}
     }
     
     for(int i = 1; i <= 12; i++) {
       listMonthNumber.add(complaintRepo.summaryInMonth(i));
       listMonth.add(i);
     }
     
     for (int i = 0; i < 20; i++) {
       if (i > 0) {
         currWeek--;
       }
       listWeeks.add(currWeek);
       listComplaintsInWeek.add(complaintRepo.countByWeek(currWeek));
     }
     
     model.addAttribute("list", listDateNumber);
     model.addAttribute("listDate", listDateString);
     model.addAttribute("listInMonth", listMonthNumber);
     model.addAttribute("listMonth", listMonth);
     model.addAttribute("listWeeks", listWeeks);
     model.addAttribute("listComplaintsInWeek", listComplaintsInWeek);
     
     return TemplateKeys.DASHBOARD_VIEW_REPORTS;
   }
   
   @RequestMapping(value = "/dashboard", params = "action=viewcredits", method = RequestMethod.GET)
   public String viewCredits(ModelMap model) {
       // TODO Sẽ cần chỉnh sửa lại userId sẽ được lấy từ session của employee đã đăng nhập.
     model.addAttribute("currentYear", complaintRepo.summaryInCurrentYear());
     model.addAttribute("currentMonth", complaintRepo.summaryInCurrentMonth());
     model.addAttribute("currentYearByEducation", complaintRepo.summaryInCurrentYearByDepart((byte)1));
     model.addAttribute("currentYearByManagement", complaintRepo.summaryInCurrentYearByDepart((byte)2));
     model.addAttribute("currentYearByLearning", complaintRepo.summaryInCurrentYearByDepart((byte)3));
     model.addAttribute("currentYearByInternal", complaintRepo.summaryInCurrentYearByDepart((byte)4));
     model.addAttribute("currentYearByHuman", complaintRepo.summaryInCurrentYearByDepart((byte)5));
     model.addAttribute("currentMonthByEducation", complaintRepo.summaryInCurrentMonthByDepart((byte)1));
     model.addAttribute("currentMonthByManagement", complaintRepo.summaryInCurrentMonthByDepart((byte)2));
     model.addAttribute("currentMonthByLearning", complaintRepo.summaryInCurrentMonthByDepart((byte)3));
     model.addAttribute("currentMonthByInternal", complaintRepo.summaryInCurrentMonthByDepart((byte)4));
     model.addAttribute("currentMonthByHuman", complaintRepo.summaryInCurrentMonthByDepart((byte)5));
     
     model.addAttribute("listCurrentYearByEducation", complaintRepo.getCurrentYearByDepartment((byte)1));
     model.addAttribute("listCurrentYearByManagement", complaintRepo.getCurrentYearByDepartment((byte)2));
     model.addAttribute("listCurrentYearByLearning", complaintRepo.getCurrentYearByDepartment((byte)3));
     model.addAttribute("listCurrentYearByInternal", complaintRepo.getCurrentYearByDepartment((byte)4));
     model.addAttribute("listCurrentYearByHuman", complaintRepo.getCurrentYearByDepartment((byte)5));
     model.addAttribute("listCurrentMonthByEducation", complaintRepo.getCurrentMonthByDepartment((byte)1));
     model.addAttribute("listCurrentMonthByManagement", complaintRepo.getCurrentMonthByDepartment((byte)2));
     model.addAttribute("listCurrentMonthByLearning", complaintRepo.getCurrentMonthByDepartment((byte)3));
     model.addAttribute("listCurrentMonthByInternal", complaintRepo.getCurrentMonthByDepartment((byte)4));
     model.addAttribute("listCurrentMonthByHuman", complaintRepo.getCurrentMonthByDepartment((byte)5));
     return TemplateKeys.DASHBOARD_VIEW_CREDITS;
   }
   
   @RequestMapping(value = "/dashboard", params = "action=viewfaqs", method = RequestMethod.GET)
   public String viewFAQs(ModelMap model) {
     return TemplateKeys.DASHBOARD_VIEW_FAQS;
   }
 
 }
