 /* SVN FILE: $Id: ValidateAddress.java 5340 2012-09-27 14:48:52Z jvuccolo $ */
 
 package edu.psu.iam.cpr.core.util;
 
 import java.util.regex.Pattern;
 
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.TableColumn;
 import edu.psu.iam.cpr.core.database.beans.CampusCs;
 import edu.psu.iam.cpr.core.database.beans.Country;
 import edu.psu.iam.cpr.core.database.tables.AddressesTable;
 import edu.psu.iam.cpr.core.database.tables.CampusCsTable;
 import edu.psu.iam.cpr.core.database.tables.CountryTable;
 import edu.psu.iam.cpr.core.database.types.AddressType;
 import edu.psu.iam.cpr.core.database.types.CprPropertyName;
 import edu.psu.iam.cpr.core.database.types.DocumentType;
 import edu.psu.iam.cpr.core.database.types.StateList;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 /**
 This class provides an implementation of functions that perform address information validation. 
 *
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 *
 * @package edu.psu.iam.cpr.core.util
 * @author $Author: jvuccolo $
 * @version $Rev: 5340 $
 * @lastrevision $Date: 2012-09-27 10:48:52 -0400 (Thu, 27 Sep 2012) $
 */
 public final class ValidateAddress {
 
 	private static final String COUNTRY_CODE = "Country code";
 	private static final String ADDRESS_DOCUMENT_TYPE = "Address type and/or document type";
 	private static final String ADDRESS = "Address";
 	private static final String CAMPUS_CODE = "Campus code";
 	private static final String GROUP_ID = "Group Id";
 	private static final String UPDATED_BY = "Updated by";
 	private static final String RETURN_HISTORY = "Return history";
 	
 	private static final int BUFFER_SIZE = 200;
 
 	/**
 	 * Constructor
 	 */
 	private ValidateAddress() {
 	}
 
 	/**
 	 * Check validity of address parameters for US addresses
 	 * @param db contains the database connection.
 	 * @param localAddress1 contains address line 1 data
 	 * @param localAddress2 contains address line 2 data
 	 * @param localAddress3 contains address line 3 data
 	 * @param localCity contains city data
 	 * @param stateName contains state data
	 * @param postalCode contains the postal code data
 	 * @param usaAddress true if address is us address, false otherwise
 	 * @return true if address is valid; false otherwise
 	 * @throws CprException 
 	 */
 	
 	
 	
 	public static boolean isAddressValid(Database db, String localAddress1, String localAddress2,
 			String localAddress3, String localCity,  String stateName, String localPostalCode, boolean usaAddress ) throws CprException {
 		
 		// at least one of the street address fields must be populated
 		if ((localAddress1 == null || localAddress1.equals("")) &&
 				(localAddress2 == null || localAddress2.equals("")) && 
 				(localAddress3 == null || localAddress3.equals(""))) {
 			return false;
 		}
 		if (localCity == null  || localCity.equals("")) {
 			return false;
 
 		}
 		if (usaAddress) {
 			if (stateName != null && !isValidState(stateName)) {
 				return false;
 			}
 
 			// assume USA zip code for now
 			if (!isValidZipCode(localPostalCode)) {
 				return false;
 			}
 
 		}
 		return true;
 	}
 
 	/**
 	 * This routine will check the input string for a valid campus code format
 	 * and existence in the database
 	 * 
 	 * @param db contains reference to open database connection
 	 * @param campusCode  contains the campus code to validate
 	 * @param campusName - contains a buffer to hold the campus name. Null is allowed if the caller doesn't want the name returned. 
 	 * @param updatedBy - contains the userid issuing the service request
 	 * 
 	 * @return true if campusCode is valid, false otherwise
 	 * @throws CprException 
 	 */
 	public static Long validCampusCode(final Database db, String campusCode,
 			StringBuffer campusName, String updatedBy) throws CprException {
 		CampusCsTable campusData = new CampusCsTable();
 		
 		// Null is valid
 		if (campusCode == null) {
 			return null;
 		}
 		String localCampusCode = campusCode.toUpperCase().trim();
 
 		// Empty string is valid
 		if (localCampusCode.length() == 0) {
 			return null;
 		}
 		
 		// Campus code contains 2 upper case alpha
 		
 		if (Pattern.matches(CprProperties.INSTANCE.getProperties().getProperty(CprPropertyName.CPR_REGEX_CAMPUS_CODE.toString()), 
 				localCampusCode)) {
 			
 			// Get the campus information by using the code against the database.
 			campusData.getCampusInfo(db, localCampusCode, updatedBy);
 			CampusCs bean = campusData.getCampusCsBean();
 			
 			// Store of the campus code key value and the campus name.
 			long code = bean.getCampusCodeKey();
 			if (code != -1L) {
 				if (campusName != null) {
 					campusName.append(bean.getCampus());
 				}
 				return code;
 			}
 		}
 		
 		// An error occurred.  
 		return -1L;
 	}
 
 	/**
 	 * This routine will check the input string for a valid country code format
 	 * and existence in the database.
 	 * 
 	 * @param db contains reference to open database connection
 	 * @param countryCode   contains the country code to validate
 	 * @param countryName contains a buffer to hold the country name. Null is allowed if the caller doesn't want the name returned.
 	 * @param retrievedBy  contains the userid issuing the service request
 	 * 
 	 * @return countryCodeKey if it exists
 	 * -1 otherwise or if countryCode null or blank
 	 * @throws CprException 
 	 */
 	public static long validCountryCode(final Database db, String countryCode,
 			StringBuffer countryName, String retrievedBy) throws CprException {
 		CountryTable countryData = new CountryTable();
 		
 		
 		// Null is valid
 		if (countryCode == null) {
 			return -1L;
 		}
 
 		String localCountryCode = countryCode.toUpperCase().trim();
 		
 		// Empty string is not valid
 		if (localCountryCode.length() == 0) {
 			return -1L;
 		}
 		
 		// Country code contains 3 upper case alpha
 		
 		if (Pattern.matches(CprProperties.INSTANCE.getProperties().getProperty(CprPropertyName.CPR_REGEX_COUNTRY_CODE.toString()), 
 				localCountryCode)) {
 			
 			// Get the country code information from the database.
 			countryData.getCountryInfo(db, localCountryCode, retrievedBy);
 			Country bean = countryData.getCountryBean();
 			
 			long code = bean.getCountryKey();
 			if (code != -1L) {
 				if (countryName != null) {
 					countryName.append(bean.getCountry());
 				}
 				return code;
 			}
 		}
 		return -1L;
 	}
 
 	
 
 	/**
 	 * This routine will check the input string for a valid state
 	 * 
 	 * @param state
 	 *            - contains the state to validate.
 	 * 
 	 * @return true if format is valid, false otherwise
 	 * 
 	 */
 	public static boolean isValidState(String state)  {
 
 		// state is not required
 
 		// Null is valid
 		if (state == null) {
 			return true;
 		}
 
 		String localState = state.toUpperCase().trim();
 
 		// Empty string is valid
 		if (localState.length() == 0) {
 			return true;
 		}
 
 		// state contains 2 alpha
 		if (Pattern.matches(CprProperties.INSTANCE.getProperties().getProperty(CprPropertyName.CPR_REGEX_STATE.toString()), localState)) {
 			try {
 				StateList.valueOf(localState);
 			}
 			catch (IllegalArgumentException e) {
 				return false;
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * This routine will check the input string for a valid postal code
 	 * 
 	 * @param zipCode
 	 *            - contains the postalCode to validate.
 	 * 
 	 * @return true if format is valid, false otherwise
 	 */
 	public static boolean isValidZipCode(String zipCode) {
 
 		// plus4 is not required
 
 		// Null is valid
 		if (zipCode == null) {
 			return true;
 		}
 
 		String localZipCode = zipCode.trim();
 
 		// Empty string is valid
 		if (localZipCode.length() == 0) {
 			return true;
 		}
 
 		// zipCode contains 5 digits + plus4
 		if (Pattern.matches(CprProperties.INSTANCE.getProperties().getProperty(CprPropertyName.CPR_REGEX_POSTAL_CODE.toString()), 
 				localZipCode)) {
 			return true;
 		}
 		
 
 		return false;
 	}
 
 	/**
 	 * Validate the parameters passed to the AddAddress Service
 	 * @param db contain the reference to the open database connection
 	 * @param personId contains the personid
 	 * @param addressTypeString contains the addresstype
 	 * @param documentType contains the document type
 	 * @param address1 contains address line1
 	 * @param address2 contains address line2
 	 * @param address3 contains address line3
 	 * @param updatedBy contains userid or system id of last updater
 	 * @param city contains the city
 	 * @param stateOrProvince contains the state or province
 	 * @param postalCode contains the postal code and plu4
 	 * @param countryCode contains the countrycode to convert to a country code id
 	 * @param campusCode contains a 2 digit campus code
 	 * @return AddressTable
 	 * @throws CprException 
 	 */
 	public static AddressesTable validateAddAddressParameters(final Database db,
 			long personId, String addressTypeString, String documentType,  String updatedBy,
 			String address1, String address2, String address3, String city,
 			String stateOrProvince, String postalCode,  String countryCode,
 			String campusCode) throws 
 			CprException {
 		
 		boolean invalidCountry = false;
 		boolean invalidCampus = false;
 		StringBuffer countryName = new StringBuffer();
 		StringBuffer campusName = new StringBuffer();
 		Long countryCodeId = -1L;
 		Long campusCodeId = -1L;
 		String stateName= null;
 		String provinceName = null;
 		
 		String localAddress1 = (address1 != null) ? address1.trim() : null;
 		String localAddress2 = (address2 != null) ? address2.trim() : null;
 		String localAddress3 = (address3 != null) ? address3.trim() : null;
 		String localCity = (city != null) ? city.trim() : null;
 		String localPostalCode = (postalCode != null) ? postalCode.trim() : null;
 		String localStateOrProvince = (stateOrProvince != null) ? stateOrProvince.trim() : null;
 		String localCampusCode = (campusCode != null) ? campusCode.trim() : null;
 		String localCountryCode = (countryCode != null) ? countryCode.trim() : null;
 		String localUpdatedBy = (updatedBy != null) ? updatedBy.trim() : null; 
 		
 		String localAddressTypeString = null;
 		if (addressTypeString != null) {
 			if (addressTypeString.trim().length() != 0) {
 				localAddressTypeString = addressTypeString.trim().toUpperCase();
 			}
 		}
 		String localDocumentType = null;
 		if (documentType != null) {
 			if (documentType.trim().length() != 0) {
 				localDocumentType = documentType.trim().toUpperCase();
 			}
 		}
 		
 		String dbColumnNames[] = { "ADDRESS1", "ADDRESS2", "ADDRESS3", "CITY",
 				"STATE", "POSTAL_CODE", "PROVINCE", "LAST_UPDATE_BY" };
 		String inputFields[] = { localAddress1, localAddress2, localAddress3, localCity, stateName,
 				localPostalCode, provinceName, localUpdatedBy  };
 		String prettyNames[] = {  "Address line 1", "Address line 2 ",
 				"Address line 3", "City", "State", "Postal code", "Province",
 				"Updated by" };
 				
 		db.getAllTableColumns("ADDRESSES");
 		
 		countryCodeId = validCountryCode(db, localCountryCode, countryName, updatedBy);
 		if (countryCodeId == -1L) {
 			invalidCountry=true;
 		}
 			
 		if (invalidCountry ) {			
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, COUNTRY_CODE);
 		}
 		if (localUpdatedBy == null || localUpdatedBy.length() == 0) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Updated by");
 		}
 		if (localAddressTypeString == null || localAddressTypeString.length() == 0) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Address type");			
 		}
 		
 		// Validate that the address type and document type combination is valid.
 		if (! isValidCombination(localAddressTypeString, localDocumentType)) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, ADDRESS_DOCUMENT_TYPE);
 		}
 		// Verify that the lengths for the individual fields do not exceed the
 		// maximum for the database.
 		db.getAllTableColumns("ADDRESSES");
 		for (int i = 0; i < dbColumnNames.length; ++i) {
 			if (inputFields[i] != null
 					&& inputFields[i].length() > db.getColumn(dbColumnNames[i])
 							.getColumnSize()) {
 				throw new CprException(ReturnType.PARAMETER_LENGTH_EXCEPTION, prettyNames[i]);
 			}
 		}
 		
 		/*	
 		 * 
 		 * IF countryCode USA or assumed USA  set stateName to stateOrProvince
 		 * otherwise set provinceName = stateOrProvince
 		 * 
 		 */
 		if (  localCountryCode.equalsIgnoreCase("USA"))
 		{
 			stateName = localStateOrProvince;
 			if (!isAddressValid(db, localAddress1, localAddress2, localAddress3, localCity, stateName, localPostalCode, true)) {
 				throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, ADDRESS);
 			}
 		}
 		else 
 		{
 			provinceName = localStateOrProvince;
 			if (!isAddressValid(db, localAddress1, localAddress2, localAddress3, localCity, provinceName, localPostalCode, false)) {
 				throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, ADDRESS);
 			}
 		}
 
 		campusCodeId = validCampusCode(db, localCampusCode, campusName, updatedBy);
 		if (campusCodeId != null && campusCodeId.equals(-1L) ) {
 			invalidCampus = true;
 		}
 		
 		if (invalidCampus) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, CAMPUS_CODE);
 			
 		}
 		
 		return new AddressesTable(personId, localAddressTypeString, localDocumentType, null,
 					localUpdatedBy, localAddress1, localAddress2, localAddress3, localCity, stateName,
 					localPostalCode,  provinceName, countryCodeId, campusCodeId, 
 					countryName.toString(), campusName.toString(),localCountryCode);		
 	}
 
 	/**
 	 * Validate the parameters passed to the ArchiveAddress Service
 	 * 
 	 * @param db contains the reference to the open database connection
 	 * @param personId contains the personid
 	 * @param addressTypeString contains the addresstype
 	 * @param documentType contains the document type
 	 * @param groupId contains the groupId
 	 * @param updatedBy contains the user requesting the service
 	 * @return AddressTable
 	 * @throws CprException 
 	 */
 	public static AddressesTable validateArchiveAndSetPrimaryAddressParameters(final Database db,
 			long personId, String addressTypeString, String documentType, Long groupId, String updatedBy)
 			throws CprException {
 				
 		String localUpdatedBy = (updatedBy != null) ? updatedBy.trim() : null; 
 		String localAddressTypeString = null;
 		if (addressTypeString != null) {
 			if (addressTypeString.trim().length() != 0) {
 				localAddressTypeString = addressTypeString.trim().toUpperCase();
 			}
 		}
 		String localDocumentType = null;
 		if (documentType != null) {
 			if (documentType.trim().length() != 0) {
 				localDocumentType = documentType.trim().toUpperCase();
 			}
 		}
 				
 		if (groupId == null) {
     		throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, GROUP_ID);
     	}
 		db.getAllTableColumns("ADDRESSES");
 		if (localUpdatedBy == null || localUpdatedBy.length() == 0) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, UPDATED_BY);
 		}
 		TableColumn c = db.getColumn("LAST_UPDATE_BY");
 		if (localUpdatedBy.length() > c.getColumnSize()) {
 			throw new CprException(ReturnType.PARAMETER_LENGTH_EXCEPTION, "Userid/server principal");
 		}
 		if (localAddressTypeString == null || localAddressTypeString.length() == 0) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Address type");
 		}
 		// Validate that the address type and document type combination is valid.
 		if (! isValidCombination(localAddressTypeString, localDocumentType)) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, ADDRESS_DOCUMENT_TYPE);
 		}
 		return new AddressesTable(personId, localAddressTypeString, localDocumentType, groupId, localUpdatedBy);
 	}
 
 	/**
 	 * Validate the parameters passed to the GetAddress Service
 	 * @param db reference to the open database connection
 	 * @param personId   contains the personid
 	 * @param updatedBy contains the requestor
 	 * @param addressType if specified, will be the type of address to be retrieved.
 	 * @param returnHistory Y/N flag that indicates whether history is to be retrieved or not.
 	 * @return AddressesTable
 	 * @throws CprException 
 	 * 
 	 */
 	public static AddressesTable validateGetAddressParameters(final Database db, long personId,
 			String updatedBy, String addressType, String returnHistory) throws CprException {
 
 		// Trim all of the string values.
 		String localUpdatedBy = (updatedBy != null) ? updatedBy.trim() : null;
 		String localAddressType = null;
 		if (addressType != null) {
 			if (addressType.trim().length() != 0) {
 				localAddressType = addressType.trim().toUpperCase();
 			}
 		}		
 		String localReturnHistory = (returnHistory != null) ? returnHistory.trim() : null;
 		
 		// Validate the updated by parameter.
 		db.getAllTableColumns("ADDRESSES");
 		if (localUpdatedBy == null || localUpdatedBy.length() == 0) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Updated by");
 		}
 		TableColumn c = db.getColumn("LAST_UPDATE_BY");
 		if (localUpdatedBy.length() > c.getColumnSize()) {
 			throw new CprException(ReturnType.PARAMETER_LENGTH_EXCEPTION, "Userid/server principal");
 		}
 		
 		// If an address type was specified, need to validate it.
 		final AddressesTable addressesTable = new AddressesTable();
 		
 		if (addressType != null) {
 				addressesTable.setAddressType(localAddressType);
 		}
 		
 		// Validate the return history parameter.
 		if ((localReturnHistory = Validate.isValidYesNo(returnHistory)) == null) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, RETURN_HISTORY);
 		}
 		addressesTable.setReturnHistoryFlag((localReturnHistory.equals("Y")) ? true : false);
 		
 		return addressesTable;
 	}
 	
 	/**
 	 * Validate the parameters passed to the UpdateAddress Service
 	 * @param db contains the reference to the open database connection
 	 * @param personId contains the personid
 	 * @param addressTypeString contains the addresstype
 	 * @param documentType contains the document type
 	 * @param groupId contains the groupId
 	 * @param address1 contains address line1
 	 * @param address2 contains address line2
 	 * @param address3 contains address line3
 	 * @param updatedBy contains userid or system id of last updater
 	 * @param city contains the city
 	 * @param stateOrProvince contains the state or province
 	 * @param postalCode contains the postal code
 	 * @param countryCode contains the countrycode to convert to a country code id
 	 * @param campusCode contains a 2 digit campus code
 	 * @return AddressTable
 	 * @throws CprException 
 	 */
 	public static AddressesTable validateUpdateAddressParameters(final Database db,
 			long personId, String addressTypeString, String documentType, Long groupId, 
 			String updatedBy,String address1, String address2, String address3, String city,
 			String stateOrProvince, String postalCode, String countryCode,
 			String campusCode) throws 
 			CprException {
 		boolean invalidCountry = false;
 		boolean invalidCampus  = false;
 		StringBuffer countryName = new StringBuffer(BUFFER_SIZE);
 		StringBuffer campusName = new StringBuffer(BUFFER_SIZE);
 		Long countryCodeId = -1L;
 		Long campusCodeId = -1L;
 		String stateName= null;
 		String provinceName = null;
 		
 		db.getAllTableColumns("ADDRESSES");
 		String localAddress1 = (address1 != null) ? address1.trim() : null;
 		String localAddress2 = (address2 != null) ? address2.trim() : null;
 		String localAddress3 = (address3 != null) ? address3.trim() : null;
 		String localCity = (city != null) ? city.trim() : null;
 		String localPostalCode = (postalCode != null) ? postalCode.trim() : null;
 		String localStateOrProvince = (stateOrProvince != null) ? stateOrProvince.trim() : null;
 		String localCampusCode = (campusCode != null) ? campusCode.trim() : null;
 		String localCountryCode = (countryCode != null) ? countryCode.trim() : null;
 		String localUpdatedBy = (updatedBy != null) ? updatedBy.trim() : null;
 		String localAddressTypeString = null;
 		if (addressTypeString != null) {
 			if (addressTypeString.trim().length() != 0) {
 				localAddressTypeString = addressTypeString.trim().toUpperCase();
 			}
 		}
 		String localDocumentType = null;
 		if (documentType != null) {
 			if (documentType.trim().length() != 0) {
 				localDocumentType = documentType.trim().toUpperCase();
 			}
 		}
 		
 		String dbColumnNames[] = { "ADDRESS1", "ADDRESS2", "ADDRESS3", "CITY",
 				"STATE", "POSTAL_CODE", "PROVINCE", "LAST_UPDATE_BY" };
 		String inputFields[] = { localAddress1, localAddress2, localAddress3, localCity, stateName,
 				localPostalCode, provinceName, localUpdatedBy  };
 		String prettyNames[] = {  "Address line 1", "Address line 2 ",
 				"Address line 3", "City", "State", "Postal code", "Province",
 				"Updated by" };
 		
 		countryCodeId = validCountryCode(db, localCountryCode, countryName, updatedBy);
 		if (countryCodeId == -1L) {
 			invalidCountry=true;
 		}
 		if (invalidCountry ) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, COUNTRY_CODE);
 		}
 		if (groupId == null ) {
     		throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, GROUP_ID);
     	}
 		if (localUpdatedBy == null || localUpdatedBy.length() == 0) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Updated by");
 		}
 		if (localAddressTypeString == null || localAddressTypeString.length() == 0) {
 			throw new CprException(ReturnType.NOT_SPECIFIED_EXCEPTION, "Address type");			
 		}
 		
 		// Validate that the address type and document type combination is valid.
 		if (! isValidCombination(localAddressTypeString, localDocumentType)) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, ADDRESS_DOCUMENT_TYPE);
 		}
 		
 		// Verify that the lengths for the individual fields do not exceed the
 		// maximum for the database.
 		db.getAllTableColumns("ADDRESSES");
 		for (int i = 0; i < dbColumnNames.length; ++i) {
 			if (inputFields[i] != null
 					&& inputFields[i].length() > db.getColumn(dbColumnNames[i])
 							.getColumnSize()) {
 				throw new CprException(ReturnType.PARAMETER_LENGTH_EXCEPTION, prettyNames[i]);
 			}
 		}
 		/*	
 		 * If CountryCodeId is 0, no country entered assumed USA
 		 * IF countryCode USA or assumed USA  set stateName to stateOrProvince
 		 * otherwise set provinceName = stateOrProvince
 		 * 
 		 */
 		if (localCountryCode.equalsIgnoreCase("USA"))
 		{
 			stateName = localStateOrProvince;
 			if (!isAddressValid(db,localAddress1, localAddress2, localAddress3, localCity, stateName, localPostalCode,true) ){
 				throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, ADDRESS);
 			}
 		}
 		else 
 		{
 			provinceName = localStateOrProvince;
 			if (!isAddressValid(db,localAddress1, localAddress2, localAddress3, localCity, provinceName, localPostalCode, false)) {
 				throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, ADDRESS);
 			}
 		}
 		
 		campusCodeId = validCampusCode(db, localCampusCode, campusName, updatedBy);
 		if (campusCodeId != null && campusCodeId.equals(-1L) ) {
 			invalidCampus = true;
 		}
 		
 		if (invalidCampus) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, CAMPUS_CODE);
 			
 		}
 		return new AddressesTable(personId, localAddressTypeString, localDocumentType, groupId, 
 					localUpdatedBy, localAddress1, localAddress2, localAddress3, localCity, stateName,
 					localPostalCode,  provinceName, countryCodeId, campusCodeId, 
 					countryName.toString(), campusName.toString(), localCountryCode);
 		
 		
 	}
 	
 	/**
 	 * This routine is used to determine if a valid combination was specified for name type and document type.
 	 * @param addressType contains the name type that was passed in.
 	 * @param documentType contains the document type that was passed in.
 	 * @return will return true if the combination is valid, otherwise it will return false.
 	 */
 	public static boolean isValidCombination(String addressType, String documentType) {
 		
 		AddressType addressEnum = null;
 		DocumentType documentTypeEnum = null;
 		
 		
 		// Convert the nameType string to an enumerated type.
 		try {
 			addressEnum = AddressType.valueOf(addressType);
 		}
 		catch (Exception e) {
 		}
 		
 		// If a document type was specified, attempt to convert it to an enumerated type.
 		if (documentType != null) {
 			try {
 				documentTypeEnum = DocumentType.valueOf(documentType);
 			}
 			catch (Exception e) {
 			}
 		}
 		
 		// If either the document type and/or name type could not have been converted return false.
 		if ((addressType != null && addressEnum == null) ||
 				(documentType != null && documentTypeEnum == null)) {
 			return false;
 		}
 		
 		// If the document type was specified, verify that a address type was specified and that the name type was
 		// DOCUMENTED_ADDRESS
 		if (documentTypeEnum != null) {
 			if (addressEnum == null || addressEnum != AddressType.DOCUMENTED_ADDRESS) {
 				return false;
 			}
 		}
 		else
 		{
 			if (addressEnum != null &&  addressEnum == AddressType.DOCUMENTED_ADDRESS) {
 				return false;
 			}
 		}
 		
 		// Success!
 		return true;
 	}
 }
