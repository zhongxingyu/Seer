 package org.motechproject.ghana.national.domain;
 
 public class Constants {
     //security roles
     public static final String SECURITY_ROLE_SUPER = "System Developer";
     public static final String SECURITY_ROLE_PROVIDER = "Provider";
     public static final String SECURITY_ROLE_CALL_CENTER = "Create/Edit MoTeCH Data";
 
     //person attributes
     public static final String PERSON_ATTRIBUTE_TYPE_EMAIL = "Email";
     public static final String PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER = "Phone Number";
     public static final String PERSON_ATTRIBUTE_TYPE_STAFF_TYPE = "Staff Type";
 
     public static final String COUNTRIES = "countries";
     public static final String REGIONS = "regions";
     public static final String DISTRICTS = "districts";
     public static final String PROVINCES = "provinces";
     public static final String FACILITIES = "facilities";
     public static final String EMAIL_SUCCESS = "email_success";
     public static final String EMAIL_FAILURE = "email_failure";
 
     public static final String FORGOT_PASSWORD_SUCCESS = "Your Password is sent via email successfully";
     public static final String FORGOT_PASSWORD_FAILURE = "Your Password is not sent to you via email successfully.Please try again.";
     public static final String FORGOT_PASSWORD_USER_NOT_FOUND = "User Name not found. Please register!!!";
     public static final String FORGOT_PASSWORD_MESSAGE = "message";
 
     //idgen sources
     public static final String IDGEN_SEQ_ID_GEN_PATIENT_ID = "MoTeCH ID Generator";
     public static final String IDGEN_SEQ_ID_GEN_STAFF_ID = "MoTeCH Staff ID Generator";
     public static final String IDGEN_SEQ_ID_GEN_FACILITY_ID = "MoTeCH Facility ID Generator";
 
     //patient identifier types
     public static final String PATIENT_IDENTIFIER_TYPE_FACILITY_ID = "MoTeCH Facility Id";
     public static final String PATIENT_IDENTIFIER_TYPE_PATIENT_ID = "MoTeCH Id";
     public static final String PATIENT_IDENTIFIER_TYPE_STAFF_ID = "MoTeCH Staff Id";
    public static final String PATIENT_GENDER_MALE = "M";
    public static final String PATIENT_GENDER_FEMALE = "F";
 
     public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";

 }
