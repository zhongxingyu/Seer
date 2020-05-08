 package org.motechproject.ghana.national.repository;
 
 import org.ektorp.CouchDbConnector;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.mrs.services.MRSFacilityAdaptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
 public class AllFacilitiesTest extends AbstractJUnit4SpringContextTests {
 
     @Qualifier("couchDbConnector")
     @Autowired
     protected CouchDbConnector dbConnector;
 
     @Mock
     MRSFacilityAdaptor mockMrsFacilityAdaptor;
 
     @Autowired
     private AllFacilities allFacilities;
 
     @Before
     public void init() {
         initMocks(this);
         ReflectionTestUtils.setField(allFacilities, "facilityAdaptor", mockMrsFacilityAdaptor);
     }
 
     @Test
     public void shouldSaveAFacility() throws Exception {
         final String facilityName = "name";
         final String phoneNumber = "0123456789";
         String additionalPhone1 = "12345";
         String additionalPhone2 = "321234";
         String additionalPhone3 = "33344";
         final org.motechproject.mrs.model.Facility mrsFacility = new org.motechproject.mrs.model.Facility(facilityName);
         Facility facility = new Facility(mrsFacility)
                 .phoneNumber(phoneNumber).additionalPhoneNumber1(additionalPhone1).additionalPhoneNumber2(additionalPhone2)
                 .additionalPhoneNumber3(additionalPhone3);
         final int initialSize = allFacilities.getAll().size();
 
         final String facilityId = "12";
         when(mockMrsFacilityAdaptor.saveFacility(mrsFacility)).thenReturn(new org.motechproject.mrs.model.Facility(facilityId, facilityName,
                 "country", "region", "district", "province"));
 
         allFacilities.add(facility);
 
         final List<Facility> facilities = allFacilities.getAll();
         final int actualSize = facilities.size();
         final Facility actualFacility = facilities.iterator().next();
         assertThat(actualSize, is(equalTo(initialSize + 1)));
         assertThat(actualFacility.phoneNumber(), is(equalTo(phoneNumber)));
         assertThat(actualFacility.additionalPhoneNumber1(), is(equalTo(additionalPhone1)));
         assertThat(actualFacility.additionalPhoneNumber2(), is(equalTo(additionalPhone2)));
         assertThat(actualFacility.additionalPhoneNumber3(), is(equalTo(additionalPhone3)));
         assertThat(actualFacility.mrsFacilityId(), is(equalTo(Integer.parseInt(facilityId))));
     }
 
     @Test
     public void shouldGetAllFacilitiesByName() {
         final String facilityName = "name";
         final String country = "country";
         final String region = "region";
         final String district = "district";
         final String province = "province";
         final String phoneNumber = "0123456789";
         final String mrsFacilityId = "12";
 
         final org.motechproject.mrs.model.Facility mrsFacility = new org.motechproject.mrs.model.Facility(mrsFacilityId,
                 facilityName, country, region, district, province);
         Facility facility = new Facility(mrsFacility)
                 .phoneNumber(phoneNumber);
 
         when(mockMrsFacilityAdaptor.saveFacility(mrsFacility)).thenReturn(new org.motechproject.mrs.model.Facility(mrsFacilityId, facilityName,
                 "country", "region", "district", "province"));
         allFacilities.add(facility);
         when(mockMrsFacilityAdaptor.getFacilities(facilityName)).thenReturn(Arrays.asList(mrsFacility));
 
         final List<Facility> actualFacilities = allFacilities.facilitiesByName(facilityName);
 
         final Facility actualFacility = actualFacilities.iterator().next();
         assertThat(actualFacility.name(), is(equalTo(facilityName)));
         assertThat(actualFacility.country(), is(equalTo(country)));
         assertThat(actualFacility.region(), is(equalTo(region)));
         assertThat(actualFacility.province(), is(equalTo(province)));
         assertThat(actualFacility.district(), is(equalTo(district)));
         assertThat(actualFacility.phoneNumber(), is(equalTo(phoneNumber)));
     }
 
     @Test
    public void shouldReturnListOfFacilitiesByNameEvenIfTheRecordsAreMissingInCouchDb() {
        final String facilityName = "name";
        final String country = "country";
        final String region = "region";
        final String district = "district";
        final String province = "province";
        final String mrsFacilityId = "12";

        final org.motechproject.mrs.model.Facility mrsFacility = new org.motechproject.mrs.model.Facility(mrsFacilityId,
                facilityName, country, region, district, province);
        when(mockMrsFacilityAdaptor.getFacilities(facilityName)).thenReturn(Arrays.asList(mrsFacility));

        final List<Facility> actualFacilities = allFacilities.facilitiesByName(facilityName);

        final Facility actualFacility = actualFacilities.iterator().next();
        assertThat(actualFacility.name(), is(equalTo(facilityName)));
        assertThat(actualFacility.country(), is(equalTo(country)));
        assertThat(actualFacility.region(), is(equalTo(region)));
        assertThat(actualFacility.province(), is(equalTo(province)));
        assertThat(actualFacility.district(), is(equalTo(district)));
    }

    @Test
     public void shouldGetAllFacilities() {
         final String facilityName = "name";
         final String country = "Utopia";
         final String region = "Region 1";
         final String district = "District 1";
         final String province = "Province 1";
         final org.motechproject.mrs.model.Facility facility = new org.motechproject.mrs.model.Facility(
                 facilityName, country, region, district, province);
         when(mockMrsFacilityAdaptor.getFacilities()).thenReturn(Arrays.asList(facility));
         final List<Facility> actualFacilities = allFacilities.facilities();
         final Facility actualFacility = actualFacilities.iterator().next();
         assertThat(actualFacilities.size(), is(equalTo(1)));
         assertThat(actualFacility.name(), is(equalTo(facilityName)));
         assertThat(actualFacility.country(), is(equalTo(country)));
         assertThat(actualFacility.region(), is(equalTo(region)));
         assertThat(actualFacility.province(), is(equalTo(province)));
         assertThat(actualFacility.district(), is(equalTo(district)));
     }
 
     @Test
     public void shouldFetchFacilitiesGivenAListOfMRSFacilityIds() {
         final org.motechproject.mrs.model.Facility anyFacility1 = new org.motechproject.mrs.model.Facility("1", "name", "country", "region", "district", "province");
         final org.motechproject.mrs.model.Facility anyFacility2 = new org.motechproject.mrs.model.Facility("2", "name", "country", "region", "district", "province");
         final org.motechproject.mrs.model.Facility anyFacility3 = new org.motechproject.mrs.model.Facility("3", "name", "country", "region", "district", "province");
         final org.motechproject.mrs.model.Facility anyFacility4 = new org.motechproject.mrs.model.Facility("4", "name", "country", "region", "district", "province");
         final Facility facility1 = new Facility(new org.motechproject.mrs.model.Facility("1", "name1", "country", "region", "district", "province"));
         final Facility facility2 = new Facility(new org.motechproject.mrs.model.Facility("2", "name2", "country", "region", "district", "province"));
         final Facility facility3 = new Facility(new org.motechproject.mrs.model.Facility("3", "name3", "country", "region", "district", "province"));
         final Facility facility4 = new Facility(new org.motechproject.mrs.model.Facility("4", "name4", "country", "region", "district", "province"));
         when(mockMrsFacilityAdaptor.saveFacility(facility1.mrsFacility())).thenReturn(anyFacility1);
         when(mockMrsFacilityAdaptor.saveFacility(facility2.mrsFacility())).thenReturn(anyFacility2);
         when(mockMrsFacilityAdaptor.saveFacility(facility3.mrsFacility())).thenReturn(anyFacility3);
         when(mockMrsFacilityAdaptor.saveFacility(facility4.mrsFacility())).thenReturn(anyFacility4);
         allFacilities.add(facility1);
         allFacilities.add(facility2);
         allFacilities.add(facility3);
         allFacilities.add(facility4);
 
         final List<Facility> byFacilityIds = allFacilities.findByFacilityIds(Arrays.asList(1, 2));
         assertThat(byFacilityIds.size(), is(2));
     }
 
     @After
     public void tearDown() {
         List<Facility> all = allFacilities.getAll();
         for (Facility facility : all)
             allFacilities.remove(facility);
     }
 }
