 package org.motechproject.whp.reports.export.query.dao;
 
 import org.junit.Test;
 import org.motechproject.whp.reports.export.query.dao.query.builder.PatientCloseTreatmentPredicateBuilder;
 import org.motechproject.whp.reports.export.query.model.PatientReportRequest;
 
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class PatientCloseTreatmentPredicateBuilderTest {
 
     @Test
     public void shouldReturnPredicateToFilterByTreatmentClosedDate(){
         PatientReportRequest patientReportRequest = new PatientReportRequest();
         patientReportRequest.setReportType(PatientReportType.CLOSED_TREATMENT);
         patientReportRequest.setFrom("25/12/2012");
         patientReportRequest.setTo("25/12/2013");
         PatientCloseTreatmentPredicateBuilder patientCloseTreatmentPredicateBuilder = new PatientCloseTreatmentPredicateBuilder(patientReportRequest);
 
         List<String> expectedPredicates = asList(" treatment.end_date between '2012-12-25' AND '2013-12-25'");
         assertThat(patientCloseTreatmentPredicateBuilder.getPredicates(), is(expectedPredicates));
     }
 
     @Test
    public void shouldReturnPredicateWhenDateRangeIsNotGiven(){
         PatientReportRequest patientReportRequest = new PatientReportRequest();
         patientReportRequest.setReportType(PatientReportType.CLOSED_TREATMENT);
         patientReportRequest.setFrom(null);
         patientReportRequest.setTo(null);
         PatientCloseTreatmentPredicateBuilder patientCloseTreatmentPredicateBuilder = new PatientCloseTreatmentPredicateBuilder(patientReportRequest);
 
        List<String> closedTreatmentPredicate = asList(" treatment.end_date is not null");
        assertThat(patientCloseTreatmentPredicateBuilder.getPredicates(), is(closedTreatmentPredicate));
     }
 }
