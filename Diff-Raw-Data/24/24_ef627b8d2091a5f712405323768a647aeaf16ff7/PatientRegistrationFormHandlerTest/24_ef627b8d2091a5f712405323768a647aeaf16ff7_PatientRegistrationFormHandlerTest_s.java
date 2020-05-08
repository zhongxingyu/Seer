 package org.motechproject.ghana.national.handlers;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.bean.RegisterClientForm;
 import org.motechproject.ghana.national.domain.*;
 import org.motechproject.ghana.national.domain.mobilemidwife.*;
 import org.motechproject.ghana.national.exception.ParentNotFoundException;
 import org.motechproject.ghana.national.exception.PatientIdIncorrectFormatException;
 import org.motechproject.ghana.national.exception.PatientIdNotUniqueException;
 import org.motechproject.ghana.national.service.*;
 import org.motechproject.ghana.national.vo.ANCVO;
 import org.motechproject.ghana.national.vo.CwcVO;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.model.Time;
 import org.motechproject.mrs.model.Attribute;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 
 import static ch.lambdaj.Lambda.*;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class PatientRegistrationFormHandlerTest {
 
     @Mock
     PatientService mockPatientService;
     @Mock
     FacilityService mockFacilityService;
     @Mock
     CareService mockCareService;
     @Mock
     MobileMidwifeService mockMobileMidwifeService;
     @Mock
     TextMessageService mockTextMessageService;
 
     PatientRegistrationFormHandler patientRegistrationFormHandler;
 
     @Before
     public void setUp() {
         initMocks(this);
         patientRegistrationFormHandler = new PatientRegistrationFormHandler();
         ReflectionTestUtils.setField(patientRegistrationFormHandler, "patientService", mockPatientService);
         ReflectionTestUtils.setField(patientRegistrationFormHandler, "facilityService", mockFacilityService);
         ReflectionTestUtils.setField(patientRegistrationFormHandler, "careService", mockCareService);
         ReflectionTestUtils.setField(patientRegistrationFormHandler, "mobileMidwifeService", mockMobileMidwifeService);
         ReflectionTestUtils.setField(patientRegistrationFormHandler, "textMessageService", mockTextMessageService);
     }
 
     @Test
     public void shouldHandleRegisterClientEventAndInvokeService() throws ParentNotFoundException, PatientIdIncorrectFormatException, PatientIdNotUniqueException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
 
         String address = "Address";
         Date dateofBirth = new Date(10, 10, 2011);
         String district = "District";
         Boolean isBirthDateEstimated = true;
         String motechFacilityId = "MotechFacilityID";
         String facilityId = "Facility Id";
         String firstName = "FirstName";
         Boolean insured = true;
         String lastName = "LastName";
         String middleName = "MiddleName";
         String motechId = "motechId";
         Date nhisExpDate = new Date(10, 10, 2022);
         String nhisNumber = "NhisNumber";
         String parentId = "ParentId";
         String region = "Region";
         RegistrationType registrationMode = RegistrationType.USE_PREPRINTED_ID;
         String sex = "M";
         String subDistrict = "SubDistrict";
         String phoneNumber = "0123456789";
         final String staffId = "456";
         PatientType patientType = PatientType.CHILD_UNDER_FIVE;
 
         RegisterClientForm registerClientForm = createRegisterClientForm(address, dateofBirth, district, isBirthDateEstimated,
                 motechFacilityId, firstName, insured, lastName, middleName, motechId, nhisExpDate, nhisNumber, parentId, region, registrationMode, sex, subDistrict, phoneNumber, patientType, staffId);
         parameters.put(Constants.FORM_BEAN, registerClientForm);
         MotechEvent event = new MotechEvent("subject", parameters);
 
         ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
         ArgumentCaptor<String> staffIdCaptor = ArgumentCaptor.forClass(String.class);
         doReturn(motechId).when(mockPatientService).registerPatient(patientArgumentCaptor.capture(),staffIdCaptor.capture());
 
         Facility facility = mock(Facility.class);
         when(facility.mrsFacilityId()).thenReturn(facilityId);
         doReturn(facility).when(mockFacilityService).getFacilityByMotechId(motechFacilityId);
 
         patientRegistrationFormHandler.handleFormEvent(event);
 
         Patient savedPatient = patientArgumentCaptor.getValue();
         MRSPerson mrsPerson = savedPatient.getMrsPatient().getPerson();
 
         assertRegisterPatient(address, dateofBirth, isBirthDateEstimated, facilityId, firstName, insured, lastName, middleName, motechId, nhisExpDate, nhisNumber, sex, phoneNumber, savedPatient, mrsPerson, parentId);
         verify(mockTextMessageService).sendSMS(registerClientForm.getSender(), motechId, savedPatient, "REGISTER_SUCCESS_SMS");
     }
 
     @Test
     public void shouldBeRegisteredAsAListenerForRegisterPatientEvent() throws NoSuchMethodException {
         String[] registeredEventSubject = patientRegistrationFormHandler.getClass().getMethod("handleFormEvent", new Class[]{MotechEvent.class}).getAnnotation(MotechListener.class).subjects();
         assertThat(registeredEventSubject, is(equalTo(new String[]{"form.validation.successful.NurseDataEntry.registerPatient"})));
     }
 
     @Test
     public void shouldRunAsAdminUser() throws NoSuchMethodException {
         assertThat(patientRegistrationFormHandler.getClass().getMethod("handleFormEvent", new Class[]{MotechEvent.class}).getAnnotation(LoginAsAdmin.class), is(not(equalTo(null))));
     }
 
     @Test
     public void shouldNotRegisterForMobileMidwifeIfConsentIsNotGiven(){
         String facilityId = "facility id";
         Facility facility = mock(Facility.class);
         doReturn(facility).when(mockFacilityService).getFacilityByMotechId(Matchers.<String>any());
         when(facility.mrsFacilityId()).thenReturn(facilityId);
 
 
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         RegisterClientForm registerClientForm = new RegisterClientForm();
         registerClientForm.setEnroll(Boolean.FALSE);
 
         parameters.put(Constants.FORM_BEAN, registerClientForm);
         MotechEvent event = new MotechEvent("subject", parameters);
         patientRegistrationFormHandler.handleFormEvent(event);
         verify(mockMobileMidwifeService, never()).register(Matchers.<MobileMidwifeEnrollment>any());
     }
 
     @Test
     public void shouldRegisterForCWCAndMobileMidwife() throws ParentNotFoundException, PatientIdIncorrectFormatException, PatientIdNotUniqueException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
 
         String address = "Address";
         Date dateofBirth = new Date(10, 10, 2011);
         String district = "District";
         Boolean isBirthDateEstimated = true;
         String motechFacilityId = "MotechFacilityID";
         String facilityId = "Facility Id";
         String firstName = "FirstName";
         Boolean insured = true;
         String lastName = "LastName";
         String middleName = "MiddleName";
         String motechId = "motechId";
         Date nhisExpDate = new Date(10, 10, 2022);
         String nhisNumber = "NhisNumber";
         String parentId = "ParentId";
         String region = "Region";
         RegistrationType registrationMode = RegistrationType.USE_PREPRINTED_ID;
         String sex = "M";
         String subDistrict = "SubDistrict";
        String phoneNumber = "0123456789";
         PatientType patientType = PatientType.CHILD_UNDER_FIVE;
 
         final Date registartionDate = new Date(2011, 9, 1);
         final Date lastBCGDate = new Date(2011, 10, 1);
         final Date lastVitADate = new Date(2011, 11, 1);
         final Date lastMeaslesDate = new Date(2011, 9, 2);
         final Date lastYfDate = new Date(2011, 9, 3);
         final Date lastPentaDate = new Date(2011, 9, 4);
         final Date lastOPVDate = new Date(2011, 9, 5);
         final Date lastIPTiDate = new Date(2011, 9, 6);
         final String staffId = "456";
         final int lastPenta = 1;
         final int lastOPV = 0;
         final int lastIPTi = 1;
 
         ServiceType serviceType = ServiceType.PREGNANCY;
         ReasonToJoin reasonToJoin = ReasonToJoin.KNOW_MORE_PREGNANCY_CHILDBIRTH;
         Medium medium = Medium.SMS;
         DayOfWeek dayOfWeek = DayOfWeek.Monday;
         Time timeOfDay = new Time(10, 30);
         Language language = Language.EN;
         LearnedFrom learnedFrom = LearnedFrom.FRIEND;
         String mmRegPhone = "0123456789";
         PhoneOwnership phoneOwnership = PhoneOwnership.HOUSEHOLD;
         Boolean consent = Boolean.TRUE;
         boolean enroll = true;
 
         RegisterClientForm registerClientForm = createRegisterClientFormForCWCEnrollment(address, dateofBirth, district, isBirthDateEstimated,
                 motechFacilityId, firstName, insured, lastName, middleName, motechId, nhisExpDate, nhisNumber, parentId,
                 region, registrationMode, sex, subDistrict, phoneNumber, patientType, registartionDate,
                 lastBCGDate, lastVitADate, lastMeaslesDate, lastYfDate, lastPentaDate, lastOPVDate, lastIPTiDate, staffId, lastPenta, lastOPV, lastIPTi);
 
         addMobileMidwifeRegistrationDetails(registerClientForm, serviceType, reasonToJoin, medium, dayOfWeek, timeOfDay, language, learnedFrom, mmRegPhone, phoneOwnership, consent, enroll);
 
         parameters.put(Constants.FORM_BEAN, registerClientForm);
         MotechEvent event = new MotechEvent("subject", parameters);
 
         Facility facility = mock(Facility.class);
         doReturn(facility).when(mockFacilityService).getFacilityByMotechId(motechFacilityId);
         when(facility.mrsFacilityId()).thenReturn(facilityId);
 
 
         when(mockPatientService.registerPatient(any(Patient.class),any(String.class))).thenReturn(motechId);
         when(mockCareService.enroll(any(CwcVO.class))).thenReturn(null);
         patientRegistrationFormHandler.handleFormEvent(event);
         ArgumentCaptor<MobileMidwifeEnrollment> mobileMidwifeEnrollmentArgumentCaptor = ArgumentCaptor.forClass(MobileMidwifeEnrollment.class);
         final ArgumentCaptor<CwcVO> cwcVOArgumentCaptor = ArgumentCaptor.forClass(CwcVO.class);
         verify(mockCareService).enroll(cwcVOArgumentCaptor.capture());
         verify(mockMobileMidwifeService).register(mobileMidwifeEnrollmentArgumentCaptor.capture());
         final CwcVO cwcVO = cwcVOArgumentCaptor.getValue();
         final MobileMidwifeEnrollment mobileMidwifeEnrollment = mobileMidwifeEnrollmentArgumentCaptor.getValue();
 
         assertCWCRegistration(motechFacilityId, facilityId, motechId, registartionDate, lastBCGDate, lastVitADate, lastMeaslesDate, lastYfDate, lastPentaDate, lastOPVDate, lastIPTiDate, staffId, lastPenta, lastOPV, cwcVO, mobileMidwifeEnrollment);
        assertMobileMidwifeRegistration(mobileMidwifeEnrollment, staffId, motechFacilityId, motechId, serviceType, reasonToJoin, medium, dayOfWeek, timeOfDay, language, learnedFrom, phoneNumber, phoneOwnership, consent);
     }
 
     @Test
     public void shouldRegisterForANCAndMobileMidwife() throws ParentNotFoundException, PatientIdIncorrectFormatException, PatientIdNotUniqueException {
 
         HashMap<String, Object> parameters = new HashMap<String, Object>();
 
         String address = "Address";
         Date dateofBirth = new Date(10, 10, 2011);
         String district = "District";
         Boolean isBirthDateEstimated = true;
         String motechFacilityId = "MotechFacilityID";
         String facilityId = "Facility Id";
         String firstName = "FirstName";
         Boolean insured = true;
         String lastName = "LastName";
         String middleName = "MiddleName";
         String motechId = "motechId";
         Date nhisExpDate = new Date(10, 10, 2022);
         String nhisNumber = "NhisNumber";
         String parentId = "ParentId";
         String region = "Region";
         RegistrationType registrationMode = RegistrationType.USE_PREPRINTED_ID;
         String sex = "M";
         String subDistrict = "SubDistrict";
        String phoneNumber = "0123456789";
         PatientType patientType = PatientType.PREGNANT_MOTHER;
         final String staffId = "456";
 
 
         Date expDeliveryDate = new Date(10, 12, 2012);
         Boolean deliveryDateConfirmed = true;
         Double height = 5.5;
         Integer gravida = 3;
         Integer parity = 1;
         Date lastIPTDate = new Date(10, 12, 2000);
         Date lastTTDate = new Date(10, 12, 1999);
         String lastIPT = "lastIPT";
         String lastTT = "lastTT";
 
         ServiceType serviceType = ServiceType.PREGNANCY;
         ReasonToJoin reasonToJoin = ReasonToJoin.KNOW_MORE_PREGNANCY_CHILDBIRTH;
         Medium medium = Medium.SMS;
         DayOfWeek dayOfWeek = DayOfWeek.Monday;
         Time timeOfDay = new Time(10, 30);
         Language language = Language.EN;
         LearnedFrom learnedFrom = LearnedFrom.FRIEND;
         String mmRegPhone = "0123456789";
         PhoneOwnership phoneOwnership = PhoneOwnership.HOUSEHOLD;
         Boolean consent = Boolean.TRUE;
         boolean enroll = true;
 
         RegisterClientForm registerClientForm = createRegisterClientFormForANCEnrollment(address, dateofBirth, district, isBirthDateEstimated,
                 motechFacilityId, firstName, insured, lastName, middleName, null, nhisExpDate, nhisNumber, parentId,
                 region, registrationMode, sex, subDistrict, phoneNumber, patientType, expDeliveryDate, deliveryDateConfirmed, height, gravida, parity, lastIPTDate, lastTTDate, lastIPT, lastTT, staffId);
 
 
         addMobileMidwifeRegistrationDetails(registerClientForm, serviceType, reasonToJoin, medium, dayOfWeek, timeOfDay, language, learnedFrom, mmRegPhone, phoneOwnership, consent, enroll);
 
         parameters.put(Constants.FORM_BEAN, registerClientForm);
         MotechEvent event = new MotechEvent("subject", parameters);
 
         Facility facility = mock(Facility.class);
         doReturn(facility).when(mockFacilityService).getFacilityByMotechId(motechFacilityId);
         when(facility.mrsFacilityId()).thenReturn(facilityId);
 
         when(mockPatientService.registerPatient(any(Patient.class),any(String.class))).thenReturn(motechId);
 
         when(mockCareService.enroll(any(ANCVO.class))).thenReturn(null);
         patientRegistrationFormHandler.handleFormEvent(event);
         ArgumentCaptor<MobileMidwifeEnrollment> mobileMidwifeEnrollmentArgumentCaptor = ArgumentCaptor.forClass(MobileMidwifeEnrollment.class);
         final ArgumentCaptor<ANCVO> ancvoArgumentCaptor = ArgumentCaptor.forClass(ANCVO.class);
         verify(mockCareService).enroll(ancvoArgumentCaptor.capture());
         verify(mockMobileMidwifeService).register(mobileMidwifeEnrollmentArgumentCaptor.capture());
 
         final ANCVO ancVO = ancvoArgumentCaptor.getValue();
         final MobileMidwifeEnrollment mobileMidwifeEnrollment = mobileMidwifeEnrollmentArgumentCaptor.getValue();
 
         assertANCRegistration(facilityId, motechId, expDeliveryDate, deliveryDateConfirmed, height, gravida, parity, lastIPTDate, lastTTDate, lastIPT, lastTT, staffId, ancVO);
        assertMobileMidwifeRegistration(mobileMidwifeEnrollment, staffId, motechFacilityId, motechId, serviceType, reasonToJoin, medium, dayOfWeek, timeOfDay, language, learnedFrom, phoneNumber, phoneOwnership, consent);
     }
 
     private RegisterClientForm createRegisterClientFormForCWCEnrollment(String address, Date dateofBirth, String district, Boolean birthDateEstimated, String motechFacilityId, String firstName, Boolean insured, String lastName, String middleName, String motechId, Date nhisExpDate, String nhisNumber, String parentId, String region, RegistrationType registrationMode, String sex, String subDistrict, String phoneNumber, PatientType patientType, Date registartionDate, Date lastBCGDate, Date lastVitADate, Date lastMeaslesDate, Date lastYfDate, Date lastPentaDate, Date lastOPVDate, Date lastIPTiDate, String staffId, int lastPenta, int lastOPV, int lastIPTi) {
         RegisterClientForm registerClientForm = createRegisterClientForm(address, dateofBirth, district, birthDateEstimated,
                 motechFacilityId, firstName, insured, lastName, middleName, motechId, nhisExpDate, nhisNumber, parentId, region, registrationMode, sex, subDistrict, phoneNumber, patientType, staffId);
         registerClientForm.setDate(registartionDate);
         registerClientForm.setBcgDate(lastBCGDate);
         registerClientForm.setLastVitaminADate(lastVitADate);
         registerClientForm.setMeaslesDate(lastMeaslesDate);
         registerClientForm.setYellowFeverDate(lastYfDate);
         registerClientForm.setLastPentaDate(lastPentaDate);
         registerClientForm.setLastPenta(lastPenta);
         registerClientForm.setLastOPVDate(lastOPVDate);
         registerClientForm.setLastOPV(lastOPV);
         registerClientForm.setLastIPTiDate(lastIPTiDate);
         registerClientForm.setLastIPTi(lastIPTi);
         return registerClientForm;
     }
 
 
     private RegisterClientForm createRegisterClientFormForANCEnrollment(String address, Date dateofBirth, String district, Boolean birthDateEstimated, String motechFacilityId, String firstName, Boolean insured, String lastName, String middleName, String motechId, Date nhisExpDate, String nhisNumber, String parentId, String region, RegistrationType registrationMode, String sex, String subDistrict, String phoneNumber, PatientType patientType, Date expDeliveryDate, Boolean deliveryDateConfirmed, Double height, Integer gravida, Integer parity, Date lastIPTDate, Date lastTTDate, String lastIPT, String lastTT, String staffId) {
         RegisterClientForm registerClientForm = createRegisterClientForm(address, dateofBirth, district, birthDateEstimated,
                 motechFacilityId, firstName, insured, lastName, middleName, motechId, nhisExpDate, nhisNumber, parentId, region, registrationMode, sex, subDistrict, phoneNumber, patientType, staffId);
 
         registerClientForm.setExpDeliveryDate(expDeliveryDate);
         registerClientForm.setDeliveryDateConfirmed(deliveryDateConfirmed);
         registerClientForm.setHeight(height);
         registerClientForm.setGravida(gravida);
         registerClientForm.setParity(parity);
         registerClientForm.setLastIPTDate(lastIPTDate);
         registerClientForm.setLastTTDate(lastTTDate);
         registerClientForm.setLastIPT(lastIPT);
         registerClientForm.setLastTT(lastTT);
 
         return registerClientForm;
     }
 
     private RegisterClientForm createRegisterClientForm(String address, Date dateofBirth, String district, Boolean birthDateEstimated, String motechFacilityId, String firstName, Boolean insured,
                                                         String lastName, String middleName, String motechId, Date nhisExpDate, String nhisNumber, String parentId, String region, RegistrationType registrationMode, String sex, String subDistrict, String phoneNumber, PatientType patientType, String staffId) {
         RegisterClientForm registerClientForm = new RegisterClientForm();
         registerClientForm.setSender("0987654321");
         registerClientForm.setStaffId(staffId);
         registerClientForm.setAddress(address);
         registerClientForm.setDateOfBirth(dateofBirth);
         registerClientForm.setDistrict(district);
         registerClientForm.setEstimatedBirthDate(birthDateEstimated);
         registerClientForm.setFacilityId(motechFacilityId);
         registerClientForm.setFirstName(firstName);
         registerClientForm.setInsured(insured);
         registerClientForm.setLastName(lastName);
         registerClientForm.setMiddleName(middleName);
         registerClientForm.setMotechId(motechId);
         registerClientForm.setNhisExpires(nhisExpDate);
         registerClientForm.setNhis(nhisNumber);
         registerClientForm.setMotherMotechId(parentId);
         registerClientForm.setRegion(region);
         registerClientForm.setRegistrationMode(registrationMode);
         registerClientForm.setSex(sex);
         registerClientForm.setSubDistrict(subDistrict);
         registerClientForm.setRegistrantType(patientType);
        registerClientForm.setMmRegPhone(phoneNumber);
         return registerClientForm;
     }
 
 
     private void assertRegisterPatient(String address, Date dateofBirth, Boolean birthDateEstimated, String facilityId, String firstName, Boolean insured, String lastName,
                                        String middleName, String motechId, Date nhisExpDate, String nhisNumber, String sex, String phoneNumber, Patient savedPatient, MRSPerson mrsPerson, String parentId) {
         assertThat(mrsPerson.getAddress(), is(equalTo(address)));
         assertThat(savedPatient.getParentId(), is(equalTo(parentId)));
         assertThat(mrsPerson.getDateOfBirth(), is(equalTo(dateofBirth)));
         assertThat(savedPatient.getMrsPatient().getFacility().getId(), is(equalTo(facilityId)));
         assertThat(savedPatient.getMrsPatient().getFacility().getCountyDistrict(), is(equalTo(null)));
         assertThat(savedPatient.getMrsPatient().getFacility().getRegion(), is(equalTo(null)));
         assertThat(savedPatient.getMrsPatient().getFacility().getStateProvince(), is(equalTo(null)));
         assertThat(mrsPerson.getBirthDateEstimated(), is(equalTo(birthDateEstimated)));
         assertThat(mrsPerson.getFirstName(), is(equalTo(firstName)));
         assertThat(mrsPerson.getLastName(), is(equalTo(lastName)));
         assertThat(mrsPerson.getMiddleName(), is(equalTo(middleName)));
         assertThat(savedPatient.getMrsPatient().getMotechId(), is(equalTo(motechId)));
         assertThat(mrsPerson.getGender(), is(equalTo(sex)));
         assertThat(((Attribute) selectUnique(mrsPerson.getAttributes(), having(on(Attribute.class).name(),
                 equalTo(PatientAttributes.NHIS_EXPIRY_DATE.getAttribute())))).value(),
                 is(equalTo(new SimpleDateFormat(Constants.PATTERN_YYYY_MM_DD).format(nhisExpDate))));
         assertThat(((Attribute) selectUnique(mrsPerson.getAttributes(), having(on(Attribute.class).name(),
                 equalTo(PatientAttributes.NHIS_NUMBER.getAttribute())))).value(), is(equalTo(nhisNumber)));
         assertThat(((Attribute) selectUnique(mrsPerson.getAttributes(), having(on(Attribute.class).name(),
                 equalTo(PatientAttributes.INSURED.getAttribute())))).value(), is(equalTo(insured.toString())));
         assertThat(((Attribute) selectUnique(mrsPerson.getAttributes(), having(on(Attribute.class).name(),
                 equalTo(PatientAttributes.PHONE_NUMBER.getAttribute())))).value(), is(equalTo(phoneNumber)));
     }
 
 
     private void assertCWCRegistration(String motechFacilityId, String facilityId, String motechId, Date registartionDate, Date lastBCGDate, Date lastVitADate, Date lastMeaslesDate, Date lastYfDate, Date lastPentaDate, Date lastOPVDate, Date lastIPTiDate, String staffId, int lastPenta, int lastOPV, CwcVO cwcVO, MobileMidwifeEnrollment mobileMidwifeEnrollment) {
         assertThat(facilityId, is(cwcVO.getFacilityId()));
         assertThat(registartionDate, is(cwcVO.getRegistrationDate()));
         assertThat(motechId, is(cwcVO.getPatientMotechId()));
         assertThat(lastBCGDate, is(cwcVO.getCWCCareHistoryVO().getBcgDate()));
         assertThat(lastVitADate, is(cwcVO.getCWCCareHistoryVO().getVitADate()));
         assertThat(lastMeaslesDate, is(cwcVO.getCWCCareHistoryVO().getMeaslesDate()));
         assertThat(lastYfDate, is(cwcVO.getCWCCareHistoryVO().getYfDate()));
         assertThat(lastPentaDate, is(cwcVO.getCWCCareHistoryVO().getLastPentaDate()));
         assertThat(lastPenta, is(cwcVO.getCWCCareHistoryVO().getLastPenta()));
         assertThat(lastOPVDate, is(cwcVO.getCWCCareHistoryVO().getLastOPVDate()));
         assertThat(lastOPV, is(cwcVO.getCWCCareHistoryVO().getLastOPV()));
         assertThat(lastIPTiDate, is(cwcVO.getCWCCareHistoryVO().getLastIPTiDate()));
         assertThat(staffId, is(cwcVO.getStaffId()));
         assertThat(staffId, is(mobileMidwifeEnrollment.getStaffId()));
         assertThat(motechFacilityId, is(mobileMidwifeEnrollment.getFacilityId()));
         assertThat(motechId, is(mobileMidwifeEnrollment.getPatientId()));
     }
 
     private void assertANCRegistration(String facilityId, String motechId, Date expDeliveryDate, Boolean deliveryDateConfirmed, Double height, Integer gravida, Integer parity, Date lastIPTDate, Date lastTTDate, String lastIPT, String lastTT, String staffId, ANCVO ancVO) {
         assertThat(ancVO.getFacilityId(), is(facilityId));
         assertThat(ancVO.getPatientMotechId(), is(motechId));
         assertThat(ancVO.getEstimatedDateOfDelivery(), is(expDeliveryDate));
         assertThat(ancVO.getDeliveryDateConfirmed(), is(deliveryDateConfirmed));
         assertThat(ancVO.getHeight(), is(height));
         assertThat(ancVO.getGravida(), is(gravida));
         assertThat(ancVO.getParity(), is(parity));
         assertThat(ancVO.getAncCareHistoryVO().getLastIPTDate(), is(lastIPTDate));
         assertThat(ancVO.getAncCareHistoryVO().getLastTTDate(), is(lastTTDate));
         assertThat(ancVO.getAncCareHistoryVO().getLastIPT(), is(lastIPT));
         assertThat(ancVO.getAncCareHistoryVO().getLastTT(), is(lastTT));
     }
 
     private void addMobileMidwifeRegistrationDetails(RegisterClientForm registerClientForm, ServiceType serviceType, ReasonToJoin reasonToJoin, Medium medium, DayOfWeek dayOfWeek, Time timeOfDay, Language language, LearnedFrom learnedFrom, String phoneNumber, PhoneOwnership phoneOwnership, Boolean consent, boolean enroll){
         registerClientForm.setEnroll(enroll);
         registerClientForm.setServiceType(serviceType);
         registerClientForm.setReasonToJoin(reasonToJoin);
         registerClientForm.setFormat(medium.getValue());
         registerClientForm.setDayOfWeek(dayOfWeek);
         registerClientForm.setTimeOfDay(timeOfDay);
         registerClientForm.setLanguage(language);
         registerClientForm.setLearnedFrom(learnedFrom);
        registerClientForm.setPhoneNumber(phoneNumber);
         registerClientForm.setPhoneOwnership(phoneOwnership);
         registerClientForm.setConsent(consent);
     }
 
     private void assertMobileMidwifeRegistration(MobileMidwifeEnrollment mobileMidwifeEnrollment, String staffId, String motechFacilityId, String motechId, ServiceType serviceType, ReasonToJoin reasonToJoin, Medium medium, DayOfWeek dayOfWeek, Time timeOfDay, Language language, LearnedFrom learnedFrom, String phoneNumber, PhoneOwnership phoneOwnership, Boolean consent){
         assertThat(mobileMidwifeEnrollment.getStaffId(), is(staffId));
         assertThat(mobileMidwifeEnrollment.getFacilityId(), is(motechFacilityId));
         assertThat(mobileMidwifeEnrollment.getPatientId(), is(motechId));
         assertThat(mobileMidwifeEnrollment.getServiceType(), is(serviceType));
         assertThat(mobileMidwifeEnrollment.getReasonToJoin(), is(reasonToJoin));
         assertThat(mobileMidwifeEnrollment.getMedium(), is(medium));
         assertThat(mobileMidwifeEnrollment.getDayOfWeek(), is(dayOfWeek));
         assertThat(mobileMidwifeEnrollment.getTimeOfDay(), is(timeOfDay));
         assertThat(mobileMidwifeEnrollment.getLanguage(), is(language));
         assertThat(mobileMidwifeEnrollment.getLearnedFrom(), is(learnedFrom));
         assertThat(mobileMidwifeEnrollment.getPhoneNumber(), is(phoneNumber));
         assertThat(mobileMidwifeEnrollment.getPhoneOwnership(), is(phoneOwnership));
         assertThat(mobileMidwifeEnrollment.getConsent(), is(consent));
     }
 
 }
