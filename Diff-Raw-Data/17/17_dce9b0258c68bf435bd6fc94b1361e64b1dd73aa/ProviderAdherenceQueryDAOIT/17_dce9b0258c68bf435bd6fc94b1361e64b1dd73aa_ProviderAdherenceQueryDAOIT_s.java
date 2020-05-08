 package org.motechproject.whp.reports.dao;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.whp.reports.IntegrationTest;
 import org.motechproject.whp.reports.builder.PatientBuilder;
 import org.motechproject.whp.reports.contract.adherence.ProviderAdherenceSummary;
import org.motechproject.whp.reports.date.WHPDate;
 import org.motechproject.whp.reports.date.WHPDateTime;
 import org.motechproject.whp.reports.domain.adherence.AdherenceAuditLog;
 import org.motechproject.whp.reports.domain.adherence.AdherenceRecord;
 import org.motechproject.whp.reports.domain.dimension.Provider;
 import org.motechproject.whp.reports.domain.patient.Patient;
 import org.motechproject.whp.reports.model.AdherenceStatus;
 import org.motechproject.whp.reports.repository.AdherenceAuditLogRepository;
 import org.motechproject.whp.reports.repository.AdherenceRecordRepository;
 import org.motechproject.whp.reports.repository.PatientRepository;
 import org.motechproject.whp.reports.repository.ProviderRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.sql.Date;
 import java.sql.Timestamp;
 import java.util.List;
