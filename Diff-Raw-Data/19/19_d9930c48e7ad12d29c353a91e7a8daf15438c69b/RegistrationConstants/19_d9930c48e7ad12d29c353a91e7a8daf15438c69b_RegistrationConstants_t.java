 /**
  *  Copyright 2008 Society for Health Information Systems Programmes, India (HISP India)
  *
  *  This file is part of Registration module.
  *
  *  Registration module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  Registration module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Registration module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 
 package org.openmrs.module.registration.util;
 
 public class RegistrationConstants {
 
 	public static final String MODULE_ID = "registration";
 	public static final String PROPERTY_FORM = MODULE_ID
 			+ ".form.patient.register";
 	public static final String PROPERTY_IDENTIFIER_PREFIX = MODULE_ID
 			+ ".identifier_prefix";
 	public static final String PROPERTY_PATIENT_IDENTIFIER_TYPE = MODULE_ID
 			+ ".patientIdentifierType";
 	public static final String PROPERTY_LOCATION = MODULE_ID + ".location";
 	public static final String PROPERTY_ENCOUNTER_TYPE_REGINIT = MODULE_ID
 			+ ".encounterType.init";
 	public static final String PROPERTY_ENCOUNTER_TYPE_REVISIT = MODULE_ID
 			+ ".encounterType.revisit";
 	public static final String PROPERTY_NUMBER_OF_DATE_VALIDATION = MODULE_ID
 			+ ".numberOfDateValidation";
 	public static final String PROPERTY_REGISTRATION_FEE = MODULE_ID
 			+ ".registrationFee";
 	public static final String PROPERTY_RSBY_NO_OF_PATIENT = MODULE_ID
 	+ ".patientPerRSBY";
 	public static final String PROPERTY_BPL_NO_OF_PATIENT = MODULE_ID
 	+ ".patientPerBPL";
 	public static final String PROPERTY_BLOODBANK_CONCEPT_ID = MODULE_ID
 			+ ".bloodbankConceptId";	
 	public static final String PROPERTY_ORDER_TYPE_ID = MODULE_ID
 	+ ".bloodbankOrderTypeId";	
 
 	// field names
 	public static final String FORM_FIELD_PATIENT_NAME = "patient.name";
 	public static final String FORM_FIELD_PATIENT_IDENTIFIER = "patient.identifier";
 	public static final String FORM_FIELD_PATIENT_BIRTHDATE = "patient.birthdate";
 	public static final String FORM_FIELD_PATIENT_BIRTHDATE_ESTIMATED = "patient.birthdateEstimate";
 	public static final String FORM_FIELD_PATIENT_GENDER = "patient.gender";
 	//26-6-2012 - Marta add constant to store the addres in the openmrs person_address table
 	public static final String FORM_FIELD_PATIENT_ADDRESS_POSTALADDRESS = "patient.address.postalAddress";
 	public static final String FORM_FIELD_PATIENT_ADDRESS_DISTRICT = "patient.address.district";
 	public static final String FORM_FIELD_PATIENT_ADDRESS_TEHSIL = "patient.address.tehsil";
 	public static final String FORM_FIELD_PATIENT_TEMPORARY_ATTRIBUTE = "patient.temporary.attribute";
 	public static final String FORM_FIELD_PATIENT_OPD_WARD = "patient.opdWard";
 	public static final String FORM_FIELD_PATIENT_REFERRED = "patient.referred";
 	public static final String FORM_FIELD_PATIENT_REFERRED_FROM = "patient.referred.from";
 	public static final String FORM_FIELD_PATIENT_REFERRED_REASON = "patient.referred.reason";
 
 	// concept name
 	public static final String CONCEPT_NAME_OPD_WARD = "OPD WARD";
 	public static final String CONCEPT_NAME_PATIENT_REFERRED_TO_HOSPITAL = "PATIENT REFERRED TO HOSPITAL?";
 	public static final String CONCEPT_NAME_REASON_FOR_REFERRAL = "REASON FOR REFERRAL";
 	public static final String CONCEPT_NAME_PATIENT_REFERRED_FROM = "PATIENT REFERRED FROM";
 	public static final String CONCEPT_NEW_PATIENT = "New Patient";
 	public static final String CONCEPT_REVISIT = "Revisit";
	// harsh 2/7/2012 adding property for temp category
	public static final String CONCEPT_NAME_TEMPORARY_CATEGORY = "TEMPORARY CATEGORY";
 
 }
