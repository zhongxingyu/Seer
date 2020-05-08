 package org.motechproject.whp.reports.query;
 
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.bigquery.model.FilterParams;
 import org.motechproject.bigquery.response.QueryResult;
 import org.motechproject.bigquery.service.BigQueryService;
 import org.motechproject.whp.reports.IntegrationTest;
 import org.motechproject.whp.reports.builder.ContainerRecordBuilder;
 import org.motechproject.whp.reports.domain.measure.container.ContainerRecord;
 import org.motechproject.whp.reports.repository.ContainerRecordRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertEquals;
 
 public class ContainerRegistrationsByStatusQueryIT extends IntegrationTest{
 
     private static final String NUMBER_OF_CONTAINER_REGISTRATIONS_BY_STATUS_QUERY = "number.of.container.registrations.by.status";
 
     @Autowired
     BigQueryService queryService;
     @Autowired
     ContainerRecordRepository containerRecordRepository;
 
     private final FilterParams emptyFilterParams = new FilterParams();
 
     @Before
     public void setUp() {
         ContainerRecord containerRecordWithClosedStatus = createContainer("containerId1", "Patna", new LocalDate(2013, 10, 12), "Closed", new LocalDate(2013, 12, 10), new LocalDate(2013, 10, 10), "PreTreatment", null);
         ContainerRecord containerRecordWithLabResults = createContainer("containerId2", "Patna", new LocalDate(2013, 11, 12), "Open", new LocalDate(2013, 12, 10), null, "PreTreatment", null);
         ContainerRecord containerRecordWithNoLabResults = createContainer("containerId3", "Begusarai", new LocalDate(2013, 12, 12), "Open", null, null, "PreTreatment", null);
         ContainerRecord inTreatmentContainerRecordWithNoLabResults = createContainer("containerId4", "Samastipur", new LocalDate(2013, 12, 12), "Open", null, null, "InTreatment", null);
         ContainerRecord preTreatmentContainerWithInTreatmentMappingInstance = createContainer("containerId4", "Samastipur", new LocalDate(2013, 12, 12), "Open", null, null, "PreTreatment", "EndIP");
 
         containerRecordRepository.save(asList(containerRecordWithClosedStatus, containerRecordWithLabResults, containerRecordWithNoLabResults, inTreatmentContainerRecordWithNoLabResults, preTreatmentContainerWithInTreatmentMappingInstance));
     }
 
     @Test
     public void shouldReturnCountOfContainerRegistrations() {
         QueryResult queryResult = queryService.executeQuery(NUMBER_OF_CONTAINER_REGISTRATIONS_BY_STATUS_QUERY, emptyFilterParams);
        QueryResult expectedQueryResult = expectedQueryResult(active(3), closed(1), pendingLabResults(2), pendingConsultationDate(2));
         assertEquals(expectedQueryResult, queryResult);
     }
 
     @Test
     public void shouldReturnStatusCountsWithBothDistrictAndDateRangeFilter() {
         FilterParams filterParamsWithDateAndDistrict = new FilterParams();
         filterParamsWithDateAndDistrict.put("from_date", "10/11/2013");
         filterParamsWithDateAndDistrict.put("to_date", "15/12/2013");
         filterParamsWithDateAndDistrict.put("district", "Begusarai");
 
         QueryResult queryResult = queryService.executeQuery(NUMBER_OF_CONTAINER_REGISTRATIONS_BY_STATUS_QUERY, filterParamsWithDateAndDistrict);
 
         QueryResult expectedQueryResult = expectedQueryResult(active(1), closed(0), pendingLabResults(1), pendingConsultationDate(1));
         assertEquals(expectedQueryResult, queryResult);
     }
 
     @Test
     public void shouldReturnStatusCountsWithDistrictFilter() {
         QueryResult queryResult;
         QueryResult expectedQueryResult;FilterParams filterParamsWithDistrict = new FilterParams();
         filterParamsWithDistrict.put("district", "Patna");
 
         queryResult = queryService.executeQuery(NUMBER_OF_CONTAINER_REGISTRATIONS_BY_STATUS_QUERY, filterParamsWithDistrict);
 
         expectedQueryResult = expectedQueryResult(active(1), closed(1), pendingLabResults(0), pendingConsultationDate(1));
         assertEquals(expectedQueryResult, queryResult);
     }
 
     @Test
     public void shouldReturnStatusCountsWithDateRangeFilter() {
         FilterParams filterParamsWithDate = new FilterParams();
         filterParamsWithDate.put("from_date", "10/11/2013");
         filterParamsWithDate.put("to_date", "15/12/2013");
 
        QueryResult expectedQueryResult = expectedQueryResult(active(3), closed(0), pendingLabResults(2), pendingConsultationDate(2));
 
         QueryResult queryResult = queryService.executeQuery(NUMBER_OF_CONTAINER_REGISTRATIONS_BY_STATUS_QUERY, filterParamsWithDate);
 
         assertEquals(expectedQueryResult, queryResult);
     }
 
     @Test
     public void shouldNotConsiderContainersWithMappingInstanceAsOneOfInTreatmentValues() {
         QueryResult queryResult = queryService.executeQuery(NUMBER_OF_CONTAINER_REGISTRATIONS_BY_STATUS_QUERY, emptyFilterParams);
        QueryResult expectedQueryResult = expectedQueryResult(active(3), closed(1), pendingLabResults(2), pendingConsultationDate(2));
         assertEquals(expectedQueryResult, queryResult);
     }
 
     @After
     public void tearDown() {
         containerRecordRepository.deleteAll();
     }
 
     private ContainerRecord createContainer(String containerId, String district, LocalDate issuedOnDate, String status, LocalDate labResultsCapturedOn, LocalDate consultationDate, String registrationInstance, String mappingInstance) {
         return new ContainerRecordBuilder().withDefaults()
                 .withContainerId(containerId)
                 .withProviderDistrict(district)
                 .withIssuedOnDate(issuedOnDate != null ? issuedOnDate.toDate() : null)
                 .withStatus(status)
                 .withLabResultsCapturedOn(labResultsCapturedOn != null ? labResultsCapturedOn.toDate() : null)
                 .withConsultationDate(consultationDate != null ? consultationDate.toDate(): null)
                 .withRegistrationInstance(registrationInstance)
                 .withMappingInstance(mappingInstance)
                 .build();
     }
 
     private QueryResult expectedQueryResult(int active, int closed, int pendingReport, int pendingConsultation) {
         return new QueryResultBuilder("active", "closed", "lab_results_pending", "consultation_pending")
                 .row(Long.valueOf(active), Long.valueOf(closed), Long.valueOf(pendingReport), Long.valueOf(pendingConsultation)).build();
     }
 
     private int pendingConsultationDate(int pendingConsultationDate) {
         return pendingConsultationDate;
     }
 
     private int pendingLabResults(int pendingLabResults) {
         return pendingLabResults;
     }
 
     private int closed(int closed) {
         return closed;
     }
 
     private int active(int active) {
         return active;
     }

 }
