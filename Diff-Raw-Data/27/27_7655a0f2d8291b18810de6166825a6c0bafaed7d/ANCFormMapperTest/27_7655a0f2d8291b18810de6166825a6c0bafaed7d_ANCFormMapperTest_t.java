 package org.motechproject.ghana.national.web.helper;
 
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.web.form.ANCEnrollmentForm;
 import org.motechproject.mrs.model.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Date;
 import java.util.HashSet;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class ANCFormMapperTest {
 
     private ANCFormMapper ancFormMapper;
 
     @Before
     public void setUp() {
         ancFormMapper = new ANCFormMapper();
     }
 
     @Test
     public void shouldCreateANCEnrollmentFormFromMRSEncounter() {
         final Date observationDate = new Date();
        final Double gravida = 3.0;
         final Double height = 123.4;
        final Double parity = 3.0;
         final Boolean deliveryDateConfirmed = true;
         final Date estimatedDateOfDelivery = new Date();
         final String serialNumber = "1A23WN3";
 
         String providerId = "121";
         String creatorId = "3232";
         String facilityId = "32";
         Date registrationDate = new Date();
         String patientId = "2343";
         String name = "name";
         String country = "country";
         String region = "region";
         String county = "county";
         String province = "province";
 
 
         final HashSet<MRSObservation> observations = new HashSet<MRSObservation>() {{
            add(new MRSObservation<Double>(observationDate, Constants.CONCEPT_GRAVIDA, gravida));
             add(new MRSObservation<Double>(observationDate, Constants.CONCEPT_HEIGHT, height));
            add(new MRSObservation<Double>(observationDate, Constants.CONCEPT_PARITY, parity));
             add(new MRSObservation<Date>(observationDate, Constants.CONCEPT_EDD, estimatedDateOfDelivery));
             add(new MRSObservation<Boolean>(observationDate, Constants.CONCEPT_CONFINEMENT_CONFIRMED, deliveryDateConfirmed));
             add(new MRSObservation<String>(observationDate, Constants.CONCEPT_ANC_REG_NUM, serialNumber));
             add(new MRSObservation<String>(observationDate, Constants.CONCEPT_IPT, "2"));
             add(new MRSObservation<String>(observationDate, Constants.CONCEPT_TT, "3"));
         }};
         MRSFacility facility = new MRSFacility(facilityId, name, country, region, county, province);
         MRSEncounter mrsEncounter = new MRSEncounter("1", new MRSPerson().id(providerId),
                 new MRSUser().systemId(creatorId), facility, registrationDate, new MRSPatient(patientId, null, null), observations, "type");
         ANCEnrollmentForm ancEnrollmentForm = ancFormMapper.convertMRSEncounterToView(mrsEncounter);
 
         assertThat(ancEnrollmentForm.getMotechPatientId(), is(equalTo(patientId)));
         assertThat(ancEnrollmentForm.getStaffId(), is(equalTo(creatorId)));
         assertThat(ancEnrollmentForm.getAddHistory(), is(true));
         assertThat(ancEnrollmentForm.getFacilityForm().getFacilityId(), is(equalTo(facility.getId())));
         assertThat(ancEnrollmentForm.getFacilityForm().getCountry(), is(equalTo(facility.getCountry())));
         assertThat(ancEnrollmentForm.getFacilityForm().getCountyDistrict(), is(equalTo(facility.getCountyDistrict())));
         assertThat(ancEnrollmentForm.getFacilityForm().getName(), is(equalTo(facility.getName())));
         assertThat(ancEnrollmentForm.getFacilityForm().getStateProvince(), is(equalTo(facility.getStateProvince())));
         assertThat(ancEnrollmentForm.getFacilityForm().getRegion(), is(equalTo(facility.getRegion())));
 
        assertThat(ancEnrollmentForm.getGravida(), is(equalTo(gravida.intValue())));
         assertThat(ancEnrollmentForm.getHeight(), is(equalTo(height)));
        assertThat(ancEnrollmentForm.getParity(), is(equalTo(parity.intValue())));
         assertThat(ancEnrollmentForm.getDeliveryDateConfirmed(), is(equalTo(deliveryDateConfirmed)));
         assertThat(ancEnrollmentForm.getEstimatedDateOfDelivery(), is(equalTo(estimatedDateOfDelivery)));
         assertThat(ancEnrollmentForm.getSerialNumber(), is(equalTo(serialNumber)));
         assertThat(ancEnrollmentForm.getLastIPT(), is(equalTo("2")));
         assertThat(ancEnrollmentForm.getLastTT(), is(equalTo("3")));
 
     }
 }
