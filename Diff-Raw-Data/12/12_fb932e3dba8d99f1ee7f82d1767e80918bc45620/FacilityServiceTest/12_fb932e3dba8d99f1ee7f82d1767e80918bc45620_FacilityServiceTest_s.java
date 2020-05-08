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
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
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
         String district = "district";
         String province = "province";
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
         assertThat(savedFacility.district(), is(equalTo(district)));
         assertThat(savedFacility.province(), is(equalTo(province)));
         assertThat(savedFacility.country(), is(equalTo(country)));
         assertThat(savedFacility.phoneNumber(), is(equalTo(phoneNumber)));
         assertThat(savedFacility.additionalPhoneNumber1(), is(equalTo(additionalPhoneNumber1)));
         assertThat(savedFacility.additionalPhoneNumber2(), is(equalTo(additionalPhoneNumber2)));
         assertThat(savedFacility.additionalPhoneNumber3(), is(equalTo(additionalPhoneNumber3)));
     }
 
     @Test
     public void shouldUpdateAFacility() throws FacilityNotFoundException {
         String facilityId = "12";
         String motechId = "12345678";
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
 
         when(mockAllFacilities.getFacility(facilityId)).thenReturn(mockFacility);
         facilityService.update(facilityId, motechId, facilityName, country, region, district, province, phoneNumber, additionalPhoneNumber1, additionalPhoneNumber2, additionalPhoneNumber3);
         final ArgumentCaptor<Facility> captor = ArgumentCaptor.forClass(Facility.class);
         verify(mockAllFacilities).update(captor.capture());
 
         final Facility savedFacility = captor.getValue();
         assertThat(savedFacility.motechId(), is(equalTo(motechId)));
         assertThat(savedFacility.mrsFacility().getId(), is(equalTo(facilityId)));
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
 
     @Test(expected = FacilityNotFoundException.class)
     public void shouldNotUpdateFacilityIfFacilityIsNotFound() throws FacilityNotFoundException {
         String facilityId = "123456";
         when(mockAllFacilities.getFacility(facilityId)).thenReturn(null);
         facilityService.update(facilityId,null, null, null, null, null, null, null, null, null, null);
     }
 
     @Test(expected = FacilityAlreadyFoundException.class)
     public void ShouldNotCreateAFacilityIfAlreadyExists() throws FacilityAlreadyFoundException {
         String facilityName = "name";
         String country = "country";
         String region = "region";
         String district = "district";
         String province = "province";
        when(mockAllFacilities.facilitiesByName(facilityName)).thenReturn(Arrays.asList(new Facility(new MRSFacility(facilityName, country, region, district, province))));
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
        when(mockAllFacilities.facilitiesByName(facilityName)).thenReturn(Arrays.asList(new Facility().phoneNumber(testPhoneNumber)));
         facilityService.create(facilityName, country, region, district, province, testPhoneNumber, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
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
     public void shouldValidateIfFacilityExists() {
         String facilityId = "0987654";
         Facility facility = mock(Facility.class);
         when(mockAllFacilities.getFacility(facilityId)).thenReturn(facility);
         assertThat(facilityService.getFacility(facilityId), is(equalTo(facility)));
         when(mockAllFacilities.getFacility(facilityId)).thenReturn(null);
         assertThat(facilityService.getFacility(facilityId), is(equalTo(null)));
     }
 }
 
