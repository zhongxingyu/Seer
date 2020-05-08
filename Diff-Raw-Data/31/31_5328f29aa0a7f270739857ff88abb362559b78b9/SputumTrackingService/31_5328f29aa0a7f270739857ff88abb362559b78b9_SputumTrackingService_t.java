 package org.motechproject.whp.reports.service;
 
 import org.motechproject.whp.reports.contract.ContainerPatientMappingReportingRequest;
 import org.motechproject.whp.reports.contract.ContainerRegistrationReportingRequest;
 import org.motechproject.whp.reports.contract.ContainerStatusReportingRequest;
 import org.motechproject.whp.reports.contract.SputumLabResultsCaptureReportingRequest;
 import org.motechproject.whp.reports.domain.measure.ContainerRecord;
 import org.motechproject.whp.reports.mapper.SputumTrackingRequestMapper;
 import org.motechproject.whp.reports.repository.AllSputumTrackingRecords;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class SputumTrackingService {
 
     private AllSputumTrackingRecords allSputumTrackingRecords;
 
     /* Required for spring proxy */
     SputumTrackingService() {
     }
 
    @Autowired
     public SputumTrackingService(AllSputumTrackingRecords allSputumTrackingRecords) {
         this.allSputumTrackingRecords = allSputumTrackingRecords;
     }
 
     public void recordContainerRegistration(ContainerRegistrationReportingRequest containerRegistrationReportingRequest) {
         ContainerRecord containerRecord = SputumTrackingRequestMapper.buildContainerRegistrationRecord(containerRegistrationReportingRequest);
         allSputumTrackingRecords.save(containerRecord);
     }
 
     public void recordLabResults(SputumLabResultsCaptureReportingRequest sputumLabResultsCaptureReportingRequest) {
         ContainerRecord containerRecord = allSputumTrackingRecords.getByContainerId(sputumLabResultsCaptureReportingRequest.getContainerId());
         SputumTrackingRequestMapper.populateSputumLabResults(sputumLabResultsCaptureReportingRequest, containerRecord);
         allSputumTrackingRecords.save(containerRecord);
 
     }
 
     public void updateContainerStatus(ContainerStatusReportingRequest containerStatusReportingRequest) {
         ContainerRecord containerRecord = allSputumTrackingRecords.getByContainerId(containerStatusReportingRequest.getContainerId());
         SputumTrackingRequestMapper.updateContainerStatus(containerStatusReportingRequest, containerRecord);
         allSputumTrackingRecords.save(containerRecord);
     }
 
     public void updateContainerPatientMapping(ContainerPatientMappingReportingRequest containerPatientMappingReportingRequest) {
         ContainerRecord containerRecord = allSputumTrackingRecords.getByContainerId(containerPatientMappingReportingRequest.getContainerId());
         SputumTrackingRequestMapper.updateContainerPatientMapping(containerPatientMappingReportingRequest, containerRecord);
         allSputumTrackingRecords.save(containerRecord);
     }
 }
