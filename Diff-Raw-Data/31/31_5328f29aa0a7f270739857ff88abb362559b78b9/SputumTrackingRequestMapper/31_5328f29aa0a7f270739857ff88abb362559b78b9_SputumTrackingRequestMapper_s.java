 package org.motechproject.whp.reports.mapper;
 
 import org.motechproject.whp.reports.contract.ContainerPatientMappingReportingRequest;
 import org.motechproject.whp.reports.contract.ContainerRegistrationReportingRequest;
 import org.motechproject.whp.reports.contract.ContainerStatusReportingRequest;
 import org.motechproject.whp.reports.contract.SputumLabResultsCaptureReportingRequest;
 import org.motechproject.whp.reports.domain.measure.ContainerRecord;
 
 import java.sql.Date;
 
 public class SputumTrackingRequestMapper {
 
     public static ContainerRecord buildContainerRegistrationRecord(ContainerRegistrationReportingRequest containerRegistrationReportingRequest) {
         ContainerRecord containerRecord = new ContainerRecord();
         containerRecord.setContainerId(containerRegistrationReportingRequest.getContainerId());
        containerRecord.setIssuedOn(new Date(containerRegistrationReportingRequest.getIssuedOn().getTime()));
         containerRecord.setProviderId(containerRegistrationReportingRequest.getProviderId());
         containerRecord.setSubmitterRole(containerRegistrationReportingRequest.getSubmitterRole());
         containerRecord.setSubmitterId(containerRegistrationReportingRequest.getSubmitterId());
         containerRecord.setInstance(containerRegistrationReportingRequest.getInstance());
         return containerRecord;
     }
 
     public static void populateSputumLabResults(SputumLabResultsCaptureReportingRequest request, ContainerRecord containerRecord) {
 
         containerRecord.setLabName(request.getLabName());
         containerRecord.setLabNumber(request.getLabNumber());
        containerRecord.setSmearTestDate1(new Date(request.getSmearTestDate1().getTime()));
        containerRecord.setSmearTestDate2(new Date(request.getSmearTestDate2().getTime()));
         containerRecord.setSmearTestResult1(request.getSmearTestResult1());
         containerRecord.setSmearTestResult2(request.getSmearTestResult2());
         containerRecord.setCumulativeResult(request.getCumulativeResult());
     }
 
     public static void updateContainerStatus(ContainerStatusReportingRequest containerStatusReportingRequest, ContainerRecord containerRecord) {
         containerRecord.setAlternateDiagnosisCode(containerStatusReportingRequest.getAlternateDiagnosisCode());
        containerRecord.setClosureDate(new Date(containerStatusReportingRequest.getClosureDate().getTime()));
        containerRecord.setConsultationDate(new Date(containerStatusReportingRequest.getConsultationDate().getTime()));
         containerRecord.setReasonForClosure(containerStatusReportingRequest.getReasonForClosure());
         containerRecord.setStatus(containerStatusReportingRequest.getStatus());
     }
 
     public static void updateContainerPatientMapping(ContainerPatientMappingReportingRequest containerPatientMappingReportingRequest, ContainerRecord containerRecord) {
         containerRecord.setPatientId(containerPatientMappingReportingRequest.getPatientId());
         containerRecord.setTbId(containerPatientMappingReportingRequest.getTbId());
         containerRecord.setMappingInstance(containerPatientMappingReportingRequest.getMappingInstance());
         containerRecord.setReasonForClosure(containerPatientMappingReportingRequest.getReasonForClosure());
         containerRecord.setStatus(containerPatientMappingReportingRequest.getStatus());
        containerRecord.setClosureDate(new Date(containerPatientMappingReportingRequest.getClosureDate().getTime()));
        containerRecord.setConsultationDate(new Date(containerPatientMappingReportingRequest.getConsultationDate().getTime()));
     }
 }
