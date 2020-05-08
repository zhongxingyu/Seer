 package org.motechproject.whp.reports.query;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Test;
 import org.motechproject.bigquery.model.FilterParams;
 import org.motechproject.bigquery.response.QueryResult;
 import org.motechproject.bigquery.service.BigQueryService;
 import org.motechproject.calllog.builder.CallLogBuilder;
 import org.motechproject.calllog.domain.CallLog;
 import org.motechproject.calllog.repository.GenericCallLogRepository;
 import org.motechproject.util.DateUtil;
 import org.motechproject.whp.reports.IntegrationTest;
 import org.motechproject.whp.reports.date.WHPDate;
 import org.motechproject.whp.reports.domain.TreatmentWeek;
 import org.motechproject.whp.reports.domain.adherence.AdherenceRecord;
 import org.motechproject.whp.reports.repository.AdherenceRecordRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.sql.Date;
 
 import static org.junit.Assert.assertEquals;
 import static org.motechproject.whp.reports.date.WHPDate.toSqlDate;
 import static org.motechproject.whp.reports.date.WHPDateTime.toSqlTimestamp;
 
 public class PatientIVRAlertEffectivenessQueryIT extends IntegrationTest{
 
     @Autowired
     AdherenceRecordRepository adherenceRecordRepository;
 
     @Autowired
     GenericCallLogRepository genericCallLogRepository;
 
     @Autowired
     BigQueryService queryService;
 
     @Test
     public void shouldCountNumberOfPatientsCalledAndGivenAdherenceEveryWeek() {
 
         LocalDate threeWeeksAgo = DateUtil.today().minusWeeks(3);
         LocalDate twoWeeksAgo = DateUtil.today().minusWeeks(2);
         LocalDate oneWeeksAgo = DateUtil.today().minusWeeks(1);
 
         DateTime threeWeeksAgoTimestamp = DateUtil.now().minusWeeks(3);
         DateTime twoWeeksAgoTimestamp = DateUtil.now().minusWeeks(2);
         DateTime oneWeeksAgoTimestamp = DateUtil.now().minusWeeks(1);
         String patientId1 = "patientId1";
         String patientId2 = "patientId2";
 
         createAdherenceRecord(patientId1, threeWeeksAgo, "Taken");
         createAdherenceRecord(patientId1, twoWeeksAgo, "Taken");
         createAdherenceRecord(patientId2, twoWeeksAgo, "Taken");
 
         createCallLog(threeWeeksAgoTimestamp, patientId1);
         createCallLog(twoWeeksAgoTimestamp, patientId1);
         createCallLog(twoWeeksAgoTimestamp, patientId2);
         createCallLog(oneWeeksAgoTimestamp, patientId1);
         createCallLog(oneWeeksAgoTimestamp, patientId2);
 
         QueryResult queryResult = queryService.executeQuery("patient.ivrAlerts.effectiveness", new FilterParams());
 
         QueryResult expectedQueryResult = new QueryResultBuilder("patient_with_ivr_calls", "patients_with_adherence_given", "call_week_end_date")
                 .row(1L, 1L, sqlDate(threeWeeksAgo))
                 .row(2L, 2L, sqlDate(twoWeeksAgo))
                 .row(2L, 0L, sqlDate(oneWeeksAgo))
                 .build();
 
         assertEquals(expectedQueryResult, queryResult);
     }
 
     private Date sqlDate(LocalDate threeWeeksAgo) {
         return WHPDate.toSqlDate(new TreatmentWeek(threeWeeksAgo).endDate());
     }
 
     private CallLog createCallLog(DateTime callAttemptDateTime, String patientId1) {
        CallLog callLog = new CallLogBuilder().withDefaults().withAttemptDateTime(toSqlTimestamp(callAttemptDateTime)).withCustomData("patient_id", patientId1).build();
         genericCallLogRepository.save(callLog);
         return callLog;
     }
 
     private AdherenceRecord createAdherenceRecord(String patientId, LocalDate pillDate, String pillStatus) {
         AdherenceRecord adherenceRecord = new AdherenceRecord();
         adherenceRecord.setDistrict("district");
         adherenceRecord.setPatientId(patientId);
         adherenceRecord.setPillStatus(pillStatus);
         adherenceRecord.setProviderId("providerId");
         adherenceRecord.setTbId("tbId");
         adherenceRecord.setTherapyId("therapyId");
         adherenceRecord.setPillDate(toSqlDate(pillDate));
 
         adherenceRecordRepository.save(adherenceRecord);
         return adherenceRecord;
     }
 
     @After
     public void tearDown() {
         adherenceRecordRepository.deleteAll();
         genericCallLogRepository.deleteAll();
     }
 }
 
