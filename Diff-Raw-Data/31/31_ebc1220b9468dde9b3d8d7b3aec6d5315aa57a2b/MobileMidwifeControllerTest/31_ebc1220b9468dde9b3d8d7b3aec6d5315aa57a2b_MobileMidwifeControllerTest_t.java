 package org.motechproject.ghana.national.web;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.mobilemidwife.*;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.service.MobileMidwifeService;
 import org.motechproject.ghana.national.validator.FormValidator;
 import org.motechproject.ghana.national.validator.MobileMidwifeValidator;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.ghana.national.web.form.MobileMidwifeEnrollmentForm;
 import org.motechproject.ghana.national.web.form.MobileMidwifeUnEnrollForm;
 import org.motechproject.ghana.national.web.helper.FacilityHelper;
 import org.motechproject.mobileforms.api.domain.FormBean;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.model.Time;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.util.DateUtil;
 import org.springframework.context.MessageSource;
 import org.springframework.ui.ModelMap;
 
 import java.util.*;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.TestUtils.isEq;
 import static org.motechproject.ghana.national.web.MobileMidwifeController.MOBILE_MIDWIFE_URL;
 import static org.motechproject.ghana.national.web.MobileMidwifeController.SUCCESS_MESSAGE;
 
 public class MobileMidwifeControllerTest {
 
     MobileMidwifeController controller;
 
     @Mock
     private MobileMidwifeValidator mobileMidwifeValidator;
     @Mock
     private MobileMidwifeService mobileMidwifeService;
     @Mock
     private MessageSource messages;
     @Mock
     private FacilityHelper mockFacilityHelper;
     @Mock
     private FacilityService mockFacilityService;
     @Mock
     private FormValidator mockFormValidator;
 
     @Before
     public void setUp() {
         initMocks(this);
         controller = new MobileMidwifeController(mobileMidwifeValidator, mobileMidwifeService, messages, mockFacilityHelper, mockFacilityService, mockFormValidator);
     }
 
     @Test
     public void shouldRetrieveEnrollmentForAPatient() {
         String patientId = "12344";
         MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollment(DateTime.now());
         enrollment.setActive(true);
         String locationId = "facilityMotechId";
         enrollment.setFacilityId(locationId);
         when(mobileMidwifeService.findLatestEnrollment(patientId)).thenReturn(enrollment);
         when(mockFacilityHelper.locationMap()).thenReturn(new HashMap());
         Facility mockFacility = mock(Facility.class);
         when(mockFacilityService.getFacilityByMotechId(locationId)).thenReturn(mockFacility);
         String facilityId = "13161";
         when(mockFacility.getMotechId()).thenReturn(facilityId);
 
         ModelMap modelMap = new ModelMap();
         controller.form(patientId, modelMap);
 
         MobileMidwifeEnrollmentForm enrollmentForm = (MobileMidwifeEnrollmentForm) modelMap.get("mobileMidwifeEnrollmentForm");
         assertFormWithEnrollment(enrollmentForm, enrollment);
         assertThat(enrollmentForm.getEnrollmentDateTime(), isEq(enrollment.getEnrollmentDateTime()));
         assertThat(enrollmentForm.getStatus(), is("ACTIVE"));
     }
 
     @Test
     public void shouldCreateMobileMidwifeEnrollmentFromEnrollmentForm() {
         String locationId = "54";
         MobileMidwifeEnrollmentForm enrollmentForm = createEnrollmentForm("12344", locationId, "345", Medium.SMS, new Time(23, 45));
         Facility mockFacility = mock(Facility.class);
         when(mockFacilityService.getFacility(locationId)).thenReturn(mockFacility);
         String facilityId = "13161";
         when(mockFacility.getMotechId()).thenReturn(facilityId);
 
         MobileMidwifeEnrollment midwifeEnrollment = controller.createEnrollment(enrollmentForm, DateUtil.now());
         assertEquals(facilityId, midwifeEnrollment.getFacilityId());
         assertFormWithEnrollment(enrollmentForm, midwifeEnrollment);
         assertEquals(new LocalDate(), midwifeEnrollment.getEnrollmentDateTime().toLocalDate());
     }
 
     @Test
     public void shouldValidateAndCreateANewMobileMidwifeEnrollmentIfActionIsRegister() {
         String patientId = "patientId";
         String facilityId = "54";
         String facilityMotechId = "13161";
         MobileMidwifeEnrollmentForm form = createEnrollmentForm("12344", facilityId, "345", Medium.SMS, new Time(23, 45));
 
         when(mobileMidwifeService.findActiveBy(patientId)).thenReturn(null);
         when(mobileMidwifeValidator.validate(Matchers.<MobileMidwifeEnrollment>any(), eq(Collections.<FormBean>emptyList()), eq(Collections.<FormBean>emptyList()))).thenReturn(Collections.<FormError>emptyList());
         Facility mockFacility = mock(Facility.class);
         when(mockFacilityService.getFacility(facilityId)).thenReturn(mockFacility);
         when(mockFacilityService.getFacilityByMotechId(facilityMotechId)).thenReturn(mockFacility);
         when(mockFacility.getMotechId()).thenReturn(facilityMotechId);
 
         ModelMap modelMap = new ModelMap();
         String editUrl = controller.save(form, null, modelMap);
 
         assertThat(editUrl, isEq(MOBILE_MIDWIFE_URL));
         ArgumentCaptor<MobileMidwifeEnrollment> enrollment = ArgumentCaptor.forClass(MobileMidwifeEnrollment.class);
 
         verify(mobileMidwifeValidator).validate(enrollment.capture(), eq(Collections.<FormBean>emptyList()), eq(Collections.<FormBean>emptyList()));
         verify(mobileMidwifeService).register(enrollment.getValue());
         assertFormWithEnrollment((MobileMidwifeEnrollmentForm) modelMap.get("mobileMidwifeEnrollmentForm"), enrollment.getValue());
     }
 
     @Test
     public void shouldValidateAndUpdateExistingMobileMidwifeEnrollment() {
         String patientId = "patientId";
         String facilityId = "54";
         String facilityMotechId = "13161";
         String successMsg = "Changes successful";
 
         MobileMidwifeEnrollmentForm form = createEnrollmentForm(patientId, facilityId, "staffId", Medium.SMS, new Time(23, 45));
         MobileMidwifeEnrollment existingEnrollment = new MobileMidwifeEnrollment(DateTime.now());
         existingEnrollment.setPatientId(patientId);
         existingEnrollment.setFacilityId("oldFacilityId");
         existingEnrollment.setStaffId("oldStaffId");
         existingEnrollment.setMedium(Medium.VOICE);
         existingEnrollment.setTimeOfDay(new Time(23, 45));
 
         when(mobileMidwifeService.findActiveBy(patientId)).thenReturn(existingEnrollment);
         when(mobileMidwifeValidator.validate(Matchers.<MobileMidwifeEnrollment>any(), eq(Collections.<FormBean>emptyList()), eq(Collections.<FormBean>emptyList()))).thenReturn(Collections.<FormError>emptyList());
         Facility mockFacility = mock(Facility.class);
         when(mockFacilityService.getFacility(facilityId)).thenReturn(mockFacility);
         when(mockFacilityService.getFacilityByMotechId(facilityMotechId)).thenReturn(mockFacility);
         when(mockFacility.getMotechId()).thenReturn(facilityMotechId);
         when(messages.getMessage(SUCCESS_MESSAGE, null, Locale.getDefault())).thenReturn(successMsg);
 
         ModelMap modelMap = new ModelMap();
         String editUrl = controller.save(form, null, modelMap);
 
         ArgumentCaptor<MobileMidwifeEnrollment> enrollment = ArgumentCaptor.forClass(MobileMidwifeEnrollment.class);
         verify(mobileMidwifeValidator).validate(enrollment.capture(), eq(Collections.<FormBean>emptyList()), eq(Collections.<FormBean>emptyList()));
         verify(mobileMidwifeService).register(enrollment.getValue());
         assertFormWithEnrollment((MobileMidwifeEnrollmentForm) modelMap.get("mobileMidwifeEnrollmentForm"), enrollment.getValue());
 
         assertThat(editUrl, isEq(MOBILE_MIDWIFE_URL));
         assertThat((String) modelMap.get("successMessage"), isEq(successMsg));
     }
 
     @Test
     public void shouldUnregisterPatientFromMobileMidwifeEnrollment() {
         String patientId = "patientId";
         String staffId = "staffId";
         String facilityId = "facilityId";
         String facilityMotechId = "facilityMotechId";
 
         MobileMidwifeUnEnrollForm mobileMidwifeUnenrollForm = new MobileMidwifeUnEnrollForm();
         mobileMidwifeUnenrollForm.setPatientMotechId(patientId);
         FacilityForm facilityForm = new FacilityForm();
         facilityForm.setFacilityId(facilityId);
         mobileMidwifeUnenrollForm.setFacilityForm(facilityForm);
         mobileMidwifeUnenrollForm.setStaffMotechId(staffId);
 
         Facility facility = new Facility(new MRSFacility(facilityId));
         facility.motechId(facilityMotechId);
         when(mockFacilityService.getFacility(facilityId)).thenReturn(facility);
         when(mobileMidwifeValidator.validatePatient(patientId, Collections.<FormBean>emptyList(), Collections.<FormBean>emptyList())).thenReturn(Collections.<FormError>emptyList());
         when(mockFormValidator.validateIfStaffExists(staffId)).thenReturn(Collections.<FormError>emptyList());
         when(mockFormValidator.validateIfFacilityExists(facilityId)).thenReturn(Collections.<FormError>emptyList());
 
         ModelMap modelMap = new ModelMap();
 
         controller.unregister(mobileMidwifeUnenrollForm, modelMap);
 
         assertTrue(CollectionUtils.isEmpty((Collection) modelMap.get("formErrors")));
         verify(mobileMidwifeService).unRegister(patientId);
     }
 
     @Test
     public void shouldNotUnregisterPatientFromMobileMidwifeEnrollmentWhenFormErrorsOccur() {
         String patientId = "patientId";
         String staffId = "staffId";
         String facilityId = "facilityId";
         String facilityMotechId = "facilityMotechId";
 
         MobileMidwifeUnEnrollForm mobileMidwifeUnenrollForm = new MobileMidwifeUnEnrollForm();
         mobileMidwifeUnenrollForm.setPatientMotechId(patientId);
         FacilityForm facilityForm = new FacilityForm();
         facilityForm.setFacilityId(facilityId);
         mobileMidwifeUnenrollForm.setFacilityForm(facilityForm);
         mobileMidwifeUnenrollForm.setStaffMotechId(staffId);
 
         Facility facility = new Facility(new MRSFacility(facilityId));
         facility.motechId(facilityMotechId);
         when(mockFacilityService.getFacility(facilityId)).thenReturn(facility);
 
         final FormError patientFormError = new FormError("patient", "not valid");
         final FormError staffFormError = new FormError("staff", "not valid");
         final FormError facilityFormError = new FormError("facility", "not valid");
 
        MobileMidwifeEnrollment mobileMidwifeEnrollment = mock(MobileMidwifeEnrollment.class);
        when(mobileMidwifeService.findActiveBy(patientId)).thenReturn(mobileMidwifeEnrollment);
        when(mobileMidwifeValidator.validate(mobileMidwifeEnrollment,Collections.<FormBean>emptyList(),Collections.<FormBean>emptyList()))
                .thenReturn(Arrays.<FormError>asList(patientFormError,staffFormError,facilityFormError));
        
         ModelMap modelMap = new ModelMap();
 
         controller.unregister(mobileMidwifeUnenrollForm, modelMap);
 
         List<FormError> formErrors = (List<FormError>) modelMap.get("formErrors");
         assertThat(formErrors, hasItem(patientFormError));
         assertThat(formErrors, hasItem(staffFormError));
         assertThat(formErrors, hasItem(facilityFormError));
         verify(mobileMidwifeService, never()).unRegister(patientId);
     }
 
     @Test
     public void shouldShowValidationErrors() {
         ModelMap map = new ModelMap();
         String locationId = "54";
 
         MobileMidwifeEnrollmentForm enrollmentForm = createEnrollmentForm("patientId", locationId, "staffId", Medium.VOICE, new Time(23, 45));
         when(mobileMidwifeValidator.validate(Matchers.<MobileMidwifeEnrollment>any(), eq(Collections.<FormBean>emptyList()),eq(Collections.<FormBean>emptyList()))).thenReturn(new ArrayList<FormError>() {{
             add(new FormError("error1", "description1"));
             add(new FormError("error2", "description2"));
         }});
         Facility mockFacility = mock(Facility.class);
         when(mockFacilityService.getFacility(locationId)).thenReturn(mockFacility);
         when(mockFacility.getMotechId()).thenReturn("13161");
 
         String editUrl = controller.save(enrollmentForm, null, map);
 
         verify(mobileMidwifeValidator).validate(Matchers.<MobileMidwifeEnrollment>any(), eq(Collections.<FormBean>emptyList()), eq(Collections.<FormBean>emptyList()));
         verify(mobileMidwifeService, never()).register((MobileMidwifeEnrollment) any());
 
         List<FormError> errors = (List<FormError>) map.get("formErrors");
         assertThat("description1", is(equalTo(errors.get(0).getError())));
         assertThat("description2", is(equalTo(errors.get(1).getError())));
         assertThat(editUrl, isEq(MOBILE_MIDWIFE_URL));
         assertEquals(enrollmentForm, map.get("mobileMidwifeEnrollmentForm"));
     }
 
 
     private MobileMidwifeEnrollmentForm createEnrollmentForm(String patientId, String locationId, String staffId, Medium medium, Time timeOfDay) {
         FacilityForm facilityForm = new FacilityForm();
         facilityForm.setFacilityId(locationId);
         MobileMidwifeEnrollmentForm form = new MobileMidwifeEnrollmentForm()
                 .setMedium(medium)
                 .setPatientMotechId(patientId).setFacilityForm(facilityForm).setStaffMotechId(staffId)
                 .setTimeOfDay(timeOfDay).setConsent(true).setDayOfWeek(DayOfWeek.Monday).setLanguage(Language.EN)
                 .setLearnedFrom(LearnedFrom.POSTERS_ADS).setMedium(Medium.SMS)
                 .setMessageStartWeek("5").setPhoneNumber("9900011234")
                 .setPhoneOwnership(PhoneOwnership.PERSONAL).setReasonToJoin(ReasonToJoin.KNOW_MORE_PREGNANCY_CHILDBIRTH)
                 .setServiceType(ServiceType.PREGNANCY).setFacilityForm(facilityForm).setStatus("ACTIVE");
 
         return form;
     }
 
     private void assertFormWithEnrollment(MobileMidwifeEnrollmentForm form, MobileMidwifeEnrollment enrollment) {
         assertThat(form.getPatientMotechId(), isEq(enrollment.getPatientId()));
         assertThat(form.getStaffMotechId(), isEq(enrollment.getStaffId()));
         assertThat(form.getReasonToJoin(), isEq(enrollment.getReasonToJoin()));
         assertThat(form.getConsent(), isEq(enrollment.getConsent()));
         assertThat(form.getDayOfWeek(), isEq(enrollment.getDayOfWeek()));
         assertThat(form.getLearnedFrom(), isEq(enrollment.getLearnedFrom()));
         assertThat(form.getPhoneNumber(), isEq(enrollment.getPhoneNumber()));
         assertThat(form.getPhoneOwnership(), isEq(enrollment.getPhoneOwnership()));
         assertThat(form.getTimeOfDay(), isEq(enrollment.getTimeOfDay()));
     }
 }
