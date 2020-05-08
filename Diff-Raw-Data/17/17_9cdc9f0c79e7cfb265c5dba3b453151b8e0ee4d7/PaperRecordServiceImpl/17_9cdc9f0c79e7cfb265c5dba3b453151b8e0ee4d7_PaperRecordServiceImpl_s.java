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
 
 package org.openmrs.module.paperrecord;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Person;
 import org.openmrs.api.APIException;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.emrapi.printer.Printer;
 import org.openmrs.module.emrapi.printer.PrinterService;
 import org.openmrs.module.emrapi.utils.GeneralUtils;
 import org.openmrs.module.idgen.service.IdentifierSourceService;
 import org.openmrs.module.paperrecord.db.PaperRecordMergeRequestDAO;
 import org.openmrs.module.paperrecord.db.PaperRecordRequestDAO;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import static org.openmrs.module.paperrecord.PaperRecordRequest.ASSIGNED_STATUSES;
 import static org.openmrs.module.paperrecord.PaperRecordRequest.PENDING_STATUSES;
 import static org.openmrs.module.paperrecord.PaperRecordRequest.Status;
 
 public class PaperRecordServiceImpl extends BaseOpenmrsService implements PaperRecordService {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private final static int NUMBER_OF_LABELS_TO_PRINT_WHEN_CREATING_NEW_RECORD = 3;
 
     private final static int NUMBER_OF_LABELS_TO_PRINT_WHEN_PULLING_RECORD = 2;
 
     private PaperRecordRequestDAO paperRecordRequestDAO;
 
     private PaperRecordMergeRequestDAO paperRecordMergeRequestDAO;
 
     private AdministrationService administrationService;
 
     private PatientService patientService;
 
     private MessageSourceService messageSourceService;
 
     private IdentifierSourceService identifierSourceService;
 
     private PrinterService printerService;
 
     private EmrApiProperties emrApiProperties;
 
     private PaperRecordProperties paperRecordProperties;
 
     private PaperRecordLabelTemplate paperRecordLabelTemplate;
 
     private IdCardLabelTemplate idCardLabelTemplate;
 
 
     public void setPaperRecordRequestDAO(PaperRecordRequestDAO paperRecordRequestDAO) {
         this.paperRecordRequestDAO = paperRecordRequestDAO;
     }
 
     public void setPaperRecordMergeRequestDAO(PaperRecordMergeRequestDAO paperRecordMergeRequestDAO) {
         this.paperRecordMergeRequestDAO = paperRecordMergeRequestDAO;
     }
 
     public void setMessageSourceService(MessageSourceService messageSourceService) {
         this.messageSourceService = messageSourceService;
     }
 
     public void setAdministrationService(AdministrationService administrationService) {
         this.administrationService = administrationService;
     }
 
     public void setIdentifierSourceService(IdentifierSourceService identifierSourceService) {
         this.identifierSourceService = identifierSourceService;
     }
 
     public void setPatientService(PatientService patientService) {
         this.patientService = patientService;
     }
 
     public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
         this.emrApiProperties = emrApiProperties;
     }
 
     public void setPaperRecordProperties(PaperRecordProperties paperRecordProperties) {
         this.paperRecordProperties = paperRecordProperties;
     }
 
     public void setPaperRecordLabelTemplate(PaperRecordLabelTemplate paperRecordLabelTemplate) {
         this.paperRecordLabelTemplate = paperRecordLabelTemplate;
     }
 
     public void setIdCardLabelTemplate(IdCardLabelTemplate idCardLabelTemplate) {
         this.idCardLabelTemplate = idCardLabelTemplate;
     }
 
     @Override
     public void setPrinterService(PrinterService printerService) {
         this.printerService = printerService;
     }
 
 
 
     @Override
     public boolean paperRecordExistsWithIdentifier(String identifier, Location location) {
 
         List<PatientIdentifier> identifiers = patientService.getPatientIdentifiers(identifier,
                 Collections.singletonList(paperRecordProperties.getPaperRecordIdentifierType()),
                 Collections.singletonList(getMedicalRecordLocationAssociatedWith(location)), null, null);
 
         return identifiers != null && identifiers.size() > 0 ? true : false;
     }
 
     @Override
     public boolean paperRecordExistsForPatientWithIdentifier(String patientIdentifier, Location location) {
 
         List<Patient> patients = patientService.getPatients(null, patientIdentifier, Collections.singletonList(emrApiProperties.getPrimaryIdentifierType()), true);
 
         if (patients == null || patients.size() == 0) {
             return false;
         }
 
         if (patients.size() > 1) {
             // data model should prevent us from ever getting her, but just in case
             throw new APIException("Multiple patients found with identifier " + patientIdentifier);
         } else {
            return paperRecordExistsForPatient(patients.get(0), location);
         }
     }
 
     @Override
     public boolean paperRecordExistsForPatient(Patient patient, Location location) {
 
         List<PatientIdentifier> identifiers = patientService.getPatientIdentifiers(null,
                 Collections.singletonList(paperRecordProperties.getPaperRecordIdentifierType()),
                 Collections.singletonList(getMedicalRecordLocationAssociatedWith(location)), Collections.singletonList(patient), null);
 
         return identifiers != null && identifiers.size() > 0 ? true : false;
 
     }
 
     @Override
     @Transactional(readOnly = true)
     public PaperRecordRequest getPaperRecordRequestById(Integer id) {
         return paperRecordRequestDAO.getById(id);
     }
 
     @Override
     public PaperRecordMergeRequest getPaperRecordMergeRequestById(Integer id) {
         return paperRecordMergeRequestDAO.getById(id);
     }
 
     @Override
     @Transactional
     public PaperRecordRequest requestPaperRecord(Patient patient, Location location, Location requestLocation) {
 
         // TODO: we will have to handle the case if there is already a request for this patient's record in the "SENT" state
         // TODO: (ie, what to do if the record is already out on the floor--right now it will just create a new request)
 
         if (patient == null) {
             throw new IllegalArgumentException("Patient cannot be null");
         }
 
         if (location == null) {
             throw new IllegalArgumentException("Record Location cannot be null");
         }
 
         if (requestLocation == null) {
             throw new IllegalArgumentException("Request Location cannot be null");
         }
 
         // fetch the nearest medical record location (or just return the given location if it is a valid
         // medical record location)
         Location recordLocation = getMedicalRecordLocationAssociatedWith(location);
 
         // fetch any pending request for this patient at this location
         List<PaperRecordRequest> requests = paperRecordRequestDAO.findPaperRecordRequests(PENDING_STATUSES, patient,
                 recordLocation, null, null);
 
         // if pending records exists, simply update that request location, don't issue a new request
         // (there should rarely be more than one pending record for a single patient, but this *may* happen if two
         // patients with pending records are merged)
         for (PaperRecordRequest request : requests) {
             request.setRequestLocation(requestLocation);
             paperRecordRequestDAO.saveOrUpdate(request);
             return request;
         }
 
         // if no pending record exists, create a new request
         // fetch the appropriate identifier (if it exists)
         PatientIdentifier paperRecordIdentifier = GeneralUtils.getPatientIdentifier(patient,
                 paperRecordProperties.getPaperRecordIdentifierType(), recordLocation);
         String identifier = paperRecordIdentifier != null ? paperRecordIdentifier.getIdentifier() : null;
 
         PaperRecordRequest request = new PaperRecordRequest();
         request.setCreator(Context.getAuthenticatedUser());
         request.setDateCreated(new Date());
         request.setIdentifier(identifier);
         request.setRecordLocation(recordLocation);
         request.setPatient(patient);
         request.setRequestLocation(requestLocation);
 
         paperRecordRequestDAO.saveOrUpdate(request);
 
         return request;
     }
 
     @Override
     @Transactional
     public PaperRecordRequest savePaperRecordRequest(PaperRecordRequest paperRecordRequest) {
         PaperRecordRequest request = null;
         if (paperRecordRequest != null) {
             return paperRecordRequestDAO.saveOrUpdate(paperRecordRequest);
         }
         return request;
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<PaperRecordRequest> getOpenPaperRecordRequestsToPull() {
         // TODO: once we have multiple medical record locations, we will need to add location as a criteria
         return paperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(PaperRecordRequest.Status.OPEN),
                 null, null, null, true);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<PaperRecordRequest> getOpenPaperRecordRequestsToCreate() {
         // TODO: once we have multiple medical record locations, we will need to add location as a criteria
         return paperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(PaperRecordRequest.Status.OPEN),
                 null, null, null, false);
     }
 
     // we break this out into an external public and internal private method because we want the transaction to
     // occur within the synchronized block
 
     @Override
     public synchronized Map<String, List<String>> assignRequests(List<PaperRecordRequest> requests, Person assignee, Location location) throws UnableToPrintLabelException {
 
         if (requests == null) {
             throw new IllegalArgumentException("Requests cannot be null");
         }
 
         if (assignee == null) {
             throw new IllegalArgumentException("Assignee cannot be null");
         }
 
         // HACK: we need to reference the service here because an internal call won't pick up the @Transactional on the
         // internal method; we could potentially wire the bean into itself, but are unsure of that
         // see PaperRecordService.assignRequestsInternal(...  for more information
         return Context.getService(PaperRecordService.class).assignRequestsInternal(requests, assignee, location);
     }
 
 
     // HACK; note that this method must be public in order for Spring to pick up the @Transactional annotation;
     // see PaperRecordService.assignRequestsInternal(...  for more information
     @Transactional(rollbackFor = UnableToPrintLabelException.class)
     public Map<String, List<String>> assignRequestsInternal(List<PaperRecordRequest> requests, Person assignee, Location location) throws UnableToPrintLabelException {
 
         Map<String, List<String>> response = new HashMap<String, List<String>>();
         response.put("success", new LinkedList<String>());
         response.put("error", new LinkedList<String>());
 
         for (PaperRecordRequest request : requests) {
 
             // as a sanity check, ignore any requests that aren't open
             if (request.getStatus() == Status.OPEN) {
 
                 // update the identifier on the request just in case it has changed since the request has been issued
                 request.setIdentifier(getPaperMedicalRecordNumberFor(request.getPatient(), request.getRecordLocation()));
 
                 if (StringUtils.isBlank(request.getIdentifier())) {
                     request.setIdentifier(createPaperMedicalRecordNumber(request.getPatient(),
                             request.getRecordLocation()).getIdentifier());
                     request.updateStatus(Status.ASSIGNED_TO_CREATE);
                     printPaperRecordLabels(request, location, NUMBER_OF_LABELS_TO_PRINT_WHEN_CREATING_NEW_RECORD);
                     printIdCardLabel(request.getPatient(), location);
                 } else {
                     request.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_PULL);
                     printPaperRecordLabels(request, location, NUMBER_OF_LABELS_TO_PRINT_WHEN_PULLING_RECORD);
                 }
 
                 request.setAssignee(assignee);
                 paperRecordRequestDAO.saveOrUpdate(request);
 
                 response.get("success").add(request.getIdentifier());
             }
         }
 
         return response;
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<PaperRecordRequest> getAssignedPaperRecordRequestsToPull() {
         // TODO: once we have multiple medical record locations, we will need to add location as a criteria
         return paperRecordRequestDAO.findPaperRecordRequests(
                 Collections.singletonList(PaperRecordRequest.Status.ASSIGNED_TO_PULL), null, null, null, null);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<PaperRecordRequest> getAssignedPaperRecordRequestsToCreate() {
         // TODO: once we have multiple medical record locations, we will need to add location as a criteria
         return paperRecordRequestDAO.findPaperRecordRequests(
                 Collections.singletonList(PaperRecordRequest.Status.ASSIGNED_TO_CREATE), null, null, null, null);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<PaperRecordRequest> getPaperRecordRequestsByPatient(Patient patient) {
         return paperRecordRequestDAO.findPaperRecordRequests(null, patient, null, null, null);
     }
 
     @Override
     @Transactional(readOnly = true)
     public PaperRecordRequest getAssignedPaperRecordRequestByIdentifier(String identifier) {
         // TODO: once we have multiple medical record locations, we will need to add location as a criteria
         List<PaperRecordRequest> requests = getPaperRecordRequestByIdentifierAndStatus(identifier, ASSIGNED_STATUSES);
 
         if (requests == null || requests.size() == 0) {
             return null;
         } else if (requests.size() > 1) {
             throw new IllegalStateException("Duplicate record requests in the pending state with identifier " + identifier);
         } else {
             return requests.get(0);
         }
 
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<PaperRecordRequest> getSentPaperRecordRequestByIdentifier(String identifier) {
         // TODO: once we have multiple medical record locations, we will need to add location as a criteria
         return getPaperRecordRequestByIdentifierAndStatus(identifier, Collections.singletonList(Status.SENT));
     }
 
     private List<PaperRecordRequest> getPaperRecordRequestByIdentifierAndStatus(String identifier, List<Status> statusList) {
         // TODO: once we have multiple medical record locations, we will need to add location as a criteria
 
         if (StringUtils.isBlank(identifier)) {
             return new ArrayList<PaperRecordRequest>();
         }
 
         // first see if we find any requests by paper record identifier
         List<PaperRecordRequest> requests = paperRecordRequestDAO.findPaperRecordRequests(
                 statusList, null, null, identifier, null);
 
         // if no requests, see if this is patient identifier
         if (requests == null || requests.size() == 0) {
             List<Patient> patients = patientService.getPatients(null, identifier, Collections.singletonList(emrApiProperties.getPrimaryIdentifierType()), true);
             if (patients != null && patients.size() > 0) {
                 if (patients.size() > 1) {
                     throw new IllegalStateException("Duplicate patients exist with identifier " + identifier);
                 } else {
                     requests = paperRecordRequestDAO.findPaperRecordRequests(statusList, patients.get(0), null,
                             null, null);
                 }
             }
 
         }
 
         return requests;
     }
 
     @Override
     public PaperRecordRequest getMostRecentSentPaperRecordRequestByIdentifier(String identifier) {
 
         List<PaperRecordRequest> requests = getSentPaperRecordRequestByIdentifier(identifier);
 
         if (requests == null || requests.size() == 0) {
             return null;
         } else {
             Collections.sort(requests, new Comparator<PaperRecordRequest>() {
                 @Override
                 public int compare(PaperRecordRequest request1, PaperRecordRequest request2) {
                     // get date status changed should never be null, but just to be safe
                     return request1.getDateStatusChanged() == null ? 1 : request2.getDateStatusChanged() == null ? -1
                             : request1.getDateStatusChanged().compareTo(request2.getDateStatusChanged());
                 }
             });
             return requests.get(requests.size() - 1);  // most recent is last one in list
         }
     }
 
     @Override
     @Transactional
     public void markPaperRecordRequestAsSent(PaperRecordRequest request) {
         // I don't think we really need to do any verification here
         request.updateStatus(Status.SENT);
         savePaperRecordRequest(request);
     }
 
     @Override
     @Transactional
     public void markPaperRecordRequestAsCancelled(PaperRecordRequest request) {
         request.updateStatus(Status.CANCELLED);
         savePaperRecordRequest(request);
     }
 
     @Override
     @Transactional
     public void markPaperRecordRequestAsReturned(PaperRecordRequest request) {
         request.updateStatus(Status.RETURNED);
         savePaperRecordRequest(request);
     }
 
     @Override
     public void printPaperRecordLabel(PaperRecordRequest request, Location location) throws UnableToPrintLabelException {
         printPaperRecordLabels(request, location, 1);
     }
 
 
     @Override
     public void printPaperRecordLabels(PaperRecordRequest request, Location location, Integer count) throws UnableToPrintLabelException {
         printPaperRecordLabels(request.getPatient(), request.getIdentifier(), location, count);
 
     }
 
     @Override
     public void printPaperRecordLabels(Patient patient, Location location, Integer count) throws UnableToPrintLabelException {
         PatientIdentifier paperRecordIdentifier = GeneralUtils.getPatientIdentifier(patient, paperRecordProperties.getPaperRecordIdentifierType(), getMedicalRecordLocationAssociatedWith(location));
         printPaperRecordLabels(patient, paperRecordIdentifier != null ? paperRecordIdentifier.getIdentifier() : null, location, count);
     }
 
     private void printPaperRecordLabels(Patient patient, String identifier, Location location, Integer count) throws UnableToPrintLabelException {
         if (count == null || count == 0) {
             return;  // just do nothing if we don't have a count
         }
 
         String data = paperRecordLabelTemplate.generateLabel(patient, identifier);
         String encoding = paperRecordLabelTemplate.getEncoding();
 
         // just duplicate the data if we are printing multiple labels
         StringBuffer dataBuffer = new StringBuffer();
         dataBuffer.append(data);
 
         while (count > 1) {
             dataBuffer.append(data);
             count--;
         }
 
         try {
             printerService.printViaSocket(dataBuffer.toString(), Printer.Type.LABEL, location, encoding);
         } catch (Exception e) {
             throw new UnableToPrintLabelException("Unable to print paper record label at location " + location + " for patient " + patient, e);
         }
     }
 
     @Override
     public void printIdCardLabel(Patient patient, Location location) throws UnableToPrintLabelException {
 
         String data = idCardLabelTemplate.generateLabel(patient);
         String encoding = idCardLabelTemplate.getEncoding();
 
         try {
             printerService.printViaSocket(data.toString(), Printer.Type.LABEL, location, encoding);
         } catch (Exception e) {
             throw new UnableToPrintLabelException("Unable to print id card label at location " + location +" for patient " + patient, e);
         }
 
     }
 
 
     @Override
     @Transactional
     public void markPaperRecordsForMerge(PatientIdentifier preferredIdentifier, PatientIdentifier notPreferredIdentifier) {
 
         if (!preferredIdentifier.getIdentifierType().equals(paperRecordProperties.getPaperRecordIdentifierType())
                 || !notPreferredIdentifier.getIdentifierType().equals(paperRecordProperties.getPaperRecordIdentifierType())) {
             throw new IllegalArgumentException("One of the passed identifiers is not a paper record identifier: "
                     + preferredIdentifier + ", " + notPreferredIdentifier);
         }
 
         if (!preferredIdentifier.getLocation().equals(notPreferredIdentifier.getLocation())) {
             throw new IllegalArgumentException("Cannot merge two records from different locations: "
                     + preferredIdentifier + ", " + notPreferredIdentifier);
         }
 
         // create the request
         PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
         mergeRequest.setStatus(PaperRecordMergeRequest.Status.OPEN);
         mergeRequest.setPreferredPatient(preferredIdentifier.getPatient());
         mergeRequest.setNotPreferredPatient(notPreferredIdentifier.getPatient());
         mergeRequest.setPreferredIdentifier(preferredIdentifier.getIdentifier());
         mergeRequest.setNotPreferredIdentifier(notPreferredIdentifier.getIdentifier());
         mergeRequest.setRecordLocation(preferredIdentifier.getLocation());
         mergeRequest.setCreator(Context.getAuthenticatedUser());
         mergeRequest.setDateCreated(new Date());
 
         paperRecordMergeRequestDAO.saveOrUpdate(mergeRequest);
 
         // void the non-preferred identifier; we do this now (instead of when the merge is confirmed)
         // so that all new requests for records for this patient use the right identifier
         patientService.voidPatientIdentifier(notPreferredIdentifier, "voided during paper record merge");
     }
 
     @Override
     @Transactional
     public void markPaperRecordsAsMerged(PaperRecordMergeRequest mergeRequest) {
 
         // merge any pending paper record requests associated with the two records we are merging
         mergePendingPaperRecordRequests(mergeRequest);
 
         // if the archivist has just merged the records, we should be able to safely close out
         // any request for the not preferred record, as this record should no longer exist
         closeOutSentPaperRecordRequestsForNotPreferredRecord(mergeRequest);
 
         // then just mark the request as merged
         mergeRequest.setStatus(PaperRecordMergeRequest.Status.MERGED);
         paperRecordMergeRequestDAO.saveOrUpdate(mergeRequest);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<PaperRecordMergeRequest> getOpenPaperRecordMergeRequests() {
         return paperRecordMergeRequestDAO.findPaperRecordMergeRequest(
                 Collections.singletonList(PaperRecordMergeRequest.Status.OPEN));
     }
 
     @Override
     @Transactional
     public void expirePendingPullRequests(Date expireDate) {
 
         List<PaperRecordRequest> pullRequests = new ArrayList<PaperRecordRequest>();
 
         // note that since we are calling the other service methods directly, they
         // won't be transactional, so we need to make sure this method is transactional
 
         pullRequests.addAll(getOpenPaperRecordRequestsToPull());
         pullRequests.addAll(getAssignedPaperRecordRequestsToPull());
 
         for (PaperRecordRequest request : pullRequests) {
             if (request.getDateCreated().before(expireDate)) {
                 markPaperRecordRequestAsCancelled(request);
             }
         }
     }
 
     @Override
     @Transactional
     public PatientIdentifier createPaperMedicalRecordNumber(Patient patient, Location location) {
         if (patient == null) {
             throw new IllegalArgumentException("Patient shouldn't be null");
         }
 
         Location medicalRecordLocation = getMedicalRecordLocationAssociatedWith(location);
 
         if(paperRecordExistsForPatient(patient, medicalRecordLocation) ){
             log.warn("Patient already has a paper record number at this location. patientId=" + patient.getId().toString() + ", location=" + location.getName());
             return null;
         }
 
         PatientIdentifierType paperRecordIdentifierType = paperRecordProperties.getPaperRecordIdentifierType();
         String paperRecordId = "";
 
         paperRecordId = identifierSourceService.generateIdentifier(paperRecordIdentifierType,
                 "generating a new dossier number");
 
         // double check to make sure this dossier number is not in use
         while (paperRecordExistsWithIdentifier(paperRecordId, medicalRecordLocation)) {
             log.error("Attempted to generate duplicate paper record identifier " + paperRecordId );
             paperRecordId = identifierSourceService.generateIdentifier(paperRecordIdentifierType,
                     "generating a new dossier number");
         }
 
         PatientIdentifier paperRecordIdentifier = new PatientIdentifier(paperRecordId, paperRecordIdentifierType,
                 medicalRecordLocation);
         patient.addIdentifier(paperRecordIdentifier);
         patientService.savePatientIdentifier(paperRecordIdentifier);
 
         return paperRecordIdentifier;
     }
 
     protected Location getMedicalRecordLocationAssociatedWith(Location location) {
 
         if (location != null) {
             if (location.hasTag(paperRecordProperties.getMedicalRecordLocationLocationTag().toString())) {
                 return location;
             } else {
                 return getMedicalRecordLocationAssociatedWith(location.getParentLocation());
             }
         }
 
         throw new IllegalStateException(
                 "There is no matching location with the tag: " + paperRecordProperties.getMedicalRecordLocationLocationTag().toString());
     }
 
 
     private String getPaperMedicalRecordNumberFor(Patient patient, Location medicalRecordLocation) {
         PatientIdentifier paperRecordIdentifier = GeneralUtils.getPatientIdentifier(patient,
                 paperRecordProperties.getPaperRecordIdentifierType(), medicalRecordLocation);
         return paperRecordIdentifier != null ? paperRecordIdentifier.getIdentifier() : null;
     }
 
     private void mergePendingPaperRecordRequests(PaperRecordMergeRequest mergeRequest) {
 
         // (note that we are not searching by patient here because the patient may have been changed during the merge)
         List<PaperRecordRequest> preferredRequests = paperRecordRequestDAO.findPaperRecordRequests(PENDING_STATUSES,
                 null, mergeRequest.getRecordLocation(), mergeRequest.getPreferredIdentifier(), null);
 
         if (preferredRequests.size() > 1) {
             throw new IllegalStateException(
                     "Duplicate pending record requests exist with identifier " + mergeRequest.getPreferredIdentifier());
         }
 
         List<PaperRecordRequest> notPreferredRequests = paperRecordRequestDAO.findPaperRecordRequests(PENDING_STATUSES,
                 null, mergeRequest.getRecordLocation(), mergeRequest.getNotPreferredIdentifier(), null);
 
         if (notPreferredRequests.size() > 1) {
             throw new IllegalStateException(
                     "Duplicate pending record requests exist with identifier " + mergeRequest.getNotPreferredIdentifier());
         }
 
         PaperRecordRequest preferredRequest = null;
         PaperRecordRequest notPreferredRequest = null;
 
         if (preferredRequests.size() == 1) {
             preferredRequest = preferredRequests.get(0);
         }
 
         if (notPreferredRequests.size() == 1) {
             notPreferredRequest = notPreferredRequests.get(0);
         }
 
         // if both the preferred and not-preferred records have a request, we need to
         // cancel on of them
         if (preferredRequest != null && notPreferredRequest != null) {
             // update the request location if the non-preferred  is more recent
             if (notPreferredRequest.getDateCreated().after(preferredRequest.getDateCreated())) {
                 preferredRequest.setRequestLocation(notPreferredRequest.getRequestLocation());
             }
 
             notPreferredRequest.updateStatus(Status.CANCELLED);
             paperRecordRequestDAO.saveOrUpdate(preferredRequest);
             paperRecordRequestDAO.saveOrUpdate(notPreferredRequest);
         }
 
         // if there is only a non-preferred request, we need to update it with the right identifier
         if (preferredRequest == null && notPreferredRequest != null) {
             notPreferredRequest.setIdentifier(mergeRequest.getPreferredIdentifier());
             paperRecordRequestDAO.saveOrUpdate(notPreferredRequest);
         }
 
     }
 
     private void closeOutSentPaperRecordRequestsForNotPreferredRecord(PaperRecordMergeRequest mergeRequest) {
         List<PaperRecordRequest> notPreferredRequests = paperRecordRequestDAO.findPaperRecordRequests(
                 Collections.singletonList(Status.SENT), null,
                 mergeRequest.getRecordLocation(), mergeRequest.getNotPreferredIdentifier(), null);
 
         for (PaperRecordRequest notPreferredRequest : notPreferredRequests) {
             notPreferredRequest.updateStatus(Status.RETURNED);
             paperRecordRequestDAO.saveOrUpdate(notPreferredRequest);
         }
     }
 
 }
