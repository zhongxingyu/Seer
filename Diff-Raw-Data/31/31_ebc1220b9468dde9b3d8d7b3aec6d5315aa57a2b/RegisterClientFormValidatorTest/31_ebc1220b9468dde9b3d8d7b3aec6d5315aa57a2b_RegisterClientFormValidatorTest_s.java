 package org.motechproject.ghana.national.validator;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.bean.RegisterClientForm;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.PatientType;
 import org.motechproject.ghana.national.domain.RegistrationType;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.mobileforms.api.domain.FormBean;
 import org.motechproject.mobileforms.api.domain.FormBeanGroup;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.openmrs.omod.validator.MotechIdVerhoeffValidator;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.util.*;
 
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.not;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.domain.Constants.NOT_FOUND;
 
 public class RegisterClientFormValidatorTest {
     private RegisterClientFormValidator registerClientFormValidator;
 
     @Mock
     private RegisterClientForm mockRegisterClientForm;
     @Mock
     private PatientService mockPatientService;
     @Mock
     private MotechIdVerhoeffValidator mockMotechIdVerhoeffValidator;
     @Mock
     private FormValidator formValidator;
 

     @Before
     public void setUp() {
         initMocks(this);
         registerClientFormValidator = new RegisterClientFormValidator();
         ReflectionTestUtils.setField(registerClientFormValidator, "formValidator", formValidator);
         ReflectionTestUtils.setField(registerClientFormValidator, "patientService", mockPatientService);
     }
 
     @Test
     public void shouldValidateIfAPatientIsAvailableWithIdAsMothersMotechIdIfMotherMotechIdIsGiven() {
         String mothersMotechId = "100";
         String motechId = "001";
         when(mockRegisterClientForm.getMotechId()).thenReturn(motechId);
         when(mockRegisterClientForm.getMotherMotechId()).thenReturn(mothersMotechId);
         when(mockRegisterClientForm.getRegistrantType()).thenReturn(PatientType.CHILD_UNDER_FIVE);
         when(mockRegisterClientForm.getDateOfBirth()).thenReturn(new Date(99, 9, 9));
         Patient patientsMotherMock = mock(Patient.class);
         when(mockPatientService.getPatientByMotechId(mothersMotechId)).thenReturn(patientsMotherMock);
         List<FormBean> formBeans = Arrays.<FormBean>asList(mockRegisterClientForm);
         assertThat(registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans), not(hasItem(new FormError("motherMotechId", NOT_FOUND))));
 
         Patient patient = new Patient(new MRSPatient(motechId, new MRSPerson().age(4), new MRSFacility("facilityId")));
         when(mockRegisterClientForm.getMotechId()).thenReturn(motechId);
         when(mockRegisterClientForm.getMotherMotechId()).thenReturn(null);
         when(formValidator.getPatient(motechId)).thenReturn(patient);
         assertThat(registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans), not(hasItem(new FormError("motherMotechId", NOT_FOUND))));
 
         when(mockRegisterClientForm.getMotherMotechId()).thenReturn(mothersMotechId);
         when(mockPatientService.getPatientByMotechId(mothersMotechId)).thenReturn(null);
         RegisterClientForm registerClientFormForMother = new RegisterClientForm();
         registerClientFormForMother.setMotechId(mothersMotechId);
         formBeans = Arrays.<FormBean>asList(mockRegisterClientForm, registerClientFormForMother);
         assertThat(registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans), not(hasItem(new FormError("motherMotechId", NOT_FOUND))));
     }
 
     @Test
     public void shouldNotReturnErrorIfPatientTypeIsNotChildAndMothersMotechIdIsNotPresent() {
         Patient patient = new Patient(new MRSPatient("motechId",new MRSPerson().gender("F"),new MRSFacility("facilityId")));
         when(mockRegisterClientForm.getMotherMotechId()).thenReturn(null);
         when(mockRegisterClientForm.getRegistrantType()).thenReturn(PatientType.PREGNANT_MOTHER);
         when(mockRegisterClientForm.getMotechId()).thenReturn("motechId");
         when(formValidator.getPatient(mockRegisterClientForm.getMotechId())).thenReturn(patient);
 
         List<FormBean> formBeans = Arrays.<FormBean>asList(mockRegisterClientForm);
         assertThat(registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans), not(hasItem(new FormError("motherMotechId", NOT_FOUND))));
         verify(mockPatientService, never()).getPatientByMotechId(Matchers.<String>any());
     }
 
     @Test
     public void shouldValidateTheMotechIdOfThePatientIfRegistrationModeIsPrePrintedId() {
        String motechId = "12345";
         RegisterClientForm registerClientForm = new RegisterClientForm();
         registerClientForm.setMotechId(motechId);
         registerClientForm.setRegistrationMode(RegistrationType.USE_PREPRINTED_ID);
         Patient patientMock = mock(Patient.class);
        when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patientMock);
 
         List<FormBean> formBeans = Arrays.<FormBean>asList(registerClientForm);
         assertThat(registerClientFormValidator.validate(registerClientForm, new FormBeanGroup(formBeans), formBeans), hasItem(new FormError("motechId", "in use")));
 
        when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(null);
        when(formValidator.getPatient(motechId)).thenReturn(patientMock);
         assertThat(registerClientFormValidator.validate(registerClientForm, new FormBeanGroup(formBeans), formBeans), not(hasItem(new FormError("motechId", "in use"))));
     }
 
     @Test
     public void shouldNotValidateMotechIdOfThePatientIfRegistrationModeIsNotPrePrintedId() {
         String motechId = "12345";
         when(mockRegisterClientForm.getRegistrationMode()).thenReturn(RegistrationType.AUTO_GENERATE_ID);
 
         when(mockRegisterClientForm.getMotechId()).thenReturn(motechId);
         List<FormBean> formBeans = Arrays.<FormBean>asList(mockRegisterClientForm);
         assertThat(registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans), not(hasItem(new FormError("motechId", NOT_FOUND))));
         verify(mockPatientService, never()).getPatientByMotechId(anyString());
     }
 
     @Test
     public void shouldVerifyIfStaffIdIsValidOrNot() {
         String staffId = "21";
         when(mockRegisterClientForm.getStaffId()).thenReturn(staffId);
 
         List<FormBean> formBeans = Arrays.<FormBean>asList(mockRegisterClientForm);
         registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans);
         verify(formValidator).validateIfStaffExists(eq(staffId));
     }
 
     @Test
     public void shouldVerifyIfFacilityIdIsValidOrNot() {
         String facilityId = "21";
         when(mockRegisterClientForm.getFacilityId()).thenReturn(facilityId);
 
 
         List<FormBean> formBeans = Arrays.<FormBean>asList(mockRegisterClientForm);
         registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans);
         verify(formValidator).validateIfFacilityExists(eq(facilityId));
     }
 
     @Test
     public void shouldReturnErrorIfChildAgeIsGreaterThanFive() {
         String motechId = "12234";
         when(mockRegisterClientForm.getMotechId()).thenReturn(motechId);
         when(mockRegisterClientForm.getRegistrantType()).thenReturn(PatientType.CHILD_UNDER_FIVE);
         when(mockRegisterClientForm.getDateOfBirth()).thenReturn(new Date(99, 9, 9));
         when(mockRegisterClientForm.getFormname()).thenReturn("registerPatient");
         when(formValidator.validateIfFacilityExists("212")).thenReturn(new ArrayList<FormError>());
         when(formValidator.validateIfStaffExists("212")).thenReturn(new ArrayList<FormError>());
 
         List<FormBean> formBeans = Arrays.<FormBean>asList(mockRegisterClientForm);
         List<FormError> formErrors = registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans);
 
         assertThat(formErrors, hasItem(new FormError(Constants.CHILD_AGE_PARAMETER, Constants.CHILD_AGE_MORE_ERR_MSG)));
     }
 
     @Test
     public void shouldReturnErrorIfMotherIsNotFemale() {
         String motherMotechId = "12234";
         when(mockRegisterClientForm.getMotherMotechId()).thenReturn(motherMotechId);
         when(mockRegisterClientForm.getRegistrantType()).thenReturn(PatientType.PREGNANT_MOTHER);
         when(mockRegisterClientForm.getSex()).thenReturn(Constants.PATIENT_GENDER_MALE);
         when(formValidator.validateIfFacilityExists("212")).thenReturn(new ArrayList<FormError>());
         when(formValidator.validateIfStaffExists("212")).thenReturn(new ArrayList<FormError>());
         when(mockRegisterClientForm.getFormname()).thenReturn("registerPatient");
 
         List<FormBean> formBeans = Arrays.<FormBean>asList(mockRegisterClientForm);
         List<FormError> formErrors = registerClientFormValidator.validate(mockRegisterClientForm, new FormBeanGroup(formBeans), formBeans);
         assertThat(formErrors, hasItem(new FormError("Sex", Constants.GENDER_ERROR_MSG)));
     }
 }
