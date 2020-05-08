 package org.motechproject.care.reporting.web.controller;
 
 import org.motechproject.care.reporting.constants.EventConstants;
 import org.motechproject.commcare.provider.sync.diagnostics.DiagnosticsResult;
 import org.motechproject.commcare.provider.sync.diagnostics.DiagnosticsStatus;
 import org.motechproject.commcare.provider.sync.diagnostics.SchedulerDiagnosticService;
 import org.quartz.SchedulerException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.ArrayList;
 
 @Controller
 @RequestMapping(value = "/web-api")
 public class SchedulerDiagnosticController {
 
     private SchedulerDiagnosticService schedulerDiagnosticService;
 
     @Autowired
     public SchedulerDiagnosticController(SchedulerDiagnosticService schedulerDiagnosticService) {
         this.schedulerDiagnosticService = schedulerDiagnosticService;
     }
 
    @RequestMapping(value = "/diagnostics/scheduler-care-reporting", method = RequestMethod.GET)
     @ResponseBody
     public String careReportingSchedulerStatus() throws SchedulerException {
         DiagnosticsResult diagnosticsResult = schedulerDiagnosticService.diagnoseSchedules(getSchedulesToDiagnose());
 
         return diagnosticsResult.getStatus().equals(DiagnosticsStatus.PASS)
                 ? "SUCCESS"
                 : String.format("FAILURE\n%s", diagnosticsResult.getMessage());
     }
 
     private ArrayList<String> getSchedulesToDiagnose() {
          ArrayList<String> schedules = new ArrayList<>();
         schedules.add(EventConstants.COMPUTE_FIELDS + "-" + EventConstants.COMPUTE_FIELDS_JOB_ID_KEY);
         return schedules;
     }
 }
