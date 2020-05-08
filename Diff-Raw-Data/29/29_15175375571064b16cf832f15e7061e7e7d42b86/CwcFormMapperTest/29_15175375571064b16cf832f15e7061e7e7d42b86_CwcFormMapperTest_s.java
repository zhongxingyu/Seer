 package org.motechproject.ghana.national.web.helper;
 
 import org.junit.Test;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.CwcCareHistory;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.web.CWCController;
 import org.motechproject.ghana.national.web.form.CWCEnrollmentForm;
 import org.motechproject.mrs.model.MRSEncounter;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSObservation;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.mrs.model.MRSUser;
 import org.openmrs.Concept;
 import org.openmrs.ConceptName;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 
 import static org.hamcrest.CoreMatchers.allOf;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasEntry;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class CwcFormMapperTest {
 
     @Test
     public void shouldSetViewAttributes() {
         final CwcFormMapper cwcFormMapper = new CwcFormMapper();
         final Map<String, Object> actual = cwcFormMapper.setViewAttributes();
 
         Map<CwcCareHistory, String> cwcCareHistories = new HashMap<CwcCareHistory, String>();
         for (CwcCareHistory cwcCareHistory : CwcCareHistory.values()) {
             cwcCareHistories.put(cwcCareHistory, cwcCareHistory.getDescription());
         }
 
         Map<RegistrationToday, String> registrationTodayValues = new HashMap<RegistrationToday, String>();
 
         for (RegistrationToday registrationToday : RegistrationToday.values()) {
             registrationTodayValues.put(registrationToday, registrationToday.getDescription());
         }
 
         assertThat((Map<CwcCareHistory, String>) actual.get(Constants.CARE_HISTORIES), is(cwcCareHistories));
         assertThat((Map<RegistrationToday, String>) actual.get(CWCController.REGISTRATION_OPTIONS), is(registrationTodayValues));
         assertThat((Map<Integer, String>) actual.get(Constants.LAST_IPTI), allOf(
                 hasEntry(1, Constants.IPTI_1),
                 hasEntry(2, Constants.IPTI_2),
                 hasEntry(3, Constants.IPTI_3)
         ));
         assertThat((Map<Integer, String>) actual.get(Constants.LAST_OPV), allOf(
                 hasEntry(0, Constants.OPV_0),
                 hasEntry(1, Constants.OPV_1),
                 hasEntry(2, Constants.OPV_2),
                 hasEntry(3, Constants.OPV_3)
         ));
         assertThat((Map<Integer, String>) actual.get(Constants.LAST_PENTA), allOf(
                 hasEntry(1, Constants.PENTA_1),
                 hasEntry(2, Constants.PENTA_2),
                 hasEntry(3, Constants.PENTA_3)
         ));
     }
 
     @Test
     public void shouldConvertEncounterToFormView() {
         final CwcFormMapper cwcFormMapper = new CwcFormMapper();
 
         String providerId = "121";
         String creatorId = "3232";
         String facilityId = "32";
         Date registrationDate = new Date();
         String patientId = "2343";
         final HashSet<MRSObservation> observations = new HashSet<MRSObservation>();
         Concept measlesValue = mock(Concept.class);
         Concept yfValue = mock(Concept.class);
         Concept bcgValue = mock(Concept.class);
         Concept vitaValue = mock(Concept.class);
 
         when(measlesValue.getName()).thenReturn(new ConceptName(Constants.CONCEPT_MEASLES, Locale.getDefault()));
         when(yfValue.getName()).thenReturn(new ConceptName(Constants.CONCEPT_YF, Locale.getDefault()));
         when(bcgValue.getName()).thenReturn(new ConceptName(Constants.CONCEPT_BCG, Locale.getDefault()));
         when(vitaValue.getName()).thenReturn(new ConceptName(Constants.CONCEPT_VITA, Locale.getDefault()));
 
 
         Date measlesDate = new Date();
         Date yfDate = new Date();
         Date bcgDate = new Date();
         Date vitaDate = new Date();
         Date iptiDate = new Date();
         Date opvDate = new Date();
         Date pentaDate = new Date();
        Integer iptiValue = 0;
         String serialNum = "serial number";
        Integer pentaValue = 2;
        Integer opvValue = 1;
         String name = "name";
         String country = "country";
         String region = "region";
         String county = "county";
         String province = "province";
         observations.add(new MRSObservation(measlesDate, Constants.CONCEPT_IMMUNIZATIONS_ORDERED, measlesValue));
         observations.add(new MRSObservation(yfDate, Constants.CONCEPT_IMMUNIZATIONS_ORDERED, yfValue));
         observations.add(new MRSObservation(bcgDate, Constants.CONCEPT_IMMUNIZATIONS_ORDERED, bcgValue));
         observations.add(new MRSObservation(vitaDate, Constants.CONCEPT_IMMUNIZATIONS_ORDERED, vitaValue));
         observations.add(new MRSObservation(iptiDate, Constants.CONCEPT_IPTI, iptiValue));
         observations.add(new MRSObservation(pentaDate, Constants.CONCEPT_PENTA, pentaValue));
         observations.add(new MRSObservation(opvDate, Constants.CONCEPT_OPV, opvValue));
         observations.add(new MRSObservation(registrationDate, Constants.CONCEPT_CWC_REG_NUMBER, serialNum));
         MRSFacility facility = new MRSFacility(facilityId, name, country, region, county, province);
         MRSEncounter mrsEncounter = new MRSEncounter("1", new MRSPerson().id(providerId),
                 new MRSUser().systemId(creatorId), facility, registrationDate, new MRSPatient(patientId, null, null), observations, "type");
 
         final CWCEnrollmentForm cwcEnrollmentForm = cwcFormMapper.mapEncounterToView(mrsEncounter);
 
         assertThat(cwcEnrollmentForm.getPatientMotechId(), is(equalTo(patientId)));
         assertThat(cwcEnrollmentForm.getStaffId(), is(equalTo(creatorId)));
         assertThat(cwcEnrollmentForm.getAddHistory(), is(true));
         assertThat(cwcEnrollmentForm.getFacilityForm().getFacilityId(), is(equalTo(facility.getId())));
         assertThat(cwcEnrollmentForm.getFacilityForm().getCountry(), is(equalTo(facility.getCountry())));
         assertThat(cwcEnrollmentForm.getFacilityForm().getCountyDistrict(), is(equalTo(facility.getCountyDistrict())));
         assertThat(cwcEnrollmentForm.getFacilityForm().getName(), is(equalTo(facility.getName())));
         assertThat(cwcEnrollmentForm.getFacilityForm().getStateProvince(), is(equalTo(facility.getStateProvince())));
         assertThat(cwcEnrollmentForm.getFacilityForm().getRegion(), is(equalTo(facility.getRegion())));
 
         assertThat(cwcEnrollmentForm.getMeaslesDate(), is(equalTo(measlesDate)));
         assertThat(cwcEnrollmentForm.getBcgDate(), is(equalTo(bcgDate)));
         assertThat(cwcEnrollmentForm.getYfDate(), is(equalTo(yfDate)));
         assertThat(cwcEnrollmentForm.getVitADate(), is(equalTo(vitaDate)));
         assertThat(cwcEnrollmentForm.getLastIPTiDate(), is(equalTo(iptiDate)));
        assertThat(cwcEnrollmentForm.getLastIPTi(), is(equalTo(iptiValue)));
         assertThat(cwcEnrollmentForm.getLastPentaDate(), is(equalTo(pentaDate)));
        assertThat(cwcEnrollmentForm.getLastPenta(), is(equalTo(pentaValue)));
         assertThat(cwcEnrollmentForm.getLastOPVDate(), is(equalTo(opvDate)));
        assertThat(cwcEnrollmentForm.getLastOPV(), is(equalTo(opvValue)));
         assertThat(cwcEnrollmentForm.getSerialNumber(), is(equalTo(serialNum)));
 
 
     }
 
 }
