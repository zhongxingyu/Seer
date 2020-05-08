 package org.motechproject.whp.reports.export.query.model;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class PatientSummaryTest {
 
     PatientSummary patientSummary;
 
     @Before
     public void setUp(){
         patientSummary = new PatientSummary();
     }
 
     @Test
     public void shouldReturnFormattedNameOfPatient(){
         patientSummary.setFirstName("John");
         patientSummary.setLastName("Doe");
 
         assertThat(patientSummary.getName(), is("John Doe"));
     }
    
     @Test
     public void shouldReturnGetIpTreatmentProgress(){
         patientSummary.setIpPillsTaken(12);
         patientSummary.setIpTotalDoses(14);
         patientSummary.setEipPillsTaken(3);
         patientSummary.setEipTotalDoses(5);
 
         assertThat(patientSummary.getIpTreatmentProgress(), is("15/19 (78.95%)"));
 
     }
 
     @Test
     public void shouldReturnGetCpTreatmentProgress(){
         patientSummary.setCpPillsTaken(12);
         patientSummary.setCpTotalDoses(15);
 
         assertThat(patientSummary.getCpTreatmentProgress(), is("12/15 (80.00%)"));
 
     }
 
     @Test
     public void shouldNotAddEipIPIfEipDetailsIsNull(){
         patientSummary.setIpPillsTaken(12);
         patientSummary.setIpTotalDoses(14);
         patientSummary.setEipPillsTaken(null);
         patientSummary.setEipTotalDoses(null);
 
         assertThat(patientSummary.getIpTreatmentProgress(), is("12/14 (85.71%)"));
 
     }
    
 }
