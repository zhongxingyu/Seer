 /**
  * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
  *
  * Copyright (c) 2010-11 The Trustees of Columbia University in the City of
  * New York and Grameen Foundation USA.  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * 3. Neither the name of Grameen Foundation USA, Columbia University, or
  * their respective contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
  * AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
  * USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.motechproject.server.svc.impl;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.motechproject.server.messaging.MessageDefDate;
 import org.motechproject.server.messaging.MessageNotFoundException;
 import org.motechproject.server.model.*;
 import org.motechproject.server.model.MessageStatus;
 import org.motechproject.server.model.ghana.Community;
 import org.motechproject.server.model.ghana.Facility;
 import org.motechproject.server.omod.*;
 import org.motechproject.server.omod.builder.PatientBuilder;
 import org.motechproject.server.omod.impl.MessageProgramServiceImpl;
 import org.motechproject.server.omod.web.model.WebStaff;
 import org.motechproject.server.service.ConceptEnum;
 import org.motechproject.server.service.ContextService;
 import org.motechproject.server.service.MotechService;
 import org.motechproject.server.svc.BirthOutcomeChild;
 import org.motechproject.server.svc.OpenmrsBean;
 import org.motechproject.server.svc.RCTService;
 import org.motechproject.server.svc.RegistrarBean;
 import org.motechproject.server.util.DateUtil;
 import org.motechproject.server.util.GenderTypeConverter;
 import org.motechproject.server.util.MotechConstants;
 import org.motechproject.server.util.Password;
 import org.motechproject.server.ws.ObservationBean;
 import org.motechproject.server.ws.PregnancyObservation;
 import org.motechproject.server.ws.WebServicePatientModelConverterImpl;
 import org.motechproject.ws.*;
 import org.motechproject.ws.mobile.MessageService;
 import org.openmrs.*;
 import org.openmrs.Patient;
 import org.openmrs.api.*;
 import org.openmrs.scheduler.SchedulerService;
 import org.openmrs.scheduler.TaskDefinition;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.lang.reflect.InvocationTargetException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import static org.motechproject.server.omod.PatientIdentifierTypeEnum.PATIENT_IDENTIFIER_MOTECH_ID;
 
 /**
  * An implementation of the RegistrarBean interface, implemented using a mix of
  * OpenMRS and module defined services.
  */
 public class RegistrarBeanImpl implements RegistrarBean, OpenmrsBean {
 
     private static Log log = LogFactory.getLog(RegistrarBeanImpl.class);
 
     private ContextService contextService;
     private MessageService mobileService;
     private PatientService patientService;
     private PersonService personService;
     private RelationshipService relationshipService;
     private UserService userService;
     private AuthenticationService authenticationService;
     private ConceptService conceptService;
     private LocationService locationService;
     private ObsService obsService;
     private EncounterService encounterService;
     private SchedulerService schedulerService;
     private AdministrationService administrationService;
     private RCTService rctService;
     private MessageProgramServiceImpl messageProgramService;
 
 
     @Autowired
     private IdentifierGenerator identifierGenerator;
 
 
     @Autowired
     private MotechUserRepository motechUserRepository;
 
     @Autowired
     @Qualifier("pregnancyObservation")
     private PregnancyObservation pregnancyObservation;
 
     @Autowired
     private ObservationBean observationBean;
     private static final String SINGLE_SPACE = " ";
 
 
     public void setContextService(ContextService contextService) {
         this.contextService = contextService;
     }
 
     public void setMobileService(MessageService mobileService) {
         this.mobileService = mobileService;
     }
 
     public User registerStaff(String firstName, String lastName, String phone,
                               String staffType, String staffId) {
         User staff;
         if (staffId != null) {
             staff = motechUserRepository.updateUser(getStaffBySystemId(staffId), new WebStaff(firstName, lastName, phone, staffType));
             return userService.saveUser(staff,null);
         } else {
             staff = motechUserRepository.newUser(new WebStaff(firstName, lastName, phone, staffType));
             return userService.saveUser(staff, generatePassword(8));
         }
     }
 
 
     private String generatePassword(int length) {
         return new Password(length).create();
     }
 
     @Transactional
     public Patient registerPatient(RegistrationMode registrationMode,
                                    Integer motechId, RegistrantType registrantType, String firstName,
                                    String middleName, String lastName, String preferredName,
                                    Date dateOfBirth, Boolean estimatedBirthDate, Gender sex,
                                    Boolean insured, String nhis, Date nhisExpires, Patient mother,
                                    Community community, Facility facility, String address, String phoneNumber,
                                    Date expDeliveryDate, Boolean deliveryDateConfirmed,
                                    Boolean enroll, Boolean consent, ContactNumberType ownership,
                                    MediaType format, String language, DayOfWeek dayOfWeek,
                                    Date timeOfDay, InterestReason reason, HowLearned howLearned,
                                    Integer messagesStartWeek) {
 
         User staff = authenticationService.getAuthenticatedUser();
         Date date = new Date();
 
         return registerPatient(staff, facility, date, registrationMode,
                 motechId, registrantType, firstName, middleName, lastName,
                 preferredName, dateOfBirth, estimatedBirthDate, sex, insured,
                 nhis, nhisExpires, mother, community, address, phoneNumber,
                 expDeliveryDate, deliveryDateConfirmed, enroll, consent,
                 ownership, format, language, dayOfWeek, timeOfDay, reason,
                 howLearned, messagesStartWeek);
     }
 
     @Transactional
     public Patient registerPatient(User staff, Facility facility, Date date,
                                    RegistrationMode registrationMode, Integer motechId,
                                    RegistrantType registrantType, String firstName, String middleName,
                                    String lastName, String preferredName, Date dateOfBirth,
                                    Boolean estimatedBirthDate, Gender sex, Boolean insured,
                                    String nhis, Date nhisExpires, Patient mother, Community community,
                                    String address, String phoneNumber, Date expDeliveryDate,
                                    Boolean deliveryDateConfirmed, Boolean enroll, Boolean consent,
                                    ContactNumberType ownership, MediaType format, String language,
                                    DayOfWeek dayOfWeek, Date timeOfDay, InterestReason reason,
                                    HowLearned howLearned, Integer messagesStartWeek) {
 
         Patient patient = createPatient(staff, motechId, firstName, middleName,
                 lastName, preferredName, dateOfBirth, estimatedBirthDate, sex,
                 insured, nhis, nhisExpires, address, phoneNumber, ownership,
                 format, language, dayOfWeek, timeOfDay, howLearned, reason, registrantType, mother);
 
         patient = patientService.savePatient(patient);
 
         if (isChildWithMother(registrantType, mother)) {
             childRegistrationPostProcessing(staff, patient, mother, facility, community, enroll, consent, messagesStartWeek, date);
         } else if (registrantType == RegistrantType.PREGNANT_MOTHER) {
             motherRegistrationPostProcessing(staff, patient, facility, community, enroll, consent, messagesStartWeek, date, expDeliveryDate, deliveryDateConfirmed);
         } else {
             patientRegistrationPostProcessing(staff, patient, facility, community, enroll, consent, messagesStartWeek, null, date);
         }
 
         return patient;
     }
 
     private boolean isChildWithMother(RegistrantType registrantType, Patient mother) {
         return registrantType == RegistrantType.CHILD_UNDER_FIVE && mother != null;
     }
 
     private void childRegistrationPostProcessing(User staff, Patient child, Patient mother, Facility facility, Community community,
                                                  Boolean enroll, Boolean consent, Integer messagesStartWeek,
                                                  Date date) {
         if (community == null) {
             community = getCommunityByPatient(mother);
         }
         if (enroll == null && consent == null) {
             List<MessageProgramEnrollment> enrollments = motechService().getActiveMessageProgramEnrollments(mother.getPatientId(), null, null, null, null);
             if (enrollments != null && !enrollments.isEmpty()) {
                 enroll = true;
                 consent = true;
             }
         }
         if (mother != null) {
             relationshipService.createMotherChildRelationship(mother, child);
         }
         patientRegistrationPostProcessing(staff, child, facility, community, enroll, consent, messagesStartWeek, null, date);
     }
 
     private void motherRegistrationPostProcessing(User staff, Patient mother, Facility facility, Community community,
                                                   Boolean enroll, Boolean consent, Integer messagesStartWeek, Date date, Date expDeliveryDate,
                                                   Boolean deliveryDateConfirmed) {
 
         Integer pregnancyDueDateObsId = registerPregnancy(staff, facility.getLocation(), date, mother, expDeliveryDate, deliveryDateConfirmed);
         patientRegistrationPostProcessing(staff, mother, facility, community, enroll, consent, messagesStartWeek, pregnancyDueDateObsId, date);
     }
 
     private void patientRegistrationPostProcessing(User staff, Patient patient, Facility facility, Community community,
                                                    Boolean enroll, Boolean consent, Integer messagesStartWeek, Integer pregnancyDueDateObsId,
                                                    Date date) {
         if (community != null) {
             community.getResidents().add(patient);
         }
         facility.addPatient(patient);
         enrollPatient(patient, enroll, consent, messagesStartWeek, pregnancyDueDateObsId);
         recordPatientRegistration(staff, facility.getLocation(), date, patient);
     }
 
     private void enrollPatientWithAttributes(Patient patient,
                                              Boolean enroll, Boolean consent,
                                              ContactNumberType ownership, String phoneNumber, MediaType format,
                                              String language, DayOfWeek dayOfWeek, Date timeOfDay,
                                              InterestReason reason, HowLearned howLearned,
                                              Integer messagesStartWeek, Integer pregnancyDueDateObsId) {
 
         setPatientAttributes(patient, phoneNumber, ownership, format, language,
                 dayOfWeek, timeOfDay, howLearned, reason, null, null, null);
 
         patientService.savePatient(patient);
 
         enrollPatient(patient, enroll, consent, messagesStartWeek,
                 pregnancyDueDateObsId);
     }
 
     private void enrollPatient(Patient patient,
                                Boolean enroll, Boolean consent, Integer messagesStartWeek,
                                Integer pregnancyDueDateObsId) {
 
         boolean enrollPatient = Boolean.TRUE.equals(enroll)
                 && Boolean.TRUE.equals(consent);
 
         Integer referenceDateObsId = null;
         String infoMessageProgramName = null;
 
         if (pregnancyDueDateObsId != null) {
             infoMessageProgramName = "Weekly Pregnancy Message Program";
 
             referenceDateObsId = pregnancyDueDateObsId;
 
         } else if (messagesStartWeek != null) {
             infoMessageProgramName = "Weekly Info Pregnancy Message Program";
 
             if (enrollPatient) {
                 referenceDateObsId = storeMessagesWeekObs(patient,
                         messagesStartWeek);
             }
 
         } else if (patient.getAge() != null && patient.getAge() < 5) {
             infoMessageProgramName = "Weekly Info Child Message Program";
 
             // TODO: If mother specified, Remove mother's pregnancy message
             // enrollment
         }
 
         if (enrollPatient) {
             if (infoMessageProgramName != null) {
                 addMessageProgramEnrollment(patient.getPatientId(),
                         infoMessageProgramName, referenceDateObsId);
             }
             addMessageProgramEnrollment(patient.getPatientId(), "Expected Care Message Program", null);
         }
     }
 
     private Integer storeMessagesWeekObs(Patient patient,
                                          Integer messagesStartWeek) {
         Location ghanaLocation = getGhanaLocation();
         Date currentDate = new Date();
 
         Calendar calendar = Calendar.getInstance();
         // Convert weeks to days, plus one day
         calendar.add(Calendar.DATE, (messagesStartWeek * -7) + 1);
         Date referenceDate = calendar.getTime();
 
         Obs refDateObs = createDateValueObs(currentDate,
                 concept(ConceptEnum.CONCEPT_ENROLLMENT_REFERENCE_DATE), patient, ghanaLocation,
                 referenceDate, null, null);
 
         refDateObs = obsService.saveObs(refDateObs, null);
         return refDateObs.getObsId();
     }
 
     @Transactional
     public void demoRegisterPatient(RegistrationMode registrationMode,
                                     Integer motechId, String firstName, String middleName,
                                     String lastName, String preferredName, Date dateOfBirth,
                                     Boolean estimatedBirthDate, Gender sex, Boolean insured,
                                     String nhis, Date nhisExpires, Community community, String address,
                                     String phoneNumber, Boolean enroll, Boolean consent,
                                     ContactNumberType ownership, MediaType format, String language,
                                     DayOfWeek dayOfWeek, Date timeOfDay, InterestReason reason,
                                     HowLearned howLearned) {
 
         User staff = contextService.getAuthenticatedUser();
 
         Patient patient = createPatient(staff, motechId, firstName, middleName,
                 lastName, preferredName, dateOfBirth, estimatedBirthDate, sex,
                 insured, nhis, nhisExpires, address, phoneNumber, ownership,
                 format, language, dayOfWeek, timeOfDay, howLearned, reason, RegistrantType.OTHER, null);
 
         patient = patientService.savePatient(patient);
 
         if (Boolean.TRUE.equals(enroll) && Boolean.TRUE.equals(consent)) {
             addMessageProgramEnrollment(patient.getPatientId(),
                     "Demo Minute Message Program", null);
         }
     }
 
     @Transactional
     public void demoEnrollPatient(Patient patient) {
         addMessageProgramEnrollment(patient.getPersonId(),
                 "Input Demo Message Program", null);
     }
 
     @Transactional
     private Patient createPatient(User staff, Integer motechId,
                                   String firstName, String middleName, String lastName,
                                   String prefName, Date birthDate, Boolean birthDateEst, Gender sex,
                                   Boolean insured, String nhis, Date nhisExpDate, String address,
                                   String phoneNumber, ContactNumberType phoneType,
                                   MediaType mediaType, String language, DayOfWeek dayOfWeek,
                                   Date timeOfDay, HowLearned howLearned, InterestReason interestReason, RegistrantType registrantType, Patient mother) {
 
         PatientBuilder patientBuilder = new PatientBuilder(personService, motechService(), identifierGenerator, registrantType, patientService, locationService);
         SimpleDateFormat timeFormatter = new SimpleDateFormat(MotechConstants.TIME_FORMAT_DELIVERY_TIME);
         SimpleDateFormat dateFormatter = new SimpleDateFormat(MotechConstants.DATE_FORMAT);
 
         patientBuilder.setMotechId(motechId).setName(firstName, middleName, lastName)
                 .setPreferredName(prefName).setGender(sex).setBirthDate(birthDate)
                 .setBirthDateEstimated(birthDateEst).setAddress1(address).setParent(mother);
 
         try {
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_PHONE_NUMBER, phoneNumber, "toString");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_PHONE_TYPE, phoneType, "name");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_MEDIA_TYPE, mediaType, "name");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_LANGUAGE, language, "toString");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_DELIVERY_DAY, dayOfWeek, "name");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_DELIVERY_TIME, timeOfDay, timeFormatter, "format");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_HOW_LEARNED, howLearned, "name");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_INTEREST_REASON, interestReason, "name");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_INSURED, insured, "toString");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_NHIS_NUMBER, nhis, "toString");
             patientBuilder.addAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_NHIS_EXP_DATE, nhisExpDate, dateFormatter, "format");
         } catch (NoSuchMethodException e) {
             log.error("Value Method not found on the invocation target");
         } catch (InvocationTargetException e) {
             log.error("Invalid invocation target set for the value method");
         } catch (IllegalAccessException e) {
             log.error("Value method is not accessible on the invocation target");
         }
 
         return patientBuilder.build();
     }
 
     private void recordPatientRegistration(User staff, Location facility,
                                            Date date, Patient patient) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PATIENTREGVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         encounterService.saveEncounter(encounter);
     }
 
     private void setPatientAttributes(Patient patient, String phoneNumber,
                                       ContactNumberType phoneType, MediaType mediaType, String language,
                                       DayOfWeek dayOfWeek, Date timeOfDay, HowLearned howLearned,
                                       InterestReason interestReason, Boolean insured, String nhis,
                                       Date nhisExpDate) {
 
         List<PersonAttribute> attrs = new ArrayList<PersonAttribute>();
 
         if (phoneNumber != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_PHONE_NUMBER.getAttributeType(personService),
                     phoneNumber));
         }
 
         if (phoneType != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_PHONE_TYPE.getAttributeType(personService),
                     phoneType.name()));
         }
 
         if (mediaType != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_MEDIA_TYPE.getAttributeType(personService),
                     mediaType.name()));
         }
 
         if (language != null) {
             attrs
                     .add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_LANGUAGE.getAttributeType(personService),
                             language));
         }
 
         if (dayOfWeek != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_DELIVERY_DAY.getAttributeType(personService),
                     dayOfWeek.name()));
         }
 
         if (timeOfDay != null) {
             SimpleDateFormat formatter = new SimpleDateFormat(
                     MotechConstants.TIME_FORMAT_DELIVERY_TIME);
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_DELIVERY_TIME.getAttributeType(personService),
                     formatter.format(timeOfDay)));
         }
 
         if (howLearned != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_HOW_LEARNED.getAttributeType(personService),
                     howLearned.name()));
         }
 
         if (interestReason != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_INTEREST_REASON.getAttributeType(personService),
                     interestReason.name()));
         }
 
         if (insured != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_INSURED.getAttributeType(personService), insured
                     .toString()));
         }
 
         if (nhis != null) {
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_NHIS_NUMBER.getAttributeType(personService), nhis));
         }
 
         if (nhisExpDate != null) {
             SimpleDateFormat formatter = new SimpleDateFormat(
                     MotechConstants.DATE_FORMAT);
             attrs.add(new PersonAttribute(PersonAttributeTypeEnum.PERSON_ATTRIBUTE_NHIS_EXP_DATE.getAttributeType(personService),
                     formatter.format(nhisExpDate)));
         }
 
         for (PersonAttribute attr : attrs)
             patient.addAttribute(attr);
     }
 
     @Transactional
     public void editPatient(User staff, Date date, Patient patient, Patient mother,
                             String phoneNumber, ContactNumberType phoneOwnership, String nhis,
                             Date nhisExpires, Date expectedDeliveryDate, Boolean stopEnrollment) {
 
         if (expectedDeliveryDate != null) {
             Obs pregnancy = getActivePregnancy(patient.getPatientId());
             Obs dueDateObs = getActivePregnancyDueDateObs(patient
                     .getPatientId(), pregnancy);
             if (dueDateObs != null) {
                 if (!expectedDeliveryDate.equals(dueDateObs.getValueDatetime())) {
                     updatePregnancyDueDateObs(pregnancy,
                             dueDateObs, expectedDeliveryDate, dueDateObs
                                     .getEncounter());
                 }
             }
         }
 
         setPatientAttributes(patient, phoneNumber, phoneOwnership, null, null,
                 null, null, null, null, null, nhis, nhisExpires);
 
         patientService.savePatient(patient);
 
         relationshipService.saveOrUpdateMotherRelationship(mother, patient, false);
 
         if (Boolean.TRUE.equals(stopEnrollment)) {
             stopEnrollmentFor(patient.getPatientId());
         }
     }
 
     @Transactional
     public void stopEnrollmentFor(Integer patientId) {
         removeAllMessageProgramEnrollments(patientId);
     }
 
     @Transactional
     public void editPatient(Patient patient, String firstName,
                             String middleName, String lastName, String preferredName,
                             Date dateOfBirth, Boolean estimatedBirthDate, Gender sex,
                             Boolean insured, String nhis, Date nhisExpires, Patient mother,
                             Community community, String addressLine1, String phoneNumber,
                             Date expDeliveryDate, Boolean enroll, Boolean consent,
                             ContactNumberType ownership, MediaType format, String language,
                             DayOfWeek dayOfWeek, Date timeOfDay, Facility facility) {
 
 
         patient.setBirthdate(dateOfBirth);
         patient.setBirthdateEstimated(estimatedBirthDate);
         patient.setGender(GenderTypeConverter.toOpenMRSString(sex));
 
         relationshipService.saveOrUpdateMotherRelationship(mother, patient, true);
 
         PatientEditor editor = new PatientEditor(patient);
         Facility currentFacility = getFacilityByPatient(patient);
 
         if (!currentFacility.equals(facility)) {
             patient = editor.removeFrom(currentFacility).addTo(facility).done();
         }
 
         Community currentCommunity = getCommunityByPatient(patient);
 
         boolean bothCommunitiesExistAndAreSame = community != null && currentCommunity != null && currentCommunity.equals(community);
 
         if (!bothCommunitiesExistAndAreSame) {
             editor.removeFrom(currentCommunity).addTo(community);
         }
 
         setPatientAttributes(patient, phoneNumber, ownership, format, language,
                 dayOfWeek, timeOfDay, null, null, insured, nhis, nhisExpires);
 
         editor.editName(new PersonName(firstName, middleName, lastName));
         editor.editPreferredName(new PersonName(preferredName, middleName, lastName));
 
         editor.editAddress(new PatientAddress().near(facility).in(community).at(addressLine1).build());
 
         patientService.savePatient(patient);
 
         Integer dueDateObsId = null;
         if (expDeliveryDate != null) {
             Obs pregnancy = getActivePregnancy(patient.getPatientId());
             Obs dueDateObs = getActivePregnancyDueDateObs(patient
                     .getPatientId(), pregnancy);
             if (dueDateObs != null) {
                 dueDateObsId = dueDateObs.getObsId();
                 if (!expDeliveryDate.equals(dueDateObs.getValueDatetime())) {
                     dueDateObsId = updatePregnancyDueDateObs(pregnancy,
                             dueDateObs, expDeliveryDate, dueDateObs
                                     .getEncounter());
                 }
             }
         }
 
         if (Boolean.FALSE.equals(enroll)) {
             removeAllMessageProgramEnrollments(patient.getPatientId());
         } else {
             enrollPatient(patient, enroll, consent, null,
                     dueDateObsId);
         }
     }
 
     @Transactional
     public void registerPregnancy(Patient patient, Date expDeliveryDate,
                                   Boolean deliveryDateConfirmed, Boolean enroll, Boolean consent,
                                   String phoneNumber, ContactNumberType ownership, MediaType format,
                                   String language, DayOfWeek dayOfWeek, Date timeOfDay,
                                   InterestReason reason, HowLearned howLearned) {
 
         Integer pregnancyDueDateObsId = checkExistingPregnancy(patient);
 
         Location facility = getGhanaLocation();
         User staff = authenticationService.getAuthenticatedUser();
         Date date = new Date();
 
         if (pregnancyDueDateObsId == null) {
             pregnancyDueDateObsId = registerPregnancy(staff, facility, date,
                     patient, expDeliveryDate, deliveryDateConfirmed);
         }
 
         enrollPatientWithAttributes(patient, enroll, consent, ownership,
                 phoneNumber, format, language, dayOfWeek, timeOfDay, reason,
                 howLearned, null, pregnancyDueDateObsId);
     }
 
     private Integer registerPregnancy(User staff, Location facility, Date date,
                                       Patient patient, Date dueDate, Boolean dueDateConfirmed) {
 
         Encounter encounter = new Encounter();
         encounter
                 .setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PREGREGVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
         encounter = encounterService.saveEncounter(encounter);
 
         Obs pregnancyObs = createObs(date, concept(ConceptEnum.CONCEPT_PREGNANCY), patient,
                 facility, encounter, null);
 
         Obs pregnancyStatusObs = createBooleanValueObs(date,
                 concept(ConceptEnum.CONCEPT_PREGNANCY_STATUS), patient, facility, Boolean.TRUE,
                 encounter, null);
         pregnancyObs.addGroupMember(pregnancyStatusObs);
 
         Obs dueDateObs = null;
         if (dueDate != null) {
             dueDateObs = createDateValueObs(date, concept(ConceptEnum.CONCEPT_ESTIMATED_DATE_OF_CONFINEMENT),
                     patient, facility, dueDate, encounter, null);
             pregnancyObs.addGroupMember(dueDateObs);
         }
 
         if (dueDateConfirmed != null) {
             Obs dueDateConfirmedObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_DATE_OF_CONFINEMENT_CONFIRMED), patient, facility,
                     dueDateConfirmed, encounter, null);
             pregnancyObs.addGroupMember(dueDateConfirmedObs);
         }
         obsService.saveObs(pregnancyObs, null);
 
         if (dueDateObs != null) {
             return dueDateObs.getObsId();
         }
         return null;
     }
 
     @Transactional
     public void registerPregnancy(User staff, Location facility, Date date,
                                   Patient patient, Date estDeliveryDate, Boolean enroll,
                                   Boolean consent, ContactNumberType ownership, String phoneNumber,
                                   MediaType format, String language, DayOfWeek dayOfWeek,
                                   Date timeOfDay, HowLearned howLearned) {
 
         Integer pregnancyDueDateObsId = checkExistingPregnancy(patient);
 
         if (pregnancyDueDateObsId == null) {
             pregnancyDueDateObsId = registerPregnancy(staff, facility, date,
                     patient, estDeliveryDate, null);
         }
 
         enrollPatientWithAttributes(patient, enroll, consent, ownership,
                 phoneNumber, format, language, dayOfWeek, timeOfDay, null,
                 howLearned, null, pregnancyDueDateObsId);
     }
 
     private Integer checkExistingPregnancy(Patient patient) {
         Obs pregnancyObs = getActivePregnancy(patient.getPatientId());
 
         Integer pregnancyDueDateObsId = null;
         if (pregnancyObs != null) {
             log.warn("Entering Pregnancy for patient with active pregnancy, "
                     + "patient id=" + patient.getPatientId());
 
             Obs pregnancyDueDateObs = getActivePregnancyDueDateObs(patient
                     .getPatientId(), pregnancyObs);
             if (pregnancyDueDateObs != null) {
                 pregnancyDueDateObsId = pregnancyDueDateObs.getObsId();
             } else {
                 log.warn("No due date found for active pregnancy, patient id="
                         + patient.getPatientId());
             }
         }
         return pregnancyDueDateObsId;
     }
 
     public void recordPatientHistory(User staff, Location facility, Date date,
                                      Patient patient, Integer lastIPT, Date lastIPTDate, Integer lastTT,
                                      Date lastTTDate, Date bcgDate, Integer lastOPV, Date lastOPVDate,
                                      Integer lastPenta, Date lastPentaDate, Date measlesDate,
                                      Date yellowFeverDate, Integer lastIPTI, Date lastIPTIDate,
                                      Date lastVitaminADate, Integer whyNoHistory) {
 
         // Not associating historical data with any facility
         Location ghanaLocation = getGhanaLocation();
 
         Encounter historyEncounter = new Encounter();
         historyEncounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PATIENTHISTORY));
         historyEncounter.setEncounterDatetime(date);
         historyEncounter.setPatient(patient);
         historyEncounter.setLocation(ghanaLocation);
         historyEncounter.setProvider(staff);
 
         if (lastIPT != null && lastIPTDate != null) {
             Obs iptDoseObs = createNumericValueObs(lastIPTDate,
                     concept(ConceptEnum.CONCEPT_INTERMITTENT_PREVENTATIVE_TREATMENT_DOSE), patient, ghanaLocation, lastIPT,
                     historyEncounter, null);
             historyEncounter.addObs(iptDoseObs);
         }
         if (lastTT != null && lastTTDate != null) {
             Obs ttDoseObs = createNumericValueObs(lastTTDate,
                     concept(ConceptEnum.CONCEPT_TETANUS_TOXOID_DOSE), patient, ghanaLocation, lastTT,
                     historyEncounter, null);
             historyEncounter.addObs(ttDoseObs);
         }
         if (bcgDate != null) {
             Obs bcgObs = createConceptValueObs(bcgDate,
                     concept(ConceptEnum.CONCEPT_IMMUNIZATIONS_ORDERED), patient, ghanaLocation,
                     concept(ConceptEnum.CONCEPT_BCG_VACCINATION), historyEncounter, null);
             historyEncounter.addObs(bcgObs);
         }
         if (lastOPV != null && lastOPVDate != null) {
             Obs opvDoseObs = createNumericValueObs(lastOPVDate,
                     concept(ConceptEnum.CONCEPT_ORAL_POLIO_VACCINATION_DOSE),
                     patient, ghanaLocation, lastOPV,
                     historyEncounter, null);
             historyEncounter.addObs(opvDoseObs);
         }
         if (lastPenta != null && lastPentaDate != null) {
             Obs pentaDoseObs = createNumericValueObs(lastPentaDate,
                     concept(ConceptEnum.CONCEPT_PENTA_VACCINATION_DOSE), patient, ghanaLocation, lastPenta,
                     historyEncounter, null);
             historyEncounter.addObs(pentaDoseObs);
         }
         if (measlesDate != null) {
             Obs measlesObs = createConceptValueObs(measlesDate,
                     concept(ConceptEnum.CONCEPT_IMMUNIZATIONS_ORDERED), patient, ghanaLocation,
                     concept(ConceptEnum.CONCEPT_MEASLES_VACCINATION), historyEncounter, null);
             historyEncounter.addObs(measlesObs);
         }
         if (yellowFeverDate != null) {
             Obs yellowFeverObs = createConceptValueObs(yellowFeverDate,
                     concept(ConceptEnum.CONCEPT_IMMUNIZATIONS_ORDERED), patient, ghanaLocation,
                     concept(ConceptEnum.CONCEPT_YELLOW_FEVER_VACCINATION), historyEncounter, null);
             historyEncounter.addObs(yellowFeverObs);
         }
         if (lastIPTI != null && lastIPTIDate != null) {
             Obs iptiObs = createNumericValueObs(lastIPTIDate,
                     concept(ConceptEnum.CONCEPT_INTERMITTENT_PREVENTATIVE_TREATMENT_INFANTS_DOSE),
                     patient, ghanaLocation, lastIPTI,
                     historyEncounter, null);
             historyEncounter.addObs(iptiObs);
         }
         if (lastVitaminADate != null) {
             Obs vitaminAObs = createConceptValueObs(lastVitaminADate,
                     concept(ConceptEnum.CONCEPT_IMMUNIZATIONS_ORDERED), patient, ghanaLocation,
                     concept(ConceptEnum.CONCEPT_VITAMIN_A), historyEncounter, null);
             historyEncounter.addObs(vitaminAObs);
         }
         if (whyNoHistory != null) {
             Obs whyNoHistoryObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_WHY_NO_HISTORY), patient, ghanaLocation, whyNoHistory,
                     historyEncounter, null);
             historyEncounter.addObs(whyNoHistoryObs);
         }
         if (!historyEncounter.getAllObs().isEmpty()) {
             encounterService.saveEncounter(historyEncounter);
         }
     }
 
     @Transactional
     public void registerANCMother(User staff, Location facility, Date date,
                                   Patient patient, String ancRegNumber, Date estDeliveryDate,
                                   Double height, Integer gravida, Integer parity, Boolean enroll,
                                   Boolean consent, ContactNumberType ownership, String phoneNumber,
                                   MediaType format, String language, DayOfWeek dayOfWeek,
                                   Date timeOfDay, HowLearned howLearned) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_ANCREGVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         if (ancRegNumber != null) {
             Obs ancRegNumObs = createTextValueObs(date,
                     concept(ConceptEnum.CONCEPT_ANC_REG_NUMBER), patient, facility,
                     ancRegNumber, encounter, null);
             encounter.addObs(ancRegNumObs);
         }
         if (gravida != null) {
             Obs gravidaObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_GRAVIDA),
                     patient, facility, gravida, encounter, null);
             encounter.addObs(gravidaObs);
         }
 
         if (parity != null) {
             Obs parityObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_PARITY),
                     patient, facility, parity, encounter, null);
             encounter.addObs(parityObs);
         }
 
         if (height != null) {
             Obs heightObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_HEIGHT),
                     patient, facility, height, encounter, null);
             encounter.addObs(heightObs);
         }
         encounterService.saveEncounter(encounter);
 
         Integer pregnancyDueDateObsId = null;
         Obs pregnancyObs = getActivePregnancy(patient.getPatientId());
         if (pregnancyObs == null) {
             pregnancyDueDateObsId = registerPregnancy(staff, facility, date,
                     patient, estDeliveryDate, null);
         } else {
             Obs pregnancyDueDateObs = getActivePregnancyDueDateObs(patient
                     .getPatientId(), pregnancyObs);
             if (pregnancyDueDateObs != null) {
                 pregnancyDueDateObsId = pregnancyDueDateObs.getObsId();
                 if (estDeliveryDate != null) {
                     pregnancyDueDateObsId = updatePregnancyDueDateObs(
                             pregnancyObs, pregnancyDueDateObs, estDeliveryDate,
                             encounter);
                 }
             } else if (estDeliveryDate != null) {
                 log.warn("Cannot update pregnancy due date, "
                         + "no active pregnancy due date found, patient id="
                         + patient.getPatientId());
             }
         }
 
         enrollPatientWithAttributes(patient, enroll, consent, ownership,
                 phoneNumber, format, language, dayOfWeek, timeOfDay, null,
                 howLearned, null, pregnancyDueDateObsId);
     }
 
     @Transactional
     public void registerCWCChild(User staff, Location facility, Date date,
                                  Patient patient, String cwcRegNumber, Boolean enroll,
                                  Boolean consent, ContactNumberType ownership, String phoneNumber,
                                  MediaType format, String language, DayOfWeek dayOfWeek,
                                  Date timeOfDay, HowLearned howLearned) {
 
         enrollPatientWithAttributes(patient, enroll, consent, ownership,
                 phoneNumber, format, language, dayOfWeek, timeOfDay, null,
                 howLearned, null, null);
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_CWCREGVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         if (cwcRegNumber != null) {
             Obs cwcRegNumObs = createTextValueObs(date,
                     concept(ConceptEnum.CONCEPT_CWC_REG_NUMBER), patient, facility,
                     cwcRegNumber, encounter, null);
             encounter.addObs(cwcRegNumObs);
         }
         encounterService.saveEncounter(encounter);
     }
 
     @Transactional
     public void recordMotherANCVisit(User staff, Location facility, Date date,
                                      Patient patient, String serialNumber, Integer visitNumber, Integer ancLocation,
                                      String house, String community, Date estDeliveryDate,
                                      Integer bpSystolic, Integer bpDiastolic, Double weight,
                                      Integer ttDose, Integer iptDose, Boolean iptReactive,
                                      Boolean itnUse, Double fht, Integer fhr, Integer urineTestProtein,
                                      Integer urineTestGlucose, Double hemoglobin, Boolean vdrlReactive,
                                      Boolean vdrlTreatment, Boolean dewormer, Boolean maleInvolved,
                                      Boolean pmtct, Boolean preTestCounseled, HIVResult hivTestResult,
                                      Boolean postTestCounseled, Boolean pmtctTreatment,
                                      Boolean referred, Date nextANCDate, String comments) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_ANCVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         Obs pregnancyObs = getActivePregnancy(patient.getPatientId());
         if (pregnancyObs == null) {
             log.warn("Entered ANC visit for patient without active pregnancy, "
                     + "patient id=" + patient.getPatientId());
         }
 
         addSerialNumberObservation(facility, date, patient, serialNumber, encounter);
 
         if (visitNumber != null) {
             Obs visitNumberObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_VISIT_NUMBER), patient, facility, visitNumber,
                     encounter, null);
             encounter.addObs(visitNumberObs);
         }
         if (ancLocation != null) {
             Obs ancLocationObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_ANC_PNC_LOCATION), patient, facility, ancLocation,
                     encounter, null);
             encounter.addObs(ancLocationObs);
         }
         if (house != null) {
             Obs houseObs = createTextValueObs(date, concept(ConceptEnum.CONCEPT_HOUSE),
                     patient, facility, house, encounter, null);
             encounter.addObs(houseObs);
         }
         if (community != null) {
             Obs communityObs = createTextValueObs(date,
                     concept(ConceptEnum.CONCEPT_COMMUNITY), patient, facility, community,
                     encounter, null);
             encounter.addObs(communityObs);
         }
         if (bpSystolic != null) {
             Obs bpSystolicObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_SYSTOLIC_BLOOD_PRESSURE), patient, facility,
                     bpSystolic, encounter, null);
             encounter.addObs(bpSystolicObs);
         }
         if (bpDiastolic != null) {
             Obs bpDiastolicObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_DIASTOLIC_BLOOD_PRESSURE), patient, facility,
                     bpDiastolic, encounter, null);
             encounter.addObs(bpDiastolicObs);
         }
         if (weight != null) {
             Obs weightObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_WEIGHT),
                     patient, facility, weight, encounter, null);
             encounter.addObs(weightObs);
         }
         if (ttDose != null) {
             Obs ttDoseObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_TETANUS_TOXOID_DOSE), patient, facility, ttDose,
                     encounter, null);
             encounter.addObs(ttDoseObs);
         }
         if (iptDose != null) {
             Obs iptDoseObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_INTERMITTENT_PREVENTATIVE_TREATMENT_DOSE),
                     patient, facility, iptDose, encounter, null);
             encounter.addObs(iptDoseObs);
         }
         if (iptReactive != null) {
             Concept iptReactionValueConcept = null;
             if (Boolean.TRUE.equals(iptReactive)) {
                 iptReactionValueConcept = concept(ConceptEnum.CONCEPT_REACTIVE);
             } else {
                 iptReactionValueConcept = concept(ConceptEnum.CONCEPT_NON_REACTIVE);
             }
             Obs iptReactiveObs = createConceptValueObs(date,
                     concept(ConceptEnum.CONCEPT_IPT_REACTION), patient, facility,
                     iptReactionValueConcept, encounter, null);
             encounter.addObs(iptReactiveObs);
         }
         if (itnUse != null) {
             Obs itnUseObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_INSECTICIDE_TREATED_NET_USAGE),
                     patient, facility, itnUse, encounter, null);
             encounter.addObs(itnUseObs);
         }
         if (fht != null) {
             Obs fhtObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_FUNDAL_HEIGHT),
                     patient, facility, fht, encounter, null);
             encounter.addObs(fhtObs);
         }
         if (fhr != null) {
             Obs fhrObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_FETAL_HEART_RATE), patient, facility, fhr,
                     encounter, null);
             encounter.addObs(fhrObs);
         }
         if (urineTestProtein != null) {
             Concept urineProteinTestValueConcept = null;
             switch (urineTestProtein) {
                 case 0:
                     urineProteinTestValueConcept = concept(ConceptEnum.CONCEPT_NEGATIVE);
                     break;
                 case 1:
                     urineProteinTestValueConcept = concept(ConceptEnum.CONCEPT_POSITIVE);
                     break;
                 case 2:
                     urineProteinTestValueConcept = concept(ConceptEnum.CONCEPT_TRACE);
                     break;
             }
             if (urineProteinTestValueConcept != null) {
                 Obs urineTestProteinPositiveObs = createConceptValueObs(date,
                         concept(ConceptEnum.CONCEPT_URINE_PROTEIN_TEST), patient, facility,
                         urineProteinTestValueConcept, encounter, null);
                 encounter.addObs(urineTestProteinPositiveObs);
             }
         }
         if (urineTestGlucose != null) {
             Concept urineGlucoseTestValueConcept = null;
             switch (urineTestGlucose) {
                 case 0:
                     urineGlucoseTestValueConcept = concept(ConceptEnum.CONCEPT_NEGATIVE);
                     break;
                 case 1:
                     urineGlucoseTestValueConcept = concept(ConceptEnum.CONCEPT_POSITIVE);
                     break;
                 case 2:
                     urineGlucoseTestValueConcept = concept(ConceptEnum.CONCEPT_TRACE);
                     break;
             }
             if (urineGlucoseTestValueConcept != null) {
                 Obs urineTestProteinPositiveObs = createConceptValueObs(date,
                         concept(ConceptEnum.CONCEPT_URINE_GLUCOSE_TEST), patient, facility,
                         urineGlucoseTestValueConcept, encounter, null);
                 encounter.addObs(urineTestProteinPositiveObs);
             }
         }
         if (hemoglobin != null) {
             Obs hemoglobinObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_HEMOGLOBIN), patient, facility, hemoglobin,
                     encounter, null);
             encounter.addObs(hemoglobinObs);
         }
         if (vdrlReactive != null) {
             Concept vdrlValueConcept;
             if (Boolean.TRUE.equals(vdrlReactive)) {
                 vdrlValueConcept = concept(ConceptEnum.CONCEPT_REACTIVE);
             } else {
                 vdrlValueConcept = concept(ConceptEnum.CONCEPT_NON_REACTIVE);
             }
             Obs vdrlReactiveObs = createConceptValueObs(date, concept(ConceptEnum.CONCEPT_VDRL),
                     patient, facility, vdrlValueConcept, encounter, null);
             encounter.addObs(vdrlReactiveObs);
         }
         if (vdrlTreatment != null) {
             Obs vdrlTreatmentObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_VDRL_TREATMENT), patient, facility,
                     vdrlTreatment, encounter, null);
             encounter.addObs(vdrlTreatmentObs);
         }
         if (dewormer != null) {
             Obs dewormerObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_DEWORMER),
                     patient, facility, dewormer, encounter, null);
             encounter.addObs(dewormerObs);
         }
         if (maleInvolved != null) {
             Obs maleInvolvedObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_MALE_INVOLVEMENT), patient, facility,
                     maleInvolved, encounter, null);
             encounter.addObs(maleInvolvedObs);
         }
         if (pmtct != null) {
             Obs pmtctObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_PMTCT),
                     patient, facility, pmtct, encounter, null);
             encounter.addObs(pmtctObs);
         }
         if (preTestCounseled != null) {
             Obs preTestCounseledObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_HIV_PRE_TEST_COUNSELING), patient, facility,
                     preTestCounseled, encounter, null);
             encounter.addObs(preTestCounseledObs);
         }
         if (hivTestResult != null) {
             Obs hivResultObs = createTextValueObs(date,
                     concept(ConceptEnum.CONCEPT_HIV_TEST_RESULT), patient, facility, hivTestResult
                             .name(), encounter, null);
             encounter.addObs(hivResultObs);
         }
         if (postTestCounseled != null) {
             Obs postTestCounseledObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_HIV_POST_TEST_COUNSELING), patient, facility,
                     postTestCounseled, encounter, null);
             encounter.addObs(postTestCounseledObs);
         }
         if (pmtctTreatment != null) {
             Obs pmtctTreatmentObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_PMTCT_TREATMENT), patient, facility,
                     pmtctTreatment, encounter, null);
             encounter.addObs(pmtctTreatmentObs);
         }
         if (referred != null) {
             Obs referredObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_REFERRED),
                     patient, facility, referred, encounter, null);
             encounter.addObs(referredObs);
         }
         if (nextANCDate != null) {
             Obs nextANCDateObs = createDateValueObs(date,
                     concept(ConceptEnum.CONCEPT_NEXT_ANC_DATE), patient, facility, nextANCDate,
                     encounter, null);
             encounter.addObs(nextANCDateObs);
         }
         if (comments != null) {
             Obs commentsObs = createTextValueObs(date, concept(ConceptEnum.CONCEPT_COMMENTS),
                     patient, facility, comments, encounter, null);
             encounter.addObs(commentsObs);
         }
 
         encounter = encounterService.saveEncounter(encounter);
 
         if (estDeliveryDate != null) {
             Obs pregnancyDueDateObs = getActivePregnancyDueDateObs(patient
                     .getPatientId(), pregnancyObs);
             if (pregnancyDueDateObs != null) {
                 updatePregnancyDueDateObs(pregnancyObs, pregnancyDueDateObs,
                         estDeliveryDate, encounter);
             } else {
                 log.warn("Cannot update pregnancy due date, "
                         + "no active pregnancy due date found, patient id="
                         + patient.getPatientId());
             }
         }
     }
 
     private void addSerialNumberObservation(Location facility, Date date, Patient patient, String serialNumber, Encounter encounter) {
         if (serialNumber != null) {
             Obs serialNumberObs = createTextValueObs(date,
                     concept(ConceptEnum.CONCEPT_SERIAL_NUMBER), patient, facility, serialNumber,
                     encounter, null);
             encounter.addObs(serialNumberObs);
         }
     }
 
     @Transactional
     public void recordPregnancyTermination(User staff, Location facility,
                                            Date date, Patient patient, Integer terminationType,
                                            Integer procedure, Integer[] complications, Boolean maternalDeath,
                                            Boolean referred, Boolean postAbortionFPCounseled,
                                            Boolean postAbortionFPAccepted, String comments) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PREGTERMVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         Obs pregnancyObs = getActivePregnancy(patient.getPatientId());
         if (pregnancyObs == null) {
             log.warn("Entered Pregnancy termination "
                     + "for patient without active pregnancy, patient id="
                     + patient.getPatientId());
         }
 
         if (terminationType != null) {
             Obs terminationTypeObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_TERMINATION_TYPE), patient, facility,
                     terminationType, encounter, null);
             encounter.addObs(terminationTypeObs);
         }
         if (procedure != null) {
             Obs procedureObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_TERMINATION_PROCEDURE), patient, facility,
                     procedure, encounter, null);
             encounter.addObs(procedureObs);
         }
         if (complications != null) {
             for (Integer complication : complications) {
                 Obs complicationObs = createNumericValueObs(date,
                         concept(ConceptEnum.CONCEPT_TERMINATION_COMPLICATION), patient, facility,
                         complication, encounter, null);
                 encounter.addObs(complicationObs);
             }
         }
         if (maternalDeath != null) {
             Obs maternalDeathObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_MATERNAL_DEATH), patient, facility,
                     maternalDeath, encounter, null);
             encounter.addObs(maternalDeathObs);
         }
         if (referred != null) {
             Obs referredObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_REFERRED),
                     patient, facility, referred, encounter, null);
             encounter.addObs(referredObs);
         }
         if (postAbortionFPCounseled != null) {
             Obs postCounseledObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_POST_ABORTION_FP_COUNSELING), patient, facility,
                     postAbortionFPCounseled, encounter, null);
             encounter.addObs(postCounseledObs);
         }
         if (postAbortionFPAccepted != null) {
             Obs postAcceptedObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_POST_ABORTION_FP_ACCEPTED), patient, facility,
                     postAbortionFPAccepted, encounter, null);
             encounter.addObs(postAcceptedObs);
         }
         if (comments != null) {
             Obs commentsObs = createTextValueObs(date, concept(ConceptEnum.CONCEPT_COMMENTS),
                     patient, facility, comments, encounter, null);
             encounter.addObs(commentsObs);
         }
 
         Obs pregnancyStatusObs = createBooleanValueObs(date,
                 concept(ConceptEnum.CONCEPT_PREGNANCY_STATUS), patient, facility, Boolean.FALSE,
                 encounter, null);
         pregnancyStatusObs.setObsGroup(pregnancyObs);
         encounter.addObs(pregnancyStatusObs);
 
         encounterService.saveEncounter(encounter);
 
         if (Boolean.TRUE.equals(maternalDeath)) {
             processPatientDeath(patient, date);
         }
     }
 
     @Transactional
     public List<Patient> recordPregnancyDelivery(User staff, Facility facility,
                                                  Date datetime, Patient patient, Integer mode, Integer outcome,
                                                  Integer deliveryLocation, Integer deliveredBy,
                                                  Boolean maleInvolved, Integer[] complications, Integer vvf,
                                                  Boolean maternalDeath, String comments,
                                                  List<BirthOutcomeChild> outcomes) {
 
         Encounter encounter = new Encounter();
         Location location = facility.getLocation();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PREGDELVISIT));
         encounter.setEncounterDatetime(datetime);
         encounter.setPatient(patient);
         encounter.setLocation(location);
         encounter.setProvider(staff);
 
         Obs pregnancyObs = getActivePregnancy(patient.getPatientId());
         if (pregnancyObs == null) {
             log.warn("Entered Pregnancy delivery "
                     + "for patient without active pregnancy, patient id="
                     + patient.getPatientId());
         }
 
         if (mode != null) {
             Obs modeObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_DELIVERY_MODE), patient, location, mode,
                     encounter, null);
             encounter.addObs(modeObs);
         }
         if (outcome != null) {
             Obs outcomeObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_DELIVERY_OUTCOME), patient, location, outcome,
                     encounter, null);
             encounter.addObs(outcomeObs);
         }
         if (deliveryLocation != null) {
             Obs locationObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_DELIVERY_LOCATION), patient, location,
                     deliveryLocation, encounter, null);
             encounter.addObs(locationObs);
         }
         if (deliveredBy != null) {
             Obs deliveredByObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_DELIVERED_BY), patient, location, deliveredBy,
                     encounter, null);
             encounter.addObs(deliveredByObs);
         }
         if (maleInvolved != null) {
             Obs maleInvolvedObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_MALE_INVOLVEMENT), patient, location,
                     maleInvolved, encounter, null);
             encounter.addObs(maleInvolvedObs);
         }
         if (complications != null) {
             for (Integer complication : complications) {
                 Obs complicationObs = createNumericValueObs(datetime,
                         concept(ConceptEnum.CONCEPT_DELIVERY_COMPLICATION), patient, location,
                         complication, encounter, null);
                 encounter.addObs(complicationObs);
             }
         }
         if (vvf != null) {
             Obs vvfObs = createNumericValueObs(datetime, concept(ConceptEnum.CONCEPT_VVF_REPAIR),
                     patient, location, vvf, encounter, null);
             encounter.addObs(vvfObs);
         }
         if (maternalDeath != null) {
             Obs maternalDeathObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_MATERNAL_DEATH), patient, location,
                     maternalDeath, encounter, null);
             encounter.addObs(maternalDeathObs);
         }
         if (comments != null) {
             Obs commentsObs = createTextValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_COMMENTS), patient, location, comments,
                     encounter, null);
             encounter.addObs(commentsObs);
         }
 
         Obs pregnancyStatusObs = createBooleanValueObs(datetime,
                 concept(ConceptEnum.CONCEPT_PREGNANCY_STATUS), patient, location, Boolean.FALSE,
                 encounter, null);
         pregnancyStatusObs.setObsGroup(pregnancyObs);
         encounter.addObs(pregnancyStatusObs);
 
         List<Patient> childPatients = new ArrayList<Patient>();
 
         for (BirthOutcomeChild childOutcome : outcomes) {
             if (childOutcome.getOutcome() == null) {
                 // Skip child outcomes missing required outcome
                 continue;
             }
             Obs childOutcomeObs = createTextValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_BIRTH_OUTCOME), patient, location, childOutcome
                             .getOutcome().name(), encounter, null);
             encounter.addObs(childOutcomeObs);
 
             if (BirthOutcome.A == childOutcome.getOutcome()) {
                 Patient child = registerPatient(staff, facility, datetime,
                         childOutcome.getIdMode(), childOutcome.getMotechId(),
                         RegistrantType.CHILD_UNDER_FIVE, childOutcome
                                 .getFirstName(), null, null, null, datetime,
                         false, childOutcome.getSex(), null, null, null,
                         patient, null, null, null, null, null, null, null,
                         null, null, null, null, null, null, null, null);
 
                 if (childOutcome.getWeight() != null) {
                     recordBirthData(staff, location, child, datetime,
                             childOutcome.getWeight());
                 }
 
                 childPatients.add(child);
             }
         }
 
         encounterService.saveEncounter(encounter);
 
         if (Boolean.TRUE.equals(maternalDeath)) {
             processPatientDeath(patient, datetime);
         }
 
         return childPatients;
     }
 
     private void recordBirthData(User staff, Location facility, Patient child,
                                  Date datetime, Double weight) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_BIRTHVISIT));
         encounter.setEncounterDatetime(datetime);
         encounter.setPatient(child);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         if (weight != null) {
             Obs weightObs = createNumericValueObs(datetime, concept(ConceptEnum.CONCEPT_WEIGHT),
                     child, facility, weight, encounter, null);
             encounter.addObs(weightObs);
         }
 
         encounterService.saveEncounter(encounter);
     }
 
     @Transactional
     public void recordPregnancyDeliveryNotification(User staff,
                                                     Location facility, Date date, Patient patient) {
 
         Encounter encounter = new Encounter();
         encounter
                 .setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PREGDELNOTIFYVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         Obs pregnancyObs = getActivePregnancy(patient.getPatientId());
         if (pregnancyObs == null) {
             log
                     .warn("Entered Pregnancy delivery notification for patient without active pregnancy, "
                             + "patient id=" + patient.getPatientId());
         }
 
         Obs pregnancyStatusObs = createBooleanValueObs(date,
                 concept(ConceptEnum.CONCEPT_PREGNANCY_STATUS), patient, facility, Boolean.FALSE,
                 encounter, null);
         pregnancyStatusObs.setObsGroup(pregnancyObs);
         encounter.addObs(pregnancyStatusObs);
 
         encounterService.saveEncounter(encounter);
 
         // Send message only if closing active pregnancy
         if (pregnancyObs != null) {
             sendDeliveryNotification(patient);
         }
     }
 
     private void sendDeliveryNotification(Patient patient) {
         // Send message to phone number of facility serving patient's community
 
         Facility facility = getFacilityByPatient(patient);
         if (isNotNull(facility)) {
             String phoneNumber = facility.getPhoneNumber();
             if (phoneNumber != null) {
                 String messageId = null;
                 NameValuePair[] nameValues = new NameValuePair[0];
                 MediaType mediaType = MediaType.TEXT;
                 String languageCode = "en";
 
                 // Send immediately if not during blackout,
                 // otherwise adjust time to after the blackout period
                 Date currentDate = new Date();
                 Date messageStartDate = adjustDateForBlackout(currentDate);
                 if (currentDate.equals(messageStartDate)) {
                     messageStartDate = null;
                 }
 
                 WebServicePatientModelConverterImpl wsModelConverter = new WebServicePatientModelConverterImpl();
                wsModelConverter.setContextService(contextService);
                 wsModelConverter.setRegistrarBean(this);
                 org.motechproject.ws.Patient wsPatient = wsModelConverter
                         .patientToWebService(patient, true);
                 org.motechproject.ws.Patient[] wsPatients = new org.motechproject.ws.Patient[]{wsPatient};
 
                 MessageDefinition messageDef = wsPatient.getCommunity() == null ? getMessageDefinition("pregnancy.notification.for.patient.with.no.community") : getMessageDefinition("pregnancy.notification");
                 if (messageDef == null) {
                     log.error("Pregnancy delivery notification message "
                             + "does not exist");
                     return;
                 }
 
                 sendStaffMessage(messageId, nameValues, phoneNumber,
                         languageCode, mediaType, messageDef.getPublicId(),
                         messageStartDate, null, wsPatients);
             }
         }
     }
 
 
     @Transactional
     public void recordMotherPNCVisit(User staff, Location facility,
                                      Date datetime, Patient patient, Integer visitNumber,
                                      Integer pncLocation, String house, String community,
                                      Boolean referred, Boolean maleInvolved, Boolean vitaminA,
                                      Integer ttDose, Integer lochiaColour, Boolean lochiaAmountExcess,
                                      Boolean lochiaOdourFoul, Double temperature, Double fht,
                                      String comments) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PNCMOTHERVISIT));
         encounter.setEncounterDatetime(datetime);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         if (visitNumber != null) {
             Obs visitNumberObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_VISIT_NUMBER), patient, facility, visitNumber,
                     encounter, null);
             encounter.addObs(visitNumberObs);
         }
         if (pncLocation != null) {
             Obs pncLocationObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_ANC_PNC_LOCATION), patient, facility, pncLocation,
                     encounter, null);
             encounter.addObs(pncLocationObs);
         }
         if (house != null) {
             Obs houseObs = createTextValueObs(datetime, concept(ConceptEnum.CONCEPT_HOUSE),
                     patient, facility, house, encounter, null);
             encounter.addObs(houseObs);
         }
         if (community != null) {
             Obs communityObs = createTextValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_COMMUNITY), patient, facility, community,
                     encounter, null);
             encounter.addObs(communityObs);
         }
         if (referred != null) {
             Obs referredObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_REFERRED), patient, facility, referred,
                     encounter, null);
             encounter.addObs(referredObs);
         }
         if (maleInvolved != null) {
             Obs maleInvolvedObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_MALE_INVOLVEMENT), patient, facility,
                     maleInvolved, encounter, null);
             encounter.addObs(maleInvolvedObs);
         }
         if (Boolean.TRUE.equals(vitaminA)) {
             Obs vitaminAObs = createConceptValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_IMMUNIZATIONS_ORDERED), patient, facility,
                     concept(ConceptEnum.CONCEPT_VITAMIN_A), encounter, null);
             encounter.addObs(vitaminAObs);
         }
         if (ttDose != null) {
             Obs ttDoseObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_TETANUS_TOXOID_DOSE), patient, facility, ttDose,
                     encounter, null);
             encounter.addObs(ttDoseObs);
         }
         if (lochiaColour != null) {
             Obs lochiaColourObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_LOCHIA_COLOUR), patient, facility, lochiaColour,
                     encounter, null);
             encounter.addObs(lochiaColourObs);
         }
         if (lochiaOdourFoul != null) {
             Obs lochiaOdourObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_LOCHIA_FOUL_ODOUR), patient, facility, lochiaOdourFoul,
                     encounter, null);
             encounter.addObs(lochiaOdourObs);
         }
         if (lochiaAmountExcess != null) {
             Obs lochiaAmountObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_LOCHIA_EXCESS_AMOUNT), patient, facility,
                     lochiaAmountExcess, encounter, null);
             encounter.addObs(lochiaAmountObs);
         }
         if (temperature != null) {
             Obs temperatureObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_TEMPERATURE), patient, facility, temperature,
                     encounter, null);
             encounter.addObs(temperatureObs);
         }
         if (fht != null) {
             Obs fhtObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_FUNDAL_HEIGHT), patient, facility, fht,
                     encounter, null);
             encounter.addObs(fhtObs);
         }
         if (comments != null) {
             Obs commentsObs = createTextValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_COMMENTS), patient, facility, comments,
                     encounter, null);
             encounter.addObs(commentsObs);
         }
 
         encounterService.saveEncounter(encounter);
     }
 
     @Transactional
     public void recordChildPNCVisit(User staff, Location facility,
                                     Date datetime, Patient patient, Integer visitNumber,
                                     Integer pncLocation, String house, String community,
                                     Boolean referred, Boolean maleInvolved, Double weight,
                                     Double temperature, Boolean bcg, Boolean opv0, Integer respiration,
                                     Boolean cordConditionNormal, Boolean babyConditionGood,
                                     String comments) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PNCCHILDVISIT));
         encounter.setEncounterDatetime(datetime);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         if (visitNumber != null) {
             Obs visitNumberObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_VISIT_NUMBER), patient, facility, visitNumber,
                     encounter, null);
             encounter.addObs(visitNumberObs);
         }
         if (pncLocation != null) {
             Obs pncLocationObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_ANC_PNC_LOCATION), patient, facility, pncLocation,
                     encounter, null);
             encounter.addObs(pncLocationObs);
         }
         if (house != null) {
             Obs houseObs = createTextValueObs(datetime, concept(ConceptEnum.CONCEPT_HOUSE),
                     patient, facility, house, encounter, null);
             encounter.addObs(houseObs);
         }
         if (community != null) {
             Obs communityObs = createTextValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_COMMUNITY), patient, facility, community,
                     encounter, null);
             encounter.addObs(communityObs);
         }
         if (referred != null) {
             Obs referredObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_REFERRED), patient, facility, referred,
                     encounter, null);
             encounter.addObs(referredObs);
         }
         if (maleInvolved != null) {
             Obs maleInvolvedObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_MALE_INVOLVEMENT), patient, facility,
                     maleInvolved, encounter, null);
             encounter.addObs(maleInvolvedObs);
         }
         if (weight != null) {
             Obs weightObs = createNumericValueObs(datetime, concept(ConceptEnum.CONCEPT_WEIGHT),
                     patient, facility, weight, encounter, null);
             encounter.addObs(weightObs);
         }
         if (temperature != null) {
             Obs temperatureObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_TEMPERATURE), patient, facility, temperature,
                     encounter, null);
             encounter.addObs(temperatureObs);
         }
         if (Boolean.TRUE.equals(bcg)) {
             Obs bcgObs = createConceptValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_IMMUNIZATIONS_ORDERED), patient, facility,
                     concept(ConceptEnum.CONCEPT_BCG_VACCINATION), encounter, null);
             encounter.addObs(bcgObs);
         }
         if (Boolean.TRUE.equals(opv0)) {
             Integer opvDose = 0;
             Obs opvDoseObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_ORAL_POLIO_VACCINATION_DOSE),
                     patient, facility, opvDose, encounter,
                     null);
             encounter.addObs(opvDoseObs);
         }
         if (respiration != null) {
             Obs respirationObs = createNumericValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_RESPIRATORY_RATE), patient, facility,
                     respiration, encounter, null);
             encounter.addObs(respirationObs);
         }
         if (cordConditionNormal != null) {
             Obs cordConditionObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_CORD_CONDITION), patient, facility,
                     cordConditionNormal, encounter, null);
             encounter.addObs(cordConditionObs);
         }
         if (babyConditionGood != null) {
             Obs babyConditionObs = createBooleanValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_CONDITION_OF_BABY), patient, facility,
                     babyConditionGood, encounter, null);
             encounter.addObs(babyConditionObs);
         }
         if (comments != null) {
             Obs commentsObs = createTextValueObs(datetime,
                     concept(ConceptEnum.CONCEPT_COMMENTS), patient, facility, comments,
                     encounter, null);
             encounter.addObs(commentsObs);
         }
 
         encounterService.saveEncounter(encounter);
     }
 
     @Transactional
     public void recordTTVisit(User staff, Location facility, Date date,
                               Patient patient, Integer ttDose) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_TTVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         if (ttDose != null) {
             Obs ttDoseObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_TETANUS_TOXOID_DOSE), patient, facility, ttDose,
                     encounter, null);
             encounter.addObs(ttDoseObs);
         }
         encounterService.saveEncounter(encounter);
     }
 
     @Transactional
     public void recordDeath(User staff, Location facility, Date date,
                             Patient patient) {
 
         processPatientDeath(patient, date);
     }
 
     private void processPatientDeath(Patient patient, Date date) {
         // Stop all messages and remove all message program enrollments
         removeAllMessageProgramEnrollments(patient.getPatientId());
 
         patient.setDead(true);
         patient.setDeathDate(date);
         patient = patientService.savePatient(patient);
 
         personService.voidPerson(patient, "Deceased");
     }
 
     @Transactional
     public void recordChildCWCVisit(User staff, Location location, Date date,
                                     Patient patient, String serialNumber, Integer cwcLocation, String house,
                                     String community, String immunizationsGiven,
                                     Integer opvDose, Integer pentaDose,Integer iptiDose,
                                     Double weight, Double muac, Double height, Boolean maleInvolved,
                                     String comments) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_CWCVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(location);
         encounter.setProvider(staff);
 
         addSerialNumberObservation(location, date, patient, serialNumber, encounter);
 
         for (String key : immunizationsGiven.split(SINGLE_SPACE)) {
             Immunizations immunization = Immunizations.enumFor(key);
             if(immunization != null){
                 encounter.addObs(immunization.obsWith(date,patient,location,encounter,staff,conceptService));
             }
         }
 
         if (cwcLocation != null) {
             Obs cwcLocationObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_CWC_LOCATION), patient, location, cwcLocation,
                     encounter, null);
             encounter.addObs(cwcLocationObs);
         }
         if (house != null) {
             Obs houseObs = createTextValueObs(date, concept(ConceptEnum.CONCEPT_HOUSE), patient,
                     location, house, encounter, null);
             encounter.addObs(houseObs);
         }
         if (community != null) {
             Obs communityObs = createTextValueObs(date, concept(ConceptEnum.CONCEPT_COMMUNITY),
                     patient, location, community, encounter, null);
             encounter.addObs(communityObs);
         }
         if (opvDose != null) {
             Obs opvDoseObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_ORAL_POLIO_VACCINATION_DOSE),
                     patient, location, opvDose, encounter, null);
             encounter.addObs(opvDoseObs);
         }
         if (pentaDose != null) {
             Obs pentaDoseObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_PENTA_VACCINATION_DOSE), patient, location, pentaDose,
                     encounter, null);
             encounter.addObs(pentaDoseObs);
         }
         if (iptiDose != null) {
             Obs iptiObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_INTERMITTENT_PREVENTATIVE_TREATMENT_INFANTS_DOSE),
                     patient, location, iptiDose, encounter, null);
             encounter.addObs(iptiObs);
         }
         if (weight != null) {
             Obs weightObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_WEIGHT),
                     patient, location, weight, encounter, null);
             encounter.addObs(weightObs);
         }
         if (muac != null) {
             Obs muacObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_MIDDLE_UPPER_ARM_CIRCUMFERENCE),
                     patient, location, muac, encounter, null);
             encounter.addObs(muacObs);
         }
         if (height != null) {
             Obs heightObs = createNumericValueObs(date, concept(ConceptEnum.CONCEPT_HEIGHT),
                     patient, location, height, encounter, null);
             encounter.addObs(heightObs);
         }
         if (maleInvolved != null) {
             Obs maleInvolvedObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_MALE_INVOLVEMENT), patient, location,
                     maleInvolved, encounter, null);
             encounter.addObs(maleInvolvedObs);
         }
         if (comments != null) {
             Obs commentsObs = createTextValueObs(date, concept(ConceptEnum.CONCEPT_COMMENTS),
                     patient, location, comments, encounter, null);
             encounter.addObs(commentsObs);
         }
 
         encounterService.saveEncounter(encounter);
     }
 
     @Transactional
     public void recordGeneralOutpatientVisit(Integer staffId,
                                              Integer facilityId, Date date, String serialNumber, Gender sex,
                                              Date dateOfBirth, Boolean insured, Integer diagnosis,
                                              Integer secondDiagnosis, Boolean rdtGiven, Boolean rdtPositive,
                                              Boolean actTreated, Boolean newCase, Boolean newPatient, Boolean referred,
                                              String comments) {
 
         GeneralOutpatientEncounter encounter = new GeneralOutpatientEncounter(
                 date, staffId, facilityId, serialNumber, sex, dateOfBirth,
                 insured, newCase, newPatient, diagnosis, secondDiagnosis, referred,
                 rdtGiven, rdtPositive, actTreated, comments);
 
         if (log.isDebugEnabled()) {
             log.debug(encounter.toString());
         }
 
         motechService().saveGeneralOutpatientEncounter(encounter);
     }
 
     @Transactional
     public void recordOutpatientVisit(User staff, Location facility, Date date,
                                       Patient patient, String serialNumber, Boolean insured,
                                       Integer diagnosis, Integer secondDiagnosis, Boolean rdtGiven,
                                       Boolean rdtPositive, Boolean actTreated, Boolean newCase,
                                       Boolean newPatient, Boolean referred, String comments) {
 
         Encounter encounter = new Encounter();
         encounter.setEncounterType(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_OUTPATIENTVISIT));
         encounter.setEncounterDatetime(date);
         encounter.setPatient(patient);
         encounter.setLocation(facility);
         encounter.setProvider(staff);
 
         addSerialNumberObservation(facility, date, patient, serialNumber, encounter);
 
         if (insured != null) {
             Obs insuredObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_INSURED),
                     patient, facility, insured, encounter, null);
             encounter.addObs(insuredObs);
         }
         if (newCase != null) {
             Obs newCaseObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_NEW_CASE),
                     patient, facility, newCase, encounter, null);
             encounter.addObs(newCaseObs);
         }
         if (newPatient != null) {
             Obs newPatientObs = createBooleanValueObs(date, concept(ConceptEnum.PATIENT_NEW_CASE),
                     patient, facility, newPatient, encounter, null);
             encounter.addObs(newPatientObs);
         }
         if (diagnosis != null) {
             Obs diagnosisObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_PRIMARY_DIAGNOSIS), patient, facility, diagnosis,
                     encounter, null);
             encounter.addObs(diagnosisObs);
         }
         if (secondDiagnosis != null) {
             Obs secondDiagnosisObs = createNumericValueObs(date,
                     concept(ConceptEnum.CONCEPT_SECONDARY_DIAGNOSIS), patient, facility,
                     secondDiagnosis, encounter, null);
             encounter.addObs(secondDiagnosisObs);
         }
         if (referred != null) {
             Obs referredObs = createBooleanValueObs(date, concept(ConceptEnum.CONCEPT_REFERRED),
                     patient, facility, referred, encounter, null);
             encounter.addObs(referredObs);
         }
         if (Boolean.TRUE.equals(rdtGiven)) {
             Concept rdtTestValueConcept;
             if (Boolean.TRUE.equals(rdtPositive)) {
                 rdtTestValueConcept = concept(ConceptEnum.CONCEPT_POSITIVE);
             } else {
                 rdtTestValueConcept = concept(ConceptEnum.CONCEPT_NEGATIVE);
             }
             Obs rdtTestObs = createConceptValueObs(date,
                     concept(ConceptEnum.CONCEPT_MALARIA_RAPID_TEST), patient, facility,
                     rdtTestValueConcept, encounter, null);
             encounter.addObs(rdtTestObs);
         }
         if (actTreated != null) {
             Obs actTreatedObs = createBooleanValueObs(date,
                     concept(ConceptEnum.CONCEPT_ACT_TREATMENT), patient, facility, actTreated,
                     encounter, null);
             encounter.addObs(actTreatedObs);
         }
         if (comments != null) {
             Obs commentsObs = createTextValueObs(date, concept(ConceptEnum.CONCEPT_COMMENTS),
                     patient, facility, comments, encounter, null);
             encounter.addObs(commentsObs);
         }
 
         encounterService.saveEncounter(encounter);
     }
 
     @Transactional
     public void setMessageStatus(String messageId, Boolean success) {
 
         log.debug("setMessageStatus WS: messageId: " + messageId
                 + ", success: " + success);
 
         Message message = motechService().getMessage(messageId);
         if (message == null) {
             throw new MessageNotFoundException();
         }
 
         if (success) {
             message.setAttemptStatus(MessageStatus.DELIVERED);
         } else {
             message.setAttemptStatus(MessageStatus.ATTEMPT_FAIL);
         }
         motechService().saveMessage(message);
     }
 
     public User getUserByPhoneNumber(String phoneNumber) {
         PersonAttributeType phoneAttributeType = PersonAttributeTypeEnum.PERSON_ATTRIBUTE_PHONE_NUMBER.getAttributeType(personService);
         List<Integer> matchingUsers = motechService()
                 .getUserIdsByPersonAttribute(phoneAttributeType, phoneNumber);
         if (matchingUsers.size() > 0) {
             if (matchingUsers.size() > 1) {
                 log.warn("Multiple staff found for phone number: "
                         + phoneNumber);
             }
             // If more than one user matches phone number, first user in list is
             // returned
             Integer userId = matchingUsers.get(0);
             return userService.getUser(userId);
         }
         log.warn("No staff found for phone number: " + phoneNumber);
         return null;
     }
 
     /* MotechService methods end */
 
     /* Controller methods start */
 
     public List<Location> getAllLocations() {
         return locationService.getAllLocations();
     }
 
     public List<User> getAllStaff() {
         return userService.getAllUsers();
     }
 
     public List<Patient> getAllPatients() {
         List<PatientIdentifierType> motechPatientIdType = new ArrayList<PatientIdentifierType>();
         motechPatientIdType.add(getPatientIdentifierTypeForMotechId());
         return patientService.getPatients(null, null, motechPatientIdType,
                 false);
     }
 
 
     public List<Patient> getPatients(String firstName, String lastName,
                                      String preferredName, Date birthDate, Integer facilityId,
                                      String phoneNumber, String nhisNumber, Integer communityId, String motechId) {
         PersonAttributeType phoneNumberAttrType = PersonAttributeTypeEnum.PERSON_ATTRIBUTE_PHONE_NUMBER.getAttributeType(personService);
         PersonAttributeType nhisAttrType = PersonAttributeTypeEnum.PERSON_ATTRIBUTE_NHIS_NUMBER.getAttributeType(personService);
         PatientIdentifierType motechIdType = getPatientIdentifierTypeForMotechId();
         Integer maxResults = getMaxQueryResults();
 
         return motechService().getPatients(firstName, lastName, preferredName,
                 birthDate, facilityId, phoneNumber, phoneNumberAttrType,
                 nhisNumber, nhisAttrType, communityId, motechId, motechIdType, maxResults);
     }
 
     public List<Patient> getDuplicatePatients(String firstName,
                                               String lastName, String preferredName, Date birthDate,
                                               Integer facilityId, String phoneNumber, String nhisNumber,
                                               String motechId) {
         PersonAttributeType phoneNumberAttrType = PersonAttributeTypeEnum.PERSON_ATTRIBUTE_PHONE_NUMBER.getAttributeType(personService);
         PersonAttributeType nhisAttrType = PersonAttributeTypeEnum.PERSON_ATTRIBUTE_NHIS_NUMBER.getAttributeType(personService);
         PatientIdentifierType motechIdType = getPatientIdentifierTypeForMotechId();
         Integer maxResults = getMaxQueryResults();
 
         return motechService().getDuplicatePatients(firstName, lastName,
                 preferredName, birthDate, facilityId, phoneNumber,
                 phoneNumberAttrType, nhisNumber, nhisAttrType, motechId,
                 motechIdType, maxResults);
     }
 
     public List<Obs> getAllPregnancies() {
         List<Concept> pregnancyConcept = new ArrayList<Concept>();
         pregnancyConcept.add(concept(ConceptEnum.CONCEPT_PREGNANCY));
         return obsService.getObservations(null, null, pregnancyConcept, null,
                 null, null, null, null, null, null, null, false);
     }
 
     public List<ExpectedEncounter> getUpcomingExpectedEncounters(Patient patient) {
         Calendar calendar = Calendar.getInstance();
         Date currentDate = calendar.getTime();
         calendar.add(Calendar.DATE, 7);
         Date oneWeekLaterDate = calendar.getTime();
         Integer maxResults = getMaxQueryResults();
         return motechService().getExpectedEncounter(patient, null, null, null,
                 oneWeekLaterDate, null, currentDate, maxResults);
     }
 
     public List<ExpectedObs> getUpcomingExpectedObs(Patient patient) {
         Calendar calendar = Calendar.getInstance();
         Date currentDate = calendar.getTime();
         calendar.add(Calendar.DATE, 7);
         Date oneWeekLaterDate = calendar.getTime();
         Integer maxResults = getMaxQueryResults();
         return motechService().getExpectedObs(patient, null, null, null,
                 oneWeekLaterDate, null, currentDate, maxResults);
     }
 
     public List<ExpectedEncounter> getDefaultedExpectedEncounters(
             Facility facility, String[] groups) {
         Date currentDate = new Date();
         Integer maxResults = getMaxQueryResults();
         return motechService().getExpectedEncounter(null, facility, groups, null,
                 null, currentDate, currentDate, maxResults);
     }
 
     public List<ExpectedObs> getDefaultedExpectedObs(Facility facility,
                                                      String[] groups) {
         Date currentDate = new Date();
         Integer maxResults = getMaxQueryResults();
         return motechService().getExpectedObs(null, facility, groups, null, null,
                 currentDate, currentDate, maxResults);
     }
 
 
     public List<ExpectedEncounter> getExpectedEncounters(Patient patient) {
         Date currentDate = new Date();
         return motechService().getExpectedEncounter(patient, null, null, null,
                 null, null, currentDate, null);
     }
 
     public List<ExpectedObs> getExpectedObs(Patient patient) {
         Date currentDate = new Date();
         return motechService().getExpectedObs(patient, null, null, null, null,
                 null, currentDate, null);
     }
 
     public List<ExpectedEncounter> getExpectedEncounters(Patient patient,
                                                          String group) {
         return motechService().getExpectedEncounter(patient, null,
                 new String[]{group}, null, null, null, null, null);
     }
 
     public List<ExpectedObs> getExpectedObs(Patient patient, String group) {
         return motechService().getExpectedObs(patient, null,
                 new String[]{group}, null, null, null, null, null);
     }
 
     public List<Encounter> getRecentDeliveries(Facility facility) {
         EncounterType deliveryEncounterType = getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PREGDELVISIT);
 
         Calendar calendar = Calendar.getInstance();
         Date currentDate = calendar.getTime();
         calendar.add(Calendar.DATE, 2 * -7);
         Date twoWeeksPriorDate = calendar.getTime();
 
         Integer maxResults = getMaxQueryResults();
 
         return motechService().getEncounters(facility, deliveryEncounterType,
                 twoWeeksPriorDate, currentDate, maxResults);
     }
 
     public Date getCurrentDeliveryDate(Patient patient) {
         List<EncounterType> deliveryEncounterType = new ArrayList<EncounterType>();
         deliveryEncounterType.add(getEncounterType(EncounterTypeEnum.ENCOUNTER_TYPE_PREGDELVISIT));
 
         List<Encounter> deliveries = encounterService.getEncounters(patient,
                 null, null, null, null, deliveryEncounterType, null, false);
 
         if (!deliveries.isEmpty()) {
             // List is ascending by date, get last match to get most recent
             return deliveries.get(deliveries.size() - 1).getEncounterDatetime();
         }
         return null;
     }
 
     public List<Obs> getUpcomingPregnanciesDueDate(Facility facility) {
         Calendar calendar = Calendar.getInstance();
         Date currentDate = calendar.getTime();
         calendar.add(Calendar.DATE, 2 * 7);
         Date twoWeeksLaterDate = calendar.getTime();
 
         return getActivePregnanciesDueDateObs(facility, currentDate,
                 twoWeeksLaterDate);
     }
 
     public List<Obs> getOverduePregnanciesDueDate(Facility facility) {
         Date currentDate = new Date();
         return getActivePregnanciesDueDateObs(facility, null, currentDate);
     }
 
     private List<Obs> getActivePregnanciesDueDateObs(Facility facility,
                                                      Date fromDueDate, Date toDueDate) {
         Concept pregnancyDueDateConcept = concept(ConceptEnum.CONCEPT_ESTIMATED_DATE_OF_CONFINEMENT);
         Concept pregnancyConcept = concept(ConceptEnum.CONCEPT_PREGNANCY);
         Concept pregnancyStatusConcept = concept(ConceptEnum.CONCEPT_PREGNANCY_STATUS);
         Integer maxResults = getMaxQueryResults();
 
         return motechService().getActivePregnanciesDueDateObs(facility,
                 fromDueDate, toDueDate, pregnancyDueDateConcept,
                 pregnancyConcept, pregnancyStatusConcept, maxResults);
     }
 
     private Integer updatePregnancyDueDateObs(Obs pregnancyObs, Obs dueDateObs,
                                               Date newDueDate, Encounter encounter) {
         Integer existingDueDateObsId = dueDateObs.getObsId();
 
         Obs newDueDateObs = createDateValueObs(
                 encounter.getEncounterDatetime(),
                 concept(ConceptEnum.CONCEPT_ESTIMATED_DATE_OF_CONFINEMENT),
                 encounter.getPatient(), encounter.getLocation(), newDueDate,
                 encounter, null);
         newDueDateObs.setObsGroup(pregnancyObs);
         newDueDateObs = obsService.saveObs(newDueDateObs, null);
 
         obsService.voidObs(dueDateObs, "Replaced by new EDD value Obs: "
                 + newDueDateObs.getObsId());
 
         // Update enrollments using duedate Obs to reference new duedate Obs
         List<MessageProgramEnrollment> enrollments = motechService()
                 .getActiveMessageProgramEnrollments(null, null,
                         existingDueDateObsId, null, null);
         for (MessageProgramEnrollment enrollment : enrollments) {
             enrollment.setObsId(newDueDateObs.getObsId());
             motechService().saveMessageProgramEnrollment(enrollment);
         }
         return newDueDateObs.getObsId();
     }
 
     public Patient getPatientById(Integer patientId) {
         return patientService.getPatient(patientId);
     }
 
     public Obs getActivePregnancy(Integer patientId) {
         return pregnancyObservation.getActivePregnancy(patientId);
     }
 
     public List<ScheduledMessage> getAllScheduledMessages() {
         return motechService().getAllScheduledMessages();
     }
 
     public List<ScheduledMessage> getScheduledMessages(
             MessageProgramEnrollment enrollment) {
         return motechService().getScheduledMessages(null, null, enrollment, null);
     }
 
     /* Controller methods end */
 
     public List<Obs> getObs(Patient patient, String conceptName,
                             String valueConceptName, Date minDate) {
         Concept concept = conceptService.getConcept(conceptName);
         Concept value = conceptService.getConcept(valueConceptName);
 
         List<Concept> questions = new ArrayList<Concept>();
         questions.add(concept);
 
         List<Concept> answers = null;
         if (value != null) {
             answers = new ArrayList<Concept>();
             answers.add(value);
         }
 
         List<Person> whom = new ArrayList<Person>();
         whom.add(patient);
 
         return obsService.getObservations(whom, null, questions, answers, null,
                 null, null, null, null, minDate, null, false);
     }
 
     public ExpectedObs createExpectedObs(Patient patient, String conceptName,
                                          String valueConceptName, Integer value, Date minDate, Date dueDate,
                                          Date lateDate, Date maxDate, String name, String group) {
         Concept concept = conceptService.getConcept(conceptName);
         Concept valueConcept = conceptService.getConcept(valueConceptName);
 
         ExpectedObs expectedObs = new ExpectedObs();
         expectedObs.setPatient(patient);
         expectedObs.setConcept(concept);
         expectedObs.setValueCoded(valueConcept);
         if (value != null) {
             expectedObs.setValueNumeric((double) value);
         }
         expectedObs.setMinObsDatetime(minDate);
         expectedObs.setDueObsDatetime(dueDate);
         expectedObs.setLateObsDatetime(lateDate);
         expectedObs.setMaxObsDatetime(maxDate);
         expectedObs.setName(name);
         expectedObs.setGroup(group);
 
         return saveExpectedObs(expectedObs);
     }
 
     public ExpectedObs saveExpectedObs(ExpectedObs expectedObs) {
         if (log.isDebugEnabled()) {
             log.debug("Saving schedule update: " + expectedObs.toString());
         }
         if (expectedObs.getDueObsDatetime() != null
                 && expectedObs.getLateObsDatetime() != null) {
 
             return motechService().saveExpectedObs(expectedObs);
         } else {
             log
                     .error("Attempt to store ExpectedObs with null due or late date");
             return null;
         }
     }
 
     public List<Encounter> getEncounters(Patient patient,
                                          String encounterTypeName, Date minDate) {
         EncounterType encounterType = encounterService
                 .getEncounterType(encounterTypeName);
 
         List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
         encounterTypes.add(encounterType);
 
         return encounterService.getEncounters(patient, null, minDate, null,
                 null, encounterTypes, null, false);
     }
 
     public ExpectedEncounter createExpectedEncounter(Patient patient,
                                                      String encounterTypeName, Date minDate, Date dueDate,
                                                      Date lateDate, Date maxDate, String name, String group) {
         EncounterType encounterType = encounterService
                 .getEncounterType(encounterTypeName);
 
         ExpectedEncounter expectedEncounter = new ExpectedEncounter();
         expectedEncounter.setPatient(patient);
         expectedEncounter.setEncounterType(encounterType);
         expectedEncounter.setMinEncounterDatetime(minDate);
         expectedEncounter.setDueEncounterDatetime(dueDate);
         expectedEncounter.setLateEncounterDatetime(lateDate);
         expectedEncounter.setMaxEncounterDatetime(maxDate);
         expectedEncounter.setName(name);
         expectedEncounter.setGroup(group);
 
         return saveExpectedEncounter(expectedEncounter);
     }
 
     public ExpectedEncounter saveExpectedEncounter(
             ExpectedEncounter expectedEncounter) {
         if (log.isDebugEnabled()) {
             log
                     .debug("Saving schedule update: "
                             + expectedEncounter.toString());
         }
         if (expectedEncounter.getDueEncounterDatetime() != null
                 && expectedEncounter.getLateEncounterDatetime() != null) {
 
             return motechService().saveExpectedEncounter(expectedEncounter);
         } else {
             log
                     .error("Attempt to store ExpectedEncounter with null due or late date");
             return null;
         }
     }
 
     /* PatientObsService methods start */
 
     public Date getPatientBirthDate(Integer patientId) {
         Patient patient = patientService.getPatient(patientId);
         return patient.getBirthdate();
     }
 
     private List<Obs> getMatchingObs(Person person, Concept question,
                                      Concept answer, Integer obsGroupId, Date from, Date to) {
 
 
         return observationBean.getMatchingObs(person, question, answer, obsGroupId, from, to);
 
 
     }
 
     public int getNumberOfObs(Integer personId, String conceptName,
                               String conceptValue) {
 
         return getNumberOfObs(personService.getPerson(personId), conceptService
                 .getConcept(conceptName), conceptService
                 .getConcept(conceptValue));
     }
 
     public Date getLastObsCreationDate(Integer personId, String conceptName,
                                        String conceptValue) {
 
         return getLastObsCreationDate(personService.getPerson(personId),
                 conceptService.getConcept(conceptName), conceptService
                         .getConcept(conceptValue));
     }
 
     public Date getLastObsDate(Integer personId, String conceptName,
                                String conceptValue) {
 
         return getLastObsDate(personService.getPerson(personId), conceptService
                 .getConcept(conceptName), conceptService
                 .getConcept(conceptValue));
     }
 
     public Date getLastDoseObsDate(Integer personId, String conceptName,
                                    Integer doseNumber) {
         List<Obs> matchingObs = obsService.getObservationsByPersonAndConcept(
                 personService.getPerson(personId), conceptService
                         .getConcept(conceptName));
         for (Obs obs : matchingObs) {
             Double value = obs.getValueNumeric();
             if (value != null && doseNumber == value.intValue()) {
                 return obs.getObsDatetime();
             }
         }
         return null;
     }
 
     public Date getLastDoseObsDateInActivePregnancy(Integer patientId,
                                                     String conceptName, Integer doseNumber) {
         Obs pregnancy = getActivePregnancy(patientId);
         if (pregnancy != null) {
             Integer pregnancyObsId = pregnancy.getObsId();
             List<Obs> matchingObs = getMatchingObs(personService
                     .getPerson(patientId), conceptService
                     .getConcept(conceptName), null, pregnancyObsId, null, null);
             for (Obs obs : matchingObs) {
                 Double value = obs.getValueNumeric();
                 if (value != null && doseNumber == value.intValue()) {
                     return obs.getObsDatetime();
                 }
             }
         }
         return null;
     }
 
     public Obs getActivePregnancyDueDateObs(Integer patientId, Obs pregnancy) {
         return pregnancyObservation.getActivePregnancyDueDateObs(patientId, pregnancy);
     }
 
     public Date getActivePregnancyDueDate(Integer patientId) {
         return pregnancyObservation.getActivePregnancyDueDate(patientId);
     }
 
     public Date getLastPregnancyEndDate(Integer patientId) {
         List<Obs> pregnancyStatusObsList = getMatchingObs(personService
                 .getPerson(patientId), concept(ConceptEnum.CONCEPT_PREGNANCY_STATUS), null, null,
                 null, null);
         for (Obs pregnancyStatusObs : pregnancyStatusObsList) {
             Boolean status = pregnancyStatusObs.getValueAsBoolean();
             if (Boolean.FALSE.equals(status)) {
                 return pregnancyStatusObs.getObsDatetime();
             }
         }
         return null;
     }
 
     public Date getLastObsValue(Integer personId, String conceptName) {
         return getLastObsValue(personService.getPerson(personId),
                 conceptService.getConcept(conceptName));
     }
 
     public int getNumberOfObs(Person person, Concept concept, Concept value) {
 
         List<Obs> obsList = getMatchingObs(person, concept, value, null, null,
                 null);
         return obsList.size();
     }
 
     public Date getLastObsCreationDate(Person person, Concept concept,
                                        Concept value) {
 
         Date latestObsDate = null;
 
         // List default sorted by Obs datetime
         List<Obs> obsList = getMatchingObs(person, concept, value, null, null,
                 null);
 
         if (obsList.size() > 0) {
             latestObsDate = obsList.get(obsList.size() - 1).getDateCreated();
         } else if (log.isDebugEnabled()) {
             log.debug("No matching Obs: person id: " + person.getPersonId()
                     + ", concept: " + concept.getConceptId() + ", value: "
                     + (value != null ? value.getConceptId() : "null"));
         }
         return latestObsDate;
     }
 
     public Date getLastObsDate(Person person, Concept concept, Concept value) {
 
         Date latestObsDate = null;
 
         // List default sorted by Obs datetime
         List<Obs> obsList = getMatchingObs(person, concept, value, null, null,
                 null);
 
         if (obsList.size() > 0) {
             latestObsDate = obsList.get(0).getObsDatetime();
         } else if (log.isDebugEnabled()) {
             log.debug("No matching Obs: person id: " + person.getPersonId()
                     + ", concept: " + concept.getConceptId() + ", value: "
                     + (value != null ? value.getConceptId() : "null"));
         }
         return latestObsDate;
     }
 
     public Date getLastObsValue(Person person, Concept concept) {
         Date lastestObsValue = null;
 
         List<Obs> obsList = getMatchingObs(person, concept, null, null, null,
                 null);
         if (obsList.size() > 0) {
             lastestObsValue = obsList.get(0).getValueDatetime();
         } else if (log.isDebugEnabled()) {
             log.debug("No matching Obs: person id: " + person.getPersonId()
                     + ", concept: " + concept.getConceptId());
         }
         return lastestObsValue;
     }
 
     public Date getObsValue(Integer obsId) {
         Date result = null;
         if (obsId != null) {
             Obs obs = obsService.getObs(obsId);
             if (obs != null) {
                 result = obs.getValueDatetime();
             }
         }
         return result;
     }
 
     /* MessageSchedulerImpl methods start */
 
     public void scheduleInfoMessages(String messageKey, String messageKeyA,
                                      String messageKeyB, String messageKeyC,
                                      MessageProgramEnrollment enrollment, Date messageDate,
                                      boolean userPreferenceBased, Date currentDate) {
 
         // TODO: Assumes recipient is person in enrollment
         Integer messageRecipientId = enrollment.getPersonId();
         Person recipient = personService.getPerson(messageRecipientId);
         MediaType mediaType = getPersonMediaType(recipient);
 
         // Schedule multiple messages if media type preference is text, or no
         // preference exists, using A/B/C message keys
         if (mediaType == MediaType.TEXT) {
             scheduleMultipleInfoMessages(messageKeyA, messageKeyB, messageKeyC,
                     enrollment, messageDate, currentDate);
         } else {
             scheduleSingleInfoMessage(messageKey, enrollment, messageDate,
                     currentDate);
         }
     }
 
     void scheduleMultipleInfoMessages(String messageKeyA, String messageKeyB,
                                       String messageKeyC, MessageProgramEnrollment enrollment,
                                       Date messageDate, Date currentDate) {
         // Return existing message definitions
         MessageDefinition messageDefinitionA = this
                 .getMessageDefinition(messageKeyA);
         MessageDefinition messageDefinitionB = this
                 .getMessageDefinition(messageKeyB);
         MessageDefinition messageDefinitionC = this
                 .getMessageDefinition(messageKeyC);
 
         // TODO: Assumes recipient is person in enrollment
         Integer messageRecipientId = enrollment.getPersonId();
 
         // Expecting message date to already be preference adjusted
         // Determine dates for second and third messages
         Calendar calendar = getCalendarWithDate(messageDate);
         calendar.add(Calendar.DATE, 2);
         Date messageDateB = calendar.getTime();
         calendar.add(Calendar.DATE, 2);
         Date messageDateC = calendar.getTime();
 
         MessageDefDate messageA = new MessageDefDate(messageDefinitionA,
                 messageDate);
         MessageDefDate messageB = new MessageDefDate(messageDefinitionB,
                 messageDateB);
         MessageDefDate messageC = new MessageDefDate(messageDefinitionC,
                 messageDateC);
         MessageDefDate[] messageDefDates = {messageA, messageB, messageC};
 
         // Cancel any unsent messages for the same enrollment and not matching
         // the messages to schedule
         this.removeUnsentMessages(messageRecipientId, enrollment,
                 messageDefDates);
 
         // Create new scheduled message (with pending attempt) for all 3
         // messages, for this enrollment, if no matching message already exist
         this.createScheduledMessage(messageRecipientId, messageDefinitionA,
                 enrollment, messageDate, currentDate);
         this.createScheduledMessage(messageRecipientId, messageDefinitionB,
                 enrollment, messageDateB, currentDate);
         this.createScheduledMessage(messageRecipientId, messageDefinitionC,
                 enrollment, messageDateC, currentDate);
     }
 
     void scheduleSingleInfoMessage(String messageKey,
                                    MessageProgramEnrollment enrollment, Date messageDate,
                                    Date currentDate) {
 
         // Return existing message definition
         MessageDefinition messageDefinition = this
                 .getMessageDefinition(messageKey);
 
         // TODO: Assumes recipient is person in enrollment
         Integer messageRecipientId = enrollment.getPersonId();
 
         // Expecting message date to already be preference adjusted
 
         // Cancel any unsent messages for the same enrollment and not matching
         // the message to schedule
         this.removeUnsentMessages(messageRecipientId, enrollment,
                 messageDefinition, messageDate);
 
         // Create new scheduled message (with pending attempt) for enrollment
         // if none matching already exist
         this.createScheduledMessage(messageRecipientId, messageDefinition,
                 enrollment, messageDate, currentDate);
     }
 
     public ScheduledMessage scheduleCareMessage(String messageKey,
                                                 MessageProgramEnrollment enrollment, Date messageDate,
                                                 boolean userPreferenceBased, String care, Date currentDate) {
         // Return existing message definition
         MessageDefinition messageDefinition = this
                 .getMessageDefinition(messageKey);
 
         // TODO: Assumes recipient is person in enrollment
         Integer messageRecipientId = enrollment.getPersonId();
 
         // Create new scheduled message (with pending attempt) for enrollment
         // Does not check if one already exists
         return this.createCareScheduledMessage(messageRecipientId,
                 messageDefinition, enrollment, messageDate, care,
                 userPreferenceBased, currentDate);
     }
 
     private MessageDefinition getMessageDefinition(String messageKey) {
         MessageDefinition messageDefinition = motechService()
                 .getMessageDefinition(messageKey);
         if (messageDefinition == null) {
             log.error("Invalid message key for message definition: "
                     + messageKey);
         }
         return messageDefinition;
     }
 
     protected void removeUnsentMessages(Integer recipientId,
                                         MessageProgramEnrollment enrollment,
                                         MessageDefDate[] messageDefDates) {
         // Get Messages matching the recipient, enrollment, and status, but
         // not matching the list of message definitions and message dates
         List<Message> unsentMessages = motechService().getMessages(recipientId,
                 enrollment, messageDefDates, MessageStatus.SHOULD_ATTEMPT);
         log.debug("Unsent messages found during scheduling: "
                 + unsentMessages.size());
 
         for (Message unsentMessage : unsentMessages) {
             unsentMessage.setAttemptStatus(MessageStatus.CANCELLED);
             motechService().saveMessage(unsentMessage);
 
             log.debug("Message cancelled to schedule new: Id: "
                     + unsentMessage.getId());
         }
     }
 
     protected void removeUnsentMessages(Integer recipientId,
                                         MessageProgramEnrollment enrollment,
                                         MessageDefinition messageDefinition, Date messageDate) {
         // Get Messages matching the recipient, enrollment, and status, but
         // not matching the message definition and message date
         List<Message> unsentMessages = motechService().getMessages(recipientId,
                 enrollment, messageDefinition, messageDate,
                 MessageStatus.SHOULD_ATTEMPT);
         log.debug("Unsent messages found during scheduling: "
                 + unsentMessages.size());
 
         for (Message unsentMessage : unsentMessages) {
             unsentMessage.setAttemptStatus(MessageStatus.CANCELLED);
             motechService().saveMessage(unsentMessage);
 
             log.debug("Message cancelled to schedule new: Id: "
                     + unsentMessage.getId());
         }
     }
 
     public void removeUnsentMessages(List<ScheduledMessage> scheduledMessages) {
         for (ScheduledMessage scheduledMessage : scheduledMessages) {
             for (Message unsentMessage : scheduledMessage.getMessageAttempts()) {
                 if (MessageStatus.SHOULD_ATTEMPT == unsentMessage
                         .getAttemptStatus()) {
 
                     unsentMessage.setAttemptStatus(MessageStatus.CANCELLED);
                     motechService().saveMessage(unsentMessage);
 
                     log
                             .debug("Message cancelled: Id: "
                                     + unsentMessage.getId());
                 }
             }
         }
     }
 
     public void addMessageAttempt(ScheduledMessage scheduledMessage,
                                   Date attemptDate, Date maxAttemptDate, boolean userPreferenceBased,
                                   Date currentDate) {
         MessageDefinition messageDefinition = scheduledMessage.getMessage();
         Person recipient = personService.getPerson(scheduledMessage
                 .getRecipientId());
 
         Date adjustedMessageDate = adjustCareMessageDate(recipient,
                 attemptDate, userPreferenceBased, currentDate);
         // Prevent scheduling reminders too far in future
         // Only schedule one reminder ahead
         if (!adjustedMessageDate.after(maxAttemptDate)) {
             Message message = messageDefinition.createMessage(scheduledMessage);
             message.setAttemptDate(attemptDate);
             scheduledMessage.getMessageAttempts().add(message);
 
             if (log.isDebugEnabled()) {
                 log.debug("Added ScheduledMessage Attempt: recipient: "
                         + scheduledMessage.getRecipientId() + ", message key: "
                         + messageDefinition.getMessageKey() + ", date: "
                         + adjustedMessageDate);
             }
 
             motechService().saveScheduledMessage(scheduledMessage);
         }
     }
 
     public void verifyMessageAttemptDate(ScheduledMessage scheduledMessage,
                                          boolean userPreferenceBased, Date currentDate) {
         Person recipient = personService.getPerson(scheduledMessage
                 .getRecipientId());
 
         List<Message> messages = scheduledMessage.getMessageAttempts();
         if (!messages.isEmpty()) {
             Message recentMessage = messages.get(0);
             if (recentMessage.getAttemptStatus() == MessageStatus.SHOULD_ATTEMPT) {
                 Date attemptDate = recentMessage.getAttemptDate();
                 // Check if current message date is valid for user
                 // preferences or blackout in case these have changed
                 if (userPreferenceBased) {
                     attemptDate = findPreferredMessageDate(recipient,
                             attemptDate, currentDate, true);
                 } else {
                     attemptDate = adjustDateForBlackout(attemptDate);
                 }
                 if (!attemptDate.equals(recentMessage.getAttemptDate())) {
                     // Recompute from original scheduled message date
                     // Allows possibly adjusting to an earlier week or day
                     Date adjustedMessageDate = adjustCareMessageDate(recipient,
                             scheduledMessage.getScheduledFor(),
                             userPreferenceBased, currentDate);
 
                     if (log.isDebugEnabled()) {
                         log.debug("Updating message id="
                                 + recentMessage.getId() + " date from="
                                 + recentMessage.getAttemptDate() + " to="
                                 + adjustedMessageDate);
                     }
 
                     recentMessage.setAttemptDate(adjustedMessageDate);
                     scheduledMessage.getMessageAttempts().set(0, recentMessage);
                     motechService().saveScheduledMessage(scheduledMessage);
                 }
             }
         }
     }
 
     public void removeAllUnsentMessages(MessageProgramEnrollment enrollment) {
         List<Message> unsentMessages = motechService().getMessages(enrollment,
                 MessageStatus.SHOULD_ATTEMPT);
         log.debug("Unsent messages found to cancel: " + unsentMessages.size()
                 + ", for enrollment: " + enrollment.getId());
 
         for (Message unsentMessage : unsentMessages) {
             unsentMessage.setAttemptStatus(MessageStatus.CANCELLED);
             motechService().saveMessage(unsentMessage);
 
             log.debug("Message cancelled: Id: " + unsentMessage.getId());
         }
     }
 
     public Date determineUserPreferredMessageDate(Integer recipientId,
                                                   Date messageDate) {
         Person recipient = personService.getPerson(recipientId);
 
         return findPreferredMessageDate(recipient, messageDate, null,
                 false);
     }
 
     private void createScheduledMessage(Integer recipientId,
                                         MessageDefinition messageDefinition,
                                         MessageProgramEnrollment enrollment, Date messageDate,
                                         Date currentDate) {
         List<ScheduledMessage> scheduledMessages = motechService()
                 .getScheduledMessages(recipientId, messageDefinition,
                         enrollment, messageDate);
 
         // Add scheduled message and message attempt is none matching exists
         if (scheduledMessages.isEmpty()) {
             if (log.isDebugEnabled()) {
                 log.debug("Creating ScheduledMessage: recipient: "
                         + recipientId + ", enrollment: " + enrollment.getId()
                         + ", message key: " + messageDefinition.getMessageKey()
                         + ", date: " + messageDate);
             }
 
             ScheduledMessage scheduledMessage = new ScheduledMessage(messageDate, recipientId, messageDefinition, enrollment);
             Message message = messageDefinition.createMessage(scheduledMessage);
             message.setAttemptDate(messageDate);
             scheduledMessage.getMessageAttempts().add(message);
 
             motechService().saveScheduledMessage(scheduledMessage);
         } else {
             if (scheduledMessages.size() > 1 && log.isWarnEnabled()) {
                 log.warn("Multiple matching scheduled messages: recipient: "
                         + recipientId + ", enrollment: " + enrollment.getId()
                         + ", message key: " + messageDefinition.getMessageKey()
                         + ", date: " + messageDate);
             }
             // Add message attempt to existing scheduled message if not exist
             boolean matchFound = false;
             ScheduledMessage scheduledMessage = scheduledMessages.get(0);
             for (Message message : scheduledMessage.getMessageAttempts()) {
                 if ((MessageStatus.SHOULD_ATTEMPT == message.getAttemptStatus()
                         || MessageStatus.ATTEMPT_PENDING == message
                         .getAttemptStatus()
                         || MessageStatus.DELIVERED == message
                         .getAttemptStatus() || MessageStatus.REJECTED == message
                         .getAttemptStatus())
                         && messageDate.equals(message.getAttemptDate())) {
                     matchFound = true;
                     break;
                 }
             }
             if (!matchFound && !currentDate.after(messageDate)) {
                 if (log.isDebugEnabled()) {
                     log.debug("Creating Message: recipient: " + recipientId
                             + ", enrollment: " + enrollment.getId()
                             + ", message key: "
                             + messageDefinition.getMessageKey() + ", date: "
                             + messageDate);
                 }
 
                 Message message = messageDefinition
                         .createMessage(scheduledMessage);
                 message.setAttemptDate(messageDate);
                 scheduledMessage.getMessageAttempts().add(message);
 
                 motechService().saveScheduledMessage(scheduledMessage);
             }
         }
     }
 
     private ScheduledMessage createCareScheduledMessage(Integer recipientId,
                                                         MessageDefinition messageDefinition,
                                                         MessageProgramEnrollment enrollment, Date messageDate, String care,
                                                         boolean userPreferenceBased, Date currentDate) {
         ScheduledMessage scheduledMessage = new ScheduledMessage(messageDate, recipientId, messageDefinition, enrollment);
         // Set care field on scheduled message (not set on informational
         // messages)
         scheduledMessage.setCare(care);
 
         Person recipient = personService.getPerson(recipientId);
         Date adjustedMessageDate = adjustCareMessageDate(recipient,
                 messageDate, userPreferenceBased, currentDate);
 
         Message message = messageDefinition.createMessage(scheduledMessage);
         message.setAttemptDate(adjustedMessageDate);
         scheduledMessage.getMessageAttempts().add(message);
 
         if (log.isDebugEnabled()) {
             log.debug("Creating ScheduledMessage: recipient: " + recipientId
                     + ", enrollment: " + enrollment.getId() + ", message key: "
                     + messageDefinition.getMessageKey() + ", date: "
                     + adjustedMessageDate);
         }
 
         return motechService().saveScheduledMessage(scheduledMessage);
     }
 
     Date adjustCareMessageDate(Person person, Date messageDate,
                                boolean userPreferenceBased, Date currentDate) {
         Date adjustedDate = verifyFutureDate(messageDate);
         if (userPreferenceBased) {
             adjustedDate = findPreferredMessageDate(person, adjustedDate,
                     currentDate, true);
         } else {
             adjustedDate = adjustDateForBlackout(adjustedDate);
         }
         return adjustedDate;
     }
 
     Date verifyFutureDate(Date messageDate) {
         Calendar calendar = Calendar.getInstance();
         if (calendar.getTime().after(messageDate)) {
             // If date in past, return date 10 minutes in future
             calendar.add(Calendar.MINUTE, 10);
             return calendar.getTime();
         }
         return messageDate;
     }
 
     public TaskDefinition updateAllMessageProgramsState(Integer batchSize, Long batchPreviousId) {
         List<MessageProgramEnrollment> activeEnrollments = motechService().getActiveMessageProgramEnrollments(null, null, null, batchPreviousId, batchSize);
         Date currentDate = new Date();
         for (MessageProgramEnrollment enrollment : activeEnrollments) {
             MessageProgram program = messageProgramService.program(enrollment.getProgram());
             log.debug("MessageProgram Update - Update State: enrollment: " + enrollment.getId());
             program.determineState(enrollment, currentDate);
             batchPreviousId = enrollment.getId();
         }
         if (activeEnrollments.size() < batchSize) {
             batchPreviousId = null;
             log.info("Completed updating all enrollments");
         }
 
         // Update task properties
         TaskDefinition task = schedulerService.getTaskByName(MotechConstants.TASK_MESSAGEPROGRAM_UPDATE);
         if (task != null) {
             Map<String, String> properties = task.getProperties();
             if (batchPreviousId != null) {
                 properties.put(MotechConstants.TASK_PROPERTY_BATCH_PREVIOUS_ID, batchPreviousId.toString());
             } else {
                 properties.remove(MotechConstants.TASK_PROPERTY_BATCH_PREVIOUS_ID);
             }
             schedulerService.saveTask(task);
         }
         return task;
     }
 
     public void updateAllCareSchedules() {
         List<Patient> patients = patientService.getAllPatients();
         log.info("Updating care schedules for " + patients.size() + " patients");
         for (Patient patient : patients) {
             // Adds patient to transaction synchronization using advice
             patientService.savePatient(patient);
         }
     }
 
 
     /* NotificationTask methods start */
 
     public void sendMessages(Date startDate, Date endDate, boolean sendImmediate) {
         try {
             List<Message> shouldAttemptMessages = motechService().getMessages(
                     startDate, endDate, MessageStatus.SHOULD_ATTEMPT);
 
             if (log.isDebugEnabled()) {
                 log
                         .debug("Notification Task executed, Should Attempt Messages found: "
                                 + shouldAttemptMessages.size());
             }
 
             if (!shouldAttemptMessages.isEmpty()) {
                 PatientMessage[] messages = constructPatientMessages(shouldAttemptMessages, sendImmediate);
 
                 if (messages.length > 0) {
                     mobileService.sendPatientMessages(messages);
                 }
             }
         } catch (Exception e) {
             log.error("Failure to send patient messages", e);
         }
     }
 
     private PatientMessage[] constructPatientMessages(List<Message> messages,
                                                       boolean sendImmediate) {
         List<PatientMessage> patientMessages = new ArrayList<PatientMessage>();
 
         for (Message message : messages) {
             PatientMessage patientMessage = constructPatientMessage(message);
             if (patientMessage != null) {
                 if (sendImmediate) {
                     patientMessage.setStartDate(null);
                     patientMessage.setEndDate(null);
                 }
                 patientMessages.add(patientMessage);
                 message.setAttemptStatus(MessageStatus.ATTEMPT_PENDING);
             } else {
                 message.setAttemptStatus(MessageStatus.REJECTED);
             }
             motechService().saveMessage(message);
         }
         return patientMessages.toArray(new PatientMessage[patientMessages.size()]);
     }
 
     private PatientMessage constructPatientMessage(Message message) {
         try {
 
             Long notificationType = message.getSchedule().getMessage().getPublicId();
             Integer recipientId = message.getSchedule().getRecipientId();
             Person person = personService.getPerson(recipientId);
 
             String phoneNumber = getPersonPhoneNumber(person);
 
             // Cancel message if phone number is considered troubled
             if (isPhoneTroubled(phoneNumber)) {
                 if (log.isDebugEnabled()) {
                     log.debug("Attempt to send to Troubled Phone, Phone: " + phoneNumber + ", Notification: " + notificationType);
                 }
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("Scheduled Message, Phone: " + phoneNumber + ", Notification: " + notificationType);
                 }
 
                 String messageId = message.getPublicId();
                 MediaType mediaType = getPersonMediaType(person);
                 String languageCode = getPersonLanguageCode(person);
                 NameValuePair[] personalInfo = new NameValuePair[0];
 
                 Date messageStartDate = message.getAttemptDate();
                 Date messageEndDate = null;
 
                 Patient patient = patientService.getPatient(recipientId);
 
                 if (rctService.isPatientRegisteredAndInTreatmentGroup(patient)) {
                     String motechId = new MotechPatient(patient).getMotechId();
                     log.info("Not creating message because the recipient falls in the RCT Control group. " +
                             "Patient MoTeCH id: " + motechId + " Message ID:" + message.getPublicId());
                     return null;
                 }
                 if (patient != null) {
                     ContactNumberType contactNumberType = getPersonPhoneType(person);
                     String motechId = patient.getPatientIdentifier(
                             MotechConstants.PATIENT_IDENTIFIER_MOTECH_ID)
                             .getIdentifier();
 
                     PatientMessage patientMessage = new PatientMessage();
                     patientMessage.setMessageId(messageId);
                     patientMessage.setPersonalInfo(personalInfo);
                     patientMessage.setPatientNumber(phoneNumber);
                     patientMessage.setPatientNumberType(contactNumberType);
                     patientMessage.setLangCode(languageCode);
                     patientMessage.setMediaType(mediaType);
                     patientMessage.setNotificationType(notificationType);
                     patientMessage.setStartDate(messageStartDate);
                     patientMessage.setEndDate(messageEndDate);
                     patientMessage.setRecipientId(motechId);
                     return patientMessage;
 
                 } else {
                     log
                             .error("Attempt to send message to non-existent Patient: "
                                     + recipientId);
                 }
             }
         } catch (Exception e) {
             log.error("Error creating patient message", e);
         }
         return null;
     }
 
     private boolean sendStaffMessage(String messageId,
                                      NameValuePair[] personalInfo, String phoneNumber,
                                      String languageCode, MediaType mediaType, Long notificationType,
                                      Date messageStartDate, Date messageEndDate,
                                      org.motechproject.ws.Patient[] patients) {
 
         try {
             org.motechproject.ws.MessageStatus messageStatus = mobileService
                     .sendCHPSMessage(messageId, personalInfo, phoneNumber,
                             patients, languageCode, mediaType,
                             notificationType, messageStartDate, messageEndDate);
 
             return messageStatus != org.motechproject.ws.MessageStatus.FAILED;
         } catch (Exception e) {
             log.error("Mobile WS staff message failure", e);
             return false;
         }
     }
 
 
     /* NotificationTask methods end */
 
     /* Factored out methods start */
 
     public String[] getActiveMessageProgramEnrollmentNames(Patient patient) {
         List<MessageProgramEnrollment> enrollments = motechService()
                 .getActiveMessageProgramEnrollments(patient.getPatientId(),
                         null, null, null, null);
 
         List<String> enrollmentNames = new ArrayList<String>();
         for (MessageProgramEnrollment enrollment : enrollments) {
             enrollmentNames.add(enrollment.getProgram());
         }
         return enrollmentNames.toArray(new String[enrollmentNames.size()]);
     }
 
     public void addMessageProgramEnrollment(Integer personId, String program,
                                             Integer obsId) {
         List<MessageProgramEnrollment> enrollments = motechService()
                 .getActiveMessageProgramEnrollments(personId, program, obsId,
                         null, null);
         if (enrollments.size() == 0) {
             MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
             enrollment.setPersonId(personId);
             enrollment.setProgram(program);
             enrollment.setStartDate(new Date());
             enrollment.setObsId(obsId);
             motechService().saveMessageProgramEnrollment(enrollment);
         }
     }
 
     public void removeMessageProgramEnrollment(
             MessageProgramEnrollment enrollment) {
         removeAllUnsentMessages(enrollment);
         if (enrollment.getEndDate() == null) {
             enrollment.setEndDate(new Date());
             motechService().saveMessageProgramEnrollment(enrollment);
         }
     }
 
     private void removeAllMessageProgramEnrollments(Integer personId) {
         List<MessageProgramEnrollment> enrollments = motechService()
                 .getActiveMessageProgramEnrollments(personId, null, null, null,
                         null);
 
         for (MessageProgramEnrollment enrollment : enrollments) {
             removeMessageProgramEnrollment(enrollment);
         }
     }
 
     private Obs createNumericValueObs(Date date, Concept concept, Person person,
                                       Location location, Integer value, Encounter encounter, User creator) {
 
         return createNumericValueObs(date, concept, person, location,
                 (double) value, encounter, creator);
     }
 
     private Obs createNumericValueObs(Date date, Concept concept, Person person,
                                       Location location, Double value, Encounter encounter, User creator) {
 
         Obs obs = createObs(date, concept, person, location, encounter, creator);
         obs.setValueNumeric(value);
         return obs;
     }
 
     private Obs createBooleanValueObs(Date date, Concept concept, Person person,
                                       Location location, Boolean value, Encounter encounter, User creator) {
 
         Double doubleValue;
         // Boolean currently stored as Numeric 1 or 0
         if (Boolean.TRUE.equals(value)) {
             doubleValue = 1.0;
         } else {
             doubleValue = 0.0;
         }
         return createNumericValueObs(date, concept, person, location,
                 doubleValue, encounter, creator);
     }
 
     private Obs createDateValueObs(Date date, Concept concept, Person person,
                                    Location location, Date value, Encounter encounter, User creator) {
 
         Obs obs = createObs(date, concept, person, location, encounter, creator);
         obs.setValueDatetime(value);
         return obs;
     }
 
     private Obs createConceptValueObs(Date date, Concept concept, Person person,
                                       Location location, Concept value, Encounter encounter, User creator) {
 
         Obs obs = createObs(date, concept, person, location, encounter, creator);
         obs.setValueCoded(value);
         return obs;
     }
 
     private Obs createTextValueObs(Date date, Concept concept, Person person,
                                    Location location, String value, Encounter encounter, User creator) {
 
         Obs obs = createObs(date, concept, person, location, encounter, creator);
         obs.setValueText(value);
         return obs;
     }
 
     private Obs createObs(Date date, Concept concept, Person person,
                           Location location, Encounter encounter, User creator) {
 
         Obs obs = new Obs();
         obs.setObsDatetime(date);
         obs.setConcept(concept);
         obs.setPerson(person);
         obs.setLocation(location);
         if (encounter != null) {
             obs.setEncounter(encounter);
         }
         if (creator != null) {
             obs.setCreator(creator);
         }
         return obs;
     }
 
     public Patient getPatientByMotechId(String motechId) {
         PatientIdentifierType motechIdType = getPatientIdentifierTypeForMotechId();
         List<PatientIdentifierType> idTypes = new ArrayList<PatientIdentifierType>();
         idTypes.add(motechIdType);
 
         // Parameters are Name, Id, Id type, match exactly boolean
         List<Patient> patients = patientService.getPatients(null, motechId,
                 idTypes, true);
         if (patients.size() > 0) {
             if (patients.size() > 1) {
                 log.warn("Multiple Patients found for Motech ID: " + motechId);
             }
             return patients.get(0);
         }
         return null;
     }
 
     public User getStaffBySystemId(String systemId) {
         return userService.getUserByUsername(systemId);
     }
 
     public String getPersonPhoneNumber(Person person) {
         PersonAttribute phoneNumberAttr = person
                 .getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_NUMBER);
         if (phoneNumberAttr != null
                 && StringUtils.isNotEmpty(phoneNumberAttr.getValue())) {
             return phoneNumberAttr.getValue();
         }
         log
                 .warn("No phone number found for Person id: "
                         + person.getPersonId());
         return null;
     }
 
     private String getPersonLanguageCode(Person person) {
         PersonAttribute languageAttr = person
                 .getAttribute(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE);
         if (languageAttr != null
                 && StringUtils.isNotEmpty(languageAttr.getValue())) {
             return languageAttr.getValue();
         }
         log.debug("No language found for Person id: " + person.getPersonId());
         return null;
     }
 
     private ContactNumberType getPersonPhoneType(Person person) {
         PersonAttribute phoneTypeAttr = person
                 .getAttribute(MotechConstants.PERSON_ATTRIBUTE_PHONE_TYPE);
         if (phoneTypeAttr != null
                 && StringUtils.isNotEmpty(phoneTypeAttr.getValue())) {
             try {
                 return ContactNumberType.valueOf(phoneTypeAttr.getValue());
             } catch (Exception e) {
                 log.error("Unable to parse phone type: "
                         + phoneTypeAttr.getValue() + ", for Person ID:"
                         + person.getPersonId(), e);
             }
         }
         log.debug("No contact number type found for Person id: "
                 + person.getPersonId());
         return null;
     }
 
     private MediaType getPersonMediaType(Person person) {
         PersonAttribute mediaTypeAttr = person.getAttribute(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
         if (mediaTypeAttr != null && StringUtils.isNotEmpty(mediaTypeAttr.getValue())) {
             try {
                 return MediaType.valueOf(mediaTypeAttr.getValue());
             } catch (Exception e) {
                 log.error("Unable to parse media type: " + mediaTypeAttr.getValue() + ", for Person ID:" + person.getPersonId(), e);
             }
         }
         log.debug("No media type found for Person id: " + person.getPersonId());
         return null;
     }
 
     private boolean isPhoneTroubled(String phoneNumber) {
         TroubledPhone troubledPhone = contextService.getMotechService()
                 .getTroubledPhone(phoneNumber);
         Integer maxFailures = getMaxPhoneNumberFailures();
         return maxFailures != null && troubledPhone != null && troubledPhone.getSendFailures() >= maxFailures;
     }
 
     private Integer getMaxPhoneNumberFailures() {
         String troubledPhoneProperty = getTroubledPhoneProperty();
         if (troubledPhoneProperty != null) {
             return Integer.parseInt(troubledPhoneProperty);
         }
         log.error("Troubled Phone Property not found");
         return null;
     }
 
     public Integer getMaxPatientCareReminders() {
         String careRemindersProperty = getPatientCareRemindersProperty();
         if (careRemindersProperty != null) {
             return Integer.parseInt(careRemindersProperty);
         }
         log.error("Patient Care Reminders Property not found");
         return null;
     }
 
     private DayOfWeek getMessageDayOfWeek(Person person) {
         PersonAttribute messageDeliveryDayChosen = person.getAttribute(MotechConstants.PERSON_ATTRIBUTE_DELIVERY_DAY);
         DayOfWeek day = null;
         if (messageDeliveryDayChosen != null && StringUtils.isNotEmpty(messageDeliveryDayChosen.getValue())) {
             try {
                 day = DayOfWeek.valueOf(messageDeliveryDayChosen.getValue());
             } catch (Exception e) {
                 log.error("Unable to parse day of week: " + messageDeliveryDayChosen.getValue()
                         + ", for Person ID:" + person.getPersonId(), e);
             }
         } else {
             log.debug("No day of week found for Person id: "
                     + person.getPersonId());
         }
         return day;
     }
 
     private Date getPersonMessageTimeOfDay(Person person) {
         PersonAttribute timeAttr = person.getAttribute(MotechConstants.PERSON_ATTRIBUTE_DELIVERY_TIME);
         Date time = null;
         if (timeAttr != null && StringUtils.isNotEmpty(timeAttr.getValue())) {
             SimpleDateFormat timeFormat = new SimpleDateFormat(
                     MotechConstants.TIME_FORMAT_DELIVERY_TIME);
             try {
                 time = timeFormat.parse(timeAttr.getValue());
             } catch (Exception e) {
                 log.error("Unable to parse time of day: " + timeAttr.getValue()
                         + ", for Person ID:" + person.getPersonId(), e);
             }
         } else {
             log.debug("No time of day found for Person id: "
                     + person.getPersonId());
         }
         return time;
     }
 
     private DayOfWeek getDefaultPatientDayOfWeek() {
         String dayProperty = getPatientDayOfWeekProperty();
         DayOfWeek day = null;
         try {
             day = DayOfWeek.valueOf(dayProperty);
         } catch (Exception e) {
             log
                     .error("Invalid Patient Day of Week Property: "
                             + dayProperty, e);
         }
         return day;
     }
 
     private Date getDefaultPatientTimeOfDay() {
         String timeProperty = getPatientTimeOfDayProperty();
         SimpleDateFormat timeFormat = new SimpleDateFormat(
                 MotechConstants.TIME_FORMAT_DELIVERY_TIME);
         Date time = null;
         try {
             time = timeFormat.parse(timeProperty);
         } catch (Exception e) {
             log.error("Invalid Patient Time of Day Property: " + timeProperty,
                     e);
         }
         return time;
     }
 
     private Integer getMaxQueryResults() {
         String maxResultsProperty = contextService.getAdministrationService().getGlobalProperty(
                 MotechConstants.GLOBAL_PROPERTY_MAX_QUERY_RESULTS);
         if (maxResultsProperty != null) {
             return Integer.parseInt(maxResultsProperty);
         }
         log.error("Max Query Results Property not found");
         return null;
     }
 
     public Date findPreferredMessageDate(Person person, Date messageDate, Date currentDate, boolean checkInFuture) {
         Calendar calendar = getCalendarWithDate(messageDate);
         setTimeOfDay(person, calendar);
         setDayOfTheWeek(person, currentDate, checkInFuture, calendar);
         return calendar.getTime();
     }
 
     private void setDayOfTheWeek(Person person, Date currentDate, boolean checkInFuture, Calendar calendar) {
         DayOfWeek day = getMessageDayOfWeek(person);
         if (day == null) {
             day = getDefaultPatientDayOfWeek();
         }
         if (day != null) {
             calendar.set(Calendar.DAY_OF_WEEK, day.getCalendarValue());
             if (checkInFuture && calendar.getTime().before(currentDate)) {
                 // Add a week if date in past after setting the day of week
                 calendar.add(Calendar.DATE, 7);
             }
         }
     }
 
     private void setTimeOfDay(Person person, Calendar calendar) {
         Date time = getPersonMessageTimeOfDay(person);
         if (time == null) {
             time = getDefaultPatientTimeOfDay();
         }
         if (time != null) {
             Calendar timeCalendar = getCalendarWithDate(time);
             calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
             calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
         }
         calendar.set(Calendar.SECOND, 0);
     }
 
     private Calendar getCalendarWithDate(Date messageDate) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(messageDate);
         return calendar;
     }
 
     Date adjustTime(Date date, Date time) {
         if (date == null || time == null) {
             return date;
         }
         Calendar calendar = getCalendarWithDate(date);
 
         Calendar timeCalendar = getCalendarWithDate(time);
         calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
         calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
         calendar.set(Calendar.SECOND, 0);
         if (calendar.getTime().before(date)) {
             // Add a day if before original date
             // after setting the time of day
             calendar.add(Calendar.DATE, 1);
         }
         return calendar.getTime();
     }
 
     Date adjustDateForBlackout(Date date) {
         if (date == null) {
             return date;
         }
         Blackout blackout = motechService().getBlackoutSettings();
         if (blackout == null) {
             return date;
         }
         Calendar blackoutCalendar = getCalendarWithDate(date);
 
         adjustForBlackoutStartDate(date, blackout, blackoutCalendar);
         Date blackoutStart = blackoutCalendar.getTime();
 
         setBlackOutTime(blackout.getEndTime(), blackoutCalendar);
 
         if (blackoutCalendar.getTime().before(blackoutStart)) {
             // Add a day if blackout end date before start date after setting time
             blackoutCalendar.add(Calendar.DATE, 1);
         }
         Date blackoutEnd = blackoutCalendar.getTime();
 
         if (date.after(blackoutStart) && date.before(blackoutEnd)) {
             return blackoutEnd;
         }
         return date;
     }
 
     private void adjustForBlackoutStartDate(Date date, Blackout blackout, Calendar blackoutCalendar) {
         setBlackOutTime(blackout.getStartTime(), blackoutCalendar);
 
         if (date.before(blackoutCalendar.getTime())) {
             // Remove a day if blackout start date before the message date
             blackoutCalendar.add(Calendar.DATE, -1);
         }
     }
 
     private void setBlackOutTime(Date blackoutTime, Calendar blackoutCalendar) {
         Calendar timeCalendar = getCalendarWithDate(blackoutTime);
         blackoutCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
         blackoutCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
         blackoutCalendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
     }
 
 
     public Community saveCommunity(Community community) {
         if (community.getCommunityId() == null) {
             community.setCommunityId(identifierGenerator.generateCommunityId());
         }
 
         return motechService().saveCommunity(community);
     }
 
     public Facility saveNewFacility(Facility facility) {
         facility.setFacilityId(identifierGenerator.generateFacilityId());
         return motechService().saveFacility(facility);
     }
 
     public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
         this.identifierGenerator = identifierGenerator;
     }
 
     private Location getGhanaLocation() {
         return locationService.getLocation(
                 MotechConstants.LOCATION_GHANA);
     }
 
     private String getTroubledPhoneProperty() {
         return administrationService.getGlobalProperty(
                 MotechConstants.GLOBAL_PROPERTY_TROUBLED_PHONE);
     }
 
     private String getPatientCareRemindersProperty() {
         return administrationService.getGlobalProperty(
                 MotechConstants.GLOBAL_PROPERTY_CARE_REMINDERS);
     }
 
     private String getPatientDayOfWeekProperty() {
         return administrationService.getGlobalProperty(
                 MotechConstants.GLOBAL_PROPERTY_DAY_OF_WEEK);
     }
 
     private String getPatientTimeOfDayProperty() {
         return administrationService.getGlobalProperty(
                 MotechConstants.GLOBAL_PROPERTY_TIME_OF_DAY);
     }
 
     /* Factored out methods end */
 
     public Facility getFacilityById(Integer facilityId) {
         return motechService().getFacilityById(facilityId);
     }
 
     public Community getCommunityById(Integer communityId) {
         return motechService().getCommunityById(communityId);
     }
 
     public Community getCommunityByPatient(Patient patient) {
         return motechService().getCommunityByPatient(patient);
     }
 
     public Facility getFacilityByPatient(Patient patient) {
         return motechService().facilityFor(patient);
     }
 
     public Date getChildRegistrationDate() {
         return motechService().getConfigurationFor("valid.child.registration.date").asDate();
     }
 
     public Facility getUnknownFacility() {
         return motechService().unknownFacility();
     }
 
     public boolean isValidOutPatientVisitEntry(Integer facilityId, Date visitDate, String serialNumber, Gender sex, Date dob, Boolean newCase, Integer diagnosis) {
         List<GeneralOutpatientEncounter> outPatientEncounters = motechService().getOutPatientVisitEntryBy(facilityId, serialNumber, sex, dob, newCase, diagnosis);
         return hasNoDuplicateEncounter(outPatientEncounters, visitDate);
     }
 
     private boolean hasNoDuplicateEncounter(List<GeneralOutpatientEncounter> outPatientEncounters, final Date visitDate) {
 
         if ((null == outPatientEncounters) || (outPatientEncounters.isEmpty()))
             return true;
 
         boolean exists = CollectionUtils.exists(outPatientEncounters, new Predicate() {
             public boolean evaluate(Object object) {
                 DateUtil dateUtil = new DateUtil();
                 Date otherVisitDate = ((GeneralOutpatientEncounter) object).getVisitDate();
                 return dateUtil.isSameMonth(visitDate, otherVisitDate) && dateUtil.isSameYear(visitDate, otherVisitDate);
             }
         });
         return !exists;
     }
 
     public List<String> getStaffTypes() {
         return motechUserRepository.userTypes().all();
     }
 
     public boolean isValidMotechIdCheckDigit(Integer motechId) {
         if (motechId == null) {
             return false;
         }
         String motechIdString = motechId.toString();
         MotechIdVerhoeffValidator validator = new MotechIdVerhoeffValidator();
         boolean isValid = false;
         try {
             isValid = validator.isValid(motechIdString);
         } catch (Exception ignored) {
         }
         return isValid;
     }
 
     public boolean isValidIdCheckDigit(Integer idWithCheckDigit) {
         if (idWithCheckDigit == null) {
             return false;
         }
         String idWithCheckDigitString = idWithCheckDigit.toString();
         VerhoeffValidator validator = new VerhoeffValidator();
         boolean isValid = false;
         try {
             isValid = validator.isValid(idWithCheckDigitString);
         } catch (Exception ignored) {
         }
         return isValid;
     }
 
     public void setRelationshipService(RelationshipService relationshipService) {
         this.relationshipService = relationshipService;
     }
 
     public void setPatientService(PatientService patientService) {
         this.patientService = patientService;
     }
 
     public void setPersonService(PersonService personService) {
         this.personService = personService;
     }
 
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 
     public void setAuthenticationService(AuthenticationService authenticationService) {
         this.authenticationService = authenticationService;
     }
 
     public void setConceptService(ConceptService conceptService) {
         this.conceptService = conceptService;
     }
 
     public void setLocationService(LocationService locationService) {
         this.locationService = locationService;
     }
 
     public void setObsService(ObsService obsService) {
         this.obsService = obsService;
     }
 
     public void setEncounterService(EncounterService encounterService) {
         this.encounterService = encounterService;
     }
 
     public void setSchedulerService(SchedulerService schedulerService) {
         this.schedulerService = schedulerService;
     }
 
     public void setAdministrationService(AdministrationService administrationService) {
         this.administrationService = administrationService;
     }
 
     public void setRctService(RCTService rctService) {
         this.rctService = rctService;
     }
 
 
     private MotechService motechService() {
         return contextService.getMotechService();
     }
 
     private PatientIdentifierType getPatientIdentifierTypeForMotechId() {
         return PATIENT_IDENTIFIER_MOTECH_ID.getIdentifierType(patientService);
     }
 
     private EncounterType getEncounterType(EncounterTypeEnum encounterType) {
         return encounterType.getEncounterType(encounterService);
     }
 
     private Concept concept(ConceptEnum conceptEnum) {
         return conceptEnum.getConcept(conceptService);
     }
 
     private boolean isNotNull(Object object) {
         return object != null;
     }
 
     public void setMessageProgramService(MessageProgramServiceImpl messageProgramService) {
         this.messageProgramService = messageProgramService;
     }
 
     public MessageProgramServiceImpl getMessageProgramService() {
         return messageProgramService;
     }
 
     public void setPregnancyObservation(PregnancyObservation pregnancyObservation) {
         this.pregnancyObservation = pregnancyObservation;
     }
 
     public void setObservationBean(ObservationBean observationBean) {
         this.observationBean = observationBean;
     }
 
     public void setMotechUserRepository(MotechUserRepository motechUserRepository) {
         this.motechUserRepository = motechUserRepository;
     }
 }
