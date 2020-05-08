 package org.openmrs.module.emr.page.controller.paperrecord;
 
 import org.openmrs.Person;
 import org.openmrs.api.APIException;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.paperrecord.PaperRecordRequest;
 import org.openmrs.module.emr.paperrecord.PaperRecordService;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpSession;
 import java.util.List;
 
 import static org.openmrs.module.emr.EmrConstants.PRIMARY_IDENTIFIER_TYPE;
 
 public class ArchivesRoomPageController {
 
     public void get(PageModel model,
                     @RequestParam(value="activeTab", required=false, defaultValue="createrequest") String activeTab,
                     @SpringBean PaperRecordService paperRecordService,
                     @SpringBean("adminService") AdministrationService administrationService) {
         List<PaperRecordRequest> openPaperRecordRequestsToPull = paperRecordService.getOpenPaperRecordRequestsToPull();
         List<PaperRecordRequest> openPaperRecordRequestsToCreate = paperRecordService.getOpenPaperRecordRequestsToCreate();
         List<PaperRecordRequest> assignedPaperRecordRequestsToPull = paperRecordService.getAssignedPaperRecordRequestsToPull();
         List<PaperRecordRequest> assignedPaperRecordRequestsToCreate = paperRecordService.getAssignedPaperRecordRequestsToCreate();
         String primaryIdentifierType = administrationService.getGlobalProperty(PRIMARY_IDENTIFIER_TYPE);
 
         model.addAttribute("openRequestsToCreate", openPaperRecordRequestsToCreate);
         model.addAttribute("openRequestsToPull", openPaperRecordRequestsToPull);
         model.addAttribute("assignedRequestsToPull", assignedPaperRecordRequestsToPull);
         model.addAttribute("assignedRequestsToCreate", assignedPaperRecordRequestsToCreate);
         model.addAttribute("primaryIdentifierType", primaryIdentifierType);
         model.addAttribute("activeTab", activeTab);
     }
 
     /**
      * Marks the given requests as "Assigned"
      * @param requests
      */
     public String post(@RequestParam("requestId") List<PaperRecordRequest> requests,
                        @RequestParam(value="activeTab", required=false, defaultValue="create") String activeTab,
                        @RequestParam("assignTo") Person assignTo,
                        @SpringBean PaperRecordService paperRecordService,
                        EmrContext emrContext,
                        UiUtils ui,
                        HttpSession session) {
         try {
             paperRecordService.assignRequests(requests, assignTo, emrContext.getSessionLocation());
         } catch (IllegalStateException ex) {
             // TODO fix this
             throw new APIException(ex);
         }
         return "redirect:emr/paperrecord/archivesRoom.page?activeTab=" + activeTab;
     }
 
 }
