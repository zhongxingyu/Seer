 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.controller;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.tools.ant.types.resources.comparators.Reverse;
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
    model.addAttribute("totalUsers", userRepo.count().intValue())
      .addAttribute("totalComplaints", complaintRepo.count().intValue())
      .addAttribute("totalCategories", categoryRepo.count().intValue())
      .addAttribute("totalEquipments", equipmentRepo.count().intValue());
     return TemplateKeys.DASHBOARD_PAGE;
   }
   
   @RequestMapping(value = "/dashboard", params = "action=viewreports", method = RequestMethod.GET)
   public String viewReports(ModelMap model) {
     return TemplateKeys.DASHBOARD_VIEW_REPORTS;
   }
   
   @RequestMapping(value = "/dashboard", params = "action=viewcredits", method = RequestMethod.GET)
   public String viewCredits(ModelMap model) {
       // TODO Sẽ cần chỉnh sửa lại userId sẽ được lấy từ session của employee đã đăng nhập.
       Calendar cal = Calendar.getInstance();
       int year = cal.get(Calendar.YEAR);
       int month = (cal.get(Calendar.MONTH)) + 1;
       int dateMonth = cal.get(Calendar.DAY_OF_MONTH);
       String s;
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 
       Date date = null;
       List<String> listString = new ArrayList<String>();
       List<Integer> listNumber = new ArrayList<Integer>();
       for (int i = 0; i < 10; i++) {
         if (i > 0) {
           dateMonth--;
         }
         try {
           s = Integer.toString(year) + "/" +
               ((month < 10)? ("0" + Integer.toString(month)): Integer.toString(month))+ "/" +
               ((dateMonth < 10)? ("0" + Integer.toString(dateMonth)): Integer.toString(dateMonth));
           date = sdf.parse(s);
           listNumber.add(complaintRepo.countByDate(date));
           listString.add(s);
         }
         catch (Exception e) {}
       }
       model.addAttribute("list", listNumber);
       model.addAttribute("listDate", listString);
     //------------------------------------
       model.addAttribute("currentYear", complaintRepo.sumaryInCurrentYear());
       model.addAttribute("currentMonth", complaintRepo.sumaryInCurrentMonth());
       model.addAttribute("currentYearByEducation", complaintRepo.sumaryInCurrentYearByDepart((byte)1));
       model.addAttribute("currentYearByManagement", complaintRepo.sumaryInCurrentYearByDepart((byte)2));
       model.addAttribute("currentYearByLearning", complaintRepo.sumaryInCurrentYearByDepart((byte)3));
       model.addAttribute("currentYearByInternal", complaintRepo.sumaryInCurrentYearByDepart((byte)4));
       model.addAttribute("currentYearByHuman", complaintRepo.sumaryInCurrentYearByDepart((byte)5));
       model.addAttribute("currentMonthByEducation", complaintRepo.sumaryInCurrentMonthByDepart((byte)1));
       model.addAttribute("currentMonthByManagement", complaintRepo.sumaryInCurrentMonthByDepart((byte)2));
       model.addAttribute("currentMonthByLearning", complaintRepo.sumaryInCurrentMonthByDepart((byte)3));
       model.addAttribute("currentMonthByInternal", complaintRepo.sumaryInCurrentMonthByDepart((byte)4));
       model.addAttribute("currentMonthByHuman", complaintRepo.sumaryInCurrentMonthByDepart((byte)5));
       
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
