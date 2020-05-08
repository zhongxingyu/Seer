 package org.motechproject.ghana.national.web;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.exception.FacilityAlreadyFoundException;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.ghana.national.web.helper.FacilityHelper;
 import org.motechproject.mrs.model.MRSFacility;
 import org.springframework.context.MessageSource;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Locale;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.anyString;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.web.FacilityController.EDIT_FACILITY_VIEW;
 import static org.motechproject.ghana.national.web.FacilityController.FACILITY_FORM;
 import static org.motechproject.ghana.national.web.FacilityController.FACILITY_ID;
 
 public class FacilityControllerTest {
     FacilityController facilityController;
     @Mock
     FacilityService mockFacilityService;
     @Mock
     MessageSource mockMessageSource;
     @Mock
     BindingResult mockBindingResult;
 
     FacilityHelper facilityHelper;
 
     @Before
     public void setUp() {
         initMocks(this);
         facilityHelper = new FacilityHelper();
         ReflectionTestUtils.setField(facilityHelper, "facilityService", mockFacilityService);
         facilityController = new FacilityController(mockFacilityService, mockMessageSource, facilityHelper);
         mockBindingResult = mock(BindingResult.class);
     }
 
     @Test
     public void shouldRenderForm() {
         when(mockFacilityService.facilities()).thenReturn(Arrays.asList(new Facility(new MRSFacility("facility"))));
         assertThat(facilityController.newFacility(new ModelMap()), is(equalTo("facilities/new")));
     }
 
     @Test
     public void shouldSaveAFacilityWhenValidIdIsSet() throws FacilityAlreadyFoundException {
         String facilityId = "12345";
         String name = "facility";
         String region = "region";
         String district = "district";
         String country = "country";
         String province = "province";
         String addPhoneNumb2 = "addPhoneNumb2";
         String addPhoneNumb3 = "addPhoneNumb3";
         String addPhoneNumb1 = "addPhoneNumb1";
         String phoneNumber = "phoneNumber";
         final BindingResult mockBindingResult = mock(BindingResult.class);
         final FacilityController spyFacilitiesController = spy(facilityController);
         ModelMap modelMap = new ModelMap();
         Facility facility = facility(name, facilityId, region, district, country, province, phoneNumber, addPhoneNumb1, addPhoneNumb2, addPhoneNumb3);
         when(mockFacilityService.facilities()).thenReturn(Arrays.asList(facility));
         final HashMap<String, Object> map = new HashMap<String, Object>() {{
             put(FACILITY_FORM, new Object());
             put(Constants.COUNTRIES, new Object());
             put(Constants.REGIONS, new Object());
             put(Constants.PROVINCES, new Object());
             put(Constants.DISTRICTS, new Object());
         }};
         when(mockFacilityService.getFacility(String.valueOf(facilityId))).thenReturn(facility);
         when(mockFacilityService.create(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(String.valueOf(facilityId));
         FacilityForm facilityForm = createFacilityForm(name, country, region, district, province, phoneNumber, addPhoneNumb1, addPhoneNumb2, addPhoneNumb3);
         
         String result = spyFacilitiesController.create(facilityForm, mockBindingResult, modelMap);
 
         verify(mockFacilityService).create(name, country, region, district, province, phoneNumber, addPhoneNumb1, addPhoneNumb2, addPhoneNumb3);
         assertThat(result, is(equalTo(EDIT_FACILITY_VIEW)));
         assertNotNull(modelMap.get(FACILITY_FORM));
         assertNotNull(modelMap.get(Constants.COUNTRIES));
         assertNotNull(modelMap.get(Constants.REGIONS));
         assertNotNull(modelMap.get(Constants.PROVINCES));
         assertNotNull(modelMap.get(Constants.DISTRICTS));
     }
 
     @Test
     public void shouldtNotSaveFacilityWhenInValid() throws FacilityAlreadyFoundException {
         final ModelMap modelMap = new ModelMap();
         final String facility = "facility";
         final String country = "country";
         final String region = "region";
         final String district = "district";
         final String province = "province";
         final String message = "Facility already exists.";
         final FacilityController spyFacilitiesController = spy(facilityController);
         doThrow(new FacilityAlreadyFoundException()).when(mockFacilityService).create(facility, country, region, district, province, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
         when(mockMessageSource.getMessage("facility_already_exists", null, Locale.getDefault())).thenReturn(message);
 
         final FacilityForm createFacilityForm = new FacilityForm();
         createFacilityForm.setName(facility);
         createFacilityForm.setCountry(country);
         createFacilityForm.setRegion(region);
         createFacilityForm.setCountyDistrict(district);
         createFacilityForm.setStateProvince(province);
         createFacilityForm.setPhoneNumber(StringUtils.EMPTY);
         createFacilityForm.setAdditionalPhoneNumber1(StringUtils.EMPTY);
         createFacilityForm.setAdditionalPhoneNumber2(StringUtils.EMPTY);
         createFacilityForm.setAdditionalPhoneNumber3(StringUtils.EMPTY);
 
         final HashMap<String, Object> map = new HashMap<String, Object>() {{
             put(FACILITY_FORM, new Object());
             put(Constants.COUNTRIES, new Object());
             put(Constants.REGIONS, new Object());
             put(Constants.PROVINCES, new Object());
             put(Constants.DISTRICTS, new Object());
         }};
 
         final String result = spyFacilitiesController.create(createFacilityForm, mockBindingResult, modelMap);
 
         final ArgumentCaptor<FieldError> captor = ArgumentCaptor.forClass(FieldError.class);
         verify(mockBindingResult).addError(captor.capture());
         final FieldError actualFieldError = captor.getValue();
         assertThat(result, is(equalTo("facilities/new")));
         assertThat(actualFieldError.getObjectName(), is(equalTo(FACILITY_FORM)));
         assertThat(actualFieldError.getField(), is(equalTo("name")));
         assertThat(actualFieldError.getDefaultMessage(), is(equalTo(message)));
         assertNotNull(modelMap.get(Constants.COUNTRIES));
         assertNotNull(modelMap.get(Constants.REGIONS));
         assertNotNull(modelMap.get(Constants.PROVINCES));
         assertNotNull(modelMap.get(Constants.DISTRICTS));
     }
 
     @Test
     public void shouldSearchForFacility() throws FacilityAlreadyFoundException {
         final String id = "id";
         final String name = "name";
         final String country = "country";
         final String region = "region";
         final String district = "district";
         final String province = "province";
         final FacilityForm searchFacilityForm = new FacilityForm();
         searchFacilityForm.setFacilityId(id);
         searchFacilityForm.setName(name);
         searchFacilityForm.setCountry(country);
         searchFacilityForm.setRegion(region);
         searchFacilityForm.setCountyDistrict(district);
         searchFacilityForm.setStateProvince(province);
 
         facilityController.search(searchFacilityForm, new ModelMap());
 
         verify(mockFacilityService).searchFacilities(id, name, country, region, district, province);
     }
 
     @Test
     public void shouldReturnDetailsOfFacilityToEdit() {
         String name = "Dummy Facility";
         String country = "Ghana";
         String region = "Western Ghana";
         String province = "Province";
         String district = "District";
         String phoneNumber = "0123456789";
         String addPhoneNumber1 = "0123456787";
         String addPhoneNumber2 = "0123456786";
         String addPhoneNumber3 = "0123456783";
 
         ModelMap modelMap = new ModelMap();
         String facilityId = "123";
         modelMap.addAttribute(FACILITY_ID, facilityId);
 
         Facility facility = mock(Facility.class);
         when(facility.country()).thenReturn(country);
         when(facility.phoneNumber()).thenReturn(phoneNumber);
         when(facility.region()).thenReturn(region);
         when(facility.name()).thenReturn(name);
         when(facility.province()).thenReturn(province);
         when(facility.additionalPhoneNumber1()).thenReturn(addPhoneNumber1);
         when(facility.additionalPhoneNumber2()).thenReturn(addPhoneNumber2);
         when(facility.additionalPhoneNumber3()).thenReturn(addPhoneNumber3);
         when(facility.district()).thenReturn(district);
 
         when(mockFacilityService.getFacility(facilityId)).thenReturn(facility);
 
         MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
         mockHttpServletRequest.setParameter(FACILITY_ID, facilityId);
         String editFormName = facilityController.edit(modelMap, mockHttpServletRequest);
 
         assertThat(editFormName, is(EDIT_FACILITY_VIEW));
         FacilityForm expectedFacilityForm = createFacilityForm(name, country, region, district, province, phoneNumber, addPhoneNumber1, addPhoneNumber2, addPhoneNumber3);
         FacilityForm facilityForm = (FacilityForm) modelMap.get(FACILITY_FORM);
 
         assertFacilityForm(expectedFacilityForm, facilityForm);
     }
 
     private void assertFacilityForm(FacilityForm expectedFacilityForm, FacilityForm facilityForm) {
         assertThat(facilityForm.getName(), is(equalTo(expectedFacilityForm.getName())));
         assertThat(facilityForm.getCountry(), is(equalTo(expectedFacilityForm.getCountry())));
         assertThat(facilityForm.getRegion(), is(equalTo(expectedFacilityForm.getRegion())));
         assertThat(facilityForm.getCountyDistrict(), is(equalTo(expectedFacilityForm.getCountyDistrict())));
         assertThat(facilityForm.getStateProvince(), is(equalTo(expectedFacilityForm.getStateProvince())));
         assertThat(facilityForm.getPhoneNumber(), is(equalTo(expectedFacilityForm.getPhoneNumber())));
         assertThat(facilityForm.getAdditionalPhoneNumber1(), is(equalTo(expectedFacilityForm.getAdditionalPhoneNumber1())));
         assertThat(facilityForm.getAdditionalPhoneNumber2(), is(equalTo(expectedFacilityForm.getAdditionalPhoneNumber2())));
         assertThat(facilityForm.getAdditionalPhoneNumber3(), is(equalTo(expectedFacilityForm.getAdditionalPhoneNumber3())));
     }
 
     private Facility facility(String name, String facilityId, String region, String district, String country, String province, String phoneNumber, String addPhoneNumb1, String addPhoneNumb2, String addPhoneNumb3) {
         MRSFacility mrsFacility = new MRSFacility(name, country, region, district, province);
         return new Facility().motechId("2134").mrsFacility(mrsFacility).mrsFacilityId(facilityId).phoneNumber(phoneNumber).additionalPhoneNumber1(addPhoneNumb1).additionalPhoneNumber2(addPhoneNumb2).additionalPhoneNumber3(addPhoneNumb3);
     }
 
     private FacilityForm createFacilityForm(String name, String country, String region, String district, String province, String phoneNumber, String addPhoneNumber1, String addPhoneNumber2, String addPhoneNumber3) {
         FacilityForm facilityForm = new FacilityForm();
         facilityForm.setName(name);
         facilityForm.setCountry(country);
         facilityForm.setRegion(region);
         facilityForm.setCountyDistrict(district);
         facilityForm.setStateProvince(province);
         facilityForm.setPhoneNumber(phoneNumber);
         facilityForm.setAdditionalPhoneNumber1(addPhoneNumber1);
         facilityForm.setAdditionalPhoneNumber2(addPhoneNumber2);
         facilityForm.setAdditionalPhoneNumber3(addPhoneNumber3);
         return facilityForm;
     }
 }
