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
 
 import org.openmrs.Person;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.emr.paperrecord.NoMatchingPaperMedicalRequestException;
 import org.openmrs.module.emr.paperrecord.PaperRecordRequest;
 import org.openmrs.module.emr.paperrecord.PaperRecordService;
 import org.openmrs.module.emr.paperrecord.UnableToPrintPaperRecordLabelException;
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
 
     // TODO: should we make sure that all these calls are wrapped in try/catch so that we can return
     // TODO: some kind of error message in the case of unexpected error?
 
     // TODO: can we use something in the UiUtils method to do this
    DateFormat timeFormat = new SimpleDateFormat("dd/MM HH:mm");
 
     public List<SimpleObject> getOpenRecordsToPull(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                    @SpringBean("emrProperties") EmrProperties emrProperties,
                                                    UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToPull();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, emrProperties, ui);
         }
 
         return results;
     }
 
     public List<SimpleObject> getOpenRecordsToCreate(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                         @SpringBean("emrProperties") EmrProperties emrProperties,
                                                         UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, emrProperties, ui);
         }
 
         return results;
     }
 
 
     public List<SimpleObject> getAssignedRecordsToPull(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                        @SpringBean("emrProperties") EmrProperties emrProperties,
                                                        UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToPull();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, emrProperties, ui);
         }
 
         return results;
     }
 
     public List<SimpleObject> getAssignedRecordsToCreate(@SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                          @SpringBean("emrProperties") EmrProperties emrProperties,
                                                          UiUtils ui) {
 
         // TODO: when we have multiple archives rooms this method will have to operate by location as well
         List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToCreate();
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         if (requests != null && requests.size() > 0) {
             results = convertPaperRecordRequestsToSimpleObjects(requests, emrProperties, ui);
         }
 
         return results;
     }
 
     public FragmentActionResult assignRequests(@RequestParam("requestId[]") List<PaperRecordRequest> requests,
                                                 @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                 EmrContext emrContext, UiUtils ui) {
 
         Person assignTo = emrContext.getUserContext().getAuthenticatedUser().getPerson();
 
         try {
             paperRecordService.assignRequests(requests, assignTo, emrContext.getSessionLocation());
             return new SuccessResult(ui.message("emr.archivesRoom.assignRecords.message"));
         }
         catch (UnableToPrintPaperRecordLabelException ex) {
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToPrintLabel"));
         }
         catch (IllegalStateException ex) {
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToAssignRecords"));
         }
 
     }
 
     public FragmentActionResult markPaperRecordRequestAsSent(@RequestParam(value = "identifier", required = true) String identifier,
                                                              @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                              UiUtils ui) {
 
         try {
             // fetch the pending request associated with this message
             PaperRecordRequest paperRecordRequest = paperRecordService.getPendingPaperRecordRequestByIdentifier(identifier);
 
             if (paperRecordRequest == null) {
                 // if matching request found, determine what error we need to return
                 paperRecordRequest = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier);
                 if (paperRecordRequest == null) {
                     return new FailureResult(ui.message("emr.archivesRoom.error.paperRecordNotRequested", ui.format(identifier)));
                 }
                 else {
                     return new FailureResult(ui.message("emr.archivesRooms.error.paperRecordAlreadySent", ui.format(identifier),
                             ui.format(paperRecordRequest.getRequestLocation()), ui.format(paperRecordRequest.getDateStatusChanged())));
                 }
             }
            else {
                 // otherwise, mark the record as sent
                 paperRecordService.markPaperRecordRequestAsSent(paperRecordRequest);
                 return new SuccessResult(ui.message("emr.archivesRoom.recordFound.message") + "<br/><br/>"
                         + ui.message("emr.archivesRoom.recordNumber.label") + ": " + ui.format(identifier) + "<br/><br/>"
                         + ui.message("emr.archivesRoom.requestedBy.label") + ": " + ui.format(paperRecordRequest.getRequestLocation() + "<br/><br/>"
                         + ui.message("emr.archivesRoom.requestedAt.label") + ": " + timeFormat.format(paperRecordRequest.getDateCreated())));
             }
         }
         catch (Exception e) {
             // generic catch-all
             return new FailureResult(ui.message("emr.error.systemError"));
         }
 
     }
 
     public FragmentActionResult markPaperRecordRequestAsReturned(@RequestParam(value = "identifier", required = true) String identifier,
                                                                 @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                                                                 @SpringBean("emrContext") EmrContext emrContext,
                                                                 UiUtils ui) {
 
         try {
             paperRecordService.markPaperRecordRequestsAsReturned(identifier);
             return new SuccessResult(ui.message("emr.archivesRoom.recordReturned.message") + "<br/><br/>"
                     + ui.message("emr.archivesRoom.recordNumber.label") + ": " + ui.format(identifier));
         }
         catch (NoMatchingPaperMedicalRequestException e)  {
            // as long as this record exists, we can return a success message
             if (paperRecordService.paperRecordExists(identifier, emrContext.getSessionLocation())) {
                 return new SuccessResult(ui.message("emr.archivesRoom.recordReturned.message") + "<br/><br/>"
                         + ui.message("emr.archivesRoom.recordNumber.label") + ": " + ui.format(identifier));
             }
             else {
                 return new FailureResult(ui.message("emr.archivesRoom.error.noPaperRecordExists", ui.format(identifier)));
             }
         }
         catch (Exception e) {
             // generic catch-all
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
             return new FailureResult(ui.message("emr.archivesRoom.error.unableToPrintLabel"));
 
         }
 
     }
 
     private List<SimpleObject> convertPaperRecordRequestsToSimpleObjects(List<PaperRecordRequest> requests, EmrProperties emrProperties, UiUtils ui) {
 
         List<SimpleObject> results = new ArrayList<SimpleObject>();
 
         for (PaperRecordRequest request : requests) {
             SimpleObject result = SimpleObject.fromObject(request, ui, "requestId", "patient", "identifier", "requestLocation");
 
             // manually add the date and patient identifier
             result.put("dateCreated", timeFormat.format(request.getDateCreated()));
             result.put("dateCreatedSortable", request.getDateCreated()) ;
             result.put("patientIdentifier", ui.format(request.getPatient().getPatientIdentifier(emrProperties.getPrimaryIdentifierType()).getIdentifier()));
 
             results.add(result);
         }
 
         return results;
     }
 
 }
