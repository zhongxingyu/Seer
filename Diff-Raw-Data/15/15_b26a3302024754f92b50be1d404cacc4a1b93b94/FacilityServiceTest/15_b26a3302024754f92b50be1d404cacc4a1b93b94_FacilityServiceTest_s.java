 package org.motechproject.ghana.national.service;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.exception.FacilityAlreadyFoundException;
 import org.motechproject.ghana.national.exception.FacilityNotFoundException;
 import org.motechproject.ghana.national.repository.AllFacilities;
 import org.motechproject.mrs.model.MRSFacility;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import static junit.framework.Assert.fail;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class FacilityServiceTest {
 
     FacilityService facilityService;
     @Mock
     private AllFacilities mockAllFacilities;
     @Mock
     private IdentifierGenerationService mockidentifierGenerationService;
 
     @Before
     public void init() {
         initMocks(this);
         facilityService = new FacilityService(mockAllFacilities, mockidentifierGenerationService);
     }
 
     @Test
     public void shouldCreateAFacility() throws FacilityAlreadyFoundException {
         String motechId = "12345678";
         String facilityId = "23";
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = "";
         String province = "";
         String phoneNumber = "1";
         String additionalPhoneNumber1 = "2";
         String additionalPhoneNumber2 = "3";
         String additionalPhoneNumber3 = "4";
         when(mockAllFacilities.facilitiesByName(facilityName)).thenReturn(Collections.<Facility>emptyList());
         when(mockidentifierGenerationService.newFacilityId()).thenReturn(motechId);
         facilityService.create(facilityName, country, region, district, province, phoneNumber, additionalPhoneNumber1, additionalPhoneNumber2, additionalPhoneNumber3);
         final ArgumentCaptor<Facility> captor = ArgumentCaptor.forClass(Facility.class);
         verify(mockAllFacilities).add(captor.capture());
 
         final Facility savedFacility = captor.getValue();
         assertThat(savedFacility.motechId(), is(equalTo(motechId)));
         assertThat(savedFacility.name(), is(equalTo(facilityName)));
         assertThat(savedFacility.region(), is(equalTo(region)));
         assertThat(savedFacility.district(), is(equalTo(null)));
         assertThat(savedFacility.province(), is(equalTo(null)));
         assertThat(savedFacility.country(), is(equalTo(country)));
         assertThat(savedFacility.phoneNumber(), is(equalTo(phoneNumber)));
         assertThat(savedFacility.additionalPhoneNumber1(), is(equalTo(additionalPhoneNumber1)));
         assertThat(savedFacility.additionalPhoneNumber2(), is(equalTo(additionalPhoneNumber2)));
         assertThat(savedFacility.additionalPhoneNumber3(), is(equalTo(additionalPhoneNumber3)));
     }
 
     @Test(expected = FacilityAlreadyFoundException.class)
     public void shouldThrowExceptionIfFacilityAlreadyExists() throws FacilityAlreadyFoundException {
         String facilityId = "23";
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = "";
         String province = "";
 
         List<Facility> facilities = Arrays.asList(new Facility(new MRSFacility(facilityName, country, region, district, province)));
         String phoneNumber = "Phone";
         MRSFacility mrsFacility = new MRSFacility(facilityId, facilityName, country, region, district, province);
         FacilityService service = spy(facilityService);
         try {
             service.isDuplicate(facilities, mrsFacility, phoneNumber);
         } catch (FacilityAlreadyFoundException e) {
             assertThat(e.getMessage(), is("facility_already_exists"));
             throw e;
         }
         fail("Expected an exception");
     }
 
     @Test(expected = FacilityAlreadyFoundException.class)
     public void shouldThrowExceptionIfPhoneNumberOfTheFacilityIsAlreadyPresent() throws FacilityAlreadyFoundException {
         String phoneNumber = "phone";
         try {
             facilityService.isDuplicatePhoneNumber(phoneNumber, Arrays.asList(new Facility(new MRSFacility("random")).phoneNumber(phoneNumber)));
         } catch (FacilityAlreadyFoundException e) {
             assertThat(e.getMessage(), is("facility_phone_not_unique"));
             throw e;
         }
         fail("Expected facility already found exception but not thrown");
     }
 
 
     @Test(expected = FacilityAlreadyFoundException.class)
     public void ShouldNotCreateAFacilityIfAlreadyExists() throws FacilityAlreadyFoundException {
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = "district";
         String province = "province";
         when(mockAllFacilities.facilities()).thenReturn(Arrays.asList(new Facility(new MRSFacility(facilityName, country, region, district, province))));
         facilityService.create(facilityName, country, region, district, province, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
     }
 
     @Test(expected = FacilityAlreadyFoundException.class)
     public void ShouldNotCreateFacilityIfPhoneNumberAlreadyExists() throws FacilityAlreadyFoundException {
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = "district";
         String province = "province";
         String testPhoneNumber = "0123456789";
         when(mockAllFacilities.facilities()).thenReturn(Arrays.asList(new Facility().phoneNumber(testPhoneNumber)));
         facilityService.create(facilityName, country, region, district, province, testPhoneNumber, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
     }
 
     @Test
     public void shouldUpdateAFacility() throws FacilityNotFoundException, FacilityAlreadyFoundException {
         String id = "1";
         String facilityId = "12345678";
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = "district";
         String province = "province";
         String phoneNumber = "1";
         String additionalPhoneNumber1 = "2";
         String additionalPhoneNumber2 = "3";
         String additionalPhoneNumber3 = "4";
 
         Facility mockFacility = mock(Facility.class);
 
         when(mockAllFacilities.getFacility(id)).thenReturn(mockFacility);
         Facility facility = createFacilityVO(id, facilityId, facilityName, country, region, district, province, phoneNumber, additionalPhoneNumber1, additionalPhoneNumber2, additionalPhoneNumber3);
         facilityService.update(facility);
         final ArgumentCaptor<Facility> captor = ArgumentCaptor.forClass(Facility.class);
         verify(mockAllFacilities).update(captor.capture());
 
         final Facility savedFacility = captor.getValue();
         assertThat(savedFacility.motechId(), is(equalTo(facilityId)));
         assertThat(savedFacility.mrsFacility().getId(), is(equalTo(id)));
         assertThat(savedFacility.name(), is(equalTo(facilityName)));
         assertThat(savedFacility.region(), is(equalTo(region)));
         assertThat(savedFacility.district(), is(equalTo(district)));
         assertThat(savedFacility.province(), is(equalTo(province)));
         assertThat(savedFacility.country(), is(equalTo(country)));
         assertThat(savedFacility.phoneNumber(), is(equalTo(phoneNumber)));
         assertThat(savedFacility.additionalPhoneNumber1(), is(equalTo(additionalPhoneNumber1)));
         assertThat(savedFacility.additionalPhoneNumber2(), is(equalTo(additionalPhoneNumber2)));
         assertThat(savedFacility.additionalPhoneNumber3(), is(equalTo(additionalPhoneNumber3)));
     }
 
     @Test(expected = FacilityAlreadyFoundException.class)
     public void shouldNotUpdateFacilityIfItMatchesWithSomeOtherFacility() throws FacilityAlreadyFoundException, FacilityNotFoundException {
         String id = "1";
         String facilityId = "12345678";
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = "district";
         String province = "province";
         String phoneNumber = "1";
         String additionalPhoneNumber1 = "2";
         String additionalPhoneNumber2 = "3";
         String additionalPhoneNumber3 = "4";
 
         Facility mockFacility = mock(Facility.class);
 
        when(mockAllFacilities.facilities()).thenReturn(Arrays.asList(new Facility(new MRSFacility(facilityName, country, region, district, province))));
         when(mockAllFacilities.getFacility(id)).thenReturn(mockFacility);
         Facility facility = createFacilityVO(id, facilityId, facilityName, country, region, district, province, phoneNumber, additionalPhoneNumber1, additionalPhoneNumber2, additionalPhoneNumber3);
         facilityService.update(facility);
 
     }
 
 
     @Test(expected = FacilityNotFoundException.class)
     public void shouldNotUpdateFacilityIfFacilityIsNotFound() throws FacilityNotFoundException, FacilityAlreadyFoundException {
         String facilityId = "123456";
         when(mockAllFacilities.getFacility(facilityId)).thenReturn(null);
         facilityService.update(createFacilityVO(facilityId, null, null, null, null, null, null, null, null, null, null));
     }
 
     @Test
     public void shouldGetAllFacilities() {
         facilityService.facilities();
         verify(mockAllFacilities).facilities();
     }
 
     @Test
     public void shouldSearchFacilities() {
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = null;
         String province = null;
         final Facility facility = new Facility(new MRSFacility(facilityName, country, region, district, province));
         when(mockAllFacilities.facilities()).thenReturn(Arrays.asList(facility));
 
         List<Facility> actualFacilities = facilityService.searchFacilities("", "", "cOuNt", "", "", "");
         assertThat(actualFacilities.size(), is(equalTo(1)));
         assertThat(actualFacilities.get(0), is(facility));
     }
 
     @Test
     public void shouldSearchFacilitiesAndReturnEmptyIfNoResultsFound() {
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = null;
         String province = null;
         final Facility facility = new Facility(new MRSFacility(facilityName, country, region, district, province));
         when(mockAllFacilities.facilities()).thenReturn(Arrays.asList(facility));
 
         List<Facility> actualFacilities = facilityService.searchFacilities("", "", "coUnTer", "", "", "");
         assertThat(actualFacilities.size(), is(equalTo(0)));
     }
 
     @Test
     public void shouldGetIfFacilityExists() {
         String facilityId = "0987654";
         Facility facility = mock(Facility.class);
         when(mockAllFacilities.getFacility(facilityId)).thenReturn(facility);
         assertThat(facilityService.getFacility(facilityId), is(equalTo(facility)));
         when(mockAllFacilities.getFacility(facilityId)).thenReturn(null);
         assertThat(facilityService.getFacility(facilityId), is(equalTo(null)));
     }
 
     @Test
     public void shouldGetFacilityByMotechIdIfItExists() {
         String motechFacilityId = "345678";
         Facility facility = mock(Facility.class);
         when(mockAllFacilities.getFacilityByMotechId(motechFacilityId)).thenReturn(facility);
         assertThat(facilityService.getFacilityByMotechId(motechFacilityId), is(equalTo(facility)));
         when(mockAllFacilities.getFacilityByMotechId(motechFacilityId)).thenReturn(null);
         assertThat(facilityService.getFacilityByMotechId(motechFacilityId), is(equalTo(null)));
     }
 
     private Facility createFacilityVO(String id, String facilityId, String facilityName, String country, String region, String district, String province, String phoneNumber, String additionalPhoneNumber1, String additionalPhoneNumber2, String additionalPhoneNumber3) {
         MRSFacility mrsFacility = new MRSFacility(id, facilityName, country, region,
                 district, province);
         return new Facility().mrsFacility(mrsFacility).mrsFacilityId(id).motechId(facilityId).phoneNumber(phoneNumber).
                 additionalPhoneNumber1(additionalPhoneNumber1).additionalPhoneNumber2(additionalPhoneNumber2).additionalPhoneNumber3(additionalPhoneNumber3);
     }
 }
 
