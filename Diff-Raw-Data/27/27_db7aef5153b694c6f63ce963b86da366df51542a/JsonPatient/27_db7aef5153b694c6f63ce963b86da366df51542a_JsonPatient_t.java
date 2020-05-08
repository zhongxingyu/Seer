 package org.openmrs.module.restmodule;
 
 import java.text.SimpleDateFormat;
 
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonName;
 
 /**
  * Facilitates the encoding of <code>org.openmrs.Patient</org> objects to/from JSON
  */
 public class JsonPatient {
 	
 	/**
 	 * All dates are reported in YYYY-MM-DD format
 	 */
 	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
 	
 	/**
 	 * Convert a patient object into JSON
 	 * 
 	 * @param patient object to marshal into JSON
 	 * @return JSON String version of patient
 	 */
 	public static String encode(Patient patient) {
 		StringBuffer json = new StringBuffer();
 		
 		json.append("{");
 		
 		addOptionalElement(json, "birthdate", dateFormatter.format(patient.getBirthdate()));
 		
 		json.append("\"birthdateEstimated\":\"").append(patient.getBirthdateEstimated()).append("\",");
 		json.append("\"gender\":\"").append(patient.getGender()).append("\",");
 		
 		json.append("\"identifierList\":[");
		boolean first = true;
 		for (PatientIdentifier pid : patient.getIdentifiers()) {
			if (!first)
				json.append(",");
 			json.append("{");
 			if (pid.isPreferred())
 				json.append("\"preferred\":\"1\",");
 			json.append("\"type\":\"").append(pid.getIdentifierType().getName()).append("\",");
 			// TODO: should encode invalid chars in name
 			json.append("\"identifier\":\"").append(pid.getIdentifier()).append("\"");
			json.append("}");
			first = false;
 		}
 		json.append("],");
 		
 		json.append("\"name\":{");
 		PersonName name = patient.getPersonName();
 		addOptionalElement(json, "prefix", name.getPrefix());
 		addOptionalElement(json, "givenName", name.getGivenName());
 		addOptionalElement(json, "middleName", name.getMiddleName());
 		addOptionalElement(json, "familyName", name.getFamilyName());
 		addOptionalElement(json, "familyName2", name.getFamilyName2());
 		addOptionalElement(json, "degree", name.getDegree());
		json.deleteCharAt(json.length()-1); // delete last comma
 		json.append("},");
 		
 		json.append("\"addressList\":[");
		first = true;
 		for (PersonAddress address : patient.getAddresses()) {
			if (!first)
				json.append(",");
 			json.append("{");
 			if (address.getPreferred())
 				json.append("\"preferred\":\"1\",");
 			addOptionalElement(json, "address1", address.getAddress1());
 			addOptionalElement(json, "address2", address.getAddress2());
 			addOptionalElement(json, "cityVillage", address.getCityVillage());
 			addOptionalElement(json, "countyDistrict", address.getCountyDistrict());
 			addOptionalElement(json, "stateProvince", address.getStateProvince());
 			addOptionalElement(json, "country", address.getCountry());
			json.deleteCharAt(json.length()-1); // delete last comma
			json.append("}");
			first = false;
 		}
 		json.append("]");
 		
 		json.append("}");
 		
 		return json.toString();
 	}
 	
 	/**
 	 * Convenience method for rendering JSON elements
 	 * 
 	 * @param json buffer for output
 	 * @param attrName name of element
 	 * @param value the value for the element. if null, then nothing is added to the output buffer
 	 */
 	private static void addOptionalElement(StringBuffer json, String attrName, String value) {
 		if (value == null || "".equals(value))
 			return;
 		json.append("\"");
 		json.append(attrName);
 		json.append("\":\"");
 		json.append(value);
 		json.append("\",");
 	}
 	
 	/**
 	 * Create patient from JSON representation
 	 * 
 	 * @param xml JSON-encoded patient
 	 * @return Patient objected created from details in JSON
 	 */
 	public static Patient decode(String xml) {
 		throw new RuntimeException("Patient decoding not yet implemented");
 	}
 	
 }
