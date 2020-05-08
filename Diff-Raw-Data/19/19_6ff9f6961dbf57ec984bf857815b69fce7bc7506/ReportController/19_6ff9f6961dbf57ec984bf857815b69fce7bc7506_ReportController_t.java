 package it.sevenbits.conferences.web.controller;
 
 import it.sevenbits.conferences.domain.Report;
 import it.sevenbits.conferences.service.ReportService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Controller for /report/{id} page.
  */
 @Controller
 public class ReportController {
 
     @Autowired
     private ReportService reportService;
 
     @RequestMapping(value = "/report/{reportId}", method = RequestMethod.GET)
     public ModelAndView showReport(@PathVariable(value = "reportId") String reportId) {
 
         ModelAndView modelAndView = new ModelAndView("report");
 
         Report report = reportService.findReportById(Long.parseLong(reportId));
         modelAndView.addObject("report", report);
        modelAndView.addObject("otherReports", reportService.findAllPresentedReportsByUser(report.getUser()));
 
         return modelAndView;
     }
 }
