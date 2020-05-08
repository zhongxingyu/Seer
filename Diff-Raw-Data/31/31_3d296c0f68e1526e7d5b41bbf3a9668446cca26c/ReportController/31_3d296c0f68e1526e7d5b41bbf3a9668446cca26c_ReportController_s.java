 package dk.drb.blacktiger.controller;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import dk.drb.blacktiger.model.CallInformation;
 import dk.drb.blacktiger.service.CallInformationService;
 import org.apache.commons.lang.time.DateUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Controller for viewing reports.
  */
 @Controller
 public class ReportController {
 
     @Autowired
     private CallInformationService service;
     
     @RequestMapping("/reports/{roomNo}")
     @ResponseBody
    public List<CallInformation> showReportAsJson(@PathVariable String roomNo, @RequestParam(defaultValue = "0") int hourStart, 
         @RequestParam(defaultValue = "23") int hourEnd, @RequestParam(defaultValue = "0") int duration) {
         return resolveCallInformations(roomNo, hourStart, hourEnd, duration);
     }
     
     
     private List<CallInformation> resolveCallInformations(String roomNo, int hourStart, int hourEnd, int duration) {
         Date dateStart = DateUtils.truncate(new Date(), Calendar.HOUR_OF_DAY);
         Date dateEnd = DateUtils.truncate(new Date(), Calendar.HOUR_OF_DAY);
         int durationInSeconds = duration*60;
         
         //Adjust dates
         dateStart = DateUtils.setHours(dateStart, hourStart);
         dateEnd = DateUtils.setHours(dateEnd, hourEnd);
         
         return service.getReport(roomNo, dateStart, dateEnd, durationInSeconds);
     }
     
 }
