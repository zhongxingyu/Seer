 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.emr.fragment.controller.paperrecord;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Person;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
 import org.openmrs.module.paperrecord.PaperRecordMergeRequest;
 import org.openmrs.module.paperrecord.PaperRecordRequest;
 import org.openmrs.module.paperrecord.PaperRecordService;
 import org.openmrs.module.paperrecord.UnableToPrintLabelException;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.fragment.action.FailureResult;
 import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
 import org.openmrs.ui.framework.fragment.action.SuccessResult;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ArchivesRoomFragmentController {
 
     private final Log log = LogFactory.getLog(getClass());
 
     // TODO: should we make sure that all these calls are wrapped in try/catch so that we can return
     // TODO: some kind of error message in the case of unexpected error?
 
     // TODO: can we use something in the UiUtils method to do this
 
     private DateFormat timeAndDateFormat = new SimpleDateFormat("HH:mm dd/MM");
 
     private DateFormat dateAndTimeFormat = new SimpleDateFormat("dd/MM HH:mm");
 
     public List<SimpleObject> getOpenRecordsToPull(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                    @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                                    UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToPull();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, paperRecordService, emrApiProperties, ui);
         }
 
         return results;
     }
 
     public List<SimpleObject> getOpenRecordsToCreate(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                         @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                                         UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, paperRecordService, emrApiProperties, ui);
         }
 
         return results;
     }
 
     public List<SimpleObject> getOpenRecordsToMerge(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                      UiUtils ui) {
 
         List<PaperRecordMergeRequest> requests = paperRecordService.getOpenPaperRecordMergeRequests();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordMergeRequestsToSimpleObjects(requests, ui);
         }
 
         return results;
     }
 
     public List<SimpleObject> getAssignedRecordsToPull(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                        @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                                        UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToPull();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, paperRecordService, emrApiProperties, ui);
         }
 
         return results;
     }
 
     public List<SimpleObject> getAssignedRecordsToCreate(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                          @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                                          UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToCreate();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, paperRecordService, emrApiProperties, ui);
         }
 
         return results;
     }
 
     public FragmentActionResult assignPullRequests(@RequestParam("requestId[]") List<PaperRecordRequest> requests,
                                                    @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                    EmrContext emrContext, UiUtils ui) {
 
         Person assignTo = emrContext.getUserContext().getAuthenticatedUser().getPerson();
 
         try {
             paperRecordService.assignRequests(requests, assignTo, emrContext.getSessionLocation());
             return new SuccessResult(ui.message("emr.archivesRoom.pullRequests.message"));
         }
         catch (UnableToPrintLabelException ex) {
             log.error("Unable to assign pull requests", ex);
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToPrintLabel"));
         }
         catch (IllegalStateException ex) {
             log.error("Unable to assign pull requests", ex);
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToAssignRecords"));
         }
 
     }
 
     public FragmentActionResult assignCreateRequests(@RequestParam("requestId[]") List<PaperRecordRequest> requests,
                                                      @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                      EmrContext emrContext, UiUtils ui) {
 
         Person assignTo = emrContext.getUserContext().getAuthenticatedUser().getPerson();
 
         try {
             paperRecordService.assignRequests(requests, assignTo, emrContext.getSessionLocation());
             return new SuccessResult(ui.message("emr.archivesRoom.createRequests.message"));
         }
         catch (UnableToPrintLabelException ex) {
             log.error("Unable to assign create requests", ex);
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToPrintLabel"));
         }
         catch (IllegalStateException ex) {
             log.error("Unable to assign create requests", ex);
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToAssignRecords"));
         }
 
     }
 
 
     public FragmentActionResult markPaperRecordsAsMerged(@RequestParam("mergeId") PaperRecordMergeRequest request,
                                                         @SpringBean("paperRecordService") PaperRecordService paperRecordService){
 
         paperRecordService.markPaperRecordsAsMerged(request);
 
         // TODO: we will need to localize this if we decide we actually want to display an alert or growl here
         return new SuccessResult("Ok");
     }
 
 
     public FragmentActionResult markPaperRecordRequestAsSent(@RequestParam(value = "identifier", required = true) String identifier,
                                                              @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                              UiUtils ui) {
 
         try {
             // fetch the pending request associated with this message
             PaperRecordRequest paperRecordRequest = paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier);
 
             if (paperRecordRequest == null) {
                 // if no matching request found, determine what error we need to return
                 List<PaperRecordRequest> sentRequests = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier);
                 if (sentRequests == null || sentRequests.size() == 0) {
                     return new FailureResult(ui.message("emr.archivesRoom.error.paperRecordNotRequested", ui.format(identifier)));
                 }
                 else {
                     // note that if for some reason there are multiple sent requests, we just return the identifier of
                     // the last request in the list (under the possibly faulty assumption that this is the most recent)
                     return new FailureResult(ui.message("emr.archivesRoom.error.paperRecordAlreadySent", ui.format(sentRequests.get(sentRequests.size() - 1).getIdentifier()),
                             ui.format(sentRequests.get(sentRequests.size() - 1).getRequestLocation()), ui.format(sentRequests.get(sentRequests.size() - 1).getDateStatusChanged())));
                 }
             }
            else {
                 // otherwise, mark the record as sent
                 paperRecordService.markPaperRecordRequestAsSent(paperRecordRequest);
                 return new SuccessResult(
                         "<span class=\"toast-record-found\">" + 
                             ui.message("emr.archivesRoom.requestedBy.label") + 
                             "<span class=\"toast-record-location\">" +  
                                 ui.format(paperRecordRequest.getRequestLocation()) + 
                             "</span>" +
                             ui.message("emr.archivesRoom.recordNumber.label") + 
                             "<span class=\"toast-record-id\">" + 
                                 ui.format(paperRecordRequest.getIdentifier()) +
                             "</span>" + 
                         "</span>");
             }
         }
         catch (Exception e) {
             // generic catch-all
             log.error("Unable to mark paper record request as sent", e);
             return new FailureResult(ui.message("emr.error.systemError"));
         }
 
     }
 
     public FragmentActionResult markPaperRecordRequestAsReturned(@RequestParam(value = "identifier", required = true) String identifier,
                                                                 @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                                 @SpringBean("emrContext") EmrContext emrContext,
                                                                 UiUtils ui) {
 
         try {
             // fetch the send requests associated with this message
             List<PaperRecordRequest> sentRequests = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier);
 
             // handle not finding a match
             if (sentRequests == null || sentRequests.size() == 0) {
                 // as long as this identifier exists, we can return a success message (no error if they mistakenly scan a record twice)
                 if (paperRecordService.paperRecordExistsWithIdentifier(identifier, emrContext.getSessionLocation())
                         || paperRecordService.paperRecordExistsForPatientWithIdentifier(identifier, emrContext.getSessionLocation()) ) {
                     return new SuccessResult(ui.message("emr.archivesRoom.recordReturned.message"));
                 }
                 else {
                     return new FailureResult(ui.message("emr.archivesRoom.error.noPaperRecordExists"));
                 }
             }
 
             // mark all the records as returned
             for (PaperRecordRequest request : sentRequests) {
                 paperRecordService.markPaperRecordRequestAsReturned(request);
             }
 
             return new SuccessResult(ui.message("emr.archivesRoom.recordReturned.message") + "<br/><br/>"
                     + ui.message("emr.archivesRoom.recordNumber.label") + " " + ui.format(sentRequests.get(0).getIdentifier()));
         }
         catch (Exception e) {
             // generic catch-all
             log.error("Unable to mark paper record request as returned", e);
             return new FailureResult(ui.message("emr.error.systemError"));
         }
 
     }
 
     public FragmentActionResult markPaperRecordRequestAsCancelled(@RequestParam("requestId") PaperRecordRequest request,
                                            @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                            EmrContext emrContext,
                                            UiUtils ui) {
 
         try {
             paperRecordService.markPaperRecordRequestAsCancelled(request);
             return new SuccessResult();
         }
         catch (Exception e) {
             log.error("Unable to mark paper record request as cancelled", e);
             return new FailureResult(ui.message("emr.error.systemError"));
 
         }
     }
 
     public FragmentActionResult printLabel(@RequestParam("requestId") PaperRecordRequest request,
                                              @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                              EmrContext emrContext,
                                              UiUtils ui) {
 
         try {
             paperRecordService.printPaperRecordLabel(request, emrContext.getSessionLocation());
             return new SuccessResult(ui.message("emr.archivesRoom.printedLabel.message", request.getIdentifier()));
         }
         catch (Exception e) {
            log.error("User " + emrContext.getUserContext().getAuthenticatedUser() + " unable to print paper record label at location "
                    + emrContext.getUserContext().getLocation(), e);
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToPrintLabel"));
 
         }
 
     }
 
     private List<SimpleObject> convertPaperRecordRequestsToSimpleObjects(List<PaperRecordRequest> requests,
                                                                          PaperRecordService paperRecordService,
                                                                          EmrApiProperties emrApiProperties, UiUtils ui) {
 
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         for (PaperRecordRequest request : requests) {
             SimpleObject result = SimpleObject.fromObject(request, ui, "requestId", "patient", "identifier", "requestLocation");
 
             // manually add the date and patient identifier
             result.put("dateCreated", timeAndDateFormat.format(request.getDateCreated()));
             result.put("dateCreatedSortable", request.getDateCreated()) ;
             result.put("patientIdentifier", ui.format(request.getPatient().getPatientIdentifier(emrApiProperties.getPrimaryIdentifierType()).getIdentifier()));
 
             // add the last sent and last sent date to any pending pull requests
             if (request.getStatus().equals(PaperRecordRequest.Status.ASSIGNED_TO_PULL)
                     || (request.getStatus().equals(PaperRecordRequest.Status.OPEN) && StringUtils.isNotBlank(request.getIdentifier()))) {
 
                 PaperRecordRequest lastSentRequest = paperRecordService.getMostRecentSentPaperRecordRequestByIdentifier(request.getIdentifier());
 
                 if (lastSentRequest != null) {
                     result.put("locationLastSent", ui.format(lastSentRequest.getRequestLocation()));
                     result.put("dateLastSent", timeAndDateFormat.format(lastSentRequest.getDateStatusChanged()));
                 }
 
             }
 
             results.add(result);
         }
 
         return results;
     }
 
 
     private List<SimpleObject> convertPaperRecordMergeRequestsToSimpleObjects(List<PaperRecordMergeRequest> requests, UiUtils ui) {
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         for (PaperRecordMergeRequest request : requests) {
             SimpleObject result = createASingleMergeRequestResult(ui, request);
 
             results.add(result);
         }
 
         return results;
     }
 
     private SimpleObject createASingleMergeRequestResult(UiUtils ui, PaperRecordMergeRequest request) {
         SimpleObject result = SimpleObject.fromObject(request, ui, "mergeRequestId", "preferredIdentifier", "notPreferredIdentifier");
 
         result.put("dateCreated", dateAndTimeFormat.format(request.getDateCreated()));
         result.put("dateCreatedSortable", request.getDateCreated());
 
         putDataFromPreferredPatient(request, result);
         putDataFromNonPreferredPatient(request, result);
 
         return result;
     }
 
     private void putDataFromNonPreferredPatient(PaperRecordMergeRequest request, SimpleObject result) {
         PatientDomainWrapper notPreferredPatient = new PatientDomainWrapper();
         notPreferredPatient.setPatient(request.getNotPreferredPatient());
 
         result.put("notPreferredName", notPreferredPatient.getFormattedName());
     }
 
     private void putDataFromPreferredPatient(PaperRecordMergeRequest request, SimpleObject result) {
         PatientDomainWrapper preferredPatient = new PatientDomainWrapper();
         preferredPatient.setPatient(request.getPreferredPatient());
 
         result.put("preferredName", preferredPatient.getFormattedName());
     }
 
 }
