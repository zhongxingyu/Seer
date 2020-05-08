 package dk.drb.blacktiger.controller;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import dk.drb.blacktiger.model.CallInformation;
 import dk.drb.blacktiger.service.IBlackTigerService;
 import org.apache.commons.lang.time.DateUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.AnonymousAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Controller for viewing reports.
  */
 @Controller
 public class ReportController {
 
     @Autowired
     private IBlackTigerService service;
     
     @RequestMapping("/reports/{roomNo}")
     public ModelAndView showReport(@PathVariable String roomNo, @RequestParam(defaultValue = "0") int hourStart, 
         @RequestParam(defaultValue = "23") int hourEnd, @RequestParam(defaultValue = "0") int duration) {
         Date dateStart = DateUtils.truncate(new Date(), Calendar.HOUR_OF_DAY);
         Date dateEnd = DateUtils.truncate(new Date(), Calendar.HOUR_OF_DAY);
         int durationInSeconds = duration*60;
         
        
         //Adjust dates
         dateStart = DateUtils.setHours(dateStart, hourStart);
         dateEnd = DateUtils.setHours(dateEnd, hourEnd);
         
         List<CallInformation> callInfos = service.getReport(roomNo, dateStart, dateEnd, durationInSeconds);
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("callInfos", callInfos);
         model.put("roomNo", roomNo);
         model.put("reportDate", dateStart);
         model.put("reportHourStart",hourStart);
         model.put("reportHourEnd",hourEnd);
         model.put("reportMinimumDuration",duration);
         return new ModelAndView("report", model);
     }
     
     @RequestMapping("/reports")
     public String redirectToReport() {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         if(auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
             return "redirect:/reports/" + auth.getName() + "1";
         } else {
             return "redirect:/";
         }
     }
     
 }
