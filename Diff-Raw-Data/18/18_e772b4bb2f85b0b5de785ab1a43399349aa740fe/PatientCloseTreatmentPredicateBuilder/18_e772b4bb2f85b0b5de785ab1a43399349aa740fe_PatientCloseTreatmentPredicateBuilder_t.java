 package org.motechproject.whp.reports.export.query.dao.query.builder;
 
 import lombok.Data;
 import org.motechproject.whp.reports.export.query.model.DateRange;
 import org.motechproject.whp.reports.export.query.model.PatientReportRequest;
 
 import java.util.List;
 
 import static java.util.Arrays.asList;
 
 @Data
 public class PatientCloseTreatmentPredicateBuilder implements PredicateBuilder{
     private PatientReportRequest patientReportRequest;
 
     public PatientCloseTreatmentPredicateBuilder(PatientReportRequest patientReportRequest) {
         this.patientReportRequest = patientReportRequest;
     }
 
     @Override
     public List<String> getPredicates() {
         DateRange dateRange = new DateRange(patientReportRequest.getFrom(), patientReportRequest.getTo());
         if(dateRange.hasValidRange()){
             return asList(String.format(" treatment.end_date between '%s' AND '%s'",
                 dateRange.getStartDateInSqlFormat(),
                 dateRange.getEndDateInSqlFormat()));
         }
         else
            return asList(String.format(" treatment.end_date is not null"));
     }
 }
