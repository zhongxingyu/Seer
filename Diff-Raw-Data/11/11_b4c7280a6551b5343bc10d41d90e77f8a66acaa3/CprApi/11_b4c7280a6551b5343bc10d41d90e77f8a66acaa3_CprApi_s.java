 package edu.psu.iam.cpr.rest;
 
 import static edu.psu.iam.cpr.core.api.BaseApi.*;
 
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.cxf.jaxrs.ext.MessageContext;
 
 import edu.psu.iam.cpr.core.api.returns.AddressServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.EmailAddressServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.FindPersonServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.NamesServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PersonServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PhoneServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.ServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.UserCommentServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.UseridServiceReturn;
 import edu.psu.iam.cpr.core.database.types.CprServiceName;
 import edu.psu.iam.cpr.core.service.AddAddressImpl;
 import edu.psu.iam.cpr.core.service.AddEmailAddressImpl;
 import edu.psu.iam.cpr.core.service.AddNameImpl;
 import edu.psu.iam.cpr.core.service.AddPersonImpl;
 import edu.psu.iam.cpr.core.service.AddPhoneImpl;
 import edu.psu.iam.cpr.core.service.AddUserCommentImpl;
 import edu.psu.iam.cpr.core.service.AddUseridImpl;
 import edu.psu.iam.cpr.core.service.ArchiveAddressImpl;
 import edu.psu.iam.cpr.core.service.ArchiveEmailAddressImpl;
 import edu.psu.iam.cpr.core.service.ArchiveNameImpl;
 import edu.psu.iam.cpr.core.service.ArchivePersonImpl;
 import edu.psu.iam.cpr.core.service.ArchivePhoneImpl;
 import edu.psu.iam.cpr.core.service.ArchiveUserCommentImpl;
 import edu.psu.iam.cpr.core.service.ArchiveUseridImpl;
 import edu.psu.iam.cpr.core.service.BaseServiceImpl;
 import edu.psu.iam.cpr.core.service.GetAddressImpl;
 import edu.psu.iam.cpr.core.service.GetEmailAddressImpl;
 import edu.psu.iam.cpr.core.service.GetNameImpl;
 import edu.psu.iam.cpr.core.service.GetPersonImpl;
 import edu.psu.iam.cpr.core.service.GetPhoneImpl;
 import edu.psu.iam.cpr.core.service.GetUserCommentsImpl;
 import edu.psu.iam.cpr.core.service.GetUseridImpl;
 import edu.psu.iam.cpr.core.service.SearchForPersonImpl;
 import edu.psu.iam.cpr.core.service.UpdateAddressImpl;
 import edu.psu.iam.cpr.core.service.UpdateEmailAddressImpl;
 import edu.psu.iam.cpr.core.service.UpdatePersonImpl;
 import edu.psu.iam.cpr.core.service.UpdatePhoneImpl;
 import edu.psu.iam.cpr.core.service.UpdateUserCommentImpl;
 import edu.psu.iam.cpr.core.service.returns.AddressReturn;
 import edu.psu.iam.cpr.core.service.returns.EmailAddressReturn;
 import edu.psu.iam.cpr.core.service.returns.NameReturn;
 import edu.psu.iam.cpr.core.service.returns.PersonReturn;
 import edu.psu.iam.cpr.core.service.returns.PhoneReturn;
 import edu.psu.iam.cpr.core.service.returns.UserCommentReturn;
 import edu.psu.iam.cpr.core.service.returns.UseridReturn;
 import edu.psu.iam.cpr.core.util.Utility;
 import edu.psu.iam.cpr.rest.handler.UserPrincipal;
 import edu.psu.iam.cpr.rest.returns.SearchPersonReturn;
 import edu.psu.iam.cpr.rest.returns.UpdatePersonReturn;
 import edu.psu.iam.cpr.rest.returns.WriteApiReturn;
 import edu.psu.iam.cpr.rest.returns.GetAddressReturn;
 import edu.psu.iam.cpr.rest.returns.GetCommentReturn;
 import edu.psu.iam.cpr.rest.returns.GetEmailReturn;
 import edu.psu.iam.cpr.rest.returns.GetNameReturn;
 import edu.psu.iam.cpr.rest.returns.GetPersonReturn;
 import edu.psu.iam.cpr.rest.returns.GetPhoneReturn;
 import edu.psu.iam.cpr.rest.returns.GetUseridReturn;
 import edu.psu.iam.cpr.rest.returns.ResourceMetadata;
 import edu.psu.iam.cpr.rest.returns.ResponseMeta;
 
 /**
  * This is the class that provides the implementation for the CPR RESTful services.
  *  
  * Copyright 2013 The Pennsylvania State University
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
  * @package edu.psu.iam.cpr.rest
  * @author $Author: jvuccolo $
  */
 public class CprApi implements CprApiInterface {
 	
 	/** Message context used to obtain information about the caller of the service */
 	@Context private MessageContext messageContext;
 	
 	/** Contains the URI information pointer */
 	@Context private UriInfo uriInfo;
 	
 	/** Contains the server version of the APIs */
 	private static final String SERVER_VERSION = "1.0";
 	
 	/** 
 	 * 
 	 * Implementation for service SearchForPerson
	 * 
	 * @param principalId The principal requesting the service. Mandatory.
	 * @param password The password for principalId. Mandatory.
 	 * @param requestedBy The user requesting the service. Mandatory.
 	 * @param psuId The Penn State id number of the user to be found. Optional.
 	 * @param userId The userId of the user to be found. Optional.
 	 * @param ssn The SSN of the user to be found. Optional.
 	 * @param firstName The first name of the user to be found. Mandatory.
 	 * @param lastName The last name of the user to be found. Mandatory.
 	 * @param middleName The middle name of the user to be found. Optional.
 	 * @param address1 First line of the street address of the user to be found. Optional.
 	 * @param address2 Second line of the street address of the user to be found. Optional.
 	 * @param address3 Third line of the street address of the user to be found. Optional.
 	 * @param city City of the user to be found. Optional.
 	 * @param state State or province of the user to be found. Optional.
 	 * @param postalCode Zip/Postcode of the user to be found. Optional.
 	 * @param plus4 Extra zipcode information. Optional.
 	 * @param country Country for the street address. Optional. If blank, "USA" is assumed.
 	 * @param dateOfBirth full birth date in format mm/dd/yyyy or partial birth date in format mm/dd
 	 * @param gender Gender for the user to be found. Optional.
 	 * @param rankCutOff The minimum ranking to be considered a positive match. If no value provided, then cut off score will be determined by default cutoff rankings from the standard and international criteria matrices
 	 *  
 	 * @return will return an instance of the HTTP response object.
 	 * 
 	 */
 	public Response searchForPerson(
 			final String requestedBy,
 			final String psuId,
 			final String userId,
 			final String ssn,
 			final String firstName,
 			final String lastName,
 			final String middleName,
 			final String address1,
 			final String address2,
 			final String address3,
 			final String city,
 			final String state,
 			final String postalCode,
 			final String plus4,
 			final String country,
 			final String dateOfBirth,
 			final String gender,
 			final String rankCutOff) {
 		
         final Map<String, Object> otherParameters = new HashMap<String,Object>(17);
         otherParameters.put(PSUID_KEY, psuId);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(SSN_KEY, ssn);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleName);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, state);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(PLUS4_KEY, plus4);
         otherParameters.put(COUNTRY_KEY, country);
         otherParameters.put(DOB_KEY, dateOfBirth);
         otherParameters.put(GENDER_KEY, gender);
         otherParameters.put(RANK_CUTOFF_KEY, rankCutOff);
 
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final SearchForPersonImpl impl = new SearchForPersonImpl();
 		
 		final FindPersonServiceReturn findPersonServiceReturn = (FindPersonServiceReturn) impl.implementService(
 				CprServiceName.FindPerson.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				null,
 				null, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(findPersonServiceReturn.getStatusCode());
 
 		return Response.status(httpStatusCode).entity(
 				new SearchPersonReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), findPersonServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						findPersonServiceReturn)).build();
 	}
 	
 	/**
 	 * Archive Address RESTful Service.
 	 *   
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressType  the type of address to delete
 	 * @param documentType the type of document
 	 * @param groupId the groupId of the address record within the address type
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * 
 	 * @return will return an instance of a REST response object.
 	 * 
 	 */
 	public Response archiveAddress(
 			final String identifierType,
 			final String identifier,
 			final String requestedBy,
 			final String addressType,
 			final String documentType,
 			final String groupId) {
 
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
 		otherParameters.put(ADDRESS_TYPE_KEY, addressType);
 		otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
 		otherParameters.put(GROUP_ID_KEY, Utility.safeConvertStringToLong(groupId));
 
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.ArchiveAddress.toString(), new ArchiveAddressImpl());
 	}
 	
 	/**
 	 * Archive Address RESTful Service.
 	 *  
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param addressKey contains the address key to be deleted.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * 
 	 * @return will return an instance of a REST response object.
 	 * 
 	 */
 	public Response archiveAddressUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String requestedBy,
 			final String addressKey) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 		otherParameters.put(ADDRESS_KEY, addressKey);
 
 		return Response.status(501).header("Status", "501: Not implemented").build();
 	}
 	
 	/**
 	 * Archive Person RESTful API.
 	 * 
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archivePerson(
 			final String identifierType,
 			final String identifier,
 			final String requestedBy) {
 		
 		return execWriteApi(identifierType, identifier, requestedBy, null,
 				CprServiceName.ArchivePerson.toString(), new ArchivePersonImpl());
 	}
 	
 	/**
 	 * Archive Userid RESTful Service
 	 *  
 	 * @param identifierType the type of identifier used to find the person in the CPR. 
 	 * @param identifier the value for the identifier specified in the identifierType argument.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param userid the userid that will be archived.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archiveUserid(
 			final String identifierType,
 			final String identifier,
 			final String userid,
 			final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 		otherParameters.put(USERID_KEY, userid);
 		
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.ArchiveUserid.toString(), new ArchiveUseridImpl());
 	}
 	
 	/**
 	 * Archive User Comment RESTful API.
 	 *  
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param commentKey contains the comment key.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archiveUserCommentUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String commentKey,
 			final String requestedBy) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(COMMENT_KEY, commentKey);
 
 		return Response.status(501).header("Status", "501: Not implemented").build();
 	}
 	
 	/**
 	 * Archive User Comment RESTful API.
 	 *  
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archiveUserComment(
 			final String identifierType,
 			final String identifier,
 			final String userId,
 			final String userCommentType,
 			final String requestedBy) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(USER_COMMENT_TYPE_KEY, userCommentType);
 
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.ArchiveUserComment.toString(), new ArchiveUserCommentImpl());
 	}
 
 	/**
 	 * Archive Email Address RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param emailAddressType contains the type of email address that is being archived.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archiveEmail(
 			final String identifierType,
 			final String identifier,
 			final String emailAddressType,
 			final String requestedBy) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailAddressType);
 
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.ArchiveEmailAddress.toString(), new ArchiveEmailAddressImpl());
 	}
 
 	/**
 	 * Archive Email Address RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param emailAddressKey contains the email address key to be archived.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archiveEmailUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String emailAddressKey,
 			final String requestedBy) {
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddressKey);
 
 		return Response.status(501).header("Status", "501: Not implemented").build();
 	}
 
 	/**
 	 * Archive Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param phoneType the type of phone number that is being archived.
 	 * @param groupId the groupId of the phone record within the phone type
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archivePhone(
 			final String identifierType,
 			final String identifier,
 			final String phoneType,
 			final String groupId,
 			final String requestedBy) {
 
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
 		otherParameters.put(PHONE_TYPE_KEY, phoneType);
 		otherParameters.put(GROUP_ID_KEY, Utility.safeConvertStringToLong(groupId));
 
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.ArchivePhone.toString(), new ArchivePhoneImpl());
 	}
 	
 	/**
 	 * Archive Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param phoneKey contains the phone key to be deleted.
 	 * 
 	 * @return will return a REST response object.
 	 */
 	public Response archivePhoneUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String phoneKey,
 			final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 		otherParameters.put(PHONE_KEY, phoneKey);
 		
 		return Response.status(501).header("Status", "501: Not implemented").build();
 	}
 	/**
 	 * This method is used to delete (archive) a name record.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param nameType contains the type of name to be archived.
 	 * @param documentType will contain the document type if the name type is "DOCUMENT".
 	 * @return will return a standard HTTP response.
 	 */
 	public Response deleteName(
 			final String identifierType,
 			final String identifier,
 			final String nameType,
 			final String documentType,
 			final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
 	    otherParameters.put(NAME_TYPE_KEY, nameType);
 	    otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
 
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.ArchiveName.toString(), new ArchiveNameImpl());
 	}
 	
 	/**
 	 * This method is used to delete (archive) a name record.
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is archiving the name, this person will be an RA agent.
 	 * @param nameKey contains the name key that identifies the record.
 	 * @return will return a standard HTTP response.
 	 */
 	public Response deleteNameUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String nameKey,
 			final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 	    otherParameters.put(NAME_KEY, nameKey);
 
 		return Response.status(501).header("Status", "501: Not implemented").build();
 	}
 
 		
 	/**
 	 * This function provides the implementation for the Add Person RESTful API.
 	 * 
 	 * @param requestedBy the person (userid) who is making the change, this person will be an RA agent.
 	 * @param assignPsuIdFlag a y/n flag to indiciate whether a new PSU ID is to be created for the person.
 	 * @param assignUseridFlag a y/n flag to indicate whether a new userid is to be created for the person.
 	 * @param gender a flag to indicate the person's gender.
 	 * @param dob the person's date of birth, it can either be a full DOB or a partial one (mm/dd).
 	 * @param nameType the type of name that is being added.
 	 * @param nameDocumentType the document type associated with the name.
 	 * @param firstName the first name that is being added.
 	 * @param middleNames the middle name(s) that are being added.
 	 * @param lastName the last name that is being added.
 	 * @param suffix optionally the suffix that is being added.
 	 * @param nickname optionally the nickname that is being added.
 	 * @param addressType the type of address that is being added.
 	 * @param addressDocumentType the document type of the address that is being added.
 	 * @param address1 line number one of the address.
 	 * @param address2 line number two of the address (if it exists).
 	 * @param address3 line number three of the address (if it exists).
 	 * @param city the city of the address.
 	 * @param stateOrProvince the state (if country is USA), or province (otherwise).
 	 * @param postalCode the postal code.
 	 * @param countryCode the ISO country code abbreviation.
 	 * @param campusCode the Penn State campus code for University addresses only.
 	 * @param verifyAddressFlag indicates whether to verify the address or not.
 	 * @param phoneType the type of phone number being added.
 	 * @param phoneNumber the phone number.
 	 * @param extension the extension for the phone number if applicable.
 	 * @param internationalNumber a y/n flag to indicate whether the phone number is an international number.
 	 * @param emailType the type of email address being added.
 	 * @param emailAddress the email address.
 	 * @param affiliation the affiliation to be added.
 	 * @param ssn contains the user's SSN.
 	 * 
 	 * @return HTTP response object.
 	 */
 	public Response addPerson(
 			final String requestedBy,
 			final String assignPsuIdFlag,
 			final String assignUseridFlag,
 			final String gender,
 			final String dob,
 			final String nameType,
 			final String nameDocumentType,
 			final String firstName,
 			final String middleNames,
 			final String lastName,
 			final String suffix,
 			final String nickname, 
 			final String addressType,
 			final String addressDocumentType,
 			final String address1,
 			final String address2,
 			final String address3,
 			final String city,
 			final String stateOrProvince,
 			final String postalCode,
 			final String countryCode,
 			final String campusCode,
 			final String verifyAddressFlag,
 			final String phoneType,
 			final String phoneNumber,
 			final String extension,
 			final String internationalNumber,
 			final String emailType,
 			final String emailAddress,
 			final String affiliation,
 			final String ssn) {
 		
 		final String doFindPersonFlag = "Y";
 
         final Map<String, Object> otherParameters = new HashMap<String, Object>(31);
         otherParameters.put(DO_FIND_PERSON_KEY, doFindPersonFlag);
         otherParameters.put(ASSIGN_PSU_ID_FLAG_KEY, assignPsuIdFlag);
         otherParameters.put(ASSIGN_USERID_FLAG_KEY, assignUseridFlag);
         otherParameters.put(GENDER_KEY, gender);
         otherParameters.put(DOB_KEY, dob);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(NAME_DOCUMENT_TYPE_KEY, nameDocumentType);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleNames);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(SUFFIX_KEY, suffix);
         otherParameters.put(NICKNAME_KEY, nickname);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(ADDRESS_DOCUMENT_TYPE_KEY, addressDocumentType);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
         otherParameters.put(AFFILIATION_KEY, affiliation);
         otherParameters.put(SSN_KEY, ssn);
 
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final AddPersonImpl impl = new AddPersonImpl();
 		
 		final PersonServiceReturn personServiceReturn = (PersonServiceReturn) impl.implementService(
 				CprServiceName.AddPerson.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				null,
 				null, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(personServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		final PersonReturn personReturn = personServiceReturn.getPersonReturn();
 		if (personReturn != null) {
 			personReturn.setUri(Utility.constructUri(path, Long.valueOf(personReturn.getPersonId()).toString()));
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new UpdatePersonReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), personServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						personServiceReturn)).build();
 	}
 
 	/**
 	 * This function provides the implementation for the Update Person RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the person, this person will be an RA agent.
 	 * @param assignPsuIdFlag a y/n flag to indiciate whether a new PSU ID is to be created for the person.
 	 * @param assignUseridFlag a y/n flag to indicate whether a new userid is to be created for the person.
 	 * @param gender a flag to indicate the person's gender.
 	 * @param dob the person's date of birth, it can either be a full DOB or a partial one (mm/dd).
 	 * @param nameType the type of name that is being added.
 	 * @param nameDocumentType the document type associated with the name.
 	 * @param firstName the first name that is being added.
 	 * @param middleNames the middle name(s) that are being added.
 	 * @param lastName the last name that is being added.
 	 * @param suffix optionally the suffix that is being added.
 	 * @param nickname optionally the nickname that is being added.
 	 * @param addressType the type of address that is being added.
 	 * @param addressDocumentType the document type of the address that is being added.
 	 * @param addressGroupId the group id associated addressType and addressDocumentType being updated.
 	 * @param address1 line number one of the address.
 	 * @param address2 line number two of the address (if it exists).
 	 * @param address3 line number three of the address (if it exists).
 	 * @param city the city of the address.
 	 * @param stateOrProvince the state (if country is USA), or province (otherwise).
 	 * @param postalCode the postal code.
 	 * @param countryCode the ISO country code abbreviation.
 	 * @param campusCode the Penn State campus code for University addresses only.
 	 * @param verifyAddressFlag indicates whether to verify the address or not.
 	 * @param phoneType the type of phone number being added.
 	 * @param phoneGroupId the group id associated with the record being updated.
 	 * @param phoneNumber the phone number.
 	 * @param extension the extension for the phone number if applicable.
 	 * @param internationalNumber a y/n flag to indicate whether the phone number is an international number.
 	 * @param emailType the type of email address being added.
 	 * @param emailAddress the email address.
 	 * @param affiliation the affiliation to be added.
 	 * @param ssn the user's social security number.
 	 * 
 	 * @return HTTP response object. 
 	 */
 	public Response updatePerson(
 			final String identifierType,
 			final String identifier,
 			final  String requestedBy,
 			final String assignPsuIdFlag,
 			final String assignUseridFlag,
 			final String gender,
 			final String dob,
 			final String nameType,
 			final String nameDocumentType,
 			final String firstName,
 			final String middleNames,
 			final String lastName,
 			final String suffix,
 			final String nickname,  
 			final String addressType,
 			final String addressDocumentType,
 			final String addressGroupId,
 			final String address1,
 			final String address2,
 			final String address3,
 			final String city,
 			final String stateOrProvince,
 			final String postalCode,
 			final String countryCode,
 			final String campusCode,
 			final String verifyAddressFlag,
 			final String phoneType,
 			final String phoneGroupId,
 			final String phoneNumber,
 			final String extension,
 			final String internationalNumber,
 			final String emailType,
 			final String emailAddress,
 			final String affiliation,
 			final String ssn) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(30);
         otherParameters.put(ASSIGN_PSU_ID_FLAG_KEY, assignPsuIdFlag);
         otherParameters.put(ASSIGN_USERID_FLAG_KEY, assignUseridFlag);
         otherParameters.put(GENDER_KEY, gender);
         otherParameters.put(DOB_KEY, dob);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(NAME_DOCUMENT_TYPE_KEY, nameDocumentType);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleNames);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(SUFFIX_KEY, suffix);
         otherParameters.put(NICKNAME_KEY, nickname);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(ADDRESS_DOCUMENT_TYPE_KEY, addressDocumentType);
         otherParameters.put(ADDRESS_GROUP_ID_KEY, addressGroupId);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(PHONE_GROUP_ID_KEY, phoneGroupId);
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
         otherParameters.put(AFFILIATION_KEY, affiliation);
         otherParameters.put(SSN_KEY, ssn);
 
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final UpdatePersonImpl impl = new UpdatePersonImpl();
 		
 		final PersonServiceReturn personServiceReturn = (PersonServiceReturn) impl.implementService(
 				CprServiceName.UpdatePerson.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				null,
 				null, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(personServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		final PersonReturn personReturn = personServiceReturn.getPersonReturn();
 		if (personReturn != null) {
 			personReturn.setUri(Utility.constructUri(path, Long.valueOf(personReturn.getPersonId()).toString()));
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new UpdatePersonReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), personServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						personServiceReturn)).build();
 	}
 	
 	/**
 	 * Add Userid RESTful service.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is adding the userid, this person will be an RA agent.
 	 * 
 	 * @return will return a response object.
 	 */
 	public Response addUserid(
 			  final String identifier,
 			  final String requestedBy) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final AddUseridImpl impl = new AddUseridImpl();
 		
 		final UseridServiceReturn useridServiceReturn = (UseridServiceReturn) impl.implementService(
 				CprServiceName.AddUserid.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				"PERSON_ID",
 				identifier, 
 				null);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(useridServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		if (useridServiceReturn.getUseridReturnRecord() != null) {
 			for (UseridReturn n: useridServiceReturn.getUseridReturnRecord()) {
 				n.setUri(Utility.constructUri(path, n.getUserid()));
 			}
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new GetUseridReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), useridServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						useridServiceReturn)).build();
 	}
 	
 	/**
 	 * Add Address RESTful API.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param addressType the type of address to add
 	 * @param documentType the type of document
 	 * @param address1 line 1 of address
 	 * @param address2 line 2 of address
 	 * @param address3 line 3 of address
 	 * @param city the city of the address
 	 * @param stateOrProvince the state of the address, if US address; Province for non-US addresses
 	 * @param postalCode the postal code of the address. For US address, may include plus4 code.
 	 * @param countryCode the three character country code.
 	 * @param campusCode the two character campus code.
 	 * @param verifyAddressFlag flag to control address validation
 	 * 
 	 * @return will return a response object.
 	 */
 	public Response addAddress(
 			  final String identifierType,
 			  final String identifier,
 			  final String requestedBy,
 			  final String addressType,
 			  final String documentType,
 			  final String address1,
 			  final String address2,
 			  final String address3,
 			  final String city,
 			  final String stateOrProvince,
 			  final String postalCode,
 			  final String countryCode,
 			  final String campusCode,
 			  final String verifyAddressFlag) {
 		
         final Map<String, Object> otherParameters = new HashMap<String,Object>(11);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
 
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.AddAddress.toString(), new AddAddressImpl());
 	}
 
 	/**
 	 * Update Address RESTful API.
 	 *
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is adding the address, this person will be an RA agent.
 	 * @param addressType the type of address to update
 	 * @param documentType the type of document
 	 * @param groupId the groupId of the phone record within the phone type
 	 * @param address1 line 1 of address
 	 * @param address2 line 2 of address
 	 * @param address3 line 3 of address
 	 * @param city the city of the address
 	 * @param stateOrProvince the state of the address, if US address; Province for non-US addresses
 	 * @param postalCode the postal code of the address. For US address, may include plus4 code.
 	 * @param countryCode the three character country code.
 	 * @param campusCode the two character campus code.
 	 * @param verifyAddressFlag flag to control address validation
 	 * 
 	 * @return will return a response object.
 	 */
 	public Response updateAddress(
 			  final String identifierType,
 			  final String identifier,
 			  final String requestedBy,
 			  final String addressType,
 			  final String documentType,
 			  final String groupId,
 			  final String address1,
 			  final String address2,
 			  final String address3,
 			  final String city,
 			  final String stateOrProvince,
 			  final String postalCode,
 			  final String countryCode,
 			  final String campusCode,
 			  final String verifyAddressFlag) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(12);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(GROUP_ID_KEY, Utility.safeConvertStringToLong(groupId));
         otherParameters.put(ADDRESS1_KEY, address1);
         otherParameters.put(ADDRESS2_KEY, address2);
         otherParameters.put(ADDRESS3_KEY, address3);
         otherParameters.put(CITY_KEY, city);
         otherParameters.put(STATE_KEY, stateOrProvince);
         otherParameters.put(POSTALCODE_KEY, postalCode);
         otherParameters.put(COUNTRY_KEY, countryCode);
         otherParameters.put(CAMPUS_KEY, campusCode);
         otherParameters.put(VERIFY_ADDRESS_FLAG_KEY, verifyAddressFlag);
         
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.UpdateAddress.toString(), new UpdateAddressImpl());
 	}
 
 	/**
 	 * Add Names RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param nameType contains the name type to be added.
 	 * @param documentType contains the document type if the name type is DOCUMENTED.
 	 * @param firstName contains the first name.
 	 * @param middleNames contains the middle names.
 	 * @param lastName contains the last name.
 	 * @param suffix contains the suffix.
 	 * @param nickname contains the person's nickname.
 	 * @return will return the results to the caller.
 	 */
 	public Response addName(
 			final String identifierType,
 			final String identifier,
 			final String requestedBy,
 			final String nameType,
 			final String documentType,
 			final String firstName,
 			final String middleNames,
 			final String lastName,
 			final String suffix,
 			final String nickname) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(7);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleNames);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(SUFFIX_KEY, suffix);
         otherParameters.put(NICKNAME_KEY, nickname);
 		
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.AddName.toString(), new AddNameImpl());
 	}
 	
 	/**
 	 * Update Names RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param nameType contains the name type to be added.
 	 * @param documentType contains the document type if the name type is DOCUMENTED.
 	 * @param firstName contains the first name.
 	 * @param middleNames contains the middle names.
 	 * @param lastName contains the last name.
 	 * @param suffix contains the suffix.
 	 * @param nickname contains the person's nickname.
 	 * @return will return the results to the caller.
 	 */
 	public Response updateName(
 			final String identifierType,
 			final String identifier,
 			final String requestedBy,
 			final String nameType,
 			final String documentType,
 			final String firstName,
 			final String middleNames,
 			final String lastName,
 			final String suffix,
 			final String nickname) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(7);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(DOCUMENT_TYPE_KEY, documentType);
         otherParameters.put(FIRST_NAME_KEY, firstName);
         otherParameters.put(MIDDLE_NAMES_KEY, middleNames);
         otherParameters.put(LAST_NAME_KEY, lastName);
         otherParameters.put(SUFFIX_KEY, suffix);
         otherParameters.put(NICKNAME_KEY, nickname);
 		
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.AddName.toString(), new AddNameImpl());
 	}
 
 	/**
 	 * Add Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the phone, this person will be an RA agent.
 	 * @param phoneType the type of phone number that is being updated.
 	 * @param phoneNumber the phone number of the update.
 	 * @param extension the extension, if available, of the update.
 	 * @param internationalNumber a flag indicating if the phone number is an international number
 	 * 
 	 * @return will return an HTTP response object.
 	 */
 	public Response addPhone(
 			  final String identifierType,
 			  final String identifier,
 			  final String requestedBy,
 			  final String phoneType,
 			  final String phoneNumber,
 			  final String extension,
 			  final String internationalNumber) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(4);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
         
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.AddPhone.toString(), new AddPhoneImpl());
 	}
 	
 	/**
 	 * Update Phone RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the phone, this person will be an RA agent.
 	 * @param phoneType the type of phone number that is being updated.
 	 * @param groupId the groupId of the phone record within the phone type
 	 * @param phoneNumber the phone number of the update.
 	 * @param extension the extension, if available, of the update.
 	 * @param internationalNumber a flag indicating if the phone number is an international number
 	 * 
 	 * @return will return a RESTful HTTP object.
 	 */
 	public Response updatePhone(
 			final String identifierType,
 			final String identifier,
 			final String requestedBy,
 			final String phoneType,
 			final String groupId,
 			final String phoneNumber,
 			final String extension,
 			final String internationalNumber) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(5);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(GROUP_ID_KEY, Utility.safeConvertStringToLong(groupId));
         otherParameters.put(PHONE_NUMBER_KEY, phoneNumber);
         otherParameters.put(PHONE_EXTENSION_KEY, extension);
         otherParameters.put(PHONE_INTERNATIONAL_NUMBER_KEY, internationalNumber);
 
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.UpdatePhone.toString(), new UpdatePhoneImpl());
 	}
 	
 	/**
 	 * Add Email RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the email address, this person will be an RA agent.
 	 * @param emailAddressType contains the type of email address that is being added.
 	 * @param emailAddress contains the email address
 	 * 
 	 * @return will return an HTTP restful object.
 	 */
 	public Response addEmail(
 			  final String identifierType,
 			  final String identifier,
 			  final String requestedBy,
 			  final String emailAddressType,
 			  final String emailAddress) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailAddressType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
         
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters, 
 				CprServiceName.AddEmailAddress.toString(), new AddEmailAddressImpl());
 	}
 
 	/**
 	 * Update Email RESTful service.
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the email address, this person will be an RA agent.
 	 * @param emailAddressType contains the type of email address that is being added.
 	 * @param emailAddress contains the email address
 	 * 
 	 * @return will return an HTTP restful object.
 	 */
 	public Response updateEmail(
 			  final String identifierType,
 			  final String identifier,
 			  final String requestedBy,
 			  final String emailAddressType,
 			  final String emailAddress) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(EMAIL_ADDRESS_TYPE_KEY, emailAddressType);
         otherParameters.put(EMAIL_ADDRESS_KEY, emailAddress);
         
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.UpdateEmailAddress.toString(), new UpdateEmailAddressImpl());
 	}
 	
 	/**
 	 * Add User Comment RESTful Service
 	 *  
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the user comment, this person will be an RA agent.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * @param comment contains the comment
 	 * 
 	 * @return will return an HTTP response.
 	 */
 	public Response addComment(
 			  final String identifierType,
 			  final String identifier,
 			  final String requestedBy,
 			  final String userId,
 			  final String userCommentType,
 			  final String comment) {
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(USER_COMMENT_TYPE_KEY, userCommentType);
         otherParameters.put(USER_COMMENT_KEY, comment);
 		
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.AddUserComment.toString(), new AddUserCommentImpl());
 	}
 
 	/**
 	 * Update User Comment RESTful Service
 	 * 
 	 * @param identifierType the type of identifier to be used to find the user in the CPR.
 	 * @param identifier the value of the identifier.
 	 * @param requestedBy the person (userid) who is updating the user comment, this person will be an RA agent.
 	 * @param userId contains the userId associated with the comment.
 	 * @param userCommentType contains the type of comment that is being added.
 	 * @param comment contains the comment
 	 * 
 	 * @return will return an HTTP response object.
 	 */
 	public Response updateComment(
 			  final String identifierType,
 			  final String identifier,
 			  final String requestedBy,
 			  final String userId,
 			  final String userCommentType,
 			  final String comment) {
         final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
         otherParameters.put(USERID_KEY, userId);
         otherParameters.put(USER_COMMENT_TYPE_KEY, userCommentType);
         otherParameters.put(USER_COMMENT_KEY, comment);
 		return execWriteApi(identifierType, identifier, requestedBy, otherParameters,
 				CprServiceName.UpdateUserComment.toString(), new UpdateUserCommentImpl());
 	}
 
 	/**
 	 * Get Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param addressType contains the type of address to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getAddress(
 			final String identifierType,
 			final String identifier,
 			final String returnHistory,
 			final String addressType,
 			final String requestedBy) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(ADDRESS_TYPE_KEY, addressType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
 
 		return execGetAddress(identifierType, identifier, requestedBy, otherParameters);	
 
 	}
 
 	/**
 	 * Get Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param addressKey contains the specific address record to retrieve.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getAddressUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String addressKey,
 			final String requestedBy) {
 		
         final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(ADDRESS_KEY, addressKey);
 
 		return execGetAddress(identifierType, identifier, requestedBy, otherParameters);	
 	}
 
 	/**
 	 * Get Email Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getEmail(
 			  final String identifierType,
 			  final String identifier,
 			  final String returnHistory,
 			  final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
         
 		return execGetEmail(identifierType, identifier, requestedBy, otherParameters);
 	}
 	
 	/**
 	 * Get Email Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param emailKey contains the email address key value.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getEmailUsingKey(
 			  final String identifierType,
 			  final String identifier,
 			  final String emailKey,
 			  final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(EMAIL_KEY, emailKey);
         
 		return execGetEmail(identifierType, identifier, requestedBy, otherParameters);
 	}
 	
 	/**
 	 * Get name RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param nameType contains the type of name to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getName(
 			final String identifierType,
 			final String identifier,
 			final String returnHistory,
 			final String nameType,
 			final String requestedBy) {
 
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(NAME_TYPE_KEY, nameType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
         
 		return execGetName(identifierType, identifier, requestedBy, otherParameters);
 	}
 
 	/**
 	 * Get name RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param nameKey contains the specific name key to be returned.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getNameUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String nameKey,
 			final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 		otherParameters.put(NAME_KEY, nameKey);
         
 		return execGetName(identifierType, identifier, requestedBy, otherParameters);	
 	}
 	
 	/**
 	 * Get phone RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param phoneType contains the type of name to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getPhone(
 			final String identifierType,
 			final String identifier,
 			final String returnHistory,
 			final String phoneType,
 			final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
         otherParameters.put(PHONE_TYPE_KEY, phoneType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
         
 		return execGetPhone(identifierType, identifier, requestedBy, otherParameters);
 	}
 	
 	/**
 	 * Get phone RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param phoneKey contains the specific phone key to be returned.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getPhoneUsingKey(
 			final String identifierType,
 			final String identifier,
 			final String phoneKey,
 			final String requestedBy) {
 		
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 		otherParameters.put(PHONE_KEY, phoneKey);
         
 		return execGetPhone(identifierType, identifier, requestedBy, otherParameters);	
 	}
 
 	/**
 	 * Get comment RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param userid contains the userid to be returned.
 	 * @param commentType contains the type of comment to query..
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getComment(
 			  final String identifierType,
 			  final String identifier,
 			  final String userid,
 			  final String returnHistory,
 			  final String commentType,
 			  final String requestedBy) {
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(3);
 		otherParameters.put(USERID_KEY, userid);
         otherParameters.put(USER_COMMENT_TYPE_KEY, commentType);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
         
 		return execGetComment(identifierType, identifier, requestedBy, otherParameters);
 	}
 	
 	/**
 	 * Get comment RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param commentKey contains the specific comment key to be returned.
 	 * @param userid contains the userid to be returned.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getCommentUsingKey(
 			  final String identifierType,
 			  final String identifier,
 			  final String userid,
 			  final String commentKey,
 			  final String requestedBy) {
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(2);
 		otherParameters.put(USERID_KEY, userid);
 		otherParameters.put(COMMENT_KEY, commentKey);
         
 		return execGetComment(identifierType, identifier, requestedBy, otherParameters);	
 	}
 
 	/**
 	 * Get userid RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getUserid(
 			  final String identifierType,
 			  final String identifier,
 			  final String returnHistory,
 			  final String requestedBy) {
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
         
 		return execGetUserid(identifierType, identifier, requestedBy, otherParameters);
 	}
 	
 	/**
 	 * Get userid RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param userid contains the specific userid to query.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getUseridUsingUserid(
 			  final String identifierType,
 			  final String identifier,
 			  final String userid,
 			  final String requestedBy) {
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
 		otherParameters.put(USERID_KEY, userid);
         
 		return execGetUserid(identifierType, identifier, requestedBy, otherParameters);	
 	}
 
 	/**
 	 * Get person RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param returnHistory contains a Y/N flag that indicates whether to return history or not.
 	 * @param requestedBy contains the requestor.
 	 * @return will return the results to the caller.
 	 */
 	public Response getPerson(
 			  final String identifierType,
 			  final String identifier,
 			  final String returnHistory,
 			  final String requestedBy) {
 		final Map<String, Object> otherParameters = new HashMap<String, Object>(1);
         otherParameters.put(RETURN_HISTORY_KEY, returnHistory);
         
 		return execGetPerson(identifierType, identifier, requestedBy, otherParameters);
 	}
 
 	/**
 	 * This method provides the actual implementation of an write API.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to the implementation.
 	 * @param serviceName contains the name of the service to be executed.
 	 * @param impl contains the implementation to be called.
 	 * @return will return the results to the caller.
 	 */
 	private Response execWriteApi(final String identifierType, 
 			final String identifier,
 			final String requestedBy, 
 			final Map<String, Object> otherParameters, 
 			final String serviceName,
 			final BaseServiceImpl impl) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
         
         final ServiceReturn serviceReturn = (ServiceReturn) impl.implementService(
                 						serviceName, 
                                         request.getRemoteAddr(), 
                                         userPrincipal.getName(), 
                                         userPrincipal.getPassword(), 
                                         requestedBy, 
                                         identifierType,
                                         identifier, 
                                         otherParameters);	
         
         final int httpStatusCode = Utility.convertCprReturnToHttpStatus(serviceReturn.getStatusCode());
                 
         if (serviceReturn.getRecordKey() != null) {
         	final String path = uriInfo.getPath();
         	serviceReturn.setUri(Utility.constructUri(path, serviceReturn.getRecordKey().toString()));
         }
 
         return Response.status(httpStatusCode).entity(
         		new WriteApiReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), serviceReturn.getStatusMessage()), 
         		new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
         		serviceReturn)).build();
 	}
 	
 	/**
 	 * This method provides the actual implementation of the Get Name RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to GetName implementation.
 	 * @return will return the results to the caller.
 	 */
 	private Response execGetName(final String identifierType, final String identifier,
 			final String requestedBy, final Map<String, Object> otherParameters) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
         final GetNameImpl impl = new GetNameImpl();
         final String nameKey = (String) otherParameters.get(NAME_KEY);
         final NamesServiceReturn namesServiceReturn = (NamesServiceReturn) impl.implementService(
                                         CprServiceName.GetName.toString(), 
                                         request.getRemoteAddr(), 
                                         userPrincipal.getName(), 
                                         userPrincipal.getPassword(), 
                                         requestedBy, 
                                         identifierType,
                                         identifier, 
                                         otherParameters);	
         
         final int httpStatusCode = Utility.convertCprReturnToHttpStatus(namesServiceReturn.getStatusCode());
         
         final String path = uriInfo.getPath();
         if (namesServiceReturn.getNamesReturnRecord() != null) {
         	if (nameKey == null) {
         		for (NameReturn n: namesServiceReturn.getNamesReturnRecord()) {
         			n.setUri(Utility.constructUri(path, n.getNameKey()));
         		}
         	}
         	else {
         		for (NameReturn n: namesServiceReturn.getNamesReturnRecord()) {
         			n.setUri(path);
         		}
         	}
         }
         
         return Response.status(httpStatusCode).entity(
         		new GetNameReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), namesServiceReturn.getStatusMessage()), 
         		new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
         		namesServiceReturn)).build();
 	}
 	
 	/**
 	 * This method provides the actual implementation of the Get Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to GetName implementation.
 	 * @return will return the results to the caller.
 	 */
 	private Response execGetAddress(final String identifierType, final String identifier,
 			final String requestedBy, final Map<String, Object> otherParameters) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final GetAddressImpl impl = new GetAddressImpl();
 		final String addressKey = (String) otherParameters.get(ADDRESS_KEY);
 		
 		final AddressServiceReturn addressServiceReturn = (AddressServiceReturn) impl.implementService(
 				CprServiceName.GetAddress.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				identifierType,
 				identifier, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(addressServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		if (addressServiceReturn.getAddressReturnRecord() != null) {
 			if (addressKey == null) {
 				for (AddressReturn n: addressServiceReturn.getAddressReturnRecord()) {
 					n.setUri(Utility.constructUri(path, n.getAddressKey()));
 				}
 			}
 			else {
 				for (AddressReturn n: addressServiceReturn.getAddressReturnRecord()) {
 					n.setUri(path);
 				}			
 			}
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new GetAddressReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), addressServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						addressServiceReturn)).build();
 	}
 	
 	/**
 	 * This method provides the actual implementation of the Get Email Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to GetEmail implementation.
 	 * @return will return the results to the caller.
 	 */
 	private Response execGetEmail(final String identifierType, final String identifier,
 			final String requestedBy, final Map<String, Object> otherParameters) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final GetEmailAddressImpl impl = new GetEmailAddressImpl();
 		final String emailKey = (String) otherParameters.get(EMAIL_KEY);
 		
 		final EmailAddressServiceReturn addressServiceReturn = (EmailAddressServiceReturn) impl.implementService(
 				CprServiceName.GetEmailAddress.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				identifierType,
 				identifier, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(addressServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		if (addressServiceReturn.getEmailAddressReturnRecord() != null) {
 			if (emailKey == null) {
 				for (EmailAddressReturn n: addressServiceReturn.getEmailAddressReturnRecord()) {
 					n.setUri(Utility.constructUri(path, n.getEmailKey()));
 				}
 			}
 			else {
 				for (EmailAddressReturn n: addressServiceReturn.getEmailAddressReturnRecord()) {
 					n.setUri(path);
 				}			
 			}
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new GetEmailReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), addressServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						addressServiceReturn)).build();
 	}
 	
 	/**
 	 * This method provides the actual implementation of the Get Phone Address RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to GetEmail implementation.
 	 * @return will return the results to the caller.
 	 */
 	private Response execGetPhone(final String identifierType, final String identifier,
 			final String requestedBy, final Map<String, Object> otherParameters) {
 		final HttpServletRequest request =  messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final GetPhoneImpl impl = new GetPhoneImpl();
 		final String phoneKey = (String) otherParameters.get(PHONE_KEY);
 		
 		final PhoneServiceReturn phoneServiceReturn = (PhoneServiceReturn) impl.implementService(
 				CprServiceName.GetPhone.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				identifierType,
 				identifier, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(phoneServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		if (phoneServiceReturn.getPhoneReturnRecord() != null) {
 			if (phoneKey == null) {
 				for (PhoneReturn n: phoneServiceReturn.getPhoneReturnRecord()) {
 					n.setUri(Utility.constructUri(path, n.getPhoneKey()));
 				}
 			}
 			else {
 				for (PhoneReturn n: phoneServiceReturn.getPhoneReturnRecord()) {
 					n.setUri(path);
 				}			
 			}
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new GetPhoneReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), phoneServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						phoneServiceReturn)).build();
 	}
 	
 	/**
 	 * This method provides the actual implementation of the Get Comment RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to GetEmail implementation.
 	 * @return will return the results to the caller.
 	 */
 	private Response execGetComment(final String identifierType, final String identifier,
 			final String requestedBy, final Map<String, Object> otherParameters) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final GetUserCommentsImpl impl = new GetUserCommentsImpl();
 		final String commentKey = (String) otherParameters.get(COMMENT_KEY);
 		
 		final UserCommentServiceReturn commentServiceReturn = (UserCommentServiceReturn) impl.implementService(
 				CprServiceName.GetUserComments.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				identifierType,
 				identifier, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(commentServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		if (commentServiceReturn.getUserCommentReturnRecord() != null) {
 			if (commentKey == null) {
 				for (UserCommentReturn n: commentServiceReturn.getUserCommentReturnRecord()) {
 					n.setUri(Utility.constructUri(path, n.getCommentKey()));
 				}
 			}
 			else {
 				for (UserCommentReturn n: commentServiceReturn.getUserCommentReturnRecord()) {
 					n.setUri(path);
 				}			
 			}
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new GetCommentReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), commentServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						commentServiceReturn)).build();
 	}
 
 	/**
 	 * This method provides the actual implementation of the Get Comment RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to GetEmail implementation.
 	 * @return will return the results to the caller.
 	 */
 	private Response execGetUserid(final String identifierType, final String identifier,
 			final String requestedBy, final Map<String, Object> otherParameters) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final GetUseridImpl impl = new GetUseridImpl();
 		final String userid = (String) otherParameters.get(USERID_KEY);
 		
 		final UseridServiceReturn useridServiceReturn = (UseridServiceReturn) impl.implementService(
 				CprServiceName.GetUserid.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				identifierType,
 				identifier, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(useridServiceReturn.getStatusCode());
 
 		final String path = uriInfo.getPath();
 		if (useridServiceReturn.getUseridReturnRecord() != null) {
 			if (userid == null) {
 				for (UseridReturn n: useridServiceReturn.getUseridReturnRecord()) {
 					n.setUri(Utility.constructUri(path, n.getUserid()));
 				}
 			}
 			else {
 				for (UseridReturn n: useridServiceReturn.getUseridReturnRecord()) {
 					n.setUri(path);
 				}			
 			}
 		}
 
 		return Response.status(httpStatusCode).entity(
 				new GetUseridReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), useridServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						useridServiceReturn)).build();
 	}
 	
 	/**
 	 * This method provides the actual implementation of the Get Person RESTful service.
 	 * @param identifierType contains the type of identifier.
 	 * @param identifier contains the value of the identifier.
 	 * @param requestedBy contains the requestor.
 	 * @param otherParameters contains the parameters that are passed to GetEmail implementation.
 	 * @return will return the results to the caller.
 	 */
 	private Response execGetPerson(final String identifierType, final String identifier,
 			final String requestedBy, final Map<String, Object> otherParameters) {
 		
 		final HttpServletRequest request = messageContext.getHttpServletRequest();
 		final UserPrincipal userPrincipal = (UserPrincipal) messageContext.getSecurityContext().getUserPrincipal();
 		final GetPersonImpl impl = new GetPersonImpl();
 		
 		final PersonServiceReturn personServiceReturn = (PersonServiceReturn) impl.implementService(
 				CprServiceName.GetPerson.toString(), 
 				request.getRemoteAddr(), 
 				userPrincipal.getName(), 
 				userPrincipal.getPassword(), 
 				requestedBy, 
 				identifierType,
 				identifier, 
 				otherParameters);	
 
 		final int httpStatusCode = Utility.convertCprReturnToHttpStatus(personServiceReturn.getStatusCode());
 
 		return Response.status(httpStatusCode).entity(
 				new GetPersonReturn(new ResourceMetadata(null, uriInfo.getBaseUri().toString(), personServiceReturn.getStatusMessage()), 
 						new ResponseMeta(impl.getResponseTimestamp(), impl.getElapsedTime(), null, httpStatusCode, SERVER_VERSION), 
 						personServiceReturn)).build();
 	}
 
 }
