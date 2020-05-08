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
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.PersonName;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hospitalcore.HospitalCoreService;
 import org.openmrs.module.hospitalcore.model.PatientSearch;
 import org.openmrs.module.hospitalcore.util.GlobalPropertyUtil;
 import org.openmrs.module.hospitalcore.util.PatientUtils;
 
 public class RegistrationUtils {
 
 	private static Log logger = LogFactory.getLog(RegistrationUtils.class);
 
 	/**
 	 * Parse Date
 	 * 
 	 * @param date
 	 * @return
 	 * @throws ParseException
 	 */
 	public static Date parseDate(String date) throws ParseException {
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 		return sdf.parse(date);
 	}
 
 	/**
 	 * Format date
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static String formatDate(Date date) {
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 		return sdf.format(date);
 	}
 
 	/**
 	 * Generate person name
 	 * 
 	 * @param personName
 	 *            TODO
 	 * @param name
 	 * 
 	 * @return
 	 */
 	public static PersonName getPersonName(PersonName personName, String name) {
 
 		if (personName == null)
 			personName = new PersonName();
 
 		personName.setGivenName("");
 		personName.setMiddleName("");
 		personName.setFamilyName("");
 
 		@SuppressWarnings("deprecation")
 		String fullname = StringUtils.capitaliseAllWords(name).trim();
 		String[] parts = fullname.split(" ");
 		if (parts.length == 1) {
 			personName.setGivenName(parts[0]);
 		} else if (parts.length == 2) {
 			personName.setGivenName(parts[0]);
 			personName.setFamilyName(parts[1]);
 		} else if (parts.length > 2) {
 			personName.setGivenName(parts[0]);
 			personName.setMiddleName(parts[1]);
 
 			String familyName = "";
 			for (int i = 2; i < parts.length; i++) {
 				familyName += parts[i] + " ";
 			}
 			familyName = familyName.trim();
 			personName.setFamilyName(familyName);
 		}
 		personName.setPreferred(true);
 		return personName;
 	}
 
 	/**
 	 * Generate patient identifier
 	 * 
 	 * @param identifier
 	 * @return
 	 */
 	public static PatientIdentifier getPatientIdentifier(String identifier) {
 		Location location = new Location(GlobalPropertyUtil.getInteger(
 				RegistrationConstants.PROPERTY_LOCATION, 1));
 		PatientIdentifierType identType = Context
 				.getPatientService()
 				.getPatientIdentifierType(
 						GlobalPropertyUtil
 								.getInteger(
 										RegistrationConstants.PROPERTY_PATIENT_IDENTIFIER_TYPE,
 										1));
 		PatientIdentifier patientIdentifier = new PatientIdentifier(identifier,
 				identType, location);
 		return patientIdentifier;
 	}
 
 	/**
 	 * Creates a new Patient Identifier: <prefix>YYMMDDhhmmxxx-checkdigit where
 	 * prefix = global_prop (registration.identifier_prefix) YY = two char
 	 * representation of current year e.g. 2009 - 09 MM = current month. e.g.
 	 * January - 1; December - 12 DD = current day of month e.g. 20 hh = hour of
 	 * day e.g. 10PM - 22 mm = minustes e.g. 10:12 - 12 xxx = three random
 	 * digits e.g. from 0 - 999 checkdigit = using the Lunh Algorithm
 	 * 
 	 */
 	public static String getNewIdentifier() {
 		Calendar now = Calendar.getInstance();
 		String shortName = GlobalPropertyUtil.getString(
 				RegistrationConstants.PROPERTY_IDENTIFIER_PREFIX, "");
 		String noCheck = shortName
 				+ String.valueOf(now.get(Calendar.YEAR)).substring(2, 4)
 				+ String.valueOf(now.get(Calendar.MONTH) + 1)
 				+ String.valueOf(now.get(Calendar.DATE))
 				+ String.valueOf(now.get(Calendar.MINUTE))
				//Sagar Bele,Ghanshyam Kumar - 12-12-2012 - Bug #467 [Registration]Duplicate Identifier
				+ String.valueOf(new Random().nextInt(9999));
 		return noCheck + "-" + generateCheckdigit(noCheck);
 	}
 
 	/*
 	 * Using the Luhn Algorithm to generate check digits
 	 * 
 	 * @param idWithoutCheckdigit
 	 * 
 	 * @return idWithCheckdigit
 	 */
 	private static int generateCheckdigit(String input) {
 		int factor = 2;
 		int sum = 0;
 		int n = 10;
 		int length = input.length();
 
 		if (!input.matches("[\\w]+"))
 			throw new RuntimeException("Invalid character in patient id: "
 					+ input);
 		// Work from right to left
 		for (int i = length - 1; i >= 0; i--) {
 			int codePoint = input.charAt(i) - 48;
 			// slight openmrs peculiarity to Luhn's algorithm
 			int accum = factor * codePoint - (factor - 1)
 					* (int) (codePoint / 5) * 9;
 
 			// Alternate the "factor"
 			factor = (factor == 2) ? 1 : 2;
 
 			sum += accum;
 		}
 
 		int remainder = sum % n;
 		return (n - remainder) % n;
 	}
 
 	/**
 	 * Get person address
 	 * 
 	 * @param address
 	 *            TODO
 	 * @param postaladdress           
 	 * @param district
 	 * @param tehsil
 	 * @return
 	 */
 	//26-6-2012 - Marta add postal Addres param. to store the addres in the openmrs person_address table
 		public static PersonAddress getPersonAddress(PersonAddress address, String postalAddress,
 				String district, String tehsil) {
 
 			if (address == null)
 				address = new PersonAddress();
 
 			address.setAddress1(postalAddress);
 			address.setCountyDistrict(district);
 			address.setCityVillage(tehsil);
 
 			return address;
 		}
 
 	/**
 	 * Get person attribute
 	 * 
 	 * @param id
 	 * @param value
 	 * @return
 	 */
 	public static PersonAttribute getPersonAttribute(Integer id, String value) {
 		PersonAttributeType type = Context.getPersonService()
 				.getPersonAttributeType(id);
 		PersonAttribute attribute = new PersonAttribute();
 		attribute.setAttributeType(type);
 		attribute.setValue(value);
 		logger.info(String.format(
 				"Saving new person attribute [name=%s, value=%s]",
 				type.getName(), value));
 		return attribute;
 	}
 
 	/**
 	 * Estimate age using birthdate
 	 * 
 	 * @param birthdate
 	 * @return
 	 * @throws ParseException
 	 */
 	public static String estimateAge(String birthdate) throws ParseException {
 		Date date = RegistrationUtils.parseDate(birthdate);
 		return PatientUtils.estimateAge(date);
 	}
 	
 	/**
 	 * Save common information to patientSearch table to speed up search process
 	 * @param patient
 	 */
 	public static void savePatientSearch(Patient patient){
 		PatientSearch ps = new PatientSearch();
 		String fullname = PatientUtils.getFullName(patient).replace(" ", "");
 		ps.setFullname(fullname);
 		ps.setPatientId(patient.getPatientId());
 		ps.setAge(patient.getAge());
 		ps.setBirthdate(patient.getBirthdate());
 		ps.setFamilyName(patient.getFamilyName());
 		ps.setGender(patient.getGender());
 		ps.setGivenName(patient.getGivenName());
 		ps.setMiddleName(patient.getMiddleName());
 		ps.setIdentifier(patient.getPatientIdentifier().getIdentifier());
 		ps.setPersonNameId(patient.getPersonName().getId());
 		
 		
 		Context.getService(HospitalCoreService.class).savePatientSearch(ps);
 	}
 }