import java.util.Map;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.is;
 import static org.motechproject.whp.reports.date.WHPDate.toSqlDate;
 
 public class ProviderAdherenceQueryDAOIT extends IntegrationTest {
 
     @Autowired
     ProviderRepository providerRepository;
 
     @Autowired
     AdherenceRecordRepository adherenceRecordRepository;
 
     @Autowired
     AdherenceAuditLogRepository adherenceAuditLogRepository;
 
     @Autowired
     PatientRepository patientRepository;
 
     @Autowired
     ProviderAdherenceQueryDAO providerAdherenceQueryDAO;
 
 
     private Patient patientFromDifferentDistrict;
     private Patient patientWithClosedTreatment;
     private Patient patientWithAdherence;
     private Patient patientWithoutAdherence;
     private final Date olderPillDate = toSqlDate(new LocalDate(2013, 1, 1));
     private final Date currentWeekPillDate = toSqlDate(new LocalDate(2013, 1, 24));
     private final Date currentTreatmentWeekStartDate = toSqlDate(new LocalDate(2013, 1, 20));
     private final Date currentTreatmentWeekEndDate = toSqlDate(new LocalDate(2013, 1, 26));
     private final static String DISTRICT = "district";
     private Provider providerWithoutAdherence;
     private Provider providerWithAdherence;
     private Provider providerWithNoActivePatients;
     private Provider providerWithDifferentDistrict;
     private AdherenceRecord adherenceRecordForPatientWithoutAdherence;
     private AdherenceRecord adherenceRecordForPatientWithAdherence;
     private AdherenceRecord adherenceRecordForClosedPatient;
     private AdherenceRecord adherenceRecordForDifferentDistrict;
 
     @Before
     public void setUp() {
 
         mockCurrentDate(new LocalDate(2013, 1, 27));
 
         providerWithoutAdherence = createProvider("provider1", DISTRICT);
         providerWithAdherence = createProvider("provider2", DISTRICT);
         providerWithNoActivePatients = createProvider("provider3", DISTRICT);
         providerWithDifferentDistrict = createProvider("provider4", "districtB");
 
         Date startDate = toSqlDate(new LocalDate(2013, 1, 1));
         Date endDate = toSqlDate(new LocalDate(2013, 1, 8));
 
         patientWithoutAdherence = new PatientBuilder()
                 .withDefaults()
                 .withPatientId("patient1")
                 .withProviderId(providerWithoutAdherence.getProviderId())
                 .withDistrict(providerWithoutAdherence.getDistrict())
                 .withTreatmentStartDate(startDate)
                 .withTreatmentEndDate(null)
                 .withTbRegistrationDate(startDate)
                 .build();
 
         patientWithAdherence = new PatientBuilder()
                 .withDefaults()
                 .withPatientId("patient2")
                 .withProviderId(providerWithAdherence.getProviderId())
                 .withDistrict(providerWithAdherence.getDistrict())
                 .withTreatmentStartDate(startDate)
                 .withTreatmentEndDate(null)
                 .withTbRegistrationDate(startDate)
                 .build();
 
         patientWithClosedTreatment = new PatientBuilder()
                 .withDefaults()
                 .withPatientId("patient3")
                 .withProviderId(providerWithNoActivePatients.getProviderId())
                 .withDistrict(providerWithNoActivePatients.getDistrict())
                 .withTreatmentStartDate(startDate)
                 .withTreatmentEndDate(endDate)
                 .withTbRegistrationDate(startDate)
                 .build();
 
         patientFromDifferentDistrict = new PatientBuilder()
                 .withDefaults()
                 .withPatientId("patient4")
                 .withProviderId(providerWithDifferentDistrict.getProviderId())
                 .withDistrict(providerWithDifferentDistrict.getDistrict())
                 .withTbRegistrationDate(startDate)
                 .withTreatmentStartDate(startDate)
                 .withTreatmentEndDate(null)
                 .build();
 
         adherenceRecordForPatientWithoutAdherence = createAdherenceRecord(patientWithoutAdherence.getPatientId(), providerWithoutAdherence.getProviderId(), DISTRICT, olderPillDate);
         adherenceRecordForPatientWithAdherence = createAdherenceRecord(patientWithAdherence.getPatientId(), providerWithAdherence.getProviderId(), DISTRICT, currentWeekPillDate);
         adherenceRecordForClosedPatient = createAdherenceRecord(patientWithClosedTreatment.getPatientId(), providerWithNoActivePatients.getProviderId(), DISTRICT, currentWeekPillDate);
         adherenceRecordForDifferentDistrict = createAdherenceRecord(patientFromDifferentDistrict.getPatientId(), providerWithDifferentDistrict.getProviderId(), "districtB", currentWeekPillDate);
 
 
         providerRepository.save(providerWithoutAdherence);
         providerRepository.save(providerWithAdherence);
         providerRepository.save(providerWithNoActivePatients);
         providerRepository.save(providerWithDifferentDistrict);
 
         patientRepository.save(patientWithoutAdherence);
         patientRepository.save(patientWithAdherence);
         patientRepository.save(patientWithClosedTreatment);
         patientRepository.save(patientFromDifferentDistrict);
 
         adherenceRecordRepository.save(adherenceRecordForPatientWithoutAdherence);
         adherenceRecordRepository.save(adherenceRecordForPatientWithAdherence);
         adherenceRecordRepository.save(adherenceRecordForClosedPatient);
         adherenceRecordRepository.save(adherenceRecordForDifferentDistrict);
     }
 
     @Test
     public void shouldReturnAdherenceSummariesForCurrentTreatmentWeek() {
 
         List<ProviderAdherenceSummary> providerAdherenceSummaries = providerAdherenceQueryDAO.getProviderAdherenceSummaries(DISTRICT);
 
         ProviderAdherenceSummary expectedProviderWithAdherence = createProviderAdherenceSummary(this.providerWithAdherence, true);
         ProviderAdherenceSummary expectedProviderWithoutAdherence = createProviderAdherenceSummary(this.providerWithoutAdherence, false);
 
         assertThat(providerAdherenceSummaries.size(), is(2));
         assertThat(providerAdherenceSummaries, hasItems(expectedProviderWithAdherence, expectedProviderWithoutAdherence));
     }
 
     @Test
     public void shouldReturnAdherenceAuditStatusForGivenDistrictAndWeek() {
         AdherenceAuditLog auditLogForProviderWithAdherence = createAdherenceAuditLog(providerWithAdherence.getProviderId(), WHPDateTime.toSqlTimestamp(new DateTime(2013, 1, 23, 0, 0)));
         AdherenceAuditLog auditLogForProviderWithoutAdherence = createAdherenceAuditLog(providerWithoutAdherence.getProviderId(), WHPDateTime.toSqlTimestamp(new DateTime(2013, 1, 8, 0, 0)));
 
         adherenceAuditLogRepository.save(auditLogForProviderWithAdherence);
         adherenceAuditLogRepository.save(auditLogForProviderWithoutAdherence);
 
         List<AdherenceStatus> adherenceGivenStatuses = providerAdherenceQueryDAO.getAdherenceGivenStatus(DISTRICT,
                 toSqlDate(new LocalDate(2013, 1, 13)),
                 toSqlDate(new LocalDate(2013, 1, 19)));
 
         assertThat(adherenceGivenStatuses.size(), is(2));
         assertThat(adherenceGivenStatuses, hasItems(new AdherenceStatus(providerWithAdherence.getProviderId(), true),
                 new AdherenceStatus(providerWithoutAdherence.getProviderId(), false)));
     }
 
     private AdherenceAuditLog createAdherenceAuditLog(String providerId, Timestamp creationTime) {
         AdherenceAuditLog auditLog = new AdherenceAuditLog();
         auditLog.setCreationTime(new Timestamp(creationTime.getTime()));
         auditLog.setProviderId(providerId);
         auditLog.setUserId(providerId);
         return auditLog;
     }
 
     private ProviderAdherenceSummary createProviderAdherenceSummary(Provider provider, boolean adherenceGiven) {
         return new ProviderAdherenceSummary(provider.getProviderId(),
                 provider.getPrimaryMobile(),
                 provider.getSecondaryMobile(),
                 provider.getTertiaryMobile(),
                 adherenceGiven,
                 0);
     }
 
     private AdherenceRecord createAdherenceRecord(String patientId, String providerId,  String district, Date pillDate) {
         AdherenceRecord adherenceRecord = new AdherenceRecord();
         adherenceRecord.setDistrict(district);
         adherenceRecord.setPatientId(patientId);
         adherenceRecord.setProviderId(providerId);
         adherenceRecord.setPillDate(pillDate);
         adherenceRecord.setPillStatus("Taken");
         adherenceRecord.setTherapyId("therapyId");
         adherenceRecord.setTbId("tbId");
         return adherenceRecord;
     }
 
     private Provider createProvider(String providerId, String district) {
         Provider provider = new Provider();
         provider.setProviderId(providerId);
         provider.setDistrict(district);
         return provider;
     }
 
     @Override
     @After
     public void tearDown() {
         patientRepository.delete(patientWithAdherence);
         patientRepository.delete(patientWithoutAdherence);
         patientRepository.delete(patientWithClosedTreatment);
         patientRepository.delete(patientFromDifferentDistrict);
 
         providerRepository.delete(providerWithAdherence);
         providerRepository.delete(providerWithoutAdherence);
         providerRepository.delete(providerWithNoActivePatients);
         providerRepository.delete(providerWithDifferentDistrict);
 
         adherenceRecordRepository.delete(adherenceRecordForPatientWithAdherence);
         adherenceRecordRepository.delete(adherenceRecordForPatientWithoutAdherence);
         adherenceRecordRepository.delete(adherenceRecordForClosedPatient);
         adherenceRecordRepository.delete(adherenceRecordForDifferentDistrict);
     }
 }
