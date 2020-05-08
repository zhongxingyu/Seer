 package org.motechproject.ghana.national.web.helper;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.PatientAttributes;
 import org.motechproject.ghana.national.web.form.PatientForm;
 import org.motechproject.mrs.model.Attribute;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class PatientHelperTest {
 
     private PatientHelper patientHelper;
 
     @Before
     public void setUp() {
         patientHelper = new PatientHelper();
     }
 
     @Test
     public void shouldConvertPatientFormIntoValueObject() {
         PatientForm createPatientForm = new PatientForm();
         String facilityId = "12345";
         String facilityName = "facilityName";
         String country = "country";
         String region = "region";
         String subDistrict = "subDistrict";
         String district = "district";
         createPatientForm.setAddress("address");
         createPatientForm.setDateOfBirth(new Date());
         createPatientForm.setEstimatedDateOfBirth(true);
         createPatientForm.setFacilityId(facilityId);
         createPatientForm.setFacilityName(facilityName);
         createPatientForm.setRegion(region);
         createPatientForm.setSubDistrict(subDistrict);
         createPatientForm.setDistrict(district);
         createPatientForm.setFacilityId(facilityId);
         createPatientForm.setFacilityId(facilityId);
         createPatientForm.setFirstName("firstNm");
         createPatientForm.setInsured(false);
         createPatientForm.setLastName("lastNm");
         createPatientForm.setMiddleName("middlenm");
         createPatientForm.setPreferredName("preferred");
         createPatientForm.setMotechId("12567");
        createPatientForm.setSex("Male");
         createPatientForm.setNhisExpirationDate(new Date());
         createPatientForm.setParentId("123");
         createPatientForm.setPhoneNumber("1234567890");
 
         Patient patientVO = patientHelper.getPatientVO(createPatientForm, new Facility(new MRSFacility(facilityId, facilityName, country, region, district, subDistrict)));
 
         assertCompareViewAndValueObject(createPatientForm, patientVO);
 
     }
 
     @Test
     public void shouldConvertValueObjectIntoPatientObject() throws ParseException {
         final MRSPerson person = new MRSPerson();
         final MRSPatient mrsPatient = new MRSPatient("12345", person, new MRSFacility("21", "facility", "Ghana", "Region", "district", "Province"));
         person.address("address");
         person.dateOfBirth(new Date());
         person.birthDateEstimated(true);
         person.firstName("firstNm");
         person.lastName("lastNm");
         person.middleName("middlenm");
         person.preferredName("preferred");
        person.gender("Male");
         person.addAttribute(new Attribute(PatientAttributes.INSURED.getAttribute(), "false"));
         person.addAttribute(new Attribute(PatientAttributes.NHIS_NUMBER.getAttribute(), "45326"));
         person.addAttribute(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), "06543987623"));
         person.addAttribute(new Attribute(PatientAttributes.NHIS_EXPIRY_DATE.getAttribute(), new SimpleDateFormat(Constants.PATTERN_YYYY_MM_DD).format(new Date())));
         final Patient patientVO = new Patient(mrsPatient);
         PatientForm patientForm = patientHelper.getPatientForm(patientVO);
 
         assertCompareViewAndValueObject(patientForm, patientVO);
     }
 
     private void assertCompareViewAndValueObject(PatientForm patientView, Patient patientVO) {
         final MRSPatient mrsPatient = patientVO.mrsPatient();
         final MRSPerson person = mrsPatient.getPerson();
         assertThat(patientView.getAddress(), is(equalTo(person.getAddress())));
         assertThat(patientView.getFirstName(), is(equalTo(person.getFirstName())));
         assertThat(patientView.getPreferredName(), is(equalTo(person.getPreferredName())));
         assertThat(patientView.getMiddleName(), is(equalTo(person.getMiddleName())));
         assertThat(patientView.getLastName(), is(equalTo(person.getLastName())));
         assertThat(patientView.getSex(), is(equalTo(person.getGender())));
         assertThat(patientView.getDateOfBirth(), is(equalTo(person.getDateOfBirth())));
         assertThat(patientView.getEstimatedDateOfBirth(), is(equalTo(person.getBirthDateEstimated())));
         final MRSFacility facility = mrsPatient.getFacility();
         assertThat(patientView.getFacilityId(), is(equalTo(facility.getId())));
         assertThat(patientView.getRegion(), is(equalTo(facility.getRegion())));
         assertThat(patientView.getDistrict(), is(equalTo(facility.getCountyDistrict())));
         assertThat(patientView.getSubDistrict(), is(equalTo(facility.getStateProvince())));
         assertThat(patientView.getMotechId(), is(equalTo(mrsPatient.getMotechId())));
         assertThat(patientView.getNhisNumber(), is(equalTo(person.attrValue(PatientAttributes.NHIS_NUMBER.getAttribute()))));
         assertThat(patientView.getPhoneNumber(), is(equalTo(person.attrValue(PatientAttributes.PHONE_NUMBER.getAttribute()))));
         assertThat(patientView.getInsured().toString(), is(equalTo(person.attrValue(PatientAttributes.INSURED.getAttribute()))));
         assertThat(new SimpleDateFormat(Constants.PATTERN_YYYY_MM_DD).format(patientView.getNhisExpirationDate()), is(equalTo(person.attrValue(PatientAttributes.NHIS_EXPIRY_DATE.getAttribute()))));
     }
 
 }
